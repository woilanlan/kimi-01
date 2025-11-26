package top.hxll.kimi.common.exception;

import lombok.Getter;
import top.hxll.kimi.common.Result;

/**
 * 自定义业务异常类
 *
 * @author kimi
 * @since 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 构造方法
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造方法
     */
    public BusinessException(String message) {
        this(500, message);
    }

    /**
     * 构造方法
     */
    public BusinessException(Result<?> result) {
        this(result.getCode(), result.getMessage());
    }

    /**
     * 构造方法
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}