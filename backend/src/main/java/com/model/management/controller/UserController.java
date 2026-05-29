package com.model.management.controller;

import com.model.management.common.Result;
import com.model.management.dto.PasswordChangeDTO;
import com.model.management.dto.UserProfileUpdateDTO;
import com.model.management.service.UserService;
import com.model.management.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile() {
        try {
            UserProfileVO profile = userService.getCurrentUserProfile();
            return Result.success(profile);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/profile")
    public Result<UserProfileVO> updateProfile(@Valid @RequestBody UserProfileUpdateDTO updateDTO) {
        try {
            UserProfileVO profile = userService.updateProfile(updateDTO);
            return Result.success(profile);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Boolean> changePassword(@Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        try {
            userService.changePassword(passwordChangeDTO);
            return Result.success(true);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "获取指定用户公开信息")
    @GetMapping("/public/{userId}")
    public Result<UserProfileVO> getUserPublicProfile(@PathVariable Long userId) {
        try {
            UserProfileVO profile = userService.getUserPublicProfile(userId);
            return Result.success(profile);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }
}
