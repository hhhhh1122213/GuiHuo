package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.common.Constant;
import com.ghostfire.entity.Post;
import com.ghostfire.mapper.PostMapper;
import com.ghostfire.service.PostService;
import org.springframework.stereotype.Service;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Override
    public Page<Post> pageByCategory(Long categoryId, int page, int size) {
        Page<Post> p = new Page<>(page, size);
        LambdaQueryWrapper<Post> w = new LambdaQueryWrapper<Post>()
                .eq(Post::getCategoryId, categoryId)
                .eq(Post::getStatus, Constant.POST_STATUS_NORMAL)
                .orderByDesc(Post::getIsTop)
                .orderByDesc(Post::getCreateTime);
        return page(p, w);
    }

    @Override
    public Page<Post> pageLatest(int page, int size) {
        Page<Post> p = new Page<>(page, size);
        LambdaQueryWrapper<Post> w = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, Constant.POST_STATUS_NORMAL)
                .orderByDesc(Post::getIsTop)
                .orderByDesc(Post::getCreateTime);
        return page(p, w);
    }

    @Override
    public Page<Post> pageEssence(int page, int size) {
        Page<Post> p = new Page<>(page, size);
        LambdaQueryWrapper<Post> w = new LambdaQueryWrapper<Post>()
                .eq(Post::getIsEssence, true)
                .eq(Post::getStatus, Constant.POST_STATUS_NORMAL)
                .orderByDesc(Post::getCreateTime);
        return page(p, w);
    }

    @Override
    public Page<Post> search(String keyword, int page, int size) {
        Page<Post> p = new Page<>(page, size);
        LambdaQueryWrapper<Post> w = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, Constant.POST_STATUS_NORMAL)
                .and(wr -> wr.like(Post::getTitle, keyword)
                        .or()
                        .like(Post::getContent, keyword))
                .orderByDesc(Post::getCreateTime);
        return page(p, w);
    }

    @Override
    public void addViewCount(Long postId) {
        Post post = getById(postId);
        if (post != null) {
            post.setViewCount(post.getViewCount() + 1);
            updateById(post);
        }
    }
}
