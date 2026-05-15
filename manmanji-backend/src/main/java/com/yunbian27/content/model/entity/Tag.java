package com.yunbian27.content.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tags")
public class Tag {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标签名 */
    private String name;
    /** URL标识 */
    private String slug;
    /** 标签颜色(如#FF6600) */
    private String color;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
