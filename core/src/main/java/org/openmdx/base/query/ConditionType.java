/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Condition Type
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
 * Condition Type
 */
public enum ConditionType implements Code {
    
    /**
     * The operator SOUNDS_UNLIKE (&#x226D) expects one or more values as &laquo;right&raquo; operand.
     */
    SOUNDS_UNLIKE(
        (short)-6,
        '\u226D'
    ),

    /**
     * The operator IS_UNLIKE (&#x2241) expects one or more values with the wildcard
     * characters '%' replacing any number of characters and '_' replacing a
     * single character as &laquo;right&raquo; operands. The escape character is '\'.
     */
    IS_UNLIKE(
        (short)-5,
        '\u2241'
    ),

    /**
     * The operator IS_OUTSIDE (&#x2276) expects a range specified by two values as 
     * &laquo;right&raquo; operand.
     */
    IS_OUTSIDE(
        (short)-4,
        '\u2276'
    ),

    /**
     * The operator IS_LESS_OR_EQUAL (&#x2264) expects one value as &laquo;right&raquo; operand.
     */
    IS_LESS_OR_EQUAL(
        (short)-3,
        '\u2264'
    ),

    /**
     * The operator IS_LESS (&#x003C) expects one value as &laquo;right&raquo; operand.
     */
    IS_LESS(
        (short)-2,
        '\u003C'
    ),

    /**
     * The operator IS_NOT_IN (&#x2209) expects a set of zero, one or more values as 
     * &laquo;right&raquo; operand.
     * <p>
     * An IS_NOT_IN expression with zero values always evaluates to true.
     */
    IS_NOT_IN(
        (short)-1,
        '\u2209'
    ),

    /**
     * The operator IS_IN (&#x2208) expects a set of zero, one or more values as &laquo;right&raquo; 
     * operand.
     * <p>
     * An IS_IN expression with zero values always evaluates to false.
     */
    IS_IN(
        (short)1,
        '\u2208'
    ),

    /**
     * The operator IS_GREATER_OR_EQUAL (&#x2265) expects one value as &laquo;right&raquo;
     * operand.
     */
    IS_GREATER_OR_EQUAL(
        (short)2,
        '\u2265'
    ),

    /**
     * The operator IS_GREATER (&#x003E) expects one value as &laquo;right&raquo; operand.
     */
    IS_GREATER(
        (short)3,
        '\u003E'
    ),

    /**
     * The operator IS_BETWEEN (&#x226C) expects a range specified by two values as 
     * &laquo;right&raquo; operand.
     */
    IS_BETWEEN(
        (short)4,
        '\u226C'
    ),

    /**
     * The operator IS_LIKE (&#x223C) expects one or more values with the wildcard
     * characters '%' replacing any number of characters and '_' replacing a
     * single character as &laquo;right&raquo; operands. The escape character is '\'.
     */
    IS_LIKE(
        (short)5,
        '\u223C'
    ),

    /**
     * The operator SOUNDS_LIKE (&#x224D) expects one or more values as &laquo;right&raquo; operand.
     */
    SOUNDS_LIKE(
        (short)6,
        '\u224D'
    );

    /**
     * Constructor 
     *
     * @param code
     * @param symbol
     */
    private ConditionType(
        short code,
        char symbol
    ){
        this.code = code;
        this.symbol = symbol;
    }
    
    /**
     * The <code>ConditionType</code>'s legacy code
     */
    private final short code;
    
    /**
     * The <code>ConditionType</code>'s UNICODE symbol
     */
    private final char symbol;
    
    /**
     * 
     */
    private ConditionType inverseCondition;
    
    /**
     * Retrieve the <code>ConditionType</code>'s code
     * 
     * @return the <code>ConditionType</code>'s code
     */
    public short code(){
        return this.code;
    }

    /**
     * Retrieve the <code>ConditionType</code>'s UNICODE symbol
     * 
     * @return the <code>ConditionType</code>'s UNICODE symbol
     */
    public char symbol() {
        return this.symbol;
    }

    /**
     * Retrieve the inverse condition
     * 
     * @return the inverse condition
     */
    public ConditionType invert(
    ){
        return this.inverseCondition;
    }
    
    /**
     * Determines the inverse condition
     * 
     * @return the inverse condition, leaving 0
     */
    public static short invert(short code) {
        return (short) -code;
    }
    
    /**
     * Retrieve the <code>ConditionType</code>'s code
     * 
     * @param type
     * 
     * @return the <code>ConditionType</code>'s code
     */
    public static short codeOf(
        ConditionType type
    ){
        return type == null ? 0 : type.code;
    }
    
    /**
     * Retrieve the <code>ConditionType</code> represented by the given code
     * 
     * @param code
     * 
     * @return the <code>ConditionType</code> represented by the given code
     * 
     * @exception IllegalArgumentException if the code does not represent a <code>ConditionType</code>
     */
    public static ConditionType valueOf(
        short code
    ){
        switch(code){
            case -6: return SOUNDS_UNLIKE;
            case -5: return IS_UNLIKE;
            case -4 : return IS_OUTSIDE;
            case -3 : return IS_LESS_OR_EQUAL;
            case -2 : return IS_LESS;
            case -1 : return IS_NOT_IN;
            case  0 : return null;
            case +1 : return IS_IN;
            case +2 : return IS_GREATER_OR_EQUAL;
            case +3 : return IS_GREATER;
            case +4 : return IS_BETWEEN;
            case +5 : return IS_LIKE;
            case +6 : return SOUNDS_LIKE;
            default: throw new IllegalArgumentException(
                "Invalid condition type code: " + code
            );
        }
    }

    /**
     * Retrieve the <code>ConditionType</code> represented by the given symbol
     * 
     * @param code
     * 
     * @return the <code>ConditionType</code> represented by the given symbol
     * 
     * @exception IllegalArgumentException if the symbol does not represent a <code>ConditionType</code>
     */
    public static ConditionType valueOf(
        char symbol
    ){
        switch(symbol){
//          case '\u27CA' : return WHERE_NOT;
            case '\u226D' : return SOUNDS_UNLIKE;
            case '\u2241' : return IS_UNLIKE;
            case '\u2276' : return IS_OUTSIDE;
            case '\u2264' : return IS_LESS_OR_EQUAL;
            case '\u003C' : return IS_LESS;
            case '\u2209' : return IS_NOT_IN;
            case '\uFFFC' : return null;
            case '\u2208' : return IS_IN;
            case '\u2265' : return IS_GREATER_OR_EQUAL;
            case '\u003E' : return IS_GREATER;
            case '\u226C' : return IS_BETWEEN;
            case '\u223C' : return IS_LIKE;
            case '\u224D' : return SOUNDS_LIKE;
//          case '\u007C' : return WHERE;
            default: throw new IllegalArgumentException(
                "Invalid condition type symbol: u" + Integer.toHexString(symbol)
            );
        }
    }
    
    static {
        for(ConditionType value : ConditionType.values()) {
            value.inverseCondition = ConditionType.valueOf((short)-value.code);
        }
    }
    
}
