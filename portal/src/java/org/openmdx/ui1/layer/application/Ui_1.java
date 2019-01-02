/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: Ui_1 plugin
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */
package org.openmdx.ui1.layer.application;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.resource.ResourceException;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.base.rest.spi.AbstractRestPort;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.log.SysLog;

/**
 * Ui_1 application plug-in.
 * 
 */
public class Ui_1 extends AbstractRestPort {

    /**
     * Constructor 
     *
     * @throws ServiceException
     */
    public Ui_1(
    ) throws ServiceException {
        this.editObjectOperationDef = this.createBasicObjectOperationDef(EDIT_OBJECT_OPERATION_NAME, false);
        this.deleteObjectOperationDef = this.createBasicObjectOperationDef(DELETE_OBJECT_OPERATION_NAME, false);
        this.reloadObjectOperationDef = this.createBasicObjectOperationDef(RELOAD_OBJECT_OPERATION_NAME, true);
        this.navigateToParentOperationDef = this.createBasicObjectOperationDef(NAVIGATE_TO_PARENT_OPERATION_NAME, true);
    }

	/* (non-Javadoc)
	 * @see org.openmdx.base.resource.spi.Port#getInteraction(javax.resource.cci.Connection)
	 */
	@Override
    public Interaction getInteraction(
        RestConnection connection
    ) throws ResourceException {
        return new RestInteraction(
        	connection
        );
    }
 
	/**
	 * Retrieve changableDefaultValue.
	 *
	 * @return Returns the changableDefaultValue.
	 */
	public boolean isChangableDefaultValue(
	) {
		return this.changableDefaultValue;
	}

	/**
	 * Set changableDefaultValue.
	 * 
	 * @param changableDefaultValue The changableDefaultValue to set.
	 */
	public void setChangableDefaultValue(
		boolean changableDefaultValue
	) {
		this.changableDefaultValue = changableDefaultValue;
	}
	
    /**
     * FeatureDefinition
     *
     */
    private abstract class FeatureDefinition {

        public FeatureDefinition(
            String name,
            String qualifiedName,
            ModelElement_1_0 container
        ) {
            this.name = name;
            this.qualifiedName = qualifiedName;
            this.container = container;
            this.modelElement = null;
        }

        public FeatureDefinition(
            ModelElement_1_0 element
        ) throws ServiceException {
            this.name = (String)element.getName();
            this.qualifiedName = (String)element.getQualifiedName();
            this.container = element.getModel().getElement(element.getContainer());
            this.modelElement = element;
        }

        public String getName(
        ) {
            return this.name;
        }

        public String getQualifiedName(
        ) {
            return this.qualifiedName;
        }

        public ModelElement_1_0 getModelElement(
        ) {
            return this.modelElement;
        }

        public ModelElement_1_0 getContainer(
        ) {
            return this.container;
        }

        protected final String name;
        protected final String qualifiedName;
        protected final ModelElement_1_0 container;
        protected final ModelElement_1_0 modelElement;

    }

    /**
     * StructuralFeatureDefinition
     *
     */
    private class StructuralFeatureDefinition extends Ui_1.FeatureDefinition {

        public StructuralFeatureDefinition(
            String name,
            String qualifiedName,
            ModelElement_1_0 type,
            ModelElement_1_0 container,
            String multiplicity,
            Boolean isChangeable,
            Boolean isReference
        ) {
            super(
                name,
                qualifiedName,
                container
            );
            this.type = type;
            this.multiplicity = multiplicity;
            this.isChangeable = isChangeable;
            this.isReference = Boolean.TRUE.equals(isReference);
            this.isReferenceStoredAsAttribute = false;
        }

        public StructuralFeatureDefinition(
            ModelElement_1_0 element
        ) throws ServiceException {
            super(element);
            Model_1_0 model = Model_1Factory.getModel();
            this.type = element.getType() == null ? null : model.getElement(element.getType());
            this.multiplicity = (String)element.getMultiplicity();
            this.isChangeable = element.isChangeable() == null || element.objGetValue("isChangeable") == null 
            	? new Boolean(Ui_1.this.changableDefaultValue) 
            	: element.isChangeable();
            if(model.isAttributeType(element) || model.isStructureFieldType(element)) {
                this.isReference = false;
                this.isReferenceStoredAsAttribute = false;
            } else if(model.isReferenceType(element)) {
                ModelElement_1_0 referencedEnd = model.getElement(element.getReferencedEnd());
                ModelElement_1_0 exposedEnd = model.getElement(element.getExposedEnd());
                // A reference is handled as attribute in case of a single-valued reference with aggregation=none
                this.isReference = 
                    !referencedEnd.objGetList("qualifierName").isEmpty() || 
                    !exposedEnd.objGetList("qualifierName").isEmpty();
                this.isReferenceStoredAsAttribute = model.referenceIsStoredAsAttribute(element);
            } else {
                this.isReference = true;
                this.isReferenceStoredAsAttribute = false;
            }
        }

        public String getMultiplicity(
        ) {
            return this.multiplicity;
        }

        public Boolean isChangeable(
        ) {
            return this.isChangeable;
        }

        public ModelElement_1_0 getType(
        ) {
            return this.type;
        }

        public boolean isReference(
        ) {
            return this.isReference;
        }
		/**
		 * @return the isReferenceStoredAsAttribute
		 */
		public boolean isReferenceStoredAsAttribute() {
			return isReferenceStoredAsAttribute;
		}

        private final ModelElement_1_0 type;
        private final String multiplicity;
        private final Boolean isChangeable;
        private final boolean isReference;
        private final boolean isReferenceStoredAsAttribute;

    }

    /**
     * OperationDefinition
     *
     */
    private class OperationDefinition extends Ui_1.FeatureDefinition {

        public OperationDefinition(
            String name,
            String qualifiedName,
            boolean isQuery,
            ModelElement_1_0 container
        ) {
            super(
                name,
                qualifiedName,
                container
            );
            this.isQuery = isQuery;
        }

        public OperationDefinition(
            ModelElement_1_0 element
        ) throws ServiceException {
            super(element);
            this.isQuery = ((Boolean)element.objGetValue("isQuery")).booleanValue();
        }

        private final boolean isQuery;

    }

    private void setElementDefault(
    	MappedRecord f,
        List<Object> toolTip,
        boolean isChangeable,
        String iconKey 
    ) throws ServiceException {
    	Object_2Facade facade;
        try {
	        facade = Object_2Facade.newInstance(f);
        } catch (ResourceException e) {
        	throw new ServiceException(e);
        }
    	facade.attributeValuesAsList("toolTip").addAll(toolTip);
    	facade.attributeValuesAsList("changeable").add(new Boolean(isChangeable));
    	facade.attributeValuesAsList("autoGenerated").add(new Boolean(true));
        if(iconKey != null) {
        	facade.attributeValuesAsList("iconKey").add(iconKey);
        }
    }

    /**
     * Set default values for abstract field.
     * 
     * @param f
     * @param toolTip
     * @param isChangeable
     * @param iconKey
     * @param color
     * @param backColor
     * @throws ServiceException
     */
    private void setAbstractFieldDefault(
    	MappedRecord f,
        List<Object> toolTip,
        boolean isChangeable,
        String iconKey,
        String color,
        String backColor,
        String cssClassFieldGroup,
        String cssClassObjectContainer
    ) throws ServiceException {
        this.setElementDefault(
            f,
            toolTip,
            isChangeable,
            iconKey
        );
    	Object_2Facade facade = Facades.asObject(f);
        if(color != null) {
        	facade.attributeValuesAsList("color").add(color);
        }
        if(backColor != null) {
        	facade.attributeValuesAsList("backColor").add(backColor);
        }
        facade.attributeValuesAsList("fontName").add("Tahoma");
        facade.attributeValuesAsList("fontSize").add(new BigDecimal(8.25));
        facade.attributeValuesAsList("cssClassFieldGroup").add(cssClassFieldGroup);
        facade.attributeValuesAsList("cssClassObjectContainer").add(cssClassObjectContainer);        
    }

    /**
     * Set default values for labelled field.
     * 
     * @param f
     * @param labels
     * @param shortLabels
     * @param toolTips
     * @param isChangeable
     * @param iconKey
     * @param color
     * @param backColor
     * @param cssClassFieldGroup
     * @param cssClassObjectContainer
     * @throws ServiceException
     */
    private void setLabelledFieldDefault(
    	MappedRecord f,
        List<Object> labels,
        List<Object> shortLabels,
        List<Object> toolTips,
        boolean isChangeable,
        String iconKey,
        String color,
        String backColor,
        String cssClassFieldGroup,
        String cssClassObjectContainer
    ) throws ServiceException {
        List<Object> toolTipDefault = new ArrayList<Object>(toolTips);
        for(int i = 0; i < toolTipDefault.size(); i++) {
            // Set label as default toolTip
            if(
                ((toolTipDefault.get(i) == null) || ((String)toolTipDefault.get(i)).isEmpty()) &&
                (i < labels.size())
            ) {
                toolTipDefault.set(
                    i,
                    labels.get(i)
                );
            }
        }
        List<Object> shortLabelDefault = new ArrayList<Object>(shortLabels);
        for(int i = 0; i < shortLabelDefault.size(); i++) {
            // Set label as default shortLabel
            if(
                ((shortLabelDefault.get(i) == null) || ((String)shortLabelDefault.get(i)).isEmpty()) &&
                (i < labels.size())
            ) {
                shortLabelDefault.set(
                    i,
                    labels.get(i)
                );
            }
        }
        this.setAbstractFieldDefault(
            f,
            toolTipDefault,
            isChangeable,
            iconKey,
            color,
            backColor,
            cssClassFieldGroup,
            cssClassObjectContainer
        );
    	Object_2Facade facade = Facades.asObject(f);
        facade.attributeValuesAsList("label").addAll(labels);
        facade.attributeValuesAsList("shortLabel").addAll(shortLabelDefault);
    }

    private void setValuedFieldDefault(
    	MappedRecord f,
        List<Object> labels,
        List<Object> shortLabels,
        List<Object> displayValueExprs,
        List<Object> toolTips,
        boolean isChangeable,
        String iconKey,
        String color,
        String backColor,
        String cssClassFieldGroup,
        String cssClassObjectContainer,
        String multiplicity,
        Integer spanRow,
        Integer skipRow,
        boolean filterable,
        boolean sortable,
        boolean mandatory,
        String featureName,
        String qualifiedFeatureName,
        String dataBindingName
    ) throws ServiceException {
        this.setLabelledFieldDefault(
            f,
            labels,
            shortLabels,
            toolTips,
            isChangeable,
            iconKey,
            color,
            backColor,
            cssClassFieldGroup,
            cssClassObjectContainer
        );
    	Object_2Facade facade = Facades.asObject(f);
        facade.attributeValuesAsList("multiplicity").add(multiplicity);
        facade.attributeValuesAsList("spanRow").add(spanRow);
        facade.attributeValuesAsList("skipRow").add(skipRow);
        facade.attributeValuesAsList("displayValueExpr").addAll(displayValueExprs);
        facade.attributeValuesAsList("filterable").add(new Boolean(filterable));
        facade.attributeValuesAsList("sortable").add(new Boolean(sortable));
        facade.attributeValuesAsList("mandatory").add(new Boolean(mandatory));
        facade.attributeValuesAsList("featureName").add(featureName);
        facade.attributeValuesAsList("qualifiedFeatureName").add(qualifiedFeatureName);
        if(dataBindingName != null) {
        	facade.attributeValuesAsList("dataBindingName").add(dataBindingName);
        }
    }

    private void setObjectReference(
    	MappedRecord objectReference,
        StructuralFeatureDefinition feature
    ) throws ServiceException {
    	Object_2Facade objectReferenceFacade;
        try {
        	objectReferenceFacade = Object_2Facade.newInstance(objectReference);
        } catch (ResourceException e) {
        	throw new ServiceException(e);
        }                            	
        Model_1_0 model = Model_1Factory.getModel();
        ModelElement_1_0 featureType = model.getDereferencedType(
            feature.getType()
        );
        objectReferenceFacade.attributeValuesAsList("referenceName").add(feature.getName());
        objectReferenceFacade.attributeValuesAsList("referencedTypeName").add(featureType.getQualifiedName());
        // Reference
        if(feature.isReference()) {
        	objectReferenceFacade.attributeValuesAsList("referenceIsStoredAsAttribute").add(
                Boolean.valueOf(feature.isReferenceStoredAsAttribute())
            );
            ModelElement_1_0 element = feature.getModelElement();
            if(element != null) {
                ModelElement_1_0 referencedEnd = model.getElement(
                    element.getReferencedEnd()
                );
                if(!referencedEnd.objGetList("qualifierName").isEmpty()) {
                    String qualifierName = (String)referencedEnd.objGetList("qualifierName").get(0);           
                    objectReferenceFacade.attributeValuesAsList("userDefinedQualifier").add(
                        new Boolean(!"id".equals(qualifierName))
                    );
                    objectReferenceFacade.attributeValuesAsList("qualifierLabel").add(qualifierName);
                } else {
                	objectReferenceFacade.attributeValuesAsList("userDefinedQualifier").add(
                        Boolean.FALSE
                    );
                }
            } else {
            	objectReferenceFacade.attributeValuesAsList("userDefinedQualifier").add(
                    Boolean.FALSE
                );                
            }
        } else {
            // Attribute of type class
        	objectReferenceFacade.attributeValuesAsList("referenceIsStoredAsAttribute").add(
                Boolean.TRUE
            );
        	objectReferenceFacade.attributeValuesAsList("userDefinedQualifier").add(
                Boolean.FALSE
            );
        }
    }

    private int compareOrder(
        List<?> o1,
        List<?> o2
    ) throws ServiceException {
        for(
            int i = 0;
            i < java.lang.Math.min(o1.size(), o2.size());
            i++
        ) {
            int res = ((Integer)o1.get(i)).compareTo((Integer)o2.get(i));
            if(res != 0) {
                return res;
            }
        }
        return new Integer(o1.size()).compareTo(new Integer(o2.size()));
    }

    private List<?> getOrderFieldGroup(
    	MappedRecord elementDefinition
    ) throws ServiceException {
    	Object_2Facade elementDefinitionFacade;
        try {
	        elementDefinitionFacade = Object_2Facade.newInstance(elementDefinition);
        } catch (ResourceException e) {
        	throw new ServiceException(e);
        }
        List<?> order = elementDefinitionFacade.attributeValuesAsList("orderFieldGroup");
        if((order == null) || order.isEmpty()) {
            order = elementDefinitionFacade.attributeValuesAsList("order");
        }
        return order;
    }

    private List<Object> getOrderObjectContainer(
    	MappedRecord elementDefinition
    ) throws ServiceException {
    	Object_2Facade elementDefinitionFacade;
        try {
	        elementDefinitionFacade = Object_2Facade.newInstance(elementDefinition);
        }
        catch (ResourceException e) {
        	throw new ServiceException(e);
        }    	
        List<Object> order = elementDefinitionFacade.attributeValuesAsList("orderObjectContainer");
        if((order == null) || order.isEmpty()) {
            order = elementDefinitionFacade.attributeValuesAsList("order");
        }
        return order;
    }

    private void setElementDefinitionDefaultLayout(
    	MappedRecord element
    ) throws ServiceException {
    	Object_2Facade elementFacade;
        try {
        	elementFacade = Object_2Facade.newInstance(element);
        } catch (ResourceException e) {
        	throw new ServiceException(e);
        }    	    	
        elementFacade.attributeValuesAsList("verticalFill").add(new Boolean(false));
        elementFacade.attributeValuesAsList("columnBreakAtElement");
        elementFacade.attributeValuesAsList("columnSizeMin");
        elementFacade.attributeValuesAsList("columnSizeMax");
    }

