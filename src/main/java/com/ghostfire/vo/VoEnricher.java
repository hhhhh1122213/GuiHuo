package com.ghostfire.vo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ghostfire.entity.*;
import com.ghostfire.service.CategoryService;
import com.ghostfire.service.PostTagService;
import com.ghostfire.service.TagService;
import com.ghostfire.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VoEnricher {

    private final UserService userService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final PostTagService postTagService;

    public void enrich(PostSummaryVO vo, Post post) {
        Map<Long, SimpleUserVO> users = loadUsers(List.of(post.getUserId()));
        vo.setAuthor(users.get(post.getUserId()));
        Map<Long, String> categories = loadCategories(List.of(post.getCategoryId()));
        vo.setCategoryName(categories.get(post.getCategoryId()));
    }

    /** 批量富化帖子列表，避免 N+1 查询 */
    public void enrichBatch(List<PostSummaryVO> vos, List<Post> posts) {
        if (posts.isEmpty()) return;
        List<Long> userIds = posts.stream().map(Post::getUserId).distinct().toList();
        List<Long> categoryIds = posts.stream().map(Post::getCategoryId).filter(Objects::nonNull).distinct().toList();
        Map<Long, SimpleUserVO> users = loadUsers(userIds);
        Map<Long, String> categories = loadCategories(categoryIds);
        Map<Long, List<Tag>> tags = loadTagsByPostIds(posts.stream().map(Post::getId).toList());
        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            PostSummaryVO vo = vos.get(i);
            vo.setAuthor(users.get(post.getUserId()));
            vo.setCategoryName(categories.get(post.getCategoryId()));
            vo.setTags(tags.getOrDefault(post.getId(), List.of()));
        }
    }

    /** 批量富化评论列表，避免 N+1 查询 */
    public void enrichBatchComments(List<CommentVO> vos, List<Comment> comments) {
        if (comments.isEmpty()) return;
        Set<Long> allUserIds = new HashSet<>();
        for (Comment c : comments) {
            allUserIds.add(c.getUserId());
            if (c.getReplyUserId() != null) allUserIds.add(c.getReplyUserId());
        }
        Map<Long, SimpleUserVO> users = loadUsers(new ArrayList<>(allUserIds));
        for (int i = 0; i < comments.size(); i++) {
            Comment comment = comments.get(i);
            CommentVO vo = vos.get(i);
            vo.setAuthor(users.get(comment.getUserId()));
            if (comment.getReplyUserId() != null) vo.setReplyUser(users.get(comment.getReplyUserId()));
        }
    }

    public void enrich(PostDetailVO vo, Post post) {
        Map<Long, SimpleUserVO> users = loadUsers(List.of(post.getUserId()));
        vo.setAuthor(users.get(post.getUserId()));
        Map<Long, String> categories = loadCategories(List.of(post.getCategoryId()));
        vo.setCategoryName(categories.get(post.getCategoryId()));
        vo.setTags(loadTags(post.getId()));
    }

    public void enrich(CommentVO vo, Comment comment) {
        Set<Long> ids = new HashSet<>();
        ids.add(comment.getUserId());
        if (comment.getReplyUserId() != null) ids.add(comment.getReplyUserId());
        Map<Long, SimpleUserVO> users = loadUsers(new ArrayList<>(ids));
        vo.setAuthor(users.get(comment.getUserId()));
        if (comment.getReplyUserId() != null) vo.setReplyUser(users.get(comment.getReplyUserId()));
    }

    public void enrich(MessageVO vo, Message message) {
        Map<Long, SimpleUserVO> users = loadUsers(List.of(message.getFromUserId(), message.getToUserId()));
        vo.setFromUser(users.get(message.getFromUserId()));
        vo.setToUser(users.get(message.getToUserId()));
    }

    private Map<Long, SimpleUserVO> loadUsers(List<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();
        return userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> {
                    SimpleUserVO svo = new SimpleUserVO();
                    svo.setId(u.getId());
                    svo.setNickname(u.getNickname());
                    svo.setAvatar(u.getAvatar());
                    return svo;
                }));
    }

    private Map<Long, String> loadCategories(List<Long> categoryIds) {
        if (categoryIds.isEmpty()) return Map.of();
        return categoryService.listByIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    private List<Tag> loadTags(Long postId) {
        List<Long> tagIds = postTagService.list(
                new LambdaQueryWrapper<PostTag>().eq(PostTag::getPostId, postId)
        ).stream().map(PostTag::getTagId).toList();
        if (tagIds.isEmpty()) return List.of();
        return tagService.listByIds(tagIds);
    }

    private Map<Long, List<Tag>> loadTagsByPostIds(List<Long> postIds) {
        if (postIds.isEmpty()) return Map.of();
        List<PostTag> rows = postTagService.list(new LambdaQueryWrapper<PostTag>().in(PostTag::getPostId, postIds));
        if (rows.isEmpty()) return Map.of();
        List<Long> tagIds = rows.stream().map(PostTag::getTagId).distinct().toList();
        Map<Long, Tag> tags = tagService.listByIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, tag -> tag));
        Map<Long, List<Tag>> result = new HashMap<>();
        for (PostTag row : rows) {
            Tag tag = tags.get(row.getTagId());
            if (tag != null) {
                result.computeIfAbsent(row.getPostId(), id -> new ArrayList<>()).add(tag);
            }
        }
        return result;
    }
}
