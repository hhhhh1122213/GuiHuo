package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/** 用户收藏表 */
@Data
@TableName("user_favorite")
public class UserFavorite {
    /** 收藏记录 ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户 ID */
    private Long userId;
    /** 被收藏的帖子 ID */
    private Long postId;
    /** 收藏时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
