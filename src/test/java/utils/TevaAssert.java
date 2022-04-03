package utils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.testng.Assert;


public class TevaAssert {

    public static boolean assertEquals(ExtentTest extentTest, Object actual, Object expected, String description) {

        try {
            Assert.assertEquals(actual, expected);
            extentTest.pass("Assertion: " + description + " [" + actual + " = " + expected + "]");
            System.out.println("Assertion: " + description + " [" + actual + " = " + expected + "]");
            return true;
        } catch (AssertionError e) {

            try {

                extentTest.fail("Assertion: " +description + " [" + actual + " != " + expected + "]");
                System.out.println("Assertion: " + description + " [" + actual + " != " + expected + "]");
                extentTest.log(Status.FAIL, "Test failed");
                throw e;
            } catch (Exception ex) {
                extentTest.fail("Assertion: " +description + " [Bad Condition]");
                System.out.println("Assertion: " + description + " [Bad Condition]");
                throw e;
            }
        }

    }

    public static boolean assertNotEquals(ExtentTest extentTest,  Object expected, Object actual,String description) {


        try {
            Assert.assertNotEquals(actual, expected);
            extentTest.pass("Assertion: " +description + " [" + actual + " != " + expected + "]");
            System.out.println("Assertion: " + description + " [" + actual + " != " + expected + "]");
            return true;
        } catch (AssertionError e) {

            try {
                extentTest.fail("Assertion: " +description + " [" + actual + " = " + expected + "]");
                System.out.println("Assertion: " + description + " [" + actual + " = " + expected + "]");
                throw e;
            } catch (Exception ex) {
                extentTest.fail("Assertion: " +description + " [Bad Condition]");
                System.out.println("Assertion: " + description + " [Bad Condition]");
                throw e;
            }
        }

    }

    public static boolean assertTrue(ExtentTest extentTest, boolean actual, String description) {


        try {
            Assert.assertTrue(actual);
            extentTest.pass("Assertion: " +description + " is true");
            System.out.println("Assertion: " + description + " is true");
            return true;
        } catch (AssertionError e) {
            extentTest.fail("Assertion: " +description + " [false]");
            System.out.println("Assertion: " + description + " [false]");
            extentTest.log(Status.FAIL, "Test failed");
            throw e;
        }
    }

    public static boolean assertFalse(ExtentTest extentTest, boolean actual, String description) {

        try {
            Assert.assertFalse(actual);
            extentTest.pass("Assertion: " +description + " is false");
            System.out.println("Assertion: " + description + " is false");
            return true;
        } catch (AssertionError e) {
            extentTest.fail("Assertion: " +description + " [true]");
            System.out.println("Assertion: " + description + " [true]");
            extentTest.log(Status.FAIL, "Test failed");
            throw e;
        }
    }

    public static boolean assertNull(ExtentTest extentTest, Object actual, String description) {


        try {
            Assert.assertNull(actual);
            extentTest.pass("Assertion: " +description + " is null");
            System.out.println("Assertion: " + description + " is null");
            return true;
        } catch (AssertionError e) {
            extentTest.fail("Assertion: " +description + " [Object has a value]");
            System.out.println("Assertion: " + description + " [Object has a value]");
            throw e;
        }
    }

    public static boolean assertNotNull(ExtentTest extentTest, Object actual, String description) {


        try {
            Assert.assertNotNull(actual);
            extentTest.pass("Assertion: " +description + "[" + actual + "] is not null");
            System.out.println("Assertion: " + description +  "[" + actual + "] is not null");
            return true;
        } catch (AssertionError e) {
            extentTest.fail("Assertion: " +description + " [Object is null]");
            System.out.println("Assertion: " + description + " [Object is null]");
            extentTest.log(Status.FAIL, "Test failed");
            throw e;
        }
    }

}
