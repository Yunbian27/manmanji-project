package com.yunbian27.flashsale.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("coupons")
public class Coupon {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 优惠券名称 */
    private String name;
    /** 描述 */
    private String description;
    /** 类型(FIXED固定减扣/PERCENT百分比) */
    private String type;
    /** 面值/百分比值 */
    private Integer value;
    /** 最低积分门槛 */
    private Integer minPoints;
    /** 发放总量 */
    private Integer totalCount;
    /** 已领取数量 */
    private Integer receivedCount;
    /** 有效期天数 */
    private Integer validDays;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
