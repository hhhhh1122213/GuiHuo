package com.ghostfire.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghostfire.entity.UserStat;

public interface UserStatService extends IService<UserStat> {

    /**
     * 原子更新金币并写钱包流水。
     *
     * @deprecated 请使用 {@link WalletService#changeCoin} 替代，支持幂等 bizKey 和完整审计字段
     */
    @Deprecated
    void addCoin(Long userId, long amount, String type, Long refId);
}
