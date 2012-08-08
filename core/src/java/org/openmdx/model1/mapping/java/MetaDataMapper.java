/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: MetaDataMapper.java,v 1.50 2008/02/13 11:05:51 hburger Exp $
 * Description: JMIInstanceTemplate 
 * Revision:    $Revision: 1.50 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/13 11:05:51 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2007, OMEX AG, Switzerland
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

package org.openmdx.model1.mapping.java;

import java.io.CharArrayWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jdo.Query;
import javax.jdo.identity.StringIdentity;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.code.PrimitiveTypes;
import org.openmdx.model1.importer.metadata.ClassMetaData;
import org.openmdx.model1.importer.metadata.ClassPersistenceModifier;
import org.openmdx.model1.importer.metadata.ColumnMetaData;
import org.openmdx.model1.importer.metadata.ExtendableMetaData;
import org.openmdx.model1.importer.metadata.ExtensionTarget;
import org.openmdx.model1.importer.metadata.FieldMetaData;
import org.openmdx.model1.importer.metadata.FieldPersistenceModifier;
import org.openmdx.model1.importer.metadata.InheritanceMetaData;
import org.openmdx.model1.importer.metadata.InheritanceStrategy;
import org.openmdx.model1.importer.metadata.JoinMetaData;
import org.openmdx.model1.importer.metadata.MetaData_2_0;
import org.openmdx.model1.importer.metadata.PackageMetaData;
import org.openmdx.model1.mapping.AttributeDef;
import org.openmdx.model1.mapping.ClassDef;
import org.openmdx.model1.mapping.MapperUtils;
import org.openmdx.model1.mapping.MetaData_1_0;
import org.openmdx.model1.mapping.Names;
import org.openmdx.model1.mapping.ReferenceDef;
import org.openmdx.model1.mapping.StructuralFeatureDef;
import org.openmdx.model1.mapping.plugin.ObjectRepositoryMetadataPlugin;

/**
 * Meta Data Mapper
 */
public class MetaDataMapper extends AbstractClassMapper {

	/**
	 * Constructor
	 * 
	 * @param classDef
	 * @param writer
	 * @param model
	 * @param format
	 * @param packageSuffix
	 * @param innerClass
	 * @param orm 
	 * @param metaData
	 * @param plugin
     * 
	 * @throws ServiceException
	 */
	public MetaDataMapper(
        ModelElement_1_0 classDef, 
        Writer writer,
		Model_1_3 model, 
        Format format, 
        String packageSuffix,
		String innerClass, 
        boolean orm,
        MetaData_1_0 metaData, 
        ObjectRepositoryMetadataPlugin plugin
    ) throws ServiceException {
        super(
            classDef,
            writer, 
            model,
            format, 
            packageSuffix, 
            metaData
        );
        this.writer = writer;
		this.innerClass = innerClass;
        this.process = innerClass == null;
        this.orm = orm;
        this.plugin = plugin;
        this.packageName = innerClass == null ? this.getNamespace(
            MapperUtils.getNameComponents(
                MapperUtils.getPackageName(
                    this.classDef.getQualifiedName()
                )
            )
        ) : null; 
	}

    /**
     * Test whether a given class is persistence aware
     * 
     * @param classDef 
     * 
     * @return <code>true</code> if the given class is persistence aware
     */
    protected boolean isPersistenceAware(
        ClassDef classDef
    ) {
        ClassMetaData classMetaData = (ClassMetaData) classDef.getClassMetaData();
        return 
            classMetaData != null && 
            classMetaData.getPersistenceModifier() == ClassPersistenceModifier.PERSISTENCE_AWARE;
    }

    /**
     * Test whether a given class is extent capable
     * 
     * @return <code>true</code> if the given class is extent capable
     */
    protected boolean isExtentCapable(
    ) {
        return this.innerClass == null && (
            this.classMetaData == null || this.classMetaData.isRequiresExtent()
        );
    }

    /**
     * 
     * @return
     * @throws ServiceException
     */
    protected String ormTableName(
    ) throws ServiceException {
        return this.classMetaData == null || this.classMetaData.getTable() == null 
            ? this.getTableName(this.qualifiedClassName) 
            : DEFAULT_IDENTIFIER_MAPPING.equals(this.classMetaData.getTable()) 
                ? null 
                : (this.innerClass == null ? this.classMetaData.getTable() : this.plugin.getSliceTableName(this.classMetaData.getTable()));
    }

