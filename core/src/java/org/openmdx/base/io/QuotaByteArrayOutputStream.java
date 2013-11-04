/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: QuotaByteArrayOutputStream 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010-2013, OMEX AG, Switzerland
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

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.openmdx.kernel.log.SysLog;

public class QuotaByteArrayOutputStream extends java.io.ByteArrayOutputStream {

	private static class Counter {
		
		public Counter(
		) {			
		}
		
		public int count = 0;
	}
	
	public QuotaByteArrayOutputStream(
		String id
	) {
		super();
		this.id = id;
		Counter counter = counters.get(id);
		if(counter == null) {
			counters.put(
				id, 
				counter = new Counter()
			);
		}
		counter.count++;
		if(counter.count % 100 == 0) {
			SysLog.log(Level.WARNING, "{0} has created {1} byte array output streams", id, Integer.valueOf(counter.count));			
		}
	}
	
	public ByteArrayInputStream toInputStream(
	) {
		return new ByteArrayInputStream(
			this.buf,
			0,
			this.size()
		);
	}
	
	public byte[] getBuffer(
	) {
		return this.buf;
	}

	
	@Override
    public synchronized void write(
        byte[] b, 
        int off, 
        int len
    ) {
	    super.write(b, off, len);
	    if(this.count >> this.div > 0) {
            SysLog.log(Level.WARNING, "{0} has allocated a byte array output stream which is larger than {1} bytes ", this.id, Integer.valueOf(this.count));
            this.div++;
	    }
    }

    @Override
    public synchronized void write(
        int b
    ) {
        super.write(b);
        if(this.count >> this.div > 0) {
            SysLog.log(Level.WARNING, "{0} has allocated a byte array output stream which is larger than {1} bytes ", this.id, Integer.valueOf(this.count));                     
            this.div++;
        }
    }

    private int div = 12;
    private final String id;
    private static final Map<String,Counter> counters = new ConcurrentHashMap<String,Counter>();
	
}
