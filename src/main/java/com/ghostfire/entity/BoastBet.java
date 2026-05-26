package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 吹牛下注记录表 */
@Data
@TableName("boast_bet")
public class BoastBet {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的吹牛话题 ID */
    private Long boastId;
    /** 下注用户 ID */
    private Long userId;
    /** 选择的选项：1=选项一, 2=选项二 */
    private Integer optionType;
    /** 下注金额 */
    private Long amount;
    /** 结算结果：0=未结算, 1=赢, 2=输 */
    private Integer result;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
