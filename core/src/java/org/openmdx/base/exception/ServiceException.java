/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ServiceException.java,v 1.12 2008/09/10 08:55:22 hburger Exp $
 * Description: ServiceException class
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:22 $
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
package org.openmdx.base.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.BasicException.Parameter;
import org.openmdx.kernel.log.SysLog;

public final class ServiceException
extends Exception
implements BasicException.Wrapper
{

    /**
     * 
     */
    private static final long serialVersionUID = 4120847750670858549L;


    /**
     * Constructor  
     *
     * @param exception the Exception to be wrapped 
     */
    public ServiceException (
        BasicException exception
    ){
        this.exceptionStack = exception == null ? 
            BasicException.toStackedException(this) : 
                exception;
    }

    /**
     * Constructor  
     *
     * @param exception the Exception to be wrapped 
     */
    public ServiceException (
        Exception exception
    ){
        this.exceptionStack = BasicException.toStackedException(
            exception,
            this
        );
    }

    /**
     * Creates a new <code>ServiceException</code>.
     *
     * @param   cause
     *          The exception cause.
     * @param   exceptionDomain
     *          The exception domain or <code>null</code> for the
     *          default exception domain containing negative exception codes
     *          only.
     * @param   exceptionCode
     *          The exception code. Negative codes are shared by all exception
     *          domains, while positive ones are (non-default) exception
     *          domain specific.
     * @param   parameters
     *          The exception specific parameters.
     * @param   description
     *          A readable description usually not including the parameters.
     */
    public ServiceException(
        Exception cause,
        String exceptionDomain,
        int exceptionCode,
        BasicException.Parameter[] parameters,
        String description
    ){
        this(cause, exceptionDomain, exceptionCode, description, parameters);
    }

    /**
     * Creates a new <code>ServiceException</code>.
     *
     * @param   cause
     *          The exception cause.
     * @param   exceptionDomain
     *          The exception domain or <code>null</code> for the
     *          default exception domain containing negative exception codes
     *          only.
     * @param   exceptionCode
     *          The exception code. Negative codes are shared by all exception
     *          domains, while positive ones are (non-default) exception
     *          domain specific.
     * @param   description
     *          A readable description usually not including the parameters.
     * @param   parameters
     *          The exception specific parameters.
     */
    public ServiceException(
        Exception cause,
        String exceptionDomain,
        int exceptionCode,
        String description,
        Parameter... parameters
    ){
        this.exceptionStack = new BasicException(
            cause,
            exceptionDomain,
            exceptionCode,
            parameters,
            description,
            this
        );
    }

    /**
     * Creates a new <code>ServiceException</code>.
     *
     * @param   exceptionDomain
     *          The exception domain or <code>null</code> for the
     *          default exception domain containing negative exception codes
     *          only.
     * @param   exceptionCode
     *          The exception code. Negative codes are shared by all exception
     *          domains, while positive ones are (non-default) exception
     *          domain specific.
     * @param   parameters
     *          The exception specific parameters.
     * @param   message
     *          A readable description not including the parameters.
     */
    public ServiceException(
        String exceptionDomain,
        int exceptionCode,
        BasicException.Parameter[] parameters,
        String description
    ) {
        this(exceptionDomain, exceptionCode, description, parameters);
    }

    /**
     * Creates a new <code>ServiceException</code>.
     *
     * @param   exceptionDomain
     *          The exception domain or <code>null</code> for the
     *          default exception domain containing negative exception codes
     *          only.
     * @param   exceptionCode
     *          The exception code. Negative codes are shared by all exception
     *          domains, while positive ones are (non-default) exception
     *          domain specific.
     * @param   parameters
     *          The exception specific parameters.
     * @param   message
     *          A readable description not including the parameters.
     */
    public ServiceException(
        String exceptionDomain,
        int exceptionCode,
        String description,
        Parameter... parameters
    ) {
        this(
            null, // none
            exceptionDomain,
            exceptionCode,
            description,
            parameters
        );
    }

    /**
     * Log the exception.
     *
     * @return the ServiceException
     */
    public ServiceException log(
    ) {
        SysLog.warning(this);
        return this;
    }


    /**
     * 
     */
    public ServiceException appendCause(
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


    //----------------------------------------------------------------------------
    // Deprecated
    //----------------------------------------------------------------------------

    /**
     * Returns a formatted multiline String representation for the exception
     * including all stacked exceptions. Includes all available exception
     * information as timestamp, class, method, line, number, domain, error code,
     * description, parameters and the stack trace for each exception.
     *
     * @return  a String representation
     * *
     * @deprecated use @{link ServiceException#toString()}.
     */
    public String stackToString()
    {
        return toString();
    }

    /**
     * @deprecated use @{link ServiceException#getStackedException()} followed by
     * @{link StackedException#getExceptionStack()} followed by 
     * @{link List#get(int)}.
     */
    public BasicException getStackedException(
        int index
    ){
        return (BasicException)this.exceptionStack.getExceptionStack().get(index);
    }

    /**
     * Retrieves the class for this <code>ServiceExceptionDescriptor</code> object.
     *
     * @return the class
     *
     * @deprecated use @{link ServiceException#getStackedException()} followed by
     * @{link StackedException#getClassName()}.
     */
    public String getClassName()
    {
        return this.exceptionStack == null ? 
            null : 
                this.exceptionStack.getClassName();
    }

    /**
     * Retrieves the method for this <code>ServiceExceptionDescriptor</code> object.
     *
     * @return the method
     *
     * @deprecated use @{link ServiceException#getStackedException()} followed by
     * @{link StackedException#getMethodName()}.
     */
    public String getMethodName()
    {
        return this.exceptionStack == null ? 
            null : 
                this.exceptionStack.getMethodName();
    }

    /**
     * Retrieves the line number for this <code>ServiceExceptionDescriptor</code> object.
     *
     * @return the line nr
     *
     * @deprecated use @{link ServiceException#getStackedException()} followed by
     * @{link StackedException#getLineNr()}.
     */
    public int getLineNr()
    {
        return this.exceptionStack == null ? 
            0 : 
                this.exceptionStack.getLineNr();
    }

    /**
     * Retrieves the domain for this <code>ServiceException</code> toplevel object.
     *
     * @return the domain value
     *
     * @deprecated use @{link ServiceException#getExceptionDomain()}.
     */
    public String getDomain()
    {
        return this.exceptionStack == null ? 
            null : 
                this.exceptionStack.getExceptionDomain();
    }

    /**
     * Retrieves the error code for this <code>ServiceException</code> toplevel object.
     *
     * @return the error code
     *
     * @deprecated use @{link ServiceException#getStackedException()} followed by
     * @{link StackedException#getExceptionCode()}.
     */
    public int getErrorCode()
    {
        return getExceptionCode();
    }

    /**
     * Retrieves the timestamp for this <code>ServiceException</code> toplevel object.
     *
     * @return the timestamp
     *
     * @deprecated use @{link ServiceException#getStackedException()} followed by
     * @{link StackedException#getTimestamp()}.
     */
    public java.util.Date getTimestamp()
    {
        return this.exceptionStack == null ? 
            null : 
                this.exceptionStack.getTimestamp();
    }


    /**
     * Retrieves the parameters for this <code>ServiceException</code> toplevel object.
     *
     * @return the parameters or an empty array if there are no parameters
     *         available
     *
     * @deprecated use @{link ServiceException#getStackedException()} followed by
     * @{link StackedException#getParameters()}.
     */
    public BasicException.Parameter[] getParameters()
    {
        return this.exceptionStack == null ? 
            null : 
                this.exceptionStack.getParameters();
    }

    /**
     * Retrieves the context for this <code>ServiceException</code> toplevel object.
     *
     * @return the description
     *
     * @deprecated use @{link ServiceException#getStackedException()} followed by
     * @{link StackedException#getDescription()}.
     */
    public String  getDescription()
    {
        return this.exceptionStack == null ? 
            null : 
                this.exceptionStack.getDescription();
    }


    //----------------------------------------------------------------------------
    // Format
    //----------------------------------------------------------------------------

    /**
     * Returns a string representation for the exception's top level object
     * using formatting information.
     * The string rendering may be parameterized by passing a format string 
     * containing text mixed with placeholders for the excpetions properties and
     * the exception parameters. If the format string is null or empty the
     * default string representation is returned. Any number of placeholders
     * may be woven into format string. The placeholders are defined through
     * "%{PLACEHOLDER}" elements.
     *
     * Example format strings:
     *    "Authorization denied for user=%{user} using role %{role}"
     *    "Error in %{EX_CLASS}.%{EX_METHOD} at line %{EX_LINE}"
     *
     * If a placeholder cannot be resolved it is replaced by
     * "<unknown '{PLACEHOLDER}'>". {PLACEHOLDER} actually represents the
     * unresolveable placeholder name.
     *
     * <p>
     * Defined placeholders for exception properties:
     * Any exception property name is a valid placeholder. Multivalue properties
     * are rendered like arrays ("[value1,value2, ...]")
     *
     * Defined placeholders for exception parameters:
     *    EX_TIMESTAMP      the exception's timestamp as "yyyy-MM-dd HH:mm:ss.SSS"
     *    EX_CLASS          the exception's class name
     *    EX_METHOD         the exception's method name
     *    EX_LINE           the exception's line number
     *    EX_DOMAIN         the exception's domain
     *    EX_ERRORCODE      the exception's error code
     *    EX_DESCRIPTION    the exception's description
     *
     * @param  format a format string
     * @return  a string representation
     * 
     */
    public String toString(String format)
    {
        if (format == null) return toString();


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        StringBuilder     tmp = new StringBuilder();
        String           placeHolder;
        BasicException.Parameter[]         props = getParameters();
        boolean          replaced;
        int              pos=0, startPos, endPos;


        while(true) {
            startPos = format.indexOf("%{",pos);
            if (startPos >= 0) {
                endPos = format.indexOf("}",startPos);
                if (endPos >= 0) {
                    tmp.append(
                        format.substring(pos, startPos)
                    );
                    placeHolder=format.substring(startPos+2, endPos);

                    // lookup first in the properties
                    replaced = false;
                    for(int ii=0; ii<props.length; ii++) {
                        if (props[ii].getName().equals(placeHolder)) {
                            replaced = true;
                            tmp.append(
                                props[ii].getValue()
                            );
                        }
                    }

                    // check for exception parameters
                    if (!replaced) {
                        if (placeHolder.equals("EX_TIMESTAMP")) {
                            tmp.append(
                                formatter.format(getTimestamp())
                            );
                        }else if (placeHolder.equals("EX_CLASS")) {
                            tmp.append(
                                getClassName()
                            );
                        }else if (placeHolder.equals("EX_METHOD")) {
                            tmp.append(
                                getMethodName()
                            );
                        }else if (placeHolder.equals("EX_LINE")) {
                            tmp.append(
                                getLineNr()
                            );
                        }else if (placeHolder.equals("EX_DOMAIN")) {
                            tmp.append(
                                getDomain()
                            );
                        }else if (placeHolder.equals("EX_ERRORCODE")) {
                            tmp.append(
                                getErrorCode()
                            );
                        }else if (placeHolder.equals("EX_DESCRIPTION")) {
                            tmp.append(
                                getDescription()
                            );
                        }else{
                            tmp.append(
                                "<unknown '"
                            ).append(
                                placeHolder
                            ).append(
                                "'>"
                            );
                        }
                    }

                    pos = endPos + 1;
                }else{
                    tmp.append(
                        format.substring(pos, format.length())
                    );
                    break;
                }
            }else{
                tmp.append(
                    format.substring(pos, format.length())
                );
                break;
            }
        }

        return tmp.toString();
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


    //----------------------------------------------------------------------------
    // Class Features
    //----------------------------------------------------------------------------

    /**
     * Map a throwable to a ServiceException
     *
     * @param       throwable
     *              a throwable to be mapped to a ServiceException
     *
     * @return      the throwable itself in case of a ServiceException;
     *              a ServiceException wrapping the throwable otherwise
     *
     * @deprecated  use new ServiceException(Exception)
     */
    public static ServiceException toServiceException(
        Exception exception
    ){
        return exception instanceof ServiceException ?
            (ServiceException)exception :
                new ServiceException(exception);
    }

}

