package top.hxll.kimi.dto.req.auth;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 刷新令牌请求DTO
 *
 * @author kimi
 * @since 1.0.0
 */
@Data
public class RefreshTokenReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 刷新令牌
     */
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}