package com.ghostfire.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class ReplyDto {
    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    private Long parentId;

    @NotBlank(message = "回复内容不能为空")
    private String content;
}
