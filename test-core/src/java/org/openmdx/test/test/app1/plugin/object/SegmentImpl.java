/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: SegmentImpl.java,v 1.10 2005/07/27 19:55:26 hburger Exp $
 * Description: class SegmentImpl
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/07/27 19:55:26 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.test.test.app1.plugin.object;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.collection.FetchSize;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.kernel.exception.BasicException;

//---------------------------------------------------------------------------
public class SegmentImpl
  extends ObjectImpl { 
    
    /**
	 * Implements <code>Serializable</code>
	 */
	private static final long serialVersionUID = -8661820428912070584L;

	//-------------------------------------------------------------------------
    public SegmentImpl(
      Object_1_0 delegation,
      ObjectFactory_1_0 objectFactory,
      Marshaller marshaller
    ) {
      super(
        delegation,
        objectFactory,
        marshaller
      );
    }
    
    //-------------------------------------------------------------------------
    public FilterableMap objGetContainer(
      String feature
    ) throws ServiceException {
      if("foreignPerson".equals(feature)) {
        return new SegmentReferencesForeignPerson(
          objGetContainer("person")
        );
      }
      else {
        return super.objGetContainer(
          feature
        );
      }
    }

    //---------------------------------------------------------------------------
    // SegmentReferencesForeignPerson_PersonImpl
    //---------------------------------------------------------------------------
    
    //---------------------------------------------------------------------------
    class SegmentReferencesForeignPerson
      extends AbstractMap
      implements FilterableMap, FetchSize
    {

      FilterableMap person;
      
      private transient Set entries = null;
            
      SegmentReferencesForeignPerson(
          FilterableMap person
      ) {
        this.person = person;
      }
      
      /* (non-Javadoc)
       * @see java.util.AbstractMap#entrySet()
       */
      public Set entrySet(
      ) {
          if(entries == null) entries = new MarshallingSet(
            ForeignPersonMarshaller.getInstance(),
            this.person.values()
          );
          return entries;
      }
       
      public FilterableMap subMap(
        Object filter
      ) {
        return new SegmentReferencesForeignPerson(
          this.person.subMap(filter)
        );
      }
    
      public Object get(
        Object filter
      ) {
        try {
          String foreignId = (String)filter;
          FilterProperty[] personFilter = new FilterProperty[]{
            new FilterProperty(
              Quantors.THERE_EXISTS,
              "foreignId",
              FilterOperators.IS_IN,
              new String[]{foreignId}
            ) 
          };
          Collection persons = SegmentImpl.this.objGetContainer("person").subMap(personFilter).values();
          if(persons.size() == 0) {
            throw new ServiceException (
              BasicException.Code.DEFAULT_DOMAIN, 
              BasicException.Code.NOT_FOUND, 
              new BasicException.Parameter[] {  
                new BasicException.Parameter("foreignId", foreignId)
              },
              "person with foreignId not found"
            );
          }
          else if(persons.size() == 1) {
            return persons.iterator().next();
          }
          else {
            throw new ServiceException (
              BasicException.Code.DEFAULT_DOMAIN, 
              BasicException.Code.ASSERTION_FAILURE, 
              new BasicException.Parameter [] {
                new BasicException.Parameter("foreignId", foreignId)
              },
              "more than one person with foreignId. foreignId must be uniquely qualifying"
            );
          }
        }
        catch(ServiceException e) {
          throw new RuntimeServiceException(e);
        }
      }
       
      public int size(
      ) { 
        return this.person.size();
      }

      /* (non-Javadoc)
       */
      public List values(Object criteria) {
          return this.person.values(criteria);
      }

      /* (non-Javadoc)
       */
      public int getFetchSize() {
          return fetchSize;
      }

      /* (non-Javadoc)
       */
      public void setFetchSize(int fetchSize) {
          this.fetchSize = fetchSize;
      }
      
      /**
       * 
       */
      private int fetchSize = SEGMENT_REFERENCES_FOREIGN_PERSON_FETCH_SIZE;

    }
    
    static class ForeignPersonMarshaller 
      implements Marshaller
    {

          
        /* (non-Javadoc)
         */
        public Object marshal(Object source) throws ServiceException {
          return source instanceof Object_1_0 ?
            new ForeignPersonEntry((Object_1_0)source) :
            source;
        }

        /* (non-Javadoc)
         */
        public Object unmarshal(Object source) throws ServiceException {
            return source instanceof Map.Entry ?
               ((Map.Entry)source).getValue() :
               source;
        }
         
        static Marshaller getInstance(
        ){
            return ForeignPersonMarshaller.instance;
        }
        
        private static final Marshaller instance = new ForeignPersonMarshaller();
         
    }
      
    static class ForeignPersonEntry implements Map.Entry {

        Object_1_0 value;
          
        ForeignPersonEntry(
            Object_1_0 value
        ){
            this.value = value;
        }
          
        /* (non-Javadoc)
         * @see java.util.Map.Entry#getKey()
         */
        public Object getKey() {
            try {
              return this.value.objGetValue("foreignId");
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

    /**
     * 
     */
    final static int SEGMENT_REFERENCES_FOREIGN_PERSON_FETCH_SIZE = 3;
    
  }

//  --- End of File ------------------------------------------------------------
