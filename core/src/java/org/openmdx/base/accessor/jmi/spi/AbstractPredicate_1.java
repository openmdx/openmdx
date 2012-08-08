/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractPredicate_1.java,v 1.5 2009/01/13 02:10:32 wfro Exp $
 * Description: Abstract Predicate Class
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/13 02:10:32 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2008, OMEX AG, Switzerland
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

import java.util.Collection;

import org.openmdx.base.accessor.jmi.cci.RefFilter_1_1;
import org.openmdx.base.mof.cci.ModelElement_1_0;

/**
 * Abstract Predicate Class
 */
abstract class AbstractPredicate_1 {

    /**
     * Constructor 
     * 
     * @param delegate 
     * @param fature
     */
    protected AbstractPredicate_1 (
        RefFilter_1_1 delegate, 
        ModelElement_1_0 featureDef
    ){
        this.delegate = delegate;
        this.featureDef = featureDef;
    }

    /**
     * 
     */
    private final RefFilter_1_1 delegate;

    /**
     * 
     */
    private final ModelElement_1_0 featureDef;

    /**
     * Add a filter criterion
     * 
     * @param quantor
     * @param operator
     * @param values
     */
    protected void refAddValue(
        short quantor,
        short operator,
        Collection<?> values
    ){
        this.delegate.refAddValue(
            featureDef,
            quantor,
            operator,
            values
        );
    }        

    /**
     * Allows to specify the sort order for a field.
     * 
     * @param index
     * @param order
     */
    void refAddValue(
      int index,
      short order
    ){
        this.delegate.refAddValue(
            featureDef,
            index,
            order
        );
    }
    
}