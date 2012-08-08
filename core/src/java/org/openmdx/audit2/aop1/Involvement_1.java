/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Involvement_1.java,v 1.6 2010/06/18 12:57:46 hburger Exp $
 * Description: Involvement_1 
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/18 12:57:46 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.audit2.aop1;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;

import org.openmdx.audit2.spi.Configuration;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.mof.spi.ModelUtils;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.SharedObjects;


/**
 * Involvement_1
 */
public class Involvement_1 extends Interceptor_1 {

    /**
     * Constructor 
     *
     * @param self
     * @param next
     */
    protected Involvement_1(
        ObjectView_1_0 self, 
        Interceptor_1 next
    ) {
        super(self, next);
    }

    /**
     * Retrieve the audit configuration
     * 
     * @return the audit configuration
     */
    protected Configuration getConfiguration(
    ){
        return SharedObjects.getPlugInObject(
            self.jdoGetPersistenceManager(), 
            Configuration.class
        );
    }
    
    /**
     * Retrieve the set of modified features
     * 
     * @return the set of modified features
     * 
     * @throws ServiceException
     */
    protected Set<Object> getModifiedFeature(
    ) throws ServiceException{
        ObjectView_1_0 beforeImage = (ObjectView_1_0) self.objGetValue("beforeImage");
        ObjectView_1_0 afterImage = (ObjectView_1_0) self.objGetValue("afterImage");
        if(beforeImage == null || afterImage == null){
            return Collections.emptySet();
        }
        if(SharedObjects.getPlugInObject(self.jdoGetPersistenceManager(), Configuration.class).isModifiedFeaturePersistent()) {
            return super.objGetSet("modifiedFeature");
        }
        ModelElement_1_0 classDef = self.getModel().getElement(beforeImage.objGetClass());
        Map<String,ModelElement_1_0> nonDerivedAttributes = self.getModel().getStructuralFeatureDefs(
            classDef, 
            false, // includeSubtypes
            false, // includeDerived
            true // attributesOnly
        );  
        Set<Object> modifiedFeatures = new HashSet<Object>();
        for(ModelElement_1_0 attribute : nonDerivedAttributes.values()) {
            if(isFeatureModified(attribute, beforeImage, afterImage)){
                modifiedFeatures.add(
                    attribute.objGetValue("qualifiedName")
                );
            }
        }
        return modifiedFeatures;
    }
    
    private static boolean isFeatureModified(
        ModelElement_1_0 feature,
        ObjectView_1_0 beforeImage,
        ObjectView_1_0 afterImage
    ) throws ServiceException {
        String multiplicity = ModelUtils.getMultiplicity(feature);
        String featureName = (String) feature.objGetValue("name");
        Object left;
        Object right;
        if(
            Multiplicities.SINGLE_VALUE.equals(multiplicity) ||
            Multiplicities.OPTIONAL_VALUE.equals(multiplicity)
        ) {
            left = beforeImage.objGetValue(featureName);
            right = afterImage.objGetValue(featureName);
        } else if (
            Multiplicities.LIST.equals(multiplicity)
        ){
            left = beforeImage.objGetList(featureName);
            right = afterImage.objGetList(featureName);
        } else if (
            Multiplicities.SET.equals(multiplicity)
        ){
            left = beforeImage.objGetSet(featureName);
            right = afterImage.objGetSet(featureName);
        } else if (
            Multiplicities.SPARSEARRAY.equals(multiplicity)
        ){
            left = beforeImage.objGetSparseArray(featureName);
            right = afterImage.objGetSparseArray(featureName);
        } else {
            return false; // as we don't know...
        }
        return left == null ? right != null : !left.equals(right);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.DelegatingObject_1#objGetSet(java.lang.String)
     */
    @Override
    public Set<Object> objGetSet(
        String feature
    ) throws ServiceException {
        return "modifiedFeature".equals(feature) ? getModifiedFeature() : super.objGetSet(feature);
    }

    /**
     * Retrieve the object's XRI
     * 
     * @return the object's XRI
     */
    protected Path getObjectPath(
    ){
        return new Path(self.jdoGetObjectId().getBase());
    }

    /**
     * Retrieve the involved object
     * 
     * @return the involved object unless it has been removed in the meanwhile
     */
    protected ObjectView_1_0 getObject(
    ){
        try {
            return (ObjectView_1_0) super.self.jdoGetPersistenceManager().getObjectById(
                getObjectPath()
            );
        } catch (JDOObjectNotFoundException exception) {
            //
            // It has been deleted in the meanwhile
            //
            return null; // that's why the "object" feature is optional
        }
    }

    /**
     * Derive the unit of work's object id
     * 
     * @return the unit of work's object id
     */
    protected Path getUnitOfWorkPath(
    ){
        return self.jdoGetObjectId().getPrefix(7);
    }

    /**
     * Derive the unit of work id
     * 
     * @return the unit of work id
     */
    protected String getUnitOfWorkId(
    ){
        return self.jdoGetObjectId().get(6);
    }

    /**
     * Retrieve the unit of work 
     * 
     * @return the unit of work 
     * 
     * @throws ServiceException
     */
    protected ObjectView_1_0 getUnitOfWork(){
        return (ObjectView_1_0) self.jdoGetPersistenceManager().getObjectById(
            getUnitOfWorkPath()
        );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.DelegatingObject_1#objGetValue(java.lang.String)
     */
    @Override
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        return
            "object".equals(feature) ? getObject() : 
            "objectId".equals(feature) ? getObjectPath().toXRI() :
            "taskId".equals(feature) ? getUnitOfWork().objGetValue("taskId") :
            "unitOfWork".equals(feature) ? getUnitOfWork() :
            "unitOfWorkId".equals(feature) ? getUnitOfWorkPath().getBase() :
            super.objGetValue(feature);
    }
    
}
