package com.stardust.easyassess.assessment.services;


import com.stardust.easyassess.assessment.models.form.Form;
import jxl.write.WriteException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public interface FormService extends EntityService<Form> {
    Form submit(Form form);

    void exportToExcel(Form form, OutputStream outputStream) throws IOException, WriteException;

    String addAttachment(String formId, String fileName, InputStream inputStream);
}
