/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UnitOfWork_1_0.java,v 1.3 2004/04/02 16:59:01 wfro Exp $
 * Description: SPICE UnitOfWork_1_0 interface
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
package org.openmdx.base.transaction;

import org.openmdx.base.exception.ServiceException;

/**
 * The unit of work interface.
 * <p>
 * Termination:<ul>
 *   <li>The before completion callbacks are invoked during commit():<ul>
 *       <li>If all before completion callbacks succed then the unit of work 
 *           is committed and afterCompletion(true) is invoked. 
 *       <li>If any of the before completion callbacks fails then the unit
 *           of work is either rolled back or aborted depnding on the 
 *           isTransactional() flag.
 *   <li>    
 * </ul>
 */
public interface UnitOfWork_1_0 extends Synchronization_1_0 {

	//------------------------------------------------------------------------
	// Control
	//------------------------------------------------------------------------

	/** 
	 * Begin a unit of work.  The type of unit of work is determined by the
	 * setting of the Optimistic flag.
	 * @throws ServiceException if transactions are managed by a container
	 * in the managed environment, or if the unit of work is already active.
	 */
	void begin(
	) throws ServiceException;
    
	/** 
	 * Commit the current unit of work.
	 * @throws ServiceException if transactions are managed by a container
	 * in the managed environment, or if the unit of work is already active.
	 */
	void commit(
	) throws ServiceException;
    
	/**
	 * Roll back the current unit of work.
	 * @throws ServiceException if transactions are managed by a container
	 * in the managed environment, or if the unit of work is not active.
	 */
	void rollback(
	) throws ServiceException;

	/**
	 * Verify the content of the current unit of work.
	 * <p>
	 * The state of the objects remains unchanged.
	 *
	 * @exception   ServiceException ILLEGAL_STATE
	 *              if no unit of work is in progress
	 * @exception   ServiceException 
	 *              if verification fails
	 */    
	void verify(
	) throws ServiceException;


	//------------------------------------------------------------------------
	// State
	//------------------------------------------------------------------------
	
    /** 
     * Tells whether there is a unit of work currently active.
     * @return <code>true</code> if the unit of work is active.
     */
    boolean isActive();
    
    /**
     * Transactional units of work are executed atomically, i.e. they are 
     * either committed or rolled back while the execution of non-transactional
     * units of work can be aborted at any point.
     *
     * @return the value of the Transactional property.
     */
    boolean isTransactional(
    );
    
    /**
     * Optimistic units of work do not hold data store locks until commit time.
     *
     * @return the value of the Optimistic property.
     */
    boolean isOptimistic(
    );

}
