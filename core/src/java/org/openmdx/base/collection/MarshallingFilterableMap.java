/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: MarshallingFilterableMap.java,v 1.15 2008/04/21 17:03:59 hburger Exp $
 * Description: Marshalling Filterable Map
 * Revision:    $Revision: 1.15 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/21 17:03:59 $
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

import org.openmdx.compatibility.base.marshalling.Marshaller;

/**
 * A Marshalling Filterable Map
 */
public class MarshallingFilterableMap<K,V,M extends FilterableMap<K,?>>
    extends MarshallingMap<K,V,M>
    implements FilterableMap<K,V>, FetchSize 
{

    /**
     * Constructor 
     * 
     * @param marshaller
     * @param map
     */
    public MarshallingFilterableMap(
        org.openmdx.base.persistence.spi.Marshaller marshaller, 
        M container
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
        M container
    ) {
        super(marshaller, container);
    }

    @SuppressWarnings("unchecked")
    public FilterableMap<K,V> subMap(Object filter) {
        return new MarshallingFilterableMap<K,V,M>(
            super.marshaller,
            (M)getDelegate().subMap(super.marshaller.unmarshal(filter))
        );
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
    
    public List<V> values(Object criteria) {
        return new MarshallingSequentialList<V>(
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
        M delegate = getDelegate();
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
        M delegate = getDelegate();
        if(delegate instanceof FetchSize) {
            ((FetchSize)delegate).setFetchSize(fetchSize);
        }
    }

}
