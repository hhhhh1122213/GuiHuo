package com.ghostfire.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ghostfire.entity.Post;
import com.ghostfire.entity.UserFavorite;

public interface UserFavoriteService extends IService<UserFavorite> {

    void favorite(Long userId, Long postId);

    void unfavorite(Long userId, Long postId);

    boolean isFavorited(Long userId, Long postId);

    Page<Post> pageFavorites(Long userId, int page, int size);
}
