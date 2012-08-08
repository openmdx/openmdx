/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Model_1.java,v 1.34 2008/09/10 08:55:27 hburger Exp $
 * Description: model1 application plugin
 * Revision:    $Revision: 1.34 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:27 $
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
package org.openmdx.model1.layer.application;

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

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequestContexts;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelUtils;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.code.AggregationKind;
import org.openmdx.model1.code.ModelAttributes;
import org.openmdx.model1.code.ModelExceptions;
import org.openmdx.model1.mapping.MapperFactory_1;
import org.openmdx.model1.mapping.Mapper_1_0;
import org.openmdx.model1.mapping.Mapper_1_1;
import org.openmdx.model1.mapping.MappingTypes;

//---------------------------------------------------------------------------
@SuppressWarnings("unchecked")
public class Model_1 
extends Layer_1 {

    private static final List<String> STANDARD_FORMAT = Collections.unmodifiableList(
        Arrays.asList(
            MappingTypes.XMI1, 
            MappingTypes.JMI_OPENMDX_1, 
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
    ) throws Exception, ServiceException {

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
    ) {
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

        Map classifiers = new HashMap();
        for(
                Iterator i = elements.values().iterator();
                i.hasNext();
        ) {
            DataproviderObject element = (DataproviderObject)i.next();
            // collect classifier
            if(element.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CLASSIFIER)) {
                classifiers.put(
                    element.path(),
                    element
                );
            }
            // qualified name
            element.clearValues("qualifiedName").add(
                element.path().getBase()
            );
        }

        // allSupertype
        for(
                Iterator i = elements.values().iterator();
                i.hasNext();
        ) { 
            DataproviderObject classifier = (DataproviderObject)i.next();
            if(classifier.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CLASSIFIER)) {
                classifier.clearValues("allSupertype").addAll(
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
                Iterator i = elements.values().iterator();
                i.hasNext();
        ) {
            DataproviderObject classifier = (DataproviderObject)i.next();
            if(classifier.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CLASSIFIER)) {
                classifier.clearValues("allSubtype").addAll(
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
                Iterator i = elements.values().iterator();
                i.hasNext();
        ) {
            DataproviderObject classifier = (DataproviderObject)i.next();
            if(classifier.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.CLASSIFIER)) {      
                classifier.clearValues("feature").addAll(
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
    private List getNamespaceContent(
        ServiceHeader header,
        DataproviderObject namespace
    ) throws ServiceException {
        RequestCollection channel = new RequestCollection(
            header,
            this.getDelegation()
        );
        return channel.addFindRequest(
            namespace.path().getParent(),
            new FilterProperty[]{
                new FilterProperty(
                    Quantors.THERE_EXISTS,
                    "container",
                    FilterOperators.IS_IN,
                    namespace.path()
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
        DataproviderObject namespace
    ) throws ServiceException {
        List content = this.getNamespaceContent(
            header,
            namespace
        );
        List contentAsPaths = new ArrayList();
        for(
                Iterator i = content.iterator();
                i.hasNext();
        ) {
            contentAsPaths.add(
                ((DataproviderObject)i.next()).path()
            );
        }
        return contentAsPaths;
    }

    //---------------------------------------------------------------------------
    /**
     * Gets all direct and indirect supertypes for a given Classifier
     */
    private Set getAllSupertype(
        DataproviderObject classifier,
        Map elements
    ) throws ServiceException {
        Set allSupertype = new TreeSet();
        for(
                Iterator i = classifier.values("supertype").iterator();
                i.hasNext();
        ) {
            Path supertypePath = (Path)i.next();
            DataproviderObject supertype = (DataproviderObject)elements.get(supertypePath);
            if(supertype == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE, 
                    "type not found in set of loaded models (HINT: probably not configured as 'modelPackage')",
                    new BasicException.Parameter("classifier", classifier.path()),
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
            classifier.path()
        );
        return allSupertype;
    }

    //---------------------------------------------------------------------------
    /**
     * Set feature 'subtype' of classifiers
     */
    private void setSubtype(
        Map classifiers
    ) {    
        // clear feature subtype
        for(
                Iterator i = classifiers.values().iterator();
                i.hasNext();
        ) {
            DataproviderObject classifier = (DataproviderObject)i.next();
            classifier.clearValues("subtype");
        }

        // recalc subtype
        for(
                Iterator i = classifiers.values().iterator();
                i.hasNext();
        ) {
            DataproviderObject classifier = (DataproviderObject)i.next();
            for(
                    Iterator j = classifier.values("supertype").iterator();
                    j.hasNext();
            ) {
                Object next = j.next();
                DataproviderObject supertype = (DataproviderObject)classifiers.get(next);
                if(supertype == null) {
                    SysLog.error("supertype " + next + " of classifier " + classifier.path() + " not found in repository");
                }
                else if(!supertype.values("subtype").contains(classifier.path())) {
                    supertype.values("subtype").add(
                        classifier.path()
                    );
                }
            }
            if(!classifier.values("subtype").contains(classifier.path())) {
                classifier.values("subtype").add(
                    classifier.path()
                );
            }
        }
    }

    //---------------------------------------------------------------------------
    /**
     * Set feature 'content' of namespaces
     */
    private void setContent(
        Map elements
    ) {    
        // clear feature subtype
        for(
                Iterator i = elements.values().iterator();
                i.hasNext();
        ) {
            DataproviderObject element = (DataproviderObject)i.next();
            element.clearValues("content");
        }

        // recalc content
        for(
                Iterator i = elements.values().iterator();
                i.hasNext();
        ) {
            DataproviderObject content = (DataproviderObject)i.next();
            if(content.getValues("container") != null) {
                DataproviderObject container = (DataproviderObject)elements.get(
                    content.values("container").get(0)
                );
                if(container == null) {
                    SysLog.error("container " + content.values("container").get(0) + " of element " + content.path() + " not found in repository");
                }
                else if(!container.values("content").contains(content.path())) {
                    container.values("content").add(
                        content.path()
                    );
                }
            }      
        } 

        // sort content by path
        // TODO: sort content by order as defined in model
        for(
                Iterator i = elements.values().iterator();
                i.hasNext();
        ) {
            DataproviderObject container = (DataproviderObject)i.next();
            if(container.getValues("content") != null) {
                Set content = new TreeSet(
                    container.values("content")
                );
                container.clearValues("content").addAll(
                    content
                );
            }
        }

    }

    //---------------------------------------------------------------------------
    /**
     * Gets all direct and indirect subtypes for a given GeneralizableElement
     * PRECONDITION: for the GeneralizableElement the attribute 'subtype'
     *               must have been computed and assigned
     */
//  private Set getAllSubtype(
//  ServiceHeader header,
//  DataproviderObject classifier,
//  Map elements
//  ) throws ServiceException {
//  Set allSubtype = new TreeSet();
//  for(
//  Iterator i = classifier.values("subtype").iterator();
//  i.hasNext();
//  ) {
//  DataproviderObject subtype = (DataproviderObject)elements.get((Path)i.next());
//  allSubtype.addAll(
//  this.getAllSubtype(
//  header,
//  subtype,
//  elements
//  )
//  );
//  }
//  allSubtype.add(
//  classifier.path()
//  );
//  return allSubtype;
//  }

    //---------------------------------------------------------------------------
    /**
     * When object is of type ModelClass completes the features allOperations,
     * allAttributes and allReferences. Precondition: allSupertype and content
     * must be set.
     */
    private Set getFeatures(
        ServiceHeader header,
        DataproviderObject classifier,
        Map elements
    ) throws ServiceException {
        Set features = new TreeSet();    
        for(
                Iterator i = classifier.values("allSupertype").iterator();
                i.hasNext();
        ) {
            DataproviderObject supertype = (DataproviderObject)elements.get(i.next());
            for(
                    Iterator j = supertype.values("content").iterator();
                    j.hasNext();
            ) {
                DataproviderObject feature = (DataproviderObject)elements.get(j.next());
                if(feature.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.FEATURE)) {
                    features.add(
                        feature.path()
                    );
                }
            }
        }
        return features;
    }

    //-------------------------------------------------------------------------
//  /**
//  * Sets the 'allSupertype' attribute of modelElement. Implicitely sets
//  * the 'allSupertype' attribute of all of its direct subtypes.
//  */
//  private void setAllSupertype(
//  DataproviderObject modelElement,
//  Map allNamespaceContent
//  ) {

//  // set supertypes of supertypes
//  for(
//  Iterator i = modelElement.values("supertype").iterator();
//  i.hasNext();
//  ) {
//  this.setAllSupertype(
//  (DataproviderObject)allNamespaceContent.get(i.next()),
//  allNamespaceContent
//  );
//  }

//  // collect allSupertype of supertypes
//  Set allSupertype = new TreeSet();
//  for(
//  Iterator i = modelElement.values("supertype").iterator();
//  i.hasNext();
//  ) {
//  DataproviderObject supertype = (DataproviderObject)allNamespaceContent.get(
//  i.next()
//  );
//  allSupertype.addAll(
//  supertype.values("allSupertype")
//  );
//  } 
//  allSupertype.add(
//  modelElement.path()
//  );   
//  modelElement.clearValues("allSupertype").addAll(
//  allSupertype
//  );
//  }

    //-------------------------------------------------------------------------
    /** 
     * Sets the 'allSubtype' attribute of modelElement. Requires that the
     * 'subtype' attribute is available.
     */
    private Set getAllSubtype(
        DataproviderObject classifier,
        Map elements
    ) {
        Set allSubtypes = new TreeSet();
        for(
                Iterator i = classifier.values("subtype").iterator();
                i.hasNext();
        ) {
            DataproviderObject subtype = (DataproviderObject)elements.get(i.next());
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
            classifier.path()
        ); 
        return allSubtypes;
    }

    //---------------------------------------------------------------------------
    /**
     * set feature 'compositeReference' of classifiers
     */
    private void setCompositeReference(
        Map classifiers,
        Map elements
    ) {

        // set 'compositeReference' of all referenced classes with composite aggregation
        for(
                Iterator i = elements.values().iterator();
                i.hasNext();
        ) {
            DataproviderObject reference = (DataproviderObject)i.next();
            if(reference.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.REFERENCE)) {
                DataproviderObject type = (DataproviderObject)classifiers.get(
                    reference.values("type").get(0)
                );
                if(type == null) {
                    SysLog.error("type " + reference.values("type").get(0) + " of reference " + reference.path() + " is not found in repository");
                }
                else {
                    DataproviderObject referencedEnd = (DataproviderObject)elements.get(
                        reference.values("referencedEnd").get(0)
                    );
                    if(referencedEnd == null) {
                        SysLog.error("association end " + reference.values("referencedEnd").get(0) + " of reference " + reference.path() + " is not found in repository");
                    }
                    else if(AggregationKind.COMPOSITE.equals(referencedEnd.values("aggregation").get(0))) {
                        type.clearValues("compositeReference").add(
                            reference.path()
                        );

                        // set 'compositeReference' for all subtypes of type
                        for(
                                Iterator j = type.values("allSubtype").iterator();
                                j.hasNext();
                        ) {
                            DataproviderObject subtype = (DataproviderObject)classifiers.get(j.next());
                            subtype.clearValues("compositeReference").add(
                                reference.path()
                            );
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
    DataproviderObject completeNames(
        DataproviderObject object
    ) throws ServiceException {

        // qualifiedName = last path component
        // name = last component of qualifiedName
        String qualifiedName = object.path().getBase();
        object.clearValues("qualifiedName").add(
            qualifiedName
        );
        object.clearValues("name").add(
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
        DataproviderObject object
    ) throws ServiceException {

        //SysLog.trace("> completeObject");

        // INSTANCEOF
        List allSupertype = ModelUtils.getallSupertype((String)object.values(SystemAttributes.OBJECT_CLASS).get(0));
        object.clearValues(SystemAttributes.OBJECT_INSTANCE_OF);
        if(allSupertype != null) {
            object.values(SystemAttributes.OBJECT_INSTANCE_OF).addAll(allSupertype);
        }

        // calculate derived attributes only if attributes have to be returned
        // and if it is a class of org:omg:model1
        if(
                (request.attributeSelector() != AttributeSelectors.NO_ATTRIBUTES) &&
                object.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(ModelAttributes.ELEMENT)
        ) {

            // Reference.referencedEndIsNavigable
            if(
                    object.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(
                        ModelAttributes.REFERENCE
                    )
            ) {
                DataproviderRequest getRequest = new DataproviderRequest(
                    new DataproviderObject((Path)object.values("referencedEnd").get(0)),
                    DataproviderOperations.OBJECT_RETRIEVAL,
                    AttributeSelectors.ALL_ATTRIBUTES,
                    null
                );
                getRequest.contexts().putAll(request.contexts());
                DataproviderObject referencedEnd = this.get(
                    header,
                    getRequest
                ).getObject();
                object.clearValues("referencedEndIsNavigable").addAll(
                    referencedEnd.values("isNavigable")
                );
            }

            // Operation.parameter
            if(
                    object.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(
                        ModelAttributes.OPERATION
                    )
            ) {
                object.clearValues("parameter").addAll(
                    this.getNamespaceContentAsPaths(
                        header,
                        object
                    )
                );
            }

            // GeneralizableElement.allSupertype
            if(
                    object.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(
                        ModelAttributes.GENERALIZABLE_ELEMENT
                    )
            ) {
                // should this statement really be empty? TODO  
            }         

            // ModelElement.name, qualifiedName
            completeNames(
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
            request.object().path().setTo(
                accessPath.getPrefix(accessPath.size()-3).getChild(accessPath.getBase())
            );

            reply = super.get(
                header,
                request
            );

            // test whether container is correct
            if(
                    !((Path)reply.getObject().values("container").get(0)).equals(
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
            reply = super.get(
                header,
                request
            );
        }

        // reset to access path
        completeReply(
            header,
            request,
            reply
        );
        reply.getObject().path().setTo(
            accessPath
        );
        return reply;

    }

    //---------------------------------------------------------------------------
    public DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {

//      Path accessPath = new Path(request.path().toString());
//      DataproviderReply reply = null;

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
                    return completeReply(
                        header,
                        request,
                        new DataproviderReply(
                            new ArrayList(
                                this.getNamespaceContent(
                                    header,
                                    new DataproviderObject(request.object().path().getParent())
                                )
                            )
                        )
                    );
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

//      Path accessPath = new Path(request.path().toString());
        String operationName = getReferenceName(request);
        DataproviderObject params = request.object();

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
                List contents = this.getNamespaceContent(
                    header,
                    new DataproviderObject(request.path().getPrefix(request.path().size()-2))
                );  
                for(
                        Iterator i = contents.iterator();
                        i.hasNext();
                ) {
                    DataproviderObject content = (DataproviderObject)i.next();
                    completeNames(
                        content
                    );
                    if(content.values("name").get(0).equals(params.values("name").get(0))) {
                        params.values("result").add(content.path());
                        break;
                    }
                }
                if(!params.containsAttributeName("result")) {
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
                List contents = this.getNamespaceContent(
                    header,
                    new DataproviderObject(request.path().getPrefix(request.path().size()-2))
                );  
                for(
                        Iterator i = contents.iterator();
                        i.hasNext();
                ) {
                    DataproviderObject content = (DataproviderObject)i.next();
                    completeNames(
                        content
                    );
                    //SysLog.trace("qualifiedName=" + content.values("qualifiedName").get(0));
                    if(content.values("qualifiedName").get(0).equals(params.values("qualifiedName").get(0))) {
                        params.values("result").add(content.path());
                        break;
                    }
                }
                if(!params.containsAttributeName("result")) {
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
                List contents = this.getNamespaceContent(
                    header,
                    new DataproviderObject(request.path().getPrefix(request.path().size()-2))
                );

                // search elements with matching types
                boolean includeSubtypes = ((Boolean)params.values("includeSubtypes").get(0)).booleanValue();
                for(
                        Iterator i = contents.iterator();
                        i.hasNext();
                ) {
                    DataproviderObject content = (DataproviderObject)i.next();

                    if(includeSubtypes) {
                        List subtypes = ModelUtils.getsubtype(
                            (String)params.values("ofType").get(0)
                        );
                        for(
                                Iterator j = subtypes.iterator();
                                j.hasNext();
                        ) {
                            if(content.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(j.next())) {
                                params.values("result").add(content.path());
                                break;
                            }
                        }
                    }
                    else if(content.values(SystemAttributes.OBJECT_INSTANCE_OF).contains(params.values("ofType").get(0))) {
                        params.values("result").add(content.path());
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
                    Model_1_3 model = new org.openmdx.model1.accessor.basic.spi.Model_1(
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
                    List formats = request.object().values("format").size() > 0
                    ? request.object().values("format")
                        : STANDARD_FORMAT;
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
                        } else {
                            mapper.externalize(
                                modelPackagePath.getBase(),
                                model,
                                zip
                            );
                        }
                    }
                    zip.close();
                    DataproviderObject result = new DataproviderObject(
                        params.path()
                    );
                    result.values(SystemAttributes.OBJECT_CLASS).add(
                        "org:omg:model1:PackageExternalizeResult"
                    );
                    result.values("packageAsJar").add(
                        bs.toByteArray()
                    );
                    return new DataproviderReply(
                        result
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
            DataproviderObject result = new DataproviderObject(
                params.path().getDescendant("reply", super.uidAsString())
            );
            result.values(SystemAttributes.OBJECT_CLASS).add(
                "org:openmdx:base:Void"
            );
            return new DataproviderReply(
                result
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
                    Iterator i = segments.iterator();
                    i.hasNext();
            ) {
                DataproviderObject segment = (DataproviderObject)i.next();
                List elements = channel.addFindRequest(
                    segment.path().getChild("element"),
                    null,
                    AttributeSelectors.ALL_ATTRIBUTES,
                    null,
                    0,
                    Integer.MAX_VALUE,
                    Directions.ASCENDING
                );
                for(
                        Iterator j = elements.iterator();
                        j.hasNext();  
                ) {
                    DataproviderObject element = (DataproviderObject)j.next();
                    completedElements.put(
                        element.path(),
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
                    Iterator i = completedElements.values().iterator();
                    i.hasNext();
            ) {
                DataproviderObject element = (DataproviderObject)i.next();
                channel.addReplaceRequest(
                    element
                );
            }
            SysLog.trace("completed elements", completedElements.size());

            // Void reply      
            DataproviderObject result = new DataproviderObject(
                params.path().getDescendant("reply", super.uidAsString())
            );
            result.values(SystemAttributes.OBJECT_CLASS).add(
                "org:openmdx:base:Void"
            );

            this.importing = false;
            return new DataproviderReply(
                result
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
