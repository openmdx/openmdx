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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;

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
    public static List<String> getallSupertype(
        String forClass
    ) throws ServiceException {
        ModelElement modelElement = modelTypeNames.get(forClass);
        if(modelElement == null) {
            return null;
        } else {
            final List allSupertype = modelElement.allSupertypes;
            return allSupertype;
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Get recursive subtypes of specified class.
     * NOTE: jmi compliant naming of method
     *
     * @param forClass is a constant from ModelTypes
     *
     */
    public static List<String> getsubtype(
        String forClass
    ) throws ServiceException {
        ModelElement modelElement = modelTypeNames.get(forClass);
        if(modelElement == null) {
            return null;
        } else {
            final List subtype = modelElement.subtypes;
            return subtype;
        }
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
        ModelElement modelElement = modelTypeNames.get(forClass);
        if(modelElement == null) {
            return null;
        } else {
            final List supertype = modelElement.supertypes;
            return supertype;
        }
    }

    //-------------------------------------------------------------------------
    private static Map<String,ModelElement> modelTypeNames = new HashMap<String,ModelElement>();

    //-------------------------------------------------------------------------
    /**
     * Sets the 'allSupertype' attribute of modelElement. Implicitely sets
     * the 'allSupertype' attribute of all of its direct subtypes.
     */
    private static void setallSupertype(
        ModelElement modelElement
    ){
        // set supertypes of supertypes
        for(
            Iterator i = modelElement.supertypes.iterator();
            i.hasNext();
        ) {
            setallSupertype(
                modelTypeNames.get(i.next())
            );
        }
        // collect allSupertype
        ArrayList allSupertype = new ArrayList();
        for(
            Iterator i = modelElement.supertypes.iterator();
            i.hasNext();
        ) {
            ModelElement supertype = modelTypeNames.get(
                i.next()
            );
            for(
                Iterator j = supertype.allSupertypes.iterator();
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
            modelElement.qualifiedName
        );
        modelElement.allSupertypes.clear();
        modelElement.allSupertypes.addAll(
            allSupertype
        );
    }

    //-------------------------------------------------------------------------
    /** 
     * Sets the 'subtype' attribute of modelElement. Requires that the
     * 'allSupertype' attribute is available.
     */
    private static void setSubtype(
        ModelElement modelElement
    ){
        ArrayList subtypes = new ArrayList();
        ModelElement supertype = null;
        for(
            Iterator<ModelElement> i = modelTypeNames.values().iterator();
            i.hasNext();
        ) {
            supertype = i.next();
            if(
                supertype.allSupertypes.contains(
                    modelElement.qualifiedName
                ) &&
                !subtypes.contains(supertype.qualifiedName)
            ) {
                subtypes.add(
                    supertype.qualifiedName
                );
            }
        } 
        modelElement.subtypes.clear();
        modelElement.subtypes.addAll(
            subtypes
        );
    }

    private static final void addElement(ModelElement modelElement){
        modelTypeNames.put(modelElement.qualifiedName, modelElement);
    }

    private static void addElementWithoutTypeHierarchy(String qualifiedName) {
        addElement(new ModelElement(qualifiedName));
    }
    
    private static void addElementWithTypeHierarchy(String qualifiedName, String... subtypes) {
        addElement(new ModelElement(qualifiedName, subtypes));
    }
        
    private static void addElementsWithoutTypeHierarchy(
    ){
        addElementWithoutTypeHierarchy(ModelAttributes.OPERATION);
        addElementWithoutTypeHierarchy(ModelAttributes.EXCEPTION);
        addElementWithoutTypeHierarchy(ModelAttributes.ATTRIBUTE);
        addElementWithoutTypeHierarchy(ModelAttributes.REFERENCE);
        addElementWithoutTypeHierarchy(ModelAttributes.ASSOCIATION_END);
        addElementWithoutTypeHierarchy(ModelAttributes.PARAMETER);
        addElementWithoutTypeHierarchy(ModelAttributes.STRUCTURE_FIELD);
    }

    private static void addElementsWithTypeHierarchy(
    ){
        addElementWithTypeHierarchy(ModelAttributes.PRIMITIVE_TYPE, ModelAttributes.DATATYPE);
        addElementWithTypeHierarchy(ModelAttributes.ENUMERATION_TYPE, ModelAttributes.DATATYPE);
        addElementWithTypeHierarchy(ModelAttributes.PRIMITIVE_TYPE,ModelAttributes.DATATYPE);
        addElementWithTypeHierarchy(ModelAttributes.COLLECTION_TYPE, ModelAttributes.DATATYPE, ModelAttributes.TYPED_ELEMENT);
        addElementWithTypeHierarchy(ModelAttributes.ALIAS_TYPE, ModelAttributes.DATATYPE, ModelAttributes.TYPED_ELEMENT);
        addElementWithTypeHierarchy(ModelAttributes.DATATYPE, ModelAttributes.CLASSIFIER);
        addElementWithTypeHierarchy(ModelAttributes.ASSOCIATION, ModelAttributes.CLASSIFIER);
        addElementWithTypeHierarchy(ModelAttributes.CLASS, ModelAttributes.CLASSIFIER);
        addElementWithTypeHierarchy(ModelAttributes.CLASSIFIER, ModelAttributes.GENERALIZABLE_ELEMENT);
        addElementWithTypeHierarchy(ModelAttributes.PACKAGE, ModelAttributes.GENERALIZABLE_ELEMENT);
        addElementWithTypeHierarchy(ModelAttributes.GENERALIZABLE_ELEMENT, ModelAttributes.NAMESPACE);
        addElementWithTypeHierarchy(ModelAttributes.BEHAVIOURAL_FEATURE, ModelAttributes.FEATURE, ModelAttributes.NAMESPACE);
        addElementWithTypeHierarchy(ModelAttributes.NAMESPACE, ModelAttributes.ELEMENT);
        addElementWithTypeHierarchy(ModelAttributes.TAG, ModelAttributes.ELEMENT);
        addElementWithTypeHierarchy(ModelAttributes.IMPORT, ModelAttributes.ELEMENT);
        addElementWithTypeHierarchy(ModelAttributes.CONSTRAINT, ModelAttributes.ELEMENT);
        addElementWithTypeHierarchy(ModelAttributes.STRUCTURAL_FEATURE, ModelAttributes.FEATURE, ModelAttributes.TYPED_ELEMENT);
        addElementWithTypeHierarchy(ModelAttributes.FEATURE, ModelAttributes.ELEMENT);
        addElementWithTypeHierarchy(ModelAttributes.CONSTANT, ModelAttributes.TYPED_ELEMENT);
        addElementWithTypeHierarchy(ModelAttributes.TYPED_ELEMENT, ModelAttributes.ELEMENT);
        addElementWithTypeHierarchy(ModelAttributes.ELEMENT);

        // calculate for all model elements allSupertype
        for (Iterator<ModelElement> i =
            modelTypeNames.values().iterator(); i.hasNext();) {
            setallSupertype(i.next());
        }

        // calculate for all model elements the subtype
        for (Iterator<ModelElement> i =
            modelTypeNames.values().iterator(); i.hasNext();) {
            setSubtype(i.next());
        }
    }

    static {
        try {
            addElementsWithTypeHierarchy();
            addElementsWithoutTypeHierarchy();
        } catch(Exception ex) {
            throw new RuntimeServiceException(ex).log();
        }
    }

    private static class ModelElement {
        
        /**
         * Constructor 
         *
         * @param qualifiedName
         */
        ModelElement(String qualifiedName, String...subtypes) {
            this.qualifiedName = qualifiedName;
            this.supertypes.addAll(Arrays.asList(subtypes));
        }
        
        final String qualifiedName;
        final List<String> supertypes = new ArrayList<String>();
        final List<String> allSupertypes = new ArrayList<String>();
        final List<String> subtypes = new ArrayList<String>();
    }
    
}

//--- End of File -----------------------------------------------------------
