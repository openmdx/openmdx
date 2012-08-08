/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LogEntityReader.java,v 1.4 2004/12/09 14:25:55 wfro Exp $
 * Description: Log entity reader
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/12/09 14:25:55 $
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
package org.openmdx.kernel.log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Defines the interface for log entity readers
 */
public interface LogEntityReader {

    /**
	 * Parses the log entity for log events. Returns at most <code>
	 * events</code> log events. If the start position is >0 the parser seeks
	 * to next starting log event.
	 * <p>
	 * If the file position is a positive number the reading starts from the
	 * given file position. If the given file position is a negative number
	 * it is used as offset from the end of the file.
	 * <p>
	 * Be prepared that the requested number of events can be limited.
	 * <p>
	 * The parser returns if either the end of the file is reached, the max
	 * number of events is parsed or the max processing time is exceeded.
	 *
	 * @param startPos             The start position where the parsing starts
	 * @param maxEvents            The max number of events to be processed
	 * @param maxProcessingTime    The max processing time in milliseconds.
	 *                             A value of 0 means no time limit.
	 * @param filter               A log event filter 
	 * @param eventList            The event list to be filled with the parsed 
	 * @return    The file position after reading all the events
	 * @exception IOException Thrown if file does not exist or cannot be read
	 * @exception UnsupportedOperationException if readig log events is not supported
	 *              on the currently used log entity
	 */
	long readLogEvents(
        long startPos,
		int maxEvents,
		long maxProcessingTime,
		LogEventFilter filter,
		ArrayList eventList
    ) throws IOException, UnsupportedOperationException;

	long readLogEvents(
        long startPos,
		int maxEvents,
		long maxProcessingTime,
		LogEventFilter filter,
		ObjectOutputStream target
    ) throws IOException, UnsupportedOperationException;

	/**
	 * Reads from a start position a given number of bytes from the log
	 * file.
	 * <p>
	 * Note that not all log entities may support binary reading. 
	 *
	 * @param startPos             The start position where the parsing starts
	 * @param buffer               The allocated buffer to receive the file data
	 * @return    The number of bytes read. 0 indicates EOF
	 * @exception IOException Thrown if file does not exist or cannot be read
	 * @exception UnsupportedOperationException if binary read is not supported
	 *              on the currently used log entity
	 */
	int readBinary(
	    long startPos,
		byte[] buffer
	) throws IOException, UnsupportedOperationException;

	/**
	 * Returns the size of the log entity in bytes
	 *
	 * @return    The log entity size
	 * @exception IOException Thrown if file does not exist or cannot be read
	 * @exception UnsupportedOperationException if the entity size is not 
	 *              supported on the currently used log entity
	 */
	long size(
	)throws IOException, UnsupportedOperationException;

}

//--- End of File -----------------------------------------------------------
