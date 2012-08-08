/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: BasicImportPlugIn.java,v 1.6 2009/12/17 14:40:30 wfro Exp $
 * Description: Basic Import Plug-In
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/12/17 14:40:30 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.application.xml.jmi;

import java.util.Map;

import javax.jdo.JDOException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jmi.reflect.JmiException;
import javax.jmi.reflect.RefObject;
import javax.resource.cci.MappedRecord;

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.application.dataprovider.cci.JmiHelper;
import org.openmdx.application.xml.spi.ImportMode;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;


/**
 * Basic Import Plug-In
 */
public class BasicImportPlugIn implements ImportPlugIn {

    /**
     * Constructor 
     *
     * @param target the target <code>PersistenceManager</code>
     */
    public BasicImportPlugIn(
    ) {
        super();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.application.xml.jmi.ImportPlugIn#getInstance(javax.resource.cci.MappedRecord, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public <T extends RefObject> T getInstance(
        PersistenceManager persistenceManager,
        ImportMode mode,
        MappedRecord objectHolder, 
        Class<T> objectClass
    ) throws ServiceException {
        Path externalId = Object_2Facade.getPath(objectHolder);
        T refObject = null;
        switch(mode) {
            case UPDATE:
                try {
                    refObject = (T) persistenceManager.getObjectById(externalId);
                } catch (JDOObjectNotFoundException exception) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND,
                        "Could find neither persistent nor transient object for the given id",
                        new BasicException.Parameter("externalId", externalId.toXRI())
                    ); 
                } catch (JDOException exception) {
                    throw new ServiceException(exception);
                }
            break;
            case SET:
                try {
                    refObject = (T) persistenceManager.getObjectById(externalId);
                } catch (JDOObjectNotFoundException exception) {
                    refObject = persistenceManager.newInstance(objectClass);
                } catch (JDOException exception) {
                    throw new ServiceException(exception);
                }
                initialize(persistenceManager, externalId, refObject);
            break;
            case CREATE:
                try {
                    refObject = persistenceManager.newInstance(objectClass);
                } catch (JDOException exception) {
                    throw new ServiceException(exception);
                }
                initialize(persistenceManager, externalId, refObject);
            break;
        }
        if(objectClass != null && !objectClass.isInstance(refObject)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND,
                "Class mismatch",
                new BasicException.Parameter("xri", externalId.toXRI()),
                new BasicException.Parameter("existing", refObject.getClass().getName()),
                new BasicException.Parameter("imported", objectClass.getName())
            );
        }
        return refObject;
    }

    /**
     * Initialize a (potentially new) object.
     * 
     * @param objectId
     * @param refObject
     * 
     * @throws ServiceException
     */
    private void initialize(
        PersistenceManager persistenceManager,
        Path objectId,
        RefObject refObject
    ) throws ServiceException{
        //
        // Initialize in case of <code>ImportMode</code> <code>SET</code> or </code>CREATE</code>
        //
        if(refObject instanceof RefObject_1_0) try {
            ((RefObject_1_0)refObject).refInitialize(
                true, // setRequiredToNull
                true // setOptionalToNull
             );
        } catch (JmiException exception) {
            throw new ServiceException(exception);
        }
        //
        // Make transient instances persistent
        //
        if(!JDOHelper.isPersistent(refObject)) {
            Path containerId = objectId.getParent();
            String qualifier = objectId.getBase();
            RefContainer refContainer = (RefContainer) persistenceManager.getObjectById(containerId);
            if(qualifier.startsWith("!")) {
                refContainer.refAdd(
                    QualifierType.PERSISTENT,
                    qualifier.substring(1),
                    refObject
                );
            } else {
                refContainer.refAdd(
                    QualifierType.REASSIGNABLE,
                    qualifier,
                    refObject
                );
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.application.xml.jmi.ImportPlugIn#prepareInstance(javax.jmi.reflect.RefObject, javax.resource.cci.MappedRecord, java.util.Map)
     */
    public void prepareInstance(
        PersistenceManager persistenceManager,
        RefObject refObject,
        MappedRecord objectHolder, 
        Map<Path, RefObject> cache
    ) throws ServiceException {
        JmiHelper.toRefObject(
            objectHolder, // source
            refObject, // target
            cache, // maps externalIds to refObjects
            null // ignorable features
        );
    }

}
