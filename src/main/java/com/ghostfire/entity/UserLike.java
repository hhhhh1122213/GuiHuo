package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 通用点赞表（多态，支持帖子和评论） */
@Data
@TableName("user_like")
public class UserLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 点赞者用户 ID */
    private Long userId;
    /** 被点赞目标 ID（帖子或评论的 ID） */
    private Long targetId;
    /** 目标类型：1=帖子, 2=评论 */
    private Integer targetType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
