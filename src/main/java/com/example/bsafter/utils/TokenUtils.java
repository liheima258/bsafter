package com.example.bsafter.utils;

import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.bsafter.service.ManagerLoginService;
import com.example.bsafter.service.UserLoginService;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;

@Component
public class TokenUtils {

    private static ManagerLoginService staticManagerLoginService;
    private static UserLoginService staticUserLoginService;

    @Resource
    ManagerLoginService managerLoginService;
    @Resource
    UserLoginService userLoginService;

    @PostConstruct
    public void setManagerService() {
        staticManagerLoginService = managerLoginService;
    }

    @PostConstruct
    public void setUserService() {
        staticUserLoginService = userLoginService;
    }

    /**
     * 生成token
     *
     * @return
     */
    public static String createToken(String managerId, String sign,String role) {
        return JWT.create().withClaim("role",role)  //设置角色 区分管理员和用户
                .withAudience(managerId) // 将 user id 保存到 token 里面,作为载荷
                .withExpiresAt(DateUtil.offsetHour(new Date(), 2)) // 2小时后token过期
                .sign(Algorithm.HMAC256(sign)); // 以 password 作为 token 的密钥
    }
}