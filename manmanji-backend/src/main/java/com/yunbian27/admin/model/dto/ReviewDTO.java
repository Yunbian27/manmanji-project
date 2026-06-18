package com.yunbian27.admin.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewDTO {

    @NotBlank(message = "操作类型不能为空")
    private String action;   // APPROVE / REJECT

    @Size(max = 100, message = "驳回理由最多 100 字")
    private String reason;
}
