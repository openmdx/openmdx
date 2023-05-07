/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: JPA3 Meta Data Mapper 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.application.mof.mapping.java;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.omg.mof.spi.Identifier;
import org.omg.mof.spi.Names;
import org.openmdx.application.mof.mapping.cci.AttributeDef;
import org.openmdx.application.mof.mapping.cci.ClassDef;
import org.openmdx.application.mof.mapping.cci.MetaData_1_0;
import org.openmdx.application.mof.mapping.cci.ReferenceDef;
import org.openmdx.application.mof.mapping.cci.StructuralFeatureDef;
import org.openmdx.application.mof.mapping.java.metadata.ClassMetaData;
import org.openmdx.application.mof.mapping.java.metadata.ColumnMetaData;
import org.openmdx.application.mof.mapping.java.metadata.FieldMetaData;
import org.openmdx.application.mof.mapping.java.metadata.FieldPersistenceModifier;
import org.openmdx.application.mof.mapping.java.metadata.InheritanceMetaData;
import org.openmdx.application.mof.mapping.java.metadata.InheritanceStrategy;
import org.openmdx.application.mof.mapping.java.metadata.JoinMetaData;
import org.openmdx.application.mof.mapping.java.metadata.MetaData_2_0;
import org.openmdx.application.mof.mapping.java.metadata.PackageMetaData;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.w3c.format.DateTimeFormat;

/**
 * JPA3 Meta Data Mapper
 */
public class StandardMetaDataMapper extends AbstractMetaDataMapper {

    public StandardMetaDataMapper(
        ModelElement_1_0 classDef, 
        Writer writer,
		Model_1_0 model, 
        Format format, 
        String packageSuffix,
		String sliceClassName, 
        MetaData_1_0 metaData, 
        boolean markdown, 
        PrimitiveTypeMapper primitiveTypeMapper, 
        ObjectRepositoryMetadataPlugin plugin
    ) throws ServiceException {
        super(
            classDef,
            writer, 
            model,
            format, 
            packageSuffix, 
            sliceClassName,
            markdown,
            metaData, 
            primitiveTypeMapper, 
            plugin
        );
	}

    //-----------------------------------------------------------------------
    protected String ormTableName(
    ) throws ServiceException {
        return this.classMetaData == null || this.classMetaData.getTable() == null ? 
            this.getTableName(this.qualifiedClassName) : 
                DEFAULT_IDENTIFIER_MAPPING.equals(this.classMetaData.getTable()) ? 
                    null : 
                    (this.sliceClassName == null ? this.classMetaData.getTable() : this.plugin.getSliceTableName(this.classMetaData.getTable()));
    }

    //-----------------------------------------------------------------------
    protected boolean isPersistent(
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        FieldMetaData fieldMetaData = getFieldMetaData(featureDef.getQualifiedName());
        return fieldMetaData == null || fieldMetaData.getPersistenceModifier() == null ? (
            !featureDef.isDerived() &&
            !this.isPersistenceAware(this.getClassDef(featureDef.getQualifiedTypeName()))
         ) : fieldMetaData.getPersistenceModifier() == FieldPersistenceModifier.PERSISTENT;
    }

    //-----------------------------------------------------------------------
    protected boolean isVersion(
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        FieldMetaData fieldMetaData = getFieldMetaData(featureDef.getQualifiedName());
        return fieldMetaData != null && fieldMetaData.getPersistenceModifier() == FieldPersistenceModifier.VERSION;
    }
    
    //-----------------------------------------------------------------------
    protected String getDiscriminatorValue(
    ) {
        return this.classDef.getQualifiedName();
    }

