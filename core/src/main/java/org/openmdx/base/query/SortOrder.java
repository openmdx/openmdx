/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Sort Order 
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
package org.openmdx.base.query;

/**
 * Sort Order
 */
public enum SortOrder implements Code {

    /**
     * Descending Sort Order (&#x2193;) 
     */
    DESCENDING(
        (short)-1, // code
        '\u2193' // symbol
    ),
    
    /**
     * Unsorted Sort Order (&nbsp;)
     */
    UNSORTED(
        (short)0, // code
        ' ' // symbol
    ),
    
    /**
     * Ascending Sort Order (&#x2191;)
     */
    ASCENDING(
        (short)+1, // code
        '\u2191' // symbol
    );

    /**
     * Constructor 
     *
     * @param code
     * @param symbol
     */
    private SortOrder(
        short code,
        char symbol
    ){
        this.code = code;
        this.symbol = symbol;
    }
    
    /**
     * The <code>SortOrder</code> legacy code
     */
    private final short code;
    
    /**
     * The <code>SortOrder</code> UNICODE symbol
     */
    private final char symbol;
    
    /**
     * Retrieve the <code>SortOrder</code>'s code
     * 
     * @return the <code>SortOrder</code>'s code
     */
    public short code(){
        return this.code;
    }

    /**
     * Retrieve the <code>SortOrder</code>'s UNICODE symbol
     * 
     * @return the <code>SortOrder</code>'s UNICODE symbol
     */
    public char symbol() {
        return this.symbol;
    }

    /**
     * Retrieve the <code>SortOrder</code>'s code
     * 
     * @param sortOrder
     * 
     * @return the <code>SortOrder</code>'s code
     */
    public static short codeOf(
        SortOrder sortOrder
    ){
        return (sortOrder == null ? UNSORTED: sortOrder).code;
    }
    
    /**
     * Retrieve the <code>SortOrder</code> represented by the given code
     * 
     * @param code
     * 
     * @return the <code>SortOrder</code> represented by the given code
     * 
     * @exception IllegalArgumentException if the code does not represent a <code>SortOrder</code>
     */
    public static SortOrder valueOf(
        short code
    ){
        switch(code){
            case -1: return DESCENDING;
            case  0: return UNSORTED;
            case +1: return ASCENDING;
            default: throw new IllegalArgumentException(
                "Invalid sort order code: " + code
            );
        }
    }

    /**
     * Retrieve the <code>SortOrder</code> represented by the given symbol
     * 
     * @param symbol
     * 
     * @return the <code>SortOrder</code> represented by the given symbol
     * 
     * @exception IllegalArgumentException if the symbol does not represent a <code>SortOrder</code>
     */
    public static SortOrder valueOf(
        char symbol
    ){
        switch(symbol){
            case '\u2193': return DESCENDING;
            case ' ': return UNSORTED;
            case '\u2191': return ASCENDING;
            default: throw new IllegalArgumentException(
                "Invalid sort order symbol: u" + Integer.toHexString(symbol)
            );
        }
    }
    
}
