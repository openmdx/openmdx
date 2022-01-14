/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: StandardPrimitiveTypeParser 
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */

package org.openmdx.kernel.text.parsing;

import java.text.ParseException;
import java.util.Collection;
import java.util.Optional;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.spi.Parser;

/**
 * Abstract Parser
 */
public abstract class AbstractParser implements Parser {

    /**
     * Constructor 
     */
    protected AbstractParser() {
        super();
    }

    /* (non-Javadoc)
     * @see org.w3c.spi.Parser#parse(java.lang.Class, java.lang.String)
     */
	@SuppressWarnings("unchecked")
	@Override
    public <T> T parse(
    	Class<T> valueClass, 
    	String string
    ) {
    	try {
    		if(string == null){
    			return null;
    		}
	    	final Object value = parseAs(string, valueClass);
			if(valueClass == null) {
				return (T)value;
			} else {
				return valueClass.cast(value);
			}
    	} catch (Exception exception) {
    		throw  BasicException.initHolder(
                new IllegalArgumentException(
                    "Unable to parse the given value",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.PARSE_FAILURE,
                        new BasicException.Parameter(
                        	"class", 
                        	valueClass == null ? "null" : valueClass.getName()
                        ),
                        new BasicException.Parameter("value", string)
                    )
                )
            );
    	}
    }
    
    /**
     * Parse the string as given type
     * 
     * @param valueClass the type
     * @param value the string to be parsed
     * @return the corresponding type instance
     * 
     * @throws Exception in case of failure
     */
    protected Object parseAs(
    	String externalRepresentation, 
    	Class<?> valueClass
    ) throws Exception {
    	if(valueClass == null) {
    		return externalRepresentation;
    	} else {
	        throw new ParseException(
	        	"The given value class is not handled by this parser", 
	        	0
	        );
    	}
    }
            
    /**
     * Tells which types are supported by this parser
     *
     * @return the types supported by this parser
     */
    protected abstract Collection<Class<?>> supportedTypes();
    
    /* (non-Javadoc)
     * @see org.w3c.spi.Parser#handles(java.lang.Class)
     */
    @Override
    public boolean handles(Class<?> type) {
        return supportedTypes().contains(type);
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.text.spi.Parser#handles(java.lang.String)
     */
    @Override
    public Optional<Class<?>> handles(String className) {
        for(Class<?> candidate : supportedTypes()) {
            if(className.equals(candidate.getName())) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

}
