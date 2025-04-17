package viettel.dac.backend.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


public class DateTimeUtils {

    private DateTimeUtils() {
        // Private constructor to prevent instantiation
    }

    public static String formatInstant(Instant instant, String pattern) {
        if (instant == null) {
            return null;
        }

        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return localDateTime.format(formatter);
    }

    public static String formatInstantIso(Instant instant) {
        return formatInstant(instant, "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    }

    public static Long durationMillis(Instant start, Instant end) {
        if (start == null || end == null) {
            return null;
        }

        return end.toEpochMilli() - start.toEpochMilli();
    }
}