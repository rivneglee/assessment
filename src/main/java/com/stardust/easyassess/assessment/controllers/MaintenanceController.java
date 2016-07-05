package com.stardust.easyassess.assessment.controllers;


import com.stardust.easyassess.assessment.models.DataModel;
import com.stardust.easyassess.assessment.services.EntityService;
import com.stardust.easyassess.core.context.ContextSession;
import com.stardust.easyassess.core.presentation.Message;
import com.stardust.easyassess.core.presentation.ResultCode;
import com.stardust.easyassess.core.presentation.ViewJSONWrapper;
import com.stardust.easyassess.core.query.Selection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class MaintenanceController<T> {
    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    protected HttpServletRequest getRequest() {
        return request;
    }

    protected HttpServletResponse getResponse() {
        return response;
    }

    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    protected ContextSession getSession() {
        return applicationContext.getBean(ContextSession.class);
    }

    protected abstract EntityService<T> getService();

    protected Map<String, Object> getUserProfile() {
        Map<String, Object> profile = (Map<String, Object>)getSession().get("userProfile");
        if (profile == null) {
            profile = new HashMap<String, Object>();
        }

        return profile;
    }


    @RequestMapping(path="/list",
            method={RequestMethod.GET})
    public ViewJSONWrapper list(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                @RequestParam(value = "size", defaultValue = "4") Integer size,
                                @RequestParam(value = "sort", defaultValue = "id") String sort,
                                @RequestParam(value = "filterField", defaultValue = "") String field,
                                @RequestParam(value = "filterValue", defaultValue = "") String value ) {

        List<Selection> selections = new ArrayList<Selection>();
        selections.add(new Selection(field, Selection.Operator.LIKE, value));
        return new ViewJSONWrapper(getService().list(page, size , sort, selections));
    }

    @RequestMapping(value = "/{id}",
            method={RequestMethod.GET})
    public ViewJSONWrapper get(@PathVariable String id) {
        if (preGet(id)) {
            return postGet(getService().get(id));
        } else {
            return createEmptyResult();
        }
    }

    @ResponseBody
    @RequestMapping(value="{id}", method={RequestMethod.PUT})
    public ViewJSONWrapper update(@PathVariable String id,
                                  @RequestBody T model) {
        if (preUpdate(id, model)) {
            return postUpdate(getService().save(model));
        } else {
            return createEmptyResult();
        }
    }

    @ResponseBody
    @RequestMapping(method={RequestMethod.POST})
    public ViewJSONWrapper add(@RequestBody T model) {
        if (preAdd(model)) {
            return postAdd(getService().save(model));
        } else {
            return createEmptyResult();
        }
    }

    @ResponseBody
    @RequestMapping(value="{id}", method={RequestMethod.DELETE})
    public void delete(@PathVariable String id) {
        if (preDelete(id)) {
            getService().remove(id);
            postDelete(id);
        }
    }

    protected boolean preGet(String id) {
        return true;
    }

    protected ViewJSONWrapper postGet(T model) {
        return new ViewJSONWrapper(model);
    }

    protected boolean preList(List<Selection> selections) {
        return true;
    }

    protected ViewJSONWrapper postList(Page<T> page) {
        return new ViewJSONWrapper(page);
    }

    protected boolean preDelete(String id) {
        return true;
    }

    protected void postDelete(String id) {

    }

    protected boolean preUpdate(String id, T model) {
        return ((DataModel)model).validate();
    }

    protected ViewJSONWrapper postUpdate(T model) {
        return new ViewJSONWrapper(model);
    }

    protected boolean preAdd(T model) {
        return ((DataModel)model).validate();
    }

    protected ViewJSONWrapper postAdd(T model) {
        return new ViewJSONWrapper(model);
    }

    protected ViewJSONWrapper createEmptyResult() {
        return new ViewJSONWrapper(new Message("无记录"), ResultCode.NOT_FOUND);
    }
}
