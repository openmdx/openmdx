/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Abstract Initial Context Factory
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2012, OMEX AG, Switzerland
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
package org.openmdx.kernel.lightweight.naming;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.InitialContextFactory;

import org.openmdx.kernel.lightweight.naming.java.javaURLContextFactory;
import org.openmdx.kernel.log.SysLog;

/**
 * Abstract Initial Context Factory
 */
public abstract class AbstractInitialContextFactory implements InitialContextFactory {

    /**
     * Prepare a lightweight initial context
     * 
     * @throws NoInitialContextException 
     */
    protected static Context newInitialContext(
        String initialContextFactory,
        Object transactionManager,
        Object transactionSynchronizationRegistry,
        Object userTransaction
    ) throws NoInitialContextException {
        try {
            //
            // Component Context Set-Up
            //
            Map<String,Object> transactionEnvironment = new HashMap<String,Object>();
            transactionEnvironment.put(
                "org.openmdx.comp.TransactionManager",
                transactionManager
            );
            transactionEnvironment.put(
                "org.openmdx.comp.TransactionSynchronizationRegistry",
                transactionSynchronizationRegistry
            );
            transactionEnvironment.put(
                "org.openmdx.comp.UserTransaction",
                userTransaction
            );
            javaURLContextFactory.populate(transactionEnvironment);
            javaURLContextFactory.populate(System.getProperties());
            //
            // JDNI set-up
            //
            Map<String,String> initialContextEnvironment = new HashMap<String,String>();
            initialContextEnvironment.put(
                Context.INITIAL_CONTEXT_FACTORY,
                initialContextFactory
            );
            initialContextEnvironment.put(
                Context.URL_PKG_PREFIXES,
                "org.openmdx.kernel.lightweight.naming"
            );
            return new NonManagedInitialContext(initialContextEnvironment);
        } catch (NamingException exception) {
            throw (NoInitialContextException) new NoInitialContextException(
                "Could not populate the non-managed environment's comp context"
            ).initCause(
                exception
            );
        }
    }

    /**
     * Assert that a specific value is among the values in a system property's 
     * value list.
     * 
     * @param name the system property's name
     * @param value the required value
     * @param separator the value separator
     */
    private static void prependSystemPropertyValue (
        String name,
        String value,
        char separator
    ){
        String values = System.getProperty(name);
        if(values == null || values.length() == 0){
            SysLog.log(
                Level.INFO,
                "Set system property {0} to \"{1}\"",
                name,value
            );
            System.setProperty(
                name, 
                value
            );
        } else if ((separator + values + separator).indexOf(separator + value + separator) < 0) {
            String newValue = value + separator + values; 
            SysLog.log(
                Level.INFO,
                "Change system property {0} from \"{1}\" to \"{2}\"",
                name, values, newValue
            );
            System.setProperty(
                name,
                newValue
            );
        }
    }

    static {
        //
        // URL set-up
        //
        prependSystemPropertyValue(
            "java.protocol.handler.pkgs",
            "org.openmdx.kernel.url.protocol",
            '|'
        );
    }

}
