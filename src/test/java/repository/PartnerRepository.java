package repository;

import models.database.PartnerKey;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class PartnerRepository extends BaseDynamoRepository {


    private static final String partnerTableName = Utils.readProperty("partnerTable");

    public List<PartnerKey> findApiKeyByPartnerID(String partnerID) {
        DynamoDbTable<PartnerKey> mappedTable = enhancedClient.table(PartnerRepository.partnerTableName, TableSchema.fromBean(PartnerKey.class));

        Key key = Key.builder()
                .partitionValue(partnerID)
                .build();

        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(key);

        Iterator<PartnerKey> results = mappedTable.query(queryConditional).items().iterator();

        List<PartnerKey> partnerKeyList = new ArrayList<>();

        while (results.hasNext()) {
            PartnerKey partnerKey = results.next();

            if (partnerKey.getSkey().startsWith("key")) {
                partnerKeyList.add(partnerKey);
            }
        }

        return partnerKeyList;
    }

    public void persistPartnerKey(PartnerKey partnerKey) {
        DynamoDbTable<PartnerKey> table = enhancedClient.table(PartnerRepository.partnerTableName, TableSchema.fromBean(PartnerKey.class));
        table.putItem(partnerKey);
    }
}
