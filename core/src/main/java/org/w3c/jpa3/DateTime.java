package org.w3c.jpa3;


/**
 * DateTime
 */
public class DateTime {

    /**
     * Avoid instantiation 
     */
    protected DateTime() {
    }

    /**
     * Convert an org::w3c:.dateTime value to an {@code SQL TIMESTAMP}
     * 
     * @param cciDateTime the org::w3c:.dateTime value to be converted
     * 
     * @return the corresponding {@code SQL TIMESTAMP}
     */
    public static final java.sql.Timestamp toJDO (java.util.Date cciDateTime) {
        return cciDateTime == null ? null : new java.sql.Timestamp(cciDateTime.getTime());
    }

    /**
     * Convert an org::w3c:.dateTime value to an {@code SQL TIMESTAMP}
     *
     * @param cciDateTime the org::w3c:.dateTime value to be converted
     *
     * @return the corresponding {@code SQL TIMESTAMP}
     */
    public static final java.sql.Timestamp toJDO (java.time.Instant cciDateTime) {
        return cciDateTime == null ? null : java.sql.Timestamp.from(cciDateTime);
    }

    /**
     * Convert an {@code SQL TIMESTAMP} to org::w3c::dateTime value
     * 
     * @param jdoDateTime the {@code SQL TIMESTAMP} to be converted
     * 
     * @return the corresponding org::w3c::dateTime value
     */
    public static final #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif toCCI (
        java.sql.Timestamp jdoDateTime
    ){
        return jdoDateTime == null ? null : #if CLASSIC_CHRONO_TYPES new java.util.Date(jdoDateTime.getTime()); #else jdoDateTime.toInstant(); #endif
    }
    
}
