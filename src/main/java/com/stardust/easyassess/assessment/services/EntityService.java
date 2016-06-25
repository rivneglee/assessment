package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.core.query.Selection;
import org.springframework.data.domain.Page;

import java.util.List;

public interface EntityService<T> {
    T get(String id);

    Page<T> list(int page, int size, String sortBy);

    Page<T> list(int page, int size, String sortBy, List<Selection> selections);

    T save(T model);

    void remove(T model);

    void remove(String id);
}
