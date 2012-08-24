/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Dataprovider Object Filter
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.layer.persistence.none;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.resource.cci.MappedRecord;

import org.openmdx.application.dataprovider.cci.FilterProperty;
import org.openmdx.application.dataprovider.spi.AbstractFilter;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.query.Filter;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.w3c.cci2.SparseArray;

/**
 * Dataprovider Object Filter
 */
public abstract class MappedRecordFilter extends AbstractFilter {

    /**
     * Constructor
     * 
     * @param filter
     * 
     * @exception   IllegalArgumentException
     *              in case of an invalid filter property set
     * @exception   NullPointerException
     *              if the filter is <code>null</code>
     */
    protected MappedRecordFilter(
        FilterProperty[] filter
    ){        
        super(filter);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.query.AbstractFilter#getValues(java.lang.Object, java.lang.String)
     */
    @Override
    protected Iterator<?> getValuesIterator(
        Object candidate, 
        String attribute
    ) throws ServiceException {
        MappedRecord object = (MappedRecord)candidate;
        Object_2Facade facade = Facades.asObject(object);
        if(SystemAttributes.OBJECT_CLASS.equals(attribute)) {
            return Collections.singleton(facade.getObjectClass()).iterator();
        } else if (SystemAttributes.OBJECT_IDENTITY.equals(attribute)) {
            return Collections.singleton(facade.getPath()).iterator();
        } else {
            Object values = facade.getAttributeValues(attribute);
            return 
                values instanceof SparseArray<?> ? ((SparseArray<?>)values).populationIterator() : 
                values instanceof List<?> ? ((List<?>)values).listIterator() :
                Collections.EMPTY_LIST.iterator();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.spi.AbstractFilter#equal(java.lang.Object, java.lang.Object)
     */
    @Override
    @SuppressWarnings("synthetic-access")
    protected boolean equal(Object candidate, Object filterValue) {
        if(filterValue instanceof Filter){
            List<FilterProperty> filterProperties = FilterProperty.getFilterProperties((Filter)filterValue);
            MappedRecordFilter mappedRecordFilter = new MappedRecordFilter(
                filterProperties.toArray(new FilterProperty[filterProperties.size()])
            ){
                @Override
                protected Iterator<?> getObjectIterator(
                    Object candidate,
                    String attribute
                ) throws Exception {
                    return MappedRecordFilter.this.getObjectIterator(candidate, attribute);
                }
                @Override
                protected Iterator<?> getValuesIterator(
                    Object candidate,
                    String attribute
                ) throws ServiceException {
                    return MappedRecordFilter.this.getValuesIterator(candidate, attribute);
                }
            };
            return mappedRecordFilter.accept(candidate);
        } else {
            return super.equal(candidate, filterValue);
        }
    }
    
}
