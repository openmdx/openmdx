/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Embedded Flags
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
package org.openmdx.base.query.spi;

import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.cci2.RegularExpressionFlag;

public class EmbeddedFlags {

	private EmbeddedFlags(){
		super();
	}
	
	private final static EmbeddedFlags INSTANCE = new EmbeddedFlags();
	
	/**
	 * Group<ol>
	 * <li>Prefix & Embedded Flags
	 * <li>Embedded Flags
	 * <li>Value
	 * </ul> 
	 */
	private final Pattern EMBEDDED_FLAGS = Pattern.compile("^(\\(\\?([A-Za-z]+))\\)(.*)$");
	
	public static EmbeddedFlags getInstance(){
		return INSTANCE;
	}
	
	public FlagsAndValue parse(
		String valueWithEmbeddedFlags 	
	){
		final Matcher matcher = EMBEDDED_FLAGS.matcher(valueWithEmbeddedFlags);
		final EnumSet<RegularExpressionFlag> flagSet;
		
		final String value;
		if(matcher.matches()) {
			flagSet = RegularExpressionFlag.toFlagSet(matcher.group(2));
			value = matcher.group(3);
		} else {
			flagSet = EnumSet.noneOf(RegularExpressionFlag.class);
			value = valueWithEmbeddedFlags;
		}
		return new FlagsAndValue(){

			@Override
			public String getValue() {
				return value;
			}

			/* (non-Javadoc)
			 * @see org.openmdx.application.dataprovider.spi.EmbeddedFlags.FlagsAndValue#getFlagSet()
			 */
			@Override
			public EnumSet<RegularExpressionFlag> getFlagSet() {
				return flagSet;
			}
			
		};
	}

    public String embedFlags(EnumSet<RegularExpressionFlag> flagSet, String value){
    	if(flagSet.isEmpty()){
    		return value;
    	} else {
	    	StringBuilder amendable = new StringBuilder(value);
	    	for(RegularExpressionFlag flag : flagSet) {
    			flag.apply(amendable);
	    	}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
	    	return amendable.toString();
    	}
    }

	public interface FlagsAndValue {
		EnumSet<RegularExpressionFlag> getFlagSet();
		String getValue();
	}

}
