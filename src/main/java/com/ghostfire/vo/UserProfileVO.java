package com.ghostfire.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserProfileVO {

    private Long id;
    private String nickname;
    private String avatar;
    private String role;
    private LocalDateTime createTime;

    private Long coin;
    private Integer postCount;
    private Integer likeCount;
    private Integer signCount;
    private Integer streakCount;
    private Integer boastCount;
    private Integer boastWinCount;
    private Long boastWinTotal;
    private Integer followerCount;
    private Integer followingCount;
    private Boolean followedByMe;

    private List<MedalVO> medals;

    @Data
    public static class MedalVO {
        private Long id;
        private String name;
        private String icon;
        private String description;
    }
}
