package com.ghostfire.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghostfire.common.Result;
import com.ghostfire.dto.ReplyDto;
import com.ghostfire.entity.Comment;
import com.ghostfire.service.CommentService;
import com.ghostfire.vo.CommentMapper;
import com.ghostfire.vo.CommentVO;
import com.ghostfire.vo.VoEnricher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;
    private final VoEnricher voEnricher;

    /** 评论列表（分页一级评论，子回复挂在父评论下） */
    @GetMapping("/list")
    public Result<Page<CommentVO>> list(
            @RequestParam Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Comment> topPage = commentService.pageTopLevel(postId, page, size);
        List<Comment> topList = topPage.getRecords();

        // 批量加载子回复
        List<Long> parentIds = topList.stream().map(Comment::getId).toList();
        List<Comment> children = commentService.listChildren(postId, parentIds);
        Map<Long, List<Comment>> childrenMap = children.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));

        // 组装 VO（批量富化避免 N+1）
        List<Comment> allComments = new ArrayList<>(topList);
        children.forEach(c -> { if (!allComments.contains(c)) allComments.add(c); });
        List<CommentVO> allVos = allComments.stream().map(commentMapper::toVO).toList();
        voEnricher.enrichBatchComments(allVos, allComments);

        Map<Long, CommentVO> voMap = new java.util.HashMap<>();
        for (int i = 0; i < allComments.size(); i++) {
            voMap.put(allComments.get(i).getId(), allVos.get(i));
        }

        List<CommentVO> vos = topList.stream().map(c -> {
            CommentVO vo = voMap.get(c.getId());
            List<Comment> childList = childrenMap.getOrDefault(c.getId(), List.of());
            vo.setChildren(childList.stream().map(ch -> voMap.get(ch.getId())).toList());
            return vo;
        }).toList();

        Page<CommentVO> voPage = new Page<>(topPage.getCurrent(), topPage.getSize(), topPage.getTotal());
        voPage.setRecords(vos);
        return Result.ok(voPage);
    }

    @PostMapping("/create")
    public Result<CommentVO> create(@Valid @RequestBody ReplyDto dto) {
        long userId = StpUtil.getLoginIdAsLong();
        Comment comment = commentService.addComment(dto.getPostId(), userId, dto.getParentId(), dto.getReplyUserId(), dto.getContent());
        CommentVO vo = commentMapper.toVO(comment);
        voEnricher.enrich(vo, comment);
        return Result.ok(vo);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.fail("评论不存在");
        }
        long userId = StpUtil.getLoginIdAsLong();
        if (!comment.getUserId().equals(userId)) {
            return Result.fail("无权删除");
        }
        commentService.deleteComment(comment);
        return Result.ok();
    }
}
