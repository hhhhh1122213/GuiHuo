package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.entity.UserStat;
import com.ghostfire.mapper.UserStatMapper;
import com.ghostfire.service.UserStatService;
import org.springframework.stereotype.Service;

@Service
public class UserStatServiceImpl extends ServiceImpl<UserStatMapper, UserStat> implements UserStatService {
}
