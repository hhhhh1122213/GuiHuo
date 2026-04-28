package com.ghostfire.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghostfire.common.Result;
import com.ghostfire.dto.PostDto;
import com.ghostfire.entity.Post;
import com.ghostfire.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/list")
    public Result<Page<Post>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId) {
        if (categoryId != null) {
            return Result.ok(postService.pageByCategory(categoryId, page, size));
        }
        return Result.ok(postService.pageLatest(page, size));
    }

    @GetMapping("/essence")
    public Result<Page<Post>> essence(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(postService.pageEssence(page, size));
    }

    @GetMapping("/detail/{id}")
    public Result<Post> detail(@PathVariable Long id) {
        postService.addViewCount(id);
        Post post = postService.getById(id);
        if (post == null) {
            return Result.fail("帖子不存在");
        }
        return Result.ok(post);
    }

    @PostMapping("/create")
    public Result<?> create(@Valid @RequestBody PostDto dto) {
        long userId = StpUtil.getLoginIdAsLong();
        Post post = new Post();
        post.setCategoryId(dto.getCategoryId());
        post.setUserId(userId);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsTop(false);
        post.setIsEssence(false);
        post.setStatus(1);
        postService.save(post);
        return Result.ok(post);
    }

    @GetMapping("/search")
    public Result<Page<Post>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(postService.search(keyword, page, size));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null) {
            return Result.fail("帖子不存在");
        }
        long userId = StpUtil.getLoginIdAsLong();
        if (!post.getUserId().equals(userId)) {
            return Result.fail("无权删除");
        }
        post.setStatus(0);
        postService.updateById(post);
        return Result.ok();
    }
}
