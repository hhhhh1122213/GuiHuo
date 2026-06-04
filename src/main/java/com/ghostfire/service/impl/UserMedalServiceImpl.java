package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.entity.UserMedal;
import com.ghostfire.mapper.UserMedalMapper;
import com.ghostfire.service.UserMedalService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserMedalServiceImpl extends ServiceImpl<UserMedalMapper, UserMedal> implements UserMedalService {

    @Override
    public boolean hasMedal(Long userId, Long medalId) {
        return count(new LambdaQueryWrapper<UserMedal>()
                .eq(UserMedal::getUserId, userId)
                .eq(UserMedal::getMedalId, medalId)) > 0;
    }

    @Override
    @Transactional
    public void award(Long userId, Long medalId) {
        if (hasMedal(userId, medalId)) {
            return;
        }
        UserMedal um = new UserMedal();
        um.setUserId(userId);
        um.setMedalId(medalId);
        try {
            save(um);
        } catch (DuplicateKeyException e) {
            // 并发授予时忽略
        }
    }

    @Override
    public List<UserMedal> listByUserId(Long userId) {
        return list(new LambdaQueryWrapper<UserMedal>()
                .eq(UserMedal::getUserId, userId));
    }
}
