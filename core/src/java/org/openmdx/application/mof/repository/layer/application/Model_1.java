/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Model_1.java,v 1.7 2009/06/14 00:03:43 wfro Exp $
 * Description: model1 application plugin
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/14 00:03:43 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 */

/**
 * @author wfro
 */
package org.openmdx.application.mof.repository.layer.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.zip.ZipOutputStream;

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderOperations;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.DataproviderRequestContexts;
import org.openmdx.application.dataprovider.cci.RequestCollection;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.application.dataprovider.spi.Layer_1_0;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.application.mof.cci.ModelExceptions;
import org.openmdx.application.mof.mapping.cci.Mapper_1_0;
import org.openmdx.application.mof.mapping.cci.Mapper_1_1;
import org.openmdx.application.mof.mapping.cci.MappingTypes;
import org.openmdx.application.mof.mapping.spi.MapperFactory_1;
import org.openmdx.application.mof.repository.utils.ModelUtils;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.Directions;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;
import org.openmdx.base.rest.spi.ObjectHolder_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

//---------------------------------------------------------------------------
@SuppressWarnings("unchecked")
public class Model_1 
extends Layer_1 {

    private static final List<String> STANDARD_FORMAT = Collections.unmodifiableList(
        Arrays.asList(
            MappingTypes.XMI1, 
            MappingTypes.UML_OPENMDX_1,
            MappingTypes.UML2_OPENMDX_1,
            MappingTypes.TOGETHER_OPENMDX_1
        )
    );

    class PathComponent {

        //-----------------------------------------------------------------------
        public PathComponent(
            String component
        ) {
            components = new ArrayList();
            StringTokenizer tokenizer = new StringTokenizer(component, ":");
            while(tokenizer.hasMoreElements()) {
                components.add(tokenizer.nextToken());
            }
        }

        //-----------------------------------------------------------------------
        public String get(
            int pos
        ) {
            return (String)components.get(pos);
        }

        //-----------------------------------------------------------------------
        public int size() {
            return components.size();
        }

        //-----------------------------------------------------------------------
        public String toString(
            int fromPos,
            int toPos
        ) {
            StringBuffer s = new StringBuffer((String)components.get(fromPos));
            for(int i = fromPos+1; i < toPos; i++) {
                s.append(":" + components.get(i));
            }
            return s.toString();
        }

        private ArrayList components = null;

    }

    //---------------------------------------------------------------------------
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws ServiceException {

        super.activate(
            id, 
            configuration, 
            delegation
        );
        this.openmdxjdoMetadataDirectory = configuration.getFirstValue(
            LayerConfigurationEntries.OPENMDXJDO_METADATA_DIRECTORY
        );
    }

    //---------------------------------------------------------------------------
    private String getReferenceName(
        DataproviderRequest request
    ) throws ServiceException {
        Path path = request.path();    
        return
        path.size() % 2 == 0 ?
            (String)path.get(path.size()-1) :
                (String)path.get(path.size()-2);
    }

    //---------------------------------------------------------------------------
    private void completeElements(
        ServiceHeader header,
        Map elements
    ) throws ServiceException { 

        long startedAt = System.currentTimeMillis();    

        Map<Path,MappedRecord> classifiers = new HashMap<Path,MappedRecord>();
        for(
            Iterator<MappedRecord> i = elements.values().iterator();
            i.hasNext();
        ) {
            MappedRecord element = i.next();
            ObjectHolder_2Facade elementFacade;
            try {
                elementFacade = ObjectHolder_2Facade.newInstance(element);
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }
            // collect classifier
            if(elementFacade.attributeValues(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CLASSIFIER)) {
                classifiers.put(
                    elementFacade.getPath(),
                    element
                );
            }
            // qualified name
            elementFacade.clearAttributeValues("qualifiedName").add(
                elementFacade.getPath().getBase()
            );
        }

        // allSupertype
        for(
            Iterator<MappedRecord> i = elements.values().iterator();
            i.hasNext();
        ) { 
            MappedRecord classifier = i.next();
            ObjectHolder_2Facade classifierFacade;
            try {
                classifierFacade = ObjectHolder_2Facade.newInstance(classifier);
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }            
            if(classifierFacade.attributeValues(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CLASSIFIER)) {
                classifierFacade.clearAttributeValues("allSupertype").addAll(
                    this.getAllSupertype(
                        classifier,
                        elements
                    )
                );
            }
        }

        // subtype
        this.setSubtype(
            classifiers
        );

        // allSubtype
        for(
            Iterator<MappedRecord> i = elements.values().iterator();
            i.hasNext();
        ) {
            MappedRecord classifier = i.next();
            ObjectHolder_2Facade classifierFacade;
            try {
                classifierFacade = ObjectHolder_2Facade.newInstance(classifier);
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }                        
            if(classifierFacade.attributeValues(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CLASSIFIER)) {
                classifierFacade.clearAttributeValues("allSubtype").addAll(
                    this.getAllSubtype(
                        classifier,
                        elements
                    )
                );
            }
        }

        // content
        this.setContent(
            elements
        );

        // feature
        for(
            Iterator<MappedRecord> i = elements.values().iterator();
            i.hasNext();
        ) {
            MappedRecord classifier = i.next();
            ObjectHolder_2Facade classifierFacade;
            try {
                classifierFacade = ObjectHolder_2Facade.newInstance(classifier);
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }                                    
            if(classifierFacade.attributeValues(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CLASSIFIER)) {      
                classifierFacade.clearAttributeValues("feature").addAll(
                    this.getFeatures(
                        header,
                        classifier,
                        elements
                    )
                );
            }
        }

        // compositeReference
        this.setCompositeReference(
            classifiers,
            elements
        );

        long executionTime = System.currentTimeMillis() - startedAt;
        SysLog.detail("recalculated " + elements.size() + " in " + executionTime + " ms");
    }

    //---------------------------------------------------------------------------
    private List<MappedRecord> getNamespaceContent(
        ServiceHeader header,
        MappedRecord namespace
    ) throws ServiceException {
        RequestCollection channel = new RequestCollection(
            header,
            this.getDelegation()
        );
        return channel.addFindRequest(
            ObjectHolder_2Facade.getPath(namespace).getParent(),
            new FilterProperty[]{
                new FilterProperty(
                    Quantors.THERE_EXISTS,
                    "container",
                    FilterOperators.IS_IN,
                    ObjectHolder_2Facade.getPath(namespace)
                )        
            },
            AttributeSelectors.ALL_ATTRIBUTES,
            null,
            0,
            Integer.MAX_VALUE,
            Directions.ASCENDING
        );
    }

    //---------------------------------------------------------------------------
    private List getNamespaceContentAsPaths(
        ServiceHeader header,
        MappedRecord namespace
    ) throws ServiceException {
        List content = this.getNamespaceContent(
            header,
            namespace
        );
        List contentAsPaths = new ArrayList();
        for(
            Iterator<MappedRecord> i = content.iterator();
            i.hasNext();
        ) {
            contentAsPaths.add(
                ObjectHolder_2Facade.getPath(i.next())
            );
        }
        return contentAsPaths;
    }

    //---------------------------------------------------------------------------
    /**
     * Gets all direct and indirect supertypes for a given Classifier
     */
    private Set getAllSupertype(
        MappedRecord classifier,
        Map<Path,MappedRecord> elements
    ) throws ServiceException {
        Set<Path> allSupertype = new TreeSet<Path>();
        try {
            for(
                Iterator i = ObjectHolder_2Facade.newInstance(classifier).attributeValues("supertype").iterator();
                i.hasNext();
            ) {
                Path supertypePath = (Path)i.next();
                MappedRecord supertype = elements.get(supertypePath);
                if(supertype == null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE, 
                        "type not found in set of loaded models (HINT: probably not configured as 'modelPackage')",
                        new BasicException.Parameter("classifier", ObjectHolder_2Facade.getPath(classifier)),
                        new BasicException.Parameter("supertype", supertypePath)
                    );
                }
                allSupertype.addAll(
                    this.getAllSupertype(
                        supertype,
                        elements
                    )
                );
            }
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        allSupertype.add(
            ObjectHolder_2Facade.getPath(classifier)
        );
        return allSupertype;
    }

    //---------------------------------------------------------------------------
    /**
     * Set feature 'subtype' of classifiers
     */
    private void setSubtype(
        Map<Path,MappedRecord> classifiers
    ) throws ServiceException {    
        // clear feature subtype
        for(
            Iterator<MappedRecord> i = classifiers.values().iterator();
            i.hasNext();
        ) {
            MappedRecord classifier = i.next();
            try {
                ObjectHolder_2Facade.newInstance(classifier).clearAttributeValues("subtype");
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }
        }
        // recalc subtype
        for(
            Iterator<MappedRecord> i = classifiers.values().iterator();
            i.hasNext();
        ) {
            MappedRecord classifier = i.next();
            ObjectHolder_2Facade classifierFacade;
            try {
                classifierFacade = ObjectHolder_2Facade.newInstance(classifier);
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }
            try {
                for(
                    Iterator j = ObjectHolder_2Facade.newInstance(classifier).attributeValues("supertype").iterator();
                    j.hasNext();
                ) {
                    Object next = j.next();
                    MappedRecord supertype = classifiers.get(next);
                    if(supertype == null) {
                        SysLog.error("supertype " + next + " of classifier " + ObjectHolder_2Facade.getPath(classifier) + " not found in repository");
                    }
                    else {
                        ObjectHolder_2Facade superTypeFacade = ObjectHolder_2Facade.newInstance(supertype);
                        if(!superTypeFacade.attributeValues("subtype").contains(classifierFacade.getPath())) {
                            superTypeFacade.attributeValues("subtype").add(
                                classifierFacade.getPath()
                            );
                        }
                    }
                }
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }
            if(!classifierFacade.attributeValues("subtype").contains(classifierFacade.getPath())) {
                classifierFacade.attributeValues("subtype").add(
                    classifierFacade.getPath()
                );
            }
        }
    }

    //---------------------------------------------------------------------------
    /**
     * Set feature 'content' of namespaces
     */
    private void setContent(
        Map<Path,MappedRecord> elements
    ) throws ServiceException {    
        // clear feature subtype
        for(
            Iterator<MappedRecord> i = elements.values().iterator();
            i.hasNext();
        ) {
            MappedRecord element = i.next();
            try {
                ObjectHolder_2Facade.newInstance(element).clearAttributeValues("content");
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }
        }
        // recalc content
        for(
            Iterator<MappedRecord> i = elements.values().iterator();
            i.hasNext();
        ) {
            MappedRecord content = i.next();
            ObjectHolder_2Facade contentFacade;
            try {
                contentFacade = ObjectHolder_2Facade.newInstance(content);
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }
            if(contentFacade.getAttributeValues("container") != null) {
                MappedRecord container = elements.get(
                    contentFacade.attributeValue("container")
                );
                if(container == null) {
                    SysLog.error("container " + contentFacade.attributeValue("container") + " of element " + contentFacade.getPath() + " not found in repository");
                }
                else { 
                    ObjectHolder_2Facade containerFacade;
                    try {
                        containerFacade = ObjectHolder_2Facade.newInstance(container);
                    } 
                    catch (ResourceException e) {
                        throw new ServiceException(e);
                    }
                    if(!containerFacade.attributeValues("content").contains(contentFacade.getPath())) {
                        containerFacade.attributeValues("content").add(
                            contentFacade.getPath()
                        );
                    }
                }
            }      
        } 

        // sort content by path
        // TODO: sort content by order as defined in model
        for(
            Iterator<MappedRecord> i = elements.values().iterator();
            i.hasNext();
        ) {
            MappedRecord container = i.next();
            ObjectHolder_2Facade containerFacade;
            try {
                containerFacade = ObjectHolder_2Facade.newInstance(container);
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }
            if(containerFacade.getAttributeValues("content") != null) {
                Set content = new TreeSet(
                    containerFacade.attributeValues("content")
                );
                containerFacade.clearAttributeValues("content").addAll(
                    content
                );
            }
        }

    }

    //---------------------------------------------------------------------------
    /**
     * When object is of type ModelClass completes the features allOperations,
     * allAttributes and allReferences. Precondition: allSupertype and content
     * must be set.
     */
    private Set getFeatures(
        ServiceHeader header,
        MappedRecord classifier,
        Map<Path,MappedRecord> elements
    ) throws ServiceException {
        Set<Path> features = new TreeSet<Path>();    
        try {
            for(
                Iterator i = ObjectHolder_2Facade.newInstance(classifier).attributeValues("allSupertype").iterator();
                i.hasNext();
            ) {
                MappedRecord supertype = elements.get(i.next());
                for(
                    Iterator j = ObjectHolder_2Facade.newInstance(supertype).attributeValues("content").iterator();
                    j.hasNext();
                ) {
                    MappedRecord feature = elements.get(j.next());
                    if(ObjectHolder_2Facade.newInstance(feature).attributeValues(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.FEATURE)) {
                        features.add(
                            ObjectHolder_2Facade.getPath(feature)
                        );
                    }
                }
            }
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        return features;
    }

    //-------------------------------------------------------------------------
    /** 
     * Sets the 'allSubtype' attribute of modelElement. Requires that the
     * 'subtype' attribute is available.
     */
    private Set getAllSubtype(
        MappedRecord classifier,
        Map<Path,MappedRecord> elements
    ) throws ServiceException {
        Set<Path> allSubtypes = new TreeSet<Path>();
        try {
            for(
                Iterator i = ObjectHolder_2Facade.newInstance(classifier).attributeValues("subtype").iterator();
                i.hasNext();
            ) {
                MappedRecord subtype = elements.get(i.next());
                // classifier is member of its subtypes
                if(subtype != classifier) {
                    allSubtypes.addAll(
                        this.getAllSubtype(
                            subtype,
                            elements
                        )
                    );
                }
            }
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        allSubtypes.add(
            ObjectHolder_2Facade.getPath(classifier)
        ); 
        return allSubtypes;
    }

    //---------------------------------------------------------------------------
    /**
     * set feature 'compositeReference' of classifiers
     */
    private void setCompositeReference(
        Map<Path,MappedRecord> classifiers,
        Map<Path,MappedRecord> elements
    ) throws ServiceException {
        // set 'compositeReference' of all referenced classes with composite aggregation
        for(
            Iterator<MappedRecord> i = elements.values().iterator();
            i.hasNext();
        ) {
            MappedRecord reference = i.next();
            ObjectHolder_2Facade referenceFacade;
            try {
                referenceFacade = ObjectHolder_2Facade.newInstance(reference);
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }
            if(referenceFacade.attributeValues(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.REFERENCE)) {
                MappedRecord type = classifiers.get(
                    referenceFacade.attributeValue("type")
                );
                if(type == null) {
                    SysLog.error("type " + referenceFacade.attributeValue("type") + " of reference " + referenceFacade.getPath() + " is not found in repository");
                }
                else {
                    ObjectHolder_2Facade typeFacade;
                    try {
                        typeFacade = ObjectHolder_2Facade.newInstance(type);
                    } 
                    catch (ResourceException e) {
                        throw new ServiceException(e);
                    }
                    MappedRecord referencedEnd = elements.get(
                        referenceFacade.attributeValue("referencedEnd")
                    );
                    if(referencedEnd == null) {
                        SysLog.error("association end " + referenceFacade.attributeValue("referencedEnd") + " of reference " + referenceFacade.getPath() + " is not found in repository");
                    }
                    else {
                        ObjectHolder_2Facade referencedEndFacade;
                        try {
                            referencedEndFacade = ObjectHolder_2Facade.newInstance(referencedEnd);
                        } 
                        catch (ResourceException e) {
                            throw new ServiceException(e);
                        }
                        if(AggregationKind.COMPOSITE.equals(referencedEndFacade.attributeValue("aggregation"))) {
                            typeFacade.clearAttributeValues("compositeReference").add(
                                referenceFacade.getPath()
                            );
                            // set 'compositeReference' for all subtypes of type
                            for(
                                Iterator j = typeFacade.attributeValues("allSubtype").iterator();
                                j.hasNext();
                            ) {
                                MappedRecord subtype = classifiers.get(j.next());
                                try {
                                    ObjectHolder_2Facade.newInstance(subtype).clearAttributeValues("compositeReference").add(
                                        referenceFacade.getPath()
                                    );
                                } 
                                catch (ResourceException e) {
                                    throw new ServiceException(e);
                                }
                            }
                        }
                    }
                }        
            }
        }
    }

    //---------------------------------------------------------------------------
    /**
     * complete derived attribute 'name' and 'qualifiedName'
     */
    MappedRecord completeNames(
        MappedRecord object
    ) throws ServiceException {
        ObjectHolder_2Facade facade;
        try {
            facade = ObjectHolder_2Facade.newInstance(object);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        // qualifiedName = last path component
        // name = last component of qualifiedName
        String qualifiedName = facade.getPath().getBase();
        facade.clearAttributeValues("qualifiedName").add(
            qualifiedName
        );
        facade.attributeValues("name").add(
            qualifiedName.substring(qualifiedName.lastIndexOf(':') + 1)
        );
        return object;
    }

    //---------------------------------------------------------------------------
    /**
     * complete derived attributes
     */
    void completeObject(
        ServiceHeader header,
        DataproviderRequest request,
        MappedRecord object
    ) throws ServiceException {

        ObjectHolder_2Facade facade;
        try {
            facade = ObjectHolder_2Facade.newInstance(object);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        
        // INSTANCEOF
        List allSupertype = ModelUtils.getallSupertype(facade.getObjectClass());
        facade.clearAttributeValues(SystemAttributes.OBJECT_INSTANCE_OF);
        if(allSupertype != null) {
            facade.attributeValues(SystemAttributes.OBJECT_INSTANCE_OF).addAll(allSupertype);
        }

        // calculate derived attributes only if attributes have to be returned
        // and if it is a class of org:omg:model1
        if(
            (request.attributeSelector() != AttributeSelectors.NO_ATTRIBUTES) &&
            facade.attributeValues(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.ELEMENT)
        ) {
            // Reference.referencedEndIsNavigable
            if(
                facade.attributeValues(SystemAttributes.OBJECT_INSTANCE_OF).contains(
                    ModelAttributes.REFERENCE
                )
            ) {
                DataproviderRequest getRequest;
                try {
                    getRequest = new DataproviderRequest(
                        ObjectHolder_2Facade.newInstance((Path)facade.attributeValue("referencedEnd")).getDelegate(),
                        DataproviderOperations.OBJECT_RETRIEVAL,
                        AttributeSelectors.ALL_ATTRIBUTES,
                        null
                    );
                } 
                catch (ResourceException e) {
                    throw new ServiceException(e);
                }
                getRequest.contexts().putAll(request.contexts());
                MappedRecord referencedEnd = this.get(
                    header,
                    getRequest
                ).getObject();
                try {
                    facade.clearAttributeValues("referencedEndIsNavigable").addAll(
                        ObjectHolder_2Facade.newInstance(referencedEnd).attributeValues("isNavigable")
                    );
                } 
                catch (ResourceException e) {
                    throw new ServiceException(e);
                }
            }
            // Operation.parameter
            if(
                facade.attributeValues(SystemAttributes.OBJECT_INSTANCE_OF).contains(
                    ModelAttributes.OPERATION
                )
            ) {
                facade.clearAttributeValues("parameter").addAll(
                    this.getNamespaceContentAsPaths(
                        header,
                        object
                    )
                );
            }
            // GeneralizableElement.allSupertype
            if(
                facade.attributeValues(SystemAttributes.OBJECT_INSTANCE_OF).contains(
                    ModelAttributes.GENERALIZABLE_ELEMENT
                )
            ) {
                // should this statement really be empty? TODO  
            }         
            // ModelElement.name, qualifiedName
            this.completeNames(
                object
            );

        }
    }

    //---------------------------------------------------------------------------
    DataproviderReply completeReply(
        ServiceHeader header,
        DataproviderRequest request,
        DataproviderReply reply
    ) throws ServiceException {

        for(
            int i = 0;
            i < reply.getObjects().length;
            i++
        ) {
            //SysLog.trace("completing object: " + reply.getObjects()[i]);
            this.completeObject(
                header,
                request,
                reply.getObjects()[i]
            );
        }
        reply.context(DataproviderReplyContexts.HAS_MORE).set(0,Boolean.FALSE);
        reply.context(DataproviderReplyContexts.TOTAL).set(0, new Integer(reply.getObjects().length));

        return reply;
    }

    //---------------------------------------------------------------------------
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        Path accessPath = new Path(request.path().toString());
        DataproviderReply reply = null;
        // get ModelElement by 'content' reference
        if(
            ModelAttributes.ELEMENT.equals(
                request.context(DataproviderRequestContexts.OBJECT_TYPE).get(0)
            ) && 
            "content".equals(getReferenceName(request))
        ) {
            // rewrite to .../element/<id> path
            try {
                ObjectHolder_2Facade.newInstance(request.object()).setPath(
                    accessPath.getPrefix(accessPath.size()-3).getChild(accessPath.getBase())
                );
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }
            reply = super.get(
                header,
                request
            );
            // test whether container is correct
            try {
                if(
                    !((Path)ObjectHolder_2Facade.newInstance(reply.getObject()).attributeValue("container")).equals(
                        accessPath.getPrefix(accessPath.size()-2)
                    )
                ) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND, 
                        "no such member",
                        new BasicException.Parameter("path", accessPath)
                    );
                }
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }
        }
        // non-derived access
        else {
            reply = super.get(
                header,
                request
            );
        }
        // reset to access path
        this.completeReply(
            header,
            request,
            reply
        );
        try {
            ObjectHolder_2Facade.newInstance(reply.getObject()).setPath(
                accessPath
            );
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        return reply;
    }

    //---------------------------------------------------------------------------
    public DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        // references on ModelElement
        if(
            ModelAttributes.ELEMENT.equals(
                request.context(DataproviderRequestContexts.OBJECT_TYPE).get(0)
            )
        ) {

            // element
            if("element".equals(getReferenceName(request))) {
                return this.completeReply(
                    header,
                    request,
                    super.find(
                        header,
                        request
                    )
                );    
            }
            else {
                if(request.operation() == DataproviderOperations.ITERATION_CONTINUATION) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE, 
                        "ITERATION_CONTINUATION not supported on derived reference"
                    );
                }
                // namespaceContent
                else if("namespaceContent".equals(getReferenceName(request))) {
                    try {
                        return this.completeReply(
                            header,
                            request,
                            new DataproviderReply(
                                new ArrayList(
                                    this.getNamespaceContent(
                                        header,
                                        ObjectHolder_2Facade.newInstance(ObjectHolder_2Facade.getPath(request.object()).getParent()).getDelegate()
                                    )
                                )
                            )
                        );
                    } 
                    catch (ResourceException e) {
                        throw new ServiceException(e);
                    }
                }
            }
        }
        // non-derived references
        else {
            return completeReply(
                header,
                request,
                super.find(
                    header,
                    request
                )
            );    
        }
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ASSERTION_FAILURE, 
            "unknown reference on type",
            new BasicException.Parameter("reference", getReferenceName(request)),
            new BasicException.Parameter("type", request.context(DataproviderRequestContexts.OBJECT_TYPE))
        );

    }

    //---------------------------------------------------------------------------
    public DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        if(!this.importing) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE, 
                "repository not in import state. Call beginImport() before importing elements",
                new BasicException.Parameter("request", request)
            );
        }
        else {
            return super.modify(
                header,
                request
            );
        }
    }

    //---------------------------------------------------------------------------
    public DataproviderReply set(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        if(!this.importing) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE, 
                "repository not in import state. Call beginImport() before importing elements",
                new BasicException.Parameter("request", request)
            );
        }
        else {
            return super.set(
                header,
                request
            );
        }
    }

    //---------------------------------------------------------------------------
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        if(!this.importing) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE, 
                "repository not in import state. Call beginImport() before importing elements",
                new BasicException.Parameter("request", request)
            );
        }
        else {
            return super.replace(
                header,
                request
            );
        }
    }

    //---------------------------------------------------------------------------
    public DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        if(!this.importing) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE, 
                "repository not in import state. Call beginImport() before importing elements",
                new BasicException.Parameter("request", request)
            );
        }
        else {
            return super.create(
                header,
                request
            );
        }
    }

    //---------------------------------------------------------------------------
    public DataproviderReply operation(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        String operationName = getReferenceName(request);
        MappedRecord params = request.object();
        ObjectHolder_2Facade paramsFacade;
        try {
            paramsFacade = ObjectHolder_2Facade.newInstance(params);
        } 
        catch (ResourceException e) {
            throw new ServiceException(e);
        }
        // Operations on ModelElements
        if(
            ModelAttributes.ELEMENT.equals(
                request.context(DataproviderRequestContexts.OBJECT_TYPE).get(0)
            )
        ) {
            // lookupElement
            if("lookupElement".equals(operationName)) {
                // get content  
                // search elements with matching name
                List<MappedRecord> contents;
                try {
                    contents = this.getNamespaceContent(
                        header,
                        ObjectHolder_2Facade.newInstance(request.path().getPrefix(request.path().size()-2)).getDelegate()
                    );
                } 
                catch (ResourceException e) {
                    throw new ServiceException(e);
                }  
                for(
                    Iterator<MappedRecord> i = contents.iterator();
                    i.hasNext();
                ) {
                    MappedRecord content = i.next();                 
                    this.completeNames(
                        content
                    );
                    try {
                        if(ObjectHolder_2Facade.newInstance(content).attributeValue("name").equals(paramsFacade.attributeValue("name"))) {
                            paramsFacade.attributeValues("result").add(ObjectHolder_2Facade.getPath(content));
                            break;
                        }
                    } 
                    catch (ResourceException e) {
                        throw new ServiceException(e);
                    }
                }
                if(!paramsFacade.getValue().keySet().contains("result")) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND, 
                        "element not found",
                        new BasicException.Parameter("params", params)
                    );
                }
                return new DataproviderReply(
                    params
                );
            }
            // resolveQualifiedName
            else if("resolveQualifiedName".equals(operationName)) {
                // get content  
                // search elements with matching qualifiedName
                List contents;
                try {
                    contents = this.getNamespaceContent(
                        header,
                        ObjectHolder_2Facade.newInstance(request.path().getPrefix(request.path().size()-2)).getDelegate()
                    );
                } 
                catch (ResourceException e) {
                    throw new ServiceException(e);
                }  
                for(
                    Iterator<MappedRecord> i = contents.iterator();
                    i.hasNext();
                ) {
                    MappedRecord content = i.next();
                    this.completeNames(
                        content
                    );
                    try {
                        if(ObjectHolder_2Facade.newInstance(content).attributeValue("qualifiedName").equals(paramsFacade.attributeValues("qualifiedName").get(0))) {
                            paramsFacade.attributeValues("result").add(
                                ObjectHolder_2Facade.getPath(content)
                            );
                            break;
                        }
                    } 
                    catch (ResourceException e) {
                        throw new ServiceException(e);
                    }
                }
                if(!paramsFacade.getValue().keySet().contains("result")) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND, 
                        "element not found",
                        new BasicException.Parameter("params", params.toString())
                    );
                }
                return new DataproviderReply(
                    params
                );
            }
            // findElementsByType
            else if("findElementsByType".equals(operationName)) {
                // get content ...
                List contents;
                try {
                    contents = this.getNamespaceContent(
                        header,
                        ObjectHolder_2Facade.newInstance(request.path().getPrefix(request.path().size()-2)).getDelegate()
                    );
                } 
                catch (ResourceException e) {
                    throw new ServiceException(e);
                }
                // search elements with matching types
                boolean includeSubtypes = ((Boolean)paramsFacade.attributeValue("includeSubtypes")).booleanValue();
                for(
                    Iterator<MappedRecord> i = contents.iterator();
                    i.hasNext();
                ) {
                    MappedRecord content = i.next();
                    if(includeSubtypes) {
                        List subtypes = ModelUtils.getsubtype(
                            (String)paramsFacade.attributeValue("ofType")
                        );
                        for(
                            Iterator j = subtypes.iterator();
                            j.hasNext();
                        ) {
                            try {
                                if(ObjectHolder_2Facade.newInstance(content).attributeValues(SystemAttributes.OBJECT_INSTANCE_OF).contains(j.next())) {
                                    paramsFacade.attributeValues("result").add(
                                        ObjectHolder_2Facade.getPath(content)
                                    );
                                    break;
                                }
                            } 
                            catch (ResourceException e) {
                                throw new ServiceException(e);
                            }
                        }
                    } else
                        try {
                            if(ObjectHolder_2Facade.newInstance(content).attributeValues(SystemAttributes.OBJECT_INSTANCE_OF).contains(paramsFacade.attributeValues("ofType").get(0))) {
                                paramsFacade.attributeValues("result").add(
                                    ObjectHolder_2Facade.getPath(content)
                                );
                            }
                        } 
                        catch (ResourceException e) {
                            throw new ServiceException(e);
                        }
                }
                return new DataproviderReply(
                    params
                );
            }
        }
        // Operations on ModelClassifier
        else if(
            ModelAttributes.CLASSIFIER.equals(
                request.context(DataproviderRequestContexts.OBJECT_TYPE).get(0)
            )
        ) {
            // externalizeClassifier
            if("externalizeClassifier".equals(operationName)) {
                return new DataproviderReply(
                    params
                );
            }
        }
        // Operations on ModelPackage
        else if(
            ModelAttributes.PACKAGE.equals(
                request.context(DataproviderRequestContexts.OBJECT_TYPE).get(0)
            )
        ) {
            // externalizePackage
            if("externalizePackage".equals(operationName)) {
                try {
                    SysLog.trace("activating model");
                    Model_1_0 model = org.openmdx.application.mof.repository.accessor.Model_1.getInstance(
                        this,
                        true
                    );
                    // check whether the desired package to externalize exists in the 
                    // model repository or not
                    Path modelPackagePath = request.path().getParent().getParent();
                    String qualifiedPackageName = modelPackagePath.getBase();

                    // only test for existence if not wildcard export
                    if(
                        (qualifiedPackageName.indexOf("%") < 0) &&
                        (model.findElement(modelPackagePath) == null)
                    ) {
                        throw new ServiceException(
                            ModelExceptions.MODEL_DOMAIN,
                            ModelExceptions.PACKAGE_TO_EXTERNALIZE_DOES_NOT_EXIST,
                            "package does not exist",
                            new BasicException.Parameter("package", modelPackagePath.toString())
                        );
                    }
                    SysLog.trace("Verify model constraints");
                    new ModelConstraintsChecker_1(model).verify();

                    ByteArrayOutputStream bs = null;
                    ZipOutputStream zip = new ZipOutputStream(
                        bs = new ByteArrayOutputStream()         
                    );
                    List formats = !paramsFacade.attributeValues("format").isEmpty() ? 
                        paramsFacade.attributeValues("format") : 
                        STANDARD_FORMAT;
                    for(
                        Iterator i = formats.iterator();
                        i.hasNext();
                    ) {
                        String format = (String)i.next();
                        Mapper_1_0 mapper = MapperFactory_1.create(format);
                        if(
                            this.openmdxjdoMetadataDirectory != null && 
                            mapper instanceof Mapper_1_1
                        ){
                            ((Mapper_1_1)mapper).externalize(
                                modelPackagePath.getBase(),
                                model,
                                zip,
                                this.openmdxjdoMetadataDirectory
                            );
                        } 
                        else {
                            mapper.externalize(
                                modelPackagePath.getBase(),
                                model,
                                zip
                            );
                        }
                    }
                    zip.close();
                    ObjectHolder_2Facade resultFacade;
                    try {
                        resultFacade = ObjectHolder_2Facade.newInstance(
                            paramsFacade.getPath(),
                            "org:omg:model1:PackageExternalizeResult"
                        );
                    } 
                    catch (ResourceException e) {
                        throw new ServiceException(e);
                    }
                    resultFacade.attributeValues("packageAsJar").add(
                        bs.toByteArray()
                    );
                    return new DataproviderReply(
                        resultFacade.getDelegate()
                    );          
                }
                catch(IOException e) {
                    throw new ServiceException(e);
                }
            }
        }
        // beginImport
        else if("beginImport".equals(operationName)) {
            this.importing = true;
            ObjectHolder_2Facade resultFacade;
            try {
                resultFacade = ObjectHolder_2Facade.newInstance(
                    paramsFacade.getPath().getDescendant("reply", super.uidAsString()),
                    "org:openmdx:base:Void"
                );
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }
            return new DataproviderReply(
                resultFacade.getDelegate()
            );
        }
        // endImport
        else if("endImport".equals(operationName)) {
            // recalculate all derived attributes and update repository
            RequestCollection channel = new RequestCollection(
                header,
                this.getDelegation()
            );
            // get full repository content ...
            List segments = channel.addFindRequest(
                PROVIDER_ROOT_PATH.getChild("segment"),
                null
            );
            Map completedElements = new HashMap();
            for(
                Iterator<MappedRecord> i = segments.iterator();
                i.hasNext();
            ) {
                MappedRecord segment = i.next();
                List<MappedRecord> elements = channel.addFindRequest(
                    ObjectHolder_2Facade.getPath(segment).getChild("element"),
                    null,
                    AttributeSelectors.ALL_ATTRIBUTES,
                    null,
                    0,
                    Integer.MAX_VALUE,
                    Directions.ASCENDING
                );
                for(
                    Iterator<MappedRecord> j = elements.iterator();
                    j.hasNext();  
                ) {
                    MappedRecord element = j.next();
                    completedElements.put(
                        ObjectHolder_2Facade.getPath(element),
                        element
                    );
                }
            }
            // ... complete it ...
            this.completeElements(
                header,
                completedElements
            );
            // ... and replace the existing elements with the completed
            for(
                Iterator<MappedRecord> i = completedElements.values().iterator();
                i.hasNext();
            ) {
                MappedRecord element = i.next();
                channel.addReplaceRequest(
                    element
                );
            }
            SysLog.trace("completed elements", completedElements.size());
            // Void reply      
            ObjectHolder_2Facade resultFacade;
            try {
                resultFacade = ObjectHolder_2Facade.newInstance(
                    paramsFacade.getPath().getDescendant("reply", super.uidAsString()),
                    "org:openmdx:base:Void"
                );
            } 
            catch (ResourceException e) {
                throw new ServiceException(e);
            }
            this.importing = false;
            return new DataproviderReply(
                resultFacade.getDelegate()
            );
        }
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ASSERTION_FAILURE, 
            "unknown operation",
            new BasicException.Parameter("operation", operationName),
            new BasicException.Parameter("object type", request.context(DataproviderRequestContexts.OBJECT_TYPE).get(0))
        );
    }

    //---------------------------------------------------------------------------
    // Variables
    //---------------------------------------------------------------------------

    /**
     * 
     */
    static private final Path PROVIDER_ROOT_PATH = new Path("xri:@openmdx:org.omg.model1/provider/Mof");

    /**
     * 
     */
    private boolean importing = false;

    /**
     * 
     */
    private String openmdxjdoMetadataDirectory; 

}

//--- End of File -----------------------------------------------------------