    /**
     * Test whether a given feature is explicitely excluded
     * 
     * @param featureDef 
     * 
     * @return <code>true</code> if the given feature is not expicitely excluded
     * @throws ServiceException 
     */
    protected boolean isPersistent(
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        FieldMetaData fieldMetaData = getFieldMetaData(featureDef.getQualifiedName());
        return fieldMetaData == null || fieldMetaData.getPersistenceModifier() == null ? (
            !featureDef.isDerived() &&
            !isPersistenceAware(getClassDef(featureDef.getQualifiedTypeName()))
         ) : fieldMetaData.getPersistenceModifier() == FieldPersistenceModifier.PERSISTENT;
    }
    
    protected boolean isIdentity(
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        return SystemAttributes.OBJECT_IDENTITY.equals(featureDef.getName());
    }
    
    /**
     * Calculate the discriminator value
     * 
     * @return the MOF cmpliant class name
     */
    protected String getDiscriminatorValue(){
        return this.classDef.getQualifiedName();
    }

    /**
     * 
     * @return
     */
    boolean isAuthority(){
        return 
            "org:openmdx:base:Authority".equals(this.classDef.getQualifiedName());
    }

    /**
     * 
     * @return
     */
    boolean isProvider(){
        return 
            "org:openmdx:base:Provider".equals(this.classDef.getQualifiedName());
    }
    
