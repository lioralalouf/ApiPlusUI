package models.database;

public class Profile {
    private String firstName;
    private String lastName;
    private String email;
    private String dateOfBirth;
    private String externalEntityID;
    private String locale;
    private String ageOfMajority;

    public String getAgeOfMajority() {
		return ageOfMajority;
	}

	public void setAgeOfMajority(String ageOfMajority) {
		this.ageOfMajority = ageOfMajority;
	}

	public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getExternalEntityID() {
        return externalEntityID;
    }

    public void setExternalEntityID(String externalEntityID) {
        this.externalEntityID = externalEntityID;
    }
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
