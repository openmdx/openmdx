/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Condition
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

import org.openmdx.base.rest.spi.ConditionRecord;

/**
 * Abstract Condition
 */
public abstract class Condition extends ConditionRecord {

	/**
     * Constructor 
     */
    protected Condition(
    ){
    	super();
    }
	
	/**
     * Constructor 
     */
    protected Condition(
    	ConditionType type	
    ) {
        super(type);
    }

    /**
     * Constructor 
     * 
     * @param quantifier the quantifier
     * @param feature the feature name
     * @param type the condition type
     * @param values the condition values with their type dependent semantic
     */
    protected Condition(
        Quantifier quantifier,
        String feature,
    	ConditionType type,	
        Object... values
    ) {
    	super(
    		quantifier,
    		feature,
    		type,
    		values
    	);
    }

    /**
     * Implements {@code Serializable}
     */
	private static final long serialVersionUID = 396749318163145090L;

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public abstract Condition clone(
    );

    /**
     * Set the quantifier's {@code enum} representation
     * 
     * @param the quantifier's {@code enum} representation
     */
    public void setQuantifier(
        Quantifier quantifier
    ) {
        super.setQuantifier(quantifier);
    }
    
    /**
     * Required to decode legacy XML data
     * 
     * @deprecated use {@link Condition#setQuantifier(Quantifier)}
     */
    @Deprecated
    public void setQuantor(
        short quantor
    ) {
        setQuantifier(Quantifier.valueOf(quantor));
    }

    /**
     * Set the feature name
     * 
     * @param the unqualified feature name
     */
    public void setFeature(
        String feature
    ) {
        super.setFeature(feature);
    }

    /**
     * Replace a single value (with a condition type specific semantic)
     * 
     * @param index the index
     * @param value the value
     */
    public void setValue(
        int index,
        Object value
    ) {
    	getValue()[index] = value;
    }

    /**
     * Retrieve the condition type's string representation
     * 
     * @return the condition type's name
     */
    public String getName(
    ){
        ConditionType type = this.getType();
        return type == null ? "PIGGY_BACK" : type.name();
    }

    /**
     * Retrieve the condition type's character representation
     * 
     * @return the condition type's symbol
     */
    public char getSymbol(
    ){
        ConditionType type = this.getType();
        return type == null ? '\uFFFC' : type.symbol();
    }

    //-------------------------------------------------------------------------
    // Implements Record
    //-------------------------------------------------------------------------
    
    @Override
    public String getRecordShortDescription() {
    	return null;
    }

}
