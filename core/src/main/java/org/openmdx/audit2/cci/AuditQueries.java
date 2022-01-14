/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Audit Queries 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jdo.PersistenceManager;

import org.openmdx.audit2.cci2.UnitOfWorkQuery;
import org.openmdx.audit2.jmi1.Involvement;
import org.openmdx.audit2.jmi1.Segment;
import org.openmdx.audit2.jmi1.UnitOfWork;
import org.openmdx.audit2.spi.Configuration;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.jmi1.Modifiable;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.ExtentCollection;
import org.openmdx.base.persistence.spi.SharedObjects;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.IsInCondition;
import org.openmdx.base.query.IsInstanceOfCondition;
import org.openmdx.base.query.IsLikeCondition;
import org.openmdx.base.query.Quantifier;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.jdo.ReducedJDOHelper;

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
            AuditQueries.getConfiguration(persistenceManager).getAuditSegmentId(persistenceManager)
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
        return ReducedJDOHelper.getPersistenceManager(modifiable[0]);
    }

    /**
     * Retrieve the objects' ids
     * 
     * @param objects
     * 
     * @return the objects' ids
     */
    private static Object[] toObjectIds(
        Modifiable... objects
    ){
        Object[] objectIds = new Object[objects.length];
        for(
            int i = 0;
            i < objects.length;
            i++
        ){
            objectIds[i] = ReducedJDOHelper.getAnyObjectId(objects[i]);
        }
        return objectIds;
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
        switch(configuration.getPersistenceMode()) {
            case EMBEDDED: {
                List<Involvement> involvements = auditSegment.<Involvement>getExtent().getAll(
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
                            Quantifier.THERE_EXISTS,
                            "object",
                            true,
                            toObjectIds(involvedObjects)
                        )
                    )
                );
                SortedMap<Date,UnitOfWork> unitsOfWork = new TreeMap<Date,UnitOfWork>();
                for(Involvement involvement : involvements) {
                    try {
                        UnitOfWork unitOfWork = involvement.getUnitOfWork();
                        if(
                            (from == null || !from.after(unitOfWork.getCreatedAt())) &&
                            (to == null || to.after(unitOfWork.getCreatedAt()))
                        ){
                            unitsOfWork.put(unitOfWork.getCreatedAt(), unitOfWork);
                        }
                    } catch (RuntimeException e) {
                        if(BasicException.toExceptionStack(e).getExceptionCode() != BasicException.Code.NOT_FOUND) {
                            throw e;
                        }
                    }
                }
                return unitsOfWork.values();
            }
            default: 
                throw BasicException.initHolder(
                    new UnsupportedOperationException(
                        "Persistence modes other than EMBEDDED are not yet supported",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_IMPLEMENTED
                        )
                    )
                );
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
            switch(configuration.getPersistenceMode()) {
                case EMBEDDED: {
                    List<Involvement> involvements = auditSegment.<Involvement>getExtent().getAll(
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
                                "object",
                                true,
                                extentCollection.getPattern()
                            )
                        )
                    );
                    SortedMap<Date,UnitOfWork> unitsOfWork = new TreeMap<Date,UnitOfWork>();
                    for(Involvement involvement : involvements) {
                        if(ReducedJDOHelper.isPersistent(involvement)) try {
                            UnitOfWork unitOfWork = involvement.getUnitOfWork();
                            if(
                                (from == null || !from.after(unitOfWork.getCreatedAt())) &&
                                (to == null || to.after(unitOfWork.getCreatedAt()))
                            ){
                                unitsOfWork.put(unitOfWork.getCreatedAt(), unitOfWork);
                            }
                        } catch (RuntimeException e) {
                            if(BasicException.toExceptionStack(e).getExceptionCode() != BasicException.Code.NOT_FOUND) {
                                throw e;
                            }
                        }
                    }
                    return unitsOfWork.values();
                }
                default: 
                    throw BasicException.initHolder(
                        new UnsupportedOperationException(
                            "Persistence modes other than EMBEDDED are not yet supported",
                            BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.NOT_IMPLEMENTED
                            )
                        )
                    );
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
        switch(configuration.getPersistenceMode()) {
            case EMBEDDED: {
                Collection<UnitOfWork> unitsOfWork = new ArrayList<UnitOfWork>();
                Candidate: for(UnitOfWork candidate : getUnitOfWorkInvolvingObject(from, to, touchedObjects)) {
                    for(Modifiable touchedObject : touchedObjects) {
                        Involvement involvement = candidate.getInvolvement(touchedObject.refGetPath().toClassicRepresentation());
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
            }
            default: 
                throw BasicException.initHolder(
                    new UnsupportedOperationException(
                        "Persistence modes other than EMBEDDED are not yet supported",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_IMPLEMENTED
                        )
                    )
                );
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
            switch(configuration.getPersistenceMode()) {
                case EMBEDDED: {
                    Collection<UnitOfWork> unitsOfWork = new ArrayList<UnitOfWork>();
                    Candidate: for(UnitOfWork candidate : getUnitOfWorkInvolvingObject(from, to, modifiable)) {
                        for(Involvement involvement : candidate.<Involvement>getInvolvement()) {
                            if(involvement.getAfterImage() != null) {
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
                }
                default: 
                    throw BasicException.initHolder(
                        new UnsupportedOperationException(
                            "Persistence modes other than EMBEDDED are not yet supported",
                            BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.NOT_IMPLEMENTED
                            )
                        )
                    );
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
        switch(configuration.getPersistenceMode()) {
            case EMBEDDED: {
                throw BasicException.initHolder(
                    new UnsupportedOperationException(
                        "EMBEDDED persistence ignores object creation",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED
                        )
                    )
                );
            }
            default: 
                throw BasicException.initHolder(
                    new UnsupportedOperationException(
                        "Persistence modes other than EMBEDDED are not yet supported",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_IMPLEMENTED
                        )
                    )
                );
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
            switch(configuration.getPersistenceMode()) {
                case EMBEDDED: {
                    throw BasicException.initHolder(
                        new UnsupportedOperationException(
                            "EMBEDDED persistence ignores object creation",
                            BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.NOT_SUPPORTED
                            )
                        )
                    );
                }
                default: 
                    throw BasicException.initHolder(
                        new UnsupportedOperationException(
                            "Persistence modes other than EMBEDDED are not yet supported",
                            BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.NOT_IMPLEMENTED
                            )
                        )
                    );
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
        switch(configuration.getPersistenceMode()) {
            case EMBEDDED: {
                Collection<UnitOfWork> unitsOfWork = new ArrayList<UnitOfWork>();
                Candidate: for(UnitOfWork candidate : getUnitOfWorkInvolvingObject(from, to, removedObjects)) {
                    for(Modifiable touchedObject : removedObjects) {
                        Involvement involvement = candidate.getInvolvement(touchedObject.refGetPath().toClassicRepresentation());
                        if(involvement != null && involvement.getAfterImage() == null) {
                            unitsOfWork.add(candidate);
                            continue Candidate;
                        }
                    }
                }
                return unitsOfWork;
            }
            default: 
                throw BasicException.initHolder(
                    new UnsupportedOperationException(
                        "Persistence modes other than EMBEDDED are not yet supported",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_IMPLEMENTED
                        )
                    )
                );
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
            switch(configuration.getPersistenceMode()) {
                case EMBEDDED: {
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
                }
                default: 
                    throw BasicException.initHolder(
                        new UnsupportedOperationException(
                            "Persistence modes other than EMBEDDED are not yet supported",
                            BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.NOT_IMPLEMENTED
                            )
                        )
                    );
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
     * @param persistenceManager the persistence manager to be used
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
