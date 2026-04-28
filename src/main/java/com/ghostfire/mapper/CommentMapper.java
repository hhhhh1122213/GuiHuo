package com.ghostfire.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ghostfire.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
