package com.model.management.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ModelVO {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private Long manufacturerId;
    private String manufacturerName;
    private String name;
    private BigDecimal price;
    private String remark;
    private Integer sold;
    private Integer isPublic;
    private Integer viewCount;
    private Integer favoriteCount;
    private String cover;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
