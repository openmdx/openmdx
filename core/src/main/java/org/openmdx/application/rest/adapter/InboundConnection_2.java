/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: InboundConnection_2 
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
package org.openmdx.application.rest.adapter;

import static org.openmdx.base.accessor.rest.spi.ControlObjects_2.isTransactionCommitIdentifier;
import static org.openmdx.base.accessor.rest.spi.ControlObjects_2.isTransactionObjectIdentifier;

import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.jdo.Constants;
import javax.jdo.FetchGroup;
import javax.jdo.FetchPlan;
import javax.jdo.JDOException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jmi.reflect.InvalidObjectException;
import javax.jmi.reflect.JmiException;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.jmi.reflect.RefStruct;

#if JAVA_8
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.LocalTransactionException;
#else
import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.IndexedRecord;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.InteractionSpec;
import jakarta.resource.cci.LocalTransaction;
import jakarta.resource.cci.MappedRecord;
import jakarta.resource.cci.Record;
import jakarta.resource.spi.EISSystemException;
import jakarta.resource.spi.LocalTransactionException;
#endif

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefStruct_1_0;
import org.openmdx.base.accessor.jmi.spi.ReferenceDef;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.TransactionalSegment;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.cci.Queries;
import org.openmdx.base.persistence.spi.SharedObjects;
import org.openmdx.base.persistence.spi.UnitOfWork;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.ConnectionFactory;
import org.openmdx.base.resource.spi.LocalTransactions;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.FeatureOrderRecord;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.AbstractConnection;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.base.rest.spi.Numbers;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.transaction.Status;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObjects;
import org.w3c.cci2.SparseArray;
import org.w3c.spi2.Datatypes;

/**
 * Inbound Connection
 * 
 * TODO Let's configure
 * <ul>
 * <li>the optimal fetch size</li>
 * <li>a maximal batch size limit (leading to quota exceeded exception when exceeded)</li>
 * </ul>
 */
public class InboundConnection_2 extends AbstractConnection {

    /**
     * Constructor
     * 
     * @param connectionSpec
     *            the JCA connection specification
     * @param persistenceManager
     *            the JDO persistence manager
     * 
     * @throws ResourceException
     */
    public InboundConnection_2(
        ConnectionFactory connectionFactory,
        RestConnectionSpec connectionSpec,
        PersistenceManager persistenceManager
    )
        throws ResourceException {
        super(connectionFactory, connectionSpec);
        this.persistenceManager = persistenceManager;
        this.localTransaction = createLocalTransaction(persistenceManager);
    }

    /**
     * The JDO persistence manager
     */
    private PersistenceManager persistenceManager;

    /**
     * The inbound connection's transaction adapter
     */
    final LocalTransaction localTransaction;

    /**
     * The org::openmdx::base authority id
     */
    protected static final Path BASE_AUTHORITY = new Path("xri://@openmdx*org.openmdx.base");

    /**
     * Used in case of FetchPlan.FETCH_SIZE_OPTIMAL
     */
    protected static final int OPTIMAL_FETCH_SIZE = 64;

    /**
     * No limit (yet) in case of FetchPlan.FETCH_SIZE_GREEDY
     */
    protected static final int BATCH_SIZE_LIMIT = Integer.MAX_VALUE;

    private LocalTransaction createLocalTransaction(
        PersistenceManager persistenceManager
    )
        throws ResourceException {
        return isResourceLocalTransaction(persistenceManager) ? LocalTransactions.getLocalTransaction(persistenceManager)
            : new TransitionalTransactionAdapter();
    }

    protected UnitOfWork currentUnitOfWork() {
        return (UnitOfWork) PersistenceHelper.currentUnitOfWork(getPersistenceManager());
    }

    /**
     * Determines whether a JTA-Transaction or a resource local transaction is used.
     * 
     * @return {@code true} if a resource local transaction is used
     */
    private static boolean isResourceLocalTransaction(
        PersistenceManager persistenceManager
    ) {
        return Constants.RESOURCE_LOCAL.equals(persistenceManager.getPersistenceManagerFactory().getTransactionType());
    }

    /**
     * Retrieve an object by its resource identifier
     * 
     * @param resourceIdentifier
     *            which may be {@code null}
     * 
     * @return the requested object or {@code null} if the resource identifier is {@code null}
     */
    protected RefObject getObjectByResourceIdentifier(
        Object resourceIdentifier
    ) {
        if (resourceIdentifier == null) {
            //
            // Null Object Id
            //
            return null;
        }
        Object objectId = resourceIdentifier;
        if (objectId instanceof String) {
            objectId = new Path((String) objectId);
        }
        if (objectId instanceof Path) {
            Path xri = (Path) objectId;
            if (xri.getLastSegment() instanceof TransactionalSegment) {
                objectId = ((TransactionalSegment) xri.getLastSegment()).getTransactionalObjectId();
            }
        }
        return (RefObject) getPersistenceManager().getObjectById(objectId);
    }

