package com.ghostfire.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ghostfire.entity.PostTag;

import java.util.List;

public interface PostTagService extends IService<PostTag> {

    void replacePostTags(Long postId, List<Long> tagIds);
}
