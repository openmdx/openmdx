/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Primitive Type Decoder 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2020, OMEX AG, Switzerland
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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmdx.kernel.text.spi.Decoder;
import org.openmdx.kernel.text.spi.Parser;


/**
 * Primitive Type Decoder
 * <p>
 * Decodes values of the form 
 * "(&lsaquo;the value's qualified type name&rsaquo;)
 * &lsaquo;the value's string representation&rsaquo;" while
 * all other {code String}s are returned unmodified
 */
class PrimitiveTypeDecoder implements Decoder {

    PrimitiveTypeDecoder(Parser parser) {
        this.parser = parser;
    }

    /**
     * Detect encoded values
     */
    private static final Pattern ENCODED = Pattern.compile("\\(([a-zA-Z_0-9$_.]+)\\)(.*)");
    
    /**
     * The parser to be used
     */
    private final Parser parser;
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.text.spi.Decoder#decode(java.lang.String)
     */
    @Override
    public Object decode(String encodedValue) {
        if(encodedValue != null) {
            final Matcher matcher = ENCODED.matcher(encodedValue);
            if(matcher.matches()) {
                final String cast = matcher.group(1);
                final Optional<Class<?>> parsableType = parser.handles(cast);
                if(parsableType.isPresent()) {
                    final String value = matcher.group(2);
                    return parser.parse(parsableType.get(), value);
                }
                throw new IllegalArgumentException("No parser for " + cast);
            }
        }
        return encodedValue;
    }

}
