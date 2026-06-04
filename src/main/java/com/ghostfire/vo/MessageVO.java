package com.ghostfire.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageVO {

    private Long id;
    private String content;
    private Integer status;
    private LocalDateTime createTime;

    private SimpleUserVO fromUser;
    private SimpleUserVO toUser;
}
