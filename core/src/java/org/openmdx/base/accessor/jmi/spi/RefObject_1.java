/*
 * ==================================================================== 
 * Name: $Id: RefObject_1.java,v 1.38 2008/02/08 16:51:25 hburger Exp $ 
 * Description: RefObject_1 class 
 * Revision: $Revision: 1.38 $ 
 * Owner: OMEX AG, Switzerland,
 *        http://www.omex.ch Date: $Date: 2008/02/08 16:51:25 $
 * ====================================================================
 * 
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  * Neither the name of the openMDX team nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by the Apache Software Foundation
 * (http://www.apache.org/).
 */

/**
 * RefObject_1
 */
package org.openmdx.base.accessor.jmi.spi;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.jdo.JDOException;
import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;
import javax.jmi.reflect.JmiException;
import javax.jmi.reflect.RefClass;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefFeatured;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.resource.cci.InteractionSpec;

import org.openmdx.base.accessor.generic.cci.LargeObject_1_0;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_3;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.accessor.generic.spi.ViewObject_1_0;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefClass_1_0;
import org.openmdx.base.accessor.jmi.cci.RefContainer_1_0;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_1;
import org.openmdx.base.accessor.jmi.cci.RefPackageFactory_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackageFactory_1_1;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_1;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_2;
import org.openmdx.base.accessor.jmi.cci.RefStruct_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.exception.MarshalException;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.collection.Container;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.layer.model.State_1_Attributes;
import org.openmdx.compatibility.base.exception.StackedException;
import org.openmdx.compatibility.base.marshalling.CachingMarshaller_1_0;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_2;
import org.openmdx.model1.code.AggregationKind;
import org.openmdx.model1.code.ModelAttributes;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.model1.code.PrimitiveTypes;

// ---------------------------------------------------------------------------
/**
 * Implementation of RefObject_1_0.
 * <p>
 * This implementation supports lightweight serialization. The only member is a
 * handle to the class object.
 */