    //-----------------------------------------------------------------------
    @Override
    public void mapReference(
        ReferenceDef featureDef
    ) throws ServiceException {
        boolean isPersistent = this.isPersistent(featureDef); 
        String objectFieldName = Identifier.ATTRIBUTE_NAME.toIdentifier(featureDef.getName());
        // Composite association
        if(featureDef.isComposition()) {
            // No member for composites. Composites are retrieved by query
        } 
        // Shared association
        else if(featureDef.isShared()) {
            if(isPersistent) {
                this.pwOneToManyRelationships.print("      <one-to-many");
                printAttribute(this.pwOneToManyRelationships, "name", objectFieldName);
                this.pwOneToManyRelationships.println(">");
                FieldMetaData fieldMetaData = getFieldMetaData(featureDef.getQualifiedName());
                JoinMetaData joinMetaData = fieldMetaData == null ? null : fieldMetaData.getJoin();
                String tableName = joinMetaData == null || !DEFAULT_IDENTIFIER_MAPPING.equals(joinMetaData.getTable()) ? (
                    joinMetaData == null || joinMetaData.getTable() == null ? getTableName(
                        Arrays.asList(
                            featureDef.getQualifiedAssociationName().split(":")
                        )
                    ) : joinMetaData.getTable()
                ) : null;
                if(tableName != null) {
                    this.pwOneToManyRelationships.print("        <join-table");
                    printAttribute(this.pwOneToManyRelationships, "name", tableName);
                    this.pwOneToManyRelationships.println(">");
                    String parentColumnName = this.toColumnName(featureDef.getExposedEndName());
                    String childColumnName = this.toColumnName(featureDef.getName());
                    this.pwOneToManyRelationships.print("          <join-column");
                    printAttribute(this.pwOneToManyRelationships, "name", parentColumnName); 
                    this.pwOneToManyRelationships.println("/>");
                    this.pwOneToManyRelationships.print("          <inverse-join-column");
                    printAttribute(this.pwOneToManyRelationships, "name", childColumnName); 
                    this.pwOneToManyRelationships.println("/>");
                    this.pwOneToManyRelationships.println("        </join-table>");
                }
                this.pwOneToManyRelationships.println("      </one-to-many>");
            } 
            else {
                this.pwTransientAttributes.print("      <transient");
                printAttribute(
                    this.pwTransientAttributes,
                    "name", 
                    objectFieldName
                );                
                this.pwTransientAttributes.println("/>");
            }
        }
        // Reference
        else if(
            !(hasContainer() && 
            objectFieldName.equals(this.directCompositeReference.getExposedEndName()))
        ) {
            if(isPersistent) {
                FieldMetaData fieldMetaData = getFieldMetaData(featureDef.getQualifiedName());
                ColumnMetaData columnMetaData = fieldMetaData == null ? null : fieldMetaData.getColumn();
                ClassDef featureClassDef = this.getClassDef(featureDef.getQualifiedTypeName()); 
                if(this.getClassType(featureClassDef) == ClassType.MIXIN) {
                    this.pwBasicAttributes.print("      <basic");
                    printAttribute(this.pwBasicAttributes, "name", objectFieldName);
                    printAttribute(this.pwBasicAttributes, "optional", "true");
                    this.pwBasicAttributes.println(">");
                    if(
                        (columnMetaData == null) ||
                        !DEFAULT_IDENTIFIER_MAPPING.equals(columnMetaData.getName())
                    ) {
                        this.printColumn(
                            this.pwBasicAttributes,
                            "column",
                            columnMetaData, 
                            toColumnName(objectFieldName),
                            null,
                            Boolean.TRUE, // allowsNull
                            -1
                        );
                    }
                    this.pwBasicAttributes.println("      </basic>");                       
                }
                else {
                    this.pwBasicAttributes.print("      <basic");
                    printAttribute(this.pwBasicAttributes, "name", objectFieldName);
                    printAttribute(this.pwBasicAttributes, "optional", "true");
                    this.pwBasicAttributes.println(">");
                    if(
                        (columnMetaData == null) ||
                        !DEFAULT_IDENTIFIER_MAPPING.equals(columnMetaData.getName())
                    ) {
                        this.printColumn(
                            this.pwBasicAttributes,
                            "column",
                            columnMetaData, 
                            toColumnName(objectFieldName),
                            null,
                            Boolean.TRUE, // allowsNull
                            -1
                        );
                    }
                    this.pwBasicAttributes.println("      </basic>");
                }
            }
            else {
                this.pwTransientAttributes.print("      <transient");
                printAttribute(
                    this.pwTransientAttributes,
                    "name", 
                    objectFieldName
                );                
                this.pwTransientAttributes.println("/>");
            }
        }
    }

