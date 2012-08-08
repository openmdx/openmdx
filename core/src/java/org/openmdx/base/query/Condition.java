/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Condition.java,v 1.8 2008/09/09 12:06:39 hburger Exp $
 * Description: Condition
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/09 12:06:39 $
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
import org.openmdx.compatibility.base.query.Quantors;

public abstract class Condition
    implements Serializable, Cloneable {

    //-----------------------------------------------------------------------    
    protected Condition(
        Object... values
    ) {
        this((short)-1, null, false, values);
    }
  
    //-----------------------------------------------------------------------    
    protected Condition(
        short quantor,
        String feature,
        boolean fulfils,
        Object... values
    ) {
        this.quantor = quantor;
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
    public short getQuantor() {
        return this.quantor;
    }
  
    //-----------------------------------------------------------------------    
    public void setQuantor(
        short quantor
    ) {
        this.quantor = quantor;
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
        Object[] values
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
                Quantors.toString(quantor) + ' ' + feature + ' ' + getName() + ' ' + Arrays.asList(values),
                TO_STRING_FIELDS, 
                new Object[]{
                    Quantors.toString(quantor),
                    feature, 
                    getName(), 
                    Arrays.asList(values)
                }
            ).toString();
        } catch (ResourceException exception) {
            return super.toString();
        }
    }
  
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    protected final static Object[] EMPTY_OBJECT_ARRAY = new Object[]{};
    
    private short quantor;
    private String feature;
    private boolean fulfils;
    protected Object[] values;
    
    private static final String[] TO_STRING_FIELDS = {
        "quantor",
        "feature",
        "operator",
        "values"
    };
    
}
