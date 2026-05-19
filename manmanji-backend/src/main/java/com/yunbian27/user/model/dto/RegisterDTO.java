package com.yunbian27.user.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDTO {

    /** 用户名(3-50字符) */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度需在 3-50 之间")
    private String username;

    /** 邮箱 */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 密码(6-100字符) */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度需在 6-100 之间")
    private String password;

    /** 邮箱验证码(6位数字) */
    @NotBlank(message = "验证码不能为空")
    private String code;

    /** 昵称(可选，不填默认用username) */
    private String nickname;
}