	/**
	 * 
	 * @param featureDef
	 * @throws ServiceException
	 */
	public void mapReference(
        ReferenceDef featureDef
    ) throws ServiceException {
        if(!this.isPersistenceAware(this.classDef)) {
            boolean isPersistent = this.isPersistent(featureDef); 
            FieldPersistenceModifier persistenceModifier = isPersistent ? FieldPersistenceModifier.PERSISTENT : FieldPersistenceModifier.NONE;                 
            String objectFieldName = Identifier.ATTRIBUTE_NAME.toIdentifier(featureDef.getName());
            if(featureDef.isComposition()) {
//              if(!this.orm){
//                  this.pw.print("      <field");
//                  this.printAttribute("name", objectFieldName);
//                  this.printAttribute("persistence-modifier", persistenceModifier.toXMLFormat());
//                  if(isPersistent) {
//                      this.printAttribute(
//                          "mapped-by", 
//                          Identifier.ATTRIBUTE_NAME.toIdentifier(featureDef.getExposedEndName())
//                      );
//                  }
//                  this.pw.println(">");
//                  if(isPersistent) {
//                      this.pw.print("        <collection");
//                      this.printAttribute("element-type", getType(featureDef, null, Boolean.FALSE)); 
//                      this.pw.println(">");
//                      this.printExtension("          ", "jpox", "cache", "false");
//                      this.pw.println("        </collection>");
//                   }
//                 this.pw.println("      </field>");
//              }
            } else if (featureDef.isShared()) {
                this.pw.print("      <field");
                this.printAttribute("name", objectFieldName);
                if(!this.orm){
                    this.printAttribute("persistence-modifier", persistenceModifier.toXMLFormat());
                }                    
                FieldMetaData fieldMetaData = getFieldMetaData(featureDef.getQualifiedName());
                if(isPersistent) {
                    boolean mixInChild = isRoot(featureDef.getQualifiedTypeName());
                    JoinMetaData joinMetaData = fieldMetaData == null ? null : fieldMetaData.getJoin();
                    String tableName = joinMetaData == null || !DEFAULT_IDENTIFIER_MAPPING.equals(joinMetaData.getTable()) ? (
                        joinMetaData == null || joinMetaData.getTable() == null ? getTableName(
                            Arrays.asList(
                                featureDef.getQualifiedAssociationName().split(":")
                            )
                        ) : joinMetaData.getTable()
                    ) : null;
                    if(tableName != null && mixInChild) {
                        this.printAttribute("table", tableName); 
                    }
                    this.pw.println(">");
                    mapExtensions(fieldMetaData);
                    if(this.orm){
                        String parentColumnName = toColumnName(featureDef.getExposedEndName());
                        String childColumnName = toColumnName(featureDef.getName());
                        this.pw.println("        <join>");
                        this.pw.print("          <column");
                        this.printAttribute("name", parentColumnName); 
                        this.printAttribute("allows-null", "false"); 
                        this.pw.println("/>");
                        mapExtensions(joinMetaData);
                        this.pw.println("        </join>");
                        this.pw.println("        <element>");
                        this.pw.print("          <column");
                        this.printAttribute("name", childColumnName); 
                        this.printAttribute("allows-null", "false"); 
                        this.pw.println("/>");
                        this.pw.println("        </element>");
                        if(tableName != null && !mixInChild) {
                            this.pw.println("        <!--join-table-name-->");        
                            this.pw.println("          <!-- The join table name is specified as column name attribute -->");        
                            this.pw.print("          <column");        
                            this.printAttribute("name", tableName);
                            this.pw.println("/>");
                            this.pw.println("        <!--/join-table-name-->");        
                        }
                    } else {
                        this.pw.print("        <collection");
                        this.printAttribute(
                            "element-type", 
                            mixInChild ? "java.lang.String" : getType(featureDef, null, null)
                        ); 
                        this.pw.println(">");
                        this.printExtension("          ", "jpox", "cache", "false");
                        this.pw.println("        </collection>");
                        this.pw.println("        <join/>");
                    }
                    this.pw.println("      </field>");
                } else {
                    this.pw.println("/>");
                }
            } else if(
                !featureDef.isDerived() &&
                !(hasContainer() && objectFieldName.equals(this.directCompositeReference.getExposedEndName()))
            ){
                this.pw.print("      <field");
                this.printAttribute("name", objectFieldName);
                if(!this.orm){
                    this.printAttribute("persistence-modifier", persistenceModifier.toXMLFormat());
                }                    
                this.pw.println(">");
                if(isPersistent) {
                    if(this.orm) {
                        FieldMetaData fieldMetaData = getFieldMetaData(featureDef.getQualifiedName());
                        mapExtensions(fieldMetaData);
                        ColumnMetaData columnMetaData = fieldMetaData == null ? null : fieldMetaData.getColumn();
                        if(
                            columnMetaData == null ||
                            !DEFAULT_IDENTIFIER_MAPPING.equals(columnMetaData.getName())
                        ) {
                            this.printColumn(
                                columnMetaData, 
                                toColumnName(objectFieldName),
                                null,
                                // TODO: JPOX SchemaTool sets all columns for required
                                // attributes to allowsNull=false. However, if multiple classes
                                // are mapped to the same table (e.g. with subclass strategy)
                                // this approach does not work. As long as JPOX SchemaTool does
                                // not work properly, allowsNull must be set to true for all columns.
                                Boolean.TRUE, // allowsNull
                                -1
                            );
                        }
                    }
                }
                this.pw.println("      </field>");
            }
        }
	}

    /**
     * 
     * @param featureDef
     * @throws ServiceException
     */
    public void mapSize(
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        if(!this.isPersistenceAware(this.classDef)) {
          boolean isPersistent = this.isPersistent(featureDef); 
          FieldPersistenceModifier persistenceModifier = isPersistent ? FieldPersistenceModifier.PERSISTENT : FieldPersistenceModifier.NONE;                 
          this.pw.print("      <field");
          String fieldName = Identifier.ATTRIBUTE_NAME.toIdentifier(featureDef.getName());
          this.printAttribute(
              "name", fieldName + 
              InstanceMapper.SIZE_SUFFIX
          );
          if(!this.orm) {
              this.printAttribute("persistence-modifier", persistenceModifier.toXMLFormat());              
          }
          this.pw.println(">");
          if(isPersistent) {          
              if(this.orm) {
                  FieldMetaData fieldMetaData = getFieldMetaData(featureDef.getQualifiedName());
                  ColumnMetaData columnMetaData = fieldMetaData == null ? null : fieldMetaData.getColumn();
                  boolean specifyColumnName = 
                      columnMetaData == null ||
                      !DEFAULT_IDENTIFIER_MAPPING.equals(columnMetaData.getName());
                  if(specifyColumnName){
                      String columnName = columnMetaData != null && columnMetaData.getName() != null ?
                          columnMetaData.getName() :
                          toColumnName(featureDef.getName());
                      this.pw.print("        <column");
                      this.printAttribute(
                          "name", 
                          this.plugin.getSizeColumnName(columnName)
                      );
                      this.printAttribute(
                          "default-value", 
                          "-1"
                      );
                  }
                  this.pw.println("/>");
              }
          }
          this.pw.println("      </field>");
        }
    }

