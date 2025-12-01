package top.hxll.kimi.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.hxll.kimi.common.UserContextUtils;
import top.hxll.kimi.security.service.UserDetailsImpl;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * MyBatis Plus 配置类
 *
 * @author kimi
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class MyBatisPlusConfig {

    /**
     * MyBatis Plus 拦截器配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    /**
     * 自动填充配置
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                LocalDateTime now = LocalDateTime.now();

                // 获取用户ID（未登录时使用默认值0L表示系统操作）
                Long userId = getCurrentUserIdSafely();

                // 创建时间
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
                // 更新时间
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
                // 删除标记（默认未删除）
                this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
                // 创建人
                this.strictInsertFill(metaObject, "createBy", Long.class, userId);
                // 更新人
                this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
            }

            /**
             * 安全获取当前用户ID
             * @return 用户ID（未登录时返回0L表示系统用户）
             */
            private Long getCurrentUserIdSafely() {
                try {
                    return UserContextUtils.getCurrentUserId();
                } catch (IllegalStateException e) {
                    return 0L; // 系统用户
                }
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                // 更新时间
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                // 更新人
                this.strictUpdateFill(metaObject, "updateBy", Long.class, getCurrentUserIdSafely());
            }
        };
    }
}