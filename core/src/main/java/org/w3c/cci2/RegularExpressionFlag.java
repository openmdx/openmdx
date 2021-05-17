package org.w3c.cci2;

import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum RegularExpressionFlag {

	SOUNDS(StringTypePredicate.SOUNDS),
	LITERAL(Pattern.LITERAL) {
		@Override
		public void apply(StringBuilder value) {
			String quotedValue = Pattern.quote(value.toString());
			value.setLength(0);
			value.append(quotedValue);
		}
	},
	POSIX_EXPRESSION(StringTypePredicate.POSIX_EXPRESSION, 'P'),
	ACCENT_INSENSITIVE(StringTypePredicate.ACCENT_INSENSITIVE,'I'),
	CASE_INSENSITIVE(Pattern.CASE_INSENSITIVE,'i', 'i'),
	WHITESPACE_INSENSITIVE(Pattern.COMMENTS,'x','x'),
	MULTILINE(Pattern.MULTILINE,'m','m'),
	DOTALL(Pattern.DOTALL, 's', 'n'),
	UNICODE_CASE(Pattern.UNICODE_CASE, 'u'),
	X_QUERY(StringTypePredicate.X_QUERY, 'X'),
	JSON_QUERY(StringTypePredicate.JSON_QUERY, 'j');

	RegularExpressionFlag(int flag, char character, char sqlCharacter) {
		this.flag = flag;
		this.embeddedFlag = character;
		this.matchParameter = sqlCharacter;
	}
	
	RegularExpressionFlag(int flag, char character) {
		this.flag = flag;
		this.embeddedFlag = character;
		this.matchParameter = NUL;
	}

	RegularExpressionFlag(int flag) {
		this.flag = flag;
		this.embeddedFlag = NUL;
		this.matchParameter = NUL;
	}
	
	private static final char NUL = Character.MIN_VALUE; 
	private final int flag;
	private final char embeddedFlag;
	private final char matchParameter;

	/**
	 * Group<ol>
	 * <li>Prefix & Embedded Flags
	 * </ul> 
	 */
	private final Pattern EMBEDDED_FLAGS = Pattern.compile("^(\\(\\?[A-Za-z]+)\\).*");
	
	public boolean isEmbeddable(){
		return embeddedFlag != NUL;
	}

	/**
	 * Tells whether the flag is an SQL match parameter
	 * 
	 * @return <code>true</code> if the flag is an SQL match parameter
	 */
	public boolean isMatchParameter(){
		return matchParameter != NUL;
	}
	
	public boolean isEmbedded(String embeddedFlags) {
		return embeddedFlags.indexOf(this.embeddedFlag) >= 0;
	}
	
	public boolean isSet(int flags) {
		return (this.flag & flags) != 0;
	}

	/**
	 * Apply the flag
	 * 
	 * @param value
	 */
	public void apply(StringBuilder value){
		if(isEmbeddable()) {
			embedInto(value);
		}
	}

	public int getFlag() {
		return flag;
	}

	private void embedInto(StringBuilder target) {
		final Matcher matcher = EMBEDDED_FLAGS.matcher(target);
		if(matcher.matches()) {
			target.insert(matcher.group(1).length(), embeddedFlag);
		} else {
			target.insert(0, "(?)").insert(2, embeddedFlag);
		}
	}

	public static String getMatchParameter(
		int flags
	){
		if(flags == 0) {
			return "";
		} else {
			StringBuilder matchParameters = new StringBuilder();
			for(RegularExpressionFlag flag : RegularExpressionFlag.values()) {
				if(flag.isMatchParameter() && flag.isSet(flags)){
					matchParameters.append(flag.matchParameter);
				}
			}
			return matchParameters.toString();
		}
	}

	public static EnumSet<RegularExpressionFlag> toFlagSet(int flags) {
		EnumSet<RegularExpressionFlag> flagsSet = EnumSet.noneOf(RegularExpressionFlag.class);
		if(flags != 0) {
			for(RegularExpressionFlag flag : RegularExpressionFlag.values()) {
				if(flag.isSet(flags)){
					flagsSet.add(flag);
				}
			}
		}
		return flagsSet;
	}

	public static EnumSet<RegularExpressionFlag> toFlagSet(String embeddedFlags) {
		EnumSet<RegularExpressionFlag> target = EnumSet.noneOf(RegularExpressionFlag.class);
		if(!embeddedFlags.isEmpty()) {
			for(RegularExpressionFlag flag : RegularExpressionFlag.values()) {
	    		if(flag.isEmbeddable() && flag.isEmbedded(embeddedFlags)) {
					target.add(flag);
				}
			}
		}
		return target;
	}
	
	public static String toMatchParameter(EnumSet<RegularExpressionFlag> flagSet){
		if(flagSet.isEmpty()) {
			return "";
		} else {
			StringBuilder matchParameter = new StringBuilder();
			for(RegularExpressionFlag flag : flagSet) {
				if(flag.isMatchParameter()) {
					matchParameter.append(flag.matchParameter);
				}
			}
			return matchParameter.toString();
		}
	}

  	public static int toFlags(EnumSet<RegularExpressionFlag> flagSet){
  		int flags = 0;
		for(RegularExpressionFlag flag : flagSet) {
			if(flag.flag < 0x10000) {
				flags |= flag.flag;
			}
		}
		return flags;
  	}
  	
}
