/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Refreshing Plug-In
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2011, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.aop0;

import java.util.EnumSet;

import javax.jdo.ObjectState;
import javax.jdo.Transaction;

import org.openmdx.base.accessor.rest.DataObjectManager_1;
import org.openmdx.base.accessor.rest.DataObject_1;
import org.openmdx.base.accessor.rest.UnitOfWork_1;
import org.openmdx.base.accessor.spi.AbstractTransaction_1;
import org.openmdx.base.exception.ServiceException;


/**
 * This Plug-In avoids unnecessary updates by removing idempotent modifications.
 */
public class UpdateAvoidance_1 extends PlugIn_1 {

    /**
     * States of objects involved in a unit of work 
     */
    private static final EnumSet<ObjectState> cleanUpCandidates = EnumSet.of(
        ObjectState.PERSISTENT_DIRTY
    );
    
    /**
     * The configuration object
     */
    public static final UpdateAvoidance plugInObject = new UpdateAvoidance() {
		
		public void touchAllDirtyObjects(Transaction transaction) {
			AbstractTransaction_1.getDelegate(UnitOfWork_1.class, transaction).disableUpdateAvoidance();
		}
		
	};

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1#flush(org.openmdx.base.accessor.rest.UnitOfWork_1, boolean)
     */
    @Override
    public void flush(UnitOfWork_1 unitOfWork, boolean beforeCompletion) {
    	if(unitOfWork.isUpdateAvoidanceEnabled()) try {
            DataObjectManager_1 persistenceManager = (DataObjectManager_1) unitOfWork.getPersistenceManager();
            for (Object managedObject : persistenceManager.getManagedObjects(cleanUpCandidates)) {
                DataObject_1 candidate = (DataObject_1) managedObject;
                candidate.makePersistentCleanWhenUnmodified();
            }
        } catch (ServiceException exception) {
            throw new javax.jdo.JDOUserCallbackException(
                "Update avoidance failure",
                exception
            );
    	}
        super.flush(unitOfWork, beforeCompletion);
    }

	/* (non-Javadoc)
	 * @see org.openmdx.base.aop0.PlugIn_1#getPlugInObject(java.lang.Class)
	 */
	@Override
	public <T> T getPlugInObject(Class<T> type) {
		return type == UpdateAvoidance.class ? type.cast(plugInObject) : super.getPlugInObject(type);
	}

}
