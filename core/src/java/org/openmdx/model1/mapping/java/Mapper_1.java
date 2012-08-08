/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Mapper_1.java,v 1.57 2008/09/10 11:39:02 hburger Exp $
 * Description: Mapper_1
 * Revision:    $Revision: 1.57 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 11:39:02 $
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
package org.openmdx.model1.mapping.java;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.omg.model1.code.VisibilityKind;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_5;
import org.openmdx.model1.accessor.basic.spi.ModelElement_1;
import org.openmdx.model1.code.ModelAttributes;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.model1.code.PrimitiveTypes;
import org.openmdx.model1.code.Stereotypes;
import org.openmdx.model1.importer.jdo2.MetaData_2;
import org.openmdx.model1.importer.metadata.FieldMetaData;
import org.openmdx.model1.mapping.AbstractMapper_1;
import org.openmdx.model1.mapping.AbstractNames;
import org.openmdx.model1.mapping.AttributeDef;
import org.openmdx.model1.mapping.ClassDef;
import org.openmdx.model1.mapping.ClassifierDef;
import org.openmdx.model1.mapping.ExceptionDef;
import org.openmdx.model1.mapping.Mapper_1_1;
import org.openmdx.model1.mapping.MappingTypes;
import org.openmdx.model1.mapping.MetaData_1_0;
import org.openmdx.model1.mapping.Names;
import org.openmdx.model1.mapping.OperationDef;
import org.openmdx.model1.mapping.ReferenceDef;
import org.openmdx.model1.mapping.StructDef;
import org.openmdx.model1.mapping.StructuralFeatureDef;
import org.openmdx.model1.mapping.plugin.ObjectRepositoryMetadataPlugin;
import org.openmdx.model1.mapping.plugin.StandardObjectRepositoryMetadataPlugin;

