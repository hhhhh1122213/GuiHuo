package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.common.Constant;
import com.ghostfire.entity.Comment;
import com.ghostfire.entity.Post;
import com.ghostfire.mapper.CommentMapper;
import com.ghostfire.service.CommentService;
import com.ghostfire.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final PostService postService;

    @Override
    public List<Comment> listByPostId(Long postId) {
        return list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, postId)
                .eq(Comment::getStatus, Constant.COMMENT_STATUS_NORMAL)
                .orderByAsc(Comment::getCreateTime));
    }

    @Override
    @Transactional
    public Comment addComment(Long postId, Long userId, Long parentId, Long replyUserId, String content) {
        Post post = postService.getById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setReplyUserId(replyUserId);
        comment.setContent(content);
        comment.setLikeCount(0);
        comment.setStatus(Constant.COMMENT_STATUS_NORMAL);
        save(comment);
        post.setCommentCount(post.getCommentCount() + 1);
        postService.updateById(post);
        return comment;
    }
}
