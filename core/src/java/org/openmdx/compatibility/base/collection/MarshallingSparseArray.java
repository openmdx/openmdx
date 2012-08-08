/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: MarshallingSparseArray.java,v 1.5 2008/10/02 17:32:26 hburger Exp $
 * Description: SPICE Collections: Merging List
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/10/02 17:32:26 $
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
package org.openmdx.compatibility.base.collection;

import java.io.Serializable;
import java.util.SortedMap;

import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.collection.Unmarshalling;
import org.openmdx.compatibility.base.marshalling.Marshaller;

/**
 * A Marshalling Sparse Array
 */
public class MarshallingSparseArray
    extends AbstractSparseArray<Object> 
    implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 3760846753015871288L;

    /**
     * Constructor
     * 
     * @param marshaller
     * @param sparseArray
     * @param unmarshalling 
     */  
    public MarshallingSparseArray(
        org.openmdx.base.persistence.spi.Marshaller marshaller,
        SparseArray<Object> sparseArray, 
        Unmarshalling unmarshalling
    ) {
        this.populationMap = new MarshallingSortedMap(
            marshaller,
            sparseArray.populationMap(),
            unmarshalling
        );
    }

    /**
     * Constructor
     * 
     * @param marshaller
     * @param sparseArray
     */  
    public MarshallingSparseArray(
        org.openmdx.base.persistence.spi.Marshaller marshaller,
        SparseArray<Object> sparseArray
    ) {
        this.populationMap = new MarshallingSortedMap(
            marshaller,
            sparseArray.populationMap()
        );
    }

    /**
     * Constructor
     * 
     * @param marshaller
     * @param sparseArray
     */  
    public MarshallingSparseArray(
        Marshaller marshaller,
        SparseArray<Object> sparseArray
    ) {
        this.populationMap = new MarshallingSortedMap(
            marshaller,
            sparseArray.populationMap()
        );
    }

    /**
     * 
     */
    public SortedMap<Integer,Object> populationMap(
    ) {
        return this.populationMap;
    }
    
    /**
     * @serial
     */    
    protected SortedMap<Integer,Object> populationMap;
  
}
