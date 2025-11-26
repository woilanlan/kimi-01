package top.hxll.kimi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.hxll.kimi.dto.PermissionDto;
import top.hxll.kimi.entity.Permission;

import java.util.List;
import java.util.Map;

/**
 * 权限服务接口
 *
 * @author kimi
 * @since 1.0.0
 */
public interface PermissionService {

    /**
     * 获取权限列表（树形结构）
     */
    List<PermissionDto> getPermissionTree();

    /**
     * 获取权限列表（分页）
     */
    IPage<PermissionDto> getPermissionPage(Page<Permission> page, String keyword, Integer permissionType);

    /**
     * 根据角色ID获取权限列表
     */
    List<PermissionDto> getPermissionsByRoleId(Long roleId);

    /**
     * 根据ID获取权限
     */
    Permission getPermissionById(Long id);

    /**
     * 根据权限编码获取权限
     */
    Permission getPermissionByCode(String permissionCode);

    /**
     * 创建权限
     */
    Permission createPermission(PermissionDto permissionDto);

    /**
     * 更新权限
     */
    Permission updatePermission(Long id, PermissionDto permissionDto);

    /**
     * 删除权限
     */
    boolean deletePermission(Long id);

    /**
     * 批量删除权限
     */
    boolean deletePermissions(List<Long> ids);

    /**
     * 启用/禁用权限
     */
    boolean togglePermissionStatus(Long id, Integer status);

    /**
     * 根据权限类型获取权限列表
     */
    List<Permission> getPermissionsByType(Integer type);

    /**
     * 获取父权限列表
     */
    List<Permission> getParentPermissions();

    /**
     * 获取权限类型统计
     */
    Map<String, Long> getPermissionTypeStats();
}