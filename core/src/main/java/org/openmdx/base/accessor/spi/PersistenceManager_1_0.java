/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Persistence Manager Interface 2.0
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
package org.openmdx.base.accessor.spi;

import java.security.PrivilegedExceptionAction;
import java.util.UUID;

import javax.jdo.JDODataStoreException;
import #if JAVA_8 javax.resource.cci.InteractionSpec #else jakarta.resource.cci.InteractionSpec #endif;

import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.base.persistence.spi.UnitOfWork;
import org.openmdx.kernel.janitor.Finalizable;
import org.openmdx.kernel.janitor.Finalizer;
import org.openmdx.kernel.jdo.JDOPersistenceManager;

/**
 * Persistence Manager Interface 1.0
 */
public interface PersistenceManager_1_0 extends Finalizable, JDOPersistenceManager {

    /**
     * Retrieve an {@code InteractionSpec} specific persistence manager
     *
     * @return the maybe newly created persistence manager
     */
    PersistenceManager_1_0 getPersistenceManager(
        InteractionSpec interactionSpec
    );

    /**
     * A way to avoid fetching an object just to retrieve its object id
     *
     * @return the value where each object is replaced by its id
     * 
     * @see org.openmdx.base.persistence.cci.PersistenceHelper#getFeatureReplacingObjectById(Object, String)
     */
    Object getFeatureReplacingObjectById(
        UUID transientObjectId,
        String featureName
    );

    /**
     * Tests whether the specified field is already loaded
     * 
     * @param transientObjectId the object's id
     * @param fieldName the name of the field to be tested
     * 
     * @return {@code true} if the field is already loaded
     * 
     * @throws JDODataStoreException if the field's state can't be determined
     */
    boolean isLoaded(
        UUID transientObjectId,
        String fieldName
    );

    /**
     * Lock the persistence manager while the action is performed
     * 
     * @param action the action to be performed
     * 
     * @throws Exception the exception propagated from the action
     */
    <T> T lock(
        PrivilegedExceptionAction<T> action
    ) throws Exception;

    /**
     * Retrieve the object's last XRI segment
     * 
     * @param pc the persistent capable object
     * 
     * @return the last segment of the object's current or future XRI
     */
    String getLastXRISegment(
    	Object pc
    );

    /**
     * Retrieve the transient id of the object's container 
     * 
     * @param pc the persistent capable object
     * 
     * @return the transient id of the object's container, 
     * or {@code null} if the object is not contained 
     */
    TransientContainerId getContainerId(
            Object pc
    );

    /**
     * Retrieve the persistence manager's unit of work
     * 
     * @return the persistence manager's unit of work
     */
    UnitOfWork currentUnitOfWork();

    /**
     * Invoked if open persistence managers shall be closed during finalization
     *
     * @param finalizer the finalizer closing open persistence managers during finalization
     */
    void registerForFinalization(Finalizer finalizer);

}
