/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ModelUtils.java,v 1.1 2009/01/13 02:10:46 wfro Exp $
 * Description: model1 model elements
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/13 02:10:46 $
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
package org.openmdx.application.mof.repository.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.base.naming.Path;

@SuppressWarnings("unchecked")
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
    ) {
        //SysLog.trace("> getallSupertype for " + forClass);
        DataproviderObject modelElement = (DataproviderObject)modelTypeNames.get(forClass);
        //SysLog.trace("modelElement=" + modelElement);
        return modelElement == null 
        ? null
            : modelElement.values("allSupertype");
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
    ) {
        DataproviderObject modelElement = (DataproviderObject)modelTypeNames.get(forClass);
        return modelElement == null 
        ? null
            : modelElement.values("subtype");
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
    ) {
        DataproviderObject modelElement = (DataproviderObject)modelTypeNames.get(forClass);
        return modelElement == null 
        ? null
            : modelElement.values("supertype");
    }

    //-------------------------------------------------------------------------
    private static HashMap modelTypeNames = new HashMap();

    //-------------------------------------------------------------------------
    /**
     * Sets the 'allSupertype' attribute of modelElement. Implicitely sets
     * the 'allSupertype' attribute of all of its direct subtypes.
     */
    private static void setallSupertype(
        DataproviderObject modelElement
    ) {

        //SysLog.trace("> setallSupertype for " + modelElement.path());

        // set supertypes of supertypes
        for(
                Iterator i = modelElement.values("supertype").iterator();
                i.hasNext();
        ) {
            setallSupertype(
                (DataproviderObject)modelTypeNames.get(i.next())
            );
        }

        // collect allSupertype
        ArrayList allSupertype = new ArrayList();
        for(
                Iterator i = modelElement.values("supertype").iterator();
                i.hasNext();
        ) {
            DataproviderObject supertype = (DataproviderObject)modelTypeNames.get(
                i.next()
            );
            for(
                    Iterator j = supertype.values("allSupertype").iterator();
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
            modelElement.path().get(0)
        );

        modelElement.clearValues("allSupertype").addAll(
            allSupertype
        );

        //SysLog.trace("< setallSupertype");
    }

    //-------------------------------------------------------------------------
    /** 
     * Sets the 'subtype' attribute of modelElement. Requires that the
     * 'allSupertype' attribute is available.
     */
    private static void setSubtype(
        DataproviderObject modelElement
    ) {

        //SysLog.trace("> setSubtype for " + modelElement.path());

        ArrayList subtypes = new ArrayList();
        DataproviderObject supertype = null;

        for(
                Iterator i = modelTypeNames.values().iterator();
                i.hasNext();
        ) {
            if(
                    (supertype = (DataproviderObject)i.next()).values("allSupertype").contains(
                        modelElement.path().get(0)
                    ) &&
                    !subtypes.contains(supertype.path().get(0))
            ) {
                subtypes.add(
                    supertype.path().get(0)
                );
            }
        } 
        modelElement.clearValues("subtype").addAll(
            subtypes
        );

        //SysLog.trace("< setSubtype");

    }

    //-------------------------------------------------------------------------
    static {

        DataproviderObject e = null;

        // ModelPrimitiveType
        modelTypeNames.put(
            ModelAttributes.PRIMITIVE_TYPE,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.PRIMITIVE_TYPE})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.PRIMITIVE_TYPE);
        e.values("supertype").add(ModelAttributes.DATATYPE);

        // ModelEnumerationType
        modelTypeNames.put(
            ModelAttributes.ENUMERATION_TYPE,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.ENUMERATION_TYPE})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.ENUMERATION_TYPE);
        e.values("supertype").add(ModelAttributes.DATATYPE);

        // ModelStructureType
        modelTypeNames.put(
            ModelAttributes.STRUCTURE_TYPE,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.STRUCTURE_TYPE})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.STRUCTURE_TYPE);
        e.values("supertype").add(ModelAttributes.DATATYPE);

        // ModelCollectionType
        modelTypeNames.put(
            ModelAttributes.COLLECTION_TYPE,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.COLLECTION_TYPE})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.COLLECTION_TYPE);
        e.values("supertype").add(ModelAttributes.DATATYPE);
        e.values("supertype").add(ModelAttributes.TYPED_ELEMENT);

        // ModelAliasType
        modelTypeNames.put(
            ModelAttributes.ALIAS_TYPE,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.ALIAS_TYPE})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.ALIAS_TYPE);
        e.values("supertype").add(ModelAttributes.DATATYPE);
        e.values("supertype").add(ModelAttributes.TYPED_ELEMENT);

        // ModelDatatype
        modelTypeNames.put(
            ModelAttributes.DATATYPE,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.DATATYPE})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.DATATYPE);
        e.values("supertype").add(ModelAttributes.CLASSIFIER);

        // ModelAssociation
        modelTypeNames.put(
            ModelAttributes.ASSOCIATION,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.ASSOCIATION})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.ASSOCIATION);
        e.values("supertype").add(ModelAttributes.CLASSIFIER);

        // ModelClass
        modelTypeNames.put(
            ModelAttributes.CLASS,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.CLASS})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.CLASS);
        e.values("supertype").add(ModelAttributes.CLASSIFIER);

        // ModelClassifier
        modelTypeNames.put(
            ModelAttributes.CLASSIFIER,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.CLASSIFIER})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.CLASSIFIER);
        e.values("supertype").add(ModelAttributes.GENERALIZABLE_ELEMENT);

        // ModelPackage
        modelTypeNames.put(
            ModelAttributes.PACKAGE,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.PACKAGE})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.PACKAGE);
        e.values("supertype").add(ModelAttributes.GENERALIZABLE_ELEMENT);

        // ModelGeneralizableElement
        modelTypeNames.put(
            ModelAttributes.GENERALIZABLE_ELEMENT,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.GENERALIZABLE_ELEMENT})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.GENERALIZABLE_ELEMENT);
        e.values("supertype").add(ModelAttributes.NAMESPACE);

        // ModelOperation
        modelTypeNames.put(
            ModelAttributes.OPERATION,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.OPERATION})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.OPERATION);
        e.values("supertype").add(ModelAttributes.BEHAVIOURAL_FEATURE);

        // ModelException
        modelTypeNames.put(
            ModelAttributes.EXCEPTION,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.EXCEPTION})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.EXCEPTION);
        e.values("supertype").add(ModelAttributes.BEHAVIOURAL_FEATURE);

        // ModelBehaviouralFeature
        modelTypeNames.put(
            ModelAttributes.BEHAVIOURAL_FEATURE,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.BEHAVIOURAL_FEATURE})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.BEHAVIOURAL_FEATURE);
        e.values("supertype").add(ModelAttributes.FEATURE);
        e.values("supertype").add(ModelAttributes.NAMESPACE);

        // ModelNamespace
        modelTypeNames.put(
            ModelAttributes.NAMESPACE,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.NAMESPACE})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.NAMESPACE);
        e.values("supertype").add(ModelAttributes.ELEMENT);

        // ModelTag
        modelTypeNames.put(
            ModelAttributes.TAG,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.TAG})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.TAG);
        e.values("supertype").add(ModelAttributes.ELEMENT);

        // ModelImport
        modelTypeNames.put(
            ModelAttributes.IMPORT,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.IMPORT})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.IMPORT);
        e.values("supertype").add(ModelAttributes.ELEMENT);

        // ModelConstraint
        modelTypeNames.put(
            ModelAttributes.CONSTRAINT,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.CONSTRAINT})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.CONSTRAINT);
        e.values("supertype").add(ModelAttributes.ELEMENT);

        // ModelAttribute
        modelTypeNames.put(
            ModelAttributes.ATTRIBUTE,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.ATTRIBUTE})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.ATTRIBUTE);
        e.values("supertype").add(ModelAttributes.STRUCTURAL_FEATURE);

        // ModelReference
        modelTypeNames.put(
            ModelAttributes.REFERENCE,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.REFERENCE})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.REFERENCE);
        e.values("supertype").add(ModelAttributes.STRUCTURAL_FEATURE);

        // ModelStructuralFeature
        modelTypeNames.put(
            ModelAttributes.STRUCTURAL_FEATURE,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.STRUCTURAL_FEATURE})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.STRUCTURAL_FEATURE);
        e.values("supertype").add(ModelAttributes.FEATURE);
        e.values("supertype").add(ModelAttributes.TYPED_ELEMENT);

        // ModelFeature
        modelTypeNames.put(
            ModelAttributes.FEATURE,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.FEATURE})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.FEATURE);
        e.values("supertype").add(ModelAttributes.ELEMENT);

        // ModelAssociationEnd
        modelTypeNames.put(
            ModelAttributes.ASSOCIATION_END,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.ASSOCIATION_END})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.ASSOCIATION_END);
        e.values("supertype").add(ModelAttributes.TYPED_ELEMENT);

        // ModelParameter
        modelTypeNames.put(
            ModelAttributes.PARAMETER,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.PARAMETER})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.PARAMETER);
        e.values("supertype").add(ModelAttributes.TYPED_ELEMENT);

        // ModelConstant
        modelTypeNames.put(
            ModelAttributes.CONSTANT,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.CONSTANT})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.CONSTANT);
        e.values("supertype").add(ModelAttributes.TYPED_ELEMENT);

        // ModelStructureField
        modelTypeNames.put(
            ModelAttributes.STRUCTURE_FIELD,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.STRUCTURE_FIELD})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.STRUCTURE_FIELD);
        e.values("supertype").add(ModelAttributes.TYPED_ELEMENT);

        // ModelTypedElement
        modelTypeNames.put(
            ModelAttributes.TYPED_ELEMENT,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.TYPED_ELEMENT})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.TYPED_ELEMENT);
        e.values("supertype").add(ModelAttributes.ELEMENT);

        // ModelElement
        modelTypeNames.put(
            ModelAttributes.ELEMENT,
            e = new DataproviderObject(
                new Path(new String[]{ModelAttributes.ELEMENT})
            )
        );
        e.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.ELEMENT);

        // calculate for all model elements allSupertype
        for(
                Iterator i = modelTypeNames.values().iterator();
                i.hasNext();
        ) { 
            setallSupertype((DataproviderObject)i.next());
        }

        // calculate for all model elements the subtype
        for(
                Iterator i = modelTypeNames.values().iterator();
                i.hasNext();
        ) {
            setSubtype((DataproviderObject)i.next());
        }

    }

}

//--- End of File -----------------------------------------------------------
