package com.stardust.easyassess.assessment.services;


import com.stardust.easyassess.assessment.models.CertificationModel;
import com.stardust.easyassess.assessment.models.form.Form;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public interface CertificationGenerator {
    URL generate(CertificationModel model) throws IOException;

    InputStream getCertification(Form form);

    void printCertification(CertificationModel model, OutputStream outputStream) throws IOException;
}
