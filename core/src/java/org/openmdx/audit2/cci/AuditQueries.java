/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: AuditQueries.java,v 1.11 2010/06/22 07:13:52 hburger Exp $
 * Description: Audit Queries 
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/22 07:13:52 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2010, OMEX AG, Switzerland
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
package org.openmdx.audit2.cci;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.audit2.cci2.UnitOfWorkQuery;
import org.openmdx.audit2.jmi1.Involvement;
import org.openmdx.audit2.jmi1.Segment;
import org.openmdx.audit2.jmi1.UnitOfWork;
import org.openmdx.audit2.spi.Configuration;
import org.openmdx.audit2.spi.Qualifiers;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.ExtentCapable;
import org.openmdx.base.jmi1.Modifiable;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.ExtentCollection;
import org.openmdx.base.persistence.spi.SharedObjects;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.IsGreaterOrEqualCondition;
import org.openmdx.base.query.IsInCondition;
import org.openmdx.base.query.IsInstanceOfCondition;
import org.openmdx.base.query.IsLikeCondition;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.SortOrder;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.Container;

/**
 * Audit Queries
 */
public class AuditQueries {

    /**
     * Constructor 
     */
    private AuditQueries(
    ) {
        // Avoid instantiation
    }
    
    /**
     * Retrieve the audit configuration
     * @param persistenceManager
     * @return the audit configuration
     */
    private static Configuration getConfiguration(
        PersistenceManager persistenceManager
    ){
        return SharedObjects.getPlugInObject(persistenceManager, Configuration.class);
    }

    /**
     * Retrieve the configured audit segment
     * 
     * @param persistenceManager
     * 
     * @return the configured audit segment 
     */
    private static Segment getAuditSegment(
        PersistenceManager persistenceManager
    ){
        return (Segment) persistenceManager.getObjectById(
            AuditQueries.getConfiguration(persistenceManager).getAuditSegmentId()
        );
    }
    
