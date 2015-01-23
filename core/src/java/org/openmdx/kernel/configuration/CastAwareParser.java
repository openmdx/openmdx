/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Cast-Aware Parser 
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
package org.openmdx.kernel.configuration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.text.parsing.AbstractParser;
import org.openmdx.kernel.text.spi.Parser;

/**
 * Cast-Aware Parser
 */
class CastAwareParser extends AbstractParser {

	CastAwareParser(
		Parser delegate
	) {
		this.delegate = delegate;
	}

	private final Parser delegate;
	
	static final Pattern CASTED_VALUE_PATTERN = Pattern.compile(
		"\\(([A-Za-z0-9_.]+)\\)(.*)"
	);
	
	@Override
	public boolean handles(Class<?> type) {
		return type == null || delegate.handles(type);
	}

	/* (non-Javadoc)
	 * @see org.openmdx.kernel.text.parsing.AbstractParser#parseAs(java.lang.String, java.lang.Class)
	 */
	@Override
	protected Object parseAs(
		String externalRepresentation, 
		Class<?> valueClass
	) throws Exception {
		final Matcher matcher = CASTED_VALUE_PATTERN.matcher(externalRepresentation);
		if(matcher.matches()) {
			final String typeName = matcher.group(1);
			final String value = matcher.group(2);
			final Class<?> type;
			if(valueClass == null || !typeName.equals(valueClass.getName())) {
				type = Classes.getApplicationClass(typeName);
			} else {
				type = valueClass;
			}
			return delegate.parse(type, value);
		} else if(valueClass == null) {
			return externalRepresentation;
		} else {
			return delegate.parse(valueClass, externalRepresentation);
		}
	}
	
}
