package com.example.bsafter.common;

/**
 * 功能：获取token
 * 日期：2024/4/815:49
 */

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.bsafter.entity.Manager;
import com.example.bsafter.entity.User;
import com.example.bsafter.exception.ServiceException;
import com.example.bsafter.service.ManagerLoginService;
import com.example.bsafter.service.UserLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//拦截
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private ManagerLoginService managerLoginService;
    @Resource
    private UserLoginService userLoginService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token)) {
            token = request.getParameter("token");
        }

        // 执行认证
        if (StrUtil.isBlank(token)) {
            throw new ServiceException("401", "请登录");
        }
        // 获取 token 中的 manager id 或者 user id
        String Id;
        String role;
        try {
            Id = JWT.decode(token).getAudience().get(0);
            role= JWT.decode(token).getClaim("role").asString();
        } catch (JWTDecodeException j) {
            throw new ServiceException("401", "请登录");
        }
        // 根据token中的user id或者manager id查询数据库
        Manager manager = managerLoginService.getById(Id);
        User user = userLoginService.getById(Id);
        if (manager != null && "admin".equals(role) ) {
            // 管理员密码加签验证 token
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(manager.getMpassword())).build();
            try {
                jwtVerifier.verify(token); // 验证token
            } catch (JWTVerificationException e) {
                throw new ServiceException("401", "请登录");
            }
        }
        if (user != null && "user".equals(role)) {
            // 用户密码加签验证 token
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getUpassword())).build();
            try {
                jwtVerifier.verify(token); // 验证token
            } catch (JWTVerificationException e) {
                throw new ServiceException("401", "请登录");
            }
        }
        return true;
    }
}
