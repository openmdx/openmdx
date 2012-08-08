/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Model_1.java,v 1.32 2012/01/05 23:20:20 hburger Exp $
 * Description: model1 application plugin
 * Revision:    $Revision: 1.32 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2012/01/05 23:20:20 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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
import javax.resource.cci.Connection;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderOperations;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.DataproviderRequestProcessor;
import org.openmdx.application.dataprovider.cci.FilterProperty;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.spi.Layer_1;
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
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

//---------------------------------------------------------------------------
@SuppressWarnings({"rawtypes","unchecked"})
public class Model_1 extends Layer_1 {

    /**
     * Constructor 
     */
    public Model_1(
    ) {
        super();
    }
    
    // --------------------------------------------------------------------------
    @Override
    public Interaction getInteraction(
        Connection connection
    ) throws ResourceException {
        return new LayerInteraction(connection);
    }
                    
    // --------------------------------------------------------------------------
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
    @Override
    public void activate(
        short id,
        Configuration configuration,
        Layer_1 delegation
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
    protected String getReferenceName(
        DataproviderRequest request
    ) throws ServiceException {
        Path path = request.path();    
        return
        path.size() % 2 == 0 ?
            (String)path.get(path.size()-1) :
                (String)path.get(path.size()-2);
    }

    //---------------------------------------------------------------------------
    protected void completeElements(
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
            Object_2Facade elementFacade = Facades.asObject(element);
            // collect classifier
            if(elementFacade.attributeValuesAsList(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CLASSIFIER)) {
                classifiers.put(
                    elementFacade.getPath(),
                    element
                );
            }
            // qualified name
            elementFacade.attributeValuesAsList("qualifiedName").clear();
            elementFacade.attributeValuesAsList("qualifiedName").add(
                elementFacade.getPath().getBase()
            );
        }

        // allSupertype
        for(
            Iterator<MappedRecord> i = elements.values().iterator();
            i.hasNext();
        ) { 
            MappedRecord classifier = i.next();
            Object_2Facade classifierFacade = Facades.asObject(classifier);
            if(classifierFacade.attributeValuesAsList(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CLASSIFIER)) {
                classifierFacade.attributeValuesAsList("allSupertype").clear();
                classifierFacade.attributeValuesAsList("allSupertype").addAll(
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
            Object_2Facade classifierFacade = Facades.asObject(classifier);
            if(classifierFacade.attributeValuesAsList(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CLASSIFIER)) {
                classifierFacade.attributeValuesAsList("allSubtype").clear();
                classifierFacade.attributeValuesAsList("allSubtype").addAll(
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
            Object_2Facade classifierFacade = Facades.asObject(classifier);
            if(classifierFacade.attributeValuesAsList(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CLASSIFIER)) {      
                classifierFacade.attributeValuesAsList("feature").clear();
                classifierFacade.attributeValuesAsList("feature").addAll(
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
    protected List<MappedRecord> getNamespaceContent(
        ServiceHeader header,
        MappedRecord namespace
    ) throws ServiceException {
        DataproviderRequestProcessor channel = new DataproviderRequestProcessor(
            header,
            this.getDelegation()
        );
        return channel.addFindRequest(
            Object_2Facade.getPath(namespace).getParent(),
            new FilterProperty[]{
                new FilterProperty(
                    Quantifier.THERE_EXISTS.code(),
                    "container",
                    ConditionType.IS_IN.code(),
                    Object_2Facade.getPath(namespace)
                )        
            },
            AttributeSelectors.ALL_ATTRIBUTES,
            null,
            0,
            Integer.MAX_VALUE,
            SortOrder.ASCENDING.code()
        );
    }

    //---------------------------------------------------------------------------
    protected List getNamespaceContentAsPaths(
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
                Object_2Facade.getPath(i.next())
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
        for(
            Iterator i = Facades.asObject(classifier).attributeValuesAsList("supertype").iterator();
            i.hasNext();
        ) {
            Path supertypePath = (Path)i.next();
            MappedRecord supertype = elements.get(supertypePath);
            if(supertype == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "type not found in set of loaded models (HINT: probably not configured as 'modelPackage')",
                    new BasicException.Parameter("classifier", Object_2Facade.getPath(classifier)),
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
        allSupertype.add(
            Object_2Facade.getPath(classifier)
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
            Facades.asObject(classifier).attributeValuesAsList("subtype").clear();
        }
        // recalc subtype
        for(
            Iterator<MappedRecord> i = classifiers.values().iterator();
            i.hasNext();
        ) {
            MappedRecord classifier = i.next();
            Object_2Facade classifierFacade = Facades.asObject(classifier);
            for(
			    Iterator j = Facades.asObject(classifier).attributeValuesAsList("supertype").iterator();
			    j.hasNext();
			) {
			    Object next = j.next();
			    MappedRecord supertype = classifiers.get(next);
			    if(supertype == null) {
			        SysLog.error("supertype " + next + " of classifier " + Object_2Facade.getPath(classifier) + " not found in repository");
			    }
			    else {
			        Object_2Facade superTypeFacade = Facades.asObject(supertype);
			        if(!superTypeFacade.attributeValuesAsList("subtype").contains(classifierFacade.getPath())) {
			            superTypeFacade.attributeValuesAsList("subtype").add(
			                classifierFacade.getPath()
			            );
			        }
			    }
			}
            if(!classifierFacade.attributeValuesAsList("subtype").contains(classifierFacade.getPath())) {
                classifierFacade.attributeValuesAsList("subtype").add(
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
            Facades.asObject(element).attributeValuesAsList("content");
        }
        // recalc content
        for(
            Iterator<MappedRecord> i = elements.values().iterator();
            i.hasNext();
        ) {
            MappedRecord content = i.next();
            Object_2Facade contentFacade = Facades.asObject(content);
            if(contentFacade.getAttributeValues("container") != null) {
                MappedRecord container = elements.get(
                    contentFacade.attributeValue("container")
                );
                if(container == null) {
                    SysLog.error("container " + contentFacade.attributeValue("container") + " of element " + contentFacade.getPath() + " not found in repository");
                }
                else { 
                    Object_2Facade containerFacade = Facades.asObject(container);
                    if(!containerFacade.attributeValuesAsList("content").contains(contentFacade.getPath())) {
                        containerFacade.attributeValuesAsList("content").add(
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
            Object_2Facade containerFacade = Facades.asObject(container);
            if(containerFacade.getAttributeValues("content") != null) {
                Set content = new TreeSet(
                    containerFacade.attributeValuesAsList("content")
                );
                containerFacade.attributeValuesAsList("content").clear();
                containerFacade.attributeValuesAsList("content").addAll(
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
        for(
            Iterator i = Facades.asObject(classifier).attributeValuesAsList("allSupertype").iterator();
            i.hasNext();
        ) {
            MappedRecord supertype = elements.get(i.next());
            for(
                Iterator j = Facades.asObject(supertype).attributeValuesAsList("content").iterator();
                j.hasNext();
            ) {
                MappedRecord feature = elements.get(j.next());
                if(Facades.asObject(feature).attributeValuesAsList(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.FEATURE)) {
                    features.add(
                        Object_2Facade.getPath(feature)
                    );
                }
            }
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
        for(
            Iterator i = Facades.asObject(classifier).attributeValuesAsList("subtype").iterator();
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
        allSubtypes.add(
            Object_2Facade.getPath(classifier)
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
            Object_2Facade referenceFacade = Facades.asObject(reference);
            if(referenceFacade.attributeValuesAsList(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.REFERENCE)) {
                MappedRecord type = classifiers.get(
                    referenceFacade.attributeValue("type")
                );
                if(type == null) {
                    SysLog.error("type " + referenceFacade.attributeValue("type") + " of reference " + referenceFacade.getPath() + " is not found in repository");
                }
                else {
                    Object_2Facade typeFacade = Facades.asObject(type);
                    MappedRecord referencedEnd = elements.get(
                        referenceFacade.attributeValue("referencedEnd")
                    );
                    if(referencedEnd == null) {
                        SysLog.error("association end " + referenceFacade.attributeValue("referencedEnd") + " of reference " + referenceFacade.getPath() + " is not found in repository");
                    }
                    else {
                        Object_2Facade referencedEndFacade = Facades.asObject(referencedEnd);
                        if(AggregationKind.COMPOSITE.equals(referencedEndFacade.attributeValue("aggregation"))) {
                            typeFacade.attributeValuesAsList("compositeReference").clear();
                            typeFacade.attributeValuesAsList("compositeReference").add(
                                referenceFacade.getPath()
                            );
                            // set 'compositeReference' for all subtypes of type
                            for(
                                Iterator j = typeFacade.attributeValuesAsList("allSubtype").iterator();
                                j.hasNext();
                            ) {
                                MappedRecord subtype = classifiers.get(j.next());
                                Object_2Facade subtypeFacade = Facades.asObject(subtype);
								subtypeFacade.attributeValuesAsList("compositeReference").clear();
								subtypeFacade.attributeValuesAsList("compositeReference").add(
								    referenceFacade.getPath()
								);
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
        Object_2Facade facade = Facades.asObject(object);
        // qualifiedName = last path component
        // name = last component of qualifiedName
        String qualifiedName = facade.getPath().getBase();
        facade.attributeValuesAsList("qualifiedName").clear();
        facade.attributeValuesAsList("qualifiedName").add(
            qualifiedName
        );
        facade.attributeValuesAsList("name").add(
            qualifiedName.substring(qualifiedName.lastIndexOf(':') + 1)
        );
        return object;
    }

    // --------------------------------------------------------------------------
    public class LayerInteraction extends Layer_1.LayerInteraction {
        
        /**
         * Constructor 
         *
         * @param connection
         * @throws ResourceException
         */
        protected LayerInteraction(
            Connection connection
        ) throws ResourceException {
            super(connection);
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
    
            Object_2Facade facade;
            facade = Facades.asObject(object);
            
            // INSTANCEOF
            List allSupertype = ModelUtils.getallSupertype(facade.getObjectClass());
            facade.attributeValuesAsList(SystemAttributes.OBJECT_INSTANCE_OF).clear();
            if(allSupertype != null) {
                facade.attributeValuesAsList(SystemAttributes.OBJECT_INSTANCE_OF).addAll(allSupertype);
            }
    
            // calculate derived attributes only if attributes have to be returned
            // and if it is a class of org:omg:model1
            if(
                (request.attributeSelector() != AttributeSelectors.NO_ATTRIBUTES) &&
                facade.attributeValuesAsList(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.ELEMENT)
            ) {
                // Reference.referencedEndIsNavigable
                if(
                    facade.attributeValuesAsList(SystemAttributes.OBJECT_INSTANCE_OF).contains(
                        ModelAttributes.REFERENCE
                    )
                ) {
                    MappedRecord referencedEnd;
                    DataproviderRequest getRequest = new DataproviderRequest(
						Facades.newQuery((Path)facade.attributeValue("referencedEnd")).getDelegate(),
					    DataproviderOperations.OBJECT_RETRIEVAL,
					    AttributeSelectors.ALL_ATTRIBUTES,
					    null
					);
					DataproviderReply getReply = this.newDataproviderReply();
					super.get(
					    getRequest.getInteractionSpec(), 
					    Facades.asQuery(getRequest.object()), 
					    getReply.getResult()
					);
					referencedEnd = getReply.getObject();
                    facade.attributeValuesAsList("referencedEndIsNavigable").clear();
					facade.attributeValuesAsList("referencedEndIsNavigable").addAll(
							Facades.asObject(referencedEnd).attributeValuesAsList("isNavigable")
					);
                }
                // Operation.parameter
                if(
                    facade.attributeValuesAsList(SystemAttributes.OBJECT_INSTANCE_OF).contains(
                        ModelAttributes.OPERATION
                    )
                ) {
                    facade.attributeValuesAsList("parameter").clear();
                    facade.attributeValuesAsList("parameter").addAll(
                        Model_1.this.getNamespaceContentAsPaths(
                            header,
                            object
                        )
                    );
                }
                // GeneralizableElement.allSupertype
                if(
                    facade.attributeValuesAsList(SystemAttributes.OBJECT_INSTANCE_OF).contains(
                        ModelAttributes.GENERALIZABLE_ELEMENT
                    )
                ) {
                    // should this statement really be empty? TODO  
                }         
                // ModelElement.name, qualifiedName
                Model_1.this.completeNames(
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
            reply.setHasMore(Boolean.FALSE);
            reply.setTotal(new Integer(reply.getObjects().length));
    
            return reply;
        }
    
        //---------------------------------------------------------------------------
        @Override
        public boolean get(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            ServiceHeader header = this.getServiceHeader();
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
            
            Path accessPath = new Path(request.path().toString());
            // get ModelElement by 'content' reference
            if("content".equals(Model_1.this.getReferenceName(request))) {
                Facades.asObject(request.object()).setPath(
				    accessPath.getPrefix(accessPath.size()-3).getChild(accessPath.getBase())
				);
                super.get(
                    ispec,
                    input,
                    output
                );
                if(
				    !((Path)Facades.asObject(reply.getObject()).attributeValue("container")).equals(
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
            // non-derived access
            else {
                super.get(
                    ispec,
                    input,
                    output
                );
            }
            // reset to access path
            this.completeReply(
                header,
                request,
                reply
            );
            Facades.asObject(reply.getObject()).setPath(
			    accessPath
			);
            return true;
        }
    
        //---------------------------------------------------------------------------
        @Override
        public boolean find(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            ServiceHeader header = this.getServiceHeader();
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
            if("element".equals(getReferenceName(request))) {
                super.find(
                    ispec, 
                    input, 
                    output
                );
                this.completeReply(
                    header,
                    request,
                    reply
                );
                return true;
            }
            // namespaceContent
            else if("namespaceContent".equals(getReferenceName(request))) {
                reply.getResult().addAll(
				    new ArrayList(
				        Model_1.this.getNamespaceContent(
				            header,
				            Facades.newObject(Object_2Facade.getPath(request.object()).getParent()).getDelegate()
				        )
				    )
				);
				this.completeReply(
				    header,
				    request,
				    reply
				);
				return true;
            }
            // non-derived references
            else {
                super.find(
                    ispec, 
                    input, 
                    output
                );
                completeReply(
                    header,
                    request,
                    reply
                );
                return true;
            }
        }
    
        //---------------------------------------------------------------------------
        @Override
        public boolean put(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            if(!Model_1.this.importing) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE, 
                    "repository not in import state. Call beginImport() before importing elements",
                    new BasicException.Parameter("request", request)
                );
            }
            else {
                super.put(
                    ispec,
                    input,
                    output
                );
                return true;
            }
        }
    
        //---------------------------------------------------------------------------
        @Override
        public boolean create(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            if(!Model_1.this.importing) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE, 
                    "repository not in import state. Call beginImport() before importing elements",
                    new BasicException.Parameter("request", request)
                );
            }
            else {
                super.create(
                    ispec,
                    input,
                    output
                );
                return true;
            }
        }
    
        //---------------------------------------------------------------------------
        @Override
        public boolean invoke(
            RestInteractionSpec ispec, 
            MessageRecord input, 
            MessageRecord output
        ) throws ServiceException {
            ServiceHeader header = this.getServiceHeader();
            Path target = input.getTarget();
            String operationName = target.getBase();
            Object_2Facade paramsFacade;
            paramsFacade = Facades.newObject(input.getPath());
			paramsFacade.setValue(input.getBody());
            if("lookupElement".equals(operationName)) {
                // get content  
                // search elements with matching name
                List<MappedRecord> contents;
                contents = Model_1.this.getNamespaceContent(
				    header,
				    Facades.newObject(input.getPath().getPrefix(input.getPath().size()-2)).getDelegate()
				);  
                for(
                    Iterator<MappedRecord> i = contents.iterator();
                    i.hasNext();
                ) {
                    MappedRecord content = i.next();                 
                    Model_1.this.completeNames(
                        content
                    );
                    if(Facades.asObject(content).attributeValue("name").equals(paramsFacade.attributeValue("name"))) {
					    paramsFacade.attributeValuesAsList("result").add(Object_2Facade.getPath(content));
					    break;
					}
                }
                if(!paramsFacade.getValue().keySet().contains("result")) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND, 
                        "element not found",
                        new BasicException.Parameter("params", input)
                    );
                }
                output.setPath(paramsFacade.getPath());
                output.setBody(paramsFacade.getValue());
                return true;
            }
            // resolveQualifiedName
            else if("resolveQualifiedName".equals(operationName)) {
                // get content  
                // search elements with matching qualifiedName
                List contents;
                contents = Model_1.this.getNamespaceContent(
				    header,
				    Facades.newObject(input.getPath().getPrefix(input.getPath().size()-2)).getDelegate()
				);  
                for(
                    Iterator<MappedRecord> i = contents.iterator();
                    i.hasNext();
                ) {
                    MappedRecord content = i.next();
                    Model_1.this.completeNames(
                        content
                    );
                    if(Facades.asObject(content).attributeValue("qualifiedName").equals(paramsFacade.attributeValuesAsList("qualifiedName").get(0))) {
					    paramsFacade.attributeValuesAsList("result").add(
					        Object_2Facade.getPath(content)
					    );
					    break;
					}
                }
                if(!paramsFacade.getValue().keySet().contains("result")) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND, 
                        "element not found",
                        new BasicException.Parameter("params", input)
                    );
                }
                output.setPath(paramsFacade.getPath());
                output.setBody(paramsFacade.getValue());
                return true;
            }
            // findElementsByType
            else if("findElementsByType".equals(operationName)) {
                // get content ...
                List contents;
                contents = Model_1.this.getNamespaceContent(
				    header,
				    Facades.newObject(input.getPath().getPrefix(input.getPath().size()-2)).getDelegate()
				);
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
                            if(Facades.asObject(content).attributeValuesAsList(SystemAttributes.OBJECT_INSTANCE_OF).contains(j.next())) {
							    paramsFacade.attributeValuesAsList("result").add(
							        Object_2Facade.getPath(content)
							    );
							    break;
							}
                        }
                    } else if(Facades.asObject(content).attributeValuesAsList(SystemAttributes.OBJECT_INSTANCE_OF).contains(paramsFacade.attributeValuesAsList("ofType").get(0))) {
					    paramsFacade.attributeValuesAsList("result").add(
					        Object_2Facade.getPath(content)
					    );
					}
                }
                output.setPath(paramsFacade.getPath());
                output.setBody(paramsFacade.getValue());
                return true;
            }
            else if("externalizeClassifier".equals(operationName)) {
                output.setPath(input.getPath());
                output.setBody(input.getBody());
                return true;
            }
            // Operations on ModelPackage
            else if("externalizePackage".equals(operationName)) {
                try {
                    SysLog.trace("activating model");
                    Model_1_0 model = org.openmdx.application.mof.repository.accessor.Model_1.getInstance(
                        Model_1.this,
                        true
                    );
                    // check whether the desired package to externalize exists in the 
                    // model repository or not
                    Path modelPackagePath = input.getPath().getParent().getParent();
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
                    List formats = !paramsFacade.attributeValuesAsList("format").isEmpty() ? 
                        paramsFacade.attributeValuesAsList("format") : 
                        STANDARD_FORMAT;
                    for(
                        Iterator i = formats.iterator();
                        i.hasNext();
                    ) {
                        String format = (String)i.next();
                        Mapper_1_0 mapper = MapperFactory_1.create(format);
                        if(
                            Model_1.this.openmdxjdoMetadataDirectory != null && 
                            mapper instanceof Mapper_1_1
                        ){
                            ((Mapper_1_1)mapper).externalize(
                                modelPackagePath.getBase(),
                                model,
                                zip,
                                Model_1.this.openmdxjdoMetadataDirectory
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
                    output.setPath(input.getPath());
                    MappedRecord body = Records.getRecordFactory().createMappedRecord("org:omg:model1:PackageExternalizeResult");
                    output.setBody(body);
                    body.put("packageAsJar", bs.toByteArray());
                    return true;
                }
                catch(IOException e) {
                    throw new ServiceException(e);
                } catch (ResourceException exception) {
                    throw new ServiceException(exception);
                }
            }
            // beginImport
            else if("beginImport".equals(operationName)) {
                Model_1.this.importing = true;
                output.setPath(newResponseId(input.getPath()));
                output.setBody(null);
                return true;
            }
            // endImport
            else if("endImport".equals(operationName)) {
                // recalculate all derived attributes and update repository
                DataproviderRequestProcessor channel = new DataproviderRequestProcessor(
                    header,
                    this.getDelegatingLayer()
                );
                // get full repository content ...
                List segments = channel.addFindRequest(
                    PROVIDER_ROOT_PATH.getChild("segment"),
                    null,
                    AttributeSelectors.ALL_ATTRIBUTES,
                    0,
                    Integer.MAX_VALUE,
                    SortOrder.ASCENDING.code()
                );
                Map completedElements = new HashMap();
                for(
                    Iterator<MappedRecord> i = segments.iterator();
                    i.hasNext();
                ) {
                    MappedRecord segment = i.next();
                    List<MappedRecord> elements = channel.addFindRequest(
                        Object_2Facade.getPath(segment).getChild("element"),
                        null,
                        AttributeSelectors.ALL_ATTRIBUTES,
                        null,
                        0,
                        Integer.MAX_VALUE,
                        SortOrder.ASCENDING.code()
                    );
                    for(
                        Iterator<MappedRecord> j = elements.iterator();
                        j.hasNext();  
                    ) {
                        MappedRecord element = j.next();
                        completedElements.put(
                            Object_2Facade.getPath(element),
                            element
                        );
                    }
                }
                // ... complete it ...
                Model_1.this.completeElements(
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
                output.setPath(newResponseId(input.getPath()));
                output.setBody(null);
                Model_1.this.importing = false;
                return true;
            }
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "unknown operation",
                new BasicException.Parameter("operation", operationName),
                new BasicException.Parameter("object type", input)
            );
        }
        
    }

    //---------------------------------------------------------------------------
    // Variables
    //---------------------------------------------------------------------------
   static protected final Path PROVIDER_ROOT_PATH = new Path("xri:@openmdx:org.omg.model1/provider/Mof");

    protected static final List<String> STANDARD_FORMAT = Collections.unmodifiableList(
        Arrays.asList(
            MappingTypes.XMI1, 
            MappingTypes.UML_OPENMDX_1,
            MappingTypes.UML2_OPENMDX_1
        )
    );

    protected boolean importing = false;
    protected String openmdxjdoMetadataDirectory; 

}

//--- End of File -----------------------------------------------------------
