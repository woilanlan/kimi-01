package top.hxll.kimi.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.hxll.kimi.common.Result;
import top.hxll.kimi.common.UserContextUtils;
import top.hxll.kimi.common.exception.PasswordException;
import top.hxll.kimi.common.exception.TokenException;
import top.hxll.kimi.dto.req.auth.LoginReq;
import top.hxll.kimi.dto.req.auth.RefreshTokenReq;
import top.hxll.kimi.dto.req.auth.RegisterReq;
import top.hxll.kimi.dto.req.user.PasswordChangeReq;
import top.hxll.kimi.dto.resp.JwtResp;
import top.hxll.kimi.entity.User;
import top.hxll.kimi.mapper.UserMapper;
import top.hxll.kimi.security.service.UserDetailsServiceImpl;
import top.hxll.kimi.security.service.UserDetailsImpl;
import top.hxll.kimi.security.util.JwtUtils;
import top.hxll.kimi.service.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证控制器
 *
 * @author kimi
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Object> register(@Valid @RequestBody RegisterReq registerReq) {
        log.info("User registration attempt: {}", registerReq.getUsername());

        // 注册用户（Service层会验证密码一致性并抛出异常）
        User user = userService.register(registerReq);

        log.info("User registered successfully: {}", user.getUsername());
        return Result.success("注册成功", user);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<JwtResp> login(@Valid @RequestBody LoginReq loginReq) {
        log.info("User login attempt: {}", loginReq.getUsername());

        // 用户认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginReq.getUsername(),
                        loginReq.getPassword())
        );

        // 设置认证信息到上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成JWT令牌
        String jwt = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(authentication.getName());

        // 获取用户信息
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userMapper.selectById(userDetails.getId());

        // 获取用户角色和权限
        List<String> roles = userDetails.getAuthorities().stream()
                .filter(grantedAuthority -> grantedAuthority.getAuthority().startsWith("ROLE_"))
                .map(grantedAuthority -> grantedAuthority.getAuthority().substring(5))
                .collect(Collectors.toList());

        List<String> permissions = userDetails.getAuthorities().stream()
                .filter(grantedAuthority -> !grantedAuthority.getAuthority().startsWith("ROLE_"))
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toList());

        // 更新最后登录时间
        if (user != null) {
            user.setLastLoginTime(java.time.LocalDateTime.now());
            // 这里可以添加IP获取逻辑
            // user.setLastLoginIp(getClientIp(request));
            userMapper.updateById(user);
        }

        // 构建响应
        JwtResp jwtResp = new JwtResp(
                jwt,
                refreshToken,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getNickname(),
                userDetails.getAvatar(),
                userDetails.getEmail(),
                roles,
                permissions,
                86400L, // 24小时
                604800L // 7天
        );

        log.info("User login successful: {}", loginReq.getUsername());
        return Result.success("登录成功", jwtResp);
    }

    /**
     * 刷新访问令牌
     */
    @PostMapping("/refresh")
    public Result<JwtResp> refreshToken(@Valid @RequestBody RefreshTokenReq refreshTokenReq) {
        String refreshToken = refreshTokenReq.getRefreshToken();

        // 验证刷新令牌
        if (!jwtUtils.validateJwtToken(refreshToken)) {
            throw new TokenException("刷新令牌无效或已过期");
        }

        // 从刷新令牌获取用户名
        String username = jwtUtils.getUserNameFromJwtToken(refreshToken);

        // 获取用户信息
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);

        // 生成新的访问令牌
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        String newAccessToken = jwtUtils.generateJwtToken(authentication);

        // 获取用户角色和权限
        List<String> roles = userDetails.getAuthorities().stream()
                .filter(grantedAuthority -> grantedAuthority.getAuthority().startsWith("ROLE_"))
                .map(grantedAuthority -> grantedAuthority.getAuthority().substring(5))
                .collect(Collectors.toList());

        List<String> permissions = userDetails.getAuthorities().stream()
                .filter(grantedAuthority -> !grantedAuthority.getAuthority().startsWith("ROLE_"))
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toList());

        // 构建响应
        JwtResp jwtResp = new JwtResp(
                newAccessToken,
                refreshToken, // 刷新令牌不变
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getNickname(),
                userDetails.getAvatar(),
                userDetails.getEmail(),
                roles,
                permissions,
                86400L, // 24小时
                604800L // 7天
        );

        log.info("Token refreshed successfully for user: {}", username);
        return Result.success("令牌刷新成功", jwtResp);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<User> getCurrentUser() {
        Long userId = UserContextUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);

        if (user != null) {
            // 清除敏感信息
            user.setPassword(null);
            return Result.success(user);
        }

        return Result.error("用户不存在");
    }

    /**
     * 修改密码（需要登录）
     */
    @PostMapping("/change-password")
    public Result<Object> changePassword(@Valid @RequestBody PasswordChangeReq passwordChangeReq) {
        String username = UserContextUtils.getCurrentUsername();
        Long userId = UserContextUtils.getCurrentUserId();
        log.info("Changing password for user: {}", username);

        // 验证新密码是否一致
        if (!passwordChangeReq.isNewPasswordMatch()) {
            throw new PasswordException("两次输入的新密码不一致");
        }

        // 修改密码
        boolean result = userService.changePassword(userId, passwordChangeReq);

        if (result) {
            log.info("Password changed successfully for user: {}", username);
            return Result.success("密码修改成功");
        } else {
            throw new PasswordException("密码修改失败");
        }
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Object> logout() {
        // 对于JWT，登出主要是客户端删除token
        // 这里可以添加一些清理逻辑，如记录登出日志等
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            log.info("User logout: {}", authentication.getName());
            SecurityContextHolder.clearContext();
        }
        return Result.success("登出成功");
    }
}