package com.ghostfire.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class PostDto {
    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题不能超过100个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 50000, message = "内容不能超过50000个字符")
    private String content;

    @Size(max = 5, message = "标签最多选择5个")
    private List<Long> tags;
}
