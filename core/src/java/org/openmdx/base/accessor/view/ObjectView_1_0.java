/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ObjectView_1_0.java,v 1.10 2009/02/10 16:36:37 hburger Exp $
 * Description: Object 1.6 
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/10 16:36:37 $
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
package org.openmdx.base.accessor.view;

import javax.resource.cci.InteractionSpec;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.spi.Delegating_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.Model_1_6;

/**
 * ObjectView_1_0 
 */
public interface ObjectView_1_0
    extends Delegating_1_0, DataObject_1_0
{
    
    /**
     * A convenience method to access the model repository
     * 
     * @return the model repository
     */
    Model_1_6 getModel();
    
    //------------------------------------------------------------------------
    // Synchronization
    //------------------------------------------------------------------------

    /**
     * Refresh the state of the instance from its provider.
     *
     * @exception   ServiceException 
     *              if the object can't be synchronized
     */
    void objRefresh(
    ) throws ServiceException;
    
    /**
     * Retrieve the interaction specification associated with this object
     * 
     * @return <code>null</code> or the interaction specification associated with this object
     */
    InteractionSpec getInteractionSpec(
    ) throws ServiceException;
    
    /**
     * Retrieve the delegate
     * 
     * @return the delegate
     */
    DataObject_1_0 objGetDelegate(
    ) throws ServiceException;
    
    /**
     * Set the delegate 
     * 
     * @param delegate the new delegate
     */
    void objSetDelegate(
        DataObject_1_0 delegate
    );

    public Marshaller getMarshaller(
    );

    public void objDelete(
    ) throws ServiceException;

    
    //------------------------------------------------------------------------
    // State Management
    //------------------------------------------------------------------------

    /**
     * After this call the object observes unit of work boundaries.
     * <p>
     * This method is idempotent.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is locked 
     * @exception   ServiceException 
     *              if the object can't be added to the unit of work for
     *        another reason.
     */
    void objMakeTransactional(
    ) throws ServiceException;

    /**
     * After this call the object ignores unit of work boundaries.
     * <p>
     * This method is idempotent.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is dirty.
     * @exception   ServiceException 
     *              if the object can't be removed from its unit of work for
     *        another reason 
     */
    void objMakeNontransactional(
    ) throws ServiceException;

}
