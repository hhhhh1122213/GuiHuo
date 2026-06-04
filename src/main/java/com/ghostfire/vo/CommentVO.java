package com.ghostfire.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {

    private Long id;
    private Long postId;
    private Long parentId;
    private String content;
    private Integer likeCount;
    private LocalDateTime createTime;

    private SimpleUserVO author;
    private SimpleUserVO replyUser;

    /** 楼中楼回复列表 */
    private List<CommentVO> children;
}
