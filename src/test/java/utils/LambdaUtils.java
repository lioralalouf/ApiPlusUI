package utils;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.*;
import software.amazon.awssdk.services.lambda.model.*;

import java.util.HashMap;
import java.util.Map;

public class LambdaUtils {

    public static Map<String,String> getLambdaConfiguration(String functionName) {
        Region region = Region.US_EAST_1;
        LambdaClient awsLambda = LambdaClient.builder()
                .region(region)
                .build();

        GetFunctionConfigurationRequest configurationRequest = GetFunctionConfigurationRequest.builder()
                .functionName(functionName)
                .build();

        GetFunctionConfigurationResponse getFunctionConfigurationResponse = awsLambda.getFunctionConfiguration(configurationRequest);
        awsLambda.close();

        Map<String,String> environmentMap = new HashMap<>();
        environmentMap.putAll( getFunctionConfigurationResponse.environment().variables());

        return environmentMap;
    }

    public static void updateLambdaConfiguration(String functionName, Map<String, String> environmentMap) {
        Region region = Region.US_EAST_1;
        LambdaClient awsLambda = LambdaClient.builder()
                .region(region)
                .build();

        Environment environment = Environment.builder()
                .variables(environmentMap)
                .build();

        UpdateFunctionConfigurationRequest updateFunctionConfigurationRequest = UpdateFunctionConfigurationRequest.builder()
                .functionName(functionName)
                .environment(environment)
                .build();

        awsLambda.updateFunctionConfiguration(updateFunctionConfigurationRequest);

        awsLambda.close();
    }
}
