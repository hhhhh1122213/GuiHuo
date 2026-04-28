package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("red_packet_record")
public class RedPacketRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long packetId;
    private Long userId;
    private Long amount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
