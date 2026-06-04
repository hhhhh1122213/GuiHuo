-- =============================================
-- 鬼火论坛 测试数据（直接 SQL 插入）
-- 数据库：PostgreSQL
-- =============================================

-- 1. 插入测试用户（密码是 123456 的 BCrypt 哈希）
INSERT INTO sys_user (username, password, nickname, avatar, role, status) VALUES
    ('testuser1', '$2a$10$EGPO/mnR9skChhEbWIAQQ.1PHF5jDXqQUfo.2TXj76els3OLnTWo6', '码农小张', '/uploads/default/avatar1.png', 'USER', 1),
    ('testuser2', '$2a$10$EGPO/mnR9skChhEbWIAQQ.1PHF5jDXqQUfo.2TXj76els3OLnTWo6', '游戏达人', '/uploads/default/avatar2.png', 'USER', 1),
    ('testuser3', '$2a$10$EGPO/mnR9skChhEbWIAQQ.1PHF5jDXqQUfo.2TXj76els3OLnTWo6', '数码控', '/uploads/default/avatar3.png', 'USER', 1),
    ('alice',     '$2a$10$EGPO/mnR9skChhEbWIAQQ.1PHF5jDXqQUfo.2TXj76els3OLnTWo6', '爱丽丝',   '/uploads/default/avatar4.png', 'USER', 1),
    ('admin',     '$2a$10$EGPO/mnR9skChhEbWIAQQ.1PHF5jDXqQUfo.2TXj76els3OLnTWo6', '管理员',   '/uploads/default/avatar5.png', 'ADMIN', 1)
ON CONFLICT (username) DO NOTHING;

-- 2. 初始化用户统计
INSERT INTO user_stat (user_id, coin, post_count, like_count, sign_count)
SELECT id, 100, 0, 0, 0 FROM sys_user WHERE username IN ('testuser1','testuser2','testuser3','alice','admin')
ON CONFLICT (user_id) DO NOTHING;

