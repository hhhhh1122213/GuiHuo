package com.ghostfire.controller.mini;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghostfire.common.Result;
import com.ghostfire.dto.PostDto;
import com.ghostfire.entity.Post;
import com.ghostfire.service.PostService;
import com.ghostfire.vo.PostMapper;
import com.ghostfire.vo.PostDetailVO;
import com.ghostfire.vo.PostSummaryVO;
import com.ghostfire.vo.VoEnricher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mini/posts")
@RequiredArgsConstructor
public class MiniPostController {

    private final PostService postService;
    private final PostMapper postMapper;
    private final VoEnricher voEnricher;

    @GetMapping("/list")
    public Result<IPage<PostSummaryVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId) {
        Page<Post> p = categoryId != null
                ? postService.pageByCategory(categoryId, page, size)
                : postService.pageLatest(page, size);
        List<Post> posts = p.getRecords();
        List<PostSummaryVO> vos = posts.stream().map(postMapper::toSummary).toList();
        voEnricher.enrichBatch(vos, posts);
        return Result.ok(postMapper.toSummaryPage(p, vos));
    }

    @GetMapping("/{id}")
    public Result<PostDetailVO> detail(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null) {
            return Result.fail("帖子不存在");
        }
        postService.addViewCount(id);
        PostDetailVO vo = postMapper.toDetail(post);
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
}
