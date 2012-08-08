/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LightweightTransaction.java,v 1.9 2011/09/16 08:53:39 hburger Exp $
 * Description: Lightweight Transaction
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/09/16 08:53:39 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2011, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package org.openmdx.kernel.lightweight.transaction;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;

/**
 * JTA Transaction implementation.
 */
final class LightweightTransaction implements Transaction {
    
    
    // -------------------------------------------------------------- Constants
    
    /**
     * The string representation of the status values
     */
    private static final String[] STATI = {
        //
        // A transaction is associated with the target object and it is in the
        // active state. An implementation returns this status after a
        // transaction has been started and prior to a Coordinator issuing
        // any prepares, unless the transaction has been marked for rollback.
        //  
        "STATUS_ACTIVE",
        //
        // A transaction is associated with the target object and it has been
        // marked for rollback, perhaps as a result of a setRollbackOnly operation.
        //
        "STATUS_MARKED_ROLLBACK",
        //
        // A transaction is associated with the target object and it has been
        // prepared. That is, all subordinates have agreed to commit. The
        // target object may be waiting for instructions from a superior as to how
        // to proceed.
        //  
        "STATUS_PREPARED",
        //
        // A transaction is associated with the target object and it has been
        // committed. It is likely that heuristics exist; otherwise, the
        // transaction would have been destroyed and NoTransaction returned.
        //  
        "STATUS_COMMITTED",
        //
        // A transaction is associated with the target object and the outcome
        // has been determined to be rollback. It is likely that heuristics exist;
        // otherwise, the transaction would have been destroyed and NoTransaction
        // returned.
        //  
        "STATUS_ROLLEDBACK",
        //
        // A transaction is associated with the target object but its
        // current status cannot be determined. This is a transient condition
        // and a subsequent invocation will ultimately return a different status.
        //
        "STATUS_UNKNOWN",
        //
        // No transaction is currently associated with the target object. This
        // will occur after a transaction has completed.
        //
        "STATUS_NO_TRANSACTION",
        //
        // A transaction is associated with the target object and it is in the
        // process of preparing. An implementation returns this status if it
        // has started preparing, but has not yet completed the process. The
        // likely reason for this is that the implementation is probably
        // waiting for responses to prepare from one or more
        // Resources.
        // 
        "STATUS_PREPARING",
        //
        // A transaction is associated with the target object and it is in the
        // process of committing. An implementation returns this status if it
        // has decided to commit but has not yet completed the committing process. 
        // This occurs because the implementation is probably waiting for 
        // responses from one or more Resources.
        //  
        "STATUS_COMMITTING",
        //
        // A transaction is associated with the target object and it is in the
        // process of rolling back. An implementation returns this status if
        // it has decided to rollback but has not yet completed the process.
        // The implementation is probably waiting for responses from one or more
        // Resources.
        //  
        "STATUS_ROLLING_BACK"
    };
    
    /**
     * The default transaction timeout in seconds
     */
    public static final int DEFAULT_TRANSACTION_TIMEOUT = 60;

    // ------------------------------------------------------------ Constructor
    
    
    /**
     * Constructor.
     */
    LightweightTransaction(TransactionIdFactory xidFactory) {
        this.xidFactory = xidFactory;
        this.xid = xidFactory.createTransactionId();
    }
    
    
    // ----------------------------------------------------- Instance Variables
    
    /**
     * Transaction Id factory
     */
    private final TransactionIdFactory xidFactory;   
    
    /**
     * Global transaction id.
     */
    final Xid xid;
    
    /**
     * Branches.
     * Keyed : branch xid -> resource manager.
     */
    private final Map<Xid,XAResource> branches = new HashMap<Xid,XAResource>();
    
    /**
     * Active branches.
     * Keyed : resource manager -> branches xid.
     */
    private final Map<XAResource,Xid> activeBranches = new HashMap<XAResource,Xid>();
    
