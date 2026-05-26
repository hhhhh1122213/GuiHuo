package com.ghostfire.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghostfire.common.Result;
import com.ghostfire.dto.UserDto;
import com.ghostfire.entity.*;
import com.ghostfire.service.*;
import com.ghostfire.vo.PostMapper;
import com.ghostfire.vo.PostSummaryVO;
import com.ghostfire.vo.UserProfileVO;
import com.ghostfire.vo.VoEnricher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserStatService userStatService;
    private final MedalService medalService;
    private final UserMedalService userMedalService;
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
                new LambdaQueryWrapper<UserMedal>().eq(UserMedal::getUserId, id)
        );
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
            vo.setBoastCount(stat.getBoastCount());
            vo.setBoastWinCount(stat.getBoastWinCount());
            vo.setBoastWinTotal(stat.getBoastWinTotal());
        }

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

    /** 用户帖子列表（分页） */
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

    @PutMapping("/profile")
    public Result<?> updateProfile(@Valid @RequestBody UserDto form) {
        long userid = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userid);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        if (form.getNickname() != null) user.setNickname(form.getNickname());
        if (form.getAvatar() != null) user.setAvatar(form.getAvatar());
        userService.updateById(user);
        return Result.ok();
    }
}
