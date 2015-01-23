/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Standard Primitive Type Parser 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013-2014, OMEX AG, Switzerland
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
package org.openmdx.kernel.text.parsing;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.ietf.jgss.Oid;
import org.openmdx.kernel.text.spi.Parser;

/**
 * Standard Primitive Type Parser
 */
public class StandardPrimitiveTypeParser extends AbstractParser {

    /**
     * Constructor 
     */
    private StandardPrimitiveTypeParser() {
        super();
    }

    /**
     * The supported types
     */
    private static final List<Class<?>> SUPPORTED_TYPES = Arrays.<Class<?>>asList(
   		Boolean.class,
   		String.class,
    	Byte.class,	
    	Short.class,
    	Integer.class,
        Long.class,
        BigDecimal.class,
        BigInteger.class,
        URI.class,
        Oid.class
    );
    
    /**
     * An instance
     */
    private static final Parser INSTANCE = new StandardPrimitiveTypeParser();
    
    /**
     * Retrieve an instance
     * 
     * @return an instance
     */
    public static Parser getInstance(){
        return INSTANCE;
    }
        
    /* (non-Javadoc)
     * @see org.w3c.spi.Parser#handles(java.lang.Class)
     */
    @Override
    public boolean handles(Class<?> type) {
        return SUPPORTED_TYPES.contains(type);
    }

    /* (non-Javadoc)
	 * @see org.openmdx.kernel.text.parse.AbstractParser#parse(java.lang.String, java.lang.Class)
	 */
	@Override
	protected Object parseAs(String externalRepresentation, Class<?> valueClass) throws Exception {
    	return 
       		valueClass == Byte.class ? Byte.valueOf(externalRepresentation) :
    		valueClass == Long.class ? Long.valueOf(externalRepresentation) :
    		valueClass == Integer.class ? Integer.valueOf(externalRepresentation) :
    		valueClass == Short.class ? Short.valueOf(externalRepresentation) :
    		valueClass == String.class ? externalRepresentation :
    		valueClass == Boolean.class ? Boolean.valueOf(externalRepresentation) :
    		valueClass == BigDecimal.class ? new BigDecimal(externalRepresentation) :
    		valueClass == BigInteger.class ? new BigInteger(externalRepresentation) :
    		valueClass == URI.class ? new URI(externalRepresentation) :
    		valueClass == Oid.class ? new Oid(externalRepresentation) :
    		super.parseAs(externalRepresentation, valueClass);
	}

}
