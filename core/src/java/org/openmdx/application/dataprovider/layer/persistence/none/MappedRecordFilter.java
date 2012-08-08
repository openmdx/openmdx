/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: MappedRecordFilter.java,v 1.8 2010/06/02 13:41:49 hburger Exp $
 * Description: Dataprovider Object Filter
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/02 13:41:49 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;

import org.openmdx.application.dataprovider.cci.FilterProperty;
import org.openmdx.application.dataprovider.spi.AbstractFilter;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.w3c.cci2.SparseArray;

/**
 * Dataprovider Object Filter
 */
public class MappedRecordFilter extends AbstractFilter {

    /**
     * 
     */
    private static final long serialVersionUID = 3256436997697515571L;

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
    public MappedRecordFilter(
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
    ) throws ResourceException, ServiceException {
        MappedRecord object = (MappedRecord)candidate;
        Object_2Facade facade = Object_2Facade.newInstance(object);
        Object values = SystemAttributes.OBJECT_CLASS.equals(attribute) ?
            Collections.singletonList(facade.getObjectClass()) :
                facade.getAttributeValues(attribute);
        return 
            values instanceof SparseArray<?> ? 
                ((SparseArray<?>)values).populationIterator() : 
                    values instanceof List<?> ?
                        ((List<?>)values).listIterator() :
                            SystemAttributes.OBJECT_IDENTITY.equals(attribute) ? 
                                Collections.singleton(facade.getPath()).iterator() :
                                Collections.EMPTY_LIST.iterator();
    }
      
  }
