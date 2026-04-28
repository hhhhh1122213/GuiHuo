package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("red_packet")
public class RedPacket {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long postId;
    private Long totalAmount;
    private Integer totalCount;
    private Integer remainCount;
    private Integer type;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime expireTime;
}
