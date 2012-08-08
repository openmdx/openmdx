/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: StandardProxyFactory_2.java,v 1.1 2008/02/19 14:18:47 hburger Exp $
 * Description: StandardMarshaller 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/19 14:18:47 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.base.object.spi;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;

import org.openmdx.base.collection.MarshallingCollection;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.w3c.cci2.SortedMaps;
import org.w3c.cci2.SparseArray;

/**
 * Standard Marshaller
 */
public class StandardProxyFactory_2
    implements ProxyFactory_2_0, Serializable
{

    /**
     * Constructor 
     *
     * @param persistenceManager
     */
    protected StandardProxyFactory_2(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 3990367632906279198L;

    /**
     * TODO shall the persistence manager really be serialized?
     */
    private final PersistenceManager persistenceManager;
    
    /**
     * May be overridden by a subclass to convert the object id.
     * 
     * @param oid
     * 
     * @return the marshalled object id
     */
    public Object marshalObjectId(
        Object oid
    ){
        return oid;
    }

    /**
     * May be overridden by a subclass to convert the object id.
     * 
     * @param oid
     * 
     * @return the unmarshalled object id
     */
    public Object unmarshalObjectId(
        Object oid
    ){
        return oid;
    }

    
    //------------------------------------------------------------------------
    // Implements Marshaller
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.Marshaller#marshal(java.lang.Object)
     */
    public Object marshal(Object source) {
      return 
        source instanceof PersistenceCapable ? PersistenceCapableProxyHandler_2.newProxy(
            this, 
            marshalObjectId(((PersistenceCapable)source).jdoGetObjectId()), 
            source
        ) : // TODO marshal structures
        source instanceof List ? new MarshallingList<Object>(this, (List<?>)source) :
        source instanceof Set ? new MarshallingSet<Object>(this, (Set<?>)source) :
        source instanceof SparseArray ? SortedMaps.asSparseArray(new MarshallingSortedMap<Integer,Object>(this, (SparseArray<?>)source)) :
        source instanceof Collection ? new MarshallingCollection<Object>(this, (Collection<?>)source) :
        source;    
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.Marshaller#unmarshal(java.lang.Object)
     */
    public Object unmarshal(Object source) {
        return source instanceof PersistenceCapable_2_0 ? 
            ((PersistenceCapable_2_0)source).openmdxjdoGetDelegate() :
            source; // TODO unmarshal structures
    }

    
    //------------------------------------------------------------------------
    // Implements PersistenceManagerFactory_2_0
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManagerFactory_2_0#getPersistenceManager()
     */
    public PersistenceManager getPersistenceManager() {
        return this.persistenceManager;
    }

}
