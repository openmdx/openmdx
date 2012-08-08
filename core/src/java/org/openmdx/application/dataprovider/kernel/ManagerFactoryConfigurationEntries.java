/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ManagerFactoryConfigurationEntries.java,v 1.3 2009/05/20 15:12:55 hburger Exp $
 * Description: Common Configuration Entries 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/20 15:12:55 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.kernel;

import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;

/**
 * Common Configuration Entries
 */
public class ManagerFactoryConfigurationEntries extends SharedConfigurationEntries {

    /**
     * Constructor 
     */
    protected ManagerFactoryConfigurationEntries() {
        // avoid instantiation
    }

    
    //------------------------------------------------------------------------
    // Configuration Options
    //------------------------------------------------------------------------
    
    /**
     * User-object names
     */
    public final static String USER_OBJECT_NAME = "userObject";

    /**
     * The user object's class must be a java bean
     */
    public final static String USER_OBJECT_CLASS = "class";

    /**
     * The plug-in names
     */
    static public final String PLUG_IN = "plugIn";

    /**
     * The object id pattern
     */
    static public final String PATTERN = "xriPattern";

    /**
     * The REST provider names
     */
    static public final String REST_PROVIDER = "restProvider";
    
    /**
     * The REST plug-in names
     */
    static public final String REST_PLUG_IN = "restPlugIn";
    
    /**
     * The user object's class must be a java bean implementing RestConnection
     */
    public final static String PLUG_IN_CLASS = "class";

    
    //------------------------------------------------------------------------
    // Manager types
    //------------------------------------------------------------------------
    
    /**
     * The Entity Manager Section
     */
    public final static String ENTITY_MANAGER = "EntityManager";
    
    /**
     * The Persistence Manager Section
     */
    public final static String PERSISTENCE_MANAGER = "PersistenceManager";
    
    
    //------------------------------------------------------------------------
    // Predefined Plug-in names
    //------------------------------------------------------------------------
    
    /**
     * The persistence manager's plug-in name
     */
    public final static String AOP2_PLUG_IN = "AOP2";

    
    //------------------------------------------------------------------------
    // State Capable Configuration Entries
    //------------------------------------------------------------------------

    /**
     * The <code>transactionTimeUnique<code> configuration field  
     * <p>
     * It's values consist of the string representation of path patterns.
     */
    static public final String TRANSACTION_TIME_UNIQUE = "transactionTimeUnique";

    /**
     * The <code>validTimeUnique<code> configuration field  
     * <p>
     * It's values consist of the string representation of path patterns.
     */
    static public final String VALID_TIME_UNIQUE = "validTimeUnique";

    
}
