package com.ghostfire.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghostfire.common.Constant;
import com.ghostfire.common.Result;
import com.ghostfire.dto.MessageDto;
import com.ghostfire.entity.Message;
import com.ghostfire.service.MessageService;
import com.ghostfire.vo.ConversationVO;
import com.ghostfire.vo.NotificationCategoryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    public Result<?> send(@Valid @RequestBody MessageDto dto) {
        long userId = StpUtil.getLoginIdAsLong();
        if (dto.getToUserId().equals(userId)) {
            return Result.fail("不能给自己发消息");
        }
        messageService.send(userId, dto.getToUserId(), dto.getContent(), Constant.MSG_TYPE_PRIVATE, null);
        return Result.ok();
    }

    @GetMapping("/received")
    public Result<Page<Message>> received(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(messageService.pageReceived(userId, page, size));
    }

    @GetMapping("/sent")
    public Result<Page<Message>> sent(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(messageService.pageSent(userId, page, size));
    }

    @GetMapping("/unread-count")
    public Result<Integer> unreadCount() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(messageService.unreadCount(userId));
    }

    @PutMapping("/read/{id}")
    public Result<?> markRead(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        messageService.markRead(id, userId);
        return Result.ok();
    }

    @PostMapping("/read-batch")
    public Result<?> markReadBatch(@RequestBody List<Long> ids) {
        long userId = StpUtil.getLoginIdAsLong();
        messageService.markReadBatch(userId, ids);
        return Result.ok();
    }

    @PutMapping("/read-all")
    public Result<?> markAllRead() {
        long userId = StpUtil.getLoginIdAsLong();
        messageService.markAllRead(userId);
        return Result.ok();
    }
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        messageService.delete(id, userId);
        return Result.ok();
    }

    @GetMapping("/conversations")
    public Result<List<ConversationVO>> conversations() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(messageService.getConversations(userId));
    }

    @GetMapping("/chat/{targetUserId}")
    public Result<Page<Message>> chatHistory(
            @PathVariable Long targetUserId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(messageService.getChatHistory(userId, targetUserId, page, size));
    }

    @PutMapping("/read-from/{targetUserId}")
    public Result<?> markAllReadFromUser(@PathVariable Long targetUserId) {
        long userId = StpUtil.getLoginIdAsLong();
        messageService.markAllReadFromUser(userId, targetUserId);
        return Result.ok();
    }

    @GetMapping("/categories")
    public Result<List<NotificationCategoryVO>> categories() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(messageService.getNotificationCategories(userId));
    }

    @GetMapping("/by-type/{type}")
    public Result<Page<Message>> byType(
            @PathVariable Integer type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (type < 2 || type > 6) {
            return Result.fail("无效的消息类型");
        }
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(messageService.pageByType(userId, type, page, size));
    }

    @GetMapping("/unread-stats")
    public Result<Map<String, Integer>> unreadStats() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(messageService.getUnreadStats(userId));
    }

    @PutMapping("/read-type/{type}")
    public Result<?> markAllReadByType(@PathVariable Integer type) {
        if (type < 2 || type > 6) {
            return Result.fail("无效的消息类型");
        }
        long userId = StpUtil.getLoginIdAsLong();
        messageService.markAllReadByType(userId, type);
        return Result.ok();
    }
}
