package generators;

import models.database.Inhalation;
import models.database.MedicalDevice;

public class InhalationGenerator {


    public static Inhalation generateGoodInhalation(MedicalDevice medicalDevice) {
        Inhalation inhalation = baseData(medicalDevice);
        inhalation.event.category = "good";
        return inhalation;
    }

    public static Inhalation generateFairInhalation(MedicalDevice medicalDevice) {
        Inhalation inhalation = baseData(medicalDevice);
        inhalation.event.category = "fair";
        return inhalation;
    }

    public static Inhalation generateLowInhalation(MedicalDevice medicalDevice) {
        Inhalation inhalation = baseData(medicalDevice);
        inhalation.event.category = "low";
        return inhalation;
    }

    public static Inhalation generateErrorCode1Inhalation(MedicalDevice medicalDevice) {
        Inhalation inhalation = baseData(medicalDevice);
        inhalation.event.category = "errorCode1";
        return inhalation;
    }

    public static Inhalation baseData(MedicalDevice medicalDevice) {
        Inhalation inhalation = new Inhalation();


        inhalation.device.serialNumber = medicalDevice.getSerialNumber();
        inhalation.event.id = 1;
        inhalation.event.time = "2022-01-27T16:36:40.509+00:00";
        inhalation.event.startOffset = 0;
        inhalation.event.duration = 3;
        inhalation.event.peakFlow = 5;
        inhalation.event.volume = 3;
        inhalation.event.status = 100101;
        inhalation.event.peakOffset = 3;
        inhalation.geoLocation.latitude = -71.058;
        inhalation.geoLocation.longitude = 42.358;

        return inhalation;
    }

}
