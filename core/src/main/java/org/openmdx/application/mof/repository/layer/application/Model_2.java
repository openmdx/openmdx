/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: model1 application plugin
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipOutputStream;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;
import javax.resource.spi.IllegalStateException;

import org.openmdx.application.mof.cci.ModelExceptions;
import org.openmdx.application.mof.mapping.cci.Mapper_1_0;
import org.openmdx.application.mof.mapping.cci.Mapper_1_1;
import org.openmdx.application.mof.mapping.cci.MappingTypes;
import org.openmdx.application.mof.mapping.spi.MapperFactory_1;
import org.openmdx.application.mof.repository.accessor.ModelBuilder_1;
import org.openmdx.application.mof.repository.utils.ModelUtils;
import org.openmdx.base.dataprovider.cci.Channel;
import org.openmdx.base.dataprovider.cci.DataproviderRequestProcessor;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.repository.cci.ClassifierRecord;
import org.openmdx.base.mof.repository.cci.ElementRecord;
import org.openmdx.base.mof.repository.cci.FeatureRecord;
import org.openmdx.base.mof.repository.cci.NamespaceRecord;
import org.openmdx.base.mof.repository.cci.OperationRecord;
import org.openmdx.base.mof.repository.cci.ReferenceRecord;
import org.openmdx.base.mof.repository.spi.NamespaceRecords;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.IsInCondition;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RequestRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.base.rest.spi.AbstractRestPort;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * The MOF repository's application plug-in
 */
public class Model_2 extends AbstractRestPort {

    private boolean importing = false;

    private String openmdxjdoMetadataDirectory; 
    
    protected static final Path PROVIDER_ROOT_PATH = new Path("xri://@openmdx*org.omg.model1/provider/Mof");

    protected static final List<String> STANDARD_FORMAT = Collections.unmodifiableList(
        Arrays.asList(
            MappingTypes.XMI1
        )
    );

    @Override
    public Interaction getInteraction(
        RestConnection connection
    ) throws ResourceException {
        return new RestInteraction(connection);
    }
                    
	/**
	 * @return the openmdxjdoMetadataDirectory
	 */
	public String getOpenmdxjdoMetadataDirectory() {
		return this.openmdxjdoMetadataDirectory;
	}

	/**
	 * @param openmdxjdoMetadataDirectory the openmdxjdoMetadataDirectory to set
	 */
	public void setOpenmdxjdoMetadataDirectory(String openmdxjdoMetadataDirectory) {
		this.openmdxjdoMetadataDirectory = openmdxjdoMetadataDirectory;
	}

	protected void assertImporting(
		RequestRecord request
	) throws ResourceException {
	    if(!this.importing) {
	    	throw ResourceExceptions.initHolder(
	    		new IllegalStateException(
    				"repository not in import state. Call beginImport() before importing elements",
    				BasicException.newEmbeddedExceptionStack(
			            BasicException.Code.DEFAULT_DOMAIN,
			            BasicException.Code.ILLEGAL_STATE, 
			            new BasicException.Parameter("request", request)
			        )
			    )
	        );
	    }
	}

	
	//------------------------------------------------------------------------
	// Class RestInteraction
	//------------------------------------------------------------------------
	
	/**
	 * This class provides The plug-in specific REST interaction implementation.
	 */
    @SuppressWarnings("unchecked")
    protected class RestInteraction extends AbstractRestInteraction {

    	/**
    	 * Constructor
    	 * 
    	 * @param connection
    	 * 
    	 * @throws ResourceException 
    	 */
		@SuppressWarnings({
            "synthetic-access"
        })
        protected RestInteraction(
			RestConnection connection
		) throws ResourceException {
			super(connection, newDelegateInteraction(connection));
		}

	    private String getReferenceName(
	        RequestRecord request
	    ){
	        final Path path = request.getResourceIdentifier();    
	        return (
	            path.isContainerPath() ? path : path.getParent()
	        ).getLastSegment().toClassicRepresentation();
	    }

	    private boolean isClassifier(
	    	ObjectRecord object
	    ){
	    	return isInstanceOf(object, ClassifierRecord.class);
	    }

	    private boolean isFeature(
	    	ObjectRecord object
	    ){
	    	return isInstanceOf(object, FeatureRecord.class);
	    }

