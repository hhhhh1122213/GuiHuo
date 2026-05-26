package com.ghostfire.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ghostfire.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {

    List<Comment> listByPostId(Long postId);

    Page<Comment> pageTopLevel(Long postId, int page, int size);

    List<Comment> listChildren(Long postId, List<Long> parentIds);

    Comment addComment(Long postId, Long userId, Long parentId, Long replyUserId, String content);

    void deleteComment(Comment comment);
}
