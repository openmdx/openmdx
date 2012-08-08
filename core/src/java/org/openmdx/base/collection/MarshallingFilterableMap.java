/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: MarshallingFilterableMap.java,v 1.16 2008/10/02 17:32:13 hburger Exp $
 * Description: Marshalling Filterable Map
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/10/02 17:32:13 $
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
package org.openmdx.base.collection;

import java.util.List;
import java.util.Map;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.compatibility.base.marshalling.Marshaller;

/**
 * A Marshalling Filterable Map
 */
public class MarshallingFilterableMap
    extends MarshallingMap<String,Object_1_0>
    implements FilterableMap<String,Object_1_0>, FetchSize 
{

    /**
     * Constructor 
     * 
     * @param marshaller
     * @param map
     */
    public MarshallingFilterableMap(
        org.openmdx.base.persistence.spi.Marshaller marshaller, 
        FilterableMap<String,Object_1_0> container
    ) {
        super(marshaller, container);
    }

    /**
     * Constructor 
     * 
     * @param marshaller
     * @param map
     */
    public MarshallingFilterableMap(
        Marshaller marshaller, 
        FilterableMap<String,Object_1_0> container
    ) {
        super(marshaller, container);
    }

    @SuppressWarnings("unchecked")
    public FilterableMap subMap(Object filter) {
        return new MarshallingFilterableMap(
            super.marshaller,
            getDelegate().subMap(super.marshaller.unmarshal(filter))
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.MarshallingMap#getDelegate()
     */
    @Override
    protected FilterableMap<String,Object_1_0> getDelegate() {
        return (FilterableMap<String,Object_1_0>) super.getDelegate();
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3545803160820921399L;

    /**
     * The proposed fetch size
     */
    private int fetchSize = DEFAULT_FETCH_SIZE;

    
    //------------------------------------------------------------------------
    // Implements FilterableMap
    //------------------------------------------------------------------------
    
    public List<Object_1_0> values(Object criteria) {
        return new MarshallingSequentialList<Object_1_0>(
            super.marshaller,
            getDelegate().values(super.marshaller.unmarshal(criteria))
        );
    }
    
    
    //------------------------------------------------------------------------
    // Implements FetchSize
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     */
    public int getFetchSize(
    ) {
        Map<String,Object_1_0> delegate = getDelegate();
        return delegate instanceof FetchSize ?
            this.fetchSize = ((FetchSize)delegate).getFetchSize() :
            this.fetchSize;
    }

    /* (non-Javadoc)
     */
    public void setFetchSize(
        int fetchSize
    ){
        this.fetchSize = fetchSize;
        Map<String,Object_1_0> delegate = getDelegate();
        if(delegate instanceof FetchSize) {
            ((FetchSize)delegate).setFetchSize(fetchSize);
        }
    }

}
