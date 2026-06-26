package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.common.Constant;
import com.ghostfire.entity.UserCheckIn;
import com.ghostfire.entity.UserStat;
import com.ghostfire.mapper.UserCheckInMapper;
import com.ghostfire.service.CheckInService;
import com.ghostfire.service.MedalService;
import com.ghostfire.service.UserStatService;
import com.ghostfire.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CheckInServiceImpl extends ServiceImpl<UserCheckInMapper, UserCheckIn> implements CheckInService {

    private final UserStatService userStatService;
    private final MedalService medalService;
    private final WalletService walletService;

    @Override
    @Transactional
    public UserCheckIn checkIn(Long userId) {
        LocalDate today = LocalDate.now();
        UserCheckIn checkIn = new UserCheckIn();
        checkIn.setUserId(userId);
        checkIn.setCheckDate(today);
        try {
            save(checkIn);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("今天已签到");
        }
        // 加金币 + 写流水 + 更新签到数
        walletService.changeCoin(userId, Constant.COIN_CHECKIN_REWARD, Constant.WALLET_SIGN_IN, checkIn.getId(),
                "SIGN_IN:" + checkIn.getId(), "签到奖励");
        userStatService.getBaseMapper().update(null,
                new LambdaUpdateWrapper<UserStat>()
                        .eq(UserStat::getUserId, userId)
                        .setSql("sign_count = sign_count + 1"));
        // 连续签到天数
        boolean yesterdayCheckedIn = baseMapper.existsByUserAndDate(userId, today.minusDays(1));
        userStatService.getBaseMapper().update(null,
                new LambdaUpdateWrapper<UserStat>()
                        .eq(UserStat::getUserId, userId)
                        .setSql(yesterdayCheckedIn ? "streak_count = streak_count + 1" : "streak_count = 1"));
        medalService.checkAutoAward(userId);
        return checkIn;
    }
}
