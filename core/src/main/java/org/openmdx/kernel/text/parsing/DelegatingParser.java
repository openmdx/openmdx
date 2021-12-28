/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Delegating Parser 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2014, OMEX AG, Switzerland
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

import java.util.Collection;
import java.util.Optional;

import org.openmdx.kernel.text.spi.Parser;

/**
 * Delegating Parser
 */
public class DelegatingParser extends AbstractParser {
	
	/**
	 * Constructor
	 * 
	 * @param delegates the delegates ordered by priority
	 */
	public DelegatingParser(
		Parser... delegates
	) {
		this.delegates = delegates;
	}

	/**
	 * The delegate parsers 
	 */
	private final Parser[] delegates;

    /* (non-Javadoc)
     * @see org.openmdx.kernel.text.parsing.AbstractParser#supportedTypes()
     */
    @Override
    protected Collection<Class<?>> supportedTypes() {
        throw new UnsupportedOperationException();
    }

	@Override
	public boolean handles(Class<?> type) {
		for(Parser parser : delegates) {
			if(parser.handles(type)) {
				return true;
			}
		}
		return false;
	}

    /* (non-Javadoc)
     * @see org.openmdx.kernel.text.parsing.AbstractParser#handles(java.lang.String)
     */
    @Override
    public Optional<Class<?>> handles(String className) {
        for(Parser parser : delegates) {
            final Optional<Class<?>> optional = parser.handles(className);
            if(optional.isPresent()) {
                return optional;
            }
        }
        return Optional.empty();
    }



    /* (non-Javadoc)
	 * @see org.openmdx.kernel.text.parsing.AbstractParser#parseAs(java.lang.String, java.lang.Class)
	 */
	@Override
	protected Object parseAs(
		String externalRepresentation,
		Class<?> type
	) throws Exception {
		for(Parser parser : delegates) {
			if(parser.handles(type)) {
				return parser.parse(type, externalRepresentation);
			}
		}
		return super.parseAs(externalRepresentation, type);
	}

}