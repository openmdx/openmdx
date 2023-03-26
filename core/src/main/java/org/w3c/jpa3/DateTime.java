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
     * Convert a {@code java.util.Date} to an {@code SQL TIMESTAMP}
     * 
     * @param cciDateTime the {@code java.util.Date} to be converted
     * 
     * @return a corresponding {@code SQL TIMESTAMP}
     */
    public static final java.sql.Timestamp toJDO (
        java.util.Date cciDateTime
    ){
        return cciDateTime == null ? null : new java.sql.Timestamp(cciDateTime.getTime());
    }

    /**
     * Convert an {@code SQL TIMESTAMP} to a {@code java.util.Date}
     * 
     * @param jdoDateTime the {@code SQL TIMESTAMP} to be converted
     * 
     * @return a corresponding {@code java.util.Date}
     */
    public static final java.util.Date toCCI (
        java.sql.Timestamp jdoDateTime
    ){
        return jdoDateTime == null ? null : new java.util.Date(jdoDateTime.getTime());
    }
    
}
