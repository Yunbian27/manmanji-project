package com.yunbian27.user.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_follows")
public class UserFollow {

    @TableId(type = IdType.AUTO)
    /** 主键 */
    private Long id;

    /** 关注者用户ID */
    private Long followerId;
    /** 被关注者用户ID */
    private Long followingId;

    @TableField(fill = FieldFill.INSERT)
    /** 关注时间(follower_id+following_id唯一,CHECK防止自己关注自己) */
    private LocalDateTime createTime;
}
