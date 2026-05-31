package com.ghostfire.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghostfire.common.Result;
import com.ghostfire.config.RateLimit;
import com.ghostfire.dto.PostDto;
import com.ghostfire.entity.Post;
import com.ghostfire.service.PostService;
import com.ghostfire.vo.PostMapper;
import com.ghostfire.vo.PostDetailVO;
import com.ghostfire.vo.PostSummaryVO;
import com.ghostfire.vo.VoEnricher;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostMapper postMapper;
    private final VoEnricher voEnricher;

    @GetMapping("/list")
    public Result<IPage<PostSummaryVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(defaultValue = "recommend") String sort) {
        Page<Post> p = tagId != null
                ? postService.pageByTag(tagId, page, size)
                : postService.pageFeed(categoryId, page, size, sort);
        List<Post> posts = p.getRecords();
        List<PostSummaryVO> vos = posts.stream().map(postMapper::toSummary).toList();
        voEnricher.enrichBatch(vos, posts);
        return Result.ok(postMapper.toSummaryPage(p, vos));
    }

    @GetMapping("/essence")
    public Result<IPage<PostSummaryVO>> essence(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId) {
        Page<Post> p = postService.pageEssence(categoryId, page, size);
        List<Post> posts = p.getRecords();
        List<PostSummaryVO> vos = posts.stream().map(postMapper::toSummary).toList();
        voEnricher.enrichBatch(vos, posts);
        return Result.ok(postMapper.toSummaryPage(p, vos));
    }

    @GetMapping("/detail/{id}")
    public Result<PostDetailVO> detail(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null) {
            return Result.fail("帖子不存在");
        }
        postService.addViewCount(id);
        PostDetailVO vo = postMapper.toDetail(post);
        // 叠加 Redis 中未刷入的浏览量增量
        long delta = postService.getViewCountDelta(id);
        vo.setViewCount((post.getViewCount() != null ? post.getViewCount() : 0) + (int) delta);
        voEnricher.enrich(vo, post);
        return Result.ok(vo);
    }

    @PostMapping("/create")
    public Result<?> create(@Valid @RequestBody PostDto dto) {
        long userId = StpUtil.getLoginIdAsLong();
        Post post = postService.createPost(userId, dto);
        return Result.ok(post);
    }

    @RateLimit(key = "search", maxCount = 30)
    @GetMapping("/search")
    public Result<IPage<PostSummaryVO>> search(
            @RequestParam @NotBlank(message = "搜索关键词不能为空") @Size(max = 50, message = "搜索关键词不能超过50个字符") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Post> p = postService.search(keyword, page, size);
        List<Post> posts = p.getRecords();
        List<PostSummaryVO> vos = posts.stream().map(postMapper::toSummary).toList();
        voEnricher.enrichBatch(vos, posts);
        return Result.ok(postMapper.toSummaryPage(p, vos));
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
        postService.deletePost(post, userId);
        return Result.ok();
    }
    @PutMapping("/{id}")
    public Result<?> editpost(@PathVariable Long id, @Valid @RequestBody PostDto dto) {
        Post post = postService.getById(id);
        if (post == null) {
            return Result.fail("帖子不存在");
        }
        long userId = StpUtil.getLoginIdAsLong();
        if (!post.getUserId().equals(userId)) {
            return Result.fail("无权修改");
        }
        postService.editPost(id, dto,post);
        return Result.ok();
    }
}