    private boolean isMemberOfObjectContainer(
        StructuralFeatureDefinition feature
    ) throws ServiceException {
    	Model_1_0 model = Model_1Factory.getModel();
        if(feature.getModelElement() == null) {
            return true;
        } else {
            ModelElement_1_0 element = feature.getModelElement();
            return 
                model.isAttributeType(element) 
                || model.isStructureFieldType(element) 
                || model.isReferenceType(element);
        }
    }

    private boolean isReferenceField(
        StructuralFeatureDefinition feature
    ) throws ServiceException {
    	Model_1_0 model = Model_1Factory.getModel();
        ModelElement_1_0 elementType = feature.getType();
        if(feature.getModelElement() == null) {
            return model.isClassType(elementType);
        } else {
            ModelElement_1_0 element = feature.getModelElement();
            return (((model.isAttributeType(element) || model.isStructureFieldType(element)) && model.isClassType(elementType)) || model.isReferenceType(element));
        }
    }

    /**
     * Map field definition to field.
     * 
     * @param field
     * @param feature
     * @param definition
     * @param asMemberOfObjectContainer
     * @param defaultMimeType
     * @param defaultValue
     * @throws ServiceException
     */
    private void mapField(
    	MappedRecord field,
        StructuralFeatureDefinition feature,
        MappedRecord definition,
        boolean asMemberOfObjectContainer,
        String defaultMimeType,
        String defaultValue
    ) throws ServiceException {
    	Object_2Facade fieldFacade;
    	Object_2Facade definitionFacade;
        try {
	        fieldFacade = Object_2Facade.newInstance(field);
	        if(fieldFacade.getValue() == null) {
	        	fieldFacade.setValue(
	        		Records.getRecordFactory().createMappedRecord("org:openmdx:ui1:Element")	        		
	        	);
	        }
	        definitionFacade = Object_2Facade.newInstance(definition);
        } catch (ResourceException e) {
        	throw new ServiceException(e);
        }
        Model_1_0 model = Model_1Factory.getModel();
        ModelElement_1_0 featureType = model.getDereferencedType(
            feature.getType()
        );
        String typeName = (String)featureType.getQualifiedName();
        boolean isAdditionalElementDefinition = 
            "org:openmdx:ui1:AdditionalElementDefinition".equals(definitionFacade.getObjectClass());
        // Default value
        if(defaultValue != null) {
        	fieldFacade.attributeValuesAsList("defaultValue").add(defaultValue);
        }
        // Reference stored as attribute or attribute of type class
        boolean isReferenceField = this.isReferenceField(feature) && !isAdditionalElementDefinition;
        if(isReferenceField) {
        	fieldFacade.getValue().setRecordName("org:openmdx:ui1:ObjectReferenceField");
            this.setObjectReference(
                field,
                feature
            );
            fieldFacade.attributeValuesAsList("titleIndex").add(
                !definitionFacade.attributeValuesAsList("titleIndex").isEmpty() 
                	? definitionFacade.attributeValue("titleIndex")
                    : new Integer(0)
            );
        }
        // string | anyURI | unknown primitive type
        // elements which are defined with as AdditionalElementDefinition are always mapped
        // to TextBox. Additional definitions typically contain expressions such as
        // ((RefObject)$this.contact).refGetValue("name"). In contrast, AlternateElementDefinitions
        // inherit the type of the base definition.
        boolean mapToString = false;
        if(
            PrimitiveTypes.STRING.equals(typeName) ||
            PrimitiveTypes.ANYURI.equals(typeName) ||
            isAdditionalElementDefinition
        ) {
            mapToString = true;
        } else if(
            PrimitiveTypes.DECIMAL.equals(typeName) ||
            PrimitiveTypes.SHORT.equals(typeName) ||
            PrimitiveTypes.LONG.equals(typeName) ||
            PrimitiveTypes.INTEGER.equals(typeName)
        ) {
            // decimal
        	fieldFacade.getValue().setRecordName("org:openmdx:ui1:NumberField");
            BigDecimal defaultMinValue = null;
            BigDecimal defaultMaxValue = null;
            Integer defaultDecimalPlaces = new Integer(0);
            if(PrimitiveTypes.DECIMAL.equals(typeName)) {
                defaultMinValue = new BigDecimal(Long.MIN_VALUE);
                defaultMaxValue = new BigDecimal(Long.MAX_VALUE);
                defaultDecimalPlaces = new Integer(2);
            } else if(PrimitiveTypes.SHORT.equals(typeName)) {
                defaultMinValue = new BigDecimal(Short.MIN_VALUE);
                defaultMaxValue = new BigDecimal(Short.MAX_VALUE);
            } else if(PrimitiveTypes.LONG.equals(typeName)) {
                defaultMinValue = new BigDecimal(Long.MIN_VALUE);
                defaultMaxValue = new BigDecimal(Long.MAX_VALUE);
            } else if(PrimitiveTypes.INTEGER.equals(typeName)) {
                defaultMinValue = new BigDecimal(Integer.MIN_VALUE);
                defaultMaxValue = new BigDecimal(Integer.MAX_VALUE);            
            }
            fieldFacade.attributeValuesAsList("minValue").add(
                !definitionFacade.attributeValuesAsList("minValue").isEmpty()
                ? new BigDecimal((String)definitionFacade.attributeValue("minValue"))
                : defaultMinValue
            );
            fieldFacade.attributeValuesAsList("maxValue").add(
                !definitionFacade.attributeValuesAsList("maxValue").isEmpty()
	                ? new BigDecimal((String)definitionFacade.attributeValue("maxValue"))
	                : defaultMaxValue
            );
            fieldFacade.attributeValuesAsList("decimalPlaces").add(
                !definitionFacade.attributeValuesAsList("decimalPlaces").isEmpty()
	                ? new Integer(((Number)definitionFacade.attributeValue("decimalPlaces")).intValue())
	                : defaultDecimalPlaces        
            );
            fieldFacade.attributeValuesAsList("increment").add(
                !definitionFacade.attributeValuesAsList("increment").isEmpty()
	                ? new BigDecimal((String)definitionFacade.attributeValue("increment"))
	                : new BigDecimal(1)
            );
            fieldFacade.attributeValuesAsList("hasThousandsSeparator").add(
                !definitionFacade.attributeValuesAsList("hasThousandsSeparator").isEmpty()
                	? (Boolean)definitionFacade.attributeValue("hasThousandsSeparator")
                    : Boolean.TRUE
            );
        } else if(PrimitiveTypes.DATE.equals(typeName)) {
            // date
            fieldFacade.getValue().setRecordName("org:openmdx:ui1:DateField");
            fieldFacade.attributeValuesAsList("format").add("d");
        } else if(PrimitiveTypes.DATETIME.equals(typeName)) {
            // dateTime
        	fieldFacade.getValue().setRecordName("org:openmdx:ui1:DateField");
        	fieldFacade.attributeValuesAsList("format").add("g");
        } else if(PrimitiveTypes.BOOLEAN.equals(typeName)) {
            // boolean
        	fieldFacade.getValue().setRecordName("org:openmdx:ui1:CheckBox");
        	fieldFacade.attributeValuesAsList("threeState").add(new Boolean(false));
        } else if(PrimitiveTypes.BINARY.equals(typeName)) {
            // binary
        	fieldFacade.getValue().setRecordName("org:openmdx:ui1:DocumentBox");
            if(defaultMimeType != null) {
                fieldFacade.attributeValuesAsList("mimeType").add(defaultMimeType);
            }
            fieldFacade.attributeValuesAsList("inPlace").add(
                !definitionFacade.attributeValuesAsList("inPlace").isEmpty()
                ? (Boolean)definitionFacade.attributeValue("inPlace")
                    : Boolean.FALSE
            );
        } else if(model.isPrimitiveType(featureType)) {
            // unknown primitive types are mapped to string
            mapToString = true;
        }
        if(mapToString) {
        	fieldFacade.getValue().setRecordName("org:openmdx:ui1:TextBox");
        	fieldFacade.attributeValuesAsList("wordWrap").add(new Boolean(true));
        	fieldFacade.attributeValuesAsList("tabStop").add(new Boolean(true));
        	fieldFacade.attributeValuesAsList("multiline").add(new Boolean(false));
        	fieldFacade.attributeValuesAsList("maxLength").add(
                !definitionFacade.attributeValuesAsList("maxLength").isEmpty()
                	? (Integer)definitionFacade.attributeValue("maxLength")
                    : new Integer(Integer.MAX_VALUE)
            );
        	fieldFacade.attributeValuesAsList("autoSize").add(new Boolean(true));
        	fieldFacade.attributeValuesAsList("acceptsTab").add(new Boolean(false));
        	fieldFacade.attributeValuesAsList("isPassword").add(
        		definitionFacade.attributeValuesAsList("isPassword").isEmpty()
	                ? new Boolean(false)
	                : definitionFacade.attributeValue("isPassword")
            );
        	fieldFacade.attributeValuesAsList("textAlign").add(new Short((short)0));
            if(defaultMimeType != null) {
                fieldFacade.attributeValuesAsList("mimeType").add(defaultMimeType);
            }
        }
        // complete
        if(fieldFacade.getObjectClass() != null) {
            // as default take changeability from model. 
            Boolean isChangeable = feature.isChangeable();
            isChangeable = (definitionFacade.getAttributeValues("changeable") == null) || definitionFacade.attributeValuesAsList("changeable").isEmpty()
            	? isChangeable
                : (Boolean)definitionFacade.attributeValue("changeable"); 
            boolean isSortable = 
                !"org:openmdx:base:ExtentCapable:identity".equals(feature.getQualifiedName()) &&
                !PrimitiveTypes.BINARY.equals(typeName);
            this.setValuedFieldDefault(
                field,
                definitionFacade.attributeValuesAsList("label"),
                definitionFacade.attributeValuesAsList("shortLabel"),
                definitionFacade.attributeValuesAsList("displayValueExpr"),
                definitionFacade.attributeValuesAsList("toolTip"),
                isChangeable.booleanValue(), 
                (String)definitionFacade.attributeValue("iconKey"),
                (String)definitionFacade.attributeValue("color"), 
                (String)definitionFacade.attributeValue("backColor"),
                (String)definitionFacade.attributeValue("cssClassFieldGroup"),
                (String)definitionFacade.attributeValue("cssClassObjectContainer"),
                definitionFacade.attributeValuesAsList("multiplicity").isEmpty() 
                	? feature.getMultiplicity() 
                	: (String)definitionFacade.attributeValue("multiplicity"),
                !definitionFacade.attributeValuesAsList("spanRow").isEmpty() 
                	? (Integer)definitionFacade.attributeValue("spanRow") 
                	: 1,
                !definitionFacade.attributeValuesAsList("skipRow").isEmpty() 
                	? (Integer)definitionFacade.attributeValue("skipRow") 
                	: 0, 
                definitionFacade.attributeValuesAsList("filterable").isEmpty() ? 
                    !isReferenceField : 
                    ((Boolean)definitionFacade.attributeValue("filterable")).booleanValue(),
                definitionFacade.attributeValuesAsList("sortable").isEmpty() 
                	? isSortable 
                	: ((Boolean)definitionFacade.attributeValue("sortable")).booleanValue(),
                definitionFacade.attributeValuesAsList("mandatory").isEmpty() 
                	? Multiplicity.SINGLE_VALUE.code().equals(feature.getMultiplicity()) && feature.isChangeable() 
                	: ((Boolean)definitionFacade.attributeValue("mandatory")).booleanValue(),
                feature.getName(),
                feature.getQualifiedName(),
                (String)definitionFacade.attributeValue("dataBindingName")
            );
        }
    }

    /**
     * Overload base definition with overload definition.
     * 
     * @param target
     * @param base
     * @param overloadWith
     * @throws ResourceException
     */
    @SuppressWarnings("unchecked")
    private void overloadDefinition(
    	MappedRecord target,
    	MappedRecord base,
    	MappedRecord overloadWith
    ) throws ResourceException {
    	try {
	    	Object_2Facade targetFacade;
	    	Object_2Facade overloadWithFacade;
	        targetFacade = Object_2Facade.newInstance(target);
	    	Path targetPath = targetFacade.getPath(); 
	    	target.putAll(
	    		Object_2Facade.cloneObject(base)
	    	);
	        targetFacade.setPath(targetPath);	        	
	        overloadWithFacade = Object_2Facade.newInstance(overloadWith);
	        // do not use addClones because attributes with no values must not
	        // be replaced/cleared
	        for(
	            Iterator<?> i = overloadWithFacade.getValue().keySet().iterator();
	            i.hasNext();
	        ) {
	            String attributeName = (String)i.next();
	            if(!overloadWithFacade.attributeValuesAsList(attributeName).isEmpty()) {
	            	targetFacade.attributeValuesAsList(attributeName).clear();
	            	targetFacade.attributeValuesAsList(attributeName).addAll(
	                	overloadWithFacade.attributeValuesAsList(attributeName)
	                );
	            }
	        }
    	} catch(ServiceException e) {
    		throw new ResourceException(e);
    	}
    }
    
    /**
     * Get mime types of container elements.
     * 
     * @param containerDefinition
     * @return
     * @throws ServiceException
     */
    private Map<String,Object> getMemberMimeTypes(
    	MappedRecord containerDefinition
    ) throws ServiceException {
    	Object_2Facade containerDefinitionFacade;
        try {
	        containerDefinitionFacade = Object_2Facade.newInstance(containerDefinition);
        } catch (ResourceException e) {
        	throw new ServiceException(e);
        }
        Map<String,Object> memberMimeTypes = new HashMap<String,Object>();
        if(containerDefinitionFacade.getAttributeValues("memberElementName") != null) {
            for(int i = 0; i < containerDefinitionFacade.attributeValuesAsList("memberElementName").size(); i++) {
                String name = (String)containerDefinitionFacade.attributeValuesAsList("memberElementName").get(i);
                if(
                    (containerDefinitionFacade.attributeValuesAsList("memberMimeType").size() > i) &&
                    (containerDefinitionFacade.attributeValuesAsList("memberMimeType").get(i) != null) &&
                    !"".equals(containerDefinitionFacade.attributeValuesAsList("memberMimeType").get(i))
                ) {
                    memberMimeTypes.put(
                        name,
                        containerDefinitionFacade.attributeValuesAsList("memberMimeType").get(i)
                    );
                }
            }
        }
        return memberMimeTypes;
    }

    /**
     * Get default values for container elements.
     * 
     * @param containerDefinition
     * @return
     * @throws ServiceException
     */
    private Map<String,Object> getMemberDefaultValues(
    	MappedRecord containerDefinition
    ) throws ServiceException {
    	Object_2Facade containerDefinitionFacade;
        try {
	        containerDefinitionFacade = Object_2Facade.newInstance(containerDefinition);
        } catch (ResourceException e) {
        	throw new ServiceException(e);
        }    	
        Map<String,Object> memberDefaultValues = new HashMap<String,Object>();
        if(containerDefinitionFacade.getAttributeValues("memberElementName") != null) {
            for(int i = 0; i < containerDefinitionFacade.attributeValuesAsList("memberElementName").size(); i++) {
                String name = (String)containerDefinitionFacade.attributeValuesAsList("memberElementName").get(i);
                if(
                    (containerDefinitionFacade.attributeValuesAsList("memberDefaultValue").size() > i) &&
                    (containerDefinitionFacade.attributeValuesAsList("memberDefaultValue").get(i) != null) &&
                    !"".equals(containerDefinitionFacade.attributeValuesAsList("memberDefaultValue").get(i))
                ) {
                    memberDefaultValues.put(
                        name,
                        containerDefinitionFacade.attributeValuesAsList("memberDefaultValue").get(i)
                    );
                }
            }
        }
        return memberDefaultValues;
    }

