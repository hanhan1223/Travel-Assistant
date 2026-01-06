package org.example.travel.aop;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.travel.annotation.AuthCheck;
import org.example.travel.exception.BusinessException;
import org.example.travel.exception.ErrorCode;
import org.example.travel.model.entity.User;
import org.example.travel.model.enums.UserRoleEnum;
import org.example.travel.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class AuthInterceptor {

    @Resource
    private UserService userService;


    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        log.info("===============进入权限校验===============");
        String mustRole = authCheck.mustRole();
        //获取当前用户
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        //获取request
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getUserRoleEnumByValue(mustRole);
        //如果不需要权限，放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }

        UserRoleEnum userRoleEnum = UserRoleEnum.getUserRoleEnumByValue(loginUser.getUserrole());

        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 判断用户角色是否满足要求
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 通过权限校验，放行
        return joinPoint.proceed();


    }

}