	    private boolean isOperation(
	    	ObjectRecord object
	    ){
	    	return isInstanceOf(object, OperationRecord.class);
	    }
	    
	    private boolean isElement(
	    	ObjectRecord object
	    ){
	    	return isInstanceOf(object, ElementRecord.class);
	    }
	    
	    private boolean isReference(
	    	ObjectRecord object
	    ){
	    	return isInstanceOf(object, ReferenceRecord.class);
	    }

	    private boolean isInstanceOf(
	    	ObjectRecord object,
	    	Class<? extends ElementRecord> type
	    ){
	        return type.isInstance(object.getValue());
	    }
	    
		@SuppressWarnings("rawtypes")
        private void addToSet(
	    	ObjectRecord object,
	    	String feature,
	    	Object element
	    ) throws ResourceException{
	    	final List set = (List<?>) object.getValue().get(feature);
	    	if(set == null) {
	    		object.getValue().put(
	    			feature,
	    			toIndexedRecordSet(Collections.singleton(element))
	    		);
	    	} else if(!set.contains(element)) {
	    		set.add(element);
	    	}
	    }
	    
	    private void completeElements(
	        Map<Path,ObjectRecord> elements
	    ) throws ResourceException { 
	    	long startedAt = System.currentTimeMillis();    

	        Map<Path,ObjectRecord> classifiers = new HashMap<Path,ObjectRecord>();
	        for(ObjectRecord element : elements.values()){
	            // collect classifier
	            if(isClassifier(element)) {
	                classifiers.put(
                		element.getResourceIdentifier(),
	                    element
	                );
	            }
	            // qualified name
	            element.getValue().put(
	                "qualifiedName",
	                element.getResourceIdentifier().getLastSegment().toClassicRepresentation()
	            );
	        }

	        // allSupertype
	        for(ObjectRecord classifier : elements.values()){
	            if(isClassifier(classifier)) {
	                classifier.getValue().put(
	                    "allSupertype",
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
	        for(ObjectRecord classifier : elements.values()){
	        	if(isClassifier(classifier)) {
	                classifier.getValue().put(
	                    "allSubtype",
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
	        for(ObjectRecord classifier : elements.values()) {
	            if(isClassifier(classifier)) { 
	            	classifier.getValue().put(
	                    "feature",
	                    this.getFeatures(
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

	    private List<ObjectRecord> getNamespaceContent(
	        Path namespace
	    ) throws ResourceException {
	        Channel channel = newDataproviderRequestProcessor();
	        final QueryRecord query = channel.newQueryRecordWithFilter(namespace.getParent());
	        query.getQueryFilter().getCondition().add(
	        	new IsInCondition(
        			Quantifier.THERE_EXISTS,
        			"container",
        			true,
        			namespace
	        	)
	        );
	        return channel.addFindRequest(query);
	    }

		protected Channel newDataproviderRequestProcessor(
		) throws ResourceException {
			return new DataproviderRequestProcessor(
	            getPrincipalChain(),
	            Model_2.this.getDelegate()
	        );
		}

	    private IndexedRecord getNamespaceContentAsPaths(
	        Path namespace
	    ) throws ResourceException {
	        List<ObjectRecord> content = this.getNamespaceContent(
	            namespace
	        );
	        List<Path> contentAsPaths = new ArrayList<Path>();
	        for(ObjectRecord object : content) {
	            contentAsPaths.add(object.getResourceIdentifier());
	        }
	        return toIndexedRecordSet(contentAsPaths);
	    }

	    /**
	     * Gets all direct and indirect supertypes for a given Classifier
	     */
	    private IndexedRecord getAllSupertype(
	        ObjectRecord classifier,
	        Map<Path,ObjectRecord> elements
	    ) throws ResourceException {
	        Set<Path> allSupertype = new TreeSet<Path>();
	        List<Path> supertypes = (List<Path>) classifier.getValue().get("supertype");
	        if(supertypes != null){
	        	for(Path supertypeId : supertypes) {
		        	ObjectRecord supertype = elements.get(supertypeId);
		            if(supertype == null) {
		            	throw ResourceExceptions.initHolder(
		            		new ResourceException(
	    	                    "supertype not found in set of loaded models (HINT: probably not configured as 'modelPackage')",
		            			BasicException.newEmbeddedExceptionStack(	
				                    BasicException.Code.DEFAULT_DOMAIN,
				                    BasicException.Code.ASSERTION_FAILURE, 
				                    new BasicException.Parameter("classifier", classifier.getResourceIdentifier()),
				                    new BasicException.Parameter("supertype", supertypeId)
				                )
				            )
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
	        allSupertype.add(classifier.getResourceIdentifier());
	        return toIndexedRecordSet(allSupertype);
	    }

	    /**
	     * Set feature 'subtype' of classifiers
	     * @throws ResourceException 
	     */
	    private void setSubtype(
	        Map<Path,ObjectRecord> classifiers
	    ) throws ResourceException {    
	        // clear feature subtype
	        for(ObjectRecord classifier : classifiers.values()) {
	        	final List<Path> subtypeIds = (List<Path>) classifier.getValue().get("subtype");
	        	if(subtypeIds != null) {
	        		subtypeIds.clear();
	        	}
	        }
	        // recalc subtype
	        for(ObjectRecord classifier : classifiers.values()) {
	        	List<Path> supertypes = (List<Path>) classifier.getValue().get("supertype");
	            if(supertypes != null){
	            	for(Path supertypeId : supertypes){
					    ObjectRecord supertype = classifiers.get(supertypeId);
					    if(supertype == null) {
					        SysLog.error("supertype " + supertypeId + " of classifier " + classifier.getResourceIdentifier() + " not found in repository");
					    } else {
					    	addToSet(supertype, "subtype", classifier.getResourceIdentifier());
					    }
	            	}
				}
		    	addToSet(classifier, "subtype", classifier.getResourceIdentifier());
	        }
        }
	        

	    /**
	     * Set feature 'content' of namespaces
	     * @throws ResourceException 
	     */
	    private void setContent(
	        Map<Path,ObjectRecord> elements
	    ) throws ResourceException {    
	        clearContentOfNamespaces(elements);
	        calculateContentOfNamespaces(elements);

	        // sort content by path
	        // TODO: sort content by order as defined in model
	        sortContentOfNamespaces(elements);

	    }

        /**
         * This method just sorts the namespaces' content by XRI
         */
        private void sortContentOfNamespaces(Map<Path, ObjectRecord> elements) {
            for(ObjectRecord elementHolder : elements.values()){
                final ElementRecord element = (ElementRecord) elementHolder.getValue();
                if(element instanceof NamespaceRecord) {
                    final NamespaceRecord namespace = (NamespaceRecord) element;
                    NamespaceRecords.sortContent(namespace, null);
                }
            }
        }

        private void calculateContentOfNamespaces(Map<Path, ObjectRecord> elements){
            for(ObjectRecord elementHolder : elements.values()) {
                final ElementRecord element = (ElementRecord) elementHolder.getValue();
	        	final Path containerId = element.getContainer();
	            if(containerId != null) {
	                final ObjectRecord containerHolder = elements.get(containerId);
	                if(containerHolder == null) {
	                    SysLog.error("Container " + containerId + " of element " + elementHolder.getResourceIdentifier() + " not found in repository");
	                } else { 
	                    final NamespaceRecord namespace = (NamespaceRecord) containerHolder.getValue();
	                    namespace.getContent().add(elementHolder.getResourceIdentifier());
	                }
	            }      
	        }
        }

        private void clearContentOfNamespaces(Map<Path, ObjectRecord> elements) {
            for(ObjectRecord elementHolder : elements.values()){
	        	final ElementRecord element = (ElementRecord) elementHolder.getValue();
	        	if(element instanceof NamespaceRecord) {
	        	    ((NamespaceRecord)element).getContent().clear();
	        	}
	        }
        }

	    /**
	     * When object is of type ModelClass completes the features allOperations,
	     * allAttributes and allReferences. Precondition: allSupertype and content
	     * must be set.
	     * @throws ResourceException 
	     */
	    private IndexedRecord getFeatures(
    		ObjectRecord classifier,
	        Map<Path,ObjectRecord> elements
	    ) throws ResourceException {
	        final Set<Path> features = new TreeSet<Path>(); 
	        final List<?> supertypeIds = (List<?>) classifier.getValue().get("allSupertype");
	        for(Object supertypeId : supertypeIds){
	        	final ObjectRecord supertype = elements.get(supertypeId);
	        	final List<?> featureIds = (List<?>) supertype.getValue().get("content");
	            if(featureIds != null) { // TODO necessary?
		            for(Object featureId : featureIds) {
		            	final Path featurePath = (Path) featureId;
		                final ObjectRecord feature = elements.get(featurePath);
		                if(isFeature(feature)) {
		                	features.add(featurePath);
		                }
		            }
	            }
	        }
	        return toIndexedRecordSet(features);
	    }

	    /** 
	     * Sets the 'allSubtype' attribute of modelElement. Requires that the
	     * 'subtype' attribute is available.
	     */
	    private IndexedRecord getAllSubtype(
	        ObjectRecord classifier,
	        Map<Path,ObjectRecord> elements
	    ) throws ResourceException {
	        Set<Path> allSubtypes = new TreeSet<Path>();
	        List<?> subytpeIds = (List<?>) classifier.getValue().get("subtype");
	        for(Object subtypeId : subytpeIds){
	        	ObjectRecord subtype = elements.get(subtypeId);
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
        		classifier.getResourceIdentifier()	
	        ); 
	        return toIndexedRecordSet(allSubtypes);
	    }

	    /**
	     * set feature 'compositeReference' of classifiers
	     */
	    private void setCompositeReference(
	        Map<Path,ObjectRecord> classifiers,
	        Map<Path,ObjectRecord> elements
	    ){
	        // set 'compositeReference' of all referenced classes with composite aggregation
	        for(ObjectRecord reference : elements.values()) {
	            if(isReference(reference)) {
	            	ObjectRecord type = classifiers.get(
	                    reference.getValue().get("type")
	                );
	                if(type == null) {
	                    SysLog.error("type " + reference.getValue().get("type") + " of reference " + reference.getResourceIdentifier() + " is not found in repository");
	                } else {
	                    ObjectRecord referencedEnd = elements.get(
	                        reference.getValue().get("referencedEnd")
	                    );
	                    if(referencedEnd == null) {
	                        SysLog.error("association end " + reference.getValue().get("referencedEnd") + " of reference " + reference.getResourceIdentifier() + " is not found in repository");
	                    }
	                    else {
	                        if(AggregationKind.COMPOSITE.equals(referencedEnd.getValue().get("aggregation"))) {
	                            type.getValue().put(
	                                "compositeReference",
	                                reference.getResourceIdentifier()
	                            );
	                            // set 'compositeReference' for all subtypes of type
	                            List<?> subtypeIds = (List<?>) type.getValue().get("allSubtype");
	                            for(Object subtypeId : subtypeIds) {
	                                ObjectRecord subtype = classifiers.get(subtypeId);
									subtype.getValue().put("compositeReference",
									    reference.getResourceIdentifier()
									);
	                            }
	                        }
	                    }
	                }        
	            }
	        }
	    }

        /**
         * complete derived attributes
         * @throws ResourceException 
         */
        void completeObject(
            QueryRecord request,
            ObjectRecord object
        ) throws ResourceException {
            // calculate derived attributes only if attributes have to be returned
            // and if it is a class of org:omg:model1
            if(isElement(object)) {
                // Reference.referencedEndIsNavigable
                if(isReference(object)){
                    final Channel channel = newDataproviderRequestProcessor();
                    ObjectRecord referencedEnd = channel.addGetRequest((Path)object.getValue().get("referencedEnd"));
                    object.getValue().put(
                        "referencedEndIsNavigable",
                        referencedEnd.getValue().get("isNavigable")
					);
                }
                // Operation.parameter
                if(isOperation(object)) {
                	object.getValue().put(
                        "parameter",
                        this.getNamespaceContentAsPaths(
                            object.getResourceIdentifier()
                        )
                    );
                }
            }
        }

		private List<String> getSubtype(String ofType) throws ResourceException {
			try {
				return ModelUtils.getsubtype(ofType);
			} catch (ServiceException e) {
				throw ResourceExceptions.toResourceException(e);
			}
		}			
		
        //---------------------------------------------------------------------------
        void completeReply(
            QueryRecord request,
            ResultRecord reply
        ) throws ResourceException {
        	List<ObjectRecord> objects = reply;
            for(ObjectRecord object : objects) { 
                this.completeObject(
				    request,
				    object
				);
            }
            reply.setHasMore(false);
            reply.setTotal(objects.size());    
        }
    
        @Override
        public boolean get(
            RestInteractionSpec ispec,
            QueryRecord input,
            ResultRecord output
        ) throws ResourceException {
            Path accessPath = input.getResourceIdentifier();
            // get ModelElement by 'content' reference
            if("content".equals(getReferenceName(input))) {
            	final QueryRecord query = newQuery(
            		accessPath.getPrefix(accessPath.size()-3).getChild(accessPath.getLastSegment().toClassicRepresentation())
            	);
                if(!(super.get(
                    ispec,
                    query,
                    output
                ) && ((ObjectRecord)output.get(0)).getValue().get("container").equals(
				        accessPath.getPrefix(accessPath.size()-2)
				    ))
				) {
				    throw ResourceExceptions.initHolder(
				    	new ResourceException(
			    			"no such member",
			    			BasicException.newEmbeddedExceptionStack(
						        BasicException.Code.DEFAULT_DOMAIN,
						        BasicException.Code.NOT_FOUND, 
						        new BasicException.Parameter(BasicException.Parameter.XRI, accessPath)
						    )
					     )
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
            if(!output.isEmpty()) {
                ((ObjectRecord)output.get(0)).setResourceIdentifier(accessPath);
                this.completeReply(
                    input,
                    output
                );
            }
            return true;
        }

        //---------------------------------------------------------------------------
        @Override
        public boolean find(
            RestInteractionSpec ispec,
            QueryRecord input,
            ResultRecord output
        ) throws ResourceException {
            final String referenceName = getReferenceName(input);
			if("namespaceContent".equals(referenceName)) {
            	//
                // namespaceContent
            	//
            	output.addAll(
			        this.getNamespaceContent(
			            input.getResourceIdentifier().getParent()
			        )
				);
            } else {
            	//
            	// non-derived references
                // 
                super.find(
                    ispec, 
                    input, 
                    output
                );
            }
			completeReply(input, output);
			return true;
        }
    
        /* (non-Javadoc)
		 * @see org.openmdx.base.rest.spi.AbstractRestInteraction#update(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.ObjectRecord, org.openmdx.base.rest.cci.ResultRecord)
		 */
		@Override
		protected boolean update(
			RestInteractionSpec ispec, 
			ObjectRecord input,
			ResultRecord output
		) throws ResourceException {
			assertImporting(input);
			return super.update(ispec, input, output);
		}

        /* (non-Javadoc)
		 * @see org.openmdx.base.rest.spi.AbstractRestInteraction#create(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.ObjectRecord, org.openmdx.base.rest.cci.ResultRecord)
		 */
		@Override
		protected boolean create(
			RestInteractionSpec ispec, 
			ObjectRecord input,
			ResultRecord output
		) throws ResourceException {
			assertImporting(input);
			return super.create(ispec, input, output);
		}

		/* (non-Javadoc)
		 * @see org.openmdx.base.rest.spi.AbstractRestInteraction#invoke(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.cci.MessageRecord, org.openmdx.base.rest.cci.MessageRecord)
		 */
		@Override
		protected boolean invoke(
            RestInteractionSpec ispec, 
            MessageRecord input, 
            MessageRecord output
        ) throws ResourceException {
            String operationName = input.getTarget().getLastSegment().toClassicRepresentation();
            return 
            	"lookupElement".equals(operationName) ? invokeLookupElement(input, output) :
            	"resolveQualifiedName".equals(operationName) ? invokeResolveQualifiedName(input, output) :
            	"findElementsByType".equals(operationName) ? invokeFindElementsByType(input, output) :
            	"externalizeClassifier".equals(operationName) ? invokeExternalizeClassifier(input, output) :
            	"externalizePackage".equals(operationName) ? invokeExternalizePackage(input, output) :
            	"beginImport".equals(operationName) ? invokeBeginImport(input, output) :
            	"endImport".equals(operationName) ? invokeEndImport(input, output) : 
            	failForUnknownOperation(input);
		}

		private boolean failForUnknownOperation(
			MessageRecord input
		) throws ResourceException {
			final String operation = input.getTarget().getLastSegment().toClassicRepresentation();
			throw ResourceExceptions.initHolder(
        		new ResourceException(
                    "unknown operation",
    				BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE, 
                        new BasicException.Parameter(BasicException.Parameter.XRI, input.getResourceIdentifier()),
                        new BasicException.Parameter("target", input.getTarget()),
                        new BasicException.Parameter("id", input.getMessageId()),
                        new BasicException.Parameter("operation", operation)
                    )
                )
            );
		}

		/**
		 * Recalculate all derived attributes and update repository
		 */
		@SuppressWarnings("synthetic-access")
        private boolean invokeEndImport(
			MessageRecord output, MessageRecord output2
		) throws ResourceException {
			// 
			Channel channel = new DataproviderRequestProcessor(
			    null, // header,
			    Model_2.this.getDelegate()
			);
			// get full repository content ...
			List<ObjectRecord> segments = channel.addFindRequest(
			    PROVIDER_ROOT_PATH.getChild("segment")
			);
			Map<Path,ObjectRecord> completedElements = new HashMap<Path,ObjectRecord>();
			for(ObjectRecord segment : segments) {
			    List<ObjectRecord> elements = channel.addFindRequest(
			        segment.getResourceIdentifier().getChild("element")
			    );
			    for(ObjectRecord element : elements) {
			        completedElements.put(
			            element.getResourceIdentifier(),
			            element
			        );
			    }
			}
			// ... complete it ...
			completeElements(completedElements);
			// ... and replace the existing elements with the completed
			for(ObjectRecord element : completedElements.values()) {
			    channel.addUpdateRequest(
			        element
			    );
			}
			SysLog.trace("completed elements", Integer.valueOf(completedElements.size()));
			// Void reply  
			output.setBody(null);
			Model_2.this.importing = false;
			return true;
		}

		/**
		 * Start the import
		 */
		@SuppressWarnings("synthetic-access")
        private boolean invokeBeginImport(
			MessageRecord input,
			MessageRecord output
		) {
			Model_2.this.importing = true;
			output.setResourceIdentifier(newResponseId(input.getResourceIdentifier()));
			output.setBody(null);
			return true;
		}

        @SuppressWarnings({
            "rawtypes",
            "synthetic-access"
        })
		private boolean invokeExternalizePackage(
			MessageRecord input,
			MessageRecord output
		) throws ResourceException {
			try {
			    SysLog.trace("activating model");
			    Model_1_0 model = new ModelBuilder_1(false, Model_2.this.getDelegate()).build();
			    Path modelPackagePath = input.getResourceIdentifier().getParent().getParent();
			    String qualifiedPackageName = modelPackagePath.getLastSegment().toClassicRepresentation();
   
			    // only test for existence if not wildcard export
			    if(
			        (qualifiedPackageName.indexOf("%") < 0) &&
			        (model.findElement(modelPackagePath) == null)
			    ) {
			    	throw ResourceExceptions.initHolder(
			    		new ResourceException(
			                "package does not exist",
							BasicException.newEmbeddedExceptionStack(
			                    ModelExceptions.MODEL_DOMAIN,
			                    ModelExceptions.PACKAGE_TO_EXTERNALIZE_DOES_NOT_EXIST,
			                    new BasicException.Parameter("package", modelPackagePath.toString())
			                )
			            )
			        );
			    }
			    SysLog.trace("Verify model constraints");
			    new ModelConstraintsChecker_2(model.getRepository()).verify();
                try(ByteArrayOutputStream bs = new ByteArrayOutputStream()){
    			    try(ZipOutputStream zip = new ZipOutputStream(bs)){
                        final List requestedFormat = (List) input.getBody().get("format");
        			    List<String> formats = requestedFormat.isEmpty() ? STANDARD_FORMAT : requestedFormat;
        			    for(String format : formats) {
        			        Mapper_1_0 mapper = MapperFactory_1.create(format);
        			        if(
        			            Model_2.this.openmdxjdoMetadataDirectory != null && 
        			            mapper instanceof Mapper_1_1
        			        ){
        			            ((Mapper_1_1)mapper).externalize(
        			                modelPackagePath.getLastSegment().toClassicRepresentation(),
        			                model,
        			                zip,
        			                Model_2.this.openmdxjdoMetadataDirectory
        			            );
        			        } 
        			        else {
        			            mapper.externalize(
        			                modelPackagePath.getLastSegment().toClassicRepresentation(),
        			                model,
        			                zip
        			            );
        			        }
        			    }
    			    }
    			    output.setResourceIdentifier(input.getResourceIdentifier());
    			    MappedRecord body = Records.getRecordFactory().createMappedRecord("org:omg:model1:PackageExternalizeResult");
    			    output.setBody(body);
    			    body.put("packageAsJar", bs.toByteArray());
                }
			    return true;
			} catch (ServiceException exception) {
				throw ResourceExceptions.toResourceException(exception);
			} catch(IOException e) {
			    throw ResourceExceptions.toResourceException(e);
			}
		}

		/**
		 * Seems to do nothing
		 */
		private boolean invokeExternalizeClassifier(
			MessageRecord input,
			MessageRecord output
		) {
			output.setBody(input.getBody());
			return true;
		}

		/**
		 * Get content ...
		 */
		private boolean invokeFindElementsByType(
			MessageRecord input,
			MessageRecord output
		) throws ResourceException {
			List<ObjectRecord> contents = getNamespaceContent(
			    input.getResourceIdentifier().getPrefix(input.getResourceIdentifier().size()-2)
			);
			// search elements with matching types
			String ofType = (String) input.getBody().get("ofType");
			boolean includeSubtypes = ((Boolean)input.getBody().get("includeSubtypes")).booleanValue();
			List<Path> result = new ArrayList<Path>();
			
			for(ObjectRecord content : contents) {
			    if(includeSubtypes) {
			        List<String> subtypes = getSubtype(ofType);
			        for(String subtype : subtypes) {
			        	if(isInstanceOf(content, subtype)){
			            	result.add(content.getResourceIdentifier());
			        	}
			        }
			    } else if(isInstanceOf(content, ofType)) {
			    	result.add(content.getResourceIdentifier());
				}
			}
			output.getBody().put("ofType", ofType);
			output.getBody().put("result", toIndexedRecordSet(result));
			return true;
		}

		/**
         * @param content
         * @param subtype
         * @return
         */
        private boolean isInstanceOf(ObjectRecord content, String subtype) {
            throw new UnsupportedOperationException("Is " + content.getValue().getRecordName() + " an instance of " + subtype);
        }

        /**
		 * Get content
		 * <p>
		 * Search elements with matching qualifiedName
		 */
		private boolean invokeResolveQualifiedName(
			MessageRecord input,
			MessageRecord output
		) throws ResourceException {
			List<ObjectRecord> contents = getNamespaceContent(
			    input.getResourceIdentifier().getPrefix(input.getResourceIdentifier().size()-2)
			);  
			final Object qualifiedName = input.getBody().get("qualifiedName");
			List<Path> result = new ArrayList<Path>();
			for(ObjectRecord content : contents){
			    if(content.getValue().get("qualifiedName").equals(qualifiedName)) {
			    	result.add(content.getResourceIdentifier());
				    break;
				}
			}
			if(result.isEmpty()) {
				throw ResourceExceptions.initHolder(
					new ResourceException(
						"element not found",
						BasicException.newEmbeddedExceptionStack(
			                BasicException.Code.DEFAULT_DOMAIN,
			                BasicException.Code.NOT_FOUND, 
			                new BasicException.Parameter("params", input)
			            )
			        )
			    );
			}
			output.getBody().put("qualifiedName", qualifiedName);
			output.getBody().put("result", toIndexedRecordSet(result));
			return true;
		}

		/**
		 * Get content
		 * <p>
		 * Search elements with matching name
		 */
		private boolean invokeLookupElement(
			MessageRecord input,
			MessageRecord output
		) throws ResourceException {
			List<ObjectRecord> contents = getNamespaceContent(
			    input.getResourceIdentifier().getPrefix(input.getResourceIdentifier().size()-2)
			);  
			final Object name = input.getBody().get("name");
			List<Path> result = new ArrayList<Path>();
			for(ObjectRecord content : contents){
				if(content.getValue().get("name").equals(name)) {
				    result.add(content.getResourceIdentifier());
				    break;
				}
			}
			if(result.isEmpty()) {
				throw ResourceExceptions.initHolder(
					new ResourceException(
						"element not found",
						BasicException.newEmbeddedExceptionStack(
			                BasicException.Code.DEFAULT_DOMAIN,
			                BasicException.Code.NOT_FOUND, 
			                new BasicException.Parameter("params", input)
			            )
			        )
			    );
			}
			output.getBody().put("name", name);
			output.getBody().put("result", toIndexedRecordSet(result));
			return true;
		}

    }
    
}
