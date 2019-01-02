/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: $
 * Description: DataObjectFilter 
 * Revision:    $Revision: $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2018, OMEX AG, Switzerland
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

package org.openmdx.base.accessor.rest;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.rest.spi.AbstractFilter;
import org.openmdx.base.accessor.rest.spi.ObjectFilter;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ConditionRecord;
import org.openmdx.base.rest.cci.QueryExtensionRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.kernel.jdo.ReducedJDOHelper;

class DataObjectFilter extends ObjectFilter {

	protected DataObjectFilter(
		DataObjectFilter superFilter,
		QueryFilterRecord filter, 
		boolean extentQuery
	) {
		super(superFilter, filter, extentQuery);
	}

	/**
	 * Implements <code>Serializable</code>
	 */
	private static final long serialVersionUID = 7352255651343841409L;
	
	/* (non-Javadoc)
	 * @see org.openmdx.base.accessor.rest.ObjectFilter#newFilter(org.openmdx.base.rest.cci.QueryFilterRecord)
	 */
	@Override
	protected AbstractFilter newFilter(QueryFilterRecord delegate) {
		return new DataObjectFilter(null, delegate, this.extentQuery);
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.accessor.rest.ObjectFilter#equal(java.lang.Object, java.lang.Object)
	 */
	@Override
	protected boolean equal(Object candidate, Object value) {
        if(candidate instanceof DataObject_1_0) {
        	if(value instanceof UUID) {
        		return value.equals(ReducedJDOHelper.getTransactionalObjectId(candidate));
        	} else if(value instanceof Path) {
        		Path oid = (Path)value;
        		if(oid.isTransactionalObjectId()) {
        			return oid.toTransactionalObjectId().equals(ReducedJDOHelper.getTransactionalObjectId(candidate));
        		} else {
        			return oid.equals(ReducedJDOHelper.getObjectId(candidate));
        		}
        	} else {
        		return false;
        	}
        } else {
			return super.equal(candidate, value);
        }
	}
	
    protected ModelElement_1_0 getClassifier(
		Object object
    ){
		try {
			return ((DataObject_1)object).getClassifier();
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
    }
    
	protected boolean isEmpty(
		Object object,
		final String featureName, 
		QueryFilterRecord filter
	) throws ServiceException {
		return ((DataObject_1)object).objGetContainer(featureName).subMap(filter).isEmpty();
	}

    @Override
    protected Iterator<?> getValuesIterator(
        Object candidate, 
        ConditionRecord condition
    ){
    	try {
            String attribute = condition.getFeature();
            DataObject_1 object = (DataObject_1)candidate;
            String objectClass = object.objGetClass();
            if(SystemAttributes.OBJECT_CLASS.equals(attribute)){
                return Collections.singleton(objectClass).iterator();
            } else {    
                ModelElement_1_0 classifier = getClassifier(candidate);
                if(SystemAttributes.OBJECT_INSTANCE_OF.equals(attribute)){
                    return newInstanceOfIterator(classifier);
                } else if(SystemAttributes.CORE.equals(attribute) && isCoreInstance(classifier)){
                	return Collections.emptySet().iterator();
                } else {
                    ModelElement_1_0 featureDef = classifier.getModel().getFeatureDef(classifier, attribute, false);
                    switch(ModelHelper.getMultiplicity(featureDef)) {
                        case LIST:
                            return object.objGetList(attribute).iterator();
                        case SET:
                            return object.objGetSet(attribute).iterator();
                        case SPARSEARRAY:
                            return object.objGetSparseArray(attribute).values().iterator();
                        default:
                            Object value = object.objGetValue(attribute);
                            return (
                                value == null ? Collections.emptySet() : Collections.singleton(value)
                            ).iterator();
                    }
                }
            }
    	} catch (ServiceException exception) {
    		throw new RuntimeServiceException(exception);
    	}
    }
    
    /**
     * Object filter factory
     * 
     * @param superFilter
     * @param subFilter
     * @param extentQuery 
     * 
     * @return a new object filter
     */
    static DataObjectFilter getInstance (
        DataObjectFilter superFilter,
        QueryFilterRecord subFilter, 
        boolean extentQuery
    ){
        if(subFilter == null) {
            return null;
        }
        List<ConditionRecord> conditions = subFilter.getCondition();
        List<QueryExtensionRecord> extensions = subFilter.getExtension();
        if(
            (conditions == null || conditions.isEmpty()) && // TODO detect idempotent conditions
            (extensions == null || extensions.isEmpty())
        ){
            return null;
        }
        return new DataObjectFilter(
            superFilter,
            subFilter,
            extentQuery
        );
    }

}