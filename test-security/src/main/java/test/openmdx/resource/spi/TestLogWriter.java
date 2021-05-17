/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: Test Log Adapter
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2018, OMEX AG, Switzerland
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
package test.openmdx.resource.spi;

import javax.resource.ResourceException;

import org.junit.Test;
import org.openmdx.resource.spi.LogWriter;

public class TestLogWriter {

	@Test
	public void whenLowLevelThanForwardToLog() {
		// Arrange
		try (LogWriter testee = new LogWriter(TestLogWriter.class.getName())){
		// Act
		testee.println("Hello World");
		testee.write("Left");
		testee.write('\t');
		testee.println("Right");
		testee.append("Epilog");
		testee.flush();
		// Assert
		}
	}

	@Test
	public void whenMessageThanForwardToLog() {
		// Arrange
		try (LogWriter testee = new LogWriter(TestLogWriter.class.getName())){
		// Act
		LogWriter.log(testee, "Hallo {0}", "Welt");	
		// Assert
		}
	}
	
	@Test
	public void whenExceptionForwardToLog() {
		// Arrange
		try (LogWriter testee = new LogWriter(TestLogWriter.class.getName())){
			final ResourceException exception = new ResourceException("Hello World","4711");
		// Act
			LogWriter.log(testee, exception, false);
		}
	}
	
}
