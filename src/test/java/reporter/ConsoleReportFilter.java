package reporter;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import io.restassured.filter.FilterContext;
import io.restassured.filter.OrderedFilter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class ConsoleReportFilter implements OrderedFilter {

    private ExtentTest extentTest;

    @Override
    public int getOrder() {
        return 0;
    }

    public ConsoleReportFilter(ExtentTest extentTest) {
        this.extentTest = extentTest;
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {

        String method = requestSpec.getMethod();
        String uri = requestSpec.getURI();

        final Response response = ctx.next(requestSpec, responseSpec);
        int statusCode = response.getStatusCode();
        String responseBody = response.getBody().asPrettyString();
        
        extentTest.info("API Request (" + method + ") " + uri);

        if (requestSpec.getBody() != null) {
            extentTest.info("API Request Header");
            extentTest.info(MarkupHelper.createCodeBlock(requestSpec.getHeaders().toString()));
            extentTest.info("API Request Body: ");
            extentTest.info(MarkupHelper.createCodeBlock(requestSpec.getBody(), CodeLanguage.JSON));
            extentTest.info("API Response Body Status Code: " + statusCode);
            extentTest.info(MarkupHelper.createCodeBlock(responseBody, CodeLanguage.JSON));
        } else {
            extentTest.info("API Request Header");
            extentTest.info(MarkupHelper.createCodeBlock(requestSpec.getHeaders().toString()));
            extentTest.info("API Response Body Status Code: " + statusCode);
            extentTest.info(MarkupHelper.createCodeBlock(responseBody, CodeLanguage.JSON));
        }

        return response;
    }
}
