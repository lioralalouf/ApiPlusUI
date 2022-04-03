package repository;

import models.database.PartnerKey;
import models.database.PartnerUserConnection;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PartnerUserConnectionRepository extends BaseDynamoRepository {


    private static final String partnerUserConnectionTable = Utils.readProperty("partnerUserConnectionTable");

    public PartnerUserConnection findConsentByPatientPartner(String externalEntityID, String partnerID) {

        DynamoDbTable<PartnerUserConnection> mappedTable = enhancedClient.table(PartnerUserConnectionRepository.partnerUserConnectionTable, TableSchema.fromBean(PartnerUserConnection.class));

        Key key = Key.builder()
                .partitionValue(externalEntityID)
                .sortValue("consent#dataTransferConsent#" + partnerID)
                .build();

        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(key);

        Iterator<PartnerUserConnection> results = mappedTable.query(queryConditional).items().iterator();

        List<PartnerUserConnection> partnerUserConnections = new ArrayList<>();

        while (results.hasNext()) {
            PartnerUserConnection partnerUserConnection = results.next();
            return partnerUserConnection;
        }

        return null;
    }

    public void updatePatientPartnerConsent(PartnerUserConnection partnerUserConnection) {
        DynamoDbTable<PartnerUserConnection> table = enhancedClient.table(PartnerUserConnectionRepository.partnerUserConnectionTable, TableSchema.fromBean(PartnerUserConnection.class));
        table.putItem(partnerUserConnection);
    }
}
