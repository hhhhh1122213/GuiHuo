package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 抢红包记录表 */
@Data
@TableName("red_packet_record")
public class RedPacketRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 红包 ID */
    private Long packetId;
    /** 抢到红包的用户 ID */
    private Long userId;
    /** 抢到的金额 */
    private Long amount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
