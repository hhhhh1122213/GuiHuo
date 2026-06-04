package com.ghostfire.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoastDto {
    @NotBlank(message = "标题不能为空")
    private String title;

    private String image;

    @NotBlank(message = "选项一不能为空")
    private String optionOne;

    @NotBlank(message = "选项二不能为空")
    private String optionTwo;

    @NotNull(message = "赌注金额不能为空")
    @Positive(message = "赌注金额必须大于0")
    private Long stakeAmount;

    @NotNull(message = "截止时间不能为空")
    private LocalDateTime deadline;

    @NotNull(message = "请选择正确答案选项")
    private Integer correctOption;
}
