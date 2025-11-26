package top.hxll.kimi.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 排序项
 *
 * @author kimi
 * @since 1.0.0
 */
@Data
public class SortItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 排序字段
     */
    private String field;

    /**
     * 排序方式（asc/desc）
     */
    private String order;
}
