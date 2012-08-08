/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: FieldDef.java,v 1.10 2007/12/13 01:24:03 wfro Exp $
 * Description: FieldDef 
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/13 01:24:03 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.portal.servlet.attribute;

import java.io.Serializable;
import java.math.BigDecimal;

import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.DataBinding_1_0;

public class FieldDef
    implements Serializable {
    
    //-----------------------------------------------------------------------
    public FieldDef(
        String featureName,
        String qualifiedFeatureName,
        String multiplicity,
        boolean isChangeable,
        String iconKey,
        String color,
        String backColor,
        String defaultValue,
        DataBinding_1_0 dataBinding        
    ) {
        this.featureName = featureName;
        this.qualifiedFeatureName = qualifiedFeatureName;
        this.multiplicity = multiplicity;
        this.isChangeable = isChangeable;
        this.iconKey = iconKey;
        this.color = color;
        this.backColor = backColor;
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
    
    //-----------------------------------------------------------------------
    private FieldDef(
        String featureName,
        String qualifiedFeatureName,
        String multiplicity,
        boolean isChangeable,
        String iconKey,
        String color,
        String backColor,
        String format,
        String defaultValue,
        DataBinding_1_0 dataBinding
    ) {
        this.featureName = featureName;
        this.qualifiedFeatureName = qualifiedFeatureName;
        this.multiplicity = multiplicity;
        this.isChangeable = isChangeable;
        this.iconKey = iconKey;
        this.color = color;
        this.backColor = backColor;
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
    
    //-----------------------------------------------------------------------
    private FieldDef(
        String featureName,
        String qualifiedFeatureName,
        String multiplicity,         
        int decimalPlaces,
        BigDecimal minValue,
        BigDecimal maxValue,
        BigDecimal increment,
        boolean isChangeable,
        String iconKey,
        String color,
        String backColor,
        String defaultValue,
        DataBinding_1_0 dataBinding
    ) {
        this.featureName = featureName;
        this.qualifiedFeatureName = qualifiedFeatureName; 
        this.multiplicity = multiplicity;       
        this.decimalPlaces = decimalPlaces;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.increment = increment;
        this.isChangeable = isChangeable;
        this.iconKey = iconKey;
        this.color = color;
        this.backColor = backColor;
        this.format = null;
        this.defaultValue = defaultValue;
        this.isInPlace = false;
        this.mimeType = null;
        this.dataBinding = dataBinding;
    }

    //-----------------------------------------------------------------------
    private FieldDef(
        String featureName,
        String qualifiedFeatureName,
        String multiplicity,
        boolean isChangeable,
        String iconKey,
        String color,
        String backColor,
        String mimeType,
        boolean isInPlace,                          
        DataBinding_1_0 dataBinding
    ) {
        this.featureName = featureName;
        this.qualifiedFeatureName = qualifiedFeatureName; 
        this.multiplicity = multiplicity;
        this.isChangeable = isChangeable;
        this.iconKey = iconKey;
        this.color = color;
        this.backColor = backColor;
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
    
    //-----------------------------------------------------------------------
    public static FieldDef createFieldDef(
        ApplicationContext application,
        org.openmdx.ui1.jmi1.ValuedField f        
    ) {
        return new FieldDef(
            f.getFeatureName(),
            f.getQualifiedFeatureName(),
            f.getMultiplicity(),
            f.isChangeable(),
            f.getIconKey(),
            f.getColor(),
            f.getBackColor(),
            f.getDefaultValue(),
            application.getPortalExtension().getDataBinding(f.getDataBindingName(), application)            
        );
    }

    //-----------------------------------------------------------------------
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
            f.getIconKey(),
            f.getColor(),
            f.getBackColor(),
            f.getDefaultValue(),
            application.getPortalExtension().getDataBinding(f.getDataBindingName(), application)            
        );
    }
    
    //-----------------------------------------------------------------------
    public static FieldDef createDateFieldDef(
        ApplicationContext application,
        org.openmdx.ui1.jmi1.DateField f    
    ) {
        return new FieldDef(
            f.getFeatureName(),
            f.getQualifiedFeatureName(),
            f.getMultiplicity(),         
            f.isChangeable(),
            f.getIconKey(),
            f.getColor(),
            f.getBackColor(),
            f.getFormat(),
            f.getDefaultValue(),
            application.getPortalExtension().getDataBinding(f.getDataBindingName(), application)
        );        
    }
    
    //-----------------------------------------------------------------------
    public static FieldDef createBinaryFieldDef(
        ApplicationContext application,
        org.openmdx.ui1.jmi1.DocumentBox f    
    ) {
        return new FieldDef(
            f.getFeatureName(),
            f.getQualifiedFeatureName(),
            f.getMultiplicity(),
            f.isChangeable(),
            f.getIconKey(),
            f.getColor(),
            f.getBackColor(),
            f.getMimeType(),
            f.isInPlace(),
            application.getPortalExtension().getDataBinding(f.getDataBindingName(), application)
        );
    }
    
    //-----------------------------------------------------------------------   
    private static final long serialVersionUID = 3544958757449838900L;
    
    public final String featureName;
    public final String qualifiedFeatureName;
    public final String multiplicity;
    public final boolean isChangeable;
    public final String iconKey;
    public final String color;
    public final String backColor;
    public final String format;
    public final String defaultValue;
    public final int decimalPlaces;
    public final BigDecimal minValue;
    public final BigDecimal maxValue;
    public final BigDecimal increment;
    public final String mimeType;
    public final boolean isInPlace;
    public DataBinding_1_0 dataBinding;

}

//--- End of File -----------------------------------------------------------
