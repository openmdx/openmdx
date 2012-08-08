/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: StateAccessor.java,v 1.8 2010/06/02 16:18:49 hburger Exp $
 * Description: JPA State Interrogation 
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/02 16:18:49 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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
package org.w3c.spi;

import javax.jdo.PersistenceManager;
import javax.jdo.spi.JDOImplHelper;
import javax.jdo.spi.StateInterrogation;

import org.openmdx.base.naming.Path;
import org.w3c.jpa3.AbstractObject;

/**
 * JPA State Accessor
 * <p>
 * TODO support interception of the JPA/JDO identity conversion routines by a plug-in
 */
public class StateAccessor
    extends AbstractObject.AbstractStateAccessor
    implements StateInterrogation 
{
    
    /**
     * Constructor 
     */
    private StateAccessor(){
        super();
    }
    
    /**
     * The singleton
     */
    private static StateAccessor instance = new StateAccessor();
    
    /**
     * Retrieve the state accessor singleton
     * 
     * @return retrieve the state accessor singleton
     */
    public static StateAccessor getInstance(){
        return instance;
    }
    
    /**
     * A transient or detached object has no persistence manager
     * 
     * @param pc
     * 
     * @return <code>null</code>
     */
    public PersistenceManager getPersistenceManager(Object pc) {
        return null;
    }


    /**
     * Retrieve the detached object's JDO identity
     * 
     * @param the detached object
     * 
     * @return  the detached object's XRI
     */
//  @Override
    public Path getTransactionalObjectId(Object pc) {
        return toTransactionalObjectId(getObjectId(pc));
    }

    /**
     * Set a detached object's JDO identity, state and version
     *  
     * @param pc the detached object
     * @param xri the detached object's XRI
     * @param version the detached object's version
     * 
     * @throw ClassCastException if pc is not an instance of <code>AbstractObject</code>
     */
    public void initializeDetachedObject(
        Object pc,
        Path xri,
        Object version
    ){
        AbstractObject.AbstractStateAccessor.setVersion(pc, version);
        setTransactionalObjectId(pc, xri);
    }

    /**
     * Set a transient or detached object's JDO identity
     *  
     * @param pc the detached object
     * @param xri the detached object's XRI
     * 
     * @throw ClassCastException if pc is not an instance of <code>AbstractObject</code>
     */
    public void setTransactionalObjectId(
        Object pc,
        Path xri
    ){
        AbstractObject.AbstractStateAccessor.setObjectId(pc, toObjectId(xri));
    }
    
    /**
     * Convert the JPA identity to the JDO object id
     * 
     * @param openmdxjdoIdentity the JPA identity
     * 
     * @return the JDO object id
     */
    public Path toTransactionalObjectId(
        String openmdxjdoIdentity
    ){
        // TODO let a plug-in intercept this method
        return openmdxjdoIdentity == null ? null : new Path(openmdxjdoIdentity); 
    }
    
    /**
     * Convert the JDO object id to the JPA identity
     * 
     * @param the JDO object id
     * 
     * @return the JPA identity
     */
    public String toObjectId(
        Path xri
    ){
        // TODO let a plug-in intercept this method
        return xri == null ? null : xri.toXRI();
    }
    
    static {
        JDOImplHelper.getInstance().addStateInterrogation(instance);
    }

}