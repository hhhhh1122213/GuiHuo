package com.ghostfire.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class MessageDto {
    @NotNull(message = "收件人不能为空")
    private Long toUserId;

    @NotBlank(message = "消息内容不能为空")
    private String content;
}
