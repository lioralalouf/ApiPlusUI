package models.database;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
public class PartnerUserConnection extends DHPBaseModel {

    private String partnerID;
    private String consentID;
    private String status;
    private String externalEntityID;
    private Boolean privacyNoticeReadIndicator;
    private String consentType;
    private String sourceTime_TZ;
    private Boolean acceptIndicator;
    private String timestamp;
    private Integer serverTimeOffset;
    private String dataEntryClassification;
    private String sourceTime_GMT;
    private String patientID;
    private String termsAndConditionsVersion;
    private String privacyNoticeVersion;
    private String messageID;
    private String consentAuditableEventDate;
    private String signatureVersion;
    private String consentStartDate;
    private Boolean consentEnabledStatus;
    private Boolean historicalDataIndicator;
    private String hipaaDisclosureVersion;

    public String getPartnerID() {
        return partnerID;
    }

    public void setPartnerID(String partnerID) {
        this.partnerID = partnerID;
    }

    public String getConsentID() {
        return consentID;
    }

    public void setConsentID(String consentID) {
        this.consentID = consentID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExternalEntityID() {
        return externalEntityID;
    }

    public void setExternalEntityID(String externalEntityID) {
        this.externalEntityID = externalEntityID;
    }

    public Boolean getPrivacyNoticeReadIndicator() {
        return privacyNoticeReadIndicator;
    }

    public void setPrivacyNoticeReadIndicator(Boolean privacyNoticeReadIndicator) {
        this.privacyNoticeReadIndicator = privacyNoticeReadIndicator;
    }

    public String getConsentType() {
        return consentType;
    }

    public void setConsentType(String consentType) {
        this.consentType = consentType;
    }

    public String getSourceTime_TZ() {
        return sourceTime_TZ;
    }

    public void setSourceTime_TZ(String sourceTime_TZ) {
        this.sourceTime_TZ = sourceTime_TZ;
    }

    public Boolean getAcceptIndicator() {
        return acceptIndicator;
    }

    public void setAcceptIndicator(Boolean acceptIndicator) {
        this.acceptIndicator = acceptIndicator;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getServerTimeOffset() {
        return serverTimeOffset;
    }

    public void setServerTimeOffset(Integer serverTimeOffset) {
        this.serverTimeOffset = serverTimeOffset;
    }

    public String getDataEntryClassification() {
        return dataEntryClassification;
    }

    public void setDataEntryClassification(String dataEntryClassification) {
        this.dataEntryClassification = dataEntryClassification;
    }

    public String getSourceTime_GMT() {
        return sourceTime_GMT;
    }

    public void setSourceTime_GMT(String sourceTime_GMT) {
        this.sourceTime_GMT = sourceTime_GMT;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getTermsAndConditionsVersion() {
        return termsAndConditionsVersion;
    }

    public void setTermsAndConditionsVersion(String termsAndConditionsVersion) {
        this.termsAndConditionsVersion = termsAndConditionsVersion;
    }

    public String getPrivacyNoticeVersion() {
        return privacyNoticeVersion;
    }

    public void setPrivacyNoticeVersion(String privacyNoticeVersion) {
        this.privacyNoticeVersion = privacyNoticeVersion;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getConsentAuditableEventDate() {
        return consentAuditableEventDate;
    }

    public void setConsentAuditableEventDate(String consentAuditableEventDate) {
        this.consentAuditableEventDate = consentAuditableEventDate;
    }

    public String getSignatureVersion() {
        return signatureVersion;
    }

    public void setSignatureVersion(String signatureVersion) {
        this.signatureVersion = signatureVersion;
    }

    public String getConsentStartDate() {
        return consentStartDate;
    }

    public void setConsentStartDate(String consentStartDate) {
        this.consentStartDate = consentStartDate;
    }

    public Boolean getConsentEnabledStatus() {
        return consentEnabledStatus;
    }

    public void setConsentEnabledStatus(Boolean consentEnabledStatus) {
        this.consentEnabledStatus = consentEnabledStatus;
    }

    public Boolean getHistoricalDataIndicator() {
        return historicalDataIndicator;
    }

    public void setHistoricalDataIndicator(Boolean historicalDataIndicator) {
        this.historicalDataIndicator = historicalDataIndicator;
    }

    public String getHipaaDisclosureVersion() {
        return hipaaDisclosureVersion;
    }

    public void setHipaaDisclosureVersion(String hipaaDisclosureVersion) {
        this.hipaaDisclosureVersion = hipaaDisclosureVersion;
    }
}


