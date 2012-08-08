/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: Ui_1.java,v 1.61 2009/03/08 18:03:23 wfro Exp $
 * Description: Ui_1 plugin
 * Revision:    $Revision: 1.61 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/08 18:03:23 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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

import javax.jmi.reflect.RefException;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.application.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.Directions;
import org.openmdx.application.dataprovider.cci.RequestCollection;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.application.dataprovider.spi.Layer_1_0;
import org.openmdx.application.mof.cci.AggregationKind;
import org.openmdx.application.mof.cci.Multiplicities;
import org.openmdx.application.mof.cci.PrimitiveTypes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_3;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.PathComponent;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * Implementation of the model org:openmdx:ui1.
 */
public class Ui_1 extends Layer_1 {

    //------------------------------------------------------------------------
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
            this.name = (String)element.objGetValue("name");
            this.qualifiedName = (String)element.objGetValue("qualifiedName");
            this.container = element.objGetValue("container");
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

        public Object getContainer(
        ) {
            return this.container;
        }

        protected final String name;
        protected final String qualifiedName;
        protected final Object container;
        protected final ModelElement_1_0 modelElement;

    }

    //------------------------------------------------------------------------
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
            this.isReference = isReference == null ? 
                false : 
                isReference.booleanValue();
            this.isReferenceStoredAsAttribute = false;
        }

        public StructuralFeatureDefinition(
            ModelElement_1_0 element
        ) throws ServiceException {
            super(element);
            this.type = element.objGetValue("type");
            this.multiplicity = (String)element.objGetValue("multiplicity");
            this.isChangeable = (element.objGetValue("isChangeable") == null) || (element.objGetList("isChangeable").isEmpty()) ? 
                new Boolean(Ui_1.this.changableDefaultValue) : 
                (Boolean)element.objGetValue("isChangeable");
            if(Ui_1.this.model.isAttributeType(element) || Ui_1.this.model.isStructureFieldType(element)) {
                this.isReference = false;
                this.isReferenceStoredAsAttribute = false;
            }
            else if(Ui_1.this.model.isReferenceType(element)) {
                ModelElement_1_0 referencedEnd = Ui_1.this.model.getElement(element.objGetValue("referencedEnd"));
                ModelElement_1_0 exposedEnd = Ui_1.this.model.getElement(element.objGetValue("exposedEnd"));
                // A reference is handled as attribute in case of a single-valued reference with aggregation=none
                this.isReference = 
                    !referencedEnd.objGetList("qualifierName").isEmpty() || 
                    !exposedEnd.objGetList("qualifierName").isEmpty();
                this.isReferenceStoredAsAttribute = Ui_1.this.model.referenceIsStoredAsAttribute(element);
            }
            else {
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

        public Object getType(
        ) {
            return this.type;
        }

        public boolean isReference(
        ) {
            return this.isReference;
        }

        public boolean isReferenceStoredAsAttribute(
        ) {
            return this.isReferenceStoredAsAttribute;
        }

        private final Object type;
        private final String multiplicity;
        private final Boolean isChangeable;
        private final boolean isReference;
        private final boolean isReferenceStoredAsAttribute;

    }

    //------------------------------------------------------------------------
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
            this.isQuery = element.objGetList("isQuery").isEmpty()
            ? true
                : ((Boolean)element.objGetValue("isQuery")).booleanValue();
        }

        public boolean isQuery(
        ) {
            return this.isQuery;
        }

        private final boolean isQuery;

    }

    //------------------------------------------------------------------------
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws ServiceException {
        super.activate(
            id, 
            configuration, 
            delegation
        );
        // Get model
        List models = configuration.values(SharedConfigurationEntries.MODEL);
        if (models.size() > 0) {
            this.model = (Model_1_3) models.get(0);
        }
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "A model must be configured with options 'modelPackage' and 'packageImpl'"
            );
        }
        // Get changableDefaultValue
        if (configuration.containsEntry("changableDefaultValue")) {
            this.changableDefaultValue = configuration.isOn("changableDefaultValue");
        }
        this.editObjectOperationDef = this.createBasicObjectOperationDef(EDIT_OBJECT_OPERATION_NAME, false);
        this.deleteObjectOperationDef = this.createBasicObjectOperationDef(DELETE_OBJECT_OPERATION_NAME, false);
        this.reloadObjectOperationDef = this.createBasicObjectOperationDef(RELOAD_OBJECT_OPERATION_NAME, true);
        this.navigateToParentOperationDef = this.createBasicObjectOperationDef(NAVIGATE_TO_PARENT_OPERATION_NAME, true);

    }

    //------------------------------------------------------------------------
    public void prolog(
        ServiceHeader header,
        DataproviderRequest[] requests
    ) throws ServiceException {
        super.prolog(
            header,
            requests
        );
        this.delegation = new RequestCollection(
            new ServiceHeader(),
            this.getDelegation()
        );    

    }

    //------------------------------------------------------------------------
    // Ui_1
    //------------------------------------------------------------------------

    //------------------------------------------------------------------------
    private void storeElement(
        Path segmentIdentity,
        DataproviderObject element
    ) throws ServiceException {
        this.delegation.addSetRequest(
            element
        );
        this.existingElements.get(segmentIdentity).put(
            element.path().getBase(),
            element
        );
    }

    //------------------------------------------------------------------------
    private void storeElementDefinition(
        Path segmentIdentity,
        DataproviderObject elementDefinition
    ) throws ServiceException {
        this.delegation.addSetRequest(
            elementDefinition
        );
        this.existingElementDefinitions.get(segmentIdentity).put(
            elementDefinition.path().getBase(),
            elementDefinition
        );
    }

    //-------------------------------------------------------------------------
    private void setElementDefault(
        DataproviderObject_1_0 f,
        List<Object> toolTip,
        boolean isChangeable,
        String iconKey 
    ) throws ServiceException {
        f.values("toolTip").addAll(toolTip);
        f.values("changeable").add(new Boolean(isChangeable));
        f.values("autoGenerated").add(new Boolean(true));
        if(iconKey != null) {
            f.values("iconKey").add(iconKey);
        }
    }

    //-------------------------------------------------------------------------
    private void setAbstractFieldDefault(
        DataproviderObject_1_0 f,
        List<Object> toolTip,
        boolean isChangeable,
        String iconKey,
        String color,
        String backColor
    ) throws ServiceException {
        this.setElementDefault(
            f,
            toolTip,
            isChangeable,
            iconKey
        );
        if(color != null) {
            f.values("color").add(color);
        }
        if(backColor != null) {
            f.values("backColor").add(backColor);
        }
        f.values("fontName").add("Tahoma");
        f.values("fontSize").add(new BigDecimal(8.25));
        f.values("fontBold").add(new Boolean(false));
        f.values("fontItalic").add(new Boolean(false));
        f.values("fontStrikeout").add(new Boolean(false));
        f.values("fontUnderline").add(new Boolean(false));
    }

    //-------------------------------------------------------------------------
    private void setLabelledFieldDefault(
        DataproviderObject_1_0 f,
        List<Object> labels,
        List<Object> shortLabels,
        List<Object> toolTips,
        boolean isChangeable,
        String iconKey,
        String color,
        String backColor
    ) throws ServiceException {
        List<Object> toolTipDefault = new ArrayList<Object>(toolTips);
        for(int i = 0; i < toolTipDefault.size(); i++) {
            // Set label as default toolTip
            if(
                ((toolTipDefault.get(i) == null) || ((String)toolTipDefault.get(i)).length() == 0) &&
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
                ((shortLabelDefault.get(i) == null) || ((String)shortLabelDefault.get(i)).length() == 0) &&
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
            backColor
        );
        f.values("label").addAll(labels);
        f.values("shortLabel").addAll(shortLabelDefault);
    }

    //-------------------------------------------------------------------------
    private void setValuedFieldDefault(
        DataproviderObject_1_0 f,
        List<Object> labels,
        List<Object> shortLabels,
        List<Object> displayValueExprs,
        List<Object> toolTips,
        boolean isChangeable,
        String iconKey,
        String color,
        String backColor,
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
            backColor
        );
        f.values("multiplicity").add(multiplicity);
        f.values("spanRow").add(spanRow);
        f.values("skipRow").add(skipRow);
        f.values("displayValueExpr").addAll(displayValueExprs);
        f.values("filterable").add(new Boolean(filterable));
        f.values("sortable").add(new Boolean(sortable));
        f.values("mandatory").add(new Boolean(mandatory));
        f.values("featureName").add(featureName);
        f.values("qualifiedFeatureName").add(qualifiedFeatureName);
        if(dataBindingName != null) {
            f.values("dataBindingName").add(dataBindingName);
        }
    }

    //-------------------------------------------------------------------------
    private void setObjectReference(
        DataproviderObject objectReference,
        StructuralFeatureDefinition feature
    ) throws ServiceException {
        ModelElement_1_0 featureType = this.model.getDereferencedType(
            feature.getType()
        );
        objectReference.values("referenceName").add(feature.getName());
        objectReference.values("referencedTypeName").add(featureType.objGetValue("qualifiedName"));
        // Reference
        if(feature.isReference()) {
            objectReference.values("referenceIsStoredAsAttribute").add(
                Boolean.valueOf(feature.isReferenceStoredAsAttribute)
            );
            ModelElement_1_0 element = feature.getModelElement();
            if(element != null) {
                ModelElement_1_0 referencedEnd = this.model.getElement(
                    element.objGetValue("referencedEnd")
                );
                if(!referencedEnd.objGetList("qualifierName").isEmpty()) {
                    String qualifierName = (String)referencedEnd.objGetValue("qualifierName");           
                    objectReference.values("userDefinedQualifier").add(
                        new Boolean(!"id".equals(qualifierName))
                    );
                    objectReference.values("qualifierLabel").add(qualifierName);
                }
                else {
                    objectReference.values("userDefinedQualifier").add(
                        Boolean.FALSE
                    );
                }
            }
            else {
                objectReference.values("userDefinedQualifier").add(
                    Boolean.FALSE
                );                
            }
        }
        // Attribute of type class
        else {
            objectReference.values("referenceIsStoredAsAttribute").add(
                Boolean.TRUE
            );
            objectReference.values("userDefinedQualifier").add(
                Boolean.FALSE
            );
        }
    }

    //-------------------------------------------------------------------------
    private int compareOrder(
        List o1,
        List o2
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

    //-------------------------------------------------------------------------
    private List getOrderFieldGroup(
        DataproviderObject_1_0 elementDefinition
    )  {
        List order = elementDefinition.values("orderFieldGroup");
        if((order == null) || order.isEmpty()) {
            order = elementDefinition.values("order");
        }
        return order;
    }

    //-------------------------------------------------------------------------
    private List<Object> getOrderObjectContainer(
        DataproviderObject_1_0 elementDefinition
    )  {
        List<Object> order = elementDefinition.values("orderObjectContainer");
        if((order == null) || order.isEmpty()) {
            order = elementDefinition.values("order");
        }
        return order;
    }

    //-------------------------------------------------------------------------
    private void setElementDefinitionDefaultLayout(
        DataproviderObject_1_0 element
    ) throws ServiceException {
        element.values("verticalFill").add(new Boolean(false));
        element.values("columnBreakAtElement");
        element.values("columnSizeMin");
        element.values("columnSizeMax");
    }

    //-------------------------------------------------------------------------
    private boolean isMemberOfObjectContainer(
        StructuralFeatureDefinition feature
    ) throws ServiceException {
        if(feature.getModelElement() == null) {
            return true;
        }
        else {
            ModelElement_1_0 element = feature.getModelElement();
            return 
                this.model.isAttributeType(element) 
                || this.model.isStructureFieldType(element) 
                || this.model.isReferenceType(element);
        }
    }

    //-------------------------------------------------------------------------
    private boolean isReferenceField(
        StructuralFeatureDefinition feature
    ) throws ServiceException {
        ModelElement_1_0 elementType = this.model.getElement(feature.getType());
        if(feature.getModelElement() == null) {
            return this.model.isClassType(elementType);
        }
        else {
            ModelElement_1_0 element = feature.getModelElement();
            return (((this.model.isAttributeType(element) || this.model.isStructureFieldType(element)) && this.model.isClassType(elementType)) || this.model.isReferenceType(element));
        }
    }

    //-------------------------------------------------------------------------
    /**
     * return element definition of container of containedFeature. level==0 -->
     * return tab definition, level==1 returns field group definition. inspectorClass
     * is the class which either defines or inherits containedFeature. Definition
     * is first looked up for inspectorClass and if not found for defining class of
     * containedFeature.
     */
    private DataproviderObject_1_0 getFeatureContainerDefinition(
        Path segmentIdentity,
        String paneName,
        String tabName,
        String groupName,
        FeatureDefinition containedFeature,
        int level,
        ModelElement_1_0 inspectorClass,
        boolean useDefaultIconKey
    ) throws ServiceException {
        // Try to find element with inspectorClass
        String containerName = 
            "" + inspectorClass.objGetValue("qualifiedName") + ":Pane:" + paneName +
            ":Tab:" + tabName +
            (level > 0 ? ":Group:" + groupName : "");
        DataproviderObject elementDefinition = this.existingElementDefinitions.get(segmentIdentity).get(containerName);
        if(elementDefinition != null) {
            return elementDefinition;
        }
        if(!"Root".equals(segmentIdentity.getBase())) {
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
        }
        else {
            // Fall back to defining class of feature
            ModelElement_1_0 definingClass = this.model.getElement(
                containedFeature.getContainer()
            );
            containerName = 
                "" + definingClass.objGetValue("qualifiedName") + ":Pane:" + paneName +
                ":Tab:" + tabName +
                (level > 0 ? ":Group:" + groupName : "");
            return this.getElementDefinition(
                segmentIdentity,
                containerName,
                "\"" + containerName + "\"",
                useDefaultIconKey
            );
        }
    }

    //-------------------------------------------------------------------------
    private DataproviderObject_1_0 getAttributeContainerDefinition(
        Path segmentIdentity,
        StructuralFeatureDefinition featureDef,
        List featureOrder,
        int level,
        ModelElement_1_0 inspectorClass
    ) throws ServiceException {
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

    //-------------------------------------------------------------------------
    private DataproviderObject_1_0 getOperationDefinition(
        Path segmentIdentity,
        FeatureDefinition operationDef,
        String parameterName,
        int level,
        ModelElement_1_0 inspectorClass
    ) throws ServiceException {
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

    //-------------------------------------------------------------------------
    private DataproviderObject getElementDefinition(
        Path segmentIdentity,
        String elementName,
        String defaultLabel,
        boolean useDefaultIconKey
    ) throws ServiceException {
        // Verify whether definition already exists
        DataproviderObject elementDefinition = null;
        PathComponent components = new PathComponent(elementName);
        for(
            int i = 0; 
            i < components.size(); 
            i++
        ) {
            if(
                (this.existingElementDefinitions == null) ||
                (this.existingElementDefinitions.get(segmentIdentity) == null) ||
                (components.getSuffix(i) == null)
            ) {
                SysLog.error("Unable to retrieve element definition", Arrays.asList(elementName, this.existingElementDefinitions, this.existingElementDefinitions == null ? null : this.existingElementDefinitions.get(segmentIdentity), components.getSuffix(i)));
            }
            else {
                elementDefinition = this.existingElementDefinitions.get(segmentIdentity).get(
                    new PathComponent(components.getSuffix(i)).toString()
                );
                if(elementDefinition != null) {
                    return elementDefinition;
                }
            }
        }
        // No element definition found for element
        
        // Get from Root segment
        if(!"Root".equals(segmentIdentity.getBase())) {
            elementDefinition = this.getElementDefinition(
                segmentIdentity.getParent().getChild("Root"),
                elementName, 
                defaultLabel, 
                useDefaultIconKey
            );
        }
        // Create default definition if already in Root
        else {
            SysLog.info("no configured definition found. creating default", elementName);
            elementDefinition = new DataproviderObject(
                segmentIdentity.getDescendant(new String[]{"elementDefinition", elementName})
            );
            elementDefinition.values(SystemAttributes.OBJECT_CLASS).add("org:openmdx:ui1:ElementDefinition");      
            // Active
            elementDefinition.values("active").add(new Boolean(true));
            // Default iconKey
            if(useDefaultIconKey) {
                elementDefinition.values("iconKey").add(elementName);
            }
            // Default label
            if(elementDefinition.getValues("label") == null) {
                List<Object> values = elementDefinition.values("label");
                values.add(
                    defaultLabel == null ? "N/A" : defaultLabel
                );
            }
            // Default shortLabel
            if(elementDefinition.getValues("shortLabel") == null) {        
                List<Object> values = elementDefinition.values("shortLabel");
                values.addAll(
                    elementDefinition.values("label")
                );
            }    
            // Default toolTip
            List<Object> values = elementDefinition.values("toolTip");
            values.add("toolTip of element " + elementName);
        }
        this.storeElementDefinition(
            segmentIdentity,
            elementDefinition
        );    
        return elementDefinition;
    }

    //-------------------------------------------------------------------------
    private DataproviderObject_1_0 getInspectorElementDefinition(
        Path segmentIdentity,
        StructuralFeatureDefinition classDef
    ) throws ServiceException {
        return this.getElementDefinition(
            segmentIdentity,
            classDef,
            null,
            true
        );    
    }

    //-------------------------------------------------------------------------
    /**
     * Get element definition for feature. By default the feature is looked up
     * with its qualified name in the catalog of element definitions. However, if
     * inspectorClass is specified then the feature is considered to be a member of
     * the base class and a lookup is done <qualified name base class>:<feature name>.
     * If not found the lookup is done <qualified name feature>.
     */
    private DataproviderObject_1_0 getElementDefinition(
        Path segmentIdentity,
        StructuralFeatureDefinition feature,
        ModelElement_1_0 inspectorClass,
        boolean useDefaultIconKey
    ) throws ServiceException {
        DataproviderObject elementDefinition = this.getElementDefinition(
            segmentIdentity,
            feature.getQualifiedName(),
            feature.getName(),
            useDefaultIconKey
        );
        // Overload definition if a definition for inspectorClass exists
        if(inspectorClass != null) {
            String qualifiedNameBase = (String)inspectorClass.objGetValue("qualifiedName") + ":" + feature.getName();
            if(
                (this.existingElementDefinitions.get(segmentIdentity).get(qualifiedNameBase) != null) ||
                (this.existingElementDefinitions.get(segmentIdentity.getParent().getChild("Root")).get(qualifiedNameBase) != null)
            ) {
                DataproviderObject overloadDefinition = this.getElementDefinition(
                    segmentIdentity,
                    qualifiedNameBase,
                    feature.getName(),
                    useDefaultIconKey
                );
                DataproviderObject definition = new DataproviderObject(
                    overloadDefinition.path()
                );
                definition.addClones(
                    elementDefinition,
                    true
                );
                for(
                    Iterator i = overloadDefinition.attributeNames().iterator();
                    i.hasNext();
                ) {
                    String attributeName = (String)i.next();
                    if(!overloadDefinition.values(attributeName).isEmpty()) {
                        definition.clearValues(attributeName).addAll(
                            overloadDefinition.values(attributeName)
                        );
                    }
                }
                elementDefinition = definition;
            }
        }
        // Default order is (100000,0,0)
        List<Object> values = elementDefinition.getValues("order");
        if((values == null) || values.isEmpty()) {
            values = elementDefinition.values("order");
            // all customized fields have lower order than non-customized fields
            values.add(new Integer(100000));
            values.add(new Integer(0));
            values.add(new Integer(0));
        }
        // Default isSortable=false, isFilterable=false if non-modeled element
        if(feature.getModelElement() == null) {
            elementDefinition.values("sortable").add(
                Boolean.FALSE
            );
            elementDefinition.values("filterable").add(
                Boolean.FALSE
            );
            elementDefinition.values("mandatory").add(
                Boolean.FALSE
            );
        }
        // Default sizeXWeight
        if(!feature.isReference()) {
            values = elementDefinition.getValues("sizeXWeight");
            if((values == null) || values.isEmpty()) {
                // set default to weight 3
                elementDefinition.values("sizeXWeight").add(new Integer(3));
            }
        }
        return elementDefinition;    
    }

    //-------------------------------------------------------------------------
    private void mapField(
        DataproviderObject field,
        StructuralFeatureDefinition feature,
        DataproviderObject_1_0 definition,
        boolean asMemberOfObjectContainer,
        String defaultMimeType,
        String defaultValue
    ) throws ServiceException {
        ModelElement_1_0 featureType = this.model.getDereferencedType(
            feature.getType()
        );
        String typeName = (String)featureType.objGetValue("qualifiedName");
        boolean isAdditionalElementDefinition = 
            "org:openmdx:ui1:AdditionalElementDefinition".equals(definition.values(SystemAttributes.OBJECT_CLASS).get(0));
        // Default value
        if(defaultValue != null) {
            field.values("defaultValue").add(defaultValue);
        }
        // Reference stored as attribute or attribute of type class
        boolean isReferenceField = this.isReferenceField(feature) && !isAdditionalElementDefinition;
        if(isReferenceField) {
            field.values(SystemAttributes.OBJECT_CLASS).add("org:openmdx:ui1:ObjectReferenceField");
            this.setObjectReference(
                field,
                feature
            );
            field.values("titleIndex").add(
                !definition.values("titleIndex").isEmpty() 
                ? definition.values("titleIndex").get(0)
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
        }
        // decimal
        else if(
            PrimitiveTypes.DECIMAL.equals(typeName) ||
            PrimitiveTypes.SHORT.equals(typeName) ||
            PrimitiveTypes.LONG.equals(typeName) ||
            PrimitiveTypes.INTEGER.equals(typeName)
        ) {
            field.values(SystemAttributes.OBJECT_CLASS).add("org:openmdx:ui1:NumberField");
            BigDecimal defaultMinValue = null;
            BigDecimal defaultMaxValue = null;
            Integer defaultDecimalPlaces = new Integer(0);
            if(PrimitiveTypes.DECIMAL.equals(typeName)) {
                defaultMinValue = new BigDecimal(Long.MIN_VALUE);
                defaultMaxValue = new BigDecimal(Long.MAX_VALUE);
                defaultDecimalPlaces = new Integer(2);
            }
            else if(PrimitiveTypes.SHORT.equals(typeName)) {
                defaultMinValue = new BigDecimal(Short.MIN_VALUE);
                defaultMaxValue = new BigDecimal(Short.MAX_VALUE);
            }
            else if(PrimitiveTypes.LONG.equals(typeName)) {
                defaultMinValue = new BigDecimal(Long.MIN_VALUE);
                defaultMaxValue = new BigDecimal(Long.MAX_VALUE);
            }
            else if(PrimitiveTypes.INTEGER.equals(typeName)) {
                defaultMinValue = new BigDecimal(Integer.MIN_VALUE);
                defaultMaxValue = new BigDecimal(Integer.MAX_VALUE);            
            }
            field.values("minValue").add(
                !definition.values("minValue").isEmpty()
                ? new BigDecimal((String)definition.values("minValue").get(0))
                : defaultMinValue
            );
            field.values("maxValue").add(
                !definition.values("maxValue").isEmpty()
                ? new BigDecimal((String)definition.values("maxValue").get(0))
                : defaultMaxValue
            );
            field.values("decimalPlaces").add(
                !definition.values("decimalPlaces").isEmpty()
                ? new Integer(((Number)definition.values("decimalPlaces").get(0)).intValue())
                : defaultDecimalPlaces        
            );
            field.values("increment").add(
                !definition.values("increment").isEmpty()
                ? new BigDecimal((String)definition.values("increment").get(0))
                : new BigDecimal(1)
            );
            field.values("hasThousandsSeparator").add(
                !definition.values("hasThousandsSeparator").isEmpty()
                ? (Boolean)definition.values("hasThousandsSeparator").get(0)
                    : Boolean.TRUE
            );
        }
        // date
        else if(PrimitiveTypes.DATE.equals(typeName)) {
            field.values(SystemAttributes.OBJECT_CLASS).add("org:openmdx:ui1:DateField");
            field.values("format").add("d");
        }
        // dateTime
        else if(PrimitiveTypes.DATETIME.equals(typeName)) {
            field.values(SystemAttributes.OBJECT_CLASS).add("org:openmdx:ui1:DateField");
            field.values("format").add("g");
        }
        // boolean
        else if(PrimitiveTypes.BOOLEAN.equals(typeName)) {
            field.values(SystemAttributes.OBJECT_CLASS).add("org:openmdx:ui1:CheckBox");
            field.values("threeState").add(new Boolean(false));
        }
        // binary
        else if(PrimitiveTypes.BINARY.equals(typeName)) {
            field.values(SystemAttributes.OBJECT_CLASS).add("org:openmdx:ui1:DocumentBox");
            if(defaultMimeType != null) {
                field.values("mimeType").add(defaultMimeType);
            }
            field.values("inPlace").add(
                !definition.values("inPlace").isEmpty()
                ? (Boolean)definition.values("inPlace").get(0)
                    : Boolean.FALSE
            );
        }
        // unknown primitive types are mapped to string
        else if(this.model.isPrimitiveType(featureType)) {
            mapToString = true;
        }
        if(mapToString) {
            field.values(SystemAttributes.OBJECT_CLASS).add("org:openmdx:ui1:TextBox");
            field.values("wordWrap").add(new Boolean(true));
            field.values("tabStop").add(new Boolean(true));
            field.values("multiline").add(new Boolean(false));
            field.values("maxLength").add(
                !definition.values("maxLength").isEmpty()
                ? (Integer)definition.values("maxLength").get(0)
                    : new Integer(Integer.MAX_VALUE)
            );
            field.values("autoSize").add(new Boolean(true));
            field.values("acceptsTab").add(new Boolean(false));
            field.values("isPassword").add(
                definition.values("isPassword").isEmpty()
                ? new Boolean(false)
                : definition.values("isPassword").get(0)
            );
            field.values("textAlign").add(new Short((short)0));
            if(defaultMimeType != null) {
                field.values("mimeType").add(defaultMimeType);
            }
        }
        // complete
        if(field.getValues(SystemAttributes.OBJECT_CLASS) != null) {
            // as default take changeability from model. 
            Boolean isChangeable = feature.isChangeable();
            isChangeable = (definition.getValues("changeable") == null) || definition.values("changeable").isEmpty()
            ? isChangeable
                : (Boolean)definition.values("changeable").get(0); 
            boolean isSortable = 
                !"org:openmdx:base:ExtentCapable:identity".equals(feature.getQualifiedName()) &&
                !PrimitiveTypes.BINARY.equals(typeName);
            this.setValuedFieldDefault(
                field,
                definition.values("label"),
                definition.values("shortLabel"),
                definition.values("displayValueExpr"),
                definition.values("toolTip"),
                isChangeable.booleanValue(), 
                (String)definition.values("iconKey").get(0),
                (String)definition.values("color").get(0), 
                (String)definition.values("backColor").get(0), 
                definition.values("multiplicity").isEmpty() ? 
                    feature.getMultiplicity() : 
                    (String)definition.values("multiplicity").get(0),
                !definition.values("spanRow").isEmpty() ? 
                    (Integer)definition.values("spanRow").get(0) : 
                    new Integer(1), 
                !definition.values("skipRow").isEmpty() ? 
                    (Integer)definition.values("skipRow").get(0) : 
                    new Integer(0), 
                definition.values("filterable").isEmpty() ? 
                    !isReferenceField : 
                    ((Boolean)definition.values("filterable").get(0)).booleanValue(),
                definition.values("sortable").isEmpty() ? 
                    isSortable : 
                    ((Boolean)definition.values("sortable").get(0)).booleanValue(),
                definition.values("mandatory").isEmpty() ? 
                    Multiplicities.SINGLE_VALUE.equals(feature.getMultiplicity()) && feature.isChangeable() : 
                    ((Boolean)definition.values("mandatory").get(0)).booleanValue(),
                feature.getName(),
                feature.getQualifiedName(),
                (String)definition.values("dataBindingName").get(0)
            );
        }
    }

    //-------------------------------------------------------------------------
    private void overloadDefinition(
        DataproviderObject target,
        DataproviderObject_1_0 base,
        DataproviderObject_1_0 overloadWith
    ) {
        target.addClones(
            base,
            true
        );
        // do not use addClones because attributes with no values must not
        // be replaced/cleared
        for(
            Iterator i = overloadWith.attributeNames().iterator();
            i.hasNext();
        ) {
            String attributeName = (String)i.next();
            if(!overloadWith.values(attributeName).isEmpty()) {
                target.clearValues(attributeName).addAll(
                    overloadWith.values(attributeName)
                );
            }
        }
    }

    //-------------------------------------------------------------------------
    private void updateAndStoreFieldGroups(
        Path segmentIdentity,
        Set<String> modifiedGroups,
        Map<Path,DataproviderObject_1_0> cachedDefinitions
    ) throws ServiceException {
        for(String groupName: modifiedGroups) {
            DataproviderObject group = this.existingElements.get(segmentIdentity).get(groupName);
            boolean customizedGroup = group.path().getBase().indexOf("Tab:100000") < 0;
            // Add column breaks to group
            // calculate columnSizeMin/Max of group 
            int currentColumn = 0;
            int currentColumnSizeMax = 0;
            int fieldIndex = 0;
            int memberIndex = 0;
            // as default put 7 members in one column except this would result in more than 
            // three columns
            int defaultColumnHeight = 
                java.lang.Math.max(
                    new Double(java.lang.Math.ceil(group.values("member").size() / 3.0)).intValue(),  
                    7
                );
            for(
                Iterator j = group.values("member").iterator();
                j.hasNext();
                memberIndex++
            ) {
                Path e = (Path)j.next();
                DataproviderObject_1_0 elementDefinition = cachedDefinitions.get(e);
                List columnBreak = elementDefinition.values("columnBreak");
                if(
//                    (memberIndex > 0) && // allow columnBreak at first element --> allows to skip column
                    ((!columnBreak.isEmpty() && ((Boolean)columnBreak.get(0)).booleanValue()) || (!customizedGroup && (fieldIndex % defaultColumnHeight == 0)))
                ) { 
                    group.values("columnSizeMin").add(new Integer(330));
                    group.values("columnSizeMax").add(new Integer(currentColumnSizeMax));
                    group.values("columnBreakAtElement").add(new Integer(memberIndex));
                    currentColumn++;
                    currentColumnSizeMax = 0;
                    fieldIndex = 0;
                }
                currentColumnSizeMax = java.lang.Math.max(
                    330 + ((Integer)(cachedDefinitions.get(e)).values("sizeXWeight").get(0)).intValue() * 100, 
                    currentColumnSizeMax
                );
                fieldIndex++;
            }
            group.values("columnSizeMin").add(new Integer(330));
            group.values("columnSizeMax").add(new Integer(Integer.MAX_VALUE));
            group.values("columnBreakAtElement").add(new Integer(Integer.MAX_VALUE));     
            this.storeElement(
                segmentIdentity,
                group
            );
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Add elements to attribute pane. The elements are grouped 
     * according to their order into tabs and field groups and ordered
     * within the field groups. 
     */
    private void addElementsToAttributePane(
        Path segmentIdentity,
        DataproviderObject_1_0 pane,
        List<Ui_1.StructuralFeatureDefinition> featureDefinitions,
        ModelElement_1_0 inspectorClass
    ) throws ServiceException {
        Map<Path,DataproviderObject_1_0> cachedDefinitions = new HashMap<Path,DataproviderObject_1_0>();
        Set<String> modifiedGroups = new HashSet<String>();
        Set<String> modifiedTabs = new HashSet<String>();
        String paneName = inspectorClass.objGetValue("qualifiedName") + ":Pane:Attr";
        // Process all features
        for(StructuralFeatureDefinition feature: featureDefinitions) {
            if(!feature.isReference()) {
                DataproviderObject_1_0 baseDefinition = this.getElementDefinition(
                    segmentIdentity,
                    feature,
                    inspectorClass,
                    false // no default iconKey for attributes
                );
                if(((Boolean)baseDefinition.values("active").get(0)).booleanValue()) {
                    List<DataproviderObject_1_0> definitions = new ArrayList<DataproviderObject_1_0>();
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
                    for(DataproviderObject_1_0 definition: definitions) {
                        Boolean definitionIsActive = Boolean.TRUE;
                        if(definition.values("active").get(0) instanceof String) {
                            SysLog.error("Value of attibute 'active' is not instanceof Boolean", definition);
                        }                        
                        else {
                            definitionIsActive = (Boolean)definition.values("active").get(0);
                        }
                        // Only render if definition is active
                        if(
                            (definitionIsActive == null) || // default is true
                            definitionIsActive.booleanValue()
                        ) {
                            List order = this.getOrderFieldGroup(
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
                            DataproviderObject tab = this.existingElements.get(segmentIdentity).get(tabName);
                            if(tab == null) {
                                DataproviderObject_1_0 tabElementDefinition = this.getAttributeContainerDefinition(
                                    segmentIdentity,
                                    feature,
                                    order,
                                    0,
                                    inspectorClass
                                );
                                // skip element in case tab is not active
                                if(!((Boolean)tabElementDefinition.values("active").get(0)).booleanValue()) {
                                    continue;
                                }
                                tab = new DataproviderObject(
                                    segmentIdentity.getDescendant(new String[]{"element", tabName})
                                );
                                tab.values(SystemAttributes.OBJECT_CLASS).add("org:openmdx:ui1:Tab");
                                this.setElementDefault(
                                    tab,
                                    tabElementDefinition.values("toolTip"),
                                    tabElementDefinition.values("changeable").isEmpty() ? 
                                        this.changableDefaultValue : 
                                        ((Boolean)tabElementDefinition.values("changeable").get(0)).booleanValue(),
                                    "N/A"
                                );
                                tab.values("title").addAll(
                                    tabElementDefinition.values("label")
                                );
                                this.setElementDefinitionDefaultLayout(tab);
                                // Add tab to pane and sort by tabName
                                List members = pane.values("member");
                                int k = 0;
                                while(
                                    (k < members.size()) &&
                                    tab.path().getBase().compareTo(((Path)members.get(k)).getBase()) >= 0
                                ) {
                                    k++;
                                }
                                pane.values("member").add(
                                    k, 
                                    tab.path()
                                );
                                this.storeElement(
                                    segmentIdentity,
                                    tab
                                );
                            }
                            // Get field group (create on demand)
                            String groupName = tabName + ":Group:" + groupId;
                            DataproviderObject group = this.existingElements.get(segmentIdentity).get(groupName);
                            if(group == null) {
                                DataproviderObject_1_0 groupElementDefinition = this.getAttributeContainerDefinition(
                                    segmentIdentity,
                                    feature,
                                    order,
                                    1,
                                    inspectorClass
                                );
                                // skip element in case field group is not active
                                if(!((Boolean)groupElementDefinition.values("active").get(0)).booleanValue()) {
                                    continue;
                                }
                                group = new DataproviderObject(
                                    segmentIdentity.getDescendant(new String[]{"element", groupName})
                                );
                                group.values(SystemAttributes.OBJECT_CLASS).add("org:openmdx:ui1:FieldGroup");
                                this.setLabelledFieldDefault(
                                    group,
                                    groupElementDefinition.values("label"),
                                    groupElementDefinition.values("shortLabel"),
                                    groupElementDefinition.values("toolTip"),
                                    groupElementDefinition.values("changeable").isEmpty() ? 
                                        this.changableDefaultValue : 
                                        ((Boolean)groupElementDefinition.values("changeable").get(0)).booleanValue(),
                                    "N/A",
                                    (String)groupElementDefinition.values("color").get(0),
                                    (String)groupElementDefinition.values("backColor").get(0)
                                );
                                this.setElementDefinitionDefaultLayout(group);
                                group.values("showMaxMember").addAll(
                                    groupElementDefinition.values("showMaxMember")
                                );
                                group.values("inPlaceEditable").addAll(
                                    groupElementDefinition.values("inPlace")
                                );
                                // add group to tab and sort by groupName
                                List members = tab.values("member");
                                int k = 0;
                                while(
                                    (k < members.size()) && 
                                    group.path().getBase().compareTo(((Path)members.get(k)).getBase()) >= 0
                                ) {
                                    k++;
                                }
                                tab.values("member").add(
                                    k, 
                                    group.path()
                                );
                                this.storeElement(
                                    segmentIdentity,
                                    group
                                );
                            }
                            // get field (create on demand)
                            String elementName = groupName + ":Field:" + feature.getName();
                            if(definition != baseDefinition) {
                                elementName += ":" + definition.path().getBase();
                            }
                            DataproviderObject element = this.existingElements.get(segmentIdentity).get(elementName);
                            if(element == null) {
                                element = new DataproviderObject(
                                    segmentIdentity.getDescendant(new String[]{"element", elementName})
                                );
                                this.mapField(
                                    element,
                                    feature,
                                    definition,
                                    false,
                                    null,
                                    (String)definition.values("defaultValue").get(0)
                                );
                                this.storeElement(
                                    segmentIdentity,
                                    element
                                );
                                // add element to group and sort by order
                                List<Object> members = group.values("member");
                                int l = 0;
                                int r = members.size() - 1;
                                while(l <= r) {
                                    int pos = (l + r) / 2;
                                    DataproviderObject_1_0 def = cachedDefinitions.get(members.get(pos));
                                    if(def == null) {
                                        SysLog.error("Can not find member in list of cached definitions", Arrays.asList(new Object[]{members.get(pos), cachedDefinitions}));
                                        break;
                                    }
                                    List existing = this.getOrderFieldGroup(def);
                                    int res = this.compareOrder(
                                        order, 
                                        existing
                                    );
                                    if(res == 0) { l = pos; r = pos; break;} 
                                    else if(res > 0) l = pos + 1;
                                    else if(res < 0) r = pos - 1;
                                }
                                if(members.isEmpty()) {
                                    members.add(
                                        element.path()
                                    );
                                }
                                else {
                                    members.add(
                                        l,
                                        element.path()
                                    );              
                                }
                            }    
                            cachedDefinitions.put(
                                element.path(),
                                definition
                            );
                            modifiedGroups.add(
                                group.path().getBase()
                            );
                            modifiedTabs.add(
                                tab.path().getBase()
                            );
                        }
                    }
                }
            }      
        }
        this.updateAndStoreFieldGroups(
            segmentIdentity,
            modifiedGroups,
            cachedDefinitions
        );
        // Store tabs
        for(String tabName: modifiedTabs) {
            DataproviderObject tab = this.existingElements.get(segmentIdentity).get(tabName);
            this.storeElement(
                segmentIdentity,
                tab
            );
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Add fields to container. Container's members are possibly modified.
     * Caller must store container.
     */
    private void addFieldsToObjectContainer(
        Path segmentIdentity,
        DataproviderObject_1_0 container,
        String containerName,
        List<Object> showMemberRange,
        List<Ui_1.StructuralFeatureDefinition> featureDefinitions,
        ModelElement_1_0 inspectorClass,
        Map memberMimeTypes,
        Map memberDefaultValues
    ) throws ServiceException {
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
        for(StructuralFeatureDefinition featureDefinition: featureDefinitions) {
            // feature is member of inspectorClass
            if(
                (featureDefinition.getModelElement() != null) &&
                inspectorClass.objGetList("content").contains(featureDefinition.getModelElement().jdoGetObjectId())
            ) {            
                String featureName = (String)featureDefinition.getName();
                if("name".equals(featureName)) {
                    featureN = featureDefinition;
                }
                else if("description".equals(featureName)) {
                    featureD = featureDefinition;
                }
                else if("title".equals(featureName)) {
                    featureT = featureDefinition;
                }
                else {
                    orderedFeatures.add(
                        featureDefinition
                    );
                }
            }
            else {
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
        Map<Path,DataproviderObject_1_0> elementDefinitions = new HashMap<Path,DataproviderObject_1_0>();
        for(StructuralFeatureDefinition feature: orderedFeatures) {
            if(this.isMemberOfObjectContainer(feature)) {
                DataproviderObject_1_0 elementDefinition = this.getElementDefinition(
                    segmentIdentity,
                    feature,
                    inspectorClass,
                    false // no default iconKey for fields
                );
                List order = this.getOrderObjectContainer(
                    elementDefinition
                );
                if(((Boolean)elementDefinition.values("active").get(0)).booleanValue()) {
                    String elementName = containerName + ":" + feature.getName();
                    DataproviderObject element = new DataproviderObject(
                        segmentIdentity.getDescendant(new String[]{"element", elementName})
                    );
                    this.mapField(
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
                    if(element.getValues(SystemAttributes.OBJECT_CLASS) != null) {
                        // Test whether element is in showMemberRange
                        List<Object> elementOrder = this.getOrderObjectContainer(elementDefinition);
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
                                }
                                else {
                                    orderFrom = new ArrayList<Integer>();
                                    for(String e: range.split(":")) {
                                        orderFrom.add(Integer.valueOf(e));
                                    }
                                    orderTo = null;
                                }
                                if(
                                    (this.compareOrder(elementOrder, orderFrom) >= 0) &&
                                    ((orderTo == null) || this.compareOrder(elementOrder, orderTo) <= 0)
                                ) {
                                    isInShowMemberRange = true;
                                    break;
                                }
                            }
                        }
                        else {
                            isInShowMemberRange = true;
                        }
                        if(isInShowMemberRange) {
                            this.storeElement(
                                segmentIdentity,
                                element
                            );                
                            elementDefinitions.put(
                                element.path(),
                                elementDefinition
                            );
    
                            // add element to container and sort by order
                            List<Object> members = container.values("member");
                            int l = 0;
                            int r = members.size() - 1;
                            while(l <= r) {
                                int pos = (l + r) / 2;
                                List existing = this.getOrderObjectContainer(
                                    (DataproviderObject_1_0)elementDefinitions.get(members.get(pos))
                                );
                                int res = this.compareOrder(
                                    order, 
                                    existing
                                );
                                if(res == 0) { l = pos; r = pos; break;} 
                                else if(res > 0) l = pos + 1;
                                else if(res < 0) r = pos - 1;
                            }
                            if(members.isEmpty()) {
                                members.add(
                                    element.path()
                                );
                            }
                            else {
                                members.add(
                                    l,
                                    element.path()
                                );
                            }
                        }
                    }
                }
            }
        }
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void addAdditionalElementDefinitions(
        DataproviderObject_1_0 baseDefinition,
        ModelElement_1_0 inspectorClass,
        List<DataproviderObject_1_0> definitions
    ) throws ServiceException {
        // get additional element definitions. Construct an element 
        // definition on the base definition and override with additional definition
        List<DataproviderObject_1_0> additionalElementDefinitions = this.delegation.addFindRequest(
            baseDefinition.path().getChild("additionalElementDefinition"),
            null,
            AttributeSelectors.ALL_ATTRIBUTES,
            0,
            Integer.MAX_VALUE,
            Directions.ASCENDING
        );
        for(DataproviderObject_1_0 additionalDefinition: additionalElementDefinitions) {
            DataproviderObject target = new DataproviderObject(
                additionalDefinition.path()
            );
            this.overloadDefinition(
                target,
                baseDefinition,
                additionalDefinition
            );
            // filter additional definition: add definition only if base class matches for class
            // or if forClass is unspecified
            String inspectorClassName = (String)inspectorClass.objGetValue("qualifiedName");
            if((target.getValues("forClass") == null) || target.values("forClass").contains(inspectorClassName)) {
                definitions.add(
                    target
                );
            }
        }
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void addAlternateElementDefinitions(
        DataproviderObject_1_0 baseDefinition,
        ModelElement_1_0 inspectorClass,
        List<DataproviderObject_1_0> definitions
    ) throws ServiceException {
        // get alternate element definitions. Construct an element 
        // definition on the base definition and override with alternate definition
        List<DataproviderObject_1_0> alternateElementDefinitions = this.delegation.addFindRequest(
            baseDefinition.path().getChild("alternateElementDefinition"),
            null,
            AttributeSelectors.ALL_ATTRIBUTES,
            0,
            Integer.MAX_VALUE,
            Directions.ASCENDING
        );
        for(
            Iterator j = alternateElementDefinitions.iterator();
            j.hasNext();
        ) {
            DataproviderObject alternateDefinition = (DataproviderObject)j.next();
            DataproviderObject target = new DataproviderObject(
                alternateDefinition.path()
            );
            this.overloadDefinition(
                target,
                baseDefinition,
                alternateDefinition
            );
            definitions.add(
                target
            );
        }    
    }

    //-------------------------------------------------------------------------
    private Map<String,Object> getMemberMimeTypes(
        DataproviderObject_1_0 containerDefinition
    ) {
        Map<String,Object> memberMimeTypes = new HashMap<String,Object>();
        if(containerDefinition.getValues("memberElementName") != null) {
            for(int i = 0; i < containerDefinition.values("memberElementName").size(); i++) {
                String name = (String)containerDefinition.values("memberElementName").get(i);
                if(
                    (containerDefinition.values("memberMimeType").size() > i) &&
                    (containerDefinition.values("memberMimeType").get(i) != null) &&
                    !"".equals(containerDefinition.values("memberMimeType").get(i))
                ) {
                    memberMimeTypes.put(
                        name,
                        containerDefinition.values("memberMimeType").get(i)
                    );
                }
            }
        }
        return memberMimeTypes;
    }

    //-------------------------------------------------------------------------
    private Map<String,Object> getMemberDefaultValues(
        DataproviderObject_1_0 containerDefinition
    ) {
        Map<String,Object> memberDefaultValues = new HashMap<String,Object>();
        if(containerDefinition.getValues("memberElementName") != null) {
            for(int i = 0; i < containerDefinition.values("memberElementName").size(); i++) {
                String name = (String)containerDefinition.values("memberElementName").get(i);
                if(
                    (containerDefinition.values("memberDefaultValue").size() > i) &&
                    (containerDefinition.values("memberDefaultValue").get(i) != null) &&
                    !"".equals(containerDefinition.values("memberDefaultValue").get(i))
                ) {
                    memberDefaultValues.put(
                        name,
                        containerDefinition.values("memberDefaultValue").get(i)
                    );
                }
            }
        }
        return memberDefaultValues;
    }

    //-------------------------------------------------------------------------
    private List<Ui_1.StructuralFeatureDefinition> getStructFeatureDefinitions(
        ModelElement_1_0 paramType
    ) throws ServiceException {
        Collection fieldDefs = ((Map)paramType.objGetValue("field")).values();
        List<Ui_1.StructuralFeatureDefinition> featureDefinitions = new ArrayList<Ui_1.StructuralFeatureDefinition>();
        for(
            Iterator i = fieldDefs.iterator();
            i.hasNext();
        ) {
            featureDefinitions.add(
                new StructuralFeatureDefinition((ModelElement_1_0)i.next())
            );
        }
        return featureDefinitions;
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private List<Ui_1.StructuralFeatureDefinition> getStructuralFeatureDefinitions(
        Path segmentIdentity,
        ModelElement_1_0 classDef,
        boolean includeSubtypes,
        boolean attributesOnly
    ) throws ServiceException {
        List<ModelElement_1_0> featureDefs = new ArrayList(
            this.model.getStructuralFeatureDefs(
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
                ModelElement_1_0 exposedEnd = this.model.getElement(featureDef.objGetValue("exposedEnd"));
                if(
                    !(REFERENCES_TO_EXCLUDE.contains(feature.getQualifiedName()) || REFERENCES_TO_EXCLUDE.contains(feature.getName())) &&
                    !AggregationKind.COMPOSITE.equals(exposedEnd.objGetValue("aggregation"))
                ) {
                    featureDefinitions.put(
                        feature.getQualifiedName(),
                        feature
                    );
                }
            }
            else {            
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
                this.model.isSubtypeOf(classDef, featureDefinition.getContainer())
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
                this.model.isSubtypeOf(classDef, featureDefinition.getContainer())
            ) {
                featureDefinitions.put(
                    featureDefinition.getQualifiedName(),
                    featureDefinition
                );
            }
        }      
        return new ArrayList<Ui_1.StructuralFeatureDefinition>(featureDefinitions.values());
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private List<Ui_1.OperationDefinition> getOperationDefinitions(
        Path segmentIdentity,
        ModelElement_1_0 classDef
    ) throws ServiceException {
        Map<String,Ui_1.OperationDefinition> featureDefinitions = new HashMap<String,Ui_1.OperationDefinition>();
        // Add modeled operations
        Collection features = ((Map)classDef.objGetValue("allFeature")).values();
        for(
            Iterator<ModelElement_1_0> i = features.iterator(); 
            i.hasNext(); 
        ) {
            ModelElement_1_0 feature = i.next();
            if(this.model.isOperationType(feature)) {
                OperationDefinition featureDefinition = new OperationDefinition(feature);
                featureDefinitions.put(
                    featureDefinition.getQualifiedName(),
                    featureDefinition
                );
            }
        }
        // Add customized operations for segment Root
        for(OperationDefinition featureDefinition: this.operationDefinitions.get(segmentIdentity.getParent().getChild("Root"))) {
            if(this.model.isSubtypeOf(classDef, featureDefinition.getContainer())) {
                featureDefinitions.put(
                    featureDefinition.getQualifiedName(),
                    featureDefinition
                );
            }
        }      
        // Add customized operations for current segment
        for(OperationDefinition featureDefinition: this.operationDefinitions.get(segmentIdentity)) {
            if(this.model.isSubtypeOf(classDef, featureDefinition.getContainer())) {
                featureDefinitions.put(
                    featureDefinition.getQualifiedName(),
                    featureDefinition
                );
            }
        }      
        return new ArrayList<Ui_1.OperationDefinition>(featureDefinitions.values());
    }

    //-------------------------------------------------------------------------
    private void addReferenceTab(
        Path segmentIdentity,
        Map<Path,DataproviderObject> panes,        
        StructuralFeatureDefinition feature,
        String forClass,
        ModelElement_1_0 inspectorClassDef,
        Map<Path,DataproviderObject_1_0> elementDefinitions
    ) throws ServiceException {
        ModelElement_1_0 featureType = this.model.getDereferencedType(
            feature.getType()
        );
        DataproviderObject_1_0 baseDefinition = this.getElementDefinition(
            segmentIdentity,
            feature,
            inspectorClassDef,
            false // no default iconKey for references
        );
        // Get additional definitions
        List<DataproviderObject_1_0> definitions = new ArrayList<DataproviderObject_1_0>();
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
        for(DataproviderObject_1_0 definition: definitions) {
            if(
                definition.values("active").isEmpty() || // default to true if active is not set
                ((Boolean)definition.values("active").get(0)).booleanValue()
            ) {            
                DataproviderObject pane = this.addPane(
                    segmentIdentity,
                    PANE_TYPE_REFERENCE,
                    inspectorClassDef,
                    definition,
                    panes
                ); 
                String alternateDefinitionNameSuffix = definition != baseDefinition ? 
                    ":" + definition.path().getBase() : 
                    "";
                // Create new ObjectContainer if it is a reference of classDef
                DataproviderObject container = this.existingElements.get(segmentIdentity).get(
                    this.model.getElement(feature.getContainer()).objGetValue("qualifiedName") + ":Ref:" + feature.getName() + alternateDefinitionNameSuffix
                );
                if(
                    (container == null) ||
                    (feature.getQualifiedName()).startsWith(inspectorClassDef.objGetValue("qualifiedName") + ":")
                ) { 
                    String containerName = forClass + ":Ref:" + feature.getName() + alternateDefinitionNameSuffix;
                    container = new DataproviderObject(
                        segmentIdentity.getDescendant(new String[]{"element", containerName})
                    );
                    container.values(SystemAttributes.OBJECT_CLASS).add("org:openmdx:ui1:ObjectContainer");
                    Boolean isChangeable = feature.isChangeable();
                    isChangeable = definition.values("changeable").isEmpty() ? 
                        isChangeable : 
                        (Boolean)definition.values("changeable").get(0);
                    this.setLabelledFieldDefault(
                        container,
                        definition.values("label"),
                        definition.values("shortLabel"),
                        definition.values("toolTip"),
                        isChangeable.booleanValue(),
                        "N/A",
                        (String)definition.values("color").get(0),
                        (String)definition.values("backColor").get(0)
                    );
                    container.values("showMaxMember").addAll(
                        definition.values("showMaxMember")
                    );
                    container.values("inPlaceEditable").addAll(
                        definition.values("inPlace")
                    );
                    container.values("dataBindingName").addAll(
                        definition.values("dataBindingName")
                    );
                    // let the renderer set default label
                    container.values("label").clear();
                    this.setObjectReference(
                        container,
                        feature
                    );
                    // Container must contain all fields of referenced type and all its subtypes
                    this.addFieldsToObjectContainer(
                        segmentIdentity,
                        container,
                        containerName,
                        definition.values("showMemberRange"),
                        this.getStructuralFeatureDefinitions(
                            segmentIdentity,
                            featureType,
                            true, // includeSubtypes
                            true // attributesOnly
                        ),
                        featureType,
                        null,
                        null
                    );
                    // Limit number of members to maxMember (if defined)
                    int maxMember = !definition.values("maxMember").isEmpty() ? 
                        ((Number)definition.values("maxMember").get(0)).intValue()
                        : DEFAULT_MAX_MEMBER;
                    if(container.values("member").size() > maxMember) {
                        List<Object> limitedMembers = new ArrayList<Object>(
                            container.values("member").subList(0, maxMember)
                        );
                        container.clearValues("member").addAll(
                            limitedMembers
                        );
                    }
                    this.storeElement(
                        segmentIdentity,
                        container
                    );
                }
                // Tab
                String tabName = pane.path().getBase() + ":Tab:" + feature.getName() + alternateDefinitionNameSuffix;
                DataproviderObject tab = new DataproviderObject(
                    segmentIdentity.getDescendant(new String[]{"element", tabName})
                );
                tab.values(SystemAttributes.OBJECT_CLASS).add("org:openmdx:ui1:Tab");
                tab.values("title").addAll(
                    definition.values("label")
                );
                elementDefinitions.put(
                    tab.path(),
                    definition
                );
                // Add tab to pane and sort by order
                List<Object> members = pane.values("member");
                int l = 0;
                int r = members.size() - 1;
                while(l <= r) {
                    int pos = (l + r) / 2;
                    int res = this.compareOrder(
                        definition.values("order"),
                        ((DataproviderObject_1_0)elementDefinitions.get(members.get(pos))).values("order")
                    );
                    if(res == 0) { l = pos; r = pos; break;} 
                    else if(res > 0) l = pos + 1;
                    else if(res < 0) r = pos - 1;
                }
                if(members.isEmpty()) {
                    members.add(
                        tab.path()
                    );
                }
                else {
                    members.add(
                        l,
                        tab.path()
                    );              
                }
                // add object container to tab
                tab.values("member").add(
                    container.path()
                );
                this.storeElement(
                    segmentIdentity,
                    tab
                );
            }
        }
    }

    //-------------------------------------------------------------------------
    private String getPaneName(
        String paneType,
        ModelElement_1_0 inspectorClass
    ) throws ServiceException {
        String id = "Multi";
        if(PANE_TYPE_ATTRIBUTE.equals(paneType)) {
            id = "Attr";
        }
        else if(PANE_TYPE_REFERENCE.equals(paneType)) {
            id = "Ref";
        }
        else if(PANE_TYPE_OPERATION.equals(paneType)) {
            id = "Op";
        }
        return inspectorClass == null ? 
            "Pane:" + paneType : 
            inspectorClass.objGetValue("qualifiedName") + ":Pane:" + id;
    }

    //-------------------------------------------------------------------------
    private DataproviderObject addTab(
        String tabName,
        String tabType,
        DataproviderObject_1_0 tabDef,
        DataproviderObject pane,
        Map<Path,DataproviderObject_1_0> tabDefs
    ) throws ServiceException {
        Path segmentIdentity = pane.path().getPrefix(5);
        Path tabIdentity = segmentIdentity.getDescendant(
            new String[]{"element", tabName}
        );
        DataproviderObject tab = new DataproviderObject(tabIdentity);
        tab.values(SystemAttributes.OBJECT_CLASS).add(tabType);
        this.setElementDefault(
            tab,
            tabDef.values("toolTip"),
            tabDef.values("changeable").isEmpty() ? 
                this.changableDefaultValue : 
                ((Boolean)tabDef.values("changeable").get(0)).booleanValue(),
            (String)tabDef.values("iconKey").get(0)
        );
        tab.values("title").addAll(
            tabDef.values("label")
        );
        this.setElementDefinitionDefaultLayout(tab);
        // Add tab to pane and sort by order
        List<Object> members = pane.values("member");
        int l = 0;
        int r = members.size() - 1;
        while(l <= r) {
            int pos = (l + r) / 2;
            if(tabDefs.get(members.get(pos)) == null) {
                System.out.println("#ERR");
            }
            int res = this.compareOrder(
                tabDef.values("order"),
                ((DataproviderObject_1_0)tabDefs.get(members.get(pos))).values("order")
            );
            if(res == 0) { l = pos; r = pos; break;} 
            else if(res > 0) l = pos + 1;
            else if(res < 0) r = pos - 1;
        }
        if(members.isEmpty()) {
            members.add(
                tabIdentity
            );
        }
        else {
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
    }

    //-------------------------------------------------------------------------
    private DataproviderObject addPane(
        Path segmentIdentity,
        String paneType,
        ModelElement_1_0 inspectorClass,
        DataproviderObject_1_0 uiDef,
        Map<Path,DataproviderObject> panes
    ) throws ServiceException {
        String paneName = this.getPaneName(
            paneType, 
            inspectorClass
        );
        Number paneOrder = uiDef == null ? 
            null : 
            !uiDef.values("order").isEmpty() ? 
                (Number)uiDef.values("order").get(0) : 
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
        DataproviderObject_1_0 paneDef = this.getElementDefinition(
            segmentIdentity,
            paneIdentity.getBase(),
            null,
            true
        );
        DataproviderObject pane = panes.get(paneIdentity);
        if(pane == null) {
            pane = new DataproviderObject(paneIdentity);
            pane.values(SystemAttributes.OBJECT_CLASS).add(paneType);
            this.setElementDefault(
                pane,
                paneDef.values("toolTip"),
                !paneDef.values("isChangeable").isEmpty() ? 
                    ((Boolean)paneDef.values("isChangeable").get(0)).booleanValue() : 
                    true, 
                (String)paneDef.values("iconKey").get(0)
            );
            panes.put(
                paneIdentity,
                pane
            );
        }
        return pane;  
    }

    //-------------------------------------------------------------------------
    private void addOperationPane(
        Path segmentIdentity,
        Map<Path,DataproviderObject> panes,
        OperationDefinition operationDef,
        ModelElement_1_0 inspectorClass,
        Map<Path,DataproviderObject_1_0> elementDefinitions
    ) throws ServiceException {
        DataproviderObject_1_0 baseDefinition = this.getOperationDefinition(
            segmentIdentity,
            operationDef,
            null,
            0,
            inspectorClass
        );
        if(((Boolean)baseDefinition.values("active").get(0)).booleanValue()) {
            List<DataproviderObject_1_0> opDefs = new ArrayList<DataproviderObject_1_0>();
            opDefs.add(baseDefinition);
            this.addAdditionalElementDefinitions(
                baseDefinition, 
                inspectorClass, 
                opDefs
            );
            // Generate operation tabs for base definition and all alternate definitions
            for(DataproviderObject_1_0 opDef: opDefs) {
                // Pane
                DataproviderObject pane = this.addPane(
                    segmentIdentity,
                    PANE_TYPE_OPERATION,
                    inspectorClass,
                    opDef,
                    panes
                );
                // Tab
                String tabName = this.getPaneName(PANE_TYPE_OPERATION, inspectorClass) + ":Tab:" + operationDef.getName();
                if(opDef != baseDefinition) {
                    tabName += ":" + opDef.path().getBase();
                }	    
                DataproviderObject tab = this.addTab(
                    tabName,
                    TAB_TYPE_OPERATION,
                    opDef,
                    pane,
                    elementDefinitions
                );
                tab.values("operationName").add(
                    operationDef.getQualifiedName()
                );
                if(tab.getValues("isQuery") != null) {
                    tab.values("isQuery").add(
                        Boolean.valueOf(operationDef.isQuery)
                    );
                }                        
                // Modeled operation
                if(operationDef.getModelElement() != null) {
                    // Map parameter to FieldGroup
                    for(
                        Iterator j = operationDef.getModelElement().objGetList("content").iterator();
                        j.hasNext();
                    ) {        
                        // create group and add to tab
                        ModelElement_1_0 paramDef = this.model.getElement(j.next());
                        DataproviderObject_1_0 groupElementDefinition = this.getOperationDefinition(
                            segmentIdentity,
                            operationDef,
                            (String)paramDef.objGetValue("name"),
                            1,
                            inspectorClass
                        );
                        String groupName = tabName + ":Group:" + paramDef.objGetValue("name");
                        DataproviderObject group = new DataproviderObject(
                            segmentIdentity.getDescendant(new String[]{"element", groupName})
                        );
                        group.values(SystemAttributes.OBJECT_CLASS).add("org:openmdx:ui1:FieldGroup");
                        this.setLabelledFieldDefault(
                            group,
                            groupElementDefinition.values("label"),
                            groupElementDefinition.values("shortLabel"),
                            groupElementDefinition.values("toolTip"),
                            groupElementDefinition.values("changeable").isEmpty() ? 
                                this.changableDefaultValue : 
                                ((Boolean)groupElementDefinition.values("changeable").get(0)).booleanValue(),
                            "N/A",
                            (String)groupElementDefinition.values("color").get(0),
                            (String)groupElementDefinition.values("backColor").get(0)
                        );
                        this.setElementDefinitionDefaultLayout(group);
                        tab.values("member").add(
                            group.path()
                        );
                        group.values("showMaxMember").addAll(
                            groupElementDefinition.values("showMaxMember")
                        );
                        group.values("inPlaceEditable").addAll(
                            groupElementDefinition.values("inPlace")
                        );
                        // Add fields of parameter to group
                        ModelElement_1_0 paramType = this.model.getElementType(
                            paramDef
                        );
                        if(this.model.isStructureType(paramType)) {
                            this.addFieldsToObjectContainer(
                                segmentIdentity,
                                group,
                                groupName,
                                null,
                                this.getStructFeatureDefinitions(paramType),		              
                                paramType,
                                this.getMemberMimeTypes(opDef),
                                this.getMemberDefaultValues(opDef)
                            );
                        }      
                        group.values("columnSizeMin").add(new Integer(330));
                        group.values("columnSizeMax").add(new Integer(600));
                        group.values("columnBreakAtElement").add(new Integer(999)); // no break
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
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private List<DataproviderObject> getAdditionalInspectorDefinitions(
        DataproviderObject_1_0 inspectorDefinition
    ) throws ServiceException {
        List<DataproviderObject> inspectorDefinitions = new ArrayList<DataproviderObject>();
        // Get additional inspector definitions
        List<DataproviderObject_1_0> additionalInspectorDefinitions = this.delegation.addFindRequest(
            inspectorDefinition.path().getChild("additionalElementDefinition"),
            null,
            AttributeSelectors.ALL_ATTRIBUTES,
            0,
            Integer.MAX_VALUE,
            Directions.ASCENDING
        );
        for(DataproviderObject_1_0 additionalInspectorDefinition: additionalInspectorDefinitions) {
            DataproviderObject target = new DataproviderObject(
                additionalInspectorDefinition.path()
            );
            this.overloadDefinition(
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

    //-------------------------------------------------------------------------
    private OperationDefinition createBasicObjectOperationDef(
        String qualifiedOperationName,
        boolean isQuery
    ) throws ServiceException {
        return new OperationDefinition(
            qualifiedOperationName.substring(qualifiedOperationName.lastIndexOf(":") + 1),
            qualifiedOperationName,
            isQuery,
            this.model.getElement("org:openmdx:base:BasicObject")
        );
    }

    //-------------------------------------------------------------------------
    private void createInspector(
        Path segmentIdentity,
        String forClass
    ) throws RefException, ServiceException {
        // Return if inspector already exists or visited in unit of work
        if(this.existingElements.get(segmentIdentity).get(forClass) != null) {
            return;
        }
        ModelElement_1_0 classDef = this.model.getElement(forClass);
        // Create inspector for all supertypes
        for(
            Iterator i = classDef.objGetList("allSupertype").iterator();
            i.hasNext(); 
        ) {
            ModelElement_1_0 supertype = this.model.getElement(i.next());
            if(supertype != classDef) {
                this.createInspector(
                    segmentIdentity,
                    (String)supertype.objGetValue("qualifiedName")
                );
            }
        }
        // Inspector
        DataproviderObject_1_0 inspectorDefinition = this.getInspectorElementDefinition(
            segmentIdentity,
            new StructuralFeatureDefinition(classDef)
        );
        DataproviderObject inspector = new DataproviderObject(
            segmentIdentity.getDescendant(new String[]{"element", forClass})
        );
        inspector.values(SystemAttributes.OBJECT_CLASS).add("org:openmdx:ui1:Inspector");
        inspector.values("toolTip").addAll(inspectorDefinition.values("toolTip"));
        inspector.values("changeable").add(
            inspectorDefinition.values("changeable").isEmpty() ? 
            new Boolean(this.changableDefaultValue) : 
            inspectorDefinition.values("changeable").get(0)
        );
        inspector.values("forClass").add(forClass);
        inspector.values("scaleX").add(new Integer(1));
        inspector.values("scaleY").add(new Integer(1));
        // Get additional inspector definitions
        List<DataproviderObject_1_0> inspectorDefinitions = new ArrayList<DataproviderObject_1_0>();
        inspectorDefinitions.add(inspectorDefinition);
        inspectorDefinitions.addAll(
            this.getAdditionalInspectorDefinitions(
                inspectorDefinition
            )
        );
        // Add inspector title for each inspector definition
        int ii = 0;
        for(
            Iterator i = inspectorDefinitions.iterator();
            i.hasNext();
            ii++
        ) {
            DataproviderObject definition = (DataproviderObject)i.next();
            DataproviderObject inspectorTitle = new DataproviderObject(
                segmentIdentity.getDescendant(new String[]{"element", forClass + ":Title:" + ii})
            );
            inspectorTitle.values(SystemAttributes.OBJECT_CLASS).add("org:openmdx:ui1:TextField");
            this.setValuedFieldDefault(
                inspectorTitle,
                definition.values("label"),
                definition.values("shortLabel"),
                definition.values("displayValueExpr"),
                definition.values("toolTip"),
                false,
                (String)definition.values("iconKey").get(0),
                (String)definition.values("color").get(0),
                (String)definition.values("backColor").get(0),
                Multiplicities.SINGLE_VALUE,
                new Integer(1),
                new Integer(0),
                false,
                false,
                false,
                (String)classDef.objGetValue("name"),
                (String)classDef.objGetValue("qualifiedName"),
                null
            );
            this.storeElement(
                segmentIdentity,
                inspectorTitle
            );
            inspector.values("member").add(
                inspectorTitle.path()
            );
        }
        Map<Path,DataproviderObject> panes = new TreeMap<Path,DataproviderObject>();
        Map<Path,DataproviderObject_1_0> elementDefinitions = new HashMap<Path,DataproviderObject_1_0>();
        /**
         * Operation panes
         */    
        List<Ui_1.OperationDefinition> operationDefs = this.getOperationDefinitions(
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
            this.editObjectOperationDef,
            classDef,
            elementDefinitions
        );
        this.addOperationPane(
            segmentIdentity,
            panes,
            this.deleteObjectOperationDef,
            classDef,
            elementDefinitions
        );
        this.addOperationPane(
            segmentIdentity,
            panes,
            this.navigateToParentOperationDef,
            classDef,
            elementDefinitions
        );
        this.addOperationPane(
            segmentIdentity,
            panes,
            this.reloadObjectOperationDef,
            classDef,
            elementDefinitions
        );
        /**
         * Attribute pane
         */
        DataproviderObject paneAttr = this.addPane(
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
            this.getStructuralFeatureDefinitions(
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
        List<Ui_1.StructuralFeatureDefinition> structuralFeatureDefinitions = this.getStructuralFeatureDefinitions(segmentIdentity, classDef, false, false);
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
            Iterator i = panes.values().iterator();
            i.hasNext();
        ) {
            DataproviderObject pane = (DataproviderObject)i.next();
            inspector.values("member").add(
                pane.path()
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
    }

    //------------------------------------------------------------------------
    private void assertCachedElements(
        Path segmentIdentity
    ) throws ServiceException {
        // Prefetch all elements
        if(this.existingElements.get(segmentIdentity) == null) {
            Collection elements = this.delegation.addFindRequest(
                segmentIdentity.getChild("element"),
                new FilterProperty[]{},
                AttributeSelectors.ALL_ATTRIBUTES,
                0,
                Integer.MAX_VALUE,
                Directions.ASCENDING
            );
            Map<String, DataproviderObject> existingElements = new HashMap<String, DataproviderObject>();
            this.existingElements.put(
                segmentIdentity,
                existingElements
            );
            for(
                Iterator j = elements.iterator();
                j.hasNext();
            ) {
                DataproviderObject_1_0 element = (DataproviderObject_1_0)j.next();
                existingElements.put(
                    element.path().getBase(),
                    new DataproviderObject(element)
                );
            }
        }
        // Prefetch all element definitions
        if(this.existingElementDefinitions.get(segmentIdentity) == null) {
            Collection elementDefinitions = this.delegation.addFindRequest(
                segmentIdentity.getChild("elementDefinition"),
                new FilterProperty[]{},
                AttributeSelectors.ALL_ATTRIBUTES,
                0,
                Integer.MAX_VALUE,
                Directions.ASCENDING
            );
            Map<String,DataproviderObject> existingElementDefinitions = new HashMap<String,DataproviderObject>();
            this.existingElementDefinitions.put(
                segmentIdentity,
                existingElementDefinitions
            );
            for(
                Iterator j = elementDefinitions.iterator();
                j.hasNext();
            ) {
                DataproviderObject_1_0 elementDefinition = (DataproviderObject_1_0)j.next();
                existingElementDefinitions.put(
                    elementDefinition.path().getBase(),
                    new DataproviderObject(elementDefinition)
                );
            }
        }
        // Prepare feature definitions
        List<Ui_1.StructuralFeatureDefinition> structuralFeatureDefinitions = new ArrayList<Ui_1.StructuralFeatureDefinition>();
        this.structuralFeatureDefinitions.put(
            segmentIdentity,
            structuralFeatureDefinitions
        );
        List<Ui_1.OperationDefinition> operationDefinitions = new ArrayList<Ui_1.OperationDefinition>();
        this.operationDefinitions.put(
            segmentIdentity,
            operationDefinitions
        );
        List featureDefinitions = this.delegation.addFindRequest(
            segmentIdentity.getChild("featureDefinition"),
            null,
            AttributeSelectors.ALL_ATTRIBUTES,
            0,
            Integer.MAX_VALUE,
            Directions.ASCENDING
        );
        for(
            Iterator i = featureDefinitions.iterator();
            i.hasNext();
        ) {
            DataproviderObject_1_0 featureDefinition = (DataproviderObject_1_0)i.next();
            String qualifiedName = featureDefinition.path().getBase();
            String name = qualifiedName.substring(qualifiedName.lastIndexOf(":") + 1);
            String className = qualifiedName.substring(0, qualifiedName.lastIndexOf(":"));
            if("org:openmdx:ui1:StructuralFeatureDefinition".equals(featureDefinition.values(SystemAttributes.OBJECT_CLASS).get(0))) {
                String typeName = (String)featureDefinition.values("type").get(0);
                try {
                    structuralFeatureDefinitions.add(
                        new StructuralFeatureDefinition(
                            name,
                            qualifiedName,
                            this.model.getElement(typeName),
                            this.model.getElement(className),
                            (String)featureDefinition.values("multiplicity").get(0),
                            (Boolean)featureDefinition.values("changeable").get(0),
                            (Boolean)featureDefinition.values("isReference").get(0)
                        )
                    );
                }
                catch(ServiceException e) {
                    SysLog.warning("Unable to register structural feature definition (ignoring)", Arrays.asList(new String[]{name, qualifiedName, className, typeName}));
                    e.log();
                }
            }
            else if("org:openmdx:ui1:OperationDefinition".equals(featureDefinition.values(SystemAttributes.OBJECT_CLASS).get(0))) {
                Boolean isQuery = (Boolean)featureDefinition.values("isQuery").get(0);
                try {
                    operationDefinitions.add(
                        new OperationDefinition(
                            name,
                            qualifiedName,
                            isQuery == null ? 
                                true : 
                                isQuery.booleanValue(),
                            this.model.getElement(className)
                        )
                    );
                }
                catch(ServiceException e) {
                    SysLog.warning("Unable to register operation definition (ignoring)", Arrays.asList(new String[]{name, qualifiedName, className}));
                    e.log();
                }
            }
            else {
                SysLog.warning("Unable to register feature definition (ignoring)", Arrays.asList(new String[]{name, qualifiedName, className}));               
            }
        }
    }          

    //------------------------------------------------------------------------
    // Layer_1_0
    //------------------------------------------------------------------------

    /**
     * any modification resets cached elements.
     */
    public DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        this.existingElementDefinitions.clear();
        this.existingElements.clear();
        return super.create(
            header, 
            request
        );
    }

    //------------------------------------------------------------------------
    public DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        this.existingElementDefinitions.clear();
        this.existingElements.clear();
        return super.modify(
            header, 
            request
        );
    }

    //------------------------------------------------------------------------
    public DataproviderReply remove(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        this.existingElementDefinitions.clear();
        this.existingElements.clear();
        return super.remove(
            header, 
            request
        );
    }

    //------------------------------------------------------------------------
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        this.existingElementDefinitions.clear();
        this.existingElements.clear();
        return super.replace(
            header, 
            request
        );
    }

    //------------------------------------------------------------------------
    public DataproviderReply set(
        ServiceHeader header, 
        DataproviderRequest request
    ) throws ServiceException {
        this.existingElementDefinitions.clear();
        this.existingElements.clear();
        return super.set(
            header, 
            request
        );
    }

    //------------------------------------------------------------------------
    public DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        if("assertableInspector".equals(request.path().getBase())) {
            Path segmentIdentity = request.path().getPrefix(5);
            this.assertCachedElements(segmentIdentity);
            List<DataproviderObject> assertableInspectors = new ArrayList<DataproviderObject>();  
            // Return assertableInspector for all loaded classes
            for(
                Iterator i = this.model.getContent().iterator(); 
                i.hasNext();
            ) {
                ModelElement_1_0 elementDef = (ModelElement_1_0)i.next();
                if(this.model.isClassType(elementDef)) {
                    DataproviderObject assertableInspector = new DataproviderObject(
                        request.path().getChild((String)elementDef.objGetValue("qualifiedName"))
                    );
                    assertableInspector.values(SystemAttributes.OBJECT_CLASS).add(
                        "org:openmdx:ui1:AssertableInspector"
                    );
                    DataproviderObject_1_0 inspectorDefinition = this.getInspectorElementDefinition(
                        segmentIdentity,
                        new StructuralFeatureDefinition(elementDef)
                    );
                    this.getAdditionalInspectorDefinitions(
                        inspectorDefinition
                    );
                    assertableInspector.values("forClass").addAll(
                        elementDef.objGetList("qualifiedName")
                    );
                    // label
                    assertableInspector.values("label").addAll(
                        inspectorDefinition.values("label")
                    );
                    // toolTip
                    assertableInspector.values("toolTip").addAll(
                        inspectorDefinition.values("toolTip")
                    );
                    // changeable
                    assertableInspector.values("changeable").addAll(
                        inspectorDefinition.values("changeable")
                    );
                    if(assertableInspector.values("changeable").isEmpty()) {
                        assertableInspector.values("changeable").add(Boolean.TRUE);
                    }
                    // filterable
                    assertableInspector.values("filterable").addAll(
                        inspectorDefinition.values("filterable")
                    );
                    if(assertableInspector.values("filterable").isEmpty()) {
                        assertableInspector.values("filterable").add(Boolean.TRUE);
                    }
                    // sortable
                    assertableInspector.values("sortable").addAll(
                        inspectorDefinition.values("sortable")
                    );
                    if(assertableInspector.values("sortable").isEmpty()) {
                        assertableInspector.values("sortable").add(Boolean.TRUE);
                    }
                    // color
                    assertableInspector.values("color").addAll(
                        inspectorDefinition.values("color")
                    );
                    // backColor
                    assertableInspector.values("backColor").addAll(
                        inspectorDefinition.values("backColor")
                    );
                    // iconKey
                    assertableInspector.values("iconKey").addAll(
                        inspectorDefinition.values("iconKey")
                    );
                    // order
                    assertableInspector.values("order").addAll(
                        inspectorDefinition.values("order")
                    );
                    assertableInspectors.add(
                        assertableInspector
                    );
                }
            }
            DataproviderReply reply = new DataproviderReply(
                assertableInspectors
            );
            reply.context(DataproviderReplyContexts.HAS_MORE).set(0, Boolean.FALSE);
            return reply;
        }
        else {
            return super.find(
                header,
                request
            );
        }
    }

    //------------------------------------------------------------------------
    public DataproviderReply operation(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        String operation = request.path().get(
            request.path().size() - 2
        );
        DataproviderObject arguments = request.object();
        if("assertInspector".equals(operation)) {
            try {
                Path segmentIdentity = request.path().getPrefix(5);
                String forClass = (String)arguments.values("forClass").get(0);
                DataproviderObject_1_0 inspectorDef = null;
                try {
                    inspectorDef = this.delegation.addGetRequest(
                        segmentIdentity.getDescendant(new String[]{"element", forClass})
                    );
                }
                catch(ServiceException e) {} 
                if(inspectorDef == null) {
                    // Always assert perspective Root
                    if(!"Root".equals(segmentIdentity.getBase())) {
                        Path rootSegmentIdentity = segmentIdentity.getParent().getChild("Root");
                        try {
                            this.delegation.addGetRequest(
                                rootSegmentIdentity.getDescendant(new String[]{"element", forClass})
                            );
                        } 
                        catch(Exception e) {
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
            }
            catch(ServiceException e) {
                throw new ServiceException(
                    e,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.CREATION_FAILURE,
                    "Inspector for class " + arguments.values("forClass").get(0) + " can not be created",
                    new BasicException.Parameter("typeName", "org:openmdx:ui1:CanNotCreateInspector"),
                    new BasicException.Parameter("reason", e.getCause().getDescription())
                );
            }
            catch(Exception e) {
                throw new ServiceException(
                    e,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.CREATION_FAILURE,
                    "Inspector for class " + arguments.values("forClass").get(0) + " can not be created",
                    new BasicException.Parameter("typeName", "org:openmdx:ui1:CanNotCreateInspector"),
                    new BasicException.Parameter("reason", e.getMessage())
                );
            }
            DataproviderObject result = new DataproviderObject(
                request.path().getDescendant(
                    new String[]{ "reply", super.uidAsString()}
                )
            );
            result.values(SystemAttributes.OBJECT_CLASS).add("org:openmdx:base:Void");
            return new DataproviderReply(result);
        }
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "operation not supported",
            new BasicException.Parameter("operation", operation)
        );
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

    private static final String PANE_TYPE_OPERATION = "org:openmdx:ui1:OperationPane";
    private static final String PANE_TYPE_ATTRIBUTE = "org:openmdx:ui1:AttributePane";
    private static final String PANE_TYPE_REFERENCE = "org:openmdx:ui1:ReferencePane";

    private static final String TAB_TYPE_OPERATION = "org:openmdx:ui1:OperationTab";

    private static final List REFERENCES_TO_EXCLUDE = Arrays.asList(
        new String[]{
            "view",
            "org:openmdx:base:Segment:extent",
            "org:openmdx:base:ViewCapable:view",
            "org:openmdx:base:ContextCapable:context"
        }
    );  
    private Model_1_3 model = null;
    private Map<Path,Map<String,DataproviderObject>> existingElementDefinitions =
        new HashMap<Path,Map<String,DataproviderObject>>();
    private Map<Path,Map<String,DataproviderObject>> existingElements =
        new HashMap<Path,Map<String,DataproviderObject>>();
    private Map<Path,List<Ui_1.StructuralFeatureDefinition>> structuralFeatureDefinitions = 
        new HashMap<Path,List<Ui_1.StructuralFeatureDefinition>>();
    private Map<Path,List<Ui_1.OperationDefinition>> operationDefinitions =
        new HashMap<Path,List<Ui_1.OperationDefinition>>();
    private boolean changableDefaultValue = true;

    private RequestCollection delegation = null; // initialized in prolog()

    private OperationDefinition editObjectOperationDef = null;
    private OperationDefinition deleteObjectOperationDef = null;
    private OperationDefinition reloadObjectOperationDef = null;
    private OperationDefinition navigateToParentOperationDef = null;

}

//--- End of File -----------------------------------------------------------
