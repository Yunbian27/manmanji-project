package com.yunbian27.flashsale.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_coupons")
public class UserCoupon {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long couponId;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime receivedAt;

    private LocalDateTime usedAt;
    private LocalDateTime expiredAt;
}
