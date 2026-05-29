package com.ghostfire.controller.mini;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghostfire.common.Result;
import com.ghostfire.entity.*;
import com.ghostfire.service.*;
import com.ghostfire.vo.PostMapper;
import com.ghostfire.vo.PostSummaryVO;
import com.ghostfire.vo.UserProfileVO;
import com.ghostfire.vo.VoEnricher;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mini/user")
@RequiredArgsConstructor
public class MiniUserController {

    private final UserService userService;
    private final UserStatService userStatService;
    private final UserMedalService userMedalService;
    private final MedalService medalService;
    private final UserFollowService userFollowService;
    private final PostService postService;
    private final PostMapper postMapper;
    private final VoEnricher voEnricher;

    @GetMapping("/{id}")
    public Result<?> profile(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        UserStat stat = userStatService.getById(id);

        List<UserMedal> userMedals = userMedalService.list(
                new LambdaQueryWrapper<UserMedal>().eq(UserMedal::getUserId, id));
        List<Long> medalIds = userMedals.stream().map(UserMedal::getMedalId).toList();
        List<Medal> medals = medalIds.isEmpty() ? List.of() : medalService.listByIds(medalIds);

        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setCreateTime(user.getCreateTime());

        if (stat != null) {
            vo.setCoin(stat.getCoin());
            vo.setPostCount(stat.getPostCount());
            vo.setLikeCount(stat.getLikeCount());
            vo.setSignCount(stat.getSignCount());
            vo.setStreakCount(stat.getStreakCount());
        }
        vo.setFollowerCount(userFollowService.followerCount(id));
        vo.setFollowingCount(userFollowService.followingCount(id));
        vo.setFollowedByMe(StpUtil.isLogin() && userFollowService.isFollowing(StpUtil.getLoginIdAsLong(), id));

        vo.setMedals(medals.stream().map(m -> {
            UserProfileVO.MedalVO mv = new UserProfileVO.MedalVO();
            mv.setId(m.getId());
            mv.setName(m.getName());
            mv.setIcon(m.getIcon());
            mv.setDescription(m.getDescription());
            return mv;
        }).toList());

        return Result.ok(vo);
    }

    @GetMapping("/{id}/posts")
    public Result<Page<PostSummaryVO>> userPosts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Post> postPage = postService.page(
                new Page<>(page, size),
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getUserId, id)
                        .eq(Post::getStatus, 1)
                        .orderByDesc(Post::getCreateTime));
        List<PostSummaryVO> vos = postPage.getRecords().stream().map(p -> {
            PostSummaryVO svo = postMapper.toSummary(p);
            voEnricher.enrich(svo, p);
            return svo;
        }).toList();
        Page<PostSummaryVO> voPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        voPage.setRecords(vos);
        return Result.ok(voPage);
    }

    @PostMapping("/{id}/follow")
    public Result<?> follow(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        userFollowService.follow(userId, id);
        return Result.ok();
    }

    @DeleteMapping("/{id}/follow")
    public Result<?> unfollow(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        userFollowService.unfollow(userId, id);
        return Result.ok();
    }
}