    /**
     * Get struct definition for given parameter.
     * 
     * @param paramType
     * @return
     * @throws ServiceException
     */
    private List<Ui_1.StructuralFeatureDefinition> getStructFeatureDefinitions(
        ModelElement_1_0 paramType
    ) throws ServiceException {
        Collection<ModelElement_1_0> fieldDefs = paramType.objGetMap("field").values();
        List<Ui_1.StructuralFeatureDefinition> featureDefinitions = new ArrayList<Ui_1.StructuralFeatureDefinition>();
        for(ModelElement_1_0 fieldDef: fieldDefs) {
            featureDefinitions.add(
                new StructuralFeatureDefinition(fieldDef)
            );
        }
        return featureDefinitions;
    }

    /**
     * Get definitions for features of given class.
     * 
     * @param segmentIdentity
     * @param classDef
     * @param includeSubtypes
     * @param attributesOnly
     * @return
     * @throws ServiceException
     */
    private List<Ui_1.StructuralFeatureDefinition> getStructuralFeatureDefinitions(
        Path segmentIdentity,
        ModelElement_1_0 classDef,
        boolean includeSubtypes,
        boolean attributesOnly
    ) throws ServiceException {
    	Model_1_0 model = Model_1Factory.getModel();
        List<ModelElement_1_0> featureDefs = new ArrayList<ModelElement_1_0>(
            model.getStructuralFeatureDefs(
                classDef, 
                includeSubtypes, 
                true, 
                attributesOnly
            ).values()
        );
        Map<String,Ui_1.StructuralFeatureDefinition> featureDefinitions = new HashMap<String,Ui_1.StructuralFeatureDefinition>();
        // Add modeled structural features
        for(ModelElement_1_0 featureDef: featureDefs) {
            StructuralFeatureDefinition feature = new StructuralFeatureDefinition(featureDef);            
            if(feature.isReference()) {
                // Only add reference features if not explicitly excluded and exposed end is not composite
                ModelElement_1_0 exposedEnd = model.getElement(featureDef.getExposedEnd());
                if(
                    !(REFERENCES_TO_EXCLUDE.contains(feature.getQualifiedName()) || REFERENCES_TO_EXCLUDE.contains(feature.getName())) &&
                    !AggregationKind.COMPOSITE.equals(exposedEnd.getAggregation())
                ) {
                    featureDefinitions.put(
                        feature.getQualifiedName(),
                        feature
                    );
                }
            } else {            
                featureDefinitions.put(
                    feature.getQualifiedName(),
                    feature
                );
            }
        }
        // Add customized structural features for segment Root
        for(StructuralFeatureDefinition featureDefinition: this.structuralFeatureDefinitions.get(segmentIdentity.getParent().getChild("Root"))) {
            if(
                (!featureDefinition.isReference() || !attributesOnly) &&
                model.isSubtypeOf(classDef, featureDefinition.getContainer())
            ) {                
                featureDefinitions.put(
                    featureDefinition.getQualifiedName(),
                    featureDefinition
                );
            }
        }      
        // Add customized structural features
        for(StructuralFeatureDefinition featureDefinition: this.structuralFeatureDefinitions.get(segmentIdentity)) {
            if(
                (!featureDefinition.isReference() || !attributesOnly) &&
                model.isSubtypeOf(classDef, featureDefinition.getContainer())
            ) {
                featureDefinitions.put(
                    featureDefinition.getQualifiedName(),
                    featureDefinition
                );
            }
        }      
        return new ArrayList<Ui_1.StructuralFeatureDefinition>(featureDefinitions.values());
    }

    /**
     * Get definitions for operations of given class.
     * 
     * @param segmentIdentity
     * @param classDef
     * @return
     * @throws ServiceException
     */
    private List<Ui_1.OperationDefinition> getOperationDefinitions(
        Path segmentIdentity,
        ModelElement_1_0 classDef
    ) throws ServiceException {
    	Model_1_0 model = Model_1Factory.getModel();
        Map<String,Ui_1.OperationDefinition> featureDefinitions = new HashMap<String,Ui_1.OperationDefinition>();
        // Add modeled operations
        Collection<ModelElement_1_0> features = classDef.objGetMap("allFeature").values();
        for(ModelElement_1_0 feature: features) {
            if(model.isOperationType(feature)) {
                OperationDefinition featureDefinition = new OperationDefinition(feature);
                featureDefinitions.put(
                    featureDefinition.getQualifiedName(),
                    featureDefinition
                );
            }
        }
        // Add customized operations for segment Root
        for(OperationDefinition featureDefinition: this.operationDefinitions.get(segmentIdentity.getParent().getChild("Root"))) {
            if(model.isSubtypeOf(classDef, featureDefinition.getContainer())) {
                featureDefinitions.put(
                    featureDefinition.getQualifiedName(),
                    featureDefinition
                );
            }
        }      
        // Add customized operations for current segment
        for(OperationDefinition featureDefinition: this.operationDefinitions.get(segmentIdentity)) {
            if(model.isSubtypeOf(classDef, featureDefinition.getContainer())) {
                featureDefinitions.put(
                    featureDefinition.getQualifiedName(),
                    featureDefinition
                );
            }
        }      
        return new ArrayList<Ui_1.OperationDefinition>(featureDefinitions.values());
    }

    /**
     * Get qualified name for given pane type.
     * 
     * @param paneType
     * @param inspectorClass
     * @return
     * @throws ServiceException
     */
    private String getPaneName(
        String paneType,
        ModelElement_1_0 inspectorClass
    ) throws ServiceException {
        String id = "Multi";
        if(PANE_TYPE_ATTRIBUTE.equals(paneType)) {
            id = "Attr";
        } else if(PANE_TYPE_REFERENCE.equals(paneType)) {
            id = "Ref";
        } else if(PANE_TYPE_OPERATION.equals(paneType)) {
            id = "Op";
        }
        return inspectorClass == null ? 
            "Pane:" + paneType : 
            inspectorClass.getQualifiedName() + ":Pane:" + id;
    }

    /**
     * Add tab definition to given pane.
     * 
     * @param tabName
     * @param tabType
     * @param tabDef
     * @param pane
     * @param tabDefs
     * @return
     * @throws ServiceException
     */
    private ObjectRecord addTab(
        String tabName,
        String tabType,
        ObjectRecord tabDef,
        ObjectRecord pane,
        Map<Path,ObjectRecord> tabDefs
    ) throws ServiceException {
    	try {
	        Path segmentIdentity = Object_2Facade.getPath(pane).getPrefix(5);
	        Path tabIdentity = segmentIdentity.getDescendant(
	            new String[]{"element", tabName}
	        );
	        ObjectRecord tab = Object_2Facade.newInstance(
	        	tabIdentity,
	        	tabType
	        ).getDelegate();
	        Object_2Facade tabDefFacade = Object_2Facade.newInstance(tabDef);
	        this.setElementDefault(
	            tab,
	            tabDefFacade.attributeValuesAsList("toolTip"),
	            tabDefFacade.attributeValuesAsList("changeable").isEmpty() ? 
	                this.changableDefaultValue : 
	                ((Boolean)tabDefFacade.attributeValuesAsList("changeable").get(0)).booleanValue(),
	            (String)tabDefFacade.attributeValue("iconKey")
	        );
	        Object_2Facade.newInstance(tab).attributeValuesAsList("title").addAll(
	        	tabDefFacade.attributeValuesAsList("label")
	        );
	        this.setElementDefinitionDefaultLayout(tab);
	        // Add tab to pane and sort by order
	        List<Object> members = Object_2Facade.newInstance(pane).attributeValuesAsList("member");
	        int l = 0;
	        int r = members.size() - 1;
	        while(l <= r) {
	            int pos = (l + r) / 2;
	            if(tabDefs.get(members.get(pos)) == null) {
	                System.out.println("#ERR");
	            }
	            int res = this.compareOrder(
	            	tabDefFacade.attributeValuesAsList("order"),
	                Object_2Facade.newInstance(tabDefs.get(members.get(pos))).attributeValuesAsList("order")
	            );
	            if(res == 0) { l = pos; r = pos; break;} 
	            else if(res > 0) l = pos + 1;
	            else if(res < 0) r = pos - 1;
	        }
	        if(members.isEmpty()) {
	            members.add(
	                tabIdentity
	            );
	        } else {
	            members.add(
	                l,
	                tabIdentity
	            );              
	        }
	        tabDefs.put(
	            tabIdentity,
	            tabDef
	        );
	        return tab;
    	} catch(ResourceException e) {
    		throw new ServiceException(e);
    	}
    }

    /**
     * Create operation definition on BasicObject.
     * 
     * @param qualifiedOperationName
     * @param isQuery
     * @return
     * @throws ServiceException
     */
    private OperationDefinition createBasicObjectOperationDef(
        String qualifiedOperationName,
        boolean isQuery
    ) throws ServiceException {
    	Model_1_0 model = Model_1Factory.getModel();
        return new OperationDefinition(
            qualifiedOperationName.substring(qualifiedOperationName.lastIndexOf(":") + 1),
            qualifiedOperationName,
            isQuery,
            model.getElement("org:openmdx:base:BasicObject")
        );
    }

    /**
     * RestInteraction
     *
     */
    public class RestInteraction extends AbstractRestInteraction {
      
        public RestInteraction(
            RestConnection connection
        ) throws ResourceException {
			super(connection, newDelegateInteraction(connection));
        }

        /**
         * Update/create given object.
         * 
         * @param segmentIdentity
         * @param object
         * @throws ResourceException
         */
        protected void storeElement(
            Path segmentIdentity,
            ObjectRecord object
        ) throws ResourceException {
            try {
            	ResultRecord output = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
            	super.update(
            		ISPECS.UPDATE, 
            		object, 
            		output
            	);
            } catch(ResourceException e) {
            	ServiceException e0 = new ServiceException(e);
            	if(e0.getExceptionCode() != BasicException.Code.NOT_FOUND) {
            		throw e;
            	}
            	ResultRecord output = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
            	super.update(
            		ISPECS.CREATE, 
            		object, 
            		output
            	);            	
            }
            Ui_1.this.existingElements.get(segmentIdentity).put(
                Object_2Facade.getPath(object).getLastSegment().toClassicRepresentation(),
                object
            );
        }

        /**
         * Update/create given element definition.
         * 
         * @param segmentIdentity
         * @param elementDefinition
         * @throws ResourceException
         */
        private void storeElementDefinition(
            Path segmentIdentity,
            ObjectRecord elementDefinition
        ) throws ResourceException {
            try {
            	ResultRecord output = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
            	super.update(
            		ISPECS.UPDATE, 
            		elementDefinition, 
            		output
            	);
            } catch(ResourceException e) {
            	ServiceException e0 = new ServiceException(e);
            	if(e0.getExceptionCode() != BasicException.Code.NOT_FOUND) {
            		throw e;
            	}
            	ResultRecord output = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
            	super.create(
            		ISPECS.CREATE, 
            		elementDefinition, 
            		output
            	);
            }
            Ui_1.this.existingElementDefinitions.get(segmentIdentity).put(
                Object_2Facade.getPath(elementDefinition).getLastSegment().toClassicRepresentation(),
                elementDefinition
            );
        }

        /**
         * Get element definition with given name.
         * 
         * @param segmentIdentity
         * @param elementName
         * @param defaultLabel
         * @param useDefaultIconKey
         * @return
         * @throws ResourceException
         */
        private ObjectRecord getElementDefinition(
            Path segmentIdentity,
            String elementName,
            String defaultLabel,
            boolean useDefaultIconKey
        ) throws ResourceException {
        	try {
                if(
                    (Ui_1.this.existingElementDefinitions == null) ||
                    (Ui_1.this.existingElementDefinitions.get(segmentIdentity) == null) ||
                    elementName.length() == 0
                ) {
                    SysLog.error("Unable to retrieve element definition", Arrays.asList(elementName, Ui_1.this.existingElementDefinitions, Ui_1.this.existingElementDefinitions == null ? null : Ui_1.this.existingElementDefinitions.get(segmentIdentity), elementName));
                }
	            // Verify whether definition already exists
	        	ObjectRecord elementDefinition = null;
	        	String lookupName = elementName;
	        	while(true) {
                    elementDefinition = Ui_1.this.existingElementDefinitions.get(segmentIdentity).get(lookupName);
                    if(elementDefinition != null) {
                        return elementDefinition;
                    }
                    int pos = lookupName.indexOf(":");
                    if(pos < 0) {
                    	break;
                    }
                    lookupName = lookupName.substring(pos + 1);
	            }
	            // No element definition found for element
	            // Get from Root segment
	            if(!"Root".equals(segmentIdentity.getLastSegment().toClassicRepresentation())) {
	                elementDefinition = this.getElementDefinition(
	                    segmentIdentity.getParent().getChild("Root"),
	                    elementName, 
	                    defaultLabel, 
	                    useDefaultIconKey
	                );
	            } else {
	                // Create default definition if already in Root
	                SysLog.detail("no configured definition found. creating default", elementName);
	                Object_2Facade elementDefinitionFacade;
		            elementDefinition = Object_2Facade.newInstance(
		                segmentIdentity.getDescendant(new String[]{"elementDefinition", elementName}),
		                "org:openmdx:ui1:ElementDefinition"
		            ).getDelegate();
		            elementDefinitionFacade = Object_2Facade.newInstance(elementDefinition);
	                // Active
	                elementDefinitionFacade.attributeValuesAsList("active").add(new Boolean(true));
	                // Default iconKey
	                if(useDefaultIconKey) {
	                	elementDefinitionFacade.attributeValuesAsList("iconKey").add(elementName);
	                }
	                // Default label
	                if(elementDefinitionFacade.getAttributeValues("label") == null) {
	                    List<Object> values = elementDefinitionFacade.attributeValuesAsList("label");
	                    values.add(
	                        defaultLabel == null ? "N/A" : defaultLabel
	                    );
	                }
	                // Default shortLabel
	                if(elementDefinitionFacade.getAttributeValues("shortLabel") == null) {        
	                    List<Object> values = elementDefinitionFacade.attributeValuesAsList("shortLabel");
	                    values.addAll(
	                    	elementDefinitionFacade.attributeValuesAsList("label")
	                    );
	                }    
	                // Default toolTip
	                List<Object> values = elementDefinitionFacade.attributeValuesAsList("toolTip");
	                values.add("toolTip of element " + elementName);
	            }
	            this.storeElementDefinition(
	                segmentIdentity,
	                elementDefinition
	            );    
	            return elementDefinition;
        	} catch(ServiceException e) {
        		throw new ResourceException(e);
        	}
        }

