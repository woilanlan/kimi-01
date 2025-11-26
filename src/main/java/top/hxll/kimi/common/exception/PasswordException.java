package top.hxll.kimi.common.exception;

/**
 * 密码异常类
 *
 * @author kimi
 * @since 1.0.0
 */
public class PasswordException extends BusinessException {

    public PasswordException(String message) {
        super(400, message);
    }

    public PasswordException(String message, Throwable cause) {
        super(400, message, cause);
    }
}