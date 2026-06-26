package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.config.BloomFilterHelper;
import com.ghostfire.dto.RedPacketDto;
import com.ghostfire.entity.RedPacket;
import com.ghostfire.entity.RedPacketRecord;
import com.ghostfire.mapper.RedPacketMapper;
import com.ghostfire.mapper.RedPacketRecordMapper;
import com.ghostfire.service.MedalService;
import com.ghostfire.service.RedPacketService;
import com.ghostfire.service.UserStatService;
import com.ghostfire.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import static com.ghostfire.common.Constant.*;

@Service
@RequiredArgsConstructor
public class RedPacketServiceImpl extends ServiceImpl<RedPacketMapper, RedPacket> implements RedPacketService {

    private final UserStatService userStatService;
    private final MedalService medalService;
    private final RedPacketRecordMapper redPacketRecordMapper;
    private final BloomFilterHelper bloomFilter;
    private final WalletService walletService;

    @Override
    @Transactional
    public RedPacket create(Long userId, RedPacketDto dto) {
        if (dto.getTotalAmount() < dto.getTotalCount()) {
            throw new RuntimeException("红包总金额不能小于红包个数");
        }
        RedPacket redPacket = new RedPacket();
        redPacket.setUserId(userId);
        redPacket.setPostId(dto.getPostId());
        redPacket.setTotalAmount(dto.getTotalAmount());
        redPacket.setTotalCount(dto.getTotalCount());
        redPacket.setType(dto.getType());
        redPacket.setRemainCount(dto.getTotalCount());
        redPacket.setStatus(RED_PACKET_ACTIVE);
        redPacket.setExpireTime(LocalDateTime.now().plusHours(24));
        save(redPacket);

        // 扣余额 + 写钱包流水
        walletService.changeCoin(userId, -redPacket.getTotalAmount(), WALLET_RED_PACKET_SEND, redPacket.getId(),
                "RED_PACKET_SEND:" + redPacket.getId(), "发送红包");

        return redPacket;
    }

    @Override
    @Transactional
    public RedPacketRecord grab(Long userId, Long packetId) {
        // SELECT FOR UPDATE 锁定红包行，防止并发超抢
        RedPacket packet = getBaseMapper().selectOne(
                new LambdaQueryWrapper<RedPacket>()
                        .eq(RedPacket::getId, packetId)
                        .last("FOR UPDATE"));
        if (packet == null) {
            throw new RuntimeException("红包不存在");
        }
        if (packet.getStatus() != RED_PACKET_ACTIVE
                || LocalDateTime.now().isAfter(packet.getExpireTime())
                || packet.getRemainCount() <= 0) {
            throw new RuntimeException("红包已抢完或已过期");
        }

        // 布隆过滤器快速排除未抢过的人（false = 肯定没抢过）
        String bloomKey = "bloom:grab:" + packetId;
        String bloomVal = String.valueOf(userId);
        if (bloomFilter.mightContain(bloomKey, bloomVal)) {
            Long count = redPacketRecordMapper.selectCount(
                    new LambdaQueryWrapper<RedPacketRecord>()
                            .eq(RedPacketRecord::getPacketId, packetId)
                            .eq(RedPacketRecord::getUserId, userId));
            if (count > 0) {
                throw new RuntimeException("你已经抢过这个红包了");
            }
        }

        // 在行锁保护下计算金额（此时不会有并发修改 remainCount）
        long grabbedAmount = getGrabbedAmount(packetId);
        long amount;
        if (packet.getRemainCount() == 1) {
            amount = packet.getTotalAmount() - grabbedAmount;
        } else if (packet.getType() == RED_PACKET_AVERAGE) {
            amount = packet.getTotalAmount() / packet.getTotalCount();
        } else {
            long remainAmount = packet.getTotalAmount() - grabbedAmount;
            long minReserved = packet.getRemainCount() - 1L;
            long max = Math.min(remainAmount - minReserved,
                    remainAmount / packet.getRemainCount() * 2);
            if (max < 1) {
                amount = 1;
            } else {
                amount = ThreadLocalRandom.current().nextLong(1, max + 1);
            }
        }

        // 原子扣减剩余个数
        int rows = getBaseMapper().update(null,
                new LambdaUpdateWrapper<RedPacket>()
                        .eq(RedPacket::getId, packetId)
                        .gt(RedPacket::getRemainCount, 0)
                        .setSql("remain_count = remain_count - 1"));
        if (rows == 0) {
            throw new RuntimeException("红包已抢完");
        }

        // 写抢红包记录（UNIQUE(packet_id, user_id) 防重）
        RedPacketRecord record = new RedPacketRecord();
        record.setPacketId(packetId);
        record.setUserId(userId);
        record.setAmount(amount);
        try {
            redPacketRecordMapper.insert(record);
            bloomFilter.add(bloomKey, bloomVal);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("你已经抢过这个红包了");
        }

        // 抢完了改状态
        if (packet.getRemainCount() - 1 == 0) {
            getBaseMapper().update(null,
                    new LambdaUpdateWrapper<RedPacket>()
                            .eq(RedPacket::getId, packetId)
                            .set(RedPacket::getStatus, RED_PACKET_FINISHED));
        }

        // 加金币 + 写流水
        walletService.changeCoin(userId, amount, WALLET_RED_PACKET_RECEIVE, packetId,
                "RED_PACKET_RECEIVE:" + packetId + ":" + userId, "抢到红包");
        medalService.checkAutoAward(userId);

        return record;
    }

    /** 已被抢走的总金额 */
    private long getGrabbedAmount(Long packetId) {
        return redPacketRecordMapper.selectList(
                        new LambdaQueryWrapper<RedPacketRecord>()
                                .eq(RedPacketRecord::getPacketId, packetId))
                .stream().mapToLong(RedPacketRecord::getAmount).sum();
    }
}
