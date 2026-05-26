package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.config.RabbitConfig;
import com.ghostfire.entity.UserStat;
import com.ghostfire.mapper.UserStatMapper;
import com.ghostfire.service.RankingService;
import com.ghostfire.service.UserStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserStatServiceImpl extends ServiceImpl<UserStatMapper, UserStat> implements UserStatService {

    private final RabbitTemplate rabbitTemplate;
    private final RankingService rankingService;

    @Override
    public void addCoin(Long userId, long amount, String type, Long refId) {
        ensureUserStat(userId);
        // 1. SQL 原子更新金币，扣币时加余额守护
        LambdaUpdateWrapper<UserStat> wrapper = new LambdaUpdateWrapper<UserStat>()
                .eq(UserStat::getUserId, userId)
                .setSql("coin = coin + {0}", amount);
        if (amount < 0) {
            wrapper.ge(UserStat::getCoin, -amount);
        }
        int rows = getBaseMapper().update(null, wrapper);
        if (amount < 0 && rows == 0) {
            throw new RuntimeException("金币不够啊！靓仔");
        }
        // 2. 事务提交后再发 MQ 写流水，防止回滚导致幽灵日志
        Map<String, Object> msg = new HashMap<>();
        msg.put("userId", userId);
        msg.put("amount", amount);
        msg.put("type", type);
        msg.put("refId", refId);
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.WALLET_LOG_KEY, msg);
                }
            });
        } else {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.WALLET_LOG_KEY, msg);
        }
        // 3. 更新金币排行榜
        UserStat stat = getById(userId);
        if (stat != null) {
            rankingService.updateScore(RankingService.RANK_COIN, userId, stat.getCoin());
        }
    }

    private void ensureUserStat(Long userId) {
        if (getById(userId) != null) {
            return;
        }
        UserStat stat = new UserStat();
        stat.setUserId(userId);
        stat.setCoin(0L);
        stat.setPostCount(0);
        stat.setLikeCount(0);
        stat.setSignCount(0);
        stat.setStreakCount(0);
        try {
            getBaseMapper().insert(stat);
        } catch (DuplicateKeyException ignored) {
            // concurrent insert by another transaction
        }
    }
}
