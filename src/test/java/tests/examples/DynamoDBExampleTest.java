package tests.examples;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import models.database.PartnerKey;
import org.testng.Assert;
import org.testng.annotations.Test;
import repository.PartnerRepository;

import java.io.IOException;
import java.util.List;


public class DynamoDBExampleTest {

    private PartnerRepository partnerRepository = new PartnerRepository();

    @Severity(SeverityLevel.CRITICAL)
    @Test(priority = 1)
    public void tc01_database_communication_test() throws IOException {

        List<PartnerKey> partnerKeys =partnerRepository.findApiKeyByPartnerID("377dccb9-1e94-439b-8834-948c63c3b879");

        PartnerKey partnerKey = partnerKeys.get(0);
        Assert.assertNotNull(partnerKey);
        partnerKey.setGrantAccessDate("2000-01-23");
        partnerRepository.persistPartnerKey(partnerKey);

        List<PartnerKey> updateKeys =partnerRepository.findApiKeyByPartnerID("377dccb9-1e94-439b-8834-948c63c3b879");
        PartnerKey updatedPartnerKey = partnerKeys.get(0);
        Assert.assertEquals("2000-01-23",updatedPartnerKey.getGrantAccessDate());

    }

}
