package com.ghostfire.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghostfire.common.Result;
import com.ghostfire.config.RateLimit;
import com.ghostfire.dto.RedPacketDto;
import com.ghostfire.entity.RedPacketRecord;
import com.ghostfire.entity.UserStat;
import com.ghostfire.service.RedPacketRecordService;
import com.ghostfire.service.RedPacketService;
import com.ghostfire.service.UserStatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.ghostfire.entity.RedPacket;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/redpacket")
@RequiredArgsConstructor
public class RedpacketController {
    private final RedPacketService redpacketService;
    private final RedPacketRecordService redPacketRecordService;
    private final UserStatService userStatService;
    @PostMapping("/create")
    public Result<?> create(@Valid @RequestBody RedPacketDto dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        UserStat user = userStatService.getById(userId);
        if (user == null || user.getCoin() < dto.getTotalAmount()){
            return Result.fail("余额不足,赶紧给我充钱");
        }
        RedPacket packet = redpacketService.create(userId, dto);
        return Result.ok(packet);
    }
    @RateLimit(key = "grab")
    @PostMapping("/grab/{packetId}")
    public Result<?> grab(@PathVariable Long packetId) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            RedPacketRecord record = redpacketService.grab(userId, packetId);
            return Result.ok(record);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }
    @GetMapping("/{packetId}")
    public Result<RedPacket> detail(@PathVariable Long packetId) {
        RedPacket redPacket = redpacketService.getById(packetId);
        if (redPacket == null) {
            return Result.fail("红包不存在");
        }
        return Result.ok(redPacket);
    }

    @GetMapping("/records")
    public Result<Page<RedPacketRecord>> records(@RequestParam(defaultValue = "1") Integer current,
                                                 @RequestParam(defaultValue = "10") Integer size,
                                                 @RequestParam Long packetId) {
        Page<RedPacketRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<RedPacketRecord> wrapper = new LambdaQueryWrapper<RedPacketRecord>()
                .eq(RedPacketRecord::getPacketId, packetId)
                .orderByDesc(RedPacketRecord::getCreateTime);

        Page<RedPacketRecord> result = redPacketRecordService.page(page, wrapper);
        return Result.ok(result);
    }

}
