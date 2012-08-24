/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: model1 model elements
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.application.mof.repository.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.application.mof.repository.accessor.ModelElement_1;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.spi.Object_2Facade;

@SuppressWarnings({"rawtypes","unchecked"})
public class ModelUtils {

    //-------------------------------------------------------------------------
    /**
     * Get recursive subtypes of specified class.
     * NOTE: jmi compliant naming of method
     *
     * @param forClass is a constant from ModelTypes
     *
     */
    public static List getallSupertype(
        String forClass
    ) throws ServiceException {
        //SysLog.trace("> getallSupertype for " + forClass);
        ModelElement_1_0 modelElement = modelTypeNames.get(forClass);
        //SysLog.trace("modelElement=" + modelElement);
        return modelElement == null ? 
            null : 
            modelElement.objGetList("allSupertype");
    }

    //-------------------------------------------------------------------------
    /**
     * Get recursive subtypes of specified class.
     * NOTE: jmi compliant naming of method
     *
     * @param forClass is a constant from ModelTypes
     *
     */
    public static List getsubtype(
        String forClass
    ) throws ServiceException {
        ModelElement_1_0 modelElement = modelTypeNames.get(forClass);
        return modelElement == null ? 
            null : 
            modelElement.objGetList("subtype");
    }

    //-------------------------------------------------------------------------
    /**
     * Get supertypes of specified class.
     * NOTE: jmi compliant naming of method
     *
     * @param forClass is a constant from ModelTypes
     *
     */
    public static List getsupertype(
        String forClass
    ) throws ServiceException {
        ModelElement_1_0 modelElement = modelTypeNames.get(forClass);
        return modelElement == null ? 
            null : 
            modelElement.objGetList("supertype");
    }

    //-------------------------------------------------------------------------
    private static HashMap<String,ModelElement_1_0> modelTypeNames = new HashMap<String,ModelElement_1_0>();

    //-------------------------------------------------------------------------
    /**
     * Sets the 'allSupertype' attribute of modelElement. Implicitely sets
     * the 'allSupertype' attribute of all of its direct subtypes.
     */
    private static void setallSupertype(
        ModelElement_1_0 modelElement
    ) throws ServiceException {
        // set supertypes of supertypes
        for(
            Iterator i = modelElement.objGetList("supertype").iterator();
            i.hasNext();
        ) {
            setallSupertype(
                modelTypeNames.get(i.next())
            );
        }
        // collect allSupertype
        ArrayList allSupertype = new ArrayList();
        for(
            Iterator i = modelElement.objGetList("supertype").iterator();
            i.hasNext();
        ) {
            ModelElement_1_0 supertype = modelTypeNames.get(
                i.next()
            );
            for(
                Iterator j = supertype.objGetList("allSupertype").iterator();
                j.hasNext();
            ) {
                Object type = null;
                if(!allSupertype.contains(type = j.next())) {
                    allSupertype.add(type);
                }
            }
        }

        // add modelElement itself
        allSupertype.add(
            modelElement.jdoGetObjectId().get(0)
        );
        modelElement.objGetList("allSupertype").clear();
        modelElement.objGetList("allSupertype").addAll(
            allSupertype
        );
    }

    //-------------------------------------------------------------------------
    /** 
     * Sets the 'subtype' attribute of modelElement. Requires that the
     * 'allSupertype' attribute is available.
     */
    private static void setSubtype(
        ModelElement_1_0 modelElement
    ) throws ServiceException {
        ArrayList subtypes = new ArrayList();
        ModelElement_1_0 supertype = null;
        for(
            Iterator i = modelTypeNames.values().iterator();
            i.hasNext();
        ) {
            if(
                (supertype = (ModelElement_1_0)i.next()).objGetList("allSupertype").contains(
                    modelElement.jdoGetObjectId().get(0)
                ) &&
                !subtypes.contains(supertype.jdoGetObjectId().get(0))
            ) {
                subtypes.add(
                    supertype.jdoGetObjectId().get(0)
                );
            }
        } 
        modelElement.objGetList("subtype").clear();
        modelElement.objGetList("subtype").addAll(
            subtypes
        );
    }

