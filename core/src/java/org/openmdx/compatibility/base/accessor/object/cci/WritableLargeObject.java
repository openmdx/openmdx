/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: WritableLargeObject.java,v 1.3 2004/04/02 16:59:01 wfro Exp $
 * Description: Large Objects: Partial accessors
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/04/02 16:59:01 $
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
package org.openmdx.compatibility.base.accessor.object.cci;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.openmdx.base.exception.ServiceException;



/**
 * @author hburger
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface WritableLargeObject 
{
	
	/**
	 * Truncates the LOB value that this WritableLargeObject represents to 
	 * have the specified length.
	 * 
	 * @param length
	 *        the length, to which the LOB value should be truncated
	 * 
	 * @exception	ServiceException NOT_SUPPORTED
	 *				if the object is not partially updateable.
	 */
	void truncate (
		long length
	) throws ServiceException;
		
		
	//------------------------------------------------------------------------
	// Binary Large Object (BLOB)
	//------------------------------------------------------------------------

	/**
	 * Updates all or part of the BLOB value that this WritableLargeObject 
	 * represents.
	 * 
	 * @param	position
	 *          the first character of the substring to be extracted. The
	 *          first character is at position 0.
	 * @param	content
	 *          the (partial) content to be put at the specified position 
	 * 
	 * @exception	ServiceException NOT_SUPPORTED
	 *				if the object is not partially updateable.
	 */
	void setBytes(
		long position,
		byte[] content
	) throws ServiceException;

	/**
	 * Replaces a large object's content.
	 * </ul>
	 * @param	stream
	 *          an InputStream for the new content
	 * @param	size
	 *          the object's size or -1 if it is unknown
	 * 
	 * @exception	ServiceException NOT_SUPPORTED
	 *				if size is -1 but required by the underlaying implementation.
	 */
	void setBinaryStream(
		InputStream stream,
		long size
	) throws ServiceException;

	/**
	 * Retrieves a stream that can be used to write to the BLOB value that
	 * this WritableLargeObject object represents. 
	 * <p>
	 * The stream begins atthe specified position. 
	 * 
	 * @param	position
	 *          the position in the BLOB value at which to start writing
	 * 
	 * @return  a java.io.OutputStream object to which data can be written 
	 *
	 * @exception	ServiceException	NOT_SUPPORTED
	 *				if the object is not partially updateable.
	 */
	OutputStream setBinaryStream(
		long position
	) throws ServiceException;


	//------------------------------------------------------------------------
	// Character Large Object (CLOB)
	//------------------------------------------------------------------------

	/**
	 * Updates all or part of the CLOB value that this WritableLargeObject 
	 * represents.
	 * 
	 * @param	position
	 *          the first character of the substring to be extracted. The
	 *          first character is at position 0.
	 * @param	content
	 *          the (partial) content to be put at the specified position 
	 * 
	 * @exception	ServiceException NOT_SUPPORTED
	 *				if the object is either read-only or not partially 
	 *              updateable.
	 */
	void setCharacters(
		long position,
		char[] content
	) throws ServiceException;

	/**
	 * Replaces a large object's content.
	 * </ul>
	 * @param	stream
	 *          a Reader for the new content
	 * @param	size
	 *          the object's size or -1 if it is unknown
	 *
	 * @exception	ServiceException	NOT_SUPPORTED
	 *				if the object is read-only
	 */
	void setCharacterStream(
		Reader stream,
		long size
	) throws ServiceException;

	/**
	 * Retrieves a stream that can be used to write to the CLOB value that
	 * this WritableLargeObject represents. 
	 * <p>
	 * The stream begins atthe specified position. 
	 * 
	 * @param	position
	 *          the position in the BLOB value at which to start writing
	 * 
	 * @return  a java.io.Writer object to which data can be written 
	 *
	 * @exception	ServiceException	NOT_SUPPORTED
	 *				if the object is not partially updateable.
	 */
	Writer setCharacterStream(
		long position
	) throws ServiceException;

}
