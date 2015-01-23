/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: model1 application plugin
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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
package org.openmdx.application.mof.repository.layer.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;

import org.openmdx.application.mof.repository.utils.ModelUtils;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.base.rest.spi.AbstractRestPort;

public class Model_2 extends AbstractRestPort {

	@Override
	public Interaction getInteraction(
		RestConnection connection
	) throws ResourceException {
		return new RestInteraction(connection);
	}

	//------------------------------------------------------------------------
	// Class RestInteraction
	//------------------------------------------------------------------------
	
	/**
	 * This class provides The plug-in specific REST interaction implementation.
	 */
    protected class RestInteraction extends AbstractRestInteraction {

    	/**
    	 * Constructor
    	 * 
    	 * @throws ResourceException 
    	 */
		protected RestInteraction(
			RestConnection connection
		) throws ResourceException {
			super(connection, newDelegateInteraction(connection));
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
			return super.create(ispec, touch(true, input), output);
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
			return super.update(ispec, touch(false, input), output);
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
			final boolean success = super.get(ispec, input, output);
			if(success) {
				complete(output);
			}
			return success;
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
			final boolean success = super.find(ispec, input, output);
			if(success) {
				complete(output);
			}
			return success;
		}

		/**
		 * This method updates the basic object's life cycle features
		 * <p>
		 * This method would have to be changed to a clone/update pattern if 
		 * the input were unmodifiable.
		 */
		@SuppressWarnings("unchecked")
		private ObjectRecord touch(
            boolean isNew,
            ObjectRecord input
        ) throws ResourceException{
            if(isInstanceOfBasicObject(input)) {
                final IndexedRecord by = toIndexedRecordList(getPrincipalChain());
            	final Date at = getInteractionTime();
            	final MappedRecord target = input.getValue();
            	if(isNew) {
            		target.put(SystemAttributes.CREATED_AT, at);
            		target.put(SystemAttributes.CREATED_BY, by);
            	}
        		target.put(SystemAttributes.MODIFIED_AT, at);
        		target.put(SystemAttributes.MODIFIED_BY, by);
            }
            return input;
        }

		/**
		 * This method adds the basic object's model features
		 */
		private void complete(
            ObjectRecord object
        ) throws ResourceException{
            completeInstanceOf(object);
            completeNames(object);
        }
		
	    /**
	     * complete derived attribute 'name' and 'qualifiedName'
	     */
		@SuppressWarnings("unchecked")
	    private void completeNames(
	        ObjectRecord object
	    ){
	        String qualifiedName = object.getResourceIdentifier().getLastSegment().toClassicRepresentation();
	        object.getValue().put(
	            "qualifiedName",
	            qualifiedName
	        );
	        object.getValue().put(
	        	"name",
	            qualifiedName.substring(qualifiedName.lastIndexOf(':') + 1)
	        );
	    }

		@SuppressWarnings("unchecked")
		private void completeInstanceOf(ObjectRecord output)
				throws ResourceException {
			final MappedRecord target = output.getValue();
			try {
				final List<String> supertype = ModelUtils.getallSupertype(
					target.getRecordName()
				);
				target.put(
					SystemAttributes.OBJECT_INSTANCE_OF, 
					toIndexedRecordList(
						supertype == null ? Collections.singletonList(target.getRecordName()) : toIndexedRecordList(supertype)
					)
				);
			} catch (ServiceException e) {
				throw ResourceExceptions.toSystemException(e);
			}
		}

		@SuppressWarnings("unchecked")
		private void complete(
			ResultRecord output
		) throws ResourceException{
			for(ObjectRecord object : (List<ObjectRecord>)output) {
				complete(object);
			}
		}
		
		/**
		 * All objects except Authority, Provider and Segment should be BasicObjects
		 */
        private boolean isInstanceOfBasicObject(
    		ObjectRecord object
        ){
            return object.getResourceIdentifier().size() > 5;
        }
		
    }
    
}