        /**
         * Add additional definitions for given base definition.
         * 
         * @param baseDefinition
         * @param inspectorClass
         * @param definitions
         * @throws ResourceException
         */
        private void addAdditionalElementDefinitions(
        	ObjectRecord baseDefinition,
            ModelElement_1_0 inspectorClass,
            List<ObjectRecord> definitions
        ) throws ResourceException {
        	try {
	            // get additional element definitions. Construct an element 
	            // definition on the base definition and override with additional definition
	        	ResultRecord additionalElementDefinitions = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
	        	super.find(
	        		ISPECS.GET, 
	        		this.newQuery(baseDefinition.getResourceIdentifier().getChild("additionalElementDefinition")),
	        		additionalElementDefinitions
	        	);
	            for(Object object: additionalElementDefinitions) {
	            	ObjectRecord additionalDefinition = (ObjectRecord)object;
	            	ObjectRecord target;
		            target = Object_2Facade.newInstance(
		                Object_2Facade.getPath(additionalDefinition)
		            ).getDelegate();
	                Ui_1.this.overloadDefinition(
	                    target,
	                    baseDefinition,
	                    additionalDefinition
	                );
	                Object_2Facade targetFacade;
		            targetFacade = Object_2Facade.newInstance(target);
	                // filter additional definition: add definition only if base class matches for class
	                // or if forClass is unspecified
	                String inspectorClassName = (String)inspectorClass.getQualifiedName();
	                if((targetFacade.getAttributeValues("forClass") == null) || targetFacade.attributeValuesAsList("forClass").contains(inspectorClassName)) {
	                    definitions.add(
	                        target
	                    );
	                }
	            }
        	} catch(ServiceException e) {
        		throw new ResourceException(e);
        	}
        }

        /**
         * Add alternate definitions for given base definition.
         * 
         * @param baseDefinition
         * @param inspectorClass
         * @param definitions
         * @throws ResourceException
         */
        @SuppressWarnings("unchecked")
        private void addAlternateElementDefinitions(
        	ObjectRecord baseDefinition,
            ModelElement_1_0 inspectorClass,
            List<ObjectRecord> definitions
        ) throws ResourceException {
            // get alternate element definitions. Construct an element 
            // definition on the base definition and override with alternate definition
        	ResultRecord alternateElementDefinitions = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
        	super.find(
        		ISPECS.GET, 
        		this.newQuery(baseDefinition.getResourceIdentifier().getChild("alternateElementDefinition")), 
        		alternateElementDefinitions
        	);
            for(
                Iterator<Object> j = alternateElementDefinitions.iterator();
                j.hasNext();
            ) {
            	ObjectRecord alternateDefinition = (ObjectRecord)j.next();
            	ObjectRecord target;
	            target = Object_2Facade.newInstance(
	                Object_2Facade.getPath(alternateDefinition)
	            ).getDelegate();
                Ui_1.this.overloadDefinition(
                    target,
                    baseDefinition,
                    alternateDefinition
                );
                definitions.add(
                    target
                );
            }    
        }

        /**
         * Add pane to panes.
         * 
         * @param segmentIdentity
         * @param paneType
         * @param inspectorClass
         * @param uiDef
         * @param panes
         * @return
         * @throws ResourceException
         */
        private ObjectRecord addPane(
            Path segmentIdentity,
            String paneType,
            ModelElement_1_0 inspectorClass,
            ObjectRecord uiDef,
            Map<Path,ObjectRecord> panes
        ) throws ResourceException {
        	try {
    	        String paneName = Ui_1.this.getPaneName(
    	            paneType, 
    	            inspectorClass
    	        );
    	        Number paneOrder = uiDef == null ? 
    	            null : 
    	            !Object_2Facade.newInstance(uiDef).attributeValuesAsList("order").isEmpty() ? 
    	                (Number)Object_2Facade.newInstance(uiDef).attributeValue("order") : 
    	                new Integer(90000);                     
    	        DecimalFormat orderFormatter= new DecimalFormat("###00"); 
    	        Path paneIdentity = segmentIdentity.getDescendant(
    	            new String[]{
    	                "element",
    	                paneOrder == null ? 
    	                    paneName : 
    	                    paneName + ":" + orderFormatter.format(paneOrder)
    	            }
    	        );
    	        MappedRecord paneDef = this.getElementDefinition(
    	            segmentIdentity,
    	            paneIdentity.getLastSegment().toClassicRepresentation(),
    	            null,
    	            true
    	        );
    	        ObjectRecord pane = panes.get(paneIdentity);
    	        if(pane == null) {
    	            pane = Object_2Facade.newInstance(
    	            	paneIdentity,
    	            	paneType
    	            ).getDelegate();
    	            Object_2Facade paneDefFacade = Object_2Facade.newInstance(paneDef);
    	            Ui_1.this.setElementDefault(
    	                pane,
    	                paneDefFacade.attributeValuesAsList("toolTip"),
    	                !paneDefFacade.attributeValuesAsList("isChangeable").isEmpty() ? 
    	                    ((Boolean)paneDefFacade.attributeValue("isChangeable")).booleanValue() : 
    	                    true, 
    	                (String)paneDefFacade.attributeValue("iconKey")
    	            );
    	            panes.put(
    	                paneIdentity,
    	                pane
    	            );
    	        }
    	        return pane;
        	} catch(ServiceException e) {
        		throw new ResourceException(e);
        	}
        }

        /**
         * Add reference tab.
         * 
         * @param segmentIdentity
         * @param panes
         * @param feature
         * @param forClass
         * @param inspectorClassDef
         * @param elementDefinitions
         * @throws ServiceException
         */
        private void addReferenceTab(
            Path segmentIdentity,
            Map<Path,ObjectRecord> panes,        
            StructuralFeatureDefinition feature,
            String forClass,
            ModelElement_1_0 inspectorClassDef,
            Map<Path,ObjectRecord> elementDefinitions
        ) throws ResourceException {
        	try {
	        	Model_1_0 model = Model_1Factory.getModel();
	            ModelElement_1_0 featureType = model.getDereferencedType(
	                feature.getType()
	            );
	            ObjectRecord baseDefinition = this.getElementDefinition(
	                segmentIdentity,
	                feature,
	                inspectorClassDef,
	                false // no default iconKey for references
	            );
	            // Get additional definitions
	            List<ObjectRecord> definitions = new ArrayList<ObjectRecord>();
	            definitions.add(
	                baseDefinition
	            );
	            this.addAlternateElementDefinitions(
	                baseDefinition,
	                inspectorClassDef,
	                definitions
	            );
	            this.addAdditionalElementDefinitions(
	                baseDefinition,
	                inspectorClassDef,
	                definitions
	            );
	            // Create ui object containers for base and additional definitions
	            for(ObjectRecord definition: definitions) {
	            	Object_2Facade definitionFacade = Facades.asObject(definition);
	                if(
	                	definitionFacade.attributeValuesAsList("active").isEmpty() || // default to true if active is not set
	                    ((Boolean)definitionFacade.attributeValue("active")).booleanValue()
	                ) {            
	                	ObjectRecord pane = this.addPane(
	                        segmentIdentity,
	                        PANE_TYPE_REFERENCE,
	                        inspectorClassDef,
	                        definition,
	                        panes
	                    );
	                    String alternateDefinitionNameSuffix = definition != baseDefinition ? 
	                        ":" + definitionFacade.getPath().getLastSegment().toClassicRepresentation() : 
	                        "";
	                    // Create new ObjectContainer if it is a reference of classDef
	                    ObjectRecord container = Ui_1.this.existingElements.get(segmentIdentity).get(
	                        model.getElement(feature.getContainer()).getQualifiedName() + ":Ref:" + feature.getName() + alternateDefinitionNameSuffix
	                    );
	                    if(
	                        (container == null) ||
	                        (feature.getQualifiedName()).startsWith(inspectorClassDef.getQualifiedName() + ":")
	                    ) { 
	                        String containerName = forClass + ":Ref:" + feature.getName() + alternateDefinitionNameSuffix;
	                        try {
	    	                    container = Object_2Facade.newInstance(
	    	                        segmentIdentity.getDescendant(new String[]{"element", containerName}),
	    	                        "org:openmdx:ui1:ObjectContainer"
	    	                    ).getDelegate();
	                        } catch (ResourceException e) {
	                        	throw new ServiceException(e);
	                        }
	                        Object_2Facade containerFacade = Facades.asObject(container);
	                        Boolean isChangeable = feature.isChangeable();
	                        isChangeable = definitionFacade.attributeValuesAsList("changeable").isEmpty() ? 
	                            isChangeable : 
	                            (Boolean)definitionFacade.attributeValue("changeable");
	                        Ui_1.this.setLabelledFieldDefault(
	                            container,
	                            definitionFacade.attributeValuesAsList("label"),
	                            definitionFacade.attributeValuesAsList("shortLabel"),
	                            definitionFacade.attributeValuesAsList("toolTip"),
	                            isChangeable.booleanValue(),
	                            "N/A",
	                            (String)definitionFacade.attributeValue("color"),
	                            (String)definitionFacade.attributeValue("backColor"),
	                            (String)definitionFacade.attributeValue("cssClassFieldGroup"),
	                            (String)definitionFacade.attributeValue("cssClassObjectContainer")
	                        );
	                        containerFacade.attributeValuesAsList("showMaxMember").addAll(
	                        	definitionFacade.attributeValuesAsList("showMaxMember")
	                        );
	                        containerFacade.attributeValuesAsList("inPlaceEditable").addAll(
	                        	definitionFacade.attributeValuesAsList("inPlace")
	                        );
	                        containerFacade.attributeValuesAsList("dataBindingName").addAll(
	                        	definitionFacade.attributeValuesAsList("dataBindingName")
	                        );
	                        // let the renderer set default label
	                        containerFacade.attributeValuesAsList("label").clear();
	                        Ui_1.this.setObjectReference(
	                            container,
	                            feature
	                        );
	                        // Container must contain all fields of referenced type and all its subtypes
	                        this.addFieldsToObjectContainer(
	                            segmentIdentity,
	                            container,
	                            containerName,
	                            definitionFacade.attributeValuesAsList("showMemberRange"),
	                            Ui_1.this.getStructuralFeatureDefinitions(
	                                segmentIdentity,
	                                featureType,
	                                true, // includeSubtypes
	                                true // attributesOnly
	                            ),
	                            featureType,
	                            null,
	                            null
	                        );
	                        // In case the referenced type is org:openmdx:base:ExtentCapable 
	                        // move the SystemAttributes at first order
	                        if("org:openmdx:base:ExtentCapable".equals(featureType.getQualifiedName())) {
	                        	List<Object> members = containerFacade.attributeValuesAsList("member");
	                        	List<Path> firstOrderMembers = new ArrayList<Path>();
	                        	for(Iterator<Object> i = members.iterator(); i.hasNext(); ) {
	                        		Path member = (Path)i.next();
	                        		if(member.getLastSegment().toClassicRepresentation().endsWith(":" + SystemAttributes.OBJECT_IDENTITY)) {
	                        			firstOrderMembers.add(member);
	                        			i.remove();
	                        		} else if(
	                        			member.getLastSegment().toClassicRepresentation().endsWith(":" + SystemAttributes.CREATED_AT) ||
	                        			member.getLastSegment().toClassicRepresentation().endsWith(":" + SystemAttributes.CREATED_BY) ||
	                        			member.getLastSegment().toClassicRepresentation().endsWith(":" + SystemAttributes.MODIFIED_AT) ||
	                        			member.getLastSegment().toClassicRepresentation().endsWith(":" + SystemAttributes.MODIFIED_BY)                    			
	                        		) {
	                        			firstOrderMembers.add(0, member);
	                        			i.remove();
	                        		}
	                        	}
	                        	for(Path member: firstOrderMembers) {
	                        		containerFacade.attributeValuesAsList("member").add(0, member);
	                        	}
	                        }
	                        // Limit number of members to maxMember (if defined)
	                        int maxMember = !definitionFacade.attributeValuesAsList("maxMember").isEmpty() ? 
	                            ((Number)definitionFacade.attributeValue("maxMember")).intValue()
	                            : DEFAULT_MAX_MEMBER;
	                        if(containerFacade.attributeValuesAsList("member").size() > maxMember) {
	                            List<Object> limitedMembers = new ArrayList<Object>(
	                            	containerFacade.attributeValuesAsList("member").subList(0, maxMember)
	                            );
	                            containerFacade.attributeValuesAsList("member").clear();
	                            containerFacade.attributeValuesAsList("member").addAll(
	                                limitedMembers
	                            );
	                        }
	                        this.storeElement(
	                            segmentIdentity,
	                            container
	                        );
	                    }
	                    // Tab
	                    String tabName = Object_2Facade.getPath(pane).getLastSegment().toClassicRepresentation() + ":Tab:" + feature.getName() + alternateDefinitionNameSuffix;
	                    ObjectRecord tab;
    	                tab = Object_2Facade.newInstance(
    	                    segmentIdentity.getDescendant(new String[]{"element", tabName}),
    	                    "org:openmdx:ui1:Tab"
    	                ).getDelegate();
	                	Object_2Facade tabFacade = Facades.asObject(tab);
	                    tabFacade.attributeValuesAsList("title").addAll(
	                        definitionFacade.attributeValuesAsList("label")
	                    );
	                    tabFacade.attributeValuesAsList("toolTip").addAll(
	                        definitionFacade.attributeValuesAsList("toolTip")
	                    );
	                    elementDefinitions.put(
	                        Object_2Facade.getPath(tab),
	                        definition
	                    );
	                    // Add tab to pane and sort by order
	                    List<Object> members;
    	                members = Object_2Facade.newInstance(pane).attributeValuesAsList("member");
	                    int l = 0;
	                    int r = members.size() - 1;
	                    while(l <= r) {
	                        int pos = (l + r) / 2;
	                        int res;
    	                    res = Ui_1.this.compareOrder(
    	                    	definitionFacade.attributeValuesAsList("order"),
    	                        Object_2Facade.newInstance(elementDefinitions.get(members.get(pos))).attributeValuesAsList("order")
    	                    );
	                        if(res == 0) {
	                        	l = pos; r = pos; break;
	                        } else if(res > 0) {
	                        	l = pos + 1;
	                        } else if(res < 0) {
	                        	r = pos - 1;
	                        }
	                    }
	                    if(members.isEmpty()) {
	                        members.add(
	                            Object_2Facade.getPath(tab)
	                        );
	                    } else {
	                        members.add(
	                            l,
	                            Object_2Facade.getPath(tab)
	                        );              
	                    }
	                    // add object container to tab
		                Object_2Facade.newInstance(tab).attributeValuesAsList("member").add(
		                    Object_2Facade.getPath(container)
		                );
	                    this.storeElement(
	                        segmentIdentity,
	                        tab
	                    );
	                }
	            }
        	} catch(ServiceException e) {
            	throw new ResourceException(e);
            }
        }
        
        /**
         * Get additional inspector definitions.
         * 
         * @param inspectorDefinition
         * @return
         * @throws ResourceException
         */
        private List<MappedRecord> getAdditionalInspectorDefinitions(
        	ObjectRecord inspectorDefinition
        ) throws ResourceException {
	        List<MappedRecord> inspectorDefinitions = new ArrayList<MappedRecord>();
	        // Get additional inspector definitions
	        ResultRecord additionalInspectorDefinitions = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
	        super.find(
	        	ISPECS.GET, 
	        	this.newQuery(inspectorDefinition.getResourceIdentifier().getChild("additionalElementDefinition")), 
	        	additionalInspectorDefinitions
	        );
	        for(Object object: additionalInspectorDefinitions) {
	        	ObjectRecord additionalInspectorDefinition = (ObjectRecord)object;
	        	MappedRecord target = Object_2Facade.newInstance(
	                Object_2Facade.getPath(additionalInspectorDefinition)
	            ).getDelegate();
	            Ui_1.this.overloadDefinition(
	                target,
	                inspectorDefinition,
	                additionalInspectorDefinition
	            );
	            inspectorDefinitions.add(
	                target
	            );
	        }
	        return inspectorDefinitions;
        }

