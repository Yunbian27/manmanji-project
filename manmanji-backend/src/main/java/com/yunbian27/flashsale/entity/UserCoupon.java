package com.yunbian27.flashsale.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_coupons")
public class UserCoupon {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;
    /** 优惠券ID */
    private Long couponId;
    /** 状态(UNUSED/USED/EXPIRED) */
    private String status;

    /** 领取时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime receivedAt;

    /** 使用时间 */
    private LocalDateTime usedAt;
    /** 过期时间 */
    private LocalDateTime expiredAt;
}
