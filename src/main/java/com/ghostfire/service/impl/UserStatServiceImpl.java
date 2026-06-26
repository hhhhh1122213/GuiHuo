package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.entity.UserStat;
import com.ghostfire.mapper.UserStatMapper;
import com.ghostfire.service.UserStatService;
import com.ghostfire.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserStatServiceImpl extends ServiceImpl<UserStatMapper, UserStat> implements UserStatService {

    private final WalletService walletService;

    @Override
    @Deprecated
    @Transactional
    public void addCoin(Long userId, long amount, String type, Long refId) {
        walletService.changeCoin(userId, amount, type, refId, null, null);
    }
}
