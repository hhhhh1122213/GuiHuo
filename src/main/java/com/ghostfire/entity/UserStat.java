package com.ghostfire.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/** 用户统计表（金币、发帖数、获赞数等） */
@Data
@TableName("user_stat")
public class UserStat {

    /** 用户 ID（与 sys_user 一对一，手动输入主键） */
    @TableId(type = IdType.INPUT)
    private Long userId;

    /** 金币余额 */
    private Long coin;
    /** 发帖数 */
    private Integer postCount;
    /** 获赞数 */
    private Integer likeCount;
    /** 签到天数 */
    private Integer signCount;
    /** 连续签到天数 */
    private Integer streakCount;
    /** 吹牛次数 */
    private Integer boastCount;
    /** 吹牛获胜次数 */
    private Integer boastWinCount;
    /** 吹牛累计赢取金币 */
    private Long boastWinTotal;
    /** 吹牛单次最大赢取 */
    private Long boastBestWin;
    /** 吹牛单次最大亏损 */
    private Long boastWorstLoss;
}
