package com.ghostfire.common;

public interface Constant {

    /** 帖子状态 */
    /** 正常可见 */
    int POST_STATUS_NORMAL = 1;
    /** 已删除 */
    int POST_STATUS_DELETED = 0;

    /** 评论状态 */
    /** 正常可见 */
    int COMMENT_STATUS_NORMAL = 1;
    /** 已删除 */
    int COMMENT_STATUS_DELETED = 0;

    /** 用户状态 */
    /** 正常使用 */
    int USER_STATUS_NORMAL = 1;
    /** 已封禁 */
    int USER_STATUS_BANNED = 0;

    /** 红包状态 */
    /** 红包进行中，可领取 */
    int RED_PACKET_ACTIVE = 1;
    /** 红包已被领完 */
    int RED_PACKET_FINISHED = 2;
    /** 红包已过期 */
    int RED_PACKET_EXPIRED = 3;

    /** 红包类型 */
    /** 随机金额红包 */
    int RED_PACKET_RANDOM = 1;
    /** 平均金额红包 */
    int RED_PACKET_AVERAGE = 2;

    /** 吹牛结果 */
    /** 挑战进行中 */
    int BOAST_ONGOING = 0;
    /** 发布者获胜（挑战者答错） */
    int BOAST_CREATOR_WIN = 1;
    /** 挑战者获胜（挑战者答对，获得90%赌注） */
    int BOAST_CALLER_WIN = 2;

    /** 吹牛选项 */
    /** 选项一 */
    int BOAST_OPTION_ONE = 1;
    /** 选项二 */
    int BOAST_OPTION_TWO = 2;

    /** 吹牛下注结算结果 */
    /** 未结算 */
    int BOAST_BET_UNSETTLED = 0;
    /** 赢（答对） */
    int BOAST_BET_WIN = 1;
    /** 输（答错） */
    int BOAST_BET_LOSE = 2;

    /** 钱包流水类型 */
    /** 签到奖励 */
    String WALLET_SIGN_IN = "SIGN_IN";
    /** 发帖奖励 */
    String WALLET_POST = "POST";
    /** 点赞奖励 */
    String WALLET_LIKE = "LIKE";
    /** 发红包扣款 */
    String WALLET_RED_PACKET_SEND = "RED_PACKET_SEND";
    /** 抢红包入账 */
    String WALLET_RED_PACKET_RECEIVE = "RED_PACKET_RECEIVE";
    /** 吹牛下注 */
    String WALLET_BOAST_BET = "BOAST_BET";
    /** 吹牛获胜奖励 */
    String WALLET_BOAST_WIN = "BOAST_WIN";

    /** 勋章规则 */
    /** 手动授予 */
    String MEDAL_MANUAL = "MANUAL";
    /** 根据统计数据自动授予 */
    String MEDAL_AUTO_STAT = "AUTO_STAT";

    /** 消息状态 */
    /** 未读 */
    int MSG_UNREAD = 0;
    /** 已读 */
    int MSG_READ = 1;

    /** 消息类型 */
    /** 私信 */
    int MSG_TYPE_PRIVATE = 1;
    /** @我的动态 */
    int MSG_TYPE_MENTION_POST = 2;
    /** @我的评论 */
    int MSG_TYPE_MENTION_COMMENT = 3;
    /** 收到的赞 */
    int MSG_TYPE_LIKE = 4;
    /** 关注通知 */
    int MSG_TYPE_FOLLOW = 5;
    /** 系统通知 */
    int MSG_TYPE_SYSTEM = 6;

    /** 点赞目标类型 */
    /** 点赞帖子 */
    int LIKE_POST = 1;
    /** 点赞评论 */
    int LIKE_COMMENT = 2;

    /** 帖子状态（扩展） */
    int POST_STATUS_PENDING = 2;   // 待审核
    int POST_STATUS_REJECTED = 3;  // 审核拒绝

    /** 评论状态（扩展） */
    int COMMENT_STATUS_PENDING = 2;
    int COMMENT_STATUS_REJECTED = 3;

    /** 举报目标类型 */
    int REPORT_TARGET_POST = 1;
    int REPORT_TARGET_COMMENT = 2;

    /** 举报状态 */
    int REPORT_STATUS_PENDING = 0;
    int REPORT_STATUS_CONFIRMED = 1;
    int REPORT_STATUS_DISMISSED = 2;

    /** 金币奖励 */
    /** 发帖奖励 */
    long COIN_POST_REWARD = 10L;
    /** 签到奖励 */
    long COIN_CHECKIN_REWARD = 10L;
    /** 点赞奖励 */
    long COIN_LIKE_REWARD = 2L;
}
