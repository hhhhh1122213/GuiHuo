package com.ghostfire.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghostfire.entity.UserMedal;
import java.util.List;

public interface UserMedalService extends IService<UserMedal> {

    boolean hasMedal(Long userId, Long medalId);

    void award(Long userId, Long medalId);

    List<UserMedal> listByUserId(Long userId);
}
