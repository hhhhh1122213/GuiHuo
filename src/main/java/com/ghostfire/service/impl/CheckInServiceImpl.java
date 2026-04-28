package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.entity.UserCheckIn;
import com.ghostfire.mapper.UserCheckInMapper;
import com.ghostfire.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CheckInServiceImpl extends ServiceImpl<UserCheckInMapper, UserCheckIn> implements CheckInService {

    @Override
    public UserCheckIn checkIn(Long userId) {
        LocalDate today = LocalDate.now();
        long count = count(new LambdaQueryWrapper<UserCheckIn>()
                .eq(UserCheckIn::getUserId, userId)
                .eq(UserCheckIn::getCheckDate, today));
        if (count > 0) {
            throw new RuntimeException("今天已签到");
        }
        UserCheckIn checkIn = new UserCheckIn();
        checkIn.setUserId(userId);
        checkIn.setCheckDate(today);
        try {
            save(checkIn);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("今天已签到");
        }
        return checkIn;
    }
}
