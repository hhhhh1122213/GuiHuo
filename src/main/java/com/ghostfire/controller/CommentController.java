package com.ghostfire.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.ghostfire.common.Result;
import com.ghostfire.dto.ReplyDto;
import com.ghostfire.entity.Comment;
import com.ghostfire.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/list")
    public Result<List<Comment>> list(@RequestParam Long postId) {
        return Result.ok(commentService.listByPostId(postId));
    }

    @PostMapping("/create")
    public Result<?> create(@Valid @RequestBody ReplyDto dto) {
        System.out.println("我被调用了");
        long userId = StpUtil.getLoginIdAsLong();
        Comment comment = commentService.addComment(dto.getPostId(), userId, dto.getParentId(), dto.getReplyUserId(), dto.getContent());
        return Result.ok(comment);
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
        comment.setStatus(0);
        commentService.updateById(comment);
        return Result.ok();
    }
}
