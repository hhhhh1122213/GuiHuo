package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_wallet_log")
public class UserWalletLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long amount;
    private Long currentBalance;
    private String type;
    private Long refId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
