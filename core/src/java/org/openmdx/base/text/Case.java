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
        
        public String toCase(String string) {
            return string == null || "".equals(string) ? 
                string : 
                string.toLowerCase(Locale.US);
        }
        
    },
    
    /**
     * The first letter of the <code>String</code> is capitalized.
     */
    TITLE_CASE {
        
        public String toCase(String string) {
            return string == null || "".equals(string) ? 
                string : 
                string.substring(0, 1).toUpperCase(Locale.US) + string.substring(1).toLowerCase(Locale.US);
         }
        
    },
    
    /**
     * The reply consists of all upper-case alphabetic characters
     */
    UPPER_CASE {
        
        public String toCase(String string) {
            return string == null || "".equals(string) ? 
                string : 
                string.toUpperCase(Locale.US);
        }
        
    };

    /**
     * Convert the characters of a <code>string</code> to their appropriate case.
     *  
     * @param string the <code>String</code> to be converted
     * 
     * @return the converted <code>string</code>
     */
    public abstract String toCase(String string);
    
}