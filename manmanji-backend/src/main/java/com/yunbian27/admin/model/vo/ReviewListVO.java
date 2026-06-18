package com.yunbian27.admin.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewListVO {

    private Long id;
    private String title;
    private String summary;
    private String coverUrl;
    private String author;
    private String reviewStatus;
    private String reviewReason;
    private LocalDateTime submittedAt;
}
