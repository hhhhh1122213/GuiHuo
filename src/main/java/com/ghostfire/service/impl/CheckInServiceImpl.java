package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.common.Constant;
import com.ghostfire.entity.UserCheckIn;
import com.ghostfire.entity.UserStat;
import com.ghostfire.entity.UserWalletLog;
import com.ghostfire.mapper.UserCheckInMapper;
import com.ghostfire.service.CheckInService;
import com.ghostfire.service.UserStatService;
import com.ghostfire.service.UserWalletLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CheckInServiceImpl extends ServiceImpl<UserCheckInMapper, UserCheckIn> implements CheckInService {

    private final UserStatService userStatService;
    private final UserWalletLogService userWalletLogService;

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
        // 原子更新用户金币和签到数
        userStatService.getBaseMapper().update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<UserStat>()
                        .eq(UserStat::getUserId, userId)
                        .setSql("coin = coin + 10")
                        .setSql("sign_count = sign_count + 1"));
        // 读取更新后的余额写钱包流水
        UserStat userStat = userStatService.getById(userId);
        UserWalletLog walletLog = new UserWalletLog();
        walletLog.setUserId(userId);
        walletLog.setAmount(10L);
        walletLog.setCurrentBalance(userStat != null ? userStat.getCoin() : 10L);
        walletLog.setType(Constant.WALLET_SIGN_IN);
        userWalletLogService.save(walletLog);
        return checkIn;
    }
}
