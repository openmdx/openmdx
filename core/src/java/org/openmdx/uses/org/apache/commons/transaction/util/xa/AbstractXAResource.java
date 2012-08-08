/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/util/xa/AbstractXAResource.java,v 1.2 2008/03/21 18:42:19 hburger Exp $
 * $Revision: 1.2 $
 * $Date: 2008/03/21 18:42:19 $
 *
 * ====================================================================
 *
 * Copyright 2004 The Apache Software Foundation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openmdx.uses.org.apache.commons.transaction.util.xa;

import java.util.HashMap;
import java.util.Map;

import javax.transaction.Status;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.openmdx.uses.org.apache.commons.transaction.util.LoggerFacade;

/**
 * Abstract XAResource doing all the tedious tasks shared by many XAResouce implementations.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractXAResource implements XAResource, Status {

    // there might be at least one active transaction branch per thread
    private ThreadLocal activeTransactionBranch = new ThreadLocal();

    private Map suspendedContexts = new HashMap();
    private Map activeContexts = new HashMap();

    public abstract boolean isSameRM(XAResource xares) throws XAException;

    public abstract Xid[] recover(int flag) throws XAException;

    protected abstract LoggerFacade getLoggerFacade();

    protected abstract boolean includeBranchInXid();
    
    public void forget(Xid xid) throws XAException {
        if (getLoggerFacade().isFineEnabled()) {
            getLoggerFacade().logFine("Forgetting transaction branch " + xid);
        }
        TransactionalResource ts = getTransactionalResource(xid);
        if (ts == null) {
            throw new XAException(XAException.XAER_NOTA);
        }
        setCurrentlyActiveTransactionalResource(null);
        removeActiveTransactionalResource(xid);
        removeSuspendedTransactionalResource(xid);
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
        TransactionalResource ts = getTransactionalResource(xid);
        if (ts == null) {
            throw new XAException(XAException.XAER_NOTA);
        }

        if (getLoggerFacade().isFineEnabled()) {
            getLoggerFacade().logFine("Committing transaction branch " + ts);
        }

        if (ts.getStatus() == STATUS_MARKED_ROLLBACK) {
            throw new XAException(XAException.XA_RBROLLBACK);
        }

        if (ts.getStatus() != STATUS_PREPARED) {
            if (onePhase) {
                ts.prepare();
            } else {
                throw new XAException(XAException.XAER_PROTO);
            }
        }
        ts.commit();
        setCurrentlyActiveTransactionalResource(null);
        removeActiveTransactionalResource(xid);
        removeSuspendedTransactionalResource(xid);
    }

    public void rollback(Xid xid) throws XAException {
        TransactionalResource ts = getTransactionalResource(xid);
        if (ts == null) {
            throw new XAException(XAException.XAER_NOTA);
        }

        if (getLoggerFacade().isFineEnabled()) {
            getLoggerFacade().logFine("Rolling back transaction branch " + ts);
        }

        ts.rollback();
        setCurrentlyActiveTransactionalResource(null);
        removeActiveTransactionalResource(xid);
        removeSuspendedTransactionalResource(xid);
    }

    public int prepare(Xid xid) throws XAException {
        TransactionalResource ts = getTransactionalResource(xid);
        if (ts == null) {
            throw new XAException(XAException.XAER_NOTA);
        }

        if (getLoggerFacade().isFineEnabled()) {
            getLoggerFacade().logFine("Preparing transaction branch " + ts);
        }

        if (ts.getStatus() == STATUS_MARKED_ROLLBACK) {
            throw new XAException(XAException.XA_RBROLLBACK);
        }
        	
        int result = ts.prepare();
        ts.setStatus(STATUS_PREPARED);
        return result;
    }

    public void end(Xid xid, int flags) throws XAException {
        TransactionalResource ts = getActiveTransactionalResource(xid);
        if (ts == null) {
            throw new XAException(XAException.XAER_NOTA);
        }
        if (getCurrentlyActiveTransactionalResource() == null) {
            throw new XAException(XAException.XAER_INVAL);
        }
        if (getLoggerFacade().isFineEnabled()) {
	        getLoggerFacade().logFine(new StringBuffer(128)
	            .append("Thread ").append(Thread.currentThread())
	            .append(flags == TMSUSPEND ? " suspends" : flags == TMFAIL ? " fails" : " ends")
	            .append(" work on behalf of transaction branch ")
	            .append(ts).toString());
        }

        switch (flags) {
            case TMSUSPEND :
                ts.suspend();
                addSuspendedTransactionalResource(xid, ts);
                removeActiveTransactionalResource(xid);
                break;
            case TMFAIL :
                ts.setStatus(STATUS_MARKED_ROLLBACK);
                break;
            case TMSUCCESS :
                break;
        }
        setCurrentlyActiveTransactionalResource(null);
    }

    public void start(Xid xid, int flags) throws XAException {
        if (getCurrentlyActiveTransactionalResource() != null) {
            throw new XAException(XAException.XAER_INVAL);
        }
        if (getLoggerFacade().isFineEnabled()) {
            getLoggerFacade().logFine(new StringBuffer(128)
                    .append("Thread ").append(Thread.currentThread())
                    .append(flags == TMNOFLAGS ? " starts" : flags == TMJOIN ? " joins" : " resumes")
                    .append(" work on behalf of transaction branch ")
                    .append(xid).toString());
        }
        
        TransactionalResource ts;
        switch (flags) {
            // a new transaction
            case TMNOFLAGS :
            case TMJOIN :
            default :
                try {
                    ts = createTransactionResource(xid);
                    ts.begin();
                } catch (Exception e) {
					getLoggerFacade().logSevere("Could not create new transactional  resource", e);
					throw new XAException(e.getMessage());
				}
                break;
            case TMRESUME :
                ts = getSuspendedTransactionalResource(xid);
                if (ts == null) {
                    throw new XAException(XAException.XAER_NOTA);
                }
                ts.resume();
                removeSuspendedTransactionalResource(xid);
                break;
        }
        setCurrentlyActiveTransactionalResource(ts);
        addAcitveTransactionalResource(xid, ts);
    }

    abstract protected TransactionalResource createTransactionResource(Xid xid) throws Exception;

    protected TransactionalResource getCurrentlyActiveTransactionalResource() {
        TransactionalResource context = (TransactionalResource) activeTransactionBranch.get();
        return context;
    }

    protected void setCurrentlyActiveTransactionalResource(TransactionalResource context) {
        activeTransactionBranch.set(context);
    }

    protected TransactionalResource getTransactionalResource(Xid xid) {
    	TransactionalResource ts =  getActiveTransactionalResource(xid);
    	if (ts != null) return ts;
    	else return getSuspendedTransactionalResource(xid);
    }
    protected TransactionalResource getActiveTransactionalResource(Xid xid) {
        Xid wxid = XidWrapper.wrap(xid, includeBranchInXid());
        return (TransactionalResource) activeContexts.get(wxid);
    }

    protected TransactionalResource getSuspendedTransactionalResource(Xid xid) {
        Xid wxid = XidWrapper.wrap(xid, includeBranchInXid());
        return (TransactionalResource) suspendedContexts.get(wxid);
    }

    protected void addAcitveTransactionalResource(Xid xid, TransactionalResource txContext) {
        Xid wxid = XidWrapper.wrap(xid, includeBranchInXid());
        activeContexts.put(wxid, txContext);
    }

    protected void addSuspendedTransactionalResource(Xid xid, TransactionalResource txContext) {
        Xid wxid = XidWrapper.wrap(xid, includeBranchInXid());
        suspendedContexts.put(wxid, txContext);
    }

    protected void removeActiveTransactionalResource(Xid xid) {
        Xid wxid = XidWrapper.wrap(xid, includeBranchInXid());
        activeContexts.remove(wxid);
    }

    protected void removeSuspendedTransactionalResource(Xid xid) {
        Xid wxid = XidWrapper.wrap(xid, includeBranchInXid());
        suspendedContexts.remove(wxid);
    }

}
