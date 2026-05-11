package com.yunbian27.article.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tags")
public class Tag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String slug;
    private String color;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
