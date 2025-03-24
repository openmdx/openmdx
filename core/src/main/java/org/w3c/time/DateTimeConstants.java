package org.w3c.time;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateTimeConstants {

    public static final DateTimeFormatter DT_WITH_UTC_TZ_BASIC_PATTERN = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSS'Z'").withZone(ZoneOffset.UTC);

    public static final DateTimeFormatter DT_WITH_UTC_TZ_EXT_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

}
