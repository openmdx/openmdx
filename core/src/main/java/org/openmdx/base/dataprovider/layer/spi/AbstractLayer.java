/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Model Aware REST Port
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
package org.openmdx.base.dataprovider.layer.spi;

import javax.resource.ResourceException;
import javax.resource.cci.Interaction;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.resource.cci.RestFunction;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.ConsumerRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.base.rest.spi.AbstractRestPort;
import org.openmdx.base.rest.spi.DelegatingConsumerRecord;

/**
 * Intercepts incoming or outgoing objects
 */
public abstract class AbstractLayer extends AbstractRestPort {
	
	/**
     * Constructor 
     */
    protected AbstractLayer(
        boolean incomingInterceptionEnabled,
        boolean outgoingInterceptionEnabled
    ) {
        this.incomingInterceptionEnabled = incomingInterceptionEnabled;
        this.outgoingInterceptionEnabled = outgoingInterceptionEnabled;
    }

    /**
	 * The MOF repository
	 */
    protected final Model_1_0 model = Model_1Factory.getModel();

    /**
     * Tells whether incoming objects shall be intercepted
     */
    protected final boolean incomingInterceptionEnabled;
    
    /**
     * Tells whether outgoing objects shall be intercepted
     */
    protected final boolean outgoingInterceptionEnabled;
    
    /**
     * Retrieve an object's dereferenced type
     * 
     * @param object the object to be inspected
     * 
     * @return the object's dereferenced type
     * 
     * @throws ServiceException
     */
    protected ModelElement_1_0 getObjectClass(ObjectRecord object) throws ServiceException{
        return model.getDereferencedType(object.getValue().getRecordName());
    }
    
    /**
     * Determines whether an object is instance of the given type
     * 
     * @param object to be tested
     * @param type the type's refMofId, e.g. 'org:openmdx:base:BasicObject'
     * 
     * @return {@code true} if the object is an instance o f the given type
     * 
     * @throws ResourceException
     */
    protected boolean isInstanceOf(ObjectRecord object, String type) throws ResourceException{
        try {
            return model.objectIsSubtypeOf(object, type);
        } catch (ServiceException exception) {
            throw ResourceExceptions.toResourceException(exception);
        }
    }
    
    /**
     * This method must be overridden to intercept incoming objects and this super method must not be invoked!
     * <p>
     * This method is dedicated to<ul>
     * <li>create
     * <li>update
     * </ul>
     * @param object
     */
    protected abstract void interceptIncomingObject(RestFunction method, ObjectRecord object) throws ResourceException;
    
    /**
     * This method is overridden to intercept outgoing objects upon success, i.e. when 
     * the delegate layer returns {@code true}.
     * <p>
     * This method is dedicated to<ul>
     * <li>get
     * <li>find
     * <li>consume
     * <li>create
     * <li>update
     * </ul>
     * @param object
     * 
     * @throws ResourceException
     */
    protected abstract void interceptOutgoingObject(RestFunction method, ObjectRecord object) throws ResourceException;
    
    protected boolean isIncomingInterceptionEnabled(RestInteractionSpec ispec) {
        return incomingInterceptionEnabled && isIncomingTrafficEnabled(ispec);
    }

    protected boolean isOutgoingInterceptionEnabled(RestInteractionSpec ispec) {
        return outgoingInterceptionEnabled && isOutgoingTrafficEnabled(ispec);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.Port#getInteraction(javax.resource.cci.Connection)
     */
    @Override
    public Interaction getInteraction(
        RestConnection connection
        ) throws ResourceException {
        return new InterceptingInteraction(connection, newDelegateInteraction(connection));
    }
    
    /**
     * Intercepting Interaction
     */
    protected class InterceptingInteraction extends AbstractRestInteraction {

        /**
         * Constructor 
         */
        protected InterceptingInteraction(
            RestConnection connection,
            Interaction delegate
        ) throws ResourceException {
            super(connection,  delegate);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#consume(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.QueryRecord, org.openmdx.base.rest.cci.ConsumerRecord)
         */
        @Override
        protected boolean consume(
            final RestInteractionSpec ispec,
            final QueryRecord input,
            final ConsumerRecord output
        ) throws ResourceException {
            return super.consume(
                ispec, 
                input, 
                isOutgoingInterceptionEnabled(ispec) ? newInterceptingConsumer(ispec, output) : output
            );
        }

        @SuppressWarnings("serial")
        private DelegatingConsumerRecord newInterceptingConsumer(
            final RestInteractionSpec ispec,
            final ConsumerRecord output
        ) {
            return new DelegatingConsumerRecord() {

                @Override
                public void accept(ObjectRecord object) {
                    try {
                        interceptOutgoingObject(ispec.getFunction(), object);
                    } catch (ResourceException exception) {
                        throw new RuntimeServiceException(exception);
                    }
                    super.accept(object);
                }

                @Override
                protected ConsumerRecord getDelegate() {
                    return output;
                }
                
            };
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#find(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.QueryRecord, org.openmdx.base.rest.cci.ResultRecord)
         */
        @Override
        protected boolean find(
            RestInteractionSpec ispec,
            QueryRecord input,
            ResultRecord output
        ) throws ResourceException {
            final boolean found = super.find(ispec, input, output);
            if(found && isOutgoingInterceptionEnabled(ispec)){
                interceptOutgoingObjects(ispec.getFunction(), output);
            }
            return found;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#get(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.QueryRecord, org.openmdx.base.rest.cci.ResultRecord)
         */
        @Override
        protected boolean get(
            RestInteractionSpec ispec,
            QueryRecord input,
            ResultRecord output
        ) throws ResourceException {
            final boolean got = super.find(ispec, input, output);
            if(got && isOutgoingInterceptionEnabled(ispec)){
                interceptOutgoingObjects(ispec.getFunction(), output);
            }
            return got;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#update(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.ObjectRecord, org.openmdx.base.rest.cci.ResultRecord)
         */
        @Override
        protected boolean update(
            RestInteractionSpec ispec,
            ObjectRecord input,
            ResultRecord output
        ) throws ResourceException {
            if(isIncomingInterceptionEnabled(ispec)) {
                interceptIncomingObject(ispec.getFunction(), input);
            }
            final boolean updated = super.update(ispec, input, output);
            if(updated && isOutgoingInterceptionEnabled(ispec)){
                interceptOutgoingObjects(ispec.getFunction(), output);
            }
            return updated;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#create(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.ObjectRecord, org.openmdx.base.rest.cci.ResultRecord)
         */
        @Override
        protected boolean create(
            RestInteractionSpec ispec,
            ObjectRecord input,
            ResultRecord output
        ) throws ResourceException {
            if(isIncomingInterceptionEnabled(ispec)) {
                interceptIncomingObject(ispec.getFunction(), input);
            }
            final boolean created = super.create(ispec, input, output);
            if(created && isOutgoingInterceptionEnabled(ispec)){
                interceptOutgoingObjects(ispec.getFunction(), output);
            }
            return created;
        }

        final private void interceptOutgoingObjects(
            RestFunction method, 
            ResultRecord result
        ) throws ResourceException{
            for(Object object : result) {
                interceptOutgoingObject(method, (ObjectRecord) object);
            }
        } 
        
    }
    
}
