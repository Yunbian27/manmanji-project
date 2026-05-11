package com.yunbian27.flashsale.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("flash_sale_orders")
public class FlashSaleOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long flashSaleId;
    private Long itemId;
    private Long userId;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