-- 3. 插入帖子
-- category_id: 1=技术交流 2=游戏天地 3=数码科技 4=生活杂谈 5=情感树洞
INSERT INTO post (user_id, category_id, title, content, view_count, like_count, comment_count, is_top, is_essence, status) VALUES
    -- testuser1 的帖子
    ((SELECT id FROM sys_user WHERE username='testuser1'), 1,
     'Java 线程池最佳实践',
     '线程池是 Java 并发编程中的重要组件，合理使用可以大幅提升系统性能。

## 核心参数
- corePoolSize: 核心线程数
- maximumPoolSize: 最大线程数
- keepAliveTime: 空闲线程存活时间
- workQueue: 工作队列

## 推荐配置
- CPU密集型: 核心数 + 1
- IO密集型: 核心数 * 2

## 常见坑点
1. 队列设为无界导致 OOM
2. 线程池拒绝策略选择不当
3. shutdown 后忘记 awaitTermination',
     128, 15, 3, FALSE, TRUE, 1),

    ((SELECT id FROM sys_user WHERE username='testuser1'), 1,
     'Spring Boot 3.x 迁移踩坑记录',
     '最近把项目从 Spring Boot 2.x 升级到 3.x，记录一下踩坑点。

## 主要变化
1. Jakarta EE 命名空间变更（javax.* → jakarta.*）
2. Spring Security 配置方式变化
3. 自动配置类调整

## 踩坑清单
- WebSecurityConfigurerAdapter 被废弃，改用 SecurityFilterChain Bean
- @GeneratedValue 策略变化
- 部分 starter 改名
- Flyway 回调接口方法签名变化

总体来说升级后性能有提升，推荐尽快迁移。',
     256, 22, 5, TRUE, TRUE, 1),

    -- testuser2 的帖子
    ((SELECT id FROM sys_user WHERE username='testuser2'), 2,
     '原神 4.7 版本角色强度排行',
     '新版本更新后角色强度变化较大，整理一下个人向排行。

## T0（必练）
- 纳西妲：草反应核心，挂草能力独一档
- 芙宁娜：增伤 + 奶，泛用性极强
- 那维莱特：水龙王，主C天花板

## T1（推荐）
- 钟离：护盾之神，安逸之选
- 雷电将军：充能 + 爆发，雷国核心
- 甘雨：远程冰C，融化/冻结两开花

## T2（看情况）
- 行秋、班尼特、香菱：国家队永不褪色
- 夜兰：水后台之王

有不同意见欢迎评论区讨论！',
     512, 35, 12, FALSE, FALSE, 1),

    ((SELECT id FROM sys_user WHERE username='testuser2'), 2,
     '艾尔登法环 DLC 通关心得',
     '终于打完黄金树幽影了，分享一下通关体验。

整体难度比本体高不少，Boss 设计很有创意但也有些恶心的。碎星拉塔恩的二阶段形态变化让人措手不及。

## 推荐装备
- 武器：血旋镖（出血流神器）
- 护符：碎星的传说
- 灵灰：模仿泪滴

## 各 Boss 难度
1. 神兽舞狮 ★★★☆☆
2. 舞狮王 ★★★★☆
3. 碎星拉塔恩 ★★★★★

DLC 地图设计是宫崎英高的巅峰之作。',
     384, 28, 8, FALSE, FALSE, 1),

    -- testuser3 的帖子
    ((SELECT id FROM sys_user WHERE username='testuser3'), 3,
     '2026年手机推荐 3000元档',
     '最近想换手机，做了大量功课分享给大家。

## 推荐机型

### 小米15
- 骁龙8 Gen4，性能拉满
- 影像系统大幅升级
- 5000mAh + 120W快充

### 一加13
- 性能释放最激进
- 屏幕素质顶级
- 质感不错

### iQOO 13
- 游戏体验最佳
- 独立显示芯片
- 散热优秀

各有所长，按需选择！',
     192, 18, 6, FALSE, FALSE, 1),

    -- alice 的帖子
    ((SELECT id FROM sys_user WHERE username='alice'), 4,
     '今天加班到凌晨，打工人太难了',
     '又是一个不眠之夜，项目下周上线，需求还在改...

老板一句话，下面跑断腿。最离谱的是今天下午才说要加一个导出功能，周末还得来。

大家一般加班到几点？有没有什么摸鱼技巧分享一下',
     640, 42, 25, FALSE, FALSE, 1),

    ((SELECT id FROM sys_user WHERE username='alice'), 1,
     'PostgreSQL 优化实战：慢查询从 8s 到 50ms',
     '分享一个真实案例，一条 SQL 从 8 秒优化到 50 毫秒。

## 原始 SQL
```sql
SELECT * FROM orders WHERE status = 1 AND create_time > ''2026-01-01''
```
全表扫描，8 秒。

## 优化方案
1. 添加组合索引 `(status, create_time)`
2. 避免 SELECT *，只查需要的字段
3. 分页改用游标分页（keyset pagination）

## 优化后
添加索引后直接走 Index Scan，50ms 搞定。

效果立竿见影！分享给有同样困扰的朋友。',
     320, 30, 7, FALSE, TRUE, 1),

    -- admin 的帖子（公告）
    ((SELECT id FROM sys_user WHERE username='admin'), 6,
     '【公告】鬼火论坛正式上线！',
     '欢迎大家来到鬼火论坛！

本站功能：
- 发帖/评论/楼中楼回复
- 点赞/收藏
- 签到领金币
- 私信交流
- 草稿保存
- 红包功能（即将上线）
- 吹牛打赌（即将上线）

有问题请在站务管理板块反馈，祝大家玩得开心！',
     1024, 50, 15, TRUE, FALSE, 1);

-- 4. 更新用户帖子统计
UPDATE user_stat SET post_count = (SELECT COUNT(*) FROM post WHERE user_id = user_stat.user_id AND status = 1);

-- 5. 插入一些评论
INSERT INTO comment (post_id, user_id, content, like_count, status) VALUES
    (1, (SELECT id FROM sys_user WHERE username='testuser2'), '写的很好，学习了！线程池这块一直不太清楚怎么配。', 5, 1),
    (1, (SELECT id FROM sys_user WHERE username='alice'), '推荐用 ThreadPoolTaskExecutor，Spring 封装得更好用。', 3, 1),
    (2, (SELECT id FROM sys_user WHERE username='testuser3'), 'javax 换 jakarta 这个坑确实大，我们项目也遇到了。', 4, 1),
    (3, (SELECT id FROM sys_user WHERE username='alice'), '原神启动！纳西妲永远的神！', 8, 1),
    (6, (SELECT id FROM sys_user WHERE username='testuser1'), '同为打工人，抱抱。学会说"这个需求排到下个迭代"很重要。', 12, 1),
    (8, (SELECT id FROM sys_user WHERE username='testuser2'), '公告收到！期待红包功能上线！', 2, 1);

