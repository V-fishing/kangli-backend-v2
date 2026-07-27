package com.konli.qms.common.api;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用分页结果封装。records 为当前页数据,total 为符合条件的记录总数,
 * page/size 为本次请求的分页参数(1-based)。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<T> records;
    private long total;
    private int page;
    private int size;
}
