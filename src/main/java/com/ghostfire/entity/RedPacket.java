package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 红包主表 */
@Data
@TableName("red_packet")
public class RedPacket {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发红包的用户 ID */
    private Long userId;
    /** 关联的帖子 ID（可选） */
    private Long postId;
    /** 红包总金额 */
    private Long totalAmount;
    /** 红包总个数 */
    private Integer totalCount;
    /** 剩余个数 */
    private Integer remainCount;
    /** 类型：1=拼手气, 2=平分 */
    private Integer type;
    /** 状态：1=进行中, 2=已抢完, 3=已过期 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 过期时间 */
    private LocalDateTime expireTime;
}