    /**
     * 
     * @param featureDef
     * @throws ServiceException
     */
    public void mapEmbedded(
        StructuralFeatureDef featureDef,
        FieldMetaData fieldMetaData
    ) throws ServiceException {
       if(!this.isPersistenceAware(this.classDef)) {
          boolean isPersistent = this.isPersistent(featureDef); 
          FieldPersistenceModifier persistenceModifier = isPersistent ? FieldPersistenceModifier.PERSISTENT : FieldPersistenceModifier.NONE;
          for(
               int i = 0, embedded = fieldMetaData.getEmbedded().intValue();
               i < embedded;
               i++
          ){
              this.pw.print("      <field");
              String fieldName = Identifier.ATTRIBUTE_NAME.toIdentifier(featureDef.getName());
              this.printAttribute(
                  "name", 
                  fieldName + InstanceMapper.SUFFIX_SEPARATOR + i
              );
              if(!this.orm) {
                  this.printAttribute("persistence-modifier", persistenceModifier.toXMLFormat());              
              }
              this.pw.println(">");
              if(isPersistent) {          
                  if(this.orm) {
                      printColumn(
                          fieldMetaData.getColumn(),
                          toColumnName(featureDef.getName()),
                          null, // defaultScale
                          Boolean.TRUE,
                          i
                      );
                  }
              }
              this.pw.println("      </field>");
          }
       }
    }
    
    /**
     * Print a standard column entry
     * 
     * @param columnMetaData
     * @param defaultColumnName
     * @param defaultScale
     * @param allowsNull
     * @param index
     */
    protected void printColumn(
        ColumnMetaData columnMetaData,
        String defaultColumnName,
        String defaultScale,
        Boolean allowsNull, 
        int index
    ) {
        this.pw.print("        <column");        
        if(columnMetaData == null) {
            this.printAttribute(
                "name", 
                index < 0 ? defaultColumnName : this.plugin.getEmbeddedColumnName(defaultColumnName, index)                         
            );
            this.printAttribute(
                "scale", 
                defaultScale                        
            );            
        } else {
            String columnName = columnMetaData.getName() == null ? defaultColumnName : columnMetaData.getName();
            if(!DEFAULT_IDENTIFIER_MAPPING.equals(columnName)) {
                this.printAttribute(
                    "name", 
                    index < 0 ? columnName : this.plugin.getEmbeddedColumnName(columnName, index)                        
                );
            }
            this.printAttribute(
                "length", 
                columnMetaData.getLength()
            );
            this.printAttribute(
                "scale", 
                columnMetaData.getScale() == null ? defaultScale : columnMetaData.getScale()
            );
            this.printAttribute(
                "jdbc-type", 
                columnMetaData.getJdbcType()
            );
        }
        if(allowsNull != null) {
            this.printAttribute(
                "allows-null", 
                allowsNull
            );
        }
        if(hasExtensions(columnMetaData)) {
            this.pw.println(">");
            mapExtensions(columnMetaData);
            this.pw.println("        </column>");
        } else {
            this.pw.println("/>");        
        }
    }

