/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Oracle SQL Datum Conversions
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2012, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.dataprovider.layer.persistence.jdbc.oracle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.loading.Classes;
import org.w3c.spi.DatatypeFactories;

/**
 * Oracle SQL Datum Conversions
 */
public class Datums {

	/**
	 * Constructor
	 */
    private Datums(
    ){
        // Avoid instantiation
    }

    /**
     * Retrieve a class' package name
     * 
     * @param className the fully qualified class name
     * 
     * @return package name
     */
    private static String getPackageName(
        String className
    ){
        int separator = className.lastIndexOf('.');
        return separator < 0 ? "" : className.substring(0, separator);
    }
    
    /**
     * Tells whether the native object is an instance of oracle.sql.Datum
     * 
     * @return <code>true</code> the native object is an instance of oracle.sql.Datum
     */
    public static boolean isDatum(
        Object nativeObject
    ){
        if(nativeObject == null) {
            return false;
        }
        if(oracleDatum == null){
            if("oracle.sql".equals(getPackageName(nativeObject.getClass().getName()))) try {
                oracleDatum = Classes.getApplicationClass("oracle.sql.Datum");
            } catch (ClassNotFoundException exception) {
                return false; // can't be an instance of a non-existing class
            } else {
                return false; // the datum instances are expected to be in the oracle.sql package
            }
        }
        return oracleDatum.isInstance(nativeObject);
    }
    
    /**
     * Convert Oracle Datum values to JDBC objects
     * 
     * @param datum an Oracle Datum
     * 
     * @return the corresponding JDBC object
     * 
     * @throws SQLException if the conversion fails
     * @throws NullPointerException if nativeObject is <code>null</code>.
     */
    public static Object toJdbcObject(
        Object datum
    ) throws SQLException{
        String nativeClass = datum.getClass().getName();
        try {
            if(nativeClass.equals("oracle.sql.TIMESTAMPTZ")) {
                if(timestampToBytes == null) timestampToBytes = datum.getClass().getMethod("toBytes");
                //                                  
                // [0] - 100 => century
                // [1] - 100 => year in century
                // [2] - 1   => zero based month
                // [3]       => one based day
                // [4] - 1   => hour
                // [5] - 1   => minute
                // [6] - 1   => second
                // [7]..[10] => nanoseconds
                // [11]/[12] => time zone
                //
                byte[] timestampWithTimezone =  (byte[]) timestampToBytes.invoke(
                    datum, 
                    (Object[])null
                );
                byte[] nanos = new byte[4];
                System.arraycopy(timestampWithTimezone, 7, nanos, 0, 4);
                Calendar calendar = Calendar.getInstance(UTC);
                calendar.clear();
                calendar.set(
                    100 * (toInt(timestampWithTimezone[0]) - 100) + (toInt(timestampWithTimezone[1]) - 100), // year
                    toInt(timestampWithTimezone[2]) - 1, // zero based month
                    timestampWithTimezone[3], // one based day
                    timestampWithTimezone[4] - 1, // hour
                    timestampWithTimezone[5] - 1, // minute
                    timestampWithTimezone[6] - 1 // second
                );
                Timestamp timestamp = new Timestamp(
                    calendar.getTimeInMillis()
                );
                timestamp.setNanos(
                    new BigInteger(nanos).intValue()
                );    
                return timestamp;
            } else if (nativeClass.equals("oracle.sql.INTERVALDS")) {
                if(intervalToBytes == null) intervalToBytes = datum.getClass().getMethod("toBytes");
                //
                // [0]..[3] - 0x80000000 => days
                // [4] - 60  => hours
                // [5] - 60  => minutes
                // [6] - 60  => seconds
                // [7]..[10] - 0x80000000 => nanoseconds
                //
                byte[] intervalDaysSeconds =  (byte[]) intervalToBytes.invoke(
                    datum, 
                    (Object[])null
                );
            	StringBuilder value = new StringBuilder();
                int days = toInt(intervalDaysSeconds, 0) - 0x80000000;
                int hours = toInt(intervalDaysSeconds[4]) - 60;
                int minutes = toInt(intervalDaysSeconds[5]) - 60;
                int seconds = toInt(intervalDaysSeconds[6]) - 60;
                int nanoseconds = toInt(intervalDaysSeconds, 7) - 0x80000000;
                if(days < 0) {
                	value.append(
                		"-P"
                	).append(
                		-days
                	).append(
                		"DT"
                	).append(
                		-hours
                	).append(
                		"H"
                	).append(
                		-minutes
                	).append(
                		"M"
                	).append(
                		BigDecimal.valueOf(
                			-seconds
                		).add(
		                	BigDecimal.valueOf(-nanoseconds, 9)
		                ).toPlainString()
	            	).append(
	            		"S"
	            	);
                } else {
                	value.append(
                		"P"
                	).append(
                		days
                	).append(
                		"DT"
                	).append(
                		hours
                	).append(
                		"H"
                	).append(
                		minutes
                	).append(
                		"M"
                	).append(
                		BigDecimal.valueOf(
                			seconds
                		).add(
		                	BigDecimal.valueOf(nanoseconds, 9)
		                ).toPlainString()
	            	).append(
	            		"S"
	            	);
                }
                return DatatypeFactories.xmlDatatypeFactory().newDurationDayTime(value.toString());
            } else if (isDatum(datum)) { 
                if(datumToJdbc == null) datumToJdbc = oracleDatum.getMethod("toJdbc");
                return datumToJdbc.invoke(datum);
            } else {
                return datum; // Don't touch foreign values
            }
        } catch (Exception exception) {
            throw Throwables.initCause(
                new SQLException(
                    "Could not convert db object to JDBC object: " + 
                    Datums.class.getName() + "'s unification attempt failed"
                ),
                exception instanceof InvocationTargetException ? 
                    ((InvocationTargetException)exception).getTargetException() : 
                    exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TRANSFORMATION_FAILURE,
                new BasicException.Parameter("class", nativeClass),
                new BasicException.Parameter("value", datum.toString())
            );
        }
    }

    /**
     * Convert a byte to an integer
     * 
     * @param b a byte
     * 
     * @return the corresponding integer value
     */
    private static int toInt(byte b) {
        return 0xFF & b;
    }

    /**
     * Convert four bytes to an integer
     * 
     * @param source
     * 
     * @return
     */
    private static int toInt(
    	byte[] source,
    	int offset
    ){
    	byte[] value = new byte[4];
    	System.arraycopy(source, offset, value, 0, 4);
    	return new BigInteger(value).intValue();
    }

    /**
     * oracle.sql.Datum
     */
    private static Class<Object> oracleDatum;
    
    /**
     * oracle.sql.Datum.toJdbc()
     */
    private static Method datumToJdbc;

    /**
     * oracle.sql.TIMESTAMPTZ.toBytes()
     */
    private static Method timestampToBytes;

    /**
     * oracle.sql.INTERVALDS.toBytes()
     */
    private static Method intervalToBytes;
    
    /**
     * UTC time zone instance
     */
    protected final static TimeZone UTC = TimeZone.getTimeZone("UTC");

}
