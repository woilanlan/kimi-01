package top.hxll.kimi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.hxll.kimi.dto.*;
import top.hxll.kimi.dto.req.auth.RegisterReq;
import top.hxll.kimi.dto.req.user.PasswordChangeReq;
import top.hxll.kimi.dto.req.user.UserCreateReq;
import top.hxll.kimi.dto.req.user.UserUpdateReq;
import top.hxll.kimi.entity.User;

import java.util.List;
import java.util.Map;

/**
 * 用户服务接口
 *
 * @author kimi
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 用户注册
     */
    User register(RegisterReq registerReq);

    /**
     * 修改密码
     */
    boolean changePassword(Long userId, PasswordChangeReq passwordChangeReq);

    /**
     * 重置密码（管理员操作）
     */
    boolean resetPassword(Long userId, String newPassword);

    /**
     * 获取用户信息（包含角色和权限）
     */
    UserDto getUserWithRolesAndPermissions(Long userId);

    /**
     * 分页查询用户列表
     */
    IPage<UserDto> getUserPage(Page<User> page, String keyword, Integer status);

    /**
     * 更新用户基本信息
     */
    boolean updateUserInfo(Long userId, UserUpdateReq updateRequest);

    /**
     * 删除用户（逻辑删除）
     */
    boolean deleteUser(Long userId);

    /**
     * 更新用户角色
     */
    boolean updateUserRoles(Long userId, List<Long> roleIds);

    /**
     * 创建用户（管理员）
     */
    User createUser(UserCreateReq createRequest);

    /**
     * 切换用户状态
     */
    boolean toggleUserStatus(Long userId, Integer status);

    /**
     * 批量删除用户
     */
    boolean deleteUsers(List<Long> userIds);

    /**
     * 获取用户统计信息
     */
    Map<String, Long> getUserStatistics();
}