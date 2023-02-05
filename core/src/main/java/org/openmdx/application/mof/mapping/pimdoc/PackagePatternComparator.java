/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Package Pattern Comparator
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
package org.openmdx.application.mof.mapping.pimdoc;

import java.util.Comparator;

/**
 * This comparator shall ensure that wildcard entries follow the corresponding entries without wildcard.
 */
class PackagePatternComparator implements Comparator<String> {

	/**
	 * The wildard (asterisks) is allowed as last segment of the package name only!
	 */
	private static final String WILDCARD = "**";
	private static final String A_LATE_ASCII_CHARACTER = "~";
	private static final String SEPARATOR = ":";
	private static final int TO_STRIP = SEPARATOR.length() + WILDCARD.length();
	
	@Override
	public int compare(final String packagePattern1, final String packagePattern2) {

		final String stripped1 = removeWildcard(packagePattern1);
		final String stripped2 = removeWildcard(packagePattern2);		
		if(stripped1.equals(stripped2)) {
			return packagePattern1.length() - packagePattern2.length();
		}
		
		final String reordered1 = orderWildcardLast(packagePattern1);
		final String reordered2 = orderWildcardLast(packagePattern2);
		return reordered1.compareTo(reordered2);
	}
	
	/**
	 * Default visibility for testing
	 */
	static String orderWildcardLast(String packagePattern) {
		return  packagePattern.replace(WILDCARD, A_LATE_ASCII_CHARACTER);
	}
	
	/**
	 * The asterisk is allowed as last character only!
	 * 
	 * @return {@code true} falls der Eintrag auf {@code '*'} endet.
	 */
	static boolean isWildcardPattern(String packagePattern) {
		return isCatchAllPattern(packagePattern) || packagePattern.endsWith(SEPARATOR + WILDCARD);
	}
	
	static String removeWildcard(String packagePattern) {
		if(isWildcardPattern(packagePattern)) {
			return isCatchAllPattern(packagePattern) ? "" :
				packagePattern.substring(0, packagePattern.length() - TO_STRIP);
		} else {
			return packagePattern;
		}
	}

	static boolean isCatchAllPattern(String packagePattern) {
		return WILDCARD.equals(packagePattern);
	}

	/**
	 * The catch all pattern shows all classes
	 * 
	 * @return {@code "**"}
	 */
	static String getCatchAllPattern() {
		return WILDCARD;
	}
	
}