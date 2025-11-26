package top.hxll.kimi.dto.req.user;

import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 更新用户请求DTO
 *
 * @author kimi
 * @since 1.0.0
 */
@Data
public class UserUpdateReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    /**
     * 头像URL
     */
    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatar;

    /**
     * 状态：1-正常，0-禁用
     */
    @Min(value = 0, message = "状态值不正确")
    @Max(value = 1, message = "状态值不正确")
    private Integer status;

    /**
     * 角色ID列表
     */
    private Long[] roleIds;
}