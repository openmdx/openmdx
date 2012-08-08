/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ModelAwareFilter.java,v 1.4 2010/06/30 13:07:18 hburger Exp $
 * Description: Model Aware Filter
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/30 13:07:18 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2010, OMEX AG, Switzerland
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

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.rest.spi.AbstractFilter;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.mof.spi.ModelUtils;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.Condition;

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
            filter == null ? new Condition[]{} : filter.toArray(new Condition[filter.size()])
        );        
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -3121625282542792849L;

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
            Model_1_0 model = Model_1Factory.getModel();
            ModelElement_1_0 classifier = model.getElement(objectClass);
            if(SystemAttributes.OBJECT_INSTANCE_OF.equals(attribute)){
                return new InstanceOfIterator(classifier);
            } else {
                ModelElement_1_0 featureDef = model.getFeatureDef(classifier, attribute, false);
                String multiplicity = featureDef == null ? null : ModelUtils.getMultiplicity(featureDef);
                if(Multiplicities.LIST.equals(multiplicity)) {
                    return object.objGetList(attribute).iterator();
                } else if(Multiplicities.SET.equals(multiplicity)) {
                    return object.objGetSet(attribute).iterator();
                } else if(Multiplicities.SPARSEARRAY.equals(multiplicity)){
                    return object.objGetSparseArray(attribute).values().iterator();
                } else {
                    Object value = object.objGetValue(attribute);
                    return (
                        value == null ? Collections.emptySet() : Collections.singleton(value)
                    ).iterator();
                }
            }
        }
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
            this.objectClass = classifier.jdoGetObjectId().getBase();
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
