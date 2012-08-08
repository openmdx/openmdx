/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PlugIn_1.java,v 1.11 2010/01/26 15:37:47 hburger Exp $
 * Description: Audit Plug-In
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/01/26 15:37:47 $
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
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.ObjectState;

import org.openmdx.audit2.spi.Configuration;
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
     * Tells whether the names of the modified features shall be persistent
     */
    private boolean modifiedFeaturePersistent = false;

    /**
     * Tells whether org::openmex::compatibility::audit1 persistence shall be
     * used
     */
    private boolean audit1Persistence = false;

    /**
     * States of objects involved in a unit of work
     */
    private static final EnumSet<ObjectState> involvedStates = EnumSet.of(
        ObjectState.PERSISTENT_NEW,
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
    @Override
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
     * Internalize a resource identifier
     * 
     * @param value
     *            an XRI's <code>String</code> representation
     * 
     * @return an XRI's <code>Path</code> representation
     */
    private static Path toSegmentIdentifier(String value) {
        if (value != null) {
            Path candidate = new Path(value);
            if (!candidate.containsWildcard() && candidate.isLike(SEGMENT_PATTERN)) {
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
     * Get an object id patterns for the before image
     * 
     * @param index
     * 
     * @return an object id patterns for the before image
     */
    public String getAuditPattern(int index) {
        return toXRI(this.auditPrefix[index], true);
    }

    /**
     * Retrieve audit1Persistence.
     * 
     * @return Returns the audit1Persistence.
     */
    public boolean isAudit1Persistence() {
        return this.audit1Persistence;
    }

    /**
     * Set audit1Persistence.
     * 
     * @param audit1Persistence
     *            The audit1Persistence to set.
     */
    public void setAudit1Persistence(boolean audit1Persistence) {
        this.audit1Persistence = audit1Persistence;
    }

    /**
     * Retrieve modifiedFeaturePersistent.
     * 
     * @return Returns the modifiedFeaturePersistent.
     */
    public boolean isModifiedFeaturePersistent() {
        return this.modifiedFeaturePersistent;
    }

    /**
     * Set modifiedFeaturePersistent.
     * 
     * @param modifiedFeaturePersistent
     *            The modifiedFeaturePersistent to set.
     */
    public void setModifiedFeaturePersistent(boolean modifiedFeaturePersistent) {
        this.modifiedFeaturePersistent = modifiedFeaturePersistent;
    }

    
    // ------------------------------------------------------------------------
    // Implements Configuration
    // ------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.audit2.spi.Configuration#getMapping()
     */
    public Map<Path, Path> getMapping() {
        return this.mapping;
    }

    /* (non-Javadoc)
     * @see org.openmdx.audit2.spi.Configuration#getAuditSegmentId()
     */
    public Path getAuditSegmentId() {
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
     * @return <code>true/code> if the candidate shall be audited
     * @throws ServiceException
     */
    protected boolean isInvolved(
        DataObject_1 candidate
    ) throws ServiceException {
        if (
            this.dataPrefix != null && 
            candidate.jdoGetPersistenceManager().getModel().isInstanceof(candidate,"org:openmdx:base:Modifiable")
        ) {
            Path path = candidate.jdoGetObjectId();
            for (Path prefix : this.dataPrefix) {
                if (path.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.aop0.PlugIn_1_0#beforeCompletion(UnitOfWork_1)
     */
    public void beforeCompletion(UnitOfWork_1 unitOfWork) {
        try {
            DataObjectManager_1 persistenceManager = (DataObjectManager_1) unitOfWork.getPersistenceManager();
            List<DataObject_1> involvedObjects = new ArrayList<DataObject_1>();
            for (Object managedObject : persistenceManager .getManagedObjects(involvedStates)) {
                DataObject_1 candidate = (DataObject_1) managedObject;
                if (isInvolved(candidate)) {
                    involvedObjects.add(candidate);
                }
            }
            //
            // No need to audit non-modifying units of work
            //
            if (!involvedObjects.isEmpty()) {
                DataObject_1_0 auditSegment = persistenceManager.getObjectById(this.auditSegmentId);
                DataObject_1_0 auditUnitOfWork = persistenceManager.newInstance(
                    isAudit1Persistence() ? "org:openmdx:compatibility:audit1:UnitOfWork" : "org:openmdx:audit2:UnitOfWork", null
                );
                auditUnitOfWork.objSetValue("taskId", unitOfWork.getTaskIdentifier());
                auditUnitOfWork.objSetValue("createdAt", unitOfWork.getTransactionTime());
                auditUnitOfWork.objGetSet("createdBy").addAll(UserObjects.getPrincipalChain(persistenceManager));
                boolean hasInvolvements = false;
                if (isAudit1Persistence()) {
                    List<Object> involvements = auditUnitOfWork.objGetList("involved");
                    for (DataObject_1 involvedObject : involvedObjects) {
                        Path objectId = involvedObject.jdoGetObjectId();
                        DataObject_1 beforeImage = involvedObject.getBeforeImage(null);
                        boolean dirty;
                        if (beforeImage == null) {
                            dirty = false; // audit1 does not keep track of object creation
                        } else if (involvedObject.jdoIsDeleted()) {
                            dirty = true;
                        } else {
                            Set<Object> modifiedFeatures = new HashSet<Object>();
                            involvedObject.addModifiedFeaturesTo(modifiedFeatures);
                            dirty = !modifiedFeatures.isEmpty();
                        }
                        if (dirty) {
                            if (beforeImage != null) {
                                String unitOfWorkId = unitOfWork.getUnitOfWorkIdentifier();
                                beforeImage.makePersistent(
                                    Qualifiers.getAudit1ImageId(this,objectId, unitOfWorkId), 
                                    false
                                );
                                involvements.add(
                                    objectId.getDescendant("view:Audit:involved", unitOfWorkId)
                                );
                            }
                            hasInvolvements = true;
                        }
                    }
                } else {
                    Container_1_0 involvements = auditUnitOfWork.objGetContainer("involvement");
                    for (DataObject_1 involvedObject : involvedObjects) {
                        Path objectId = involvedObject.jdoGetObjectId();
                        DataObject_1_0 involvement = persistenceManager.newInstance("org:openmdx:audit2:Involvement", null);
                        DataObject_1 beforeImage = involvedObject.getBeforeImage(null);
                        DataObject_1 afterImage;
                        boolean dirty;
                        if (involvedObject.jdoIsDeleted()) {
                            afterImage = null;
                            dirty = true;
                        } else {
                            afterImage = involvedObject.jdoIsDeleted() ? null : (DataObject_1) persistenceManager.getObjectById(
                                Qualifiers.getAudit2ImageId(
                                    this,
                                    objectId,
                                    unitOfWork.getTransactionTime()
                                ),
                                false // validate would fail!
                            );
                            if (beforeImage == null) {
                                dirty = true;
                            } else {
                                Set<Object> modifiedFeatures = isModifiedFeaturePersistent() ? 
                                    involvement.objGetSet("modifiedFeature") : 
                                    new HashSet<Object>();
                                involvedObject.addModifiedFeaturesTo(modifiedFeatures);
                                dirty = !modifiedFeatures.isEmpty();
                            }
                        }
                        if (dirty) {
                            if (beforeImage != null) {
                                Path beforeImageId = Qualifiers.getAudit2ImageId(
                                    this,
                                    objectId,
                                    (Date) beforeImage.objGetValue(SystemAttributes.MODIFIED_AT)
                                );
                                beforeImage.makePersistent(beforeImageId, false);
                            }
                            involvement.objMove(involvements, objectId.toString());
                            involvement.objSetValue("beforeImage", beforeImage);
                            involvement.objSetValue("afterImage", afterImage);
                            hasInvolvements = true;
                        }
                    }
                }
                if(hasInvolvements){
                    auditUnitOfWork.objMove(
                        auditSegment.objGetContainer("unitOfWork"), 
                        unitOfWork.getUnitOfWorkIdentifier()
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

}
