package com.yunbian27.flashsale.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("coupons")
public class Coupon {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String description;
    private String type;
    private Integer value;
    private Integer minPoints;
    private Integer totalCount;
    private Integer receivedCount;
    private Integer validDays;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
