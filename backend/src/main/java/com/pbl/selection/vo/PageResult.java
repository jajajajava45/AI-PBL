package com.pbl.selection.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.function.Function;

/**
 * 分页结果
 */
@Data
@AllArgsConstructor
public class PageResult<T> {

    private List<T> records;
    private long total;
    private long page;
    private long size;

    public static <S, T> PageResult<T> of(IPage<S> p, Function<S, T> mapper) {
        return new PageResult<>(p.getRecords().stream().map(mapper).toList(),
                p.getTotal(), p.getCurrent(), p.getSize());
    }
}
