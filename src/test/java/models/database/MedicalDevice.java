package models.database;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.Optional;

@DynamoDbBean
public class MedicalDevice extends DHPBaseModel {

    private String authenticationKey;
    private String addedDate;
    private String lastConnectionDate;
    private int lastRecord;
    private int doseCount;
    private String drugID;
    private String nickName;
    private int remainingDoseCount;
    private long serialNumber;
    private String hardwareRevision;
    private String softwareRevision;

    public String getAuthenticationKey() {
        return authenticationKey;
    }

    public void setAuthenticationKey(String authenticationKey) {
        this.authenticationKey = authenticationKey;
    }

    public String getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(String addedDate) {
        this.addedDate = addedDate;
    }
    
    public String getLastConnectionDate() {
        return lastConnectionDate;
    }

    public void setLastConnectionDate(String lastConnectionDate) {
        this.lastConnectionDate = lastConnectionDate;
    }

    public int getLastRecord() {
        return lastRecord;
    }

    public void setLastRecord(int lastRecord) {
        this.lastRecord = lastRecord;
    }

    public int getDoseCount() {
        return doseCount;
    }

    public void setDoseCount(int doseCount) {
        this.doseCount = doseCount;
    }

    public String getDrugID() {
        return drugID;
    }

    public void setDrugID(String drugID) {
        this.drugID = drugID;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getRemainingDoseCount() {
        return remainingDoseCount;
    }

    public void setRemainingDoseCount(int remainingDoseCount) {
        this.remainingDoseCount = remainingDoseCount;
    }

    public long getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(long serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getHardwareRevision() {
        return hardwareRevision;
    }

    public void setHardwareRevision(String hardwareRevision) {
        this.hardwareRevision = hardwareRevision;
    }

    public String getSoftwareRevision() {
        return softwareRevision;
    }

    public void setSoftwareRevision(String softwareRevision) {
        this.softwareRevision = softwareRevision;
    }
}
