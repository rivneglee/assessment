package com.stardust.easyassess.assessment.services;


import com.stardust.easyassess.assessment.dao.repositories.DataRepository;
import com.stardust.easyassess.core.query.Selection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEntityService<T> implements EntityService<T> {
    protected abstract DataRepository<T, String> getRepository();

    public T get(String id) {
        return getRepository().findOne(id);
    }

    public Page<T> list(int page, int size, String sortBy) {
        return list(page, size, sortBy, new ArrayList<Selection>());
    }

    public Page<T> list(int page, int size, String sortBy, List<Selection> selections) {
        return getRepository().findAllBy(createPageRequest(page, size, sortBy), selections);
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

    protected PageRequest createPageRequest(int page, int size, String sortBy) {
        Sort sort = new Sort(Sort.Direction.ASC, sortBy);
        return new PageRequest(page, size, sort);
    }
}
