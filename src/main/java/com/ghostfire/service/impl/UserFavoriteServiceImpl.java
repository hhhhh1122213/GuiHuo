package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.entity.Post;
import com.ghostfire.entity.UserFavorite;
import com.ghostfire.mapper.UserFavoriteMapper;
import com.ghostfire.service.PostService;
import com.ghostfire.service.UserFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite> implements UserFavoriteService {

    private final PostService postService;

    @Override
    @Transactional
    public void favorite(Long userId, Long postId) {
        UserFavorite fav = new UserFavorite();
        fav.setUserId(userId);
        fav.setPostId(postId);
        try {
            save(fav);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("已收藏");
        }
    }

    @Override
    public void unfavorite(Long userId, Long postId) {
        remove(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getPostId, postId));
    }

    @Override
    public boolean isFavorited(Long userId, Long postId) {
        return count(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getPostId, postId)) > 0;
    }

    @Override
    public Page<Post> pageFavorites(Long userId, int page, int size) {
        Page<UserFavorite> favPage = page(new Page<>(page, size),
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .orderByDesc(UserFavorite::getCreateTime));
        if (favPage.getRecords().isEmpty()) {
            return new Page<>(page, size, 0);
        }
        java.util.List<Long> postIds = favPage.getRecords().stream().map(UserFavorite::getPostId).toList();
        java.util.List<Post> posts = postService.listByIds(postIds);
        Page<Post> result = new Page<>(page, size, favPage.getTotal());
        result.setRecords(posts);
        return result;
    }
}
