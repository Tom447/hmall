package com.hmall.search.domain.vo;



import cn.hutool.core.collection.CollectionUtil;
import com.hmall.common.utils.BeanUtils;
import com.hmall.common.utils.CollUtils;
import com.hmall.common.utils.Convert;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {
    protected Long total;
    protected Long pages;
    protected List<T> list;

    public static <T> PageVO<T> of(Long total, Long pages , List<T> list) {
        return new PageVO<>(total, pages, list);
    }

    public static <T> PageVO<T> empty(Long total, Long pages) {
        return new PageVO<>(total, pages, Collections.emptyList());
    }


}
