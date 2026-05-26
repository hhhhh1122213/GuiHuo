package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 吹牛话题表 */
@Data
@TableName("boast")
public class Boast {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发起者用户 ID */
    private Long userId;
    /** 挑战标题 */
    private String title;
    /** 配图 URL */
    private String image;
    /** 选项一 */
    private String optionOne;
    /** 选项二 */
    private String optionTwo;
    /** 正确答案：1=选项一, 2=选项二 */
    private Integer correctOption;
    /** 赌注金额 */
    private Long stakeAmount;
    /** 结果：0=进行中, 1=发布者胜, 2=挑战者胜 */
    private Integer result;
    /** 截止时间 */
    private LocalDateTime deadline;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
