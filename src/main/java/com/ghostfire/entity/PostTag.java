package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/** 帖子标签关联表 */
@Data
@TableName("post_tag")
public class PostTag {

    /** 帖子 ID（联合主键） */
    private Long postId;
    /** 标签 ID（联合主键） */
    private Long tagId;
}
