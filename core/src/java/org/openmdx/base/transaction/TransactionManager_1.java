/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TransactionManager_1.java,v 1.15 2009/02/24 15:48:55 hburger Exp $
 * Description: Transaction Manager
 * Revision:    $Revision: 1.15 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/24 15:48:55 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The transaction manager unifies the exception handling.
 */
public class TransactionManager_1 {

	/**
	 * Constructor
	 */
	private TransactionManager_1(
	){
	    //  Avoid instantiation
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
			} 
			catch (NotSupportedException exception) {
				throw new ServiceException(
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.ROLLBACK,
					"The thread is already associated with a transaction " +
					"and the Transaction Manager implementation does not " +
					"support nested transactions.",
					phase.getPhase()
				);
			} 
			catch (Exception exception) {
				throw new ServiceException(
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.ROLLBACK,
					"The transaction manager encountered an " +
					"unexpected error condition",
					phase.getPhase()
				);
			}
			
			phase.setPhase("afterBegin");
			try {
				synchronization.afterBegin();
			} 
			catch (Exception exception) {
				throw rollback(transaction,synchronization,phase,exception);
			}
	
			phase.setPhase("beforeCompletion");
			try {
				synchronization.beforeCompletion();
			} 
			catch (Exception exception) {
				throw rollback(transaction,synchronization,phase,exception);
			}
	
			phase.setPhase("commit");
			try {
				transaction.commit();
			} 
			catch (RollbackException exception) {
				throw new ServiceException(
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.ROLLBACK,
					"Transaction rolled back",
					phase.getPhase()
				);
			} 
			catch (HeuristicMixedException exception) {
				throw new ServiceException(
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.HEURISTIC,
					"A heuristic decision was made and some " +
					"relevant updates have been committed " +
					"while others have been rolled back",
					phase.getPhase()
				).log();
			} 
			catch (HeuristicRollbackException exception) {
				throw new ServiceException(
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.HEURISTIC,
					"A heuristic decision was made and all " +
					"relevant updates have been rolled back",
					phase.getPhase()
				).log();
			} 
			catch (SecurityException exception) {
				throw new ServiceException(
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.HEURISTIC,
					"The current thread is not allowed to commit " +
					"the transaction",
					phase.getPhase()
				).log();
			} 
			catch (IllegalStateException exception) {
				throw new ServiceException(
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.HEURISTIC,
					"The current thread is not associated with a " +
					"transaction",
					phase.getPhase()
				).log();
			} 
			catch(Exception exception) {
				throw new ServiceException(
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.HEURISTIC,
					"The transaction manager encountered an " +
					"unexpected error condition",
					phase.getPhase()
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
						"Transaction processing aborted with " +
						"runtime exception",
						phase.getPhase()
					)
				)
			).log();
		}

		phase.setPhase("afterCommit");
		try {
			synchronization.afterCompletion(Status.STATUS_COMMITTED);
		} 
		catch (Exception exception) {
			throw new ServiceException(
				exception,
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.NONE,
				"The afterCompletion code failed",
				phase.getPhase()
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
                            "Rollback not invoked explicitely as the transaction " +
                            "is already rolling back, i.e. there is " +
                            "no synchronization for rollback outcome",
                            phase.getPhase()
                        ), cause
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
    					"The current thread is not allowed to roll " +
    					"back the transaction",
    					phase.getPhase()
    				), 
    				cause
    			).log();
    		} catch (IllegalStateException exception) {
    			return noCommit(
    				synchronization,
    				phase,
    				new ServiceException(
    					exception,
    					BasicException.Code.DEFAULT_DOMAIN,
    					BasicException.Code.HEURISTIC,
    					"The current thread is not associated with a " +
    					"transaction",
    					phase.getPhase()
    				), 
    				cause
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
    				"Transaction rolled back",
    				phase.getPhase()
    			), cause
    		);
        } catch (SystemException exception) {
            return noCommit(
                synchronization,
                phase,
                new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.HEURISTIC,
                    "The transaction manager encountered an " +
                    "unexpected error condition",
                    phase.getPhase()
                ), 
                cause
            ).log();
        }
	}

	/**
	 * After completion
	 * 
	 * @param synchronization
	 * @param initialCause TODO
	 * @param status
	 */
	private static ServiceException noCommit(
		Synchronization_1_0 synchronization,
		Phase phase,
		ServiceException cause, 
		Throwable initialCause
	){
		try {
			synchronization.afterCompletion(Status.STATUS_ROLLEDBACK);
			return cause;
		} 
		catch (Exception exception) {
		    ServiceException serviceException = new ServiceException(
				exception,
				phase.getPhase()
			);
		    serviceException.getCause(null).initCause(cause);
		    return serviceException;
		}
	}
	
	static class Phase {

	    /**
	     * The logger instance
	     */
	    private static Logger logger = LoggerFactory.getLogger(TransactionManager_1.class);
	    	    
		void setPhase(
			String to
		){
			logger.trace("Setting to phase \"{}\"",to);
			this.phase[0] = new BasicException.Parameter("phase",to);
		}

		BasicException.Parameter[] getPhase(
		){
			return this.phase; 
		}

		private final BasicException.Parameter[] phase = new BasicException.Parameter[1];

	}

}
