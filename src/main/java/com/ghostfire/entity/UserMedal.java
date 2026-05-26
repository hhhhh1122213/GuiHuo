package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 用户勋章关联表 */
@Data
@TableName("user_medal")
public class UserMedal {

    /** 用户 ID */
    private Long userId;
    /** 勋章 ID */
    private Long medalId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
