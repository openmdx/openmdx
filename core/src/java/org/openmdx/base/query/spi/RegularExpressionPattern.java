package org.openmdx.base.query.spi;

import java.util.EnumSet;
import java.util.regex.Pattern;

import org.openmdx.application.dataprovider.spi.EmbeddedFlags.FlagsAndValue;
import org.w3c.cci2.RegularExpressionFlag;

/**
 * Pattern implementation 
 */
class RegularExpressionPattern extends AbstractPattern {

	/**
	 * Constructor
	 * 
	 * @param pattern
	 * @param flags
	 */
	protected RegularExpressionPattern(
		String pattern, 
		EnumSet<RegularExpressionFlag> flagSet
	) {
		this.pattern = pattern;
		this.compiledExpression = Pattern.compile(pattern, RegularExpressionFlag.toFlags(flagSet));
	}

	static AbstractPattern newInstance(
		final FlagsAndValue flagsAndValue
	) {
		return new RegularExpressionPattern(flagsAndValue.getValue(), flagsAndValue.getFlagSet());
	}
	
	/**
	 * Implements <code>Serializable</code>
	 */
    private static final long serialVersionUID = 1340205266230718256L;
    
    private final Pattern compiledExpression;
    private final String pattern;

    public boolean matches(String source) {
        return this.compiledExpression.matcher(source).matches();
    }

    public String pattern() {
        return this.pattern;
    }

}