/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: FieldDef 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2013, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.attribute;

import java.io.Serializable;
import java.math.BigDecimal;

import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.DataBinding;

public class FieldDef implements Serializable {
    
    /**
     * Constructor 
     *
     * @param featureName
     * @param qualifiedFeatureName
     * @param multiplicity
     * @param isChangeable
     * @param isMandatory
     * @param iconKey
     * @param color
     * @param backColor
     * @param cssClassFieldGroup
     * @param cssClassObjectContainer
     * @param defaultValue
     * @param dataBinding
     */
    public FieldDef(
        String featureName,
        String qualifiedFeatureName,
        String multiplicity,
        boolean isChangeable,
        boolean isMandatory,
        String iconKey,
        String color,
        String backColor,
        String cssClassFieldGroup,
        String cssClassObjectContainer,
        String defaultValue,
        DataBinding dataBinding        
    ) {
        this.featureName = featureName;
        this.qualifiedFeatureName = qualifiedFeatureName;
        this.multiplicity = multiplicity;
        this.isChangeable = isChangeable;
        this.isMandatory = isMandatory;
        this.iconKey = iconKey;
        this.color = color;
        this.backColor = backColor;
        this.cssClassFieldGroup = cssClassFieldGroup;
        this.cssClassObjectContainer = cssClassObjectContainer;
        this.format = null;
        this.defaultValue = defaultValue;
        this.decimalPlaces = -1;
        this.minValue = null;
        this.maxValue = null;
        this.increment = null;
        this.isInPlace = false;
        this.mimeType = null;
        this.dataBinding = dataBinding;
    }
    
    /**
     * Constructor 
     *
     * @param featureName
     * @param qualifiedFeatureName
     * @param multiplicity
     * @param isChangeable
     * @param isMandatory
     * @param iconKey
     * @param color
     * @param backColor
     * @param cssClassFieldGroup
     * @param cssClassObjectContainer
     * @param format
     * @param defaultValue
     * @param dataBinding
     */
    private FieldDef(
        String featureName,
        String qualifiedFeatureName,
        String multiplicity,
        boolean isChangeable,
        boolean isMandatory,
        String iconKey,
        String color,
        String backColor,
        String cssClassFieldGroup,
        String cssClassObjectContainer,
        String format,
        String defaultValue,
        DataBinding dataBinding
    ) {
        this.featureName = featureName;
        this.qualifiedFeatureName = qualifiedFeatureName;
        this.multiplicity = multiplicity;
        this.isChangeable = isChangeable;
        this.isMandatory = isMandatory;
        this.iconKey = iconKey;
        this.color = color;
        this.backColor = backColor;
        this.cssClassFieldGroup = cssClassFieldGroup;
        this.cssClassObjectContainer = cssClassObjectContainer;
        this.format = format;
        this.defaultValue = defaultValue;
        this.decimalPlaces = -1;
        this.minValue = null;
        this.maxValue = null;
        this.increment = null;
        this.isInPlace = false;
        this.mimeType = null;
        this.dataBinding = dataBinding;
    }
    
    /**
     * Constructor 
     *
     * @param featureName
     * @param qualifiedFeatureName
     * @param multiplicity
     * @param decimalPlaces
     * @param minValue
     * @param maxValue
     * @param increment
     * @param isChangeable
     * @param isMandatory
     * @param iconKey
     * @param color
     * @param backColor
     * @param cssClassFieldGroup
     * @param cssClassObjectContainer
     * @param defaultValue
     * @param dataBinding
     */
    private FieldDef(
        String featureName,
        String qualifiedFeatureName,
        String multiplicity,         
        int decimalPlaces,
        BigDecimal minValue,
        BigDecimal maxValue,
        BigDecimal increment,
        boolean isChangeable,
        boolean isMandatory,
        String iconKey,
        String color,
        String backColor,
        String cssClassFieldGroup,
        String cssClassObjectContainer,        
        String defaultValue,
        DataBinding dataBinding
    ) {
        this.featureName = featureName;
        this.qualifiedFeatureName = qualifiedFeatureName; 
        this.multiplicity = multiplicity;       
        this.decimalPlaces = decimalPlaces;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.increment = increment;
        this.isChangeable = isChangeable;
        this.isMandatory = isMandatory;
        this.iconKey = iconKey;
        this.color = color;
        this.backColor = backColor;
        this.cssClassFieldGroup = cssClassFieldGroup;
        this.cssClassObjectContainer = cssClassObjectContainer;        
        this.format = null;
        this.defaultValue = defaultValue;
        this.isInPlace = false;
        this.mimeType = null;
        this.dataBinding = dataBinding;
    }

