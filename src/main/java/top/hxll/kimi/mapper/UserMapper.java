package top.hxll.kimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.hxll.kimi.entity.User;

import java.util.List;

/**
 * 用户Mapper接口
 *
 * @author kimi
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户（包含角色和权限）
     */
    User selectUserWithRolesAndPermissions(@Param("username") String username);

    /**
     * 根据用户ID查询权限编码列表
     */
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户基本信息（分页）- 只包含列表展示需要的字段
     * 排除了敏感字段：password, last_login_ip, create_by, update_by, deleted
     */
    IPage<User> selectUserBasicPage(Page<User> page, @Param("ew") Wrapper<User> queryWrapper);
}