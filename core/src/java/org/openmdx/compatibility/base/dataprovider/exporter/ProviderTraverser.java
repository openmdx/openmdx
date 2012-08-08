/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ProviderTraverser.java,v 1.38 2007/12/28 19:37:06 wfro Exp $
 * Description: Traversing a provider
 * Revision:    $Revision: 1.38 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/28 19:37:06 $
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.layer.model.RoleAttributes;
import org.openmdx.compatibility.base.dataprovider.layer.model.State_1_Attributes;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.naming.PathComponent;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.BasicException.Parameter;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.text.StringBuilders;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.code.AggregationKind;

/**
 * Class which traverses a provider and invokes a TraversalHandler in the 
 * correct sequence. The sequence is important for correct treatment of 
 * states and roles.
 */
public class ProviderTraverser 
   implements Traverser {

    //-----------------------------------------------------------------------
    /** 
     * Helper class for holding composite references declared within a role 
     * and the type of the role. 
     * <p>
     * roleCompositePath is the relative path within the core object of the 
     * role. It consists of a string array. 
     * <p>
     * roleType is the type of the role having the composite reference. 
     */ 
    private static class RoleCompositeReferences {
        
        public RoleCompositeReferences() {
            _roleTypeList = new ArrayList();
            _compositeReferenceTypeList = new ArrayList();
        }
        
        /**
         * Adds the roleType and roleInstance to its internal structure. 
         * <p>
         * If the roleType is already present, the index of the existing 
         * roleType is returned.
         * 
         * @param roleType
         */
        public int addContainerRole(
            ModelElement_1_0 roleType
        ) {
            int roleTypeIndex = getRoleTypeIndex(roleType);
            if (roleTypeIndex < 0) {
               roleTypeIndex = _roleTypeList.size();
               _roleTypeList.add(roleType);
            }
            return roleTypeIndex;
        }
        
        /**
         * Is the containerRole already been added.
         * @param roleType
         */
        public boolean hasContainerRole(
            ModelElement_1_0 roleType
        ) {
            return getRoleTypeIndex(roleType) >= 0;
        }
        
        public int containerRoleSize() {
            return _roleTypeList.size();
        }
        
        public ModelElement_1_0 getContainerRole(int number) {
            return (ModelElement_1_0) _roleTypeList.get(number);
        }
        
        public Set getCompositeReferencesForContainerRole(int number) {
            return (Set) _compositeReferenceTypeList.get(number);
        }
        
        public void addCompositeReference(
            int roleTypeIndex,
            ModelElement_1_0 compositeReferenceType
        ) {
            Set compRefs = null;
            if (roleTypeIndex < _compositeReferenceTypeList.size()) {
                compRefs = (Set)_compositeReferenceTypeList.get(roleTypeIndex);
            }
            else {
                for (int i = _compositeReferenceTypeList.size(); i <= roleTypeIndex; i++) {
                    _compositeReferenceTypeList.add(compRefs = new HashSet());
                }
            }
            
            for (Iterator i = compRefs.iterator(); i.hasNext(); ) {
                if (i.next() == compositeReferenceType) {
                    return; // already present
                }
            }
            compRefs.add(compositeReferenceType);
        }
        
        /**
         * Get the index of the roleType. Returns -1 if not present.
         * @param roleType
         */
        private int getRoleTypeIndex(
            ModelElement_1_0 roleType
        ) {
            for (int i = 0; i < _roleTypeList.size(); i++) {
                if (roleType == _roleTypeList.get(i)) {
                    return i;
                }
            }
            return -1;
        }
        
        private List _roleTypeList;
        private List _compositeReferenceTypeList;
        
    }
        
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
        List startPoints,
        List referenceFilters,
        Map attributeFilters
    ) throws java.lang.NullPointerException {
        this(
            header,
            new RequestCollection(header, provider),
            model,
            startPoints,
            referenceFilters,
            attributeFilters
        );
    }
    
    //-----------------------------------------------------------------------
    public ProviderTraverser(
        ServiceHeader header,
        RequestCollection reader,
        Model_1_0 model,
        List startPoints,
        List referenceFilters,
        Map attributeFilters
    ) throws NullPointerException {
        if(header == null) {
            throw new NullPointerException("header must not be null");
        }
        this.header = header;
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
        this.referenceFilters = referenceFilters;
        this.attributeFilters = attributeFilters;
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
                new Parameter[] {
                     new Parameter("transactionBehavior", transactionBehavior)},
                "The only transaction behavior supported is NO_TRANSACTION");
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
            getTraversalHandler().startTraversal(this.startPoints);
            if(!this.startPoints.isEmpty()) {
                Path startPath = null;
                Path lastStartPath = null;
                int ii = 0;
                for(
                    Iterator i = this.startPoints.iterator(); 
                    i.hasNext();
                    ii++
                ) {
                    Path next = (Path) i.next();
                    lastStartPath = startPath;
                    startPath = next;
                    SysLog.trace("starting traversal from: " + startPath);
                    ModelElement_1_0 classOfLast = this.traverseParents(
                        lastStartPath, 
                        startPath
                    );
                    if (startPath.size() < 5) {
                        throw new ServiceException( 
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ACTIVATION_FAILURE,
                            new Parameter[] { 
                                new Parameter("path", startPath)
                            },
                            "Can handle only one segment, path must be at least of size 5"
                        );
                    } else if (startPath.size() % 2 == 0) {
                        this.traverseFromReference(
                            startPath, 
                            classOfLast 
                        );
                    } else {
                        this.startTraversalAtObject(
                            startPath 
                        );
                    }
                    this.traversedPaths.add(startPath);
                }
                this.traverseParents(startPath, null);
            }
            ending = true;
            this.traversalHandler.endTraversal();
        } catch (java.lang.Exception ex) {
            // all exceptions that get here are fatal, because the position to 
            // continue is lost. 
            handleFatalError(ex);
            // Fatal errors can just be thrown from anywhere out of the code.
            ServiceException se = new ServiceException(ex); 
            if(!ending) try {
                this.traversalHandler.endTraversal();
            } catch (ServiceException ee) {
                se = ee.appendCause(se);
            }
            throw se;
        }
        
    }

    //-----------------------------------------------------------------------
    /**
     * Start traversal at the object denoted by startPath.
     * 
     * @param startPath
     * @throws ServiceException
     * @throws Exception
     */
    protected void startTraversalAtObject(
        Path startPath
    ) throws ServiceException, Exception {
        List objects = this.findObjectAndStates(startPath);
        if(objects != null) {
            if(objects.size() > 0) {
                DataproviderObject_1_0 object = (DataproviderObject_1_0)objects.get(0);
                ModelElement_1_0 objectClass = this.model.getDereferencedType(
                    object.getValues(SystemAttributes.OBJECT_CLASS).get(0)
                );
                this.traverseObject(
                    startPath, 
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
    protected List findAtPath(
        Path reference,
        Path identity
    ) throws ServiceException {
        List objects = null;
        boolean matches = this.referenceFilters == null;
        if(!matches) {
            for(Iterator i = this.referenceFilters.iterator(); i.hasNext(); ) {
                Path referenceFilter = (Path)i.next();                
                // In case the reference pattern ends with a wildcard (:*) the corresponding
                // exposed end must have aggregation kind composite and the reference must not
                // be derived
                boolean isComposite = false;
                boolean isDerived = true;
                try {
                    ModelElement_1_0 referenceType = this.model.getReferenceType(reference);
                    if(referenceType != null) {
                        isDerived = this.model.referenceIsDerived(referenceType);
                        ModelElement_1_0 referencedEnd = this.model.getElement(referenceType.values("referencedEnd").get(0));
                        if(referencedEnd != null) {
                            isComposite = AggregationKind.COMPOSITE.equals(referencedEnd.values("aggregation").get(0));
                        }
                    }
                } catch(ServiceException e) {
                    // ignore
                }                
                if(
                    reference.isLike(referenceFilter) &&
                    (!referenceFilter.endsWith(new String[]{":*"}) || (!isDerived && isComposite))
                ) {
                    matches = true;
                    break;
                }            
            }
        }
        if(matches) {
            try {            
                Filter filter = this.attributeFilters == null 
                    ? null 
                    : (Filter)this.attributeFilters.get(reference);
                if(filter != null) {
                    FilterProperty[] filterProperties = new FilterProperty[filter.getCondition().length];
                    for(int i = 0; i < filterProperties.length; i++) {
                        Condition condition = filter.getCondition()[i];
                        filterProperties[i] = new FilterProperty(
                            condition.getQuantor(),
                            condition.getFeature(),
                            (short)FilterOperators.fromString(condition.getName()),
                            condition.getValue()
                        );
                    }                
                    AttributeSpecifier[] attributeSpecifiers = new AttributeSpecifier[filter.getOrderSpecifier().length];
                    for(int i = 0; i < attributeSpecifiers.length; i++) {
                        OrderSpecifier specifier = filter.getOrderSpecifier()[i];
                        attributeSpecifiers[i] = new AttributeSpecifier(
                            specifier.getFeature(),
                            0,
                            specifier.getOrder()
                        );
                    }
                    objects = this.reader.addFindRequest(
                        reference,
                        filterProperties,
                        AttributeSelectors.ALL_ATTRIBUTES,
                        attributeSpecifiers,
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
//                  FilterProperty validToFilter =
//                  new FilterProperty(
//                      Quantors.FOR_ALL,
//                      State_1_Attributes.VALID_TO,
//                      FilterOperators.IS_NOT_IN,
//                      new Object[] {
//              });        
//              // Add identity filter for reducing the number of objects
//              FilterProperty idFilter = null;
//              if(identity != null) {
//                  idFilter = new FilterProperty(
//                      Quantors.THERE_EXISTS,
//                      SystemAttributes.OBJECT_IDENTITY,
//                      FilterOperators.IS_IN,
//                      new String[] {identity.toUri()}
//                  );
//              }
//              objects = this.reader.addFindRequest(
//                  reference,
//                  (idFilter == null ? 
//                      new FilterProperty[] {validToFilter} : 
//                      new FilterProperty[] {validToFilter, idFilter}
//                  ),
//                  AttributeSelectors.ALL_ATTRIBUTES,
//                  0,
//                  Integer.MAX_VALUE,
//                  Directions.ASCENDING
//              );
//          }
                    ModelElement_1_0 referenceType = this.model.getReferenceType(reference);
                    if(referenceType != null) {
                        ModelElement_1_0 referencedEnd = this.model.getElement(referenceType.values("referencedEnd").get(0));
                        if(referencedEnd != null) {
                            ModelElement_1_0 objectClass = this.model.getDereferencedType(
                                referencedEnd.getValues("type").get(0)
                            );
                            boolean stated = this.model.isSubtypeOf(objectClass, BASIC_STATE); 
                            objects = this.reader.addFindRequest(
                                reference,
                                stated ? new FilterProperty[]{
                                    new FilterProperty(
                                        Quantors.THERE_EXISTS,
                                        State_1_Attributes.STATED_OBJECT,
                                        identity == null ? FilterOperators.IS_NOT_IN : FilterOperators.IS_IN,
                                        identity == null ? new Object[] {} : new Object[] {identity}
                                    )
                                } : null,
                                AttributeSelectors.ALL_ATTRIBUTES,
                                0,
                                Integer.MAX_VALUE,
                                Directions.ASCENDING
                            );
                        }
                    }
                }
                
            }
            // Don't care about NOT_FOUND exceptions
            catch(ServiceException e) {
                if(e.getExceptionStack().getExceptionCode() != BasicException.Code.NOT_FOUND) {
                    throw e;
                }
                else {
                    objects = new ArrayList();
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
    protected List findObjectAndStates(
        Path objectPath
    ) throws ServiceException {
        List objects = new ArrayList();
        try {
            if(objectPath.size() % 2 == 0) {
                throw new ServiceException(
	                BasicException.Code.DEFAULT_DOMAIN,
	                BasicException.Code.ASSERTION_FAILURE,
	                new Parameter[] {
	                    new Parameter(
	                        "objectPath",
	                        objectPath)
	                },
	                "Path does not lead to an object."
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
                (e.getExceptionStack().getExceptionCode() != BasicException.Code.NOT_FOUND) &&
                (e.getExceptionStack().getExceptionCode() != BasicException.Code.AUTHORIZATION_FAILURE)
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
     * @param startPath   path ending in a reference
     * 
     * @return collection with ids of referenced objects
     * 
     * @throws Exception  
     */
    protected Collection traverseFromReference(
        Path startPath,
        ModelElement_1_0 referenceType
    ) throws Exception {
        List containedIds = new ArrayList();
        List objects = this.findAtPath(
            startPath, 
            null 
        );
        if(objects != null) {
            if(objects.size() > 0) {
                this.startReference(referenceType);
            }
            // traverse object (states) recursively
            for(Iterator i = objects.listIterator(); i.hasNext();) {
                DataproviderObject next = new DataproviderObject(
                    (DataproviderObject_1_0) i.next()
                );                   
                ModelElement_1_0 objectClass =
                    this.model.getDereferencedType(
                        next.values(SystemAttributes.OBJECT_CLASS).get(0));
                
                if(objectClass == null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        new Parameter[] {
                            new Parameter(
                                "object class",
                                next.values(
                                    SystemAttributes.OBJECT_CLASS).get(
                                    0)),
                            new Parameter("object path", next.path())},
                        "Unknown object class.");
                }
                // it is a stated object, the id of the object must be provided
                // in the first state
                if(this.model.isSubtypeOf(next, BASIC_STATE)) {
                    Path path = getStatelessPath(next.path());
                    if (!containedIds.contains(path)) {
                        containedIds.add(path);
                        this.traverseObject(
                            path, 
                            objects, 
                            objectClass, 
                            IS_STATE 
                        );
                    }
                    // else nothing, object and all its states already treated
                }
                else {
                    containedIds.add(next.path());
                    List nonStatedObjects = new ArrayList();
                    nonStatedObjects.add(next);
                    this.traverseObject(
                        next.path(), 
                        nonStatedObjects, 
                        objectClass, 
                        !IS_STATE 
                    );
                }
            }
            if(objects.size() > 0) {
                this.completeReference(startPath, containedIds);
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
        Path objectPath,
        List allStates,
        ModelElement_1_0 objectClass
    ) throws java.lang.Exception {
        boolean continueTraversal = true;
//      Set containedIds = new HashSet();
        List validFromSorted = new ArrayList();
        
        for (Iterator s = allStates.iterator();
            s.hasNext() && continueTraversal;
        ) {
            DataproviderObject state = (DataproviderObject) s.next();

            // if it is a state of the object treated right now
            if (state.path().toString().startsWith(objectPath.toString())) {
                
                int pos = -1; 
                if (state.getValues(State_1_Attributes.VALID_FROM) != null &&
                    !state.getValues(State_1_Attributes.VALID_FROM).isEmpty()
                ) {
                    String validFrom = 
                        (String) state.getValues(State_1_Attributes.VALID_FROM).get(0);
                    
                    for (int i = 0; i < validFromSorted.size() && pos == -1; i++) {
                        List current = 
                            ((DataproviderObject_1_0)validFromSorted.get(i))
                                .getValues(State_1_Attributes.VALID_FROM);
                                        
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
        
        for (Iterator s = validFromSorted.iterator(); s.hasNext(); ) {
                
            DataproviderObject state = (DataproviderObject) s.next();
              
//          containedIds.add(objectPath); //state.path());   
            
            continueTraversal = startObject(state.path(), objectClass, TraversalHandler.SET_OP);

            if (continueTraversal) {
                continueTraversal = completeFeatures(state, objectClass);
            }
            endObject(objectClass);
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
     * @param path               path to traverse from
     * @param objectStates       all states of all objects at path  to traverse
     * @param objectClass        class of that object
     * @param isState            if it is a state rather than an object
     * @throws ServiceException 
     */
    protected void traverseObject(
        Path path,
        List objectStates,
        ModelElement_1_0 objectClass,
        boolean isState
    ) throws Exception {
        boolean continueTraversal = true;
        List references = null;
        if(isState) {
            // first all states (just the attributes)            
            continueTraversal = 
                traverseObjectStates(path, objectStates, objectClass);            
            // then the contained objects
            if(continueTraversal) {
                // the same object as in the states
                continueTraversal = this.startObject(
                    path, 
                    objectClass, 
                    TraversalHandler.SET_OP
                );            
                if (continueTraversal) {
                    // need an empty object for featureComplete
                    DataproviderObject empty = new DataproviderObject(
                        path
                    );
                    empty.values(SystemAttributes.OBJECT_CLASS).set(
                        0, 
                        objectClass.getValues("qualifiedName").get(0)
                    );                    
                    completeFeatures(empty, objectClass);                    
                    // the roles as normal reference
                    continueTraversal = 
                        this.traverseContainedRoles(
                            path, 
                            objectStates, 
                            objectClass,
                            null
                       );
                    references = this.traverseContained(
                        path, 
                        objectClass 
                    );                    
                    this.completeContent(
                        path, 
                        objectClass, 
                        references
                    );
                }                
                this.endObject(objectClass);
            }
        }
        else {
            continueTraversal = this.startObject(
                path, 
                objectClass, 
                TraversalHandler.SET_OP
            );
            if(continueTraversal) {                
                DataproviderObject obj = new DataproviderObject(
                    (DataproviderObject_1_0) objectStates.get(0)
                );
                completeFeatures(obj, objectClass);
                if (this.model.isSubtypeOf(obj, "org:openmdx:compatibility:role1:Role")) {
                    List allStates = new ArrayList();
                    allStates.add(obj);
                    continueTraversal = 
                        this.traverseContainedRoles(
                            path, 
                            allStates, 
                            objectClass,
                            null
                        );
                }
                references = this.traverseContained(
                    obj.path(), 
                    objectClass 
                );
                this.completeContent(
                    obj.path(), 
                    objectClass, 
                    references
                );
            }
            endObject(objectClass);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * @param objectPath
     * @param allStates
     * @param objectClass
     * @return true if the traversal should continue
     * @throws java.lang.Exception
     */
    protected boolean traverseContainedRoles(
        Path objectPath,
        List allStates,
        ModelElement_1_0 objectClass,
        Filter filter
    ) throws java.lang.Exception {
        boolean continueTraversal = true;
        
        Set presentRoles = new HashSet();
        
        List rolePaths = new ArrayList();
        
        // find if there are any roles for the state treated rigth now.
        for (Iterator s = allStates.iterator();
            s.hasNext();
        ) {
            DataproviderObject state = (DataproviderObject) s.next();

            // allStates contains all the states of all the contained objects, 
            // thus the startsWith() is needed.
            if (state.path().startsWith(objectPath)) {
                presentRoles.addAll(state.values(RoleAttributes.HAS_ROLE));
            }
        }
        
        if (presentRoles.size() > 0) {
            startRoleReference(objectClass);
        }
        
        // iterate over the roles to get an ordering per Role
        for (Iterator r = presentRoles.iterator(); 
            r.hasNext() && continueTraversal; 
        ) {
            String roleName = (String)r.next();            
            DataproviderObject aState = null;
            RoleCompositeReferences roleCompositeRefs = new RoleCompositeReferences();            
            for (Iterator s = allStates.iterator();
                s.hasNext() && continueTraversal;
            ) {
                DataproviderObject state = (DataproviderObject) s.next();
                // if it is a state of the object treated right now
                if(
                    state.path().startsWith(objectPath) && 
                    state.values(RoleAttributes.HAS_ROLE)!= null && 
                    state.values(RoleAttributes.HAS_ROLE).contains(roleName)
                ) {
                    // any state will do, need just one
                    aState = state;
                    continueTraversal = 
                        traverseRoleWithinState(
                            state, 
                            roleName, 
                            rolePaths, 
                            roleCompositeRefs
                        );
                }
            }            
            for(
                int i = 0; 
                i < roleCompositeRefs.containerRoleSize() && 
                roleCompositeRefs.getCompositeReferencesForContainerRole(i) != null && 
                roleCompositeRefs.getCompositeReferencesForContainerRole(i).size() > 0; 
                i++
            ) {                
                // String reference = roleComposite.roleCompositePath[2];
                Path rolePath = removeStateFromPath(aState.path().getChild("role")).add(roleName);
                continueTraversal = 
                    startObject(
                        rolePath,
                        roleCompositeRefs.getContainerRole(i), 
                        TraversalHandler.SET_OP
                    );
                 
                DataproviderObject empty = new DataproviderObject(
                    rolePath//roleCompositeRefs.getRoleInstance(i),
                    // false
                );
                // need object_class
                empty.values(SystemAttributes.OBJECT_CLASS).set(0,  // addAll
                    //roleCompositeRefs.getRoleInstance(i).getValues(SystemAttributes.OBJECT_CLASS)
                    roleCompositeRefs.getContainerRole(i).getValues("qualifiedName").get(0) //.getRoleInstance(i).getValues(SystemAttributes.OBJECT_CLASS)
                );
                completeFeatures(empty, roleCompositeRefs.getContainerRole(i));                
                for (Iterator rcr = roleCompositeRefs.getCompositeReferencesForContainerRole(i).iterator();
                    rcr.hasNext();
                ) {
                    ModelElement_1_0 compositeReference = (ModelElement_1_0) rcr.next();                    
                    //Collection referencedObjs =
                    this.traverseFromReference(
                        rolePath.add((String)compositeReference.getValues("name").get(0)),
                        compositeReference
                    );                
                }                
                endObject(roleCompositeRefs.getContainerRole(i));
            }
        }        
        if (presentRoles.size() > 0) {
            completeReference(objectPath.getChild("role"), rolePaths);
            endRoleReference(objectClass);
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
    protected List traverseContained(
        Path objectPath,
        ModelElement_1_0 objectClass
    ) throws Exception {
        List features = objectClass.getValues("feature");
        List existingReferences = new ArrayList();
        for (int roleTypesFirst = 0; roleTypesFirst < 2; roleTypesFirst++) {
            for (int j = 0; j < features.size(); j++) {
                String featureName = ((Path) features.get(j)).getBase();
                ModelElement_1_0 elementType = this.model.getElement(featureName);
                if (this.model.isReferenceType(elementType)) {
                    // Navigate composite references only in case there is no reference filter specified
                    if(
                        (this.isCompositeReference(elementType) || (this.isSharedReference(elementType) && (this.referenceFilters != null))) && 
                        ((roleTypesFirst == 0 && this.isReferencedEndARoleType(elementType)) || (roleTypesFirst == 1 && !isReferencedEndARoleType(elementType))) && 
                        !isViewReference(elementType)
                    ) {
                        String referenceName = (String) elementType.getValues("name").get(0);
                        // do not navigate object views, new
                        if(!"view".equals(referenceName)) {
                            Path referencePath = objectPath.getChild(referenceName);
                            boolean matches = this.referenceFilters == null;
                            if(!matches) {
                                for(Iterator k = this.referenceFilters.iterator(); k.hasNext(); ) {
                                    Path referenceFilter = (Path)k.next();
                                    if(referencePath.isLike(referenceFilter)) {
                                        matches = true;
                                        break;
                                    }
                                }
                            }
                            if(matches) {
    	                        Collection referencedObjs =
    	                            this.traverseFromReference(
    	                                objectPath.getChild(referenceName),
    	                                elementType
                                    );    
    	                        if (referencedObjs.size() > 0) {
    	                            existingReferences.add(
    	                                objectPath.getChild(referenceName));
    	                        }
                            }
                        }
                    }
                }
            }
        }
        return existingReferences;
    }

    //-----------------------------------------------------------------------
    /**
     * Search the parent classes belonging to the same role. 
     * <p>
     * All the parent classes up to the first which has the stereotype role
     * belong to the same role. 
     * 
     * @param roleClass   roleClass to search parent role classes for
     */
    protected List findRoleAnchestors(
        DataproviderObject_1_0 _roleClass
    ) throws ServiceException {
        DataproviderObject_1_0 roleClass = _roleClass;
        // contains the inheritance paths to check
        ArrayList inheritancePaths = new ArrayList();

        // inheritancePath contains all the classes which are superclasses
        // from the specified roleClass, starting at the roleClass itself.
        ArrayList inheritancePath = new ArrayList();
        inheritancePath.add(roleClass);
        inheritancePaths.add(inheritancePath);
        inheritancePath = null;

        while (inheritancePath == null && inheritancePaths.size() > 0) {
            ArrayList newInheritancePaths = new ArrayList();

            // for the last class in a inheritance path check if it has the 
            // stereotype role 
            for (Iterator ip = inheritancePaths.iterator();
                ip.hasNext() && inheritancePath == null;
                ) {
                inheritancePath = (ArrayList) ip.next();

                roleClass =
                    (DataproviderObject_1_0) inheritancePath.get(
                        inheritancePath.size() - 1);

                if (roleClass.getValues("stereotype") != null
                    && "role".equals(roleClass.getValues("stereotype").get(0))) {
                    // found it!

                }
                else {
                    for (Iterator si =
                        roleClass.getValues("supertype").iterator();
                        si.hasNext();
                        ) {
                        List newInheritancePath =
                            (List) inheritancePath.clone();
                        newInheritancePath.add(
                            this.model.getDereferencedType(si.next()));
                        newInheritancePaths.add(newInheritancePath);
                    }
                    inheritancePath = null;
                }

            }
            inheritancePaths = newInheritancePaths;
        }
        // add role class itself
        inheritancePath.add(this.model.getDereferencedType("org:openmdx:compatibility:role1:Role"));
        return inheritancePath;
    }

    //-----------------------------------------------------------------------
    /**
     * Remove all the attributes which don't belong to the role itself from the 
     * role instance. 
     * <p>
     * Keep some system attributes to ease handling of the role in the handler.
     * @param role
     * @param roleClass
     * @throws ServiceException
     */
    protected void reduceToRoleAttributes(
        DataproviderObject_1_0 role,
        DataproviderObject_1_0 roleClass
    ) throws ServiceException {
        // classes which belong to the same role 
        List roleClasses = new ArrayList();
        roleClasses = findRoleAnchestors(roleClass);
        for (Iterator a = role.attributeNames().iterator(); a.hasNext();) {
            String attribute = (String) a.next();
            if (!SystemAttributes.OBJECT_CLASS.equals(attribute)
                && !State_1_Attributes.VALID_FROM.equals(attribute)
                && !State_1_Attributes.VALID_TO.equals(attribute)
                && !isContentAttribute(attribute, roleClasses)) {
                a.remove();
            }
        }
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
        List classes
    ) throws ServiceException {
        ModelElement_1_0 elementType = null;
        boolean isAttribute = false;
        boolean isNonCompositeReference = false;
        for (Iterator classIter = classes.iterator();
            classIter.hasNext() && elementType == null;
            ) {
            DataproviderObject_1_0 contentClass =
                (DataproviderObject_1_0) classIter.next();

            List content = contentClass.getValues("content");

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
                .getValues(SystemAttributes.OBJECT_CLASS)
                .get(0)
                .equals("org:omg:model1:Attribute")) {
                isAttribute = true;
            }
            // reference, only non contained references are valid 
            else if (
                elementType.getValues(SystemAttributes.OBJECT_CLASS).get(
                    0).equals(
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
     * Find the composite references within this role class and construct the
     * relativ paths for accessing the objects (if any).
     * 
     * @param roleName
     * @param roleClass
     * @return list of RoleComposites.
     */
    protected void findRoleCompositePaths(
        ModelElement_1_0 roleClass,
        RoleCompositeReferences roleCompositeRefs
    ) throws ServiceException {      
        if (!roleCompositeRefs.hasContainerRole(roleClass)) {            
            // get classes which belong to the same role; avoid classes of the
            // core or another role
            List roleParents = 
                getClosestRoleStereotypeHierarchy(
                    roleClass,
                    new ArrayList(),
                    Integer.MAX_VALUE
                );            
            if (roleParents != null) {
                for (Iterator rp = roleParents.iterator(); rp.hasNext();) {
                    ModelElement_1_0 parent = (ModelElement_1_0)rp.next();                    
                    List contents = parent.getValues("content");            
                    for (int j = 0; j < contents.size(); j++) {
                        String featureName = ((Path) contents.get(j)).getBase();            
                        ModelElement_1_0 elementType = this.model.getElement(featureName);            
                        // reference
                        if (elementType.getValues(SystemAttributes.OBJECT_CLASS).get(0)
                                .equals("org:omg:model1:Reference")
                            && isCompositeReference(elementType)
                        ) {                    
                            int index = 
                                roleCompositeRefs.addContainerRole(roleClass);
                            
                            roleCompositeRefs.addCompositeReference(index, elementType);
                        }
                    }
                }
            }
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Recursively searches for the nearest class with the role stereotype set, 
     * returns the direct line of superclasses from the starting class to the 
     * nearest class with the role stereotype.
     * <p>
     * Searches starting from current; hierarchy contains the list of direct
     * ancestors of the starting class to the current class. 
     * ClosestRoleStereotype is the number of ancestors in the shortest line 
     * of superclasses found so far. It is used to shortcut the search if the 
     * hierarchy gets longer.
     * <p>
     * The algorithm always checks all parents to find the shortest hierarchy. 
     * 
     * @param current     current class to start from
     * @param hierarchy   hierarchy from the starting element to current
     * @param closestRoleStereotype  
     * @throws ServiceException
     */
    protected List getClosestRoleStereotypeHierarchy(
        ModelElement_1_0 current,
        List hierarchy,
        int _closestRoleStereotype
    ) throws ServiceException {
        int closestRoleStereotype = _closestRoleStereotype;
        List bestRoleHierarchy = null;
        
        // if the current hierarchy is farther away then the closest 
        // role stereotype found so far, there is no use in searching on
        if (hierarchy.size() < closestRoleStereotype) {             
            hierarchy.add(current);        
            if (current.getValues("stereotype") != null 
                && current.getValues("stereotype").contains("role")
            ) {
                // this is a class with stereotype role
                bestRoleHierarchy = new ArrayList();
                bestRoleHierarchy.addAll(hierarchy);
            }
            else if (current.getValues("supertype") == null) {
                // nothing to do 
            }
            else {
                for (Iterator s = current.getValues("supertype").populationIterator();
                    s.hasNext();
                ) {
                    ModelElement_1_0 currentsSuper = this.model.getDereferencedType(
                        s.next()
                    );                    
                    List roleHierarchy = getClosestRoleStereotypeHierarchy(
                        currentsSuper, 
                        hierarchy, 
                        closestRoleStereotype
                    );                  
                    if (roleHierarchy != null 
                        && 
                        ( bestRoleHierarchy == null 
                          || 
                          roleHierarchy.size() < bestRoleHierarchy.size()
                        )
                    ) {
                        bestRoleHierarchy = roleHierarchy;
                        closestRoleStereotype = bestRoleHierarchy.size();
                    }
                }
            }
        
            hierarchy.remove(current);
        }
        return bestRoleHierarchy;
    }

    //-----------------------------------------------------------------------
    /**
     * Traverse the roles that the object has in object_hasRole.
     * 
     * @param object           object to traverse for roles
     * @param objectClass      class of the object
     * @param roleName         role to treat            
     * @param roleComposite    objects contained in roles (out)
     * @throws ServiceException
     * @return continue with traversal
     */
    protected boolean traverseRoleWithinState(
        DataproviderObject object,
        String roleName,
        List rolePaths,
        RoleCompositeReferences roleCompositeReferences
    ) throws ServiceException {
        boolean continueTraversal = true;
        
        if (object.values(RoleAttributes.HAS_ROLE)!= null 
            && object.values(RoleAttributes.HAS_ROLE).contains(roleName)
        ) {
            Path rolePath = removeStateFromPath(new Path(object.path())).add("role").add(roleName);
            String validFrom =
                object.getValues(State_1_Attributes.VALID_FROM) == null
                    ? null
                    : (String) object.getValues(
                        State_1_Attributes.VALID_FROM).get(0);
            String validTo =
                object.getValues(State_1_Attributes.VALID_TO) == null
                    ? null
                    : (String) object.getValues(State_1_Attributes.VALID_TO).get(0);

            DataproviderObject role = new DataproviderObject(
                this.retrieveRole(rolePath, validFrom, validTo)
            );
            rolePaths.add(rolePath);
            ModelElement_1_0 roleClass =
                this.model.getDereferencedType(
                    role.getValues(SystemAttributes.OBJECT_CLASS).get(0));
            continueTraversal = 
                startObject(rolePath, roleClass, TraversalHandler.SET_OP);           
            if (continueTraversal) {  
                reduceToRoleAttributes(role, roleClass);
                continueTraversal = this.completeFeatures(role, roleClass);
                findRoleCompositePaths( roleClass, roleCompositeReferences);
            }
            endObject(roleClass);
        }        
        return continueTraversal;
    }

    //-----------------------------------------------------------------------
    /**
     * Retrieve the role instance from the source provider.
     * <p>
     * Each start or stop of a role leads to a new state of the core object. 
     * Thus within a state, we can be sure that a is either present or missing
     * at all. It does not change within the period of a state.
     * 
     * @param  rolePath                path to role
     * @param  validFrom               start of search period
     * @param  validTo                 end of search period
     * @return DataproviderObject_1_0  role instance found
     * @throws ServiceException
     */
    protected DataproviderObject_1_0 retrieveRole(
        Path rolePath,
        String validFrom,
        String validTo
    ) throws ServiceException {
        DataproviderObject_1_0 role = null;
        // to get the role of the correct state, adjust the header requestedFor.
        ServiceHeader statedHeader =
            new ServiceHeader(
                (String[]) this.header.getPrincipalChain().toArray(new String[0]),
                this.header.getCorrelationId(),
                false,
                this.header.getQualityOfService(),
                null,
                validFrom != null ? validFrom : validTo);
        RequestCollection statedReader = this.reader.createRequestCollection(statedHeader);
        try {
            role = statedReader.addGetRequest(rolePath);
        }
        catch (ServiceException se) {
            // roles indicated by object_hasRole must be found!
            if (se.getExceptionStack().getExceptionCode() == BasicException.Code.NOT_FOUND) {
                throw new ServiceException(
                    se,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    new Parameter[] { new Parameter("objectPath", rolePath)},
                    "Role contained in object_hasRole must be found!");
            }
        }
        return role;
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
        CharSequence indentBuf = StringBuilders.newStringBuilder();
        if (deltaIndent > 0) {
            indent += deltaIndent;
        }
        for (int i = 0; i < indent; i++) {
            StringBuilders.asStringBuilder(indentBuf).append(" ");
        }
        if (deltaIndent < 0) {
            indent += deltaIndent;
        }
        StringBuilders.asStringBuilder(indentBuf).append(message);
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
        Path path,
        DataproviderObject_1_0 objectClass,
        short operation
    ) throws ServiceException {
        
        logIndent(
            1,
            "startObject:"
                + (String) objectClass.getValues("qualifiedName").get(0)
                + " "
                + path.getBase());
                
        boolean result = this.getTraversalHandler().startObject(
            (String) objectClass.getValues("qualifiedName").get(0),
            getQualifierLeadingToClass(objectClass),
            path.getBase(),
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
        DataproviderObject_1_0 objectClass
    ) throws ServiceException {
        logIndent(
            -1,
            "endObject:"
                + (String) objectClass.getValues("qualifiedName").get(0)
        );

        this.getTraversalHandler().endObject(
            (String) objectClass.getValues("qualifiedName").get(0)
        );
    }

    //-----------------------------------------------------------------------
    /**
     * Call the featureComplete of the handler.
     * 
     * @param outObject    prepared out object
     * @param objectClass  class of the object
     * @return boolean     return value from the handler
     */
    protected boolean completeFeatures(
        DataproviderObject outObject,
        ModelElement_1_0 objectClass
    ) throws ServiceException {
        logIndent(
            0,
            "featureComplete: "
                + (String) objectClass.getValues("qualifiedName").get(0)
                + " "
                + outObject.path().getBase()
        );
        boolean fcResult =  getTraversalHandler().featureComplete(outObject);

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
        DataproviderObject_1_0 objectClass,
        List containedReferences
    ) throws ServiceException {
        logIndent(
            0,
            "contentComplete: "
                + (String) objectClass.getValues("qualifiedName").get(0)
                + " "
                + objectPath.getBase()
        );
        getTraversalHandler().contentComplete(
            objectPath,
            (String) objectClass.getValues("qualifiedName").get(0),
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
        Collection objectIds
    ) throws ServiceException {
        logIndent(0, "referenceComplete: " + reference + " ids: " + objectIds);
        getTraversalHandler().referenceComplete(reference, objectIds);
    }

    //-----------------------------------------------------------------------
    /**
     * Roles are treated as references of the class itself, not of Role.
     * 
     * @param exposedEnd         the objectClass containing a role reference
     * @return boolean           continue traversal
     * @throws ServiceException
     */
    protected boolean startRoleReference(
        DataproviderObject_1_0 exposedEnd
    ) throws ServiceException {
        logIndent(
            1,
            "startRoleReference: "
                + ((String) exposedEnd.getValues("qualifiedName").get(0))
                + ":role");
        return getTraversalHandler().startReference(
            ((String) exposedEnd.getValues("qualifiedName").get(0)) + ":role");
    }

    //-----------------------------------------------------------------------
    /**
     * Roles are treated as references of the class itself, not of Role.
     * 
     * @param exposedEnd            the objectClass containing the role
     * reference
     * @throws ServiceException
     */
    protected void endRoleReference(
        DataproviderObject_1_0 exposedEnd
    ) throws ServiceException {
        logIndent(
            -1,
            "endReference: "
                + ((String) exposedEnd.getValues("qualifiedName").get(0))
                + ":role");
        getTraversalHandler().endReference(
            ((String) exposedEnd.getValues("qualifiedName").get(0)) + ":role");
    }

    //-----------------------------------------------------------------------
    /**
     * Call the startReference of the handler
     * 
     * @param qualifiedName
     * @return boolean
     */
    protected boolean startReference(
        DataproviderObject_1_0 referenceType
    ) throws ServiceException {

        if (referenceType != null) {
            logIndent(
                1,
                "startReference: "
                    + (String) referenceType.getValues("qualifiedName").get(0));
            return getTraversalHandler().startReference(
                (String) referenceType.getValues("qualifiedName").get(0));
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
        DataproviderObject_1_0 referenceType
    ) throws ServiceException {

        if (referenceType != null) {
            logIndent(
                -1,
                "endReference: "
                    + (String) referenceType.getValues("qualifiedName").get(0));
            getTraversalHandler().endReference(
                (String) referenceType.getValues("qualifiedName").get(0));
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
//        if (inNoStateModus())
        return statefullPath;
//        else
//      new ServiceException(
//         BasicException.Code.DEFAULT_DOMAIN,
//         BasicException.Code.ASSERTION_FAILURE,
//         new Parameter[] {
//            new Parameter("statefullPath", statefullPath)
//         },
//         "path not statefull.");
   }
      
      Path stateless = statefullPath.getPrefix(statefullPath.size() - 1);
      
      stateless.add(statefullPath.getBase().substring(0,pos));      
      
      return stateless;
   }
/**
   protectedPath getStatelessPath(Path statefullPath) throwsServiceException {
      intpos = statefullPath.getBase().indexOf(";state=");
      if(pos < 0 ) {
//           if (inNoStateModus())
           return statefullPath;
//           else
//         new ServiceException(
//            BasicException.Code.DEFAULT_DOMAIN,
//            BasicException.Code.ASSERTION_FAILURE,
//            new Parameter[] {
//               new Parameter("statefullPath", statefullPath)
//            },
//            "path not statefull.");
      }

      Path stateless = statefullPath.getPrefix(statefullPath.size() - 1);

      stateless.add(statefullPath.getBase().substring(0,pos));

      returnstateless;
   }
 */
    //-----------------------------------------------------------------------
    /**
     * Get the model classes which correspond to the path's element. 
     * The path must be full qualified.
     * 
     * @param path
     * @return List
     */
    protected List getClassesForPath(
        Path path
    ) throws ServiceException {
        ArrayList classes = new ArrayList();
        DataproviderObject_1_0 objectClass =
            this.model.getDereferencedType(AUTHORITY);

        for (int i = 1; i < path.size(); i = i + 2) {
            //classes.add(objectClass);

            String referenceName = path.get(i);
            String fullReferenceName = null;
            // the feature may also be in one of the subclasses:
            for (Iterator subIter = objectClass.getValues("allSubtype").iterator();
                  subIter.hasNext() && fullReferenceName == null;
            ) {
                DataproviderObject_1_0 subClass =
                    this.model.getDereferencedType(
                        ((Path) subIter.next()).getBase());
                for (Iterator refIter = subClass.getValues("feature").iterator();
                    refIter.hasNext() && fullReferenceName == null;
                ) {
                    String feature = ((Path) refIter.next()).getBase();
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
                    new Parameter[] {
                        new Parameter("reference", referenceName),
                        new Parameter(
                            "objectClass",
                            objectClass.getValues("qualifiedName"))},
                    "Reference not found in objectClass or its children.");
            }
            ModelElement_1_0 assocEnd = this.model.getElement(fullReferenceName);

            classes.add(assocEnd);

            objectClass =
                this.model.getDereferencedType(assocEnd.getValues("type").get(0));
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
        List classes = this.getClassesForPath(path);
        int pos = startFrom;
        if (pos % 2 == 1 && pos < path.size() - 1) {
            DataproviderObject clazz = (DataproviderObject) classes.get(pos++);
            this.startReference(clazz);
        }
        while (pos < path.size() - 1 && pos >= 0) {
            this.startObject(
                path.getPrefix(pos + 1),
                (DataproviderObject) classes.get(pos),
                TraversalHandler.NULL_OP
            );
            DataproviderObject obj = new DataproviderObject(path.getPrefix(pos + 1));
            // Dummy object for Authority, Provider, etc.
            obj.values("qualifiedName").set(
                0,
                ((DataproviderObject) classes.get(pos))
                    .getValues("qualifiedName").get(0)
            );
            obj.values("attribute");
            this.completeFeatures(obj, (ModelElement_1_0) classes.get(pos));
            pos++;
            if (pos < path.size() - 1) {
                DataproviderObject clazz =
                    (DataproviderObject) classes.get(pos++);
                this.startReference(clazz);
            }
        }
        return (ModelElement_1_0)classes.get(classes.size() - 1);
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
        
        List classes = getClassesForPath(classesPath);
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
              endObject((DataproviderObject)classes.get(pos--)); // end the first object
            }
        }
        // else the last object is not contained in classes, because it 
        // always ends with the last reference, thus it must not be removed
        // or anything
        
        while (pos >= 0 && pos >= upTo) {
            DataproviderObject clazz = (DataproviderObject) classes.get(pos--);
            endReference(clazz);

            if (pos >= upTo) {
                endObject((DataproviderObject) classes.get(pos--));
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
        ModelElement_1_0 referencedEnd = this.model.getElement(reference.getValues("referencedEnd").get(0));
        
        ModelElement_1_0 type = this.model.getElement(referencedEnd.getValues("type").get(0));
        
        return  this.model.isSubtypeOf(type, "org:openmdx:compatibility:role1:RoleType");
    }

    //-----------------------------------------------------------------------
   /**
    * Find out if it is a view reference 
    * (org:openmdx:compatibility:view1:ObjectHasView:view). 
    *   
    * @param referenceType  type of the reference
    * @return boolean
    */
   private boolean isViewReference(
       ModelElement_1_0 referenceType
   ) throws ServiceException {
       String qualifiedName = 
           (String) referenceType.getValues("qualifiedName").get(0);
       
       return qualifiedName.equals("org:openmdx:compatibility:view1:ViewCapable:view");
   }

    //-----------------------------------------------------------------------
    private boolean isCompositeReference(
        ModelElement_1_0 referenceDef
    ) throws ServiceException {
        ModelElement_1_0 referencedEnd = this.model.getElement(referenceDef.getValues("referencedEnd").get(0));
        return AggregationKind.COMPOSITE.equals(referencedEnd.getValues("aggregation").get(0));
    }
    
    //-----------------------------------------------------------------------
    private boolean isSharedReference(
        ModelElement_1_0 referenceDef
    ) throws ServiceException {
        ModelElement_1_0 referencedEnd = this.model.getElement(referenceDef.getValues("referencedEnd").get(0));
        return AggregationKind.SHARED.equals(referencedEnd.getValues("aggregation").get(0));
    }
    
    //-----------------------------------------------------------------------
    /**
     * Removes the state parts from the path supplied. 
     * 
     * @param path path to remove state part from
     * @return the path without the state part
     */
    private Path removeStateFromPath(Path path) {
        for (int i = 0; i < path.size(); i++) {
            if (path.get(i).equals("state") 
                && path.size() > i
                && path.get(i+1).charAt(0) >= '0'  // is it a number
                && path.get(i+1).charAt(0) <= '9'
            ) {
                path.remove(i);  // state
                path.remove(i);  // number
            }
        }
        return path;
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
        DataproviderObject_1_0 objectClass
    ) throws ServiceException {
        String qualifierName = null;
        if (objectClass.values("compositeReference").size() > 0) {
            ModelElement_1_0 compReference =
                this.model.getElement(
                    ((Path) objectClass.getValues("compositeReference").get(0))
                        .getBase());

            ModelElement_1_0 associationEnd =
                this.model.getElement(
                    ((Path) compReference.getValues("referencedEnd").get(0))
                        .getBase());

            qualifierName =
                (String) associationEnd.getValues("qualifierName").get(0);
        }
        else if (
            AUTHORITY.equals(
                objectClass.getValues("qualifiedName").get(0))) {
            qualifierName = "name";
        }
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                new Parameter[] {
                     new Parameter("class", objectClass)
                },
                "no composite reference found for class.");
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
                                       null,
                                       "fatal exception." );
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
    private ServiceHeader header = null;
    private RequestCollection reader = null;
    private Model_1_0 model = null;
    private final List startPoints;
    private final List referenceFilters;
    private final Map attributeFilters;

    /** The source paths which have already been traversed */
    private List traversedPaths = new ArrayList();

    // some constants to ease reading
    private final static boolean IS_STATE = true;
    //
    // Model Classes
    //
    protected final static String BASIC_STATE = "org:openmdx:compatibility:state1:BasicState";
    protected final static String AUTHORITY = "org:openmdx:base:Authority";
    
}

//--- End of File -----------------------------------------------------------
