package generators;

import com.github.javafaker.Faker;
import models.database.MedicalDevice;

public class MedicalDeviceGenerator {


    public static MedicalDevice getProAir() {
        MedicalDevice device = MedicalDeviceGenerator.createBase("Rescue");
        device.setDrugID("AAA200");
        return device;
    }

    public static MedicalDevice getGoResp() {
        MedicalDevice device = MedicalDeviceGenerator.createBase("Maintenance");
        device.setDrugID("BFM180");
        return device;
    }

    public static MedicalDevice getArmonAir() {
        MedicalDevice device  = MedicalDeviceGenerator.createBase("Maintenance");
        device.setDrugID("FPL060");
        return device;
    }

    public static MedicalDevice getAirDuo() {
        MedicalDevice device  = MedicalDeviceGenerator.createBase("Maintenance");
        device.setDrugID("FSL060");
        return device;
    }

    private static MedicalDevice createBase(String drugMedicationType) {
        MedicalDevice medicalDevice = new MedicalDevice();
        Faker faker = new Faker();

        medicalDevice.setSerialNumber(Long.parseLong(faker.number().digits(10)));
        medicalDevice.setAuthenticationKey(faker.number().digits(16));
        medicalDevice.setAddedDate("2020-12-01T00:00:00+04:00");
        medicalDevice.setLastConnectionDate("2020-12-01T00:00:00+04:00");
        medicalDevice.setDrugID(null);
        medicalDevice.setLastRecord(0);
        medicalDevice.setNickName(faker.aquaTeenHungerForce().character());
        medicalDevice.setRemainingDoseCount(5);
        medicalDevice.setHardwareRevision("003H");
        medicalDevice.setSoftwareRevision("1.1.1274.26676");

        return medicalDevice;
    }
}
