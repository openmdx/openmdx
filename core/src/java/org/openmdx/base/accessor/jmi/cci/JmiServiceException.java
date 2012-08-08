/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: JmiServiceException.java,v 1.22 2009/03/05 13:53:30 hburger Exp $
 * Description: JmiServiceException class
 * Revision:    $Revision: 1.22 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/05 13:53:30 $
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

import org.openmdx.kernel.exception.BasicException;


/**
 * The JmiServiceException is a specialization of the JmiException. It 
 * embeds a ServiceExeption and is thrown by the openMDX implementation.
 */
public class JmiServiceException extends JmiException 
    implements BasicException.Holder
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
        super(cause.getMessage());
        super.initCause(
            BasicException.toStackedException(
                cause,
                this
            )
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
        super.initCause(
            BasicException.toStackedException(
                cause,
                this,
                getParameters(elementInError)
            )
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
    public JmiServiceException log() {
        return BasicException.log(this);
    }

    /**
     * Returns the cause of an exception. The cause actually is the wrapped exception.
     *
     * @return Throwable  The exception cause.
     */
    public final BasicException getCause(
    ){
        return (BasicException) super.getCause();
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.exception.BasicException.Holder#getCause(java.lang.String)
     */
    public BasicException getCause(String exceptionDomain) {
        return getCause().getCause(exceptionDomain);
    }

    /**
     * Retrieves the exception domain of this <code>ServiceException</code>.
     *
     * @return the exception domain
     */
    public String getExceptionDomain(
    ){
        return getCause().getExceptionDomain();
    }

    /**
     * Retrieves the exception code of this <code>ServiceException</code>.
     *
     * @return the exception code
     */
    public int getExceptionCode(
    ){
        return getCause().getExceptionCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#printStackTrace(java.io.PrintStream)
     */
    @Override
    public void printStackTrace(PrintStream s) {
        getCause().printStackTrace(s);
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#printStackTrace(java.io.PrintWriter)
     */
    @Override
    public void printStackTrace(PrintWriter s) {
        getCause().printStackTrace(s);
    }

}

//--- End of File -----------------------------------------------------------
