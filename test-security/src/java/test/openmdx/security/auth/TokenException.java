/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Extended IO Exception 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
package test.openmdx.security.auth;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;


/**
 * Extended IO Exception
 */
public class TokenException
    extends GeneralSecurityException 
    implements BasicException.Holder {

	/**
     * Constructor 
	 * 
	 * @param cause
	 * @param exceptionDomain
	 * @param exceptionCode
	 * @param description
	 * @param parameters
	 */
    public TokenException(
		Throwable cause,
        String exceptionDomain,
        int exceptionCode,
        String description,
        BasicException.Parameter... parameters
    ) {
        super.initCause(
            new BasicException(
                cause,
                exceptionDomain,
                exceptionCode,
                parameters,
                description,
                this
            )
        );
    }

    /**
     * Constructor 
     * 
     * @param exceptionDomain
     * @param exceptionCode
     * @param description
     * @param parameters
     */
    public TokenException(
        String exceptionDomain,
        int exceptionCode,
        String description,
        BasicException.Parameter... parameters
    ) {
    	this(
            null,
            exceptionDomain,
            exceptionCode,
            description,
            parameters
        );
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 4361051563608646179L;

    /**
     * Returns the cause of an exception. The cause actually is the wrapped exception.
     *
     * @return Throwable  The exception cause.
     */
    @Override
	public final BasicException getCause(
    ){
        return (BasicException) super.getCause();
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.exception.BasicException.Holder#getCause(java.lang.String)
     */
    public BasicException getCause(String exceptionDomain) {
        return this.getCause().getCause(exceptionDomain);
    }

    /**
     * Retrieves the exception domain of this <code>ServiceException</code>.
     *
     * @return the exception domain
     */
    public String getExceptionDomain(
    ){
        return this.getCause().getExceptionDomain();
    }

    /**
     * Retrieves the exception code of this <code>ServiceException</code>.
     *
     * @return the exception code
     */
    public int getExceptionCode(
    ){
        return this.getCause().getExceptionCode();
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

	public TokenException log() {
		return BasicException.log(this);
	}

    /* (non-Javadoc)
     * @see java.lang.Throwable#getMessage()
     */
    @Override 
    public String getMessage() {
        return Throwables.getMessage(this);
    }

}
