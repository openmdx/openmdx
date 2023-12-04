/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Facade Factory
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
package org.openmdx.base.rest.spi;


import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;

/**
 * The facade factory maps {@code ResourceException}s to 
 * {@code ServiceException}s.
 */
public class Facades {

	/**
	 * Constructor
	 */
	private Facades() {
		// Avoid instantiation
	}

    /**
     * Wrap a MappedRecord into an object facade
     * 
     * @param object an object record
     * @return the facade wrapping the given object record
     * 
     * @throws ServiceException
     */
    public static Object_2Facade asObject(
        MappedRecord object
    ) throws ServiceException {
        try {
            return Object_2Facade.newInstance(object);
        } catch (ResourceException exception) {
            throw new ServiceException(exception);
        }
    }

    /**
     * Create an object facade for the given object id
     * 
     * @param objectId
     * 
     * @return an object facade for the given object id
     * 
     * @throws ResourceException
     */
    public static Object_2Facade newObject(
        Path objectId
    ) throws ServiceException {
    	try {
			return Object_2Facade.newInstance(objectId);
        } catch (ResourceException exception) {
            throw new ServiceException(exception);
        }
    }

    /**
     * Creates a facade for a new object with the given path and the given 
     * object class.
     * 
     * @param objectId the object's id
     * @param objectClass the fully qualified model name
     * 
     * @return a facade for the newly created object
     * 
     * @throws ServiceException
     */
    public static Object_2Facade newObject(
    	Path objectId,
    	String objectClass
    ) throws ServiceException {
    	try {
			return Object_2Facade.newInstance(
		        objectId,
		        objectClass
		    );
		} catch (ResourceException e) {
            throw new ServiceException(e);
		}
    }

    /**
     * Creates a facade for a new object with the given path and the given 
     * object class.
     * 
     * @param objectId the object's id
     * @param objectClass the fully qualified model name
     * 
     * @return a facade for the newly created object
     * 
     * @throws ServiceException
     */
    public static Object_2Facade newObject2(
    	Path objectId,
    	String objectClass
    ) throws ResourceException {
		return Object_2Facade.newInstance(
	        objectId,
	        objectClass
	    );
    }
    
    /**
     * Wrap a MappedRecord into a query facade
     * 
     * @param query a query record
     * @param preferringNotFoundException Tells whether a NOT_FOUND exception 
     * shall be thrown rather than returning an empty result set in case a 
     * requested object does not exist.
     * 
     * @return the facade wrapping the given query record
     * 
     * @throws ServiceException
     */
    public static Query_2Facade asQuery(
        MappedRecord query,
        boolean preferringNotFoundException
    ) throws ServiceException {
        try {
            return Query_2Facade.newInstance(query, preferringNotFoundException);
        } catch (ResourceException exception) {
            throw new ServiceException(exception);
        }
    }

    /**
     * Wrap a MappedRecord into a query facade
     * 
     * @param query a query record
     * 
     * @return the facade wrapping the given query record
     * 
     * @throws ServiceException
     */
    public static Query_2Facade asQuery(
        MappedRecord query
    ) throws ServiceException {
        return asQuery(query, false);
    }
    
    /**
     * Create a query facade for the given object id
     * 
     * @param objectId a data object's id
     * @param preferringNotFoundException Tells whether a NOT_FOUND exception 
     * shall be thrown rather than returning an empty result set in case a 
     * requested object does not exist.
     * 
     * @return a query facade for the given object id
     * 
     * @throws ResourceException
     */
    public static Query_2Facade newQuery(
        Path objectId,
        boolean preferringNotFoundException
    ) throws ServiceException {
        try {
            return Query_2Facade.newInstance(objectId, preferringNotFoundException);
        } catch (ResourceException exception) {
            throw new ServiceException(exception);
        }
    }

    /**
     * Create a query facade for the given object id
     * 
     * @param objectId a data object's id
     * 
     * @return a query facade for the given object id
     * 
     * @throws ResourceException
     */
    public static Query_2Facade newQuery(
        Path objectId
    ) throws ServiceException {
        return newQuery(objectId, false);
    }

}