public abstract class RefObject_1
    implements RefObject_1_2, Serializable, PersistenceCapable
{

    // -------------------------------------------------------------------------
    public RefObject_1(
        Object_1_0 object, 
        RefClass refClass
    ) {
        this.object = extendObject(refClass, object);
        RefPackage refPackage  = refClass.refOutermostPackage();
        if(
            object instanceof ViewObject_1_0 &&
            refPackage instanceof RefPackage_1_2 && 
            refPackage instanceof RefPackageFactory_1_0
        ) {
            InteractionSpec objContext = ((ViewObject_1_0)object).getViewContext();
            Object refContext = ((RefPackage_1_2)refPackage).refViewContext();
            if(objContext == null || objContext.equals(refContext)){
                this.refClass = (RefClass_1_0) refClass;
            } else {
                this.refClass = (RefClass_1_0) (
                   (RefPackageFactory_1_1) refPackage
                ).getRefPackage(
                    objContext
                ).refClass(
                    refClass.refMofId()
                );
            }
        } else {
            this.refClass = (RefClass_1_0) refClass;
        }
    }

    // -------------------------------------------------------------------------
    public RefObject_1(
        RefClass refClass
    ) {
        this.object = newObject(refClass);
        this.refClass = (RefClass_1_0) refClass;
    }

    // -------------------------------------------------------------------------
    final private static Object_1_0 newObject(
        RefClass refClass
    ) {
        try {
            return ((RefPackage_1_0) refClass.refOutermostPackage())
                .refObjectFactory()
                .createObject(refClass.refMofId());
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    // -------------------------------------------------------------------------
    final private static Object_1_0 extendObject(
        RefClass refClass,
        Object_1_0 base
    ) {
        try {
            if (base instanceof CloneableObject_1) {
                CloneableObject_1 wrapper = (CloneableObject_1) base;
                return (
                    (ObjectFactory_1_3) ((RefPackage_1_0) refClass.refOutermostPackage()).refObjectFactory()
                ).cloneObject(
                    wrapper.getIdentity(),
                    (Object_1_0) wrapper.objGetDelegate(),
                    wrapper.isCompleteyDirty()
                );
            } 
            else if (base.objGetClass().equals(refClass.refMofId())) {
                return base;
            } 
            else {
                return ((RefPackage_1_0) refClass.refOutermostPackage())
                    .refObjectFactory()
                    .createObject(refClass.refMofId(), base);
            }
        } 
        catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    // -------------------------------------------------------------------------
    final private void assertStructuralFeature(
        ModelElement_1_0 elementDef
    ) throws ServiceException {
        if (!this.getModel().isStructuralFeatureType(elementDef)) { 
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                new BasicException.Parameter[] {
                    new BasicException.Parameter("model element", elementDef)
                },
                "model element not of type " + ModelAttributes.STRUCTURAL_FEATURE
            ); 
        }
    }

    // -------------------------------------------------------------------------
    final private boolean isAttributeOrReferenceStoredAsAttribute(
        ModelElement_1_0 elementDef
    ) throws ServiceException {
        return 
            !this.getModel().isOperationType(elementDef) && 
            (this.getModel().isAttributeType(elementDef) || 
             this.getModel().referenceIsStoredAsAttribute(elementDef));
    }

    // -------------------------------------------------------------------------
    final private void assertOperation(
        ModelElement_1_0 elementDef
    ) throws ServiceException {
        if (!this.getModel().isOperationType(elementDef)) { 
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                new BasicException.Parameter[] {
                    new BasicException.Parameter("model element", elementDef)
                },
                "model element not of type " + ModelAttributes.OPERATION
            ); 
        }
    }

    // -------------------------------------------------------------------------
    final private ModelElement_1_0 getType(
        ModelElement_1_0 elementDef
    )
        throws ServiceException {
        return this.getModel().getDereferencedType(
            elementDef.values("type").get(0));
    }

    // -------------------------------------------------------------------------
    final private ModelElement_1_0 getFeature(
        String featureName
    ) throws ServiceException {

        // Fully qualified feature name. Lookup in model
        if (featureName.indexOf(':') >= 0) {
            return this.getModel().getElement(featureName);
        }
        // Get all features of class and find feature with featureName
        else {
            if(this.refClassDef == null) {
                this.refClassDef = this.getModel().getElement(this.refClass.refMofId());
            }
            ModelElement_1_0 feature = this.getModel().getFeatureDef(
                this.refClassDef,
                featureName,
                false
            );
            if(feature == null) {                 
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    StackedException.NOT_FOUND,
                    new BasicException.Parameter[] {
                        new BasicException.Parameter("class name", this.refClass().refMofId()),
                        new BasicException.Parameter("feature", featureName)
                    },
                    "feature not found"
                ); 
            }
            return feature;
        }
    }

    // -------------------------------------------------------------------------
    final private Marshaller toRefStructMarshaller(
        String typeName
    ) {
        return new StructMarshaller(
            typeName, 
            (RefPackage_1_0) this.refClass().refOutermostPackage(), 
            true
        );
    }

    // -------------------------------------------------------------------------
    final public Model_1_2 getModel(
    ) throws ServiceException {
        if (RefObject_1.model == null) {
            try {
                RefObject_1.model = (Model_1_2) ((RefPackage_1_0)this.refClass.refImmediatePackage()).refModel();
            } 
            catch (Exception exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ACTIVATION_FAILURE,
                    null,
                    "Model_1_2 acquisition failed"
                );
            }
        }
        return RefObject_1.model;
    }

    // -------------------------------------------------------------------------
    public final Object getValue(
        ModelElement_1_0 featureDef, 
        Object qualifier
    ) throws ServiceException {
        return this.getValue(featureDef, qualifier, true);
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    final Object getValue(
        ModelElement_1_0 featureDef,
        Object qualifier,
        boolean marshal
    ) throws ServiceException {

        // SysLog.trace("refMofId", refMofId());
        // SysLog.trace("feature", featureDef);
        // SysLog.trace("qualifier", qualifier);

        boolean isReference = this.getModel().isReferenceType(featureDef);
        boolean isAttribute = this.getModel().isAttributeType(featureDef);
        boolean isReferenceStoredAsAttribute = isReference && this.getModel().referenceIsStoredAsAttribute(featureDef);
        if (!isAttribute && !isReference) { 
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                new BasicException.Parameter[] {
                    new BasicException.Parameter("model element", featureDef)
                },
                "model element not of type " + ModelAttributes.STRUCTURAL_FEATURE
            ); 
        }

        ModelElement_1_0 type = this.getType(featureDef);
        String qualifiedTypeName = (String) type.values("qualifiedName").get(0);
        String multiplicity = (String) featureDef.values("multiplicity").get(0);

        /**
         * Attribute or Reference stored as attribute. Don't care about
         * qualifier which can anyway only an index. The caller is responsible
         * to get the required element from the collection.
         */
        if (isAttribute || isReferenceStoredAsAttribute) {
            if (qualifier != null) { 
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    new BasicException.Parameter[] {
                        new BasicException.Parameter("feature", featureDef),
                        new BasicException.Parameter("qualifier", qualifier)
                    },
                    "qualifier must be null in case of attributes and references stored as attribute"
                ); 
            }

            /**
             * determine multiplicity of feature. In case of an attribute it is
             * the modeled multiplicity. In case of a reference with a qualifier
             * the multiplicity is <<list>> else the modeled multiplicity.
             */
            if (isReference) {
                ModelElement_1_0 referencedEnd = this.getModel().getElement(
                    featureDef.values("referencedEnd").get(0)
                );
                if(!referencedEnd.values("qualifierType").isEmpty()) {
                    ModelElement_1_0 qualifierType = this.getModel().getDereferencedType(
                        referencedEnd.values("qualifierType").get(0)
                    );
                    if (this.getModel().isNumericType(qualifierType)) {
                        multiplicity = Multiplicities.LIST;
                    } 
                    else {
                        multiplicity = Multiplicities.MAP;
                    }
                }
                // map aggregation none, multiplicity 0..n, no qualifier to
                // <<set>>
                // in case <<list>> semantic is required it must be modeled as
                // aggregation none, multiplicita 0..1, numeric qualifier
                else if (Multiplicities.MULTI_VALUE.equals(multiplicity)) {
                    multiplicity = Multiplicities.SET;
                }
            }

            // SINGLE_VALUE|OPTIONAL_VALUE
            if (
                Multiplicities.SINGLE_VALUE.equals(multiplicity) || 
                Multiplicities.OPTIONAL_VALUE.equals(multiplicity)
            ) {
                Object value = this.object.objGetValue((String) featureDef.values("name").get(0));
                if (!marshal) { 
                    return value; 
                }
                if (PrimitiveTypes.STRING.equals(qualifiedTypeName)) {
                    return value;
                } 
                else if (PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName)) {
                    return BooleanMarshaller.getInstance(true).marshal(value);
                } 
                else if (PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) {
                    return DateTimeMarshaller.getInstance(true).marshal(value);
                } 
                else if (PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                    return DateMarshaller.getInstance(true).marshal(value);
                } 
                else if (PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                    return DurationMarshaller.getInstance(true).marshal(value);
                } 
                else if (PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                    return ShortMarshaller.getInstance(true).marshal(value);
                } 
                else if (PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                    return IntegerMarshaller.getInstance(true).marshal(value);
                } 
                else if (PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                    return LongMarshaller.getInstance(true).marshal(value);
                } 
                else if (PrimitiveTypes.DECIMAL.equals(qualifiedTypeName)) {
                    return DecimalMarshaller.getInstance(true).marshal(value);
                } 
                else if (PrimitiveTypes.BINARY.equals(qualifiedTypeName)) {
                    return value;
                } 
                else if (this.getModel().isStructureType(type)) {
                    return this
                        .toRefStructMarshaller(qualifiedTypeName)
                        .marshal(value);
                } 
                else if (
                    this.getModel().isClassType(type) || 
                    PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                ) {
                    return ((Marshaller) this.refClass.refOutermostPackage()).marshal(value);
                } 
                else {
                    return value;
                }
            }
            // STREAM
            else if (Multiplicities.STREAM.equals(multiplicity)) {
                LargeObject_1_0 value = this.object.objGetLargeObject((String) featureDef.values("name").get(0));
                if (PrimitiveTypes.STRING.equals(qualifiedTypeName)) {
                    return value.getCharacterStream();
                } 
                else if (PrimitiveTypes.BINARY.equals(qualifiedTypeName)) {
                    return value.getBinaryStream();
                } 
                else {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        new BasicException.Parameter[] {
                            new BasicException.Parameter("feature", featureDef),
                            new BasicException.Parameter("type", type)
                        },
                        "unsupported stream type. Supported are [string|binary]"
                    );
                }
            }
            // LIST
            else if (
                Multiplicities.LIST.equals(multiplicity) || 
                Multiplicities.MULTI_VALUE.equals(multiplicity)
            ) {
                List values = this.object.objGetList((String) featureDef.values("name").get(0));
                if (!marshal) { 
                    return values; 
                }
                if (PrimitiveTypes.STRING.equals(qualifiedTypeName)) {
                    return values;
                } 
                else if (PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName)) {
                    return new MarshallingList(BooleanMarshaller.getInstance(true), values);
                } 
                else if (PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) {
                    return new MarshallingList(DateTimeMarshaller.getInstance(true), values);
                } 
                else if (PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                    return new MarshallingList(
                        DateMarshaller.getInstance(true),
                        values
                    );
                } 
                else if (PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                    return new MarshallingList(DurationMarshaller.getInstance(true), values);
                } 
                else if (PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                    return new MarshallingList(ShortMarshaller.getInstance(true), values);
                } 
                else if (PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                    return new MarshallingList(IntegerMarshaller.getInstance(true), values);
                } 
                else if (PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                    return new MarshallingList(
                        LongMarshaller.getInstance(true),
                        values);
                } 
                else if (PrimitiveTypes.DECIMAL.equals(qualifiedTypeName)) {
                    return new MarshallingList(DecimalMarshaller.getInstance(true), values);
                } 
                else if (this.getModel().isStructureType(type)) {
                    return new MarshallingList(this.toRefStructMarshaller(qualifiedTypeName), values);
                } 
                else if (
                    this.getModel().isClassType(type) || 
                    PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                ) {
                    return new MarshallingList((Marshaller) this.refClass.refOutermostPackage(), values);
                } 
                else {
                    return values;
                }
            }
            // SET
            else if (Multiplicities.SET.equals(multiplicity)) {
                Set values = this.object.objGetSet((String) featureDef.values("name").get(0));
                if (!marshal) { 
                    return values; 
                }
                if (PrimitiveTypes.STRING.equals(qualifiedTypeName)) {
                    return values;
                } 
                else if (PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName)) {
                    return new MarshallingSet(BooleanMarshaller.getInstance(true), values);
                } 
                else if (PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) {
                    return new MarshallingSet(DateTimeMarshaller.getInstance(true), values);
                } 
                else if (PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                    return new MarshallingSet(
                        DateMarshaller.getInstance(true),
                        values
                    );
                } 
                else if (PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                    return new MarshallingSet(DurationMarshaller.getInstance(true), values);
                } 
                else if (PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                    return new MarshallingSet(
                        ShortMarshaller.getInstance(true),
                        values
                    );
                } 
                else if (PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                    return new MarshallingSet(IntegerMarshaller.getInstance(true), values);
                } 
                else if (PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                    return new MarshallingSet(
                        LongMarshaller.getInstance(true),
                        values
                    );
                } 
                else if (PrimitiveTypes.DECIMAL.equals(qualifiedTypeName)) {
                    return new MarshallingSet(DecimalMarshaller.getInstance(true), values);
                } 
                else if (this.getModel().isStructureType(type)) {
                    return new MarshallingSet(this.toRefStructMarshaller(qualifiedTypeName), values);
                } 
                else if (
                    this.getModel().isClassType(type) || 
                    PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                ) {
                    return new MarshallingSet((Marshaller) this.refClass.refOutermostPackage(), values);
                } 
                else {
                    return values;
                }
            }
            // SPARSEARRAY
            else if (Multiplicities.SPARSEARRAY.equals(multiplicity)) {
                SortedMap values = this.object.objGetSparseArray((String) featureDef.values("name").get(0));
                if (!marshal) { 
                    return values; 
                }
                if (PrimitiveTypes.STRING.equals(qualifiedTypeName)) {
                    return values;
                } 
                else if (PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName)) {
                    return new MarshallingSortedMap(BooleanMarshaller.getInstance(true), values);
                } 
                else if (PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) {
                    return new MarshallingSortedMap(DateTimeMarshaller.getInstance(true), values);
                } 
                else if (PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                    return new MarshallingSortedMap(DateMarshaller.getInstance(true), values);
                } 
                else if (PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                    return new MarshallingSortedMap(DurationMarshaller.getInstance(true), values);
                } 
                else if (PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                    return new MarshallingSortedMap(ShortMarshaller.getInstance(true), values);
                } 
                else if (PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                    return new MarshallingSortedMap(IntegerMarshaller.getInstance(true), values);
                } 
                else if (PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                    return new MarshallingSortedMap(LongMarshaller.getInstance(true), values);
                } 
                else if (PrimitiveTypes.DECIMAL.equals(qualifiedTypeName)) {
                    return new MarshallingSortedMap(DecimalMarshaller.getInstance(true), values);
                } 
                else if (this.getModel().isStructureType(type)) {
                    return new MarshallingSortedMap(this.toRefStructMarshaller(qualifiedTypeName), values);
                } 
                else if (
                    this.getModel().isClassType(type) || 
                    PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                ) {
                    return new MarshallingSortedMap((Marshaller) this.refClass.refOutermostPackage(), values);
                } 
                else {
                    return values;
                }
            }
            // MAP
            else if (Multiplicities.MAP.equals(multiplicity)) {
                FilterableMap values = this.object.objGetContainer((String) featureDef.values("name").get(0));
                return values == null 
                    ? null 
                    : new RefContainer_1(
                        (Marshaller) this.refClass.refOutermostPackage(),
                        values
                      );
            } 
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    new BasicException.Parameter[] {
                        new BasicException.Parameter("feature", featureDef),
                        new BasicException.Parameter("type", type)
                    },
                    "unsupported multiplicity. Supported are [set|list|sparsearray|map|stream|0..n|0..1|1..1]"
                );
            }
        }

        /**
         * Reference (not stored as attribute)
         */
        else if (isReference) {

            // Class type qualifier
            if (qualifier instanceof RefObject) {

                /**
                 * Get qualifier of exposing association end. This qualifier is
                 * used to construct the reference filter:
                 */
                String exposedEndName = (String) this.getModel().getElement(
                    featureDef.values("exposedEnd").get(0)
                ).values("name").get(0);
                String qualifierName = (String) this.getModel().getElement(
                    featureDef.values("referencedEnd").get(0)
                ).values("qualifierName").get(0);
                int pos = 0;
                if (
                    ((pos = qualifierName.indexOf("Container")) >= 0) || 
                    ((pos = qualifierName.indexOf("container")) >= 0)
                ) {
                    qualifierName = qualifierName.substring(0, pos);
                }
                FilterableMap container = ((RefObject_1_0) qualifier).refDelegate().objGetContainer(qualifierName).subMap(
                    new FilterProperty[] {
                        new FilterProperty(
                            Quantors.THERE_EXISTS,
                            exposedEndName,
                            FilterOperators.IS_IN,
                            new Path[] {
                                this.object.objGetPath()
                            })
                    }
                );
                return new RefContainer_1((RefRootPackage_1) this.refClass.refOutermostPackage(), container);
            }

            // Primitive type or null qualifier
            else {
                RefRootPackage_1 rootPkg = (RefRootPackage_1) this.refClass.refOutermostPackage();
                ModelElement_1_0 referencedEnd = this.getModel().getElement(
                    featureDef.values("exposedEnd").get(0)
                );
                // navigation to parent object is performed locally by removing
                // the last to object path components
                if(
                    AggregationKind.SHARED.equals(referencedEnd.values("aggregation").get(0)) || 
                    AggregationKind.COMPOSITE.equals(referencedEnd.values("aggregation").get(0))
                ) {
                    return rootPkg.refObject(new Path(this.refMofId()).getParent().getParent().toString());
                } 
                else {
                    if(
                        (qualifier instanceof String) && 
                        (((String) qualifier).indexOf(';') >= 0)
                    ) {
                        return rootPkg.marshal(
                            rootPkg.refObjectFactory().getObject(
                                this.refGetPath().getDescendant(
                                    new String[]{
                                        (String)featureDef.values("name").get(0), 
                                        (String) qualifier
                                    }
                                )
                            )
                        );
                    } 
                    else {
                        String feature = (String) featureDef.values("name").get(0);
                        String objectClass = this.refClass().refMofId();
                        if (
                            this.getModel().isSubtypeOf(objectClass, "org:openmdx:base:ExtentCapable") && 
                            this.getModel().isSubtypeOf(objectClass,"org:openmdx:compatibility:state1:BasicState") && 
                            this.object.objIsPersistent() && 
                            !this.object.objIsNew() && 
                            !this.object.objIsDeleted()
                        ) {
                            feature += SystemAttributes.USE_OBJECT_IDENTITY_HINT;
                        }
                        FilterableMap container = this.object.objGetContainer(feature);
                        Object object = null;
                        try {
                            object = qualifier == null 
                                ? new RefContainer_1(rootPkg, container) 
                                : rootPkg.marshal(container.get(qualifier.toString()));
                        } 
                        catch (ServiceException e) {
                            // in case of 0..1 multiplicity allow null as return
                            // value
                            if(
                                rootPkg.getThrowNotFoundIfNull() || 
                                (e.getExceptionCode() != StackedException.NOT_FOUND) || 
                                !Multiplicities.OPTIONAL_VALUE.equals(multiplicity)
                            ) { 
                                throw new JmiServiceException(
                                    e,
                                    this
                                ); 
                            }
                        }
                        return object;
                    }
                }
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    final long getValue(
        ModelElement_1_0 featureDef, 
        Object value, 
        long position
    ) throws ServiceException {

        // SysLog.trace("refMofId", refMofId());
        // SysLog.trace("feature", featureDef);
        // SysLog.trace("qualifier", qualifier);

        boolean isAttribute = this.getModel().isAttributeType(featureDef);
        if (!isAttribute) { 
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                new BasicException.Parameter[] {
                    new BasicException.Parameter("model element", featureDef)
                },
                "model element not of type " + ModelAttributes.STRUCTURAL_FEATURE
            ); 
        }

        ModelElement_1_0 type = this.getType(featureDef);
        String qualifiedTypeName = (String) type.values("qualifiedName").get(0);
        String multiplicity = (String) featureDef.values("multiplicity").get(0);

        // STREAM
        if (Multiplicities.STREAM.equals(multiplicity)) {
            LargeObject_1_0 largeValue = this.object.objGetLargeObject((String) featureDef.values("name").get(0));
            if (PrimitiveTypes.STRING.equals(qualifiedTypeName)) {
                largeValue.getCharacterStream((java.io.Writer) value, position);
                return largeValue.length();
            } 
            else if (PrimitiveTypes.BINARY.equals(qualifiedTypeName)) {
                largeValue.getBinaryStream(
                    (java.io.OutputStream) value,
                    position
                );
                return largeValue.length();
            } 
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    new BasicException.Parameter[] {
                        new BasicException.Parameter("feature", featureDef),
                        new BasicException.Parameter("type", type)
                    },
                    "unsupported stream type. Supported are [string|binary]"
                );
            }
        } 
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                new BasicException.Parameter[] {
                    new BasicException.Parameter("feature", featureDef),
                    new BasicException.Parameter("type", type)
                },
                "unsupported multiplicity. Supported are [stream]"
            );
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    final private void setValue(
        ModelElement_1_0 featureDef, 
        Object value
    ) throws ServiceException {

        // SysLog.trace("refMofId", refMofId());
        // SysLog.trace("feature", featureDef);
        // SysLog.trace("value", value);

        this.assertStructuralFeature(featureDef);

        ModelElement_1_0 type = this.getType(featureDef);
        String qualifiedTypeName = (String) type.values("qualifiedName").get(0);

        /**
         * Attribute or Reference stored as attribute.
         */
        if (this.isAttributeOrReferenceStoredAsAttribute(featureDef)) {
            String multiplicity = (String) featureDef.values("multiplicity").get(0);
            if (this.getModel().isReferenceType(featureDef)) {
                ModelElement_1_0 referencedEnd = this.getModel().getElement(
                    featureDef.values("referencedEnd").get(0)
                );
                if(!referencedEnd.values("qualifierType").isEmpty()) {
                    multiplicity = Multiplicities.LIST;
                }
            }
            if (
                Multiplicities.OPTIONAL_VALUE.equals(multiplicity) || 
                Multiplicities.SINGLE_VALUE.equals(multiplicity) || 
                Multiplicities.STREAM.equals(multiplicity)
            ) {
                if (PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName)) {
                    this.object.objSetValue(
                        (String) featureDef.values("name").get(0), 
                        BooleanMarshaller.getInstance(true).unmarshal(value)
                    );
                } 
                else if (PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) {
                    this.object.objSetValue(
                        (String) featureDef.values("name").get(0), 
                        DateTimeMarshaller.getInstance(true).unmarshal(value)
                    );
                } 
                else if (PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                    this.object.objSetValue(
                        (String) featureDef.values("name").get(0), 
                        DateMarshaller.getInstance(true).unmarshal(value)
                    );
                } 
                else if (PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                    this.object.objSetValue(
                        (String) featureDef.values("name").get(0), 
                        DurationMarshaller.getInstance(true).unmarshal(value)
                    );
                } 
                else if (PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                    this.object.objSetValue(
                        (String) featureDef.values("name").get(0), 
                        ShortMarshaller.getInstance(true).unmarshal(value)
                    );
                } 
                else if (PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                    this.object.objSetValue(
                        (String) featureDef.values("name").get(0), 
                        IntegerMarshaller.getInstance(true).unmarshal(value)
                    );
                } 
                else if (PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                    this.object.objSetValue(
                        (String) featureDef.values("name").get(0), 
                        LongMarshaller.getInstance(true).unmarshal(value)
                    );
                } 
                else if (PrimitiveTypes.DECIMAL.equals(qualifiedTypeName)) {
                    this.object.objSetValue(
                        (String) featureDef.values("name").get(0), 
                        DecimalMarshaller.getInstance(true).unmarshal(value)
                    );
                } 
                else if (PrimitiveTypes.BINARY.equals(qualifiedTypeName)) {
                    this.object.objSetValue(
                        (String) featureDef.values("name").get(0), 
                        value
                    );
                } 
                else if (this.getModel().isStructureType(type)) {
                    this.object.objSetValue(
                        (String) featureDef.values("name").get(0), 
                        this.toRefStructMarshaller(qualifiedTypeName).unmarshal(value)
                    );
                } 
                else if (this.getModel().isClassType(type)) {
                    this.object.objSetValue(
                        (String) featureDef.values("name").get(0), 
                        ((Marshaller) this.refClass.refOutermostPackage()).unmarshal(value)
                    );
                } 
                else {
                    this.object.objSetValue(
                        (String) featureDef.values("name").get(0), 
                        value
                    );
                }
            }

            /**
             * In case of multi-valued attributes clear() and addAll()
             */
            else {
                Object newValue = null;
                if (
                    (value != null) && 
                    (value.getClass().isArray())
                ) {
                    newValue = new ArrayList();
                    for (int i = 0; i < Array.getLength(value); i++) {
                        ((List) newValue).add(
                            (value instanceof short[]) 
                                ?  new Short(((short[]) value)[i])
                                : (value instanceof int[]) 
                                    ? new Integer(((int[]) value)[i])
                                    : (value instanceof long[]) 
                                        ? new Long(((long[]) value)[i])
                                        : (value instanceof boolean[]) 
                                            ? Boolean.valueOf(((boolean[]) value)[i])
                                            : ((Object[]) value)[i]
                        );
                    }
                } 
                else {
                    newValue = value;
                }
                Object values = this.getValue(featureDef, null);
                if (values != newValue) {
                    if (Multiplicities.SPARSEARRAY.equals(multiplicity)) {
                        ((SortedMap) values).clear();
                        if (newValue instanceof Collection) {
                            int i = 0;
                            for(
                                Iterator j = ((Collection) newValue).iterator(); 
                                j.hasNext();
                            ) {
                                ((SortedMap) values).put(
                                    new Integer(i++), 
                                    j.next()
                                );
                            }
                        } 
                        else {
                            ((SortedMap) values).putAll((SortedMap) newValue);
                        }
                    } 
                    else if (
                        Multiplicities.LIST.equals(multiplicity) || 
                        Multiplicities.MULTI_VALUE.equals(multiplicity)
                    ) {
                        ((List) values).clear();
                        if (newValue != null) {
                            ((List) values).addAll((Collection) newValue);
                        }
                    } 
                    else if (Multiplicities.SET.equals(multiplicity)) {
                        ((Set) values).clear();
                        ((Set) values).addAll((Collection) newValue);
                    } 
                    else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE,
                            new BasicException.Parameter[] {
                                new BasicException.Parameter("feature", featureDef),
                                new BasicException.Parameter("multiplicity", multiplicity),
                            },
                            "unknown multiplicity"
                        );
                    }
                }
            }
        }

        /**
         * References (not supported)
         */
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                new BasicException.Parameter[] {
                    new BasicException.Parameter("feature", featureDef),
                    new BasicException.Parameter("type", type)
                },
                "set supported only for attributes and references stored as attributes"
            );
        }
    }

    // -------------------------------------------------------------------------
    final void setValue(
        ModelElement_1_0 featureDef,
        Object newValue,
        long length
    ) throws ServiceException {

        // SysLog.trace("refMofId", refMofId());
        // SysLog.trace("feature", featureDef);
        // SysLog.trace("qualifier", qualifier);

        boolean isAttribute = this.getModel().isAttributeType(featureDef);
        if (!isAttribute) { 
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                new BasicException.Parameter[] {
                    new BasicException.Parameter("model element", featureDef)
                },
                "model element not of type " + ModelAttributes.STRUCTURAL_FEATURE
            ); 
        }

        ModelElement_1_0 type = this.getType(featureDef);
        String qualifiedTypeName = (String) type.values("qualifiedName").get(0);
        String multiplicity = (String) featureDef.values("multiplicity").get(0);

        // STREAM
        if (Multiplicities.STREAM.equals(multiplicity)) {
            LargeObject_1_0 largeValue = this.object.objGetLargeObject((String) featureDef.values("name").get(0));
            if (PrimitiveTypes.STRING.equals(qualifiedTypeName)) {
                largeValue.setCharacterStream(
                    (java.io.Reader)newValue, 
                    length
                );
            } 
            else if (PrimitiveTypes.BINARY.equals(qualifiedTypeName)) {
                largeValue.setBinaryStream(
                    (java.io.InputStream) newValue, 
                    length
                );
            } 
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    new BasicException.Parameter[] {
                        new BasicException.Parameter("feature", featureDef),
                        new BasicException.Parameter("type", type)
                    },
                    "unsupported stream type. Supported are [string|binary]"
                );
            }
        } 
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                new BasicException.Parameter[] {
                    new BasicException.Parameter("feature", featureDef),
                    new BasicException.Parameter("type", type)
                },
                "unsupported multiplicity. Supported are [stream]"
            );
        }
    }

    // -------------------------------------------------------------------------
    /**
     * args contains one element which is of type RefStruct
     */
    final private Object invokeOperation(
        ModelElement_1_0 featureDef, 
        List<?> args
    ) throws ServiceException {

        SysLog.trace("refMofId", this.object.objGetPath());
        SysLog.trace("feature", featureDef);
        SysLog.trace("args", args);

        this.assertOperation(featureDef);

        // get the type names of 'in' parameter and 'result'
        String qualifiedNameResultType = null;
        String qualifiedNameInParamType = null;
        for (
            Iterator<?> i = featureDef.values("content").iterator(); 
            i.hasNext();
        ) {
            ModelElement_1_0 paramDef = this.getModel().getElement(i.next());
            ModelElement_1_0 paramDefType = this.getType(paramDef);
            if ("in".equals(paramDef.values("name").get(0))) {
                qualifiedNameInParamType = (String) paramDefType.values("qualifiedName").get(0);
            } 
            else if ("result".equals(paramDef.values("name").get(0))) {
                qualifiedNameResultType = (String) paramDefType.values("qualifiedName").get(0);
            }
        }
        if (qualifiedNameInParamType == null) { 
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                new BasicException.Parameter[] {
                    new BasicException.Parameter("operation", featureDef)
                },
                "no parameter with name \"in\" defined for operation"
            ); 
        }
        if (qualifiedNameResultType == null) { 
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                new BasicException.Parameter[] {
                    new BasicException.Parameter("operation", featureDef)
                },
                "no parameter with name \"result\" defined for operation"
            ); 
        }
        
        // Create in param as JMI struct
        RefStruct_1_0 param = null;
        if (
            (args.size() == 1) && 
            (args.get(0) instanceof RefStruct_1_0)
        ) { 
            param = (RefStruct_1_0)args.get(0);
        }
        else {
            param = (RefStruct_1_0)this.refImmediatePackage().refCreateStruct(
                qualifiedNameInParamType, 
                args
            );
        }
        
        // invoke operation
        Structure_1_0 unmarshalledParam = (Structure_1_0)this.toRefStructMarshaller(
            qualifiedNameInParamType
        ).unmarshal(param);
        return this.toRefStructMarshaller(qualifiedNameResultType).marshal(
            ((Boolean) featureDef.values("isQuery").get(0)).booleanValue() 
                ? this.object.objInvokeOperation((String) featureDef.values("name").get(0), unmarshalledParam)
                : this.object.objInvokeOperationInUnitOfWork((String) featureDef.values("name").get(0), unmarshalledParam)
        );
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    final public Object refGetValue(
        RefObject feature, 
        int index
    ) {
        try {
            Object values = this.getValue((ModelElement_1_0) feature, null);
            if (values instanceof List) {
                return ((List<?>) values).get(index);
            } else if (values instanceof SortedMap) {
                return ((SortedMap<Integer,?>) values).get(new Integer(index));
            } else {
                throw new JmiServiceException(
                    new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        StackedException.NOT_SUPPORTED,
                        new BasicException.Parameter[] {
                            new BasicException.Parameter("values", values)
                        },
                        "operation only supported for features instanceof [List|SparseArray]"),
                    this);
            }
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    // RefObject_1
    // operations which might be useful for subclasses
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    /**
     * feature must be attribute or reference stored as attribute
     */
    @SuppressWarnings("unchecked")
    final protected Object refGetValue(
        String featureName, 
        int index
    ) {
        Object value = null;
        try {
            value = this.getValue(this.getFeature(featureName), null);
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        if (value instanceof Collection) {
            if (value instanceof List) {
                return ((List<?>) value).get(index);
            } 
            else if (value instanceof SortedMap) {
                return ((SortedMap<Integer,?>) value).get(new Integer(index));
            } 
            else {
                throw new JmiServiceException(
                    new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        StackedException.NOT_SUPPORTED,
                        new BasicException.Parameter[] {
                            new BasicException.Parameter("value", value)
                        },
                        "operation only supported for features instanceof [List|SparseArray]"
                    ),
                    this
                );
            }
        }

        // single-valued --> index = 0
        else if (index == 0) {
            return value;
        }

        // index > 0 not supported
        else {
            throw new JmiServiceException(
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    StackedException.NOT_SUPPORTED,
                    new BasicException.Parameter[] {
                        new BasicException.Parameter("feature", featureName),
                        new BasicException.Parameter("value", value)
                    },
                    "index must be 0 in case of a non-collection value"
                ), this
            );
        }
    }

    // -------------------------------------------------------------------------
    /**
     * Feature must be attribute or reference stored as attribute
     */
    @SuppressWarnings("unchecked")
    final protected Object refGetValue(String featureName, String qualifier) {
        Object map = null;
        Object value = null;
        RefRootPackage_1 rootPkg = (RefRootPackage_1) this.refClass.refOutermostPackage();
        try {
            map = this.getValue(this.getFeature(featureName), null);
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        if (map instanceof Map) {
            value = ((Map<String,?>) map).get(qualifier);
        } 
        else if (map instanceof RefContainer_1_0) {
            try {
                value = ((RefContainer_1_0) map).get(qualifier);
            } 
            catch (MarshalException exception) {
                throw new JmiServiceException(exception, this);
            }
        } 
        else {
            throw new JmiServiceException(
                new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.NOT_SUPPORTED,
                    new BasicException.Parameter[] {
                        new BasicException.Parameter("feature", featureName),
                        new BasicException.Parameter("value", map)
                    },
                    "index must be 0 in case of a non-collection value"
                ), 
                this
            );
        }
        if ((value == null) && rootPkg.getThrowNotFoundIfNull()) { 
            throw new JmiServiceException(
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    new BasicException.Parameter[] {
                        new BasicException.Parameter("object", this.refGetPath()),
                        new BasicException.Parameter("feature", featureName),
                        new BasicException.Parameter("qualifier", qualifier)
                    },
                    "object not found"
                )
            ); 
        }
        return value;
    }

    // -------------------------------------------------------------------------
    final protected Object refGetValue(
        String featureName, 
        Object qualifier
    ) {
        try {
            Model_1_0 model = this.getModel();
            ModelElement_1_0 featureDef = this.getFeature(featureName);
            Object value = null;
            if (
                model.isAttributeType(featureDef) || 
                model.referenceIsStoredAsAttribute(featureDef)
            ) {
                // TODO: check modeled qualifier type instead of qualifier class
                if (qualifier instanceof String) {
                    value = this.refGetValue(featureName, (String) qualifier);
                }
                // must be numeric
                else {
                    value = this.refGetValue(
                        featureName,
                        ((Integer) IntegerMarshaller.getInstance(true).unmarshal(qualifier)).intValue()
                    );
                }
            } 
            else {
                value = this.getValue(featureDef, qualifier);
            }
            return value;
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        } 
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public long refGetValue(
        String featureName,
        Object value,
        long position) {
        try {
            /* Model_1_0 model = */this.getModel();
            ModelElement_1_0 featureDef = this.getFeature(featureName);
            return this.getValue(featureDef, value, position);
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        } 
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    final protected void refSetValue(
        String featureName, 
        int index, 
        Object value
    ) {
        try {
            RefPackage refPackage = refOutermostPackage();
            Object viewContext = refPackage instanceof RefPackage_1_2 
                ? ((RefPackage_1_2) refPackage).refViewContext()
                : null;
            Object values = viewContext == null 
                ? this.getValue(this.getFeature(featureName), null) 
                : null;

            // set indexed element in case of Colletion values. For convencience
            // if collection.size() == index the element is added.
            if (values instanceof Collection) {
                if (values instanceof List) {
                    List<Object> valuesAsList = (List<Object>) values;
                    if (valuesAsList.size() == index) {
                        valuesAsList.add(value);
                    } 
                    else {
                        valuesAsList.set(index, value);
                    }
                } 
                else if (values instanceof SortedMap) {
                    ((SortedMap<Integer,Object>) values).put(new Integer(index), value);
                } 
                else {
                    throw new JmiServiceException(
                        new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            StackedException.NOT_SUPPORTED,
                            new BasicException.Parameter[] {
                                new BasicException.Parameter("values", values)
                            },
                            "operation only supported for features instanceof [List|SparseArray]"
                        ),
                        this
                    );
                }
            }

            // reset value in case of non-collection attributes only supported
            // when index=0
            else if (index == 0) {
                this.setValue(this.getFeature(featureName), value);
            }

            // index > 0 not supported
            else {
                throw new JmiServiceException(
                    new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        StackedException.NOT_SUPPORTED,
                        new BasicException.Parameter[] {
                            new BasicException.Parameter("values", values)
                        },
                        "index must be 0 for non-collection values"
                    ), 
                    this
                );
            }
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        } 
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public void refSetValue(
        String featureName,
        Object newValue,
        long length) {
        try {
            /* Model_1_0 model = */this.getModel();
            ModelElement_1_0 featureDef = this.getFeature(featureName);
            this.setValue(featureDef, newValue, length);
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        } 
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    final protected void refAddValue(
        String featureName, 
        int index, 
        Object value
    ) {
        Object values = null;
        try {
            values = this.getValue(this.getFeature(featureName), null);
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        if (values instanceof List) {
            ((List<Object>) values).add(index, value);
        } 
        else {
            throw new JmiServiceException(
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    StackedException.NOT_SUPPORTED,
                    new BasicException.Parameter[] {
                        new BasicException.Parameter("values", values)
                    },
                    "operation only supported for features instanceof [List]"                   
                ),
                this
            );
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    final protected void refAddValue(
        String featureName, 
        Object value
    ) {
        Object values = null;
        try {
            values = this.getValue(this.getFeature(featureName), null);
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        if (values instanceof List || values instanceof Set) {
            ((Collection<Object>) values).add(value);
        } 
        else if (value instanceof SortedMap) {
            ((SortedMap<Integer,Object>) values).put(
                new Integer(((Integer) ((SortedMap) values).lastKey()).intValue() + 1),
                value
            );
        } 
        else if (values instanceof Container && value instanceof RefObject_1_0) {
            ((Container<RefObject_1_0>) values).add((RefObject_1_0)value);
        } 
        else {
            throw new JmiServiceException(
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    StackedException.NOT_SUPPORTED,
                    new BasicException.Parameter[] {
                        new BasicException.Parameter("value class", values.getClass().getName()),
                        new BasicException.Parameter("values", values)
                    },
                    "operation only supported for features instanceof [Set|List|SparseArray|Container]"
                ),
                this
            );
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings({
        "unchecked", "unchecked", "unchecked"
    })
    final protected void refRemoveValue(
        String featureName, 
        int index
    ) {
        Object values = null;
        try {
            values = this.getValue(this.getFeature(featureName), null);
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        if (values instanceof List) {
            ((List<Object>) values).remove(index);
        } 
        else if (values instanceof SortedMap) {
            ((SortedMap<Integer,Object>) values).remove(new Integer(index));
        } 
        else {
            throw new JmiServiceException(
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    StackedException.NOT_SUPPORTED,
                    new BasicException.Parameter[] {
                        new BasicException.Parameter("values", values)
                    },
                    "operation only supported for features instanceof [List|SparseArray]"
                ),
                this
            );
        }
    }

    // -------------------------------------------------------------------------
    final protected void refRemoveValue(
        String featureName
    ) {
        Object values = null;
        try {
            values = this.getValue(this.getFeature(featureName), null);
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        if (
            values instanceof List || 
            values instanceof Set || 
            values instanceof Container
        ) {
            try {
                ((Collection<?>) values).clear();
            } 
            catch (RuntimeServiceException e) {
                throw new JmiServiceException(e, this);
            }
        } 
        else if (values instanceof SortedMap) {
            try {
                ((SortedMap<?,?>) values).clear();
            } 
            catch (RuntimeServiceException e) {
                throw new JmiServiceException(e, this);
            }
        } 
        else {
            throw new JmiServiceException(
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    StackedException.NOT_SUPPORTED,
                    new BasicException.Parameter[] {
                        new BasicException.Parameter("values", values)
                    },
                    "operation only supported for features instanceof [Set|List|SparseArray|Container]"
                ),
                this
            );
        }
    }

    // -------------------------------------------------------------------------
    // RefObject interface
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    final public boolean refIsInstanceOf(
        RefObject objType,
        boolean considerSubtypes
    ) {
        try {
            if (!this.getModel().isClassType(objType)) { 
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    new BasicException.Parameter[] {
                        new BasicException.Parameter("objType", objType),
                    },
                    "objType must be a class type"
                ); 
            }
            return this.getModel().isInstanceof(this.object, objType);
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        } 
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public RefClass refClass(
    ) {
        return this.refClass;
    }

    // -------------------------------------------------------------------------
    final public RefFeatured refImmediateComposite(
    ) {
        throw new UnsupportedOperationException();
    }

    // -------------------------------------------------------------------------
    final public RefFeatured refOutermostComposite(
    ) {
        throw new UnsupportedOperationException();
    }

    // -------------------------------------------------------------------------
    final public void refDelete() {
        try {
            this.object.objRemove();
        } catch (ServiceException e) {
            throw new RuntimeServiceException(e);
        } catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    // RefFeatured interface
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    final public Object refGetValue(
        RefObject feature
    ) {
        try {
            Object value = this.getValue(((RefMetaObject_1) feature).getElementDef(), null);
            return value;
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        } 
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public Object refGetValue(
        String featureName
    ) {
        try {
            Object value = this.getValue(this.getFeature(featureName), null);
            return value;
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        } 
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public void refSetValue(
        RefObject feature, 
        Object value
    ) {
        try {
            this.setValue(((RefMetaObject_1) feature).getElementDef(), value);
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        } 
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public void refSetValue(
        String featureName, 
        Object value
    ) {
        try {
            this.setValue(this.getFeature(featureName), value);
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        } catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    final public Object refInvokeOperation(
        RefObject requestedOperation,
        List args
    ) {
        try {
            return this.invokeOperation(
                (ModelElement_1_0) requestedOperation,
                args
            );
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        } 
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    final public Object refInvokeOperation(
        String operationName, 
        List args
    ) throws RefException {
        try {
            return this.invokeOperation(
                this.getFeature(operationName), 
                args
            );
        } 
        catch (ServiceException e) {
            String exceptionType = null;
            for (int i = 0; i < e.getExceptionStack().getParameters().length; i++) {
                if ("typeName".equals(e.getExceptionStack().getParameters()[i].getName())) {
                    exceptionType = e.getExceptionStack().getParameters()[i].getValue();
                    break;
                }
            }

            // try to map to a user-defined exception. If not possible map to
            // a RefException
            if (exceptionType != null) {
                String packageName = this.refImmediatePackage().refMofId();
                packageName = packageName.substring(
                    0,
                    packageName.lastIndexOf(':')).replace(':', '.'
                );
                String typeName = exceptionType.substring(exceptionType.lastIndexOf(":") + 1);
                try {
                    Class exceptionClass = Classes.getApplicationClass(
                        packageName + 
                        "." + 
                        ((RefRootPackage_1)this.refClass.refOutermostPackage()).refBindingPackageSuffix() + "." + typeName
                    );
                    Constructor constructor = exceptionClass.getConstructor(
                        new Class[] {
                            ServiceException.class
                        }
                    );
                    throw (RefException) constructor.newInstance(new Object[] {
                        e
                    });
                } 
                catch (InstantiationException dummy) {
                    throw new RefException_1(e);
                } 
                catch (IllegalAccessException dummy) {
                    throw new RefException_1(e);
                } 
                catch (InvocationTargetException dummy) {
                    throw new RefException_1(e);
                } 
                catch (ClassNotFoundException dummy) {
                    throw new RefException_1(e);
                } 
                catch (NoSuchMethodException dummy) {
                    throw new RefException_1(e);
                }
            }
            throw new RefException_1(e);
        } 
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    // RefBaseObject
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    final public RefObject refMetaObject(
    ) {
        try {
            if (this.metaObject == null) {
                this.metaObject = new RefMetaObject_1(
                    this.getModel().getElement(this.refClass().refMofId())
                );
            }
            return this.metaObject;
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public RefPackage refImmediatePackage(
    ) {
        return "org:openmdx:base:Authority".equals(this.refClass().refMofId())
            ? this.refOutermostPackage().refPackage(this.refGetPath().get(0))
            : this.refClass().refImmediatePackage();
    }

    // -------------------------------------------------------------------------
    final public RefPackage refOutermostPackage(
    ) {
        return this.refClass().refOutermostPackage();
    }

    // -------------------------------------------------------------------------
    final public String refMofId(
    ) {
        try {
            String objectClass = this.refClass().refMofId();
            // Return object identity as refMofId else access path
            if (this.refMofId != null) {
                return this.refMofId;
            } 
            else {
                if (
                    !refIsDeleted() &&
                    this.getModel().isSubtypeOf(objectClass, "org:openmdx:base:ExtentCapable") && 
                    this.object.objDefaultFetchGroup().contains(SystemAttributes.OBJECT_IDENTITY)
                ) {
                    this.refMofId = (String) this.object.objGetValue(
                        SystemAttributes.OBJECT_IDENTITY
                    );
                    if(this.refMofId != null) {
                        return this.refMofId;
                    }
                }
                Path identity = this.object.objGetPath();
                return identity == null ? null : identity.toXri();
            }
        } 
        catch (ServiceException exception) {
            // Do not include "this" to avoid endless recursion!
            throw new JmiServiceException(exception);
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    final public Collection refVerifyConstraints(
        boolean deepVerify
    ) {
        try {
            Collection<ServiceException> violationSource = getModel().verifyObject(
                this.object,
                deepVerify,
                false // verifyDerived
            );
            if (violationSource == null) {
                return null;
            } 
            else {
                String thisElement = getElementIdentifier(this.object);
                JmiException[] violationTarget = new JmiException[violationSource.size()];
                Iterator<ServiceException> violationIterator = violationSource.iterator();
                for (int i = 0; i < violationTarget.length; i++) {
                    ServiceException serviceException = violationIterator.next();
                    boolean thisElementInError = 
                        thisElement != null && 
                        thisElement.equals(getElementInError(serviceException));
                    violationTarget[i] = thisElementInError 
                        ? new JmiServiceException(serviceException, this)
                        : new JmiServiceException(serviceException);
                }
                return Arrays.asList(violationTarget);
            }
        } 
        catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    // -------------------------------------------------------------------------
    private String getElementIdentifier(
        Object_1_0 object
    ) {
        try {
            return object.objGetPath().toString();
        } catch (ServiceException exception) {
            return object.objGetResourceIdentifier().toString();
        }
    }

    // -------------------------------------------------------------------------
    private String getElementInError(
        ServiceException serviceException
    ) {
        return serviceException
            .getCause(BasicException.Code.DEFAULT_DOMAIN)
            .getParameter("elementInError");
    }

    // -------------------------------------------------------------------------
    // RefObject_1_0
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    final public Path refGetPath(
    ) {
        try {
            return this.object.objGetPath();
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public void refWriteProtect(
    ) {
        throw new JmiServiceException(
            new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                StackedException.NOT_SUPPORTED,
                null,
                "Write protection no longer supported"
            ), 
            this
        );
    }

    // -------------------------------------------------------------------------
    final public void refAddToUnitOfWork(
    ) {
        try {
            this.object.objAddToUnitOfWork();
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public void refRemoveFromUnitOfWork(
    ) {
        try {
            this.object.objRemoveFromUnitOfWork();
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public boolean refIsWriteProtected(
    ) {
        return false; // Write protection no longer supported
    }

    // -------------------------------------------------------------------------
    final public boolean refIsDirty(
    ) {
        try {
            return this.object.objIsDirty();
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public boolean refIsPersistent(
    ) {
        try {
            return this.object.objIsPersistent();
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public boolean refIsNew(
    ) {
        try {
            return this.object.objIsNew();
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public boolean refIsDeleted(
    ) {
        try {
            return this.object.objIsDeleted();
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public void refRefresh(
    ) {
        try {
            this.object.objRefresh();
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public void refFlush(
    ) {
        try {
            this.object.objFlush();
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    public void refRefreshAsynchronously(
    ) {
        try {
            this.object.objMakeVolatile();
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Set<String> refDefaultFetchGroup(
    ) {
        try {
            // fetch group of object
            Set<String> fetchGroup = this.object.objDefaultFetchGroup();

            // add non-derived features
            RefMetaObject_1 objectDef = (RefMetaObject_1) this.refMetaObject();
            fetchGroup.addAll(this.getModel().getAttributeDefs(
                objectDef.getElementDef(),
                false,
                false).keySet()
            );
            return fetchGroup;
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public Object refGetValue(
        RefObject feature,
        Object qualifier,
        boolean marshal
    ) {
        try {
            return this.getValue(
                ((RefMetaObject_1) feature).getElementDef(),
                qualifier,
                marshal
            );
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    private boolean isUnmodifiable(
        ModelElement_1_0 featureDef
    ) {
        if(
            (refDelegate() instanceof ViewObject_1_0) &&
            refIsPersistent() 
        ) {
            String feature = (String) featureDef.values("name").get(0);
            return 
                State_1_Attributes.STATE_VALID_FROM.equals(feature) ||
                State_1_Attributes.STATE_VALID_TO.equals(feature);
        } 
        else {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    final public void refInitialize(
        boolean setRequiredToNull,
        boolean setOptionalToNull
    ) {
        try {
            ModelElement_1_0 elementDef = ((RefMetaObject_1)this.refMetaObject()).getElementDef();
            for (
                Iterator<ModelElement_1_0> i = ((Map) elementDef.values("allFeature").get(0)).values().iterator(); 
                i.hasNext();
            ) {
                ModelElement_1_0 featureDef = i.next();
                if (
                    this.isAttributeOrReferenceStoredAsAttribute(featureDef) && 
                    ((Boolean) featureDef.values("isChangeable").get(0)).booleanValue() && 
                    !isUnmodifiable(featureDef)
                ) {
                    String multiplicity = (String) featureDef.values("multiplicity").get(0);
                    ModelElement_1_0 type = this.getModel().getDereferencedType(featureDef.values("type").get(0));
                    if (Multiplicities.OPTIONAL_VALUE.equals(multiplicity)) {
                        if (setOptionalToNull) {
                            this.setValue(featureDef, null);
                        }
                    } 
                    else if (
                        Multiplicities.SET.equals(multiplicity) || 
                        Multiplicities.LIST.equals(multiplicity) || 
                        Multiplicities.MULTI_VALUE.equals(multiplicity) || 
                        Multiplicities.SPARSEARRAY.equals(multiplicity)
                    ) {
                        this.setValue(featureDef, new Object[] {});
                    } 
                    else if (Multiplicities.SINGLE_VALUE.equals(multiplicity)) {
                        if (setRequiredToNull) {
                            this.setValue(featureDef, null);
                        } 
                        else {
                            String qualifiedTypeName = (String) type.values("qualifiedName").get(0);
                            if (PrimitiveTypes.STRING.equals(qualifiedTypeName)) {
                                this.setValue(featureDef, "");
                            } 
                            else if (PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName)) {
                                this.setValue(featureDef, Boolean.FALSE);
                            } 
                            else if (PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) {
                                this.setValue(
                                    featureDef, 
                                    DateTimeMarshaller.getInstance(true).marshal("20000101T000000Z")
                                );
                            } 
                            else if (PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                                this.setValue(
                                    featureDef, 
                                    DateMarshaller.getInstance(true).marshal("20000101")
                                );
                            } 
                            else if (PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                                this.setValue(
                                    featureDef, 
                                    DurationMarshaller.getInstance(true).marshal("P0M")
                                );
                            } 
                            else if (PrimitiveTypes.SHORT .equals(qualifiedTypeName)) {
                                this.setValue(featureDef, new Short((short) 0));
                            } 
                            else if (PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                                this.setValue(featureDef, new Integer(0));
                            } 
                            else if (PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                                this.setValue(featureDef, new Long(0L));
                            } 
                            else if (PrimitiveTypes.DECIMAL.equals(qualifiedTypeName)) {
                                this.setValue(featureDef, new BigDecimal(0));
                            } 
                            else if (PrimitiveTypes.BINARY.equals(qualifiedTypeName)) {
                                this.setValue(featureDef, new byte[] {});
                            } 
                            else if (this.getModel().isStructureType(type)) {
                                throw new UnsupportedOperationException(
                                    "initializing of structs not supported"
                                );
                            } 
                            else if (
                                this.getModel().isClassType(type) || 
                                PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                            ) {
                                SysLog.warning("initializing of object references not supported", featureDef);
                            } 
                            else {
                                throw new UnsupportedOperationException(
                                    "unsupported type " + type
                                );
                            }
                        }
                    }
                }
            }
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    final public void refInitialize(
        RefObject source
    ) {
        try {
            ModelElement_1_0 elementDef = ((RefMetaObject_1) this.refMetaObject()).getElementDef();
            for (
                Iterator<ModelElement_1_0> i = ((Map) elementDef.values("allFeature").get(0)).values().iterator(); 
                i.hasNext();
            ) {
                ModelElement_1_0 featureDef = i.next();
                if (
                    this.isAttributeOrReferenceStoredAsAttribute(featureDef) && 
                    Boolean.TRUE.equals(featureDef.values("isChangeable").get(0)) &&
                    !isUnmodifiable(featureDef)
                ) {
                    this.setValue(featureDef, ((RefObject_1_2) source).getValue(
                        featureDef,
                        null)
                    );
                }
            }
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    final public void refAddValue(
        String featureName,
        Object qualifier,
        Object value
    ) {
        try {
            if (qualifier == null) {
                this.refAddValue(featureName, value);
            } 
            else {
                Object container = this.getValue(
                    this.getFeature(featureName),
                    null
                );
                if (
                    (container instanceof List) && 
                    (qualifier instanceof Number)
                ) {
                    ((List<Object>) container).add(
                        ((Number) qualifier).intValue(),
                        value
                    );
                } 
                else if (
                    (container instanceof RefContainer_1) && 
                    (value instanceof RefObject_1_0)
                ) {
                    RefContainer_1 refContainer = (RefContainer_1) container;
                    (((RefObject_1_0) value).refDelegate()).objMove(
                        refContainer.refDelegate(),
                        qualifier.toString()
                    );
                    if (value instanceof RefObject_1_1) {
                        ((RefObject_1_1) value).refSetOutermostPackage(
                            refContainer.refOutermostPackage()
                        );
                    }
                } 
                else {
                    throw new JmiServiceException(
                        new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            StackedException.NOT_SUPPORTED,
                            new BasicException.Parameter[] {
                                new BasicException.Parameter(
                                    "values", container == null ? null : container.getClass().getName()
                                )
                            },
                            "a) feature type = Reference and qualifier is RefObject_1_0; b) feature type = collection type attribute and qualifier is Number"
                        ),
                        this
                    );
                }
            }
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        } 
        catch (RuntimeServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public void refRemoveValue(
        String featureName, 
        Object qualifier
    ) {
        Object values = null;
        try {
            ModelElement_1_0 feature = this.getFeature(featureName);
            // optimized remove. do not marshal Object_1_0 to RefObject
            if (this.getModel().isReferenceType(feature)) {
                String base = qualifier.toString();
                String reference = (String) feature.values("name").get(0);
                if (base.indexOf(';') < 0) {
                    if (this.object.objGetContainer(reference).remove(base) == null) { 
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_FOUND,
                            new BasicException.Parameter[] {
                                new BasicException.Parameter("feature", featureName),
                                new BasicException.Parameter("qualifier", base)
                            },
                            "Attempt to remove an object with a given qualifier while no object is bound to this qualifier"
                        ); 
                    }
                } 
                else {
                    RefRootPackage_1 rootPkg = (RefRootPackage_1) this.refClass.refOutermostPackage();
                    rootPkg.refObjectFactory().getObject(
                        this.refGetPath().getDescendant(new String[] {
                            reference, base
                        })).objRemove();
                }
            }
            // non-optimized remove
            else {
                values = this.getValue(this.getFeature(featureName), null);
            }
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }

        if (values == null) {
            // object removal handle above
        } 
        else if (
            (values instanceof List) && 
            (qualifier instanceof Number)
        ) {
            ((List<?>) values).remove(((Number) qualifier).intValue());
        } 
        else if (values instanceof SortedMap && qualifier instanceof Number) {
            try {
                ((SortedMap<?,?>) values).remove(
                    new Integer(((Number) qualifier).intValue())
                );
            } 
            catch (RuntimeServiceException e) {
                throw new JmiServiceException(e, this);
            }
        } 
        else {
            throw new JmiServiceException(
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    StackedException.NOT_SUPPORTED,
                    new BasicException.Parameter[] {
                        new BasicException.Parameter("values", values)
                    },
                    "operation only supported for features instanceof [List|SparseArray|FilterableMap]"
                ),
                this
            );
        }
    }

    // -------------------------------------------------------------------------
    final public void refRemoveValue(
        String featureName, 
        RefObject value
    ) {
        Object values = null;
        try {
            values = this.getValue(this.getFeature(featureName), null);
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }

        // remove object
        if (values instanceof Container) {
            try {
                Object_1_0 objToRemove = ((RefObject_1_0) value).refDelegate();
                if (objToRemove.objIsPersistent()) {
                    objToRemove.objRemove();
                } 
                else {
                    objToRemove.objMove(null, null);
                }
            } 
            catch (ServiceException e) {
                throw new JmiServiceException(e, this);
            } 
            catch (RuntimeServiceException e) {
                throw new JmiServiceException(e, this);
            }
        }

        // remove reference to object
        else if (values instanceof List || values instanceof Set) {
            try {
                ((Collection<?>) values).remove(value);
            } 
            catch (RuntimeServiceException e) {
                throw new JmiServiceException(e, this);
            }
        } else if (values instanceof SortedMap) {
            try {
                ((SortedMap<?,?>) values).remove(value);
            } 
            catch (RuntimeServiceException e) {
                throw new JmiServiceException(e, this);
            }
        }

        // unsupported
        else {
            throw new JmiServiceException(
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    StackedException.NOT_SUPPORTED,
                    new BasicException.Parameter[] {
                        new BasicException.Parameter("values", values)
                    },
                    "operation only supported for features instanceof [Set|List|SparseArray|Container]"
                ),
                this
            );
        }
    }

    // -------------------------------------------------------------------------
    final public Object_1_0 refDelegate(
    ) {
        return this.object;
    }

    // -------------------------------------------------------------------------
    final public Object refContext(
    ) {
        return ((RefRootPackage_1) this.refOutermostPackage()).refUserContext();
    }

    // -------------------------------------------------------------------------
    // Event Handling
    // -------------------------------------------------------------------------
    final public void refAddEventListener(
        String feature, 
        EventListener listener
    )
        throws ServiceException {
        this.object.objAddEventListener(feature, listener); // TODO marshalling
    }

    // -------------------------------------------------------------------------
    final public void refRemoveEventListener(
        String feature,
        EventListener listener
    ) throws ServiceException {
        this.object.objRemoveEventListener(feature, listener); // TODO marshalling
    }

    // -------------------------------------------------------------------------
    final public EventListener[] refGetEventListeners(
        String feature,
        Class<? extends EventListener> listenerType
    ) throws ServiceException {
        return this.object.objGetEventListeners(feature, listenerType); // TODO marshalling
    }

    // --------------------------------------------------------------------------
    // Implements RefObject_1_1
    // --------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.accessor.jmi.cci.RefObject_1_1#refSetOutermostPackage(javax.jmi.reflect.RefPackage)
     */
    public void refSetOutermostPackage(
        RefPackage newPackage
    ) {
        RefPackage oldPackage = refOutermostPackage(); 
        if (oldPackage != newPackage) {
            if(oldPackage instanceof CachingMarshaller_1_0) {
                ((CachingMarshaller_1_0)oldPackage).evict(this);
            }
            this.refClass = (RefClass_1_0) newPackage.refClass(
                this.refClass.refMofId()
            );
            if(newPackage instanceof Marshaller) try {
                ((Marshaller)newPackage).marshal(this.object);
            } 
            catch (ServiceException exception) {
                throw new JmiServiceException(exception);
            }            
        }
    }

    // --------------------------------------------------------------------------
    // Implements Serializable
    // --------------------------------------------------------------------------

    // --------------------------------------------------------------------------
    /**
     * Save the data of the <tt>Object_1_0</tt> instance to a stream (that is,
     * serialize it).
     * 
     * @serialData The objects data
     */
    private synchronized void writeObject(
        java.io.ObjectOutputStream stream
    ) throws java.io.IOException {
        stream.defaultWriteObject();
    }

    // --------------------------------------------------------------------------
    /**
     * Reconstitute the <tt>Object_1_0</tt> instance from a stream (that is,
     * deserialize it).
     */
    private synchronized void readObject(
        java.io.ObjectInputStream stream
    ) throws java.io.IOException, ClassNotFoundException {
        stream.defaultReadObject();
        ((RefRootPackage_1) refClass.refOutermostPackage()).cache(
            this.object,
            this
        );
    }

    // -------------------------------------------------------------------------
    // Object
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    public String toString(
    ) {
        return getClass().getName() + " delegating to " + this.object;
    }
    
    // -------------------------------------------------------------------------
    public boolean equals(
        Object that
    ) {
        return 
            that instanceof RefObject_1_0 && 
            this.object.equals(((RefObject_1_0) that).refDelegate());
    }
    
    // -------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode(
    ) {
        return this.object.hashCode();
    }

    
    // -------------------------------------------------------------------------
    // Implements PersistenceCapable
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyFields(java.lang.Object,
     *      int[])
     */
    public void jdoCopyFields(
        Object other, 
        int[] fieldNumbers
    ) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsFromObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldConsumer,
     *      java.lang.Object)
     */
    public void jdoCopyKeyFieldsFromObjectId(
        ObjectIdFieldConsumer fm,
        Object oid
    ) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(java.lang.Object)
     */
    public void jdoCopyKeyFieldsToObjectId(
        Object oid
    ) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldSupplier,
     *      java.lang.Object)
     */
    public void jdoCopyKeyFieldsToObjectId(
        ObjectIdFieldSupplier fm, 
        Object oid
    ) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoGetObjectId()
     */
    public Object jdoGetObjectId(
    ) {
        try {
            return this.object.objIsPersistent() 
            ? new Path(this.object.objGetPath()) 
            : null;
        } 
        catch (ServiceException exception) {
            throw new JDOException("Object id retrieval failed", exception);
        }
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoGetPersistenceManager()
     */
    public PersistenceManager jdoGetPersistenceManager(
    ) {
        RefPackage outermostPackage = refOutermostPackage();
        return outermostPackage instanceof RefPackage_1_1 
            ? ((RefPackage_1_1) outermostPackage).refPersistenceManager()
            : null;
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoGetTransactionalObjectId()
     */
    public Object jdoGetTransactionalObjectId(
    ) {
        return this.jdoGetObjectId(); // TODO
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoGetVersion()
     */
    public Object jdoGetVersion(
    ) {
        // TODO Auto-generated method stub
        return null;
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDeleted()
     */
    public boolean jdoIsDeleted(
    ) {
        try {
            return this.object.objIsDeleted();
        } 
        catch (ServiceException exception) {
            throw new JDOException("Object state retrieval failed", exception);
        }
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDetached()
     */
    public boolean jdoIsDetached(
    ) {
        return false;
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDirty()
     */
    public boolean jdoIsDirty(
    ) {
        try {
            return this.object.objIsDirty();
        } 
        catch (ServiceException exception) {
            throw new JDOException("Object state retrieval failed", exception);
        }
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoIsNew()
     */
    public boolean jdoIsNew(
    ) {
        try {
            return this.object.objIsNew();
        } 
        catch (ServiceException exception) {
            throw new JDOException("Object state retrieval failed", exception);
        }
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoIsPersistent()
     */
    public boolean jdoIsPersistent(
    ) {
        try {
            return this.object.objIsPersistent();
        } 
        catch (ServiceException exception) {
            throw new JDOException("Object state retrieval failed", exception);
        }
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoIsTransactional()
     */
    public boolean jdoIsTransactional(
    ) {
        try {
            return this.object.objIsInUnitOfWork();
        } 
        catch (ServiceException exception) {
            throw new JDOException("Object state retrieval failed", exception);
        }
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoMakeDirty(java.lang.String)
     */
    public void jdoMakeDirty(
        String fieldName
    ) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager,
     *      java.lang.Object)
     */
    public PersistenceCapable jdoNewInstance(
        StateManager sm, 
        Object oid
    ) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager)
     */
    public PersistenceCapable jdoNewInstance(
        StateManager sm
    ) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance()
     */
    public Object jdoNewObjectIdInstance(
    ) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance(java.lang.Object)
     */
    public Object jdoNewObjectIdInstance(
        Object o
    ) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideField(int)
     */
    public void jdoProvideField(
        int fieldNumber
    ) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideFields(int[])
     */
    public void jdoProvideFields(
        int[] fieldNumbers
    ) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceField(int)
     */
    public void jdoReplaceField(
        int fieldNumber
    ) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFields(int[])
     */
    public void jdoReplaceFields(
        int[] fieldNumbers
    ) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFlags()
     */
    public void jdoReplaceFlags(
    ) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceStateManager(javax.jdo.spi.StateManager)
     */
    public void jdoReplaceStateManager(
        StateManager sm
    ) throws SecurityException {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);
    }

    // -------------------------------------------------------------------------
    // Instance members
    // -------------------------------------------------------------------------
    private static final String OPENMDX_1_JDO = "This JDO operation is not supported in openMDX 1 compatibility mode";
    private static Model_1_2 model = null;

    private String refMofId = null;
    private transient RefObject metaObject = null;
    private transient ModelElement_1_0 refClassDef = null;

    /**
     * @serial
     */
    private Object_1_0 object;

    /**
     * @serial
     */
    private RefClass_1_0 refClass;
 
}
