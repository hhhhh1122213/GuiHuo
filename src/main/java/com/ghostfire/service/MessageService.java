package com.ghostfire.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ghostfire.entity.Message;

public interface MessageService extends IService<Message> {

    void send(Long fromUserId, Long toUserId, String content);

    Page<Message> pageReceived(Long userId, int page, int size);

    Page<Message> pageSent(Long userId, int page, int size);

    int unreadCount(Long userId);

    void markRead(Long messageId, Long userId);
}
