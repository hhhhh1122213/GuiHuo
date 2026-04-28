package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("medal")
public class Medal {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String icon;
    private String description;
    private String ruleType;
    private Integer ruleValue;
}
