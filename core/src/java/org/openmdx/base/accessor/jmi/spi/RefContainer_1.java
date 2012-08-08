/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefContainer_1.java,v 1.6 2008/02/11 17:23:41 hburger Exp $
 * Description: RefContainer_1 class
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/11 17:23:41 $
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
package org.openmdx.base.accessor.jmi.spi;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jmi.reflect.RefPackage;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefContainer_1_0;
import org.openmdx.base.accessor.jmi.cci.RefFilter_1_0;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.collection.MarshallingSequentialList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.exception.InvalidCardinalityException;
import org.openmdx.base.exception.MarshalException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.compatibility.base.collection.Container;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.exception.StackedException;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;

//---------------------------------------------------------------------------
// RefContainer_1
//---------------------------------------------------------------------------
public class RefContainer_1
  extends AbstractCollection<RefObject_1_0>
  implements Serializable, RefContainer_1_0 {

  /**
   * Implements <code>Serializable</code>
   */
  private static final long serialVersionUID = 1211709415207799992L;

  private Marshaller marshaller;

  private FilterableMap<String,Object_1_0> container;

  //-------------------------------------------------------------------------
  /**
   * Constructor
   *
   * @param   the marshaller to be applied to the elements, filter and order
   *           objects.
   * @param   The delegate contains unmarshalled elements
   */
  @SuppressWarnings("unchecked")
public RefContainer_1(
    Marshaller marshaller,
    FilterableMap<String,Object_1_0> container
  ) {
    this.marshaller = marshaller;
    this.container = container;
  }

  //-------------------------------------------------------------------------
  public FilterableMap<String,Object_1_0> refDelegate(
  ) {
    return this.container;
  }

  //-------------------------------------------------------------------------
  public RefPackage refOutermostPackage(
  ) {
    return (RefPackage) this.marshaller;
  }
  
  //-------------------------------------------------------------------------
  public void refAddValue(
    String qualifier,
    RefObject_1_0 value
  ) {
    try {
      value.refDelegate().objMove(
        this.container,
        qualifier
      );
    }
    catch(ServiceException e) {
      throw new JmiServiceException(e);
    }
  }

  //-------------------------------------------------------------------------
  public Container<RefObject_1_0> subSet(
    Object filter
  ) {
    FilterableMap<String, Object_1_0> delegate;
    if(filter instanceof RefFilter_1_0) {
      Collection<FilterProperty> filterProperties = ((RefFilter_1_0)filter).refGetFilterProperties();
      delegate = this.container.subMap(
        filterProperties.toArray(new FilterProperty[filterProperties.size()])
      );
    } else if(filter instanceof Filter) {
      FilterProperty[] mapped = new FilterProperty[((Filter)filter).getCondition().length];
      for(int i = 0; i < mapped.length; i++) {
        Condition condition = ((Filter)filter).getCondition()[i];
        mapped[i] = new FilterProperty(
          condition.getQuantor(),
          condition.getFeature(),
          (short)FilterOperators.fromString(condition.getName()),
          condition.getValue()
        );
      }
      delegate = this.container.subMap(mapped);
    } else {
      delegate = this.container.subMap(filter);
    }
    return new RefContainer_1(
      this.marshaller,
      delegate
    );
  }

  //-------------------------------------------------------------------------
  public List<RefObject_1_0> toList(
    Object _criteria
  ) {
    Object criteria = _criteria;
    FilterableMap<String, ?> source = this.container;
    if(criteria instanceof RefFilter_1_0) {
      RefFilter_1_0 filter=(RefFilter_1_0)criteria;
      Collection<?> filterProperties = filter.refGetFilterProperties();
      Collection<?> attributeSpecifiers = filter.refGetAttributeSpecifiers();
      source = this.container.subMap(
      filterProperties.toArray(new FilterProperty[filterProperties.size()])
      );
      criteria = attributeSpecifiers.toArray(new AttributeSpecifier[attributeSpecifiers.size()]);
    }
    else if(criteria instanceof Filter) {
      AttributeSpecifier[] mapped = new AttributeSpecifier[((Filter)criteria).getOrderSpecifier().length];
      for(int i = 0; i < mapped.length; i++) {
        OrderSpecifier specifier = ((Filter)criteria).getOrderSpecifier()[i];
        mapped[i] = new AttributeSpecifier(
          specifier.getFeature(),
          0,
          specifier.getOrder()
        );
      }
      criteria = mapped;
    }
    return new MarshallingSequentialList<RefObject_1_0>(this.marshaller, source.values(criteria));
  }

  /* (non-Javadoc)
   * @see java.util.AbstractCollection#iterator()
   */
  public Iterator<RefObject_1_0> iterator() {
    return new MarshallingSet<RefObject_1_0>(
      this.marshaller,
      this.container.values()
    ).iterator();
  }

  /* (non-Javadoc)
   * @see java.util.AbstractCollection#size()
   */
  public int size() {
    return this.container.size();
  }

  /* (non-Javadoc)
   * @see org.openmdx.compatibility.base.collection.Container#get(java.lang.Object)
   */
  public RefObject_1_0 get(Object filter) {
   if(filter instanceof String) {
     try {
        return (RefObject_1_0) this.marshaller.marshal(this.container.get(filter));
     } catch (ServiceException exception){
        if(exception.getExceptionCode() == StackedException.NOT_FOUND) return null;
        throw new MarshalException(exception);
     }
   } else {
      List<RefObject_1_0> selection = toList(filter);
      switch(selection.size()){
        case 0: return null;
        case 1: return selection.get(0);
        default: throw new InvalidCardinalityException("Matching objects: " + selection.size());
      }
    }
  }

    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(RefObject_1_0 o) {
        this.refAddValue(null, o);
        return true;
    }

}

//--- End of File -----------------------------------------------------------
