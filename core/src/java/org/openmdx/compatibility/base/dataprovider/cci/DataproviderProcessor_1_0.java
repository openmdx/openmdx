/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DataproviderProcessor_1_0.java,v 1.3 2004/04/02 16:59:01 wfro Exp $
 * Description: DataproviderProcessor_1_0 class
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/04/02 16:59:01 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.cci;

import org.openmdx.base.exception.ServiceException;

/**
 * 
 */
public interface DataproviderProcessor_1_0
{

    /**
     * Clear the request and reply list.
     *
     * @return  the old reply list
     */
    void clear();
    
    /**
     * Calling beginBatch() postpones request processing until endBatch() is
     * called.
     *
     * @exception   ServiceException    ILLEGAL_STATE
     *              if the collection is already in batch or working unit mode
     */
    void beginBatch(
    ) throws ServiceException;

    /**
     * Calling endBatch() starts processing of all requests added since
     * beginBatch().
     *
     * @return      the working unit replies
     *
     * @exception   ServiceException    ILLEGAL_STATE
     *              if the collection is not in batch mode
     * @exception   RuntimeException    
     *              in case of system failure
     */
    UnitOfWorkReply[] endBatch(
    ) throws ServiceException;

    /**
     * The requests embedded by beginUnitOfWork() and endUnitOfWork() are 
     * processed together.
     *
     * @param       transactionalUnit
     *              Defines whether the working unit is a transactional unit;
     *              false means that it is either a part of a bigger
     *              transactional unit or a non-transactional unit
     *
     * @exception   ServiceException    ILLEGAL_STATE
     *              if the collection is already in working unit mode
     */
    void beginUnitOfWork(
        boolean transactionalUnit
    ) throws ServiceException;

    /**
     * The requests embedded by beginUnitOfWork() and endUnitOfWork() are 
     * processed together.
     *
     * @exception   ServiceException    ILLEGAL_STATE
     *              if the collection is not in working unit mode
     * @exception   RuntimeException    
     *              in case of system failure
     */
    void endUnitOfWork(
    ) throws ServiceException;

}
