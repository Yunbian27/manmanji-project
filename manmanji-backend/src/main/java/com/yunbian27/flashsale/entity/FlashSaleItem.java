package com.yunbian27.flashsale.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("flash_sale_items")
public class FlashSaleItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long flashSaleId;
    private String name;
    private String description;
    private Integer totalStock;
    private Integer soldCount;
    private Integer perUserLimit;
    private Integer pointsCost;
    private Integer aiQuotaReward;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
