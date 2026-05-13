package com.yunbian27.auth.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshDTO {

    /** 刷新令牌 */
    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;
}
