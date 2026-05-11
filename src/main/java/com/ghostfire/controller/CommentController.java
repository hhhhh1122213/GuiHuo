package com.ghostfire.controller;

import cn.dev33.satoken.stp.StpUtil;
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

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;
    private final VoEnricher voEnricher;

    @GetMapping("/list")
    public Result<List<CommentVO>> list(@RequestParam Long postId) {
        List<Comment> comments = commentService.listByPostId(postId);
        List<CommentVO> vos = comments.stream().map(c -> {
            CommentVO vo = commentMapper.toVO(c);
            voEnricher.enrich(vo, c);
            return vo;
        }).toList();
        return Result.ok(vos);
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
