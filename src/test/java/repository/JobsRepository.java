package repository;

import models.database.Job;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import utils.Utils;

import java.util.*;

public class JobsRepository extends BaseDynamoRepository {

    private static final String jobTable = Utils.readProperty("jobsTable");

    public List<Job> findJobsByFunction(String functionName) {
        List<Job> jobs = new ArrayList<>();
        DynamoDbTable<Job> mappedTable = enhancedClient.table(JobsRepository.jobTable, TableSchema.fromBean(Job.class));

        AttributeValue attVal = AttributeValue.builder()
                .s(functionName)
                .build();

        // Get only items in the Issues table for 2013-11-15.
        Map<String, AttributeValue> myMap = new HashMap<>();
        myMap.put(":functionName", attVal);

        Map<String, String> myExMap = new HashMap<>();
        myExMap.put("#functionName", "functionName");

        Expression expression = Expression.builder()
                .expressionValues(myMap)
                .expressionNames(myExMap)
                .expression("contains(#functionName,:functionName)")
                .build();

        ScanEnhancedRequest enhancedRequest = ScanEnhancedRequest.builder()
                .filterExpression(expression)
                .limit(15)
                .build();

        // Get items in the Issues table.
        Iterator<Job> results = mappedTable.scan(enhancedRequest).items().iterator();

        while (results.hasNext()) {
            Job job = results.next();
            jobs.add(job);
        }

        return jobs;
    }


    public boolean removeByFunction(String functionName) {
        DynamoDbTable<Job> mappedTable = enhancedClient.table(JobsRepository.jobTable, TableSchema.fromBean(Job.class));

        Key key = Key.builder()
                .partitionValue(functionName)
                .build();


        mappedTable.deleteItem(key);

        return true;
    }
}
