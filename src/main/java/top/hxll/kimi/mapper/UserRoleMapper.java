package top.hxll.kimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.hxll.kimi.entity.UserRole;

import java.util.List;

/**
 * 用户角色关联Mapper接口
 *
 * @author kimi
 * @since 1.0.0
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 批量插入用户角色关联
     */
    int batchInsert(@Param("userRoles") List<UserRole> userRoles);
}