package com.ghostfire.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConversationVO {

    private Long targetUserId;
    private String targetNickname;
    private String targetAvatar;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer unreadCount;
}
