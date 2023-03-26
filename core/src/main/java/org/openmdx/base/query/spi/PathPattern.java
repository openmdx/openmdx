/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Path Pattern
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

import org.openmdx.base.naming.Path;

/**
 * Path Pattern
 */
public final class PathPattern extends AbstractPattern {

	/**
	 * Constructor
	 * 
	 * @param pathPattern
	 */
	PathPattern(
		Path pathPattern
	) {
		this.pathPattern = pathPattern;
	}
	
	/**
	 * Implements {@code Serializable}
	 */
    private static final long serialVersionUID = 3256441391432086579L;
    
    private final Path pathPattern;
    
    /**
     * Creates a new pattern
     * 
     * @param value
     * 
     * @return the corresponding pattern
     */
    public static PathPattern newInstance(Path value) {
    	return new PathPattern(value);
    }
    
    /**
     * Creates a new pattern
     * 
     * @param value
     * 
     * @return the corresponding pattern
     */
    public static PathPattern newInstance(String value) {
    	return newInstance(new Path(value));
    }
    
    @Override
    public boolean matches(String input) {
        try {
            return this.matches(new Path(input));
        } catch (Exception exception){
            return false;
        }
    }

    public boolean matches(Path input) {
        return input.isLike(this.pathPattern);
    }

    @SuppressWarnings("deprecation")
    public String pattern() {
        return this.pathPattern.toUri();
    }

	
}