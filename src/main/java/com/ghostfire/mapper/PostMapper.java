package com.ghostfire.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghostfire.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    Page<Post> searchFullText(@Param("keyword") String keyword, Page<Post> page);

    Page<Post> selectByTag(@Param("tagId") Long tagId, Page<Post> page);
}
