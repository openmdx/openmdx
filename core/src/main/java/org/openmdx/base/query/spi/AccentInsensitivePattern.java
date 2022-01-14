/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Access Insensitive Pattern
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.query.spi;

import java.util.EnumSet;

import org.w3c.cci2.RegularExpressionFlag;

public class AccentInsensitivePattern extends RegularExpressionPattern {

	private AccentInsensitivePattern(String pattern, EnumSet<RegularExpressionFlag> flagSet) {
		super(fold(pattern), flagSet);
		this.pattern = pattern;
	}

	/**
	 * Implements <code>Serializable</code>
	 */
	private static final long serialVersionUID = -7427116394830879489L;
	
	private final String pattern;
	
	private static char[] MAPPING = {
		'a', // u00e0
		'a', // u00e1
		'a', // u00e2
		'a', // u00e3
		'a', // u00e4
		'a', // u00e5
		'a', // u00e6
		'c', // u00e7
		'e', // u00e8
		'e', // u00e9
		'e', // u00ea
		'e', // u00eb
		'i', // u00ec
		'i', // u00ed
		'i', // u00ee
		'i', // u00ef
		'd', // u00e0
		'n', // u00f1
		'o', // u00f2
		'o', // u00f3
		'o', // u00f4
		'o', // u00f5
		'o', // u00f6
		'\u00f7',
		'o', // u00f8
		'u', // u00f9
		'u', // u00fa
		'u', // u00fb
		'u', // u00fc
		'y', // u00fd
		'\u00fe',
		'y'  // u00ff
	};

	static AccentInsensitivePattern newInstance(
		final EmbeddedFlags.FlagsAndValue embeddedFlags
	){
		return new AccentInsensitivePattern(embeddedFlags.getValue(), embeddedFlags.getFlagSet());
	}
	
	@Override
	public boolean matches(String input) {
		return super.matches(fold(input));
	}

	@Override
	public String pattern() {
		return this.pattern;
	}

	public static String fold(String value) {
		StringBuilder buffer = new StringBuilder(value.toLowerCase());
		for(int i = 0,l = buffer.length(); i < l; i++) {
			final char c = buffer.charAt(i);
			if(c >= '\u00e0' && c <= '\u00ff') {
				buffer.setCharAt(i, MAPPING[c - '\u00e0']);
			}
		}
		return buffer.toString();
	}	
	
}
