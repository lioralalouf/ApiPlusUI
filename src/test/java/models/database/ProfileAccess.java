package models.database;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
public class ProfileAccess extends DHPBaseModel {

    private String externalEntityID;
    private String accessDate;
    private Long accessTimestamp;
    private Long foregroundAccessTimestamp;
    private Long backgroundAccessTimestamp;

    public String getExternalEntityID() {
        return externalEntityID;
    }

    public void setExternalEntityID(String externalEntityID) {
        this.externalEntityID = externalEntityID;
    }

    public String getAccessDate() {
        return accessDate;
    }

    public void setAccessDate(String accessDate) {
        this.accessDate = accessDate;
    }

    public Long getAccessTimestamp() {
        return accessTimestamp;
    }

    public void setAccessTimestamp(Long accessTimestamp) {
        this.accessTimestamp = accessTimestamp;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public Long getForegroundAccessTimestamp() {
        return foregroundAccessTimestamp;
    }

    public void setForegroundAccessTimestamp(Long foregroundAccessTimestamp) {
        this.foregroundAccessTimestamp = foregroundAccessTimestamp;
    }

    public Long getBackgroundAccessTimestamp() {
        return backgroundAccessTimestamp;
    }

    public void setBackgroundAccessTimestamp(Long backgroundAccessTimestamp) {
        this.backgroundAccessTimestamp = backgroundAccessTimestamp;
    }
}
