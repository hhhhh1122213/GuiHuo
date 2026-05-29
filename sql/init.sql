-- =============================================
-- 鬼火论坛系统 数据库初始化脚本
-- 数据库：PostgreSQL
-- =============================================

-- =============================================
-- 一、用户与账户模块
-- =============================================

-- 1. 用户主表
CREATE TABLE sys_user (
    id          BIGSERIAL    PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,
    nickname    VARCHAR(50)  NULL,
    avatar      VARCHAR(255) NULL,
    role        VARCHAR(20)  DEFAULT 'USER',
    status      SMALLINT     DEFAULT 1,
    source      VARCHAR(10)  DEFAULT 'web',
    openid      VARCHAR(64)  UNIQUE,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- 2. 用户统计表
CREATE TABLE user_stat (
    user_id          BIGINT PRIMARY KEY REFERENCES sys_user(id),
    coin             BIGINT DEFAULT 0,
    post_count       INT    DEFAULT 0,
    like_count       INT    DEFAULT 0,
    sign_count       INT    DEFAULT 0,
    streak_count     INT    DEFAULT 0,
    boast_count      INT    DEFAULT 0,
    boast_win_count  INT    DEFAULT 0,
    boast_win_total  BIGINT DEFAULT 0,
    boast_best_win   BIGINT DEFAULT 0,
    boast_worst_loss BIGINT DEFAULT 0
);

-- 3. 钱包流水表
CREATE TABLE user_wallet_log (
    id              BIGSERIAL    PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES sys_user(id),
    amount          BIGINT       NOT NULL,
    current_balance BIGINT       NOT NULL,
    type            VARCHAR(50)  NOT NULL,
    ref_id          BIGINT       NULL,
    create_time     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 二、互动与荣誉模块
-- =============================================

-- 4. 签到记录表
CREATE TABLE user_check_in (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT    NOT NULL REFERENCES sys_user(id),
    check_date  DATE      NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, check_date)
);

-- 5. 勋章表
CREATE TABLE medal (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    icon        VARCHAR(255) NULL,
    description VARCHAR(200) NULL,
    rule_type   VARCHAR(50)  NULL,
    rule_field  VARCHAR(50)  NULL,
    rule_value  INT          NULL
);

-- 6. 用户勋章关联表
CREATE TABLE user_medal (
    user_id     BIGINT    NOT NULL REFERENCES sys_user(id),
    medal_id    BIGINT    NOT NULL REFERENCES medal(id),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, medal_id)
);

-- 6. 用户关注关系表
CREATE TABLE user_follow (
    follower_id BIGINT    NOT NULL REFERENCES sys_user(id) ON DELETE CASCADE,
    followee_id BIGINT    NOT NULL REFERENCES sys_user(id) ON DELETE CASCADE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, followee_id),
    CHECK (follower_id <> followee_id)
);

-- =============================================
-- 三、内容核心模块（需在红包模块之前创建，因为红包引用帖子）
-- =============================================

-- 7. 板块分类表
CREATE TABLE category (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(200) NULL,
    sort_order  INT          DEFAULT 0,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- 8. 帖子主表
CREATE TABLE post (
    id            BIGSERIAL    PRIMARY KEY,
    user_id       BIGINT       NOT NULL REFERENCES sys_user(id),
    category_id   BIGINT       NOT NULL REFERENCES category(id),
    title         VARCHAR(150) NOT NULL,
    content       TEXT         NOT NULL,
    view_count    INT          DEFAULT 0,
    like_count    INT          DEFAULT 0,
    comment_count INT          DEFAULT 0,
    is_top        BOOLEAN      DEFAULT FALSE,
    is_essence    BOOLEAN      DEFAULT FALSE,
    status        SMALLINT     DEFAULT 1,
    create_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- 9. 标签表
CREATE TABLE tag (
    id   BIGSERIAL   PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
);

-- 10. 帖子标签关联表
CREATE TABLE post_tag (
    post_id BIGINT NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    tag_id  BIGINT NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, tag_id)
);

-- 11. 评论表
CREATE TABLE comment (
    id            BIGSERIAL PRIMARY KEY,
    post_id       BIGINT    NOT NULL REFERENCES post(id),
    user_id       BIGINT    NOT NULL REFERENCES sys_user(id),
    parent_id     BIGINT    NULL REFERENCES comment(id),
    reply_user_id BIGINT    NULL REFERENCES sys_user(id),
    content       TEXT      NOT NULL,
    like_count    INT       DEFAULT 0,
    status        SMALLINT  DEFAULT 1,
    create_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 12. 通用点赞表
CREATE TABLE user_like (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT    NOT NULL REFERENCES sys_user(id),
    target_id   BIGINT    NOT NULL,
    target_type SMALLINT  NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, target_id, target_type)
);

-- 13. 收藏帖子表
CREATE TABLE user_favorite (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT    NOT NULL REFERENCES sys_user(id),
    post_id     BIGINT    NOT NULL REFERENCES post(id),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, post_id)
);

