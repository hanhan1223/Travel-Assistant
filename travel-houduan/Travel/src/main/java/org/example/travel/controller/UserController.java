package org.example.travel.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.example.travel.common.BaseResponse;
import org.example.travel.common.Result;
import org.example.travel.exception.BusinessException;
import org.example.travel.exception.ErrorCode;
import org.example.travel.exception.ThrowUtils;
import org.example.travel.model.dto.user.*;
import org.example.travel.model.entity.User;
import org.example.travel.model.vo.LoginUserVO;
import org.example.travel.service.UserService;
import org.example.travel.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import cn.hutool.core.util.StrUtil;


/**
 * 用户接口
 */
@RequestMapping("/user")
@RestController
public class UserController {

    @Resource
    UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户通过邮箱注册
     * @param request
     * @return
     */
    @PostMapping("/register/email")
    public BaseResponse<Long> userRegisterByEmail(@RequestBody UserRegisterByEmailRequest request) {
        long result = userService.userEmailRegister(
                request.getEmail(),
                request.getCode(),
                request.getUserPassword(),
                request.getCheckPassword(),
                request.getUserName(),
                request.getUserAvatar()
        );
        return Result.success(result);
    }

    /**
     * 用户邮件登录
     *
     * @param userLoginByEmailRequest
     * @param request
     * @return
     */
    @PostMapping("/login/email")
    public BaseResponse<LoginUserVO> userLoginByEmail(@RequestBody UserLoginByEmailRequest userLoginByEmailRequest,
                                                      HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginByEmailRequest == null, ErrorCode.PARAMS_ERROR);
        String email = userLoginByEmailRequest.getEmail();
        String password = userLoginByEmailRequest.getPassword();
        LoginUserVO loginUserVO = userService.userEmailLogin(email, password, request);
        return Result.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return Result.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return Result.success(result);
    }

    /**
     * 更新用户个人信息
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                              HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean result = userService.updateMyUser(userUpdateMyRequest, request);
        return Result.success(result);
    }

    /**
     * 更新用户密码
     */
    @PostMapping("/update/password")
    public BaseResponse<Boolean> updatePassword(@RequestBody UserUpdatePasswordRequest userUpdatePasswordRequest,
                                                HttpServletRequest request) {
        if (userUpdatePasswordRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean result = userService.updatePassword(userUpdatePasswordRequest, request);
        return Result.success(result);
    }

    /**
     * 发送注册验证码
     *
     * @param request
     * @return
     */
    @PostMapping("/register/send-code")
    public BaseResponse<Boolean> sendRegisterCode(@RequestBody SendCodeRequest request) {
        userService.sendEmailRegisterCode(request.getEmail());
        return Result.success(true);
    }

    /**
     * 微信登录
     *
     * @param weChatLoginRequest 微信登录请求
     * @param request HTTP请求对象
     * @return 登录用户信息
     */
    @PostMapping("/wechat/login")
    public BaseResponse<LoginUserVO> weChatLogin(@RequestBody WeChatLoginRequest weChatLoginRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(weChatLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String code = weChatLoginRequest.getCode();
        ThrowUtils.throwIf(StrUtil.isBlank(code), ErrorCode.PARAMS_ERROR, "授权码不能为空");
        
        LoginUserVO loginUserVO = userService.weChatLogin(code, request);
        return Result.success(loginUserVO);
    }

    /**
     * 刷新Token
     *
     * @param request HTTP请求对象
     * @return 新的token
     */
    @PostMapping("/refresh-token")
    public BaseResponse<String> refreshToken(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 生成新token
        String newToken = jwtUtils.generateToken(loginUser.getId(), loginUser.getUserrole());
        return Result.success(newToken);
    }

    
}
