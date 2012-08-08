/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: DataproviderRequest.java,v 1.24 2010/03/31 14:32:43 hburger Exp $
 * Description: DataproviderRequest
 * Revision:    $Revision: 1.24 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/31 14:32:43 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.cci;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;

import org.openmdx.base.collection.Sets;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.AnyTypeCondition;
import org.openmdx.base.query.AttributeSpecifier;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.Directions;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.RestFunction;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.base.text.conversion.JavaBeans;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * DataproviderRequest
 */
public class DataproviderRequest {

    //-----------------------------------------------------------------------
    /**
     * Creates DataproviderRequest object with no filter properties set
     * and using default index values. 
     */
    public DataproviderRequest(
        MappedRecord object,
        short operation,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier
    ) throws ServiceException {
        this(
            object,
            operation,
            NO_FILTER_PROPERTIES,
            0,
            Integer.MAX_VALUE,
            Directions.ASCENDING,
            attributeSelector,
            attributeSpecifier
        );
    }

    //-----------------------------------------------------------------------
    /**
     * Constructor for clone()
     * 
     * @param that
     */
    private DataproviderRequest(
        DataproviderRequest that
    ) {
        this.interactionSpec = that.interactionSpec;
        this.object = that.object;
    }

    //-----------------------------------------------------------------------
    public DataproviderRequest(
        RestInteractionSpec interactionSpec,
        MappedRecord object
    ) {
        this.interactionSpec = interactionSpec;
        this.object = object;
    }

