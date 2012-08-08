/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractDataObject_1.java,v 1.9 2011/11/26 01:34:59 hburger Exp $
 * Description: SPICE Object Layer: Abstract Object_1_0 Implementation
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/11/26 01:34:59 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.spi;

import javax.jdo.JDOHelper;
import javax.jdo.ObjectState;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;

/**
 * Abstract Object_1_0 implementation
 */
public abstract class AbstractDataObject_1 
    implements DataObject_1_0
{

    /**
     * Constructor 
     */
    protected AbstractDataObject_1(
    ){
        // Nothing to do here
    }

    private static final String[] TO_STRING_KEYES = {
        "objectId",
        "class",
        "state",
        "defaultFetchGroup"
    };

    private static final String[] INACCESSIBLE_KEYES = {
        "objectId",
        "transactionalObjectId",
        "exceptionDomain",
        "exceptionCode"
    };

    /**
     * <code>null</code> unless the object is inaccessible
     */
    private transient ServiceException inaccessibilityReason;
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_2#getInaccessabilityReason()
     */
    public ServiceException getInaccessibilityReason(
    ) throws ServiceException {
        return this.inaccessibilityReason;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_2#objIsInaccessible()
     */
    public boolean objIsInaccessible(
    ){
        return this.inaccessibilityReason != null;
    }

    /**
     * Set the inaccessibility reason
     * 
     * @param inaccessibilityReason <code>null</code> unless the object is
     * inaccessible
     */
    protected void setInaccessibilityReason(
        ServiceException inaccessibilityReason
    ){
        this.inaccessibilityReason = inaccessibilityReason;
    }
    

    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    protected static ObjectState stateToString(
        DataObject_1_0 source
    ){
        return JDOHelper.getObjectState(source);
    }

    protected static Object defaultFetchGroupToString(
        DataObject_1_0 source
    ){
        try {
            return source.objDefaultFetchGroup().toString();
        } catch (ServiceException e) {
            return '(' + e.getMessage() + ')';
        }
    }

    /**
     * Create a String representation of an Object_1_0 instance
     * 
     * @param source
     * @param objectClass
     * @param description
     * 
     * @return
     */
    public static String toString(
        DataObject_1_0 source,
        String description
    ) {
        try {
            DataObject_1_0 object = source;
            if(object.objIsInaccessible()) {
                ServiceException reason = object.getInaccessibilityReason();
                return Records.getRecordFactory().asMappedRecord(
                    source.getClass().getName(), // recordName
                    reason.getMessage(), // recordShortDescription
                    INACCESSIBLE_KEYES, // keys, 
                    new Object[]{
                        object.jdoGetObjectId(),
                        object.jdoGetTransactionalObjectId(),
                        reason.getExceptionDomain(),
                        reason.getExceptionCode()
                    }
                ).toString();
            }
            else {
                return toString(
                    source, 
                    source.objGetClass(), 
                    description
                 );
            }
        } catch (ServiceException exception) {
            return toString(
                source, 
                "n/a", // objectClass
                exception.getMessage()
            );
        }
    }
    
    /**
     * Create a String representation of an Object_1_0 instance
     * 
     * @param source
     * @param objectClass
     * @param description
     * @return
     */
    public static String toString(
        DataObject_1_0 source,
        String objectClass, 
        String description
    ) {
        Path objectId;
        try {
            objectId = source.jdoGetObjectId();
        } catch (Exception exception) {
            objectId = null;
        }
        return Records.getRecordFactory().asMappedRecord(
		    source.getClass().getName(), // recordName
		    description, // recordShortDescription
		    TO_STRING_KEYES, // keys, 
		    new Object[]{
		        objectId,
		        objectClass,
		        JDOHelper.getObjectState(source),
		        defaultFetchGroupToString(source)
		    }
		).toString();
    }

    @Override
    public String toString(
    ){
        return toString(this, null);
    }

}
