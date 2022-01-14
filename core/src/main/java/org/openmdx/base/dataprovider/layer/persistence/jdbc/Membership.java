/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Membership 
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
package org.openmdx.base.dataprovider.layer.persistence.jdbc;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.Quantifier;
import org.openmdx.kernel.exception.BasicException;

/**
 * Membership
 */
class Membership {
    
    Membership(
        Quantifier quantifier,
        ConditionType condition
    ) throws ServiceException{
        switch(condition) {
            case IS_IN:
                if(quantifier == Quantifier.FOR_ALL) {
                    member = false;
                    negated = true;
                } else {
                    member = true;
                    negated = false;
                }
                break;
            case IS_NOT_IN:
                if(quantifier == Quantifier.FOR_ALL) {
                    member = true;
                    negated = true;
                } else {
                    member = false;
                    negated = false;
                }
                break;
            default: throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.BAD_CONTEXT, 
                "Unsupported condition", 
                new BasicException.Parameter("actual", condition),
                new BasicException.Parameter("supported", ConditionType.IS_IN, ConditionType.IS_NOT_IN)
            );
        }
    }

    private final boolean member;
    private final boolean negated;
    
    /**
     * Retrieve member.
     *
     * @return Returns the member.
     */
    boolean isMember() {
        return this.member;
    }
    
    /**
     * Retrieve negated.
     *
     * @return Returns the negated.
     */
    boolean isNegated() {
        return this.negated;
    }
    
}