    //-----------------------------------------------------------------------
    public DataproviderRequest(
        MappedRecord object,
        short operation,
        FilterProperty[] filter,
        int position,
        int size,
        short direction,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier
    ) throws ServiceException {
        try {
            // interaction
            switch(operation) {
                case DataproviderOperations.ITERATION_START:
                    this.interactionSpec = interactionSpecs.GET;
                    break;
                case DataproviderOperations.OBJECT_RETRIEVAL:
                    this.interactionSpec = interactionSpecs.GET;
                    break;
                case DataproviderOperations.OBJECT_REMOVAL:
                    this.interactionSpec = interactionSpecs.DELETE;
                    break;
                case DataproviderOperations.OBJECT_OPERATION:
                    this.interactionSpec = interactionSpecs.INVOKE;
                    break;
                case DataproviderOperations.OBJECT_CREATION:
                    this.interactionSpec = interactionSpecs.CREATE;
                    break;
                case DataproviderOperations.OBJECT_REPLACEMENT:
                    this.interactionSpec = interactionSpecs.PUT;
                    break;
                default:
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "Unsupported Operation",
                        new BasicException.Parameter("operation", DataproviderOperations.toString(operation))
                    );
            }
            MappedRecord input;
            String kind = object.getRecordName();
            if(ObjectRecord.NAME.equals(kind) || MessageRecord.NAME.equals(kind)) {
                input = object;
            } else if (QueryRecord.NAME.equals(kind)) {
                Query_2Facade queryFacade = Query_2Facade.newInstance(object);
                // filter
                this.setFilter(
                    queryFacade,
                    new Filter(
                        filter,
                        attributeSpecifier
                    )            
                );
                // direction
                switch(direction) {
                    case Directions.ASCENDING:
                        queryFacade.setPosition(position);
                        break;
                    case Directions.DESCENDING:
                        queryFacade.setPosition(-(1 + position));
                        break;
                }
                // size
                queryFacade.setSize(size);
                // fetch groups
                this.setGroups(
                    queryFacade, 
                    attributeSelector
                );
                input = queryFacade.getDelegate();
            } else {
                SysLog.warning("Unsupported request", kind);
                input = null;
            }
            
            this.object = input;
        }
        catch(ResourceException e) {
            throw new ServiceException(e);
        }
    }

    //-----------------------------------------------------------------------
    private void setGroups(
        Query_2Facade queryFacade,        
        short attributeSelector
    ) throws ResourceException {        
        switch(attributeSelector) {
            case AttributeSelectors.ALL_ATTRIBUTES:
                queryFacade.setGroups(Sets.asSet(Arrays.asList(FetchPlan.ALL)));
                break;
            case AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES:
            case AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES:
            case AttributeSelectors.NO_ATTRIBUTES:
                queryFacade.setGroups(Sets.asSet(Arrays.asList(FetchPlan.DEFAULT)));
                break;
        }                    
    }
    
    //-----------------------------------------------------------------------
    public void setAttributeSelector(
        short attributeSelector
    ) throws ServiceException {
        try {
            this.setGroups(
                Query_2Facade.newInstance(this.object), 
                attributeSelector
            );
        } catch(ResourceException e) {
            throw new ServiceException(e);
        }
    }
    
    //-----------------------------------------------------------------------
    /**
     * Get the request's path
     *
     * @return      an object of DataproviderObject class
     */
    public Path path(
    ) throws ServiceException {
        try {
            if(Query_2Facade.isDelegate(this.object)) {
                return Query_2Facade.newInstance(this.object).getPath();
            }
            else if(Object_2Facade.isDelegate(this.object)) {
                return Object_2Facade.newInstance(this.object).getPath();
            }
            else {
                return (Path)this.object.keySet().iterator().next();
            }
        }
        catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    //-----------------------------------------------------------------------
    public String objectClass(
    ) throws ServiceException {
        try {
            if(Object_2Facade.isDelegate(this.object)) {
                return Object_2Facade.newInstance(this.object).getObjectClass();
            }
            else {
                return ((MappedRecord)this.object.values().iterator().next()).getRecordName();
            }
        }
        catch(Exception e) {
            throw new ServiceException(e);
        }        
    }

    //-----------------------------------------------------------------------
    public RestInteractionSpec getInteractionSpec(
    ) {
        return this.interactionSpec;
    }
    
    //-----------------------------------------------------------------------
    /**
     * Get the object
     *
     * @return      an object of DataproviderObject class;
     */
    public MappedRecord object(
    ) throws ServiceException {
        return this.object;
    }

    //-----------------------------------------------------------------------
    /**
     * Get the operation
     *
     * @return      an operation code;
     */
    public short operation(
    ) throws ServiceException {
        RestFunction function = this.interactionSpec.getFunction();
        switch(function) {
            case GET:
                Path path = this.path();
                return path.size() % 2 == 0 ?
                    DataproviderOperations.ITERATION_START :
                        DataproviderOperations.OBJECT_RETRIEVAL;
            case DELETE:
                return DataproviderOperations.OBJECT_REMOVAL;
            case PUT:
                return DataproviderOperations.OBJECT_REPLACEMENT;
            case POST:
                // Model is not available on bootstrapping.
                // In this case fall back to heuristic decision
                String objectClass = this.objectClass();
                if(!Model_1Factory.isLoaded()) {
                    return "org:openmdx:base:Void".equals(objectClass) || "org:omg:model1:PackageExternalizeParams".equals(objectClass) ?
                        DataproviderOperations.OBJECT_OPERATION : 
                            DataproviderOperations.OBJECT_CREATION;                    
                }
                else {
                    return Model_1Factory.getModel().isStructureType(objectClass) ? 
                        DataproviderOperations.OBJECT_OPERATION : 
                            DataproviderOperations.OBJECT_CREATION;
                }
            default:
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "Can not determine operation from interactionSpec",
                    new BasicException.Parameter("interactionSpec", this.interactionSpec)
                );
        }
    }

    //-----------------------------------------------------------------------
    private Filter getFilter(
        Query_2Facade facade
    ) throws ServiceException {
        Object recordFilter = facade.getQuery();        
        if(recordFilter != null) {
            return (Filter)JavaBeans.fromXML(
                ((CharSequence)recordFilter)
            );
        }
        return null;        
    }
    
    //-----------------------------------------------------------------------
    private void setFilter(
        Query_2Facade facade,
        Filter filter
    ) throws ServiceException {
        facade.setQuery(
            JavaBeans.toXML(filter)
        );
    }

    //-----------------------------------------------------------------------
    private void prepareSpecifiers(
    ) throws ServiceException, ResourceException {
        if(this.attributeFilter == null || this.attributeSpecifier == null) {
            this.attributeFilter = NO_FILTER_PROPERTIES;                    
            this.attributeSpecifier = NO_ATTRIBUTE_SPECIFIERS;
            if(Query_2Facade.isDelegate(this.object)) {
                Filter filter = this.getFilter(
                    Query_2Facade.newInstance(this.object)
                );
                if(filter != null) {        
                    // cachedAttributeFilter
                    List<FilterProperty> filterProperties = Filter.getFilterProperties(filter);
                    this.attributeFilter = filterProperties.isEmpty() ?
                        NO_FILTER_PROPERTIES :
                            filterProperties.toArray(new FilterProperty[filterProperties.size()]);
                    // cachedAttributeSpecifier
                    List<AttributeSpecifier> attributeSpecifiers = new ArrayList<AttributeSpecifier>();
                    for(OrderSpecifier orderSpecifier: filter.getOrderSpecifier()) {
                        attributeSpecifiers.add(
                            new AttributeSpecifier(
                                orderSpecifier.getFeature(),
                                0, // position
                                orderSpecifier.getOrder()
                            )
                        );
                    }
                    this.attributeSpecifier = attributeSpecifiers.toArray(
                        new AttributeSpecifier[attributeSpecifiers.size()]
                    );
                }
            }
        }
    }
    
    //-----------------------------------------------------------------------
    /**
     * Get the filter
     *
     * @return an array of the current FilterProperties.
     */
    public FilterProperty[] attributeFilter(
    ) throws ServiceException {
        try {
            this.prepareSpecifiers();
            return this.attributeFilter;
        }
        catch(ResourceException e) {
            throw new ServiceException(e);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Add a filter property to the current set of properties
     *
     * @return array of the current FilterProperties.
     */
    public void addAttributeFilterProperty(
        FilterProperty filterProperty
    ) throws ServiceException {
        try {
            Query_2Facade facade = Query_2Facade.newInstance(this.object); 
            Filter filter = this.getFilter(facade);
            if(filter == null) {
                filter = new Filter();
            }
            List<Condition> conditions = new ArrayList<Condition>(
                Arrays.asList(filter.getCondition())
            );
            conditions.add(
                new AnyTypeCondition(filterProperty)
            );
            filter.setCondition(
                conditions.toArray(new Condition[conditions.size()])
            );
            this.setFilter(
                facade,
                filter
            );
            // Add filter property to attribute filter
            if(this.attributeFilter != null) {
                FilterProperty[] newAttributeFilter = new FilterProperty[this.attributeFilter.length+1];
                System.arraycopy(this.attributeFilter, 0, newAttributeFilter, 0, this.attributeFilter.length);
                newAttributeFilter[newAttributeFilter.length-1] = filterProperty;
                this.attributeFilter = newAttributeFilter;
            }
        }
        catch(ResourceException e) {
            throw new ServiceException(e);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Remove a filter property from the current set of properties
     *
     * @return removed property if found, null otherwise.
     */
    public void removeAttributeFilterProperty(
        String feature
    ) throws ServiceException {
        try {
            this.attributeFilter = null;
            Query_2Facade facade = Query_2Facade.newInstance(this.object); 
            Filter filter = this.getFilter(facade);
            if(filter == null) return;
            List<Condition> conditions = new ArrayList<Condition>(Arrays.asList(filter.getCondition()));
            for(Iterator<Condition> i = conditions.iterator(); i.hasNext(); ) {
                Condition condition = i.next();
                if(condition.getFeature().equals(feature)) {
                    i.remove();
                }
            }
            filter.setCondition(
                conditions.toArray(new Condition[conditions.size()])
            );
            this.setFilter(
                facade,
                filter
            );
        }
        catch(ResourceException e) {
            throw new ServiceException(e);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Get the start position of the extraction
     *
     * @return      an int value;
     */
    public int position(
    ) throws ServiceException {
        try {
            Number position = Query_2Facade.newInstance(this.object).getPosition();
            if(position == null) {
                return 0;
            } 
            else if(position.intValue() >= 0) {
                return position.intValue();
            } 
            else {
                return -(position.intValue() + 1);
            }            
        }
        catch(ResourceException e) {
            throw new ServiceException(e);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Get the maximum size of the extraction
     *
     * @return      an int value;
     */
    public int size(
    ) {
        try {
            return Query_2Facade.newInstance(this.object).getSize().intValue();
        }
        catch(ResourceException e) {
            throw new RuntimeServiceException(e);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Get the direction of the extraction
     *
     * @return a short value;
     */
    public short direction(
    ) throws ServiceException {
        try {
            Number position = Query_2Facade.newInstance(this.object).getPosition();
            if(position == null) {
                return Directions.ASCENDING;
            } 
            else if(position.intValue() >= 0) {
                return Directions.ASCENDING;
            } 
            else {
                return Directions.DESCENDING;
            }            
        }
        catch(ResourceException e) {
            throw new ServiceException(e);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Get the attribute selector
     *
     * @return      an attribute selector
     */
    public short attributeSelector(
    ) throws ServiceException {
        try {
            if(Query_2Facade.isDelegate(this.object)) {
                Set<?> fetchGroups = Query_2Facade.newInstance(this.object).getGroups();
                return 
                    fetchGroups == null ? AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES :
                    fetchGroups.contains(FetchPlan.ALL) ? AttributeSelectors.ALL_ATTRIBUTES :
                    fetchGroups.contains(FetchPlan.DEFAULT) ? AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES :
                    AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES;
            }
            else {
                return AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES;
            }
        }
        catch(ResourceException e) {
            throw new ServiceException(e);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Get the attribute specifier
     *
     * @return      an array of AttributeSpecifier
     */
    public AttributeSpecifier[] attributeSpecifier(
    ) throws ServiceException {
        try {
            this.prepareSpecifiers();
            return this.attributeSpecifier;
        }
        catch(ResourceException e) {
            throw new ServiceException(e);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Get the attribute specifiers as Map. The key of
     * the entries are attribute.name(), the value is the
     * attribute itself.
     */
    public Map<String,AttributeSpecifier> attributeSpecifierAsMap(
    ) throws ServiceException {
        Map<String,AttributeSpecifier> attributeSpecifierAsMap = new HashMap<String,AttributeSpecifier>();
        AttributeSpecifier[] attributeSpecifier = this.attributeSpecifier();
        if(attributeSpecifier != null) {
            for(int i = 0; i < attributeSpecifier.length; i++) {
                attributeSpecifierAsMap.put(
                    attributeSpecifier[i].name(),
                    attributeSpecifier[i]
                );
            }
        }
        return attributeSpecifierAsMap;
    }

    //------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone(
    ) throws CloneNotSupportedException {
        return new DataproviderRequest(this);
    }

    //------------------------------------------------------------------------
    @Override
    public String toString(
    ) {
        try {
            return Records.getRecordFactory().asMappedRecord(
                DataproviderRequest.class.getName(),
                null,
                TO_STRING_CONTENT,
                new Object[]{
                    this.interactionSpec,
                    this.object,
                }
            ).toString();
        } 
        catch (ResourceException exception) {
            return super.toString();
        }
    }

    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------
    private static final long serialVersionUID = 3688790250033920310L;

    final static private FilterProperty[] NO_FILTER_PROPERTIES = {};
    final static private AttributeSpecifier[] NO_ATTRIBUTE_SPECIFIERS = {};
    
    /**
     * For toString
     */
    private final static String[] TO_STRING_CONTENT = {
        "interactionSpec",
        "object"
    };

    private final RestInteractionSpec interactionSpec;
    private final MappedRecord object;
    private transient FilterProperty[] attributeFilter = null;
    private transient AttributeSpecifier[] attributeSpecifier = null;
    private static final InteractionSpecs interactionSpecs = InteractionSpecs.getRestInteractionSpecs(true);

}
