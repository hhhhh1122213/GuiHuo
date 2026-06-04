package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 用户关注关系表 */
@Data
@TableName("user_follow")
public class UserFollow {

    /** 关注者用户 ID */
    private Long followerId;

    /** 被关注者用户 ID */
    private Long followeeId;

    /** 关注时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
