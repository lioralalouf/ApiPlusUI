package service;

import models.database.PartnerKey;
import models.database.PartnerUserConnection;
import repository.PartnerRepository;
import repository.PartnerUserConnectionRepository;
import utils.DateUtils;

import java.util.List;

public class PartnerService {

    private PartnerRepository partnerRepository = new PartnerRepository();
    private PartnerUserConnectionRepository partnerUserConnectionRepository = new PartnerUserConnectionRepository();

    public void expireApiKey(String partnerID) {
        List<PartnerKey> partnerKeys =partnerRepository.findApiKeyByPartnerID(partnerID);

        for (PartnerKey partnerKey: partnerKeys) {
            partnerKey.setGrantAccessDate(DateUtils.getDate());
            partnerRepository.persistPartnerKey(partnerKey);
        }
    }

    public PartnerUserConnection findConsentByPatientPartner() {

        PartnerUserConnection partnerUserConnection = partnerUserConnectionRepository.findConsentByPatientPartner("ds","asd");

        return null;
    }

    public void updatePatientPartnerConsent(PartnerUserConnection partnerUserConnection) {
        partnerUserConnectionRepository.updatePatientPartnerConsent(partnerUserConnection);
    }
}
