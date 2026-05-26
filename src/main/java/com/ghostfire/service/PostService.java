package com.ghostfire.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ghostfire.dto.PostDto;
import com.ghostfire.entity.Post;

public interface PostService extends IService<Post> {

    Page<Post> pageByCategory(Long categoryId, int page, int size);

    Page<Post> pageByTag(Long tagId, int page, int size);

    Page<Post> pageLatest(int page, int size);

    Page<Post> pageEssence(int page, int size);

    Page<Post> search(String keyword, int page, int size);

    void addViewCount(Long postId);

    /** 获取 Redis 中未刷入 DB 的浏览量增量 */
    long getViewCountDelta(Long postId);

    Post createPost(Long userId, PostDto dto);

    void deletePost(Post post, Long userId);
    void editPost(long id, PostDto dto,Post  post);
}
