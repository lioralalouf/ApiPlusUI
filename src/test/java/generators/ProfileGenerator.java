package generators;

import com.github.javafaker.Faker;
import models.database.Profile;
import utils.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class ProfileGenerator {

    public static Profile getProfile() {
        Profile profile = new Profile();
        Faker faker = new Faker();


        String dob =DateUtils.extractDate(faker.date().birthday(18,80));
        profile.setFirstName(faker.name().firstName());
        profile.setLastName(faker.name().lastName());
        profile.setDateOfBirth(dob);
        profile.setExternalEntityID("FED-" + faker.number().digits(15));
        profile.setLocale("en-US");
        profile.setEmail(profile.getFirstName() + "." + profile.getFirstName() + "+" + UUID.randomUUID().toString() + "@example.com");

        return profile;
    }

    public static Profile getDependent() {
        Profile profile = new Profile();
        Faker faker = new Faker();


        Date birthday = faker.date().birthday(14,18);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(birthday);
        calendar.add(Calendar.YEAR, 21);

        String dob = DateUtils.extractDate(birthday);
        String ageOfMajority = DateUtils.extractDate(calendar.getTime());

        profile.setFirstName(faker.name().firstName());
        profile.setLastName(faker.name().lastName());
        profile.setDateOfBirth(dob);
        profile.setLocale("en-US");
        profile.setExternalEntityID(UUID.randomUUID().toString());
        profile.setAgeOfMajority(ageOfMajority);

        return profile;
    }
}
