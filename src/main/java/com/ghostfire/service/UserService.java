package com.ghostfire.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghostfire.entity.User;
import com.ghostfire.dto.RegisterDto;

public interface UserService extends IService<User> {

    User register(RegisterDto dto);

    User login(String username, String password);

    User getByUsername(String username);
}