    //-----------------------------------------------------------------------
    @Override
	public void mapSize(
	    StructuralFeatureDef featureDef
	) throws ServiceException {
        boolean isPersistent = this.isPersistent(featureDef);
        String fieldName = Identifier.ATTRIBUTE_NAME.toIdentifier(featureDef.getName());
        if(isPersistent) {
            print("      <basic");
            printAttribute(
                this.pw,
                "name", 
                fieldName + InstanceMapper.SIZE_SUFFIX
            );
            printLine(">");              
            FieldMetaData fieldMetaData = getFieldMetaData(featureDef.getQualifiedName());
            ColumnMetaData columnMetaData = fieldMetaData == null ? null : fieldMetaData.getColumn();
            boolean specifyColumnName = 
                columnMetaData == null ||
                !DEFAULT_IDENTIFIER_MAPPING.equals(columnMetaData.getName());
            if(specifyColumnName) {
                String columnName = columnMetaData != null && columnMetaData.getName() != null ?
                    columnMetaData.getName() :
                    toColumnName(featureDef.getName());
                print("        <column");
                printAttribute(
                    this.pw,
                    "name", 
                    this.plugin.getSizeColumnName(columnName)
                );
                printAttribute(
                    this.pw,
                    "column-definition", 
                    "INTEGER DEFAULT -1"
                );
            }
            printLine("/>");
            printLine("      </basic>");
        }
        else {
            this.pwTransientAttributes.print("      <transient");
            printAttribute(
                this.pwTransientAttributes,
                "name", 
                fieldName + InstanceMapper.SIZE_SUFFIX
            );                
            this.pwTransientAttributes.println("/>");
        }
	}

    //-----------------------------------------------------------------------
    @Override
    public void mapEmbedded(
        StructuralFeatureDef featureDef,
        FieldMetaData fieldMetaData
    ) throws ServiceException {
       if(!this.isPersistenceAware(this.classDef)) {
          boolean isPersistent = this.isPersistent(featureDef); 
          for(
               int i = 0, embedded = fieldMetaData.getEmbedded().intValue();
               i < embedded;
               i++
          ) {
              if(isPersistent) {
                  this.pwBasicAttributes.print("      <basic");
                  String fieldName = Identifier.ATTRIBUTE_NAME.toIdentifier(featureDef.getName());
                  printAttribute(
                      this.pwBasicAttributes,
                      "name", 
                      fieldName + InstanceMapper.SUFFIX_SEPARATOR + i
                  );
                  this.pwBasicAttributes.println(">");
                  if(isPersistent) {          
                      this.printColumn(
                          this.pwBasicAttributes,
                          fieldMetaData.getColumn(),
                          toColumnName(featureDef.getName()),
                          null, // defaultScale
                          Boolean.TRUE,
                          i
                      );
                  }
                  this.pwBasicAttributes.println("      </basic>");
              }
              else {
                  String fieldName = Identifier.ATTRIBUTE_NAME.toIdentifier(featureDef.getName());
                  this.pwTransientAttributes.print("      <transient");
                  printAttribute(
                      this.pwTransientAttributes,
                      "name", 
                      fieldName + InstanceMapper.SUFFIX_SEPARATOR + i
                  );                
                  this.pwTransientAttributes.println("/>");
              }
          }
       }
    }
    
