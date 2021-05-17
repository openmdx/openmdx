/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: Filters
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.cci.ConditionRecord;
import org.openmdx.base.rest.cci.FeatureOrderRecord;

/**
 * Filters
 *
 */
public class Filters implements Serializable {

	/**
	 * Constructor 
	 *
	 */
	public Filters() {
	}

	/**
	 * Constructor 
	 *
	 * @param forReference
	 * @param filter
	 */
	public Filters(
		String[] forReference,
		Filter[] filter
		) {
		this.setForReference(forReference);
		this.setFilter(filter);
	}

	/**
	 * Get reference names.
	 * 
	 * @return
	 */
	public String[] getForReference(
	) {
		return this.forReference;      
	}

	/**
	 * Get configured reference name at index.
	 * 
	 * @param index
	 * @return
	 */
	public String getForReference(
		int index
	) {
		return this.forReference[index];
	}

	/**
	 * Set reference names.
	 * 
	 * @param forReference
	 */
	public void setForReference(
		String[] forReference
	) {
		this.forReference = forReference;
	}

	/**
	 * Set reference name at index.
	 * 
	 * @param index
	 * @param forReference
	 */
	public void setForReference(
		int index,
		String forReference
	) {
		this.forReference[index] = forReference;
	}

	/**
	 * Get filters.
	 * 
	 * @return
	 */
	public Filter[] getFilter(
	) {
		return this.filter;
	}

	/**
	 * Set filters.
	 * 
	 * @param filter
	 */
	public void setFilter(
		Filter[] filter
	) {
		this.filter = filter;
	}

	/**
	 * Get filter at index.
	 * 
	 * @param index
	 * @return
	 */
	public Filter getFilter(
		int index
	) {
		return this.filter[index];
	}

	/**
	 * Set filter at index.
	 * 
	 * @param index
	 * @param filter
	 */
	public void setFilter(
		int index,
		Filter filter
	) {
		this.filter[index] = filter;
	}

	/**
	 * Set filters defined by filter.
	 * 
	 * @param filter
	 */
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

	/**
	 * Get filter with given name.
	 * 
	 * @param name
	 * @return
	 */
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

	/**
	 * Add and clone filter.
	 * 
	 * @param filter
	 */
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

	/**
	 * Stringify given orders.
	 * 
	 * @param order
	 * @return
	 */
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

	/**
	 * Returns all filters without the base filters (filters with group name = "~").
	 * The conditions of the filter with group name = "~" and name = baseFilterId 
	 * are added to the returned filters.
	 * 
	 * @param baseFilterId
	 * @param defaultFilter
	 * @return
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
				String order = f.getGroupName() + ":" + this.getOrderAsString(f.getOrder()) + ":"  + f.getName();
				// The preparedFilter is a merge of baseFilter and f. 
				// preparedFilter is a clone f of if baseFilter is null.
				// Combine conditions of base filter and f
				List<ConditionRecord> conditions = new ArrayList<ConditionRecord>();
				if(baseFilter != null) {
					conditions.addAll(baseFilter.getCondition());
				}
				conditions.addAll(f.getCondition());
				// Combine order specifiers of base filter and f. Ordering of base filter 
				// has lower priority. Eliminate duplicate order specifiers.
				List<FeatureOrderRecord> orderSpecifiers = new ArrayList<FeatureOrderRecord>();
				Set<String> orderedFeatures = new HashSet<String>();
				for(FeatureOrderRecord orderSpecifier : f.getOrderSpecifier()) {
					if(!orderedFeatures.contains(orderSpecifier.getFeature())) {
						orderSpecifiers.add(orderSpecifier);
						orderedFeatures.add(orderSpecifier.getFeature());
					}
				}
				if(baseFilter != null) {
					for(FeatureOrderRecord orderSpecifier: baseFilter.getOrderSpecifier()) {
						if(!orderedFeatures.contains(orderSpecifier.getFeature())) {
							orderSpecifiers.add(orderSpecifier);
							orderedFeatures.add(orderSpecifier.getFeature());
						}
					}
				}
				// Combine extensions of base filter and f
				List<org.openmdx.base.rest.cci.QueryExtensionRecord> extensions = new ArrayList<org.openmdx.base.rest.cci.QueryExtensionRecord>();
				if(baseFilter != null) {
					if(baseFilter.getExtension() != null) {
						extensions.addAll(baseFilter.getExtension());
					}
				}
				if(!f.getExtension().isEmpty()) {
					extensions.addAll(f.getExtension());
				}
				Filter preparedFilter = new Filter(
					f.getName(),
					f.getLabel(),
					f.getGroupName(),
					f.getIconKey(),
					f.getOrder(),
					conditions,
					orderSpecifiers,
					extensions,
					this.getClass().getName()
				);
				preparedFilters.put(
					order,
					preparedFilter
				);
			}
		}
		return (Filter[])preparedFilters.values().toArray(new Filter[preparedFilters.size()]);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(
	) {
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

	//-------------------------------------------------------------------------
	// Members
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