    /**
     * Enlisted resources.
     */
    private final List<XAResource> enlistedResources = new ArrayList<XAResource>();
    
    /**
     * Suspended resources.
     * Keyed : resource manager -> branches xid.
     */
    private Map<XAResource,Xid> suspendedResources = new HashMap<XAResource,Xid>();
    
    /**
     * Transaction status.
     */
    int status = Status.STATUS_ACTIVE;
    
    /**
     * Synchronization objects.
     */
    private final List<Synchronization> synchronizationObjects = new ArrayList<Synchronization>();
    
    /**
     * Branch counter.
     */
    private int branchCounter = 1;
    
    /**
     * Map of resources being managed for the transaction 
     */
    final Map<Object,Object> managedResources = new HashMap<Object,Object>();
    
    /**
     * A Synchronization instance with special ordering semantics. Its beforeCompletion will 
     * be called after all SessionSynchronization beforeCompletion callbacks and callbacks 
     * registered directly with the Transaction, but before the 2-phase commit process starts. 
     * Similarly, the afterCompletion callback will be called after 2-phase commit completes 
     * but before any SessionSynchronization and Transaction afterCompletion callbacks.
     */
    Synchronization interposedSynchronization;
    
    
    // ------------------------------------------------------------- Properties
    
    /**
     * The default transaction timeout in seconds
     */
    @SuppressWarnings("unused")
    private int timeout = DEFAULT_TRANSACTION_TIMEOUT;

    /**
     * Modify the value of the timeout value that is associated with the
     * transactions started by the current thread with the begin method.
     * <p>
     * If an application has not called this method, the transaction service
     * uses some default value for the transaction timeout.
     *
     * @param timeout The value of the timeout in seconds. If the value is
     * zero, the transaction service restores the default value.
     */
    void setTimeout(
        int timeout
    ){
        this.timeout = timeout <= 0 ? DEFAULT_TRANSACTION_TIMEOUT : timeout;        
    }
    
    // ---------------------------------------------------- Transaction Methods
    
    /**
     * Call synchronized objects beforeCompletion
     * 
     * @return the exceptions raised during beforeCompletion()
     * 
     * @throws SystemException 
     * @throws IllegalStateException 
     */
    private List<Throwable> beforeCompletion(
    ) throws IllegalStateException, SystemException{
        List<Throwable> warnings = null;
        if(this.interposedSynchronization != null) try {
            this.interposedSynchronization.beforeCompletion();
        } catch (RuntimeException exception) {
            if(warnings == null) {
                warnings = new ArrayList<Throwable>();
            }
            warnings.add(
                Throwables.log(BasicException.newStandAloneExceptionStack(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ROLLBACK,
                    "Transaction.beforeCompletionFail",
                    new BasicException.Parameter("xid", this.xid),
                    new BasicException.Parameter("status", getStatusAsString())
                ))
            );
            this.setRollbackOnly();
        }
        for(Synchronization synchronization : synchronizationObjects) try {
            synchronization.beforeCompletion();
        } catch (RuntimeException exception) {
            if(warnings == null) {
                warnings = new ArrayList<Throwable>();
            }
            warnings.add(
                Throwables.log(BasicException.newStandAloneExceptionStack(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ROLLBACK,
                    "Transaction.beforeCompletionFail",
                    new BasicException.Parameter("xid", this.xid),
                    new BasicException.Parameter("status", getStatusAsString())
                ))
            );
            this.setRollbackOnly();
        }
        return warnings == null ? Collections.<Throwable>emptyList(): warnings;
    }

