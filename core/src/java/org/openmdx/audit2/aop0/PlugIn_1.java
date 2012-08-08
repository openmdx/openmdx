/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PlugIn_1.java,v 1.22 2010/12/23 17:38:45 hburger Exp $
 * Description: Audit Plug-In
 * Revision:    $Revision: 1.22 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/12/23 17:38:45 $
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.audit2.aop0;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.ObjectState;
import javax.jdo.PersistenceManager;

import org.openmdx.audit2.spi.Configuration;
import org.openmdx.audit2.spi.InvolvementPersistence;
import org.openmdx.audit2.spi.Qualifiers;
import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.rest.DataObjectManager_1;
import org.openmdx.base.accessor.rest.DataObject_1;
import org.openmdx.base.accessor.rest.UnitOfWork_1;
import org.openmdx.base.aop0.PlugIn_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.UserObjects;
import org.openmdx.kernel.exception.BasicException;

/**
 * Audit Plug-In
 */
public class PlugIn_1 implements Configuration, PlugIn_1_0 {

    /**
     * The audit segment
     */
    private Path auditSegmentId;

    /**
     * The XRI pattern of the objects to be audited
     */
    protected Path[] dataPrefix;

    /**
     * The XRI pattern of the before images
     */
    protected Path[] auditPrefix;

    /**
     * The involvement persistence mode
     */
    private InvolvementPersistence involvementPersistence = InvolvementPersistence.STANDARD;

    /**
     * States of objects involved in a unit of work 
     */
    private static final EnumSet<ObjectState> involvedStatesForInvolvementPersistenceEmbedded = EnumSet.of(
        ObjectState.PERSISTENT_DIRTY,
        ObjectState.PERSISTENT_DELETED
    );

    private static final Path SEGMENT_PATTERN = new Path(
        "xri://@openmdx*($..)/provider/($..)/segment/($..)"
    );

    /**
     * Maps the data paths to audit paths
     */
    private final Map<Path, Path> mapping = new AbstractMap<Path, Path>() {

        Set<java.util.Map.Entry<Path, Path>> entries =
            new AbstractSet<java.util.Map.Entry<Path, Path>>() {

            @Override
            public Iterator<java.util.Map.Entry<Path, Path>> iterator() {
                return new Iterator<java.util.Map.Entry<Path, Path>>() {

                    protected int cursor = 0;

                    public boolean hasNext() {
                        return this.cursor < PlugIn_1.this.dataPrefix.length;
                    }

                    public java.util.Map.Entry<Path, Path> next() {
                        return new java.util.Map.Entry<Path, Path>() {

                            private final int index = cursor++;

                            public Path getKey() {
                                return PlugIn_1.this.dataPrefix[index];
                            }

                            public Path getValue() {
                                return PlugIn_1.this.auditPrefix[index];
                            }

                            public Path setValue(Path value) {
                                throw new UnsupportedOperationException();
                            }

                        };
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }

                };
            }

            @Override
            public int size() {
                return PlugIn_1.this.dataPrefix.length;
            }

        };

        @Override
        public Set<java.util.Map.Entry<Path, Path>> entrySet() {
            return this.entries;
        }

    };

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.aop0.PlugIn_1_0#g<Object>etSharedObject()
     */
    //  @Override
    public Object getUserObject(Object key) {
        return Configuration.class == key ? this : null;
    }

