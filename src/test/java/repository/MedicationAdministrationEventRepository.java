package repository;

import models.database.MedicationAdministrationEvent;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import utils.Utils;

public class MedicationAdministrationEventRepository extends BaseDynamoRepository {

    private static final String medicationEventsTable = Utils.readProperty("medicationEventsTable");

    public boolean removeByID(String id) {

        DynamoDbTable<MedicationAdministrationEvent> mappedTable = enhancedClient.table(MedicationAdministrationEventRepository.medicationEventsTable, TableSchema.fromBean(MedicationAdministrationEvent.class));

        Key key = Key.builder()
                .partitionValue(id)
                .build();

        mappedTable.deleteItem(key);

        return true;
    }


}
