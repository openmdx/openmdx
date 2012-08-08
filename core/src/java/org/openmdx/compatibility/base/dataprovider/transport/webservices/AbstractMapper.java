/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractMapper.java,v 1.10 2004/07/11 19:15:26 hburger Exp $
 * Description: AbstractSoapMapper class
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/07/11 19:15:26 $
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
package org.openmdx.compatibility.base.dataprovider.transport.webservices;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.kernel.exception.BasicException;

/**
 * RequestCollection to SOAP mapping
 * 
 * @author wfro
 */
public abstract class AbstractMapper {
  
  protected Writer writer;
  
  //-------------------------------------------------------------------------
  public abstract void mapProlog (
    String prolog
  ) throws IOException;
  
  //-------------------------------------------------------------------------
  public abstract void mapEpilog (
    String epilog
  ) throws IOException;
  
  //-------------------------------------------------------------------------
  void mapValues(
    List source
  ) throws ServiceException {
    try {
      if (source.isEmpty()) {
        // Ignore
      }
      else if (source.get(0) instanceof String) {
        for(int i = 0; i < source.size(); i++) {
          writer.write("<str>");
          writer.write(Entities.XML.escape(String.valueOf(source.get(i))));
          writer.write("</str>");
        }
      }
      else if (source.get(0) instanceof Path) {
        for(int i = 0; i < source.size(); i++) {
          writer.write("<path>");
          writer.write(((Path)source.get(i)).toUri());
          writer.write("</path>");
        }
      }
      else if(source.get(0) instanceof Number) {
        for(int i = 0; i < source.size(); i++) {
          Number number = (Number)source.get(i);
          if(number instanceof Short) {
            writer.write("<short>");
            writer.write(number.toString());
            writer.write("</short>");
          }
          else if(number instanceof Integer) {
            writer.write("<int>");
            writer.write(number.toString());
            writer.write("</int>");
          }
          else if(number instanceof Long) {
            writer.write("<long>");
            writer.write(number.toString());
            writer.write("</long>");
          }
          else {
            writer.write("<dec>");
            writer.write(number.toString());
            writer.write("</dec>");
          }
        }
      }
      else if(source.get(0) instanceof Boolean) {
        for (int i = 0; i < source.size(); i++) {
          writer.write("<bool>");
          writer.write(String.valueOf(source.get(i)));
          writer.write("</bool>");
        }
      }
      else if(source.get(0) instanceof byte[]) {
        for(int i = 0; i < source.size(); i++) {
          writer.write("<bin>");
          writer.write(Base64.encode((byte[])source.get(i)));
          writer.write("</bin>");
        }
      }
      else {
        throw new ServiceException(
          BasicException.Code.DEFAULT_DOMAIN,
          BasicException.Code.TRANSFORMATION_FAILURE,
          new BasicException.Parameter[] {
            new BasicException.Parameter("class", '[' + source.get(0).getClass().getName() + ", ...]")
          },
          "can not marshal value. Supported are [String, Number, Boolean and byte[]"
        );
      }
    } catch (IOException e) {
      throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.TRANSFORMATION_FAILURE,
        new BasicException.Parameter[] {
          new BasicException.Parameter("class", '[' + source.get(0).getClass().getName() + ", ...]")
        },
        "can not marshal value, Error while marshalling to Writer"
      );
    }
    return;
  }

  //-------------------------------------------------------------------------
  void mapContext(
    String contextName,
    SparseList contextValues
  ) throws ServiceException, IOException {
    writer.write("<Context>");
    List population = contextValues.population();
    int[] indices = new int[population.size()];
    int indicesIndex = 0;
    for(ListIterator j = contextValues.populationIterator();
      j.hasNext();
      j.next()
    ) {
      indices[indicesIndex++] = j.nextIndex();
    }
    writer.write("<name>");
    writer.write(contextName);
    writer.write("</name>");
    writer.write("<Values>");
    
    this.mapValues(population);
    
    writer.write("</Values>");
    writer.write("<Indices>");
    for(int j = 0; j < indices.length; j++) {
      writer.write("<idx>" + indices[j] + "</idx>");
    }
    writer.write("</Indices>");
    writer.write("</Context>");
  }

  //-------------------------------------------------------------------------
  void mapContexts(
    Map context
  ) throws ServiceException, IOException {
    writer.write("<ContextList>");
    for(
      Iterator i = context.keySet().iterator();
      i.hasNext();
    ) {
      String contextName = (String)i.next();
      
      this.mapContext(contextName, (SparseList)context.get(contextName));
    }      
    writer.write("</ContextList>");
  }

  //-------------------------------------------------------------------------
  void mapAttribute(
    String attributeName,
    SparseList attributeValues
  ) throws ServiceException, IOException {
    final List population = attributeValues.population();
    final int[] indices = new int[population.size()];
    int indicesIndex = 0;
    for(ListIterator i = attributeValues.populationIterator();
      i.hasNext();
      i.next()
    ) {
      indices[indicesIndex++] = i.nextIndex();
    }
    writer.write("<Attribute>");
    writer.write("<name>");
    writer.write(attributeName);
    writer.write("</name>");
    writer.write("<Values>");
    
    this.mapValues(population);
    
    writer.write("</Values>");
    writer.write("<Indices>");
    for(int i = 0; i < indices.length; i++) {
      writer.write("<idx>" + indices[i] + "</idx>");
//      target.write(indices[i].toString());
//      target.write("</idx>");
    }
    writer.write("</Indices>");
    writer.write("</Attribute>");
  }

  //-------------------------------------------------------------------------
  void mapDataproviderObject(
    DataproviderObject source
  ) throws ServiceException, IOException {
    writer.write("<DataproviderObject>");
    writer.write("<identity>");
    writer.write(source.path().toUri());
    writer.write("</identity>");
    for(
      Iterator i = source.attributeNames().iterator(); 
      i.hasNext();
    ) {
      String attributeName = (String)i.next();
      
      this.mapAttribute(attributeName, source.values(attributeName));
    }
    if(source.getDigest() != null) {
      writer.write("<digest>");
      writer.write(Base64.encode(source.getDigest()));
      writer.write("</digest>");
    }
    writer.write("</DataproviderObject>");
  }
        
  //-------------------------------------------------------------------------
  void mapDataproviderObjects(
    DataproviderObject[] source
  ) throws ServiceException, IOException {
    for(int i = 0; i < source.length; i++) {
      this.mapDataproviderObject(source[i]);
    }
  }

  //-------------------------------------------------------------------------
  void mapFilterProperty(
    FilterProperty source
  ) throws ServiceException, IOException {
    writer.write("<FilterProperty>");
    writer.write("<quantor>");
    writer.write(Quantors.toString(source.quantor()));
    writer.write("</quantor>");
    writer.write("<name>");
    writer.write(source.name());
    writer.write("</name>");
    writer.write("<operator>");
    writer.write(FilterOperators.toString(source.operator()));
    writer.write("</operator>");
    writer.write("<Values>");
    this.mapValues(source.values());
    writer.write("</Values>");
    writer.write("</FilterProperty>");
  }
        
  //-------------------------------------------------------------------------
  void mapFilterProperties(
    FilterProperty[] source
  ) throws ServiceException, IOException {
//  StringBuilder target = new StringBuilder(512);
    for(int i = 0; i < source.length; i++) {
      this.mapFilterProperty(source[i]);
    }
  }
    
}

//--- End of File -----------------------------------------------------------
