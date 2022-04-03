package tests.examples;

import extentReports.TestListeners;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import software.amazon.awssdk.services.lambda.model.Environment;
import software.amazon.awssdk.services.lambda.model.GetFunctionConfigurationRequest;
import software.amazon.awssdk.services.lambda.model.GetFunctionConfigurationResponse;
import utils.LambdaUtils;
import utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class LambdaExampleTest {

    public static void main(String args[]) {
        Map<String,String> environment = LambdaUtils.getLambdaConfiguration(Utils.readProperty("getPatientInhalations"));

        environment.put("volumeTransferEnabled","false");
        environment.put("peakFlowTransferEnabled","false");

        LambdaUtils.updateLambdaConfiguration(Utils.readProperty("getPatientInhalations"), environment);

    }
}
