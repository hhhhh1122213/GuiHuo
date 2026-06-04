package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.entity.UserWalletLog;
import com.ghostfire.mapper.UserWalletLogMapper;
import com.ghostfire.service.UserWalletLogService;
import org.springframework.stereotype.Service;

@Service
public class UserWalletLogServiceImpl extends ServiceImpl<UserWalletLogMapper, UserWalletLog> implements UserWalletLogService {

    @Override
    public Page<UserWalletLog> pageByUserId(Long userId, int page, int size) {
        Page<UserWalletLog> p = new Page<>(page, size);
        LambdaQueryWrapper<UserWalletLog> w = new LambdaQueryWrapper<UserWalletLog>()
                .eq(UserWalletLog::getUserId, userId)
                .orderByDesc(UserWalletLog::getCreateTime);
        return page(p, w);
    }
}
