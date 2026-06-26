package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.common.Constant;
import com.ghostfire.entity.Message;
import com.ghostfire.entity.User;
import com.ghostfire.mapper.MessageMapper;
import com.ghostfire.service.MessageService;
import com.ghostfire.service.UserService;
import com.ghostfire.vo.ConversationVO;
import com.ghostfire.vo.NotificationCategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final UserService userService;
    private final NotificationPublisher notificationPublisher;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String UNREAD_CACHE_PREFIX = "notify:unread:";
    private static final Duration UNREAD_CACHE_TTL = Duration.ofDays(7);

    @Override
    public void send(Long fromUserId, Long toUserId, String content) {
        send(fromUserId, toUserId, content, Constant.MSG_TYPE_PRIVATE, null);
    }

    @Override
    public void send(Long fromUserId, Long toUserId, String content, Integer type, Long targetId) {
        User toUser = userService.getById(toUserId);
        if (toUser == null) {
            throw new RuntimeException("收件人不存在");
        }
        Message msg = new Message();
        msg.setFromUserId(fromUserId);
        msg.setToUserId(toUserId);
        msg.setContent(content);
        msg.setType(type != null ? type : Constant.MSG_TYPE_PRIVATE);
        msg.setTargetId(targetId);
        msg.setStatus(Constant.MSG_UNREAD);
        msg.setFromDeleted(false);
        msg.setToDeleted(false);
        save(msg);

        // Redis Hash 增量更新未读统计
        if (type != null) {
            String key = UNREAD_CACHE_PREFIX + toUserId;
            String field = typeToKey(type);
            if (field != null) {
                redisTemplate.opsForHash().increment(key, field, 1);
                redisTemplate.opsForHash().increment(key, "total", 1);
                redisTemplate.expire(key, UNREAD_CACHE_TTL);
            }
        }

        // 异步 SSE 推送（通过事件发布解耦，避免主线程被 IO 阻塞）
        notificationPublisher.notifyUser(toUserId);
    }

    @Override
    public Page<Message> pageReceived(Long userId, int page, int size) {
        Page<Message> p = new Page<>(page, size);
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId, userId)
                .eq(Message::getToDeleted, false)
                .orderByDesc(Message::getCreateTime);
        return page(p, w);
    }

    @Override
    public Page<Message> pageSent(Long userId, int page, int size) {
        Page<Message> p = new Page<>(page, size);
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<Message>()
                .eq(Message::getFromUserId, userId)
                .eq(Message::getFromDeleted, false)
                .orderByDesc(Message::getCreateTime);
        return page(p, w);
    }

    @Override
    public int unreadCount(Long userId) {
        return (int) count(new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId, userId)
                .eq(Message::getToDeleted, false)
                .eq(Message::getStatus, Constant.MSG_UNREAD));
    }

    @Override
    public void markRead(Long messageId, Long userId) {
        Message msg = getById(messageId);
        if (msg != null && msg.getToUserId().equals(userId) && msg.getStatus() == Constant.MSG_UNREAD) {
            msg.setStatus(Constant.MSG_READ);
            updateById(msg);

            // Redis 缓存同步扣减
            String field = typeToKey(msg.getType());
            if (field != null) {
                String key = UNREAD_CACHE_PREFIX + userId;
                redisTemplate.opsForHash().increment(key, field, -1);
                redisTemplate.opsForHash().increment(key, "total", -1);
            }
        }
    }

    @Override
    public void markReadBatch(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        update(new LambdaUpdateWrapper<Message>()
                .in(Message::getId, ids)
                .eq(Message::getToUserId, userId)
                .eq(Message::getStatus, Constant.MSG_UNREAD)
                .set(Message::getStatus, Constant.MSG_READ));
        // 批量已读后清除缓存
        redisTemplate.delete(UNREAD_CACHE_PREFIX + userId);
    }

    @Override
    public void delete(Long messageId, Long userId) {
        Message msg = getById(messageId);
        if (msg == null) {
            return;
        }
        LambdaUpdateWrapper<Message> w = new LambdaUpdateWrapper<Message>()
                .eq(Message::getId, messageId);
        boolean canDelete = false;
        if (userId.equals(msg.getFromUserId())) {
            w.set(Message::getFromDeleted, true);
            canDelete = true;
        }
        if (userId.equals(msg.getToUserId())) {
            w.set(Message::getToDeleted, true);
            canDelete = true;
        }
        if (canDelete) {
            update(w);
            // 收件人删除未读消息 → 清除缓存
            if (userId.equals(msg.getToUserId()) && msg.getStatus() == Constant.MSG_UNREAD) {
                redisTemplate.delete(UNREAD_CACHE_PREFIX + userId);
            }
        }
    }

    @Override
    public void markAllRead(long id) {
        update(new LambdaUpdateWrapper<Message>()
                .eq(Message::getToUserId, id)
                .eq(Message::getToDeleted, false)
                .eq(Message::getStatus, Constant.MSG_UNREAD)
                .set(Message::getStatus, Constant.MSG_READ));
        // 批量已读后清除缓存，下次读取时从 DB 重新加载
        redisTemplate.delete(UNREAD_CACHE_PREFIX + id);
    }

    @Override
    public List<ConversationVO> getConversations(Long userId) {
        List<ConversationVO> conversations = baseMapper.selectConversations(userId);
        for (ConversationVO c : conversations) {
            c.setUnreadCount(baseMapper.countUnreadFromUser(userId, c.getTargetUserId()));
        }
        return conversations;
    }

    @Override
    public Page<Message> getChatHistory(Long userId, Long targetUserId, int page, int size) {
        Page<Message> p = new Page<>(page, size);
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<Message>()
                .eq(Message::getType, Constant.MSG_TYPE_PRIVATE)
                .and(wrapper -> wrapper
                        .and(w1 -> w1.eq(Message::getFromUserId, userId)
                                .eq(Message::getToUserId, targetUserId)
                                .eq(Message::getFromDeleted, false))
                        .or(w2 -> w2.eq(Message::getFromUserId, targetUserId)
                                .eq(Message::getToUserId, userId)
                                .eq(Message::getToDeleted, false)))
                .orderByDesc(Message::getCreateTime);
        return page(p, w);
    }

    @Override
    public void markAllReadFromUser(Long userId, Long targetUserId) {
        update(new LambdaUpdateWrapper<Message>()
                .eq(Message::getFromUserId, targetUserId)
                .eq(Message::getToUserId, userId)
                .eq(Message::getType, Constant.MSG_TYPE_PRIVATE)
                .eq(Message::getToDeleted, false)
                .eq(Message::getStatus, Constant.MSG_UNREAD)
                .set(Message::getStatus, Constant.MSG_READ));
        // 批量已读后清除缓存，下次读取时从 DB 重新加载
        redisTemplate.delete(UNREAD_CACHE_PREFIX + userId);
    }

    @Override
    public List<NotificationCategoryVO> getNotificationCategories(Long userId) {
        List<NotificationCategoryVO> list = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd HH:mm");

        addCategory(list, Constant.MSG_TYPE_MENTION_POST, "@我的动态", "at",
                unreadCountByType(userId, Constant.MSG_TYPE_MENTION_POST),
                getLastMessage(userId, Constant.MSG_TYPE_MENTION_POST), fmt);
        addCategory(list, Constant.MSG_TYPE_MENTION_COMMENT, "@我的评论", "comment",
                unreadCountByType(userId, Constant.MSG_TYPE_MENTION_COMMENT),
                getLastMessage(userId, Constant.MSG_TYPE_MENTION_COMMENT), fmt);
        addCategory(list, Constant.MSG_TYPE_LIKE, "收到的赞", "like",
                unreadCountByType(userId, Constant.MSG_TYPE_LIKE),
                getLastMessage(userId, Constant.MSG_TYPE_LIKE), fmt);
        addCategory(list, Constant.MSG_TYPE_FOLLOW, "关注通知", "follow",
                unreadCountByType(userId, Constant.MSG_TYPE_FOLLOW),
                getLastMessage(userId, Constant.MSG_TYPE_FOLLOW), fmt);
        addCategory(list, Constant.MSG_TYPE_PRIVATE, "私信", "private",
                unreadCountByType(userId, Constant.MSG_TYPE_PRIVATE),
                getLastMessage(userId, Constant.MSG_TYPE_PRIVATE), fmt);
        addCategory(list, Constant.MSG_TYPE_SYSTEM, "系统通知", "system",
                unreadCountByType(userId, Constant.MSG_TYPE_SYSTEM),
                getLastMessage(userId, Constant.MSG_TYPE_SYSTEM), fmt);

        return list;
    }

    private void addCategory(List<NotificationCategoryVO> list, int type, String name, String icon,
                             int unread, Message lastMsg, DateTimeFormatter fmt) {
        NotificationCategoryVO vo = new NotificationCategoryVO();
        vo.setType(type);
        vo.setName(name);
        vo.setIcon(icon);
        vo.setUnreadCount(unread);
        if (lastMsg != null) {
            vo.setLastMessage(lastMsg.getContent());
            if (lastMsg.getCreateTime() != null) {
                vo.setLastMessageTime(lastMsg.getCreateTime().format(fmt));
            }
        }
        list.add(vo);
    }

    private Message getLastMessage(Long userId, Integer type) {
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId, userId)
                .eq(Message::getType, type)
                .eq(Message::getToDeleted, false)
                .orderByDesc(Message::getCreateTime)
                .last("LIMIT 1");
        List<Message> list = list(w);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public Page<Message> pageByType(Long userId, Integer type, int page, int size) {
        Page<Message> p = new Page<>(page, size);
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId, userId)
                .eq(Message::getType, type)
                .eq(Message::getToDeleted, false)
                .orderByDesc(Message::getCreateTime);
        return page(p, w);
    }

    @Override
    public int unreadCountByType(Long userId, Integer type) {
        return (int) count(new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId, userId)
                .eq(Message::getType, type)
                .eq(Message::getToDeleted, false)
                .eq(Message::getStatus, Constant.MSG_UNREAD));
    }

    @Override
    public void markAllReadByType(Long userId, Integer type) {
        update(new LambdaUpdateWrapper<Message>()
                .eq(Message::getToUserId, userId)
                .eq(Message::getType, type)
                .eq(Message::getToDeleted, false)
                .eq(Message::getStatus, Constant.MSG_UNREAD)
                .set(Message::getStatus, Constant.MSG_READ));
        // 批量已读后清除缓存，下次读取时从 DB 重新加载
        redisTemplate.delete(UNREAD_CACHE_PREFIX + userId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Integer> getUnreadStats(Long userId) {
        // 优先读 Redis 缓存
        String key = UNREAD_CACHE_PREFIX + userId;
        Map<Object, Object> cached = redisTemplate.opsForHash().entries(key);
        if (!cached.isEmpty()) {
            return cached.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> (String) e.getKey(),
                            e -> ((Number) e.getValue()).intValue(),
                            (a, b) -> a,
                            LinkedHashMap::new));
        }

        // 缓存未命中 → 从 DB 加载
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("mentionPost", 0);
        stats.put("mentionComment", 0);
        stats.put("like", 0);
        stats.put("follow", 0);
        stats.put("private", 0);
        stats.put("system", 0);

        int total = 0;
        for (Map<String, Object> row : baseMapper.selectUnreadStats(userId)) {
            Integer type = ((Number) row.get("type")).intValue();
            Integer cnt = ((Number) row.get("cnt")).intValue();
            total += cnt;
            String field = typeToKey(type);
            if (field != null) stats.put(field, cnt);
        }
        stats.put("total", total);

        // 回写 Redis 缓存
        redisTemplate.opsForHash().putAll(key, stats);
        redisTemplate.expire(key, UNREAD_CACHE_TTL);

        return stats;
    }

    private static String typeToKey(Integer type) {
        return switch (type) {
            case Constant.MSG_TYPE_MENTION_POST -> "mentionPost";
            case Constant.MSG_TYPE_MENTION_COMMENT -> "mentionComment";
            case Constant.MSG_TYPE_LIKE -> "like";
            case Constant.MSG_TYPE_FOLLOW -> "follow";
            case Constant.MSG_TYPE_PRIVATE -> "private";
            case Constant.MSG_TYPE_SYSTEM -> "system";
            default -> null;
        };
    }

}