	    /**
	     * Retrieve object with given identity. Return null if not found.
	     * 
	     * @param identity
	     * @return
	     * @throws ResourceException
	     */
	    protected ObjectRecord retrieveObject(
	        Path identity
	    ) throws ResourceException {
        	ResultRecord objects = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
	        super.get(
	        	ISPECS.GET,
	            this.newQuery(identity),
	            objects
	        );
	        return objects.isEmpty() ? null : (ObjectRecord)objects.get(0);
	    }

	    /**
	     * Update and store field groups.
	     * 
	     * @param segmentIdentity
	     * @param modifiedGroups
	     * @param cachedDefinitions
	     * @throws ResourceException
	     */
	    private void updateAndStoreFieldGroups(
	        Path segmentIdentity,
	        Set<String> modifiedGroups,
	        Map<Path,MappedRecord> cachedDefinitions
	    ) throws ResourceException {
	        for(String groupName: modifiedGroups) {
	        	try {
		            ObjectRecord group = Ui_1.this.existingElements.get(segmentIdentity).get(groupName);
		            Object_2Facade groupFacade;
		            groupFacade = Object_2Facade.newInstance(group);
		            boolean customizedGroup = groupFacade.getPath().getLastSegment().toClassicRepresentation().indexOf("Tab:100000") < 0;
		            // Add column breaks to group
		            // calculate columnSizeMin/Max of group 
		            int currentColumnSizeMax = 0;
		            int fieldIndex = 0;
		            int memberIndex = 0;
		            // as default put 7 members in one column except this would result in more than 
		            // three columns
		            int defaultColumnHeight = 
		                java.lang.Math.max(
		                    new Double(java.lang.Math.ceil(groupFacade.attributeValuesAsList("member").size() / 3.0)).intValue(),  
		                    7
		                );
		            for(
		                Iterator<?> j = groupFacade.attributeValuesAsList("member").iterator();
		                j.hasNext();
		                memberIndex++
		            ) {
		                Path e = (Path)j.next();
		                MappedRecord elementDefinition = cachedDefinitions.get(e);
		                List<?> columnBreak;
		                try {
			                columnBreak = Object_2Facade.newInstance(elementDefinition).attributeValuesAsList("columnBreak");
		                } catch (ResourceException e0) {
		                	throw new ServiceException(e0);
		                }
		                if(
		                    ((!columnBreak.isEmpty() && ((Boolean)columnBreak.get(0)).booleanValue()) || (!customizedGroup && (fieldIndex % defaultColumnHeight == 0)))
		                ) { 
		                	groupFacade.attributeValuesAsList("columnSizeMin").add(new Integer(330));
		                	groupFacade.attributeValuesAsList("columnSizeMax").add(new Integer(currentColumnSizeMax));
		                	groupFacade.attributeValuesAsList("columnBreakAtElement").add(new Integer(memberIndex));
		                    currentColumnSizeMax = 0;
		                    fieldIndex = 0;
		                }
		                try {
		                	Integer sizeXWeight = (Integer)Object_2Facade.newInstance(cachedDefinitions.get(e)).attributeValuesAsList("sizeXWeight").get(0);
			                currentColumnSizeMax = java.lang.Math.max(
			                    330 + (sizeXWeight == null ? 0 : sizeXWeight.intValue()) * 100, 
			                    currentColumnSizeMax
			                );
		                } catch (ResourceException e0) {
		                	throw new ServiceException(e0);
		                }
		                fieldIndex++;
		            }
		            groupFacade.attributeValuesAsList("columnSizeMin").add(new Integer(330));
		            groupFacade.attributeValuesAsList("columnSizeMax").add(new Integer(Integer.MAX_VALUE));
		            groupFacade.attributeValuesAsList("columnBreakAtElement").add(new Integer(Integer.MAX_VALUE));     
		            this.storeElement(
		                segmentIdentity,
		                group
		            );
		        } catch(ServiceException e) {
		        	throw new ResourceException(e);
		        }
	        }
	    }

	    /**
	     * Add elements to attribute pane. The elements are grouped 
	     * according to their order into tabs and field groups and ordered
	     * within the field groups.
	     *  
	     * @param segmentIdentity
	     * @param pane
	     * @param featureDefinitions
	     * @param inspectorClass
	     * @throws ServiceException
	     */
	    private void addElementsToAttributePane(
	        Path segmentIdentity,
	        MappedRecord pane,
	        List<Ui_1.StructuralFeatureDefinition> featureDefinitions,
	        ModelElement_1_0 inspectorClass
	    ) throws ResourceException {
	        Map<Path,MappedRecord> cachedDefinitions = new HashMap<Path,MappedRecord>();
	        Set<String> modifiedGroups = new HashSet<String>();
	        Set<String> modifiedTabs = new HashSet<String>();
            try {
		        String paneName = inspectorClass.getQualifiedName() + ":Pane:Attr";
		        // Process all features
		        for(StructuralFeatureDefinition feature: featureDefinitions) {
		            if(!feature.isReference()) {
		            	ObjectRecord baseDefinition = this.getElementDefinition(
		                    segmentIdentity,
		                    feature,
		                    inspectorClass,
		                    false // no default iconKey for attributes
		                );
	                	Boolean isActive = (Boolean)Object_2Facade.newInstance(baseDefinition).attributeValue("active");
		                if(isActive != null && isActive.booleanValue()) {
		                    List<ObjectRecord> definitions = new ArrayList<ObjectRecord>();
		                    definitions.add(
		                        baseDefinition
		                    );
		                    this.addAlternateElementDefinitions(
		                        baseDefinition,
		                        inspectorClass,
		                        definitions
		                    );
		                    this.addAdditionalElementDefinitions(
		                        baseDefinition,
		                        inspectorClass,
		                        definitions
		                    );
		                    // Map to tab/field group
		                    for(MappedRecord definition: definitions) {
		                    	Object_2Facade definitionFacade = Object_2Facade.newInstance(definition);
		                        Boolean definitionIsActive = Boolean.TRUE;
		                        if(definitionFacade.attributeValue("active") instanceof String) {
		                            SysLog.error("Value of attibute 'active' is not instanceof Boolean", definition);
		                        } else {
		                            definitionIsActive = (Boolean)definitionFacade.attributeValue("active");
		                        }
		                        // Only render if definition is active
		                        if(
		                            (definitionIsActive == null) || // default is true
		                            definitionIsActive.booleanValue()
		                        ) {
		                            List<?> order = Ui_1.this.getOrderFieldGroup(
		                                definition
		                            );
		                            if(order.size() < 3) {
		                                throw new ServiceException(
		                                    BasicException.Code.DEFAULT_DOMAIN,
		                                    BasicException.Code.INVALID_CONFIGURATION,
		                                    "order.size() == 3 for element definitions mapped to tabs/field groups"
		                                );
		                            }
		                            String tabId = order.get(0).toString();
		                            String groupId = order.get(1).toString();
		                            // Get tab (create on demand)
		                            String tabName = paneName + ":Tab:" + tabId;
		                            ObjectRecord tab = Ui_1.this.existingElements.get(segmentIdentity).get(tabName);
		                            if(tab == null) {
		                            	MappedRecord tabElementDefinition = this.getAttributeContainerDefinition(
		                                    segmentIdentity,
		                                    feature,
		                                    order,
		                                    0,
		                                    inspectorClass
		                                );
		                            	Object_2Facade tabElementDefinitionFacade = Object_2Facade.newInstance(tabElementDefinition);
		                                // skip element in case tab is not active
		                                if(!((Boolean)tabElementDefinitionFacade.attributeValue("active")).booleanValue()) {
		                                    continue;
		                                }
		                                tab = Object_2Facade.newInstance(
		                                    segmentIdentity.getDescendant(new String[]{"element", tabName}),
		                                    "org:openmdx:ui1:Tab"
		                                ).getDelegate();   
		                                Ui_1.this.setElementDefault(
		                                    tab,
		                                    tabElementDefinitionFacade.attributeValuesAsList("toolTip"),
		                                    tabElementDefinitionFacade.attributeValuesAsList("changeable").isEmpty() ? 
		                                        Ui_1.this.changableDefaultValue : 
		                                        ((Boolean)tabElementDefinitionFacade.attributeValue("changeable")).booleanValue(),
		                                    "N/A"
		                                );
		                                Object_2Facade.newInstance(tab).attributeValuesAsList("title").addAll(
		                                	tabElementDefinitionFacade.attributeValuesAsList("label")
		                                );
		                                Ui_1.this.setElementDefinitionDefaultLayout(tab);
		                                // Add tab to pane and sort by tabName
		                                List<Object> members = Object_2Facade.newInstance(pane).attributeValuesAsList("member");
		                                int k = 0;
		                                while(
		                                    (k < members.size()) &&
		                                    Object_2Facade.getPath(tab).getLastSegment().toClassicRepresentation().compareTo(((Path)members.get(k)).getLastSegment().toClassicRepresentation()) >= 0
		                                ) {
		                                    k++;
		                                }
		                                members.add(
		                                    k, 
		                                    Object_2Facade.getPath(tab)
		                                );
		                                this.storeElement(
		                                    segmentIdentity,
		                                    tab
		                                );
		                            }
		                            // Get field group (create on demand)
		                            String groupName = tabName + ":Group:" + groupId;
		                            ObjectRecord group = Ui_1.this.existingElements.get(segmentIdentity).get(groupName);
		                            if(group == null) {
		                            	MappedRecord groupElementDefinition = this.getAttributeContainerDefinition(
		                                    segmentIdentity,
		                                    feature,
		                                    order,
		                                    1,
		                                    inspectorClass
		                                );
		                            	Object_2Facade groupElementDefinitionFacade = Object_2Facade.newInstance(groupElementDefinition);
		                                // skip element in case field group is not active
		                                if(!((Boolean)groupElementDefinitionFacade.attributeValue("active")).booleanValue()) {
		                                    continue;
		                                }
		                                group = Object_2Facade.newInstance(
		                                    segmentIdentity.getDescendant(new String[]{"element", groupName}),
		                                    "org:openmdx:ui1:FieldGroup"
		                                ).getDelegate();
		                                Object_2Facade groupFacade = Object_2Facade.newInstance(group);
		                                Ui_1.this.setLabelledFieldDefault(
		                                    group,
		                                    groupElementDefinitionFacade.attributeValuesAsList("label"),
		                                    groupElementDefinitionFacade.attributeValuesAsList("shortLabel"),
		                                    groupElementDefinitionFacade.attributeValuesAsList("toolTip"),
		                                    groupElementDefinitionFacade.attributeValuesAsList("changeable").isEmpty() 
		                                    	? Ui_1.this.changableDefaultValue 
		                                    	: ((Boolean)groupElementDefinitionFacade.attributeValue("changeable")).booleanValue(),
		                                    "N/A",
		                                    (String)groupElementDefinitionFacade.attributeValue("color"),
		                                    (String)groupElementDefinitionFacade.attributeValue("backColor"),
		                                    (String)groupElementDefinitionFacade.attributeValue("cssClassFieldGroup"),
		                                    (String)groupElementDefinitionFacade.attributeValue("cssClassObjectContainer")	                                    
		                                );
		                                Ui_1.this.setElementDefinitionDefaultLayout(group);
		                                groupFacade.attributeValuesAsList("showMaxMember").addAll(
		                                	groupElementDefinitionFacade.attributeValuesAsList("showMaxMember")
		                                );
		                                groupFacade.attributeValuesAsList("inPlaceEditable").addAll(
		                                	groupElementDefinitionFacade.attributeValuesAsList("inPlace")
		                                );
		                                // add group to tab and sort by groupName
		                                List<Object> members = Object_2Facade.newInstance(tab).attributeValuesAsList("member");
		                                int k = 0;
		                                while(
		                                    (k < members.size()) && 
		                                    groupFacade.getPath().getLastSegment().toClassicRepresentation().compareTo(((Path)members.get(k)).getLastSegment().toClassicRepresentation()) >= 0
		                                ) {
		                                    k++;
		                                }
		                                members.add(
		                                    k, 
		                                    groupFacade.getPath()
		                                );
		                                this.storeElement(
		                                    segmentIdentity,
		                                    group
		                                );
		                            }
		                            // get field (create on demand)
		                            String elementName = groupName + ":Field:" + feature.getName();
		                            if(definition != baseDefinition) {
		                                elementName += ":" + definitionFacade.getPath().getLastSegment().toClassicRepresentation();
		                            }
		                            ObjectRecord element = Ui_1.this.existingElements.get(segmentIdentity).get(elementName);
		                            if(element == null) {
		                                element = Object_2Facade.newInstance(
		                                    segmentIdentity.getDescendant(new String[]{"element", elementName})                                    
		                                ).getDelegate();
		                                Ui_1.this.mapField(
		                                    element,
		                                    feature,
		                                    definition,
		                                    false,
		                                    null,
		                                    (String)definitionFacade.attributeValue("defaultValue")
		                                );
		                                this.storeElement(
		                                    segmentIdentity,
		                                    element
		                                );
		                                // add element to group and sort by order
		                                List<Object> members = Object_2Facade.newInstance(group).attributeValuesAsList("member");
		                                int l = 0;
		                                int r = members.size() - 1;
		                                while(l <= r) {
		                                    int pos = (l + r) / 2;
		                                    MappedRecord def = cachedDefinitions.get(members.get(pos));
		                                    if(def == null) {
		                                        SysLog.error("Can not find member in list of cached definitions", Arrays.asList(new Object[]{members.get(pos), cachedDefinitions}));
		                                        break;
		                                    }
		                                    List<?> existing = Ui_1.this.getOrderFieldGroup(def);
		                                    int res = Ui_1.this.compareOrder(
		                                        order, 
		                                        existing
		                                    );
		                                    if(res == 0) { l = pos; r = pos; break;} 
		                                    else if(res > 0) l = pos + 1;
		                                    else if(res < 0) r = pos - 1;
		                                }
		                                if(members.isEmpty()) {
		                                    members.add(
		                                        Object_2Facade.getPath(element)
		                                    );
		                                } else {
		                                    members.add(
		                                        l,
		                                        Object_2Facade.getPath(element)
		                                    );              
		                                }
		                            }    
		                            cachedDefinitions.put(
		                            	Object_2Facade.getPath(element),
		                                definition
		                            );
		                            modifiedGroups.add(
		                            	Object_2Facade.getPath(group).getLastSegment().toClassicRepresentation()
		                            );
		                            modifiedTabs.add(
		                            	Object_2Facade.getPath(tab).getLastSegment().toClassicRepresentation()
		                            );
		                        }
		                    }
		                }
		            }      
		        }
            } catch(ServiceException e) {
            	throw new ResourceException(e);
            }
	        this.updateAndStoreFieldGroups(
	            segmentIdentity,
	            modifiedGroups,
	            cachedDefinitions
	        );
	        // Store tabs
	        for(String tabName: modifiedTabs) {
	        	ObjectRecord tab = Ui_1.this.existingElements.get(segmentIdentity).get(tabName);
	            this.storeElement(
	                segmentIdentity,
	                tab
	            );
	        }
	    }

