/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Constants_1.java,v 1.3 2007/05/11 14:12:40 hburger Exp $
 * Description: Constants Generator
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/05/11 14:12:40 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package ch.css.model1.mapping.java;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.omg.model1.code.VisibilityKind;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.code.ModelAttributes;
import org.openmdx.model1.mapping.AbstractMapper_1;
import org.openmdx.model1.mapping.AttributeDef;
import org.openmdx.model1.mapping.OperationDef;
import org.openmdx.model1.mapping.ReferenceDef;

/**
 * Mapper_1
 */
public class Constants_1
    extends AbstractMapper_1 
{

    /**
     * Constructor 
     */
    public Constants_1(
    ){
        this(
            "css1", // mappingFormat
            "css1", // packageSuffix
            "java" // fileExtension
        );
    }
    
    /**
     * Constructor.
     * 
     * @param mappingFormat mapping format defined MapperFactory_1.
     * @param packageSuffix The suffix for the package to be generated in (without leading dot), e.g. 'cci'.
     * @param fileExtension The file extension (without leading point), e.g. 'java'.
     */
    protected Constants_1(
        String mappingFormat,
        String packageSuffix,
        String fileExtension
    ) {
        super(
            packageSuffix
        );    
        this.mappingFormat = mappingFormat;
        this.fileExtension = fileExtension;
    }

    /**
     * Map Structural Feature
     * 
     * @param classDef
     * @param attributeDef
     * @param instanceIntfMapper
     * 
     * @throws ServiceException
     */
    void mapAttribute(
        ModelElement_1_0 classDef,
        ModelElement_1_0 attributeDef,
        InstanceFeaturesMapper instanceIntfMapper
    ) throws ServiceException {
        SysLog.trace("attribute", attributeDef.path());
        // required for ...Class.create...() operations
        try {      
            if(VisibilityKind.PUBLIC_VIS.equals(attributeDef.values("visibility").get(0))) {    
                AttributeDef mAttributeDef = new AttributeDef(
                    attributeDef,
                    this.model
                );       
                // setter/getter interface only if modelAttribute.container = modelClass. 
                // Otherwise inherit from super interfaces.
                if(attributeDef.values("container").get(0).equals(classDef.path())) {
                    instanceIntfMapper.mapAttribute(mAttributeDef);
                }
            }
        } catch(Exception ex) {
            throw new ServiceException(ex);
        }
    }

    /**
     * Map Reference Feature
     * 
     * @param classDef
     * @param referenceDef
     * @param instanceIntfMapper
     * 
     * @throws ServiceException
     */
    public void mapReference(
        ModelElement_1_0 classDef,
        ModelElement_1_0 referenceDef,
        InstanceFeaturesMapper instanceIntfMapper
    ) throws ServiceException {
        SysLog.trace("reference", referenceDef.path());
        // name only if modelAttribute.container = modelClass. 
        // Otherwise inherit from super interfaces.
        String visibility = (String)referenceDef.values("visibility").get(0);
        try {
            if(VisibilityKind.PUBLIC_VIS.equals(visibility)) {  
                ReferenceDef mReferenceDef = new ReferenceDef(
                    referenceDef,
                    this.model,
                    true // openmdx1
                );
                instanceIntfMapper.mapReference(mReferenceDef);
            }
        } catch(Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    /**
     * Map Behavioural Feature
     * 
     * @param classDef
     * @param operationDef
     * @param instanceIntfMapper
     * 
     * @throws ServiceException
     */
    void mapOperation(
        ModelElement_1_0 classDef,
        ModelElement_1_0 operationDef,
        InstanceFeaturesMapper instanceIntfMapper
    ) throws ServiceException {
        SysLog.trace("operation", operationDef.path());
        boolean isMemberOfClass = operationDef.values("container").get(0).equals(classDef.path());
        if(VisibilityKind.PUBLIC_VIS.equals(operationDef.values("visibility").get(0))) {
            try {
                OperationDef mOperationDef = new OperationDef(
                    operationDef,
                    this.model,
                    true //openmdx1
                );
                if(isMemberOfClass) {
                    instanceIntfMapper.mapOperation(mOperationDef);
                }
            } catch(Exception ex) {
                throw new ServiceException(ex).log();
            }
        }
    }

    /**
     * Map Class Constants Begin
     * 
     * @param classDef
     * @param classIntfMapper
     * @param instanceIntfMapper
     * @throws ServiceException
     */
    void mapBeginClass(
        ModelElement_1_0 classDef,
        ClassMapper classIntfMapper,
        InstanceFeaturesMapper instanceIntfMapper
    ) throws ServiceException {
        SysLog.trace("class", classDef.path());
        try {
            classIntfMapper.mapBegin();
            // object interface
            instanceIntfMapper.mapBegin();
        } catch(Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    /**
     * Map Class Cnstants ENd
     * 
     * @param classDef
     * @param classIntfMapper
     * @param instanceIntfMapper
     * 
     * @throws ServiceException
     */
    void mapEndClass(
        ModelElement_1_0 classDef,
        ClassMapper classIntfMapper,
        InstanceFeaturesMapper instanceIntfMapper
    ) throws ServiceException {
        // add additional template references to context
        List structuralFeatures = new ArrayList();
        List operations = new ArrayList();
        for(
            Iterator i = classDef.values("feature").iterator();
            i.hasNext();
        ) {
            ModelElement_1_0 feature = this.model.getElement(i.next());
            if(VisibilityKind.PUBLIC_VIS.equals(feature.values("visibility").get(0))) {
                if(
                    feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.ATTRIBUTE)
                ) {
                    structuralFeatures.add(
                        new AttributeDef(
                            feature, 
                            this.model
                        )
                    );
                } else if(
                    feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.REFERENCE)
                ) {
                    ModelElement_1_0 referencedEnd = this.model.getElement(
                        feature.values("referencedEnd").get(0)
                    );
                    List qualifierTypes = referencedEnd.values("qualifierType");
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
                                true // openmdx1
                            )
                        );
                    }
                } else if(
                    feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.OPERATION)
                ) {
                    operations.add(
                        new OperationDef(
                            feature,
                            this.model,
                            true //openmdx1
                        )
                    );
                }
            }
        }
        classIntfMapper.mapEnd();
        instanceIntfMapper.mapEnd();
    }

    /**
     * Map Struct Constants Begin
     * 
     * @param structDef
     * @param structIntfMapper
     * 
     * @throws ServiceException
     */
    void mapBeginStruct(
        ModelElement_1_0 structDef,
        StructFeaturesMapper structIntfMapper
    ) throws ServiceException {
        SysLog.trace("struct", structDef.path());
        try {
            structIntfMapper.mapBegin();  
        } catch(Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    /**
     * Map Struct Constants End
     * 
     * @param classDef
     * @param structureFieldDef
     * @param structIntfMapper
     * 
     * @throws ServiceException
     */
    void mapStructField(
        ModelElement_1_0 classDef,
        ModelElement_1_0 structureFieldDef,
        StructFeaturesMapper structIntfMapper
    ) throws ServiceException {
        SysLog.trace("structure field", structureFieldDef.path());
        // required for ...Class.create...() operations
        try {
            AttributeDef mStructureFieldDef = new AttributeDef(
                structureFieldDef,
                this.model
            );
            // getter interface only if modelAttribute.container = modelClass. 
            // Otherwise inherit from super interfaces.
            if(structureFieldDef.values("container").get(0).equals(classDef.path())) {
                structIntfMapper.mapField(mStructureFieldDef);
            }
        }  catch(Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    /**
     * Map Struct Constants End
     * 
     * @param structIntfMapper
     * @throws ServiceException
     */
    void jmiEndStruct(
        StructFeaturesMapper structIntfMapper
    ) throws ServiceException {
        try {
            structIntfMapper.mapEnd();  
        } catch(Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

    /**
     * 
     */
    public void externalize(
        String qualifiedPackageName,
        org.openmdx.model1.accessor.basic.cci.Model_1_0 model,
        ZipOutputStream zip
    ) throws ServiceException {
        SysLog.trace("starting...");
        this.model = (Model_1_3)model;
        List packagesToExport = this.getMatchingPackages(qualifiedPackageName);
        SysLog.detail("exporting packages", packagesToExport);
        try {
            // allocate streams one time
            ByteArrayOutputStream classFile = new ByteArrayOutputStream();
            ByteArrayOutputStream instanceFeaturesFile = new ByteArrayOutputStream();
            ByteArrayOutputStream structFeaturesFile = new ByteArrayOutputStream();
            Writer classWriter = new OutputStreamWriter(classFile);
            Writer instanceFeaturesWriter = new OutputStreamWriter(instanceFeaturesFile);
            Writer structFeaturesWriter = new OutputStreamWriter(structFeaturesFile);
            // export matching packages
            for(
                Iterator pkgs = packagesToExport.iterator(); 
                pkgs.hasNext();
            ) {
                ModelElement_1_0 currentPackage = (ModelElement_1_0)pkgs.next();
                String currentPackageName = (String)currentPackage.values("qualifiedName").get(0);
                // process packageContent
                for(
                    Iterator i = this.model.getContent().iterator(); 
                    i.hasNext();
                ) {  
                    ModelElement_1_0 element = (ModelElement_1_0)i.next();
                    SysLog.trace("processing package element", element.path());
                    // org:omg:model1:Class
                    if(
                        this.model.isClassType(element) &&
                        this.model.isLocal(element, currentPackageName)
                    ) {
                        SysLog.trace("processing class " + element.path());
                        classFile.reset();
                        instanceFeaturesFile.reset();
                        ClassMapper classIntfMapper = new ClassMapper(
                            element, 
                            classWriter, 
                            this.model, 
                            this.mappingFormat, packageSuffix
                        );
                        InstanceFeaturesMapper instanceIntfMapper = new InstanceFeaturesMapper(
                            element, 
                            instanceFeaturesWriter, 
                            this.model, 
                            this.mappingFormat, packageSuffix
                        );
                        this.mapBeginClass(
                            element,
                            classIntfMapper,
                            instanceIntfMapper
                        );
                        // get class features
                        for(
                            Iterator j = element.values("feature").iterator();
                            j.hasNext();
                        ) {
                            ModelElement_1_0 feature = this.model.getElement(j.next());
                            SysLog.trace("processing class feature", feature.path());
                            if(feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.ATTRIBUTE)) {
                                this.mapAttribute(
                                    element,
                                    feature,
                                    instanceIntfMapper
                                );
                            } else if(feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.REFERENCE)) {
                                this.mapReference(
                                    element,
                                    feature,
                                    instanceIntfMapper
                                );
                            } else if(feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.OPERATION)) {
                                this.mapOperation(
                                    element,
                                    feature,
                                    instanceIntfMapper
                                );
                            }
                        }
                        this.mapEndClass(
                            element,
                            classIntfMapper,
                            instanceIntfMapper
                        );
                        instanceFeaturesWriter.flush();
                        this.addToZip(zip, instanceFeaturesFile, element, FeaturesMapper.FEATURES_INTERFACCE_SUFFIX + '.' + this.fileExtension);
                        classWriter.flush();
                        this.addToZip(zip, classFile, element, "Class." + this.fileExtension);
                    } else if(
                        // org:omg:model1:StructureType
                        this.model.isStructureType(element) &&
                        this.model.isLocal(element, currentPackageName)
                    ) {
                        SysLog.trace("processing structure type", element.path());
                        // only generate for structs which are content of the modelPackage. 
                        // Do not generate for imported model elements
                        if(this.model.isLocal(element, currentPackageName)) {
                            structFeaturesFile.reset();
                            StructFeaturesMapper structFeaturesMapper = new StructFeaturesMapper(
                                element, 
                                structFeaturesWriter, 
                                this.model, 
                                this.mappingFormat, 
                                packageSuffix
                            );
                            this.mapBeginStruct(
                                element,
                                structFeaturesMapper
                            );
                            // StructureFields
                            for(
                                Iterator j = element.values("content").iterator();
                                j.hasNext();
                            ) {
                                ModelElement_1_0 feature = this.model.getElement(j.next());
                                SysLog.trace("processing structure field", feature.path());
                                if(feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.STRUCTURE_FIELD)) {
                                    this.mapStructField(
                                        element,
                                        feature,
                                        structFeaturesMapper
                                    );
                                }
                            }                            
                            this.jmiEndStruct(
                                structFeaturesMapper
                            );
                            structFeaturesWriter.flush();
                            this.addToZip(zip, structFeaturesFile, element, FeaturesMapper.FEATURES_INTERFACCE_SUFFIX + '.' + this.fileExtension);
                        }
                    } else if(
                        // org:omg:model1:Exception
                        element.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.EXCEPTION) &&
                        this.model.isLocal(element, currentPackageName)
                    ) {   
                        SysLog.trace("processing exception " + element.path());
                        // only generate for structs which are content of the modelPackage. 
                        // Do not generate for imported model elements
                    }
                }   
            }
        } catch(Exception ex) {
            throw new ServiceException(ex).log();
        }    
        SysLog.trace("done");
    }
  
    /**
     * 
     */
    private final String fileExtension;
    
    /**
     * 
     */
    private final String mappingFormat;
    
}
