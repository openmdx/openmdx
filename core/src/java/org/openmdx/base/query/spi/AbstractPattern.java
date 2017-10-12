/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Pattern
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2013, OMEX AG, Switzerland
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
package org.openmdx.base.query.spi;

import java.io.Serializable;

import org.openmdx.application.dataprovider.spi.EmbeddedFlags;
import org.openmdx.application.dataprovider.spi.EmbeddedFlags.FlagsAndValue;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.url.protocol.XRI_1Protocols;
import org.w3c.cci2.RegularExpressionFlag;

/**
 * Pattern interface
 * <p>
 * Instances of this class are immutable and are safe for use by 
 * multiple concurrent threads. 
 */
public abstract class AbstractPattern implements Serializable {

    /**
	 * Implements <code>Serializable</code>
	 */
	private static final long serialVersionUID = 5177895128844669908L;

	/**
     * Attempts to match the given input against the pattern
     *  
     * @param input
     * 
     * @return <code>true</code> if the input mtaches the pattern
     */
    public abstract boolean matches (
        String input
    );

    /**
     * Returns the expression from which this pattern was compiled. 
     *  
     * @return The source of this pattern
     */
    public abstract String pattern();
        
	/**
	 * Creates a new pattern
	 * 
	 * @param value
	 * 
	 * @return the corresponding pattern
	 */
	public static AbstractPattern newInstance(Object value) {
		return value instanceof Path ? PathPattern.newInstance((Path)value) : newInstance((String)value);
	}

	/**
	 * Creates a new pattern
	 * 
	 * @param valueWithEmbeddedFlags
	 * 
	 * @return the corresponding pattern
	 */
	private static AbstractPattern newInstance(String valueWithEmbeddedFlags) {
		if(valueWithEmbeddedFlags.startsWith(XRI_1Protocols.OPENMDX_PREFIX)) {
			return PathPattern.newInstance(valueWithEmbeddedFlags);
		} else {
			final FlagsAndValue flagsAndValue = EmbeddedFlags.getInstance().parse(valueWithEmbeddedFlags);
			if(flagsAndValue.getFlagSet().contains(RegularExpressionFlag.ACCENT_INSENSITIVE)){
				return AccentInsensitivePattern.newInstance(flagsAndValue);
			} else if(flagsAndValue.getFlagSet().contains(RegularExpressionFlag.JSON_QUERY)) {
			    return JsonQueryPattern.newInstance(flagsAndValue);
			} else {
				return RegularExpressionPattern.newInstance(flagsAndValue);
			}
		}
	}

}