	    /**
	     * Create inspector element.
	     * 
	     * @param segmentIdentity
	     * @param forClass
	     * @throws ServiceException
	     */
	    private void createInspector(
	        Path segmentIdentity,
	        String forClass
	    ) throws ResourceException {
	    	try {
	    		Model_1_0 model = Model_1Factory.getModel();
		        // Return if inspector already exists or visited in unit of work
		        if(Ui_1.this.existingElements.get(segmentIdentity).get(forClass) != null) {
		            return;
		        }
		        ModelElement_1_0 classDef = model.getElement(forClass);
		        // Create inspector for all supertypes
		        for(
		            Iterator<?> i = classDef.objGetList("allSupertype").iterator();
		            i.hasNext(); 
		        ) {
		            ModelElement_1_0 supertype = model.getElement(i.next());
		            if(supertype != classDef) {
		                this.createInspector(
		                    segmentIdentity,
		                    (String)supertype.getQualifiedName()
		                );
		            }
		        }
		        // Inspector
		        ObjectRecord inspectorDefinition = this.getInspectorElementDefinition(
		            segmentIdentity,
		            new StructuralFeatureDefinition(classDef)
		        );
		        Object_2Facade inspectorDefinitionFacade = Object_2Facade.newInstance(inspectorDefinition);
		        ObjectRecord inspector = Object_2Facade.newInstance(
		            segmentIdentity.getDescendant(new String[]{"element", forClass}),
		            "org:openmdx:ui1:Inspector"
		        ).getDelegate();
		        Object_2Facade inspectorFacade = Object_2Facade.newInstance(inspector);
		        inspectorFacade.attributeValuesAsList("toolTip").addAll(inspectorDefinitionFacade.attributeValuesAsList("toolTip"));
		        inspectorFacade.attributeValuesAsList("changeable").add(
		        	inspectorDefinitionFacade.attributeValuesAsList("changeable").isEmpty() ? 
		            new Boolean(Ui_1.this.changableDefaultValue) : 
		            inspectorDefinitionFacade.attributeValue("changeable")
		        );
		        inspectorFacade.attributeValuesAsList("forClass").add(forClass);
		        inspectorFacade.attributeValuesAsList("scaleX").add(new Integer(1));
		        inspectorFacade.attributeValuesAsList("scaleY").add(new Integer(1));
		        // Get additional inspector definitions
		        List<MappedRecord> inspectorDefinitions = new ArrayList<MappedRecord>();
		        inspectorDefinitions.add(inspectorDefinition);
		        inspectorDefinitions.addAll(
		            this.getAdditionalInspectorDefinitions(
		                inspectorDefinition
		            )
		        );
		        // Add inspector title for each inspector definition
		        int ii = 0;
		        for(
		            Iterator<MappedRecord> i = inspectorDefinitions.iterator();
		            i.hasNext();
		            ii++
		        ) {
		        	MappedRecord definition = i.next();
		        	Object_2Facade definitionFacade = Object_2Facade.newInstance(definition);
		        	ObjectRecord inspectorTitle = Object_2Facade.newInstance(
		                segmentIdentity.getDescendant(new String[]{"element", forClass + ":Title:" + ii}),
		                "org:openmdx:ui1:TextField"
		            ).getDelegate();
		            Ui_1.this.setValuedFieldDefault(
		                inspectorTitle,
		                definitionFacade.attributeValuesAsList("label"),
		                definitionFacade.attributeValuesAsList("shortLabel"),
		                definitionFacade.attributeValuesAsList("displayValueExpr"),
		                definitionFacade.attributeValuesAsList("toolTip"),
		                false,
		                (String)definitionFacade.attributeValue("iconKey"),
		                (String)definitionFacade.attributeValue("color"),
		                (String)definitionFacade.attributeValue("backColor"),
		                (String)definitionFacade.attributeValue("cssClassFieldGroup"),
		                (String)definitionFacade.attributeValue("cssClassObjectContainer"),
		                Multiplicity.SINGLE_VALUE.code(),
		                new Integer(1),
		                new Integer(0),
		                false,
		                false,
		                false,
		                (String)classDef.getName(),
		                (String)classDef.getQualifiedName(),
		                null
		            );
		            this.storeElement(
		                segmentIdentity,
		                inspectorTitle
		            );
		            inspectorFacade.attributeValuesAsList("member").add(
		                Object_2Facade.getPath(inspectorTitle)
		            );
		        }
		        Map<Path,ObjectRecord> panes = new TreeMap<Path,ObjectRecord>();
		        Map<Path,ObjectRecord> elementDefinitions = new HashMap<Path,ObjectRecord>();
		        /**
		         * Operation panes
		         */    
		        List<Ui_1.OperationDefinition> operationDefs = Ui_1.this.getOperationDefinitions(
		            segmentIdentity,
		            classDef
		        );
		        for(OperationDefinition operationDef: operationDefs) {
		            this.addOperationPane(
		                segmentIdentity,
		                panes,
		                operationDef,
		                classDef,
		                elementDefinitions
		            );
		        }
		        this.addOperationPane(
		            segmentIdentity,
		            panes,
		            Ui_1.this.editObjectOperationDef,
		            classDef,
		            elementDefinitions
		        );
		        this.addOperationPane(
		            segmentIdentity,
		            panes,
		            Ui_1.this.deleteObjectOperationDef,
		            classDef,
		            elementDefinitions
		        );
		        this.addOperationPane(
		            segmentIdentity,
		            panes,
		            Ui_1.this.navigateToParentOperationDef,
		            classDef,
		            elementDefinitions
		        );
		        this.addOperationPane(
		            segmentIdentity,
		            panes,
		            Ui_1.this.reloadObjectOperationDef,
		            classDef,
		            elementDefinitions
		        );
		        /**
		         * Attribute pane
		         */
		        MappedRecord paneAttr = this.addPane(
		            segmentIdentity,
		            PANE_TYPE_ATTRIBUTE,
		            classDef,
		            null,
		            panes
		        );
		        // Add features to attribute pane
		        this.addElementsToAttributePane(
		            segmentIdentity,
		            paneAttr,
		            Ui_1.this.getStructuralFeatureDefinitions(
		                segmentIdentity,
		                classDef, 
		                false, // includeSubtypes
		                true // attributesOnly
		            ),
		            classDef
		        );
		        /**
		         * Object container panes (reference panes)
		         */    
		        List<Ui_1.StructuralFeatureDefinition> structuralFeatureDefinitions = Ui_1.this.getStructuralFeatureDefinitions(segmentIdentity, classDef, false, false);
		        for(StructuralFeatureDefinition feature: structuralFeatureDefinitions) {
		            if(feature.isReference()) {
		                this.addReferenceTab(
		                    segmentIdentity,
		                    panes,
		                    feature,
		                    forClass,
		                    classDef,
		                    elementDefinitions
		                );
		            }
		        }
		        // Store all panes and add to inspector
		        for(
		            Iterator<ObjectRecord> i = panes.values().iterator();
		            i.hasNext();
		        ) {
		        	ObjectRecord pane = i.next();
		            inspectorFacade.attributeValuesAsList("member").add(
		                Object_2Facade.getPath(pane)
		            );                
		            this.storeElement(
		                segmentIdentity,
		                pane
		            );
		        }
		        this.storeElement(
		            segmentIdentity,
		            inspector
		        );
	    	} catch(ServiceException e) {
	    		throw new ResourceException(e);
	    	}
	    }

	    /**
	     * Map operation definition to operation pane.
	     * 
	     * @param segmentIdentity
	     * @param panes
	     * @param operationDef
	     * @param inspectorClass
	     * @param elementDefinitions
	     * @throws ServiceException
	     */
	    private void addOperationPane(
	        Path segmentIdentity,
	        Map<Path,ObjectRecord> panes,
	        OperationDefinition operationDef,
	        ModelElement_1_0 inspectorClass,
	        Map<Path,ObjectRecord> elementDefinitions
	    ) throws ResourceException {
	    	Model_1_0 model = Model_1Factory.getModel();
	    	ObjectRecord baseDefinition = this.getOperationDefinition(
	            segmentIdentity,
	            operationDef,
	            null,
	            0,
	            inspectorClass
	        );
	        try {
		        if(((Boolean)Object_2Facade.newInstance(baseDefinition).attributeValue("active")).booleanValue()) {
		            List<ObjectRecord> opDefs = new ArrayList<ObjectRecord>();
		            opDefs.add(baseDefinition);
		            this.addAdditionalElementDefinitions(
		                baseDefinition, 
		                inspectorClass, 
		                opDefs
		            );
		            // Generate operation tabs for base definition and all alternate definitions
		            for(ObjectRecord opDef: opDefs) {
		                // Pane
		            	ObjectRecord pane = this.addPane(
		                    segmentIdentity,
		                    PANE_TYPE_OPERATION,
		                    inspectorClass,
		                    opDef,
		                    panes
		                );
		                // Tab
		                String tabName = Ui_1.this.getPaneName(PANE_TYPE_OPERATION, inspectorClass) + ":Tab:" + operationDef.getName();
		                if(opDef != baseDefinition) {
		                    tabName += ":" + Object_2Facade.getPath(opDef).getLastSegment().toClassicRepresentation();
		                }	    
		                ObjectRecord tab = Ui_1.this.addTab(
		                    tabName,
		                    TAB_TYPE_OPERATION,
		                    opDef,
		                    pane,
		                    elementDefinitions
		                );
		                Object_2Facade tabFacade = Object_2Facade.newInstance(tab);
		                tabFacade.attributeValuesAsList("operationName").add(
		                    operationDef.getQualifiedName()
		                );
		                if(tabFacade.getAttributeValues("isQuery") == null) {
		                	tabFacade.attributeValuesAsList("isQuery").add(
		                        Boolean.valueOf(operationDef.isQuery)
		                    );
		                }
		                // Modeled operation
		                if(operationDef.getModelElement() != null) {
		                    // Map parameter to FieldGroup
		                    for(
		                        Iterator<?> j = operationDef.getModelElement().objGetList("content").iterator();
		                        j.hasNext();
		                    ) {        
		                        // create group and add to tab
		                        ModelElement_1_0 paramDef = model.getElement(j.next());
		                        MappedRecord groupElementDefinition = this.getOperationDefinition(
		                            segmentIdentity,
		                            operationDef,
		                            (String)paramDef.getName(),
		                            1,
		                            inspectorClass
		                        );
		                        Object_2Facade groupElementDefinitionFacade = Object_2Facade.newInstance(groupElementDefinition);
		                        String groupName = tabName + ":Group:" + paramDef.getName();
		                        ObjectRecord group = Object_2Facade.newInstance(
		                            segmentIdentity.getDescendant(new String[]{"element", groupName}),
		                            "org:openmdx:ui1:FieldGroup"
		                        ).getDelegate();
		                        Object_2Facade groupFacade = Object_2Facade.newInstance(group);
		                        Ui_1.this.setLabelledFieldDefault(
		                            group,
		                            groupElementDefinitionFacade.attributeValuesAsList("label"),
		                            groupElementDefinitionFacade.attributeValuesAsList("shortLabel"),
		                            groupElementDefinitionFacade.attributeValuesAsList("toolTip"),
		                            groupElementDefinitionFacade.attributeValuesAsList("changeable").isEmpty() 
		                            	? Ui_1.this.changableDefaultValue 
		                            	: ((Boolean)groupElementDefinitionFacade.attributeValue("changeable")).booleanValue(),
		                            "N/A",
		                            (String)groupElementDefinitionFacade.attributeValue("color"),
		                            (String)groupElementDefinitionFacade.attributeValue("backColor"),
		                            (String)groupElementDefinitionFacade.attributeValue("cssClassFieldGroup"),
		                            (String)groupElementDefinitionFacade.attributeValue("cssClassObjectContainer")
		                        );
		                        Ui_1.this.setElementDefinitionDefaultLayout(group);
		                        tabFacade.attributeValuesAsList("member").add(
		                        	groupFacade.getPath()
		                        );
		                        groupFacade.attributeValuesAsList("showMaxMember").addAll(
		                        	groupElementDefinitionFacade.attributeValuesAsList("showMaxMember")
		                        );
		                        groupFacade.attributeValuesAsList("inPlaceEditable").addAll(
		                        	groupElementDefinitionFacade.attributeValuesAsList("inPlace")
		                        );
		                        // Add fields of parameter to group
		                        ModelElement_1_0 paramType = model.getElementType(
		                            paramDef
		                        );
		                        if(model.isStructureType(paramType)) {
		                            this.addFieldsToObjectContainer(
		                                segmentIdentity,
		                                group,
		                                groupName,
		                                null,
		                                Ui_1.this.getStructFeatureDefinitions(paramType),		              
		                                paramType,
		                                Ui_1.this.getMemberMimeTypes(opDef),
		                                Ui_1.this.getMemberDefaultValues(opDef)
		                            );
		                        }
		                        groupFacade.attributeValuesAsList("columnSizeMin").add(new Integer(330));
		                        groupFacade.attributeValuesAsList("columnSizeMax").add(new Integer(600));
		                        groupFacade.attributeValuesAsList("columnBreakAtElement").add(new Integer(999)); // no break
		                        this.storeElement(
		                            segmentIdentity,
		                            group
		                        );
		                    }
		                }
		                this.storeElement(
		                    segmentIdentity,
		                    tab
		                );
		            }
		        }
	        } catch (ServiceException e) {
	        	throw new ResourceException(e);
	        }
	    }
	    
	    /**
	     * Return element definition of container of containedFeature. level==0 -->
	     * return tab definition, level==1 returns field group definition. inspectorClass
	     * is the class which either defines or inherits containedFeature. Definition
	     * is first looked up for inspectorClass and if not found for defining class of
	     * containedFeature.
	     * 
	     * @param segmentIdentity
	     * @param paneName
	     * @param tabName
	     * @param groupName
	     * @param containedFeature
	     * @param level
	     * @param inspectorClass
	     * @param useDefaultIconKey
	     * @return
	     * @throws ServiceException
	     */
	    private ObjectRecord getFeatureContainerDefinition(
	        Path segmentIdentity,
	        String paneName,
	        String tabName,
	        String groupName,
	        FeatureDefinition containedFeature,
	        int level,
	        ModelElement_1_0 inspectorClass,
	        boolean useDefaultIconKey
	    ) throws ResourceException {
	    	try {
		        // Lookup element definition based on inspector class
		        String containerName = 
		            "" + inspectorClass.getQualifiedName() + ":Pane:" + paneName +
		            ":Tab:" + tabName +
		            (level > 0 ? ":Group:" + groupName : "");
		        ObjectRecord elementDefinition = Ui_1.this.existingElementDefinitions.get(segmentIdentity).get(containerName);
		        if(elementDefinition != null) {
		            return elementDefinition;
		        }
		        // Lookup element definition with defining class of feature
		        Model_1_0 model = Model_1Factory.getModel();
		        ModelElement_1_0 definingClass = model.getElement(
		            containedFeature.getContainer()
		        );        
		        containerName =
		        	"" + definingClass.getQualifiedName() + ":Pane:" + paneName +
			        ":Tab:" + tabName +
			        (level > 0 ? ":Group:" + groupName : "");
		        elementDefinition = Ui_1.this.existingElementDefinitions.get(segmentIdentity).get(containerName);
		        if(elementDefinition != null) {
		            return elementDefinition;
		        }
		        // Fall back to Root perspective
		        if(!"Root".equals(segmentIdentity.getLastSegment().toClassicRepresentation())) {
		            return this.getFeatureContainerDefinition(
		                segmentIdentity.getParent().getChild("Root"),
		                paneName, 
		                tabName, 
		                groupName, 
		                containedFeature, 
		                level, 
		                inspectorClass, 
		                useDefaultIconKey
		            );
		        } else {
		            // Construct a default element definition
		            return this.getElementDefinition(
		                segmentIdentity,
		                containerName,
		                "\"" + containerName + "\"",
		                useDefaultIconKey
		            );
		        }
	    	} catch(ServiceException e) {
	    		throw new ResourceException(e);
	    	}
	    }

