/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Datums.java,v 1.1 2009/05/26 14:31:21 wfro Exp $
 * Description: Oracle SQL Datum Conversions
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/26 14:31:21 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.layer.persistence.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;

/**
 * Oracle SQL Datum Conversions
 */
public class Datums {

    protected Datums(
    ){
        // Avoid instantiation
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
        if(nativeClass.startsWith("oracle.sql.")) try {
            if(nativeClass.equals("oracle.sql.TIMESTAMPTZ")) {
                if(oracleToBytes == null) oracleToBytes = datum.getClass().getMethod(
                    "toBytes",
                    (Class[])null
                );
                try {
//                  [0] - 100 => century
//                  [1] - 100 => year in century
//                  [2] - 1   => zero based month
//                  [3]       => one based day
//                  [4] - 1   => hour
//                  [5] - 1   => minute
//                  [6] - 1   => second
//                  [7]..[10] => nanoseconds
//                  [11]/[12] => time zone
                    byte[] timestampWithTimezone =  (byte[]) oracleToBytes.invoke(
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
                } catch (InvocationTargetException exception) {
                    throw exception.getTargetException() instanceof Exception ?
                        (Exception) exception.getTargetException() :
                            exception;
                }
            } else {
                if(oracleToJdbc == null) oracleToJdbc = Classes.getApplicationClass(
                    "oracle.sql.Datum"
                ).getMethod(
                    "toJdbc",
                    (Class[])null
                );
                try {
                    return oracleToJdbc.invoke(datum, (Object[])null);
                } catch (InvocationTargetException exception) {
                    throw exception.getTargetException() instanceof Exception ?
                        (Exception) exception.getTargetException() :
                            exception;
                }
            }          
        } catch (Exception exception) {
            throw Throwables.initCause(
                new SQLException(
                    "Could not convert db object to JDBC object: " + 
                    Datums.class.getName() + "'s unification attempt failed"
                ),
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TRANSFORMATION_FAILURE,
                new BasicException.Parameter("class", datum.getClass().getName()),
                new BasicException.Parameter("value", datum.toString())
            );
        } else {
            return datum;
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
     * oracle.sql.Datum.toJdbc()
     */
    private static Method oracleToJdbc;

    /**
     * oracle.sql.TIMESTAMPTZ.toBytes()
     */
    private static Method oracleToBytes;

    /**
     * UTC time zone instance
     */
    protected final static TimeZone UTC = TimeZone.getTimeZone("UTC");

}
