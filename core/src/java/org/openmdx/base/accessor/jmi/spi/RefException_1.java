/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: RefException_1 class
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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

/**
 * @author wfro
 */
package org.openmdx.base.accessor.jmi.spi;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import javax.jmi.reflect.RefException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;

/**
 * RefException extension 
 */
public class RefException_1
    extends RefException
    implements BasicException.Holder
{

    /**
     * 
     */
    private static final long serialVersionUID = -6994022553371842584L;

    //-------------------------------------------------------------------------
    public RefException_1(
        Exception cause,
        String exceptionDomain,
        int exceptionCode,
        String description,
        BasicException.Parameter[] properties
    ) {
        super.initCause(
            new BasicException(
                cause,
                exceptionDomain,
                exceptionCode,
                properties,
                description,
                this
            )
        );
    }

    //-------------------------------------------------------------------------
    public RefException_1(
        BasicException.Parameter[] properties
    ) {
        this(
            null,
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.PROCESSING_FAILURE,
            null,
            properties
        );
    }

    //-------------------------------------------------------------------------
    public RefException_1(
        String domain,
        int errorCode,
        String description,
        BasicException.Parameter[] properties
    ) {
        this(
            null,
            domain,
            errorCode,
            description,
            properties
        );
    }

    //-------------------------------------------------------------------------
    public RefException_1(
        ServiceException e
    ) {
        this(e.getCause());
    }

    //-------------------------------------------------------------------------
    public RefException_1(
        BasicException e
    ) {
        super(e.getMessage());
        super.initCause(e);
    }

    //-------------------------------------------------------------------------
    public RefException_1 log(
    ) {
        return BasicException.log(this);
    }

    //-------------------------------------------------------------------------
    public java.lang.Object refGetValue(
        String propertyName
    ) {
        return getCause().getParameter(propertyName);
    }

    //-------------------------------------------------------------------------
    public java.lang.Object refGetValue(
        String propertyName,
        int index
    ) {
        java.lang.Object values = refGetValue(propertyName);
        if(values != null) {
            return ((List<?>)values).get(index);
        }
        return null;
    }

    //-------------------------------------------------------------------------
    public static BasicException.Parameter refNewProperty(
        String propertyName,
        java.lang.Object propertyValue
    ) {
        return new BasicException.Parameter(
            propertyName,
            propertyValue
        );
    }

    //-------------------------------------------------------------------------
    public ServiceException refGetServiceException(
    ) {
        return new ServiceException(getCause());
    }

    /**
     * Returns the cause of an exception. The cause actually is the wrapped exception.
     *
     * @return Throwable  The exception cause.
     */
    @Override
    public final synchronized BasicException getCause(
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
        getCause().printStackTrace(getClass().getName(), s);
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#printStackTrace(java.io.PrintWriter)
     */
    @Override
    public void printStackTrace(PrintWriter s) {
        getCause().printStackTrace(getClass().getName(), s);
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#getMessage()
     */
    @Override 
    public String getMessage() {
        return Throwables.getMessage(this);
    }

}
//--- End of File -----------------------------------------------------------
