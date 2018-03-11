package com.stardust.easyassess.assessment.controllers;

import com.stardust.easyassess.assessment.models.ArticleReader;
import com.stardust.easyassess.assessment.models.Owner;
import com.stardust.easyassess.assessment.models.form.FormTemplate;
import com.stardust.easyassess.assessment.services.ArticleReaderService;
import com.stardust.easyassess.assessment.services.EntityService;
import com.stardust.easyassess.assessment.services.FormTemplateService;
import com.stardust.easyassess.core.exception.ESAppException;
import com.stardust.easyassess.core.exception.MinistryOnlyException;
import com.stardust.easyassess.core.presentation.ViewJSONWrapper;
import com.stardust.easyassess.core.query.Selection;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping({"{domain}/assess/readers"})
@EnableAutoConfiguration
public class ArticleReaderController extends MaintenanceController<ArticleReader> {
    @Override
    protected EntityService<ArticleReader> getService() {
        return applicationContext.getBean(ArticleReaderService.class);
    }

    @RequestMapping(path = "/mine/articles/unread/{id}",
            method = {RequestMethod.POST})
    public ViewJSONWrapper markAsUnread(@PathVariable String id) throws MinistryOnlyException {
        ArticleReader reader = getService().get(id);
        reader.setHasBeenRead(false);
        getService().save(reader);
        return new ViewJSONWrapper(reader);
    };

    @RequestMapping(path = "/mine/articles/read/{id}",
            method = {RequestMethod.POST})
    public ViewJSONWrapper markAsRead(@PathVariable String id) throws MinistryOnlyException {
        ArticleReader reader = getService().get(id);
        reader.setHasBeenRead(true);
        getService().save(reader);
        return new ViewJSONWrapper(reader);
    }


    @RequestMapping(path = "/mine/articles/unread/list",
            method = {RequestMethod.GET})
    public ViewJSONWrapper listUnread(
                                         @RequestParam(value = "page", defaultValue = "0") Integer page,
                                         @RequestParam(value = "size", defaultValue = "4") Integer size,
                                         @RequestParam(value = "sort", defaultValue = "id") String sort,
                                         @RequestParam(value = "filterField", defaultValue = "") String field,
                                         @RequestParam(value = "filterValue", defaultValue = "") String value) throws MinistryOnlyException {

        List<Selection> selections = new ArrayList();
        selections.add(new Selection(field, Selection.Operator.LIKE, value));
        Owner owner = getNullableOwner();
        if (owner != null) {
            selections.add(new Selection("readerId", Selection.Operator.EQUAL,  owner.getId()));
            selections.add(new Selection("hasBeenRead", Selection.Operator.EQUAL,  false));
            return new ViewJSONWrapper(getService().list(page, size, sort, selections));
        } else {
            return new ViewJSONWrapper(null);
        }
    }

    @RequestMapping(path = "/mine/articles/read/list",
            method = {RequestMethod.GET})
    public ViewJSONWrapper listRead(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "4") Integer size,
            @RequestParam(value = "sort", defaultValue = "id") String sort,
            @RequestParam(value = "filterField", defaultValue = "") String field,
            @RequestParam(value = "filterValue", defaultValue = "") String value) throws MinistryOnlyException {

        List<Selection> selections = new ArrayList();
        selections.add(new Selection(field, Selection.Operator.LIKE, value));
        Owner owner = getNullableOwner();
        if (owner != null) {
            selections.add(new Selection("readerId", Selection.Operator.EQUAL,  owner.getId()));
            selections.add(new Selection("hasBeenRead", Selection.Operator.EQUAL,  true));
            return new ViewJSONWrapper(getService().list(page, size, sort, selections));
        } else {
            return new ViewJSONWrapper(null);
        }
    }
}
