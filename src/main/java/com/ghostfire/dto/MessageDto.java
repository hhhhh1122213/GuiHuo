package com.ghostfire.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class MessageDto {
    @NotNull(message = "收件人不能为空")
    private Long toUserId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容不能超过2000个字符")
    private String content;

    /** 消息类型：1=私信, 2=@我的动态, 3=@我的评论, 4=收到的赞, 5=关注通知, 6=系统通知 */
    private Integer type;

    /** 关联目标 ID */
    private Long targetId;
}
