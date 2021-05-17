/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Model Importer 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013-2014, OMEX AG, Switzerland
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
package org.openmdx.application.mof.repository.accessor;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;

import org.omg.mof.spi.Names;
import org.openmdx.application.xml.spi.Dataprovider_2Target;
import org.openmdx.application.xml.spi.ImportHelper;
import org.openmdx.application.xml.spi.ImportMode;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.dataprovider.cci.Channel;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.repository.cci.ElementRecord;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Resources;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.xri.XRI_2Protocols;
import org.xml.sax.InputSource;

/**
 * Model Package Loader
 */
public class ModelPackageLoader_2 implements ModelLoader {

    /**
     * Constructor 
     * 
     * @throws ResourceException 
     */
    public ModelPackageLoader_2(
        boolean xmlValidation,
        String[] qualifiedPackageNames,
        boolean metaData
    ) throws ResourceException {
        this(
            ModelProvider_2.newInstance(metaData),
            xmlValidation,
            qualifiedPackageNames
        );
    }

    /**
     * Constructor 
     * 
     * @throws ResourceException 
     */
    ModelPackageLoader_2(
        Port<RestConnection> modelProvider,
        boolean xmlValidation 
    ) throws ResourceException {
        this(modelProvider, xmlValidation, null);
    }
    
    /**
     * Constructor 
     * 
     * @throws ResourceException 
     */
    ModelPackageLoader_2(
        Port<RestConnection> modelProvider,
        boolean xmlValidation,
        String[] qualifiedPackageNames 
    ) throws ResourceException {
        this.channel = newChannel(modelProvider);
        this.importHelper = new ImportHelper(xmlValidation);
        this.qualifiedPackageNames = qualifiedPackageNames;
    }
    
    /**
     * The MOF repository provider
     */
    static private final Path PROVIDER_ROOT_PATH = new Path(
        "xri://@openmdx*org.omg.model1/provider/Mof"
    );
    
    /**
     * Channel to access repository
     */
    private final Channel channel;
    
    /**
     * An import helper instance
     */
    private final ImportHelper importHelper;    

    /**
     * Single dot separated package names
     */
    private final String[] qualifiedPackageNames;

    /**
     * 
     */
    private Map<String,ModelElement_1_0> modelElements;
    
    /* (non-Javadoc)
     * @see org.openmdx.application.mof.repository.accessor.ModelLoader#getModelElements()
     */
    @Override
    public void populateModelElements(
        Model_1 model
    ) throws ServiceException {
        this.modelElements = model.getModelElements();
        if(this.qualifiedPackageNames != null) {
            try {
                importModels();
            } catch (ResourceException e) {
                throw new ServiceException(e);
            }
        }
        try {
            loadElements(model);
        } catch (ResourceException e) {
            throw new ServiceException(e);
        }
        completeFeatures();
        completeAllFeatures();
        completeAllFeatureWithSubtypes();
    }  
    
    /**
     * Import the models
     * 
     * @throws ServiceException
     * @throws ResourceException 
     */
    private void importModels(
    ) throws ResourceException, ServiceException{
        beginImport();
        for(String qualifiedPackageName : this.qualifiedPackageNames){
            importModel(qualifiedPackageName);
        }
        endImport();
    }
    
    /**
     * Import Prolog 
     * 
     * @throws ServiceException
     * @throws ResourceException 
     */
    private void beginImport(
    ) throws ResourceException {
        final MessageRecord params = Records.getRecordFactory().createMappedRecord(MessageRecord.class);
        params.setResourceIdentifier(PROVIDER_ROOT_PATH.getDescendant("segment", "-", "beginImport"));
        params.setBody(null);
        this.channel.addOperationRequest(params);
    }
    
    /**
     * Import Epilog
     * 
     * @throws ServiceException
     */
    private void endImport(
    ) throws ResourceException {
        final MessageRecord params = Records.getRecordFactory().createMappedRecord(MessageRecord.class);
        params.setResourceIdentifier(PROVIDER_ROOT_PATH.getDescendant("segment", "-", "endImport"));
        params.setBody(null);
        this.channel.addOperationRequest(params);
    }
    
