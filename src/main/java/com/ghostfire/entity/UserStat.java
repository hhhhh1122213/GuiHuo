package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("user_stat")
public class UserStat {

    @TableId(type = IdType.INPUT)
    private Long userId;

    private Long coin;
    private Integer postCount;
    private Integer likeCount;
    private Integer signCount;
    private Integer boastCount;
    private Integer boastWinCount;
    private Long boastWinTotal;
    private Long boastBestWin;
    private Long boastWorstLoss;
}
