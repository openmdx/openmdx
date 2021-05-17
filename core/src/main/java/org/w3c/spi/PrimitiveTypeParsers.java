/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Primitive Type Parser 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013, OMEX AG, Switzerland
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
package org.w3c.spi;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.openmdx.base.text.parsing.ImmutablePrimitiveTypeParser;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.loading.Resources;
import org.openmdx.kernel.text.parsing.DelegatingParser;
import org.openmdx.kernel.text.parsing.StandardPrimitiveTypeParser;
import org.openmdx.kernel.text.spi.Decoder;
import org.openmdx.kernel.text.spi.Parser;

/**
 * Primitive Type Parsers
 */
public class PrimitiveTypeParsers {

	/**
	 * Constructor
	 */
	private PrimitiveTypeParsers(){
		// Avoid instantiation
	}

	/**
	 * This array is for private use only!
	 */
	private static final Parser[] STANDARD_PARSERS = {
        StandardPrimitiveTypeParser.getInstance(),  
        ImmutablePrimitiveTypeParser.getInstance()
	};
	
	/**
	 * The eagerly provided standard parser.
	 */
	private static final Parser STANDARD_PARSER = new DelegatingParser(STANDARD_PARSERS);

	
	/**
	 * The lazily provided extended parser
	 */
    private static Parser EXTENDED_PARSER;

	/**
	 * Provides the standard parsers.
	 * 
	 * @return a parser combining the standard parsers with the configured ones
	 */
    public static Parser getStandardParser(
    ){
    	return STANDARD_PARSER;
    }

	/**
	 * Provides a parser which combines the standard parsers with the 
	 * configured ones.
	 * 
	 * @return a parser combining the standard parsers with the configured ones
	 */
    public static Parser getExtendedParser(
    ){
    	if(EXTENDED_PARSER == null) {
    		EXTENDED_PARSER = new DelegatingParser(getExtendedParsers());
    	}
    	return EXTENDED_PARSER;
    }
    
    public static Decoder getDecoder(
        Parser parser
    ) {
       return new PrimitiveTypeDecoder(parser); 
    }
    
    /**
     * Combine the standard parsers with the configured parsers
     * 
     * @return the extended parser array
     */
    private static Parser[] getExtendedParsers(
    ){
        final List<Parser> parsers = new ArrayList<Parser>(
            Arrays.asList(STANDARD_PARSERS)
        );
        addConfiguredParsers(parsers);
    	return parsers.toArray(new Parser[parsers.size()]);
    }

    /**
     * This method amends the given parsers with the configured ones
     * and logs configuration exceptions silently. Problems caused by
     * missing <code>Parser</code>s on the other hand are signaled by 
     * <code>IllegalArgumentException</code>s at run-time.
     */
	private static void addConfiguredParsers(final List<Parser> parsers) {
		try {
            for(URL resource : Resources.getMetaInfResources("openmdx-primitive-types.properties")) {
                final Properties properties = new Properties();
                properties.load(resource.openStream());
                final String parserClassName = properties.getProperty("parser");
                try {
                    parsers.add(Classes.newApplicationInstance(Parser.class, parserClassName));
                } catch (Exception exception) {
                	Throwables.log(
	                	BasicException.newStandAloneExceptionStack(
	                        exception,
	                        BasicException.Code.DEFAULT_DOMAIN,
	                        BasicException.Code.INVALID_CONFIGURATION,
	                        "Primitive type configuration failure",
	                        new BasicException.Parameter("resource", resource.toExternalForm()),
	                        new BasicException.Parameter("parser", parserClassName)
	                    )
	                );
                 }
            }
        } catch (IOException exception) {
        	Throwables.log(
            	BasicException.newStandAloneExceptionStack(
	                exception,
	                BasicException.Code.DEFAULT_DOMAIN,
	                BasicException.Code.INVALID_CONFIGURATION,
	                "Primitive type configuration failure",
	                new BasicException.Parameter("resource", "openmdx-primitive-types.properties")
	            )
	        );
        }
	}
	
}
