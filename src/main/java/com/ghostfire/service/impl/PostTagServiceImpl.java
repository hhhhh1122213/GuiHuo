package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.entity.PostTag;
import com.ghostfire.mapper.PostTagMapper;
import com.ghostfire.service.PostTagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class PostTagServiceImpl extends ServiceImpl<PostTagMapper, PostTag> implements PostTagService {

    @Override
    @Transactional
    public void replacePostTags(Long postId, List<Long> tagIds) {
        remove(new LambdaQueryWrapper<PostTag>().eq(PostTag::getPostId, postId));
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        List<PostTag> rows = tagIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .limit(5)
                .map(tagId -> {
                    PostTag row = new PostTag();
                    row.setPostId(postId);
                    row.setTagId(tagId);
                    return row;
                })
                .toList();
        if (!rows.isEmpty()) {
            saveBatch(rows);
        }
    }
}
