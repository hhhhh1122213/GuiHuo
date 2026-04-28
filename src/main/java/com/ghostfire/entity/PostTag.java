package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("post_tag")
public class PostTag {

    private Long postId;
    private Long tagId;
}
