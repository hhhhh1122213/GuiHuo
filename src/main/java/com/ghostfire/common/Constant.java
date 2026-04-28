package com.ghostfire.common;

public interface Constant {

    /** 帖子状态 */
    int POST_STATUS_NORMAL = 1;
    int POST_STATUS_DELETED = 0;

    /** 评论状态 */
    int COMMENT_STATUS_NORMAL = 1;
    int COMMENT_STATUS_DELETED = 0;

    /** 用户状态 */
    int USER_STATUS_NORMAL = 1;
    int USER_STATUS_BANNED = 0;

    /** 红包状态 */
    int RED_PACKET_ACTIVE = 1;
    int RED_PACKET_FINISHED = 2;
    int RED_PACKET_EXPIRED = 3;

    /** 红包类型 */
    int RED_PACKET_RANDOM = 1;
    int RED_PACKET_AVERAGE = 2;

    /** 吹牛结果 */
    int BOAST_ONGOING = 0;
    int BOAST_CREATOR_WIN = 1;
    int BOAST_CALLER_WIN = 2;

    /** 吹牛站队 */
    int BOAST_BET_BELIEVE = 1;
    int BOAST_BET_FAKE = 2;

    /** 钱包流水类型 */
    String WALLET_SIGN_IN = "SIGN_IN";
    String WALLET_POST = "POST";
    String WALLET_LIKE = "LIKE";
    String WALLET_RED_PACKET = "RED_PACKET";
    String WALLET_BOAST_BET = "BOAST_BET";
    String WALLET_BOAST_WIN = "BOAST_WIN";

    /** 勋章规则 */
    String MEDAL_MANUAL = "MANUAL";
    String MEDAL_AUTO_STAT = "AUTO_STAT";

    /** 消息状态 */
    int MSG_UNREAD = 0;
    int MSG_READ = 1;

    /** 点赞目标类型 */
    int LIKE_POST = 1;
    int LIKE_COMMENT = 2;
}