    /**
     * Retrieve the model URI<ol>
     * <li>try to locate the WBXML resource URL
     * <li>fall back to the XML resource XRI
     * </ol>
     * 
     * @param qualifiedPackageName
     * @param modelName
     * 
     * @return the model resource URI 
     *   
     * @throws ServiceException
     */
    private static String getResourceURI(
        String qualifiedPackageName, 
        String modelName
    ){
        String resourcePath = ModelHelper_1.toJavaPackageName(qualifiedPackageName, Names.XMI_PACKAGE_SUFFIX).replace('.', '/') + '/' + modelName;
        URL resourceURL = Resources.getResource(resourcePath + ".wbxml");
        return resourceURL == null ? (
            XRI_2Protocols.RESOURCE_PREFIX + resourcePath + ".xml" 
        ) : resourceURL.toExternalForm();  
    }

    /**
     * Import a model file
     * 
     * @param qualifiedPackageName the single colon separated qualified package name
     * 
     * @throws ServiceException
     */
    private void importModel(
        String qualifiedPackageName
    ) throws ServiceException{
        final String modelName = qualifiedPackageName.substring(
            qualifiedPackageName.lastIndexOf(':') + 1
        );
        String xmlResource = getResourceURI(qualifiedPackageName, modelName);
        SysLog.detail("loading model " + modelName + " from " + xmlResource);
        importHelper.importObjects(
            new Dataprovider_2Target(this.channel),
            ImportHelper.asSource(new InputSource(xmlResource)), 
            null, // errorHandler
            ImportMode.CREATE
        );
    }
    
    /**
     * Complete allFeatureWithSubtype
     * 
     * @throws ServiceException
     */
    private void completeAllFeatureWithSubtypes(
    ) throws ServiceException {
        for(ModelElement_1_0 classDef : modelElements.values()) {
            if(classDef.isClassType()) {
                completeAllFeatureWithSubtype(classDef);
            }
        }
    }

    /**
     * Complete allFeatureWithSubtype
     * 
     * @param classDef
     * 
     * @throws ServiceException
     */
    private void completeAllFeatureWithSubtype(
        ModelElement_1_0 classDef
    ) throws ServiceException {
        Map<String,ModelElement_1_0> allFeatureWithSubtype = classDef.objGetMap("allFeatureWithSubtype");
        allFeatureWithSubtype.putAll(classDef.objGetMap("allFeature"));
        for(
            Iterator<?> j = classDef.objGetList("allSubtype").iterator();
            j.hasNext();
        ) {
            ModelElement_1_0 subtype = ModelHelper_1.findElement(j.next(), modelElements);
            allFeatureWithSubtype.putAll(subtype.objGetMap("attribute"));
            allFeatureWithSubtype.putAll(subtype.objGetMap("reference"));        
            allFeatureWithSubtype.putAll(subtype.objGetMap("operation"));        
        }
    }

    /**
     * Complete allFeature
     * 
     * @throws ServiceException
     */
    private void completeAllFeatures(
    ) throws ServiceException {
        for(ModelElement_1_0 classDef : modelElements.values()) {
            if(classDef.isClassType()) {
                completeAllFeature(classDef);
            }
        }
    }

    /**
     * Complete allFeature
     * 
     * @param classDef
     * 
     * @throws ServiceException
     */
    private void completeAllFeature(ModelElement_1_0 classDef)
        throws ServiceException {
        Map<String,ModelElement_1_0> allFeature = classDef.objGetMap("allFeature");
        List<Object> allSupertypes = classDef.objGetList("allSupertype");
        for(
            Iterator<?> j = allSupertypes.iterator();
            j.hasNext();
        ) {
            ModelElement_1_0 supertype = ModelHelper_1.findElement(j.next(), modelElements);
            allFeature.putAll(supertype.objGetMap("attribute"));
            allFeature.putAll(supertype.objGetMap("reference"));
            allFeature.putAll(supertype.objGetMap("operation"));
        }
    }

