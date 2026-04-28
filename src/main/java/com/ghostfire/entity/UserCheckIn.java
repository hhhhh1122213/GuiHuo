package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_check_in")
public class UserCheckIn {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private LocalDate checkDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
