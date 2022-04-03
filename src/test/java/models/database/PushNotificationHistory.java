package models.database;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
public class PushNotificationHistory extends DHPBaseModel {

    private String notificationType;
    private int count;
    private String data;
    private String externalEntityID;
    private Long timestamp;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getExternalEntityID() {
        return externalEntityID;
    }

    public void setExternalEntityID(String externalEntityID) {
        this.externalEntityID = externalEntityID;
    }
}
