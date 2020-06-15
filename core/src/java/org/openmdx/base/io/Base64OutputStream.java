/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Base64 Output Stream
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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
package org.openmdx.base.io;

import java.io.ByteArrayOutputStream;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.kernel.exception.BasicException;

/**
 * Base64 Output Stream
 */
public class Base64OutputStream extends ByteArrayOutputStream {

    /* (non-Javadoc)
     * @see java.io.ByteArrayOutputStream#toString()
     */
    @Override
    public synchronized String toString(
    ) {
        return Base64.encode(
            this.buf,
            0,
            this.count
        );
    }

    public static String encode(
    	Object data
    ) throws ServiceException{
    	if(data == null) {
    		return "";
    	} else {
    	    try (
        		Base64OutputStream target = new Base64OutputStream();
        		java.io.ObjectOutputStream objectOutputStream = new java.io.ObjectOutputStream(target)
            ){
                objectOutputStream.writeObject(data);
        		return target.toString();
    		} catch (Exception exception) {
    			throw new ServiceException(
    				exception,
    				BasicException.Code.DEFAULT_DOMAIN,
    				BasicException.Code.TRANSFORMATION_FAILURE,
    				"Base-64 encoding failed",
    				new BasicException.Parameter("class", data.getClass().getName())
    			);
    		}
    	}
    }
    
}