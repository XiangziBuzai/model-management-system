package com.model.management.service;

import com.model.management.dto.LoginDTO;
import com.model.management.dto.RegisterDTO;
import com.model.management.vo.LoginVO;

public interface AuthService {
    /**
     * 用户登录
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户注册
     */
    void register(RegisterDTO registerDTO);

    /**
     * 根据token获取用户信息
     */
    Long getUserIdFromToken(String token);
}