    /**
     * Internalize a resource identifier
     * 
     * @param value
     *            an XRI's <code>String</code> representation
     * @return an XRI's <code>Path</code> representation
     */
    private static Path toPrefix(String value) {
        if (value != null) {
            Path candidate = new Path(value);
            if (candidate.toXRI().endsWith("/($...)")) {
                Path parent = candidate.getParent();
                if (!parent.containsWildcard()) {
                    return parent;
                }
            }
        }
        throw BasicException.initHolder(
            new IllegalArgumentException(
                "Valid segment starts-with pattern expected",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter("xri", value)
                )
            )
        );
    }

    /**
     * Validate a segment identifier
     * 
     * @param value
     *            an XRI's <code>String</code> representation
     * 
     * @return an XRI's <code>Path</code> representation
     */
    private static Path toSegmentIdentifier(String value) {
        if (value != null) {
            Path candidate = new Path(value);
            if (candidate.isLike(SEGMENT_PATTERN)) {
                return candidate;
            }
        }
        throw BasicException.initHolder(new IllegalArgumentException(
            "Valid segment path expected",
            BasicException.newEmbeddedExceptionStack(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                new BasicException.Parameter("xri", value))));
    }

    /**
     * Externalize a resource identifier
     * 
     * @param value
     *            an XRI's <code>Path</code> representation
     * @param prefix
     * 
     * @return an XRI's <code>String</code> representation
     */
    private static String toXRI(Path value, boolean prefix) {
        return 
            value == null ? null : 
            prefix ? value.toXRI() + "/($...)" : 
            value.toXRI();
    }

    /**
     * Retrieve auditSegment.
     * 
     * @return Returns the auditSegment.
     */
    public String getAuditSegment() {
        return toXRI(this.auditSegmentId, false);
    }

    /**
     * Set auditSegment.
     * 
     * @param auditSegment
     *            The auditSegment to set.
     */
    public void setAuditSegment(String auditSegment) {
        this.auditSegmentId = toSegmentIdentifier(auditSegment);
    }

    /**
     * Set the object id patterns for the objects to be audited
     * 
     * @param value
     *            the object id patterns for the objects to be audited
     */
    public void setDataPattern(String[] value) {
        this.dataPrefix = new Path[value.length];
        int i = 0;
        for (String dataPattern : value) {
            this.dataPrefix[i++] = toPrefix(dataPattern);
        }
    }

    /**
     * Retrieve the object id patterns for the objects to be audited
     * 
     * @return the object id patterns for the objects to be audited
     */
    public String[] getDataPattern() {
        String[] dataPattern = new String[this.dataPrefix.length];
        int i = 0;
        for (Path value : this.dataPrefix) {
            dataPattern[i++] = toXRI(value, true);
        }
        return dataPattern;
    }

    /**
     * Set an object id patterns for objects to be audited
     * 
     * @param index
     * @param value
     */
    public void setDataPattern(int index, String value) {
        this.dataPrefix[index] = toPrefix(value);
    }

    /**
     * Get an object id patterns for objects to be audited
     * 
     * @param index
     * 
     * @return an object id patterns for objects to be audited
     */
    public String getDataPattern(int index) {
        return toXRI(this.dataPrefix[index], true);
    }

    /**
     * Set the object id patterns for the before images
     * 
     * @param value
     *            the object id patterns for the before images
     */
    public void setAuditPattern(String[] value) {
        this.auditPrefix = new Path[value.length];
        int i = 0;
        for (String auditPattern : value) {
            this.auditPrefix[i++] = toPrefix(auditPattern);
        }
    }

    /**
     * Retrieve the object id patterns for the before images
     * 
     * @return the object id patterns for the before images
     */
    public String[] getAuditPattern() {
        String[] auditPattern = new String[this.auditPrefix.length];
        int i = 0;
        for (Path value : this.auditPrefix) {
            auditPattern[i++] = toXRI(value, true);
        }
        return auditPattern;
    }

    /**
     * Set an object id patterns for the before image
     * 
     * @param index
     * @param value
     */
    public void setAuditPattern(int index, String value) {
        this.auditPrefix[index] = toPrefix(value);
    }
    
    /**
     * Retrieve involvementPersistence.
     *
     * @return Returns the involvementPersistence.
     */
    public String getInvolvementPersistence() {
        return this.involvementPersistence.name();
    }

    
    /**
     * Set involvementPersistence.
     * 
     * @param involvementPersistence The involvementPersistence to set.
     */
    public void setInvolvementPersistence(
        String involvementPersistence
    ) {
        this.involvementPersistence = InvolvementPersistence.valueOf(involvementPersistence);
    }

   
    /* (non-Javadoc)
     * @see org.openmdx.audit2.spi.Configuration#getPersistenceMode()
     */
