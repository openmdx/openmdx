/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Legacy Helper
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
package org.openmdx.state2.spi;

import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.SharedObjects;

/**
 * Some common methods for valid-time-unique support
 */
public class LegacyPlugInHelper {

	/**
	 * Constructor
	 */
	private LegacyPlugInHelper() {
		// Avoid instantiation
	}

	/**
	 * Tells whether the given type is a sub-type of org::openmdx::state2::Legacy
	 * 
	 * @param view
	 * @param type
	 * 
	 * @return {@code true} if the given type is a sub-type of org::openmdx::state2::Legacy
	 * @throws ServiceException
	 */
	public static boolean isLegacy(
        ObjectView_1_0 view,
        ModelElement_1_0 type
    ) throws ServiceException{
        return view.getModel().isSubtypeOf(type, "org:openmdx:state2:Legacy");
	}

	/**
	 * Tells whether the container's parent object is an instance of {@code StateCapable}
	 * 
	 * @param parent the container's parent object
	 * 
	 * @return {@code true} if the container's parent object is an instance of {@code StateCapable}
	 * 
	 * @throws ServiceException
	 */
	private static boolean isStateCapable(
		ObjectView_1_0 parent
	) throws ServiceException {
		return parent.getModel().isInstanceof(parent, "org:openmdx:state2:StateCapable");
	}
	
	/**
	 * @param value
	 * @param model
	 * 
	 * @return {@code true} if the Object is an instance of org::openmdx::state2::Legacy
	 * 
	 * @throws ServiceException
	 */
	public static boolean isLegacy(
		DataObject_1_0 value, 
		Model_1_0 model
	) throws ServiceException {
		return model.isInstanceof(value, "org:openmdx:state2:Legacy");
	}

	/**
	 * Tells whether the valid time is configured to be unique
	 *  
	 * @param persistenceManager
	 * @param xri the object or container id
	 * 
	 * @return {@code true} if the valid time is unique by configuration
	 */
	private static boolean isValidTimeUniqueByConfiguration(
		PersistenceManager persistenceManager,
		Path xri
	){
		LegacyConfiguration legacyConfiguration = SharedObjects.getPlugInObject(
			persistenceManager, 
            LegacyConfiguration.class
        );
		return 
			legacyConfiguration != null && 
			legacyConfiguration.isValidTimeUnique(xri);
	}
	
	/**
	 * Tells whether the valid time is configured to be unique
	 *  
	 * @param value
	 * 
	 * @return {@code true} if the valid time is unique by configuration
	 */
	private static boolean isValidTimeUniqueByConfiguration(
		DataObject_1_0 value
	){
		return isValidTimeUniqueByConfiguration(
			value.jdoGetPersistenceManager(),
			value.jdoGetObjectId()
		);
	}
	
	private static boolean isValidTimeUniqueByProperty(
		DataObject_1_0 value
	) throws ServiceException{
		return Boolean.TRUE.equals(value.objGetValue(TechnicalAttributes.VALID_TIME_UNIQUE));
	}
	
    /**
     * Tells whether the object is an org::openmdx::state2::Legacy instance 
     * with unique valid time.
     * 
     * @param view
     * @param interceptor
     * @param type
     * 
     * @return {@code true} if the object is a Legacy instance with 
     * unique valid time
     * 
     * @throws ServiceException
     */
    public static boolean isValidTimeUnique(
        ObjectView_1_0 view,
        Interceptor_1 interceptor,
        ModelElement_1_0 type
    ) throws ServiceException {
        return view.jdoIsPersistent() ? isValidTimeUniqueByConfiguration(view) : (
            isLegacy(view, type) && isValidTimeUniqueByProperty(interceptor)
        );
    }

    /**
     * Tells whether the object is an org::openmdx::state2::Legacy instance 
     * with unique valid time.
     * 
     * @param value
     * @param model
     * 
     * @return {@code true} if the object is a Legacy instance with 
     * unique valid time
     * 
     * @throws ServiceException
     */
    public static boolean isValidTimeUnique(
    	DataObject_1_0 value,
    	Model_1_0 model
    ) throws ServiceException{
    	return value.jdoIsPersistent() ? isValidTimeUniqueByConfiguration(value) : (     
            isLegacy(value, model) && isValidTimeUniqueByProperty(value)
        );
    }

	/**
	 * Tells whether the children's valid time is unique
	 * 
	 * @param parent
	 * 
	 * @return {@code true} if the children's valid time is unique
	 * 
	 * @throws ServiceException
	 */
	private static boolean isTheChildrensValidTimeUnique(
		ObjectView_1_0 parent
	) throws ServiceException {
		return SharedObjects.getPlugInObject(
			parent.jdoGetPersistenceManager(), 
			LegacyConfiguration.class
		).isTheChildrensValidTimeUnique(
			parent.getModel().getElement(parent.objGetClass())
		);
	}
    
	/**
	 * Tells whether a container's valid time is unique
	 * 
	 * @param parent
	 * @param containerId
	 * 
	 * @return {@code true} if the container's valid time is unique 
	 * 
	 * @throws ServiceException
	 */
	public static boolean isValidTimeUnique(
		ObjectView_1_0 parent,
		Path containerId
	) throws ServiceException {
		return
			containerId != null ? isValidTimeUniqueByConfiguration(parent.jdoGetPersistenceManager(), containerId) :
			isStateCapable(parent) ? isValidTimeUniqueByProperty(parent) :
			isTheChildrensValidTimeUnique(parent);
	}

}
