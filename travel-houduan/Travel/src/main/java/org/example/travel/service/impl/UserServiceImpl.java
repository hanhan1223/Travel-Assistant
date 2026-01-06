package org.example.travel.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.travel.exception.BusinessException;
import org.example.travel.exception.ErrorCode;
import org.example.travel.exception.ThrowUtils;
import org.example.travel.model.dto.user.UserUpdateMyRequest;
import org.example.travel.model.dto.user.UserUpdatePasswordRequest;
import org.example.travel.model.entity.User;
import org.example.travel.model.vo.LoginUserVO;
import org.example.travel.service.EmailService;
import org.example.travel.service.UserService;
import org.example.travel.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author LiaoHan
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-12-29 12:49:39
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private EmailService emailService;

    @Override
    public long userEmailRegister(String email, String code, String password, String checkPassword, String userName,
            String userAvatar) {
        if (StrUtil.hasBlank(email, code, password, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 2. 校验密码
        if (password.length() < 6 || checkPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }

        if (!password.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 3. 校验邮箱格式
        if (StrUtil.isBlank(email) || !ReUtil.isMatch("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        }

        // 4. 检查邮箱是否已注册
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已被注册");
        }

        // 5. 验证验证码
        if (!emailService.verifyCode(email, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误或已过期");
        }

        // 6. 密码加密
        String encryptPassword = getEncryptPassword(password);

        // 7. 插入数据到数据库中
        User user = new User();
        user.setEmail(email);
        user.setUserpassword(encryptPassword);
        user.setUsername(StrUtil.isBlank(userName) ? "用户_" + System.currentTimeMillis() : userName.trim());
        user.setUseraccount("user_" + System.currentTimeMillis());
        if (StrUtil.isNotBlank(userAvatar)) {
            user.setUseravatar(userAvatar.trim());
        }
        user.setUserrole("user");
        // 设置时间字段
        Date now = new Date();
        user.setCreatetime(now);
        user.setUpdatetime(now);
        user.setEdittime(now);
        user.setIsdelete(0);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }

        // 8. 清除验证码
        emailService.clearCode(email);
        return user.getId();
    }

    /**
     * 获取加密后的密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 加盐，混淆密码
        final String SALT = "yupi";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public LoginUserVO userEmailLogin(String email, String password, HttpServletRequest request) {
        String loginFailKey = "LOGIN_FAIL:" + email;

        // 检查失败次数
        String failCountStr = (String) redisTemplate.opsForValue().get(loginFailKey);
        int failCount = failCountStr == null ? 0 : Integer.parseInt(failCountStr);

        if (failCount >= 5) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "登录失败次数过多，请稍后再试");
        }

        // 校验参数
        if (StrUtil.hasBlank(email, password)) {
            incrementFailCount(loginFailKey);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 校验密码
        String encryptPassword = getEncryptPassword(password);

        // 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);

        // 用户不存在
        if (user == null) {
            incrementFailCount(loginFailKey);
            log.info("用户不存在或密码错误");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        // 清除失败记录
        redisTemplate.delete(loginFailKey);

        // 设置登录状态
        request.getSession().setAttribute("user_login_state", user);

        return this.getLoginUserVO(user);
    }

    /**
     * 获取脱敏类的用户信息
     *
     * @param user 用户
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUservo = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUservo, CopyOptions.create().setIgnoreCase(true));
        return loginUservo;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object loginState = request.getSession().getAttribute("user_login_state");
        User currentUser = (User) loginState;
        if (loginState == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 查询数据库中的用户是否存在
        Long id = currentUser.getId();
        currentUser = this.getById(id);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public boolean updateMyUser(UserUpdateMyRequest userUpdateMyRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userUpdateMyRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = getLoginUser(request);
        
        // 检查是否有字段需要更新
        String userName = userUpdateMyRequest.getUserName();
        String userAvatar = userUpdateMyRequest.getUserAvatar();
        if (userName == null && userAvatar == null) {
            return true; // 没有字段需要更新，直接返回成功
        }
        
        User user = new User();
        user.setId(loginUser.getId());
        if (userName != null) {
            user.setUsername(userName);
        }
        if (userAvatar != null) {
            user.setUseravatar(userAvatar);
        }
        return this.updateById(user);
    }

    @Override
    public boolean updatePassword(UserUpdatePasswordRequest userUpdatePasswordRequest, HttpServletRequest request) {
        if (userUpdatePasswordRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String oldPassword = userUpdatePasswordRequest.getOldPassword();
        String newPassword = userUpdatePasswordRequest.getNewPassword();
        String confirmPassword = userUpdatePasswordRequest.getConfirmPassword();

        // 参数校验
        if (StringUtils.isAnyBlank(oldPassword, newPassword, confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        if (newPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能少于8位");
        }

        // 获取当前登录用户
        User loginUser = getLoginUser(request);

        // 验证旧密码是否正确
        String encryptOldPassword = DigestUtils.md5DigestAsHex((SALT + oldPassword).getBytes());
        if (!loginUser.getUserpassword().equals(encryptOldPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "旧密码错误");
        }

        // 加密新密码
        String encryptNewPassword = getEncryptPassword(newPassword);

        // 更新密码
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserpassword(encryptNewPassword);

        return this.updateById(user);
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 判断用户是否已经登录
        Object loginState = request.getSession().getAttribute("user_login_state");
        if (loginState == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未登录");
        }
        // 移除登录状态
        request.getSession().removeAttribute("user_login_state");
        return true;
    }

    @Override
    public void sendEmailRegisterCode(String email) {
        // 1. 校验邮箱格式
        if (StrUtil.isBlank(email) || !ReUtil.isMatch("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$", email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        }
        // 2. 检查邮箱是否已注册
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已被注册");
        }

        // 3. 调用邮件服务发送验证码
        emailService.sendVerificationCode(email);
    }

    final String SALT = "yupi";

    private void incrementFailCount(String key) {
        String countStr = (String) redisTemplate.opsForValue().get(key);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);
        redisTemplate.opsForValue().set(key, String.valueOf(count + 1), 30, TimeUnit.MINUTES);
    }
}
