/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DataproviderRequest.java,v 1.7 2009/06/01 15:36:57 wfro Exp $
 * Description: Dataprovider Cursor
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/01 15:36:57 $
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
package org.openmdx.application.dataprovider.cci;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.AttributeSpecifier;
import org.openmdx.base.query.Directions;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.spi.ObjectHolder_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * DataproviderRequest
 */
@SuppressWarnings("unchecked")
public class DataproviderRequest
extends DataproviderContext
implements MappedRecord
{

    /**
     * 
     */
    private static final long serialVersionUID = 3688790250033920310L;

    final static private AttributeSpecifier[] NO_ATTRIBUTE_SPECIFIERS = {};
    final static private FilterProperty[] NO_FILTER_PROPERTIES    = {};

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

    /**
     * Constructor for clone()
     * 
     * @param that
     */
    private DataproviderRequest(
        DataproviderRequest that
    ){
        super(that);
        this.object = that.object;
        this.operation = that.operation;
        this.filter = that.filter;
        this.position = that.position;
        this.size = that.size;
        this.direction = that.direction;
        this.attributeSelector = that.attributeSelector;
        this.attributeSpecifier = that.attributeSpecifier;
    }

    /**
     * Creates DataproviderRequest object setting all supplied parameters.
     * This ensures that all access methods would return non null values and
     * it would also ease the task of marshalling the object.
     */
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
        this(
            null,
            object,
            operation,
            filter,
            position,
            size,
            direction,
            attributeSelector,
            attributeSpecifier
        );

    }

    /**
     * Constructor
     * 
     * @param that
     * @param object
     * @param operation
     * @param filter
     * @param position
     * @param size
     * @param direction
     * @param attributeSelector
     * @param attributeSpecifier
     */
    public DataproviderRequest(
        DataproviderContext that,
        MappedRecord object,
        short operation,
        FilterProperty[] filter,
        int position,
        int size,
        short direction,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier
    ) throws ServiceException {
        super(that);
        try {
            if(ObjectHolder_2Facade.getPath(object) == null) {
                throw new RuntimeServiceException(
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.ASSERTION_FAILURE,
                    "A request's path must not be null"
                );
            }
            this.object = object;
            if(
                operation < DataproviderOperations.min() ||
                operation > DataproviderOperations.max()
            ) {
                throw new RuntimeServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "Illegal value for parameter 'operation'",
                    new BasicException.Parameter("operation", operation)
                );
            }
            this.attributeSpecifier = attributeSpecifier == null ? 
                NO_ATTRIBUTE_SPECIFIERS : 
                attributeSpecifier;
            this.operation = operation;
            this.filter = filter == null ? 
                NO_FILTER_PROPERTIES : 
                filter;
            this.position = position;
            this.size = size;
            this.direction = direction;
            this.attributeSelector = attributeSelector;
            // Check that no name is repeating in the array of AttributeSpecifier
            for(
                int i = 0;
                i < this.attributeSpecifier.length - 1;
                i++
            ){
                for(
                    int j = i + 1;
                    j < this.attributeSpecifier.length;
                    j++
                ){
                    if(
                        this.attributeSpecifier[i].name().equals(this.attributeSpecifier[j].name())
                    ){
                        new RuntimeServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE,
                            "Different attribute specifiers with the same name found",
                            new BasicException.Parameter("name", this.attributeSpecifier[i].name()),
                            new BasicException.Parameter("context", that),
                            new BasicException.Parameter("request", object),
                            new BasicException.Parameter("filter", Arrays.asList(this.filter)),
                            new BasicException.Parameter("attributeSpecifier", Arrays.asList(this.attributeSpecifier))
                        ).log();
                        break;
                    }
                }
            }
        }
        catch (RuntimeServiceException assertionFailure) {
            SysLog.error(
                assertionFailure.getMessage(), 
                assertionFailure.getCause()
            );
            throw assertionFailure;
        }
        catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    //------------------------------------------------------------------------
    // Members
    //------------------------------------------------------------------------

    /**
     * Get the request's path
     *
     * @return      an object of DataproviderObject class
     */
    public Path path(
    ) throws ServiceException {
        try {
            return ObjectHolder_2Facade.getPath(this.object);
        }
        catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Get the object
     *
     * @return      an object of DataproviderObject class;
     */
    public MappedRecord object(
    ){
        return this.object;
    }

    /**
     * Get the operation
     *
     * @return      an operation code;
     */
    public short operation(
    ){
        return this.operation;
    }

    /**
     * Get the filter
     *
     * @return      an array of the current FilterProperties.
     */
    public FilterProperty[] attributeFilter(
    ){
        return this.filter;
    }

    /**
     * Add a filter property to the current set of properties
     *
     * @return array of the current FilterProperties.
     */
    public FilterProperty[] addAttributeFilterProperty(
        FilterProperty filterProperty
    ) {
        FilterProperty[] newFilter = new FilterProperty[filter.length+1];
        System.arraycopy(
            filter, 0,
            newFilter, 0,
            filter.length
        );
        newFilter[filter.length] = filterProperty;
        this.filter = newFilter;
        return newFilter;
    }

    /**
     * Get the start position of the extraction
     *
     * @return      an int value;
     */
    public int position() {
        return this.position; 
    }

    /**
     * Get the maximum size of the extraction
     *
     * @return      an int value;
     */
    public int size(
    ){
        return this.size;
    }

    /**
     * Get the direction of the extraction
     *
     * @return      a short value;
     */
    public short direction(
    ){
        return this.direction;
    }

    /**
     * Get the attribute selector
     *
     * @return      an attribute selector
     */
    public short attributeSelector(
    ){
        return this.attributeSelector;
    }

    /**
     * Get the attribute specifier
     *
     * @return      an array of AttributeSpecifier
     */
    public AttributeSpecifier[] attributeSpecifier(
    ){
        return this.attributeSpecifier;
    }

    /**
     * Get the attribute specifier corresponding to the 
     * name string parameter supplied.
     * This method is used in sorting procedure.
     *
     * @return      an object of AttributeSpecifier or null
     */
    public AttributeSpecifier attributeSpecifier(
        String name
    ){
        AttributeSpecifier result = null;
        for(
                int index = 0;
                index < attributeSpecifier.length;
                index++
        ){
            if(attributeSpecifier[index].name().equals(name)) {
                result = attributeSpecifier[index];
                break;
            }
        }
        return result;
    }

    /**
     * Get the attribute specifiers as Map. The key of
     * the entries are attribute.name(), the value is the
     * attribute itself.
     */
    public Map<String,AttributeSpecifier> attributeSpecifierAsMap() {
        if(this.attributeSpecifierAsMap == null) {
            this.attributeSpecifierAsMap = new HashMap();
            if(attributeSpecifier != null) {
                for(int i = 0; i < this.attributeSpecifier.length; i++) {
                    this.attributeSpecifierAsMap.put(
                        this.attributeSpecifier[i].name(),
                        this.attributeSpecifier[i]
                    );
                }
            }
        }
        return this.attributeSpecifierAsMap;
    }

    //------------------------------------------------------------------------
    // Extends DataproviderContext
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.cci.DataproviderContext#keys()
     */
    protected Collection keys() {
        return KEYS;
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        try {
            return "object".equals(key) ?
                object():
                    "operation".equals(key) ?
                        DataproviderOperations.toString(operation()):
                            "attributeFilter".equals(key) ?
                                Records.getRecordFactory().asIndexedRecord(
                                    "set", 
                                    null, 
                                    attributeFilter()
                                ):
                                    "position".equals(key) ?
                                        new Integer(position) :
                                            "size".equals(key) ?
                                                new Integer(size()) :
                                                    "direction".equals(key) ?
                                                        Directions.toString(direction()) :
                                                            "attributeSelector".equals(key) ?
                                                                AttributeSelectors.toString(attributeSelector()):
                                                                    "attributeSpecifier".equals(key) ?
                                                                        Records.getRecordFactory().asIndexedRecord(
                                                                            "list", 
                                                                            null, 
                                                                            attributeSpecifier()
                                                                        ):
                                                                            super.get(key);
        } catch (ResourceException e) {
            throw new RuntimeServiceException(e);
        }
    }


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException {
        return new DataproviderRequest(this);
    }


    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------

    /**
     * Dataprovider object
     */
    private final MappedRecord object;

    /**
     * Operation
     */
    private final short operation;

    /**
     * Filter
     */
    private FilterProperty[] filter;

    /**
     * The start position of the extraction
     */
    private final int position;

    /**
     * The maximum size of the extraction
     */
    private final int size;

    /**
     * The direction of the extraction
     */
    private final short direction;

    /**
     * Attribute selector
     */
    private final short attributeSelector;

    /**
     * Attribute specifier
     */
    private final AttributeSpecifier[] attributeSpecifier;

    /**
     * Attribute specifier as HashMap
     */
    private transient Map<String,AttributeSpecifier> attributeSpecifierAsMap;

    private static final List<String> KEYS = Collections.unmodifiableList(
        Arrays.asList(
            "object",
            "operation",
            "attributeFilter",
            "position",
            "size",
            "direction",
            "attributeSelector",
            "attributeSpecifier",
            "contexts"
        )
    );

}
