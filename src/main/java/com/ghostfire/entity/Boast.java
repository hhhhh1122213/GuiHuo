package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("boast")
public class Boast {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String title;
    private String image;
    private String optionOne;
    private String optionTwo;
    private Integer correctOption;
    private Long stakeAmount;
    private Integer result;
    private LocalDateTime deadline;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
