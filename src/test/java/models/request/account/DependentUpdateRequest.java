package models.request.account;

import models.database.Profile;
import models.request.PlatformRequestBase;

public class DependentUpdateRequest extends PlatformRequestBase {

    public DependentUpdateDto patient;

    public DependentUpdateRequest(Profile profile) {
        patient = new DependentUpdateDto();
        patient.firstName = profile.getFirstName();
        patient.lastName = profile.getLastName();
        patient.dateOfBirth = profile.getDateOfBirth();
        patient.locale = profile.getLocale();
        patient.ageOfMajority = profile.getAgeOfMajority();

    }
}
