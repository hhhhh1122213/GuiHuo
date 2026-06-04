package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.common.Constant;
import com.ghostfire.entity.Medal;
import com.ghostfire.entity.UserMedal;
import com.ghostfire.entity.UserStat;
import com.ghostfire.mapper.MedalMapper;
import com.ghostfire.service.MedalService;
import com.ghostfire.service.UserMedalService;
import com.ghostfire.service.UserStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedalServiceImpl extends ServiceImpl<MedalMapper, Medal> implements MedalService {

    private final UserMedalService userMedalService;
    private final UserStatService userStatService;

    @Override
    public List<Medal> listAll() {
        return list();
    }

    @Override
    public List<Medal> listUserMedals(Long userId) {
        List<UserMedal> userMedals = userMedalService.listByUserId(userId);
        if (userMedals.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> medalIds = userMedals.stream()
                .map(UserMedal::getMedalId)
                .collect(Collectors.toList());
        return list(new LambdaQueryWrapper<Medal>().in(Medal::getId, medalIds));
    }

    @Override
    public void awardManual(Long userId, Long medalId) {
        Medal medal = getById(medalId);
        if (medal == null) {
            throw new RuntimeException("勋章不存在");
        }
        userMedalService.award(userId, medalId);
    }

    @Override
    public void checkAutoAward(Long userId) {
        // 查所有 AUTO_STAT 类勋章
        List<Medal> autoMedals = list(new LambdaQueryWrapper<Medal>()
                .eq(Medal::getRuleType, Constant.MEDAL_AUTO_STAT));
        if (autoMedals.isEmpty()) {
            return;
        }
        // 查用户已获得的勋章 ID
        List<UserMedal> userMedals = userMedalService.listByUserId(userId);
        Set<Long> ownedIds = userMedals.stream()
                .map(UserMedal::getMedalId)
                .collect(Collectors.toSet());
        // 查用户统计
        UserStat stat = userStatService.getById(userId);
        if (stat == null) {
            return;
        }
        // 逐个检测
        for (Medal medal : autoMedals) {
            if (ownedIds.contains(medal.getId())) {
                continue;
            }
            if (medal.getRuleField() == null || medal.getRuleValue() == null) {
                continue;
            }
            long current = getStatValue(stat, medal.getRuleField());
            if (current >= medal.getRuleValue()) {
                userMedalService.award(userId, medal.getId());
            }
        }
    }

    private long getStatValue(UserStat stat, String field) {
        return switch (field) {
            case "postCount" -> stat.getPostCount() != null ? stat.getPostCount() : 0;
            case "likeCount" -> stat.getLikeCount() != null ? stat.getLikeCount() : 0;
            case "coin" -> stat.getCoin() != null ? stat.getCoin() : 0;
            case "signCount" -> stat.getSignCount() != null ? stat.getSignCount() : 0;
            default -> 0;
        };
    }
}
