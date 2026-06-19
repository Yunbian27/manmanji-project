package com.yunbian27.content.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("groups")
public class Group {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;
    /** 分组名称，同一用户下不重名 */
    private String name;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
