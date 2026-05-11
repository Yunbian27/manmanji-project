package com.yunbian27.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article_embeddings")
public class ArticleEmbedding {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;
    private Integer chunkIndex;
    private String chunkText;
    private String chunkHash;
    private String embedding;
    private Integer tokenCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
