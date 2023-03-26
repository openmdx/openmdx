/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Query Record
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
package org.openmdx.base.rest.cci;

import java.util.List;

import javax.resource.cci.MappedRecord;

import org.w3c.cci2.AnyTypePredicate;

/**
 * {@code org::openmdx::kernel::QueryFilter}
 */
public interface QueryFilterRecord extends MappedRecord, AnyTypePredicate {
    
    /**
     * An object record's name
     */
    String NAME = "org:openmdx:kernel:QueryFilter";

    /**
     * Retrieve the query conditions
     * 
     * @return the query conditions
     */
    List<ConditionRecord> getCondition();
    
    /**
     * Retrieve the order specifiers
     * 
     * @return the order specifiers
     */
    List<FeatureOrderRecord> getOrderSpecifier();
    
    /**
     * Retrieve the query extensions
     * 
     * @return the query extensions
     */
    List<QueryExtensionRecord> getExtension();
 
    /**
     * Deep clone
     * 
     * @return a clone of this record
     */
    QueryFilterRecord clone();
    
    enum Member {
        condition,
        extension,
        orderSpecifier
    }

}
