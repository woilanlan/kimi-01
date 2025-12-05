package top.hxll.kimi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hxll.kimi.dto.RoleDto;
import top.hxll.kimi.entity.Role;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 角色服务接口
 *
 * @author kimi
 * @since 1.0.0
 */
public interface RoleService extends IService<Role> {

    /**
     * 根据角色ID列表获取有效的角色ID集合
     * @param roleIds 角色ID列表
     * @return 有效的角色ID集合
     */
    Set<Long> getValidRoleIds(List<Long> roleIds);

    /**
     * 获取所有角色列表
     */
    List<RoleDto> getAllRoles();

    /**
     * 获取角色列表（分页）
     */
    IPage<RoleDto> getRolePage(Page<Role> page, String keyword, Integer status);

    /**
     * 根据角色ID获取角色详情
     */
    RoleDto getRoleById(Long id);

    /**
     * 根据ID获取角色
     */
    Role getRoleByIdRaw(Long id);

    /**
     * 根据角色编码获取角色
     */
    Role getRoleByCode(String roleCode);

    /**
     * 根据用户ID获取用户角色列表
     */
    List<RoleDto> getRolesByUserId(Long userId);

    /**
     * 创建角色
     */
    Role createRole(RoleDto roleDto);

    /**
     * 更新角色
     */
    Role updateRole(Long id, RoleDto roleDto);

    /**
     * 删除角色
     */
    boolean deleteRole(Long id);

    /**
     * 批量删除角色
     */
    boolean deleteRoles(List<Long> ids);

    /**
     * 启用/禁用角色
     */
    boolean toggleRoleStatus(Long id, Integer status);

    /**
     * 获取角色权限列表
     */
    List<Long> getRolePermissionIds(Long roleId);

    /**
     * 更新角色权限
     */
    boolean updateRolePermissions(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色统计信息
     */
    Map<String, Long> getRoleStats();
}