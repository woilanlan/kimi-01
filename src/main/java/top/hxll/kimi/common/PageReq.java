package top.hxll.kimi.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 分页请求基类
 *
 * @author kimi
 * @since 1.0.0
 */
@Data
public class PageReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码不能小于1")
    private Integer page;

    /**
     * 每页条数
     */
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 500, message = "每页条数不能大于500")
    private Integer size;

    public Integer getPage() {
        return page == null ? 1 : page;
    }

    public Integer getSize() {
        return size == null ? 10 : size;
    }

    /**
     * 转换为MyBatis Plus的Page对象
     */
    public <T> Page<T> toPage() {
        return new Page<>(getPage(), getSize());
    }
}
