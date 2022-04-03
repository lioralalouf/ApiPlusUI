package models.request;

public class PartnerRequest {

    public class SuccessFailureUrls {
        public String success;
        public String failure;
    }

    public class Throttle {
        public int rate;
        public int burst;
    }

    public class Quota {
        public int limit;
        public String period;
    }

    public class PartnerContact {
        public String firstName;
        public String lastName;
        public String email;
        public String phoneNumber;
    }

    public String name;
    public String icon;
    public String activePrivacyNoticeVersion;
    public String activeTermsOfUse;
    public String activeHipaaDisclosure;
    public String activeSignature;
    public String activeMarketingConsent;
    public String onDone;
    public SuccessFailureUrls redirects;
    public SuccessFailureUrls callbacks;
    public Throttle throttle;
    public Quota quota;
    public PartnerContact contact;

}
