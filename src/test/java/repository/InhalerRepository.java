package repository;

import models.database.MedicalDevice;
import models.database.PartnerKey;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InhalerRepository extends BaseDynamoRepository {

    private static final String inhalerTable = Utils.readProperty("inhalerTable");

    public MedicalDevice findInhalerBySerialNumber(String externalEntityID, Long serialNumber) {
        DynamoDbTable<MedicalDevice> deviceDynamoDbTable = enhancedClient.table(InhalerRepository.inhalerTable, TableSchema.fromBean(MedicalDevice.class));

        Key key = Key.builder()
                .partitionValue("serialNumber#" + serialNumber)
                .build();

        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(key);

        for (MedicalDevice medicalDevice : deviceDynamoDbTable.query(queryConditional).items()) {
            if (medicalDevice.getSkey().startsWith("externalEntityID#" + externalEntityID)) {
                return medicalDevice;
            }
        }

        return null;
    }

    public void updateInhaler(MedicalDevice medicalDevice) {
        DynamoDbTable<MedicalDevice> table = enhancedClient.table(InhalerRepository.inhalerTable, TableSchema.fromBean(MedicalDevice.class));
        table.putItem(medicalDevice);
    }
}
