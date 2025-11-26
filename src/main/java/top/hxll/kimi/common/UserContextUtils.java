package top.hxll.kimi.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import top.hxll.kimi.security.service.UserDetailsImpl;

/**
 * 用户上下文工具类
 * 提供获取当前登录用户信息的便捷方法
 *
 * @author kimi
 * @since 1.0.0
 */
@Slf4j
public class UserContextUtils {

    /**
     * 获取当前登录用户信息
     *
     * @return UserDetailsImpl 当前登录用户信息
     * @throws IllegalStateException 如果用户未登录或上下文异常
     */
    public static UserDetailsImpl getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("用户认证信息不存在");
        }

        if (!authentication.isAuthenticated()) {
            throw new IllegalStateException("用户未认证");
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            throw new IllegalStateException("用户主体信息不存在");
        }

        if (!(principal instanceof UserDetailsImpl)) {
            throw new IllegalStateException("用户主体类型异常");
        }

        return (UserDetailsImpl) principal;
    }

    /**
     * 获取当前登录用户ID
     *
     * @return Long 用户ID
     * @throws IllegalStateException 如果用户未登录或上下文异常
     */
    public static Long getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }

    /**
     * 获取当前登录用户名
     *
     * @return String 用户名
     * @throws IllegalStateException 如果用户未登录或上下文异常
     */
    public static String getCurrentUsername() {
        return getCurrentUserDetails().getUsername();
    }

    /**
     * 检查用户是否已登录
     *
     * @return boolean true-已登录，false-未登录
     */
    public static boolean isUserLoggedIn() {
        try {
            getCurrentUserDetails();
            return true;
        } catch (IllegalStateException e) {
            log.debug("用户未登录: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取当前登录用户信息（安全模式）
     * 如果用户未登录，返回null而不是抛出异常
     *
     * @return UserDetailsImpl 当前登录用户信息，未登录时返回null
     */
    public static UserDetailsImpl getCurrentUserDetailsSafely() {
        try {
            return getCurrentUserDetails();
        } catch (IllegalStateException e) {
            log.debug("获取用户信息失败: {}", e.getMessage());
            return null;
        }
    }
}