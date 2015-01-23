/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Persistence Manager Target 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2014, OMEX AG, Switzerland
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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefObject;
import javax.resource.cci.MappedRecord;

import org.omg.mof.spi.Names;
import org.openmdx.application.xml.spi.ImportMode;
import org.openmdx.application.xml.spi.ImportTarget;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.cci.UnitOfWork;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.loading.Classes;

/**
 * Persistence Manager Target
 * <p>
 * Import is done in two steps<ol>
 * <li>Fetch existing objects and create transient instances for new objects
 * <li>Populate the attribute values and prepare the instances
 * </ol>
 */
public class PersistenceManagerTarget implements ImportTarget {

    /**
     * Constructor 
     *
     * @param target
     */
    public PersistenceManagerTarget(
        PersistenceManager persistenceManager,
        ImportPlugIn target
    ){
        this.persistenceManager = persistenceManager;
        this.target = target;
    }
    
    /**
     * The Plug-In's <code>PersistenceManager</code>
     */
    private final PersistenceManager persistenceManager;
    
    /**
     * The delegate
     */
    private final ImportPlugIn target;
    
    /**
     * The objects to be populated during epilog
     */
    private final Map<RefObject,MappedRecord> data = new LinkedHashMap<RefObject,MappedRecord>();

    /**
     * The cache required by the JMI Helper
     */
    private final Map<Path,RefObject> cache = new HashMap<Path,RefObject>();
    
    /**
     * Tells whether the import spawns an implicit transaction or participates in a running transaction.
     */
    private boolean autoCommit;
    
    protected UnitOfWork currentUnitOfWork(){
        return PersistenceHelper.currentUnitOfWork(this.persistenceManager);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ImportTarget#importObject(org.openmdx.application.xml.spi.ImportTarget.Mode, javax.resource.cci.MappedRecord)
     */
    @Override
    public void importObject(
        ImportMode mode, 
        ObjectRecord objectHolder
    ) throws ServiceException {
        Path externalId = Object_2Facade.getPath(objectHolder);
        if(this.cache.containsKey(externalId)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "The same object must not be imported twice from the same source",
                new BasicException.Parameter("externaId", externalId)
            );
        }
        String modelClassName = Object_2Facade.getObjectClass(objectHolder);
        String javaClassName = Names.toClassName(
            modelClassName, 
            Names.JMI1_PACKAGE_SUFFIX
        );
        try {
        	RefObject refObject = this.target.getInstance(
                this.persistenceManager, 
                mode, 
                objectHolder, 
                Classes.<RefObject>getApplicationClass(javaClassName)
            );
        	if(refObject != null) {
	            this.cache.put(externalId, refObject);
	            this.data.put(refObject, objectHolder);
        	}
        } catch (ClassNotFoundException exception) {
              throw new ServiceException(
                  exception,
                  BasicException.Code.DEFAULT_DOMAIN,
                  BasicException.Code.INVALID_CONFIGURATION,
                  "Could not retrieve the Java class corresponding to the given model class",
                  new BasicException.Parameter("model-class", modelClassName),
                  new BasicException.Parameter("binding", Names.JMI1_PACKAGE_SUFFIX),
                  new BasicException.Parameter("java-class", javaClassName)
              );
        }
    }

    /**
     * Prepare the unit of work
     * 
     * @throws ServiceException
     */
    private void prepareUnitOfWork(
    ) throws ServiceException {
        try {
            for(Map.Entry<RefObject, MappedRecord> data : this.data.entrySet()) {
                this.target.prepareInstance(
                    this.persistenceManager, 
                    data.getKey(), 
                    data.getValue(), 
                    this.cache
                );
            }
        } catch (RuntimeException exception) {
            currentUnitOfWork().setRollbackOnly();
            Throwables.log(exception);
            throw exception;
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ImportTarget#importEpilog()
     */
    @Override
    public void importEpilog(
        boolean success
    ) throws ServiceException {
        if(this.autoCommit) {
            //
            // Auto-commit mode
            //
            if(success){
                try {
                    prepareUnitOfWork();
                    currentUnitOfWork().commit();
                } catch (ServiceException exception) {
                    currentUnitOfWork().rollback();
                    throw exception;
                } catch (JDOUserException exception) {
                    throw new ServiceException(exception);
                }
            } else {
                try {
                    currentUnitOfWork().rollback();
                } catch (JDOUserException exception) {
                    throw new ServiceException(exception);
                }
            }
        } else {
            //
            // Participating in a running transaction
            //
            if(!currentUnitOfWork().getRollbackOnly()){
                if(success) { 
                    try {
                        prepareUnitOfWork();
                    } catch (ServiceException exception) {
                        currentUnitOfWork().setRollbackOnly();
                        throw exception;
                    }
                } else {
                    currentUnitOfWork().setRollbackOnly();
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ImportTarget#importProlog()
     */
    @Override
    public void importProlog(
    ) throws ServiceException {
        //
        // Cache management
        //
        this.data.clear();
        this.cache.clear();
        //
        // Transaction management
        //
        UnitOfWork transaction = currentUnitOfWork();
        this.autoCommit = !transaction.isActive();
        if(this.autoCommit) {
            transaction.begin();
        }
    }

}
