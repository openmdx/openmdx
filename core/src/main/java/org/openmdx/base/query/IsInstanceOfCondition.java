/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Is-Instance-Of Condition
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

import org.openmdx.base.accessor.cci.SystemAttributes;

/**
 * Typed Instance-Of Condition
 */
public class IsInstanceOfCondition extends Condition {

	/**
     * Constructor 
     */
    public IsInstanceOfCondition(
    ) {
    	super(toConditionType(false));
    }

    /**
     * Constructor 
     *
     * @param quantifier
     * @param fulfils tells whether IS_IN or IS_NOT_IN shall be applied
     * @param subClasses tells whether sub-classes match as well or not
     * @param className the qualified class name
     */
    public IsInstanceOfCondition(
        Quantifier quantifier,
        boolean fulfils,
        boolean subClasses,
        String... className
    ) {
        super(
            quantifier,
            subClasses ? SystemAttributes.OBJECT_INSTANCE_OF : SystemAttributes.OBJECT_CLASS,
    		toConditionType(fulfils),		
            (Object[])className
        );
    }

    /**
     * Constructor 
     *
     * @param subClasses tells whether sub-classes match as well or not
     * @param className the qualified class name
     */
    public IsInstanceOfCondition(
        boolean subClasses,
        String... className
    ) {
        this(
            Quantifier.THERE_EXISTS,
            true, // fulfils
            subClasses,
            className
        );
    }

    /**
     * Constructor 
     *
     * @param className the qualified class name
     */
    public IsInstanceOfCondition(
        String... className
    ) {
        this(
            true, // subClasses
            className
        );
    }
    
    /**
     * Implements {@code Serializable}
     */
	private static final long serialVersionUID = -1204381390127466354L;

    /**
     * Clone the condition
     * 
     * @return a clone
     */
    @Override
    public IsInCondition clone(
    ){
        return new IsInCondition(
            this.getQuantifier(), 
            this.getFeature(), 
            this.isFulfil(),
            this.getValue()
        );
    }
    
    /**
     * Defines whether the condition shall be {@code true} or {@code false}
     * 
     * @param fulful {@code true} if the condition shall be fulfilled
     */
    public void setFulfil(
		boolean fulfil
	) {
    	super.setType(toConditionType(fulfil));
    }
    
    /**
     * Tells whether the condition shall be {@code true} or {@code false}
     * 
     * @return {@code true} if the condition shall be fulfilled
     */
    public boolean isFulfil() {
    	final ConditionType type = getType();
		switch(type) {
	    	case IS_IN : return true;
	    	case IS_NOT_IN : return false;
	    	default: throw new IllegalStateException("An " + getClass().getSimpleName() + " requires another type: " + type);
		}
    }

    /**
     * Convert the fulfil argument to the underlying condition type
     * 
     * @param fulfil
     * 
     * @return the corresponding condition type
     */
    private static ConditionType toConditionType(boolean fulfil) {
    	return fulfil ? ConditionType.IS_IN : ConditionType.IS_NOT_IN;
    }
    
}
