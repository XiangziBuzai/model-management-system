package com.model.management.service;

import com.model.management.dto.PasswordChangeDTO;
import com.model.management.dto.UserProfileUpdateDTO;
import com.model.management.vo.UserProfileVO;

/**
 * 用户服务接口
 */
public interface UserService {
    /**
     * 获取当前用户信息
     */
    UserProfileVO getCurrentUserProfile();

    /**
     * 更新用户信息
     */
    UserProfileVO updateProfile(UserProfileUpdateDTO updateDTO);

    /**
     * 修改密码
     */
    void changePassword(PasswordChangeDTO passwordChangeDTO);

    /**
     * 获取指定用户公开信息
     */
    UserProfileVO getUserPublicProfile(Long userId);
}