    //-----------------------------------------------------------------------
    protected void printColumn(
        PrintWriter pw,
        ColumnMetaData columnMetaData,
        String defaultColumnName,
        String defaultScale,
        Boolean allowsNull, 
        int index
    ) {
        this.printColumn(
            pw, 
            "column", 
            columnMetaData, 
            defaultColumnName, 
            defaultScale, 
            allowsNull, 
            index
        );
    }
    
    //-----------------------------------------------------------------------
    protected void printColumn(
        PrintWriter pw,
        String columnTag,
        ColumnMetaData columnMetaData,
        String defaultColumnName,
        String defaultScale,
        Boolean allowsNull, 
        int index
    ) {
        pw.print("        <" + columnTag);        
        if(columnMetaData == null) {
            printAttribute(
                pw,
                "name", 
                index < 0 ? defaultColumnName : this.plugin.getEmbeddedColumnName(defaultColumnName, index)                         
            );
            printAttribute(
                pw,
                "scale", 
                defaultScale                        
            );            
        } 
        else {
            String columnName = columnMetaData.getName() == null ? defaultColumnName : columnMetaData.getName();
            if(!DEFAULT_IDENTIFIER_MAPPING.equals(columnName)) {
                printAttribute(
                    pw,
                    "name", 
                    index < 0 ? columnName : this.plugin.getEmbeddedColumnName(columnName, index)                        
                );
            }
            printAttribute(
                pw,
                "length", 
                columnMetaData.getLength()
            );
            printAttribute(
                pw,
                "scale", 
                columnMetaData.getScale() == null ? defaultScale : columnMetaData.getScale()
            );
            printAttribute(
                pw,
                "column-definition", 
                columnMetaData.getJdbcType()
            );
        }
        if(allowsNull != null) {
            printAttribute(
                pw,
                "nullable", 
                allowsNull
            );
        }
        pw.println("/>");        
    }

    //-----------------------------------------------------------------------
    @Override
    public void mapAttribute(
        AttributeDef featureDef
    ) throws ServiceException {
        if(this.isPersistent(featureDef)) {
            this.pwBasicAttributes.print("      <basic");
            printAttribute(
                this.pwBasicAttributes,
                "name", 
                Identifier.ATTRIBUTE_NAME.toIdentifier(featureDef.getName())
            );                
            this.pwBasicAttributes.println(">");
            FieldMetaData fieldMetaData = getFieldMetaData(featureDef.getQualifiedName());
            ColumnMetaData columnMetaData = fieldMetaData == null ? null : fieldMetaData.getColumn();
            if(
                columnMetaData == null ||
                !DEFAULT_IDENTIFIER_MAPPING.equals(columnMetaData.getName()) ||
                columnMetaData.getLength() != null ||
                columnMetaData.getScale() != null ||
                columnMetaData.getJdbcType() != null
            ) {
                this.printColumn(
                    this.pwBasicAttributes,
                    columnMetaData,
                    toColumnName(featureDef.getName()),
                    PrimitiveTypes.DECIMAL.equals(featureDef.getQualifiedTypeName()) ? 
                        this.plugin.getDecimalScale(
                            this.qualifiedClassName,
                            featureDef.getName()
                        ) : 
                        null,
                    Boolean.TRUE, 
                    -1
                );
            }
            this.pwBasicAttributes.println("      </basic>");
        } else if (!isVersion(featureDef)){
            this.pwTransientAttributes.print("      <transient");
            printAttribute(
                this.pwTransientAttributes,
                "name", 
                Identifier.ATTRIBUTE_NAME.toIdentifier(featureDef.getName())
            );                
            this.pwTransientAttributes.println("/>");
        }
    }