    /**
     * Call synchronized objects afterCompletion
     * 
     * @return the warnings raised during afterCompletion()
     * 
     * @param status the completion status
     * @param exceptions the exception sink
     */
    private List<Throwable> afterCompletion(
        int status
    ){
        List<Throwable> warnings = null;
        if(this.interposedSynchronization != null) try {
            this.interposedSynchronization.afterCompletion(status);
        } catch (RuntimeException exception) {
            if(warnings == null) {
                warnings = new ArrayList<Throwable>();
            }
            warnings.add(
                Throwables.log(BasicException.newStandAloneExceptionStack(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.GENERIC,
                    "Transaction.afterCompletionFail",
                    new BasicException.Parameter("xid", this.xid),
                    new BasicException.Parameter("status", getStatusAsString())
                ))
            );
        }
        for(Synchronization synchronization : synchronizationObjects) try {
            synchronization.afterCompletion(status);
        } catch (RuntimeException exception) {
            if(warnings == null) {
                warnings = new ArrayList<Throwable>();
            }
            warnings.add(
                Throwables.log(BasicException.newStandAloneExceptionStack(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.GENERIC,
                    "Transaction.afterCompletionFail",
                    new BasicException.Parameter("xid", this.xid),
                    new BasicException.Parameter("status", getStatusAsString())
                ))
            );
        }
        if(warnings == null) {
            return Collections.emptyList();
        } else {
            return warnings;
        }
    }
    
