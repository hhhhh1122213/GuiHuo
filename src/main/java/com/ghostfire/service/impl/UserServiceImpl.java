package com.ghostfire.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.secure.SaSecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.common.Constant;
import com.ghostfire.dto.RegisterDto;
import com.ghostfire.entity.User;
import com.ghostfire.entity.UserStat;
import com.ghostfire.mapper.UserMapper;
import com.ghostfire.service.UserService;
import com.ghostfire.service.UserStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserStatService userStatService;

    @Override
    @Transactional
    public User register(RegisterDto dto) {
        User exist = getByUsername(dto.getUsername());
        if (exist != null) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        user.setNickname(dto.getUsername());
        user.setRole("USER");
        user.setStatus(Constant.USER_STATUS_NORMAL);
        save(user);

        UserStat stat = new UserStat();
        stat.setUserId(user.getId());
        stat.setCoin(0L);
        stat.setPostCount(0);
        stat.setLikeCount(0);
        stat.setSignCount(0);
        userStatService.save(stat);

        return user;
    }

    @Override
    public User login(String username, String password) {
        User user = getByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getStatus() == Constant.USER_STATUS_BANNED) {
            throw new RuntimeException("账号已被禁用");
        }
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }

    @Override
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public User changePassword(long id, String oldPassword, String newPassword) {
        User user = getById(id);
        if(user == null){
            throw new RuntimeException("用户不存在");
        }
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        user.setPassword(BCrypt.hashpw(newPassword,BCrypt.gensalt()));
        save(user);
        return user;
    }

}
