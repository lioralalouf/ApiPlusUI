package models.request.questionnaire;

import models.request.shared.GeoLocationDto;

public class DailySelfAssessmentDto {

    public String timestamp ="2020-01-01T00:00:00+05:00";
    public DailySelfAssessment dailySelfAssessment = new DailySelfAssessment();

    public class DailySelfAssessment {
        public int assessment;
        public String date;
        public GeoLocationDto geolocation = new GeoLocationDto();
    }

}