    //-----------------------------------------------------------------------
    @Override
	public void mapEnd(
        AbstractMetaDataMapper sliceClass
    ) throws ServiceException {
        try {
    	    this.pwBasicAttributes.close();
            this.pwOneToManyRelationships.close();
            this.pwManyToOneRelationships.close();
    	    this.pwOneToOneRelationships.close();
    	    this.pwVersionAttributes.close();
    	    this.pwTransientAttributes.close();
    	    print(this.streamBasicAttributes.toString("UTF-8"));
            print(this.streamVersionAttributes.toString("UTF-8"));
            print(this.streamManyToOneRelationships.toString("UTF-8"));
            print(this.streamOneToManyRelationships.toString("UTF-8"));
            print(this.streamOneToOneRelationships.toString("UTF-8"));
            print(this.streamTransientAttributes.toString("UTF-8"));
            printLine("    </attributes>");
            printLine("  </entity>");
            this.embed((StandardMetaDataMapper) sliceClass);
        }
        catch(Exception e) {
            throw new ServiceException(e);
        }
	}

    //-----------------------------------------------------------------------
    private void mapSliceClassEnd(
    ) throws ServiceException {
        try {
            this.pwBasicAttributes.close();
            this.pwManyToOneRelationships.close();
            this.pwOneToManyRelationships.close();
            this.pwOneToOneRelationships.close();
            this.pwVersionAttributes.close();
            this.pwTransientAttributes.close();
            print(this.streamBasicAttributes.toString("UTF-8"));
            print(this.streamVersionAttributes.toString("UTF-8"));
            print(this.streamManyToOneRelationships.toString("UTF-8"));
            print(this.streamOneToManyRelationships.toString("UTF-8"));
            print(this.streamOneToOneRelationships.toString("UTF-8"));
            print(this.streamTransientAttributes.toString("UTF-8"));
            printLine("    </attributes>");
            printLine("  </entity>");
        }
        catch(Exception e) {
            throw new ServiceException(e);
        }
    }
    
