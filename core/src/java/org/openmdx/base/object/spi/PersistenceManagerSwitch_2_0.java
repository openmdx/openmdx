/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PersistenceManagerSwitch_2_0.java,v 1.1 2008/02/19 13:54:52 hburger Exp $
 * Description: Persistence Manager Switch Interface 2.0
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/19 13:54:52 $
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.object.spi;

import javax.jdo.PersistenceManager;
import javax.jdo.datastore.Sequence;

/**
 * Persistence Manager Switch Interface 2.0
 */
public interface PersistenceManagerSwitch_2_0 extends Iterable<PersistenceManager> {
    
    /**
     * The following method is used to create an instance of  the given persistence-capable interface.
     * 
     * @param persistenceCapableClass
     * 
     * @return a transient instance of <code>persistenceCapableClass</code>. 
     */
    Object newDelegateInstance(
        Class<?> persistenceCapableClass
    );

    /**
     * Retrieve the delegate for the given object id
     * 
     * @param oid the delegate's object id 
     * @param validate
     * 
     * @return the delegate for the given object id
     */
    Object getDelegateInstance(
        Object oid,
        boolean validate
    );
    
    /**
     * Retrieve the named sequence
     * 
     * @param name
     * 
     * @return the named sequence
     */
    Sequence getSequence(
        String name
    );

    /**
     * Retrieve the delegate manager for a given delegate instance
     * 
     * @param pc the delegate object
     * 
     * @return the delegate manager
     */
    PersistenceManager getDelegateManager(
        Object pc
    );
        
}