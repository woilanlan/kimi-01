package top.hxll.kimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import top.hxll.kimi.dto.RoleDto;
import top.hxll.kimi.entity.Role;
import top.hxll.kimi.entity.RolePermission;
import top.hxll.kimi.entity.UserRole;
import top.hxll.kimi.mapper.RoleMapper;
import top.hxll.kimi.mapper.RolePermissionMapper;
import top.hxll.kimi.mapper.UserRoleMapper;
import top.hxll.kimi.security.service.UserDetailsImpl;
import top.hxll.kimi.service.RoleService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 *
 * @author kimi
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;


    @Override
    public Set<Long> getValidRoleIds(List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptySet();
        }

        LambdaQueryWrapper<Role> qw = new LambdaQueryWrapper<Role>()
                .in(Role::getId, roleIds)
                .select(Role::getId);

        return new HashSet<>(this.listObjs(qw, obj -> (Long) obj));
    }

    @Override
    public List<RoleDto> getAllRoles() {
        QueryWrapper<Role> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0)
               .orderByAsc("sort_order", "id");

        List<Role> roles = roleMapper.selectList(wrapper);
        return roles.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public IPage<RoleDto> getRolePage(Page<Role> page, String keyword, Integer status) {
        QueryWrapper<Role> wrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like("role_name", keyword)
                   .or()
                   .like("role_code", keyword);
        }

        if (status != null) {
            wrapper.eq("status", status);
        }

        wrapper.eq("deleted", 0)
               .orderByAsc("sort_order", "id");

        IPage<Role> rolePage = roleMapper.selectPage(page, wrapper);
        IPage<RoleDto> dtoPage = new Page<>();
        dtoPage.setCurrent(rolePage.getCurrent());
        dtoPage.setSize(rolePage.getSize());
        dtoPage.setTotal(rolePage.getTotal());
        dtoPage.setRecords(rolePage.getRecords().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));

        return dtoPage;
    }

    @Override
    public RoleDto getRoleById(Long id) {
        Role role = getRoleByIdRaw(id);
        return role != null ? convertToDto(role) : null;
    }

    @Override
    public Role getRoleByIdRaw(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public Role getRoleByCode(String roleCode) {
        QueryWrapper<Role> wrapper = new QueryWrapper<>();
        wrapper.eq("role_code", roleCode)
               .eq("deleted", 0);
        return roleMapper.selectOne(wrapper);
    }

    @Override
    public List<RoleDto> getRolesByUserId(Long userId) {
        List<Role> roles = roleMapper.selectRolesByUserId(userId);
        return roles.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Role createRole(RoleDto roleDto) {
        // 检查角色编码是否已存在
        if (getRoleByCode(roleDto.getRoleCode()) != null) {
            throw new RuntimeException("角色编码已存在");
        }

        Role role = new Role();
        role.setRoleName(roleDto.getRoleName());
        role.setRoleCode(roleDto.getRoleCode());
        role.setDescription(roleDto.getDescription());
        role.setSortOrder(roleDto.getSortOrder() != null ? roleDto.getSortOrder() : 0);
        role.setStatus(roleDto.getStatus() != null ? roleDto.getStatus() : 1);
        role.setCreateBy(getCurrentUserId());
        role.setCreateTime(LocalDateTime.now());

        roleMapper.insert(role);
        log.info("Role created successfully: {}", role.getRoleCode());
        return role;
    }

    @Override
    @Transactional
    public Role updateRole(Long id, RoleDto roleDto) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        // 检查角色编码是否被其他角色使用
        if (!role.getRoleCode().equals(roleDto.getRoleCode())) {
            Role existingRole = getRoleByCode(roleDto.getRoleCode());
            if (existingRole != null && !existingRole.getId().equals(id)) {
                throw new RuntimeException("角色编码已存在");
            }
        }

        role.setRoleName(roleDto.getRoleName());
        role.setRoleCode(roleDto.getRoleCode());
        role.setDescription(roleDto.getDescription());
        role.setSortOrder(roleDto.getSortOrder() != null ? roleDto.getSortOrder() : 0);
        role.setStatus(roleDto.getStatus() != null ? roleDto.getStatus() : 1);
        role.setUpdateBy(getCurrentUserId());
        role.setUpdateTime(LocalDateTime.now());

        roleMapper.updateById(role);
        log.info("Role updated successfully: {}", role.getRoleCode());
        return role;
    }

    @Override
    @Transactional
    public boolean deleteRole(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        // 检查是否被用户使用
        List<UserRole> userRoles = userRoleMapper.selectList(
                new QueryWrapper<UserRole>().eq("role_id", id));
        if (!CollectionUtils.isEmpty(userRoles)) {
            throw new RuntimeException("该角色已被用户使用，不能删除");
        }

        // 逻辑删除
        role.setDeleted(1);
        role.setUpdateBy(getCurrentUserId());
        role.setUpdateTime(LocalDateTime.now());

        int result = roleMapper.updateById(role);

        // 删除角色权限关联
        rolePermissionMapper.deleteByRoleId(id);

        log.info("Role deleted logically: {}", role.getRoleCode());
        return result > 0;
    }

    @Override
    @Transactional
    public boolean deleteRoles(List<Long> ids) {
        for (Long id : ids) {
            deleteRole(id);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean toggleRoleStatus(Long id, Integer status) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        role.setStatus(status);
        role.setUpdateBy(getCurrentUserId());
        role.setUpdateTime(LocalDateTime.now());

        int result = roleMapper.updateById(role);
        log.info("Role status changed: {} -> {}", role.getRoleCode(), status);
        return result > 0;
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    @Transactional
    public boolean updateRolePermissions(Long roleId, List<Long> permissionIds) {
        // 删除原有权限关联
        rolePermissionMapper.deleteByRoleId(roleId);

        // 添加新权限关联
        if (!CollectionUtils.isEmpty(permissionIds)) {
            List<RolePermission> rolePermissions = permissionIds.stream()
                    .distinct()
                    .map(permissionId -> {
                        RolePermission rolePermission = new RolePermission();
                        rolePermission.setRoleId(roleId);
                        rolePermission.setPermissionId(permissionId);
                        rolePermission.setCreateBy(getCurrentUserId());
                        rolePermission.setCreateTime(LocalDateTime.now());
                        return rolePermission;
                    })
                    .collect(Collectors.toList());

            if (!rolePermissions.isEmpty()) {
                rolePermissionMapper.batchInsert(rolePermissions);
            }
        }

        log.info("Updated permissions for role {}: {}", roleId, permissionIds);
        return true;
    }

    @Override
    public Map<String, Long> getRoleStats() {
        Map<String, Long> stats = new HashMap<>();

        // 总角色数
        long totalCount = roleMapper.selectCount(new QueryWrapper<Role>().eq("deleted", 0));
        stats.put("total", totalCount);

        // 启用角色数
        long activeCount = roleMapper.selectCount(new QueryWrapper<Role>().eq("status", 1).eq("deleted", 0));
        stats.put("active", activeCount);

        // 禁用角色数
        long inactiveCount = roleMapper.selectCount(new QueryWrapper<Role>().eq("status", 0).eq("deleted", 0));
        stats.put("inactive", inactiveCount);

        return stats;
    }

    /**
     * 实体转换为DTO
     */
    private RoleDto convertToDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setRoleName(role.getRoleName());
        dto.setRoleCode(role.getRoleCode());
        dto.setDescription(role.getDescription());
        dto.setSortOrder(role.getSortOrder());
        dto.setStatus(role.getStatus());
        dto.setCreateTime(role.getCreateTime());
        return dto;
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                return userDetails.getId();
            }
        } catch (Exception e) {
            log.error("Failed to get current user ID", e);
        }
        return null;
    }
}