-- =============================================
-- 四、红包模块
-- =============================================

-- 13. 红包主表
CREATE TABLE red_packet (
    id           BIGSERIAL    PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES sys_user(id),
    post_id      BIGINT       NULL REFERENCES post(id),
    total_amount BIGINT       NOT NULL,
    total_count  INT          NOT NULL,
    remain_count INT          NOT NULL,
    type         SMALLINT     DEFAULT 1,
    status       SMALLINT     DEFAULT 1,
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    expire_time  TIMESTAMP    NULL
);

-- 14. 抢红包记录表
CREATE TABLE red_packet_record (
    id          BIGSERIAL PRIMARY KEY,
    packet_id   BIGINT    NOT NULL REFERENCES red_packet(id),
    user_id     BIGINT    NOT NULL REFERENCES sys_user(id),
    amount      BIGINT    NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (packet_id, user_id)
);

-- =============================================
-- 五、吹牛打赌模块
-- =============================================

-- 15. 吹牛主表
CREATE TABLE boast (
    id             BIGSERIAL    PRIMARY KEY,
    user_id        BIGINT       NOT NULL REFERENCES sys_user(id),
    title          VARCHAR(150) NOT NULL,
    image          VARCHAR(255) NULL,
    option_one     VARCHAR(100) NOT NULL,
    option_two     VARCHAR(100) NOT NULL,
    correct_option SMALLINT     NOT NULL,
    stake_amount   BIGINT       NOT NULL,
    result         SMALLINT     DEFAULT 0,
    deadline       TIMESTAMP    NOT NULL,
    create_time    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- 16. 挑战者下注记录表
CREATE TABLE boast_bet (
    id          BIGSERIAL PRIMARY KEY,
    boast_id    BIGINT    NOT NULL REFERENCES boast(id),
    user_id     BIGINT    NOT NULL REFERENCES sys_user(id),
    option_type SMALLINT  NOT NULL,
    amount      BIGINT    NOT NULL,
    result      SMALLINT  DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (boast_id, user_id)
);

-- =============================================
-- 六、扩展模块
-- =============================================

-- 17. 草稿表
CREATE TABLE draft (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES sys_user(id),
    category_id BIGINT       NULL,
    title       VARCHAR(150) NULL,
    content     TEXT         NULL,
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- 18. 消息表
CREATE TABLE t_message (
    id           BIGSERIAL PRIMARY KEY,
    from_user_id BIGINT    NOT NULL REFERENCES sys_user(id),
    to_user_id   BIGINT    NOT NULL REFERENCES sys_user(id),
    content      TEXT      NOT NULL,
    type         SMALLINT  DEFAULT 1,
    target_id    BIGINT    NULL,
    status       SMALLINT  DEFAULT 0,
    create_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- 索引
-- =============================================

-- 钱包流水：按用户查流水、按业务类型筛选
CREATE INDEX idx_wallet_log_user_id ON user_wallet_log (user_id);
CREATE INDEX idx_wallet_log_type ON user_wallet_log (type);

-- 帖子：按板块查帖子、按用户查帖子、按时间排序、常见筛选组合
CREATE INDEX idx_post_category_id ON post (category_id);
CREATE INDEX idx_post_user_id ON post (user_id);
CREATE INDEX idx_post_create_time ON post (create_time DESC);
CREATE INDEX idx_post_status_top_essence ON post (status, is_top, is_essence);

-- 评论：按帖子查评论、楼中楼查询
CREATE INDEX idx_comment_post_id ON comment (post_id);
CREATE INDEX idx_comment_parent_id ON comment (parent_id);

-- 点赞：按目标查点赞（查某帖子/评论被谁赞了）
CREATE INDEX idx_user_like_target ON user_like (target_id, target_type);

-- 红包：按用户查红包、按状态筛选
CREATE INDEX idx_red_packet_user_id ON red_packet (user_id);
CREATE INDEX idx_red_packet_status ON red_packet (status);
CREATE INDEX idx_red_packet_record_user_id ON red_packet_record (user_id);

-- 吹牛：按用户查挑战、按状态筛选、按截止时间排序
CREATE INDEX idx_boast_user_id ON boast (user_id);
CREATE INDEX idx_boast_result ON boast (result);
CREATE INDEX idx_boast_deadline ON boast (deadline);
CREATE INDEX idx_boast_bet_user_id ON boast_bet (user_id);

-- 消息：收件箱查询、按发送者查询
CREATE INDEX idx_message_to_user_id ON t_message (to_user_id, status);
CREATE INDEX idx_message_from_user_id ON t_message (from_user_id);

-- 草稿：按用户查草稿
CREATE INDEX idx_draft_user_id ON draft (user_id);

-- 收藏：按用户查收藏
CREATE INDEX idx_favorite_user_id ON user_favorite (user_id);

-- 帖子全文搜索：GIN 索引 + tsvector 生成列
ALTER TABLE post ADD COLUMN IF NOT EXISTS search_vector tsvector
    GENERATED ALWAYS AS (to_tsvector('simple', title || ' ' || content)) STORED;
CREATE INDEX idx_post_search ON post USING GIN (search_vector);

-- =============================================
-- 初始数据：板块分类
-- =============================================

INSERT INTO category (name, description, sort_order) VALUES
    ('技术交流', 'Java、Python、前端、后端等技术讨论', 1),
    ('游戏天地', '手游、端游、主机、电竞等游戏话题', 2),
    ('数码科技', '手机、电脑、相机等数码产品讨论', 3),
    ('生活杂谈', '闲聊、吐槽、问答等日常话题', 4),
    ('情感树洞', '相亲、初恋等情感故事分享', 5),
    ('站务管理', '公告、活动等站内事务', 6);

-- =============================================
-- 初始数据：标签
-- =============================================

INSERT INTO tag (name) VALUES
    ('Java'), ('Python'), ('前端'), ('后端'), ('数据库'), ('Linux'),
    ('吐槽'), ('问答'), ('求助'), ('闲聊'), ('灌水'), ('秀操作'),
    ('原创'), ('转载'), ('教程'), ('资源分享'), ('新闻'),
    ('精华'), ('公告'), ('活动'),
    ('相亲'), ('初恋'),
    ('手游'), ('端游'), ('主机'), ('Steam'), ('电竞'), ('攻略'),
    ('手机'), ('电脑'), ('相机'), ('耳机'), ('智能穿戴');

-- =============================================
-- 兼容已有库：补列（IF NOT EXISTS 已有则跳过）
-- =============================================
ALTER TABLE user_stat ADD COLUMN IF NOT EXISTS streak_count INT DEFAULT 0;
ALTER TABLE medal ADD COLUMN IF NOT EXISTS rule_field VARCHAR(50) NULL;
ALTER TABLE t_message ADD COLUMN IF NOT EXISTS type SMALLINT DEFAULT 1;
ALTER TABLE t_message ADD COLUMN IF NOT EXISTS target_id BIGINT NULL;
ALTER TABLE t_message ADD COLUMN IF NOT EXISTS from_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE t_message ADD COLUMN IF NOT EXISTS to_deleted BOOLEAN DEFAULT FALSE;
UPDATE t_message SET from_deleted = FALSE WHERE from_deleted IS NULL;
UPDATE t_message SET to_deleted = FALSE WHERE to_deleted IS NULL;
CREATE INDEX IF NOT EXISTS idx_message_type ON t_message (to_user_id, type, status);
CREATE TABLE IF NOT EXISTS user_follow (
    follower_id BIGINT    NOT NULL REFERENCES sys_user(id) ON DELETE CASCADE,
    followee_id BIGINT    NOT NULL REFERENCES sys_user(id) ON DELETE CASCADE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, followee_id),
    CHECK (follower_id <> followee_id)
);
CREATE INDEX IF NOT EXISTS idx_user_follow_followee ON user_follow (followee_id);
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS source VARCHAR(10) DEFAULT 'web';
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS openid VARCHAR(64) UNIQUE;
