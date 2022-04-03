package service;

import models.database.MedicalDevice;
import repository.InhalerRepository;

public class InhalerService {

    private InhalerRepository inhalerRepository = new InhalerRepository();

    public MedicalDevice findInhalerBySerialNumber(String externalEntityID, Long serialNumber) {
        return inhalerRepository.findInhalerBySerialNumber(externalEntityID, serialNumber);
    }

    public void updateInhaler(MedicalDevice medicalDevice) {
        inhalerRepository.updateInhaler(medicalDevice);
    }

}
