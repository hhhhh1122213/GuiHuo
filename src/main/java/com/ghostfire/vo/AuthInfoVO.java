package com.ghostfire.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuthInfoVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String role;
    private Integer status;
    private LocalDateTime createTime;

    private Long coin;
    private Integer postCount;
    private Integer likeCount;
    private Integer signCount;
}