    //-----------------------------------------------------------------------
    @Override
	public void mapBegin(
	    boolean isSliceHolder
	) throws ServiceException {
		String className = this.sliceClassName == null ? 
		    this.className : 
		    this.className + sliceClassName;
        print("  <entity");
		printAttribute(this.pw, "class", this.packageName + "." + className);
        printLine(">");
        // table
        InheritanceStrategy inheritanceStrategy = this.getInheritanceStrategy();
        if (
            (this.classMetaData.getTable() != null) ||                
            (InheritanceStrategy.JOINED == inheritanceStrategy) || (
                isBaseClass() && 
                InheritanceStrategy.TABLE_PER_CLASS != inheritanceStrategy 
            )
        ) {
            print("    <table");
            printAttribute(this.pw, "name", this.ormTableName());
            printLine("/>");
        }
        // id-class
        ClassDef immediateSuperClassDef = this.classDef.getSuperClassDef(true);
        boolean isBaseClass = 
            (this.isBaseClass() || (this.classMetaData.getBaseClass() != null)) &&
            (this.sliceClassName == null);
        boolean sliceIsBaseClass =
            (this.isBaseClass() || (this.classMetaData.getBaseClass() != null) || (immediateSuperClassDef.getClassMetaData() != null && !((ClassMetaData)immediateSuperClassDef.getClassMetaData()).isRequiresSlices())) &&                                
            (this.sliceClassName != null);
        if(
            (isBaseClass || sliceIsBaseClass) &&
            (this.classMetaData.getBaseClass() == null)
        ) {
            if(this.sliceClassName != null) {
                print("    <id-class");            
                printAttribute(
                    this.pw,
                    "class", 
                    this.packageName + "." + className + "$" + InstanceMapper.SLICE_ID_CLASS_NAME
                );
                printLine("/>");
            }
        }
        // inheritance
		if(inheritanceStrategy != null) {
            print("    <inheritance");
            printAttribute(this.pw, "strategy", inheritanceStrategy.toXMLFormat());
            printLine("/>");
		}
        // discriminator-value
        print("    <discriminator-value>");
        print(this.getDiscriminatorValue());
        printLine("</discriminator-value>");
        // discriminator-column
        print("    <discriminator-column");
        printAttribute(this.pw, "name", this.plugin.getDiscriminatorColumnName(this.qualifiedClassName));
        printAttribute(this.pw, "discriminator-type", "STRING");
        printLine("/>");
        // attributes
        streamBasicAttributes.reset();
        this.pwBasicAttributes = new PrintWriter(this.streamBasicAttributes);
        streamTransientAttributes.reset();
        this.pwTransientAttributes = new PrintWriter(this.streamTransientAttributes);
        streamOneToManyRelationships.reset();
        this.pwOneToManyRelationships = new PrintWriter(this.streamOneToManyRelationships);
        streamManyToOneRelationships.reset();
        this.pwManyToOneRelationships = new PrintWriter(this.streamManyToOneRelationships);
        streamOneToOneRelationships.reset();
        this.pwOneToOneRelationships = new PrintWriter(this.streamOneToOneRelationships);
        streamVersionAttributes.reset();
        this.pwVersionAttributes = new PrintWriter(this.streamVersionAttributes);        
        printLine("    <attributes>");
        // version
        if(
            isBaseClass &&
            (this.classMetaData.getBaseClass() == null) &&
            this.isInstanceOf("org:openmdx:base:BasicObject")
        ) {
            this.pwVersionAttributes.print("      <version");
            printAttribute(pwVersionAttributes, "name", SystemAttributes.MODIFIED_AT);
            this.pwVersionAttributes.println(">");
            this.pwVersionAttributes.print("        <column");
            printAttribute(pwVersionAttributes, "name", toColumnName(SystemAttributes.MODIFIED_AT)); 
            this.pwVersionAttributes.println("/>");
            this.pwVersionAttributes.println("      </version>");
        }
        // id attributes
        FieldMetaData identityFieldMetaData = this.classMetaData.getFieldMetaData(
            InstanceMapper.JDO_IDENTITY_MEMBER
        );
        ColumnMetaData identityColumnMetaData = identityFieldMetaData == null ? 
            null : 
            identityFieldMetaData.getColumn();                        
		if(isBaseClass) {
            // id
            if(this.classMetaData.getBaseClass() == null) {		    
                print("      <id");
                printAttribute(this.pw, "name", InstanceMapper.JDO_IDENTITY_MEMBER);
                printLine(">");
                this.printColumn(
                    this.pw,
                    identityColumnMetaData, 
                    this.toColumnName(InstanceMapper.JDO_IDENTITY_MEMBER),
                    null,
                    null, 
                    -1
                );
                printLine("      </id>");
            }
            // parent
            if(this.hasContainer()) {
                this.pwBasicAttributes.print("      <basic");
                printAttribute(
                    this.pwBasicAttributes,
                    "name", 
                    Identifier.ATTRIBUTE_NAME.toIdentifier(this.directCompositeReference.getExposedEndName())
                );
                printAttribute(
                    this.pwBasicAttributes, 
                    "optional", 
                    "false"
                );
                this.pwBasicAttributes.println(">");
                FieldMetaData fieldMetaDataContainer = this.classMetaData.getFieldMetaData(
                    this.directCompositeReference.getExposedEndName()
                );
                this.printColumn(
                    this.pwBasicAttributes,
                    "column",
                    fieldMetaDataContainer == null ? 
                        null : 
                        fieldMetaDataContainer.getColumn(), 
                    this.toColumnName(this.directCompositeReference.getExposedEndName()), 
                    null, 
                    null, 
                    -1
                );
                this.pwBasicAttributes.println("      </basic>");                    
            }
        }
        // openmdxjdoSlices
        if(isSliceHolder && !sliceIsBaseClass) {
            this.pwOneToManyRelationships.print("      <one-to-many");
            printAttribute(this.pwOneToManyRelationships, "name", InstanceMapper.SLICES_MEMBER);
            printAttribute(this.pwOneToManyRelationships, "mapped-by", InstanceMapper.JDO_IDENTITY_MEMBER);
            this.pwOneToManyRelationships.println(">");
            this.pwOneToManyRelationships.print("        <map-key");
            printAttribute(this.pwOneToManyRelationships, "name", InstanceMapper.INDEX_MEMBER);
            this.pwOneToManyRelationships.println("/>");
            this.pwOneToManyRelationships.println("      </one-to-many>");
        }
        if(sliceIsBaseClass) {
            if(this.classMetaData.getBaseClass() == null) {
                // <id name="openmdxjdoIdentity">
                //  <column name="OBJECT_ID"/>
                // </id>                
                String tag = "id";
                print("      <" + tag);
                printAttribute(this.pw, "name", InstanceMapper.JDO_IDENTITY_MEMBER);
                printLine(">");
                this.printColumn(
                    this.pw,
                    identityColumnMetaData, 
                    this.toColumnName(InstanceMapper.JDO_IDENTITY_MEMBER),
                    null,
                    null, 
                    -1
                );
                printLine("      </", tag, ">");
                // <id name="openmdxjdoIndex">
                //  <column name="IDX"/>
                // </id>                
                FieldMetaData indexFieldMetaData = this.classMetaData.getFieldMetaData(
                    InstanceMapper.INDEX_MEMBER
                );
                ColumnMetaData indexColumnMetaData = indexFieldMetaData == null ? 
                    null : 
                    indexFieldMetaData.getColumn();                        
                print("      <" + tag);
                printAttribute(this.pw, "name", InstanceMapper.INDEX_MEMBER);
                printLine(">");
                this.printColumn(
                    this.pw,
                    indexColumnMetaData, 
                    this.toColumnName(InstanceMapper.INDEX_MEMBER),
                    null,
                    null, 
                    -1
                );
                printLine("      </", tag, ">");                
                // <many-to-one name="openmdxjdoIdentity">
                //  <join-column name="OBJECT_ID" referenced-column-name="OBJECT_ID"/>
                // </many-to-one>
                this.pwManyToOneRelationships.print("      <many-to-one");
                printAttribute(this.pwManyToOneRelationships, "name", InstanceMapper.JDO_IDENTITY_MEMBER);
                this.pwManyToOneRelationships.println(">");
                this.pwManyToOneRelationships.print("        <join-column");
                printAttribute(this.pwManyToOneRelationships, "name", identityColumnMetaData == null ? this.toColumnName(InstanceMapper.JDO_IDENTITY_MEMBER) : identityColumnMetaData.getName());
                this.pwManyToOneRelationships.println("/>");
                this.pwManyToOneRelationships.println("      </many-to-one>");
            }
		}
	}

