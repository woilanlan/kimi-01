package top.hxll.kimi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * 自定义信息贡献者
 * Spring Boot 2.x 后，默认不会自动将 info.* 配置属性转换为 info 端点内容
 * 需要显式启用或通过 InfoContributor 实现
 *
 * @author kimi
 * @since 1.0.0
 */
@Component
public class CustomInfoContributor implements InfoContributor {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${info.app.description:}")
    private String description;

    @Value("${info.app.version:}")
    private String version;

    @Value("${info.app.author:}")
    private String author;

    @Override
    public void contribute(Info.Builder builder) {
        // Java 9+ 版本来使用 Map.of()
//        builder.withDetail("app", Map.of(
//                "name", appName,
//                "description", description,
//                "version", version,
//                "author", author
//        ));

        //只需要读取信息的场景，使用不可变 Map 更为合适。
        Map<String, Object> appInfo = Collections.unmodifiableMap(new HashMap<String, Object>() {{
            put("name", appName);
            put("description", description);
            put("version", version);
            put("author", author);
        }});
        builder.withDetail("app", appInfo);

    }
}

