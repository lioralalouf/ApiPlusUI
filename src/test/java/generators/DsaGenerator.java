package generators;

import models.database.DailySelfAssessment;

public class DsaGenerator {

    public static DailySelfAssessment getDsa() {

        DailySelfAssessment dailySelfAssessment = new DailySelfAssessment();

        dailySelfAssessment.date = "2020-01-01";
        dailySelfAssessment.assessment = 1;

        return dailySelfAssessment;
    }
}
