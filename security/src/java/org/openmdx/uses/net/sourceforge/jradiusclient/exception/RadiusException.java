/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RadiusException.java,v 1.11 2009/03/05 16:29:53 hburger Exp $
 * Description: Radius Exception 
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/05 16:29:53 $
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.uses.net.sourceforge.jradiusclient.exception;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.BasicException.Parameter;

/**
 * Radius Exception
 */
public class RadiusException
    extends Exception 
    implements BasicException.Holder
{

	/**
     * Constructor
     * 
     * @param message
     */
    public RadiusException(String message) {
        this(null, message);
    }
    
    /**
     * Constructor
     * 
     * @param cause
     */
    public RadiusException(
        Exception cause
    ) {
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
     * @param cause
     * @param description
     * @param parameters
     */
    public RadiusException(
        Exception cause,
        String description,
        Parameter... parameters
    ){
    	super.initCause(
        	new BasicException(
	            cause,
	            BasicException.Code.DEFAULT_DOMAIN,
	            BasicException.Code.GENERIC,
	            parameters,
	            description,
				this
			)
        );
    }

    /**
	 * Implements <code>Serializable</code>.
	 */
	private static final long serialVersionUID = 5014284731661341777L;
	

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

	public RadiusException log() {
		return BasicException.log(this);
	}

}
