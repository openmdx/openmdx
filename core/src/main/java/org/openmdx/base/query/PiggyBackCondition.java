/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Piggy-Back Condition
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
 * Required to decode legacy XML data
 * 
 * @deprecated use {@link org.openmdx.base.rest.cci.QueryExtensionRecord}
 */
@Deprecated
public class PiggyBackCondition extends Condition {
    
    /**
     * Required to decode legacy XML data
     * 
     * @deprecated use {@link org.openmdx.base.rest.cci.QueryExtensionRecord}
     */
    @Deprecated
    public PiggyBackCondition(
    ) {
        super((ConditionType)null);
    }

    /**
     * Constructor 
     *
     * @param feature
     * @param values
     */
    private PiggyBackCondition(
        String feature,
        Object... values
    ) {
        super(
            null,
            feature,
            null,
            values
        );
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -734596366612105605L;

    /**
     * Clone the condition
     * 
     * @return a clone
     */
    @Override
    public PiggyBackCondition clone(
    ){
        return new PiggyBackCondition(
            this.getFeature(), 
            this.getValue()
        );
    }

    /**
     * Required to decode legacy XML data
     * 
     * @deprecated
     */
    @Deprecated
    public void setFulfil(
        boolean fulfil
    ) {
        // ignored
    }

}