    //-------------------------------------------------------------------------
    static {

        Object_2Facade e = null;
        try {
            // ModelPrimitiveType
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.PRIMITIVE_TYPE}),
                ModelAttributes.PRIMITIVE_TYPE
            );        
            e.attributeValuesAsList("supertype").add(ModelAttributes.DATATYPE);
            modelTypeNames.put(
                ModelAttributes.PRIMITIVE_TYPE,
                new ModelElement_1(e.getDelegate(), null)
            );
    
            // ModelEnumerationType
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.ENUMERATION_TYPE}),
                ModelAttributes.ENUMERATION_TYPE
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.DATATYPE);
            modelTypeNames.put(
                ModelAttributes.ENUMERATION_TYPE,
                new ModelElement_1(e.getDelegate(), null)
            );
    
            // ModelStructureType
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.PRIMITIVE_TYPE}),
                ModelAttributes.PRIMITIVE_TYPE
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.DATATYPE);
            modelTypeNames.put(
                ModelAttributes.STRUCTURE_TYPE,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelCollectionType
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.COLLECTION_TYPE}),
                ModelAttributes.COLLECTION_TYPE
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.DATATYPE);
            e.attributeValuesAsList("supertype").add(ModelAttributes.TYPED_ELEMENT);
            modelTypeNames.put(
                ModelAttributes.COLLECTION_TYPE,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelAliasType
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.ALIAS_TYPE}),
                ModelAttributes.ALIAS_TYPE
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.DATATYPE);
            e.attributeValuesAsList("supertype").add(ModelAttributes.TYPED_ELEMENT);
            modelTypeNames.put(
                ModelAttributes.ALIAS_TYPE,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelDatatype
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.DATATYPE}),
                ModelAttributes.DATATYPE
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.CLASSIFIER);
            modelTypeNames.put(
                ModelAttributes.DATATYPE,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelAssociation
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.ASSOCIATION}),
                ModelAttributes.ASSOCIATION
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.CLASSIFIER);
            modelTypeNames.put(
                ModelAttributes.ASSOCIATION,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelClass
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.CLASS}),
                ModelAttributes.CLASS
            );                
            e.attributeValuesAsList(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.CLASS);
            e.attributeValuesAsList("supertype").add(ModelAttributes.CLASSIFIER);
            modelTypeNames.put(
                ModelAttributes.CLASS,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelClassifier
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.CLASSIFIER}),
                ModelAttributes.CLASSIFIER
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.GENERALIZABLE_ELEMENT);
            modelTypeNames.put(
                ModelAttributes.CLASSIFIER,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelPackage
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.PACKAGE}),
                ModelAttributes.PACKAGE
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.GENERALIZABLE_ELEMENT);
            modelTypeNames.put(
                ModelAttributes.PACKAGE,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelGeneralizableElement
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.GENERALIZABLE_ELEMENT}),
                ModelAttributes.GENERALIZABLE_ELEMENT
            );        
            e.attributeValuesAsList("supertype").add(ModelAttributes.NAMESPACE);
            modelTypeNames.put(
                ModelAttributes.GENERALIZABLE_ELEMENT,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelOperation
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.OPERATION}),
                ModelAttributes.OPERATION
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.BEHAVIOURAL_FEATURE);
            modelTypeNames.put(
                ModelAttributes.OPERATION,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelException
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.EXCEPTION}),
                ModelAttributes.EXCEPTION
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.BEHAVIOURAL_FEATURE);
            modelTypeNames.put(
                ModelAttributes.EXCEPTION,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelBehaviouralFeature
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.BEHAVIOURAL_FEATURE}),
                ModelAttributes.BEHAVIOURAL_FEATURE
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.FEATURE);
            e.attributeValuesAsList("supertype").add(ModelAttributes.NAMESPACE);
            modelTypeNames.put(
                ModelAttributes.BEHAVIOURAL_FEATURE,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelNamespace
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.NAMESPACE}),
                ModelAttributes.NAMESPACE
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.ELEMENT);
            modelTypeNames.put(
                ModelAttributes.NAMESPACE,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelTag
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.TAG}),
                ModelAttributes.TAG
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.ELEMENT);
            modelTypeNames.put(
                ModelAttributes.TAG,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelImport
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.IMPORT}),
                ModelAttributes.IMPORT
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.ELEMENT);
            modelTypeNames.put(
                ModelAttributes.IMPORT,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelConstraint
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.CONSTRAINT}),
                ModelAttributes.CONSTRAINT
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.ELEMENT);
            modelTypeNames.put(
                ModelAttributes.CONSTRAINT,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelAttribute
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.ATTRIBUTE}),
                ModelAttributes.ATTRIBUTE
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.STRUCTURAL_FEATURE);
            modelTypeNames.put(
                ModelAttributes.ATTRIBUTE,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelReference
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.REFERENCE}),
                ModelAttributes.REFERENCE
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.STRUCTURAL_FEATURE);
            modelTypeNames.put(
                ModelAttributes.REFERENCE,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelStructuralFeature
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.STRUCTURAL_FEATURE}),
                ModelAttributes.STRUCTURAL_FEATURE
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.FEATURE);
            e.attributeValuesAsList("supertype").add(ModelAttributes.TYPED_ELEMENT);
            modelTypeNames.put(
                ModelAttributes.STRUCTURAL_FEATURE,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelFeature
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.FEATURE}),
                ModelAttributes.FEATURE
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.ELEMENT);
            modelTypeNames.put(
                ModelAttributes.FEATURE,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelAssociationEnd
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.ASSOCIATION_END}),
                ModelAttributes.ASSOCIATION_END
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.TYPED_ELEMENT);
            modelTypeNames.put(
                ModelAttributes.ASSOCIATION_END,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelParameter
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.PARAMETER}),
                ModelAttributes.PARAMETER
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.TYPED_ELEMENT);
            modelTypeNames.put(
                ModelAttributes.PARAMETER,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelConstant
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.CONSTANT}),
                ModelAttributes.CONSTANT
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.TYPED_ELEMENT);
            modelTypeNames.put(
                ModelAttributes.CONSTANT,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelStructureField
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.STRUCTURE_FIELD}),
                ModelAttributes.STRUCTURE_FIELD
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.TYPED_ELEMENT);
            modelTypeNames.put(
                ModelAttributes.STRUCTURE_FIELD,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelTypedElement
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.TYPED_ELEMENT}),
                ModelAttributes.TYPED_ELEMENT
            );                
            e.attributeValuesAsList("supertype").add(ModelAttributes.ELEMENT);
            modelTypeNames.put(
                ModelAttributes.TYPED_ELEMENT,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // ModelElement
            e = Object_2Facade.newInstance(
                new Path(new String[]{ModelAttributes.ELEMENT}),
                ModelAttributes.ELEMENT
            );                
            modelTypeNames.put(
                ModelAttributes.ELEMENT,
                new ModelElement_1(e.getDelegate(), null)            
            );
    
            // calculate for all model elements allSupertype
            for(
                Iterator<ModelElement_1_0> i = modelTypeNames.values().iterator();
                i.hasNext();
            ) { 
                setallSupertype(i.next());
            }
    
            // calculate for all model elements the subtype
            for(
                Iterator<ModelElement_1_0> i = modelTypeNames.values().iterator();
                i.hasNext();
            ) {
                setSubtype(i.next());
            }
        }
        catch(Exception ex) {
            throw new RuntimeServiceException(ex);
        }
    }

}

//--- End of File -----------------------------------------------------------