    /**
     * 
     * @param featureDef
     * @throws ServiceException
     */
    public void mapAttribute(
        AttributeDef featureDef
    ) throws ServiceException {
        if(!this.isPersistenceAware(this.classDef)) {
            boolean isPersistent = this.isPersistent(featureDef);
            FieldPersistenceModifier persistenceModifier = isPersistent ? FieldPersistenceModifier.PERSISTENT : FieldPersistenceModifier.NONE;                             
            this.pw.print("      <field");
            this.printAttribute(
                "name", 
                Identifier.ATTRIBUTE_NAME.toIdentifier(featureDef.getName())
            );
            if(!this.orm) {
                this.printAttribute("persistence-modifier", persistenceModifier.toXMLFormat());                
            }
            this.pw.println(">");
            if(isPersistent) {          
                if(this.orm) {
                    FieldMetaData fieldMetaData = getFieldMetaData(featureDef.getQualifiedName());
                    ColumnMetaData columnMetaData = fieldMetaData == null ? null : fieldMetaData.getColumn();
                    if(
                        columnMetaData == null ||
                        !DEFAULT_IDENTIFIER_MAPPING.equals(columnMetaData.getName()) ||
                        columnMetaData.getLength() != null ||
                        columnMetaData.getScale() != null ||
                        columnMetaData.getJdbcType() != null
                    ){
                        this.printColumn(
                            columnMetaData,
                            toColumnName(featureDef.getName()),
                            PrimitiveTypes.DECIMAL.equals(featureDef.getQualifiedTypeName()) ? 
                                this.plugin.getDecimalScale(
                                    this.qualifiedClassName,
                                    featureDef.getName()
                                ) : 
                                null,
                            // TODO: JPOX SchemaTool sets all columns for required
                            // attributes to allowsNull=false. However, if multiple classes
                            // are mapped to the same table (e.g. with subclass strategy)
                            // this approach does not work. As long as JPOX SchemaTool does
                            // not work properly, allowsNull must be set to true for all columns.
                            Boolean.TRUE, -1
                        );
                    }
                }
            }
            this.pw.println("      </field>");
        }
    }

	/**
	 * 
	 * @throws ServiceException
	 */
	public void mapEnd(
        MetaDataMapper... innerClasses
    ) throws ServiceException {
        if(isPersistenceAware(this.classDef)) {
            this.pw.println("/>");
        } else {
            if(!this.orm) {
                if(isSliceHolder()) {
                    this.pw.print("      <field");
                    this.printAttribute("name", InstanceMapper.SLICES_MEMBER);
                    this.printAttribute("persistence-modifier", "persistent");
                    this.printAttribute("mapped-by", InstanceMapper.JDO_IDENTITY_MEMBER);
                    this.pw.println(">");
                    this.pw.print("        <map");
                    this.printAttribute("key-type", "java.lang.Integer");
                    this.printAttribute(
                        "value-type", 
                        this.className + '$' + InstanceMapper.SLICE_CLASS_NAME
                    );
                    this.pw.println("/>");
                    this.pw.print("        <key");
                    this.printAttribute("mapped-by", InstanceMapper.INDEX_MEMBER);
                    this.pw.println("/>");
                    this.pw.println("      </field>");
                }
                if(
                    this.isBaseClass() &&
                    innerClass == null && 
                    this.compositeReference != null &&
                    this.classMetaData.isRequiresExtent()
                ){
                    String queryName = this.compositeReference.getAssociationName();
                    String referenceName = this.compositeReference.getExposedEndName();
                    this.pw.println("      <query name=\"" + queryName + "\" language=\"" + Query.JDOQL + "\"><![CDATA[");
                    this.pw.println("        SELECT FROM " + this.packageName + '.' + this.className + " WHERE " + referenceName + " == :parent");
                    this.pw.println("      ]]></query>");
                }
            }
            this.pw.println("    </class>");
            for(MetaDataMapper innerClass : innerClasses) {
                embed(innerClass);
            }
        }
		this.pw.println("  </package>");
		this.pw.println(
            "</" + (
                this.orm ? "orm" : "jdo"
            ) + ">"
        );
	}

    /**
     * 
     *
     */
    private void mapInnerClassEnd(){
        this.pw.println("    </class>");
    }
    
    /**
     * 
     * @param metaData
     */
    private void mapExtensions(
        ExtendableMetaData metaData
    ){
        Map<String,Map<String,String>> extensions = metaData == null ? null : metaData.getExtension(
            this.orm ? ExtensionTarget.ORM : ExtensionTarget.JDO
        );
        if(extensions != null) {
            for(Map.Entry<String,Map<String,String>> vendor : extensions.entrySet()) {
                for(Map.Entry<String,String> extension : vendor.getValue().entrySet()) {
                    this.pw.print("        <extension");
                    this.printAttribute("vendor-name", vendor.getKey());
                    this.printAttribute("key", extension.getKey());
                    this.printAttribute("value", extension.getValue());
                    this.pw.println("/>");
                }
            }
        }
    }

