/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestAdaptiveInputStreamReader.java,v 1.2 2005/08/24 20:17:02 hburger Exp $
 * Description: Test Adaptive InputStream Reader
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/08/24 20:17:02 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package org.openmdx.test.tools.ant;

import java.io.IOException;

import org.openmdx.tools.ant.util.AdaptiveInputStreamReader;

/**
 * Test Adaptive InputStream Reader
 */
public class TestAdaptiveInputStreamReader {

	/**
	 * Avoid instantiation
	 */
	private TestAdaptiveInputStreamReader() {
	}

	/**
	 * No JUnit test yet
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String resourcePrefix = "org/openmdx/test/tools/ant/";
		for(
			int i = 0;
			i < RESOURCES.length;
			i++
		) try {
			System.out.println("Testing " + RESOURCES[i]);
			AdaptiveInputStreamReader r = new AdaptiveInputStreamReader(
				TestAdaptiveInputStreamReader.class.getClassLoader().getResourceAsStream(
					resourcePrefix + RESOURCES[i]
				),
				null,
				true,
				true,
				null
			);
			System.out.println("ByteOrderMark = " + r.getByteOrderMark());
			System.out.println("XMLDeclaration = " + r.getXMLDclaration());
			System.out.print("Document: ");
			for(
				int c = r.read();
				c > 0;
				c = r.read()
			) System.out.print((char)c);
			System.out.println('\n');
		} catch (IOException exception) {
			exception.printStackTrace();
		}		
	}

	final static private String[] RESOURCES = new String[]{
		"US-ASCII.txt",
		"ISO-8859-1.txt",
		"UTF-8.txt",
		"UTF-16LE.txt",
		"UTF-16BE.txt"		
	};

}