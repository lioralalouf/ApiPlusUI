package service;

import models.database.ProfileAccess;
import repository.ProfileAccessRepository;

public class ProfileAccessService {

    private ProfileAccessRepository profileAccessRepository = new ProfileAccessRepository();

    public void updateLastAccess(String externalEntityID, long accessTimestamp, Long foregroundAccess, Long backgroundAccess ) {

    }

    public ProfileAccess findByExternalEntityID(String externalEntityID) {
        return profileAccessRepository.findByExternalEntityID(externalEntityID);
    }
}
