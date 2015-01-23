package org.openmdx.application.dataprovider.cci;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.Record;

import org.openmdx.base.dataprovider.cci.Channel;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RequestRecord;
import org.openmdx.base.rest.cci.ResultRecord;

/**
 * Incomplete Channel Adapter (data provider 2 -> data provider 1)
 * 
 * @deprecated not necessary in the data provider 2 stack
 */
@Deprecated
public class ChannelAdapter implements Channel {

	public ChannelAdapter(
		DataproviderRequestProcessor delegate
	) {
		this.delegate = delegate;
	}

	final private DataproviderRequestProcessor delegate;
	
	@Override
	public boolean isBatching() {
		return this.delegate.isBatching;
	}

	@Override
	public void beginBatch() throws ResourceException {
		try {
			this.delegate.beginBatch();
		} catch (ServiceException e) {
			throw ResourceExceptions.toResourceException(e);
		}
	}

	@Override
	public boolean endBatch() throws ResourceException {
		try {
			this.delegate.endBatch();
			return true;
		} catch (ServiceException e) {
			throw ResourceExceptions.toResourceException(e);
		}
	}

	@Override
	public void forgetBatch() {
		this.delegate.forgetBatch();
	}

	@Override
	public ObjectRecord addGetRequest(
		Path resourceIdentifier
	) throws ResourceException {
		try {
			return this.delegate.addGetRequest(resourceIdentifier);
		} catch (ServiceException e) {
			throw ResourceExceptions.toResourceException(e);
		}
	}

	@Override
	public ObjectRecord addGetRequest(
		QueryRecord request
	) throws ResourceException {
		throw new NotSupportedException("The Adapter does not support GET with query");
	}

	@Override
	public void addCreateRequest(ObjectRecord object) throws ResourceException {
		try {
			this.delegate.addCreateRequest(object);
		} catch (ServiceException e) {
			throw ResourceExceptions.toResourceException(e);
		}
	}

	@Override
	public void addUpdateRequest(ObjectRecord object) throws ResourceException {
		try {
			this.delegate.addReplaceRequest(object);
		} catch (ServiceException e) {
			throw ResourceExceptions.toResourceException(e);
		}
	}

	@Override
	public void addRemoveRequest(Path path) throws ResourceException {
		try {
			this.delegate.addRemoveRequest(path);
		} catch (ServiceException e) {
			throw ResourceExceptions.toResourceException(e);
		}
	}

	@Override
	public ResultRecord addFindRequest(
		Path referenceFilter
	) throws ResourceException {
		throw new NotSupportedException("The Adapter does not support FIND requests");
	}

	@Override
	public ResultRecord addFindRequest(
		QueryRecord request
	) throws ResourceException {
		throw new NotSupportedException("The Adapter does not support FIND requests");
	}

	@Override
	public MessageRecord addOperationRequest(
		MessageRecord request
	) throws ResourceException {
		try {
			return this.delegate.addOperationRequest(request);
		} catch (ServiceException e) {
			throw ResourceExceptions.toResourceException(e);
		}
	}

	@Override
	public void addSendReceiveRequest(RestInteractionSpec interactionSpec, RequestRecord request, Record response) throws ResourceException {
		throw new NotSupportedException("The Adapter does not support raw requests");
	}

	@Override
	public void addSendOnlyRequest(RestInteractionSpec interactionSpec,
			RequestRecord request) throws ResourceException {
		throw new NotSupportedException("The Adapter does not support raw requests");
	}
	
    private <T extends RequestRecord> T newRequestRecord(
    	Class<T> type,
    	Path resourceIdentifier
    ) throws ResourceException{
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
		String type
	) throws ResourceException {
    	final ObjectRecord objectRecord = newRequestRecord(ObjectRecord.class, resourceIdentifier);
    	objectRecord.setValue(
	    	Records.getRecordFactory().createMappedRecord(type)
	    );
		return objectRecord;
    }
    
    /* (non-Javadoc)
	 * @see org.openmdx.base.dataprovider.cci.Channel#newQueryRecord(org.openmdx.base.naming.Path)
	 */
    @Override
	public final QueryRecord newQueryRecord(
    	Path resourceIdentifier
    ) throws ResourceException{
    	return newRequestRecord(QueryRecord.class, resourceIdentifier);
    }

    /* (non-Javadoc)
	 * @see org.openmdx.base.dataprovider.cci.Channel#newQueryRecordWithFilter(org.openmdx.base.naming.Path)
	 */
    @Override
	public final QueryRecord newQueryRecordWithFilter(
    	Path resourceIdentifier
    ) throws ResourceException{
    	final QueryRecord queryRecord = newQueryRecord(resourceIdentifier);
    	QueryFilterRecord queryFilter = Records.getRecordFactory().createMappedRecord(QueryFilterRecord.class);
    	queryRecord.setQueryFilter(queryFilter);
		return queryRecord;
    }
    
    /* (non-Javadoc)
	 * @see org.openmdx.base.dataprovider.cci.Channel#newResultRecord()
	 */
    @Override
	public final ResultRecord newResultRecord(
    ) throws ResourceException{
    	return Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
    }

    /* (non-Javadoc)
	 * @see org.openmdx.base.dataprovider.cci.Channel#newMessageRecord()
	 */
    @Override
	public final MessageRecord newMessageRecord(
    ) throws ResourceException{
    	return Records.getRecordFactory().createMappedRecord(MessageRecord.class);
    }

    /* (non-Javadoc)
	 * @see org.openmdx.base.dataprovider.cci.Channel#newMessageRecord()
	 */
    @Override
	public final MessageRecord newMessageRecord(
		Path resourceIdentifier
    ) throws ResourceException{
    	final MessageRecord messageRecord = newMessageRecord();
    	messageRecord.setResourceIdentifier(resourceIdentifier);
    	return messageRecord;
    }
    
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone(
	){
		return new ChannelAdapter(delegate.clone());
	}

}
