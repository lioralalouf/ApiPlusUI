package models.request.questionnaire;

import models.database.DailySelfAssessment;

import java.util.ArrayList;
import java.util.List;

public class UploadDsasRequest {

    public List<DailySelfAssessmentDto> dailySelfAssessments = new ArrayList<>();

    public UploadDsasRequest(List<DailySelfAssessment> dailySelfAssessments){

        for (DailySelfAssessment dailySelfAssessment : dailySelfAssessments) {

            DailySelfAssessmentDto dailySelfAssessmentDto = new DailySelfAssessmentDto();

            dailySelfAssessmentDto.dailySelfAssessment.assessment = dailySelfAssessment.assessment;
            dailySelfAssessmentDto.dailySelfAssessment.date = dailySelfAssessment.date;

            this.dailySelfAssessments.add(dailySelfAssessmentDto);
        }


    }
}
