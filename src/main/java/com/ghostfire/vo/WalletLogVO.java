package com.ghostfire.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WalletLogVO {

    private Long id;
    private Long amount;
    private Long currentBalance;
    private String type;
    private String typeName;
    private LocalDateTime createTime;
}
