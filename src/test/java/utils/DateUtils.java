package utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

    public static long utcTimeMinutesAgo(int minutes) {
        LocalDateTime now = LocalDateTime.now();
        return now.minus(minutes, ChronoUnit.MINUTES).toInstant(ZoneOffset.UTC).getEpochSecond();
    }

    public static long timeMinutesAgo(int minutes) {
        LocalDateTime now = LocalDateTime.now();
        ZoneId zoneId = ZoneId.systemDefault();

        LocalDateTime localDateTime = now.minus(minutes, ChronoUnit.MINUTES);

        return localDateTime.atZone(zoneId).toEpochSecond();
    }

    public static int utcDay() {
        LocalDateTime now = LocalDateTime.now();
        return now.toInstant(ZoneOffset.UTC).get(ChronoField.DAY_OF_WEEK);
    }

    public static String getISO8601() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date()).replace( " " , "T") + "+00:00";
    }

    public static String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    public static String extractDate(Date birthday) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(birthday);
    }
}
