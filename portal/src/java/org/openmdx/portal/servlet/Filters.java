/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: Filters.java,v 1.14 2009/03/08 18:03:18 wfro Exp $
 * Description: Filters
 * Revision:    $Revision: 1.14 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/08 18:03:18 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */
package org.openmdx.portal.servlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.resource.ResourceException;

import org.openmdx.base.query.Condition;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.resource.Records;

public class Filters
  implements Serializable {
  
//-------------------------------------------------------------------------
  public Filters(
  ) {
  }
  
  //-------------------------------------------------------------------------
  public Filters(
    String[] forReference,
    Filter[] filter
  ) {
      this.setForReference(forReference);
      this.setFilter(filter);
  }
  
  //-------------------------------------------------------------------------
  public String[] getForReference(
  ) {
      return this.forReference;      
  }
  
  //-------------------------------------------------------------------------
  public String getForReference(
      int index
  ) {
      return this.forReference[index];
  }
  
  //-------------------------------------------------------------------------
  public void setForReference(
      String[] forReference
  ) {
      this.forReference = forReference;
  }
  
  //-------------------------------------------------------------------------
  public void setForReference(
      int index,
      String forReference
  ) {
      this.forReference[index] = forReference;
  }
  
  //-------------------------------------------------------------------------
  public Filter[] getFilter(
  ) {
      return this.filter;
  }
  
  //-------------------------------------------------------------------------
  public void setFilter(
      Filter[] filter
  ) {
      this.filter = filter;
  }
  
  //-------------------------------------------------------------------------
  public Filter getFilter(
      int index
  ) {
      return this.filter[index];
  }
  
  //-------------------------------------------------------------------------
  public void setFilter(
      int index,
      Filter filter
  ) {
      this.filter[index] = filter;
  }

  //-------------------------------------------------------------------------
  public void setFilter(
      Filter filter
  ) {
      for(int i = 0; i < this.filter.length; i++) {
          if(filter.getName().equals(this.filter[i].getName())) {
              this.filter[i] = filter;
              break;
          }
      }
  }

  //-------------------------------------------------------------------------
  public Filter getFilter(
    String name
  ) {
      for(int i = 0; i < this.filter.length; i++) {          
          if((this.filter[i] != null) && (name.equals(this.filter[i].getName()))) {
              return this.filter[i];
          }
      }
      return null;
  }

  //-------------------------------------------------------------------------
  public void addFilter(
    Filter filter
  ) {
      if(this.filter == null) {
          this.filter = new Filter[]{};
      }
      Filter[] newFilter = new Filter[this.filter.length+1];
      System.arraycopy(
          this.filter,
          0,
          newFilter,
          0,
          this.filter.length
      );
      this.filter = newFilter;
      this.filter[this.filter.length-1] = filter;
  }
  
  //-------------------------------------------------------------------------
  private String getOrderAsString(
      Integer[] order
  ) {
      if(order == null) return "0";
      String orderAsString = "";
      for(int i = 0; i < order.length; i++) {
          if(i > 0) orderAsString += ":";
          orderAsString += "" + order[i];
      }
      return orderAsString;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Returns all filters without the base filters (filters with group name = "~").
   * The conditions of the filter with group name = "~" and name = baseFilterId 
   * are added to the returned filters.
   */
  public Filter[] getPreparedFilters(
      String baseFilterId,
      Filter defaultFilter
  ) {
      // Lookup base filter
      Filter baseFilter = null;
      for(int i = 0; i < this.filter.length; i++) {
          Filter f = this.filter[i];
          if((f != null) && "~".equals(f.getGroupName()) && baseFilterId.equals(f.getName())) {
              baseFilter = f;
              break;
          }
      }
      // Prepare filters. Skip base filters and add conditions of base filter to all returned filters.
      Map<String,Filter> preparedFilters = new TreeMap<String,Filter>();
      for(int i = 0; i < this.filter.length; i++) {
          Filter f = filter[i];
          // Replace default filter by supplied default filter
          if((f != null) && DEFAULT_FILTER_NAME.equals(f.getName())) { 
              f = (defaultFilter == null ? f : defaultFilter); 
          }
          if((f != null) && !"~".equals(f.getGroupName())) {
              String order = 
                  f.getGroupName() + ":" + this.getOrderAsString(f.getOrder()) + ":"  + f.getName();              
              Filter preparedFilter = null;
              if(baseFilter != null) {          
                  // Combine conditions of f and baseFilter
                  List<Condition> preparedConditions = new ArrayList<Condition>();
                  preparedConditions.addAll(
                      Arrays.asList(baseFilter.getCondition())
                  );
                  preparedConditions.addAll(
                      Arrays.asList(f.getCondition())
                  );
                  // Combine order specifiers of f and baseFilter. Ordering of base filter 
                  // has lower priority. Eliminate duplicate order specifiers.
                  List<OrderSpecifier> orderSpecifiers = new ArrayList<OrderSpecifier>();
                  Set<String> orderedFeatures = new HashSet<String>();
                  for(int j = 0; j < f.getOrderSpecifier().length; j++) {
                      OrderSpecifier orderSpecifier = f.getOrderSpecifier()[j];
                      if(!orderedFeatures.contains(orderSpecifier.getFeature())) {
                          orderSpecifiers.add(orderSpecifier);
                          orderedFeatures.add(orderSpecifier.getFeature());
                      }
                  }
                  for(int j = 0; j < baseFilter.getOrderSpecifier().length; j++) {
                      OrderSpecifier orderSpecifier = baseFilter.getOrderSpecifier()[j];
                      if(!orderedFeatures.contains(orderSpecifier.getFeature())) {
                          orderSpecifiers.add(orderSpecifier);
                          orderedFeatures.add(orderSpecifier.getFeature());
                      }
                  }
                  preparedFilter = new Filter(
                      f.getName(),
                      f.getLabel(),
                      f.getGroupName(),
                      f.getIconKey(),
                      f.getOrder(),
                      (Condition[])preparedConditions.toArray(new Condition[preparedConditions.size()]),
                      (OrderSpecifier[])orderSpecifiers.toArray(new OrderSpecifier[orderSpecifiers.size()]),
                      new Object[]{this.getClass().getName()}
                  );
              }
              else {
                  preparedFilter = f;
              }
              preparedFilters.put(
                  order,
                  preparedFilter
              );
          }
      }
      return (Filter[])preparedFilters.values().toArray(new Filter[preparedFilters.size()]);
  }
  
  //-------------------------------------------------------------------------
  public String toString(
  ) {
    try {
      return Records.getRecordFactory().asMappedRecord(
        this.getClass().getName(), 
        null,
        TO_STRING_FIELDS,
        new Object[]{
          Records.getRecordFactory().asIndexedRecord(
            String.class.getName(),
            null,
            this.forReference
          ),
          Records.getRecordFactory().asIndexedRecord(
            Filter.class.getName(),
            null,
            this.filter
          )
        }
      ).toString();
    } 
    catch (ResourceException exception) {
        return super.toString();
    }    
  }
  
  //-------------------------------------------------------------------------
  // Variables
  //-------------------------------------------------------------------------
  private static final long serialVersionUID = 3906371506526040119L;

  public static final String DEFAULT_FILTER_NAME = "Default";

  private Filter[] filter = null;
  private String[] forReference = null;
  
  private static final String[] TO_STRING_FIELDS = {
      "forReference",
      "filters"
  };
  
}

//--- End of File -----------------------------------------------------------
