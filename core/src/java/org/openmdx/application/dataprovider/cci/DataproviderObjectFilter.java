/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: DataproviderObjectFilter.java,v 1.4 2009/02/19 16:30:58 hburger Exp $
 * Description: Dataprovider Object Filter
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/19 16:30:58 $
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
package org.openmdx.application.dataprovider.cci;

import java.util.Collections;
import java.util.Iterator;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.query.AbstractFilter;
import org.openmdx.base.query.FilterProperty;

/**
 * Dataprovider Object Filter
 */
public class DataproviderObjectFilter extends AbstractFilter {

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
    public DataproviderObjectFilter(
        FilterProperty[] filter
    ){
        super(filter);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.query.AbstractFilter#getValues(java.lang.Object, java.lang.String)
     */
    protected Iterator<?> getValues(
        Object candidate, 
        String attribute
    ){
        try {
            DataproviderObject object = (DataproviderObject)candidate;
            SparseList<?> values = object.getValues(attribute);
            return 
                values != null ? values.populationIterator() :
                SystemAttributes.OBJECT_IDENTITY.equals(attribute) ? Collections.singleton(object.path()).iterator() :
                Collections.EMPTY_LIST.iterator();
        } catch (Exception exception){
            new RuntimeServiceException(exception).log();
            return null;
        }
    }
      
  }
