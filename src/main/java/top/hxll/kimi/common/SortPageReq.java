package top.hxll.kimi.common;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 可排序分页请求类
 *
 * @author kimi
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SortPageReq extends PageReq {

    private static final long serialVersionUID = 1L;

    /**
     * 排序列表
     */
    private List<SortItem> sorts;

    /**
     * 转换为MyBatis Plus的Page对象（带排序）
     */
    @Override
    public <T> Page<T> toPage() {
        Page<T> page = super.toPage();

        if (sorts != null && !sorts.isEmpty()) {
            sorts.forEach(sort -> {
                if (sort.getField() != null && !sort.getField().trim().isEmpty()) {
                    boolean isAsc = !"desc".equalsIgnoreCase(sort.getOrder());
                    page.addOrder(isAsc ? OrderItem.asc(sort.getField())
                                       : OrderItem.desc(sort.getField()));
                }
            });
        }

        return page;
    }
}
