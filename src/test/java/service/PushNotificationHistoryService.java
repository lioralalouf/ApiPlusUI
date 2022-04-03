package service;

import models.database.PushNotificationHistory;
import repository.PushNotificationHistoryRepository;
import utils.DateUtils;

import java.util.UUID;

public class PushNotificationHistoryService {

    private PushNotificationHistoryRepository pushNotificationHistoryRepository = new PushNotificationHistoryRepository();

    public PushNotificationHistory find(String externalEntityID, String notificationType) {

        return pushNotificationHistoryRepository.findNotificationByExternalEntityID(externalEntityID,notificationType);
    }

    public void createNotificationRecord(String externalEntityID, String notificationType, int count, Long timestamp) {
        PushNotificationHistory pushNotificationHistory = new PushNotificationHistory();


        pushNotificationHistory.setCount(count);

       pushNotificationHistory.setData(DateUtils.getDate());

       String iso8601 = DateUtils.getISO8601();


       pushNotificationHistory.setRowCreated(iso8601);
       pushNotificationHistory.setRowModified(iso8601);
        pushNotificationHistory.setExternalEntityID(externalEntityID);
        pushNotificationHistory.setNotificationType(notificationType);
        pushNotificationHistory.setObjectName("push_notification_history");
        pushNotificationHistory.setpkey(externalEntityID);
        pushNotificationHistory.setRowIdentifier(UUID.randomUUID().toString());
        pushNotificationHistory.setSkey(notificationType);
        pushNotificationHistory.setTimestamp(timestamp);

        pushNotificationHistoryRepository.persist(pushNotificationHistory);
    }

    public void updateCount(String externalEntityID, String notificationType, int newCount) {
        PushNotificationHistory pushNotificationHistory = pushNotificationHistoryRepository.findNotificationByExternalEntityID(externalEntityID,notificationType);
        pushNotificationHistory.setCount(newCount);
        pushNotificationHistoryRepository.persist(pushNotificationHistory);
    }

    public void updateTimestamp(String externalEntityID, String notificationType, Long timestamp) {
        PushNotificationHistory pushNotificationHistory = pushNotificationHistoryRepository.findNotificationByExternalEntityID(externalEntityID,notificationType);
        pushNotificationHistory.setTimestamp(timestamp);
        pushNotificationHistoryRepository.persist(pushNotificationHistory);
    }
}
