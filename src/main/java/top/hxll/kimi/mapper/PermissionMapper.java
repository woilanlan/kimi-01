package top.hxll.kimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.hxll.kimi.entity.Permission;

import java.util.List;

/**
 * 权限Mapper接口
 *
 * @author kimi
 * @since 1.0.0
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据角色ID查询权限列表
     */
    List<Permission> selectPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据父权限ID查询子权限列表
     */
    List<Permission> selectByParentId(@Param("parentId") Long parentId);
}