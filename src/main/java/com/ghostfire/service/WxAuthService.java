package com.ghostfire.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ghostfire.entity.User;
import com.ghostfire.entity.UserStat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WxAuthService {

    private final UserService userService;
    private final UserStatService userStatService;
    private final WxService wxService;

    /**
     * 微信一键登录：code → openid → 查找/创建用户 → Sa-Token 登录
     * @return token 信息
     */
    @Transactional
    public Object wxLogin(String code) {
        String openid = wxService.getOpenid(code);

        // 查找已有微信用户
        User user = userService.getOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));

        if (user == null) {
            // 创建新用户
            user = new User();
            user.setUsername("wx_" + openid.substring(0, 8));
            user.setPassword(""); // 微信用户无密码
            user.setNickname("微信用户");
            user.setAvatar("");
            user.setRole("user");
            user.setStatus(1);
            user.setSource("wechat");
            user.setOpenid(openid);
            userService.save(user);

            // 创建 UserStat 记录
            UserStat stat = new UserStat();
            stat.setUserId(user.getId());
            stat.setCoin(0L);
            stat.setPostCount(0);
            stat.setLikeCount(0);
            stat.setSignCount(0);
            stat.setStreakCount(0);
            try {
                userStatService.save(stat);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 忽略并发创建
            }
        }

        // Sa-Token 登录
        StpUtil.login(user.getId());
        return StpUtil.getTokenInfo();
    }
}
