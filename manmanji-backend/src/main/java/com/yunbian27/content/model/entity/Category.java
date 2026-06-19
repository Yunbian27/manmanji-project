package com.yunbian27.content.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("categories")
public class Category {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类名 */
    private String name;
    /** URL标识 */
    private String slug;
    /** 描述 */
    private String description;
    /** 父分类ID(支持多级) */
    private Long parentId;
    /** 排序权重 */
    private Integer sortOrder;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