    /**
     * Test whether an element has extenions
     * 
     * @param metaData
     * 
     * @return <code>true</code> if the given element has extension
     */
    private boolean hasExtensions(
        ExtendableMetaData metaData
    ){
        return 
            metaData != null && 
            metaData.getExtension(
                this.orm ? ExtensionTarget.ORM : ExtensionTarget.JDO
            ) != null;
    }
    
    
	/**
	 * 
	 * @throws ServiceException
	 */
	public void mapBegin() throws ServiceException {
        if(this.innerClass == null) {
    		this.fileHeader();
    		this.pw.println(
                "<" + (
                    this.orm ? "orm" : "jdo" 
                ) + ">"
            );
    		this.pw.print("  <package");
    		printAttribute(
                "name", 
                this.packageName
            );
    		this.pw.println(">");
            mapExtensions (
                this.metaData == null ? null : ((MetaData_2_0)this.metaData).getPackage(
                    this.packageName
                )
            );
        }
		this.pw.print("    <class");
		String className = this.innerClass == null ? this.className : this.className + '$' + innerClass;
		this.printAttribute("name", className);
        if(isPersistenceAware(this.classDef)) {
            if(!this.orm) {
                this.printAttribute("persistence-modifier", "persistence-aware");
            }
        } else {
            if(!this.orm) {
                this.printAttribute("persistence-modifier", "persistence-capable");
            }
            InheritanceStrategy inheritanceStrategy = getInheritanceStrategy();
            ClassDef immediateSuperClassDef = this.classDef.getSuperClassDef(true);
            boolean isBaseClass = this.innerClass == null && this.isBaseClass();
            boolean innerIsBaseClass =
                (this.innerClass != null) &&
                (this.isBaseClass() || (immediateSuperClassDef.getClassMetaData() != null && !((ClassMetaData)immediateSuperClassDef.getClassMetaData()).isRequiresSlices()));                                
            if (isBaseClass || innerIsBaseClass) {
                if(!this.orm) {
                    if(!isExtentCapable()) {
                        this.printAttribute("requires-extent", "false");
                    }
                    this.printAttribute("detachable", "true");
        			this.printAttribute("identity-type", "application");
                    this.printAttribute(
                        "objectid-class", 
                        this.innerClass == null ? 
                            StringIdentity.class.getName() :
                            className + '$' + InstanceMapper.SLICE_ID_CLASS_NAME
                    );
                }
            } 
            if (
                 this.orm && (
                     InheritanceStrategy.NEW_TABLE == inheritanceStrategy || (
                         isBaseClass() && 
                         InheritanceStrategy.SUBCLASS_TABLE != inheritanceStrategy 
                     ) 
                 )
            ){
                this.printAttribute("table",  ormTableName());
            }
			this.pw.println(">");
            mapExtensions(this.classMetaData);
            //
            // Inheritance
            //
            this.pw.print("      <inheritance");
            if(inheritanceStrategy != null) {
                this.printAttribute("strategy", inheritanceStrategy.toXMLFormat());
            }
    		this.pw.println(">");
            if(this.orm) {
                this.pw.print("        <discriminator");
                this.printAttribute("strategy", "value-map");
                this.printAttribute("column", this.plugin.getDiscriminatorColumnName(this.qualifiedClassName));
                this.printAttribute("value", getDiscriminatorValue());
                this.pw.println("/>");
            }
            this.pw.println("      </inheritance>");
            if(
                this.orm &&
                innerClass == null &&
                //
                // TODO use enhanced openmdxjdo meta data information instead 
                //      of knowledge about org::openmdx::base::BasicObject 
                //
                isInstanceOf("org:openmdx:base:BasicObject")
            ) {
                //
                // Version
                //
                this.pw.print("      <version");
                this.printAttribute("strategy", "date-time");
                this.pw.println(">");
                this.pw.print("        <column");
                this.printAttribute("name", toColumnName(SystemAttributes.MODIFIED_AT)); 
                this.pw.println("/>");
                this.pw.println("      </version>");
            }
            //
            // Primary key innerClass==null
            //
            FieldMetaData fieldMetaData = this.classMetaData.getFieldMetaData(
                InstanceMapper.JDO_IDENTITY_MEMBER
            );
            ColumnMetaData columnMetaData = fieldMetaData == null 
                ? null 
                : fieldMetaData.getColumn();                        
			if(isBaseClass) {
                //
                // String identity
                //
                this.pw.print("      <field");
                this.printAttribute("name", InstanceMapper.JDO_IDENTITY_MEMBER);
                if(this.orm) {
                    this.pw.println(">");
                    this.printColumn(
                        columnMetaData, 
                        toColumnName(InstanceMapper.JDO_IDENTITY_MEMBER),
                        null,
                        null, 
                        -1
                    );
                    this.pw.println("      </field>");
                } else {
                    this.printAttribute("primary-key", "true");
                    this.pw.println("/>");
                }
                if (hasContainer()) {
                    //
                    // Containment
                    //
                    this.pw.print("      <field");
                    this.printAttribute(
                        "name", 
                        Identifier.ATTRIBUTE_NAME.toIdentifier(this.directCompositeReference.getExposedEndName())
                    );
                    if(this.orm) {
                        this.pw.println(">");
                        FieldMetaData fieldMetaDataContainer = this.classMetaData.getFieldMetaData(
                            this.directCompositeReference.getExposedEndName()
                        );
                        this.printColumn(
                            fieldMetaDataContainer == null 
                                ? null 
                                : fieldMetaDataContainer.getColumn(), 
                            toColumnName(this.directCompositeReference.getExposedEndName()), 
                            null, 
                            null, 
                            -1
                        );
                        this.pw.println("      </field>");
                    } else {
                        this.printAttribute("persistence-modifier", "persistent");
                        this.pw.println("/>");
                    }
                }
            }
            //
            // Primary key innerClass!=null
            //
            if(innerIsBaseClass) {
				this.pw.print("      <field");
				this.printAttribute("name", InstanceMapper.JDO_IDENTITY_MEMBER);
                if(this.orm) {
                    this.pw.println(">");
                    this.printColumn(
                        columnMetaData, 
                        toColumnName(InstanceMapper.JDO_IDENTITY_MEMBER),
                        null,
                        null, 
                        -1
                    );
                    this.pw.println("      </field>");
                } else {
    				this.printAttribute("primary-key", "true");
                    this.pw.println("/>");
                }
				this.pw.print("      <field");
				this.printAttribute("name", InstanceMapper.INDEX_MEMBER);
                if(this.orm) {
                    this.pw.println(">");
                    this.pw.print("        <column");
                    this.printAttribute("name", toColumnName(InstanceMapper.INDEX_MEMBER));
                    this.pw.println("/>");
                    this.pw.println("      </field>");
                } else {
                    this.printAttribute("primary-key", "true");
                    this.pw.println("/>");
                }
			}
		}
	}

