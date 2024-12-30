package org.openmdx.base.dataprovider.cci;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.Record;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Record;
#endif

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RequestRecord;
import org.openmdx.base.rest.cci.ResultRecord;

public interface Channel extends Cloneable {

	/**
	 * Determines whether the request processor is in batching mode 
	 * 
	 * @return {@code true< if the request processor is in batching mode
	 */
	boolean isBatching();

	/**
	 * The requests embedded by beginUnitOfWork() and endUnitOfWork() are 
	 * processed together.
	 *
	 * @exception   ServiceException    ILLEGAL_STATE
	 *              if the collection is already in working unit mode
	 */
	void beginBatch() throws ResourceException;

	/**
	 * The requests embedded by beginUnitOfWork() and endUnitOfWork() are 
	 * processed together.
	 * 
	 * @return {@code true} if all requests have been executed successfully
	 *
	 * @exception   ServiceException    ILLEGAL_STATE
	 *              if the collection is not in working unit mode
	 * @exception   RuntimeException    
	 *              in case of system failure
	 */
	boolean endBatch() throws ResourceException;

	/**
	 * Forget the current batch.
	 */
	void forgetBatch();

	/**
	 * Adds a get request retrieving the typical attributes.
	 *
	 * @param       resourceIdentifier
	 *              the object's resource identifier
	 *
	 * @return      the reply, which may be {@code null} in synchronous mode only.
	 *              A non-existing instance leads to a runtime exception during access in batching mode.
	 *              To avoid this behaviour you may use {@link #addFindRequest(Path)} with the same resource identifier.
	 *
	 * @exception   ResourceException
	 *              if the request fails
	 */
	ObjectRecord addGetRequest(Path resourceIdentifier)
			throws ResourceException;

	/**
	 * Adds a get request retrieving the typical attributes.
	 *
	 * @param       request
	 *              the query record
	 *
	 * @return      the reply
	 *
	 * @exception   ResourceException
	 *              if the request fails
	 */
	ObjectRecord addGetRequest(QueryRecord request) throws ResourceException;

	/**
	 * Adds a create request retrieving the typical attributes.
	 *
	 * @param       object
	 *              thr object to be created
	 *
	 * @exception   ResourceException
	 *              if the request fails
	 */
	void addCreateRequest(ObjectRecord object) throws ResourceException;

	/**
	 * Adds an update request
	 *
	 * @param       object
	 *              the object to be modified
	 *
	 * @exception   ResourceException
	 *              if the request fails
	 */
	void addUpdateRequest(ObjectRecord object) throws ResourceException;

	/**
	 * Adds a remove request 
	 *
	 * @param       path
	 *              the object's path
	 *              
	 * @exception   ResourceException
	 *              if the request fails
	 */
	void addRemoveRequest(Path path) throws ResourceException;

	/**
	 * Adds a find request selecting all objects where the
	 * {@code referenceFilter} evaluates to {@code true}.
	 *
	 * @param       referenceFilter
	 *              an object may be included into the result sets only if it
	 *              is accessible through the path passed as
	 *              {@code referenceFilter}
	 *
	 * @return      the reply
	 *
	 * @exception   ResourceException
	 *              if the request fails
	 */
	ResultRecord addFindRequest(Path referenceFilter) throws ResourceException;

	/**
	 * Adds a get request retrieving the typical attributes.
	 *
	 * @param       request
	 *              the query record
	 *
	 * @return      the reply
	 *
	 * @exception   ResourceException
	 *              if the request fails
	 */
	ResultRecord addFindRequest(QueryRecord request) throws ResourceException;

	/**
	 * Ad an operation request
	 *
	 * @param       request
	 *              the request object
	 *
	 * @return      the reply
	 *
	 * @exception   ResourceException
	 *              if the request fails
	 */
	MessageRecord addOperationRequest(MessageRecord request)
			throws ResourceException;

	/**
	 * Adds a send/receive request
	 * 
	 * @throws      ResourceException
	 *              if the request fails
	 */
	void addSendReceiveRequest(RestInteractionSpec interactionSpec,
			RequestRecord request, Record response) throws ResourceException;

	/**
	 * Adds a send only request
	 * 
	 * @throws      ResourceException
	 *              if the request fails
	 */
	void addSendOnlyRequest(RestInteractionSpec interactionSpec,
			RequestRecord request) throws ResourceException;

	Object clone();

	QueryRecord newQueryRecord(Path resourceIdentifier) throws ResourceException;

	QueryRecord newQueryRecordWithFilter(Path resourceIdentifier) throws ResourceException;

	ResultRecord newResultRecord() throws ResourceException;

	MessageRecord newMessageRecord() throws ResourceException;
	
	MessageRecord newMessageRecord(Path resourceIdentifier)	throws ResourceException;

	/**
	 * Create a new object record of the given type
	 * 
	 * @param resourceIdentifier the object's resource identifier
	 * @param type the object's model class name, e.g. org:openmdx:base:Authority
	 * 
	 * @return the object record
	 * 
	 * @throws ResourceException
	 */
	ObjectRecord newObjectRecord(Path resourceIdentifier, String type) throws ResourceException;


}