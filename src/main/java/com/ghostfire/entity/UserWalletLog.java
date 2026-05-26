package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 用户金币流水账本 */
@Data
@TableName("user_wallet_log")
public class UserWalletLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;
    /** 变动金额（正=收入, 负=支出） */
    private Long amount;
    /** 变动后余额快照 */
    private Long currentBalance;
    /** 流水类型：SIGN_IN/POST/LIKE/RED_PACKET_SEND/RED_PACKET_RECEIVE/BOAST_BET/BOAST_WIN */
    private String type;
    /** 关联业务 ID（帖子 ID、红包 ID 等） */
    private Long refId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
