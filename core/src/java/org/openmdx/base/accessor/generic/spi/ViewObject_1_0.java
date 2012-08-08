/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ViewObject_1_0.java,v 1.7 2007/11/20 12:14:24 hburger Exp $
 * Description: Readable_1_0 
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/11/20 12:14:24 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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

package org.openmdx.base.accessor.generic.spi;

import java.util.Collection;

import javax.resource.cci.InteractionSpec;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.exception.ServiceException;

/**
 * ViewObject_1_0
 */
public interface ViewObject_1_0 {

    /**
     * Tells whether one may read the object's features
     * 
     * @return <code>true</code> if the object's features are readable
     */
    boolean isReadable(
    ) throws ServiceException;
    
    /**
     * Retrieve the object's delegate
     * 
     * @return the object's delegate
     * 
     * @throws ServiceException 
     */
    Object_1_0 getSourceDelegate(
    ) throws ServiceException;

    /**
     * Set the object's model class.
     *
     * @param objectCLass  the object's model class
     * 
     * @throws ServiceException 
     */
    public void objSetClass(
        String objectClass
    ) throws ServiceException;

    /**
     * Retrieve all states of an object
     * 
     * @param invalidated tells whether one looks for valid or invalid states 
     * @param deleted tells whether one looks for persistent or persistent-deleted states
     * 
     * @return all states of an object
     * 
     * @throws ServiceException 
     */
    Collection allStates(
        Boolean invalidated, 
        Boolean deleted
    ) throws ServiceException;

    void cloneSourceDelegate(
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ) throws ServiceException;

    InteractionSpec getViewContext();

    /**
     * Tells whether there exist underlying valid states
     * 
     * @return <code>true</code> if there exists at least one underlying valid state
     */
    public boolean exists(
    ) throws ServiceException;

}
