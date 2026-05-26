package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 板块分类表 */
@Data
@TableName("category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 分类名称 */
    private String name;
    /** 分类描述 */
    private String description;
    /** 排序权重（越大越靠前） */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