	//-----------------------------------------------------------------------
	/**
	 * Print the XML file header
	 */
	public static void fileHeader(
	    PrintWriter pw
	) {
        pw.print("<?xml");
        printAttribute(pw, "version", "1.0");
        printAttribute(pw, "encoding", "UTF-8");
        pw.println("?>");
	    pw.print("<entity-mappings");
	    printAttribute(pw, "xmlns", "http://java.sun.com/xml/ns/persistence/orm");
	    printAttribute(pw, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
	    printAttribute(pw, "xsi:schemaLocation", "http://java.sun.com/xml/ns/persistence/orm http://java.sun.com/xml/ns/persistence/orm_1_0.xsd"); 	    
        printAttribute(pw, "version", "1.0");
        pw.println(">");
		pw.println("<!--");
		pw.println(" ! Generated by: openMDX Meta Data Mapper");
		pw.println(" ! Date: " + DateTimeFormat.EXTENDED_UTC_FORMAT.format(new Date()));
		pw.println(" !");
		pw.println(" ! GENERATED - DO NOT CHANGE MANUALLY");
		pw.println(" !-->");
		pw.println("  <access>FIELD</access>");		
	}
    
    //-----------------------------------------------------------------------
    /**
     * Print the XML file footer
     */
    public static void fileFooter(
        PrintWriter pw
    ) {
        pw.print("</entity-mappings>");
    }
    
    //-----------------------------------------------------------------------
    private void embed(
        StandardMetaDataMapper that
    ) throws ServiceException {
        that.mapSliceClassEnd();        
        that.pw.flush();
        if(that.process) {
            this.pw.write(((CharArrayWriter) that.writer).toCharArray());
        }
    }

    //-----------------------------------------------------------------------
    public static void printAttribute(
        PrintWriter pw,
        String name, 
        Object value
    ) {
        if(value != null) {
            pw.print(' ');
            pw.print(name);
            pw.print('=');
            pw.print('"');
            pw.print(value);
            pw.print('"');
        }
    }
    
    //-----------------------------------------------------------------------
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

    //-----------------------------------------------------------------------
    private String getPackagePrefix(
        List<String> qualifiedClassName
    ){
        if(this.metaData == null) {
            return null;
        } 
        else {
            int last = qualifiedClassName.size() - 1;
            StringBuilder packageName = new StringBuilder();
            for(String component : qualifiedClassName.subList(0, last)) {
                packageName.append(
                    component
                ).append(
                    '.'
                );
            }
            packageName.append(Names.JPA3_PACKAGE_SUFFIX);
            PackageMetaData metaData = ((MetaData_2_0)this.metaData).getPackage(
                packageName.toString()
            ); 
            return metaData == null ? null : metaData.getTablePrefix();
        }
    }
    
    //-----------------------------------------------------------------------
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
        return this.sliceClassName == null ?
            tableName :
            this.plugin.getSliceTableName(tableName);
    }
    
