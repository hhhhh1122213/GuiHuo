package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_medal")
public class UserMedal {

    private Long userId;
    private Long medalId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
