/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Database Driver Formatter
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.kernel.text.format;

import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.text.MultiLineStringRepresentation;

/**
 * Database Driver Formatter
 */
public class DatabaseDriverFormatter
        implements MultiLineStringRepresentation
{

    /**
     * 
     */
    private final Driver driver;

    /**
     * 
     */
    private final String url;

    /**
     * 
     */
    private final Properties info;

    /**
     * Constructor
     * 
     * @param driver
     * @param url
     * @param info
     */
    public DatabaseDriverFormatter(
        Driver driver,
        String url,
        Properties info
    ){
        this.driver = driver;
        this.url = url;
        this.info = info;
    }

    /**
     * Get the JDBC Driver's Property Info
     * 
     * @return the JDBC Driver's Property Info
     */
    private Object getPropertyInfo(
    ){
        try {
            DriverPropertyInfo[] source = this.driver.getPropertyInfo(
               this.url,
               this.info == null ? new Properties() : this.info
            );
            Object[] target = new Object[source.length];
            for(
                int i = 0;
                i< source.length;
                i++
            ) target[i] = source[i] == null ? null : ArraysExtension.asMap(
                DRIVER_PROPERTY_INFO_KEYS,
                new Object[]{
                    source[i].name,
                    source[i].description,
                    source[i].value,
                    Boolean.valueOf(source[i].required),
                    source[i].choices
                }
            );
            return target;
        } catch (SQLException e) {
                SysLog.error("JDBC Driver Property Info Acquisition failed", e);
                return "n/a";
        }
    }

    /**
     * Test whether the JDBC Driver accepts a given URL
     * 
     * @return "accepted" if the JDBC driver accepts the given URL,
     *         "rejected" otherwise.
     */
    private Object getURLAcceptance(
    ){
        try {
            return this.driver.acceptsURL(this.url) ?
                "accepted" :
                "rejected";
        } catch (SQLException e) {
                SysLog.error("JDBC Driver URL Aceptance Test failed", e);
                return "n/a";
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString(
    ){
        return this.driver.getClass().getName() + ": " + IndentingFormatter.toString(
            ArraysExtension.asMap(
                new String[]{
                    "version",
                    this.url,
                    "propertyInfo"
                },
                new Object[]{
                    Integer.toString(this.driver.getMinorVersion()) + "." + this.driver.getMajorVersion(),
                    getURLAcceptance(),
                    getPropertyInfo()
                }
             )
        );
    }

    private static final String[] DRIVER_PROPERTY_INFO_KEYS = {
        "name",
        "description",
        "value",
        "required",
        "choices"
    };

}
