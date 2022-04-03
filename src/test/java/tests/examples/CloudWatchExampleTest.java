package tests.examples;

import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;
import repository.DataApiAuditCWRepository;
import utils.DateUtils;

import java.io.IOException;


public class CloudWatchExampleTest {

    private DataApiAuditCWRepository dataApiAuditCWRepository = new DataApiAuditCWRepository();

    @Severity(SeverityLevel.CRITICAL)
    @Test(priority = 1)
    public void tc01_cloudwatch_communication_test() throws IOException {


        long tenMinutesAgo = DateUtils.utcTimeMinutesAgo(10);
        long timeNow = DateUtils.utcTimeMinutesAgo(0);

        dataApiAuditCWRepository.findLogsByTimeRange(tenMinutesAgo, timeNow);

    }
}