	/**
	 * Print the XML file header
	 */
	public void fileHeader() {
		this.pw.print("<?xml");
		printAttribute("version", "1.0");
		printAttribute("encoding", "UTF-8");
		this.pw.println("?>");
		this.pw.print(
            "<!DOCTYPE " +
            (this.orm ? "orm" : "jdo") +
            " PUBLIC "
        );
		this.pw.print(
            '"' + "-//Sun Microsystems, Inc.//" + (this.orm ? 
                "DTD Java Data Objects Mapping Metadata 2.0" :
                "DTD Java Data Objects Metadata 2.0" 
                    
            ) + "//EN" + '"'
        );
		this.pw.println(
            " " + '"' + (this.orm ? 
                "http://java.sun.com/dtd/orm_2_0.dtd" :
                "http://java.sun.com/dtd/jdo_2_0.dtd"
            ) + '"' + '>'
        );
		this.pw.println();
		this.pw.println("<!--");
		this.pw.println(" ! Name: $Id: MetaDataMapper.java,v 1.50 2008/02/13 11:05:51 hburger Exp $");
		this.pw.println(" ! Generated by: openMDX Meta Data Mapper");
		this.pw.println(" ! Date: " + new Date());
		this.pw.println(" !");
		this.pw.println(" ! GENERATED - DO NOT CHANGE MANUALLY");
		this.pw.println(" !-->");
		this.pw.println();
	}
    
    private void embed(
        MetaDataMapper that
    ){
        that.mapInnerClassEnd();        
        that.pw.flush();
        if(that.process) {
            this.pw.write(((CharArrayWriter) that.writer).toCharArray());
        }
        
    }

