package com.ghostfire.vo;

import com.ghostfire.entity.Tag;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDetailVO {

    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isTop;
    private Boolean isEssence;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private SimpleUserVO author;
    private Long categoryId;
    private String categoryName;
    private List<Tag> tags;

    private Boolean isLiked;
    private Boolean isFavorited;
}