	    private MappedRecord getAttributeContainerDefinition(
	        Path segmentIdentity,
	        StructuralFeatureDefinition featureDef,
	        List<?> featureOrder,
	        int level,
	        ModelElement_1_0 inspectorClass
	    ) throws ResourceException {
	        return this.getFeatureContainerDefinition(
	            segmentIdentity,
	            "Attr",
	            "" + featureOrder.get(0),
	            level > 0 ? "" + featureOrder.get(1) : null,
	                featureDef,
	                level,
	                inspectorClass,
	                true // default icon key for attribute containers
	        );
	    }

	    private ObjectRecord getOperationDefinition(
	        Path segmentIdentity,
	        FeatureDefinition operationDef,
	        String parameterName,
	        int level,
	        ModelElement_1_0 inspectorClass
	    ) throws ResourceException {
	        return this.getFeatureContainerDefinition(
	            segmentIdentity,
	            "Op",
	            "" + operationDef.getName(),
	            parameterName,
	            operationDef,
	            level,
	            inspectorClass,
	            true // default icon key for operations
	        );
	    }

	    private ObjectRecord getInspectorElementDefinition(
	        Path segmentIdentity,
	        StructuralFeatureDefinition classDef
	    ) throws ResourceException {
	        return this.getElementDefinition(
	            segmentIdentity,
	            classDef,
	            null,
	            true
	        );    
	    }

	    /**
	     * Get element definition for feature. By default the feature is looked up
	     * with its qualified name in the catalog of element definitions. However, if
	     * inspectorClass is specified then the feature is considered to be a member of
	     * the base class and a lookup is done <qualified name base class>:<feature name>.
	     * If not found the lookup is done <qualified name feature>.
	     * 
	     * @param segmentIdentity
	     * @param feature
	     * @param inspectorClass
	     * @param useDefaultIconKey
	     * @return
	     * @throws ServiceException
	     */
	    private ObjectRecord getElementDefinition(
	        Path segmentIdentity,
	        StructuralFeatureDefinition feature,
	        ModelElement_1_0 inspectorClass,
	        boolean useDefaultIconKey
	    ) throws ResourceException {
	    	try {
		    	ObjectRecord elementDefinition = this.getElementDefinition(
		            segmentIdentity,
		            feature.getQualifiedName(),
		            feature.getName(),
		            useDefaultIconKey
		        );
		        // Overload definition if a definition for inspectorClass exists
		        if(inspectorClass != null) {
		            String qualifiedNameBase = (String)inspectorClass.getQualifiedName() + ":" + feature.getName();
		            if(
		                (Ui_1.this.existingElementDefinitions.get(segmentIdentity).get(qualifiedNameBase) != null) ||
		                (Ui_1.this.existingElementDefinitions.get(segmentIdentity.getParent().getChild("Root")).get(qualifiedNameBase) != null)
		            ) {
		            	MappedRecord overloadDefinition = this.getElementDefinition(
		                    segmentIdentity,
		                    qualifiedNameBase,
		                    feature.getName(),
		                    useDefaultIconKey
		                );
		            	try {
		            		ObjectRecord definition = Object_2Facade.cloneObject(elementDefinition);            	
			            	Object_2Facade definitionFacade = Object_2Facade.newInstance(definition);
			            	Object_2Facade overloadDefinitionFacade = Object_2Facade.newInstance(overloadDefinition);
			            	definitionFacade.setPath(overloadDefinitionFacade.getPath());
			                for(
			                    Iterator<?> i = overloadDefinitionFacade.getValue().keySet().iterator();
			                    i.hasNext();
			                ) {
			                    String attributeName = (String)i.next();
			                    if(!overloadDefinitionFacade.attributeValuesAsList(attributeName).isEmpty()) {
			                    	definitionFacade.attributeValuesAsList(attributeName).clear();
			                    	definitionFacade.attributeValuesAsList(attributeName).addAll(
			                    		overloadDefinitionFacade.attributeValuesAsList(attributeName)
			                        );
			                    }
			                }
			                elementDefinition = definition;
		            	} catch(ResourceException e) {
		            		throw new ServiceException(e);
		            	}
		            }
		        }
		        // Default order is (100000,0,0)
		    	Object_2Facade elementDefinitionFacade;
		        try {
			        elementDefinitionFacade = Object_2Facade.newInstance(elementDefinition);
		        } catch (ResourceException e) {
		        	throw new ServiceException(e);
		        }
		        List<Object> values = elementDefinitionFacade.getAttributeValues("order") == null ?
		        	null :
		        		elementDefinitionFacade.attributeValuesAsList("order");
		        if((values == null) || values.isEmpty()) {
		            values = elementDefinitionFacade.attributeValuesAsList("order");
		            // all customized fields have lower order than non-customized fields
		            values.add(new Integer(100000));
		            values.add(new Integer(0));
		            values.add(new Integer(0));
		        }
		        // Default isSortable=false, isFilterable=false if non-modeled element
		        if(feature.getModelElement() == null) {
		        	if(elementDefinitionFacade.attributeValuesAsList("sortable").isEmpty()) {
			        	elementDefinitionFacade.attributeValuesAsList("sortable").add(Boolean.FALSE);
		        	}
		        	if(elementDefinitionFacade.attributeValuesAsList("filterable").isEmpty()) {
		        		elementDefinitionFacade.attributeValuesAsList("filterable").add(Boolean.FALSE);
		        	}
		        	if(elementDefinitionFacade.attributeValuesAsList("mandatory").isEmpty()) {
			        	elementDefinitionFacade.attributeValuesAsList("mandatory").add(Boolean.FALSE);
		        	}
		        }
		        // Default sizeXWeight
		        if(!feature.isReference()) {
		            values = elementDefinitionFacade.getAttributeValues("sizeXWeight") == null ?
		            	null :
		            		elementDefinitionFacade.attributeValuesAsList("sizeXWeight");
		            if((values == null) || values.isEmpty()) {
		                // set default to weight 3
		            	elementDefinitionFacade.attributeValuesAsList("sizeXWeight").add(new Integer(3));
		            }
		        }
		        return elementDefinition;
	    	} catch(ServiceException e) {
	    		throw new ResourceException(e);
	    	}
	    }
	    
	    private void assertCachedElements(
	        Path segmentIdentity
	    ) throws ResourceException {    	
	    	try {
	    		Model_1_0 model = Model_1Factory.getModel();
		        // Pre-fetch all elements
		        if(Ui_1.this.existingElements.get(segmentIdentity) == null) {
		        	ResultRecord objects = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
		        	super.find(
		        		ISPECS.GET,
		        		this.newQuery(segmentIdentity.getChild("element")),
		        		objects
		        	);
		            Map<String,ObjectRecord> existingElements = new HashMap<String,ObjectRecord>();
		            Ui_1.this.existingElements.put(
		                segmentIdentity,
		                existingElements
		            );
		            for(Object object: objects) {
	            		ObjectRecord element = (ObjectRecord)object;
	                    existingElements.put(
	                    	element.getResourceIdentifier().getLastSegment().toClassicRepresentation(),
	                    	Object_2Facade.cloneObject(element)
	                    );
		            }
		        }
		        // Prefetch all element definitions
		        if(Ui_1.this.existingElementDefinitions.get(segmentIdentity) == null) {
		        	ResultRecord objects = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
		        	super.find(
		        		ISPECS.GET, 
		        		this.newQuery(segmentIdentity.getChild("elementDefinition")), 
		        		objects
		        	);
		            Map<String,ObjectRecord> existingElementDefinitions = new HashMap<String,ObjectRecord>();
		            Ui_1.this.existingElementDefinitions.put(
		                segmentIdentity,
		                existingElementDefinitions
		            );
		            for(Object object: objects) {
	            		ObjectRecord elementDefinition = (ObjectRecord)object;
	                    existingElementDefinitions.put(
	                    	elementDefinition.getResourceIdentifier().getLastSegment().toClassicRepresentation(),
	                        Object_2Facade.cloneObject(elementDefinition)
	                    );
		            }
		        }
		        // Prepare feature definitions
		        List<Ui_1.StructuralFeatureDefinition> structuralFeatureDefinitions = new ArrayList<Ui_1.StructuralFeatureDefinition>();
		        Ui_1.this.structuralFeatureDefinitions.put(
		            segmentIdentity,
		            structuralFeatureDefinitions
		        );
		        List<Ui_1.OperationDefinition> operationDefinitions = new ArrayList<Ui_1.OperationDefinition>();
		        Ui_1.this.operationDefinitions.put(
		            segmentIdentity,
		            operationDefinitions
		        );
	        	ResultRecord objects = Records.getRecordFactory().createIndexedRecord(ResultRecord.class);
	        	super.find(
	        		ISPECS.GET, 
	        		this.newQuery(segmentIdentity.getChild("featureDefinition")), 
	        		objects
	        	);
		        for(Object featureDefinition: objects) {
		        	Object_2Facade featureDefinitionFacade = Object_2Facade.newInstance((ObjectRecord)featureDefinition);
		            String qualifiedName = featureDefinitionFacade.getPath().getLastSegment().toClassicRepresentation();
		            String name = qualifiedName.substring(qualifiedName.lastIndexOf(":") + 1);
		            String className = qualifiedName.substring(0, qualifiedName.lastIndexOf(":"));
		            if("org:openmdx:ui1:StructuralFeatureDefinition".equals(featureDefinitionFacade.getObjectClass())) {
		                String typeName = (String)featureDefinitionFacade.attributeValue("type");
		                try {
		                    structuralFeatureDefinitions.add(
		                        new StructuralFeatureDefinition(
		                            name,
		                            qualifiedName,
		                            model.getElement(typeName),
		                            model.getElement(className),
		                            (String)featureDefinitionFacade.attributeValue("multiplicity"),
		                            (Boolean)featureDefinitionFacade.attributeValue("changeable"),
		                            (Boolean)featureDefinitionFacade.attributeValue("isReference")
		                        )
		                    );
		                } catch(ServiceException e) {
		                    SysLog.warning("Unable to register structural feature definition (ignoring)", Arrays.asList(new String[]{name, qualifiedName, className, typeName}));
		                    e.log();
		                }
		            } else if("org:openmdx:ui1:OperationDefinition".equals(featureDefinitionFacade.getObjectClass())) {
		                Boolean isQuery = (Boolean)featureDefinitionFacade.attributeValue("isQuery");
		                try {
		                    operationDefinitions.add(
		                        new OperationDefinition(
		                            name,
		                            qualifiedName,
		                            isQuery == null ? 
		                                true : 
		                                isQuery.booleanValue(),
		                            model.getElement(className)
		                        )
		                    );
		                } catch(ServiceException e) {
		                    SysLog.warning("Unable to register operation definition (ignoring)", Arrays.asList(new String[]{name, qualifiedName, className}));
		                    e.log();
		                }
		            } else {
		                SysLog.warning("Unable to register feature definition (ignoring)", Arrays.asList(new String[]{name, qualifiedName, className}));               
		            }
		        }
	    	} catch(ServiceException e) {
	    		throw new ResourceException(e);
	    	}
	    }

	    /**
	     * Add fields to container. Container's members are possibly modified.
	     * Caller must store container.
	     * 
	     * @param segmentIdentity
	     * @param container
	     * @param containerName
	     * @param showMemberRange
	     * @param featureDefinitions
	     * @param inspectorClass
	     * @param memberMimeTypes
	     * @param memberDefaultValues
	     * @throws ServiceException
	     */
	    private void addFieldsToObjectContainer(
	        Path segmentIdentity,
	        MappedRecord container,
	        String containerName,
	        List<Object> showMemberRange,
	        List<Ui_1.StructuralFeatureDefinition> featureDefinitions,
	        ModelElement_1_0 inspectorClass,
	        Map<?,?> memberMimeTypes,
	        Map<?,?> memberDefaultValues
	    ) throws ResourceException {
	        /**
	         * Iterate all features and create tabs and groups according to
	         * the customized ordering of feature. If feature or ordering is
	         * not customized make sure that features of inspectorClass appear first
	         * (--> must be at the end of orderedFeatures because of quick sort below)
	         */
	        List<Ui_1.StructuralFeatureDefinition> orderedFeatures = new ArrayList<Ui_1.StructuralFeatureDefinition>();
	        StructuralFeatureDefinition featureN = null; // feature with name="name"
	        StructuralFeatureDefinition featureD = null; // feature with name="description"
	        StructuralFeatureDefinition featureT = null; // feature with name="title"
	        try {
		        for(StructuralFeatureDefinition featureDefinition: featureDefinitions) {
		            // feature is member of inspectorClass
		            if(
		                (featureDefinition.getModelElement() != null) &&
		                inspectorClass.objGetList("content").contains(featureDefinition.getModelElement().jdoGetObjectId())
		            ) {            
		                String featureName = (String)featureDefinition.getName();
		                if("name".equals(featureName)) {
		                    featureN = featureDefinition;
		                } else if("description".equals(featureName)) {
		                    featureD = featureDefinition;
		                } else if("title".equals(featureName)) {
		                    featureT = featureDefinition;
		                } else {
		                    orderedFeatures.add(
		                        featureDefinition
		                    );
		                }
		            } else {
		                orderedFeatures.add(
		                    0, 
		                    featureDefinition
		                );            
		            }
		        }
		        if(featureT != null) orderedFeatures.add(featureT);
		        if(featureD != null) orderedFeatures.add(featureD);
		        if(featureN != null) orderedFeatures.add(featureN);
		        // Process all features 
		        Map<Path,MappedRecord> elementDefinitions = new HashMap<Path,MappedRecord>();
		        for(StructuralFeatureDefinition feature: orderedFeatures) {
		            if(Ui_1.this.isMemberOfObjectContainer(feature)) {
		            	MappedRecord elementDefinition = this.getElementDefinition(
		                    segmentIdentity,
		                    feature,
		                    inspectorClass,
		                    false // no default iconKey for fields
		                );
		                List<?> order = Ui_1.this.getOrderObjectContainer(
		                    elementDefinition
		                );
		                try {
		                	Boolean isActive = elementDefinition == null ? null : (Boolean)Object_2Facade.newInstance(elementDefinition).attributeValuesAsList("active").get(0);
			                if(isActive != null && isActive.booleanValue()) {
			                    String elementName = containerName + ":" + feature.getName();
			                    ObjectRecord element = Object_2Facade.newInstance(
			                        segmentIdentity.getDescendant(new String[]{"element", elementName})
			                    ).getDelegate();
			                    Ui_1.this.mapField(
			                        element,
			                        feature,
			                        elementDefinition,
			                        true,
			                        memberMimeTypes == null ? 
			                            null :
			                            (String)memberMimeTypes.get(feature.getName()),
			                        memberDefaultValues == null ? 
			                            null : 
			                            (String)memberDefaultValues.get(feature.getName())                  
			                    );
			                    if(Object_2Facade.getObjectClass(element) != null) {
			                        // Test whether element is in showMemberRange
			                        List<Object> elementOrder = Ui_1.this.getOrderObjectContainer(elementDefinition);
			                        boolean isInShowMemberRange = false;
			                        if(
			                            (showMemberRange != null) && 
			                            !showMemberRange.isEmpty()
			                        ) {
			                            for(Object r: showMemberRange) {
			                                String range = (String)r;
			                                List<Integer> orderFrom = null;
			                                List<Integer> orderTo = null;
			                                if(range.indexOf("-") > 0) {
			                                    orderFrom = new ArrayList<Integer>();
			                                    for(String e: range.substring(0, range.indexOf("-")).split(":")) {
			                                        orderFrom.add(Integer.valueOf(e));
			                                    }
			                                    orderTo = new ArrayList<Integer>();
			                                    for(String e: range.substring(range.indexOf("-") + 1).split(":")) {
			                                        orderTo.add(Integer.valueOf(e));
			                                    }
			                                } else {
			                                    orderFrom = new ArrayList<Integer>();
			                                    for(String e: range.split(":")) {
			                                        orderFrom.add(Integer.valueOf(e));
			                                    }
			                                    orderTo = null;
			                                }
			                                if(
			                                    (Ui_1.this.compareOrder(elementOrder, orderFrom) >= 0) &&
			                                    ((orderTo == null) || Ui_1.this.compareOrder(elementOrder, orderTo) <= 0)
			                                ) {
			                                    isInShowMemberRange = true;
			                                    break;
			                                }
			                            }
			                        } else {
			                            isInShowMemberRange = true;
			                        }
			                        if(isInShowMemberRange) {
			                            this.storeElement(
			                                segmentIdentity,
			                                element
			                            );                
			                            elementDefinitions.put(
			                                Object_2Facade.getPath(element),
			                                elementDefinition
			                            );
			                            // add element to container and sort by order
			                            List<Object> members;
			                            try {
			                                members = Object_2Facade.newInstance(container).attributeValuesAsList("member");
			                            } catch (ResourceException e) {
			                            	throw new ServiceException(e);
			                            }
			                            int l = 0;
			                            int r = members.size() - 1;
			                            while(l <= r) {
			                                int pos = (l + r) / 2;
			                                List<?> existing = Ui_1.this.getOrderObjectContainer(
			                                    (MappedRecord)elementDefinitions.get(members.get(pos))
			                                );
			                                int res = Ui_1.this.compareOrder(
			                                    order, 
			                                    existing
			                                );
			                                if(res == 0) { l = pos; r = pos; break;} 
			                                else if(res > 0) l = pos + 1;
			                                else if(res < 0) r = pos - 1;
			                            }
			                            if(members.isEmpty()) {
			                                members.add(
			                                    Object_2Facade.getPath(element)
			                                );
			                            } else {
			                                members.add(
			                                    l,
			                                    Object_2Facade.getPath(element)
			                                );
			                            }
			                        }
			                    }
			                }
		                } catch (NumberFormatException e) {
		                	throw new ResourceException(e);
		                }
		            }
		        }
	        } catch(ServiceException e) {
		        throw new ResourceException(e);
		    }
	    }

