package com.yunbian27.flashsale.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("flash_sale_items")
public class FlashSaleItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 秒杀活动ID */
    private Long flashSaleId;
    /** 商品名称 */
    private String name;
    /** 商品描述 */
    private String description;
    /** 总库存 */
    private Integer totalStock;
    /** 已售数量 */
    private Integer soldCount;
    /** 每人限购数量 */
    private Integer perUserLimit;
    /** 积分消耗 */
    private Integer pointsCost;
    /** 奖励AI次数 */
    private Integer aiQuotaReward;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
