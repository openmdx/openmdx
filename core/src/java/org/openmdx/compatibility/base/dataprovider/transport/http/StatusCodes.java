/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: StatusCodes.java,v 1.1 2005/09/16 00:44:42 hburger Exp $
 * Description: Remote Exception Categorization
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/09/16 00:44:42 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.transport.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import javax.resource.ResourceException;

import org.openmdx.base.resource.spi.OrderedRecordFactory;
import org.openmdx.kernel.log.SysLog;

/**
 * Remote Exception Categorization
 */
public class StatusCodes {

    /**
     * The retriable status codes
     */
    private static int[] retriableStatusCodes; 

    /**
     * Tells whether an operation returning a given 
     * <code>status</code> code is retriable
     * 
     * @param statusCode the status code to be tested
     * 
     * @return <code>true</code> if a HTTP request yielding the given
     * status code may safely be retried.
     */
    public static boolean isRetriable(
        int statusCode
    ){
        for(
           int i = 0;
           i < StatusCodes.retriableStatusCodes.length;
           i++
        ) if (
            statusCode == StatusCodes.retriableStatusCodes[i]
        ) return true;
        return false;
    }
   
    /**
     * The retrieable remote exceptions properties 
     */
    public static final String PROPERTIES = "org/openmdx/compatibility/base/dataprovider/transport/http/retriable-status-codes.properties";

    /**
     * Load the properties
     * 
     * @return the properties; or <code>null</code>
     */
    private static Properties getProperties(){
        InputStream source = StatusCodes.class.getClassLoader().getResourceAsStream(PROPERTIES);
        if(source == null) {
            SysLog.error(
                "Could not find properties " + PROPERTIES
            );
            return null;
        } else try {
            Properties properties = new Properties();
            properties.load(source);
            return properties;
        } catch (IOException exception) {
            SysLog.error("Could not read properties " + PROPERTIES, exception);
            return null;
        }
    }

    static {
        Properties properties = getProperties();
        if(properties == null) {
            SysLog.error(
                "Properties lacking, falling back to the default an empty set of status codes"
            );
            retriableStatusCodes = new int[]{};
        } else {
            int s = properties.size();
            retriableStatusCodes = new int[s];
            int i = 0;
            String n = null;
            for(
                Enumeration e = properties.propertyNames();
                e.hasMoreElements();
                i++
            ) try {  
                n = ((String) e.nextElement()).trim();
                retriableStatusCodes[i] = Integer.parseInt(n);
            } catch (NumberFormatException exception) {
                retriableStatusCodes[i] = -1;
                SysLog.error(
                    PROPERTIES,
                    "Entry '" + n + "' can't be parsed as HTTP status code"
                );
            }
        }
        try {
            SysLog.info(
                PROPERTIES,
                OrderedRecordFactory.getInstance().asIndexedRecord(
                    "retriableStatusCodes",
                    null,
                    retriableStatusCodes
                )
            );
        } catch (ResourceException logException) {
            SysLog.info("retriableStatusCodes", logException);
        }
    }
    
}