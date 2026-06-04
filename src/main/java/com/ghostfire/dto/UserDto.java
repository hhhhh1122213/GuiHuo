package com.ghostfire.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDto {
    @Size(max = 20, message = "昵称不能超过20个字符")
    private String nickname;

    @Size(max = 255, message = "头像URL过长")
    private String avatar;
}
