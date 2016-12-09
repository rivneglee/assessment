package com.stardust.easyassess.assessment.services;


import com.stardust.easyassess.assessment.models.Assessment;
import com.stardust.easyassess.assessment.models.form.ActualValue;
import com.stardust.easyassess.assessment.models.form.Form;
import com.stardust.easyassess.assessment.models.form.Specimen;
import com.stardust.easyassess.core.query.Selection;
import jxl.write.WriteException;
import org.springframework.data.domain.Page;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface AssessmentService extends EntityService<Assessment> {
    void createAssessment(Assessment assessment);

    List<Assessment> findByParticipant(String participant);

    Form addParticipant(String assessmentId, String participant, String participantName);

    Form removeParticipant(String assessmentId, String participantId);

    Assessment reopenAssessment(String assessmentId);

    Specimen findSpecimen(String assessmentId, String group,  String specimenCode);

    Assessment finalizeAssessment(String id);

    void finalizeAssessment(Assessment assessment);

    void exportToExcel(Assessment assessment, OutputStream outputStream) throws IOException, WriteException;
}
