/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Is-Greater-Than-Or-Equal-To Condition
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
 * Typed condition for<ul>
 * <li>ConditionType.IS_GREATER_OR_EQUAL
 * <li>ConditionType.IS_LESS
 * </ul>
 */
public class IsGreaterOrEqualCondition extends Condition {

	/**
     * Constructor 
     */
    public IsGreaterOrEqualCondition(
    ) {
    	super(toConditionType(false));
    }

    /**
     * Constructor 
     *
     * @param quantifier
     * @param feature
     * @param fulfil
     * @param values
     */
    public IsGreaterOrEqualCondition(
        Quantifier quantifier,
        String feature,
        boolean fulfil,
        Object expression
    ) {
        super(
            quantifier,
            feature,
            toConditionType(fulfil),
            expression
        );
    }

    /**
     * Implements {@code Serializable}
     */
	private static final long serialVersionUID = 679373812211390193L;

    /**
     * Clone the condition
     * 
     * @return a clone
     */
    @Override
    public IsGreaterOrEqualCondition clone(
    ){
        return new IsGreaterOrEqualCondition(
            this.getQuantifier(), 
            this.getFeature(), 
            this.isFulfil(),
            this.getExpression()
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
    	case IS_GREATER_OR_EQUAL : return true;
    	case IS_LESS : return false;
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
    	return fulfil ? ConditionType.IS_GREATER_OR_EQUAL : ConditionType.IS_LESS;
    }
    
    /**
     * Retrieve the expression to used in the comparison
     * 
     * @return the expression to used in the comparison
     */
    public Object getExpression(){
        return super.getValue(0);
    }

}
