package com.ghostfire.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghostfire.common.Result;
import com.ghostfire.dto.MessageDto;
import com.ghostfire.entity.Message;
import com.ghostfire.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
        messageService.send(userId, dto.getToUserId(), dto.getContent());
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
}
