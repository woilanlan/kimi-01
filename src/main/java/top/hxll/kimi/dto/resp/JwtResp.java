package top.hxll.kimi.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * JWT响应DTO
 *
 * @author kimi
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResp implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 权限列表
     */
    private List<String> permissions;

    /**
     * 访问令牌过期时间（秒）
     */
    private Long accessTokenExpireTime;

    /**
     * 刷新令牌过期时间（秒）
     */
    private Long refreshTokenExpireTime;

    public JwtResp(String accessToken, String refreshToken, Long userId, String username,
                   String nickname, String avatar, String email, List<String> roles,
                   List<String> permissions, Long accessTokenExpireTime, Long refreshTokenExpireTime) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar;
        this.email = email;
        this.roles = roles;
        this.permissions = permissions;
        this.accessTokenExpireTime = accessTokenExpireTime;
        this.refreshTokenExpireTime = refreshTokenExpireTime;
    }
}