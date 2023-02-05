/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Case
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
package org.openmdx.base.text;

import java.util.Locale;

/**
 * Case
 */
public enum Case {
    
    /**
     * The reply consists of lower-case alphabetic characters only.
     */
    LOWER_CASE {
        
        @Override
        public String toCase(String string) {
            return string == null || string.length() == 0 ? 
                string : 
                string.toLowerCase(Locale.US);
        }
        
    },
    
    /**
     * The first letter of the {@code String} is capitalized while all 
     * others are decapitalized.
     */
    TITLE_CASE {
        
        @Override
        public String toCase(String string) {
            return string == null || string.length() == 0 ? 
                string :
                Character.toTitleCase(string.charAt(0)) + string.substring(1).toLowerCase(Locale.US);
         }
        
    },
    
    /**
     * The reply consists of all upper-case alphabetic characters
     */
    UPPER_CASE {
        
        @Override
        public String toCase(String string) {
            return string == null || string.length() == 0 ? 
                string : 
                string.toUpperCase(Locale.US);
        }
        
    },

    /**
     * The first letter of the {@code String} is decapitalized.
     */
    LOWER_CAMEL_CASE {
        
        @Override
        public String toCase(String string) {
            return string == null || string.length() == 0 ? 
                string : 
                Character.toLowerCase(string.charAt(0)) + string.substring(1);
         }
        
    },
    
    /**
     * The first letter of the {@code String} is capitalized.
     */
    UPPER_CAMEL_CASE {
        
        @Override
        public String toCase(String string) {
            return string == null || string.length() == 0 ? 
                string : 
                Character.toUpperCase(string.charAt(0)) + string.substring(1);
         }
        
    };
    
    /**
     * Convert the characters of a {@code string} to their appropriate case.
     *  
     * @param string the {@code String} to be converted
     * 
     * @return the converted {@code string}
     */
    public abstract String toCase(String string);
    
}