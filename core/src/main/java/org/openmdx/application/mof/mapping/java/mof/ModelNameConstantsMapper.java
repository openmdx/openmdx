/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Model Name Constant Mapper
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
package org.openmdx.application.mof.mapping.java.mof;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.omg.mof.cci.VisibilityKind;
import org.openmdx.application.mof.mapping.cci.AttributeDef;
import org.openmdx.application.mof.mapping.cci.Mapper_1_1;
import org.openmdx.application.mof.mapping.cci.MetaData_1_0;
import org.openmdx.application.mof.mapping.cci.OperationDef;
import org.openmdx.application.mof.mapping.cci.ReferenceDef;
import org.openmdx.application.mof.mapping.cci.StructuralFeatureDef;
import org.openmdx.application.mof.mapping.java.Format;
import org.openmdx.application.mof.mapping.java.MetaData_2;
import org.openmdx.application.mof.mapping.spi.AbstractMapper_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.log.SysLog;

/**
 * Provides interfaces with model name constants
 */
public class ModelNameConstantsMapper
    extends AbstractMapper_1 
    implements Mapper_1_1 
{

    /**
     * Constructor 
     * 
     * @param markdown {@code true} if annotations use markdown
     */
    public ModelNameConstantsMapper(
    	boolean markdown
    ){
        super(markdown, PACKAGE_SUFFIX);    
    }
    
    /**
     * The suffix for the package to be generated in (without leading dot), e.g. 'cci2'.
     */
    private static final String PACKAGE_SUFFIX = "mof1";
    
    /**
     * The file extension (without leading point), e.g. 'java'.
     */
    private static final String FILE_EXTENSION = "java";
    
    /**
     * Mapping format defined MapperFactory_1.
     */
    private static final Format MAPPING_FORMAT = null;
    
    /**
     * 
     */
    private MetaData_1_0 metaData;

    /**
     * Map Structural Feature
     * 
     * @param classDef
     * @param attributeDef
     * @param instanceIntfMapper
     * 
     * @throws ServiceException
     */
    private void mapAttribute(
        ModelElement_1_0 classDef,
        ModelElement_1_0 attributeDef,
        InstanceFeaturesMapper instanceIntfMapper
    ) throws ServiceException {
        SysLog.trace("attribute", attributeDef.jdoGetObjectId());
        try {      
            if(VisibilityKind.PUBLIC_VIS.equals(attributeDef.objGetValue("visibility"))) {    
                // setter/getter interface only if modelAttribute.container = modelClass. 
                // Otherwise inherit from super interfaces.
                if(attributeDef.objGetValue("container").equals(classDef.jdoGetObjectId())) {
                    instanceIntfMapper.mapAttribute(
                    	new AttributeDef(
                    		attributeDef,
                    		this.model
                    	)
                    );
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
    private void mapReference(
        ModelElement_1_0 classDef,
        ModelElement_1_0 referenceDef,
        InstanceFeaturesMapper instanceIntfMapper
    ) throws ServiceException {
        SysLog.trace("reference", referenceDef.jdoGetObjectId());
        // name only if modelAttribute.container = modelClass. 
        // Otherwise inherit from super interfaces.
        String visibility = (String)referenceDef.objGetValue("visibility");
        try {
            if(VisibilityKind.PUBLIC_VIS.equals(visibility)) {  
                instanceIntfMapper.mapReference(
               		new ReferenceDef(
					    referenceDef,
					    this.model
					)
                );
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
    private void mapOperation(
        ModelElement_1_0 classDef,
        ModelElement_1_0 operationDef,
        InstanceFeaturesMapper instanceIntfMapper
    ) throws ServiceException {
        SysLog.trace("operation", operationDef.jdoGetObjectId());
        boolean isMemberOfClass = operationDef.objGetValue("container").equals(classDef.jdoGetObjectId());
        if(VisibilityKind.PUBLIC_VIS.equals(operationDef.objGetValue("visibility"))) {
            try {
                if(isMemberOfClass) {
                    instanceIntfMapper.mapOperation(
                    	new OperationDef(
						    operationDef,
						    this.model
						)
                    );
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
    private void mapBeginClass(
        ModelElement_1_0 classDef,
        ClassMapper classIntfMapper,
        InstanceFeaturesMapper instanceIntfMapper
    ) throws ServiceException {
        SysLog.trace("class", classDef.jdoGetObjectId());
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
    private void mapEndClass(
        ModelElement_1_0 classDef,
        ClassMapper classIntfMapper,
        InstanceFeaturesMapper instanceIntfMapper
    ) throws ServiceException {
        // add additional template references to context
        List<StructuralFeatureDef> structuralFeatures = new ArrayList<StructuralFeatureDef>();
        List<OperationDef> operations = new ArrayList<OperationDef>();
        for(
            Iterator<?> i = classDef.objGetList("feature").iterator();
            i.hasNext();
        ) {
            ModelElement_1_0 feature = this.model.getElement(i.next());
            if(VisibilityKind.PUBLIC_VIS.equals(feature.objGetValue("visibility"))) {
                if(feature.isAttributeType()) {
                    structuralFeatures.add(
                        new AttributeDef(
                            feature, 
                            this.model
                        )
                    );
                } else if(feature.isReferenceType()) {
                    ModelElement_1_0 referencedEnd = this.model.getElement(
                        feature.objGetValue("referencedEnd")
                    );
                    List<?> qualifierTypes = referencedEnd.objGetList("qualifierType");
                    // skip references for which a qualifier exists and the qualifier is
                    // not a primitive type
                    if(
                        qualifierTypes.isEmpty() ||
                        this.model.isPrimitiveType(qualifierTypes.get(0))
                    ) {
                        structuralFeatures.add(
                            new ReferenceDef(
                                feature,
                                model
                            )
                        );
                    }
                } else if(feature.isOperationType()) {
                    operations.add(
                        new OperationDef(
                            feature,
                            this.model
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
    private void mapBeginStruct(
        ModelElement_1_0 structDef,
        StructFeaturesMapper structIntfMapper
    ) throws ServiceException {
        SysLog.trace("struct", structDef.jdoGetObjectId());
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
    private void mapStructField(
        ModelElement_1_0 classDef,
        ModelElement_1_0 structureFieldDef,
        StructFeaturesMapper structIntfMapper
    ) throws ServiceException {
        SysLog.trace("structure field", structureFieldDef.jdoGetObjectId());
        // required for ...Class.create...() operations
        try {
            AttributeDef mStructureFieldDef = new AttributeDef(
                structureFieldDef,
                this.model
            );
            // getter interface only if modelAttribute.container = modelClass. 
            // Otherwise inherit from super interfaces.
            if(structureFieldDef.objGetValue("container").equals(classDef.jdoGetObjectId())) {
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
    private void jmiEndStruct(
        StructFeaturesMapper structIntfMapper
    ) throws ServiceException {
        try {
            structIntfMapper.mapEnd();  
        } catch(Exception ex) {
            throw new ServiceException(ex).log();
        }
    }

	public void externalize(
		String qualifiedPackageName, 
		Model_1_0 model,
		ZipOutputStream zip, 
		String openmdxjdoMetadataDirectory
	) throws ServiceException {
        SysLog.trace("starting...");
        this.model = model;
        this.metaData = new MetaData_2(openmdxjdoMetadataDirectory);
        List<ModelElement_1_0> packagesToExport = this.getMatchingPackages(qualifiedPackageName);
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
            for(ModelElement_1_0 currentPackage : packagesToExport) {
                String currentPackageName = (String)currentPackage.objGetValue("qualifiedName");
                // process packageContent
                for(ModelElement_1_0 element : this.model.getContent()) {  
                    SysLog.trace("processing package element", element.jdoGetObjectId());
                    // org:omg:model1:Class
                    if(
                        this.model.isClassType(element) &&
                        this.model.isLocal(element, currentPackageName)
                    ) {
                        SysLog.trace("processing class " + element.jdoGetObjectId());
                        classFile.reset();
                        instanceFeaturesFile.reset();
                        ClassMapper classIntfMapper = new ClassMapper(
                            element, 
                            classWriter, 
                            this.model, 
                            MAPPING_FORMAT, 
                            this.packageSuffix,
                            this.metaData, markdown
                        );
                        InstanceFeaturesMapper instanceIntfMapper = new InstanceFeaturesMapper(
                            element, 
                            instanceFeaturesWriter, 
                            this.model, 
                            MAPPING_FORMAT, 
                            this.packageSuffix,
                            this.metaData, markdown
                        );
                        this.mapBeginClass(
                            element,
                            classIntfMapper,
                            instanceIntfMapper
                        );
                        // get class features
                        for(
                            Iterator<?> j = element.objGetList("feature").iterator();
                            j.hasNext();
                        ) {
                            ModelElement_1_0 feature = this.model.getElement(j.next());
                            SysLog.trace("processing class feature", feature.jdoGetObjectId());
                            if(feature.isAttributeType()) {
                                this.mapAttribute(
                                    element,
                                    feature,
                                    instanceIntfMapper
                                );
                            } else if(feature.isReferenceType()) {
                                this.mapReference(
                                    element,
                                    feature,
                                    instanceIntfMapper
                                );
                            } else if(feature.isOperationType()) {
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
                        this.addToZip(zip, instanceFeaturesFile, element, FeaturesMapper.FEATURES_INTERFACE_SUFFIX + '.' + FILE_EXTENSION);
                        classWriter.flush();
                        this.addToZip(zip, classFile, element, "Class." + FILE_EXTENSION);
                    } else if(
                        // org:omg:model1:StructureType
                        this.model.isStructureType(element) &&
                        this.model.isLocal(element, currentPackageName)
                    ) {
                        SysLog.trace("processing structure type", element.jdoGetObjectId());
                        // only generate for structs which are content of the modelPackage. 
                        // Do not generate for imported model elements
                        if(this.model.isLocal(element, currentPackageName)) {
                            structFeaturesFile.reset();
                            StructFeaturesMapper structFeaturesMapper = new StructFeaturesMapper(
                                element, 
                                structFeaturesWriter, 
                                this.model, 
                                MAPPING_FORMAT, 
                                this.packageSuffix,
                                this.metaData, markdown
                            );
                            this.mapBeginStruct(
                                element,
                                structFeaturesMapper
                            );
                            // StructureFields
                            for(
                                Iterator<?> j = element.objGetList("content").iterator();
                                j.hasNext();
                            ) {
                                ModelElement_1_0 feature = this.model.getElement(j.next());
                                SysLog.trace("processing structure field", feature.jdoGetObjectId());
                                if(feature.isStructureFieldType()) {
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
                            this.addToZip(
                            	zip, 
                            	structFeaturesFile, element, FeaturesMapper.FEATURES_INTERFACE_SUFFIX + '.' + FILE_EXTENSION);
                        }
                    } else if(element.isExceptionType() &&
                        this.model.isLocal(element, currentPackageName)
                    ) {   
                        SysLog.trace("processing exception " + element.jdoGetObjectId());
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
     * Externalizes given packageContent and stores result in the jar output
     * stream.
     * 
     * @param qualifiedPackageName
     *            fully qualified, ':' separated name of a model package. '%' as
     *            last character is allowed as wildcard, e.g. 'org:%' exports
     *            all models contained in package 'org', 'org:openmdx:%' exports
     *            all models contained in package 'org:openmdx'. All models are
     *            written to os.
     * @param model
     * @param os
     * 
     * @throws ServiceException
     */
    public void externalize(
        String qualifiedPackageName,
        Model_1_0 model,
        ZipOutputStream os
    ) throws ServiceException {
        externalize(
            qualifiedPackageName, 
            model, 
            os, 
            null // openmdxjdoMetadataDirectory
        );
    }

}
