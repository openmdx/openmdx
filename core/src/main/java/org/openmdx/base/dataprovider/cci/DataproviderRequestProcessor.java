/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Dataprovider Request Processor
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
package org.openmdx.base.dataprovider.cci;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.resource.ResourceException;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.application.dataprovider.cci.AttributeSpecifier;
import org.openmdx.application.dataprovider.cci.FilterProperty;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RequestRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.AbstractMappedRecord;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;

/**
 * Convenience class to create and execute REST requests
 */
public final class DataproviderRequestProcessor implements Channel {

    /**
     * Constructor
     */
    public DataproviderRequestProcessor(
        List<String> principalChain,
        Port<RestConnection> port) throws ResourceException {
        this(new DataproviderRequestConnectionFactory(port, newConnectionSpec(principalChain)));
    }

    /**
     * Constructor
     * 
     * @param connectionFactory
     *            the JCA connection factory
     * 
     * 
     * @throws ResourceException
     */
    private DataproviderRequestProcessor(
        DataproviderRequestConnectionFactory connectionFactory) throws ResourceException {
        this(connectionFactory.getConnection());
    }

    /**
     * Constructor
     * 
     * @param connection
     *            the JCA connection
     */
    private DataproviderRequestProcessor(
        DataproviderRequestConnection connection) {
        this.connection = connection;
        this.pending = null;
    }

    /**
     * The request processor's REST connection
     */
    private final DataproviderRequestConnection connection;

    /**
     * The pending request futures in batching mode
     */
    private List<Pending> pending;

