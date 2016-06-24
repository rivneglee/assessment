package com.stardust.easyassess.assessment.services;

import com.stardust.easyassess.assessment.dao.repositories.DataRepository;
import com.stardust.easyassess.core.query.Selection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public abstract class EntityService<T> {

    protected abstract DataRepository<T, String> getRepository();

    public T get(String id) {
        return getRepository().findOne(id);
    }

    public Page<T> list(int page, int size, String sortBy) {
        return list(page, size, sortBy, new ArrayList<Selection>());
    }

    public Page<T> list(int page, int size, String sortBy, List<Selection> selections) {
        Sort sort = new Sort(Sort.Direction.ASC, sortBy);
        Pageable pageable = new PageRequest(page, size, sort);
        return getRepository().findAllBy(pageable, selections);
    }

    public T save(T model) {
        return getRepository().save(model);
    }

    public void remove(T model) {
        getRepository().delete(model);
    }

    public void remove(String id) {
        getRepository().delete(id);
    }
}
