package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ghostfire.common.Constant;
import com.ghostfire.entity.Comment;
import com.ghostfire.entity.Post;
import com.ghostfire.mapper.CommentMapper;
import com.ghostfire.service.CommentService;
import com.ghostfire.service.MessageService;
import com.ghostfire.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final PostService postService;
    private final MessageService messageService;


    @Override
    public List<Comment> listByPostId(Long postId) {
        return list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, postId)
                .eq(Comment::getStatus, Constant.COMMENT_STATUS_NORMAL)
                .orderByAsc(Comment::getCreateTime));
    }

    @Override
    public Page<Comment> pageTopLevel(Long postId, int page, int size) {
        return page(new Page<>(page, size), new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, postId)
                .and(w -> w.isNull(Comment::getParentId)
                        .or()
                        .eq(Comment::getParentId, 0))
                .eq(Comment::getStatus, Constant.COMMENT_STATUS_NORMAL)
                .orderByAsc(Comment::getCreateTime));
    }

    @Override
    public List<Comment> listChildren(Long postId, List<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return List.of();
        }
        return list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, postId)
                .in(Comment::getParentId, parentIds)
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
        // 通知帖子作者
        if (!userId.equals(post.getUserId())) {
            try {
                int notifyType = parentId != null ? Constant.MSG_TYPE_MENTION_COMMENT : Constant.MSG_TYPE_MENTION_POST;
                messageService.send(userId, post.getUserId(), content, notifyType, postId);
            } catch (Exception e) {
                log.warn("评论通知发送失败: userId={}, postUserId={}", userId, post.getUserId(), e);
            }
        }
        // 楼中楼回复：通知被回复的用户
        if (replyUserId != null && !userId.equals(replyUserId) && !replyUserId.equals(post.getUserId())) {
            try {
                messageService.send(userId, replyUserId, content, Constant.MSG_TYPE_MENTION_COMMENT, postId);
            } catch (Exception e) {
                log.warn("回复通知发送失败: userId={}, replyUserId={}", userId, replyUserId, e);
            }
        }
        postService.getBaseMapper().update(null,
                new LambdaUpdateWrapper<Post>()
                        .eq(Post::getId, postId)
                        .setSql("comment_count = comment_count + 1"));
        return comment;
    }

    @Override
    @Transactional
    public void deleteComment(Comment comment) {
        comment.setStatus(Constant.COMMENT_STATUS_DELETED);
        updateById(comment);
        // 原子递减帖子评论数，用 GREATEST 防止变负
        postService.getBaseMapper().update(null,
                new LambdaUpdateWrapper<Post>()
                        .eq(Post::getId, comment.getPostId())
                        .setSql("comment_count = GREATEST(comment_count - 1, 0)"));
    }
}