    //-----------------------------------------------------------------------
    private InheritanceStrategy getInheritanceStrategy(
    ) {
        InheritanceStrategy thisStrategy = getInheritanceStrategy(this.classDef);
        if(thisStrategy == null && !isBaseClass()) {
            InheritanceStrategy superStrategy = getInheritanceStrategy(this.extendsClassDef);
            if(superStrategy == InheritanceStrategy.TABLE_PER_CLASS) {
                thisStrategy = InheritanceStrategy.JOINED;
            }
        }
        return thisStrategy;
    }

    //-----------------------------------------------------------------------
    protected static InheritanceStrategy getInheritanceStrategy(
        ClassDef classDef
    ) {
        ClassMetaData classMetaData = (ClassMetaData) classDef.getClassMetaData();
        if(classMetaData != null) {
            InheritanceMetaData inheritanceMetaData = classMetaData.getInheritance();
            if(inheritanceMetaData != null){
                return inheritanceMetaData.getStrategy();
            }
        }
        return null;
    }
    
    //-----------------------------------------------------------------------
    @Override
    public void setProcess(
        boolean process
    ) {
        this.process = process;
    }
    
    //-----------------------------------------------------------------------
    static final String DEFAULT_IDENTIFIER_MAPPING = "?";
    
    private boolean process;    
    private final ByteArrayOutputStream streamBasicAttributes = new ByteArrayOutputStream();
    private PrintWriter pwBasicAttributes;
    private final ByteArrayOutputStream streamTransientAttributes = new ByteArrayOutputStream();
    private PrintWriter pwTransientAttributes;
    private final ByteArrayOutputStream streamOneToOneRelationships = new ByteArrayOutputStream();
    private PrintWriter pwOneToOneRelationships;
    private final ByteArrayOutputStream streamOneToManyRelationships = new ByteArrayOutputStream();
    private PrintWriter pwOneToManyRelationships;
    private final ByteArrayOutputStream streamManyToOneRelationships = new ByteArrayOutputStream();
    private PrintWriter pwManyToOneRelationships;
    private final ByteArrayOutputStream streamVersionAttributes = new ByteArrayOutputStream();
    private PrintWriter pwVersionAttributes;

}
