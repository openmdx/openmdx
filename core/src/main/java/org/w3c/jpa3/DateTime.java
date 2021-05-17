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
     * Convert a <code>java.util.Date</code> to an <code>SQL TIMESTAMP</code>
     * 
     * @param cciDateTime the <code>java.util.Date</code> to be converted
     * 
     * @return a corresponding <code>SQL TIMESTAMP</code>
     */
    public static final java.sql.Timestamp toJDO (
        java.util.Date cciDateTime
    ){
        return cciDateTime == null ? null : new java.sql.Timestamp(cciDateTime.getTime());
    }

    /**
     * Convert an <code>SQL TIMESTAMP</code> to a <code>java.util.Date</code>
     * 
     * @param jdoDateTime the <code>SQL TIMESTAMP</code to be converted
     * 
     * @return a corresponding <code>java.util.Date</code>
     */
    public static final java.util.Date toCCI (
        java.sql.Timestamp jdoDateTime
    ){
        return jdoDateTime == null ? null : new java.util.Date(jdoDateTime.getTime());
    }
    
}