    /**
     * Complete the transaction represented by this Transaction object.
     *
     * @exception RollbackException Thrown to indicate that the transaction
     * has been rolled back rather than committed.
     * @exception HeuristicMixedException Thrown to indicate that a heuristic
     * decision was made and that some relevant updates have been committed
     * while others have been rolled back.
     * @exception HeuristicRollbackException Thrown to indicate that a
     * heuristic decision was made and that some relevant updates have been
     * rolled back.
     * @exception SecurityException Thrown to indicate that the thread is not
     * allowed to commit the transaction.
     * @exception IllegalStateException Thrown if the current thread is not
     * associated with a transaction.
     * @exception SystemException Thrown if the transaction manager encounters
     * an unexpected error condition.
     */
	public void commit(
	) throws RollbackException, HeuristicMixedException, HeuristicRollbackException, 
    SecurityException, IllegalStateException, SystemException {
        Vector<Throwable> exceptions = new Vector<Throwable>();
        // Check status ACTIVE
        if (status != Status.STATUS_ACTIVE)
            throw new IllegalStateException();
        
        exceptions.addAll(beforeCompletion());
        
        if (status == Status.STATUS_MARKED_ROLLBACK) {
            rollback();
            return;
        }

        boolean fail = false;
        
        Set<Xid> keys = branches.keySet();
        
        switch (enlistedResources.size()) {
            case 0: // Trivial commit 
                status = Status.STATUS_COMMITTED;
                break;
            case 1: // One phase commit
                status = Status.STATUS_COMMITTING;
                for(Object key : keys) {
                    XAResource resourceManager = branches.get(key);
                    try {
                        if (!fail)
                            resourceManager.commit(xid, true);
                        else
                            resourceManager.rollback(xid);
                    } catch (Throwable e) {
                        // Adding the exception to the error code list
                        exceptions.addElement(
                    		BasicException.newStandAloneExceptionStack(
                            	e,	
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.GENERIC,
                                "Transaction.commitFail",
                                new BasicException.Parameter("resourceManager", resourceManager),
                                new BasicException.Parameter("xaErrorCode", getXAErrorCode(e)),
                                new BasicException.Parameter("xid", this.xid),
                                new BasicException.Parameter("status", getStatusAsString())
                            )
                        );
                        fail = true;
                        status = Status.STATUS_MARKED_ROLLBACK;
                    }
                }
                
                status = fail ? Status.STATUS_ROLLEDBACK : Status.STATUS_COMMITTED;
                break;
            default:         
                // Prepare each enlisted resource
                status = Status.STATUS_PREPARING;
                Branches: for(Object key : keys) {
                    XAResource resourceManager = branches.get(key);
                    try {
                        // Preparing the resource manager using its branch xid
                        resourceManager.prepare((Xid) key);
                    } catch (Throwable e) {
                        // Adding the exception to the error code list
                        exceptions.addElement(
                            BasicException.newStandAloneExceptionStack(
                            	e,	
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.GENERIC,
                                "Transaction.prepareFail",
                                new BasicException.Parameter("resourceManager", resourceManager),
                                new BasicException.Parameter("xaErrorCode", getXAErrorCode(e)),
                                new BasicException.Parameter("xid", this.xid),
                                new BasicException.Parameter("status", getStatusAsString())
                            )
                        );
                        fail = true;
                        break Branches;
                    }
                }
                status = fail ? Status.STATUS_MARKED_ROLLBACK : Status.STATUS_PREPARED;

                // If fail, rollback
                if (fail) {
                    status = Status.STATUS_ROLLING_BACK;
                    fail = false;
                    // Rolling back all the prepared (and unprepared) branches
                    keys = branches.keySet();
                    for(Object key : keys) {
                        XAResource resourceManager = branches.get(key);
                        try {
                            resourceManager.rollback((Xid) key);
                        } catch(Throwable e) {
                            // Adding the exception to the error code list
                            exceptions.addElement(
                                Throwables.log(BasicException.newStandAloneExceptionStack(
                                	e,
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.GENERIC,
                                    "Transaction.rollbackFail",
                                    new BasicException.Parameter("resourceManager", resourceManager),
                                    new BasicException.Parameter("xaErrorCode", getXAErrorCode(e)),
                                    new BasicException.Parameter("xid", this.xid),
                                    new BasicException.Parameter("status", getStatusAsString())
                                ))
                            ); 
                            fail = true;
                        }
                    }
                    status = Status.STATUS_ROLLEDBACK;
                } else {
                    status = Status.STATUS_COMMITTING;
                    // Commit each enlisted resource
                    keys = branches.keySet();
                    for(Object key : keys) {
                        XAResource resourceManager = branches.get(key);
                        try {
                            resourceManager.commit((Xid) key, false);
                        } catch(Throwable e) {
                            // Adding the exception to the error code list
                            exceptions.addElement(
                                Throwables.log(BasicException.newStandAloneExceptionStack(
                                	e,
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.GENERIC,
                                    "Transaction.commitFail",
                                    new BasicException.Parameter("resourceManager", resourceManager),
                                    new BasicException.Parameter("xaErrorCode", getXAErrorCode(e)),
                                    new BasicException.Parameter("xid", this.xid),
                                    new BasicException.Parameter("status", getStatusAsString())
                                ))
                            );
                            fail = true;
                        }
                    }
                    status = Status.STATUS_COMMITTED;
                }            
        }
        
        // Call synchronized objects afterCompletion
        exceptions.addAll(afterCompletion(status));
        
        // Parsing exception and throwing an appropriate exception
        if(!exceptions.isEmpty()) switch (status) {
            case Status.STATUS_ROLLEDBACK: 
                if(fail) {
                    HeuristicRollbackException heuristicException = new HeuristicRollbackException();
                    if(exceptions.size() == 1) heuristicException.initCause(exceptions.get(0));
                    throw heuristicException;
                } else {
                    throw new RollbackException();
                }
            case Status.STATUS_COMMITTED : 
                if (fail) {
                    HeuristicMixedException heuristicException = new HeuristicMixedException();
                    if(exceptions.size() == 1) heuristicException.initCause(exceptions.get(0));
                    throw heuristicException;
                } else {
                    break;
                }
        }

    }
    
    
    /**
     * Delist the resource specified from the current transaction associated
     * with the calling thread.
     *
     * @param xaRes The XAResource object representing the resource to delist
     * @param flag One of the values of TMSUCCESS, TMSUSPEND, or TMFAIL
     * @exception IllegalStateException Thrown if the transaction in the
     * target object is inactive.
     * @exception SystemException Thrown if the transaction manager encounters
     * an unexpected error condition.
     */
	public boolean delistResource(XAResource xaRes, int flag)
        throws IllegalStateException, SystemException {
        // Check status ACTIVE
        if (status != Status.STATUS_ACTIVE)
            throw new IllegalStateException();
        
        Xid xid = activeBranches.get(xaRes);
        
        if (xid == null)
            throw new IllegalStateException();
        
        activeBranches.remove(xaRes);
        
        SysLog.log(
            Level.FINEST,
            "Delist xaResource {0} from transaction {1} ({2})",
            xaRes, getXAFlag(flag), this.xid
        );
        
        try {
            xaRes.end(xid, flag);
        } catch (XAException e) {
            Throwables.log(BasicException.newStandAloneExceptionStack(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.GENERIC,
                "Transaction.delistFail",
                new BasicException.Parameter("xaResource", xaRes),
                new BasicException.Parameter("xaErrorCode", getXAErrorCode(e)),
                new BasicException.Parameter("xid", this.xid),
                new BasicException.Parameter("status", getStatusAsString())
            ));
            return false;
        }

        if (flag == XAResource.TMSUSPEND)
            suspendedResources.put(xaRes, xid);

        return true;
        
    }
    
    
    /**
     * Enlist the resource specified with the current transaction context of
     * the calling thread.
     *
     * @param xaRes The XAResource object representing the resource to delist
     * @return true if the resource was enlisted successfully; otherwise false.
     * @exception RollbackException Thrown to indicate that the transaction
     * has been marked for rollback only.
     * @exception IllegalStateException Thrown if the transaction in the
     * target object is in prepared state or the transaction is inactive.
     * @exception SystemException Thrown if the transaction manager
     * encounters an unexpected error condition.
     */
    public boolean enlistResource(XAResource xaRes)
        throws RollbackException, IllegalStateException, SystemException {

        if (status == Status.STATUS_MARKED_ROLLBACK)
            throw new RollbackException();
        
        // Check status ACTIVE
        if (status != Status.STATUS_ACTIVE)
            throw new IllegalStateException();
        
        // Preventing two branches from being active at the same time on the
        // same resource manager
        Xid activeXid = activeBranches.get(xaRes);
        if (activeXid != null)
            return false;
        
        boolean alreadyEnlisted = false;
        int flag = XAResource.TMNOFLAGS;
        
        Xid branchXid = suspendedResources.get(xaRes);
        
        if (branchXid == null) {
            Resources: for(XAResource resourceManager : enlistedResources){
                try {
                    if (resourceManager.isSameRM(xaRes)) {
                        flag = XAResource.TMJOIN;
                        alreadyEnlisted = true;
                        break Resources;
                    }
                } catch (XAException e) {
                    // ignore
                }
            }
            branchXid = this.xidFactory.createTransactionBranchId(this.xid, branchCounter++);
        } else {
            alreadyEnlisted = true;
            flag = XAResource.TMRESUME;
            suspendedResources.remove(xaRes);
        }
        
        SysLog.log(
        	Level.FINEST,
            "Enlist xaResource {0} with transaction {1} ({2})",
            xaRes, getXAFlag(flag), this.xid
        );
        
        try {
            xaRes.start(branchXid, flag);
        } catch (XAException xaException) {
            throw Throwables.initCause(
                new SystemException(
                    "Transaction.enlistFail: " + xaException.getMessage()
                ),
                xaException,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.GENERIC,
                new BasicException.Parameter("xaResource", xaRes),
                new BasicException.Parameter("xaErrorCode", getXAErrorCode(xaException)),
                new BasicException.Parameter("xid", this.xid),
                new BasicException.Parameter("branch", branchXid),
                new BasicException.Parameter("status", getStatusAsString()),
                new BasicException.Parameter("flag", getXAFlag(flag))
            );
        }
        
        if (!alreadyEnlisted) {
            enlistedResources.add(xaRes);
        }
        
        branches.put(branchXid, xaRes);
        activeBranches.put(xaRes, branchXid);
        
        return true;
        
    }
    
    
    /**
     * Roll back the transaction associated with the current thread. When
     * this method completes, the thread becomes associated with no
     * transaction.
     *
     * @exception SecurityException Thrown to indicate that the thread is not
     * allowed to commit the transaction.
     * @exception IllegalStateException Thrown if the current thread is not
     * associated with a transaction.
     * @exception SystemException Thrown if the transaction manager encounters
     * an unexpected error condition.
     */
	public void rollback()
        throws SecurityException, IllegalStateException, SystemException {
        // Check status ACTIVE
        if (status != Status.STATUS_ACTIVE && status != Status.STATUS_MARKED_ROLLBACK)
            throw new IllegalStateException();
        
        Vector<Throwable> exceptions = new Vector<Throwable>();
        
        status = Status.STATUS_ROLLING_BACK;
        for(Xid xid : branches.keySet()) {
            XAResource resourceManager = branches.get(xid);
            try {
                resourceManager.rollback(xid);
            } catch (Throwable e) {
                // Adding the exception to the error code list
                exceptions.addElement(
                    Throwables.log(BasicException.newStandAloneExceptionStack(
                    	e,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.GENERIC,
                        "Transaction.rollbackFail",
                        new BasicException.Parameter("resourceManager", resourceManager),
                        new BasicException.Parameter("xaErrorCode", getXAErrorCode(e)),
                        new BasicException.Parameter("xid", this.xid),
                        new BasicException.Parameter("status", getStatusAsString())
                    ))
                );
            }
        }
        status = Status.STATUS_ROLLEDBACK;
		
        // Call synchronized objects afterCompletion
        if(this.interposedSynchronization != null) {
            this.interposedSynchronization.afterCompletion(status);
        }
        for(Synchronization synchronization : synchronizationObjects) {
            synchronization.afterCompletion(status);
        }
        
    }
    
    
    /**
     * Modify the transaction associated with the current thread such that
     * the only possible outcome of the transaction is to roll back the
     * transaction.
     *
     * @exception IllegalStateException Thrown if the current thread is not
     * associated with a transaction.
     * @exception SystemException Thrown if the transaction manager encounters
     * an unexpected error condition.
     */
    public void setRollbackOnly()
        throws IllegalStateException, SystemException {
        status = Status.STATUS_MARKED_ROLLBACK;
    }
    
    
    /**
     * Obtain the status of the transaction associated with the current thread.
     *
     * @exception SystemException Thrown if the transaction manager encounters
     * an unexpected error condition.
     * @return The transaction status. If no transaction is associated with
     * the current thread, this method returns the Status.NoTransaction value.
     */
    public int getStatus()
        throws SystemException {
        return status;
    }
    
    
    /**
     * Register a synchronization object for the transaction currently
     * associated with the calling thread. The transction manager invokes the
     * beforeCompletion method prior to starting the transaction commit
     * process. After the transaction is completed, the transaction manager
     * invokes the afterCompletion method.
     *
     * @param sync The Synchronization object for the transaction associated
     * with the target object.
     * @exception RollbackException Thrown to indicate that the transaction
     * has been marked for rollback only.
     * @exception IllegalStateException Thrown if the transaction in the
     * target object is in prepared state or the transaction is inactive.
     * @exception SystemException Thrown if the transaction manager encounters
     * an unexpected error condition.
     */
    public void registerSynchronization(
    	Synchronization sync
    ) throws RollbackException, IllegalStateException, SystemException {
        if (status == Status.STATUS_MARKED_ROLLBACK) throw new RollbackException(
        	"Invalid transaction status " + getStatusAsString()
        );
        if (status != Status.STATUS_ACTIVE) throw new IllegalStateException(
        	"Invalid transaction status " + getStatusAsString()
        );
        synchronizationObjects.add(sync);
    }
    
    
    // --------------------------------------------------------- Public Methods
    
    
    /**
     * Return a String representation of the error code contained in a
     * XAException.
     */
    public static String getXAErrorCode(Throwable throww) {
        String result = null;
        if (throww instanceof XAException)
            result = getXAErrorCode((XAException)throww);
        else {
            StringWriter sw = new StringWriter();
            throww.printStackTrace( new PrintWriter(sw, true) ); //autoFlush=true
            result = sw.toString();
        }
        return result;
    }
    
