package com.yunbian27.article.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article_versions")
public class ArticleVersion {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文章ID */
    private Long articleId;
    /** 当时的标题 */
    private String title;
    /** 当时的内容 */
    private String content;
    /** 版本号 */
    private Integer versionNumber;
    /** 变更说明 */
    private String changeSummary;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
