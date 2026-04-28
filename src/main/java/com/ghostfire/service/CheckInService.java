package com.ghostfire.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghostfire.entity.UserCheckIn;

public interface CheckInService extends IService<UserCheckIn> {

    UserCheckIn checkIn(Long userId);
}
