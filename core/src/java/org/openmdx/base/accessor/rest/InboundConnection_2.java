/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: InboundConnection_2.java,v 1.14 2009/06/09 12:45:17 hburger Exp $
 * Description: InboundConnection_2 
 * Revision:    $Revision: 1.14 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 12:45:17 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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

package org.openmdx.base.accessor.rest;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.jdo.JDOException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefStruct;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
import javax.resource.cci.ResourceWarning;
import javax.resource.cci.ResultSetInfo;
import javax.transaction.Synchronization;
import javax.xml.datatype.XMLGregorianCalendar;

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.Version;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefStruct_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.cci.Queries;
import org.openmdx.base.persistence.cci.UserObjects;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.RestFunction;
import org.openmdx.base.resource.cci.ResultRecord;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.spi.ObjectHolder_2Facade;
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
public class InboundConnection_2 implements Connection, Synchronization {

    /**
     * Constructor 
     *
     * @param persistenceManager
     */
    public InboundConnection_2(
        PersistenceManager persistenceManager
    ){
        this.persistenceManager = persistenceManager;
        this.model = Model_1Factory.getModel();
    }

    /**
     * 
     */
    PersistenceManager persistenceManager;

    /**
     * The MOF repository accessor
     */
    final Model_1_0 model;

    /**
     * The inbound connection's meta data
     */
    private ConnectionMetaData metaData = null;

