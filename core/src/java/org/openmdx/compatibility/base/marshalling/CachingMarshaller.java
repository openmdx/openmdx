/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: CachingMarshaller.java,v 1.9 2008/09/03 17:27:36 hburger Exp $
 * Description: Caching Marshaller 
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/03 17:27:36 $
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
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.compatibility.base.marshalling;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openmdx.base.exception.ServiceException;

/**
* This marshaller caches marshalled objects.
* <p>
* The embedded marshaller is called only if there is no marshalled object
* registered under the source's object identity. The marshalled objects are
* registered with weak references.
* <p>
* <strong>Note that this implementation is not synchronized.</strong>
* If multiple threads access a <code>CachingMarshaller</code> instance 
* concurrently, it must be synchronized externally. This is typically
* accomplished by synchronizing on some object that naturally encapsulates the 
* caching marshaller. 
*/
public abstract class CachingMarshaller 
    implements Serializable, CachingMarshaller_1_0
{

    /**
     * Constructs a new, empty caching marshaller with the default initial
     * cache size. 
     */
    protected CachingMarshaller(
    ){
        this(
            false // multithreaded
        );
    }

    /**
     * Constructs a new, empty caching marshaller with the default initial
     * cache size. 
     */
    protected CachingMarshaller(
        boolean multithreaded
    ){
        this.multithreaded = multithreaded;
        initialize();
    }
    
    /**
     * 
     */
    protected final boolean multithreaded;
    
    /**
     * The unmarshalled objects
     */ 
    protected transient Map<Object,Object> mapping;

    /**
     * Clears the cache 
     */
    protected void clear(
    ){
        this.mapping.clear();
    }

    private void initialize(
    ){
        this.mapping = this.multithreaded ? 
            new ConcurrentHashMap<Object,Object>() : 
            new HashMap<Object,Object>();
    }
    
    
    //--------------------------------------------------------------------------
    // Implements Marshaller
    //--------------------------------------------------------------------------

    /**
     * Registers an object unless an object matching the unmarshalled object is
     * already registerd.
     *
     * @param   unmarshalled
     *          the unmarshalled object
     * @param   marshalled
     *          the marshalled object
     *
     * @return  true if no object matching the unmarshalled object is registered
     *          yet
     */
    public boolean cache(
        Object unmarshalled,
        Object marshalled
    ){
        Object oldValue;
        if(this.multithreaded) {
            //
            // Intrinsic Put Of Absent
            //
            oldValue = ((ConcurrentMap<Object,Object>)this.mapping).putIfAbsent(
                unmarshalled, 
                marshalled
            );
        } else {
            //
            // Optimistic Put If Absent
            //
            oldValue = this.mapping.put(
                unmarshalled, 
                marshalled
            );
            if(oldValue != null) {
                //
                // Undo
                //
                this.mapping.put(
                    unmarshalled, 
                    oldValue
                );
            }
        }
        return oldValue == null;
    }

    /**
     * Evicts an object
     *
     * @param   marshalled
     *          the marshalled object
     * *
     * @return  true if the unmarshalled object has been evicted by this call
     */
    public boolean evict(
        Object marshalled
    ){
        return this.mapping.values().remove(marshalled);
    }

    
    //--------------------------------------------------------------------------
    // Implements CachingMarshaller_1_0
    //--------------------------------------------------------------------------

    /**
     * Marshals an object
     *
     * @param     source
     *            The object to be marshalled
     * 
     * @return    The marshalled object;
     *            or null if source is null
     * 
     * @exception       ServiceException MARSHAL_FAILURE
     *                  Object can't be unmarshalled
     */
    public Object marshal (
        Object source
    ) throws ServiceException {
        if(source == null) return null;
        Object target = this.mapping.get(source);
        if(target == null) cache(
            source,
            target = createMarshalledObject(source)
        );
        return target;
    }

    /**
     * Unmarshals an object
     *
     * @param  source   The marshalled object
     * 
     * @return          The unmarshalled object;
     *          or null if source is null
     * 
     * @exception       ServiceException MARSHAL_FAILURE
     *                  Object can't be unmarshalled
     */
    public abstract Object unmarshal (
        Object source
    ) throws ServiceException;

    /**
     * Marshals an object to be cached.
     *
     * @param     source
     *            The object to be marshalled
     * 
     * @return    The marshalled object;
     *            or null if source is null
     * 
     * @exception       ServiceException MARSHAL_FAILURE
     *                  Object can't be marshalled
     */
    protected abstract Object createMarshalledObject (
        Object source
    ) throws ServiceException;


    //--------------------------------------------------------------------------
    // Implements Serializable
    //--------------------------------------------------------------------------

    /**
     * Save the components of the <tt>CachingMarshaller</tt> instance to a 
     * stream (that is, serialize it).
     *
     * @serialData  The cache itself is not serialized, i.e. the deserialized 
     *              members have to register themselves.
     */
    private synchronized void writeObject(
        java.io.ObjectOutputStream stream
    ) throws java.io.IOException {
        stream.defaultWriteObject();
    }

    /**
     * The cache itself has not been serialized, i.e. the deserialized 
     * members have to register themselves.
     */
    private synchronized void readObject(
        java.io.ObjectInputStream stream
    ) throws java.io.IOException, ClassNotFoundException {
        stream.defaultReadObject();
        initialize();
    }

}