    private static final InteractionSpecs INTERACTION_SPECS = InteractionSpecs.getRestInteractionSpecs(false);

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#isBatching()
     */
    @Override
    public boolean isBatching() {
        return this.pending != null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#beginBatch()
     */
    @Override
    public void beginBatch() throws ResourceException {
        if (isBatching()) {
            throw ResourceExceptions.initHolder(
                new javax.resource.spi.IllegalStateException(
                    "Request processor is already in batching mode", BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.ILLEGAL_STATE
                    )
                )
            );
        }
        this.pending = new ArrayList<Pending>();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#endBatch()
     */
    @Override
    public boolean endBatch() throws ResourceException {
        if (!this.isBatching()) {
            throw ResourceExceptions.initHolder(
                new javax.resource.spi.IllegalStateException(
                    "Request processor is not in batching mode", BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.ILLEGAL_STATE
                    )
                )
            );
        }
        final Interaction interaction = this.connection.createInteraction();
        boolean overall = true;
        for (Pending pending: this.pending) {
            final boolean success = interaction.execute(pending.interactionSpec, pending.request, pending.response);
            pending.onCompletion(success);
            overall &= success;
        }
        interaction.close();
        this.pending = null;
        return overall;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#forgetBatch()
     */
    @Override
    public void forgetBatch() {
        this.pending = null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#addGetRequest(org.openmdx.base.naming.Path)
     */
    @Override
    public ObjectRecord addGetRequest(
        Path resourceIdentifier) throws ResourceException {
        return this.addGetRequest(newQueryRecord(resourceIdentifier));
    }

    public ObjectRecord addGetRequest(
        Path resourceIdentifier,
        String fetchGroupName) throws ResourceException {
        final QueryRecord request = newQueryRecord(resourceIdentifier);
        request.setFetchGroupName(fetchGroupName);
        return this.addGetRequest(request);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#addGetRequest(org.openmdx.base.rest.cci.QueryRecord)
     */
    @Override
    public ObjectRecord addGetRequest(
        QueryRecord request) throws ResourceException {
        final Pending requestFuture = new Pending(INTERACTION_SPECS.GET, request, newResultRecord());
        final ObjectRecord objectFuture = requestFuture.getObjectFuture();
        if (this.isBatching()) {
            this.pending.add(requestFuture);
            return objectFuture;
        } else {
            final Interaction interaction = this.connection.createInteraction();
            final boolean success = requestFuture.execute(interaction);
            interaction.close();
            return success ? objectFuture : null;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#addCreateRequest(org.openmdx.base.rest.cci.ObjectRecord)
     */
    @Override
    public void addCreateRequest(
        ObjectRecord object) throws ResourceException {
        this.addSendOnlyRequest(INTERACTION_SPECS.CREATE, object);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#addUpdateRequest(org.openmdx.base.rest.cci.ObjectRecord)
     */
    @Override
    public void addUpdateRequest(
        ObjectRecord object) throws ResourceException {
        this.addSendOnlyRequest(INTERACTION_SPECS.UPDATE, object);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#addRemoveRequest(org.openmdx.base.naming.Path)
     */
    @Override
    public void addRemoveRequest(
        Path path) throws ResourceException {
        this.addSendOnlyRequest(INTERACTION_SPECS.DELETE, newQueryRecord(path));
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#addFindRequest(org.openmdx.base.naming.Path)
     */
    @Override
    public ResultRecord addFindRequest(
        Path referenceFilter) throws ResourceException {
        final QueryRecord request = newQueryRecord(referenceFilter);
        return addFindRequest(request);
    }

    public ResultRecord addFindRequest(
        Path referenceFilter,
        String fetchGroupName) throws ResourceException {
        final QueryRecord request = newQueryRecord(referenceFilter);
        request.setFetchGroupName(fetchGroupName);
        return addFindRequest(request);
    }

    /**
     * Adds a find request selecting all objects where the
     * <code>referenceFilter</code> as well as the
     * <code>attributeFilter</code> evaluate to true.
     * No attributes are included.
     *
     * @param referenceFilter
     *            an object may be included into the result sets only if it
     *            is accessible through the path passed as
     *            <code>referenceFilter</code>
     * @param attributeFilter
     *            an object may be included into the result sets only if all
     *            the filter properties evaluate to true if applied to it;
     *            this argument may be <code>null</code>.
     *
     * @return the reply
     *
     * @exception ResourceException
     *                if the request fails
     * 
     * @deprecated avoid {@link org.openmdx.application.dataprovider.cci.FilterProperty}
     */
    @Deprecated
    public ResultRecord addFindRequest(
        Path referenceFilter,
        FilterProperty[] attributeFilter) throws ResourceException {
        final QueryRecord request = newQueryRecordWithFilter(referenceFilter);
        final List<Condition> condition = FilterProperty.toCondition(attributeFilter);
        request.getQueryFilter().getCondition().addAll(condition);
        return addFindRequest(request);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#addFindRequest(org.openmdx.base.rest.cci.QueryRecord)
     */
    @Override
    public ResultRecord addFindRequest(
        QueryRecord request) throws ResourceException {
        final ResultRecord response = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
        addSendReceiveRequest(INTERACTION_SPECS.GET, request, response);
        return response;
    }

    public void addFindRequest(
        QueryRecord request,
        ResultRecord response) throws ResourceException {
        addSendReceiveRequest(INTERACTION_SPECS.GET, request, response);
    }

    /**
     * Adds a find request selecting all objects where the
     * <code>referenceFilter</code> as well as the
     * <code>attributeFilter</code> evaluate to true.
     *
     * @param referenceFilter
     *            an object may be included into the result sets only if it
     *            is accessible through the path passed as
     *            <code>referenceFilter</code>
     * @param attributeFilter
     *            an object may be included into the result sets only if all
     *            the filter properties evaluate to true if applied to it;
     *            this argument may be <code>null</code>.
     * @param attributeSpecifiers
     *            An array of attribute specifiers
     *
     * @return the reply
     *
     * @exception ResourceException
     *                if the request fails
     * 
     * @deprecated avoid {@link org.openmdx.application.dataprovider.cci.FilterProperty} and
     *             {@link org.openmdx.application.dataprovider.cci.AttributeSpecifier}
     */
    @Deprecated
    public ResultRecord addFindRequest(
        Path referenceFilter,
        FilterProperty[] attributeFilter,
        AttributeSpecifier[] attributeSpecifiers) throws ResourceException {
        final QueryRecord request = newQueryRecordWithFilter(referenceFilter);
        final List<Condition> condition = FilterProperty.toCondition(attributeFilter);
        final List<OrderSpecifier> order = AttributeSpecifier.toOrderSpecifier(attributeSpecifiers);
        request.getQueryFilter().getCondition().addAll(condition);
        request.getQueryFilter().getOrderSpecifier().addAll(order);
        return addFindRequest(request);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#addOperationRequest(org.openmdx.base.rest.cci.MessageRecord)
     */
    @Override
    public MessageRecord addOperationRequest(
        MessageRecord request) throws ResourceException {
        final MessageRecord response = newMessageRecord();
        if (request.getMessageId() == null) {
            request.setResourceIdentifier(request.getResourceIdentifier().getChild(uuidAsString()));
        }
        addSendReceiveRequest(INTERACTION_SPECS.INVOKE, request, response);
        return response;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#addSendReceiveRequest(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.RequestRecord, javax.resource.cci.Record)
     */
    @Override
    public void addSendReceiveRequest(
        RestInteractionSpec interactionSpec,
        RequestRecord request,
        Record response) throws ResourceException {
        if (this.isBatching()) {
            this.pending.add(new Pending(interactionSpec, request, response));
        } else {
            final Interaction interaction = this.connection.createInteraction();
            interaction.execute(interactionSpec, request, response);
            interaction.close();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#addSendOnlyRequest(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.RequestRecord)
     */
    @Override
    public void addSendOnlyRequest(
        RestInteractionSpec interactionSpec,
        RequestRecord request) throws ResourceException {
        if (this.isBatching()) {
            this.pending.add(new Pending(interactionSpec, request));
        } else {
            final Interaction interaction = this.connection.createInteraction();
            interaction.execute(interactionSpec, request);
            interaction.close();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#clone()
     */
    @Override
    public Object clone() {
        return new DataproviderRequestProcessor(this.connection);
    }

    protected final String uuidAsString() {
        return UUIDs.newUUID().toString();
    }

    private <T extends RequestRecord> T newRequestRecord(
        Class<T> type,
        Path resourceIdentifier) throws ResourceException {
        final T requestRecord = Records.getRecordFactory().createMappedRecord(type);
        requestRecord.setResourceIdentifier(resourceIdentifier);
        return requestRecord;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#newObjectRecord(org.openmdx.base.naming.Path, java.lang.String)
     */
    @Override
    public ObjectRecord newObjectRecord(
        Path resourceIdentifier,
        String type) throws ResourceException {
        final ObjectRecord objectRecord = newRequestRecord(ObjectRecord.class, resourceIdentifier);
        objectRecord.setValue(Records.getRecordFactory().createMappedRecord(type));
        return objectRecord;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#newQueryRecord(org.openmdx.base.naming.Path)
     */
    @Override
    public final QueryRecord newQueryRecord(
        Path resourceIdentifier) throws ResourceException {
        return newRequestRecord(QueryRecord.class, resourceIdentifier);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#newQueryRecordWithFilter(org.openmdx.base.naming.Path)
     */
    @Override
    public final QueryRecord newQueryRecordWithFilter(
        Path resourceIdentifier) throws ResourceException {
        final QueryRecord queryRecord = newQueryRecord(resourceIdentifier);
        QueryFilterRecord queryFilter = Records.getRecordFactory().createMappedRecord(QueryFilterRecord.class);
        queryRecord.setQueryFilter(queryFilter);
        return queryRecord;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#newResultRecord()
     */
    @Override
    public final ResultRecord newResultRecord() throws ResourceException {
        return Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#newMessageRecord()
     */
    @Override
    public final MessageRecord newMessageRecord() throws ResourceException {
        return Records.getRecordFactory().createMappedRecord(MessageRecord.class);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.cci.Channel#newMessageRecord()
     */
    @Override
    public final MessageRecord newMessageRecord(
        Path resourceIdentifier) throws ResourceException {
        final MessageRecord messageRecord = newMessageRecord();
        messageRecord.setResourceIdentifier(resourceIdentifier);
        return messageRecord;
    }

    private static RestConnectionSpec newConnectionSpec(
        List<String> principalChain) {
        List<String> userName = principalChain == null ? Collections.singletonList(System.getProperty("user.name")) : principalChain;
        return new RestConnectionSpec(userName.toString(), null);
    }

    // ------------------------------------------------------------------------
    // Class Pending
    // ------------------------------------------------------------------------

    protected static final class Pending {

        /**
         * Constructor
         */
        Pending(
            RestInteractionSpec interactionSpec,
            RequestRecord request) throws ResourceException {
            this(interactionSpec, request, null);
        }

        /**
         * Constructor
         */
        Pending(
            RestInteractionSpec interactionSpec,
            RequestRecord request,
            Record response) {
            this.interactionSpec = interactionSpec;
            this.request = request;
            this.response = response;
        }

        final RestInteractionSpec interactionSpec;
        final RequestRecord request;
        final Record response;
        private Boolean success;
        private ObjectFuture objectFuture;

        boolean execute(
            Interaction interaction) throws ResourceException {
            onCompletion(interaction.execute(interactionSpec, request, response));
            return success.booleanValue();
        }

        void onCompletion(
            boolean success) {
            this.success = Boolean.valueOf(success);
            if (this.objectFuture != null && response instanceof ResultRecord) {
                final ResultRecord resultRecord = (ResultRecord) response;
                if (!resultRecord.isEmpty()) {
                    this.objectFuture.setDelegate((ObjectRecord) resultRecord.get(0));
                }
            }
        }

        ObjectRecord getObjectFuture() {
            if (this.objectFuture == null) {
                this.objectFuture = new ObjectFuture();
            }
            return this.objectFuture;
        }

        @SuppressWarnings({"rawtypes", "synthetic-access"})
        class ObjectFuture extends AbstractMappedRecord implements ObjectRecord {

            private Members<Member> members;
            private ObjectRecord delegate;
            private static final long serialVersionUID = 2587154012169349226L;

            ObjectRecord getDelegate() {
                if (success == null) {
                    throw new IllegalStateException(
                        "The REST request has not been executed yet", 
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN, 
                            BasicException.Code.ILLEGAL_STATE, 
                            new BasicException.Parameter(
                                "interactionVerb", 
                                interactionSpec.getInteractionVerbName()
                            ), new BasicException.Parameter(
                                "function", 
                                interactionSpec.getFunctionName()
                            ), new BasicException.Parameter(
                                BasicException.Parameter.XRI, 
                                request.getResourceIdentifier()
                            )
                        )
                    );
                }
                if (delegate == null) {
                    throw new RuntimeServiceException(
                        BasicException.Code.DEFAULT_DOMAIN, 
                        BasicException.Code.NO_RESPONSE,
                        "The REST request's result record did not contain an ObjectRecord", 
                        new BasicException.Parameter(
                            "interactionVerb", 
                            interactionSpec.getInteractionVerbName()
                        ), new BasicException.Parameter(
                            "function", 
                            interactionSpec.getFunctionName()
                        ), new BasicException.Parameter(
                            BasicException.Parameter.XRI, 
                            request.getResourceIdentifier()
                        ), new BasicException.Parameter(
                            "success", 
                            success
                        )
                    );
                }
                return delegate;
            }

            void setDelegate(
                ObjectRecord delegate) {
                this.delegate = delegate;
            }

            @Override
            public Path getResourceIdentifier() {
                return getDelegate().getResourceIdentifier();
            }

            @Override
            public void setResourceIdentifier(
                Path resourceIdentifier) {
                getDelegate().setResourceIdentifier(resourceIdentifier);
            }

            @Override
            public String getRecordName() {
                return getDelegate().getRecordName();
            }

            @Override
            public MappedRecord getValue() {
                return getDelegate().getValue();
            }

            @Override
            public void setValue(
                MappedRecord value) {
                getDelegate().setValue(value);
            }

            @Override
            public byte[] getVersion() {
                return getDelegate().getVersion();
            }

            @Override
            public void setVersion(
                byte[] version) {
                getDelegate().setVersion(version);
            }

            @Override
            public Object getLock() {
                return getDelegate().getLock();
            }

            @Override
            public void setLock(
                Object lock) {
                getDelegate().setLock(lock);
            }

            @Override
            public UUID getTransientObjectId() {
                return getDelegate().getTransientObjectId();
            }

            @Override
            public void setTransientObjectId(
                UUID transientObjectId) {
                getDelegate().setTransientObjectId(transientObjectId);
            }

            @Override
            public org.openmdx.base.rest.spi.ObjectRecord clone() {
                return ((org.openmdx.base.rest.spi.ObjectRecord) getDelegate()).clone();
            }

            @Override
            protected Members<Member> members() {
                if (this.members == null) {
                    this.members = Members.newInstance(Member.class);
                }
                return members;
            }

        }

    }

}
