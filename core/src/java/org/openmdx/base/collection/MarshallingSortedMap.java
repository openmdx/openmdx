/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: MarshallingSortedMap.java,v 1.12 2008/10/02 17:32:13 hburger Exp $
 * Description: Marshalling Sorted Map
 * Revision:    $Revision: 1.12 $
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

import java.io.Serializable;
import java.util.Comparator;
import java.util.SortedMap;

import org.openmdx.compatibility.base.marshalling.Marshaller;

/**
 * A Marshalling Sorted Map
 */
public class MarshallingSortedMap
  extends MarshallingMap<Integer,Object> 
  implements SortedMap<Integer,Object>, Serializable 
{

    /**
     * Constructor
     * 
     * @param marshaller
     * @param sortedMap
     * @param unmarshalling 
     */    
    public MarshallingSortedMap(
        org.openmdx.base.persistence.spi.Marshaller marshaller,
        SortedMap<Integer,Object> sortedMap, 
        Unmarshalling unmarshalling 
    ) {
        super(
            marshaller, 
            sortedMap, 
            unmarshalling
        );
    }

    /**
     * Constructor
     * 
     * @param marshaller
     * @param sortedMap
     */    
    public MarshallingSortedMap(
        org.openmdx.base.persistence.spi.Marshaller marshaller,
        SortedMap<Integer,Object> sortedMap 
    ) {
        super(marshaller, sortedMap);
    }

    /**
     * Constructor
     * 
     * @param marshaller
     * @param sortedMap
     */    
    public MarshallingSortedMap(
        Marshaller marshaller,
        SortedMap<Integer,Object> sortedMap 
    ) {
        super(marshaller, sortedMap);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.collection.MarshallingMap#getDelegate()
     */
    @Override
    protected SortedMap<Integer, Object> getDelegate() {
        return (SortedMap<Integer, Object>) super.getDelegate();
    }

    /**
     * 
     */  
    public SortedMap<Integer,Object> subMap(
        Integer fromKey, 
        Integer toKey
    ) {
        return new MarshallingSortedMap(
            super.marshaller,
            getDelegate().subMap(fromKey, toKey), Unmarshalling.EAGER
        );  
    }
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3761121648119788080L;

    
    //------------------------------------------------------------------------
    // Implements SortedMap
    //------------------------------------------------------------------------

    /**
     * 
     */  
    public Integer firstKey(
    ) {
        return getDelegate().firstKey();
    }

    /**
     * 
     */  
    public Integer lastKey(
    ) {
        return getDelegate().lastKey();
    }

    /**
     * 
     */  
    public SortedMap<Integer,Object> headMap(
        Integer toKey
    ) {
        return new MarshallingSortedMap(
            super.marshaller,
            getDelegate().headMap(toKey), Unmarshalling.EAGER
        );  
    }

    /**
     * 
     */  
    public SortedMap<Integer,Object> tailMap(
        Integer fromKey
    ) {
        return new MarshallingSortedMap(
            super.marshaller,
            getDelegate().tailMap(fromKey), Unmarshalling.EAGER
        );  
    }
  
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public Comparator comparator(
    ) {
        return getDelegate().comparator();    
    }

}
