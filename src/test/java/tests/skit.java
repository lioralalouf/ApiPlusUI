package tests;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.request.PartnerRequest;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import requests.RestAssuredOAuth;
import annotations.Traceability;
import utils.Utils;

import java.io.IOException;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class skit {
	String accessToken = "";
	String partnerID = "";
	String usagePlanID = "";
	String apiKey = "";
	private final ObjectMapper objectMapper = new ObjectMapper();
	private static ExtentReports extent = ExtentManager.createInstance();


	@Test(testName = "CreatePartner")
	@Traceability(URS = "x.x.x", FS = "x.x.x")
	public void tc01_CreatePartner() throws IOException {
		ExtentTest extentTest = ExtentManager.getTest(this.getClass());
		this.accessToken = RestAssuredOAuth.getToken();
		
		extentTest.pass("Get new token key trough service ivr");

		PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
				PartnerRequest.class);
		partnerRequest.name = UUID.randomUUID().toString();
		partnerRequest.contact.email = "beforeUpdate@gmail.com";

		RestAssured.baseURI = Utils.readProperty("adminUrl");
		String response = 
				given() 
				.header("Authorization", "Bearer " + accessToken)
				.header("Content-Type", "application/json")
				.request()
				.body(partnerRequest)
				.when().log().all()
				.post("/configuration/partners")
				.then().log().all()
				.assertThat()
				.statusCode(200)
				.extract().response().asString();
		JsonPath js = new JsonPath(response);
		this.partnerID = js.getString("partnerID");
		extentTest.pass("Onboarding A new partner, Request is expected to have HTTP Response Code `200`");

		// get partner details after onboarding
		String response2 = 
				given()
				.baseUri(Utils.readProperty("adminUrl"))
				.basePath("configuration/partners")
				.header("Authorization", "Bearer " + accessToken)
				.pathParam("partnerID", partnerID)
				.when().log().all()
				.get("/{partnerID}")
				.then()
				.log().all()
				.assertThat().statusCode(200)
				.extract().response().asString();

		JsonPath js2 = new JsonPath(response2);
		String responseEmail = js2.getString("contact.email");
		Assert.assertEquals(responseEmail, "beforeUpdate@gmail.com", "original email has been updated succesfully");
		extentTest.pass("Get the onboarded partner details, Request is expected to have HTTP Response Code `200`");
	}
		@Test(testName = "UpdatePartner")
		@Traceability(URS = "x.x.x", FS = "x.x.x")
		public void tc02_UpdatePartner() throws IOException {
			ExtentTest extentTest = ExtentManager.getTest(this.getClass());
			PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
					PartnerRequest.class);
			partnerRequest.name = UUID.randomUUID().toString();
			partnerRequest.contact.email = "afterUpdate@gmail.com";
			extentTest.pass("Updated partner email = afterUpdate@gmail.com");
			
			String response3 = 
					given()
					.baseUri(Utils.readProperty("adminUrl"))
					.basePath("configuration/partners")
					.header("Authorization", "Bearer " + accessToken)
					.header("Content-Type", "application/json")
					.pathParam("partnerID", partnerID)
					.request()
					.body(partnerRequest)
					.when().log().all()
					.put("/{partnerID}")
					.then().log().all()
					.assertThat().statusCode(200)
					.extract().response().asString();
			extentTest.pass("Update the onboarded partner details, Request is expected to have HTTP Response Code `200`");
			
			// get partner details after onboarding
			Response response4 = 
					given()
					.baseUri(Utils.readProperty("adminUrl"))
					.basePath("configuration/partners")
					.header("Authorization", "Bearer " + accessToken)
					.pathParam("partnerID", partnerID)
					.when().log().all()
					.get("/{partnerID}")
					.then()
					.log().all()
					.extract().response();

			JsonPath extractor = response4.jsonPath();
			String responseEmail2 = extractor.get("contact.email").toString();
			Assert.assertEquals(responseEmail2, "afterUpdate@gmail.com", "new email has been updated succesfully");
			String ResponsePartnerID = extractor.get("partnerID").toString();
			Assert.assertEquals(ResponsePartnerID, partnerID, "Partner Id should be identical to the original");
			extentTest.pass("Get the onboarded partner details, Request is expected to have HTTP Response Code `200`");
		
			
			
		}
		
		

		@Test(testName = "DeletePartner")
		@Traceability(URS = "x.x.x", FS = "x.x.x")
		public void tc03_DeletePartnerr() throws IOException {
			ExtentTest extentTest = ExtentManager.getTest(this.getClass());
			Response response5 = given()
					.baseUri(Utils.readProperty("adminUrl"))
					.basePath("configuration/partners")
					.header("Content-Type", "application/json")
					.header("Authorization", "Bearer " + accessToken)
					.pathParam("partnerID", partnerID)
					.when()
					.delete("/{partnerID}")
					.then()
					.log().all()
					.extract().response();

			Assert.assertEquals(response5.getStatusCode(), 200, "Request is expected to have HTTP Response Code `200`");
			extentTest.pass("Delete the partner, Request is expected to have HTTP Response Code `200`");
			
			Response response6 = given()
					.baseUri(Utils.readProperty("adminUrl"))
					.basePath("configuration/partners")
					.header("Authorization", "Bearer " + accessToken)
					.pathParam("partnerID", partnerID)
					.when()
					.log()
					.all()
					.when().get("/{partnerID}").then()
					.log().all().
					extract().response();
			
			Assert.assertEquals(response6.getStatusCode(), 404, "Request is expected to have HTTP Response Code `404`");
			extentTest.pass("Get the deleted partner details, Request is expected to have HTTP Response Code `404`");
		}
	
	

}
