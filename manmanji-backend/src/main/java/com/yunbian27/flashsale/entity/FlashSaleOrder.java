package com.yunbian27.flashsale.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("flash_sale_orders")
public class FlashSaleOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 秒杀活动ID */
    private Long flashSaleId;
    /** 商品ID */
    private Long itemId;
    /** 用户ID */
    private Long userId;
    /** 订单状态(CREATED/PAID/CANCELLED) */
    private String status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
