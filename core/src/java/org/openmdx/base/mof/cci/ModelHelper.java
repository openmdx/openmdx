/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: ModelUtils
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2013, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.mof.cci;


import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * Provide some helpful static methods
 */
public class ModelHelper {

	/**
	 * Constructor
	 */
    protected ModelHelper() {
		// Avoid instantiation
	}

    public static String UNBOUND = "0..n";
    
	/**
     * Determine multiplicity of feature. In case of an attribute it is
     * the modeled multiplicity. In case of a reference with a qualifier
     * the multiplicity is <<list>> else the modeled multiplicity.
     * 
     * @param featureDef
     * @return the featur's multiplicity
     * 
     * @throws ServiceException
     */
    public static Multiplicity getMultiplicity(
        ModelElement_1_0 featureDef
    ) throws ServiceException{
        Model_1_0 model = featureDef.getModel();
        String multiplicity = featureDef.getMultiplicity();
        if(featureDef.isReference()) {
            ModelElement_1_0 referencedEnd = model.getElement(
                featureDef.getReferencedEnd()
            );
            if(!referencedEnd.objGetList("qualifierType").isEmpty()) {
                ModelElement_1_0 qualifierType = model.getDereferencedType(
                    referencedEnd.getQualifierType()
                );
                return model.isNumericType(qualifierType) ? Multiplicity.LIST : Multiplicity.MAP;
            } else if (UNBOUND.equals(multiplicity)) {
                // map aggregation none, multiplicity 0..n, no qualifier to <<set>>
            	return Multiplicity.SET;
            }
        } else if (UNBOUND.equals(multiplicity)) {
        	// map 0..n for primitive types to <<list>> (deprecated)
        	return Multiplicity.LIST;
        }
        try {
	        return Multiplicity.parse(multiplicity);
        } catch (IllegalArgumentException exception) {
        	throw new ServiceException(
        		exception,
        		BasicException.Code.DEFAULT_DOMAIN,
        		BasicException.Code.BAD_PARAMETER,
        		"Unable the convert the sterotype into a multiplicity",
                new BasicException.Parameter("feature", featureDef.getQualifiedName()),
        		new BasicException.Parameter("stereotype", multiplicity)
        	);
        }
    }

    /**
     * Tells whether the given feature is derived
     * 
     * @param featureDef
     * 
     * @return <code>true</code> if the given feature is derived
     * 
     * @throws ServiceException
     */
    public static boolean isDerived(
        ModelElement_1_0 featureDef
    ) throws ServiceException {
        if(featureDef.isAttributeType()) {
            return Boolean.TRUE.equals(featureDef.isDerived());
        } else if(featureDef.isReferenceType()) {
            Model_1_0 model = featureDef.getModel();
            ModelElement_1_0 referencedEnd = model.getElement(featureDef.getReferencedEnd());
            ModelElement_1_0 association = model.getElement(referencedEnd.getContainer());
            return Boolean.TRUE.equals(association.isDerived());
        } else {
            return false;
        }
    }
    
    /**
     * Tells whether the given feature is changeable
     * 
     * @param feature the feature to be inspected
     * 
     * @return <code>true</code> if the given feature is changeable
     * 
     * @throws ServiceException
     */
    public static boolean isChangeable(
        ModelElement_1_0 feature
    ) throws ServiceException{
        Model_1_0 model = feature.getModel();
        return model.isReferenceType(feature) ? (
            !model.referenceIsDerived(feature) 
        ) : (
            !Boolean.TRUE.equals(feature.isDerived()) && 
            Boolean.TRUE.equals(feature.isChangeable())
        );
    }

    /**
     * Tells whether the feature is an attribute or a reference stored as attribute
     * 
     * @param feature the feature to be inspected
     * 
     * @return <code>true</code> if he feature is an attribute or a reference stored as attribute
     * 
     * @throws ServiceException 
     */
    public static boolean isStoredAsAttribute(
        ModelElement_1_0 feature
    ) throws ServiceException{
        Model_1_0 model = feature.getModel();
        return model.isAttributeType(feature) || (
            model.isReferenceType(feature) && 
            model.referenceIsStoredAsAttribute(feature)
        );
    }
    
