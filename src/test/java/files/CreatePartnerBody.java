package files;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreatePartnerBody {

	public CreatePartnerBody() {
		super();
	}
	

	public class Redirects {
		@JsonProperty("success")
		public String getSuccess() {
			return this.success;
		}

		public void setSuccess(String success) {
			this.success = success;
		}

		String success;

		@JsonProperty("failure")
		public String getFailure() {
			return this.failure;
		}

		public void setFailure(String failure) {
			this.failure = failure;
		}

		String failure;
	}

	public class Callbacks {
		@JsonProperty("success")
		public String getSuccess() {
			return this.success;
		}

		public void setSuccess(String success) {
			this.success = success;
		}

		String success;

		@JsonProperty("failure")
		public String getFailure() {
			return this.failure;
		}

		public void setFailure(String failure) {
			this.failure = failure;
		}

		String failure;
	}

	public class Throttle {
		@JsonProperty("rate")
		public int getRate() {
			return this.rate;
		}

		public void setRate(int rate) {
			this.rate = rate;
		}

		int rate;

		@JsonProperty("burst")
		public int getBurst() {
			return this.burst;
		}

		public void setBurst(int burst) {
			this.burst = burst;
		}

		int burst;
	}

	public class Quota {
		@JsonProperty("limit")
		public int getLimit() {
			return this.limit;
		}

		public void setLimit(int limit) {
			this.limit = limit;
		}

		int limit;

		@JsonProperty("period")
		public String getPeriod() {
			return this.period;
		}

		public void setPeriod(String period) {
			this.period = period;
		}

		String period;
	}

	public class Contact {
		@JsonProperty("firstName")
		public String getFirstName() {
			return this.firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		String firstName;

		@JsonProperty("lastName")
		public String getLastName() {
			return this.lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		String lastName;

		@JsonProperty("email")
		public String getEmail() {
			return this.email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		String email;

		@JsonProperty("phoneNumber")
		public String getPhoneNumber() {
			return this.phoneNumber;
		}

		public void setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
		}

		String phoneNumber;
	}

	public class Root {
		@JsonProperty("name")
		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}

		String name;

		@JsonProperty("icon")
		public String getIcon() {
			return this.icon;
		}

		public void setIcon(String icon) {
			this.icon = icon;
		}

		String icon;

		@JsonProperty("activePrivacyNoticeVersion")
		public String getActivePrivacyNoticeVersion() {
			return this.activePrivacyNoticeVersion;
		}

		public void setActivePrivacyNoticeVersion(String activePrivacyNoticeVersion) {
			this.activePrivacyNoticeVersion = activePrivacyNoticeVersion;
		}

		String activePrivacyNoticeVersion;

		@JsonProperty("activeTermsOfUse")
		public String getActiveTermsOfUse() {
			return this.activeTermsOfUse;
		}

		public void setActiveTermsOfUse(String activeTermsOfUse) {
			this.activeTermsOfUse = activeTermsOfUse;
		}

		String activeTermsOfUse;

		@JsonProperty("activeHipaaDisclosure")
		public String getActiveHipaaDisclosure() {
			return this.activeHipaaDisclosure;
		}

		public void setActiveHipaaDisclosure(String activeHipaaDisclosure) {
			this.activeHipaaDisclosure = activeHipaaDisclosure;
		}

		String activeHipaaDisclosure;

		@JsonProperty("activeSignature")
		public String getActiveSignature() {
			return this.activeSignature;
		}

		public void setActiveSignature(String activeSignature) {
			this.activeSignature = activeSignature;
		}

		String activeSignature;

		@JsonProperty("activeMarketingConsent")
		public String getActiveMarketingConsent() {
			return this.activeMarketingConsent;
		}

		public void setActiveMarketingConsent(String activeMarketingConsent) {
			this.activeMarketingConsent = activeMarketingConsent;
		}

		String activeMarketingConsent;

		@JsonProperty("redirects")
		public Redirects getRedirects() {
			return this.redirects;
		}

		public void setRedirects(Redirects redirects) {
			this.redirects = redirects;
		}

		Redirects redirects;

		@JsonProperty("callbacks")
		public Callbacks getCallbacks() {
			return this.callbacks;
		}

		public void setCallbacks(Callbacks callbacks) {
			this.callbacks = callbacks;
		}

		Callbacks callbacks;

		@JsonProperty("throttle")
		public Throttle getThrottle() {
			return this.throttle;
		}

		public void setThrottle(Throttle throttle) {
			this.throttle = throttle;
		}

		Throttle throttle;

		@JsonProperty("quota")
		public Quota getQuota() {
			return this.quota;
		}

		public void setQuota(Quota quota) {
			this.quota = quota;
		}

		Quota quota;

		@JsonProperty("contact")
		public Contact getContact() {
			return this.contact;
		}

		public void setContact(Contact contact) {
			this.contact = contact;
		}

		Contact contact;

	}
}
	