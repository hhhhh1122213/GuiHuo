package com.ghostfire.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ghostfire.entity.UserWalletLog;

public interface UserWalletLogService extends IService<UserWalletLog> {

    Page<UserWalletLog> pageByUserId(Long userId, int page, int size);
}
