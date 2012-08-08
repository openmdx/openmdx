/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: InboundConnection_2.java,v 1.54 2010/08/09 13:13:21 hburger Exp $
 * Description: InboundConnection_2 
 * Revision:    $Revision: 1.54 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/08/09 13:13:21 $
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
package org.openmdx.base.accessor.rest;

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
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import javax.jdo.Constants;
import javax.jdo.JDOException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jmi.reflect.InvalidObjectException;
import javax.jmi.reflect.JmiException;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.jmi.reflect.RefStruct;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.xml.datatype.XMLGregorianCalendar;

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefStruct_1_0;
import org.openmdx.base.accessor.jmi.spi.Jmi1ContainerInvocationHandler;
import org.openmdx.base.accessor.rest.spi.Synchronization_2_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.spi.ModelUtils;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.cci.Queries;
import org.openmdx.base.persistence.spi.SharedObjects;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.LocalTransactions;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.AbstractConnection;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.base.text.conversion.JavaBeans;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObjects;
import org.w3c.cci2.SparseArray;
import org.w3c.spi2.Datatypes;

/**
 * Inbound Connection
 */
public class InboundConnection_2 
    extends AbstractConnection
    implements Synchronization_2_0 
{

    /**
     * Constructor 
     * 
     * @param connectionSpec 
     * @param persistenceManager
     * 
     * @throws ResourceException 
     */
    public InboundConnection_2(
        RestConnectionSpec connectionSpec, 
        PersistenceManager persistenceManager
    ) throws ResourceException{
        super(connectionSpec);
        this.persistenceManager = persistenceManager;
        this.localTransaction = Constants.RESOURCE_LOCAL.equals(
            persistenceManager.getPersistenceManagerFactory().getTransactionType()
        ) ? LocalTransactions.getLocalTransaction(
            persistenceManager
        ) : null;

    }

    /**
     * 
     */
    private PersistenceManager persistenceManager;

    /**
     * The inbound connection's local transaction
     */
    final LocalTransaction localTransaction;

    /**
     * The org::openmdx::base authority id
     */
    protected static final Path BASE_AUTHORITY = new Path("xri://@openmdx*org.openmdx.base");

    /**
     * Retrieve an object by its resource identifier
     * 
     * @param resourceIdentifier which may be <code>null</code>
     * 
     * @return the requested object or <code>null</code> if the resource identifier is <code>null</code>
     */
    protected RefObject getObjectByResourceIdentifier(
        Object resourceIdentifier
    ){
        if(resourceIdentifier == null) {
            //
            // Null Object Id
            //
            return null;
        }
        if(resourceIdentifier instanceof String) {
            resourceIdentifier = new Path((String)resourceIdentifier);
        }
        if(resourceIdentifier instanceof Path){
            Path xri = (Path) resourceIdentifier;
            if(xri.isTransientObjectId()) {
                resourceIdentifier = xri.toUUID();
            }
        }
        return (RefObject) getPersistenceManager().getObjectById(resourceIdentifier);
    }

    /**
     * Retrieve an object's XRO<ul>
     * <li>a <code>$t*uuid</code> XRI in case of a transient object
     * <li>an <code>@openmdx</code> XRI in case of a persistent object
     * </ul>
     * 
     * @param object
     * @return the object's resource identifier
     */
    protected static Path getResourceIdentifier(
        Object object
    ){
        return JDOHelper.isPersistent(object) ? 
            (Path)JDOHelper.getObjectId(object) : 
                new Path((UUID)JDOHelper.getTransactionalObjectId(object));
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#close()
     */
    @Override
    public void close(
    ) throws ResourceException {
        super.close();
        try {
            this.persistenceManager.close();
        } catch (JDOException exception) {
            throw ResourceExceptions.initHolder(
                new ResourceException(
                    "Connection disposal failure",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.DEACTIVATION_FAILURE
                    )
                )
            );
        } finally {
            this.persistenceManager = null;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.Synchronization_2_0#afterBegin()
     */
    public void afterBegin() {
        Transaction transaction = this.persistenceManager.currentTransaction();
        if(!transaction.isActive()) {
            transaction.begin();
        }
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#afterCompletion(int)
     */
    public void afterCompletion(int status) {
        ((Synchronization)this.persistenceManager.currentTransaction()).afterCompletion(status);
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#beforeCompletion()
     */
    public void beforeCompletion() {
        ((Synchronization)this.persistenceManager.currentTransaction()).beforeCompletion();
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#getLocalTransaction()
     */
    public LocalTransaction getLocalTransaction(
    ) throws ResourceException {
        if(this.localTransaction == null) throw new NotSupportedException(
            "Local transaction demarcation is supported " +
            "if and only if the transaction type is " + Constants.RESOURCE_LOCAL
        );
        return this.localTransaction;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#createInteraction()
     */
    public Interaction createInteraction(
    ) throws ResourceException {
        return new InboundInteraction(this);
    }

    /**
     * Provide the <codePersistenceManager</code> for the inbound interaction
     * 
     * @return the <codePersistenceManager</code>
     */
    protected PersistenceManager getPersistenceManager(){
        return this.persistenceManager;
    }


    //------------------------------------------------------------------------
    // Class Inbound Interaction
    //------------------------------------------------------------------------

    /**
     * InboundInteraction
     *
     */
    class InboundInteraction extends AbstractRestInteraction {

        /**
         * Constructor 
         *
         * @param connection
         */
        protected InboundInteraction(Connection connection) {
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
        ) throws ServiceException{
            boolean active = getPersistenceManager().currentTransaction().isActive(); 
            if(active != existence) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE,
                    "Invalid transaction state",
                    new BasicException.Parameter("expected", existence ? "active" : "not active"),
                    new BasicException.Parameter("actual", active ? "active" : "not active")
                );
            }
            if(path.size() > 2 && existence) {
                String requestedId = path.get(2);
                String actualId = SharedObjects.getUnitOfWorkIdentifier(getPersistenceManager());
                if(!requestedId.equals(actualId)) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND,
                        "Invalid transaction id",
                        new BasicException.Parameter("requested", requestedId),
                        new BasicException.Parameter("actual", actualId)
                    );
                }
            }
        }

        private Path getTransactionId(
            Path path
        ){
            String actualId = SharedObjects.getUnitOfWorkIdentifier(getPersistenceManager());
            return 
                actualId == null ? null :
                path.size() == 2 ? path.getChild(actualId) :
                path.get(2).equals(actualId) ? path.getPrefix(3) :
                null;
        }

        /**
         * Convert a <code>RefStruct</code>'s type name to a <code>MappedRecord</code> record name
         * 
         * @param refValue the <code>RefStruct</code>
         * 
         * @return to its <code>MappedRecord</code> record name
         */
        private String jcaRecordName(
            RefStruct refValue
        ){
            if(refValue instanceof RefStruct_1_0){
                return ((RefStruct_1_0)refValue).refDelegate().getRecordName();
            }  else {
                StringBuilder recordName = new StringBuilder();
                for(Object component : refValue.refTypeName()) {
                    recordName.append(':').append(component);
                }
                return recordName.substring(1);
            }
        }

        /**
         * Guarded iteration
         * 
         * @param type the result record type
         * @param source the JMI collection
         * 
         * @return the next JCA value
         * 
         * @throws ServiceException
         * @throws ResourceException
         */
        @SuppressWarnings("unchecked")
        private IndexedRecord toJcaValue(
            String type,
            Collection<?> source
        ) throws ServiceException, ResourceException{
            IndexedRecord target = Records.getRecordFactory().createIndexedRecord(type);
            for(
                Iterator<?> i = source.iterator();
                i.hasNext();
            ){
                try {
                    target.add(toJcaValue(i.next()));
                } catch (InvalidObjectException exception) {
                    target.add(toJcaValue(exception));
                } catch(RuntimeException exception) {
                    throw new ServiceException(exception);
                }
            }
            return target;
        }

        /**
         * Guarded iteration
         * 
         * @param type the result record type
         * @param source the JMI map
         * 
         * @return the next JCA value
         * 
         * @throws ServiceException
         * @throws ResourceException
         */
        @SuppressWarnings("unchecked")
        private MappedRecord toJcaValue(
            String type,
            Map<?,?> source
        ) throws ServiceException, ResourceException{
            MappedRecord target = Records.getRecordFactory().createMappedRecord(type);
            for(
                Iterator<?> i = source.keySet().iterator();
                i.hasNext();
            ){
                try {
                    Object key = i.next();
                    try {
                        target.put(key, toJcaValue(source.get(key)));
                    } catch (InvalidObjectException exception) {
                        target.put(key, toJcaValue(exception));
                    }
                } catch(RuntimeException exception) {
                    throw new ServiceException(exception);
                }
            }
            return target;
        }

        /**
         * Guarded iteration
         * 
         * @param type the result record type
         * @param source the JMI structure
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
        ) throws ServiceException, ResourceException{
            MappedRecord target = Records.getRecordFactory().createMappedRecord(type);
            for(
                Iterator<?> i = source.refFieldNames().iterator();
                i.hasNext();
            ){
                try {
                    String fieldName = (String) i.next();
                    try {
                        target.put(fieldName, toJcaValue(source.refGetValue(fieldName)));
                    } catch (InvalidObjectException exception) {
                        target.put(fieldName, toJcaValue(exception));
                    }
                } catch(RuntimeException exception) {
                    throw new ServiceException(exception);
                }
            }
            return target;
        }

        /**
         * Retrieve an invalid object's id
         * 
         * @param source the invalid object exception
         *  
         * @return the invalid object's id
         */
        private Path toJcaValue(
            InvalidObjectException source
        ){
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
        ) throws ServiceException, ResourceException {
            try {
                Model_1_0 model = featureDef.getModel();
                String featureName = (String)featureDef.objGetValue("name");
                if(
                    featureDef.isReferenceType() &&
                    model.referenceIsStoredAsAttribute(featureDef) && 
                    !ModelUtils.isDerived(featureDef)
                ) {
                    return this.toJcaValue(
                        PersistenceHelper.getFeatureReplacingObjectById(source, featureName)
                    );
                }
                else {
                    return this.toJcaValue(
                        source.refGetValue(featureName)
                    );
                }
            } catch (InvalidObjectException exception) {
                return this.toJcaValue(exception);
            } catch(RuntimeException exception) {
                throw new ServiceException(exception);
            }
        }

        /**
         * Convert a <code>RefObject</code> value to a <code>MappedRecord</code> value
         * 
         * @param refValue the <code>RefObject</code> value
         * 
         * @return its <code>MappedRecord</code> value representation
         * 
         * @throws ResourceException
         * @throws ServiceException  
         */
        @SuppressWarnings("unchecked")
        private Object toJcaValue(
            Object refValue
        ) throws ResourceException, ServiceException {
            if(refValue instanceof RefObject) {
                return getResourceIdentifier(refValue);
            } else if(refValue instanceof Set) {
                return this.toJcaValue(
                    Multiplicities.SET,
                    (Set<?>)refValue
                );
            } else if(refValue instanceof List) {
                return this.toJcaValue(
                    Multiplicities.LIST,
                    (List<?>)refValue
                );
            } else if(refValue instanceof SparseArray) {
                return this.toJcaValue(  
                    Multiplicities.SPARSEARRAY,
                    (SparseArray)refValue
                );
            } else if(refValue instanceof RefStruct){
                RefStruct refStruct = (RefStruct) refValue;
                return this.toJcaValue(  
                    jcaRecordName(refStruct),
                    refStruct
                );
            } else {
                return refValue;
            }
        }

        /**
         * Convert a <code>MappedRecord</code> value to a <code>RefObject</code> value
         * 
         * @param jcaValue the JCA value
         * @param featureDef
         * 
         * @return the JMI value
         * 
         * @throws ResourceException
         */
        private Object toRefValue(
            Object jcaValue,
            ModelElement_1_0 featureDef
        ) throws ResourceException{
            try {
                ModelElement_1_0 featureType = this.model.getDereferencedType(featureDef.objGetValue("type"));
                if(Multiplicities.STREAM.equals(featureDef.objGetValue("multiplicity"))) {
                    return 
                        jcaValue instanceof char[] ? CharacterLargeObjects.valueOf((char[])jcaValue) :
                        jcaValue instanceof byte[] ? BinaryLargeObjects.valueOf((byte[])jcaValue) :
                        jcaValue instanceof Reader ? CharacterLargeObjects.valueOf((Reader)jcaValue) :   
                        jcaValue instanceof InputStream ? BinaryLargeObjects.valueOf((InputStream)jcaValue) : 
                        jcaValue;    
                } else if(
                    jcaValue instanceof String &&
                    PrimitiveTypes.DATETIME.equals(featureType.objGetValue("qualifiedName"))
                ) {
                    return Datatypes.create(
                        java.util.Date.class,
                        (String)jcaValue
                    );
                } else if(
                    jcaValue instanceof String &&
                    PrimitiveTypes.DATE.equals(featureType.objGetValue("qualifiedName"))
                ) {
                    return Datatypes.create(
                        XMLGregorianCalendar.class,
                        (String)jcaValue
                    );
                } else {
                    return featureDef.getModel().isReferenceType(featureDef) ? getObjectByResourceIdentifier(jcaValue) : jcaValue;
                }
            } catch (ServiceException exception) {
                throw ResourceExceptions.initHolder(
                    new ResourceException(
                        exception.getCause().getDescription(),
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            }
        }

        /**
         * Convert a <code>RefObject</code> to a <code>MappedRecord</code>
         * 
         * @param object the <code>RefObject</code>
         * 
         * @return its <code>MappedRecord</code> representation
         * 
         * @throws ResourceException
         */
        @SuppressWarnings("unchecked")
        private MappedRecord toJcaRecord(
            RefObject object,
            Set<String> requestedFeatures
        ) throws ServiceException {
            try{
                RefObject_1_0 refObject = (RefObject_1_0) object;
                Object_2Facade reply = Object_2Facade.newInstance();
                reply.setPath(getResourceIdentifier(object));
                reply.setVersion(JDOHelper.getVersion(refObject));
                MappedRecord jcaValue = Records.getRecordFactory().createMappedRecord(
                    refObject.refClass().refMofId()
                );
                reply.setValue(jcaValue);
                Map<String,ModelElement_1_0> features = this.model.getAttributeDefs(
                    this.model.getElement(refObject.refClass().refMofId()),
                    false,
                    true
                );
                if(
                    requestedFeatures != null && 
                    !requestedFeatures.isEmpty() &&
                    !features.keySet().containsAll(requestedFeatures)
                ) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "requested features not acceptable",
                        new BasicException.Parameter("object", refObject),
                        new BasicException.Parameter("requested", requestedFeatures),
                        new BasicException.Parameter("acceptable", features.keySet())
                    );
                }
                for(ModelElement_1_0 feature : features.values()) {
                    String featureName = (String)feature.objGetValue("name");
                    if(
                        !"context".equals(featureName) && 
                        featureName.indexOf(":") < 0
                    ) {        
                        jcaValue.put(
                            featureName,
                            this.getJcaValue(
                                refObject,
                                feature
                            )
                        );
                    }
                }
                return reply.getDelegate();
            } catch (ResourceException exception) {
                throw new ServiceException(exception);
            }
        }

        /**
         * Create a query object
         * 
         * @param input
         * 
         * @return a new query object
         * 
         * @throws ResourceException
         */
        private Query toRefQuery(
            Query_2Facade input
        ) throws ServiceException {
            Query query = getPersistenceManager().newQuery(
                Queries.QUERY_LANGUAGE, 
                input.getDelegate()
            );
            //
            // TODO Fetch Plan
            //
            //          query.getFetchPlan();
            //
            // TODO Extension
            //
            //          for(Map.Entry<String, ?> extension : input.getExtensions().entrySet()) {
            //              query.addExtension(extension.getKey(), extension.getValue());
            //          }
            return query;
        }

        @SuppressWarnings("unchecked")
        private void toRefObject(
            Path resourceId,
            RefObject refTarget,
            MappedRecord jcaSource
        ) throws ServiceException {
            try {
                ModelElement_1_0 classDef = this.model.getElement(refTarget.refClass().refMofId());
                for(Object rawObjectEntry : jcaSource.entrySet()) {
                    Map.Entry<?, ?> objectEntry = (Entry<?, ?>) rawObjectEntry;
                    String featureName = objectEntry.getKey().toString();
                    Object rawValue = objectEntry.getValue();
                    ModelElement_1_0 featureDef = this.model.getFeatureDef(classDef, featureName, false);
                    if(featureDef == null) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_MEMBER_NAME,
                            "Unknown feature",
                            new BasicException.Parameter("xri", resourceId.toXRI()),
                            new BasicException.Parameter("class", refTarget.refClass().refMofId()),
                            new BasicException.Parameter("feature", featureName)
                        );
                    }
                    featureName = (String) featureDef.objGetValue("name");
                    Object multiplicity = ModelUtils.getMultiplicity(featureDef);
                    Boolean isChangeable = (Boolean)featureDef.objGetValue("isChangeable");                
                    Boolean isDerived = (Boolean)featureDef.objGetValue("isDerived");
                    if(isChangeable != null && isChangeable && (isDerived == null || !isDerived)) {
                        if(
                            Multiplicities.LIST.equals(multiplicity) ||
                            Multiplicities.SET.equals(multiplicity)
                        ) {
                            Collection target = (Collection) refTarget.refGetValue(featureName);
                            target.clear();
                            Collection<?> source = 
                                rawValue == null ? Collections.EMPTY_LIST :
                                    rawValue instanceof List ? (List<?>)rawValue : 
                                        Collections.singletonList(rawValue);
                                    for(Object v : source) {
                                        target.add(
                                            this.toRefValue(
                                                v,
                                                featureDef
                                            )
                                        );
                                    }
                        } 
                        else if (Multiplicities.SPARSEARRAY.equals(multiplicity)) {
                            SparseArray target = (SparseArray) refTarget.refGetValue(featureName);
                            target.clear();
                            if(rawValue != null) {
                                if(rawValue instanceof MappedRecord) {
                                    Map<?,?> source = (MappedRecord) rawValue;
                                    for(Map.Entry<?,?> e : source.entrySet()) {
                                        target.put(
                                            e.getKey(),
                                            this.toRefValue(
                                                e.getValue(), 
                                                featureDef
                                            )
                                        );
                                    }
                                } else if (rawValue instanceof SparseArray<?>){
                                    SparseArray<?> source = (SparseArray<?>)rawValue;
                                    for(
                                        ListIterator<?> i = source.populationIterator();
                                        i.hasNext();
                                    ){
                                        target.put(
                                            Integer.valueOf(i.nextIndex()),
                                            this.toRefValue(
                                                i.next(), 
                                                featureDef
                                            )
                                        );
                                    }
                                } else {
                                    target.put(
                                        Integer.valueOf(0),
                                        this.toRefValue(
                                            rawValue, 
                                            featureDef
                                        )
                                    );
                                }
                            }
                        } else {
                            refTarget.refSetValue(
                                featureName, 
                                this.toRefValue(
                                    rawValue, 
                                    featureDef
                                )
                            );
                        }
                    }
                }
            } catch (ResourceException exception) {
                throw new ServiceException(exception);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
         */
        @Override
        public boolean execute(
            InteractionSpec ispec,
            Record input,
            Record output
        ) throws ResourceException {
            try {
                return super.execute(ispec, input, output);
            } catch (JDOException  exception) {
                BasicException cause = BasicException.newEmbeddedExceptionStack(exception);
                throw ResourceExceptions.initHolder(
                    new ResourceException(
                        cause.getDescription(),
                        cause
                    )
                );
            } catch (JmiException exception) {
                BasicException cause = BasicException.newEmbeddedExceptionStack(exception);
                throw ResourceExceptions.initHolder(
                    new ResourceException(
                        cause.getDescription(),
                        cause
                    )
                );
            }
        }

        /**
         * Propagate the <code>RefObject</code> to indexed <code>IndexedRecord<code>
         * 
         * @param refObject
         * @param output
         * @param requestedFeatures 
         * @return <code>true</code>
         * @throws ResourceException 
         */
        @SuppressWarnings("unchecked")
        private boolean propagate(
            RefObject refObject,
            IndexedRecord output, 
            Set<String> requestedFeatures
        ) throws ServiceException{
            if(output != null) output.add(
                this.toJcaRecord(
                    refObject,
                    requestedFeatures
                )
            );
            return true;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#get(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean get(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            Path xri = input.getPath();
            if(isTransactionObjectIdentifier(xri)) {
                Path transactionId = getTransactionId(xri);
                if(transactionId != null) {
                    if(output != null) try {
                        output.add(
                            Object_2Facade.newInstance(
                                transactionId,
                                "org:openmdx:kernel:UnitOfWork"
                            ).getDelegate()
                        );
                    } catch (ResourceException exception) {
                        throw new ServiceException(exception);
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                RefObject refObject = getObjectByResourceIdentifier(xri);
                if(output == null) {
                    return true;
                } else {
                    Set<String> requestedFeatures = new HashSet<String>();
                    if(
                        (input.getQuery() != null) && 
                        input.getQuery().startsWith("<?xml")
                    ) {
                        Filter filter = (Filter)JavaBeans.fromXML(input.getQuery());
                        for(OrderSpecifier orderSpecifier: filter.getOrderSpecifier()) {
                            requestedFeatures.add(
                                orderSpecifier.getFeature()
                            );
                        }
                    }
                    return propagate(refObject, output, requestedFeatures);
                }

            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#create(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.ObjectHolder_2Facade, javax.resource.cci.IndexedRecord)
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean create(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            Path xri = input.getPath();
            if(isTransactionObjectIdentifier(xri)) {
                validateTransactionStateAndId(xri, false);
                try {
                    if(InboundConnection_2.this.localTransaction == null) {
                        InboundConnection_2.this.afterBegin();
                    } else {
                        InboundConnection_2.this.localTransaction.begin();
                    }
                    if(output != null) {
                        output.add(
                            Object_2Facade.newInstance(
                                getTransactionId(xri),
                                "org:openmdx:kernel:UnitOfWork"
                            ).getDelegate()
                        );
                    }
                } catch (JDOException exception) {
                    throw new ServiceException(exception);
                } catch (ResourceException exception) {
                    throw new ServiceException(exception);
                }
                return true;
            } else if (xri.isTransientObjectId()) {
                RefPackage refPackage = getObjectByResourceIdentifier(BASE_AUTHORITY).refOutermostPackage();
                RefObject_1_0 newObject = (RefObject_1_0)refPackage.refClass(input.getObjectClass()).refCreateInstance(
                    Collections.singletonList(xri)
                );
                newObject.refInitialize(false, false);
                this.toRefObject(
                    xri,
                    newObject,
                    input.getValue()
                );
                return propagate(newObject, output, null);
            } else {
                boolean newId = xri.size() % 2 == 0; 
                int featurePosition = xri.size() - (newId ? 1 : 2);
                RefObject refParent = getObjectByResourceIdentifier(xri.getPrefix(featurePosition));
                RefObject_1_0 refObject = (RefObject_1_0)refParent.refOutermostPackage().refClass(input.getObjectClass()).refCreateInstance(null);
                refObject.refInitialize(false, false);
                this.toRefObject(
                    xri,
                    refObject,
                    input.getValue()
                );
                Object container = refParent.refGetValue(xri.get(featurePosition));
                if(newId) {
                    Collection refContainer = (Collection) container;
                    refContainer.add(refObject);
                } else {
                    RefContainer<?> refContainer = (RefContainer<?>) container;
                    refContainer.refAdd(
                        toAddArguments(refContainer.getClass(), xri.getBase(), refObject)
                    );
                }
                return propagate(refObject, output, null);
            }
        }

        /**
         * Provide the <code>add()</code> argument list
         * 
         * @param containerClass
         * @param qualifier
         * @param object
         * 
         * @return the <code>add()</code> argument list
         * @throws ServiceException 
         */
        @SuppressWarnings("unchecked")
        private Object[] toAddArguments(
            Class<? extends RefContainer> containerClass,
            String qualifier,
            RefObject object
        ) throws ServiceException{
            Class<?>[] argumentClasses = Jmi1ContainerInvocationHandler.getAddArguments(containerClass);
            if(argumentClasses.length == 3) {
                boolean persistent = qualifier.startsWith("!");
                return new Object[]{
                    QualifierType.valueOf(persistent),
                    Datatypes.create(argumentClasses[1], persistent ? qualifier.substring(1) : qualifier),
                    object
                };
            } else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_IMPLEMENTED,
                    "More than one qualifier is not yet supported"
                );
            }
        }


        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#move(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.naming.Path, org.openmdx.base.rest.spi.Object_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean move(
            RestInteractionSpec ispec,
            Path xri,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            RefObject_1_0 newObject = (RefObject_1_0) getObjectByResourceIdentifier(xri);
            this.toRefObject(
                xri,
                newObject,
                input.getValue()
            );
            Path newResourceIdentifier = input.getPath();
            int featurePosition = newResourceIdentifier.size() - 2;
            RefObject refObject = getObjectByResourceIdentifier(newResourceIdentifier.getPrefix(featurePosition));
            RefContainer<?> refContainer = (RefContainer<?>) refObject.refGetValue(newResourceIdentifier.get(featurePosition));
            String qualifier = newResourceIdentifier.getBase();
            boolean persistent = qualifier.startsWith("!"); 
            refContainer.refAdd(
                QualifierType.valueOf(persistent),
                persistent ? qualifier.substring(1) : qualifier,
                newObject
            );
            return propagate(newObject, output, null);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#delete(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.ObjectHolder_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean delete(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            Path xri = input.getPath();
            if(isTransactionObjectIdentifier(xri)) {
                validateTransactionStateAndId(xri, true);
                try {
                    if(InboundConnection_2.this.localTransaction == null) {
                        InboundConnection_2.this.afterCompletion(Status.STATUS_ROLLEDBACK);
                    } else {
                        InboundConnection_2.this.localTransaction.rollback();
                    }
                } catch (JDOException exception) {
                    throw new ServiceException(exception);
                } catch (ResourceException exception) {
                    throw new ServiceException(exception);
                }
            } else {
                getObjectByResourceIdentifier(xri).refDelete();
            }
            return true;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#put(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.ObjectHolder_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean put(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            RefObject refObject = getObjectByResourceIdentifier(input.getPath());
            this.toRefObject(
                input.getPath(),
                refObject,
                input.getValue()
            );
            return propagate(refObject, output, null);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#find(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean find(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            Query query = this.toRefQuery(input);
            List<RefObject> objects = (List<RefObject>)query.execute();
            if(output != null){
                int size = input.getSize().intValue();
                int position = input.getPosition().intValue();
                if(position >= 0) {
                    ListIterator<RefObject> i = objects.listIterator(position);
                    int count = 0;
                    while(i.hasNext() && count < size){
                        output.add(
                            this.toJcaRecord(
                                i.next(),
                                null
                            )
                        );
                        count++;
                    }
                    if(output instanceof ResultRecord){
                        ResultRecord outputRecord = (ResultRecord) output;
                        boolean hasMore = i.hasNext();
                        outputRecord.setHasMore(hasMore);
                        if(!hasMore) {
                            outputRecord.setTotal(position + count);
                        }
                    }
                } else {
                    ListIterator<RefObject> i = objects.listIterator(-position);
                    for(
                        int count = 0;
                        i.hasPrevious() && count < size;
                        count++
                    ) {
                        output.add(
                            0,
                            this.toJcaRecord(
                                i.previous(),
                                null
                            )
                        );
                    }
                }
            }
            return true;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#delete(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean delete(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            Path xri = input.getPath();
            if(xri.size() % 2 == 0 || xri.containsWildcard()) {
                try {
                    Query query = this.toRefQuery(input);
                    return query.deletePersistentAll() > 0;
                } catch (JDOException exception) {
                    throw new ServiceException(exception);
                }
            } else if(isTransactionObjectIdentifier(xri)) {
                validateTransactionStateAndId(xri, true);
                try {
                    if(InboundConnection_2.this.localTransaction == null) {
                        InboundConnection_2.this.afterCompletion(Status.STATUS_ROLLEDBACK);
                    } else {
                        InboundConnection_2.this.localTransaction.rollback();
                    }
                    return true;
                } catch (JDOException exception) {
                    throw new ServiceException(exception);
                } catch (ResourceException exception) {
                    throw new ServiceException(exception);
                }
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
                    throw new ServiceException(exception);
                }
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#invoke(org.openmdx.base.resource.spi.RestInteractionSpec, javax.resource.cci.MessageRecord, javax.resource.cci.MessageRecord)
         */
        @Override
        public boolean invoke(
            RestInteractionSpec ispec,
            MessageRecord input,
            MessageRecord output
        ) throws ServiceException {
            try {
                Path xri = input.getPath();
                if(isTransactionCommitIdentifier(xri)) {
                    validateTransactionStateAndId(xri, true );
                    if(InboundConnection_2.this.localTransaction == null) {
                        InboundConnection_2.this.beforeCompletion();
                    } else {
                        InboundConnection_2.this.localTransaction.commit();
                    }
                    if(output != null) {
                        output.setPath(newResponseId(xri));
                        output.setBody(null);
                    }
                } else {
                    int featurePosition = xri.size();
                    featurePosition -= featurePosition % 2 == 1 ? 2 : 1;
                    RefObject refObject = getObjectByResourceIdentifier(xri.getPrefix(featurePosition));
                    RefPackage_1_0 refPackage = (RefPackage_1_0) refObject.refOutermostPackage();
                    MappedRecord arguments = input.getBody();
                    Object reply = refObject.refInvokeOperation(
                        xri.get(featurePosition), 
                        Collections.singletonList(refPackage.refCreateStruct(arguments))
                    );
                    if(output != null) {
                        output.setPath(xri);
                        output.setBody(
                            reply instanceof RefStruct_1_0 ? (MappedRecord)((RefStruct_1_0)reply).refDelegate() : 
                            (MappedRecord)reply
                        );
                    }
                }
                return true;
            } catch (RefException exception) {
                throw new ServiceException(exception);
            } catch (ResourceException exception) {
                throw new ServiceException(exception);
            }
        }

    }

}

