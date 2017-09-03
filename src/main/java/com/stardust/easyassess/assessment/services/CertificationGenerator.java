package com.stardust.easyassess.assessment.services;


import com.stardust.easyassess.assessment.models.CertificationModel;

import java.io.IOException;
import java.io.OutputStream;

public interface CertificationGenerator {
    void generate(CertificationModel model, OutputStream output) throws IOException;
}
