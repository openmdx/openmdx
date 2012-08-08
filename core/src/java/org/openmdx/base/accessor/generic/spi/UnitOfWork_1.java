/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UnitOfWork_1.java,v 1.5 2008/09/22 23:38:19 hburger Exp $
 * Description: UnitOfWork_1 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/22 23:38:19 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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

package org.openmdx.base.accessor.generic.spi;

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_2;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.transaction.UnitOfWork_1_2;

/**
 * UnitOfWork_1
 *
 */
public class UnitOfWork_1
    implements UnitOfWork_1_2
{

    /**
     * Constructor 
     *
     * @param delegate
     */
    public UnitOfWork_1(
        ObjectFactory_1_0 delegate
    ){
        this.delegate = delegate;
    }

    /**
     * 
     */
    private final ObjectFactory_1_0 delegate;

    /**
     * @throws ServiceException
     * @see org.openmdx.base.transaction.UnitOfWork_1_0#begin()
     */
    public void begin()
    throws ServiceException {
        this.delegate.getUnitOfWork().begin();
    }

    /**
     * @throws ServiceException
     * @see org.openmdx.base.transaction.UnitOfWork_1_0#commit()
     */
    public void commit()
    throws ServiceException {
        this.delegate.getUnitOfWork().commit();
    }

    /**
     * @return
     * @see org.openmdx.base.transaction.UnitOfWork_1_0#isActive()
     */
    public boolean isActive() {
        try {
            return this.delegate.getUnitOfWork().isActive();
        } catch (ServiceException exception) {
            return false;
        }
    }

    /**
     * @return
     * @see org.openmdx.base.transaction.UnitOfWork_1_0#isOptimistic()
     */
    public boolean isOptimistic() {
        try {
            return this.delegate.getUnitOfWork().isOptimistic();
        } catch (ServiceException exception) {
            return false;
        }
    }

    /**
     * @return
     * @see org.openmdx.base.transaction.UnitOfWork_1_0#isTransactional()
     */
    public boolean isTransactional() {
        if(
            this.delegate instanceof ObjectFactory_1_2 && 
            ((ObjectFactory_1_2)this.delegate).hasContainerManagedUnitOfWork()
        ) try {
            return this.delegate.getUnitOfWork().isTransactional();
        } catch (ServiceException exception) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * @throws ServiceException
     * @see org.openmdx.base.transaction.UnitOfWork_1_0#rollback()
     */
    public void rollback()
    throws ServiceException {
        this.delegate.getUnitOfWork().rollback();
    }

    /**
     * @throws ServiceException
     * @see org.openmdx.base.transaction.UnitOfWork_1_0#verify()
     */
    public void verify()
    throws ServiceException {
        this.delegate.getUnitOfWork().verify();
    }

    /**
     * @throws ServiceException
     * @see org.openmdx.base.transaction.Synchronization_1_0#afterBegin()
     */
    public void afterBegin()
    throws ServiceException {
        this.delegate.getUnitOfWork().afterBegin();
    }

    /**
     * @param committed
     * @throws ServiceException
     * @see org.openmdx.base.transaction.Synchronization_1_0#afterCompletion(boolean)
     */
    public void afterCompletion(boolean committed)
    throws ServiceException {
        this.delegate.getUnitOfWork().afterCompletion(committed);
    }

    /**
     * @throws ServiceException
     * @see org.openmdx.base.transaction.Synchronization_1_0#beforeCompletion()
     */
    public void beforeCompletion()
    throws ServiceException {
        this.delegate.getUnitOfWork().beforeCompletion();
    }

    /**
     * @return
     * @see org.openmdx.base.transaction.UnitOfWork_1_2#getRollbackOnly()
     */
    public boolean getRollbackOnly() {
        try {
            return ((UnitOfWork_1_2)this.delegate.getUnitOfWork()).getRollbackOnly();
        } catch (ServiceException exception) {
            return false;
        }
    }

    /**
     * 
     * @throws ServiceException 
     * @see org.openmdx.base.transaction.UnitOfWork_1_2#setRollbackOnly()
     */
    public void setRollbackOnly(
    ) throws ServiceException {
        ((UnitOfWork_1_2)this.delegate.getUnitOfWork()).setRollbackOnly();
    }

}
