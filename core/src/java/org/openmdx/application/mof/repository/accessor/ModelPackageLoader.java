/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Model Importer 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013, OMEX AG, Switzerland
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
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderRequestProcessor;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.application.xml.spi.DataproviderTarget;
import org.openmdx.application.xml.spi.ImportHelper;
import org.openmdx.application.xml.spi.ImportMode;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Resources;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.url.protocol.XRI_2Protocols;
import org.xml.sax.InputSource;

/**
 * Model Package Loader
 */
class ModelPackageLoader implements ModelLoader {

    /**
     * Constructor 
     *
     * @param modelProvider
     * @param qualifiedPackageNames
     * @param xmlValidation 
     * 
     * @throws ServiceException
     */
    ModelPackageLoader(
        Dataprovider_1_0 modelProvider,
        String[] qualifiedPackageNames, 
        boolean xmlValidation
    ) throws ServiceException {
        this.channel = ModelProvider.createChannel(modelProvider == null ? ModelProvider.newInstance() : modelProvider);
        this.importHelper = new ImportHelper(xmlValidation);
        this.qualifiedPackageNames = qualifiedPackageNames;
    }
    
    static private final Path PROVIDER_ROOT_PATH = new Path("xri:@openmdx:org.omg.model1/provider/Mof");
    
