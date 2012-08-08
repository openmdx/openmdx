/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Records.java,v 1.2 2007/10/10 16:05:50 hburger Exp $
 * Description: Records 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:05:50 $
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

package org.openmdx.base.accessor.generic.cci;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.resource.cci.Interaction;
import javax.resource.cci.RecordFactory;

import org.openmdx.base.accessor.generic.spi.InteractionCapable;

/**
 * Records
 * 
 * @since openMDX 2.0
 */
public class Records {

    /**
     * Avoid instantiation 
     */
    private Records() {
        super();
    }
    
    /**
     * Retrieve the <code>Object</code>'s <code>Interaction</code> interface
     * 
     * @return the <code>Object</code>'s <code>Interaction</code> interface 
     * if it is an instance of <code>InteractionCapable</code>; 
     * <code>null</code> otherwise.
     */
    public static Interaction getInteraction(
        Object object
    ){
        return object instanceof InteractionCapable ?
            ((InteractionCapable)object).getInteraction() :
            null;
    }

    /**
     * Retrieve the <code>Object</code>'s <code>RecordFactory</code> 
     * 
     * @return the <code>Object</code>'s <code>RecordFactory</code> or 
     * <code>null</code> if there is none.
     */
    public static RecordFactory getRecordFactory(
        Object object
    ){
        PersistenceManager persistenceManager = JDOHelper.getPersistenceManager(object);
        return (RecordFactory) (persistenceManager == null ?
            null :
            persistenceManager.getUserObject(RecordFactory.class));
    }
    
    /**
     * Put the specified key-value pair into the map of user objects.
     */
    public static Object putUserObject (
        Object key, 
        Object val
    ){
        return null; // TODO
    }

    /**
     * Get the value for the specified key from the map of user objects.
     * @param key the key of the object to be returned
     * @return the object 
     */
    public static Object getUserObject (
        Object key
    ){
        return null; // TODO
    }

    /**
     * Remove the specified key and its value from the map of user objects.
     * @param key the key of the object to be removed
     * @since 2.0
     */
    public static Object removeUserObject (
        Object key
    ) {
        return null; // TODO
    }

}