	/**
	 * Parse the multiplicity 
	 * 
	 * @param value the value's String representation, or <code>null</code>
	 * 
	 * @return the corresponding enumeration value, or <code>null</code> if the value does not match any of Multiplicity's <code>String</code> representations
	 */
    public static Multiplicity toMultiplicity(
    	String value
    ){
		if(value == null) {
			return null;
		} else if (UNBOUND.equals(value)){
			return Multiplicity.LIST;
		} else {
			for(Multiplicity candidate : Multiplicity.values()) {
				if(candidate.code().equals(value)){
					return candidate;
				}
			}
		}
		return null;
    }
    
    /**
     * Retrieve the referenced or exposed end
     * 
     * @param referenceDef
     * @param exposedEnd
     * 
     * @return the referenced or exposed end
     * 
     * @throws ServiceException
     */
    private static ModelElement_1_0 getEnd(
        ModelElement_1_0 referenceDef,
        boolean exposedEnd
    ) throws ServiceException {
        return referenceDef.getModel().getElement(
        	exposedEnd ? referenceDef.getExposedEnd() : referenceDef.getReferencedEnd()
        );
    }
    
    /**
     * Retrieve the referenced or exposed end's aggregation
     * 
     * @param referenceDef
     * @param exposedEnd
     * 
     * @return the referenced or exposed end's aggregation
     * 
     * @throws ServiceException
     */
    private static Object getAggregation(
        ModelElement_1_0 referenceDef,
        boolean exposedEnd
    )throws ServiceException {
        return getEnd(referenceDef, exposedEnd).getAggregation();
    }

    /**
     * Tells whether aggregation of association end is AggregationKind.COMPOSITE.
     * 
     * @param referenceDef model element of type Reference.
     * 
     * @param exposedEnd exposed end if true, otherwise referenced end.
     * 
     * @return true if aggregation of association end is AggregationKind.COMPOSITE. 
     */
    public static boolean isCompositeEnd(
        ModelElement_1_0 referenceDef,
        boolean exposedEnd
    ) throws ServiceException {
        return AggregationKind.COMPOSITE.equals(
            getAggregation(referenceDef, exposedEnd)
        );
    }

    /**
     * Tells whether aggregation of association end is AggregationKind.SHARED.
     * 
     * @param referenceDef model element of type Reference.
     * 
     * @param exposedEnd exposed end if true, otherwise referenced end.
     *
     * @return true if aggregation of association end is AggregationKind.SHARED. 
     */
    public static boolean isSharedEnd(
        ModelElement_1_0 referenceDef,
        boolean exposedEnd
    ) throws ServiceException {
        return AggregationKind.SHARED.equals(
            getAggregation(referenceDef, exposedEnd)
        );
    }


    /**
     * Tells whether the classDef refers to an aspect and the given feature is held by its core instance
     * 
     * @param classDef
     * @param featureName
     * 
     * @return <code>true</code> if the given feature is is held by its core instance
     * 
     * @throws ServiceException
     */
    public static boolean isFeatureHeldByCore(
        ModelElement_1_0 classDef, 
        String featureName
    ) throws ServiceException {
        Model_1_0 model = classDef.getModel();
        if (model.isSubtypeOf(classDef, "org:openmdx:base:Aspect")) {
           for (Object superType : classDef.objGetList("allSupertype")) {
              ModelElement_1_0 superClassDef = model.getElement(superType);
              if (model.isSubtypeOf(superClassDef, "org:openmdx:base:AspectCapable")) {
                 if (superClassDef.objGetMap("attribute").containsKey(featureName)) {
                    return true;
                 }
              }
           }
        }
        return false;
     }
    
}
