package models.request.medicaldevice;

import models.database.MedicalDevice;
import models.request.PlatformRequestBase;

public class RegisterMedicalDeviceRequest extends PlatformRequestBase {

    public InhalerDto inhaler;

    public RegisterMedicalDeviceRequest(MedicalDevice medicalDevice) {
        inhaler = new InhalerDto();

        inhaler.serialNumber = medicalDevice.getSerialNumber();
        inhaler.nickName = medicalDevice.getNickName();
        inhaler.authenticationKey = medicalDevice.getAuthenticationKey();
        inhaler.drugID = medicalDevice.getDrugID();
        inhaler.addedDate = medicalDevice.getAddedDate();
        inhaler.hardwareRevision = medicalDevice.getHardwareRevision();
        inhaler.softwareRevision = medicalDevice.getSoftwareRevision();
        inhaler.lastConnectionDate = medicalDevice.getLastConnectionDate();
        inhaler.remainingDoseCount = medicalDevice.getRemainingDoseCount();
        

    }
}
