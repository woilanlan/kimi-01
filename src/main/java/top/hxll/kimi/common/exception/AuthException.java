package top.hxll.kimi.common.exception;

import top.hxll.kimi.common.Result;

/**
 * 认证异常类
 *
 * @author kimi
 * @since 1.0.0
 */
public class AuthException extends BusinessException {

    public AuthException(String message) {
        super(401, message);
    }

    public AuthException(String message, Throwable cause) {
        super(401, message, cause);
    }

    public AuthException(Result<?> result) {
        super(result);
    }
}