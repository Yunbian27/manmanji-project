package com.yunbian27.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article_embeddings")
public class ArticleEmbedding {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文章ID */
    private Long articleId;
    /** 分块序号 */
    private Integer chunkIndex;
    /** 分块文本内容 */
    private String chunkText;
    /** 分块哈希(SHA-256去重) */
    private String chunkHash;
    /** 向量数据(pgvector 1536维) */
    private String embedding;
    /** token数量 */
    private Integer tokenCount;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
