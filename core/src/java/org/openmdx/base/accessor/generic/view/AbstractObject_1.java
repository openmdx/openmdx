/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractObject_1.java,v 1.1 2008/09/18 12:46:37 hburger Exp $
 * Description: Abstract Object_1
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/18 12:46:37 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.generic.view;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.spi.MarshallingObject_1;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.ExtendedIOException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.naming.Path;

/**
 * Registers the the delegates with their manager
 */
abstract class AbstractObject_1 extends MarshallingObject_1<Manager_1> {

    /**
     * Constructor 
     *
     * @param object
     * @param marshaller
     * @throws ServiceException
     */
    protected AbstractObject_1(
        Object_1_0 object,
        Manager_1 marshaller
    ) throws ServiceException{
        super(object, marshaller);
        objAddEventListener(
            null, // feature 
            marshaller // listener
        );
    }
    
    
    //--------------------------------------------------------------------------
    // Extends DelegatingObject_1
    //--------------------------------------------------------------------------

    /**
     * Get a reference feature.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       requestedFeature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ClassCastException
     *              if the feature is not a reference
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     */
    public FilterableMap<String, Object_1_0> objGetContainer(
        String requestedFeature
    ) throws ServiceException {
        String feature;
        if(requestedFeature.endsWith(SystemAttributes.USE_OBJECT_IDENTITY_HINT)) {
            feature = requestedFeature.substring(
                0, 
                requestedFeature.length() - SystemAttributes.USE_OBJECT_IDENTITY_HINT.length()
            );
            if(!objIsInaccessable()) {
                Object path = objGetValue(SystemAttributes.OBJECT_IDENTITY);
                if(path != null) try {
                    Path identity = new Path(path.toString());
                    if(!objGetPath().equals(identity)) {
                        return getMarshaller().getObject(
                            identity
                        ).objGetContainer(
                            feature
                        );
                    }
                } catch (Exception ignore) {
                    // fall back to the object's path
                }
            }
        } else {
            feature = requestedFeature;
        }
        return super.objGetContainer(feature);
    }
    

    //--------------------------------------------------------------------------
    // Implements Serializable
    //--------------------------------------------------------------------------

    /**
     * Save the data of the <tt>Object_1_0</tt> instance to a stream (that
     * is, serialize it).
     *
     * @serialData The objects data
     */
    private synchronized void writeObject(
        java.io.ObjectOutputStream stream
    ) throws java.io.IOException {
        // stream.defaultWriteObject(); has nothing to do
    }

    /**
     * Reconstitute the <tt>Object_1_0</tt> instance from a stream (that is,
     * deserialize it).
     */
    private synchronized void readObject(
        java.io.ObjectInputStream stream
    ) throws java.io.IOException, ClassNotFoundException {
        // stream.defaultReadObject(); has nothing to do
        try {
            getMarshaller().cache(getDelegate(), this);
        } catch (ServiceException exception) {
            throw new ExtendedIOException(exception);
        }
    }
    
}