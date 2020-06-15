/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Code Parser 
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
package test.openmdx.datatypes1.dto;

import java.util.Optional;

import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.text.spi.Parser;

/**
 * Code Parser
 */
public class CodeParser implements Parser {

    /**
     * Constructor 
     */
    public CodeParser() {
        super();
    }

    /* (non-Javadoc)
     * @see org.w3c.spi.Parser#handles(java.lang.Class)
     */
    @Override
    public boolean handles(Class<?> type) {
        return type != null && Code.class.isAssignableFrom(type);
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.text.spi.Parser#handles(java.lang.String)
     */
    @Override
    public Optional<Class<?>> handles(
        String className
    ) {
        final Class<Object> type;
        try {
            type = Classes.getApplicationClass(className);
        } catch (ClassNotFoundException exception) {
            return Optional.empty();
        }
        return handles(type) ? Optional.of(type) : Optional.empty();
    }

    /* (non-Javadoc)
     * @see org.w3c.spi.Parser#parse(java.lang.Class, java.lang.String)
     */
    @Override
    public <T> T parse(Class<T> type, String source) {
        if(source == null && handles(type)) return null;
        if(type == CountryCode.class)  return type.cast(CountryCode.valueOf(source));
        throw new IllegalArgumentException(type.getName() + " is not supported by " + getClass().getName());
    }

}
