package com.ghostfire.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class ReplyDto {
    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    private Long parentId;

    private Long replyUserId;

    @NotBlank(message = "回复内容不能为空")
    @Size(max = 2000, message = "回复内容不能超过2000个字符")
    private String content;
}
