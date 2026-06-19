package com.yunbian27.content.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ArticleTitlesVO {
    /** 主键ID */
    private Long id;
    /** 标题 */
    private String title;
    /** 状态(UNPUBLISHED/PUBLISHED) */
    private String status;
    /** 可见范围(PUBLIC/PRIVATE) */
    private String visibility;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
