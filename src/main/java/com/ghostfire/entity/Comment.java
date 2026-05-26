package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 评论表（支持楼中楼回复） */
@Data
@TableName("comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属帖子 ID */
    private Long postId;
    /** 评论者用户 ID */
    private Long userId;
    /** 父评论 ID（null=一级评论，非 null=楼中楼回复） */
    private Long parentId;
    /** 被回复的用户 ID（楼中楼时使用） */
    private Long replyUserId;
    /** 评论内容 */
    private String content;
    /** 点赞数 */
    private Integer likeCount;
    /** 状态：1=正常, 0=已删除 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