    /**
     * Constructor 
     *
     * @param featureName
     * @param qualifiedFeatureName
     * @param multiplicity
     * @param isChangeable
     * @param isMandatory
     * @param iconKey
     * @param color
     * @param backColor
     * @param cssClassFieldGroup
     * @param cssClassObjectContainer
     * @param mimeType
     * @param isInPlace
     * @param dataBinding
     */
    private FieldDef(
        String featureName,
        String qualifiedFeatureName,
        String multiplicity,
        boolean isChangeable,
        boolean isMandatory,
        String iconKey,
        String color,
        String backColor,
        String cssClassFieldGroup,
        String cssClassObjectContainer,
        String mimeType,
        boolean isInPlace,
        DataBinding dataBinding
    ) {
        this.featureName = featureName;
        this.qualifiedFeatureName = qualifiedFeatureName; 
        this.multiplicity = multiplicity;
        this.isChangeable = isChangeable;
        this.isMandatory = isMandatory;
        this.iconKey = iconKey;
        this.color = color;
        this.backColor = backColor;
        this.cssClassFieldGroup = cssClassFieldGroup;
        this.cssClassObjectContainer = cssClassObjectContainer;        
        this.mimeType = mimeType;
        this.isInPlace = isInPlace;
        this.format = null;
        this.defaultValue = null;
        this.decimalPlaces = -1;
        this.minValue = null;
        this.maxValue = null;
        this.increment = null;
        this.dataBinding = dataBinding;
    }
    
    /**
     * Create field definition.
     * 
     * @param application
     * @param f
     * @return
     */
    public static FieldDef createFieldDef(
        ApplicationContext application,
        org.openmdx.ui1.jmi1.ValuedField f        
    ) {
        return new FieldDef(
            f.getFeatureName(),
            f.getQualifiedFeatureName(),
            f.getMultiplicity(),
            f.isChangeable(),
            f.isMandatory(),
            f.getIconKey(),
            f.getColor(),
            f.getBackColor(),
            f.getCssClassFieldGroup(),
            f.getCssClassObjectContainer(),
            f.getDefaultValue(),
            application.getPortalExtension().getDataBinding(f.getDataBindingName())            
        );
    }

    /**
     * Create number field definition.
     * 
     * @param application
     * @param f
     * @return
     */
    public static FieldDef createNumberFieldDef(
        ApplicationContext application,
        org.openmdx.ui1.jmi1.NumberField f    
    ) {
        return new FieldDef(
            f.getFeatureName(),
            f.getQualifiedFeatureName(),
            f.getMultiplicity(),         
            f.getDecimalPlaces(),
            f.getMinValue(),
            f.getMaxValue(),
            f.getIncrement(),
            f.isChangeable(),
            f.isMandatory(),
            f.getIconKey(),
            f.getColor(),
            f.getBackColor(),
            f.getCssClassFieldGroup(),
            f.getCssClassObjectContainer(),
            f.getDefaultValue(),
            application.getPortalExtension().getDataBinding(f.getDataBindingName())            
        );
    }
    
    /**
     * Create date field definition.
     * 
     * @param application
     * @param f
     * @return
     */
    public static FieldDef createDateFieldDef(
        ApplicationContext application,
        org.openmdx.ui1.jmi1.DateField f    
    ) {
        return new FieldDef(
            f.getFeatureName(),
            f.getQualifiedFeatureName(),
            f.getMultiplicity(),         
            f.isChangeable(),
            f.isMandatory(),
            f.getIconKey(),
            f.getColor(),
            f.getBackColor(),
            f.getCssClassFieldGroup(),
            f.getCssClassObjectContainer(),
            f.getFormat(),
            f.getDefaultValue(),
            application.getPortalExtension().getDataBinding(f.getDataBindingName())
        );        
    }
    
    /**
     * Create binary field definition.
     * 
     * @param application
     * @param f
     * @return
     */
    public static FieldDef createBinaryFieldDef(
        ApplicationContext application,
        org.openmdx.ui1.jmi1.DocumentBox f    
    ) {
        return new FieldDef(
            f.getFeatureName(),
            f.getQualifiedFeatureName(),
            f.getMultiplicity(),
            f.isChangeable(),
            f.isMandatory(),
            f.getIconKey(),
            f.getColor(),
            f.getBackColor(),
            f.getCssClassFieldGroup(),
            f.getCssClassObjectContainer(),
            f.getMimeType(),
            f.isInPlace(),
            application.getPortalExtension().getDataBinding(f.getDataBindingName())
        );
    }
    
    /**
	 * Retrieve cssClassFieldGroup.
	 *
	 * @return Returns the cssClassFieldGroup.
	 */
	public String getCssClassFieldGroup(
	) {
		return this.cssClassFieldGroup;
	}

	/**
	 * Retrieve cssClassObjectContainer.
	 *
	 * @return Returns the cssClassObjectContainer.
	 */
	public String getCssClassObjectContainer(
	) {
		return this.cssClassObjectContainer;
	}	

    //-----------------------------------------------------------------------   
    private static final long serialVersionUID = 3544958757449838900L;
    
    public final String featureName;
    public final String qualifiedFeatureName;
    public final String multiplicity;
    public final boolean isChangeable;
    public final boolean isMandatory;
    public final String iconKey;
    public final String color;
    public final String backColor;
    public final String cssClassFieldGroup;
	public final String cssClassObjectContainer;
    public final String format;
    public final String defaultValue;
    public final int decimalPlaces;
    public final BigDecimal minValue;
    public final BigDecimal maxValue;
    public final BigDecimal increment;
    public final String mimeType;
    public final boolean isInPlace;
    public DataBinding dataBinding;

}

//--- End of File -----------------------------------------------------------
