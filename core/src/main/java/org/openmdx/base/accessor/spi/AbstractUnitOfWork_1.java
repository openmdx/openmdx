/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: AbstractTransaction_1 
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

import javax.jdo.JDOException;
import javax.jdo.JDOFatalInternalException;
import javax.jdo.spi.PersistenceCapable;

import org.openmdx.base.persistence.cci.Synchronization;
import org.openmdx.base.persistence.spi.UnitOfWork;
import org.openmdx.base.transaction.Status;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.jdo.ReducedJDOHelper;

/**
 * AbstractTransaction_1
 */
public abstract class AbstractUnitOfWork_1 implements UnitOfWork {

    /**
     * Provide the delegate
     * 
     * @return the delegate
     */
    protected abstract UnitOfWork getDelegate();
    
    /**
     * Marshal a JDOException's failedObject instances
     * 
     * @param source a JDOException
     * 
     * @return the marshalled JDOException
     */
    protected JDOException marshal(
        JDOException source
    ){
        if(source instanceof JDOFatalInternalException) {
            return source;
        } else {
            JDOException target = source;
            Object failedObject = source.getFailedObject();
            if(failedObject instanceof PersistenceCapable) {
                Object objectId = ReducedJDOHelper.getObjectId(failedObject);
                try {
                    target = source.getClass().getConstructor(
                        String.class,
                        Throwable[].class,
                        Object.class
                    ).newInstance(
                        source.getMessage(),
                        source.getNestedExceptions(),
                        getPersistenceManager().getObjectById(objectId)
                    );
                } catch (Exception exception) {
                    try {
                        target = source.getClass().getConstructor(
                            String.class,
                            Throwable[].class
                        ).newInstance(
                            source.getMessage() + " (" + objectId + ")",
                            source.getNestedExceptions()
                        );
                    } catch (Exception retry) {
                        throw BasicException.initHolder(
                            new JDOFatalInternalException(
                                "Could not marshal JDOException with failed object",
                                BasicException.newEmbeddedExceptionStack(
                                    source,
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.TRANSFORMATION_FAILURE,
                                    new BasicException.Parameter("objectId", objectId)
                                )
                           )
                        );
                    }
                }
            }
            Throwable[] nestedExceptions = target.getNestedExceptions();
            if(nestedExceptions != null) {
                for(int i = 0; i < nestedExceptions.length; i++) {
                    Throwable nestedSource = nestedExceptions[i];
                    if(nestedSource instanceof JDOException) {
                        try {
                            JDOException nestedTarget = marshal((JDOException)nestedSource);
                            if(nestedTarget != nestedSource) {
                                nestedExceptions[i] = nestedTarget;
                            }
                        } catch (Exception forget) {
                            marshal((JDOException)nestedSource);
                        }
                    }
                }
            }
            return target;
        }
    }
    
    @Override
    public void begin() {
        this.getDelegate().begin();
    }

    @Override
    public void commit() {
        try {
            this.getDelegate().commit();
        } catch (JDOException exception) {
            throw this.marshal(exception);
        }
    }

    @Override
    public String getIsolationLevel(
    ) {
        return this.getDelegate().getIsolationLevel();
    }

    @Override
    public boolean getNontransactionalRead(
    ) {
        return this.getDelegate().getNontransactionalRead();
    }

    @Override
    public boolean getNontransactionalWrite(
    ) {
        return this.getDelegate().getNontransactionalWrite();
    }

    @Override
    public boolean getOptimistic(
    ) {
        return this.getDelegate().getOptimistic();
    }

    @Override
    public boolean getRestoreValues(
    ) {
        return this.getDelegate().getRestoreValues();
    }

    @Override
    public boolean getRetainValues(
    ) {
        return this.getDelegate().getRetainValues();
    }

    @Override
    public boolean getRollbackOnly(
    ) {
        return this.getDelegate().getRollbackOnly();
    }

    @Override
    public boolean isActive(
    ) {
        UnitOfWork delegate = this.getDelegate();
        return delegate != null && delegate.isActive();
    }

    @Override
    public void rollback(
    ) {
        this.getDelegate().rollback();
    }

    @Override
    public void setIsolationLevel(
        String level
    ) {
        this.getDelegate().setIsolationLevel(level);
    }

    @Override
    public void setNontransactionalRead(
        boolean nontransactionalRead
    ) {
        this.getDelegate().setNontransactionalRead(nontransactionalRead);
    }

    @Override
    public void setNontransactionalWrite(
        boolean nontransactionalWrite
    ) {
        this.getDelegate().setNontransactionalWrite(nontransactionalWrite);
    }

    @Override
    public void setOptimistic(
        boolean optimistic
    ) {
        this.getDelegate().setOptimistic(optimistic);
    }

    @Override
    public void setRestoreValues(
        boolean restoreValues
    ) {
        this.getDelegate().setRestoreValues(restoreValues);
    }

    @Override
    public void setRetainValues(
        boolean retainValues
    ) {
        this.getDelegate().setRetainValues(retainValues);
    }

    @Override
    public void setRollbackOnly(
    ) {
        this.getDelegate().setRollbackOnly();
    }

    @Override
    public void setSynchronization(
        Synchronization sync
    ) {
        this.getDelegate().setSynchronization(sync);
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.jdo.LocalTransaction#getSynchronization()
     */
    @Override
    public Synchronization getSynchronization() {
        return this.getDelegate().getSynchronization();
    }

    
    //-----------------------------------------------------------------------
    // Implements Synchronization
    //-----------------------------------------------------------------------
    
    @Override
    public void afterCompletion(
        Status status
    ) {
        this.getDelegate().afterCompletion(status);        
    }

    @Override
    public void beforeCompletion(
    ) {
        this.getDelegate().beforeCompletion();                
    }

    
    //-----------------------------------------------------------------------
    // Implements UnitOfWork
    //-----------------------------------------------------------------------

    /* (non-Javadoc)
	 * @see org.openmdx.base.persistence.cci.UnitOfWork#setForgetOnly()
	 */
    @Override
	public void setForgetOnly() {
        this.getDelegate().setForgetOnly();                
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.persistence.cci.UnitOfWork#isForgetOnly()
	 */
    @Override
	public boolean isForgetOnly() {
        return this.getDelegate().isForgetOnly();                
	}

	
    //-----------------------------------------------------------------------
    // Implements Synchronization_2_0
    //-----------------------------------------------------------------------

	/* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.Synchronization_2_0#afterBegin()
     */
    @Override
    public void afterBegin() {
        this.getDelegate().afterBegin();                
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.Synchronization_2_0#clear()
     */
    @Override
    public void clear() {
        this.getDelegate().clear();                
    }
   
    /**
     * Retrieve a delegate of the given type
     * 
     * @param type
     * @param transaction
     * 
     * @return the delegate of the given type
     * 
     * @exception NullPointerException if either argument is {@code null}
     * @exception IllegalArgumentException if the transaction has no delegate of the given type
     */
    public static <T> T getDelegate(
    	Class<T> type,
    	Object transaction
    ){
    	Object current = transaction;
    	while(!type.isInstance(current)) {
    		if(current instanceof AbstractUnitOfWork_1) {
        		current = ((AbstractUnitOfWork_1)current).getDelegate();
    		} else {
    	    	throw new IllegalArgumentException(
	        		"The given transaction has no delegate of type " + type.getName() + ": " +  transaction.getClass().getName()
	        	);
    		}
    	}
		return type.cast(current);
    }
    
}
