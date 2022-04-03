package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeMethod;
import requests.RestAssuredOAuth;

public class ApiBaseTest {
    protected String accessToken = "";
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeMethod
    public void setup() {
        accessToken = RestAssuredOAuth.getToken();
    }


}
