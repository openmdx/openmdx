/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Condition.java,v 1.12 2010/03/31 14:39:23 hburger Exp $
 * Description: Condition
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/31 14:39:23 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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

import java.io.Serializable;
import java.util.Arrays;

import javax.resource.ResourceException;

import org.openmdx.base.resource.Records;

public abstract class Condition
    implements Serializable, Cloneable {


    /**
     * Constructor 
     */
    protected Condition(){
        // For encoding and decoding
    }
    
    //-----------------------------------------------------------------------    
    protected Condition(
        Object... values
    ) {
        this(null, null, false, values);
    }
  
    //-----------------------------------------------------------------------    
    protected Condition(
        short quantor,
        String feature,
        boolean fulfils,
        Object... values
    ) {
        this.quantifier = Quantifier.valueOf(quantor);
        this.feature = feature;
        this.fulfils = fulfils;
        this.values = values;
    }

    //-----------------------------------------------------------------------    
    protected Condition(
        Quantifier quantifier,
        String feature,
        boolean fulfils,
        Object... values
    ) {
        this.quantifier = quantifier;
        this.feature = feature;
        this.fulfils = fulfils;
        this.values = values;
    }

    //-----------------------------------------------------------------------    
    public Object clone(
    ) throws CloneNotSupportedException {
        return super.clone();
    }

    //-----------------------------------------------------------------------    
    /**
     * Retrieve the quantor's <code>enum</code> representation
     * 
     * @return the quantor's <code>enum</code> representation
     */
    public Quantifier quantifier() {
        return this.quantifier;
    }
  
    //-----------------------------------------------------------------------    
    public short getQuantor() {
        return this.quantifier == null ? 0 : this.quantifier.code();
    }
  
    //-----------------------------------------------------------------------    
    public void setQuantor(
        short quantor
    ) {
        this.quantifier = Quantifier.valueOf(quantor);
    }

    //-----------------------------------------------------------------------    
    public String getFeature() {
        return this.feature;
    }

    //-----------------------------------------------------------------------    
    public void setFeature(
        String feature
    ) {
        this.feature = feature;
    }

    //-----------------------------------------------------------------------    
    public boolean isFulfil() {
        return this.fulfils;
    }

    //-----------------------------------------------------------------------    
    public void setFulfil(
        boolean fulfil
    ) {
        this.fulfils = fulfil;
    }

    //-----------------------------------------------------------------------    
    public Object[] getValue(
    ) {
        return this.values;
    }

    //-----------------------------------------------------------------------    
    public Object getValue(
        int index
    ) {
        return this.values[index];
    }

    //-----------------------------------------------------------------------    
    public void setValue(
        int index,
        Object value
    ) {
        this.values[index] = value;
    }

    //-----------------------------------------------------------------------    
    public void setValue(
        Object... values
    ) {
        this.values = values;
    }

    //-----------------------------------------------------------------------    
    public abstract String getName();

    //-----------------------------------------------------------------------    
    public String toString (
    ) {
        try {
            return Records.getRecordFactory().asMappedRecord(
                getClass().getName(), 
                (quantifier == null ? "" : quantifier.toString() + ' ' + feature + ' ') + getName() + ' ' + Arrays.asList(values),
                TO_STRING_FIELDS, 
                new Object[]{
                    quantifier,
                    feature, 
                    getName(), 
                    Arrays.asList(values)
                }
            ).toString();
        } catch (ResourceException exception) {
            return super.toString();
        }
    }
  
    @Override
    public boolean equals(
        Object obj
    ) {
        if(obj instanceof Condition) {
            Condition that = (Condition)obj;
            boolean isEqual =
                this.isFulfil() == that.isFulfil() &&
                this.getFeature().equals(that.getFeature()) &&
                this.getName().equals(that.getName()) &&
                Arrays.asList(this.getValue()).equals(Arrays.asList(that.getValue()));
            return isEqual;
                
        }
        else {
            return false;
        }
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------

    protected final static Object[] EMPTY_OBJECT_ARRAY = new Object[]{};
    
    private Quantifier quantifier;
    private String feature;
    private boolean fulfils;
    protected Object[] values;
    
    private static final String[] TO_STRING_FIELDS = {
        "quantor",
        "feature",
        "operator",
        "values"
    };
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -3115618018740431736L;

}
