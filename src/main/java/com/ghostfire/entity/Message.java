package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 站内私信表 */
@Data
@TableName("t_message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送者用户 ID */
    private Long fromUserId;
    /** 接收者用户 ID */
    private Long toUserId;
    /** 消息内容 */
    private String content;
    /** 消息类型：1=私信, 2=@我的动态, 3=@我的评论, 4=收到的赞, 5=关注通知, 6=系统通知 */
    private Integer type;

    /** 关联目标 ID（如帖子ID、评论ID等） */
    private Long targetId;

    /** 状态：0=未读, 1=已读 */
    private Integer status;

    /** 发送方是否已删除该消息 */
    private Boolean fromDeleted;

    /** 接收方是否已删除该消息 */
    private Boolean toDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
