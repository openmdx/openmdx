/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: NamingExceptions.java,v 1.3 2008/09/09 12:00:28 hburger Exp $
 * Description: Naming Exception Categorization
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/09 12:00:28 $
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
package org.openmdx.base.exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import javax.naming.NamingException;
import javax.resource.ResourceException;

import org.openmdx.base.resource.Records;
import org.openmdx.kernel.log.SysLog;

/**
 * Naming Exception Categorization
 */
public class NamingExceptions {

    /**
     * The names of the retriable remote exceptions
     */
    private static String[] retriableNamingExceptions; 

    /**
     * The names of the detail classes assoicated withthe retriable remote 
     * exceptions.
     */
    private static String[] retriableRootCauseThrowables; 

    /**
     * Tells whether an operation throwing a given 
     * <code>NamingException</code> is retriable
     * 
     * @param exception the <code>NamingException</code> to be tested
     * 
     * @return <code>true</code> if a remote call throwing the given exception 
     * may safely be retried.
     * 
     * @exception NullPointerException if exception is <code>null</code>
     */
    public static boolean isRetriable(
        NamingException exception
    ){
        for(
           int i = 0;
           i < NamingExceptions.retriableNamingExceptions.length;
           i++
        ) if(
            instanceOf(exception, NamingExceptions.retriableNamingExceptions[i]) &&
            instanceOf(exception.getRootCause(), NamingExceptions.retriableRootCauseThrowables[i])
        ) return true;
        return false;
    }
   
    /**
     * Tests whether a <code>Throwable</code> is an instance of a class
     * given by its name.
     * 
     * @param object
     * @param className 
     * 
     * @return <code>true</code> if the <code>Throwable</code> is an 
     * instance of the given class or if the classNamde is <code>null</code>
     */
    private static boolean instanceOf(
        Throwable object,
        String className
    ){
        if(className == null) return true;
        if(
            object != null
        ) for(
            Class<?> type = object.getClass();
            type != null;
            type = type.getSuperclass()
        ) if (
            className.equals(type.getName())
        ) return true;
        return false;
    }
    
    /**
     * The retrieable remote exceptions properties 
     */
    public static final String PROPERTIES = "org/openmdx/base/exception/retriable-naming-exceptions.properties";

    /**
     * The delimiter between <code>RemoteException</code> class and detail
     * <code>Throwable</code> class. 
     */
    public static final char DELIMITER = '*';

    /**
     * Load the properties
     * 
     * @return the properties; or <code>null</code>
     */
    private static Properties getProperties(){
        InputStream source = NamingExceptions.class.getClassLoader().getResourceAsStream(PROPERTIES);
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
                "Properties lacking, falling back to default entries " +
                "java.rmi.ConnectException and " + 
                "java.rmi.RemoteException*java.net.ConnectException"
            );
            retriableNamingExceptions = new String[]{
                "java.rmi.ConnectException",
                "java.rmi.RemoteException"
            };
            retriableRootCauseThrowables = new String[]{
                null,
                "java.net.ConnectException"
            };
        } else {
            int s = properties.size();
            retriableNamingExceptions = new String[s];
            retriableRootCauseThrowables = new String[s];
            int i = 0;
            for(
                Enumeration<?> e = properties.propertyNames();
                e.hasMoreElements();
                i++
            ){
                String n = (String) e.nextElement();
                int a = n.indexOf(DELIMITER);
                if(a < 0) {
                    retriableNamingExceptions[i] = n;
                    retriableRootCauseThrowables[i] = null;
                } else {
                    retriableNamingExceptions[i] = a > 0 ? n.substring(0, a) : null;
                    retriableRootCauseThrowables[i] = ++a < n.length() ? n.substring(a) : null;
                }
            }
        }
        try {
            SysLog.info(
                PROPERTIES,
                Records.getRecordFactory().asMappedRecord(
                    "RetriableNamingExceptions",
                    "namingExceptionClass" + DELIMITER + "rootCauseClass",
                    retriableNamingExceptions,
                    retriableRootCauseThrowables
                )
            );
        } catch (ResourceException logException) {
            SysLog.info("retriableNamingExceptions",logException);
        }
    }
    
}