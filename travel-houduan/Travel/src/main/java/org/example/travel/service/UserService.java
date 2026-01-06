package org.example.travel.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.travel.model.dto.user.UserUpdateMyRequest;
import org.example.travel.model.dto.user.UserUpdatePasswordRequest;
import org.example.travel.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.travel.model.vo.LoginUserVO;

/**
* @author LiaoHan
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-12-29 12:49:39
*/
public interface UserService extends IService<User> {

    long userEmailRegister(String email, String code, String password, String checkPassword, String userName, String userAvatar);


    /**
     * 获取加密后的密码
     *
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 用户邮箱登录
     *
     * @param email
     * @param password
     * @param request
     * @return
     */
    LoginUserVO userEmailLogin(String email, String password, HttpServletRequest request);

    /**
     * 获得脱敏后的登录用户信息
     *
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 更新用户个人信息
     *
     * @param userUpdateMyRequest 用户更新请求
     * @param request             HTTP请求对象
     * @return 是否更新成功
     */
    boolean updateMyUser(UserUpdateMyRequest userUpdateMyRequest, HttpServletRequest request);

    /**
     * 更新用户密码
     *
     * @param userUpdatePasswordRequest 密码更新请求
     * @param request                   HTTP请求对象
     * @return 是否更新成功
     */
    boolean updatePassword(UserUpdatePasswordRequest userUpdatePasswordRequest, HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 发送注册邮件验证码
     *
     * @param email 邮箱地址
     */

    void sendEmailRegisterCode(String email);
}

