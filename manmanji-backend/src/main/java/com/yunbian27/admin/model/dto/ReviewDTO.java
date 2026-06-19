package com.yunbian27.admin.model.dto;

import com.yunbian27.admin.constant.ReviewAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewDTO {

    @NotNull(message = "操作类型不能为空")
    private ReviewAction action;

    @Size(max = 100, message = "驳回理由最多 100 字")
    private String reason;
}
