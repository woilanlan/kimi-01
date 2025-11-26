package top.hxll.kimi.common.exception;

/**
 * Token异常类
 *
 * @author kimi
 * @since 1.0.0
 */
public class TokenException extends AuthException {

    public TokenException(String message) {
        super(message);
    }

    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }
}