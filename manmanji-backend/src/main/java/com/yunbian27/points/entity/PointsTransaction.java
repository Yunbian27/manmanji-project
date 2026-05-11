package com.yunbian27.points.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("points_transactions")
public class PointsTransaction {

    @TableId(type = IdType.AUTO)
    /** 主键 */
    private Long id;

    /** 用户ID */
    private Long userId;
    /** 积分变动量(正数增加负数扣减) */
    private Integer amount;
    /** 交易类型(CHECK_IN/READ_ARTICLE/LIKE/COMMENT/EXCHANGE_AI等) */
    private String type;
    /** 交易描述 */
    private String description;
    /** 关联业务ID(如文章ID) */
    private Long referenceId;

    @TableField(fill = FieldFill.INSERT)
    /** 创建时间 */
    private LocalDateTime createdAt;
}
