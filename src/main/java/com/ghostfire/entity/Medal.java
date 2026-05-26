package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/** 勋章定义表 */
@Data
@TableName("medal")
public class Medal {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 勋章名称 */
    private String name;
    /** 勋章图标 URL */
    private String icon;
    /** 勋章描述 */
    private String description;
    /** 授予规则类型：MANUAL=手动, AUTO_STAT=自动统计 */
    private String ruleType;
    /** 自动检测关联的 UserStat 字段名（如 postCount, likeCount, coin, signCount） */
    private String ruleField;
    /** 规则阈值（如发帖数达到多少自动授予） */
    private Integer ruleValue;
}
