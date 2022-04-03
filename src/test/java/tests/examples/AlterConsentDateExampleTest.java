package tests.examples;

import models.database.PartnerUserConnection;
import repository.PartnerUserConnectionRepository;

public class AlterConsentDateExampleTest {



    public static void main(String args[]) {
        PartnerUserConnectionRepository partnerUserConnectionRepository = new PartnerUserConnectionRepository();
        PartnerUserConnection partnerUserConnection = partnerUserConnectionRepository.findConsentByPatientPartner("1646581165311","de348766-aa79-4ae2-b610-7823f186bc89");

        partnerUserConnection.setConsentStartDate("1900-01-01T00:00:00");

        partnerUserConnectionRepository.updatePatientPartnerConsent(partnerUserConnection);

        System.exit(0);

    }
}
