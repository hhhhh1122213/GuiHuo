package com.ghostfire.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghostfire.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {

    List<Comment> listByPostId(Long postId);

    Comment addComment(Long postId, Long userId, Long parentId, Long replyUserId, String content);

    void deleteComment(Comment comment);
}
