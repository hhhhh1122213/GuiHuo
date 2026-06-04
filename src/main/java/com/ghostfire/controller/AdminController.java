package com.ghostfire.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghostfire.common.Constant;
import com.ghostfire.common.Result;
import com.ghostfire.entity.*;
import com.ghostfire.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@SaCheckRole("ADMIN")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final PostService postService;
    private final CategoryService categoryService;
    private final UserStatService userStatService;
    private final CommentService commentService;
    private final MedalService medalService;

    // ==================== 用户管理 ====================

    /** 用户列表（分页，支持关键词搜索） */
    @GetMapping("/users")
    public Result<Page<User>> userList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreateTime);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getNickname, keyword)
                    .or()
                    .like(User::getUsername, keyword));
        }
        Page<User> userPage = userService.page(new Page<>(page, size), wrapper);
        // 脱敏：不返回密码
        userPage.getRecords().forEach(u -> u.setPassword(null));
        return Result.ok(userPage);
    }

    /** 封禁用户 */
    @PutMapping("/users/{id}/ban")
    public Result<?> banUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        if (Constant.USER_STATUS_BANNED == user.getStatus()) {
            return Result.fail("用户已被封禁");
        }
        user.setStatus(Constant.USER_STATUS_BANNED);
        userService.updateById(user);
        // 踢下线
        try {
            StpUtil.kickout(id);
        } catch (Exception e) {
            log.warn("踢出用户 {} 失败: {}", id, e.getMessage());
        }
        return Result.ok();
    }

    /** 解封用户 */
    @PutMapping("/users/{id}/unban")
    public Result<?> unbanUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        if (Constant.USER_STATUS_NORMAL == user.getStatus()) {
            return Result.fail("用户未被封禁");
        }
        user.setStatus(Constant.USER_STATUS_NORMAL);
        userService.updateById(user);
        return Result.ok();
    }

    // ==================== 勋章管理 ====================

    /** 手动颁发勋章 */
    @PostMapping("/medals/award")
    public Result<?> awardMedal(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        Long medalId = body.get("medalId");
        if (userId == null || medalId == null) {
            return Result.fail("用户ID和勋章ID不能为空");
        }
        medalService.awardManual(userId, medalId);
        return Result.ok();
    }

    // ==================== 帖子管理 ====================

    /** 帖子列表（分页） */
    @GetMapping("/posts")
    public Result<Page<Post>> postList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Post> postPage = postService.page(new Page<>(page, size),
                new LambdaQueryWrapper<Post>()
                        .orderByDesc(Post::getCreateTime));
        return Result.ok(postPage);
    }

    /** 置顶/取消置顶 */
    @PutMapping("/posts/{id}/top")
    public Result<?> toggleTop(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null) {
            return Result.fail("帖子不存在");
        }
        post.setIsTop(!Boolean.TRUE.equals(post.getIsTop()));
        postService.updateById(post);
        return Result.ok(post.getIsTop());
    }

    /** 加精/取消加精 */
    @PutMapping("/posts/{id}/essence")
    public Result<?> toggleEssence(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null) {
            return Result.fail("帖子不存在");
        }
        post.setIsEssence(!Boolean.TRUE.equals(post.getIsEssence()));
        postService.updateById(post);
        return Result.ok(post.getIsEssence());
    }

    /** 管理员删帖 */
    @DeleteMapping("/posts/{id}/delete")
    public Result<?> deletePost(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null) {
            return Result.fail("帖子不存在");
        }
        if (Constant.POST_STATUS_DELETED == post.getStatus()) {
            return Result.fail("帖子已删除");
        }
        post.setStatus(Constant.POST_STATUS_DELETED);
        postService.updateById(post);
        // 原子递减作者发帖数
        userStatService.getBaseMapper().update(null,
                new LambdaUpdateWrapper<UserStat>()
                        .eq(UserStat::getUserId, post.getUserId())
                        .setSql("post_count = GREATEST(post_count - 1, 0)"));

        return Result.ok();
    }

    // ==================== 分类管理 ====================

    /** 创建分类 */
    @PostMapping("/categories")
    public Result<Category> createCategory(@RequestBody Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            return Result.fail("分类名称不能为空");
        }
        categoryService.save(category);
        return Result.ok(category);
    }

    /** 编辑分类 */
    @PutMapping("/categories/{id}")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category existing = categoryService.getById(id);
        if (existing == null) {
            return Result.fail("分类不存在");
        }
        if (category.getName() != null) existing.setName(category.getName());
        if (category.getDescription() != null) existing.setDescription(category.getDescription());
        if (category.getSortOrder() != null) existing.setSortOrder(category.getSortOrder());
        categoryService.updateById(existing);
        return Result.ok(existing);
    }

    /** 删除分类 */
    @DeleteMapping("/categories/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        Category existing = categoryService.getById(id);
        if (existing == null) {
            return Result.fail("分类不存在");
        }
        // 检查是否有帖子关联
        long postCount = postService.count(new LambdaQueryWrapper<Post>()
                .eq(Post::getCategoryId, id));
        if (postCount > 0) {
            return Result.fail("该分类下有 " + postCount + " 篇帖子，无法删除");
        }
        categoryService.removeById(id);
        return Result.ok();
    }

    // ==================== 系统统计 ====================

    /** 系统统计数据 */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();

        // 用户统计
        long totalUsers = userService.count();
        long bannedUsers = userService.count(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, Constant.USER_STATUS_BANNED));
        data.put("totalUsers", totalUsers);
        data.put("bannedUsers", bannedUsers);
        data.put("activeUsers", totalUsers - bannedUsers);

        // 帖子统计
        long totalPosts = postService.count();
        long normalPosts = postService.count(new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, Constant.POST_STATUS_NORMAL));
        data.put("totalPosts", totalPosts);
        data.put("normalPosts", normalPosts);
        data.put("deletedPosts", totalPosts - normalPosts);

        // 评论统计
        long totalComments = commentService.count();
        data.put("totalComments", totalComments);

        // 分类数
        long totalCategories = categoryService.count();
        data.put("totalCategories", totalCategories);

        return Result.ok(data);
    }
}
