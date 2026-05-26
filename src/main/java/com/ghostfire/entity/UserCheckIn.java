package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 用户签到记录表 */
@Data
@TableName("user_check_in")
public class UserCheckIn {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;
    /** 签到日期（UNIQUE(userId, checkDate) 防重复签到） */
    private LocalDate checkDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
