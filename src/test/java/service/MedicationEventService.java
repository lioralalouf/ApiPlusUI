package service;

import repository.MedicationAdministrationEventRepository;

public class MedicationEventService {

    private MedicationAdministrationEventRepository medicationAdministrationEventRepository = new MedicationAdministrationEventRepository();

    public void removeByID(String id) {
        medicationAdministrationEventRepository.removeByID(id);
    }
}
