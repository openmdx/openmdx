/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: JmiServiceException.java,v 1.15 2007/10/10 16:05:51 hburger Exp $
 * Description: JmiServiceException class
 * Revision:    $Revision: 1.15 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:05:51 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.jmi.cci;

import java.io.PrintStream;
import java.io.PrintWriter;

import javax.jmi.reflect.JmiException;
import javax.jmi.reflect.RefClass;
import javax.jmi.reflect.RefObject;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;


/**
 * The JmiServiceException is a specialization of the JmiException. It 
 * embeds a ServiceExeption and is thrown by the openMDX implementation.
 */
public class JmiServiceException 
	extends JmiException 
	implements BasicException.Wrapper
{

    /**
     * 
     */
    private static final long serialVersionUID = 3688792466186909232L;

    /**
     * Constructor
     * 
     * @param cause a <code>BasicException</code> wrapper
     */
    public JmiServiceException(
        Throwable cause 
    ){
        super(null, null, cause.getMessage());
        this.exceptionStack = BasicException.toStackedException(
            cause,
            this
        );
    }

    /**
	 * Constructor
	 * 
	 * @param cause a <code>BasicException</code> wrapper
	 * @param elementInError
	 */
	public JmiServiceException(
		Throwable cause,
		RefObject elementInError
	) {
		super(null, elementInError, cause.getMessage());
		this.exceptionStack = BasicException.toStackedException(
			cause,
			this,
            getParameters(elementInError)
		);
	}

    /**
     * Retrieve attributes of the element in error
     * 
     * @param elementInError
     * 
     * @return some attributes of the element in error
     */
    protected static BasicException.Parameter[] getParameters(
        RefObject elementInError
    ){
        if(elementInError == null) return null;
        try {
            RefClass refClass = elementInError.refClass();
            return new BasicException.Parameter[]{
                new BasicException.Parameter(
                    "object.java.class", 
                    elementInError.getClass().getName()
                ),
                new BasicException.Parameter(
                    "object.mof.id", 
                    elementInError.refMofId()
                ),
                new BasicException.Parameter(
                    "object.mof.class", 
                     refClass == null ? null : refClass.refMofId()
                )
            };
        } catch (RuntimeException exception) {
            return new BasicException.Parameter[]{
                new BasicException.Parameter(
                    "object.java.class", 
                    elementInError.getClass().getName()
                )
            };
        }
    }
    
    /**
     * Log the exception at warning level.
     *
     * @return this RuntimeServiceException
     */
    public JmiServiceException log()
    {
        SysLog.warning(getMessage(), getCause(), 1);
        return this;
    }

    /**
     * 
     */
    public JmiServiceException appendCause(
        Throwable cause
    ){
        (
            (BasicException)this.exceptionStack.getExceptionStack().get(0)
        ).initCause(cause);
        return this;
    }


    //------------------------------------------------------------------------
    // Implements StackedException.Wrapper
    //------------------------------------------------------------------------

    /**
     * @deprecated use getExceptionStack()
     * 
     * @return the BasicException wrapped by this object.
     */
    public final BasicException getStackedException (
    ) {
        return getExceptionStack();
    }

    /**
     * Return a StackedException, this exception object's cause.
     * 
     * @return the BasicException wrapped by this object.
     */
    public BasicException getExceptionStack (
    ) {
        return this.exceptionStack;
    }

    /**
     * Retrieves the exception domain of this <code>ServiceException</code>.
     *
     * @return the exception domain
     */
    public String getExceptionDomain()
    {
        return this.exceptionStack == null ? 
            BasicException.Code.DEFAULT_DOMAIN : 
            this.exceptionStack.getExceptionDomain();
    }

    /**
     * Retrieves the exception code of this <code>ServiceException</code>.
     *
     * @return the exception code
     */
    public int getExceptionCode()
    {
        return this.exceptionStack == null ? 
            BasicException.Code.GENERIC : 
            this.exceptionStack.getExceptionCode();
    }
    
	/**
	 * Returns the cause belonging to a specific exception domain.
	 * 
	 * @param 	exceptionDomain
	 * 			the desired exception domain,
	 *          or <code>null</code> to retrieve the initial cause.
	 *
	 * @return  Either the cause belonging to a specific exception domain
	 *          or the initial cause if <code>exceptionDomain</code> is
	 * 			<code>null</code>.  
	 */
	public BasicException getCause(
	    String exceptionDomain
	){
        return this.exceptionStack == null ? 
            null : 
            this.exceptionStack.getCause(exceptionDomain);
	}

    /**
     * The exception stack
     */
    private BasicException exceptionStack;
         

    //------------------------------------------------------------------------
    // Extends Throwable
    //------------------------------------------------------------------------

    /**
     * Returns the detail message string of this RuntimeServiceException.  
     */
    public String getMessage(
    ){
        return this.exceptionStack == null ?
            super.getMessage() : 
            this.exceptionStack.getMessage() + ": " +
            this.exceptionStack.getDescription();
    }
    
    /**
     * A String consisting of the class of this exception, the exception 
     * domain, the exception code, the exception description and the exception
     * stack.
     * 
     * @return a multiline representation of this exception.
     */     
    public String toString(){
        return 
            this.exceptionStack == null ?
            super.toString() : 
            super.toString() + '\n' + this.exceptionStack;
    }

    /**
     * Initializes the cause of this throwable to the specified value. 
     * (The cause is the throwable that caused this throwable to get thrown.) 
     * 
     * @param   cause
     *          the cause (which is saved for later retrieval by the
     *          getCause() method). (A null value is permitted, and indicates 
     *          that the cause is nonexistent or unknown.) 
     *
     * @return      a reference to this RuntimeServiceException instance. 
     *
     * @exception   IllegalArgumentException
     *              if cause is this throwable.
     *              (A throwable cannot be its own cause.) 
     * @exception   IllegalStateException
     *              if the cause is already set.
     */     
    public Throwable initCause(
        Throwable cause
    ){
        throw new IllegalStateException(
            "A RuntimeServiceException's cause can't be changed"
        );
    }

    /**
     * Returns the cause of an exception. The cause actually is the wrapped
     * exception.
     *
     * @return Throwable  The exception cause.
     */
    public Throwable getCause(
    ){
        return this.exceptionStack;
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#printStackTrace(java.io.PrintStream)
     */
    public void printStackTrace(PrintStream s) {
        if(this.exceptionStack == null){
            super.printStackTrace(s);
        } else {
            this.exceptionStack.printStack(this, s, true);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#printStackTrace(java.io.PrintWriter)
     */
    public void printStackTrace(PrintWriter s) {
        if(this.exceptionStack == null){
            super.printStackTrace(s);
        } else {
            this.exceptionStack.printStack(this, s, true);
        }
    }


    //------------------------------------------------------------------------
    // Deprecated
    //------------------------------------------------------------------------

    /**
     * Convert this JmiServiceException to a ServiceException 
     *
     * @deprecated  use new 
     * @{link ServiceException(Exception jmiServiceException)}
     */
    public ServiceException getServiceException (
    ) {
        return new ServiceException(this);
    }

}

//--- End of File -----------------------------------------------------------