    /**
     * Complete attributes 'attribute' and 'reference' for class elements
     * and 'field' for structures. This improves performance when accessing
     * the features and fields of classes and structures.
     * 
     * @throws ServiceException
     */
    private void completeFeatures(
    ) throws ServiceException {
        for(ModelElement_1_0 element : modelElements.values()) {
            // feature = content + content of all supertypes. 
            if(element.isStructureType()) {
                completeFeature(element, element.objGetList("content"));
            } else if(element.isClassType()) {
                completeFeature(element, element.objGetList("feature"));
            }
        }
    }

    /**
     * Complete a class' or struct's features
     * 
     * @param element
     * @param content
     * 
     * @throws ServiceException
     */
    private void completeFeature(ModelElement_1_0 element, List<Object> content)
        throws ServiceException {
        Map<String,ModelElement_1_0> attributes = element.objGetMap("attribute");
        Map<String,ModelElement_1_0> references = element.objGetMap("reference");
        Map<String,ModelElement_1_0> fields = element.objGetMap("field");
        Map<String,ModelElement_1_0> operations = element.objGetMap("operation");

        for(
            Iterator<?> j = content.iterator();
            j.hasNext();
        ) {
            Path contentElementId = (Path) j.next();
            String contentElementName = contentElementId.getLastSegment().toClassicRepresentation();
            if(!modelElements.containsKey(contentElementName)) {
                throw new ServiceException (
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.NOT_FOUND, 
                    "element is member of container but was not found in model. Probably the model is inconsistent.",
                    new BasicException.Parameter("container", element.jdoGetObjectId()),
                    new BasicException.Parameter("element", contentElementName)
                );
            }
            ModelElement_1_0 contentElement = modelElements.get(contentElementName);
            if(contentElement.isAttributeType()) {
                attributes.put(
                    contentElement.getName(),
                    contentElement
                );
            } else if(contentElement.isOperationType()) {
                operations.put(
                    contentElement.getName(),
                    contentElement
                );
            } else if(contentElement.isReferenceType()) {
                references.put(
                    contentElement.getName(),
                    contentElement
                );
                // add references stored as attribute to the list of attributes
                final ModelElement_1_0 referenceStoredAsAttribute = ModelHelper_1.getReferenceStoredAsAttribute(contentElement, modelElements);
                if(referenceStoredAsAttribute != null) {
                    SysLog.trace("referenceIsStoredAsAttribute", referenceStoredAsAttribute.getQualifiedName());
                    attributes.put(
                        referenceStoredAsAttribute.getName(),
                        referenceStoredAsAttribute
                    );
                }
            } else if(contentElement.isStructureFieldType()) {
                fields.put(
                    contentElement.getName(),
                    contentElement
                );
            }
        }
    }

    /**
     * Load the elements from the provider
     * 
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    private void loadElements(
        Model_1 model
    ) throws ResourceException, ServiceException {
        List<MappedRecord> modelPackages = channel.addFindRequest(
            PROVIDER_ROOT_PATH.getChild("segment")
        );
        for(MappedRecord modelPackage : modelPackages){
            List<MappedRecord> elementDefs = channel.addFindRequest(
                Object_2Facade.getPath(modelPackage).getChild("element")
            );
            for(MappedRecord elementDef : elementDefs) {
                final Path xri = Object_2Facade.getPath(elementDef);
                final String mofId = xri.getLastSegment().toClassicRepresentation();
                ElementRecord elementRecord = (ElementRecord) Object_2Facade.getValue(elementDef);
                elementRecord.put(SystemAttributes.OBJECT_IDENTITY, xri);
                modelElements.put(
                    mofId,
                    new ModelElement_1(elementRecord, model)
                ); 
            }
        }
        SysLog.detail("Number of elements in cache", Integer.valueOf(modelElements.size()));
    }

    private static Channel newChannel(
        Port<RestConnection> modelProvider
    ) throws ResourceException{
        return new org.openmdx.base.dataprovider.cci.DataproviderRequestProcessor(
            (List<String>)null,
            modelProvider
        );
    }

}
