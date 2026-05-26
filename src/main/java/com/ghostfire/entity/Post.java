package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 帖子主表 */
@Data
@TableName("post")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发帖用户 ID */
    private Long userId;
    /** 所属分类 ID */
    private Long categoryId;
    /** 标题 */
    private String title;
    /** 正文内容 */
    private String content;
    /** 浏览量 */
    private Integer viewCount;
    /** 点赞数 */
    private Integer likeCount;
    /** 评论数 */
    private Integer commentCount;
    /** 是否置顶 */
    private Boolean isTop;
    /** 是否精华 */
    private Boolean isEssence;
    /** 状态：1=正常, 0=已删除 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
