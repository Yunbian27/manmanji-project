package com.yunbian27.points.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("points_transactions")
public class PointsTransaction {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Integer amount;
    private String type;
    private String description;
    private Long referenceId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
