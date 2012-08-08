/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: OptionalAttributePredicate_1.java,v 1.6 2006/10/19 17:07:10 hburger Exp $
 * Description: Optional Attribute Predicate implementation
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/10/19 17:07:10 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.jmi.spi;

import java.util.Collections;

import org.openmdx.base.accessor.jmi.cci.RefFilter_1_1;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.w3c.cci2.OptionalFeaturePredicate;

/**
 * OptionalAttributePredicate implementation
 */
class OptionalAttributePredicate_1
    extends AbstractPredicate_1
    implements OptionalFeaturePredicate
{

    /**
     * Constructor 
     * 
     * @param delegate
     * @param fieldName
     */
    OptionalAttributePredicate_1 (
        RefFilter_1_1 delegate, 
        ModelElement_1_0 feature
    ){
        super(delegate, feature);
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.OptionalAttributePredicate#isNull()
     */
    public void isNull() {
        refAddValue(
            Quantors.FOR_ALL,
            FilterOperators.IS_IN,
            Collections.EMPTY_SET
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.OptionalAttributePredicate#isNonNull()
     */
    public void isNonNull() {
        refAddValue(
            Quantors.THERE_EXISTS,
            FilterOperators.IS_NOT_IN,
            Collections.EMPTY_SET
        );
    }

}