//  @Override
    public InvolvementPersistence getPersistenceMode() {
        return this.involvementPersistence;
    }

    /**
     * Get an object id patterns for the before image
     * 
     * @param index
     * 
     * @return an object id patterns for the before image
     */
    public String getAuditPattern(int index) {
        return toXRI(this.auditPrefix[index], true);
    }

    /* (non-Javadoc)
     * @see org.openmdx.audit2.spi.Configuration#getMapping()
     */
    public Map<Path, Path> getMapping() {
        return this.mapping;
    }

    /* (non-Javadoc)
     * @see org.openmdx.audit2.spi.Configuration#getAuditSegmentId()
     */
    public Path getAuditSegmentId(
        PersistenceManager context
    ) {
        return this.auditSegmentId;
    }


    // ------------------------------------------------------------------------
    // Implements PlugIn_1_0
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.base.aop0.PlugIn_1_0#getQualifier(org.openmdx.base.accessor
     * .rest.DataObject_1, java.lang.String)
     */
    public String getQualifier(DataObject_1 object, String qualifier)
    throws ServiceException {
        return qualifier;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.base.aop0.PlugIn_1_0#setCore(org.openmdx.base.accessor.rest
     * .DataObject_1, org.openmdx.base.accessor.rest.DataObject_1)
     */
    public void postSetCore(
        DataObject_1 target, 
        DataObject_1 core
    ) throws ServiceException {
        // nothing to do
    }

    /**
     * Tests whether a given object shall be audited
     * 
     * @param candidate
     *            the object to be tested
     * 
     * @return the matching index if the object is involved, -1 otherwise
     * @throws ServiceException
     */
    protected int getMatchingIndex(
        DataObject_1 candidate
    ) throws ServiceException {
        if (
            this.dataPrefix != null && 
            candidate.jdoGetPersistenceManager().getModel().isInstanceof(candidate,"org:openmdx:base:Modifiable")
        ) {
            Path path = candidate.jdoGetObjectId();
            for(int i = 0; i < this.dataPrefix.length; i++) {
                if(path.startsWith(this.dataPrefix[i])) {
                    return i;
                }
             }
        }
        return -1;
    }

    /**
     * Tests whether a given object shall be audited
     * 
     * @param candidate
     *            the object to be tested
     * 
     * @return <code>true/code> if the candidate shall be audited
     * @throws ServiceException
     */
    protected boolean isInvolved(
        DataObject_1 candidate
    ) throws ServiceException {
        return getMatchingIndex(candidate) >= 0;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.aop0.PlugIn_1_0#beforeCompletion(UnitOfWork_1)
     */
    public void flush(
        UnitOfWork_1 unitOfWork, 
        boolean beforeCompletion
    ) {
        try {
            DataObjectManager_1 persistenceManager = (DataObjectManager_1) unitOfWork.getPersistenceManager();
            Map<Path,DataObject_1> auditBeforeImages = new HashMap<Path, DataObject_1>();
            for (Object managedObject : persistenceManager.getManagedObjects(involvedStatesForInvolvementPersistenceEmbedded)) {
                DataObject_1 candidate = (DataObject_1) managedObject;
                if(this.getMatchingIndex(candidate) >= 0) {
                    DataObject_1 beforeImage = candidate.getBeforeImage(null); // do not in-line!
                    if(beforeImage != null && !beforeImage.jdoIsPersistent()) {
                        if(candidate.jdoIsDeleted() || candidate.objIsModified()){
                            auditBeforeImages.put(
                                Qualifiers.getAudit2BeforeImageId(
                                    this,
                                    candidate.jdoGetObjectId(),
                                    unitOfWork.getUnitOfWorkIdentifier()
                                ), 
                                beforeImage
                            );
                        } else if (candidate.jdoIsDirty()) {
                            candidate.unconditionalEvict();
                        }
                    }
                }
            }
            if(!auditBeforeImages.isEmpty()) {
                if(this.involvementPersistence != InvolvementPersistence.EMBEDDED) {
                    throw new UnsupportedOperationException("Persistence modes other than EMBEDDED are not yet supported");
                }
                DataObject_1_0 auditSegment = persistenceManager.getObjectById(this.getAuditSegmentId(persistenceManager));
                String unitOfWorkId = unitOfWork.getUnitOfWorkIdentifier();
                Container_1_0 unitOfWorkContainer = auditSegment.objGetContainer("unitOfWork");
                if(!unitOfWorkContainer.containsKey(unitOfWorkId)){
                    DataObject_1_0 auditUnitOfWork = persistenceManager.newInstance("org:openmdx:audit2:UnitOfWork", null);
                    auditUnitOfWork.objSetValue("taskId", unitOfWork.getTaskIdentifier());
                    auditUnitOfWork.objSetValue(SystemAttributes.CREATED_AT, unitOfWork.getTransactionTime());
                    auditUnitOfWork.objGetSet(SystemAttributes.CREATED_BY).addAll(UserObjects.getPrincipalChain(persistenceManager));
                    auditUnitOfWork.objMove(
                        unitOfWorkContainer, 
                        unitOfWorkId
                    );
                }
                for(Map.Entry<Path, DataObject_1> auditBeforeImage : auditBeforeImages.entrySet()){
                    auditBeforeImage.getValue().makePersistent(
                        auditBeforeImage.getKey(), 
                        false // already flushed
                    );
                }
            }
        } catch (ServiceException exception) {
            throw new javax.jdo.JDOUserCallbackException(
                "audit2's beforeCompletion() callback failed",
                exception
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#callbackOnCascadedDeletes()
     */
//  @Override
    public boolean requiresCallbackOnCascadedDelete(
        DataObject_1 object
    ) throws ServiceException {
        return this.isInvolved(object);
    }

}
