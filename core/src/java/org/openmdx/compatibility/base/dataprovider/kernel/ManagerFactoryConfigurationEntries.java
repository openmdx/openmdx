/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ManagerFactoryConfigurationEntries.java,v 1.2 2008/11/04 10:01:08 hburger Exp $
 * Description: Common Configuration Entries 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/04 10:01:08 $
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
package org.openmdx.compatibility.base.dataprovider.kernel;

import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;

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
     * Lenient flag
     */
    static public final String LENIENT = "lenient";

    
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
     * The persistence manager's legacy plug-in name
     */
    public final static String LEGACY_PLUG_IN = "LegacyPlugIn";

}
