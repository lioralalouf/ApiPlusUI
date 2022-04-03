package repository;

import models.database.ProfileAccess;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import utils.Utils;

public class ProfileAccessRepository extends BaseDynamoRepository {

    private static final String profileAccessTable = Utils.readProperty("profileAccessTable");

    public void updateLastAccessByExternalEntityID(ProfileAccess profileAccessModel) {

    }

    public Object getLastAccessByExternalEntityID(String externalEntityID) {

        return null;
    }

    public ProfileAccess findByExternalEntityID(String externalEntityID) {

        DynamoDbTable<ProfileAccess> mappedTable = enhancedClient.table(ProfileAccessRepository.profileAccessTable, TableSchema.fromBean(ProfileAccess.class));

        Key key = Key.builder()
                .partitionValue(externalEntityID)
                .build();

        return mappedTable.getItem(key);

    }
}
