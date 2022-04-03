package tools;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.ApiGatewayException;
import software.amazon.awssdk.services.apigateway.model.ApiKey;
import software.amazon.awssdk.services.apigateway.model.DeleteRestApiRequest;
import software.amazon.awssdk.services.apigateway.model.GetApiKeysResponse;

import java.util.List;

public class RemoveUsagePlans {

    public static void main(String[] args) {

        Region region = Region.US_EAST_1;
        ApiGatewayClient apiGateway = ApiGatewayClient.builder()
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create() )
                .region(region)
                .build();

        getKeys(apiGateway);
        apiGateway.close();

    }

    public static void getKeys(ApiGatewayClient apiGateway) {

        try {

            GetApiKeysResponse response = apiGateway.getApiKeys();
            List<ApiKey> keys = response.items();

            System.out.println(keys.size());

            for (ApiKey key: keys) {

                if (key.name().contains("Canary Medical") ||
                        key.name().contains("AppSec 1") ||
                        key.name().contains("AppSec 2")
                ) {
                    continue;
                } else {
                    System.out.println("key name is: "+key.name() + " " + key.id());
                }


                try {
                    DeleteRestApiRequest request = DeleteRestApiRequest.builder()
                            .restApiId(key.id())
                            .build();

                    apiGateway.deleteRestApi(request);
                    System.out.println("The API was successfully deleted");

                } catch (ApiGatewayException e) {
                    System.err.println(e.awsErrorDetails().errorMessage());
                    System.exit(1);
                }

                Thread.sleep(250);
            }

        } catch (ApiGatewayException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
