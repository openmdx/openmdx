/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: NamingPreferences.java,v 1.3 2008/03/21 18:29:18 hburger Exp $
 * Description: Naming Preferences 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:29:18 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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

package org.openmdx.base.configuration.spi;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;

import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.kernel.log.SysLog;

/**
 * Naming Preferences
 * <p>
 * Note:<br>
 * The current implementation<ul>
 * <li>is designed for <code>java:comp/env</code> entries only
 * <li><em>forgets</em> modification operations which can't be proagated to the backing store
 * </ul>
 */
@SuppressWarnings("unchecked")
class NamingPreferences
    extends AbstractPreferences
{

    /**
     * Constructor 
     */
    NamingPreferences(
    ) {
        super(null, "");
    }

    /**
     * Constructor 
     *
     * @param parent
     * @param name
     */
    private NamingPreferences(
        NamingPreferences parent, 
        String name
    ) {
        super(parent, name);
    }
    
    /**
     * The corrresponding JNDI name
     */
    private final String name = "java:comp/env" + this.absolutePath();

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#flushSpi()
     */
    protected void flushSpi(
    ) throws BackingStoreException {
        //
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#removeNodeSpi()
     */
    protected void removeNodeSpi(
    ) throws BackingStoreException {
        try {
            Context context = new InitialContext();
            try {
                context.destroySubcontext(this.name);
            } finally {
                context.close();
            }
        } catch (NamingException exception) {
            throw new BackingStoreException(exception);
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#syncSpi()
     */
    protected void syncSpi(
    ) throws BackingStoreException {
        //
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#childrenNamesSpi()
     */
    protected String[] childrenNamesSpi(
    ) throws BackingStoreException {
        return bindings(true);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#keysSpi()
     */
    protected String[] keysSpi(
    ) throws BackingStoreException {
        return bindings(false);
    }
    
    protected String[] bindings (
        boolean children
    ) throws BackingStoreException{
        try {
            Context context = new InitialContext();
            try {
                List names = new ArrayList();
                for(
                    Enumeration e = context.listBindings(this.name);
                    e.hasMoreElements();
                ){
                    Binding binding = (Binding) e.nextElement();
                    if(children == binding.getObject() instanceof Context) {
                        String name = binding.getName();
                        names.add(
                            SharedConfigurationEntries.WORK_AROUND_SUN_APPLICATION_SERVER_BINDINGS || 
                            !binding.isRelative() ? name.substring(name.lastIndexOf('/') + 1) : name
                        );
                    }                    
                }
                return (String[]) names.toArray(new String[names.size()]);
            } finally {
                context.close();
            }
        } catch (NamingException exception) {
            throw new BackingStoreException(exception);
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#removeSpi(java.lang.String)
     */
    protected void removeSpi(String key) {
        try {
            Context context = new InitialContext();
            String name = this.name + "/" + key;
            try {
                context.unbind(name);
            } finally {
                context.close();
            }
        } catch (NamingException exception) {
            SysLog.warning("Unable to remove " + key + " from " + this, exception);
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#getSpi(java.lang.String)
     */
    protected String getSpi(String key) {
        try {
            Context context = new InitialContext();
            String name = this.name + "/" + key;
            try {
                return context.lookup(name).toString();
            } finally {
                context.close();
            }
        } catch (NamingException exception) {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#putSpi(java.lang.String, java.lang.String)
     */
    protected void putSpi(String key, String value) {
        try {
            Context context = new InitialContext();
            String name = this.name + "/" + key;
            try {
                context.bind(name, value);
            } catch (NameAlreadyBoundException exception) {
                context.rebind(name, value);
            } finally {
                context.close();
            }
        } catch (NamingException exception) {
            SysLog.warning("Unable to put " + key + " to " + this, exception);
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.AbstractPreferences#childSpi(java.lang.String)
     */
    protected AbstractPreferences childSpi(
        String name
    ) {
        return new NamingPreferences(this, name);
    }

}
