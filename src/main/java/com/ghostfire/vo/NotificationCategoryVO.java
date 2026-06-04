package com.ghostfire.vo;

import lombok.Data;

@Data
public class NotificationCategoryVO {

    private Integer type;
    private String name;
    private String icon;
    private Integer unreadCount;
    private String lastMessage;
    private String lastMessageTime;
}
