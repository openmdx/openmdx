/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Mapping Format Parser
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.application.mof.mapping.spi;

/**
 * Mapping Format Parser
 */
public class MappingFormatParser {

	/**
	 * Constructor
	 * 
	 * @param format the format to be parsed
	 */
	MappingFormatParser(String format) {
        if(format.endsWith(")")){
            int open = format.indexOf('(');
            int close = format.length() - 1;
            id = format.substring(0, open);
            arguments = format.substring(open+1, close).split(",");
        } else {
        	int colon = format.indexOf(':') ;
        	if(colon > 0) {
        		id = format.substring(0, colon);
                arguments = format.substring(colon+1).split(",");
        	} else {
	            id = format;
	            arguments = new String[]{};
        	}
        }
	}
	
	/**
	 * Either one of the predefined mapping types or a fully qualified class name
	 * 
	 * @return the format id
	 * 
	 * @see org.openmdx.application.mof.mapping.cci.MappingTypes
	 */
	private final String id;
	
	/**
	 * The (optional) arguments
	 */
	private final String[] arguments;
	
	/**
	 * Either one of the predefined mapping types or a fully qualified class name
	 * 
	 * @return the format id
	 * 
	 * @see org.openmdx.application.mof.mapping.cci.MappingTypes
	 */
	String getId() {
		return id;
	}
	
	String[] getArguments() {
		return arguments;
	}
	
}
