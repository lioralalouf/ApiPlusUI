package repository;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilteredLogEvent;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class DataApiAuditCWRepository extends BaseCloudWatchRepository {

    private static final String logGroup = Utils.readProperty("dhpDataAuditLogs");


    public List<JSONObject> findLogsByTimeRange(Long startTime, Long endTime) {

        List<JSONObject> filteredLogEvents = new ArrayList<>();

        FilterLogEventsRequest filterLogEventsRequest = FilterLogEventsRequest.builder()
                .logGroupName(logGroup)
                .startTime(startTime)
                .endTime(endTime)
                .limit(500)
                .build();
        JSONParser parser = new JSONParser();

        int logLimit = cloudWatchLogsClient.filterLogEvents(filterLogEventsRequest).events().size();
        for (int c = 0; c < logLimit; c++) {
           FilteredLogEvent filteredLogEvent = cloudWatchLogsClient.filterLogEvents(filterLogEventsRequest).events().get(c);

            try {
                filteredLogEvents.add((JSONObject) parser.parse(filteredLogEvent.message()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return filteredLogEvents;
    }

}
