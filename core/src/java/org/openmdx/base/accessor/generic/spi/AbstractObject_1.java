/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractObject_1.java,v 1.25 2008/12/15 03:15:36 hburger Exp $
 * Description: SPICE Object Layer: Abstract Object_1_0 Implementation
 * Revision:    $Revision: 1.25 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:36 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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

import java.util.HashSet;
import java.util.Set;

import javax.resource.ResourceException;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_1;
import org.openmdx.base.accessor.generic.cci.Object_1_2;
import org.openmdx.base.accessor.generic.cci.Object_1_3;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.resource.Records;
import org.openmdx.compatibility.base.naming.Path;

/**
 * Abstract Object_1_0 implementation
 */
public abstract class AbstractObject_1 
    implements Object_1_1, Object_1_2, Object_1_3
{

    /**
     * Constructor 
     */
    protected AbstractObject_1(
    ){
        // Nothing to do here
    }

    private static final String[] TO_STRING_KEYES = {
        "objectId",
        "resourceIdentifier",
        "class",
        "state",
        "defaultFetchGroup"
    };

    private static final String[] INACCESSIBLE_KEYES = {
        "objectId",
        "resourceIdentifier",
        "exceptionDomain",
        "exceptionCode"
    };


    private static final String[] HOLLOW_KEYES = {
        "objectId",
        "state"
    };
    
    /**
     * <code>null</code> unless the object is inaccessible
     */
    private transient ServiceException inaccessibilityReason;
    
    
    //------------------------------------------------------------------------
    // Implements Object_1_2
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_2#getInaccessabilityReason()
     */
    public ServiceException getInaccessibilityReason() {
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

    protected static String stateToString(
        Object_1_0 source
    ){
        try {
            Set<String> state = new HashSet<String>();
            if(source.objIsDeleted()) state.add("deleted");
            if(source.objIsDirty()) state.add("dirty");
            if(source.objIsInUnitOfWork()) state.add("inUnitOfWork");
            if(source.objIsNew()) state.add("new");
            if(source.objIsPersistent()) state.add("persistent");
            return state.toString();
        } catch (ServiceException e) {
            return '(' + e.getMessage() + ')';
        }
    }

    protected static Object defaultFetchGroupToString(
        Object_1_0 source
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
        Object_1_0 source,
        String description
    ) {
        try {
            if(source instanceof Object_1_1) {
                Object_1_1 object = (Object_1_1) source;
                if(object.objIsHollow()) {
                    return Records.getRecordFactory().asMappedRecord(
                        source.getClass().getName(), // recordName
                        description, // recordShortDescription
                        HOLLOW_KEYES, // keys, 
                        new Object[]{
                            object.objGetPath(),
                            "hollow"
                        }
                    ).toString();
                }
            }
            if(source instanceof Object_1_2) {
                Object_1_2 object = (Object_1_2) source;
                if(object.objIsInaccessible()) {
                    ServiceException reason = object.getInaccessibilityReason();
                    return Records.getRecordFactory().asMappedRecord(
                        source.getClass().getName(), // recordName
                        reason.getMessage(), // recordShortDescription
                        INACCESSIBLE_KEYES, // keys, 
                        new Object[]{
                            object.objGetPath(),
                            object.objGetResourceIdentifier(),
                            reason.getExceptionDomain(),
                            reason.getExceptionCode()
                        }
                    ).toString();
                }
            }
            return toString(
                source, 
                source.objGetClass(), 
                description
             );
        } catch (ResourceException exception) {
            return source.getClass().getName() + '@' + System.identityHashCode(source);
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
        Object_1_0 source,
        String objectClass, 
        String description
    ) {
        Path objectId;
        try {
            objectId = source.objGetPath();
        } catch (ServiceException exception) {
            objectId = null;
        }
        try {
            return Records.getRecordFactory().asMappedRecord(
                source.getClass().getName(), // recordName
                description, // recordShortDescription
                TO_STRING_KEYES, // keys, 
                new Object[]{
                    objectId,
                    source.objGetResourceIdentifier(),  
                    objectClass,
                    stateToString(source),
                    defaultFetchGroupToString(source)
                }
            ).toString();
        } catch (ResourceException exception) {
            return source.getClass().getName() + '@' + System.identityHashCode(source);
        }
    }

    public String toString(
    ){
        return toString(this, null);
    }

}
