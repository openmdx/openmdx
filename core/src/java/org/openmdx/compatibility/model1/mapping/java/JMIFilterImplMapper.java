/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: JMIFilterImplMapper.java,v 1.11 2008/04/02 17:39:09 wfro Exp $
 * Description: JMIFilterTemplate 
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/02 17:39:09 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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

package org.openmdx.compatibility.model1.mapping.java;

import java.io.Writer;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.model1.mapping.AttributeDef;
import org.openmdx.model1.mapping.ClassifierDef;
import org.openmdx.model1.mapping.MapperUtils;
import org.openmdx.model1.mapping.ReferenceDef;
import org.openmdx.model1.mapping.StructuralFeatureDef;

public class JMIFilterImplMapper
    extends JMIAbstractMapper {

    //-----------------------------------------------------------------------
    public JMIFilterImplMapper(
        Writer writer,
        Model_1_3 model,
        String format, 
        String packageSuffix
    ) {
        super(
            writer,
            model,
            format, 
            packageSuffix
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.model1.mapping.java.JMIAbstractMapper#getId()
     */
    protected String mapperId() {
        return "$Id: JMIFilterImplMapper.java,v 1.11 2008/04/02 17:39:09 wfro Exp $";
    }

    //-----------------------------------------------------------------------
    public void mapImplStructureFieldIsStruct(
        StructuralFeatureDef structureFieldDef
    ) throws ServiceException {
        this.trace("Filter/ImplStructureFieldIsStruct");
        this.pw.println("  public void forAll" + structureFieldDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getType(structureFieldDef.getQualifiedTypeName()) + "Filter filter");
        this.pw.println("  ) {");
        this.pw.println("    refAddValue(");
        this.pw.println("      \"" + structureFieldDef.getQualifiedName() + "\",");
        this.pw.println("      org.openmdx.compatibility.base.query.Quantors.FOR_ALL,");
        this.pw.println("      operator,");
        this.pw.println("      filter");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("  public void thereExists" + structureFieldDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getType(structureFieldDef.getQualifiedTypeName()) + "Filter filter");
        this.pw.println("  ) {");
        this.pw.println("    refAddValue(");
        this.pw.println("      \"" + structureFieldDef.getQualifiedName() + "\",");
        this.pw.println("      org.openmdx.compatibility.base.query.Quantors.THERE_EXISTS,");
        this.pw.println("      operator,");
        this.pw.println("      filter");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println(); 
        mapStructuralFeature(structureFieldDef);
    }
    
    //-----------------------------------------------------------------------
    public void mapImplStructureFieldIsNotStruct(
        AttributeDef fieldDef
    ) throws ServiceException {
        this.trace("Filter/ImplStructureFieldIsNotStruct");
        this.pw.println("  public void forAll" + fieldDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getCollectionType(fieldDef) + " values");
        this.pw.println("  ) {");
        this.pw.println("    refAddValue(");
        this.pw.println("      \"" + fieldDef.getQualifiedName() + "\",");
        this.pw.println("      org.openmdx.compatibility.base.query.Quantors.FOR_ALL,");
        this.pw.println("      operator,");
        this.pw.println("      values");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("  public void thereExists" + fieldDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getCollectionType(fieldDef) + " values");
        this.pw.println("  ) {");
        this.pw.println("    refAddValue(");
        this.pw.println("      \"" + fieldDef.getQualifiedName() + "\",");
        this.pw.println("      org.openmdx.compatibility.base.query.Quantors.THERE_EXISTS,");
        this.pw.println("      operator,");
        this.pw.println("      values");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("  public void forAll" + fieldDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + getType(fieldDef.getQualifiedTypeName()) + "[] filterValues");
        this.pw.println("  ) {");
        this.arrayToList( fieldDef, "arrayAsList", "filterValues" );
        this.pw.println("    forAll" + fieldDef.getBeanGenericName() + " (");
        this.pw.println("      operator,");
        this.pw.println("      arrayAsList");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("  public void thereExists" + fieldDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + getType(fieldDef.getQualifiedTypeName()) + "[] filterValues");
        this.pw.println("  ) {");
        this.arrayToList( fieldDef, "arrayAsList", "filterValues" );
        this.pw.println("    thereExists" + fieldDef.getBeanGenericName() + " (");
        this.pw.println("      operator,");
        this.pw.println("      arrayAsList");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("  public void orderBy" + fieldDef.getBeanGenericName() + " (");
        this.pw.println("    short order");
        this.pw.println("  ) {");
        this.pw.println("    refAddValue(");
        this.pw.println("      \"" + fieldDef.getQualifiedName() + "\",");
        this.pw.println("      order");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();  
        mapStructuralFeature(fieldDef);
    }
    
    //-----------------------------------------------------------------------
    public void mapImplReference(
        ReferenceDef referenceDef
    ) throws ServiceException {
        this.trace("Filter/ImplReference");
        this.pw.println("  public void forAll" + referenceDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedTypeName()) + "[] filterValues");
        this.pw.println("  ) {");
        this.arrayToList( referenceDef, "arrayAsList", "filterValues" );
        this.pw.println("    forAll" + referenceDef.getBeanGenericName() + " (");
        this.pw.println("      operator,");
        this.pw.println("      arrayAsList");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();
        this.pw.println("  public void thereExists" + referenceDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedTypeName()) + "[] filterValues");
        this.pw.println("  ) {");
        this.arrayToList( referenceDef, "arrayAsList", "filterValues" );
        this.pw.println("    thereExists" + referenceDef.getBeanGenericName() + " (");
        this.pw.println("      operator,");
        this.pw.println("      arrayAsList");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();
        this.pw.println("  public void forAll" + referenceDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getCollectionType(referenceDef) + " values");
        this.pw.println("  ) {");
        this.pw.println("    refAddValue(");
        this.pw.println("      \"" + referenceDef.getQualifiedName() + "\",");
        this.pw.println("      org.openmdx.compatibility.base.query.Quantors.FOR_ALL,");
        this.pw.println("      operator,");
        this.pw.println("      values");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();
        this.pw.println("  public void thereExists" + referenceDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getCollectionType(referenceDef) + " values");
        this.pw.println("  ) {");
        this.pw.println("    refAddValue(");
        this.pw.println("      \"" + referenceDef.getQualifiedName() + "\",");
        this.pw.println("      org.openmdx.compatibility.base.query.Quantors.THERE_EXISTS,");
        this.pw.println("      operator,");
        this.pw.println("      values");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();  
        mapStructuralFeature(referenceDef);
    }
    
    //-----------------------------------------------------------------------
    protected void mapStructuralFeature(
        StructuralFeatureDef featureDef
    ) throws ServiceException{
        String queryType = getQueryType(featureDef.getQualifiedTypeName()); 
        boolean qualifiedReference =
            featureDef instanceof ReferenceDef &&
            ((ReferenceDef)featureDef).getQualifiedQualifierTypeName() != null;        
        if(
            Multiplicities.SINGLE_VALUE.equals(featureDef.getMultiplicity()) &&
            !qualifiedReference
        ) {
            this.pw.println("  public " + queryType + ' ' + getPredicateName(null, featureDef) + '(');
            this.pw.println("  ){");
            this.pw.println("    return (" + queryType + ")refGetPredicate(");
            this.pw.println("      \"" + featureDef.getQualifiedName() + "\"");
            this.pw.println("    );");
            this.pw.println("  }");
            this.pw.println();
        } else {
            if(
                Multiplicities.OPTIONAL_VALUE.equals(featureDef.getMultiplicity()) &&
                !qualifiedReference
            ) {
                this.pw.println("  public org.w3c.cci2.OptionalFeaturePredicate " + getPredicateName(null, featureDef) + '(');
                this.pw.println("  ){");
                this.pw.println("    return (org.w3c.cci2.OptionalFeaturePredicate)refGetPredicate(");
                this.pw.println("      \"" + featureDef.getQualifiedName() + "\"");
                this.pw.println("    );");
                this.pw.println("  }");
                this.pw.println();
            } else {
                this.pw.println("  public org.w3c.cci2.MultivaluedFeaturePredicate " + getPredicateName(null, featureDef) + '(');
                this.pw.println("  ){");
                this.pw.println("    return (org.w3c.cci2.MultivaluedFeaturePredicate)refGetPredicate(");
                this.pw.println("      \"" + featureDef.getQualifiedName() + "\"");
                this.pw.println("    );");
                this.pw.println("  }");
                this.pw.println();
            }
            this.pw.println("  public " + queryType + ' ' + getPredicateName("thereExists", featureDef) + '(');
            this.pw.println("  ){");
            this.pw.println("    return (" + queryType + ")refGetPredicate(");
            this.pw.println("      org.openmdx.compatibility.base.query.Quantors.THERE_EXISTS,");
            this.pw.println("      \"" + featureDef.getQualifiedName() + "\"");
            this.pw.println("    );");
            this.pw.println("  }");
            this.pw.println();
            this.pw.println("  public " + queryType + ' ' + getPredicateName("forAll", featureDef) + '(');
            this.pw.println("  ){");
            this.pw.println("    return (" + queryType + ")refGetPredicate(");
            this.pw.println("      org.openmdx.compatibility.base.query.Quantors.FOR_ALL,");
            this.pw.println("      \"" + featureDef.getQualifiedName() + "\"");
            this.pw.println("    );");
            this.pw.println("  }");
            this.pw.println();
        }
        if(this.model.isPrimitiveType(featureDef.getQualifiedTypeName())) {
            String orderType = getOrderType(featureDef);
            this.pw.println("  public " + orderType + ' ' + getPredicateName("orderBy", featureDef) + '(');
            this.pw.println("  ) {");            
            this.pw.println("    return (" + orderType + ")refGetOrder(");
            this.pw.println("      \"" + featureDef.getQualifiedName() + "\"");
            this.pw.println("    );");
            this.pw.println("  }");
            this.pw.println();
        }
    }
    
    //-----------------------------------------------------------------------
    public void mapImplEnd(
    ) {
        this.trace("Filter/ImplEnd");
        this.pw.println("}        ");
    }
    
    //-----------------------------------------------------------------------
    public void mapImplBegin(
        ClassifierDef classifierDef
    ) {
        this.trace("Filter/ImplBegin");
        this.fileHeader();
        String qualifiedName = classifierDef.getQualifiedName();
        List<String> nameComponents = MapperUtils.getNameComponents(MapperUtils.getPackageName(qualifiedName));
        this.pw.println("package " + this.getNamespace(nameComponents) + ";");
        this.pw.println();
        this.pw.println("@SuppressWarnings({\"serial\",\"unchecked\"})");
        this.pw.println("public class " + classifierDef.getName() + "FilterImpl");
        this.pw.println("  extends org.openmdx.base.accessor.jmi.spi.RefPredicate_1");
        this.pw.println("  implements " + classifierDef.getName() + "Filter, " + getNamespace(nameComponents, "query") + '.' + classifierDef.getName() + "Query {");
        this.pw.println();
        this.pw.println("  public " + classifierDef.getName() + "FilterImpl(");
        this.pw.println("    org.openmdx.base.accessor.jmi.cci.RefPackage_1_0 aPackage");
        this.pw.println("  ) {");
        this.pw.println("    this(");
        this.pw.println("      aPackage,");
        this.pw.println("      null,");
        this.pw.println("      null");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();
        this.pw.println("  public " + classifierDef.getName() + "FilterImpl(");
        this.pw.println("    org.openmdx.base.accessor.jmi.cci.RefPackage_1_0 aPackage,");
        this.pw.println("    org.openmdx.compatibility.base.query.FilterProperty[] filterProperties,");
        this.pw.println("    org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier[] attributeSpecifiers");
        this.pw.println("  ) {");
        this.pw.println("    this(");
        this.pw.println("      aPackage,");
        this.pw.println("      filterProperties,");
        this.pw.println("      attributeSpecifiers,");
        this.pw.println("      null,");
        this.pw.println("      null,");
        this.pw.println("      null");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();  
        this.pw.println("  public " + classifierDef.getName() + "FilterImpl(");
        this.pw.println("    org.openmdx.base.accessor.jmi.cci.RefPackage_1_0 aPackage,");
        this.pw.println("    org.openmdx.compatibility.base.query.FilterProperty[] filterProperties,");
        this.pw.println("    org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier[] attributeSpecifiers,");
        this.pw.println("    org.openmdx.base.accessor.jmi.cci.RefFilter_1_0 delegateFilter,");
        this.pw.println("    Short delegateQuantor,");
        this.pw.println("    String delegateName");        
        this.pw.println("  ) {");
        this.pw.println("    super(");
        this.pw.println("      aPackage,");
        this.pw.println("      \"" + qualifiedName + "\",");
        this.pw.println("      filterProperties,");
        this.pw.println("      attributeSpecifiers,");
        this.pw.println("      delegateFilter,");
        this.pw.println("      delegateQuantor,");
        this.pw.println("      delegateName");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();  
    }
    
    //-----------------------------------------------------------------------
    public void mapImplAttributeIsStruct(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Filter/ImplAttributeIsStruct");
        this.pw.println("  public void forAll" + attributeDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getType(attributeDef.getQualifiedTypeName()) + "Filter filter");
        this.pw.println("  ) {");
        this.pw.println("    refAddValue(");
        this.pw.println("      \"" + attributeDef.getQualifiedName() + "\",");
        this.pw.println("      org.openmdx.compatibility.base.query.Quantors.FOR_ALL,");
        this.pw.println("      operator,");
        this.pw.println("      filter");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("  public void thereExists" + attributeDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getType(attributeDef.getQualifiedTypeName()) + "Filter filter");
        this.pw.println("  ) {");
        this.pw.println("    refAddValue(");
        this.pw.println("      \"" + attributeDef.getQualifiedName() + "\",");
        this.pw.println("      org.openmdx.compatibility.base.query.Quantors.THERE_EXISTS,");
        this.pw.println("      operator,");
        this.pw.println("      filter");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();  
        mapStructuralFeature(attributeDef);
    }
    
    //-----------------------------------------------------------------------
    public void mapImplAttributeIsNotStruct(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Filter/ImplAttributeIsNotStruct");
        this.pw.println("  public void forAll" + attributeDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getCollectionType(attributeDef) + " values");
        this.pw.println("  ) {");
        this.pw.println("    refAddValue(");
        this.pw.println("      \"" + attributeDef.getQualifiedName() + "\",");
        this.pw.println("      org.openmdx.compatibility.base.query.Quantors.FOR_ALL,");
        this.pw.println("      operator,");
        this.pw.println("      values");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("  public void thereExists" + attributeDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getCollectionType(attributeDef) + " values");
        this.pw.println("  ) {");
        this.pw.println("    refAddValue(");
        this.pw.println("      \"" + attributeDef.getQualifiedName() + "\",");
        this.pw.println("      org.openmdx.compatibility.base.query.Quantors.THERE_EXISTS,");
        this.pw.println("      operator,");
        this.pw.println("      values");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("  public void forAll" + attributeDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getType(attributeDef.getQualifiedTypeName()) + "[] filterValues");
        this.pw.println("  ) {");
        this.arrayToList( attributeDef, "arrayAsList", "filterValues" );
        this.pw.println("    forAll" + attributeDef.getBeanGenericName() + " (");
        this.pw.println("      operator,");
        this.pw.println("      arrayAsList");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("  public void thereExists" + attributeDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getType(attributeDef.getQualifiedTypeName()) + "[] filterValues");
        this.pw.println("  ) {");
        this.arrayToList( attributeDef, "arrayAsList", "filterValues" );
        this.pw.println("    thereExists" + attributeDef.getBeanGenericName() + " (");
        this.pw.println("      operator,");
        this.pw.println("      arrayAsList");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("  public void orderBy" + attributeDef.getBeanGenericName() + " (");
        this.pw.println("    short order");
        this.pw.println("  ) {");
        this.pw.println("    refAddValue(");
        this.pw.println("      \"" + attributeDef.getQualifiedName() + "\",");
        this.pw.println("      order");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();  
        mapStructuralFeature(attributeDef);
    }
    
}
