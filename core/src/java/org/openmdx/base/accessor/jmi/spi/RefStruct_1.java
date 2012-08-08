/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefStruct_1.java,v 1.10 2008/11/11 15:37:52 wfro Exp $
 * Description: RefStruct_1 class
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/11 15:37:52 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.jmi.spi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.jmi.reflect.RefObject;

import org.openmdx.base.accessor.generic.cci.StructureFactory_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_4;
import org.openmdx.base.accessor.jmi.cci.RefStruct_1_0;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.code.ModelAttributes;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.model1.code.PrimitiveTypes;

//---------------------------------------------------------------------------
/**
 * RefStruct_1 class
 */
public abstract class RefStruct_1
implements RefStruct_1_0, Serializable {

    //-------------------------------------------------------------------------
    public RefStruct_1(
        RefPackage_1_0 refPackage,
        Structure_1_0 struct
    ) {
        this.refPackage = refPackage;
        this.structure = struct;
    }

    //-------------------------------------------------------------------------
    public RefStruct_1(
        RefPackage_1_0 refPackage,
        RefStruct_1_0 refStruct
    ) {
        this(
            refPackage,
            refStruct.refDelegate()
        );
    }

    //-------------------------------------------------------------------------
    public RefStruct_1(
        String typeName,
        RefPackage_1_0 refPackage,
        List<?> values
    ) {
        this.refPackage = refPackage;
        this.structure = newStructure(
            this.refQualifiedTypeName(),
            refPackage,
            values
        );
    }

    //-------------------------------------------------------------------------
    public RefStruct_1(
        RefPackage_1_0 refPackage,
        Object value
    ) {
        this.refPackage = refPackage;
        if(value instanceof Structure_1_0) {
            this.structure = (Structure_1_0)value;
        }
        else {
            this.structure = newStructure(
                this.refQualifiedTypeName(),
                refPackage,
                (List<?>)value
            );
        }
    }

    //-------------------------------------------------------------------------
    public RefStruct_1(
        String typeName,
        RefPackage_1_0 refPackage,
        Object value
    ) {
        this.refPackage = refPackage;
        if(value instanceof Structure_1_0) {
            this.structure = (Structure_1_0)value;
        }
        else {
            this.structure = newStructure(
                typeName,
                refPackage,
                (List<?>)value
            );
        }
    }

    //-------------------------------------------------------------------------
    final private ModelElement_1_0 getFeature(
        String featureName
    ) throws ServiceException {

        // full-qualified feature name. Lookup in model
        if(featureName.indexOf(':') >= 0) {
            return this.getModel().getElement(featureName);
        }

        // get all features of class and find feature with featureName
        else {
            ModelElement_1_0 feature = this.getModel().getFeatureDef(
                this.getModel().getElement(this.refQualifiedTypeName()),
                featureName,
                false
            );
            if(feature == null) {
                throw new ServiceException (
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "field not found",
                    new BasicException.Parameter [] {
                        new BasicException.Parameter("struct name", this.refQualifiedTypeName()),
                        new BasicException.Parameter("field", featureName)
                    }
                );
            }
            return feature;
        }
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private Structure_1_0 newStructure(
        String typeName,
        RefPackage_1_0 refPackage,
        List<?> structValues
    ) {
        try {
            Model_1_0 model = refPackage.refModel();
            ModelElement_1_0 structDef = model.getElement(typeName);

            List<Object> marshalledValues = new ArrayList<Object>();
            List<String> fieldNames = new ArrayList<String>();

            // unmarshal values (JMI types -> object layer types)
            int ee = 0;
            for(
                    Iterator<?> e = structDef.values("content").iterator();
                    e.hasNext();
                    ee++
            ) {
                ModelElement_1_0 fieldDef = model.getElement(e.next());
                ModelElement_1_0 fieldType = model.getElementType(
                    fieldDef
                );
                fieldNames.add(
                    (String)fieldDef.values("name").get(0)
                );
                String qualifiedTypeName = (String)fieldType.values("qualifiedName").get(0);
                String multiplicity = (String)fieldDef.values("multiplicity").get(0);
                Object v = structValues.get(ee);

                if(
                        Multiplicities.LIST.equals(multiplicity) ||
                        Multiplicities.MULTI_VALUE.equals(multiplicity)
                ) {
                    List<?> values = (List<?>)v;
                    if(PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingList<Object>(
                                    DateTimeMarshaller.getInstance(),
                                    values
                            )
                        );
                    }
                    else if(PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingList<Object>(
                                    DateMarshaller.getInstance(),
                                    values
                            )
                        );
                    }
                    else if(PrimitiveTypes.ANYURI.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingList<Object>(
                                    URIMarshaller.getInstance(false),
                                    values
                            )
                        );
                    }
                    else if(PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingList<Object>(
                                    DurationMarshaller.getInstance(),
                                    values
                            )
                        );
                    }
                    else if(PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingList<Object>(
                                    ShortMarshaller.getInstance(),
                                    values
                            )
                        );
                    }
                    else if(PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingList<Object>(
                                    IntegerMarshaller.getInstance(),
                                    values
                            )
                        );
                    }
                    else if(PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingList<Object>(
                                    LongMarshaller.getInstance(),
                                    values
                            )
                        );
                    }
                    else if(model.isStructureType(fieldType)) {
                        marshalledValues.add(
                            new MarshallingList<Object>(
                                    new StructMarshaller(
                                        qualifiedTypeName,
                                        refPackage,
                                        false
                                    ),
                                    values
                            )
                        );
                    }
                    else if(
                            model.isClassType(fieldType) ||
                            PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                    ) {
                        List<Object> l = new ArrayList<Object>();
                        marshalledValues.add(l);
                        for(
                                Iterator<?> j = values.iterator();
                                j.hasNext();
                        ) {
                            l.add(
                                toPath(j.next())
                            );
                        }
                    }
                    else {
                        marshalledValues.add(values);
                    }
                }
                else if(
                        Multiplicities.SET.equals(multiplicity)
                ) {
                    Set<?> values = v instanceof Set ? (Set<?>)v : Collections.EMPTY_SET;
                    if(PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingSet<Object>(
                                    DateTimeMarshaller.getInstance(),
                                    values
                            )
                        );
                    }
                    else if(PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingSet<Object>(
                                    DateMarshaller.getInstance(),
                                    values
                            )
                        );
                    }
                    else if(PrimitiveTypes.ANYURI.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingSet<Object>(
                                    URIMarshaller.getInstance(false),
                                    values
                            )
                        );
                    }
                    else if(PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingSet<Object>(
                                    DurationMarshaller.getInstance(),
                                    values
                            )
                        );
                    }
                    else if(PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingSet<Object>(
                                    ShortMarshaller.getInstance(),
                                    values
                            )
                        );
                    }
                    else if(PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingSet<Object>(
                                    IntegerMarshaller.getInstance(),
                                    values
                            )
                        );
                    }
                    else if(PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingSet<Object>(
                                    LongMarshaller.getInstance(),
                                    values
                            )
                        );
                    }
                    else if(model.isStructureType(fieldType)) {
                        marshalledValues.add(
                            new MarshallingSet<Object>(
                                    new StructMarshaller(
                                        qualifiedTypeName,
                                        refPackage,
                                        false
                                    ),
                                    values
                            )
                        );
                    }
                    else if(
                            model.isClassType(fieldType) ||
                            PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                    ) {
                        Set<Object> l = new HashSet<Object>();
                        marshalledValues.add(l);
                        for(
                                Iterator<?> i = (values).iterator();
                                i.hasNext();
                        ) {
                            l.add(
                                toPath(i.next())
                            );
                        }
                    }
                    else {
                        marshalledValues.add(values);
                    }
                }
                else if(
                        Multiplicities.SPARSEARRAY.equals(multiplicity)
                ) {
                    SortedMap<Integer,Object> values;
                    if(v instanceof SortedMap) {
                        values = (SortedMap<Integer,Object>)v;
                    } else {
                        SortedMap<Integer,Object> l = new TreeMap<Integer,Object>();
                        int i = 0;
                        for(Object u : (Collection<?>)v) {
                            l.put(Integer.valueOf(i++), u);
                        }
                        values = l;
                    }
                    if(PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingSortedMap(
                                    DateTimeMarshaller.getInstance(),
                                    values
                            )
                        );
                    }
                    else if(PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingSortedMap(
                                    DateMarshaller.getInstance(),
                                    values
                            )
                        );
                    }
                    else if(PrimitiveTypes.ANYURI.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingSortedMap(
                                    URIMarshaller.getInstance(false),
                                    values
                            )
                        );
                    }
                    else if(PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingSortedMap(
                                    DurationMarshaller.getInstance(),
                                    values
                            )
                        );
                    }
                    else if(PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingSortedMap(
                                    ShortMarshaller.getInstance(),
                                    values
                            )
                        );
                    }
                    else if(PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingSortedMap(
                                    IntegerMarshaller.getInstance(),
                                    values
                            )
                        );
                    }
                    else if(PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                        marshalledValues.add(
                            new MarshallingSortedMap(
                                    LongMarshaller.getInstance(),
                                    values
                            )
                        );
                    }
                    else if(model.isStructureType(fieldType)) {
                        marshalledValues.add(
                            new MarshallingSortedMap(
                                    new StructMarshaller(
                                        qualifiedTypeName,
                                        refPackage,
                                        false
                                    ),
                                    values
                            )
                        );
                    }
                    else if(
                            model.isClassType(fieldType) ||
                            PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                    ) {
                        SortedMap<Object,Object> l = new TreeMap<Object,Object>();
                        marshalledValues.add(l);
                        for(Map.Entry<Integer,?> j : values.entrySet()) {
                            l.put(j.getKey(), toPath(j.getValue()));
                        }
                    }
                    else {
                        marshalledValues.add(values);
                    }
                }
                else if(
                        Multiplicities.SINGLE_VALUE.equals(multiplicity) ||
                        Multiplicities.OPTIONAL_VALUE.equals(multiplicity)
                ) {
                    Object value = v == null
                    ? null
                        : v instanceof Collection
                        ? ((Collection<?>)v).size() > 0
                            ? ((Collection<?>)v).iterator().next()
                                : null
                                : v;
                        if(PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) {
                            marshalledValues.add(
                                DateTimeMarshaller.getInstance().marshal(value)
                            );
                        }
                        else if(PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                            marshalledValues.add(
                                DateMarshaller.getInstance().marshal(value)
                            );
                        }
                        else if(PrimitiveTypes.ANYURI.equals(qualifiedTypeName)) {
                            marshalledValues.add(
                                URIMarshaller.getInstance(false).marshal(value)
                            );
                        }
                        else if(PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                            marshalledValues.add(
                                DurationMarshaller.getInstance().marshal(value)
                            );
                        }
                        else if(PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                            marshalledValues.add(
                                ShortMarshaller.getInstance().marshal(value)
                            );
                        }
                        else if(PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                            marshalledValues.add(
                                IntegerMarshaller.getInstance().marshal(value)
                            );
                        }
                        else if(PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                            marshalledValues.add(
                                LongMarshaller.getInstance().marshal(value)
                            );
                        }
                        else if(model.isStructureType(fieldType)) {
                            marshalledValues.add(
                                new StructMarshaller(
                                    qualifiedTypeName,
                                    refPackage,
                                    false
                                ).marshal(value)
                            );
                        }
                        else if(
                                model.isClassType(fieldType) ||
                                PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                        ) {
                            if(value instanceof Collection) {
                                marshalledValues.add(
                                    ((Collection<?>)value).size() > 0
                                    ? toPath(((Collection<?>)value).iterator().next())
                                        : null
                                );
                            }
                            else {
                                marshalledValues.add(
                                    toPath(value)
                                );
                            }
                        }
                        else {
                            marshalledValues.add(value);
                        }
                }
                else {
                    throw new ServiceException (
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "unsupported multiplicity. Supported are [set|list|sparsearray|0..n|0..1|1..1]",
                        new BasicException.Parameter [] {
                            new BasicException.Parameter("field", fieldDef),
                            new BasicException.Parameter("type", fieldType)
                        }
                    );
                }
            }
            StructureFactory_1_0 structureFactory = hasLegacyDelegate() ?
                this.refPackage.refObjectFactory() :
                    (StructureFactory_1_0) ((RefPackage_1_4)this.refPackage).getDelegate();
                return structureFactory.createStructure(
                    typeName,
                    fieldNames,
                    marshalledValues
                );
        } catch (ServiceException exception) {
            throw new JmiServiceException(exception);
        }
    }

    protected boolean hasLegacyDelegate(
    ){
        return 
        !(this.refPackage instanceof RefPackage_1_4) ||
        ((RefPackage_1_4)this.refPackage).hasLegacyDelegate();
    }

    //-------------------------------------------------------------------------
    protected String refQualifiedTypeName(
    ) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    public Model_1_0 getModel(
    ) {
        return this.refPackage.refModel();
    }

    //-------------------------------------------------------------------------
    private void assertStructureField(
        ModelElement_1_0 elementDef
    ) throws ServiceException {
        if(!this.getModel().isStructureFieldType(elementDef)) {
            throw new ServiceException (
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "model element not of type " + ModelAttributes.STRUCTURE_FIELD,
                new BasicException.Parameter [] {
                    new BasicException.Parameter("model element", elementDef)
                }
            );
        }
    }

    //-------------------------------------------------------------------------
    private ModelElement_1_0 getType(
        ModelElement_1_0 elementDef
    ) throws ServiceException {
        return this.getModel().getElementType(
            elementDef
        );
    }

    //-------------------------------------------------------------------------
    private Marshaller toRefStructMarshaller(
        String typeName
    ) {
        return new StructMarshaller(
            typeName,
            this.refPackage,
            true
        );
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private Object getValue(
        ModelElement_1_0 fieldDef
    ) throws ServiceException {

        SysLog.trace("field", fieldDef);

        this.assertStructureField(fieldDef);

        ModelElement_1_0 type = this.getType(fieldDef);
        String qualifiedTypeName = (String)type.values("qualifiedName").get(0);
        String multiplicity = (String)fieldDef.values("multiplicity").get(0);
        Object v = this.structure.objGetValue(
            (String)fieldDef.values("name").get(0)
        );
        if(
                Multiplicities.LIST.equals(multiplicity) ||
                Multiplicities.MULTI_VALUE.equals(multiplicity)
        ) {
            List<?> values = (List<?>)v;
            if(PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) {
                return new MarshallingList<Object>(
                        DateTimeMarshaller.getInstance(),
                        values
                );
            }
            else if(PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                return new MarshallingList<Object>(
                        DateMarshaller.getInstance(),
                        values
                );
            }
            else if(PrimitiveTypes.ANYURI.equals(qualifiedTypeName)) {
                return new MarshallingList<Object>(
                        URIMarshaller.getInstance(true),
                        values
                );
            }
            else if(PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                return new MarshallingList<Object>(
                        DurationMarshaller.getInstance(),
                        values
                );
            }
            else if(PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                return new MarshallingList<Object>(
                        ShortMarshaller.getInstance(),
                        values
                );
            }
            else if(PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                return new MarshallingList<Object>(
                        IntegerMarshaller.getInstance(),
                        values
                );
            }
            else if(PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                return new MarshallingList<Object>(
                        LongMarshaller.getInstance(),
                        values
                );
            }
            else if(this.getModel().isStructureType(type)) {
                return new MarshallingList<Object>(
                        this.toRefStructMarshaller(qualifiedTypeName),
                        values
                );
            }
            else if(
                    this.getModel().isClassType(type) ||
                    PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
            ) {
                return new MarshallingList<Object>(
                        (Marshaller)this.refPackage.refOutermostPackage(),
                        values
                );
            }
            else {
                return values;
            }
        }
        else if(
                Multiplicities.SET.equals(multiplicity)
        ) {
            Set<?> values = v instanceof Set ? (Set<?>)v : new HashSet<Object>((List<?>)v);
            if(PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) {
                return new MarshallingSet<Object>(
                        DateTimeMarshaller.getInstance(),
                        values
                );
            }
            else if(PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                return new MarshallingSet<Object>(
                        DateMarshaller.getInstance(),
                        values
                );
            }
            else if(PrimitiveTypes.ANYURI.equals(qualifiedTypeName)) {
                return new MarshallingSet<Object>(
                        URIMarshaller.getInstance(true),
                        values
                );
            }
            else if(PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                return new MarshallingSet<Object>(
                        DurationMarshaller.getInstance(),
                        values
                );
            }
            else if(PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                return new MarshallingSet<Object>(
                        ShortMarshaller.getInstance(),
                        values
                );
            }
            else if(PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                return new MarshallingSet<Object>(
                        IntegerMarshaller.getInstance(),
                        values
                );
            }
            else if(PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                return new MarshallingSet<Object>(
                        LongMarshaller.getInstance(),
                        values
                );
            }
            else if(this.getModel().isStructureType(type)) {
                return new MarshallingSet<Object>(
                        this.toRefStructMarshaller(qualifiedTypeName),
                        values
                );
            }
            else if(
                    this.getModel().isClassType(type) ||
                    PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
            ) {
                return new MarshallingSet<Object>(
                        (Marshaller)this.refPackage.refOutermostPackage(),
                        values
                );
            }
            else {
                return values;
            }
        }
        else if(
                Multiplicities.SPARSEARRAY.equals(multiplicity)
        ) {
            SortedMap<Integer,Object> values;
            if(v instanceof SortedMap) {
                values = (SortedMap<Integer,Object>)v;
            } else {
                TreeMap<Integer,Object> j = new TreeMap<Integer,Object>();
                int i=0;
                for(Object k : (Collection<?>)v){
                    j.put(Integer.valueOf(i++), k);
                }
                values = j;
            }
            if(PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) {
                return new MarshallingSortedMap(
                        DateTimeMarshaller.getInstance(),
                        values
                );
            }
            else if(PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                return new MarshallingSortedMap(
                        DateMarshaller.getInstance(),
                        values
                );
            }
            else if(PrimitiveTypes.ANYURI.equals(qualifiedTypeName)) {
                return new MarshallingSortedMap(
                        URIMarshaller.getInstance(true),
                        values
                );
            }
            else if(PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                return new MarshallingSortedMap(
                        DurationMarshaller.getInstance(),
                        values
                );
            }
            else if(PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                return new MarshallingSortedMap(
                        ShortMarshaller.getInstance(),
                        values
                );
            }
            else if(PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                return new MarshallingSortedMap(
                        IntegerMarshaller.getInstance(),
                        values
                );
            }
            else if(PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                return new MarshallingSortedMap(
                        LongMarshaller.getInstance(),
                        values
                );
            }
            else if(this.getModel().isStructureType(type)) {
                return new MarshallingSortedMap(
                        this.toRefStructMarshaller(qualifiedTypeName),
                        values
                );
            }
            else if(
                    this.getModel().isClassType(type) ||
                    PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
            ) {
                return new MarshallingSortedMap(
                        (Marshaller)this.refPackage.refOutermostPackage(),
                        values
                );
            }
            else {
                return values;
            }
        }
        else if(
                Multiplicities.SINGLE_VALUE.equals(multiplicity) ||
                Multiplicities.OPTIONAL_VALUE.equals(multiplicity)
        ) {
            Object value = v == null
            ? null
                : v instanceof Collection
                ? ((Collection<?>)v).size() > 0
                    ? ((Collection<?>)v).iterator().next()
                        : null
                        : v;
                if(PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) {
                    return DateTimeMarshaller.getInstance().marshal(value);
                }
                else if(PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                    return DateMarshaller.getInstance().marshal(value);
                }
                else if(PrimitiveTypes.ANYURI.equals(qualifiedTypeName)) {
                    return URIMarshaller.getInstance(true).marshal(value);
                }
                else if(PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                    return DurationMarshaller.getInstance().marshal(value);
                }
                else if(PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                    return ShortMarshaller.getInstance().marshal(value);
                }
                else if(PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                    return IntegerMarshaller.getInstance().marshal(value);
                }
                else if(PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                    return LongMarshaller.getInstance().marshal(value);
                }
                else if(this.getModel().isStructureType(type)) {
                    return this.toRefStructMarshaller(qualifiedTypeName).marshal(value);
                }
                else if(
                        this.getModel().isClassType(type) ||
                        PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                ) {
                    if(value instanceof Collection) {
                        return ((Collection<?>)value).size() > 0
                        ? ((Marshaller)this.refPackage.refOutermostPackage()).marshal(((Collection<?>)value).iterator().next())
                            : null;
                    }
                    else {
                        return ((Marshaller)this.refPackage.refOutermostPackage()).marshal(value);
                    }
                }
                else {
                    return value;
                }
        }
        else {
            throw new ServiceException (
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "unsupported multiplicity. Supported are [set|list|sparsearray|0..n|0..1|1..1]",
                new BasicException.Parameter [] {
                    new BasicException.Parameter("field", fieldDef),
                    new BasicException.Parameter("type", type)
                }
            );
        }
    }

    //-------------------------------------------------------------------------
    // RefStructure_1_0
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    public Structure_1_0 refDelegate(
    ) {
        return this.structure;
    }

    //-------------------------------------------------------------------------
    public boolean refContainsValue(
        String fieldName,
        Object value
    ) {
        Object values = null;
        try {
            values = this.getValue(
                this.getFeature(fieldName)
            );
        }
        catch(ServiceException e) {
            throw new JmiServiceException(e, null);
        }
        if(values instanceof Collection) {
            return ((Collection<?>)values).contains(value);
        }
        else {
            return values.equals(value);
        }
    }

    //-------------------------------------------------------------------------
    // RefStruct
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    /**
     * The returned value is retrieved and returned from the delegate without
     * checking the type against the model assuming that the delegate is
     * in a consistent state.
     */
    public Object refGetValue(
        String fieldName
    ) {
        Object values = null;
        try {
            values = this.getValue(
                this.getFeature(fieldName)
            );
        }
        catch(ServiceException e) {
            throw new JmiServiceException(e, null);
        }
        return values;
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Object refGetValue(
        String fieldName,
        int index
    ) {
        try {
            Object values = this.getValue(
                this.getFeature(fieldName)
            );
            if(!(values instanceof Collection)) {
                if(index > 0) {
                    throw new ServiceException (
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "can not get element of non-collection value with index > 0",
                        new BasicException.Parameter("fieldName", fieldName),
                        new BasicException.Parameter("value", values),
                        new BasicException.Parameter("index", index)
                    );
                }
                return values;
            }
            else if(values instanceof Set) {
                throw new ServiceException (
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "indexed access to value of type set not supported",
                    new BasicException.Parameter("fieldName", fieldName),
                    new BasicException.Parameter("value", values),
                    new BasicException.Parameter("index", index)
                );
            }
            else if(values instanceof List) {
                return ((List<?>)values).get(index);
            }
            else if(values instanceof SortedMap) {
                return ((SortedMap<Integer,?>)values).get(Integer.valueOf(index));
            }
            else {
                throw new ServiceException (
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "unsupported collection class",
                    new BasicException.Parameter("fieldName", fieldName),
                    new BasicException.Parameter("value", values),
                    new BasicException.Parameter("value class", values.getClass().getName()),
                    new BasicException.Parameter("index", index)
                );
            }
        }
        catch(ServiceException e) {
            throw new JmiServiceException(e, null);
        }
    }

    //-------------------------------------------------------------------------
    public Object refGetValue(
        RefObject field
    ) {
        Object values = null;
        try {
            values = this.getValue(
                (ModelElement_1_0)field
            );
        }
        catch(ServiceException e) {
            throw new JmiServiceException(e, null);
        }
        return values;
    }

    //-------------------------------------------------------------------------
    /**
     * For openMDX 2 JMI mapping
     */
    public List<String> refTypeName(
    ) {
        List<String> typeName = new ArrayList<String>();
        for(
                Enumeration<Object> e = new StringTokenizer(this.refQualifiedTypeName(), ":");
                e.hasMoreElements();
        ) typeName.add((String)e.nextElement());
        return Collections.unmodifiableList(typeName);
    }


    //-------------------------------------------------------------------------
    public boolean equals(
        Object that
    ) {
        return that instanceof RefStruct_1 && super.equals(that);
    }

    //-------------------------------------------------------------------------
    public List<String> refFieldNames(
    ) {
        return this.structure.objFieldNames();
    }

    //-------------------------------------------------------------------------
    protected Object toPath(
        Object source
    ){
        return source instanceof RefObject_1_0 ?
            ((RefObject_1_0)source).refGetPath() :
                source;
    }

    protected RefObject toRefObject(
        String refMofId
    ){
        return this.refPackage.refObject(refMofId);
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private RefPackage_1_0 refPackage;
    private Structure_1_0 structure;

}

//--- End of File -----------------------------------------------------------