	/**
	 * Print an XML attribute
	 * 
	 * @param name
	 * @param value, suppress printing if <code>null</code>
	 */
	protected void printAttribute(
        String name, 
        Object value
    ) {
        if(value != null) {
    		this.pw.print(' ');
    		this.pw.print(name);
    		this.pw.print('=');
    		this.pw.print('"');
    		this.pw.print(value);
    		this.pw.print('"');
        }
	}

    /**
     * Print an extension
     * 
     * @param indentation
     * @param vendor
     * @param name
     * @param value
     */
    protected void printExtension(
        String indentation, 
        String vendor, 
        String name, 
        String value
    ) {
        this.pw.print(indentation);
        this.pw.print("<extension vendor-name=");
        this.pw.print('"');
        this.pw.print(vendor);
        this.pw.print('"');
        this.pw.print(" key=");
        this.pw.print('"');
        this.pw.print(name);
        this.pw.print('"');
        this.pw.print(" value=");
        this.pw.print('"');
        this.pw.print(value);
        this.pw.print('"');
        this.pw.println("/>");
    }

    /**
	 * Convert a model name to a database name
	 * 
	 * @param fieldName
	 * @param size 
     * 
	 * @return
	 */
	protected String toColumnName(
        String fieldName
    ) {        
	    return fieldName.equals(InstanceMapper.INDEX_MEMBER) ?
            this.plugin.getIndexColumnName(this.qualifiedClassName) :
        fieldName.equals(InstanceMapper.JDO_IDENTITY_MEMBER) ?
            this.plugin.getIdentityColumnName(this.qualifiedClassName) :
            this.plugin.getFieldColumnName(this.qualifiedClassName, fieldName);
	}

    private String getPackagePrefix(
        List<String> qualifiedClassName
    ){
        if(this.metaData == null) {
            return null;
        } else {
            int last = qualifiedClassName.size() - 1;
            StringBuilder packageName = new StringBuilder();
            for(String component : qualifiedClassName.subList(0, last)) {
                packageName.append(
                    component
                ).append(
                    '.'
                );
            }
            packageName.append(Names.JDO2_PACKAGE_SUFFIX);
            PackageMetaData metaData = ((MetaData_2_0)this.metaData).getPackage(
                packageName.toString()
            ); 
            return metaData == null ? null : metaData.getTablePrefix();
        }
    }
    
    /**
     * Convert a model name to a database name
     * @param qualifiedClassName 
     * @param modelName
     * 
     * @return the table name
     */
    private String getTableName(
        List<String> qualifiedClassName
    ) { 
        String tableName = this.plugin.getTableName(getPackagePrefix(qualifiedClassName), qualifiedClassName);
        return this.innerClass == null ?
            tableName :
            this.plugin.getSliceTableName(tableName);
    }
    
    private InheritanceStrategy getInheritanceStrategy(
    ){
        InheritanceStrategy thisStrategy = getInheritanceStrategy(this.classDef);
        if(thisStrategy == null && !isBaseClass()) {
            InheritanceStrategy superStrategy = getInheritanceStrategy(this.extendsClassDef);
            if(superStrategy == InheritanceStrategy.SUBCLASS_TABLE) {
                thisStrategy = InheritanceStrategy.NEW_TABLE;
            }
        }
        return thisStrategy;
    }

    protected static InheritanceStrategy getInheritanceStrategy(
        ClassDef classDef
    ){
        ClassMetaData classMetaData = (ClassMetaData) classDef.getClassMetaData();
        if(classMetaData != null) {
            InheritanceMetaData inheritanceMetaData = classMetaData.getInheritance();
            if(inheritanceMetaData != null){
                return inheritanceMetaData.getStrategy();
            }
        }
        return null;
    }
    
    public void setProcess(
        boolean process
    ){
        this.process = process;
    }
    
    /**
     * 
     */
    private boolean process;
    
    /**
     * 
     */
    private final boolean orm;

    /**
     * The mapper's writer
     */
	private final Writer writer;
    
	/**
	 * 
	 */
	private final String innerClass;

    /**
     * 
     */
    private final String packageName;
    
    /**
     * 
     */
    static final String DEFAULT_IDENTIFIER_MAPPING = "?";
    
    /**
     * TODO make it configurable
     */
    protected final ObjectRepositoryMetadataPlugin plugin;

}