//---------------------------------------------------------------------------
public class Mapper_1
extends AbstractMapper_1 
implements Mapper_1_1
{

    //---------------------------------------------------------------------------  
    /**
     * Constructor.
     * @param mappingFormat mapping format defined MapperFactory_1.
     * @param packageSuffix The suffix for the package to be generated in (without leading dot), e.g. 'cci'.
     * @param fileExtension The file extension (without leading point), e.g. 'java'.
     */
    public Mapper_1(
        String mappingFormat,
        String packageSuffix,
        String fileExtension
    ) throws ServiceException {
        super(
            packageSuffix
        );    
        this.fileExtension = fileExtension;
        if(mappingFormat.startsWith(MappingTypes.JDO2 + ':')) {
            this.format = Format.JDO2;
            String plugIn = mappingFormat.substring(MappingTypes.JDO2.length() + 1);
            try {
                this.objectRepositoryMetadataPlugin = (ObjectRepositoryMetadataPlugin) Classes.getApplicationClass(
                    plugIn
                ).newInstance();
            } catch (Exception exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ACTIVATION_FAILURE,
                    "Could not load object repository metadata plugin",
                    new BasicException.Parameter("format", MappingTypes.JDO2),
                    new BasicException.Parameter("packageSuffix", packageSuffix),
                    new BasicException.Parameter("fileExtension", fileExtension),
                    new BasicException.Parameter("plugIn", plugIn)
                );
            }
        } else {
            this.format = 
                MappingTypes.CCI2.equals(mappingFormat) ? Format.CCI2 :
                    MappingTypes.JMI1.equals(mappingFormat) ? Format.JMI1 :
                        MappingTypes.JDO2.equals(mappingFormat) ? Format.JDO2 :
                            null;
            this.objectRepositoryMetadataPlugin = new StandardObjectRepositoryMetadataPlugin();
        }
    }

    //---------------------------------------------------------------------------  
    /**
     * Is called for all ModelAttribute features of a class including suerptyes.
     * This method must check wheter modelAttribute.container = modelClass and
     * behave accordingly. 
     * @param jdoMetaDataMapper 
     * @param jdoSliceMetaDataMapper 
     * @param ormMetaDataMapper 
     * @param ormSliceMetaDataMapper 
     */
    void mapAttribute(
        ModelElement_1_0 classDef,
        ModelElement_1_0 attributeDef,
        QueryMapper queryMapper,
        InstanceMapper instanceMapper,
        MetaDataMapper jdoMetaDataMapper, 
        MetaDataMapper jdoSliceMetaDataMapper, 
        MetaDataMapper ormMetaDataMapper, 
        MetaDataMapper ormSliceMetaDataMapper
    ) throws ServiceException {

        SysLog.trace("attribute", attributeDef.path());
        String multiplicity = (String)attributeDef.values("multiplicity").get(0);
        boolean isDerived = ((Boolean)attributeDef.values("isDerived").get(0)).booleanValue();
        boolean isChangeable = ((Boolean)attributeDef.values("isChangeable").get(0)).booleanValue();
        // required for ...Class.create...() operations
        this.processedAttributes.add(attributeDef);
        try {
            if(
                    this.format == Format.JDO2 ||
                    VisibilityKind.PUBLIC_VIS.equals(attributeDef.values("visibility").get(0))
            ) {    
                AttributeDef mAttributeDef = new AttributeDef(
                    attributeDef,
                    this.model,
                    false // openmdx1
                );       
                ClassDef mClassDef = new ClassDef(
                    classDef,
                    this.model,
                    this.metaData
                );
                // Attributes are read-only if they are not changeable or derived
                boolean isReadOnly = !isChangeable || isDerived;
                // setter/getter interface only if modelAttribute.container = modelClass. 
                // Otherwise inherit from super interfaces.
                if(
                        this.format == Format.JDO2 ||
                        attributeDef.values("container").get(0).equals(classDef.path())
                ){
                    // query interface
                    if(queryMapper != null) {
                        queryMapper.mapStructurealFeature(mClassDef, mAttributeDef);
                    }
                    // Note:
                    // Set operations in interfaces are generated only if the attribute
                    // is changeable and is not derived.

                    // set/get interface 0..1
                    if(Multiplicities.OPTIONAL_VALUE.equals(multiplicity)) {
                        if(instanceMapper != null) {
                            instanceMapper.mapAttributeGet0_1(mAttributeDef);
                        }
                        if(this.format == Format.JDO2) {
                            jdoMetaDataMapper.mapAttribute(mAttributeDef);
                            ormMetaDataMapper.mapAttribute(mAttributeDef);
                        }
                        if(!isReadOnly) {
                            if(instanceMapper != null) {
                                instanceMapper.mapAttributeSet0_1(mAttributeDef);
                            }
                        }
                    }

                    // set/get interface 1..1
                    else if(Multiplicities.SINGLE_VALUE.equals(multiplicity)) {
                        if(instanceMapper != null) {
                            instanceMapper.mapAttributeGet1_1(mAttributeDef);
                        }
                        if(this.format == Format.JDO2) {
                            jdoMetaDataMapper.mapAttribute(mAttributeDef);
                            ormMetaDataMapper.mapAttribute(mAttributeDef);
                        }
                        if(!isReadOnly) {
                            if(instanceMapper != null) {
                                instanceMapper.mapAttributeSet1_1(mAttributeDef);
                            }
                        }
                    }

                    // set/get interface list
                    else if(Multiplicities.LIST.equals(multiplicity)) {
                        if(instanceMapper != null) {
                            instanceMapper.mapAttributeGetList(mAttributeDef);
                        }
                        if(this.format == Format.JDO2) {
                            FieldMetaData fieldMetaData = instanceMapper.getFieldMetaData(mAttributeDef.getQualifiedName()); 
                            Integer embedded = fieldMetaData == null ? null : fieldMetaData.getEmbedded();
                            if(embedded == null) {
                                jdoSliceMetaDataMapper.mapAttribute(mAttributeDef);
                                ormSliceMetaDataMapper.mapAttribute(mAttributeDef);
                                jdoMetaDataMapper.mapSize(mAttributeDef);                                
                                ormMetaDataMapper.mapSize(mAttributeDef);
                            } else {
                                jdoMetaDataMapper.mapEmbedded(mAttributeDef, fieldMetaData);                                
                                ormMetaDataMapper.mapEmbedded(mAttributeDef, fieldMetaData);
                            }
                        }
                        if(!isReadOnly) {
                            if(instanceMapper != null) {                
                                instanceMapper.mapAttributeSetList(mAttributeDef);
                            }
                        }
                    }  

                    // set/get interface set
                    else if(Multiplicities.SET.equals(multiplicity)) {
                        if(instanceMapper != null) {
                            instanceMapper.mapAttributeGetSet(mAttributeDef);
                        }
                        if(this.format == Format.JDO2) {
                            FieldMetaData fieldMetaData = instanceMapper.getFieldMetaData(mAttributeDef.getQualifiedName()); 
                            Integer embedded = fieldMetaData == null ? null : fieldMetaData.getEmbedded();
                            if(embedded == null) {
                                jdoSliceMetaDataMapper.mapAttribute(mAttributeDef);
                                ormSliceMetaDataMapper.mapAttribute(mAttributeDef);
                                jdoMetaDataMapper.mapSize(mAttributeDef);                                
                                ormMetaDataMapper.mapSize(mAttributeDef);
                            } else {
                                jdoMetaDataMapper.mapEmbedded(mAttributeDef, fieldMetaData);                                
                                ormMetaDataMapper.mapEmbedded(mAttributeDef, fieldMetaData);
                            }
                        }
                        if(!isReadOnly) {
                            if(instanceMapper != null) {               
                                instanceMapper.mapAttributeSetSet(mAttributeDef);
                            }
                        }
                    }  

                    // set/get interface sparsearray
                    else if(Multiplicities.SPARSEARRAY.equals(multiplicity)) {
                        if(instanceMapper != null) {
                            instanceMapper.mapAttributeGetSparseArray(mAttributeDef);
                        }
                        if(this.format == Format.JDO2) {
                            jdoSliceMetaDataMapper.mapAttribute(mAttributeDef);
                            ormSliceMetaDataMapper.mapAttribute(mAttributeDef);
                            jdoMetaDataMapper.mapSize(mAttributeDef);
                            ormMetaDataMapper.mapSize(mAttributeDef);
                        }
                        if(!isReadOnly) {
                            if(instanceMapper != null) {
                                instanceMapper.mapAttributeSetSparseArray(mAttributeDef);
                            }
                        }
                    }  
                    // set/get interface map
                    else if(Multiplicities.MAP.equals(multiplicity)) {
                        if(instanceMapper != null) {
                            instanceMapper.mapAttributeGetMap(mAttributeDef);
                        }

                    }  
                    // set/get interface stream
                    else if(Multiplicities.STREAM.equals(multiplicity)) {
                        if(instanceMapper != null) {
                            instanceMapper.mapAttributeGetStream(mAttributeDef);
                        }

                        if(!isReadOnly) {
                            if(instanceMapper != null) {
                                instanceMapper.mapAttributeSetStream(mAttributeDef);
                            }
                        }
                    }  
                    // set/get interface with lower and upper bound, e.g. 0..n
                    else {
                        if(instanceMapper != null) {
                            instanceMapper.mapAttributeGetList(mAttributeDef);
                        }
                        if(this.format == Format.JDO2) {
                            jdoSliceMetaDataMapper.mapAttribute(mAttributeDef);
                            ormSliceMetaDataMapper.mapAttribute(mAttributeDef);
                            jdoMetaDataMapper.mapSize(mAttributeDef);
                            ormMetaDataMapper.mapSize(mAttributeDef);
                        }
                    }  
                }
            }
        } 
        catch(Exception ex) {
            throw new ServiceException(ex);
        }
    }

    //---------------------------------------------------------------------------    
    @SuppressWarnings("unchecked")
    void mapReference(
        ModelElement_1_0 classDef,
        ModelElement_1_0 referenceDef,
        QueryMapper queryMapper,
        InstanceMapper instanceMapper, 
        MetaDataMapper jdoMetaDataMapper, 
        MetaDataMapper jdoSliceMetaDataMapper, 
        MetaDataMapper ormMetaDataMapper, 
        MetaDataMapper ormSliceMetaDataMapper, 
        boolean inherited
    ) throws ServiceException {

        SysLog.trace("reference", referenceDef.path());
        ModelElement_1_0 referencedEnd = this.model.getElement(
            referenceDef.values("referencedEnd").get(0)
        );
        DataproviderObject association = (DataproviderObject)this.model.getElement(
            referencedEnd.values("container").get(0)
        );

        SysLog.trace("referencedEnd", referencedEnd);
        SysLog.trace("association", association);

        // setter/getter interface only if modelAttribute.container = modelClass. 
        // Otherwise inherit from super interfaces.
        boolean includeInClass = 
            ((this.format != Format.CCI2) && (this.format != Format.JMI1)) ||
            referenceDef.values("container").get(0).equals(classDef.path());
        String multiplicity = (String)referenceDef.values("multiplicity").get(0);
        String visibility = (String)referenceDef.values("visibility").get(0);
        List qualifierNames = referencedEnd.values("qualifierName");
        List qualifierTypes = referencedEnd.values("qualifierType");
        boolean isChangeable = ((Boolean)referenceDef.values("isChangeable").get(0)).booleanValue();
        boolean isDerived = ((Boolean)association.values("isDerived").get(0)).booleanValue();
        boolean includeExtensions = this.format == Format.JMI1 && !inherited;

        // check whether this reference is stored as attribute
        // required for ...Class.create...() operations
        // Note:
        if(this.model.referenceIsStoredAsAttribute(referenceDef)) {
            SysLog.trace(
                "the reference is stored as attribute", 
                referenceDef.values("qualifiedName").get(0)
            );
            ModelElement_1_0 referenceAsAttribute = null;
            this.processedAttributes.add(
                referenceAsAttribute = new ModelElement_1(referenceDef)
            );
            if(!qualifierNames.isEmpty()) {
                SysLog.trace(qualifierNames.toString(), qualifierTypes);
                String newMultiplicity = 
                    qualifierTypes.size() == 1 && 
                    PrimitiveTypes.STRING.equals(((Path)qualifierTypes.get(0)).getBase()) ? 
                        Multiplicities.MAP : 
                            Multiplicities.MULTI_VALUE;
                SysLog.trace("Adjust multiplicity to " + Multiplicities.MULTI_VALUE, newMultiplicity);
                // 0..n association        
                // set multiplicity to 0..n, this ensures that the instance creator uses
                // a multivalued parameter for this reference attribute
                referenceAsAttribute.clearValues("multiplicity").add(newMultiplicity);
            }
            referenceAsAttribute.values("isDerived").add(
                association.values("isDerived").get(0)
            );
        }
        try {
            if(VisibilityKind.PUBLIC_VIS.equals(visibility)) {  
                ReferenceDef mReferenceDef = new ReferenceDef(
                    referenceDef,
                    this.model, 
                    false // openmdx1
                );
                ClassDef mClassDef = new ClassDef(
                    classDef,
                    this.model,
                    this.metaData
                );
                /**
                 * References are read-only if they are not changeable. In addition they are also
                 * read-only if the reference is derived and stored as attribute.
                 */
                boolean isReadOnly = !isChangeable || (
                        isDerived && this.model.referenceIsStoredAsAttribute(referenceDef)
                );
                if(this.model.referenceIsStoredAsAttribute(referenceDef)) {  
                    //
                    // query for references stored as attributes
                    //
                    if(includeInClass && (queryMapper != null) && !inherited) {
                        queryMapper.mapStructurealFeature(mClassDef, mReferenceDef);
                    } 
                    //
                    // JDO fields for references stored as attributes
                    //
                    if(
                            includeInClass && 
                            this.format == Format.JDO2 &&
                            !instanceMapper.isTransient(mReferenceDef)
                    ){
                        if(qualifierNames.isEmpty()) {
                            if(
                                    Multiplicities.OPTIONAL_VALUE.equals(multiplicity) ||
                                    Multiplicities.SINGLE_VALUE.equals(multiplicity)
                            ){
                                jdoMetaDataMapper.mapReference(mReferenceDef);
                                ormMetaDataMapper.mapReference(mReferenceDef);
                            } else if(
                                    Multiplicities.LIST.equals(multiplicity) ||
                                    Multiplicities.SET.equals(multiplicity) ||
                                    Multiplicities.MULTI_VALUE.equals(multiplicity) ||
                                    Multiplicities.SPARSEARRAY.equals(multiplicity)
                            ){
                                jdoSliceMetaDataMapper.mapReference(mReferenceDef);
                                ormSliceMetaDataMapper.mapReference(mReferenceDef);
                                jdoMetaDataMapper.mapSize(mReferenceDef);
                                ormMetaDataMapper.mapSize(mReferenceDef);
                            }
                        } else {
                            jdoSliceMetaDataMapper.mapReference(mReferenceDef);
                            ormSliceMetaDataMapper.mapReference(mReferenceDef);
                            jdoMetaDataMapper.mapSize(mReferenceDef);
                            ormMetaDataMapper.mapSize(mReferenceDef);
                        }
                    }
                }
                // no qualifier, multiplicity must be [0..1|1..1|0..n]
                if(qualifierNames.isEmpty()) {
                    // 0..1
                    if(Multiplicities.OPTIONAL_VALUE.equals(multiplicity)) {
                        if(includeInClass && !inherited) {                            
                            if(instanceMapper != null){
                                // get
                                instanceMapper.mapReferenceGetx_1NoQualifier(
                                    mReferenceDef, 
                                    true, // optional
                                    true // referencedEnd
                                );
                                if(!isReadOnly) {
                                    // set
                                    instanceMapper.mapReferenceSetNoQualifier(
                                        mReferenceDef, 
                                        true // referencedEnd
                                    );
                                    if(includeExtensions) {
                                        instanceMapper.mapReferenceRemoveOptional(mReferenceDef);
                                    }
                                }
                            }
                        }
                    }
                    // 1..1
                    else if (Multiplicities.SINGLE_VALUE.equals(multiplicity)) {
                        if(includeInClass && !inherited) {
                            if(instanceMapper != null){
                                // get
                                instanceMapper.mapReferenceGetx_1NoQualifier(
                                    mReferenceDef, 
                                    false, // optional
                                    true // referencedEnd
                                );
                                if(!isReadOnly) {
                                    // set
                                    instanceMapper.mapReferenceSetNoQualifier(
                                        mReferenceDef, 
                                        true // referencedEnd
                                    );
                                }
                            }
                        }
                    }
                    // 0..n
                    else {
                        // get
                        if(includeInClass) {
                            if(instanceMapper != null){
                                instanceMapper.mapReferenceGet0_nNoQuery(mReferenceDef, inherited);                  
                            }
                        }
                    }          
                }
                // 0..n association where qualifier qualifies 1..1, 0..1, 0..n
                else { 
                    boolean qualifierTypeIsPrimitive = this.model.isPrimitiveType(qualifierTypes.get(0));
                    boolean optional = Multiplicities.OPTIONAL_VALUE.equals(multiplicity);
                    boolean mandatory = Multiplicities.SINGLE_VALUE.equals(multiplicity); 
                    boolean qualifiesUniquely = optional | mandatory;
                    if(qualifiesUniquely) {
                        //
                        // qualifier is optional if not complex
                        //
                        if(
                                includeInClass &&
                                instanceMapper != null
                        ){
                            if(this.model.referenceIsStoredAsAttribute(referenceDef)) {
                                instanceMapper.mapReferenceGet0_nNoQuery(mReferenceDef, inherited);                  
                            } else {
                                if(
                                        this.format == Format.JDO2 &&
                                        !instanceMapper.isTransient(mReferenceDef)
                                ) {
                                    jdoMetaDataMapper.mapReference(mReferenceDef);
                                    ormMetaDataMapper.mapReference(mReferenceDef);
                                }
                                instanceMapper.mapReferenceGet0_nWithQuery(mReferenceDef);
                                if(optional) {
                                    instanceMapper.mapReferenceGet0_1WithQualifier(mReferenceDef);
                                } else {
                                    instanceMapper.mapReferenceGet1_1WithQualifier(mReferenceDef);
                                }
                            }
                        }
                    } else {
                        //
                        // !qualifiesUniquely. qualifier is required
                        //
                        if(!this.model.referenceIsStoredAsAttribute(referenceDef)) {  
                            if(includeInClass) {
                                if(instanceMapper != null){
                                    instanceMapper.mapReferenceGet0_nWithQualifier(mReferenceDef, inherited);
                                }
                            }
                        }
                        /**
                         * It is NOT possible to have this situation. If a qualifier does
                         * not qualify uniquely, then it must be a non-primitive type.
                         * (If the qualifier type is primitive, then the multiplicity must
                         * be 0..1 or 1..1, i.e. the qualifier qualifes uniquely [openMDX 
                         * Constraint].)
                         * Non-primitive, ambiguous qualifiers are used to indicate the 
                         * owner of the associated element. The owner is only used if the
                         * reference is NOT stored as attribute (otherwise you would know 
                         * your associated elements).
                         */
                        else {              
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE,
                                "reference with non-primitive, ambiguous qualifier cannot be stored as attribute",
                                new BasicException.Parameter("reference", referenceDef.path()),
                                new BasicException.Parameter("qualifier", qualifierNames.get(0))
                            );
                        }
                    }  
                    // add with qualifier
                    if(qualifiesUniquely && qualifierTypeIsPrimitive) {
                        if(!isReadOnly) {
                            if(includeInClass) {
                                if(instanceMapper != null){
                                    instanceMapper.mapReferenceAddWithQualifier(mReferenceDef);
                                }
                            }
                        }
                    }
                    if(includeExtensions) {
                        // add without qualifier
                        if(!isReadOnly) {
                            if(includeInClass) {
                                if(instanceMapper != null){
                                    instanceMapper.mapReferenceAddWithoutQualifier(mReferenceDef);
                                }
                            }
                        }
                        // remove with qualifier if qualifier qualifies uniquely
                        if(qualifiesUniquely) {

                            if(!isReadOnly) {
                                if(includeInClass) {
                                    if(instanceMapper != null){
                                        instanceMapper.mapReferenceRemoveWithQualifier(mReferenceDef);
                                    }
                                }
                            }
                        }
                    }              
                }
            }
        } 
        catch(Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    //---------------------------------------------------------------------------    
    void mapOperation(
        ModelElement_1_0 classDef,
        ModelElement_1_0 operationDef,
        InstanceMapper instanceMapper, 
        MetaDataMapper objectMetaDataMapper, 
        MetaDataMapper collectionSliceMetaDataMapper, 
        MetaDataMapper arraySliceMetaDataMapper
    ) throws ServiceException {

        SysLog.trace("operation", operationDef.path());
        boolean includeInClass = this.format == Format.JDO2 || operationDef.values("container").get(0).equals(classDef.path());
        if(VisibilityKind.PUBLIC_VIS.equals(operationDef.values("visibility").get(0))) {

            try {
                OperationDef mOperationDef = new OperationDef(
                    operationDef,
                    this.model, 
                    false // openmdx1
                );
                if(includeInClass) {
                    instanceMapper.mapOperation(mOperationDef);
                }
            } 
            catch(Exception ex) {
                throw new ServiceException(ex).log();
            }
        }
    }

    //---------------------------------------------------------------------------    
    void mapException(
        ModelElement_1_0 exceptionDef,
        ExceptionMapper exceptionMapper
    ) throws ServiceException {

        SysLog.trace("exception", exceptionDef.path());
        if(VisibilityKind.PUBLIC_VIS.equals(exceptionDef.values("visibility").get(0))) {
            try {
                exceptionMapper.mapException();
            } 
            catch(Exception ex) {
                throw new ServiceException(ex).log();
            }
        }
    }

    //---------------------------------------------------------------------------    
    boolean mapAssociation(
        ModelElement_1_0 associationDef,
        AssociationMapper associationMapper
    ) throws ServiceException {

        SysLog.trace("association", associationDef.path());
        if(VisibilityKind.PUBLIC_VIS.equals(associationDef.values("visibility").get(0))) try {
            return associationMapper.mapAssociation();
        } catch(Exception exception) {
            throw new ServiceException(exception).log();
        } else {
            return false;
        }
    }

    //---------------------------------------------------------------------------    
    void mapBeginClass(
        ModelElement_1_0 classDef,
        ClassMapper classMapper,
        InstanceMapper instanceMapper, 
        InterfaceMapper interfaceMapper,
        MetaDataMapper jdoMetaDataMapper, 
        MetaDataMapper jdoSliceMetaDataMapper, 
        MetaDataMapper ormMetaDataMapper, 
        MetaDataMapper ormSliceMetaDataMapper
    ) throws ServiceException {

        SysLog.trace("class", classDef.path());
        boolean isAbstract = ((Boolean)classDef.values("isAbstract").get(0)).booleanValue();
        try {
            // only generate for non-abstract classes
            if(this.format == Format.JMI1 && !isAbstract) {
                // class interface
                classMapper.mapBegin();
            }
            // object interface
            instanceMapper.mapBegin();
            if(this.format == Format.JDO2) {
                if(interfaceMapper != null) {
                    interfaceMapper.mapBegin();
                }
                jdoMetaDataMapper.mapBegin();
                ormMetaDataMapper.mapBegin();
                jdoSliceMetaDataMapper.mapBegin();
                ormSliceMetaDataMapper.mapBegin();
            }
        } 
        catch(Exception ex) {
            throw new ServiceException(ex).log();
        }
        this.processedAttributes = new ArrayList<ModelElement_1_0>();
    }

    //---------------------------------------------------------------------------    
    void mapObjectCreator(
        ModelElement_1_0 classDef,
        ModelElement_1_0 supertypeDef,
        ClassMapper classMapper
    ) throws ServiceException {

        SysLog.trace("class", classDef.path());

        // traverse all processed attributes (this includes all regular attributes and
        // those references that are stored as attributes) to find out which 
        // attributes are mandatory
        List<AttributeDef> allAttributes = new ArrayList<AttributeDef>();
        List<AttributeDef> requiredAttributes = new ArrayList<AttributeDef>();
        for(
                Iterator<ModelElement_1_0> i = this.processedAttributes.iterator();
                i.hasNext();
        ) {
            ModelElement_1_0 attributeDef = i.next();
            // attribute member of class, non-derived and public
            if(
                    ((supertypeDef == null) || !supertypeDef.values("feature").contains(attributeDef.path())) &&
                    !((Boolean)attributeDef.values("isDerived").get(0)).booleanValue() &&
                    VisibilityKind.PUBLIC_VIS.equals(attributeDef.values("visibility").get(0))
            ) {
                AttributeDef att = new AttributeDef(
                    attributeDef,
                    this.model,
                    false // openmdx1
                );
                allAttributes.add(att);
                // required attribute
                String multiplicity = (String)attributeDef.values("multiplicity").get(0);
                SysLog.trace(attributeDef.values("qualifiedName").get(0).toString(), multiplicity);
                if(Multiplicities.SINGLE_VALUE.equals(attributeDef.values("multiplicity").get(0))) {
                    requiredAttributes.add(att);
                } 
                else {
                    /* isAllAttributesRequired = false; */
                }
            }
        }

        try {
            // creators
            if(supertypeDef == null) {        

                // check whether all attributes are mandatory and whether there are some
                // mandatory attributes at all; if so, do not generate creator 
                // (otherwise we get a duplicate creator definition)
                if(/*!isAllAttributesRequired &&*/ requiredAttributes.size() > 0) {
                    classMapper.mapInstanceCreatorRequiredAttributes(requiredAttributes);      
                }
            }
            // extenders
            else {
                ClassDef mSuperclassDef = new ClassDef(
                    supertypeDef,
                    this.model,
                    this.metaData
                );
                // check whether all attributes are mandatory and whether there are some
                // mandatory attributes at all; if so, do not generate creator 
                // (otherwise we get a duplicate creator definition)
                if(/*!isAllAttributesRequired &&*/ requiredAttributes.size() > 0) {
                    classMapper.mapInstanceExtenderRequiredAttributes(mSuperclassDef, requiredAttributes);    
                }
            }
        } 
        catch(Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    //---------------------------------------------------------------------------    
    void mapEndClass(
        ModelElement_1_0 classDef,
        ClassMapper classMapper,
        InstanceMapper instanceMapper, 
        InterfaceMapper interfaceMapper,
        MetaDataMapper jdoMetaDataMapper, 
        MetaDataMapper jdoSliceMetaDataMapper, 
        MetaDataMapper ormMetaDataMapper, 
        MetaDataMapper ormSliceMetaDataMapper
    ) throws ServiceException {

        if (this.format == Format.JMI1) {
            // add additional template references to context
            List<StructuralFeatureDef> structuralFeatures = new ArrayList<StructuralFeatureDef>();
            List<OperationDef> operations = new ArrayList<OperationDef>();
            for(
                    Iterator<?> i = classDef.values("feature").iterator();
                    i.hasNext();
            ) {
                ModelElement_1_0 feature = this.model.getElement(i.next());
                if(
                        VisibilityKind.PUBLIC_VIS.equals(feature.values("visibility").get(0))
                ) {
                    if(
                            feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.ATTRIBUTE)
                    ) {
                        structuralFeatures.add(
                            new AttributeDef(
                                feature, 
                                this.model,
                                false // openmdx1
                            )
                        );
                    }
                    else if(
                            feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.REFERENCE)
                    ) {
                        ModelElement_1_0 referencedEnd = this.model.getElement(
                            feature.values("referencedEnd").get(0)
                        );
                        List<?> qualifierTypes = referencedEnd.values("qualifierType");
                        // skip references for which a qualifier exists and the qualifier is
                        // not a primitive type
                        if(
                                qualifierTypes.isEmpty() ||
                                this.model.isPrimitiveType(qualifierTypes.get(0))
                        ) {
                            structuralFeatures.add(
                                new ReferenceDef(
                                    feature,
                                    model, 
                                    false // openmdx1
                                )
                            );
                        }
                    }
                    else if(
                            feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.OPERATION)
                    ) {
                        operations.add(
                            new OperationDef(
                                feature,
                                this.model, 
                                false // openmdx1
                            )
                        );
                    }
                }
            }
        }
        boolean classIsAbstract = ((Boolean)classDef.values("isAbstract").get(0)).booleanValue();
        try {
            if (
                    this.format == Format.JMI1 &&
                    !classIsAbstract
            ) {  
                // standard creators
                mapObjectCreator(
                    classDef,
                    null,
                    classMapper
                );
                // narrow creators
                SysLog.trace("creators for", classDef.path());
                SysLog.trace("supertypes", classDef.values("allSupertype"));
                for(
                        Iterator<?> i = classDef.values("allSupertype").iterator();
                        i.hasNext();
                ) {
                    ModelElement_1_0 supertype = this.model.getDereferencedType(
                        i.next(),
                        false // openmdx1
                    );
                    if(!supertype.path().equals(classDef.path())) {
                        SysLog.trace("creating", supertype.path());
                        this.mapObjectCreator(
                            classDef,
                            supertype,
                            classMapper
                        );
                    }
                    else {
                        SysLog.trace("skipping", supertype.path());
                    }
                }
                classMapper.mapEnd();  
            }
            // impl for non-abstract and abstract classes (JMI plugins)
            instanceMapper.mapEnd();
            if(this.format == Format.JDO2) {
                if(interfaceMapper != null) {
                    interfaceMapper.mapEnd();
                }
                boolean process = 
                    instanceMapper.isSliceHolder() || 
                    instanceMapper.hasSlices(); 
                jdoSliceMetaDataMapper.setProcess(process);
                ormSliceMetaDataMapper.setProcess(process);
                jdoMetaDataMapper.mapEnd(jdoSliceMetaDataMapper);
                ormMetaDataMapper.mapEnd(ormSliceMetaDataMapper);
            }
        } catch(Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    //---------------------------------------------------------------------------    
    void mapBeginQuery(
        ClassifierDef mClassifierDef,
        QueryMapper queryMapper
    ) throws ServiceException {
        if(queryMapper != null) try {
            queryMapper.mapBegin(mClassifierDef);
        } 
        catch(Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    //---------------------------------------------------------------------------    
    void mapEndQuery(
        QueryMapper queryMapper
    ) throws ServiceException {
        if(queryMapper != null) try {
            queryMapper.mapEnd();  
        } 
        catch(Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    //---------------------------------------------------------------------------    
    void mapBeginStructure(
        ModelElement_1_0 structDef,
        StructureMapper structureMapper
    ) throws ServiceException {
        SysLog.trace("struct", structDef.path());
        try {
            structureMapper.mapBegin();  
        } 
        catch(Exception ex) {
            throw new ServiceException(ex).log();
        }
        this.processedAttributes = new ArrayList<ModelElement_1_0>();
    }

    //--------------------------------------------------------------------------------
    /**
     * Is called for all ModelAttribute features of a class including suerptyes.
     * This method must check wheter modelAttribute.container = modelClass and
     * behave accordingly. 
     */
    void mapStructureField(
        ModelElement_1_0 classDef,
        ModelElement_1_0 structureFieldDef,
        QueryMapper queryMapper,
        StructureMapper structureMapper, 
        boolean jmi
    ) throws ServiceException {

        SysLog.trace("structure field", structureFieldDef.path());
        String multiplicity = (String)structureFieldDef.values("multiplicity").get(0);

        // required for ...Class.create...() operations
        this.processedAttributes.add(structureFieldDef);
        try {
            AttributeDef mStructureFieldDef = new AttributeDef(
                structureFieldDef,
                this.model,
                false // openmdx1
            );
            StructDef mStructDef = new StructDef(
                classDef,
                this.model, 
                false // openmdx1
            );
            // getter interface only if modelAttribute.container = modelClass. 
            // Otherwise inherit from super interfaces.
            if(
                    this.format == Format.JDO2 ||
                    structureFieldDef.values("container").get(0).equals(classDef.path())
            ) {
                // query interface
                if(queryMapper != null) {
                    queryMapper.mapStructurealFeature(mStructDef, mStructureFieldDef);
                }
                // get interface 0..1
                if(Multiplicities.OPTIONAL_VALUE.equals(multiplicity)) {
                    structureMapper.mapFieldGet0_1(mStructureFieldDef);
                }
                // get interface 1..1
                else if(Multiplicities.SINGLE_VALUE.equals(multiplicity)) {
                    structureMapper.mapFieldGet1_1(mStructureFieldDef);
                }
                // get interface list
                else if(Multiplicities.LIST.equals(multiplicity)) {
                    structureMapper.mapFieldGetList(mStructureFieldDef);
                }
                // get interface set
                else if(Multiplicities.SET.equals(multiplicity)) {
                    structureMapper.mapFieldGetSet(mStructureFieldDef);
                }
                // get interface sparsearray
                else if(Multiplicities.SPARSEARRAY.equals(multiplicity)) {
                    structureMapper.mapFieldGetSparseArray(mStructureFieldDef);
                }
                // get interface stream
                else if(Multiplicities.STREAM.equals(multiplicity)) {
                    structureMapper.mapFieldGetStream(mStructureFieldDef);
                }
                // get interface with lower and upper bound, e.g. 0..n
                else {
                    structureMapper.mapFieldGetList(mStructureFieldDef);
                }  
            }
        } 
        catch(Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    //---------------------------------------------------------------------------    
    void mapEndStructure(
        StructureMapper structureMapper
    ) throws ServiceException {
        try {
            structureMapper.mapEnd();  
        } 
        catch(Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    //--------------------------------------------------------------------------------
    void mapBeginPackage(
        String forPackage,
        PackageMapper packageMapper
    ) throws ServiceException {
        try {
            packageMapper.mapBegin(forPackage);
        } 
        catch(Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    //--------------------------------------------------------------------------------
    void mapEndPackage(
        String forPackage,
        PackageMapper packageMapper
    ) throws ServiceException {
        try {
            packageMapper.mapEnd();  
        } catch(Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    //--------------------------------------------------------------------------------
    void mapObjectMarshaller(
        ModelElement_1_0 classDef,
        PackageMapper packageMapper
    ) throws ServiceException {
        try {
            ClassDef mClassDef = new ClassDef(
                classDef,
                this.model,
                this.metaData
            );
            packageMapper.mapClassAccessor(mClassDef);  
        } catch(Exception ex) {
            throw new ServiceException(ex);
        }

    }

    //--------------------------------------------------------------------------------
    private void mapStructureCreator(
        ModelElement_1_0 structDef,
        PackageMapper packageMapper
    ) throws ServiceException {
        try {
            StructDef mStructDef = new StructDef(
                structDef,
                this.model, 
                false // openmdx1
            );
            packageMapper.mapStructCreator(mStructDef);
        } catch(Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    //--------------------------------------------------------------------------------
    // Generates create query operations for a struct or a class in the current 
    // package
    private void mapQueryCreator(
        ClassifierDef mClassifierDef,
        PackageMapper packageMapper
    ) throws ServiceException {
        try {
            packageMapper.mapQueryCreator(mClassifierDef);
        } 
        catch(Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    //---------------------------------------------------------------------------    
    protected Model_1_5 getModel(){
        return (Model_1_5) super.model;
    }

    //---------------------------------------------------------------------------    
    /**
     * Externalizes given packageContent and stores result in the jar output
     * stream. 
     * 
     * @param qualifiedPackageName fully qualified, ':' separated name of a 
     *         model package. '%' as last character is allowed as wildcard, e.g.
     *         'org:%' exports all models contained in package 'org', 'org:openmdx:%' 
     *         exports all models contained in package 'org:openmdx'. All models
     *         are written to os.
     * @param model
     * @param os
     * 
     * @throws ServiceException
     */
    public void externalize(
        String qualifiedPackageName,
        org.openmdx.model1.accessor.basic.cci.Model_1_3 model,
        ZipOutputStream os
    ) throws ServiceException {
        externalize(
            qualifiedPackageName,
            model,
            os,
            null // openmdxjdoMetadataDirectory
        );
    }

    /**
     * Externalizes given packageContent and stores result in the jar output
     * stream. 
     * 
     * @param qualifiedPackageName fully qualified, ':' separated name of a 
     *         model package. '%' as last character is allowed as wildcard, e.g.
     *         'org:%' exports all models contained in package 'org', 'org:openmdx:%' 
     *         exports all models contained in package 'org:openmdx'. All models
     *         are written to os.
     * @param model
     * @param zip
     * @param openmdxjdoMetadataDirectory base directory for .openmdxjdo meta data files
     * 
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    public void externalize(
        String qualifiedPackageName,
        org.openmdx.model1.accessor.basic.cci.Model_1_3 model,
        ZipOutputStream zip,
        String openmdxjdoMetadataDirectory
    ) throws ServiceException {

        SysLog.trace("starting...");

        super.model = model;
        this.metaData = new MetaData_2(openmdxjdoMetadataDirectory);

        List<ModelElement_1_0> packagesToExport = this.getMatchingPackages(qualifiedPackageName);
        SysLog.detail("exporting packages " + packagesToExport);

        final boolean jmi1 = this.format == Format.JMI1;
        final boolean cci2 = this.format == Format.CCI2;
        final boolean jdo2 = this.format == Format.JDO2;

        try {
            // allocate streams one time
            ByteArrayOutputStream pkgFile = jdo2 ? null : new ByteArrayOutputStream();
            ByteArrayOutputStream classFile = jmi1 ? new ByteArrayOutputStream() : null;
            ByteArrayOutputStream instanceFile = new ByteArrayOutputStream();
            ByteArrayOutputStream interfaceFile = new ByteArrayOutputStream();
            ByteArrayOutputStream jdoMetaDataFile = jdo2 ? new ByteArrayOutputStream() : null;
            ByteArrayOutputStream ormMetaDataFile = jdo2 ? new ByteArrayOutputStream() : null;
            ByteArrayOutputStream structFile = jdo2 ? null : new ByteArrayOutputStream();
            ByteArrayOutputStream queryFile = cci2 ? new ByteArrayOutputStream() : null;
            ByteArrayOutputStream exceptionFile = jdo2 ? null : new ByteArrayOutputStream();
            ByteArrayOutputStream associationFile = cci2 ? new ByteArrayOutputStream() : null;

            Writer packageWriter = jdo2 ? null : new OutputStreamWriter(pkgFile); 
            Writer classWriter = jmi1 ? new OutputStreamWriter(classFile) : null;
            Writer queryWriter = cci2 ? new OutputStreamWriter(queryFile) : null;
            Writer instanceWriter = new OutputStreamWriter(instanceFile);
            Writer interfaceWriter = new OutputStreamWriter(interfaceFile);
            Writer jdoMetaDataWriter = jdo2 ? new OutputStreamWriter(jdoMetaDataFile) : null;
            Writer ormMetaDataWriter = jdo2 ? new OutputStreamWriter(ormMetaDataFile) : null;
            CharArrayWriter jdoSliceMetaDataWriter = jdo2 ? new CharArrayWriter() : null;
            CharArrayWriter ormSliceMetaDataWriter = jdo2 ? new CharArrayWriter() : null;
            Writer structWriter = jdo2 ? null : new OutputStreamWriter(structFile);

            // export matching packages
            for(
                    Iterator<ModelElement_1_0> pkgs = packagesToExport.iterator(); 
                    pkgs.hasNext();
            ) {
                ModelElement_1_0 currentPackage = pkgs.next();
                String currentPackageName = (String)currentPackage.values("qualifiedName").get(0);
                if(!excludePackage(currentPackageName)) {
                    SysLog.detail("Processing package", currentPackageName);
                    PackageMapper packageMapper = null;
                    if(jmi1) {
                        pkgFile.reset();
                        packageMapper = new PackageMapper(
                            packageWriter, 
                            getModel(), 
                            this.format, 
                            packageSuffix, 
                            this.metaData
                        );
                        // initialize package
                        this.mapBeginPackage(
                            currentPackageName,
                            packageMapper
                        );
                    }    
                    // process packageContent
                    for(
                            Iterator<ModelElement_1_0> i = getModel().getContent().iterator(); 
                            i.hasNext();
                    ) {  
                        ModelElement_1_0 element = i.next();
                        if(getModel().isLocal(element, currentPackageName)) {
                            //
                            // only generate elements which are content of the modelPackage. 
                            // Do not generate for imported model elements
                            //
                            SysLog.trace("processing package element", element.path());

                            // org:omg:model1:Class
                            if(
                                    getModel().isClassType(element) && (
                                            this.format != Format.JDO2 || 
                                            !element.values("stereotype").contains(Stereotypes.ROOT)
                                    )            
                            ) {
                                boolean isAbstract = ((Boolean)element.values("isAbstract").get(0)).booleanValue();      
                                // object marshaller for non-abstract and abstract classes (JMI plugins)
                                // generate create query operations for the current class in package
                                ClassDef mClassDef = new ClassDef(
                                    element,
                                    this.model,
                                    this.metaData
                                );
                                if(!excludeClass(mClassDef.getQualifiedName())) {
                                    SysLog.detail("Processing class", mClassDef.getQualifiedName());
                                    if(
                                            this.format != Format.JDO2 ||
                                            !isAbstract ||
                                            mClassDef.getSuperClassDef(true) == null
                                    ) {
                                        if(jmi1) {
                                            if(!isAbstract) {
                                                this.mapObjectMarshaller(
                                                    element,
                                                    packageMapper
                                                );
                                            }
                                            this.mapQueryCreator(
                                                mClassDef,
                                                packageMapper
                                            );
                                            classFile.reset();
                                        }
                                        if(cci2) {
                                            queryFile.reset();
                                        }
                                        instanceFile.reset();
                                        if(jdo2) {
                                            jdoMetaDataFile.reset();
                                            ormMetaDataFile.reset();
                                            jdoSliceMetaDataWriter.reset();
                                            ormSliceMetaDataWriter.reset();
                                        }

                                        ClassMapper classMapper = jmi1 ? new ClassMapper(
                                            element, 
                                            classWriter, 
                                            getModel(), 
                                            this.format, 
                                            this.packageSuffix,
                                            this.metaData
                                        ) : null;
                                            InstanceMapper instanceMapper = new InstanceMapper(
                                                element, 
                                                instanceWriter, 
                                                getModel(), 
                                                this.format, 
                                                this.packageSuffix,
                                                this.metaData
                                            );
                                            InterfaceMapper interfaceMapper = jdo2 && instanceMapper.hasSPI() ? new InterfaceMapper(
                                                element, 
                                                interfaceWriter, 
                                                getModel(), 
                                                Format.SPI2,
                                                Names.SPI2_PACKAGE_SUFFIX,
                                                this.metaData
                                            ) : null;
                                                MetaDataMapper jdoMetaDataMapper = jdo2 ? new MetaDataMapper(
                                                    element, 
                                                    jdoMetaDataWriter, 
                                                    getModel(), 
                                                    this.format, 
                                                    this.packageSuffix,
                                                    null, // innerClass
                                                    false, // orm
                                                    this.metaData, new StandardObjectRepositoryMetadataPlugin()
                                                ) : null;
                                                    MetaDataMapper jdoSliceMetaDataMapper = jdo2 ? new MetaDataMapper(
                                                        element, 
                                                        jdoSliceMetaDataWriter, 
                                                        getModel(), 
                                                        this.format, 
                                                        this.packageSuffix,
                                                        InstanceMapper.SLICE_CLASS_NAME, 
                                                        false, // orm
                                                        this.metaData, new StandardObjectRepositoryMetadataPlugin()
                                                    ) : null;
                                                        MetaDataMapper ormMetaDataMapper = jdo2 ? new MetaDataMapper(
                                                            element, 
                                                            ormMetaDataWriter, 
                                                            getModel(), 
                                                            this.format, 
                                                            this.packageSuffix,
                                                            null, // innerClass
                                                            true, // orm
                                                            this.metaData, new StandardObjectRepositoryMetadataPlugin()
                                                        ) : null;
                                                            MetaDataMapper ormSliceMetaDataMapper = jdo2 ? new MetaDataMapper(
                                                                element, 
                                                                ormSliceMetaDataWriter, 
                                                                getModel(), 
                                                                this.format, 
                                                                this.packageSuffix,
                                                                InstanceMapper.SLICE_CLASS_NAME, 
                                                                true, // orm
                                                                this.metaData, new StandardObjectRepositoryMetadataPlugin()
                                                            ) : null;
                                                                QueryMapper queryMapper = cci2 ? new QueryMapper(
                                                                    queryWriter, 
                                                                    getModel(), 
                                                                    this.format, 
                                                                    this.packageSuffix, this.metaData
                                                                ) : null;
                                                                    this.mapBeginClass(
                                                                        element,
                                                                        classMapper,
                                                                        instanceMapper, 
                                                                        interfaceMapper,
                                                                        jdoMetaDataMapper, 
                                                                        jdoSliceMetaDataMapper, 
                                                                        ormMetaDataMapper, 
                                                                        ormSliceMetaDataMapper
                                                                    );                
                                                                    this.mapBeginQuery(
                                                                        mClassDef,
                                                                        queryMapper
                                                                    );                
                                                                    // get class features
                                                                    for(Object f : getFeatures(element, instanceMapper, false)) {
                                                                        ModelElement_1_0 feature = f instanceof ModelElement_1_0 ? 
                                                                            (ModelElement_1_0) f :
                                                                                getModel().getElement(f);  
                                                                            SysLog.trace("processing class feature", feature.path());

                                                                            if(feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.ATTRIBUTE)) {
                                                                                this.mapAttribute(
                                                                                    element,
                                                                                    feature,
                                                                                    queryMapper,
                                                                                    instanceMapper, 
                                                                                    jdoMetaDataMapper, 
                                                                                    jdoSliceMetaDataMapper, 
                                                                                    ormMetaDataMapper, 
                                                                                    ormSliceMetaDataMapper
                                                                                );
                                                                            } else if(feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.REFERENCE)) {
                                                                                this.mapReference(
                                                                                    element,
                                                                                    feature,
                                                                                    queryMapper,
                                                                                    instanceMapper, 
                                                                                    jdoMetaDataMapper, 
                                                                                    jdoSliceMetaDataMapper, 
                                                                                    ormMetaDataMapper, 
                                                                                    ormSliceMetaDataMapper, 
                                                                                    false // inherited
                                                                                );
                                                                            } else if(feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.OPERATION)) {
                                                                                this.mapOperation(
                                                                                    element,
                                                                                    feature,
                                                                                    instanceMapper, 
                                                                                    jdoMetaDataMapper, 
                                                                                    jdoMetaDataMapper, 
                                                                                    jdoSliceMetaDataMapper
                                                                                );
                                                                            }
                                                                    }                
                                                                    this.mapEndQuery(
                                                                        queryMapper
                                                                    );                
                                                                    this.mapEndClass(
                                                                        element,
                                                                        classMapper,
                                                                        instanceMapper, 
                                                                        interfaceMapper,
                                                                        jdoMetaDataMapper, 
                                                                        jdoSliceMetaDataMapper, 
                                                                        ormMetaDataMapper, 
                                                                        ormSliceMetaDataMapper
                                                                    );                
                                                                    instanceWriter.flush();
                                                                    String elementName = instanceMapper.getClassName();
                                                                    //                                    Identifier.CLASS_PROXY_NAME.toIdentifier(
                                                                    //                                        (String)element.values("name").get(0)
                                                                    //                                    );
                                                                    this.addToZip(zip, instanceFile, element, elementName, "." + this.fileExtension);                
                                                                    if(jdo2){
                                                                        jdoMetaDataWriter.flush();
                                                                        this.addToZip(zip, jdoMetaDataFile, element, elementName, ".jdo");
                                                                        ormMetaDataWriter.flush();
                                                                        this.addToZip(zip, ormMetaDataFile, element, elementName, "-" + this.objectRepositoryMetadataPlugin.getMappingName() + ".orm");
                                                                        if(interfaceMapper != null ){
                                                                            interfaceWriter.flush();
                                                                            this.addToZip(
                                                                                zip,
                                                                                interfaceFile,
                                                                                element,
                                                                                elementName,
                                                                                "." + this.fileExtension,
                                                                                true, 
                                                                                Names.SPI2_PACKAGE_SUFFIX
                                                                            );
                                                                        }
                                                                    }                
                                                                    if(cci2) {
                                                                        queryWriter.flush();
                                                                        this.addToZip(zip, queryFile, element, elementName, "Query." + this.fileExtension);
                                                                    }

                                                                    if(jmi1 && !isAbstract) {
                                                                        classWriter.flush();
                                                                        this.addToZip(zip, classFile, element, elementName, "Class." + this.fileExtension);
                                                                    } 
                                    }
                                }
                            }
                            // org:omg:model1:StructureType
                            else if(
                                    getModel().isStructureType(element)
                            ) {
                                SysLog.trace("processing structure type", element.path());
                                StructDef mStructDef = new StructDef(
                                    element,
                                    getModel(), 
                                    false // openmdx1
                                );
                                if(jmi1) {
                                    this.mapStructureCreator(
                                        element,
                                        packageMapper
                                    );
                                }
                                if(structFile != null) {
                                    structFile.reset();
                                }
                                if(cci2) queryFile.reset();    
                                StructureMapper structureMapper = jdo2 
                                ? null 
                                    : new StructureMapper(
                                        element, 
                                        structWriter, 
                                        getModel(), 
                                        this.format, 
                                        this.packageSuffix, 
                                        this.metaData
                                    );
                                QueryMapper queryMapper = cci2 
                                ? new QueryMapper(
                                    queryWriter, 
                                    getModel(), 
                                    this.format, 
                                    this.packageSuffix, this.metaData
                                ) 
                                : null;                                            
                                    if(structureMapper != null) {
                                        this.mapBeginStructure(
                                            element,
                                            structureMapper
                                        );
                                    }            
                                    this.mapBeginQuery(
                                        mStructDef,
                                        queryMapper
                                    );
                                    // StructureFields
                                    for(
                                            Iterator<?> j = element.values("content").iterator();
                                            j.hasNext();
                                    ) {
                                        ModelElement_1_0 feature = getModel().getElement(j.next());    
                                        SysLog.trace("processing structure field", feature.path());   
                                        if(
                                                structureMapper != null &&
                                                feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.STRUCTURE_FIELD)
                                        ) {
                                            this.mapStructureField(
                                                element,
                                                feature,
                                                queryMapper,
                                                structureMapper, 
                                                jmi1
                                            );
                                        }
                                    }    
                                    this.mapEndQuery(
                                        queryMapper
                                    );
                                    String elementName = Identifier.CLASS_PROXY_NAME.toIdentifier(
                                        (String)element.values("name").get(0)
                                    );    
                                    if(structureMapper != null) {
                                        this.mapEndStructure(
                                            structureMapper
                                        );
                                        structWriter.flush();
                                        this.addToZip(zip, structFile, element, elementName, "." + this.fileExtension);
                                    }    
                                    if (cci2) {
                                        queryWriter.flush();
                                        this.addToZip(zip, queryFile, element, elementName, "Query." + this.fileExtension);
                                    }
                            }
                            // org:omg:model1:Exception
                            else if(
                                    element.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.EXCEPTION)
                            ) {   
                                SysLog.trace("processing exception", element.path());
                                if(!jdo2) {  
                                    exceptionFile.reset();
                                    Writer exceptionWriter = new OutputStreamWriter(exceptionFile);
                                    ExceptionMapper exceptionMapper = new ExceptionMapper(
                                        element,
                                        exceptionWriter,
                                        getModel(),
                                        this.format, 
                                        packageSuffix, 
                                        this.metaData
                                    );
                                    this.mapException(
                                        element,
                                        exceptionMapper
                                    );
                                    exceptionWriter.flush();
                                    String elementName = Identifier.CLASS_PROXY_NAME.toIdentifier(
                                        (String)element.values("name").get(0),
                                        null, // removablePrefix
                                        null, // prependablePrefix
                                        ExceptionDef.STANDARD_COMPLIANT ? "exception" : null, // removableSuffix
                                            ExceptionDef.STANDARD_COMPLIANT ? "exception" : null //appendableSuffix
                                    );
                                    this.addToZip(zip, exceptionFile, element, elementName, "." + this.fileExtension);
                                }
                            } else if(getModel().isAssociationType(element)) {
                                //
                                // org:omg:model1:AssociationType
                                // 
                                SysLog.trace("processing association", element.path());
                                if(cci2) {
                                    associationFile.reset();
                                    Writer associationWriter = new OutputStreamWriter(associationFile);                                    
                                    AssociationMapper associationMapper = new AssociationMapper(
                                        element, 
                                        associationWriter, 
                                        getModel(), 
                                        this.format, 
                                        this.packageSuffix,
                                        this.metaData
                                    );
                                    if(mapAssociation(element, associationMapper)){
                                        associationWriter.flush();
                                        this.addToZip(
                                            zip, 
                                            associationFile, 
                                            element, 
                                            associationMapper.associationName, 
                                            "." + this.fileExtension
                                        );
                                    }
                                }
                            } else {
                                SysLog.trace("Ignoring element", element.path());
                            }
                        } else {
                            SysLog.trace("Skipping non-package element", element.path());
                        }
                    }    
                    if(jmi1) {
                        // flush package
                        this.mapEndPackage(
                            currentPackageName,
                            packageMapper
                        );    
                        packageWriter.flush();
                        this.addToZip(
                            zip, 
                            pkgFile, 
                            currentPackage, 
                            AbstractNames.openmdx2PackageName(
                                new StringBuffer(),
                                (String)currentPackage.values("name").get(0)
                            ).toString(),
                            '.' + this.fileExtension
                        );
                    }
                }
            }
        } 
        catch(Exception ex) {
            throw new ServiceException(ex).log();
        }

        SysLog.trace("done");
    }

    /**
     * 
     * @param classDef
     * @param instanceMapper
     * @param inherited 
     * @return
     */
    private Collection<?> getFeatures(
        ModelElement_1_0 classDef,
        InstanceMapper instanceMapper, 
        boolean inherited
    ){
        return inherited || format == Format.JDO2 ?
            instanceMapper.getFeatures(inherited).values() :
                classDef.values("feature");
    }

    /**
     * Test whether the given model is in an archive
     * 
     * @param packageName the qualified model package name
     * 
     * @return <code>true</code> if model is in an archive
     */
    private boolean excludePackage(
        String packageName
    ){
        if(EXCLUDED_PACKAGES.contains(packageName)) {
            return true;
        } else {
            String[] components = packageName.split(":");
            StringBuilder resource = new StringBuilder();
            for(
                    int i = 0;
                    i < components.length;
                    i++
            ){
                resource.append(
                    i == 0 ? "" :
                        i == components.length - 1 ? "/xmi1/" :
                            "/"
                ).append(
                    components[i]
                );

            }
            return artifactIsInArchive(resource.append(".xml"));
        }
    }

    /**
     * Test whether the given JDO metadata is in an archive
     * 
     * @param className
     * 
     * @return <code>true</code> if model is in an archive
     */
    private boolean excludeClass(
        String className
    ){
        if(EXCLUDED_CLASSES.contains(className)) {
            return true;
        } else {
            String[] components = className.split(":");
            StringBuilder resource = new StringBuilder();
            for(
                    int i = 0;
                    i < components.length;
                    i++
            ){
                resource.append(
                    i == 0 ? "" :
                        i == components.length - 1 ? "/jdo2/" :
                            "/"
                ).append(
                    components[i]
                );

            }
            return artifactIsInArchive(resource.append(".jdo"));
        }
    }

    /**
     * Test whether the given artifact is in an archive
     * 
     * @param name
     * 
     * @return <code>true</code> if model is in an archive
     */
    private static boolean artifactIsInArchive(
        CharSequence iri
    ){
        String uri = iri.toString();
        URL url = Thread.currentThread().getContextClassLoader().getResource(uri);
        SysLog.detail(uri, url);
        return url != null && "jar".equals(url.getProtocol());
    }

    //--------------------------------------------------------------------------------
    private final String fileExtension;
    private List<ModelElement_1_0> processedAttributes = null;  
    private final Format format;

    //--------------------------------------------------------------------------------

    /**
     * Packages not to be processed
     */
    private final static Collection<String> EXCLUDED_PACKAGES = Arrays.asList(
        "org:omg:PrimitiveTypes:PrimitiveTypes" // due to a conflict between jmi and jmi1 
    );

    /**
     * Classes not to be processed
     */
    private final static Collection<String> EXCLUDED_CLASSES = Arrays.asList();

    /**
     * 
     */
    private MetaData_1_0 metaData;

    /**
     * 
     */
    private final ObjectRepositoryMetadataPlugin objectRepositoryMetadataPlugin;

}

//--- End of File -----------------------------------------------------------
