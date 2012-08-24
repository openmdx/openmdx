/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Is-Greater-Than Condition
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2010, OMEX AG, Switzerland
 * All rights reserved.
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
 * <li>ConditionType.IS_GREATER
 * <li>ConditionType.IS_LESS_OR_EQUAL
 * </ul>
 */
public class IsGreaterCondition extends Condition {

    /**
     * Constructor 
     */
    public IsGreaterCondition(
    ) {
        this.fulfils = false;
    }

    /**
     * Constructor 
     *
     * @param quantifier
     * @param feature
     * @param fulfil
     * @param values
     */
    public IsGreaterCondition(
        Quantifier quantifier,
        String feature,
        boolean fulfil,
        Object expression
    ) {
        super(
            quantifier,
            feature,
            expression
        );
        this.fulfils = fulfil;
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3905243407629758775L;

    /**
     * Defines whether the condition shall be <code>true</code> of <code>false</code>
     */
    private boolean fulfils;
        
    /**
     * Clone the condition
     * 
     * @return a clone
     */
    @Override
    public IsGreaterCondition clone(
    ){
        return new IsGreaterCondition(
            this.getQuantifier(), 
            this.getFeature(), 
            this.isFulfil(),
            this.getExpression()
        );
    }

    /**
     * Tells whether the condition shall be <code>true</code> or <code>false</code>
     * 
     * @return <code>true</code> if the condition shall be fulfilled
     */
    public boolean isFulfil() {
        return this.fulfils;
    }

    /**
     * Defines whether the condition shall be <code>true</code> or <code>false</code>
     * 
     * @param fulful <code>true</code> if the condition shall be fulfilled
     */
    public void setFulfil(
        boolean fulfil
    ) {
        this.fulfils = fulfil;
    }

    @Override
    public ConditionType getType(
    ) {
        return this.isFulfil() ? ConditionType.IS_GREATER : ConditionType.IS_LESS_OR_EQUAL;
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
