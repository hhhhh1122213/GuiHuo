package com.ghostfire.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghostfire.common.Constant;
import com.ghostfire.entity.Message;
import com.ghostfire.entity.User;
import com.ghostfire.mapper.MessageMapper;
import com.ghostfire.service.MessageService;
import com.ghostfire.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final UserService userService;

    @Override
    public void send(Long fromUserId, Long toUserId, String content) {
        User toUser = userService.getById(toUserId);
        if (toUser == null) {
            throw new RuntimeException("收件人不存在");
        }
        Message msg = new Message();
        msg.setFromUserId(fromUserId);
        msg.setToUserId(toUserId);
        msg.setContent(content);
        msg.setStatus(Constant.MSG_UNREAD);
        save(msg);
    }

    @Override
    public Page<Message> pageReceived(Long userId, int page, int size) {
        Page<Message> p = new Page<>(page, size);
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId, userId)
                .orderByDesc(Message::getCreateTime);
        return page(p, w);
    }

    @Override
    public Page<Message> pageSent(Long userId, int page, int size) {
        Page<Message> p = new Page<>(page, size);
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<Message>()
                .eq(Message::getFromUserId, userId)
                .orderByDesc(Message::getCreateTime);
        return page(p, w);
    }

    @Override
    public int unreadCount(Long userId) {
        return (int) count(new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId, userId)
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
}
