package com.stardust.easyassess.assessment.services;


import com.stardust.easyassess.assessment.models.CertificationModel;
import com.stardust.easyassess.assessment.models.Owner;
import com.stardust.easyassess.assessment.models.form.Form;
import jxl.write.WriteException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public interface FormService extends EntityService<Form> {
    Form submit(Form form);

    void exportToExcel(Form form, OutputStream outputStream) throws IOException, WriteException;

    String addAttachment(String formId, String fileType, InputStream inputStream);

    void updateOwnerName(Owner owner);

    void generateAssessmentCertification(String formId, OutputStream outputStream) throws IOException;
}
