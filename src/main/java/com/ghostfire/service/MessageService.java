package com.ghostfire.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ghostfire.entity.Message;
import com.ghostfire.vo.ConversationVO;
import com.ghostfire.vo.NotificationCategoryVO;

import java.util.List;
import java.util.Map;

public interface MessageService extends IService<Message> {

    void send(Long fromUserId, Long toUserId, String content);

    void send(Long fromUserId, Long toUserId, String content, Integer type, Long targetId);

    Page<Message> pageReceived(Long userId, int page, int size);

    Page<Message> pageSent(Long userId, int page, int size);

    int unreadCount(Long userId);

    void markRead(Long messageId, Long userId);
    void markReadBatch(Long userId, List<Long> ids);
    void delete(Long messageId, Long userId);
    void markAllRead(long id);

    List<ConversationVO> getConversations(Long userId);

    Page<Message> getChatHistory(Long userId, Long targetUserId, int page, int size);

    void markAllReadFromUser(Long userId, Long targetUserId);

    List<NotificationCategoryVO> getNotificationCategories(Long userId);

    Page<Message> pageByType(Long userId, Integer type, int page, int size);

    int unreadCountByType(Long userId, Integer type);

    void markAllReadByType(Long userId, Integer type);

    Map<String, Integer> getUnreadStats(Long userId);
}