    /**
     * Retrieve an object's XRO
     * <ul>
     * <li>a {@code $t*uuid} XRI in case of a transient object
     * <li>an {@code @openmdx} XRI in case of a persistent object
     * </ul>
     * 
     * @param object
     * @return the object's resource identifier
     */
    protected static Path getResourceIdentifier(
        Object object
    ) {
        return JDOHelper.isPersistent(object) ? (Path) JDOHelper.getObjectId(object)
            : new Path(
                (UUID) JDOHelper.getTransactionalObjectId(object)
            );
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.resource.cci.Connection#close()
     */
    @Override
    public void close()
        throws ResourceException {
        super.close();
        try {
            this.persistenceManager.close();
        } catch (JDOException exception) {
            throw ResourceExceptions.initHolder(
                new EISSystemException(
                    "Connection disposal failure", BasicException.newEmbeddedExceptionStack(
                        exception, BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.DEACTIVATION_FAILURE
                    )
                )
            );
        } finally {
            this.persistenceManager = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.resource.cci.Connection#getLocalTransaction()
     */
    public LocalTransaction getLocalTransaction()
        throws ResourceException {
        assertResourceLocalTransaction();
        return this.localTransaction;
    }

    /**
     * Asserts that local transaction demarcation is supported
     * <p>
     * <em>TODO<br>
     * It will be unnecessary to distinguish the two transaction
     * types once the JTAAdapter delegates the requests to the JTA
     * transaction.
     * </em>
     * 
     * @throws NotSupportedException
     */
    private void assertResourceLocalTransaction()
        throws NotSupportedException {
        if (!isResourceLocalTransaction(this.persistenceManager)) {
            throw new NotSupportedException(
                "Local transaction demarcation is supported if and only if "
                    + "the transaction type is " + Constants.RESOURCE_LOCAL
            );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.resource.cci.Connection#createInteraction()
     */
    public Interaction createInteraction()
        throws ResourceException {
        return new InboundInteraction(this);
    }

    /**
     * Provide the <codePersistenceManager} for the inbound interaction
     * 
     * @return the <codePersistenceManager}
     */
    protected PersistenceManager getPersistenceManager() {
        return this.persistenceManager;
    }

    // ------------------------------------------------------------------------
    // Class TransitionalTransactionAdapter
    // ------------------------------------------------------------------------

    /**
     * Transitional Transaction Adapter for transaction type {@code JCA}
     * <p>
     * <em>TODO<br>
     * This implementation keeps the actual behaviour for the moment.<br>
     * But in the light of the (CDI induced) recent changes it seems more
     * appropriate to forward the transaction control requests to JTA and
     * rely on the call-back for container managed transactions.</em>
     */
    class TransitionalTransactionAdapter implements LocalTransaction {

        /*
         * (non-Javadoc)
         * 
         * @see javax.resource.cci.LocalTransaction#begin()
         */
        @Override
        public void begin()
            throws ResourceException {
            try {
                final UnitOfWork unitOfWork = currentUnitOfWork();
                if (!unitOfWork.isActive()) {
                    unitOfWork.begin();
                }
            } catch (JDOException exception) {
                throw ResourceExceptions.toResourceException(exception);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.resource.cci.LocalTransaction#commit()
         */
        @Override
        public void commit()
            throws ResourceException {
            try {
                currentUnitOfWork().beforeCompletion();
            } catch (JDOException exception) {
                throw ResourceExceptions.toResourceException(exception);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.resource.cci.LocalTransaction#rollback()
         */
        @Override
        public void rollback()
            throws ResourceException {
            try {
                currentUnitOfWork().afterCompletion(Status.STATUS_ROLLEDBACK);
            } catch (JDOException exception) {
                throw ResourceExceptions.toResourceException(exception);
            }
        }

    }

    // ------------------------------------------------------------------------
    // Class InboundInteraction
    // ------------------------------------------------------------------------

    /**
     * Inbound Interaction
     */
    class InboundInteraction extends AbstractRestInteraction {

        /**
         * Constructor
         *
         * @param connection
         *            the REST connection
         */
        protected InboundInteraction(
            RestConnection connection
        ) {
            super(connection);
        }

        /**
         * The MOF repository accessor
         */
        protected final Model_1_0 model = Model_1Factory.getModel();

        /**
         * Test the transaction state and id
         * 
         * @param path
         * @param existence
         * 
         * @throws ResourceException
         */
        private void validateTransactionStateAndId(
            Path path,
            boolean existence
        )
            throws ResourceException {
            boolean active = currentUnitOfWork().isActive();
            if (active != existence) {
                throw ResourceExceptions.initHolder(
                    new LocalTransactionException(
                        "Invalid transaction state", BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.ILLEGAL_STATE, new BasicException.Parameter(
                                "expected", existence ? "active" : "not active"
                            ), new BasicException.Parameter(
                                "actual", active ? "active" : "not active"
                            )
                        )
                    )
                );
            }
            if (path.size() > 2 && existence) {
                String requestedId = path.getSegment(2).toClassicRepresentation();
                String actualId = SharedObjects.getUnitOfWorkIdentifier(getPersistenceManager());
                if (!requestedId.equals(actualId)) {
                    throw ResourceExceptions.initHolder(
                        new LocalTransactionException(
                            "Invalid transaction id", BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.BAD_PARAMETER, new BasicException.Parameter(
                                    "requested", requestedId
                                ), new BasicException.Parameter("actual", actualId)
                            )
                        )
                    );
                }
            }
        }

        private Path getTransactionId(
            Path path
        ) {
            String actualId = SharedObjects.getUnitOfWorkIdentifier(getPersistenceManager());
            return actualId == null ? null
                : path.size() == 2 ? path.getChild(actualId)
                    : path.getSegment(
                        2
                    ).toClassicRepresentation().equals(actualId) ? path.getPrefix(3) : null;
        }

        /**
         * Convert a {@code RefStruct}'s type name to a {@code MappedRecord} record name
         * 
         * @param refValue
         *            the {@code RefStruct}
         * 
         * @return to its {@code MappedRecord} record name
         */
        private String jcaRecordName(
            RefStruct refValue
        ) {
            if (refValue instanceof RefStruct_1_0) {
                return ((RefStruct_1_0) refValue).refDelegate().getRecordName();
            } else {
                StringBuilder recordName = new StringBuilder();
                for (Object component : refValue.refTypeName()) {
                    recordName.append(':').append(component);
                }
                return recordName.substring(1);
            }
        }

        /**
         * Guarded iteration
         * 
         * @param type
         *            the result record type
         * @param source
         *            the JMI collection
         * 
         * @return the next JCA value
         * 
         * @throws ServiceException
         * @throws ResourceException
         */
        @SuppressWarnings("unchecked")
        private IndexedRecord toJcaValue(
            Multiplicity type,
            Collection<?> source
        )
            throws ServiceException,
            ResourceException {
            IndexedRecord target = Records.getRecordFactory().createIndexedRecord(type.toString());
            for (Iterator<?> i = source.iterator(); i.hasNext();) {
                try {
                    target.add(toJcaValue(i.next()));
                } catch (InvalidObjectException exception) {
                    target.add(toJcaValue(exception));
                } catch (RuntimeException exception) {
                    throw new ServiceException(exception);
                }
            }
            return target;
        }

        /**
         * Guarded iteration
         * 
         * @param type
         *            the result record type
         * @param source
         *            the JMI map
         * 
         * @return the next JCA value
         * 
         * @throws ServiceException
         * @throws ResourceException
         */
        @SuppressWarnings("unchecked")
        private MappedRecord toJcaValue(
            Multiplicity type,
            Map<?, ?> source
        )
            throws ServiceException,
            ResourceException {
            MappedRecord target = Records.getRecordFactory().createMappedRecord(type.code());
            for (Iterator<?> i = source.keySet().iterator(); i.hasNext();) {
                try {
                    Object key = i.next();
                    try {
                        target.put(key, toJcaValue(source.get(key)));
                    } catch (InvalidObjectException exception) {
                        target.put(key, toJcaValue(exception));
                    }
                } catch (RuntimeException exception) {
                    throw new ServiceException(exception);
                }
            }
            return target;
        }

        /**
         * Guarded iteration
         * 
         * @param type
         *            the result record type
         * @param source
         *            the JMI structure
         * 
         * @return the next JCA value
         * 
         * @throws ServiceException
         * @throws ResourceException
         */
        @SuppressWarnings("unchecked")
        private MappedRecord toJcaValue(
            String type,
            RefStruct source
        )
            throws ServiceException,
            ResourceException {
            MappedRecord target = Records.getRecordFactory().createMappedRecord(type);
            for (Iterator<?> i = source.refFieldNames().iterator(); i.hasNext();) {
                try {
                    String fieldName = (String) i.next();
                    try {
                        target.put(fieldName, toJcaValue(source.refGetValue(fieldName)));
                    } catch (InvalidObjectException exception) {
                        target.put(fieldName, toJcaValue(exception));
                    }
                } catch (RuntimeException exception) {
                    throw new ServiceException(exception);
                }
            }
            return target;
        }

        /**
         * Retrieve an invalid object's id
         * 
         * @param source
         *            the invalid object exception
         * 
         * @return the invalid object's id
         */
        private Path toJcaValue(
            InvalidObjectException source
        ) {
            return new Path(source.getElementInError().refMofId());
        }

        /**
         * Guarded feature retrieval
         * 
         * @param source
         * @param feature
         * 
         * @return the requested feature
         * 
         * @throws ServiceException
         */
        private Object getJcaValue(
            RefObject source,
            ModelElement_1_0 featureDef
        )
            throws ServiceException,
            ResourceException {
            try {
                Model_1_0 model = featureDef.getModel();
                String featureName = featureDef.getName();
                if (featureDef.isReferenceType() && model.referenceIsStoredAsAttribute(featureDef) && !ModelHelper.isDerived(featureDef)) {
                    return this.toJcaValue(PersistenceHelper.getFeatureReplacingObjectById(source, featureName));
                } else {
                    return this.toJcaValue(source.refGetValue(featureName));
                }
            } catch (InvalidObjectException exception) {
                return this.toJcaValue(exception);
            } catch (RuntimeException exception) {
                throw new ServiceException(exception);
            }
        }

        /**
         * Convert a {@code RefObject} value to a {@code MappedRecord} value
         * 
         * @param refValue
         *            the {@code RefObject} value
         * 
         * @return its {@code MappedRecord} value representation
         * 
         * @throws ResourceException
         * @throws ServiceException
         */
        private Object toJcaValue(
            Object refValue
        )
            throws ResourceException,
            ServiceException {
            if (refValue instanceof RefObject) {
                return getResourceIdentifier(refValue);
            } else if (refValue instanceof Set) {
                return this.toJcaValue(Multiplicity.SET, (Set<?>) refValue);
            } else if (refValue instanceof List) {
                return this.toJcaValue(Multiplicity.LIST, (List<?>) refValue);
            } else if (refValue instanceof SparseArray) {
                return this.toJcaValue(Multiplicity.SPARSEARRAY, (SparseArray<?>) refValue);
            } else if (refValue instanceof RefStruct) {
                RefStruct refStruct = (RefStruct) refValue;
                return this.toJcaValue(jcaRecordName(refStruct), refStruct);
            } else {
                return refValue;
            }
        }

        /**
         * Convert a {@code MappedRecord} value to a {@code RefObject} value
         * 
         * @param jcaValue
         *            the JCA value
         * @param featureDef
         * 
         * @return the JMI value
         * 
         * @throws ResourceException
         */
        private Object toRefValue(
            Object jcaValue,
            ModelElement_1_0 featureDef
        )
            throws ResourceException {
            try {
                ModelElement_1_0 featureType = this.model.getDereferencedType(featureDef.getType());
                if (ModelHelper.getMultiplicity(featureDef) == Multiplicity.STREAM) {
                    return jcaValue instanceof char[] ? CharacterLargeObjects.valueOf((char[]) jcaValue)
                        : jcaValue instanceof byte[]
                            ? BinaryLargeObjects.valueOf((byte[]) jcaValue)
                            : jcaValue instanceof Reader ? CharacterLargeObjects.valueOf(
                                (Reader) jcaValue
                            )
                                : jcaValue instanceof InputStream ? BinaryLargeObjects.valueOf(
                                    (InputStream) jcaValue
                                ) : jcaValue;
                } else if (jcaValue instanceof String && PrimitiveTypes.DATETIME.equals(featureType.getQualifiedName())) {
                    return Datatypes.create(Datatypes.DATE_TIME_CLASS, (String) jcaValue);
                } else if (jcaValue instanceof String && PrimitiveTypes.DATE.equals(featureType.getQualifiedName())) {
                    return Datatypes.create(Datatypes.DATE_CLASS, (String) jcaValue);
                } else if (jcaValue instanceof String && PrimitiveTypes.DURATION.equals(featureType.getQualifiedName())) {
                    return Datatypes.create(Datatypes.DURATION_CLASS, (String) jcaValue);
                } else if (jcaValue instanceof String && PrimitiveTypes.DURATION_DAYTIME.equals(featureType.getQualifiedName())) {
                    return Datatypes.create(Datatypes.DURATION_DAYTIME_CLASS, (String) jcaValue);
                } else if (jcaValue instanceof String && PrimitiveTypes.DURATION_YEARMONTH.equals(featureType.getQualifiedName())) {
                    return Datatypes.create(Datatypes.DURATION_YEARMONTH_CLASS, (String) jcaValue);
                } else {
                    return featureDef.getModel().isReferenceType(featureDef) ? getObjectByResourceIdentifier(jcaValue) : jcaValue;
                }
            } catch (ServiceException exception) {
                throw ResourceExceptions.toResourceException(exception);
            }
        }

        /**
         * Convert a {@code RefObject} to a {@code MappedRecord}
         * 
         * @param object
         *            the {@code RefObject}
         * @param requestedFeatures,
         *            the requested features, maybe {@code null}
         * @param fetchGroups,
         *            the requested getch groups maybe {@code null}
         * 
         * @return its {@code MappedRecord} representation
         * 
         * @throws ResourceException
         */
        @SuppressWarnings("unchecked")
        private MappedRecord toJcaRecord(
            RefObject object,
            Set<String> requestedFeatures,
            Set<String> fetchGroups
        )
            throws ResourceException {
            try {
                RefObject_1_0 refObject = (RefObject_1_0) object;
                ObjectRecord reply = newObject(getResourceIdentifier(object));
                reply.setVersion((byte[]) JDOHelper.getVersion(refObject));
                MappedRecord jcaValue = Records.getRecordFactory().createMappedRecord(refObject.refClass().refMofId());
                reply.setValue(jcaValue);
                Map<String, ModelElement_1_0> features = this.model.getAttributeDefs(
                    this.model.getElement(refObject.refClass().refMofId()), false, true
                );
                if (requestedFeatures != null && !requestedFeatures.isEmpty() && !features.keySet().containsAll(requestedFeatures)) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN, 
                        BasicException.Code.ASSERTION_FAILURE, 
                        "requested features not acceptable",
                        new BasicException.Parameter("object", refObject), new BasicException.Parameter(
                            "requested", requestedFeatures
                        ), 
                        new BasicException.Parameter(
                            "acceptable", features.keySet()
                        )
                    );
                }
                boolean fetchAll = JDOHelper.isPersistent(refObject) &&
                    !JDOHelper.isNew(refObject) &&
                    (fetchGroups == null || fetchGroups.contains(FetchGroup.ALL));
                for (ModelElement_1_0 feature : features.values()) {
                    final String featureName = feature.getName();
                    if (fetchAll || isRquestedFeature(requestedFeatures, featureName) || isLoadedFeature(refObject, featureName)) {
                        try {
                            jcaValue.put(featureName, this.getJcaValue(refObject, feature));
                        } catch (RuntimeException exception) {
                            new ServiceException(
                                exception, 
                                BasicException.Code.DEFAULT_DOMAIN, 
                                BasicException.Code.TRANSFORMATION_FAILURE,
                                "Unable to retrieve feature value", 
                                new BasicException.Parameter(BasicException.Parameter.XRI, refObject.refMofId()), 
                                new BasicException.Parameter("feature", featureName)
                            ).log();
                        }
                    }
                }
                return reply;
            } catch (ServiceException exception) {
                throw ResourceExceptions.toResourceException(exception);
            }
        }

        private boolean isLoadedFeature(
            RefObject_1_0 refObject,
            final String featureName
        ) {
            return ((RefPackage_1_0) refObject.refOutermostPackage()).refPersistenceManager().isLoaded(
                (UUID) JDOHelper.getTransactionalObjectId(refObject),
                featureName
            );
        }

        private boolean isRquestedFeature(
            Set<String> requestedFeatures,
            final String featureName
        ) {
            return (requestedFeatures != null && requestedFeatures.contains(featureName)) ||
                SystemAttributes.OBJECT_IDENTITY.equals(featureName);
        }

        /**
         * Create a query object.
         * <ul>
         * <li>TODO take explicitly requested features into consideration
         * <li>TODO take extensions into consideration
         * </ul>
         * 
         * @param input the Query
         * 
         * @return a new query object
         * 
         * @throws ResourceException
         */
        private Query toRefQuery(
            QueryRecord input
        )
            throws ResourceException {
            Query query = getPersistenceManager().newQuery(Queries.QUERY_LANGUAGE, input);
            //
            // Fetch Plan
            //
            query.getFetchPlan().setGroups(toFetchGroups(input));
            //
            // Fetch Size
            //
            Long fetchSize = input.getSize();
            if (fetchSize != null) {
                query.getFetchPlan().setFetchSize(fetchSize.intValue());
            }
            //
            // TODO Extension
            //
            // for(Map.Entry<String, ?> extension : input.getExtensions().entrySet()) {
            // query.addExtension(extension.getKey(), extension.getValue());
            // }
            //
            return query;
        }

        private Set<String> toFetchGroups(QueryRecord input) {
            final String fetchGroupName = input.getFetchGroupName();
            return fetchGroupName == null ? Collections.emptySet() : Collections.singleton(fetchGroupName);
        }

        @SuppressWarnings("unchecked")
        private void toRefObject(
            UUID transactionalObjectId,
            Path objectId,
            RefObject refTarget,
            MappedRecord jcaSource
        ) throws ResourceException {
            try {
                ModelElement_1_0 classDef = this.model.getElement(refTarget.refClass().refMofId());
                for (Object rawObjectEntry : jcaSource.entrySet()) {
                    Map.Entry<?, ?> objectEntry = (Entry<?, ?>) rawObjectEntry;
                    String featureName = objectEntry.getKey().toString();
                    Object rawValue = objectEntry.getValue();
                    ModelElement_1_0 featureDef = this.model.getFeatureDef(classDef, featureName, false);
                    if (featureDef == null) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.BAD_MEMBER_NAME, "Unknown feature",
                            new BasicException.Parameter(
                                BasicException.Parameter.XRI, objectId
                            ), new BasicException.Parameter("uuid", transactionalObjectId), new BasicException.Parameter(
                                "class", refTarget.refClass().refMofId()
                            ), new BasicException.Parameter("feature", featureName)
                        );
                    }
                    featureName = featureDef.getName();
                    final Boolean isChangeable = featureDef.isChangeable();
                    final Boolean isDerived = featureDef.isDerived();
                    if (Boolean.TRUE.equals(isChangeable) && !Boolean.TRUE.equals(isDerived)) {
                        switch (ModelHelper.getMultiplicity(featureDef)) {
                            case LIST:
                            case SET: {
                                @SuppressWarnings("rawtypes")
                                Collection target = (Collection) refTarget.refGetValue(featureName);
                                target.clear();
                                Collection<?> source = rawValue == null ? Collections.EMPTY_LIST
                                    : rawValue instanceof List
                                        ? (List<?>) rawValue
                                        : Collections.singletonList(rawValue);
                                for (Object v : source) {
                                    target.add(this.toRefValue(v, featureDef));
                                }
                            }
                                break;
                            case SPARSEARRAY: {
                                @SuppressWarnings("rawtypes")
                                SparseArray target = (SparseArray) refTarget.refGetValue(featureName);
                                target.clear();
                                if (rawValue != null) {
                                    if (rawValue instanceof MappedRecord) {
                                        Map<?, ?> source = (MappedRecord) rawValue;
                                        for (Map.Entry<?, ?> e : source.entrySet()) {
                                            target.put(e.getKey(), this.toRefValue(e.getValue(), featureDef));
                                        }
                                    } else if (rawValue instanceof SparseArray<?>) {
                                        SparseArray<?> source = (SparseArray<?>) rawValue;
                                        for (ListIterator<?> i = source.populationIterator(); i.hasNext();) {
                                            target.put(Integer.valueOf(i.nextIndex()), this.toRefValue(i.next(), featureDef));
                                        }
                                    } else {
                                        target.put(Integer.valueOf(0), this.toRefValue(rawValue, featureDef));
                                    }
                                }
                            }
                                break;
                            default: {
                                refTarget.refSetValue(featureName, this.toRefValue(rawValue, featureDef));
                            }
                        }
                    }
                }
            } catch (ServiceException exception) {
                throw ResourceExceptions.toResourceException(exception);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record,
         * javax.resource.cci.Record)
         */
        @Override
        public boolean execute(
            InteractionSpec ispec,
            Record input,
            Record output
        )
            throws ResourceException {
            try {
                return super.execute(ispec, input, output);
            } catch (JDOException|JmiException exception) {
                throw ResourceExceptions.toResourceException(exception);
            }
        }

        /**
         * Propagate the {@code RefObject} to indexed {@code IndexedRecord{@code 
         * 
         * &#64;param refObject
         * &#64;param output
         * @param requestedFeatures the requested features, may be {@code null}
         * 
         * @param fetchGroups
         *            the requested fetch groups, may be {@code null}
         * 
         * @return {@code true}
         * 
         * @throws ResourceException
         */
        @SuppressWarnings("unchecked")
        private boolean propagate(
            RefObject refObject,
            IndexedRecord output,
            Set<String> requestedFeatures,
            Set<String> fetchGroups
        )
            throws ResourceException {
            if (output != null)
                output.add(this.toJcaRecord(refObject, requestedFeatures, fetchGroups));
            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#get(org.openmdx.base.resource.spi.RestInteractionSpec,
         * org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean get(
            RestInteractionSpec ispec,
            QueryRecord input,
            ResultRecord output
        )
            throws ResourceException {
            Path xri = input.getResourceIdentifier();
            if (isTransactionObjectIdentifier(xri)) {
                Path transactionId = getTransactionId(xri);
                if (transactionId != null) {
                    if (output == null) {
                        return false;
                    } else {
                        output.add(Object_2Facade.newInstance(transactionId, "org:openmdx:kernel:UnitOfWork").getDelegate());
                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                RefObject refObject = getObjectByResourceIdentifier(xri);
                if (input.isRefresh()) {
                    getPersistenceManager().refresh(refObject);
                }
                if (output == null) {
                    return true;
                } else {
                    Set<String> features = input.getFeatureName();
                    final QueryFilterRecord queryFilter = input.getQueryFilter();
                    if (queryFilter != null) {
                        features = features == null ? new HashSet<String>() : new HashSet<String>(features);
                        for (FeatureOrderRecord orderSpecifier : queryFilter.getOrderSpecifier()) {
                            features.add(orderSpecifier.featureName());
                        }
                    }
                    return propagate(refObject, output, features, toFetchGroups(input));
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#create(org.openmdx.base.resource.spi.RestInteractionSpec,
         * org.openmdx.base.rest.spi.ObjectHolder_2Facade, javax.resource.cci.IndexedRecord)
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean create(
            RestInteractionSpec ispec,
            ObjectRecord input,
            ResultRecord output
        )
            throws ResourceException {
            Path xri = input.getResourceIdentifier();
            if (isTransactionObjectIdentifier(xri)) {
                validateTransactionStateAndId(xri, false);
                localTransaction.begin();
                if (output != null) {
                    output.add(Object_2Facade.newInstance(getTransactionId(xri), "org:openmdx:kernel:UnitOfWork").getDelegate());
                }
                return true;
            } else if (xri.isTransactionalObjectId()) {
                RefPackage refPackage = getObjectByResourceIdentifier(BASE_AUTHORITY).refOutermostPackage();
                RefObject_1_0 newObject = (RefObject_1_0) refPackage.refClass(
                    input.getValue().getRecordName()
                ).refCreateInstance(Collections.singletonList(xri));
                this.toRefObject(input.getTransientObjectId(), xri, newObject, input.getValue());
                return propagate(newObject, output, null, null);
            } else {
                boolean newId = xri.size() % 2 == 0;
                int featurePosition = xri.size() - (newId ? 1 : 2);
                RefObject refParent = getObjectByResourceIdentifier(xri.getPrefix(featurePosition));
                RefObject_1_0 refObject = (RefObject_1_0) refParent.refOutermostPackage().refClass(
                    input.getValue().getRecordName()
                ).refCreateInstance(null);
                this.toRefObject(input.getTransientObjectId(), xri, refObject, input.getValue());
                Object container = refParent.refGetValue(xri.getSegment(featurePosition).toClassicRepresentation());
                if (newId) {
                    @SuppressWarnings("rawtypes")
                    Collection refContainer = (Collection) container;
                    refContainer.add(refObject);
                } else {
                    RefContainer<RefObject> refContainer = (RefContainer<RefObject>) container;
                    String qualifier = xri.getLastSegment().toClassicRepresentation();
                    final Class<? extends RefContainer> containerClass = refContainer.getClass();
                    final Class<?>[] argumentClasses = ReferenceDef.getAddArguments(containerClass);
                    if (argumentClasses.length == 3) {
                        #if CLASSIC_CHRONO_TYPES
                            refContainer.refAdd(toAddArguments(argumentClasses, qualifier, refObject));
                        #else
                            boolean persistent = qualifier.startsWith("!");
                            QualifierType qualifierType = QualifierType.valueOf(persistent);
                            Object qualifierValue = persistent ? qualifier.substring(1) : qualifier;
                            refContainer.refAdd(qualifierType, qualifierValue, refObject);
                        #endif
                    } else {
                        throw ResourceExceptions.initHolder(
                                new NotSupportedException(
                                        "More than one qualifier is not yet supported",
                                        BasicException.newEmbeddedExceptionStack(
                                                BasicException.Code.DEFAULT_DOMAIN,
                                                BasicException.Code.NOT_IMPLEMENTED,
                                                new BasicException.Parameter(
                                                        "argumentClasses", (Object[]) argumentClasses
                                                )
                                        )
                                )
                        );
                    }
                }
                return propagate(refObject, output, null, null);
            }
        }

        /**
         * Provide the {@code add()} argument list
         *
         * @param argumentClasses
         * @param qualifier
         * @param object
         * 
         * @return the {@code add()} argument list
         * @throws ServiceException
         */
        @SuppressWarnings("rawtypes")
        private Object[] toAddArguments(
                Class<?>[] argumentClasses,
                String qualifier,
                RefObject object
        ) {
                boolean persistent = qualifier.startsWith("!");
                return new Object[] { QualifierType.valueOf(
                    persistent
                ), Datatypes.create(argumentClasses[1], persistent ? qualifier.substring(1) : qualifier), object };
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#move(org.openmdx.base.resource.spi.RestInteractionSpec,
         * org.openmdx.base.naming.Path, org.openmdx.base.rest.spi.Object_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean move(
            RestInteractionSpec ispec,
            ObjectRecord input,
            ResultRecord output
        )
            throws ResourceException {
            RefObject_1_0 newObject = (RefObject_1_0) getObjectByResourceIdentifier(input.getTransientObjectId());
            this.toRefObject(input.getTransientObjectId(), input.getResourceIdentifier(), newObject, input.getValue());
            Path newResourceIdentifier = input.getResourceIdentifier();
            int featurePosition = newResourceIdentifier.size() - 2;
            RefObject refObject = getObjectByResourceIdentifier(newResourceIdentifier.getPrefix(featurePosition));
            RefContainer<RefObject> refContainer = (RefContainer<RefObject>) refObject.refGetValue(
                newResourceIdentifier.getSegment(featurePosition).toClassicRepresentation()
            );
            String qualifier = newResourceIdentifier.getLastSegment().toClassicRepresentation();
            boolean persistent = qualifier.startsWith("!");
            refContainer.refAdd(QualifierType.valueOf(persistent), persistent ? qualifier.substring(1) : qualifier, newObject);
            return propagate(newObject, output, null, null);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#delete(org.openmdx.base.resource.spi.RestInteractionSpec,
         * org.openmdx.base.rest.spi.ObjectHolder_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean delete(
            RestInteractionSpec ispec,
            ObjectRecord input
        )
            throws ResourceException {
            Path xri = input.getResourceIdentifier();
            if (isTransactionObjectIdentifier(xri)) {
                validateTransactionStateAndId(xri, true);
                localTransaction.rollback();
            } else {
                getObjectByResourceIdentifier(xri).refDelete();
            }
            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#put(org.openmdx.base.resource.spi.RestInteractionSpec,
         * org.openmdx.base.rest.spi.ObjectHolder_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean update(
            RestInteractionSpec ispec,
            ObjectRecord input,
            ResultRecord output
        )
            throws ResourceException {
            final Path xri = input.getResourceIdentifier();
            final UUID transientObjectId = input.getTransientObjectId();
            RefObject refObject = getObjectByResourceIdentifier(transientObjectId == null ? xri : transientObjectId);
            this.toRefObject(transientObjectId, xri, refObject, input.getValue());
            return propagate(refObject, output, null, null);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#find(org.openmdx.base.resource.spi.RestInteractionSpec,
         * org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean find(
            RestInteractionSpec ispec,
            QueryRecord input,
            ResultRecord output
        )
            throws ResourceException {
            Query query = this.toRefQuery(input);
            List<RefObject> objects = (List<RefObject>) query.execute();
            if (output != null) {
                final Long size = input.getSize();
                final int batchSize = getBatchSize(size);
                final int position = Numbers.getValue(input.getPosition(), 0);
                if (position >= 0) {
                    ListIterator<RefObject> i = objects.listIterator(position);
                    int count = 0;
                    while (i.hasNext() && count < batchSize) {
                        output.add(this.toJcaRecord(i.next(), input.getFeatureName(), Collections.singleton(input.getFetchGroupName())));
                        count++;
                    }
                    boolean hasMore = i.hasNext();
                    output.setHasMore(hasMore);
                    if (!hasMore) {
                        output.setTotal(position + count);
                    }
                } else {
                    ListIterator<RefObject> i = objects.listIterator(-position);
                    for (int count = 0; i.hasPrevious() && count < batchSize; count++) {
                        output.add(
                            0, this.toJcaRecord(i.previous(), input.getFeatureName(), Collections.singleton(input.getFetchGroupName()))
                        );
                    }
                }
            }
            return true;
        }

        private int getBatchSize(
            final Long size
        ) {
            final int fetchSize = Numbers.getValue(size, FetchPlan.FETCH_SIZE_OPTIMAL);
            return fetchSize == FetchPlan.FETCH_SIZE_GREEDY ? BATCH_SIZE_LIMIT
                : fetchSize == FetchPlan.FETCH_SIZE_OPTIMAL
                    ? OPTIMAL_FETCH_SIZE
                    : fetchSize;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#delete(org.openmdx.base.resource.spi.RestInteractionSpec,
         * org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean delete(
            RestInteractionSpec ispec,
            QueryRecord input
        )
            throws ResourceException {
            Path xri = input.getResourceIdentifier();
            if (xri.size() % 2 == 0 || xri.isPattern()) {
                try {
                    Query query = this.toRefQuery(input);
                    return query.deletePersistentAll() > 0;
                } catch (JDOException exception) {
                    throw ResourceExceptions.toResourceException(exception);
                }
            } else if (isTransactionObjectIdentifier(xri)) {
                validateTransactionStateAndId(xri, true);
                localTransaction.rollback();
                return true;
            } else {
                try {
                    getObjectByResourceIdentifier(xri).refDelete();
                    return true;
                } catch (JDOException exception) {
                    //
                    // Retrieval Failure
                    //
                    return false;
                } catch (JmiException exception) {
                    //
                    // Removal Failure
                    //
                    throw ResourceExceptions.toResourceException(exception);
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#invoke(org.openmdx.base.resource.spi.RestInteractionSpec,
         * javax.resource.cci.MessageRecord, javax.resource.cci.MessageRecord)
         */
        @Override
        public boolean invoke(
            RestInteractionSpec ispec,
            MessageRecord input,
            MessageRecord output
        )
            throws ResourceException {
            try {
                Path xri = input.getResourceIdentifier();
                if (isTransactionCommitIdentifier(xri)) {
                    validateTransactionStateAndId(xri, true);
                    localTransaction.commit();
                    if (output != null) {
                        output.setResourceIdentifier(newResponseId(xri));
                        output.setBody(null);
                    }
                } else {
                    final int featurePosition = xri.size() - (xri.isObjectPath() ? 2 : 1);
                    RefObject refObject = getObjectByResourceIdentifier(xri.getPrefix(featurePosition));
                    RefPackage_1_0 refPackage = (RefPackage_1_0) refObject.refOutermostPackage();
                    MappedRecord arguments = input.getBody();
                    Object reply = refObject.refInvokeOperation(
                        xri.getSegment(
                            featurePosition
                        ).toClassicRepresentation(), Collections.singletonList(refPackage.refCreateStruct(arguments))
                    );
                    if (output != null) {
                        output.setResourceIdentifier(xri);
                        output.setBody(
                            reply instanceof RefStruct_1_0 ? (MappedRecord) ((RefStruct_1_0) reply).refDelegate() : (MappedRecord) reply
                        );
                    }
                }
                return true;
            } catch (RefException exception) {
                throw ResourceExceptions.toResourceException(exception);
            }
        }

    }

}
