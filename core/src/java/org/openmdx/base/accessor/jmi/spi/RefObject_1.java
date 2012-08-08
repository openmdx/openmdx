/*
 * ==================================================================== 
 * Name: $Id: RefObject_1.java,v 1.105 2009/06/09 12:45:17 hburger Exp $ 
 * Description: RefObject_1 class 
 * Revision: $Revision: 1.105 $ 
 * Owner: OMEX AG, Switzerland,
 *        http://www.omex.ch Date: $Date: 2009/06/09 12:45:17 $
 * ====================================================================
 * 
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland All rights reserved.
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
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.jdo.JDOException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;
import javax.jmi.reflect.JmiException;
import javax.jmi.reflect.RefClass;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefFeatured;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.resource.ResourceException;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;

import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.LargeObject_1_0;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefStruct_1_0;
import org.openmdx.base.accessor.jmi.spi.Jmi1ObjectInvocationHandler.StandardMarshaller;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingMap;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.CachingMarshaller_1_0;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.spi.ModelUtils;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

//---------------------------------------------------------------------------
/**
 * Implementation of RefObject_1_0.
 * <p>
 * This implementation supports lightweight serialization. The only member is a
 * handle to the class object.
 */
class RefObject_1
    implements Jmi1Object_1_0, Serializable, PersistenceCapable, org.openmdx.base.persistence.spi.Cloneable<RefObject>
{

    /**
     * Constructor 
     *
     * @param object
     * @param refClass
     */
    public RefObject_1(
        ObjectView_1_0 object, 
        RefClass refClass
    ) {
        try {
            this.object = object;
            RefRootPackage_1 refPackage = (RefRootPackage_1) refClass.refOutermostPackage();
                InteractionSpec objContext = object.getInteractionSpec();
                Object refContext = refPackage.refInteractionSpec();
                if(
                    InteractionSpecs.NULL == objContext || (
                        objContext == null ? refContext == null : objContext.equals(refContext)
                    )
                ){
                    this.refClass = refClass;
                } else {
                    this.refClass = refPackage.refPackage(
                        objContext
                    ).refClass(
                        refClass.refMofId()
                    );
                }
        }
        catch(Exception e) {
            throw new JmiServiceException(e);
        }
    }

    // -------------------------------------------------------------------------

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -276854474114899063L;

    
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};
    private static final String OPENMDX_1_JDO = 
        "This JDO operation is not supported in openMDX 1 compatibility mode";

    private transient RefObject metaObject = null;
    private transient ModelElement_1_0 refClassDef = null;

    /**
     * @serial
     */
    private ObjectView_1_0 object;

    /**
     * @serial
     */
    private RefClass refClass;

    private static final List<String> excludeFromInitialization = Arrays.asList(
        "org:openmdx:base:Aspect:core",
        "org:openmdx:base:Creatable::createdAt",
        "org:openmdx:base:Creatable::rcreatedBy",
        "org:openmdx:base:Removable::removedAt",
        "org:openmdx:base:Removable::removedBy",
        "org:openmdx:state2:DateState:stateValidFrom",
        "org:openmdx:state2:DateState:stateValidTo",
        "org:openmdx:state2:StateCapable:state",
        "org:openmdx:state2:StateCapable:transactionTimeUnique",
        "org:openmdx:state2:StateCapable:validTimeUnique"
    );
 
    protected static final String STATE1_CAPABLE = "org:openmdx:compatibility:state1:StateCapable";
    
    // -------------------------------------------------------------------------
    final private void assertStructuralFeature(
        ModelElement_1_0 elementDef
    ) throws ServiceException {
        if (!this.object.getModel().isStructuralFeatureType(elementDef)) { 
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "model element not of type " + ModelAttributes.STRUCTURAL_FEATURE,
                new BasicException.Parameter("model element", elementDef)
            ); 
        }
    }

    // -------------------------------------------------------------------------
    final private boolean isAttributeOrReferenceStoredAsAttribute(
        ModelElement_1_0 elementDef
    ) throws ServiceException {
        Model_1_0 model = this.object.getModel();
        return 
        !model.isOperationType(elementDef) && 
        (model.isAttributeType(elementDef) || 
            model.referenceIsStoredAsAttribute(elementDef));
    }

    // -------------------------------------------------------------------------
    final private void assertOperation(
        ModelElement_1_0 elementDef
    ) throws ServiceException {
        if (!this.object.getModel().isOperationType(elementDef)) { 
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "model element not of type " + ModelAttributes.OPERATION,
                new BasicException.Parameter("model element", elementDef)
            ); 
        }
    }

    // -------------------------------------------------------------------------
    final private ModelElement_1_0 getType(
        ModelElement_1_0 elementDef
    ) throws ServiceException {
        return this.object.getModel().getElementType(
            elementDef
        );
    }

    // -------------------------------------------------------------------------
    final private ModelElement_1_0 getFeature(
        String featureName
    ) throws ServiceException {
        Model_1_0 model = this.object.getModel();

        // Fully qualified feature name. Lookup in model
        if (featureName.indexOf(':') >= 0) {
            return model.getElement(featureName);
        }
        // Get all features of class and find feature with featureName
        else {
            if(this.refClassDef == null) {
                this.refClassDef = model.getElement(this.refClass.refMofId());
            }
            ModelElement_1_0 feature = model.getFeatureDef(
                this.refClassDef,
                featureName,
                false
            );
            if(feature == null) { 
                if(STATE1_CAPABLE.equals(refClass().refMofId())) {
                    return null;
                }
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "feature not found",
                    new BasicException.Parameter("class name", this.refClass().refMofId()),
                    new BasicException.Parameter("feature", featureName)
                ); 
            }
            return feature;
        }
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
    private final Object getValue(
        ModelElement_1_0 featureDef,
        Object qualifier,
        boolean marshal
    ) throws ServiceException {
        Model_1_0 model = this.object.getModel();
        boolean isReference = model.isReferenceType(featureDef);
        boolean isAttribute = model.isAttributeType(featureDef);
        boolean isReferenceStoredAsAttribute = isReference && model.referenceIsStoredAsAttribute(featureDef);

        if (!isAttribute && !isReference) { 
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "model element not of type " + ModelAttributes.STRUCTURAL_FEATURE,
                new BasicException.Parameter("model element", featureDef)
            ); 
        }

        ModelElement_1_0 type = this.getType(featureDef);
        String qualifiedTypeName = (String) type.objGetValue("qualifiedName");

        /**
         * Attribute or Reference stored as attribute. Don't care about
         * qualifier which can anyway only an index. The caller is responsible
         * to get the required element from the collection.
         */
        if (isAttribute || isReferenceStoredAsAttribute) {
            String multiplicity = ModelUtils.getMultiplicity(featureDef);
            if (qualifier != null) { 
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "qualifier must be null in case of attributes and references stored as attribute",
                    new BasicException.Parameter("feature", featureDef),
                    new BasicException.Parameter("qualifier", qualifier)
                ); 
            }
            // SINGLE_VALUE|OPTIONAL_VALUE
            if (
                Multiplicities.SINGLE_VALUE.equals(multiplicity) || 
                Multiplicities.OPTIONAL_VALUE.equals(multiplicity)
            ) {
                Object value = this.object.objGetValue((String) featureDef.objGetValue("name"));
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
                    return DateTimeMarshaller.getInstance().marshal(value);
                } 
                else if (PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                    return DateMarshaller.getInstance().marshal(value);
                } 
                else if (PrimitiveTypes.ANYURI.equals(qualifiedTypeName)) {
                    return URIMarshaller.getInstance(true).marshal(value);
                } 
                else if (PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                    return DurationMarshaller.getInstance().marshal(value);
                } 
                else if (PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                    return ShortMarshaller.getInstance().marshal(value);
                } 
                else if (PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                    return IntegerMarshaller.getInstance().marshal(value);
                } 
                else if (PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                    return LongMarshaller.getInstance().marshal(value);
                } 
                else if (PrimitiveTypes.DECIMAL.equals(qualifiedTypeName)) {
                    return DecimalMarshaller.getInstance().marshal(value);
                } 
                else if (PrimitiveTypes.BINARY.equals(qualifiedTypeName)) {
                    return value;
                } 
                else if (model.isStructureType(type)) {
                    return refOutermostPackage().refCreateStruct((Record)value);
                } 
                else if (
                    model.isClassType(type) || 
                        PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                ) {
                    return refOutermostPackage().marshal(value);
                } 
                else {
                    return value;
                }
            }
            // STREAM
            else if (Multiplicities.STREAM.equals(multiplicity)) {
                LargeObject_1_0 value = this.object.objGetLargeObject((String) featureDef.objGetValue("name"));
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
                        "unsupported stream type. Supported are [string|binary]",
                        new BasicException.Parameter("feature", featureDef),
                        new BasicException.Parameter("type", type)
                    );
                }
            }
            // LIST
            else if (
                    Multiplicities.LIST.equals(multiplicity) || 
                    Multiplicities.MULTI_VALUE.equals(multiplicity)
            ) {
                List values = this.object.objGetList((String) featureDef.objGetValue("name"));
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
                    return new MarshallingList(DateTimeMarshaller.getInstance(), values);
                } 
                else if (PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                    return new MarshallingList(
                        DateMarshaller.getInstance(),
                        values
                    );
                } 
                else if (PrimitiveTypes.ANYURI.equals(qualifiedTypeName)) {
                    return new MarshallingList(
                        URIMarshaller.getInstance(true),
                        values
                    );
                }                 
                else if (PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                    return new MarshallingList(DurationMarshaller.getInstance(), values);
                } 
                else if (PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                    return new MarshallingList(ShortMarshaller.getInstance(), values);
                } 
                else if (PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                    return new MarshallingList(IntegerMarshaller.getInstance(), values);
                } 
                else if (PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                    return new MarshallingList(
                        LongMarshaller.getInstance(),
                        values);
                } 
                else if (PrimitiveTypes.DECIMAL.equals(qualifiedTypeName)) {
                    return new MarshallingList(DecimalMarshaller.getInstance(), values);
                } 
                else if (
                    model.isStructureType(type) ||
                    model.isClassType(type) || 
                    PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                ) {
                    return new MarshallingList(refOutermostPackage(), values);
                } 
                else {
                    return values;
                }
            }
            // SET
            else if (Multiplicities.SET.equals(multiplicity)) {
                Set values = this.object.objGetSet((String) featureDef.objGetValue("name"));
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
                    return new MarshallingSet(DateTimeMarshaller.getInstance(), values);
                } 
                else if (PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                    return new MarshallingSet(
                        DateMarshaller.getInstance(),
                        values
                    );
                } 
                else if (PrimitiveTypes.ANYURI.equals(qualifiedTypeName)) {
                    return new MarshallingSet(
                        URIMarshaller.getInstance(true),
                        values
                    );
                } 
                else if (PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                    return new MarshallingSet(DurationMarshaller.getInstance(), values);
                } 
                else if (PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                    return new MarshallingSet(
                        ShortMarshaller.getInstance(),
                        values
                    );
                } 
                else if (PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                    return new MarshallingSet(IntegerMarshaller.getInstance(), values);
                } 
                else if (PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                    return new MarshallingSet(
                        LongMarshaller.getInstance(),
                        values
                    );
                } 
                else if (PrimitiveTypes.DECIMAL.equals(qualifiedTypeName)) {
                    return new MarshallingSet(DecimalMarshaller.getInstance(), values);
                } 
                else if (
                    model.isStructureType(type) ||
                    model.isClassType(type) || 
                    PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                ) {
                    return new MarshallingSet(refOutermostPackage(), values);
                } 
                else {
                    return values;
                }
            }
            // SPARSEARRAY
            else if (Multiplicities.SPARSEARRAY.equals(multiplicity)) {
                SortedMap values = this.object.objGetSparseArray((String) featureDef.objGetValue("name"));
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
                    return new MarshallingSortedMap(DateTimeMarshaller.getInstance(), values);
                } 
                else if (PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                    return new MarshallingSortedMap(DateMarshaller.getInstance(), values);
                } 
                else if (PrimitiveTypes.ANYURI.equals(qualifiedTypeName)) {
                    return new MarshallingSortedMap(URIMarshaller.getInstance(true), values);
                } 
                else if (PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                    return new MarshallingSortedMap(DurationMarshaller.getInstance(), values);
                } 
                else if (PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                    return new MarshallingSortedMap(ShortMarshaller.getInstance(), values);
                } 
                else if (PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                    return new MarshallingSortedMap(IntegerMarshaller.getInstance(), values);
                } 
                else if (PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                    return new MarshallingSortedMap(LongMarshaller.getInstance(), values);
                } 
                else if (PrimitiveTypes.DECIMAL.equals(qualifiedTypeName)) {
                    return new MarshallingSortedMap(DecimalMarshaller.getInstance(), values);
                } 
                else if (
                    model.isStructureType(type) ||
                    model.isClassType(type) || 
                    PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                ) {
                    return new MarshallingSortedMap(refOutermostPackage(), values);
                } 
                else {
                    return values;
                }
            }
            // MAP
            else if (Multiplicities.MAP.equals(multiplicity)) {
                FilterableMap values = this.object.objGetContainer((String) featureDef.objGetValue("name"));
                return values == null ? null : new MarshallingMap(
                    refOutermostPackage(),
                    values
                );
            } 
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "unsupported multiplicity. Supported are [set|list|sparsearray|map|stream|0..n|0..1|1..1]",
                    new BasicException.Parameter("feature", featureDef),
                    new BasicException.Parameter("type", type)
                );
            }
        }

        /**
         * Reference (not stored as attribute)
         */
        else if (isReference) {

            String multiplicity = (String) featureDef.objGetValue("multiplicity");
            // Class type qualifier
            if (qualifier instanceof RefObject) {

                /**
                 * Get qualifier of exposing association end. This qualifier is
                 * used to construct the reference filter:
                 */
                String exposedEndName = (String) model.getElement(
                    featureDef.objGetValue("exposedEnd")
                ).objGetValue("name");
                String qualifierName = (String) model.getElement(
                    featureDef.objGetValue("referencedEnd")
                ).objGetValue("qualifierName");
                int pos = 0;
                if (
                    ((pos = qualifierName.indexOf("Container")) >= 0) || 
                    ((pos = qualifierName.indexOf("container")) >= 0)
                ) {
                    qualifierName = qualifierName.substring(0, pos);
                }
                Container_1_0 container = (Container_1_0)((RefObject_1_0)qualifier).refDelegate().objGetContainer(
                    qualifierName
                ).subMap(
                    new FilterProperty[] {
                        new FilterProperty(
                            Quantors.THERE_EXISTS,
                            exposedEndName,
                            FilterOperators.IS_IN,
                            this.object.jdoGetObjectId()
                        )
                    }
                );
                return new RefContainer_1(
                    refOutermostPackage(), 
                    container
                );
            }

            // Primitive type or null qualifier
            else {
                RefRootPackage_1 rootPkg = refOutermostPackage();
                ModelElement_1_0 exposedEnd = model.getElement(
                    featureDef.objGetValue("exposedEnd")
                );
                // navigation to parent object is performed locally by removing
                // the last to object path components
                if(
                    AggregationKind.SHARED.equals(exposedEnd.objGetValue("aggregation")) || 
                    AggregationKind.COMPOSITE.equals(exposedEnd.objGetValue("aggregation"))
                ) {
                    Path objectId = this.refGetPath();
                    return rootPkg.refObject(objectId.getPrefix(objectId.size() - 2));
                } 
                else {
                    if(
                        (qualifier instanceof String) && 
                        (((String) qualifier).indexOf(';') >= 0)
                    ) {
                        return rootPkg.marshal(
                            ((DataObjectManager_1_0) rootPkg.refDelegate()).getObjectById(
                                this.refGetPath().getDescendant(
                                    (String)featureDef.objGetValue("name"), 
                                    (String) qualifier
                                )
                            )
                        );
                    } 
                    else {
                        String feature = (String) featureDef.objGetValue("name");
                        Container_1_0 container = this.object.objGetContainer(feature);
                        Object object = null;
                        try {
                            object = qualifier == null ? 
                                new RefContainer_1(rootPkg, container) : 
                                rootPkg.marshal(container.get(qualifier.toString()));
                        } 
                        catch (ServiceException e) {
                            // in case of 0..1 multiplicity allow null as return value
                            if(
                                (e.getExceptionCode() != BasicException.Code.NOT_FOUND) || 
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

        boolean isAttribute = this.object.getModel().isAttributeType(featureDef);
        if (!isAttribute) { 
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "model element not of type " + ModelAttributes.STRUCTURAL_FEATURE,
                new BasicException.Parameter("model element", featureDef)
            ); 
        }

        ModelElement_1_0 type = this.getType(featureDef);
        String qualifiedTypeName = (String) type.objGetValue("qualifiedName");
        String multiplicity = (String) featureDef.objGetValue("multiplicity");

        // STREAM
        if (Multiplicities.STREAM.equals(multiplicity)) {
            LargeObject_1_0 largeValue = this.object.objGetLargeObject((String) featureDef.objGetValue("name"));
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
                    "unsupported stream type. Supported are [string|binary]",
                    new BasicException.Parameter("feature", featureDef),
                    new BasicException.Parameter("type", type)
                );
            }
        } 
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "unsupported multiplicity. Supported are [stream]",
                new BasicException.Parameter("feature", featureDef),
                new BasicException.Parameter("type", type)
            );
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    final private void setValue(
        ModelElement_1_0 featureDef, 
        Object value
    ) throws ServiceException {
        this.assertStructuralFeature(featureDef);
        
        ModelElement_1_0 type = this.getType(featureDef);
        String qualifiedTypeName = (String) type.objGetValue("qualifiedName");

        /**
         * Attribute or Reference stored as attribute.
         */
        if (this.isAttributeOrReferenceStoredAsAttribute(featureDef)) {
            String multiplicity = (String) featureDef.objGetValue("multiplicity");
            if (this.object.getModel().isReferenceType(featureDef)) {
                ModelElement_1_0 referencedEnd = this.object.getModel().getElement(
                    featureDef.objGetValue("referencedEnd")
                );
                if(!referencedEnd.objGetList("qualifierType").isEmpty()) {
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
                        (String) featureDef.objGetValue("name"), 
                        BooleanMarshaller.getInstance(true).unmarshal(value)
                    );
                } 
                else if (PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) {
                    this.object.objSetValue(
                        (String) featureDef.objGetValue("name"), 
                        DateTimeMarshaller.getInstance().unmarshal(value)
                    );
                } 
                else if (PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                    this.object.objSetValue(
                        (String) featureDef.objGetValue("name"), 
                        DateMarshaller.getInstance().unmarshal(value)
                    );
                } 
                else if (PrimitiveTypes.ANYURI.equals(qualifiedTypeName)) {
                    this.object.objSetValue(
                        (String) featureDef.objGetValue("name"), 
                        URIMarshaller.getInstance(true).unmarshal(value)
                    );
                } 
                else if (PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                    this.object.objSetValue(
                        (String) featureDef.objGetValue("name"), 
                        DurationMarshaller.getInstance().unmarshal(value)
                    );
                } 
                else if (PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                    this.object.objSetValue(
                        (String) featureDef.objGetValue("name"), 
                        ShortMarshaller.getInstance().unmarshal(value)
                    );
                } 
                else if (PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                    this.object.objSetValue(
                        (String) featureDef.objGetValue("name"), 
                        IntegerMarshaller.getInstance().unmarshal(value)
                    );
                } 
                else if (PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                    this.object.objSetValue(
                        (String) featureDef.objGetValue("name"), 
                        LongMarshaller.getInstance().unmarshal(value)
                    );
                } 
                else if (PrimitiveTypes.DECIMAL.equals(qualifiedTypeName)) {
                    this.object.objSetValue(
                        (String) featureDef.objGetValue("name"), 
                        DecimalMarshaller.getInstance().unmarshal(value)
                    );
                } 
                else if (PrimitiveTypes.BINARY.equals(qualifiedTypeName)) {
                    this.object.objSetValue(
                        (String) featureDef.objGetValue("name"), 
                        value
                    );
                } 
                else if (
                    this.object.getModel().isStructureType(type) ||
                    this.object.getModel().isClassType(type)
                ){
                    this.object.objSetValue(
                        (String) featureDef.objGetValue("name"), 
                        refOutermostPackage().unmarshal(value)
                    );
                } 
                else {
                    this.object.objSetValue(
                        (String) featureDef.objGetValue("name"), 
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
                            "unknown multiplicity",
                            new BasicException.Parameter("feature", featureDef),
                            new BasicException.Parameter("multiplicity", multiplicity)
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
                "set supported only for attributes and references stored as attributes",
                new BasicException.Parameter("feature", featureDef),
                new BasicException.Parameter("type", type)
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

        boolean isAttribute = this.object.getModel().isAttributeType(featureDef);
        if (!isAttribute) { 
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "model element not of type " + ModelAttributes.STRUCTURAL_FEATURE,
                new BasicException.Parameter("model element", featureDef)
            ); 
        }

        ModelElement_1_0 type = this.getType(featureDef);
        String qualifiedTypeName = (String) type.objGetValue("qualifiedName");
        String multiplicity = (String) featureDef.objGetValue("multiplicity");

        // STREAM
        if (Multiplicities.STREAM.equals(multiplicity)) {
            LargeObject_1_0 largeValue = this.object.objGetLargeObject((String) featureDef.objGetValue("name"));
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
                    "unsupported stream type. Supported are [string|binary]",
                    new BasicException.Parameter("feature", featureDef),
                    new BasicException.Parameter("type", type)
                );
            }
        } 
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "unsupported multiplicity. Supported are [stream]",
                new BasicException.Parameter("feature", featureDef),
                new BasicException.Parameter("type", type)
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

        SysLog.trace("refMofId", this.object.jdoGetObjectId());
        SysLog.trace("feature", featureDef);
        SysLog.trace("args", args);

        this.assertOperation(featureDef);

        // get the type names of 'in' parameter and 'result'
        String qualifiedNameResultType = null;
        String qualifiedNameInParamType = null;
        for (
            Iterator<?> i = featureDef.objGetList("content").iterator(); 
            i.hasNext();
        ) {
            ModelElement_1_0 paramDef = this.object.getModel().getElement(i.next());
            ModelElement_1_0 paramDefType = this.getType(paramDef);
            if ("in".equals(paramDef.objGetValue("name"))) {
                qualifiedNameInParamType = (String) paramDefType.objGetValue("qualifiedName");
            } 
            else if ("result".equals(paramDef.objGetValue("name"))) {
                qualifiedNameResultType = (String) paramDefType.objGetValue("qualifiedName");
            }
        }
        if (qualifiedNameInParamType == null) { 
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "no parameter with name \"in\" defined for operation",
                new BasicException.Parameter("operation", featureDef)
            ); 
        }
        if (qualifiedNameResultType == null) { 
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "no parameter with name \"result\" defined for operation",
                new BasicException.Parameter("operation", featureDef)
            ); 
        }
        RefPackage_1_0 refPackage = refOutermostPackage();
        RefStruct_1_0 input = (RefStruct_1_0) (
            args.size() == 1 && args.get(0) instanceof RefStruct_1_0 ? args.get(0) :
            this.refOutermostPackage().refCreateStruct(qualifiedNameInParamType,args)
        );   
        RefStruct_1_0 output = (RefStruct_1_0) refPackage.refCreateStruct(
            qualifiedNameResultType, 
            (List<?>)null // output record will be updated by method invocation
        );
        try {
            this.object.execute(
                InteractionSpecs.newMethodInvocationSpec(
                    (String) featureDef.objGetValue("name"),
                    getInteractionVerb(Boolean.TRUE.equals(featureDef.objGetValue("isQuery")))
                ),
                input.refDelegate(),
                output.refDelegate()
              );
        } catch (ResourceException exception) {
            throw new ServiceException(exception);
        }
        return output;
    }

    /**
     * Tells, whether an operation must be invoked immediately or not
     * 
     * @return <code>SYNC_SEND_RECEIVE</code> if an operation must be invoked immediately
     * 
     * @see InteractionSpec.SYNC_SEND_RECEIVE
     * @see InteractionSpec.SYNC_SEND
     */
    private int getInteractionVerb(
        boolean query
    ) {
        return !query && jdoGetPersistenceManager().currentTransaction().getOptimistic() ? 
            InteractionSpec.SYNC_SEND :
            InteractionSpec.SYNC_SEND_RECEIVE;
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
                        BasicException.Code.NOT_SUPPORTED,
                        "operation only supported for features instanceof [List|SparseArray]",
                        new BasicException.Parameter("values", values)
                    ),
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
                        BasicException.Code.NOT_SUPPORTED,
                        "operation only supported for features instanceof [List|SparseArray]",
                        new BasicException.Parameter("value", value)
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
                    BasicException.Code.NOT_SUPPORTED,
                    "index must be 0 in case of a non-collection value",
                    new BasicException.Parameter("feature", featureName),
                    new BasicException.Parameter("value", value)
                ), this
            );
        }
    }

    // -------------------------------------------------------------------------
    /**
     * Feature must be attribute or reference stored as attribute
     */
    @SuppressWarnings({
        "unchecked", "deprecation"
    })
    final protected Object refGetValue(
        String featureName, 
        String qualifier
    ) {
        Object map = null;
        Object value = null;
        try {
            map = this.getValue(this.getFeature(featureName), null);
        } 
        catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
        if (map instanceof Map) {
            value = ((Map<String,?>) map).get(qualifier);
        } 
        else if (map instanceof org.openmdx.base.collection.Container) {
            try {
                value = ((org.openmdx.base.collection.Container) map).get(qualifier);
            } catch (RuntimeServiceException exception) {
                throw new JmiServiceException(exception, this);
            }
        } 
        else {
            throw new JmiServiceException(
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "index must be 0 in case of a non-collection value",
                    new BasicException.Parameter("feature", featureName),
                    new BasicException.Parameter("value", map)
                ), 
                this
            );
        }
        return value;
    }

    // -------------------------------------------------------------------------
    final public Object refGetValue(
        String featureName, 
        Object qualifier
    ) {
        try {
            Model_1_0 model = this.object.getModel();
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
                        ((Integer) IntegerMarshaller.getInstance().unmarshal(qualifier)).intValue()
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
            Object viewContext = refOutermostPackage().refInteractionSpec();
            Object values = viewContext == null ? this.getValue(this.getFeature(featureName), null) : null;

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
                            BasicException.Code.NOT_SUPPORTED,
                            "operation only supported for features instanceof [List|SparseArray]",
                            new BasicException.Parameter("values", values)
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
                        BasicException.Code.NOT_SUPPORTED,
                        "index must be 0 for non-collection values",
                        new BasicException.Parameter("values", values)
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
                    BasicException.Code.NOT_SUPPORTED,
                    "operation only supported for features instanceof [List]",
                    new BasicException.Parameter("values", values)
                ),
                this
            );
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings({
        "unchecked", "deprecation"
    })
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
        else if (values instanceof org.openmdx.base.collection.Container && value instanceof RefObject_1_0) {
            ((org.openmdx.base.collection.Container<RefObject_1_0>) values).add((RefObject_1_0)value);
        } 
        else {
            throw new JmiServiceException(
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "operation only supported for features instanceof [Set|List|SparseArray|Container]",
                    new BasicException.Parameter("value class", values.getClass().getName()),
                    new BasicException.Parameter("values", values)
                ),
                this
            );
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings({"unchecked"})
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
                    BasicException.Code.NOT_SUPPORTED,
                    "operation only supported for features instanceof [List|SparseArray]",
                    new BasicException.Parameter("values", values)
                ),
                this
            );
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings("deprecation")
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
                values instanceof List<?> || 
                values instanceof Set<?> || 
                values instanceof org.openmdx.base.collection.Container<?>
        ) {
            try {
                ((Collection<?>) values).clear();
            } 
            catch (RuntimeServiceException e) {
                throw new JmiServiceException(e, this);
            }
        } 
        else if (values instanceof SortedMap<?,?>) {
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
                    BasicException.Code.NOT_SUPPORTED,
                    "operation only supported for features instanceof [Set|List|SparseArray|Container]",
                    new BasicException.Parameter("values", values)
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
            if (!this.object.getModel().isClassType(objType)) { 
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "objType must be a class type",
                    new BasicException.Parameter("objType", objType)
                ); 
            }
            return this.object.getModel().isInstanceof(this.object, objType);
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
        Path path = this.refGetPath();
        return path == null || path.size() == 1 ? 
            null : 
            refOutermostPackage().refObject(path.getPrefix(path.size() - 2));
    }

    // -------------------------------------------------------------------------
    final public RefFeatured refOutermostComposite(
    ) {
        throw new UnsupportedOperationException();
    }

    // -------------------------------------------------------------------------
    final public void refDelete(
    ) {
        try {
            JDOHelper.getPersistenceManager(this.object).deletePersistent(this.object);
        } 
        catch(Exception e) {
            throw new RuntimeServiceException(e);
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
            ModelElement_1_0 featureDef = this.getFeature(featureName);
            Object value;
            if(featureDef == null && STATE1_CAPABLE.equals(refClass().refMofId())) {
                Container_1_0 container = this.object.objGetContainer(featureName);
                value = new RefContainer_1(refOutermostPackage(), container);
                StandardMarshaller marshaller = ((Jmi1Class_1_0)this.refClass).getMarshaller(); 
                value = marshaller.marshal(value); 
            } else {
                value = this.getValue(featureDef, null);
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
            String exceptionType = e.getCause().getParameter("typeName");

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
                        refOutermostPackage().refBindingPackageSuffix() + "." + typeName
                    );
                    Constructor constructor = exceptionClass.getConstructor(
                        new Class[] {
                            ServiceException.class
                        }
                    );
                    throw (RefException) constructor.newInstance(
                        e
                    );
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
                    this.object.getModel().getElement(this.refClass().refMofId())
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
    final public RefRootPackage_1 refOutermostPackage(
    ) {
        return (RefRootPackage_1) this.refClass().refOutermostPackage();
    }

    // -------------------------------------------------------------------------
    final public String refMofId(
    ) {
        Path identity = this.object.jdoGetObjectId();
        return identity == null ? null : identity.toXRI();
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    final public Collection refVerifyConstraints(
        boolean deepVerify
    ) {
        Collection<ServiceException> violationSource = this.object.getModel().verifyObject(
            this.object,
            deepVerify,
            false // verifyDerived
        );
        if (violationSource == null) {
            return null;
        } 
        else {
            String thisElement = this.object.jdoGetObjectId().toString();
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

    // -------------------------------------------------------------------------
    private String getElementInError(
        ServiceException serviceException
    ) {
        return serviceException.getCause(
        ).getCause(
            BasicException.Code.DEFAULT_DOMAIN
        ).getParameter(
            "elementInError"
        );
    }

    // -------------------------------------------------------------------------
    // RefObject_1_0
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    final public Path refGetPath(
    ) {
        try {
            return this.object.jdoGetObjectId();
        } catch (JDOException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public void refAddToUnitOfWork(
    ) {
        try {
            this.object.objMakeTransactional();
        } catch (ServiceException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public void refRemoveFromUnitOfWork(
    ) {
        try {
            this.object.objMakeNontransactional();
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
            return this.object.jdoIsDirty();
        } catch (JDOException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public boolean refIsPersistent(
    ) {
        try {
            return this.object.jdoIsPersistent();
        } catch (JDOException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public boolean refIsNew(
    ) {
        try {
            return this.object.jdoIsNew();
        } catch (JDOException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    final public boolean refIsDeleted(
    ) {
        try {
            return this.object.jdoIsDeleted();
        } catch (JDOException e) {
            throw new JmiServiceException(e, this);
        }
    }

    // -------------------------------------------------------------------------
    public Set<String> refDefaultFetchGroup(
    ) {
        try {
            // fetch group of object
            Set<String> fetchGroup = this.object.objDefaultFetchGroup();

            // add non-derived features
            RefMetaObject_1 objectDef = (RefMetaObject_1) this.refMetaObject();
            fetchGroup.addAll(this.object.getModel().getAttributeDefs(
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
    @SuppressWarnings("unchecked")
    final public void refInitialize(
        boolean setRequiredToNull,
        boolean setOptionalToNull
    ) {
        try {
            ModelElement_1_0 elementDef = ((RefMetaObject_1)this.refMetaObject()).getElementDef();
            for (
                    Iterator<ModelElement_1_0> i = ((Map) elementDef.objGetValue("allFeature")).values().iterator(); 
                    i.hasNext();
            ) {
                ModelElement_1_0 featureDef = i.next();
                if (
                        this.isAttributeOrReferenceStoredAsAttribute(featureDef) && 
                        ((Boolean) featureDef.objGetValue("isChangeable")).booleanValue() 
                ) {
                    String multiplicity = (String) featureDef.objGetValue("multiplicity");
                    ModelElement_1_0 type = this.object.getModel().getElementType(featureDef);
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
                        this.setValue(featureDef, EMPTY_OBJECT_ARRAY);
                    } 
                    else if (Multiplicities.SINGLE_VALUE.equals(multiplicity)) {
                        if (setRequiredToNull) {
                            this.setValue(featureDef, null);
                        } 
                        else {
                            String qualifiedTypeName = (String) type.objGetValue("qualifiedName");
                            if (PrimitiveTypes.STRING.equals(qualifiedTypeName)) {
                                this.setValue(featureDef, "");
                            } 
                            else if (PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName)) {
                                this.setValue(featureDef, Boolean.FALSE);
                            } 
                            else if (PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) {
                                this.setValue(
                                    featureDef, 
                                    DateTimeMarshaller.getInstance().marshal
                                    (org.openmdx.base.text.format.DateFormat.getInstance().format(new Date())
                                    )
                                );
                            } 
                            else if (PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                                this.setValue(
                                    featureDef, 
                                    DateMarshaller.getInstance().marshal("20000101")
                                );
                            } 
                            else if (PrimitiveTypes.ANYURI.equals(qualifiedTypeName)) {
                                this.setValue(
                                    featureDef, 
                                    URIMarshaller.getInstance(true).marshal("20000101")
                                );
                            } 
                            else if (PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                                this.setValue(
                                    featureDef, 
                                    DurationMarshaller.getInstance().marshal("P0M")
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
                            else if (this.object.getModel().isStructureType(type)) {
                                throw new UnsupportedOperationException(
                                    "initializing of structs not supported"
                                );
                            } 
                            else if (
                                    this.object.getModel().isClassType(type) || 
                                    PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                            ) {
                                SysLog.detail("initializing of object references not supported", featureDef);
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
                Iterator<ModelElement_1_0> i = ((Map) elementDef.objGetValue("allFeature")).values().iterator(); 
                i.hasNext();
            ) {
                ModelElement_1_0 featureDef = i.next();
                if (
                    !RefObject_1.excludeFromInitialization.contains(featureDef.jdoGetObjectId().getBase()) &&
                    this.isAttributeOrReferenceStoredAsAttribute(featureDef) && 
                    Boolean.TRUE.equals(featureDef.objGetValue("isChangeable")) 
                ) {
                    this.setValue(
                        featureDef, 
                        source.refGetValue((String)featureDef.objGetValue("name"))
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
                    if (value instanceof RefObject_1) {
                        ((RefObject_1) value).refSetOutermostPackage(
                            refContainer.refOutermostPackage()
                        );
                    }
                } 
                else {
                    throw new JmiServiceException(
                        new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED,
                            "a) feature type = Reference and qualifier is RefObject_1_0; b) feature type = collection type attribute and qualifier is Number",
                            new BasicException.Parameter(
                                "values", container == null ? null : container.getClass().getName()
                            )
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
            if (
                    this.object.getModel().isReferenceType(feature) &&
                    !this.object.getModel().referenceIsStoredAsAttribute(feature)
            ) {
                String base = qualifier.toString();
                String reference = (String) feature.objGetValue("name");
                if (base.indexOf(';') < 0) {
                    if (this.object.objGetContainer(reference).remove(base) == null) { 
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_FOUND,
                            "Attempt to remove an object with a given qualifier while no object is bound to this qualifier",
                            new BasicException.Parameter("feature", featureName),
                            new BasicException.Parameter("qualifier", base)
                        ); 
                    }
                } 
                else {
                    DataObject_1_0 object = ((DataObject_1_0)((DataObjectManager_1_0) refOutermostPackage().refDelegate()).getObjectById(
                        this.refGetPath().getDescendant(
                            reference, base
                        ))
                    );
                    JDOHelper.getPersistenceManager(object).deletePersistent(object);
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
                (values instanceof List<?>) && 
                (qualifier instanceof Number)
        ) {
            ((List<?>) values).remove(((Number) qualifier).intValue());
        } 
        else if (values instanceof SortedMap<?,?> && qualifier instanceof Number) {
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
                    BasicException.Code.NOT_SUPPORTED,
                    "operation only supported for features instanceof [List|SparseArray|FilterableMap]",
                    new BasicException.Parameter("values", values)
                ),
                this
            );
        }
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings("deprecation")
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
        if (values instanceof org.openmdx.base.collection.Container<?>) {
            try {
                DataObject_1_0 objToRemove = ((RefObject_1_0) value).refDelegate();
                if (objToRemove.jdoIsPersistent()) {
                    JDOHelper.getPersistenceManager(objToRemove).deletePersistent(objToRemove);
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
        else if (values instanceof List<?> || values instanceof Set<?>) {
            try {
                ((Collection<?>) values).remove(value);
            } 
            catch (RuntimeServiceException e) {
                throw new JmiServiceException(e, this);
            }
        } else if (values instanceof SortedMap<?,?>) {
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
                    BasicException.Code.NOT_SUPPORTED,
                    "operation only supported for features instanceof [Set|List|SparseArray|Container]",
                    new BasicException.Parameter("values", values)
                ),
                this
            );
        }
    }

    // -------------------------------------------------------------------------
    final public ObjectView_1_0 refDelegate(
    ) {
        return this.object;
    }

    // -------------------------------------------------------------------------
    /**
     * @deprecated
     */
    final public Object refContext(
    ) {
        return jdoGetPersistenceManager().getUserObject();
    }

    // -------------------------------------------------------------------------
    // Event Handling
    // -------------------------------------------------------------------------
    final public void refAddEventListener(
        EventListener listener
    ){
        try {
            this.object.objAddEventListener(listener);
        } catch (ServiceException exception) {
            throw new JmiServiceException(exception, this);
        }
    }

    // -------------------------------------------------------------------------
    final public void refRemoveEventListener(
        EventListener listener
    ){
        try {
            this.object.objRemoveEventListener(listener);
        } catch (ServiceException exception) {
            throw new JmiServiceException(exception, this);
        }
    }

    // -------------------------------------------------------------------------
    final public <T  extends EventListener> T[] refGetEventListeners(
        Class<T> listenerType
    ){
        try {
            return this.object.objGetEventListeners(listenerType);
        } catch (ServiceException exception) {
            throw new JmiServiceException(exception, this);
        }
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
                ((CachingMarshaller_1_0)oldPackage).evictObject(this);
            }
            this.refClass = newPackage.refClass(
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
        refOutermostPackage().cacheObject(
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
        return this.object.jdoIsPersistent() ? this.object.jdoGetObjectId() : null;
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoGetPersistenceManager()
     */
    public PersistenceManager jdoGetPersistenceManager(
    ) {
        return refOutermostPackage().refPersistenceManager();
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoGetTransactionalObjectId()
     */
    public Object jdoGetTransactionalObjectId(
    ) {
        return this.object.jdoGetTransactionalObjectId();
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoGetVersion()
     */
    public Object jdoGetVersion(
    ) {
        return this.object.jdoGetVersion();
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDeleted()
     */
    public boolean jdoIsDeleted(
    ) {
        return this.object.jdoIsDeleted();
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
        return this.object.jdoIsDirty();
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoIsNew()
     */
    public boolean jdoIsNew(
    ) {
        return this.object.jdoIsNew();
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoIsPersistent()
     */
    public boolean jdoIsPersistent(
    ) {
        return this.object.jdoIsPersistent();
    }

    // -------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoIsTransactional()
     */
    public boolean jdoIsTransactional(
    ) {
        return this.object.jdoIsTransactional();
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

    //--------------------------------------------------------------------
    // Implements Cloneable
    //--------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Cloneable#openmdxjdoClone()
     */
    public RefObject openmdxjdoClone() {
        try {
            return this.refClass().refCreateInstance(
                Collections.singletonList(
                    refDelegate().openmdxjdoClone()
                )
            );
        } catch (Exception exception) {
            throw new JDOUserException(
                "Object could not be cloned",
                exception,
                this
            );
        }
    }
    
    
    // -------------------------------------------------------------------------
    // Implements InstanceCallbacks
    // -------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see javax.jdo.listener.ClearCallback#jdoPreClear()
     */
    public void jdoPreClear() {
        // accept but do not delegate
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.StoreCallback#jdoPreStore()
     */
    public void jdoPreStore() {
        // accept but do not delegate
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.DeleteCallback#jdoPreDelete()
     */
    public void jdoPreDelete() {
        // accept but do not delegate
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.LoadCallback#jdoPostLoad()
     */
    public void jdoPostLoad() {
        // accept but do not delegate
    }

}
