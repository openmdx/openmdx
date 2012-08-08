/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ProviderTraverser.java,v 1.60 2009/02/24 15:48:54 hburger Exp $
 * Description: Traversing a provider
 * Revision:    $Revision: 1.60 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/24 15:48:54 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2006, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.exporter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.application.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.Directions;
import org.openmdx.application.dataprovider.cci.RequestCollection;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.mof.cci.AggregationKind;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.PathComponent;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.BasicException.Parameter;
import org.openmdx.kernel.log.SysLog;

/**
 * Class which traverses a provider and invokes a TraversalHandler in the 
 * correct sequence. The sequence is important for correct treatment of 
 * states and roles.
 */
@SuppressWarnings("unchecked")
public class ProviderTraverser 
implements Traverser {

    //-----------------------------------------------------------------------
    /**
     * Initialize a provider traverser with the needed information.
     * <p>
     * None of the parameters are allowed to be null, a NullPointerException is
     * thrown if there is one null.
     * 
     * @param header                request header to access provider
     * @param provider              provider holding data
     * @param model                 model of the source data
     * @param startPoints           collection containing paths to traverse from
     * the traversal follows the order of the paths
     * @throws NullPointerException if any of the parameters is null
     */
    public ProviderTraverser(
        ServiceHeader header,
        Dataprovider_1_0 provider,
        Model_1_0 model,
        List<Path> startPoints,
        Set<String> referenceFilter,
        Map<String,FilterProperty[]> attributeFilter
    ) throws java.lang.NullPointerException {
        this(
            new RequestCollection(header, provider),
            model,
            startPoints,
            referenceFilter,
            attributeFilter
        );
    }

    //-----------------------------------------------------------------------
    public ProviderTraverser(
        RequestCollection reader,
        Model_1_0 model,
        List<Path> startPoints,
        Set<String> referenceFilter,
        Map<String,FilterProperty[]> attributeFilter
    ) throws NullPointerException {
        if (reader == null) {
            throw new NullPointerException("reader must not be null");
        }
        this.reader = reader;
        if (model == null) {
            throw new NullPointerException("model must not be null");
        }
        this.model = model;
        if (startPoints == null || startPoints.size() == 0) {
            throw new NullPointerException("sourcePaths must not be null and not empty.");
        }
        this.startPoints = startPoints;
        this.referenceFilter = referenceFilter;
        this.attributeFilter = attributeFilter;
    }

    //-----------------------------------------------------------------------
    /**
     * set the traversalHandler.
     * 
     * @see org.openmdx.compatibility.base.dataprovider.exporter.Traverser#setTraversalHandler(org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler)
     */
    public void setTraversalHandler(TraversalHandler th) {
        if (th != null) {
            this.traversalHandler = th;
        }
        else {
            throw new NullPointerException();
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Get the traversalHandler. 
     * 
     * @see org.openmdx.compatibility.base.dataprovider.exporter.Traverser#getTraversalHandler()
     */
    public TraversalHandler getTraversalHandler() {
        return this.traversalHandler;
    }

    //-----------------------------------------------------------------------
    /**
     * Set the errorHandler.
     * 
     * @see org.openmdx.compatibility.base.dataprovider.exporter.Traverser#setErrorHandler(org.openmdx.compatibility.base.dataprovider.exporter.ErrorHandler)
     */
    public void setErrorHandler(ErrorHandler eh) {
        if (eh != null) {
            this.errorHandler = eh;
        }
        else {
            throw new NullPointerException();
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Get the errorHandler.
     * 
     * @see org.openmdx.compatibility.base.dataprovider.exporter.Traverser#getErrorHandler()
     */
    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    //-----------------------------------------------------------------------
    /** 
     * Set the transaction behavior.
     * <p>
     * The only behavior supported is NO_TRANSACTION 
     *
     * @param transactionBehavior
     * @throws ServiceException
     */
    public void setTransactionBehavior(short transactionBehavior)
    throws ServiceException {
        if (transactionBehavior != TraversalHandler.NO_TRANSACTION) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "The only transaction behavior supported is NO_TRANSACTION",
                new Parameter[] {
                    new Parameter("transactionBehavior", transactionBehavior)});
        }
    }

    /**
     * Get the transaction behavior.
     * 
     */
    public short getTransactionBehavior() {
        return TraversalHandler.NO_TRANSACTION;
    }

    //-----------------------------------------------------------------------
    /**
     * Start the traversal after all configurations have been setup.
     * 
     * @see org.openmdx.compatibility.base.dataprovider.exporter.Traverser#traverse()
     */
    public void traverse(
    ) throws ServiceException {
        boolean ending = false;
        try {
            this.getTraversalHandler().startTraversal(this.startPoints);
            if(!this.startPoints.isEmpty()) {
                Path objectPath = null;
                Path lastStartPath = null;
                int ii = 0;
                for(
                        Iterator<Path> i = this.startPoints.iterator(); 
                        i.hasNext();
                        ii++
                ) {
                    Path startPoint = i.next();
                    lastStartPath = objectPath;
                    objectPath = startPoint;
                    SysLog.trace("starting traversal from: " + objectPath);
                    ModelElement_1_0 classOfLast = this.traverseParents(
                        lastStartPath, 
                        objectPath
                    );
                    if (objectPath.size() < 5) {
                        throw new ServiceException( 
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ACTIVATION_FAILURE,
                            "Can handle only one segment, path must be at least of size 5",
                            new Parameter[] { 
                                new Parameter("path", objectPath)
                            }
                        );
                    } else if (objectPath.size() % 2 == 0) {
                        this.traverseFromReference(
                            objectPath, 
                            classOfLast 
                        );
                    } else {
                        this.startTraversalAtObject(
                            objectPath.getParent(),
                            objectPath 
                        );
                    }
                    this.traversedPaths.add(objectPath);
                }
                this.traverseParents(objectPath, null);
            }
            ending = true;
            this.traversalHandler.endTraversal();
        } 
        catch(java.lang.Exception ex) {
            // all exceptions that get here are fatal, because the position to 
            // continue is lost. 
            this.handleFatalError(ex);
            // Fatal errors can just be thrown from anywhere out of the code.
            ServiceException se = new ServiceException(ex); 
            if(!ending) try {
                this.traversalHandler.endTraversal();
            } catch (ServiceException ee) {
                ee.getCause(null).initCause(se);
                se = ee;
            }
            throw se;
        }        
    }

    //-----------------------------------------------------------------------
    /**
     * Start traversal at the object denoted by startPath.
     * 
     * @param objectPath
     * @throws ServiceException
     * @throws Exception
     */
    protected void startTraversalAtObject(
        Path reference,
        Path objectPath
    ) throws ServiceException, Exception {
        List<DataproviderObject_1_0> objects = this.findObjectAndStates(objectPath);
        if(objects != null) {
            if(!objects.isEmpty()) {
                DataproviderObject_1_0 object = objects.get(0);
                ModelElement_1_0 objectClass = this.model.getDereferencedType(
                    object.getValues(SystemAttributes.OBJECT_CLASS).get(0)
                );
                this.traverseObject(
                    reference,
                    objectPath, 
                    objects, 
                    objectClass, 
                    this.model.isSubtypeOf(objectClass, BASIC_STATE) // IS_STATE
                );
            }
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Find objects and states at the startPath. startPath must not lead to an
     * object but rather to a reference.
     * <p>
     * All objects and all valid states at startPath are searched for. If 
     * identity is provided just this one object is searched for, including
     * its states. This helps reducing the number of objects included in 
     * the reply.
     * 
     * @param reference  path to search from
     * @param identity   just search for this object incl. its states
     * @return List      
     * @throws ServiceException
     */
    protected List<DataproviderObject_1_0> findAtPath(
        Path reference,
        Path identity
    ) throws ServiceException {
        List<DataproviderObject_1_0> objects = null;
        boolean matches = this.referenceFilter == null;
        if(!matches) {
            try {
                ModelElement_1_0 referenceDef = this.model.getReferenceType(reference);
                if(referenceDef != null) {
                    String referenceName = (String)referenceDef.objGetValue("name");
                    String qualifiedReferenceName = (String)referenceDef.objGetValue("qualifiedName");
                    if(
                        this.referenceFilter.contains(referenceName) ||
                        this.referenceFilter.contains(qualifiedReferenceName)
                    ) {
                        matches = true;
                    }            
                }
            } catch(ServiceException e) {}
        }
        if(matches) {
            try {            
                FilterProperty[] filterProperties = this.attributeFilter == null ? 
                    null : 
                    this.attributeFilter.get(reference);
                if(filterProperties != null) {
                    objects = this.reader.addFindRequest(
                        reference,
                        filterProperties,
                        AttributeSelectors.ALL_ATTRIBUTES,
                        null,
                        0,
                        Integer.MAX_VALUE,
                        Directions.ASCENDING
                    );
                }    
                // Get valid object states of _ALL_ objects or the specific one,
                // if identity is provided.     
                // This is just a dummy request to have a filter containing 
                // an object_validTo request, otherwise the operation will only be 
                // executed on the current date (header.requestedFor, 
                // header.requestedAt)
                else {
                    ModelElement_1_0 referenceType = this.model.getReferenceType(reference);
                    if(referenceType != null) {
                        ModelElement_1_0 referencedEnd = this.model.getElement(referenceType.objGetValue("referencedEnd"));
                        if(referencedEnd != null) {
                            ModelElement_1_0 objectClass = this.model.getElementType(
                                referencedEnd
                            );
                            boolean stated = this.model.isSubtypeOf(objectClass, BASIC_STATE); 
                            objects = this.reader.addFindRequest(
                                reference,
                                !stated ? null : 
                                identity == null ? new FilterProperty[]{
                                    IS_STATE
                                } : new FilterProperty[]{
                                    IS_STATE,
                                    new FilterProperty(
                                        Quantors.THERE_EXISTS,
                                        SystemAttributes.OBJECT_IDENTITY,
                                        FilterOperators.IS_IN,
                                        identity
                                    )
                                },
                                AttributeSelectors.ALL_ATTRIBUTES,
                                0,
                                Integer.MAX_VALUE,
                                Directions.ASCENDING
                            );
                        }
                    }
                }               
            }
            // Don't care about NOT_FOUND and AUTHORIZATION_FAILURE exceptions
            catch(Exception e) {
                ServiceException e0 = new ServiceException(e);
                if(
                        (e0.getExceptionCode() != BasicException.Code.NOT_FOUND) &&
                        (e0.getExceptionCode() != BasicException.Code.AUTHORIZATION_FAILURE)                    
                ) {
                    throw e0;
                }
                else {
                    objects = new ArrayList<DataproviderObject_1_0>();
                }
            }
        }
        return objects;
    }

    //-----------------------------------------------------------------------
    /**
     * Get object and all states.
     * <p>
     * This first gets the object through a direct get request. If the
     * object is stated, the states have to be accessed through  
     * findAtPath(), which could lead to problems if that path is 
     * is not exposed by any provider.
     *
     * @param objectPath  path to search from
     * @return List      
     * @throws ServiceException
     */
    protected List<DataproviderObject_1_0> findObjectAndStates(
        Path objectPath
    ) throws ServiceException {
        List<DataproviderObject_1_0> objects = new ArrayList<DataproviderObject_1_0>();
        try {
            if(objectPath.size() % 2 == 0) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "Path does not lead to an object.",
                    new Parameter[] {
                        new Parameter(
                            "objectPath",
                            objectPath)
                    }
                );
            }
            // first get the object
            DataproviderObject_1_0 object = 
                this.reader.addGetRequest(objectPath, AttributeSelectors.ALL_ATTRIBUTES, null);
            // if it is a stated object, try getting the states
            if (this.model.isSubtypeOf(object.getValues(SystemAttributes.OBJECT_CLASS).get(0), BASIC_STATE)) {
                // try getting the object and all its states. 
                // This may lead to a problem if the objects parent path is
                // not an exposed path of any of the providers.
                objects = this.findAtPath(objectPath.getParent(), objectPath);
            }
            else {
                // the object is all there is, no need to get the states
                objects.add(object);
            }
        }
        // don't care about NOT_FOUND and AUTHORIZATION_FAILURE exceptions
        catch (ServiceException e) {
            if(
                    (e.getCause().getExceptionCode() != BasicException.Code.NOT_FOUND) &&
                    (e.getCause().getExceptionCode() != BasicException.Code.AUTHORIZATION_FAILURE)
            ) {
                throw e;
            }
        }
        return objects;
    }

    //-----------------------------------------------------------------------
    /** 
     * Traverse all the objects starting at startPath.
     * <n>
     * Any exceptions thrown here will lead to a fatal exception.
     * 
     * @param reference   path ending in a reference
     * 
     * @return collection with ids of referenced objects
     * 
     * @throws Exception  
     */
    protected Collection<Path> traverseFromReference(
        Path reference,
        ModelElement_1_0 referenceType
    ) throws Exception {
        List<Path> containedIds = new ArrayList<Path>();
        List<DataproviderObject_1_0> objects = this.findAtPath(
            reference, 
            null 
        );
        if(objects != null) {
            if(!objects.isEmpty()) {
                this.startReference(referenceType);
            }
            // traverse object (states) recursively
            for(Iterator<DataproviderObject_1_0> i = objects.listIterator(); i.hasNext();) {
                DataproviderObject object = new DataproviderObject(
                    i.next()
                );                   
                ModelElement_1_0 objectClass =
                    this.model.getDereferencedType(
                        object.values(SystemAttributes.OBJECT_CLASS).get(0)
                    );                
                if(objectClass == null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "Unknown object class.",
                        new Parameter[] {
                            new Parameter("object class", object.values(SystemAttributes.OBJECT_CLASS).get(0)),
                            new Parameter("object path", object.path())
                        });
                }
                // it is a stated object, the id of the object must be provided in the first state
                boolean continueTraversal = true;
                if(this.model.objectIsSubtypeOf(object, BASIC_STATE)) {
                    Path objectPath = getStatelessPath(object.path());
                    if (!containedIds.contains(objectPath)) {
                        containedIds.add(objectPath);
                        continueTraversal = this.traverseObject(
                            reference,
                            objectPath, 
                            objects, 
                            objectClass, 
                            true // isState 
                        );
                    }
                    // else nothing, object and all its states already treated
                }
                else {
                    containedIds.add(object.path());
                    List<DataproviderObject_1_0> nonStatedObjects = new ArrayList<DataproviderObject_1_0>();
                    nonStatedObjects.add(object);
                    continueTraversal = this.traverseObject(
                        reference,
                        object.path(), 
                        nonStatedObjects, 
                        objectClass, 
                        false // isState 
                    );
                }
                if(!continueTraversal) break;
            }
            if(!objects.isEmpty()) {
                this.completeReference(
                    reference, 
                    containedIds
                );
                this.endReference(referenceType);
            }
        }
        return containedIds;
    }

    //-----------------------------------------------------------------------
    /**
     * Traverse the states of an object. With new specification each state
     * is treated as an object of its own. 
     * <p>
     * The states contained in allStates don't belong just to the one object 
     * denoted by the objectPath.
     * 
     * @param objectPath   path of the object 
     * @param allStates    states of all the objects at this level.
     * @param objectClass  class of the object at the object path
     * 
     * @return boolean
     * @throws Exception
     */
    protected boolean traverseObjectStates(
        Path reference,
        Path objectPath,
        List<DataproviderObject_1_0> allStates,
        ModelElement_1_0 objectClass
    ) throws java.lang.Exception {
        boolean continueTraversal = true;
        List<DataproviderObject_1_0> validFromSorted = new ArrayList<DataproviderObject_1_0>();        
        for(
                Iterator<DataproviderObject_1_0> s = allStates.iterator(); 
                s.hasNext() && continueTraversal;
        ) {
            DataproviderObject_1_0 state = s.next();
            // if it is a state of the object treated right now
            if (state.path().toString().startsWith(objectPath.toString())) {                
                int pos = -1; 
                if (state.getValues("object_validFrom") != null &&
                        !state.getValues("object_validFrom").isEmpty()
                ) {
                    String validFrom = 
                        (String) state.getValues("object_validFrom").get(0);                    
                    for (int i = 0; i < validFromSorted.size() && pos == -1; i++) {
                        List<Object> current = 
                            validFromSorted.get(i).getValues("object_validFrom");                                        
                        if (current != null 
                                && current.get(0) != null
                                && validFrom.compareTo((String) current.get(0)) < 0
                        ) {
                            pos = i;
                        }
                    }
                    if (pos == -1) {
                        pos = validFromSorted.size();
                    }
                }
                else {
                    // empty validFrom in state
                    pos = 0;
                }

                if (pos < validFromSorted.size()) {
                    validFromSorted.add(pos, state);
                }
                else {
                    validFromSorted.add(state);
                }
            }
        }        
        for(Iterator<DataproviderObject_1_0> s = validFromSorted.iterator(); s.hasNext(); ) {                
            DataproviderObject_1_0 state = s.next();
            continueTraversal = this.startObject(
                reference,
                state.path(), 
                objectClass, 
                TraversalHandler.SET_OP
            );
            if (continueTraversal) {
                continueTraversal = this.completeFeatures(
                    reference,
                    state, 
                    objectClass
                );
            }
            this.endObject(objectClass);
        }
        return continueTraversal;
    }

    //-----------------------------------------------------------------------
    /**
     * Traverse the object at path. This includes traversing all the states 
     * of the object from the object states of this object. 
     * Attention: objectStates contains all states of all the objects at that 
     * path. Thus the correct states must be searched for with the help of the
     * path.
     * 
     * @param objectPath path to traverse from
     * @param objectStates all states of all objects at path  to traverse
     * @param objectClass class of that object
     * @param isState if it is a state rather than an object
     * @return true if traversal should be continued, false otherwise
     * @throws ServiceException 
     */
    protected boolean traverseObject(
        Path reference,
        Path objectPath,
        List<DataproviderObject_1_0> objectStates,
        ModelElement_1_0 objectClass,
        boolean isState
    ) throws Exception {
        boolean continueTraversal = true;
        List<Path> references = null;
        if(isState) {
            // first all states (just the attributes)            
            continueTraversal = this.traverseObjectStates(
                reference,
                objectPath, 
                objectStates, 
                objectClass
            );            
            // then the contained objects
            if(continueTraversal) {
                continueTraversal = this.startObject(
                    reference,
                    objectPath, 
                    objectClass, 
                    TraversalHandler.SET_OP
                );            
                if(continueTraversal) {
                    // need an empty object for featureComplete
                    DataproviderObject empty = new DataproviderObject(
                        objectPath
                    );
                    empty.values(SystemAttributes.OBJECT_CLASS).set(
                        0, 
                        objectClass.objGetValue("qualifiedName")
                    );                    
                    continueTraversal = this.completeFeatures(
                        reference,
                        empty, 
                        objectClass
                    );                  
                    if(continueTraversal) {
                        references = this.traverseContent(
                            objectPath, 
                            objectClass 
                        );                    
                        this.completeContent(
                            objectPath, 
                            objectClass, 
                            references
                        );
                    }
                    this.endObject(objectClass);
                }                
            }
        }
        else {
            continueTraversal = this.startObject(
                reference,
                objectPath, 
                objectClass, 
                TraversalHandler.SET_OP
            );
            if(continueTraversal) {                
                DataproviderObject state = new DataproviderObject(
                    objectStates.get(0)
                );
                continueTraversal = this.completeFeatures(
                    reference,
                    state, 
                    objectClass
                );
                if(continueTraversal) {
                    references = this.traverseContent(
                        state.path(), 
                        objectClass 
                    );
                    this.completeContent(
                        state.path(), 
                        objectClass, 
                        references
                    );
                }
                this.endObject(objectClass);
            }
        }
        return continueTraversal;
    }

    //-----------------------------------------------------------------------
    /**
     * Traverse all the contained objects.
     * <p>
     * The roleCompositePaths list contains paths relative to the objectPath,
     * of roles with a composite reference. The realtive paths are string arrays.
     * 
     * @param objectPath    path leading to the containing object
     * @param objectClass   the objects class
     * @param roles         names of the roles contained in the object
     * @return List         list containing the referenced paths 
     * @throws Exception
     */
    protected List<Path> traverseContent(
        Path objectPath,
        ModelElement_1_0 objectClass
    ) throws Exception {
        List<?> features = objectClass.objGetList("feature");
        List<Path> existingReferences = new ArrayList<Path>();
        for (int roleTypesFirst = 0; roleTypesFirst < 2; roleTypesFirst++) {
            for (int j = 0; j < features.size(); j++) {
                String featureName = ((Path)features.get(j)).getBase();
                ModelElement_1_0 featureDef = this.model.getElement(featureName);
                if (this.model.isReferenceType(featureDef)) {
                    // Navigate composite references only in case there is no reference filter specified
                    if(
                        (this.isCompositeReference(featureDef) || (this.isSharedReference(featureDef) && (this.referenceFilter != null))) && 
                        ((roleTypesFirst == 0 && this.isReferencedEndARoleType(featureDef)) || (roleTypesFirst == 1 && !isReferencedEndARoleType(featureDef)))  
                    ) {
                        String referenceName = (String) featureDef.objGetValue("name");
//                      if(!"context".equals(referenceName)) { 
//                          //  
//                          // do not navigate object views
//                          //  
                            boolean matches = this.referenceFilter == null;
                            if(!matches) {
                                String qualifiedReferenceName = (String) featureDef.objGetValue("qualifiedName");
                                matches = 
                                    this.referenceFilter.contains(referenceName) || 
                                    this.referenceFilter.contains(qualifiedReferenceName);
                            }
                            if(matches) {
                                Collection<Path> referencedObjs =
                                    this.traverseFromReference(
                                        objectPath.getChild(referenceName),
                                        featureDef
                                    );    
                                if (referencedObjs.size() > 0) {
                                    existingReferences.add(
                                        objectPath.getChild(referenceName));
                                }
                            }
//                      }
                    }
                }
            }
        }
        return existingReferences;
    }

    //-----------------------------------------------------------------------
    /**
     * Detect if the attribute is content of one of the classes specified. 
     * <p>
     * Inherited attributes are not considered. The attribute must be of the
     * class itself.
     * <p>
     * attribute may be an attribute or a non contained reference.
     * 
     * @param attribute
     * @param classes
     * @return boolean
     * @throws ServiceException
     */
    protected boolean isContentAttribute(
        String attribute, 
        List<DataproviderObject_1_0> classes
    ) throws ServiceException {
        ModelElement_1_0 elementType = null;
        boolean isAttribute = false;
        boolean isNonCompositeReference = false;
        for (
                Iterator<DataproviderObject_1_0> classIter = classes.iterator();
                classIter.hasNext() && elementType == null;
        ) {
            DataproviderObject_1_0 contentClass = classIter.next();
            List<Object> content = contentClass.getValues("content");
            // first the attributes and referenced objects
            for (int j = 0; j < content.size() && elementType == null; j++) {
                String contentName = ((Path) content.get(j)).getBase();
                if (contentName
                        .endsWith(PathComponent.FIELD_DELIMITER + attribute)
                ) {
                    elementType = this.model.getElement(contentName);
                }
            }
        }
        // must check if it is an attribute or a reference
        if (elementType != null) {
            // attribute
            if (elementType
                    .objGetValue(SystemAttributes.OBJECT_CLASS)
                    .equals("org:omg:model1:Attribute")) {
                isAttribute = true;
            }
            // reference, only non contained references are valid 
            else if (
                    elementType.objGetValue(SystemAttributes.OBJECT_CLASS).equals(
                        "org:omg:model1:Reference")) {
                // also derived references are exported. 
                // they have to be treated in the import.
                if (!isCompositeReference(elementType)) {
                    isNonCompositeReference = true;
                }
            }
        }
        SysLog.trace(
            "isContentAttribute: attribute "
            + attribute
            + " elementType: "
            + elementType);

        return isAttribute || isNonCompositeReference;
    }

    //-----------------------------------------------------------------------
    /**
     * Log with indentation to better follow the single objects.
     * 
     * @param deltaIndent  -1 to outdent, 1 to indent, 0 to stay at current level
     * @param message      message to printout
     */
    private void logIndent(
        int deltaIndent, 
        String message
    ) {
        StringBuilder indentBuf = new StringBuilder();
        if (deltaIndent > 0) {
            indent += deltaIndent;
        }
        for (int i = 0; i < indent; i++) {
            indentBuf.append(" ");
        }
        if (deltaIndent < 0) {
            indent += deltaIndent;
        }
        indentBuf.append(message);
        SysLog.detail(indentBuf.toString());
    }

    //-----------------------------------------------------------------------
    /**
     * Call the startObject of the handler.
     * 
     * @param obj            object which starts
     * @param objectClass    class of the object which starts
     * @return boolean       return value from handler
     * @throws ServiceException
     */
    protected boolean startObject(
        Path reference,
        Path objectPath,
        ModelElement_1_0 objectClass,
        short operation
    ) throws ServiceException {        
        boolean result = this.getTraversalHandler().startObject(
            reference,
            (String) objectClass.objGetValue("qualifiedName"),
            getQualifierLeadingToClass(objectClass),
            objectPath.getBase(),
            operation
        );
        return result;
    }

    //-----------------------------------------------------------------------
    /**
     * Call the endObject of the handler
     * 
     * @param objectClass         class of object which ends here
     * @throws ServiceException  
     */
    protected void endObject(
        ModelElement_1_0 objectClass
    ) throws ServiceException {
        logIndent(
            -1,
            "endObject:"
            + (String) objectClass.objGetValue("qualifiedName")
        );

        this.getTraversalHandler().endObject(
            (String) objectClass.objGetValue("qualifiedName")
        );
    }

    //-----------------------------------------------------------------------
    /**
     * Call the featureComplete of the handler.
     * 
     * @param object    prepared out object
     * @param objectClass  class of the object
     * @return boolean     return value from the handler
     */
    protected boolean completeFeatures(
        Path reference,
        DataproviderObject_1_0 object,
        ModelElement_1_0 objectClass
    ) throws ServiceException {
        boolean fcResult =  getTraversalHandler().featureComplete(
            reference,
            object
        );
        return fcResult;
    }

    //-----------------------------------------------------------------------
    /**
     * Call the contentComplete of the handler.
     * 
     * @param objectPath          path of the object the map belongs to
     * @param referenceMap        map containing the referencePaths of the
     * referenced objects by reference.
     * @throws ServiceException
     */
    protected void completeContent(
        Path objectPath,
        ModelElement_1_0 objectClass,
        List<Path> containedReferences
    ) throws ServiceException {
        getTraversalHandler().contentComplete(
            objectPath,
            (String) objectClass.objGetValue("qualifiedName"),
            containedReferences
        );
    }

    //-----------------------------------------------------------------------
    /**
     * Call the referenceComplete of the handler
     * @param reference
     * @param objectIds
     * @throws ServiceException
     */
    protected void completeReference(
        Path reference, 
        Collection<Path> objectIds
    ) throws ServiceException {
        logIndent(0, "referenceComplete: " + reference + " ids: " + objectIds);
        this.getTraversalHandler().referenceComplete(reference, objectIds);
    }

    //-----------------------------------------------------------------------
    /**
     * Call the startReference of the handler
     * 
     * @param qualifiedName
     * @return boolean
     */
    protected boolean startReference(
        ModelElement_1_0 referenceType
    ) throws ServiceException {

        if (referenceType != null) {
            logIndent(
                1,
                "startReference: "
                + (String) referenceType.objGetValue("qualifiedName"));
            return getTraversalHandler().startReference(
                (String) referenceType.objGetValue("qualifiedName"));
        }
        else {
            return getTraversalHandler().startReference(null);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Call the endReference of the handler
     * 
     */
    protected void endReference(
        ModelElement_1_0 referenceType
    ) throws ServiceException {

        if (referenceType != null) {
            logIndent(
                -1,
                "endReference: "
                + (String) referenceType.objGetValue("qualifiedName"));
            getTraversalHandler().endReference(
                (String) referenceType.objGetValue("qualifiedName"));
        }
        else {
            getTraversalHandler().endReference(null);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Create a new path without state information from a path which has state
     * information.
     * 
     * statefullPath is expected to be of the form ..../katalog/001;state=0
     * 
     * @throws serviceException if path is not statefull
     */
    protected Path getStatelessPath(Path statefullPath) throws ServiceException {
        int pos = statefullPath.getBase().indexOf(";state=");
        if(pos < 0 ) { // TODO
            return statefullPath;
        }      
        Path stateless = statefullPath.getPrefix(statefullPath.size() - 1);      
        stateless.add(statefullPath.getBase().substring(0,pos));            
        return stateless;
    }

    //-----------------------------------------------------------------------
    /**
     * Get the model classes which correspond to the path's element. 
     * The path must be full qualified.
     * 
     * @param path
     * @return List
     */
    protected List<ModelElement_1_0> getClassesForPath(
        Path path
    ) throws ServiceException {
        ArrayList<ModelElement_1_0> classes = new ArrayList<ModelElement_1_0>();
        ModelElement_1_0 objectClass =
            this.model.getDereferencedType(AUTHORITY);
        for (int i = 1; i < path.size(); i = i + 2) {
            //classes.add(objectClass);

            String referenceName = path.get(i);
            String fullReferenceName = null;
            // the feature may also be in one of the subclasses:
            for (Iterator<Object> subIter = objectClass.objGetList("allSubtype").iterator();
            subIter.hasNext() && fullReferenceName == null;
            ) {
                ModelElement_1_0 subClass = this.model.getDereferencedType(
                    subIter.next()
                );
                for (
                    Iterator<?> refIter = subClass.objGetList("feature").iterator(); 
                    refIter.hasNext() && fullReferenceName == null;
                ) {
                    String feature = ((Path)refIter.next()).getBase();
                    if (feature.endsWith(
                        PathComponent.FIELD_DELIMITER + referenceName)
                    ) {
                        fullReferenceName = feature;
                        classes.add(subClass);
                    }
                }
            }
            if (fullReferenceName == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "Reference not found in objectClass or its children.",
                    new Parameter[] {
                        new Parameter("reference", referenceName),
                        new Parameter(
                            "objectClass",
                            objectClass.objGetList("qualifiedName"))});
            }
            ModelElement_1_0 assocEnd = this.model.getElement(fullReferenceName);
            classes.add(assocEnd);
            objectClass =
                this.model.getElementType(assocEnd);
        }
        return classes;
    }

    //-----------------------------------------------------------------------
    /**
     * Start the containing objects and references according to the path.
     * <p>
     * startFrom indicates from which position in the path the containers have
     * to be started. The numbering starts at 0. 
     * <p>
     * The last element of the path is not "started", this is left to surrounding 
     * block.
     * 
     * @param path
     * @param startFrom  from which position in path the containers are started
     * @return DataproviderObject  class of the last entry in path
     * @throws ServiceException
     */
    protected ModelElement_1_0 startParents(
        Path path, 
        int startFrom
    ) throws ServiceException {
        List<ModelElement_1_0> classes = this.getClassesForPath(path);
        int pos = startFrom;
        if (pos % 2 == 1 && pos < path.size() - 1) {
            ModelElement_1_0 classDef = classes.get(pos++);
            this.startReference(classDef);
        }
        while (pos < path.size() - 1 && pos >= 0) {
            DataproviderObject object = new DataproviderObject(path.getPrefix(pos + 1));
            object.values(SystemAttributes.OBJECT_CLASS).set(
                0,
                classes.get(pos).objGetValue("qualifiedName")
            );
            this.startObject(
                object.path().getParent(),
                path.getPrefix(pos + 1),
                classes.get(pos),
                TraversalHandler.NULL_OP
            );
            this.completeFeatures(
                object.path().getParent(),
                object, 
                classes.get(pos)
            );
            pos++;
            if (pos < path.size() - 1) {
                ModelElement_1_0 classDef = classes.get(pos++);
                this.startReference(classDef);
            }
        }
        return classes.get(classes.size() - 1);
    }

    //-----------------------------------------------------------------------
    /**
     * End the containing objects and references according to the path.
     * <p>
     * upTo indicates up to (not including) which position the containers get
     * ended. The numbering of path elements start with 0.
     * 
     * @param path
     * @param upTo   position up to which the containers get ended
     * @throws ServiceException
     */
    protected void endParents(
        Path path, 
        int upTo
    ) throws ServiceException {
        SysLog.trace("upTo: " + upTo + " end path:", path);
        boolean isReferencePath = false;        
        Path classesPath = path;
        /// if the last element is a reference, remove it (because getClassesForPath()
        /// accepts only full qualified paths)
        if (classesPath.size() % 2 == 0) {
            isReferencePath = true;
            /// classesPath = path.getParent();
        }        
        List<ModelElement_1_0> classes = this.getClassesForPath(classesPath);
        int pos = classes.size() - 1;
        if (isReferencePath) {
            // getClassesForPath returns all the classes up to the last reference
            // contained in path. It always ends in a reference.          
            // ignore the last entry, because this is already ended. 
            // if it is a reference path, the last part of the path has already 
            // been removed above.
            pos--;            
            // end the first object
            if (pos >= upTo) {
                this.endObject(classes.get(pos--)); // end the first object
            }
        }
        // else the last object is not contained in classes, because it 
        // always ends with the last reference, thus it must not be removed
        // or anything        
        while (pos >= 0 && pos >= upTo) {
            ModelElement_1_0 classDef = classes.get(pos--);
            this.endReference(classDef);
            if (pos >= upTo) {
                this.endObject(classes.get(pos--));
            }
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Traverse the parent objects of target starting from current.
     * 
     * @return class of the last element in nextPosition path
     */
    protected ModelElement_1_0 traverseParents(
        Path current,
        Path target
    ) throws ServiceException {
        SysLog.trace("current position: ", current);
        SysLog.trace("target position: ", target);

        ModelElement_1_0 classOfLast = null;
        if (current == null) {
            classOfLast = this.startParents(target, 0);
        }
        else if (target == null) {
            this.endParents(current, 0);
        }
        else {
            // how many components are equal?
            int eq = 0;
            while (eq < current.size()
                    && eq < target.size()
                    && current.get(eq).equals(target.get(eq))) {
                eq++;
            }

            this.endParents(current, eq); 
            // object/reference at eq has been closed and must be opened
            classOfLast = this.startParents(target, eq);
        }
        return classOfLast;
    }

    //-----------------------------------------------------------------------
    /**
     * find if the class referenced is a RoleType.
     * 
     * @param reference
     * @throws ServiceException
     */
    private boolean isReferencedEndARoleType(
        ModelElement_1_0 reference
    ) throws ServiceException {
        return false;
    }

    //-----------------------------------------------------------------------
    private boolean isCompositeReference(
        ModelElement_1_0 referenceDef
    ) throws ServiceException {
        ModelElement_1_0 referencedEnd = this.model.getElement(referenceDef.objGetValue("referencedEnd"));
        return AggregationKind.COMPOSITE.equals(referencedEnd.objGetValue("aggregation"));
    }

    //-----------------------------------------------------------------------
    private boolean isSharedReference(
        ModelElement_1_0 referenceDef
    ) throws ServiceException {
        ModelElement_1_0 referencedEnd = this.model.getElement(referenceDef.objGetValue("referencedEnd"));
        return AggregationKind.SHARED.equals(referencedEnd.objGetValue("aggregation"));
    }

    //-----------------------------------------------------------------------
    /** 
     * Get the qualifier of the containment path leading to objectClass.
     * 
     * @param objectClass         class to search qualifier for 
     * @return String             qualifier
     * @throws ServiceException
     */
    protected String getQualifierLeadingToClass(
        ModelElement_1_0 objectClass
    ) throws ServiceException {
        String qualifierName = null;
        if (!objectClass.objGetList("compositeReference").isEmpty()) {
            ModelElement_1_0 compReference =
                this.model.getElement(
                    ((Path) objectClass.objGetValue("compositeReference"))
                    .getBase());
            ModelElement_1_0 associationEnd =
                this.model.getElement(
                    ((Path) compReference.objGetValue("referencedEnd"))
                    .getBase());

            qualifierName =
                (String) associationEnd.objGetValue("qualifierName");
        }
        else if (
                AUTHORITY.equals(
                    objectClass.objGetValue("qualifiedName"))) {
            qualifierName = "name";
        }
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "no composite reference found for class.",
                new Parameter[] {
                    new Parameter("class", objectClass)
                });
        }
        return qualifierName;
    }

    //-----------------------------------------------------------------------
    /**
     * Complete processing by checking if any exceptions occured and initiate
     * the appropriate termination sequence.
     */
    private void handleFatalError(
        Exception exception
    ) throws ServiceException {
        ServiceException ex = null;

        if (exception instanceof ServiceException) {
            ex = (ServiceException) exception;
        }
        else {
            ex = new ServiceException( exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.SYSTEM_EXCEPTION,
                "fatal exception.");
        }
        SysLog.error("fatal exception", ex);

        if (getErrorHandler() != null) {

            try {
                getErrorHandler().fatalError(ex);
            }
            catch (ServiceException se) {
                SysLog.error("exception in errorHandler.fatalError()", se);

                throw se;
            }
        }
    }

    //-----------------------------------------------------------------------
    private static int indent = 0;

    private TraversalHandler traversalHandler = null;
    private ErrorHandler errorHandler = null;
    private RequestCollection reader = null;
    private Model_1_0 model = null;
    private final List<Path> startPoints;
    private final Set<String> referenceFilter;
    private final Map<String,FilterProperty[]> attributeFilter;

    /** The source paths which have already been traversed */
    private List<Path> traversedPaths = new ArrayList<Path>();

    //
    // Model Classes
    //
    protected final static String BASIC_STATE = "org:openmdx:compatibility:state1:BasicState";
    protected final static String AUTHORITY = "org:openmdx:base:Authority";
    protected final static FilterProperty IS_STATE = new FilterProperty(
        Quantors.THERE_EXISTS,
        "core",
        FilterOperators.IS_NOT_IN
    );
    
}

//--- End of File -----------------------------------------------------------
