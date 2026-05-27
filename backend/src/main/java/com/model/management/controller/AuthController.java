package com.model.management.controller;

import com.model.management.common.Result;
import com.model.management.dto.LoginDTO;
import com.model.management.dto.RegisterDTO;
import com.model.management.service.AuthService;
import com.model.management.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            LoginVO loginVO = authService.login(loginDTO);
            return Result.success(loginVO);
        } catch (RuntimeException e) {
            // 区分业务异常和其他异常
            return Result.error(401, e.getMessage());
        } catch (Exception e) {
            // 记录未知异常日志
            return Result.error(500, "登录服务暂时不可用");
        }
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Boolean> register(@Valid @RequestBody RegisterDTO registerDTO) {
        try {
            authService.register(registerDTO);
            return Result.success(true);
        } catch (RuntimeException e) {
            // 区分业务异常和其他异常
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            // 记录未知异常日志
            return Result.error(500, "注册服务暂时不可用");
        }
    }
}
