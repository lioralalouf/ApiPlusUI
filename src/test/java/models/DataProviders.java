package models;

import org.testng.annotations.DataProvider;
import utils.IpAddressUtils;


public class DataProviders extends IpAddressUtils {


	@DataProvider
	public static Object[][] getLimits() {
		return new Object[][]{{ -1 }, { 0 }, { -5 }};
	}
	
	@DataProvider
	public static Object[][] getPeriodFalse() {
		return new Object[][]{{ "days"}, { "weeks"}, { "months"}};
	}
	
	@DataProvider
	public static Object[][] getPeriodTrue() {
		return new Object[][]{{ "day"}, { "week"}, { "month"}};
	}
	
	@DataProvider
	public static Object[][] getScopeInvalid() {
		return new Object[][]{{ "data:api:partner:write" }, { "data-api-partner-read" }, { "lior:api:partner:read" }, { "data:lior:partner:read"}};
	}
	
	
	@DataProvider
	public static Object[][] getValidIPOctals() {
		IpAddressUtils ipa = new IpAddressUtils();
		String s1 = ipa.myIPOctal1()+".*";
		String s2 = ipa.myIPOctal2()+".*";
		String s3 = ipa.myIPOctal3()+".*";
		return new Object[][]{{ "*" }, { s1 }, { s2 }, { s3 }};
	}
	
	@DataProvider
	public static Object[][] getPeakFlowMaintenance () {
		return new Object[][]{{ 30 }, { 90 }, { 120 }};
	}
	
	@DataProvider
	public static Object[][] getPeakFlowMaintenanceInvalid () {
		return new Object[][]{{ 29 }, { 121 }};
	}
	
	@DataProvider
	public static Object[][] getPeakFlowEmergency () {
		return new Object[][]{{ 30 }, { 90 }};
	}
	
	@DataProvider
	public static Object[][] getPeakFlowEmergencyInvalid () {
		return new Object[][]{{ 29 }, { 91 }};
	}
	
	@DataProvider
	public static Object[][] getVolumePositive () {
		return new Object[][]{{ 3.0 }, { 6.0 }};
	}
	@DataProvider
	public static Object[][] getVolumeNegative () {
		return new Object[][]{{ 0.29 }, { 6.1 }};
	}
}
