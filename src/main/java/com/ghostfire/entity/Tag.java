package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/** 标签表 */
@Data
@TableName("tag")
public class Tag {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标签名称 */
    private String name;
}
