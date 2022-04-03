package models.database;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

public class DHPBaseModel {

    protected String pkey;
    protected String skey;
    protected String rowCreated;
    protected String rowModified;
    protected String objectName;
    protected String rowIdentifier;

    public String getPkey() {
        return pkey;
    }

    public void setPkey(String pkey) {
        this.pkey = pkey;
    }

    public String getRowIdentifier() {
        return rowIdentifier;
    }

    public void setRowIdentifier(String rowIdentifier) {
        this.rowIdentifier = rowIdentifier;
    }

    @DynamoDbPartitionKey
    public String getpkey() {
        return pkey;
    }

    public void setpkey(String pkey) {
        this.pkey = pkey;
    }

    @DynamoDbSortKey
    public String getSkey() {
        return skey;
    }

    public void setSkey(String skey) {
        this.skey = skey;
    }

    public String getRowCreated() {
        return rowCreated;
    }

    public void setRowCreated(String rowCreated) {
        this.rowCreated = rowCreated;
    }

    public String getRowModified() {
        return rowModified;
    }

    public void setRowModified(String rowModified) {
        this.rowModified = rowModified;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }
}
