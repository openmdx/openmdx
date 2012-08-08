/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ModelAwareFilter.java,v 1.8 2011/08/15 15:35:23 hburger Exp $
 * Description: Model Aware Filter
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/08/15 15:35:23 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2011, OMEX AG, Switzerland
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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.rest.spi.AbstractFilter;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.Selector;
import org.openmdx.kernel.exception.BasicException;

/**
 * Model Aware Filter
 */
abstract class ModelAwareFilter 
    extends AbstractFilter
{

    /**
     * Constructor 
     * @param filter
     * 
     * @exception   NullPointerException
     *              if the filter is <code>null</code>
     */
    protected ModelAwareFilter(
        List<Condition> filter
    ){
        super(
            filter == null ? NO_CONDITION : filter.toArray(new Condition[filter.size()])
        );        
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -3121625282542792849L;
    
    /**
     * No condition
     */
    private static final Condition[] NO_CONDITION = {};

    /**
     * Creates a new object filter
     * 
     * @param delegate
     * 
     * @return a new Object filter
     */
    protected abstract Selector newFilter(Filter delegate);
    
    @Override
    protected Iterator<?> getValuesIterator(
        Object candidate, 
        String attribute
    ) throws ServiceException {
        DataObject_1 object = (DataObject_1)candidate;
        String objectClass = object.objGetClass();
        if(SystemAttributes.OBJECT_CLASS.equals(attribute)){
            return Collections.singleton(objectClass).iterator();
        } else {    
            ModelElement_1_0 classifier = object.getClassifier();
            if(SystemAttributes.OBJECT_INSTANCE_OF.equals(attribute)){
                return new InstanceOfIterator(classifier);
            } else if("core".equals(attribute) && isCoreInstance(classifier)){
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
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.AbstractFilter#meetsCondition(java.lang.Object, int)
     */
    @Override
    protected boolean meetsCondition(Object candidate, int conditionIndex, Condition condition) { 
        Object[] values = condition.getValue();
        if(values != null && values.length > 0 && values[0] instanceof Filter) {
            if(values.length > 1) {
                throw BasicException.initHolder( 
                    new IllegalArgumentException(
                        "Unsupported filter cardinality: exactly one filter expected",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            new BasicException.Parameter("filterCount", values.length)
                        )
                    )
                );
            }
            ConditionType type = condition.getType();
            if(type != ConditionType.IS_IN && type != ConditionType.IS_NOT_IN) {
                throw BasicException.initHolder( 
                    new IllegalArgumentException(
                        "Unsupported filter condition type: Only IS_IN and IS_NOT_IN are supported",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            new BasicException.Parameter("type", type)
                        )
                    )
                );
            }
            try {
                DataObject_1 object = (DataObject_1) candidate;
                ModelElement_1_0 classifier = object.getClassifier();
                ModelElement_1_0 featureDef = classifier.getModel().getFeatureDef(classifier, condition.getFeature(), false);
                if(!featureDef.getModel().isReferenceType(featureDef)) {
                    throw BasicException.initHolder( 
                        new IllegalArgumentException(
                            "Filter condition are supported for references only",
                            BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_PARAMETER,
                                new BasicException.Parameter("type", type)
                            )
                        )
                    );
                }
                Quantifier quantifier = condition.getQuantifier();
                Filter filter = (Filter) values[0];
                if(featureDef.getModel().referenceIsStoredAsAttribute(featureDef)) {
                    Selector selector = newFilter(filter);
                    for(
                        Iterator<?> i = getValuesIterator(object, condition.getFeature()); 
                        i.hasNext(); 
                    ){
                        if (selector.accept(i.next()) == (type == ConditionType.IS_IN)) {
                            if(quantifier == Quantifier.THERE_EXISTS) return true;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                    }
                    return quantifier == Quantifier.FOR_ALL;
                } else {
                    boolean emptinessExpected;
                    if(quantifier == Quantifier.THERE_EXISTS && type == ConditionType.IS_IN) {
                        emptinessExpected = false;
                    } else if(quantifier == Quantifier.FOR_ALL && type == ConditionType.IS_NOT_IN) {
                        emptinessExpected = true;
                    } else {
                        throw BasicException.initHolder( 
                            new IllegalArgumentException(
                                "This combination of quantifier and condition type is not yet supported",
                                BasicException.newEmbeddedExceptionStack(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.NOT_IMPLEMENTED,
                                    new BasicException.Parameter("quantifier", quantifier),
                                    new BasicException.Parameter("type", type)
                                )
                            )
                        );
                    }
                    Container_1_0 container = object.objGetContainer(condition.getFeature());
                    return emptinessExpected == container.subMap(filter).isEmpty();
                }
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        } else {
            return super.meetsCondition(candidate, conditionIndex, condition);
        }
    }

    /**
     * Tells whether the classifier represents a core instance
     * 
     * @param classifier
     * 
     * @return <code>true</code> if the class is <code>org::openmdx::base::AspectCapable</code> 
     * but not an <code>org::openmdx:base::Aspect</code>
     * 
     * @throws ServiceException 
     */
    private boolean isCoreInstance(
		ModelElement_1_0 classifier	
    ) throws ServiceException{
        boolean aspectCapable = false;
        boolean aspect = false;
        for(Object superType : classifier.objGetList("allSupertype")) {
           String superTypeName = ((Path) superType).getBase();
           aspect |= "org:openmdx:base:Aspect".equals(superTypeName);
           aspectCapable |= "org:openmdx:base:AspectCapable".equals(superTypeName);
        }
        return aspectCapable & !aspect;
    }
    
    /**
     * InstanceOfIterator
     */
    private class InstanceOfIterator implements Iterator<String> {

        /**
         * Constructor 
         *
         * @param classifier
         * @throws ServiceException 
         */
        InstanceOfIterator(
            ModelElement_1_0 classifier
        ) throws ServiceException{
            this.objectClass = (String) classifier.objGetValue("qualifiedName");
            this.superTypes = classifier.objGetList("allSupertype");
        }

        /**
         * 
         */
        private final String objectClass;

        /**
         * 
         */
        private final Collection<?> superTypes;

        /**
         * Initially <code>null</code> to include the object class
         */
        private Iterator<?> superTypeIterator = null;
        
    //  @Override
        public boolean hasNext() {
            return this.superTypeIterator == null || this.superTypeIterator.hasNext();
        }

    //  @Override
        public String next() {
            if(this.superTypeIterator == null) {
                this.superTypeIterator = this.superTypes.iterator(); 
                return this.objectClass;
            } else {
                return ((Path)this.superTypeIterator.next()).getBase();
            }
        }

    //  @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }

}
