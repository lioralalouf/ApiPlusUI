package models.database;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.List;

@DynamoDbBean
public class PartnerKey extends DHPBaseModel {

    private String partnerID;
    private String apiKey;
    private String rowIdentifier;
    private List<String> ipAddresses;
    private List<String> scopes;
    private String apiKeyID;
    private String grantAccessTimestamp;
    private String grantAccessDate;

    public String getPartnerID() {
        return partnerID;
    }

    public void setPartnerID(String partnerID) {
        this.partnerID = partnerID;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getRowIdentifier() {
        return rowIdentifier;
    }

    public void setRowIdentifier(String rowIdentifier) {
        this.rowIdentifier = rowIdentifier;
    }

    public List<String> getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(List<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public String getApiKeyID() {
        return apiKeyID;
    }

    public void setApiKeyID(String apiKeyID) {
        this.apiKeyID = apiKeyID;
    }

    public String getGrantAccessTimestamp() {
        return grantAccessTimestamp;
    }

    public void setGrantAccessTimestamp(String grantAccessTimestamp) {
        this.grantAccessTimestamp = grantAccessTimestamp;
    }

    public String getGrantAccessDate() {
        return grantAccessDate;
    }

    public void setGrantAccessDate(String grantAccessDate) {
        this.grantAccessDate = grantAccessDate;
    }
}