    /**
     * Return a String representation of the error code contained in an
     * XAException.
     */
    public static String getXAErrorCode(XAException xae) {
        switch (xae.errorCode) {
            case XAException.XA_HEURCOM:
                return "XA_HEURCOM";
            case XAException.XA_HEURHAZ:
                return "XA_HEURHAZ";
            case XAException.XA_HEURMIX:
                return "XA_HEURMIX";
            case XAException.XA_HEURRB:
                return "XA_HEURRB";
            case XAException.XA_NOMIGRATE:
                return "XA_NOMIGRATE";
            case XAException.XA_RBBASE:
                return "XA_RBBASE";
            case XAException.XA_RBCOMMFAIL:
                return "XA_RBCOMMFAIL";
            case XAException.XA_RBDEADLOCK:
                return "XA_RBBEADLOCK";
            case XAException.XA_RBEND:
                return "XA_RBEND";
            case XAException.XA_RBINTEGRITY:
                return "XA_RBINTEGRITY";
            case XAException.XA_RBOTHER:
                return "XA_RBOTHER";
            case XAException.XA_RBPROTO:
                return "XA_RBPROTO";
            case XAException.XA_RBTIMEOUT:
                return "XA_RBTIMEOUT";
            case XAException.XA_RDONLY:
                return "XA_RDONLY";
            case XAException.XA_RETRY:
                return "XA_RETRY";
            case XAException.XAER_ASYNC:
                return "XAER_ASYNC";
            case XAException.XAER_DUPID:
                return "XAER_DUPID";
            case XAException.XAER_INVAL:
                return "XAER_INVAL";
            case XAException.XAER_NOTA:
                return "XAER_NOTA";
            case XAException.XAER_OUTSIDE:
                return "XAER_OUTSIDE";
            case XAException.XAER_PROTO:
                return "XAER_PROTO";
            case XAException.XAER_RMERR:
                return "XAER_RMERR";
            case XAException.XAER_RMFAIL:
                return "XAER_RMFAIL";
            default:
                return "UNKNOWN";
        }
    }
    
    
    /**
     * Return a String representation of a flag.
     */
    public static String getXAFlag(int flag) {
        switch (flag) {
            case XAResource.TMENDRSCAN:
                return "TMENDRSCAN";
            case XAResource.TMFAIL:
                return "TMFAIL";
            case XAResource.TMJOIN:
                return "TMJOIN";
            case XAResource.TMNOFLAGS:
                return "TMNOFLAGS";
            case XAResource.TMONEPHASE:
                return "TMONEPHASE";
            case XAResource.TMRESUME:
                return "TMRESUME";
            case XAResource.TMSTARTRSCAN:
                return "TMSTARTRSCAN";
            case XAResource.TMSUCCESS:
                return "TMSUCCESS";
            case XAResource.TMSUSPEND:
                return "TMSUSPEND";
            default:
                return "UNKNOWN";
        }
    }    
    
    /**
     * Print the Transaction object in a debugger friendly manner
     */
    @Override
    public String toString(
    ) {
        return getClass().getName() + ": " + xid + " (" + getStatusAsString() + ")";
    }
        
    /**
     * Provide the <code>String</code> representation of the transaction state
     * 
     * @return the <code>String</code> representation of the transaction state
     */
    private String getStatusAsString(){
        return  status >= 0 && status < STATI.length ? STATI[status] : Integer.toString(status); 
    }
    
}