    /**
     * Retrieve the persistence manager from a validated Modifiable[]
     * 
     * @param modifiable a Modifiable[]
     * 
     * @return a  persistence manager retrieved from modifiable
     */
    private static PersistenceManager getPersistenceManager(
        Modifiable... modifiable
    ){
        if(modifiable == null || modifiable.length == 0) {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "The argument 'modifable' must be neither null nor empty",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER
                    )
                )
             );
        }
        return JDOHelper.getPersistenceManager(modifiable[0]);
    }

    /**
     * Retrieve the units of work any of the given objects is involved in
     * 
     * @param from earlier units of works are excluded unless <code>from</code>
     * is <code>null</code> 
     * @param units of works starting then or later are excluded unless <code>to</code>
     * is <code>null</code> 
     * @param modifiable the objects which are involved in these units of work
     * 
     * @return the units of work any of the given objects is involved in
     */
    public static Collection<UnitOfWork> getUnitOfWorkInvolvingObject(
        Date from,
        Date to,
        Modifiable... involvedObjects
    ){
        PersistenceManager persistenceManager = AuditQueries.getPersistenceManager(involvedObjects);
        Configuration configuration = AuditQueries.getConfiguration(persistenceManager);
        Segment auditSegment = AuditQueries.getAuditSegment(persistenceManager);
        if(configuration.isAudit1Persistence()) {
            Object[] involved = new Path[involvedObjects.length];
            for(
                int i = 0;
                i < involvedObjects.length;
                i++
            ){
                involved[i] = Qualifiers.toAudit1InvolvedId(involvedObjects[i].refGetPath());
            }
            List<Condition> conditions = new ArrayList<Condition>();
            conditions.add(
                new IsInstanceOfCondition(
                    true,
                    "org:openmdx:compatibility:audit1:UnitOfWork"
                )
            );
            conditions.add(
                new IsLikeCondition(
                    Quantifier.THERE_EXISTS,
                    "involved",
                    true,
                    involved
                )
            );
            if(from != null){
                conditions.add(
                    new IsGreaterOrEqualCondition(
                        Quantifier.THERE_EXISTS,
                        SystemAttributes.CREATED_AT,
                        true,
                        from
                    )
                );
            }
            if(to != null){
                conditions.add(
                    new IsGreaterOrEqualCondition(
                        Quantifier.THERE_EXISTS,
                        SystemAttributes.CREATED_AT,
                        false,
                        to
                    )
                );
            }
            return auditSegment.<UnitOfWork>getUnitOfWork().getAll(
                new Filter(
                    conditions,
                    Collections.singletonList(
                        new OrderSpecifier(
                            SystemAttributes.CREATED_AT,
                            SortOrder.ASCENDING
                        )
                    ),
                    null // extension
                )
            );
        } else {
            SortedMap<Date,UnitOfWork> unitsOfWork = new TreeMap<Date,UnitOfWork>();
            Object[] involved = new Path[involvedObjects.length];
            for(
                int i = 0;
                i < involvedObjects.length;
                i++
            ) try {
                involved[i] = Qualifiers.getAudit2ImageId(configuration, involvedObjects[i].refGetPath(), null);
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
            Container<Involvement> extent = auditSegment.getExtent();
            {
                List<Involvement> involvements = extent.getAll(
                    new Filter(
                        new IsInstanceOfCondition(
                            "org:openmdx:audit2:Involvement"
                        ),
                        new IsLikeCondition(
                            Quantifier.THERE_EXISTS,
                            SystemAttributes.OBJECT_IDENTITY,
                            true,
                            ExtentCollection.toIdentityPattern(
                                auditSegment.refGetPath().getDescendant("unitOfWork", ":*", "involvement", ":*")
                            )
                        ),
                        new IsLikeCondition(
                            Quantifier.THERE_EXISTS,
                            "beforeImage",
                            true,
                            involved
                        )
                    )
                );
                Involvements: for(Involvement involvement : involvements) {
                    for(Modifiable involvedObject : involvedObjects){
                        String xri = involvement.getObjectId();
                        if(involvedObject.refMofId().equals(xri)){
                            UnitOfWork unitOfWork = involvement.getUnitOfWork();
                            if(
                                (from == null || !from.after(unitOfWork.getCreatedAt())) &&
                                (to == null || to.after(unitOfWork.getCreatedAt()))
                            ){
                                unitsOfWork.put(unitOfWork.getCreatedAt(), unitOfWork);
                            }
                            continue Involvements;
                        }
                    }
                }
            }
            {
                List<Involvement> involvements = extent.getAll(
                    new Filter(
                        new Condition[]{
                            new IsInstanceOfCondition(
                                "org:openmdx:audit2:Involvement"
                            ),
                            new IsLikeCondition(
                                Quantifier.THERE_EXISTS,
                                SystemAttributes.OBJECT_IDENTITY,
                                true,
                                ExtentCollection.toIdentityPattern(
                                    auditSegment.refGetPath().getDescendant("unitOfWork", ":*", "involvement", ":*")
                                )
                            ),
                            new IsLikeCondition(
                                Quantifier.THERE_EXISTS,
                                "afterImage",
                                true,
                                involved
                            )
                        }
                    )
                );
                Involvements: for(Involvement involvement : involvements) {
                    for(Modifiable involvedObject : involvedObjects){
                        String xri = involvement.getObjectId();
                        if(involvedObject.refMofId().equals(xri)){
                            UnitOfWork unitOfWork = involvement.getUnitOfWork();
                            if(
                                (from == null || !from.after(unitOfWork.getCreatedAt())) &&
                                (to == null || to.after(unitOfWork.getCreatedAt()))
                            ){
                                unitsOfWork.put(unitOfWork.getCreatedAt(), unitOfWork);
                            }
                            continue Involvements;
                        }
                    }
                }
            }
            return unitsOfWork.values();
        }
    }

    /**
     * Retrieve the units of work objects with a given object id pattern are 
     * involved in.
     * 
     * @param from earlier units of works are excluded unless <code>from</code>
     * is <code>null</code> 
     * @param units of works starting then or later are excluded unless <code>to</code>
     * is <code>null</code> 
     * @param modifable a usually object id pattern based collection of objects involved
     * in any of the returned units of work
     * 
     * @return the selected units of work
     */
    public static Collection<UnitOfWork> getUnitOfWorkInvolvingObject(
        Date from,
        Date to,
        Collection<?> modifiable
    ){
        if(modifiable instanceof ExtentCollection<?>) {
            ExtentCollection<?> extentCollection = (ExtentCollection<?>) modifiable;
            PersistenceManager persistenceManager = extentCollection.getExtent().getPersistenceManager();
            Configuration configuration = getConfiguration(persistenceManager);
            Segment auditSegment = getAuditSegment(persistenceManager);
            Path pattern = extentCollection.getPattern(); 
            if(configuration.isAudit1Persistence()) { 
                List<Condition> conditions = new ArrayList<Condition>();
                conditions.add(
                    new IsInstanceOfCondition(
                        "org:openmdx:compatibility:audit1:UnitOfWork"
                    )
                );
                conditions.add(
                    new IsLikeCondition(
                        Quantifier.THERE_EXISTS,
                        "involved",
                        true,
                        pattern.getBase().endsWith("%") ? pattern : pattern.getChild("%")
                    )
                );
                if(from != null){
                    conditions.add(
                        new IsGreaterOrEqualCondition(
                            Quantifier.THERE_EXISTS,
                            SystemAttributes.CREATED_AT,
                            true,
                            from
                        )
                    );
                }
                if(to != null){
                    conditions.add(
                        new IsGreaterOrEqualCondition(
                            Quantifier.THERE_EXISTS,
                            SystemAttributes.CREATED_AT,
                            false,
                            to
                        )
                    );
                }
                return auditSegment.<UnitOfWork>getUnitOfWork().getAll(
                    new Filter(
                        conditions,
                        Collections.singletonList(
                            new OrderSpecifier(
                                SystemAttributes.CREATED_AT,
                                SortOrder.ASCENDING
                            )
                        ),
                        null // extension
                    )
                );
            } else {
                SortedMap<Date,UnitOfWork> unitsOfWork = new TreeMap<Date,UnitOfWork>();
                Container<ExtentCapable> extent = auditSegment.getExtent();
                Path involved;
                try {
                    involved = Qualifiers.getAudit2ImageId(configuration, pattern, null);
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
                }
                List<?> involvements = extent.getAll(
                    new Filter(
                        new IsInstanceOfCondition(
                            "org:openmdx:audit2:Involvement"
                        ),
                        new IsLikeCondition(
                            Quantifier.THERE_EXISTS,
                            SystemAttributes.OBJECT_IDENTITY,
                            true,
                            ExtentCollection.toIdentityPattern(
                                auditSegment.refGetPath().getDescendant("unitOfWork", ":*", "involvement", ":*")
                            )
                        ),
                        new IsLikeCondition(
                            Quantifier.THERE_EXISTS,
                            "beforeImage",
                            true,
                            involved
                        )
                    )
                );
                Involvements: for(Object i : involvements) {
                    Involvement involvement = (Involvement) i;
                    if(new Path(involvement.getObjectId()).isLike(pattern)){
                        UnitOfWork unitOfWork = involvement.getUnitOfWork();
                        if(
                            (from == null || !from.after(unitOfWork.getCreatedAt())) &&
                            (to == null || to.after(unitOfWork.getCreatedAt()))
                        ){
                            unitsOfWork.put(unitOfWork.getCreatedAt(), unitOfWork);
                        }
                        continue Involvements;
                    }
                }
                involvements = extent.getAll(
                    new Filter(
                        new Condition[]{
                            new IsInstanceOfCondition(
                                "org:openmdx:audit2:Involvement"
                            ),
                            new IsLikeCondition(
                                Quantifier.THERE_EXISTS,
                                SystemAttributes.OBJECT_IDENTITY,
                                true,
                                ExtentCollection.toIdentityPattern(
                                    auditSegment.refGetPath().getDescendant("unitOfWork", ":*", "involvement", ":*")
                                )
                            ),
                            new IsLikeCondition(
                                Quantifier.THERE_EXISTS,
                                "afterImage",
                                true,
                                involved
                            )
                        }
                    )
                );
                Involvements: for(Object i : involvements) {
                    Involvement involvement = (Involvement) i;
                    if(new Path(involvement.getObjectId()).isLike(pattern)){
                        UnitOfWork unitOfWork = involvement.getUnitOfWork();
                        if(
                            (from == null || !from.after(unitOfWork.getCreatedAt())) &&
                            (to == null || to.after(unitOfWork.getCreatedAt()))
                        ){
                            unitsOfWork.put(unitOfWork.getCreatedAt(), unitOfWork);
                        }
                        continue Involvements;
                    }
                }
                return unitsOfWork.values();
            }
        } else {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "The argument 'modifiable' must be a collection retrieved by PersistenceHelper.getCandidates()",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER
                    )
                )
             );
        }
    }
    
    /**
     * Retrieve the units of work a given object was touched in
     * 
     * @param from earlier units of works are excluded unless <code>from</code>
     * is <code>null</code> 
     * @param units of works starting then or later are excluded unless <code>to</code>
     * is <code>null</code> 
     * @param attributes the units of work are restricted to the ones which 
     * modified any of the given attributes unless <code>attributes</code> is 
     * <code>null</code> 
     * @param modifiable the object which was touched in the returned units of work
     * 
     * @return the units of work modifiable was touched in
     */
    public static Collection<UnitOfWork> getUnitOfWorkTouchingObject(
        Date from,
        Date to,
        Set<String> attributes,
        Modifiable... touchedObjects
    ){
        PersistenceManager persistenceManager = getPersistenceManager(touchedObjects);
        Configuration configuration = getConfiguration(persistenceManager);
        Segment auditSegment = getAuditSegment(persistenceManager);
        if(configuration.isAudit1Persistence()) {
            Collection<UnitOfWork> unitsOfWork = new ArrayList<UnitOfWork>();
            Candidate: for(UnitOfWork candidate : getUnitOfWorkInvolvingObject(from, to, touchedObjects)) {
                for(Modifiable touchedObject : touchedObjects) {
                    Involvement involvement = candidate.getInvolvement(touchedObject.refGetPath().toString());
                    if(involvement != null && involvement.getAfterImage() != null) {
                        if(attributes == null) {
                            unitsOfWork.add(candidate);
                            continue Candidate;
                        } else {
                            for(String modifiedFeature : involvement.getModifiedFeature()) {
                                if(attributes.contains(modifiedFeature)){
                                    unitsOfWork.add(candidate);
                                    continue Candidate;
                                }
                            }
                        }
                    }
                }
            }
            return unitsOfWork;
        } else {
            SortedMap<Date,UnitOfWork> unitsOfWork = new TreeMap<Date,UnitOfWork>();
            Object[] involved = new Path[touchedObjects.length];
            for(
                int i = 0;
                i < touchedObjects.length;
                i++
            ) try {
                involved[i] = Qualifiers.getAudit2ImageId(configuration, touchedObjects[i].refGetPath(), null);
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
            RefContainer<?> extent = (RefContainer<?>) auditSegment.getExtent();
            List<Condition> conditions = new ArrayList<Condition>();
            conditions.add(
                new IsInstanceOfCondition(
                    "org:openmdx:audit2:Involvement"
                )
            );
            conditions.add(
                new IsLikeCondition(
                    Quantifier.THERE_EXISTS,
                    SystemAttributes.OBJECT_IDENTITY,
                    true,
                    ExtentCollection.toIdentityPattern(
                        auditSegment.refGetPath().getDescendant("unitOfWork", ":*", "involvement", ":*")
                    )
                )
            );
            conditions.add(
                new IsLikeCondition(
                    Quantifier.THERE_EXISTS,
                    "afterImage",
                    true,
                    involved
                )
            );
            conditions.add(
                new IsLikeCondition(
                    Quantifier.THERE_EXISTS,
                    "beforeImage",
                    true,
                    involved
                )
            );
            if(configuration.isModifiedFeaturePersistent()) {
                conditions.add(
                    new IsInCondition(
                        Quantifier.THERE_EXISTS,
                        "modifiedFeature",
                        true,
                        attributes
                    )
                );
            }
            Filter filter = new Filter();
            filter.getCondition().addAll(conditions);
            List<?> involvements = extent.refGetAll(filter);
            Involvements: for(Object i : involvements) {
                Involvement involvement = (Involvement) i;
                String xri = involvement.getObjectId();
                for(Modifiable touchedObject : touchedObjects){
                    if(touchedObject.refMofId().equals(xri)){
                        if(involvement.getAfterImage() != null) {
                            boolean include = attributes == null || configuration.isModifiedFeaturePersistent();
                            if(!include) {
                                Features: for(String modifiedFeature : involvement.getModifiedFeature()) {
                                    if(attributes.contains(modifiedFeature)){
                                        include = true;
                                        break Features;
                                    }
                                }
                            }
                            if(include){
                                UnitOfWork unitOfWork = involvement.getUnitOfWork();
                                if(
                                    (from == null || !from.after(unitOfWork.getCreatedAt())) &&
                                    (to == null || to.after(unitOfWork.getCreatedAt()))
                                ){
                                    unitsOfWork.put(unitOfWork.getCreatedAt(), unitOfWork);
                                }
                            }
                        }
                        continue Involvements;
                    }
                }
            }
            return unitsOfWork.values();
        }
    }
    
    /**
     * Retrieve the units of work objects with a given object id pattern are 
     * involved in.
     * 
     * @param from earlier units of works are excluded unless <code>from</code>
     * is <code>null</code> 
     * @param units of works starting then or later are excluded unless <code>to</code>
     * is <code>null</code> 
     * @param attributes the units of work are restricted to the ones which 
     * modified any of the given attributes unless <code>attributes</code> is 
     * <code>null</code> 
     * @param modifable a usually object id pattern based collection of objects touched
     * by any of the returned units of work
     * 
     * @return the selected units of work
     */
    public static Collection<UnitOfWork> getUnitOfWorkTouchingObject(
        Date from,
        Date to,
        Set<String> attributes,
        Collection<?> modifiable
    ){
        if(modifiable instanceof ExtentCollection<?>) {
            ExtentCollection<?> extentCollection = (ExtentCollection<?>) modifiable;
            PersistenceManager persistenceManager = extentCollection.getExtent().getPersistenceManager();
            Configuration configuration = getConfiguration(persistenceManager);
            Path pattern = extentCollection.getPattern();
            if(configuration.isAudit1Persistence()) { 
                Collection<UnitOfWork> unitsOfWork = new ArrayList<UnitOfWork>();
                Candidate: for(UnitOfWork candidate : getUnitOfWorkInvolvingObject(from, to, modifiable)) {
                    for(Involvement involvement : candidate.<Involvement>getInvolvement()) {
                        if(
                            new Path(involvement.getObjectId()).isLike(pattern) &&
                            involvement.getAfterImage() != null
                        ) {
                            if(attributes == null) {
                                unitsOfWork.add(candidate);
                                continue Candidate;
                            } else {
                                for(String modifiedFeature : involvement.getModifiedFeature()) {
                                    if(attributes.contains(modifiedFeature)){
                                        unitsOfWork.add(candidate);
                                        continue Candidate;
                                    }
                                }
                            }
                        }
                    }
                }
                return unitsOfWork;
            } else {
                SortedMap<Date,UnitOfWork> unitsOfWork = new TreeMap<Date,UnitOfWork>();
                Segment auditSegment = getAuditSegment(persistenceManager);
                RefContainer<?> extent = (RefContainer<?>) auditSegment.getExtent();
                Path involved;
                try {
                    involved = Qualifiers.getAudit2ImageId(configuration, pattern, null);
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
                }
                List<?> involvements = extent.refGetAll(
                    new Filter(
                        new IsInstanceOfCondition(
                            "org:openmdx:audit2:Involvement"
                        ),
                        new IsLikeCondition(
                            Quantifier.THERE_EXISTS,
                            SystemAttributes.OBJECT_IDENTITY,
                            true,
                            ExtentCollection.toIdentityPattern(
                                auditSegment.refGetPath().getDescendant("unitOfWork", ":*", "involvement", ":*")
                            )
                        ),
                        new IsLikeCondition(
                            Quantifier.THERE_EXISTS,
                            "afterImage",
                            false,
                            involved
                        ),
                        new IsLikeCondition(
                            Quantifier.THERE_EXISTS,
                            "beforeImage",
                            true,
                            involved
                        )
                    )
                );
                Involvements: for(Object i : involvements) {
                    Involvement involvement = (Involvement) i;
                    if(new Path(involvement.getObjectId()).isLike(pattern)){
                        UnitOfWork unitOfWork = involvement.getUnitOfWork();
                        if(
                            (from == null || !from.after(unitOfWork.getCreatedAt())) &&
                            (to == null || to.after(unitOfWork.getCreatedAt()))
                        ){
                            unitsOfWork.put(unitOfWork.getCreatedAt(), unitOfWork);
                        }
                        continue Involvements;
                    }
                }
                return unitsOfWork.values();
            }
        } else {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "The argument 'modifiable' must be a collection retrieved by PersistenceHelper.getCandidates()",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER
                    )
                )
             );
        }
    }

    /**
     * Retrieve the units of work a given object was created in
     * 
     * @param from earlier units of works are excluded unless <code>from</code>
     * is <code>null</code> 
     * @param units of works starting then or later are excluded unless <code>to</code>
     * is <code>null</code> 
     * @param createdObjects the object which was created in the returned units of work
     * 
     * @return the units of work modifiable was created in
     */
    public static Collection<UnitOfWork> getUnitOfWorkCreatingObject(
        Date from,
        Date to,
        Modifiable... createdObjects
    ){
        PersistenceManager persistenceManager = getPersistenceManager(createdObjects);
        Configuration configuration = getConfiguration(persistenceManager);
        if(configuration.isAudit1Persistence()) {  
            throw BasicException.initHolder(
                new UnsupportedOperationException(
                    "audit1 persistence ignores object creation",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED
                    )
                )
            );
        } else {
            SortedMap<Date,UnitOfWork> unitsOfWork = new TreeMap<Date,UnitOfWork>();
            Object[] involved = new Path[createdObjects.length];
            for(
                int i = 0;
                i < createdObjects.length;
                i++
            ) try {
                involved[i] = Qualifiers.getAudit2ImageId(configuration,createdObjects[i].refGetPath(), null);
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
            Segment auditSegment = getAuditSegment(persistenceManager);
            RefContainer<?> extent = (RefContainer<?>) auditSegment.getExtent();
            List<?> involvements = extent.refGetAll(
                new Filter(
                    new IsInstanceOfCondition(
                        "org:openmdx:audit2:Involvement"
                    ),
                    new IsLikeCondition(
                        Quantifier.THERE_EXISTS,
                        SystemAttributes.OBJECT_IDENTITY,
                        true,
                        ExtentCollection.toIdentityPattern(
                            auditSegment.refGetPath().getDescendant("unitOfWork", ":*", "involvement", ":*")
                        )
                    ),
                    new IsInCondition(
                        Quantifier.FOR_ALL,
                        "beforeImage",
                        true
                    ),
                    new IsLikeCondition(
                        Quantifier.THERE_EXISTS,
                        "afterImage",
                        true,
                        involved
                    )
                )
            );
            Involvements: for(Object i : involvements) {
                Involvement involvement = (Involvement) i;
                String xri = involvement.getObjectId();
                for(Modifiable createdObject : createdObjects){
                    if(createdObject.refMofId().equals(xri)){
                        UnitOfWork unitOfWork = involvement.getUnitOfWork();
                        if(
                            (from == null || !from.after(unitOfWork.getCreatedAt())) &&
                            (to == null || to.after(unitOfWork.getCreatedAt()))
                        ){
                            unitsOfWork.put(unitOfWork.getCreatedAt(), unitOfWork);
                        }
                        continue Involvements;
                    }
                }
            }
            return unitsOfWork.values();
        }
    }

    /**
     * Retrieve the units of work objects with a given object id pattern were 
     * created in.
     * 
     * @param from earlier units of works are excluded unless <code>from</code>
     * is <code>null</code> 
     * @param units of works starting then or later are excluded unless <code>to</code>
     * is <code>null</code> 
     * @param modifable a usually object id pattern based collection of objects created
     * by any of the returned units of work
     * 
     * @return the selected units of work
     */
    public static Collection<UnitOfWork> getUnitOfWorkCreatingObject(
        Date from,
        Date to,
        Collection<?> modifiable
    ){
        if(modifiable instanceof ExtentCollection<?>) {
            ExtentCollection<?> extentCollection = (ExtentCollection<?>) modifiable;
            PersistenceManager persistenceManager = extentCollection.getExtent().getPersistenceManager();
            Configuration configuration = getConfiguration(persistenceManager);
            if(configuration.isAudit1Persistence()) { 
                throw BasicException.initHolder(
                    new UnsupportedOperationException(
                        "audit1 persistence ignores object creation",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED
                        )
                    )
                );
            } else {
                Path pattern = extentCollection.getPattern();
                SortedMap<Date,UnitOfWork> unitsOfWork = new TreeMap<Date,UnitOfWork>();
                Segment auditSegment = getAuditSegment(persistenceManager);
                RefContainer<?> extent = (RefContainer<?>) auditSegment.getExtent();
                Path involved;
                try {
                    involved = Qualifiers.getAudit2ImageId(configuration, pattern, null);
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
                }
                List<?> involvements = extent.refGetAll(
                    new Filter(
                        new IsInstanceOfCondition(
                            "org:openmdx:audit2:Involvement"
                        ),
                        new IsLikeCondition(
                            Quantifier.THERE_EXISTS,
                            SystemAttributes.OBJECT_IDENTITY,
                            true,
                            ExtentCollection.toIdentityPattern(
                                auditSegment.refGetPath().getDescendant("unitOfWork", ":*", "involvement", ":*")
                            )
                        ),
                        new IsInCondition(
                            Quantifier.FOR_ALL,
                            "beforeImage",
                            true
                        ),
                        new IsLikeCondition(
                            Quantifier.THERE_EXISTS,
                            "afterImage",
                            true,
                            involved
                        )
                    )
                );
                Involvements: for(Object i : involvements) {
                    Involvement involvement = (Involvement) i;
                    if(new Path(involvement.getObjectId()).isLike(pattern)){
                        UnitOfWork unitOfWork = involvement.getUnitOfWork();
                        if(
                            (from == null || !from.after(unitOfWork.getCreatedAt())) &&
                            (to == null || to.after(unitOfWork.getCreatedAt()))
                        ){
                            unitsOfWork.put(unitOfWork.getCreatedAt(), unitOfWork);
                        }
                        continue Involvements;
                    }
                }
                return unitsOfWork.values();
            }
        } else {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "The argument 'modifiable' must be a collection retrieved by PersistenceHelper.getCandidates()",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER
                    )
                )
             );
        }
    }

    /**
     * Retrieve the units of work a given object was removed in
     * 
     * @param from earlier units of works are excluded unless <code>from</code>
     * is <code>null</code> 
     * @param units of works starting then or later are excluded unless <code>to</code>
     * is <code>null</code> 
     * @param removedObjects the object which was created in the returned units of work
     * 
     * @return the units of work modifiable was created in
     */
    public static Collection<UnitOfWork> getUnitOfWorkRemovingObject(
        Date from,
        Date to,
        Modifiable... removedObjects
    ){
        PersistenceManager persistenceManager = getPersistenceManager(removedObjects);
        Configuration configuration = getConfiguration(persistenceManager);
        if(configuration.isAudit1Persistence()) { 
            Collection<UnitOfWork> unitsOfWork = new ArrayList<UnitOfWork>();
            Candidate: for(UnitOfWork candidate : getUnitOfWorkInvolvingObject(from, to, removedObjects)) {
                for(Modifiable touchedObject : removedObjects) {
                    Involvement involvement = candidate.getInvolvement(touchedObject.refGetPath().toString());
                    if(involvement != null && involvement.getAfterImage() == null) {
                        unitsOfWork.add(candidate);
                        continue Candidate;
                    }
                }
            }
            return unitsOfWork;
        } else {
            SortedMap<Date,UnitOfWork> unitsOfWork = new TreeMap<Date,UnitOfWork>();
            Object[] involved = new Path[removedObjects.length];
            for(
                int i = 0;
                i < removedObjects.length;
                i++
            ) try {
                involved[i] = Qualifiers.getAudit2ImageId(configuration, removedObjects[i].refGetPath(), null);
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
            Segment auditSegment = getAuditSegment(persistenceManager);
            RefContainer<?> extent = (RefContainer<?>) auditSegment.getExtent();
            List<?> involvements = extent.refGetAll(
                new Filter(
                    new IsInstanceOfCondition(
                        "org:openmdx:audit2:Involvement"
                    ),
                    new IsLikeCondition(
                        Quantifier.THERE_EXISTS,
                        SystemAttributes.OBJECT_IDENTITY,
                        true,
                        ExtentCollection.toIdentityPattern(
                            auditSegment.refGetPath().getDescendant("unitOfWork", ":*", "involvement", ":*")
                        )
                    ),
                    new IsInCondition(
                        Quantifier.FOR_ALL,
                        "afterImage",
                        true
                    ),
                    new IsLikeCondition(
                        Quantifier.THERE_EXISTS,
                        "beforeImage",
                        true,
                        involved
                    )
                )
            );
            Involvements: for(Object i : involvements) {
                Involvement involvement = (Involvement) i;
                String xri = involvement.getObjectId();
                for(Modifiable removedObject : removedObjects){
                    if(removedObject.refMofId().equals(xri)){
                        UnitOfWork unitOfWork = involvement.getUnitOfWork();
                        if(
                            (from == null || !from.after(unitOfWork.getCreatedAt())) &&
                            (to == null || to.after(unitOfWork.getCreatedAt()))
                        ){
                            unitsOfWork.put(unitOfWork.getCreatedAt(), unitOfWork);
                        }
                        continue Involvements;
                    }
                }
            }
            return unitsOfWork.values();
        }
    }

    /**
     * Retrieve the units of work objects with a given object id pattern were 
     * removed in.
     * 
     * @param from earlier units of works are excluded unless <code>from</code>
     * is <code>null</code> 
     * @param units of works starting then or later are excluded unless <code>to</code>
     * is <code>null</code> 
     * @param modifable a usually object id pattern based collection of objects removed
     * by any of the returned units of work
     * 
     * @return the selected units of work
     */
    public static Collection<UnitOfWork> getUnitOfWorkRemovingObject(
        Date from,
        Date to,
        Collection<?> modifiable
    ){
        if(modifiable instanceof ExtentCollection<?>) {
            ExtentCollection<?> extentCollection = (ExtentCollection<?>) modifiable;
            PersistenceManager persistenceManager = extentCollection.getExtent().getPersistenceManager();
            Configuration configuration = getConfiguration(persistenceManager);
            Path pattern = extentCollection.getPattern();
            if(configuration.isAudit1Persistence()) { 
                Collection<UnitOfWork> unitsOfWork = new ArrayList<UnitOfWork>();
                Candidate: for(UnitOfWork candidate : getUnitOfWorkInvolvingObject(from, to, modifiable)) {
                    for(Involvement involvement : candidate.<Involvement>getInvolvement()) {
                        if(
                            new Path(involvement.getObjectId()).isLike(pattern) &&
                            involvement.getAfterImage() == null
                        ) {
                            unitsOfWork.add(candidate);
                            continue Candidate;
                        }
                    }
                }
                return unitsOfWork;
            } else {
                SortedMap<Date,UnitOfWork> unitsOfWork = new TreeMap<Date,UnitOfWork>();
                Segment auditSegment = getAuditSegment(persistenceManager);
                RefContainer<?> extent = (RefContainer<?>) auditSegment.getExtent();
                Path involved;
                try {
                    involved = Qualifiers.getAudit2ImageId(configuration, pattern, null);
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
                }
                List<?> involvements = extent.refGetAll(
                    new Filter(
                        new IsInstanceOfCondition(
                            "org:openmdx:audit2:Involvement"
                        ),
                        new IsLikeCondition(
                            Quantifier.THERE_EXISTS,
                            SystemAttributes.OBJECT_IDENTITY,
                            true,
                            ExtentCollection.toIdentityPattern(
                                auditSegment.refGetPath().getDescendant("unitOfWork", ":*", "involvement", ":*")
                            )
                        ),
                        new IsInCondition(
                            Quantifier.FOR_ALL,
                            "afterImage",
                            true
                        ),
                        new IsLikeCondition(
                            Quantifier.THERE_EXISTS,
                            "beforeImage",
                            true,
                            involved
                        )
                    )
                );
                Involvements: for(Object i : involvements) {
                    Involvement involvement = (Involvement) i;
                    if(new Path(involvement.getObjectId()).isLike(pattern)){
                        UnitOfWork unitOfWork = involvement.getUnitOfWork();
                        if(
                            (from == null || !from.after(unitOfWork.getCreatedAt())) &&
                            (to == null || to.after(unitOfWork.getCreatedAt()))
                        ){
                            unitsOfWork.put(unitOfWork.getCreatedAt(), unitOfWork);
                        }
                        continue Involvements;
                    }
                }
                return unitsOfWork.values();
            }
        } else {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "The argument 'modifiable' must be a collection retrieved by PersistenceHelper.getCandidates()",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER
                    )
                )
             );
        }
    }

    /**
     * Retrieve the units of work belonging to a given task
     * 
     * @param persistenceManager the persistence manager to be used
     * @param taskId the id of the task the unit of work belongs to
     * 
     * @return the selected units of work
     */
    public static List<UnitOfWork> getUnitOfWorkBelongingToTask(
        PersistenceManager persistenceManager,
        String... taskId
    ){
        UnitOfWorkQuery query = (UnitOfWorkQuery) persistenceManager.newQuery(UnitOfWork.class);
        query.thereExistsTaskId().elementOf((Object[])taskId);
        query.orderByCreatedAt();
        return getAuditSegment(persistenceManager).getUnitOfWork(query);
    }
    
    /**
     * Retrieve the units of work within a given time range
     * 
     * @param persistenceManager the persistence manager ot be used
     * @param from earlier units of works are excluded unless <code>from</code>
     * is <code>null</code> 
     * @param to later units of works are excluded unless <code>to</code>
     * is <code>null</code> 
     * 
     * @return the selected units of work
     */
    public static Collection<UnitOfWork> getUnitOfWorkForTimeRange(
        PersistenceManager persistenceManager,
        Date from,
        Date to
    ){
        UnitOfWorkQuery query = (UnitOfWorkQuery) persistenceManager.newQuery(UnitOfWork.class);
        if(from != null) {
            query.createdAt().greaterThanOrEqualTo(from);
        }
        if(to != null) {
            query.createdAt().lessThanOrEqualTo(to);
        }
        query.orderByCreatedAt();
        return getAuditSegment(persistenceManager).getUnitOfWork(query);
    }

}
