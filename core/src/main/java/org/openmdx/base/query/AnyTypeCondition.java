/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Any Type Condition
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
 * Any Type Condition
 */
public class AnyTypeCondition extends Condition {

	/**
     * Constructor 
     */
    public AnyTypeCondition(
    ){
        super();
    }

    /**
     * Constructor 
     * 
     * @param quantifier the quantifier
     * @param feature the feature name
     * @param type the condition type
     * @param values the condition values with their type dependent semantic
     */
    public AnyTypeCondition(
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
     * Implements <code>Serializable</code>
     */
	private static final long serialVersionUID = -1593541490032431549L;

    /**
     * Clone the condition
     * 
     * @return a clone
     */
    @Override
    public AnyTypeCondition clone(
    ){
        return new AnyTypeCondition(
            getQuantifier(), 
            getFeature(), 
            getType(), 
            getValue()
        );
    }

    /**
     * Set the condition type by name
     * 
     * @param name
     */
    public void setName(
        String name
    ){
    	super.setType(ConditionType.valueOf(name));
    }
    
}
