/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: jdbcURLContextFactory.java,v 1.5 2008/10/13 09:53:34 hburger Exp $
 * Description: java URL Context Factory
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/10/13 09:53:34 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.naming.container.jdbc;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.ObjectFactory;

import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.application.container.spi.sql.DatabaseConnectionRequestInfo;
import org.openmdx.kernel.application.container.spi.sql.LightweightXADataSource;
import org.openmdx.kernel.application.container.spi.sql.ManagedDatabaseConnectionFactory;
import org.openmdx.kernel.naming.container.openmdx.openmdxURLContextFactory;
import org.openmdx.kernel.text.format.DatabaseDriverFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * java URL Context Factory
 */
public class jdbcURLContextFactory implements ObjectFactory {

    /**
     * Registers a driver and tests for the given URL
     * 
     * @param driverClass
     * @param connectionURL
     * 
     * @throws ClassNotFoundException
     *         if the Driver can't be loaded
     * @throws SQLException
     *         if no driver supports the given URL
     */
    public static void validateConnectionURL(
        String driverClass, 
        String connectionURL
    ) throws ClassNotFoundException, SQLException {
        if(driverClass != null) Classes.getKernelClass(driverClass);
        Driver driver = DriverManager.getDriver(connectionURL);
        logger.info(
            "Registered driver for given URL {}", 
            new DatabaseDriverFormatter(driver, connectionURL, null)
        );
    }

    
    /**
     * Return a database connection factory
     */
    public Object getObjectInstance(
       Object _object, 
       Name name, 
       Context nameCtx,
       Hashtable<?,?> environment
    ) throws NamingException {
        Object object = _object;
        if(object instanceof Object[]){
            Object[] urls = (Object[]) object;
            if(urls.length == 0) throw new NoInitialContextException("URL array is empty");
            object = urls[0]; // Just take the first of the equivalent URLs
        }
        if(object == null){
            return null;
        } else if(object instanceof String){
            String url = (String) object;
            if(url.startsWith(URL_PREFIX)) return new ManagedDatabaseConnectionFactory(
                new LightweightXADataSource(url, environment),
                new DatabaseConnectionRequestInfo(
                    null, // transactionIsolation
                    null, // validationStatement
                    null // loginTimeout
                )
            ); 
            throw new NoInitialContextException(
                URL_SCHEME + " URL scheme expected: " + url
            );            
        } else { 
            throw new NoInitialContextException(
                URL_SCHEME + " URL supports String object only: " + object.getClass().getName()
            );
        }
    }

    /**
     * To log database driver information
     */
    final static private Logger logger = LoggerFactory.getLogger(openmdxURLContextFactory.class);

    /**
     * The URL scheme supported by this factory
     */
    final static public String URL_SCHEME = "jdbc";
    
    /**
     * The URL prefix supported by this factory
     */
    final static public String URL_PREFIX = URL_SCHEME + ':';
    
}