    /**
     * Channel to access repository
     */
    private final DataproviderRequestProcessor channel;
    
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
            importModels();
        }
        loadElements(model);
        completeFeatures();
        completeAllFeatures();
        completeAllFeatureWithSubtypes();
    }  
    
    /**
     * Import the models
     * 
     * @throws ServiceException
     */
    private void importModels(
    ) throws ServiceException{
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
     */
    private void beginImport(
    ) throws ServiceException {
        MessageRecord params = (MessageRecord) newMappedRecord(MessageRecord.NAME);
        params.setPath(PROVIDER_ROOT_PATH.getDescendant("segment", "-", "beginImport"));
        params.setBody(null);
        this.channel.addOperationRequest(params);
    }
    
    /**
     * Import Epilog
     * 
     * @throws ServiceException
     */
    private void endImport(
    ) throws ServiceException {
        MessageRecord params = (MessageRecord) newMappedRecord(MessageRecord.NAME);
        params.setPath(PROVIDER_ROOT_PATH.getDescendant("segment", "-", "endImport"));
        params.setBody(null);
        this.channel.addOperationRequest(params);
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String,ModelElement_1_0> newMappedRecord(
        String type
    ) throws ServiceException{
        try {
            return Records.getRecordFactory().createMappedRecord(type);
        } catch (ResourceException exception) {
            throw new ServiceException(exception);
        }
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
    ) throws ServiceException {
        String resourcePath = ModelHelper.toJavaPackageName(qualifiedPackageName, Names.XMI_PACKAGE_SUFFIX).replace('.', '/') + '/' + modelName;
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
    private void importModel(String qualifiedPackageName)
        throws ServiceException {
        final String modelName = qualifiedPackageName.substring(
            qualifiedPackageName.lastIndexOf(':') + 1
        );
        String xmlResource = getResourceURI(qualifiedPackageName, modelName);
        SysLog.detail("loading model " + modelName + " from " + xmlResource);
        importHelper.importObjects(
            new DataproviderTarget(this.channel),
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
            if(classDef.objGetClass().equals(ModelAttributes.CLASS)) {
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
    @SuppressWarnings("unchecked")
    private void completeAllFeatureWithSubtype(
        ModelElement_1_0 classDef
    ) throws ServiceException {
        Map<String,ModelElement_1_0> allFeatureWithSubtype = classDef.objGetMap("allFeatureWithSubtype");
        allFeatureWithSubtype.putAll(classDef.objGetMap("allFeature"));
        for(
            Iterator<?> j = classDef.objGetList("allSubtype").iterator();
            j.hasNext();
        ) {
            ModelElement_1_0 subtype = ModelHelper.findElement(j.next(), modelElements);
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
            if(classDef.objGetClass().equals(ModelAttributes.CLASS)) {
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
    @SuppressWarnings("unchecked")
    private void completeAllFeature(ModelElement_1_0 classDef)
        throws ServiceException {
        Map<String,ModelElement_1_0> allFeature = classDef.objGetMap("allFeature");
        List<Object> allSupertypes = classDef.objGetList("allSupertype");
        for(
            Iterator<?> j = allSupertypes.iterator();
            j.hasNext();
        ) {
            ModelElement_1_0 supertype = ModelHelper.findElement(j.next(), modelElements);
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
            if(element.objGetClass().equals(ModelAttributes.STRUCTURE_TYPE)) {
                completeFeature(element, element.objGetList("content"));
            } else if(element.objGetClass().equals(ModelAttributes.CLASS)) {
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
    @SuppressWarnings("unchecked")
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
            Path contentElementPath = (Path)j.next();
            if(!modelElements.containsKey(contentElementPath.getBase())) {
                throw new ServiceException (
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.NOT_FOUND, 
                    "element is member of container but was not found in model. Probably the model is inconsistent.",
                    new BasicException.Parameter("container", element.jdoGetObjectId()),
                    new BasicException.Parameter("element", contentElementPath.getBase())
                );
            }
            ModelElement_1_0 contentElement = modelElements.get(
                contentElementPath.getBase()
            );
            if(contentElement.objGetClass().equals(ModelAttributes.ATTRIBUTE)) {
                attributes.put(
                    (String)contentElement.objGetValue("name"),
                    contentElement
                );
            } else if(contentElement.objGetClass().equals(ModelAttributes.OPERATION)) {
                operations.put(
                    (String)contentElement.objGetValue("name"),
                    contentElement
                );
            } else if(contentElement.objGetClass().equals(ModelAttributes.REFERENCE)) {
                references.put(
                    (String)contentElement.objGetValue("name"),
                    contentElement
                );
                // add references stored as attribute to the list of attributes
                if(ModelHelper.referenceIsStoredAsAttribute(contentElement.jdoGetObjectId(), modelElements)) {
                    ModelElement_1_0 attribute = new ModelElement_1(contentElement);
                    attribute.objSetValue(
                        SystemAttributes.OBJECT_CLASS,
                        ModelAttributes.ATTRIBUTE
                    );
                    attribute.objSetValue(
                        "isDerived",
                        Boolean.valueOf(
                            ModelHelper.referenceIsDerived(contentElement, modelElements)
                        )
                    );
                    // Maximum length of path
                    attribute.objSetValue(
                        "maxLength",
                        new Integer(1024)
                    );
                    // If reference has a qualifier --> multiplicity 0..n
                    if(!ModelHelper.findElement(contentElement.objGetValue("referencedEnd"), modelElements).objGetList("qualifierName").isEmpty()) {
                        attribute.objSetValue("multiplicity", org.openmdx.base.mof.cci.ModelHelper.UNBOUND);
                    }
                    SysLog.trace("referenceIsStoredAsAttribute", attribute.jdoGetObjectId());
                    attributes.put(
                        (String)attribute.objGetValue("name"),
                        attribute
                    );
                }
            } else if(contentElement.objGetClass().equals(ModelAttributes.STRUCTURE_FIELD)) {
                fields.put(
                    (String)contentElement.objGetValue("name"),
                    contentElement
                );
            }
        }
    }

    /**
     * Load the elements from the provider
     * @param model TODO
     * 
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    private void loadElements(
        Model_1 model
    ) throws ServiceException {
        List<MappedRecord> modelPackages = channel.addFindRequest(
            PROVIDER_ROOT_PATH.getChild("segment"),
            null,
            AttributeSelectors.ALL_ATTRIBUTES,
            0,
            Integer.MAX_VALUE,
            SortOrder.ASCENDING.code()
        );
        for(MappedRecord modelPackage : modelPackages){
            List<MappedRecord> elementDefs = channel.addFindRequest(
                Object_2Facade.getPath(modelPackage).getChild("element"),
                null,
                AttributeSelectors.ALL_ATTRIBUTES,
                null, 
                0,
                Integer.MAX_VALUE,
                SortOrder.ASCENDING.code()
            );
            for(MappedRecord elementDef : elementDefs) {
                modelElements.put(
                    Object_2Facade.getPath(elementDef).getBase(),
                    new ModelElement_1(elementDef, model)
                ); 
            }
        }
        SysLog.detail("Number of elements in cache", Integer.valueOf(modelElements.size()));
    }

}
