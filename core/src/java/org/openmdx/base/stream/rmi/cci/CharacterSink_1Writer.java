/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CharacterSink_1Writer.java,v 1.4 2004/09/26 22:34:35 hburger Exp $
 * Description: Streams: Character Sink Writer
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.base.stream.rmi.cci;

import java.io.IOException;
import java.io.Writer;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.openmdx.kernel.security.ExecutionContext;

/**
 * Streams: Character Sink Writer
 */
public class CharacterSink_1Writer extends Writer {

    /**
     * 
     */
    protected CharacterSink_1_0 out;
 
    /**
     * The buffer
     */
    char[] buffer;

    /**
     * The index into the buffer
     */
    int cursor = 0;

    /**
     * The stream's default capacity
     */
    protected static int DEFAULT_CAPACITY = 100000; 

    /**
     * The stream's actual capacity
     */
    private final int capacity;
    
    /**
     * Callback Context
     */
    protected final ExecutionContext callbackContext;
    
    /**
     * Constructor
     * 
     * @param sink
     * @param capacity
     * @param callbackContext The execution context for callbacks
     */    
    public CharacterSink_1Writer(
        CharacterSink_1_0 sink,
        int capacity,
        ExecutionContext callbackContext
    ){
        this.out = sink;
        this.capacity = capacity;
        this.callbackContext = callbackContext;
    }

    /**
     * Constructor
     * 
     * @param sink
     * @param callbackContext The execution context for callbacks
     */    
    public CharacterSink_1Writer(
        CharacterSink_1_0 sink,
        ExecutionContext callbackContext
    ){
        this(sink,DEFAULT_CAPACITY,callbackContext);
    }

    /**
     * 
     */    
    protected boolean isOpen(
    ){
        return this.out != null;
    }

    /**
     * Tests whether the stream is open and allocates the buffer if necessary.
     * 
     * @throws IOException
     */
    private void prepare(
    	boolean write
    ) throws IOException{
        if(!(isOpen())) throw new IOException("Stream not open");
        if(write && this.buffer == null) this.buffer = new char[this.capacity];
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
    // Extends Writer
    //------------------------------------------------------------------------
    
	/* (non-Javadoc)
	 * @see java.io.Writer#write(char[], int, int)
	 */
	public void write(
		char[] cbuf, 
		int off, 
		int len
	) throws IOException {
		if(len > 0) synchronized(super.lock){
			prepare(true);
			for(
				int sourceCursor = 0;
				sourceCursor < len;
			){
				int remainingInTarget = this.buffer.length - this.cursor;
				int remainingInSource = len - sourceCursor;
				if(remainingInSource >= remainingInTarget){
					System.arraycopy(cbuf,sourceCursor,this.buffer,this.cursor,remainingInTarget);
					sourceCursor += remainingInTarget;
					try {
			            this.callbackContext.execute(
			                new PrivilegedExceptionAction(){
			                    public Object run() throws Exception {
			    					out.writeCharacters(buffer);
			                        return null;
			                    }					        
			                }
			            );
			        } catch (PrivilegedActionException e) {
			            throw toIOException(e);
			        }
					this.cursor = 0;
				} else {
					System.arraycopy(cbuf,sourceCursor,this.buffer,this.cursor,remainingInSource);
					sourceCursor += remainingInSource;
					this.cursor += remainingInSource;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.io.Writer#write(int)
	 */
	public void write(int c) throws IOException {
		synchronized(super.lock){
			prepare(true);
			this.buffer[this.cursor++] = (char) c;
			if(this.cursor == this.buffer.length) try {
	            this.callbackContext.execute(
	                new PrivilegedExceptionAction(){
	                    public Object run() throws Exception {
	        				out.writeCharacters(buffer);
	        				cursor = 0;
	                        return null;
	                    }					        
	                }
	            );
	        } catch (PrivilegedActionException e) {
	            throw toIOException(e);
			}
		}
	}

	char[] flushBuffer;
	
	/* (non-Javadoc)
	 * @see java.io.Writer#flush()
	 */
	public void flush() throws IOException {
		if(this.cursor != 0) synchronized(super.lock){
			prepare(false);
	        if(!(isOpen())) throw new IOException("Stream not open");
			if(this.cursor == this.buffer.length) { 
				flushBuffer = this.buffer;
			} else {
				flushBuffer = new char[this.cursor];
				System.arraycopy(this.buffer,0,flushBuffer,0,this.cursor);
			}
			try {
	            this.callbackContext.execute(
	                new PrivilegedExceptionAction(){
	                    public Object run() throws Exception {
	            			out.writeCharacters(flushBuffer);
	                        return null;
	                    }					        
	                }
	            );
	        } catch (PrivilegedActionException e) {
	            throw toIOException(e);
	        }
			this.cursor = 0;
		}
	}

	/* (non-Javadoc)
	 * @see java.io.Writer#close()
	 */
	public void close() throws IOException {
		synchronized(super.lock){
			if(isOpen()) try {
	            this.callbackContext.execute(
	                new PrivilegedExceptionAction(){
	                    public Object run() throws Exception {
	        				flush();
	        				out.close();
	        				buffer = null;
	        				out = null;			
	                        return null;
	                    }					        
	                }
	            );
	        } catch (PrivilegedActionException e) {
	            throw toIOException(e);
			}
		}
	}

}