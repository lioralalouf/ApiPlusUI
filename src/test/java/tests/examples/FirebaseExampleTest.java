package tests.examples;

import models.database.Job;
import models.database.ProfileAccess;
import models.database.PushNotificationHistory;
import org.junit.Assert;
import org.testng.annotations.Test;
import service.JobsService;
import service.MedicationEventService;
import service.ProfileAccessService;
import service.PushNotificationHistoryService;

import java.util.List;
import java.util.Optional;

public class FirebaseExampleTest {

    private JobsService jobsService = new JobsService();
    private ProfileAccessService profileAccessService = new ProfileAccessService();
    private PushNotificationHistoryService pushNotificationHistoryService = new PushNotificationHistoryService();
    private MedicationEventService medicationEventService = new MedicationEventService();

    @Test
    public void tc01_firebaseTest() {
        ProfileAccess profileAccess = profileAccessService.findByExternalEntityID("00525f36-e993-4293-89ab-eff62876faed");

        Assert.assertNotNull(profileAccess);
        profileAccessService.updateLastAccess("00525f36-e993-4293-89ab-eff62876faed",0,0L,0L);
    }

    @Test
    public void tc02_firebaseTest() {
        List<Job> jobs = jobsService.findJobByFunction("dhp-apis-automation-int-startDigiConnNotifications");

        for (Job job : jobs ) {
            jobsService.removeJobByFunction(job.getpkey());
        }

        jobs = jobsService.findJobByFunction("dhp-apis-automation-int-startDigiConnNotifications");
        Assert.assertEquals(0, jobs.size());
    }

    @Test
    public void tc03_firebaseTest() {

        pushNotificationHistoryService.createNotificationRecord("FED-ALEXAYERS","engagement_booster", 9, 12345L);
        PushNotificationHistory pushNotificationHistory = pushNotificationHistoryService.find("FED-ALEXAYERS","engagement_booster" );
        Assert.assertNotNull(pushNotificationHistory);
        pushNotificationHistoryService.updateCount("FED-ALEXAYERS", "engagement_booster", 10);
        pushNotificationHistory = pushNotificationHistoryService.find("FED-ALEXAYERS","engagement_booster" );
        Assert.assertNotNull(pushNotificationHistory);
        Assert.assertEquals(10, pushNotificationHistory.getCount());
        pushNotificationHistoryService.updateTimestamp("FED-ALEXAYERS", "engagement_booster", 9000L);
        pushNotificationHistory = pushNotificationHistoryService.find("FED-ALEXAYERS","engagement_booster" );
        Assert.assertNotNull(pushNotificationHistory);

        Assert.assertEquals(Optional.ofNullable(9000L), Optional.ofNullable(pushNotificationHistory.getTimestamp()));

    }

    @Test
    public void tc04_firebaseTest() {
        medicationEventService.removeByID("ASDAS");
    }

}
