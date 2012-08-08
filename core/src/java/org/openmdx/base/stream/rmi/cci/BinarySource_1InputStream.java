/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: BinarySource_1InputStream.java,v 1.4 2004/09/26 22:34:35 hburger Exp $
 * Description: Streams: Binary Source InputStream
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/09/26 22:34:35 $
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
package org.openmdx.base.stream.rmi.cci;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.openmdx.base.stream.cci.Source_1_0;
import org.openmdx.kernel.security.ExecutionContext;

/**
 * Streams: Binary Source InputStream
 */
public class BinarySource_1InputStream 
    extends InputStream 
    implements Source_1_0
{

    /**
     * 
     */
    protected BinarySource_1_0 in;
    
    /**
     * The buffer
     */
    byte[] buffer = new byte[]{};

    /**
     * The index into the buffer
     */
    private int cursor = 0;

    /**
     * According to InputStream's contract
     */    
    private static int END_OF_STREAM = -1;
    
    /**
     * The stream's default capacity
     */
    protected static int DEFAULT_CAPACITY = 100000; 

    /**
     * The stream's actual capacity
     */
    final int capacity;
    
    /**
     * 
     */
    private static long NOT_FETCHED = Long.MIN_VALUE;    

    /**
     * The stream's length
     */
    long length = NOT_FETCHED; 

    /**
     * Callback Context
     */
    protected final ExecutionContext callbackContext;
    
    /**
     * Constructor
     * 
     * @param source
     * @param capacity
     * @param callbackContext The execution context for callbacks
     */    
    public BinarySource_1InputStream(
        BinarySource_1_0 source,
        int capacity,
        ExecutionContext callbackContext
    ){
        this.in = source;
        this.capacity = capacity;
        this.callbackContext = callbackContext;
    }

    /**
     * Constructor
     * 
     * @param source
     * @param callbackContext The execution context for callbacks
     */    
    public BinarySource_1InputStream(
        BinarySource_1_0 source,
        ExecutionContext callbackContext
    ){
        this(source,DEFAULT_CAPACITY,callbackContext);
    }

    protected boolean isOpen(
    ){
        return this.in != null;
    }

    /**
     * Return an I/O Exception based on the given Privileged Action Exception
     * 
     * @param source a Privileged Action Exception
     * @return the corresponding I/O Exception
     */
    private static IOException toIOException(
        PrivilegedActionException source
    ){
        Exception cause = source.getException();
        return cause instanceof IOException ?
            (IOException)cause :
            new RemoteException(source.getMessage(), cause);
    }

    
    //------------------------------------------------------------------------
    // Implements Source_1_0
    //------------------------------------------------------------------------
    
    /**
     * Get the stream's length
     * 
     * @return the length of the stream or -1 if it is unknown.
     */
    public long length(
    ) throws IOException {
        if(this.length == NOT_FETCHED) try {
            this.callbackContext.execute(
                new PrivilegedExceptionAction(){
                    public Object run() throws Exception {
                        length = in.length();
                        return null;
                    }					        
                }
            );
        } catch (PrivilegedActionException e) {
            throw toIOException(e);
        }
        return this.length;
    }
        
    /* (non-Javadoc)
     * @see java.io.InputStream#close()
     */
    public void close() throws IOException {
    	if(isOpen()) try {
	        super.close();
            this.callbackContext.execute(
                new PrivilegedExceptionAction(){
                    public Object run() throws Exception {
            	        in.close();
                        return null;
                    }					        
                }
            );
        } catch (PrivilegedActionException e) {
            throw toIOException(e);
        } finally {
	        this.in = null;
	        }
    }

        
    //------------------------------------------------------------------------
    // Extends InputStream
    //------------------------------------------------------------------------
        
    /* (non-Javadoc)
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        if(!(isOpen())) throw new IOException("Stream not open");
        if(this.buffer == null) return END_OF_STREAM;
        if(this.cursor == this.buffer.length){
            try {
	            this.callbackContext.execute(
	                new PrivilegedExceptionAction(){
	                    public Object run() throws Exception {
	                        buffer = in.readBytes(capacity);
	                        return null;
	                    }					        
	                }
	            );
	        } catch (PrivilegedActionException e) {
	            throw toIOException(e);
	        }
            if(this.buffer == null) return END_OF_STREAM;
            this.cursor = 0;
        }
        return 0xFF & this.buffer[cursor++] ;
    }
    
}
