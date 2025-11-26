package top.hxll.kimi.common.exception;

/**
 * 用户异常类
 *
 * @author kimi
 * @since 1.0.0
 */
public class UserException extends BusinessException {

    public UserException(String message) {
        super(400, message);
    }

    public UserException(String message, Throwable cause) {
        super(400, message, cause);
    }
}
