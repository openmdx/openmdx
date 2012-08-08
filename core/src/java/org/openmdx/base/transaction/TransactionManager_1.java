/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TransactionManager_1.java,v 1.8 2007/10/10 16:05:54 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:05:54 $
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

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * @author hburger
 *
 * The transaction manager unifies the exception handling.
 */
public class TransactionManager_1 {

	/**
	 * Avoid instantiation
	 */
	private TransactionManager_1(
	){
	    super();
	}

	/**
	 * 
	 * @param transaction
	 * @param synchronization
	 * @throws ServiceException
	 */
	public static void execute(
		UserTransaction transaction,
		Synchronization_1_0 synchronization
	) throws ServiceException {
		execute(transaction, synchronization, 0L);
	}
	
	/**
	 * 
	 * @param transaction
	 * @param synchronization
	 * @param timeout
	 * @throws ServiceException
	 */
	public static void execute(
		UserTransaction transaction,
		Synchronization_1_0 synchronization,
		long timeout
	) throws ServiceException {

		Phase phase = new Phase();

		try {
			phase.setPhase("begin");
			try {
				if(timeout > 0L) transaction.setTransactionTimeout(
					(int) (timeout / 1000) // ms -> s
				);
				transaction.begin();
			} catch (NotSupportedException exception) {
				throw new ServiceException(
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.ROLLBACK,
					phase.getPhase(),
					"The thread is already associated with a transaction " +
					"and the Transaction Manager implementation does not " +
					"support nested transactions."
				);
			} catch (SystemException exception) {
				throw new ServiceException(
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.ROLLBACK,
					phase.getPhase(),
					"The transaction manager encountered an " +
					"unexpected error condition"
				);
			}
			
			phase.setPhase("afterBegin");
			try {
				synchronization.afterBegin();
			} catch (ServiceException exception) {
				throw rollback(transaction,synchronization,phase,exception);
			}
	
			phase.setPhase("beforeCompletion");
			try {
				synchronization.beforeCompletion();
			} catch (ServiceException exception) {
				throw rollback(transaction,synchronization,phase,exception);
			}
	
			phase.setPhase("commit");
			try {
				transaction.commit();
			} catch (RollbackException exception) {
				throw new ServiceException(
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.ROLLBACK,
					phase.getPhase(),
					"Transaction rolled back"
				);
			} catch (HeuristicMixedException exception) {
				throw new ServiceException(
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.HEURISTIC,
					phase.getPhase(),
					"A heuristic decision was made and some " +
					"relevant updates have been committed " +
					"while others have been rolled back"
				).log();
			} catch (HeuristicRollbackException exception) {
				throw new ServiceException(
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.HEURISTIC,
					phase.getPhase(),
					"A heuristic decision was made and all " +
					"relevant updates have been rolled back"
				).log();
			} catch (SecurityException exception) {
				throw new ServiceException(
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.HEURISTIC,
					phase.getPhase(),
					"The current thread is not allowed to commit " +
					"the transaction"
				).log();
			} catch (IllegalStateException exception) {
				throw new ServiceException(
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.HEURISTIC,
					phase.getPhase(),
					"The current thread is not associated with a " +
					"transaction"
				).log();
			} catch (SystemException exception) {
				throw new ServiceException(
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.HEURISTIC,
					phase.getPhase(),
					"The transaction manager encountered an " +
					"unexpected error condition"
				).log();
			}

		} catch (RuntimeException exception) {
			throw new RuntimeServiceException(
				rollback(
					transaction,
					synchronization,
					phase,
					new RuntimeServiceException(
						exception,
						BasicException.Code.DEFAULT_DOMAIN,
						BasicException.Code.GENERIC,
						phase.getPhase(),
						"Transaction processing aborted with " +
						"runtime exception"
					)
				)
			).log();
		}

		phase.setPhase("afterCommit");
		try {
			synchronization.afterCompletion(true);
		} catch (ServiceException exception) {
			throw new ServiceException(
				exception,
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.NONE,
				phase.getPhase(),
				"The afterCompletion code failed"
			).log();
		} catch (RuntimeException exception) {
			throw new RuntimeServiceException(
				exception,
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.NONE,
				phase.getPhase(),
				"The afterCompletion code failed"
			).log();
		}

	}

	/**
	 * 
	 * @param transaction
	 * @param synchronization
	 * @param rollbackPhase
	 * @param cause
	 */
	private static ServiceException rollback(
		UserTransaction transaction,
		Synchronization_1_0 synchronization,
		Phase phase,
		Exception cause
	){
        try {
            int status = transaction.getStatus();
            if(status != Status.STATUS_ROLLEDBACK) try { 
                //
                // Rolling Back
                // 
    			phase.setPhase("rollingBack");
                if(status != Status.STATUS_ROLLING_BACK) {
        			transaction.rollback();
                } else {
                    return noCommit(
                        synchronization,
                        phase,
                        new ServiceException(
                            cause,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.HEURISTIC,
                            phase.getPhase(),
                            "Rollback not invoked explicitely as the transaction " +
                            "is already rolling back, i.e. there is " +
                            "no synchronization for rollback outcome"
                        )
                    );
                }
    		} catch (SecurityException exception) {
    			return noCommit(
    				synchronization,
    				phase,
    				new ServiceException(
    					exception,
    					BasicException.Code.DEFAULT_DOMAIN,
    					BasicException.Code.HEURISTIC,
    					phase.getPhase(),
    					"The current thread is not allowed to roll " +
    					"back the transaction"
    				).appendCause(cause)
    			).log();
    		} catch (IllegalStateException exception) {
    			return noCommit(
    				synchronization,
    				phase,
    				new ServiceException(
    					exception,
    					BasicException.Code.DEFAULT_DOMAIN,
    					BasicException.Code.HEURISTIC,
    					phase.getPhase(),
    					"The current thread is not associated with a " +
    					"transaction"
    				).appendCause(cause)
    			).log();
            }
            //
            // Rolled Back
            // 
    		phase.setPhase("rolledBack");
    		return noCommit(
    			synchronization,
    			phase,
    			new ServiceException(
    				cause,
    				BasicException.Code.DEFAULT_DOMAIN,
    				BasicException.Code.ROLLBACK,
    				phase.getPhase(),
    				"Transaction rolled back"
    			)
    		);
        } catch (SystemException exception) {
            return noCommit(
                synchronization,
                phase,
                new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.HEURISTIC,
                    phase.getPhase(),
                    "The transaction manager encountered an " +
                    "unexpected error condition"
                ).appendCause(cause)
            ).log();
        }
	}

	/**
	 * After completion
	 * 
	 * @param synchronization
	 * @param status
	 */
	private static ServiceException noCommit(
		Synchronization_1_0 synchronization,
		Phase phase,
		ServiceException cause
	){
		try {
			synchronization.afterCompletion(false);
			return cause;
		} catch (ServiceException exception) {
			return new ServiceException(
				exception,
				exception.getExceptionDomain(),
				exception.getExceptionCode(),
				phase.getPhase(),
				"After completion code failed"
			).appendCause(cause);
		}
	}
	
	private static class Phase {
		
		void setPhase(
			String to
		){
			SysLog.trace("phase",to);
			this.phase[0] = new BasicException.Parameter("phase",to);
		}

		BasicException.Parameter[] getPhase(
		){
			return this.phase; 
		}

		private final BasicException.Parameter[] phase = new BasicException.Parameter[1];

	}

}
