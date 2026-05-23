package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.common.Constant;
import com.ghostfire.entity.Message;
import com.ghostfire.entity.User;
import com.ghostfire.handler.SseConnectionManager;
import com.ghostfire.mapper.MessageMapper;
import com.ghostfire.service.MessageService;
import com.ghostfire.service.UserService;
import com.ghostfire.vo.ConversationVO;
import com.ghostfire.vo.NotificationCategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final UserService userService;
    private final SseConnectionManager sseConnectionManager;

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
        // SSE 实时推送
        try {
            sseConnectionManager.sendToUser(toUserId, getUnreadStats(toUserId));
        } catch (Exception e) {
            log.debug("SSE 推送失败: userId={}", toUserId, e);
        }
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
        if (msg != null && msg.getToUserId().equals(userId)) {
            msg.setStatus(Constant.MSG_READ);
            updateById(msg);
        }
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
        }
    }

    @Override
    public void markAllRead(long id) {
        update(new LambdaUpdateWrapper<Message>()
                .eq(Message::getToUserId, id)
                .eq(Message::getToDeleted, false)
                .eq(Message::getStatus, Constant.MSG_UNREAD)
                .set(Message::getStatus, Constant.MSG_READ));
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
    }

    @Override
    public Map<String, Integer> getUnreadStats(Long userId) {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("mentionPost", unreadCountByType(userId, Constant.MSG_TYPE_MENTION_POST));
        stats.put("mentionComment", unreadCountByType(userId, Constant.MSG_TYPE_MENTION_COMMENT));
        stats.put("like", unreadCountByType(userId, Constant.MSG_TYPE_LIKE));
        stats.put("follow", unreadCountByType(userId, Constant.MSG_TYPE_FOLLOW));
        stats.put("private", unreadCountByType(userId, Constant.MSG_TYPE_PRIVATE));
        stats.put("system", unreadCountByType(userId, Constant.MSG_TYPE_SYSTEM));
        stats.put("total", unreadCount(userId));
        return stats;
    }

}
