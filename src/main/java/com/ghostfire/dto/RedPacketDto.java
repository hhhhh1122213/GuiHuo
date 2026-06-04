package com.ghostfire.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RedPacketDto {
    @NotNull(message = "红包总金额不能为空")
    @Positive(message = "红包总金额必须大于0")
    private Long totalAmount;

    @NotNull(message = "红包个数不能为空")
    @Positive(message = "红包个数必须大于0")
    private Integer totalCount;

    @NotNull(message = "红包类型不能为空")
    private Integer type;

    private Long postId;
}