	    /* (non-Javadoc)
	     * @see org.openmdx.base.rest.spi.AbstractRestInteraction#create(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.ObjectRecord, org.openmdx.base.rest.cci.ResultRecord)
	     */
	    @Override
	    public boolean create(
	        RestInteractionSpec ispec, 
	        ObjectRecord input, 
	        ResultRecord output
	    ) throws ResourceException {
	    	try {
		        Ui_1.this.existingElementDefinitions.clear();
		        Ui_1.this.existingElements.clear();
		        Object_2Facade facade = Object_2Facade.newInstance(input);
		        String objectClass = facade.getObjectClass();
		        if("org:openmdx:ui1:StructuralFeatureDefinition".equals(objectClass)) {
		        	if(facade.getAttributeValues("isReference") == null) {
		        		facade.attributeValuesAsList("isReference").add(Boolean.FALSE);
		        	}
		        }
		        return super.create(
		            ispec,
		            input,
		            output
		        );
	    	} catch(ServiceException e) {
	    		throw new ResourceException(e);
	    	}
	    }
	
	    /* (non-Javadoc)
	     * @see org.openmdx.base.rest.spi.AbstractRestInteraction#delete(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.ObjectRecord)
	     */
	    protected boolean delete(
	        RestInteractionSpec ispec, 
	        ObjectRecord input
	    ) throws ResourceException {
	    	Ui_1.this.existingElementDefinitions.clear();
	    	Ui_1.this.existingElements.clear();
	        return super.delete(
	            ispec,
	            input
	        );
	    }

	    /* (non-Javadoc)
	     * @see org.openmdx.base.rest.spi.AbstractRestInteraction#update(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.ObjectRecord, org.openmdx.base.rest.cci.ResultRecord)
	     */
	    @Override
		public boolean update(
			RestInteractionSpec ispec, 
			ObjectRecord input,
			ResultRecord output
		) throws ResourceException {
	    	Ui_1.this.existingElementDefinitions.clear();
	    	Ui_1.this.existingElements.clear();
	        return super.update(
	            ispec,
	            input,
	            output
	        );
	    }

	    
	    /* (non-Javadoc)
		 * @see org.openmdx.base.rest.spi.AbstractRestInteraction#get(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.QueryRecord, org.openmdx.base.rest.cci.ResultRecord)
		 */
        @Override
        protected boolean get(
            RestInteractionSpec ispec,
            QueryRecord input,
            ResultRecord output
        ) throws ResourceException {
	        return super.get(
	        	ispec, 
	        	input, 
	        	output
	        );
        }

		/* (non-Javadoc)
	     * @see org.openmdx.base.rest.spi.AbstractRestInteraction#find(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.QueryRecord, org.openmdx.base.rest.cci.ResultRecord)
	     */
	    @SuppressWarnings("unchecked")
		@Override
		protected boolean find(
			RestInteractionSpec ispec, 
			QueryRecord input,
			ResultRecord output
		) throws ResourceException {
	    	Path requestPath = input.getResourceIdentifier();
            Model_1_0 model = Model_1Factory.getModel();
	        if("assertableInspector".equals(requestPath.getLastSegment().toClassicRepresentation())) {
	            Path segmentIdentity = requestPath.getPrefix(5);
	            this.assertCachedElements(segmentIdentity);
	            List<MappedRecord> assertableInspectors = new ArrayList<MappedRecord>();  
            	try {
		            // Return assertableInspector for all loaded classes
		            for(
		                Iterator<?> i = model.getContent().iterator(); 
		                i.hasNext();
		            ) {
		                ModelElement_1_0 elementDef = (ModelElement_1_0)i.next();
		                if(model.isClassType(elementDef)) {
		                	MappedRecord assertableInspector = Object_2Facade.newInstance(
		                		requestPath.getChild((String)elementDef.getQualifiedName()),
		                        "org:openmdx:ui1:AssertableInspector"
		                    ).getDelegate();
		                    ObjectRecord inspectorDefinition = this.getInspectorElementDefinition(
		                        segmentIdentity,
		                        new StructuralFeatureDefinition(elementDef)
		                    );
		                    Object_2Facade assertableInspectorFacade = Object_2Facade.newInstance(assertableInspector);
		                    Object_2Facade inspectorDefinitionFacade = Object_2Facade.newInstance(inspectorDefinition);
		                    this.getAdditionalInspectorDefinitions(
		                        inspectorDefinition
		                    );
		                    assertableInspectorFacade.attributeValuesAsList("forClass").add(
		                        elementDef.getQualifiedName()
		                    );
		                    // label
		                    assertableInspectorFacade.attributeValuesAsList("label").addAll(
		                    	inspectorDefinitionFacade.attributeValuesAsList("label")
		                    );
		                    // toolTip
		                    assertableInspectorFacade.attributeValuesAsList("toolTip").addAll(
		                    	inspectorDefinitionFacade.attributeValuesAsList("toolTip")
		                    );
		                    // changeable
		                    assertableInspectorFacade.attributeValuesAsList("changeable").addAll(
		                    	inspectorDefinitionFacade.attributeValuesAsList("changeable")
		                    );
		                    if(assertableInspectorFacade.attributeValuesAsList("changeable").isEmpty()) {
		                    	assertableInspectorFacade.attributeValuesAsList("changeable").add(Boolean.TRUE);
		                    }
		                    // filterable
		                    assertableInspectorFacade.attributeValuesAsList("filterable").addAll(
		                    	inspectorDefinitionFacade.attributeValuesAsList("filterable")
		                    );
		                    if(assertableInspectorFacade.attributeValuesAsList("filterable").isEmpty()) {
		                    	assertableInspectorFacade.attributeValuesAsList("filterable").add(Boolean.TRUE);
		                    }
		                    // sortable
		                    assertableInspectorFacade.attributeValuesAsList("sortable").addAll(
		                    	inspectorDefinitionFacade.attributeValuesAsList("sortable")
		                    );
		                    if(assertableInspectorFacade.attributeValuesAsList("sortable").isEmpty()) {
		                    	assertableInspectorFacade.attributeValuesAsList("sortable").add(Boolean.TRUE);
		                    }
		                    // color
		                    assertableInspectorFacade.attributeValuesAsList("color").addAll(
		                    	inspectorDefinitionFacade.attributeValuesAsList("color")
		                    );
		                    // backColor
		                    assertableInspectorFacade.attributeValuesAsList("backColor").addAll(
		                    	inspectorDefinitionFacade.attributeValuesAsList("backColor")
		                    );
		                    // iconKey
		                    assertableInspectorFacade.attributeValuesAsList("iconKey").addAll(
		                    	inspectorDefinitionFacade.attributeValuesAsList("iconKey")
		                    );
		                    // order
		                    assertableInspectorFacade.attributeValuesAsList("order").addAll(
		                    	inspectorDefinitionFacade.attributeValuesAsList("order")
		                    );
		                    assertableInspectors.add(
		                        assertableInspector
		                    );
		                }
		            }
            	} catch(ServiceException e) {
            		throw new ResourceException(e);
            	}
	            if(input.getPosition() >= assertableInspectors.size()) {
		            output.setHasMore(Boolean.FALSE);
		            output.setTotal(0);            	
	            } else {
            		output.addAll(assertableInspectors);
		            output.setHasMore(Boolean.FALSE);
		            output.setTotal(assertableInspectors.size());
	            }
	            return true;
	        } else {
	            return super.find(
	                ispec,
	                input,
	                output
	            );
	        }
	    }

	    /* (non-Javadoc)
	     * @see org.openmdx.base.rest.spi.AbstractRestInteraction#invoke(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.MessageRecord, org.openmdx.base.rest.cci.MessageRecord)
	     */
	    @Override
	    protected boolean invoke(
	        RestInteractionSpec ispec, 
	        MessageRecord input,
	        MessageRecord output
	    ) throws ResourceException {
    		Path requestPath = input.getResourceIdentifier();
	        String operationName = requestPath.getSegment(requestPath.size() - 2).toClassicRepresentation();
	        MappedRecord body = input.getBody();
	        if("assertInspector".equals(operationName)) {
	            try {
	                Path segmentIdentity = requestPath.getPrefix(5);
	                String forClass = (String)body.get("forClass");
	                MappedRecord inspectorDef = null;
                    inspectorDef = this.retrieveObject(
                        segmentIdentity.getDescendant(new String[]{"element", forClass})
                    );
	                if(inspectorDef == null) {
	                    // Always assert perspective Root
	                    if(!"Root".equals(segmentIdentity.getLastSegment().toClassicRepresentation())) {
	                        Path rootSegmentIdentity = segmentIdentity.getParent().getChild("Root");
	                        try {
	                            this.retrieveObject(
	                                rootSegmentIdentity.getDescendant(new String[]{"element", forClass})
	                            );
	                        } catch(Exception e) {
	                        	this.assertCachedElements(
	                                rootSegmentIdentity
	                            );
	                        	this.createInspector(
	                                rootSegmentIdentity,
	                                forClass
	                            );
	                        }
	                    }
	                    this.assertCachedElements(
	                        segmentIdentity
	                    );
	                    this.createInspector(
	                        segmentIdentity,
	                        forClass
	                    );
	                    this.createInspector(
	                        segmentIdentity.getParent().getChild("Root"),
	                        forClass
	                    );                        
	                }
	            } catch(Exception e) {
					throw ResourceExceptions.initHolder(
						new ResourceException(
		                    "Inspector for class " + body.get("forClass") + " can not be created",
							BasicException.newEmbeddedExceptionStack(
			                    e,
			                    BasicException.Code.DEFAULT_DOMAIN,
			                    BasicException.Code.CREATION_FAILURE,
			                    new BasicException.Parameter("typeName", "org:openmdx:ui1:CanNotCreateInspector"),
			                    new BasicException.Parameter("reason", e.getMessage())
			                )
			            )
					);
	            }
	            Object_2Facade result = Object_2Facade.newInstance(
	            	requestPath.getDescendant(
	                    new String[]{ "reply", UUIDs.newUUID().toString()}
	                ),
	                "org:openmdx:base:Void"
	            );		            
	            output.setResourceIdentifier(result.getPath());
	            output.setBody(result.getValue());
	            return true;
	        }
			throw ResourceExceptions.initHolder(
				new ResourceException(
		            "operation not supported",
					BasicException.newEmbeddedExceptionStack(
			            BasicException.Code.DEFAULT_DOMAIN,
			            BasicException.Code.NOT_SUPPORTED,
			            new BasicException.Parameter("operation", operationName)
			        )
			    )
	        );
    	}
	    
	    private final InteractionSpecs ISPECS = InteractionSpecs.getRestInteractionSpecs(false);

    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------  
    public static final int DEFAULT_MAX_MEMBER = 6;

    
    // names of auto-generated operations
    public static final String EDIT_OBJECT_OPERATION_NAME = "org:openmdx:base:BasicObject:edit";
    public static final String DELETE_OBJECT_OPERATION_NAME = "org:openmdx:base:BasicObject:delete";
    public static final String RELOAD_OBJECT_OPERATION_NAME = "org:openmdx:base:BasicObject:reload";
    public static final String NAVIGATE_TO_PARENT_OPERATION_NAME = "org:openmdx:base:BasicObject:navigateToParent";

    protected static final String PANE_TYPE_OPERATION = "org:openmdx:ui1:OperationPane";
    protected static final String PANE_TYPE_ATTRIBUTE = "org:openmdx:ui1:AttributePane";
    protected static final String PANE_TYPE_REFERENCE = "org:openmdx:ui1:ReferencePane";

    protected static final String TAB_TYPE_OPERATION = "org:openmdx:ui1:OperationTab";

    protected static final List<String> REFERENCES_TO_EXCLUDE = Arrays.asList(
        "org:openmdx:base:Segment:extent"
    );  
    protected Map<Path,Map<String,ObjectRecord>> existingElementDefinitions =
        new HashMap<Path,Map<String,ObjectRecord>>();
    protected Map<Path,Map<String,ObjectRecord>> existingElements =
        new HashMap<Path,Map<String,ObjectRecord>>();
    protected Map<Path,List<Ui_1.StructuralFeatureDefinition>> structuralFeatureDefinitions = 
        new HashMap<Path,List<Ui_1.StructuralFeatureDefinition>>();
    protected Map<Path,List<Ui_1.OperationDefinition>> operationDefinitions =
        new HashMap<Path,List<Ui_1.OperationDefinition>>();
    protected boolean changableDefaultValue = true;	
	protected OperationDefinition editObjectOperationDef = null;
    protected OperationDefinition deleteObjectOperationDef = null;
    protected OperationDefinition reloadObjectOperationDef = null;
    protected OperationDefinition navigateToParentOperationDef = null;

}

//--- End of File -----------------------------------------------------------
