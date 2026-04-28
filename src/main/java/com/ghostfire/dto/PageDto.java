package com.ghostfire.dto;

import lombok.Data;

@Data
public class PageDto {
    private Integer page = 1;
    private Integer size = 20;
    private Long categoryId;
    private String keyword;
}
