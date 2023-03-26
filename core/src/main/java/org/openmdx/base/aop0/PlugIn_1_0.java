/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: PlugIn_1_0 
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
package org.openmdx.base.aop0;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.rest.DataObject_1;
import org.openmdx.base.accessor.rest.UnitOfWork_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;


/**
 * Plug-In 1.0
 * <p>
 * A {@code PlugIn_1_0} implementation may implement any of the 
 * {@code InstanceLifecycleListener} interfaces in order to be
 * notified about life cycle events.
 * 
 * @see javax.jdo.listener.InstanceLifecycleListener
 */
public interface PlugIn_1_0 {
    
    /**
     * New qualifier callback
     * 
     * @param object
     * @param qualifier
     * 
     * @return the last XRI segment of the new object id, or the given
     * qualifier if it is not to be modified by the current plug-in
     */
    String getQualifier(
        DataObject_1 object,
        String qualifier
    ) throws ServiceException;

    /**
     * Post-set-"core" callback
     * 
     * @see SystemAttributes#CORE
     */
    void postSetCore(
        DataObject_1 target,
        DataObject_1 core
    ) throws ServiceException;

    /**
     * Called when the unit of work is about to be completed
     * 
     * @param unitOfWork the unit of work to be flushed
     * @param beforeCompletion {@code true} if the flush is induced by the unit of
     * work's before completion transition, {@code false} otherwise 
     */
    void flush(
        UnitOfWork_1 unitOfWork, 
        boolean beforeCompletion
    );
    
    /**
     * A plug-in may optionally provide objects
     * 
     * @param type 
     * 
     * @return an object of the given type, or {@code null} if no 
     * such user object is provided by this plug-in
     */
    <T> T getPlugInObject(
        Class<T> type
    );    
 
    /**
     * Tells whether the plug-in requires cascaded deletes to be handled by the
     * data object manager
     *  
     * @param object the ancestor
     *  
     * @return {@code true} if the data object manager has to handle
     * cascading deletes 
     * 
     * @throws ServiceException 
     */
    boolean requiresCallbackOnCascadedDelete(
        DataObject_1 object
    ) throws ServiceException;

    /**
     * This method is invoked by the data object validator
     * in order to determine whether the given feature is 
     * exempt from the standard validation.
     * 
     * @param object the object to be validated
     * @param feature the feature's meta-data
     * 
     * @return {@code true} if the plug-in exempts the feature
     * from the standard validation.
     * 
     * @throws ServiceException 
     */
    boolean isExemptFromValidation(
        DataObject_1 object, 
        ModelElement_1_0 feature
    ) throws ServiceException;

    /**
     * Tells whether the object is an aspect
     * 
     * @param object
     * 
     * @return {@code true} if the object is an aspect
     */
    Boolean isAspect(
		DataObject_1 object
    ) throws ServiceException;

}
