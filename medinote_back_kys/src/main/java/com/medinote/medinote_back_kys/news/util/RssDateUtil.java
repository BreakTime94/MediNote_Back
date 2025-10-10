package com.medinote.medinote_back_kys.news.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class RssDateUtil {

    private static final DateTimeFormatter RSS_FORMAT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    public static LocalDateTime parse(String rssDate) {
        try {
            return ZonedDateTime.parse(rssDate, RSS_FORMAT)
                    .withZoneSameInstant(ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now(); // fallback
        }
    }

}
