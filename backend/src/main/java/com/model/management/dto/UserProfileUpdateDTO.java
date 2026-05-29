package com.model.management.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

/**
 * 用户信息更新DTO
 */
@Data
public class UserProfileUpdateDTO {
    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    @Pattern(regexp = "^$|^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 是否公开收藏列表: 0-私密, 1-公开
     */
    private Integer isPublicFavorite;
}