    /**
     * The inbound connection's local transaction
     */
    private LocalTransaction localTransaction = null;

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#close()
     */
    public void close(
    ) throws ResourceException {
        try {
            this.getPersistenceManager().close();
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
        }
        this.persistenceManager = null;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#createInteraction()
     */
    public Interaction createInteraction(
    ) throws ResourceException {
        return new InboundInteraction(this);
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#afterCompletion(int)
     */
    public void afterCompletion(int status) {
        try {
            ((Synchronization)this.getPersistenceManager().currentTransaction()).afterCompletion(status);
        } catch (ResourceException exception) {
            throw new IllegalStateException(
                "Unable to synchronize a closed connection",
                exception
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#beforeCompletion()
     */
    public void beforeCompletion() {
        try {
            ((Synchronization)this.getPersistenceManager().currentTransaction()).beforeCompletion();
        } catch (ResourceException exception) {
            throw new IllegalStateException(
                "Unable to synchronize a closed connection",
                exception
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#getLocalTransaction()
     */
    public LocalTransaction getLocalTransaction(
    ) throws ResourceException {
        if(this.localTransaction == null) {
            this.localTransaction = new LocalTransaction() {

                /* (non-Javadoc)
                 * @see javax.resource.cci.LocalTransaction#begin()
                 */
                public void begin(
                ) throws ResourceException {
                    try {
                        getPersistenceManager().currentTransaction().begin();
                    } catch (JDOException exception) {
                        throw ResourceExceptions.initHolder(
                            new ResourceException(
                                "Connection disposal failure",
                                BasicException.newEmbeddedExceptionStack(exception)
                            )
                        );
                    }
                }

                /* (non-Javadoc)
                 * @see javax.resource.cci.LocalTransaction#commit()
                 */
                public void commit(
                ) throws ResourceException {
                    try {
                        getPersistenceManager().currentTransaction().commit();
                    } catch (JDOException exception) {
                        throw ResourceExceptions.initHolder(
                            new ResourceException(
                                "Connection disposal failure",
                                BasicException.newEmbeddedExceptionStack(exception)
                            )
                        );
                    }
                }

                /* (non-Javadoc)
                 * @see javax.resource.cci.LocalTransaction#rollback()
                 */
                public void rollback(
                ) throws ResourceException {
                    try {
                        getPersistenceManager().currentTransaction().rollback();
                    } catch (JDOException exception) {
                        throw ResourceExceptions.initHolder(
                            new ResourceException(
                                "Connection disposal failure",
                                BasicException.newEmbeddedExceptionStack(exception)
                            )
                        );
                    }
                }

            }; 
        }
        return this.localTransaction;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#getMetaData()
     */
    public ConnectionMetaData getMetaData(
    ) throws ResourceException {
        if(this.metaData == null) {
            this.metaData = new ConnectionMetaData(){

                /**
                 * It's an openMDX connection
                 */
                public String getEISProductName(
                ) throws ResourceException {
                    return "openMDX/REST";
                }

                /**
                 * with the given openMDX version
                 */
                public String getEISProductVersion(
                ) throws ResourceException {
                    return Version.getSpecificationVersion();
                }

                /**
                 * Use the stringified principal chain
                 */
                public String getUserName(
                ) throws ResourceException {
                    List<String> principalChain = UserObjects.getPrincipalChain(getPersistenceManager());
                    return principalChain == null ? null : principalChain.toString();
                }

            };
        }
        return this.metaData;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#getResultSetInfo()
     */
    public ResultSetInfo getResultSetInfo(
    ) throws ResourceException {
        throw new NotSupportedException(
            "Result sets are not supported by " + getClass().getName()
        );
    }

    /**
     * Retrieve the inbound connection's persistence manager
     * 
     * @return the inbound connection's persistence manager
     * 
     * @throws ResourceException if the connection is already closed
     */
    protected PersistenceManager getPersistenceManager(
    ) throws ResourceException {
        if(this.persistenceManager != null){
            return this.persistenceManager;
        } else throw ResourceExceptions.initHolder( 
            new ResourceException(
                "The connection is already closed",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE
                )
            )
        );
    }

    
    //------------------------------------------------------------------------
    // Class InboundInteraction
    //------------------------------------------------------------------------

    /**
     * Inbound Interaction
     */
    static class InboundInteraction implements Interaction {

        InboundInteraction(
            InboundConnection_2 connection
        ){
            this.connection = connection;
        }

        /**
         * The interaction's connection
         */
        InboundConnection_2 connection;

        /**
         * The chain of warnings
         */
        private ResourceWarning warnings;

        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#clearWarnings()
         */
        public void clearWarnings(
        ) throws ResourceException {
            this.warnings = null;
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#close()
         */
        public void close(
        ) throws ResourceException {
            this.connection = null;
        }

        /**
         * Validate the output record
         * 
         * @param output the output record to be validated
         * 
         * @return the validated output record 
         * 
         * @throws ResourceException
         */
        private IndexedRecord restOutput(
            Record output
        ) throws ResourceException{
            if(output == null || output instanceof IndexedRecord) {
                return (IndexedRecord) output;
            } else throw BasicException.initHolder(
                new ResourceException(
                    "Unsupported output record class",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("expected", IndexedRecord.class.getName()),
                        new BasicException.Parameter("actual", output == null ? null : output.getClass().getName())
                    )
                )
            );
        }

        /**
         * Validate the interaction specification
         * 
         * @param interactionSpec the interaction specification to be validated
         * 
         * @return the validated interaction specification
         * 
         * @throws ResourceException
         */
        private RestInteractionSpec toRestInteractionSpec(
            InteractionSpec interactionSpec
        ) throws ResourceException{
            if(interactionSpec instanceof RestInteractionSpec) {
                return (RestInteractionSpec) interactionSpec;
            } else throw BasicException.initHolder(
                new ResourceException(
                    "Unsupported interaction specification class",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("expected", RestInteractionSpec.class.getName()),
                        new BasicException.Parameter("actual", interactionSpec == null ? null : interactionSpec.getClass().getName())
                    )
                )
            );
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
         */
        protected boolean execute(
            RestInteractionSpec interactionSpec,
            MappedRecord input,
            IndexedRecord output
        ) throws ResourceException {
            try {
                RestFunction function = interactionSpec.getFunction();
                if(Query_2Facade.isDelegate(input)) {
                    this.handle(
                        function,
                        Query_2Facade.newInstance(input),
                        output
                    );
                } 
                else if (ObjectHolder_2Facade.isDelegate(input)) {
                    this.handle(
                        function,
                        ObjectHolder_2Facade.newInstance(input),
                        output
                    );
                }
                else if (function == RestFunction.POST){
                    for(Object e : input.entrySet()) {
                        Map.Entry<?, ?> entry = (Entry<?, ?>) e;
                        Object key = entry.getKey();
                        Object value = entry.getValue();
                        if(
                            (key instanceof String || key instanceof Path) && 
                            (value instanceof MappedRecord)
                        ) {
                            this.handle(
                                function,
                                key instanceof String ? new Path((String)key) : (Path)key,
                                    (MappedRecord)value,
                                    output
                            );
                        } 
                        else throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            "POST request entries have to be XRI/MappedRecord entries",
                            new BasicException.Parameter("function", function),
                            new BasicException.Parameter("recordName", input.getRecordName()),
                            new BasicException.Parameter("key", key == null ? null : key.getClass().getName()),
                            new BasicException.Parameter("value", value == null ? null : value.getClass().getName())
                        ); 
                    }
                } 
                else throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "Only POST requests accept pure maps as input",
                    new BasicException.Parameter("function", function),
                    new BasicException.Parameter("recordName", input.getRecordName())
                );
            } 
            catch (JDOException exception) {
                throw BasicException.initHolder(
                    new ResourceException(
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            } 
            catch (ServiceException exception) {
                throw BasicException.initHolder(
                    new ResourceException(
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            } 
            catch (RefException exception) {
                throw BasicException.initHolder(
                    new ResourceException(
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            }
            return output != null;
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
         */
        protected boolean execute(
            RestInteractionSpec interactionSpec,
            IndexedRecord input,
            IndexedRecord output
        ) throws ResourceException {
            RestFunction function = interactionSpec.getFunction();
            for(Object request : input) try {
                if(request instanceof String) {
                    this.handle(
                        function,
                        new Path((String)request),
                        null,
                        output
                    );
                } 
                else throw BasicException.initHolder(
                    new ResourceException(
                        "The input record members should be XRI 2 string values",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            new BasicException.Parameter("expected", String.class.getName()),
                            new BasicException.Parameter("actual", request == null ? null : request.getClass().getName())
                        )
                    )
                );
            } 
            catch (JDOException exception) {
                throw BasicException.initHolder(
                    new ResourceException(
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            } 
            catch (ServiceException exception) {
                throw BasicException.initHolder(
                    new ResourceException(
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            } 
            catch (RefException exception) {
                throw BasicException.initHolder(
                    new ResourceException(
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            }
            return output != null;
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
         */
        protected boolean execute(
            RestInteractionSpec interactionSpec,
            Record input,
            IndexedRecord output
        ) throws ResourceException {
            if(input instanceof IndexedRecord) {
                return this.execute(
                    interactionSpec,
                    (IndexedRecord) input,
                    interactionSpec.getInteractionVerb() == InteractionSpec.SYNC_SEND ? 
                        null : 
                            output
                );
            } 
            else if (input instanceof MappedRecord) {
                return this.execute(
                    interactionSpec,
                    (MappedRecord) input,
                    interactionSpec.getInteractionVerb() == InteractionSpec.SYNC_SEND ? 
                        null : 
                            output
                );
            } 
            else throw BasicException.initHolder(
                new ResourceException(
                    "Unsupported input record class",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("expected", IndexedRecord.class.getName(), MappedRecord.class.getName()),
                        new BasicException.Parameter("actual", input == null ? null : input.getClass().getName())
                    )
                )
            );
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
         */
        public boolean execute(
            InteractionSpec ispec,
            Record input,
            Record output
        ) throws ResourceException {
            return this.execute(
                toRestInteractionSpec(ispec),
                input,
                restOutput(output)
            );
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record)
         */
        public Record execute(
            InteractionSpec ispec, 
            Record input
        ) throws ResourceException {
            RestInteractionSpec interactionSpec = this.toRestInteractionSpec(ispec);
            IndexedRecord output = 
                interactionSpec.getInteractionVerb() == InteractionSpec.SYNC_SEND ? null :
                    Records.getRecordFactory().createIndexedRecord("list", null);
            this.execute(
                interactionSpec,
                input,
                output
            );
            return output;
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
            } 
            else {
                StringBuilder recordName = new StringBuilder();
                for(Object component : refValue.refTypeName()) {
                    recordName.append(':').append(component);
                }
                return recordName.substring(1);
            }
        }

        /**
         * Convert a <code>RefObject</code> value to a <code>MappedRecord</code> value
         * 
         * @param object the <code>RefObject</code> value
         * 
         * @return its <code>MappedRecord</code> value representation
         * 
         * @throws ResourceException
         */
        @SuppressWarnings("unchecked")
        private Object toJcaValue(
            Object refValue
        ) throws ResourceException{
            if(refValue instanceof RefObject) {
                return PersistenceHelper.getCurrentObjectId(refValue);
            } 
            else if(refValue instanceof Set) {
                IndexedRecord target = Records.getRecordFactory().createIndexedRecord(Multiplicities.SET);
                for(Object feature : ((Set<?>)refValue)){
                    target.add(this.toJcaValue(feature));
                }
                return target;
            } 
            else if(refValue instanceof List) {
                IndexedRecord target = Records.getRecordFactory().createIndexedRecord(Multiplicities.LIST);
                for(Object e : ((List<?>)refValue)){
                    target.add(this.toJcaValue(e));
                }
                return target;
            } 
            else if(refValue instanceof SparseArray) {
                MappedRecord target = Records.getRecordFactory().createMappedRecord(Multiplicities.SPARSEARRAY);
                target.putAll((SparseArray)refValue);
                return target;
            } 
            else if(refValue instanceof RefStruct){
                RefStruct refStruct = (RefStruct) refValue;
                MappedRecord target = Records.getRecordFactory().createMappedRecord(jcaRecordName(refStruct));
                for(String fieldName : (List<String>)refStruct.refFieldNames()) {
                    target.put(
                        fieldName,
                        this.toJcaValue(refStruct.refGetValue(fieldName))
                    );
                }
                return target;
            } 
            else {
                return refValue;
            }
        }

        /**
         * Convert a <code>MappedRecord</code> value to a <code>RefObject</code> value
         * 
         * @param object the <code>MappedRecord</code> value
         * 
         * @return its <code>RefObject</code> value representation
         * 
         * @throws ResourceException
         */
        Object toRefValue(
            Object jcaValue,
            ModelElement_1_0 featureDef
        ) throws ServiceException{
            ModelElement_1_0 featureType = this.connection.model.getDereferencedType(featureDef.objGetValue("type"));
            if(Multiplicities.STREAM.equals(featureDef.objGetValue("multiplicity"))) {
                return jcaValue instanceof char[] ?
                    CharacterLargeObjects.valueOf((char[])jcaValue) :
                        BinaryLargeObjects.valueOf((byte[])jcaValue);    
            }
            else if(
                (jcaValue instanceof String) &&
                PrimitiveTypes.DATETIME.equals(featureType.objGetValue("qualifiedName"))
            ) {
                return Datatypes.create(
                    java.util.Date.class,
                    (String)jcaValue
                );
            }
            else if(
                (jcaValue instanceof String) &&
                PrimitiveTypes.DATE.equals(featureType.objGetValue("qualifiedName"))
            ) {
                return Datatypes.create(
                    XMLGregorianCalendar.class,
                    (String)jcaValue
                );
            }
            else {
                return featureDef.getModel().isReferenceType(featureDef) ? 
                    this.connection.persistenceManager.getObjectById(new Path(jcaValue.toString())) :
                        jcaValue;
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
        ) throws ResourceException {
            RefObject_1_0 refObject = (RefObject_1_0) object;
            ObjectHolder_2Facade reply = ObjectHolder_2Facade.newInstance();
            reply.setPath(refObject.refGetPath());
            reply.setVersion(
                JDOHelper.getVersion(refObject)
            );
            MappedRecord jcaValue = Records.getRecordFactory().createMappedRecord(
                refObject.refClass().refMofId()
            );
            reply.setValue(jcaValue);
            for(String feature : refObject.refDefaultFetchGroup()) {
                if(
                    !"context".equals(feature) && 
                    (feature.indexOf(":") == -1)
                ) {        
                    try {
                        jcaValue.put(
                            feature,
                            this.toJcaValue(
                                refObject.refGetValue(feature)
                            )
                        );
                    }
                    catch(Exception e) {
                        new ServiceException(e).log();
                    }
                }
            }
            if(requestedFeatures != null) {
                for(String feature: requestedFeatures) {
                    try {
                        jcaValue.put(
                            feature,
                            this.toJcaValue(
                                refObject.refGetValue(feature)
                            )
                        );                    
                    }
                    catch(Exception e) {
                        new ServiceException(e).log();
                    }
                }
            }
            return reply.getDelegate();
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
        protected Query toRefQuery(
            Query_2Facade input
        ) throws ResourceException {        
            PersistenceManager pm = this.connection.getPersistenceManager();
            //
            // TODO handle total & hasMore
            //
            Query query = pm.newQuery(
                Queries.OPENMDXQL, 
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
            Path objectId,
            RefObject refTarget,
            MappedRecord jcaSource
        ) throws ServiceException{
            ModelElement_1_0 classDef = this.connection.model.getElement(refTarget.refClass().refMofId());
            for(Object rawObjectEntry : jcaSource.entrySet()) {
                Map.Entry<?, ?> objectEntry = (Entry<?, ?>) rawObjectEntry;
                String featureName = objectEntry.getKey().toString();
                if(!featureName.startsWith(SystemAttributes.CONTEXT_PREFIX)) {
                    Object rawValue = objectEntry.getValue();
                    ModelElement_1_0 featureDef = this.connection.model.getFeatureDef(classDef, featureName, false);
                    if(featureDef == null) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_MEMBER_NAME,
                            "Unknown feature",
                            new BasicException.Parameter("xri", objectId.toXRI()),
                            new BasicException.Parameter("class", refTarget.refClass().refMofId()),
                            new BasicException.Parameter("feature", featureName)
                        );
                    }
                    featureName = (String) featureDef.objGetValue("name");
                    Object multiplicity = featureDef.objGetValue("multiplicity");
                    Boolean isChangeable = (Boolean)featureDef.objGetValue("isChangeable");                
                    Boolean isDerived = (Boolean)featureDef.objGetValue("isDerived");
                    if(isChangeable != null && isChangeable && (isDerived == null || !isDerived)) {
                        if(
                            Multiplicities.LIST.equals(multiplicity) ||
                            Multiplicities.SET.equals(multiplicity)
                        ) {
                            Collection<?> source =
                                rawValue == null ? Collections.EMPTY_LIST :
                                    rawValue instanceof List ? (List<?>)rawValue : 
                                        Collections.singletonList(rawValue);
                                    Collection target = (Collection) refTarget.refGetValue(featureName);
                                    target.clear();
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
                            Map<?,?> source =
                                rawValue == null ? Collections.EMPTY_MAP :
                                    rawValue instanceof MappedRecord ? (Map<?,?>)rawValue : 
                                        Collections.singletonMap(0,rawValue);
                                    SparseArray target = (SparseArray) refTarget.refGetValue(featureName);
                                    target.clear();
                                    for(Object rawValueEntry : source.entrySet()) {
                                        Map.Entry<?,?> valueEntry = (Entry<?, ?>) rawValueEntry;
                                        target.put(
                                            valueEntry.getKey(),
                                            this.toRefValue(
                                                valueEntry.getValue(), 
                                                featureDef
                                            )
                                        );
                                    }
                        } 
                        else {
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
            }
        }

        /**
         * Handle data object 
         * 
         * @param function the REST function to be executed
         * @param request the request's data object facade
         * @param replies the reply holder, or <code>null</code> if the interaction verb is {@link InteractionSpec#SYNC_SEND}
         * 
         * @throws ServiceException
         * @throws RefException
         * @throws ResourceException
         */
        @SuppressWarnings("unchecked")
        private void handle(
            RestFunction function,
            ObjectHolder_2Facade request,
            IndexedRecord replies
        ) throws ServiceException, RefException, ResourceException{
            PersistenceManager persistenceManager = this.connection.getPersistenceManager();
            Path xri = request.getPath();
            RefObject refObject = (RefObject) persistenceManager.getObjectById(xri);
            switch(function){
                case DELETE:
                    refObject.refDelete();
                    return;
                case PUT:
                    this.toRefObject(
                        xri,
                        refObject,
                        request.getValue()
                    );
                    break;
                case GET:
                    break;
                default: throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "Unexpected function",
                    new BasicException.Parameter("xri", xri),
                    new BasicException.Parameter("actual", function),
                    new BasicException.Parameter("expected", RestFunction.PUT, RestFunction.DELETE, RestFunction.GET)
                );
            }
            if(replies != null) {
                replies.add(
                    this.toJcaRecord(
                        refObject,
                        null
                    )
                );
            }
        }

        /**
         * Handle data object 
         * 
         * @param function the REST function to be executed
         * @param xri the object's resource identifier
         * @param replies the reply holder, or <code>null</code> if the interaction verb is {@link InteractionSpec#SYNC_SEND}
         * 
         * @throws ServiceException
         * @throws RefException
         * @throws ResourceException
         */
        @SuppressWarnings("unchecked")
        private void handle(
            RestFunction function,
            Path xri,
            MappedRecord value,
            IndexedRecord replies
        ) throws ServiceException, RefException, ResourceException{
            PersistenceManager persistenceManager = this.connection.getPersistenceManager();
            RefObject refObject;
            switch(function) {
                case GET:
                    refObject = (RefObject) persistenceManager.getObjectById(xri);
                    if(replies != null) {
                        replies.add(
                            this.toJcaRecord(
                                refObject,
                                null
                            )
                        );
                    }
                    break;
                case DELETE:
                    refObject = (RefObject) persistenceManager.getObjectById(xri);
                    refObject.refDelete();
                    break;
                case POST:
                    String typeName = value.getRecordName();
                    int featurePosition = xri.size() - 2;
                    refObject = (RefObject) persistenceManager.getObjectById(xri.getPrefix(featurePosition));
                    // Method invocation
                    if(this.connection.model.isStructureType(typeName)) {
                        RefPackage_1_0 refPackage = (RefPackage_1_0) refObject.refOutermostPackage();
                        Object reply = refObject.refInvokeOperation(
                            xri.get(featurePosition), 
                            Collections.singletonList(refPackage.refCreateStruct(value.getRecordName(), value))
                        );
                        if(replies != null) {
                            replies.add(
                                reply instanceof RefStruct_1_0 ? ((RefStruct_1_0)reply).refDelegate() : reply
                            );
                        }
                    } 
                    // Object creation
                    else {
                        RefObject_1_0 newObject = (RefObject_1_0)refObject.refOutermostPackage().refClass(typeName).refCreateInstance(null);
                        newObject.refInitialize(false, false);
                        this.toRefObject(
                            xri,
                            newObject,
                            value
                        );
                        RefContainer refContainer = (RefContainer) refObject.refGetValue(xri.get(featurePosition));
                        String qualifier = xri.getBase();
                        boolean persistent = qualifier.startsWith("!"); 
                        refContainer.refAdd(
                            QualifierType.valueOf(persistent),
                            persistent ? qualifier.substring(1) : qualifier,
                                newObject
                        );
                        if(replies != null) {
                            replies.add(
                                this.toJcaRecord(
                                    newObject,
                                    null
                                )
                            );
                        }
                    }
                    break;
                case PUT:
                    // TODO touch
                    throw BasicException.initHolder(
                        new ResourceException(
                            "Touch is not yet implemented",
                            BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.NOT_IMPLEMENTED,
                                new BasicException.Parameter("xri", xri),
                                new BasicException.Parameter("function", function)
                            )
                        )
                    );
            }
        }

        /**
         * Handle data object 
         * 
         * @param function the REST function to be executed
         * @param request the request's data object facade
         * @param replies the reply holder, or <code>null</code> if the interaction verb is {@link InteractionSpec#SYNC_SEND}
         * 
         * @throws ServiceException
         * @throws RefException
         * @throws ResourceException
         */
        @SuppressWarnings("unchecked")
        private void handle(
            RestFunction function,
            Query_2Facade request,
            IndexedRecord replies
        ) throws ServiceException, RefException, ResourceException{
            Path objectId = request.getPath();
            PersistenceManager persistenceManager = this.connection.getPersistenceManager();
            if(objectId.size() % 2 == 1 && !objectId.containsWildcard()) {
                RefObject refObject = (RefObject) persistenceManager.getObjectById(objectId);
                switch(function) {
                    case GET:
                        if(replies != null) {
                            Set<String> requestedFeatures = new HashSet<String>();
                            if(
                                (request.getQuery() != null) && 
                                request.getQuery().startsWith("<?xml")
                            ) {
                                Filter filter = (Filter)JavaBeans.fromXML(request.getQuery());
                                for(OrderSpecifier orderSpecifier: filter.getOrderSpecifier()) {
                                    requestedFeatures.add(
                                        orderSpecifier.getFeature()
                                    );
                                }
                            }
                            replies.add(
                                this.toJcaRecord(
                                    refObject,
                                    requestedFeatures
                                )
                            );
                        }
                        break;
                    case DELETE:    
                        refObject.refDelete();
                        break;
                }
            } 
            else {                
                Query_2Facade unboundQuery = Query_2Facade.newInstance();
                unboundQuery.setQuery(request.getQuery());
                unboundQuery.setQueryType(request.getQueryType());
                unboundQuery.setPath(request.getPath());
                Query query = this.toRefQuery(unboundQuery);
                List<RefObject> objects = (List<RefObject>)query.execute();
                int size = request.getSize().intValue();
                int position = request.getPosition().intValue();
                Iterator<RefObject> i = objects.listIterator(position);
                int count = 0;
                while(i.hasNext() && (count < size)) {
                    replies.add(
                        this.toJcaRecord(
                            i.next(),
                            null
                        )
                    );
                    count++;
                }
                if(i.hasNext()) {
                    ((ResultRecord)replies).setHasMore(true);
                }
                else {
                    ((ResultRecord)replies).setTotal(objects.size());                    
                    ((ResultRecord)replies).setHasMore(false);                    
                }
            }
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#getConnection()
         */
        public Connection getConnection() {
            return this.connection;
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#getWarnings()
         */
        public ResourceWarning getWarnings(
        ) throws ResourceException {
            return this.warnings;
        }

    }

}