-- 楼中楼回复
INSERT INTO comment (post_id, user_id, parent_id, reply_user_id, content, like_count, status) VALUES
    (1, (SELECT id FROM sys_user WHERE username='testuser3'), 1, (SELECT id FROM sys_user WHERE username='testuser2'), '赞同，核心数+1 这个配置很实用', 1, 1),
    (6, (SELECT id FROM sys_user WHERE username='testuser2'), 5, (SELECT id FROM sys_user WHERE username='testuser1'), '这个建议太真实了哈哈', 3, 1);

-- 6. 插入一些点赞
INSERT INTO user_like (user_id, target_id, target_type) VALUES
    ((SELECT id FROM sys_user WHERE username='testuser2'), 1, 1),
    ((SELECT id FROM sys_user WHERE username='testuser2'), 2, 1),
    ((SELECT id FROM sys_user WHERE username='testuser2'), 7, 1),
    ((SELECT id FROM sys_user WHERE username='testuser3'), 1, 1),
    ((SELECT id FROM sys_user WHERE username='testuser3'), 3, 1),
    ((SELECT id FROM sys_user WHERE username='testuser3'), 5, 1),
    ((SELECT id FROM sys_user WHERE username='alice'), 1, 1),
    ((SELECT id FROM sys_user WHERE username='alice'), 2, 1),
    ((SELECT id FROM sys_user WHERE username='alice'), 4, 1),
    ((SELECT id FROM sys_user WHERE username='alice'), 6, 1);

-- 更新帖子点赞数
UPDATE post SET like_count = (SELECT COUNT(*) FROM user_like WHERE target_id = post.id AND target_type = 1);

-- 7. 插入几条私信
INSERT INTO t_message (from_user_id, to_user_id, content, status) VALUES
    ((SELECT id FROM sys_user WHERE username='testuser2'), (SELECT id FROM sys_user WHERE username='testuser1'), '你好，你的帖子写得不错！', 0),
    ((SELECT id FROM sys_user WHERE username='alice'), (SELECT id FROM sys_user WHERE username='testuser1'), '大佬带带我学Java！', 0),
    ((SELECT id FROM sys_user WHERE username='alice'), (SELECT id FROM sys_user WHERE username='testuser2'), '你那个原神帖子太有用了', 1);

-- 8. 插入收藏
INSERT INTO user_favorite (user_id, post_id) VALUES
    ((SELECT id FROM sys_user WHERE username='testuser2'), 2),
    ((SELECT id FROM sys_user WHERE username='testuser3'), 1),
    ((SELECT id FROM sys_user WHERE username='alice'), 7);

-- 9. 插入签到记录
INSERT INTO user_check_in (user_id, check_date) VALUES
    ((SELECT id FROM sys_user WHERE username='testuser1'), CURRENT_DATE),
    ((SELECT id FROM sys_user WHERE username='alice'), CURRENT_DATE);

-- 更新签到统计和金币
UPDATE user_stat SET sign_count = 1, coin = coin + 10 WHERE user_id = (SELECT id FROM sys_user WHERE username='testuser1');
UPDATE user_stat SET sign_count = 1, coin = coin + 10 WHERE user_id = (SELECT id FROM sys_user WHERE username='alice');

-- 10. 插入草稿
INSERT INTO draft (user_id, category_id, title, content) VALUES
    ((SELECT id FROM sys_user WHERE username='testuser1'), 1, 'Docker 入门教程（草稿）', '还在写...'),
    ((SELECT id FROM sys_user WHERE username='testuser1'), 4, '周末去哪玩（草稿）', '待补充...');

-- 14. 勋章定义
INSERT INTO medal (name, icon, description, rule_type, rule_field, rule_value) VALUES
    ('初出茅庐', NULL, '发布第一篇帖子', 'AUTO_STAT', 'postCount', 1),
    ('小有名气', NULL, '累计获赞100', 'AUTO_STAT', 'likeCount', 100),
    ('百万富翁', NULL, '金币达到100000', 'AUTO_STAT', 'coin', 100000),
    ('签到达人', NULL, '累计签到30天', 'AUTO_STAT', 'signCount', 30),
    ('特别荣誉', NULL, '管理员手动授予', 'MANUAL', NULL, NULL);

-- 完成
SELECT '测试数据插入完成！' AS result;
