/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Lock Assertion
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
package org.openmdx.base.accessor.rest.spi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
#if CLASSIC_CHRONO_TYPES
import org.w3c.spi.DatatypeFactories;
#endif

/**
 * Lock Assertion
 * <p>
 * Parses an expression of the form
 * {@code &lsaquo;feature&rsaquo;&lsaquo;relation&rsaquo;&lsaquo;value&rsaquo;},
 * e.g. {@code modifiedAt=2000-02-29T00:00:00.000000Z}
 */
public class LockAssertion {
	
	/**
	 * Constructor
	 * 
	 * @param expression
	 * 
	 * @throws ServiceException
	 */
	public LockAssertion(
		String expression
	) throws ServiceException {
		Matcher matcher = PATTERN.matcher(expression);
		if(matcher.matches()) {
			this.feature = matcher.group(1);
			this.relation = matcher.group(2);
			this.value = toValue(matcher.group(3));
		} else throw new ServiceException(
			BasicException.Code.DEFAULT_DOMAIN,
			BasicException.Code.BAD_PARAMETER,
			"Assertion can't be parsed into <feature><operator><value>",
			new BasicException.Parameter("pattern", PATTERN.pattern()),
			new BasicException.Parameter("assertion", expression)
		);
	}

	/**
	 * The lock assertion pattern
	 */
	static final Pattern PATTERN = Pattern.compile("([\\w]+)([<=>]+)(.*)");
	
	private final String feature;
	
	private final String relation;
	
	private final Object value;

	/**
	 * Marshal the assertion value
	 *
	 * @param value the assertion value to be marshaled
	 * 
	 * @return the assertion value, or {@code null} if the value is empty
	 */
	private static Object toValue(
		String value
	){
		return value.isEmpty() ?
			null :
			#if CLASSIC_CHRONO_TYPES
			DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(value);
			#else
			java.time.Instant.parse(value);
			#endif
	}

	/**
	 * @return the feature
	 */
	public String getFeature() {
		return feature;
	}

	/**
	 * @return the relation
	 */
	public String getRelation() {
		return relation;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
			
}
