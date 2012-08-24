/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: ModelUtils
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2011, OMEX AG, Switzerland
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


import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * Provide some helpful static methods
 */
public class ModelHelper {

	/**
	 * Constructor
	 */
    private ModelHelper() {
		// Avoid instantiation
	}

    public static String UNBOUNDED = "0..n";
    
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
        String multiplicity = (String) featureDef.objGetValue("multiplicity");
        if(isReference(featureDef)) {
            ModelElement_1_0 referencedEnd = model.getElement(
                featureDef.objGetValue("referencedEnd")
            );
            if(!referencedEnd.objGetList("qualifierType").isEmpty()) {
                ModelElement_1_0 qualifierType = model.getDereferencedType(
                    referencedEnd.objGetValue("qualifierType")
                );
                return model.isNumericType(qualifierType) ? Multiplicity.LIST : Multiplicity.MAP;
            } else if (UNBOUNDED.equals(multiplicity)) {
                // map aggregation none, multiplicity 0..n, no qualifier to <<set>>
            	return Multiplicity.SET;
            }
        } else if (UNBOUNDED.equals(multiplicity)) {
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
                new BasicException.Parameter("feature", featureDef.objGetValue("qualifiedName")),
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
            return Boolean.TRUE.equals(featureDef.objGetValue("isDerived"));
        } else if(featureDef.isReferenceType()) {
            Model_1_0 model = featureDef.getModel();
            ModelElement_1_0 referencedEnd = model.getElement(featureDef.objGetValue("referencedEnd"));
            ModelElement_1_0 association = model.getElement(referencedEnd.objGetValue("container"));
            return Boolean.TRUE.equals(association.objGetValue("isDerived"));
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
            !((Boolean)feature.objGetValue("isDerived")).booleanValue() && 
            ((Boolean)feature.objGetValue("isChangeable")).booleanValue()
        );
    }

    /**
     * Tells whether the given feature is a reference
     * 
     * @param feature the feature to be inspected
     * 
     * @return <code>true</code> if the given feature is a reference
     * 
     * @throws ServiceException
     */
    public static boolean isReference(
        ModelElement_1_0 feature
    ) throws ServiceException {
    	return 
    		feature.getModel().isReferenceType(feature) || // standard
    		hasFeatures(feature, "exposedEnd", "referencedEnd"); // list of references stored as attribute
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
     * Test whether certain features exists and have non-null values
     * 
     * @param object
     * @param features
     * 
     * @return <code>true</code> if the given features exists and have non-null values
     * @throws ServiceException 
     */
    private static boolean hasFeatures(
		ModelElement_1_0 object,
    	String... features
    ) throws ServiceException {
		Set<String> attributes = object.objDefaultFetchGroup();
		for(String feature : features) {
			if(!attributes.contains(feature) || object.objGetValue(feature) == null) {
				return false;
			}
		}
		return true;
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
		} else if (UNBOUNDED.equals(value)){
			return Multiplicity.LIST;
		} else {
			for(Multiplicity candidate : Multiplicity.values()) {
				if(candidate.toString().equals(value)){
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
            referenceDef.objGetValue(
                exposedEnd ? "exposedEnd" : "referencedEnd"
            )
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
        return getEnd(referenceDef, exposedEnd).objGetValue("aggregation");
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

}
