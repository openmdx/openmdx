/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: LineReader.java,v 1.1 2010/03/05 13:23:50 hburger Exp $
 * Description: Line Reader 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/05 13:23:50 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2010, OMEX AG, Switzerland
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

package org.openmdx.resource.ldap.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * Line Reader
 */
public class LineReader extends BufferedReader {

	/**
     * Constructor 
     *
     * @param in
     * @param sz
     */
    public LineReader(Reader in, int sz) {
	    super(in, sz);
    }

	/**
     * Constructor 
     *
     * @param in
     */
    public LineReader(Reader in) {
	    super(in);
    }

    /**
     * Line read ahead
     */
    private String nextLine = null;

    /**
     * Marked line
     */
    private String markedLine = null;
    
	/* (non-Javadoc)
     * @see java.io.BufferedReader#mark(int)
     */
    @Override
    public void mark(
    	int readAheadLimit
    ) throws IOException {
	    super.mark(readAheadLimit);
	    this.markedLine = this.nextLine;
    }

	/* (non-Javadoc)
     * @see java.io.BufferedReader#read()
     */
    @Override
    public int read(
    ) throws IOException {
    	throw new UnsupportedOperationException("Only complete lines my be read");
    }

	/* (non-Javadoc)
     * @see java.io.BufferedReader#read(char[], int, int)
     */
    @Override
    public int read(
    	char[] cbuf, 
    	int off, 
    	int len
    ) throws IOException {
    	throw new UnsupportedOperationException("Only complete lines my be read");
    }

	/* (non-Javadoc)
     * @see java.io.BufferedReader#readLine()
     */
    @Override
    public String readLine(
    ) throws IOException {
    	String thisLine = this.nextLine == null ? super.readLine() : this.nextLine;
    	if(thisLine != null) {
    		//
    		// Read ahead
    		//
    		StringBuilder buffer = null;
    		while(
    			(this.nextLine = super.readLine()) != null && 
    			(this.nextLine.startsWith(" ") || this.nextLine.startsWith("\t"))
    		){
    			if(buffer == null){
    				buffer = new StringBuilder(thisLine);
    			}
    			buffer.append(this.nextLine.substring(1));
    		}
    		if(buffer != null){
    			return buffer.toString();
    		}
    	}
	    return thisLine;
    }

	/* (non-Javadoc)
     * @see java.io.BufferedReader#ready()
     */
    @Override
    public boolean ready(
    ) throws IOException {
	    return this.nextLine != null || super.ready();
    }

	/* (non-Javadoc)
     * @see java.io.BufferedReader#reset()
     */
    @Override
    public void reset(
    ) throws IOException {
	    super.reset();
	    this.nextLine = this.markedLine;
	    this.markedLine = null;
    }

	/* (non-Javadoc)
     * @see java.io.BufferedReader#skip(long)
     */
    @Override
    public long skip(
    	long n
    ) throws IOException {
    	throw new UnsupportedOperationException("Only complete lines my be read");
    }

	/* (non-Javadoc)
     * @see java.io.Reader#read(char[])
     */
    @Override
    public int read(
    	char[] cbuf
    ) throws IOException {
    	throw new UnsupportedOperationException("Only complete lines my be read");
    }

	/* (non-Javadoc)
     * @see java.io.Reader#read(java.nio.CharBuffer)
     */
    @Override
    public int read(
    	CharBuffer target
    ) throws IOException {
    	throw new UnsupportedOperationException("Only complete lines my be read");
    }
	    
}
