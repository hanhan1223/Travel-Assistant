package org.example.travel.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Token拦截器
 */
@Component
@Slf4j
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头中获取token
        String token = request.getHeader("Authorization");
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            
            // 验证token
            if (jwtUtils.validateToken(token)) {
                // 将用户ID和角色存入request attribute，供后续使用
                Long userId = jwtUtils.getUserIdFromToken(token);
                String userRole = jwtUtils.getUserRoleFromToken(token);
                
                request.setAttribute("userId", userId);
                request.setAttribute("userRole", userRole);
                request.setAttribute("token", token);
                
                log.debug("Token验证成功，用户ID: {}, 角色: {}", userId, userRole);
                return true;
            } else {
                log.warn("Token验证失败或已过期");
            }
        }
        
        // 没有token或token无效，继续执行（由具体的接口决定是否需要登录）
        return true;
    }
}
