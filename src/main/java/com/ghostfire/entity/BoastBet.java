package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("boast_bet")
public class BoastBet {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long boastId;
    private Long userId;
    private Integer optionType;
    private Long amount;
    private Integer result;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
