package com.ghostfire.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ghostfire.entity.Post;

public interface PostService extends IService<Post> {

    Page<Post> pageByCategory(Long categoryId, int page, int size);

    Page<Post> pageLatest(int page, int size);

    Page<Post> pageEssence(int page, int size);

    Page<Post> search(String keyword, int page, int size);

    void addViewCount(Long postId);
}
