/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DelegatingContainer.java,v 1.16 2008/04/21 16:53:28 hburger Exp $
 * Description: Delegating Container
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/21 16:53:28 $
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
package org.openmdx.compatibility.base.dataprovider.transport.delegation;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.collection.FetchSize;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.collection.Container;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.compatibility.base.naming.Path;

/**
 * Delegating Container
 */
@SuppressWarnings("unchecked")
public class DelegatingContainer
    extends AbstractMap  
    implements Serializable, FilterableMap, FetchSize
{

 
    /**
     * 
     */
    private static final long serialVersionUID = 3907208269167474229L;
    /**
     * @serial
     */
    Container container;

    /* (non-Javadoc)
     */
    public DelegatingContainer(
        Container container
    ) {
        this.container = container;
    }

    /* (non-Javadoc)
     */
    public void clear() {
        container.clear();
    }

    /* (non-Javadoc)
     */
    public boolean containsKey(Object key) {
        return container.get(key) != null;
    }

    /* (non-Javadoc)
     */
    public boolean containsValue(Object value) {
        return container.contains(value);
    }

    /* (non-Javadoc)
     */
    public Set entrySet() {
        return new MarshallingSet(
            ContainerMarshaller.getInstance(),
            this.container
        );
    }

    /* (non-Javadoc)
     */
    public Object get(Object key) {
        return container.get(key);
    }

    /* (non-Javadoc)
     */
    public boolean isEmpty() {
        return container.isEmpty();
    }

    /* (non-Javadoc)
     */
    public Object put(Object key, Object value) {
        try {
            ((Object_1)value).objMove(this, (String)key);
            return null;
        } catch (ServiceException e) {
           throw new RuntimeServiceException(e);
        }
    }

    /* (non-Javadoc)
     */
    public Object remove(Object key) {
        Object_1_0 object = (Object_1_0)this.container.get(key);
        if(object != null) try {
            if(object.objIsPersistent()) {
                object.objGetClass(); // validation
                object.objRemove();
            } else {
                object.objMove(null, null);
            }
        } catch (ServiceException exception) {
            return null;
        }
        return object;
    }

    /* (non-Javadoc)
     */
    public int size() {
        return container.size();
    }

    /* (non-Javadoc)
     */
    public FilterableMap subMap(Object filter) {
        return new DelegatingContainer(this.container.subSet(filter));
    }

    /* (non-Javadoc)
     */
    public String toString() {
        return container.toString();
    }

    /* (non-Javadoc)
     */
    public Collection values() {
        return this.container;
    }

    /* (non-Javadoc)
     */
    public List values(Object criteria) {
        return this.container.toList(criteria);
    }

    
    //------------------------------------------------------------------------
    // Implements FetchSize
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     */
    public int getFetchSize(
    ) {
        if(
            this.container instanceof FetchSize
        ) this.fetchSize = ((FetchSize)this.container).getFetchSize();
        return this.fetchSize;
    }

    /* (non-Javadoc)
     */
    public void setFetchSize(
        int fetchSize
    ){
        this.fetchSize = fetchSize;
        if(
            this.container instanceof FetchSize
        ) ((FetchSize)this.container).setFetchSize(fetchSize);
    }

    /**
     * The proposed fetch size
     */
    private int fetchSize = DEFAULT_FETCH_SIZE;

    
    //------------------------------------------------------------------------
    // Class ContainerMarshaller
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     */
    static class ContainerMarshaller
      implements Marshaller
    {

      /* (non-Javadoc)
       */
      public Object marshal(Object source) throws ServiceException {
          return source instanceof Object_1_0 ? 
              new ContainerEntry((Object_1_0) source) :
              source;
      }

      /* (non-Javadoc)
       */
      public Object unmarshal(Object source) throws ServiceException {
          return source instanceof Map.Entry ? 
              ((Map.Entry) source).getValue() :
              source;
      }  

      /* (non-Javadoc)
       */
      static Marshaller getInstance(){
          return instance;
      }
    
      /* (non-Javadoc)
       */
      static Marshaller instance = new ContainerMarshaller();
    
    }

    //------------------------------------------------------------------------
    // Class ContainerEntry
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     */
    static class ContainerEntry 
      implements Map.Entry
    {

      /* (non-Javadoc)
       */
      Object_1_0 value;
    
      /* (non-Javadoc)
       */
      ContainerEntry(
          Object_1_0 value
      ){
          this.value = value;
      }
    
      /* (non-Javadoc)
       * @see java.util.Map.Entry#getKey()
       */
      public Object getKey() {
        try {
            Path path = this.value.objGetPath();
            return path == null ? null : path.getBase();
        } catch (ServiceException e) {
            return null;
        }
      }

      /* (non-Javadoc)
       * @see java.util.Map.Entry#getValue()
       */
      public Object getValue() {
          return this.value;
      }

      /* (non-Javadoc)
       * @see java.util.Map.Entry#setValue(java.lang.Object)
       */
      public Object setValue(Object value) {
          throw new UnsupportedOperationException();
      }
      
    }

}
