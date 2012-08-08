/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ViewObject_1.java,v 1.41 2008/02/29 18:01:22 hburger Exp $
 * Description: View Object
 * Revision:    $Revision: 1.41 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/29 18:01:22 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
 * listed in the NOTICE file.etObject
 */
package org.openmdx.base.accessor.generic.spi;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.resource.cci.InteractionSpec;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.generic.cci.LargeObject_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.layer.model.State_1_Attributes;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.state1.view.DateStateContext;
import org.openmdx.compatibility.state1.view.DateStateContexts;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_1;
import org.openmdx.model1.code.AggregationKind;


/**
 * An abstract delegating object
 */
@SuppressWarnings("unchecked")
public class ViewObject_1
    implements Object_1_0, Delegating_1_0, ViewObject_1_0
{

    protected ViewObject_1(
        ViewConnection_1 factory,
        Object_1_0 object
    ) throws ServiceException {
        this.factory = factory;
        this.sourceObject = object;
        this.sinkObject = factory.getSink().getObject(object);
    }

    protected ViewObject_1(
        ViewConnection_1 factory,
        SinkObject_1 sinkObject
    ) throws ServiceException {
        this.factory = factory;
        this.sourceObject = null;
        this.sinkObject = sinkObject;
    }
    
    /**
     * Retrieve the object's delegate
     * 
     * @return the object's delegate
     * 
     * @throws ServiceException 
     */
    public Object_1_0 getSourceDelegate(
    ) throws ServiceException { 
        return getSourceDelegate(
            true, // assertSingleton
            false // forStateQuery
         );
    }

    /**
     * Retrieve the object's delegates
     * 
     * @return the object's delegates
     * 
     * @throws ServiceException 
     */
    public List getSinkDelegates(
    ) throws ServiceException { 
        return getSinkDelegates(
            false, // forStateQuery
            false, // forRemoval
            false // asSource
         );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.ViewObject_1_0#getViewContext()
     */
    public InteractionSpec getViewContext() {
        Object context = this.factory.getContext();
        return context instanceof InteractionSpec ? (InteractionSpec)context : null;
    }

    /**
     * Retrieve the object's delegate
     * 
     * @param assertSingleton tells whether an exception should be thrown
     * if the result is not a singleton
     * @param forStateQuery 
     * 
     * @return the object's delegate, or <code>null</code> if the result is
     * not a singleton and <code>assertSingleton</code> is <code>false</code>
     * 
     * @throws ServiceException <ul>
     * <li>INVALID_CARDINALITY if more than one state matches
     * <li>NOT_FOUND if no state matches
     * </ul>
     */
    Object_1_0 getSourceDelegate(
        boolean assertSingleton, 
        boolean forStateQuery
    ) throws ServiceException{
        if(objIsPersistent()) {
            if(isInstanceOfDateState()) {
                DateStateContext context = this.factory.getContext();                
                if(context.isWritable()) {
                    List delegates = getSinkDelegates(
                        forStateQuery, 
                        false, // forRemoval
                        true // asSource
                    );
                    if(delegates.size() == 1) {
                        Object_1_0 delegate = (Object_1_0) delegates.get(0); 
                        if(assertSingleton && !forStateQuery && delegate.objIsDeleted()) {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ILLEGAL_STATE,
                                new BasicException.Parameter[]{
                                    new BasicException.Parameter("context", this.factory.getContext()),
                                    new BasicException.Parameter("deleted", Boolean.TRUE)
                                },
                                "Can't access the features of a deleted state"
                            );
                        }
                        return delegate;
                    } else if (assertSingleton) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.INVALID_CARDINALITY,
                            new BasicException.Parameter[]{
                                new BasicException.Parameter("context", this.factory.getContext()),
                                new BasicException.Parameter("states", delegates.size())
                            },
                            "The object is accessed through a write-only view not referring to exactly one state"
                        );
                    } else {
                        return null;
                    }
                } else {
                    if(this.sinkObject.hasStateCache()) {
                        Object_1_0 delegate = this.sinkObject.getDelegate(
                            context.getValidFor(), 
                            context.getValidAt(), 
                            forStateQuery
                        );
                        if(delegate == null){
                            if (assertSingleton) {
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.NOT_FOUND,
                                    new BasicException.Parameter[]{
                                        new BasicException.Parameter("context", this.factory.getContext()),
                                        new BasicException.Parameter("states", 0)
                                    },
                                    "The object is accessed through a read-only view not referring a valid state"
                                );
                            } else {
                                return null;
                            }
                        }
                        return delegate;
                    } else {
                        return getSourceObject();
                    }
                }
            } else {
                return this.sinkObject.isDirty() || objIsHollow() ?
                    this.sinkObject.getDelegate() :
                    getSourceObject();
            }
        } else {
            return getSourceObject();
        }
    }

    private Object_1_0 getSourceObject(
    ) throws ServiceException{
        if(this.sourceObject == null) {
            if(this.sinkObject.isDirty()) {
                return this.sinkObject.getQualifiedDelegate();
            } else {
                this.sourceObject = this.factory.getSource().getObject(
                    this.sinkObject.objGetPath()
                );
                this.factory.cache(this.sourceObject, this);
            }
        }
        return this.sourceObject;
    }
    
    /**
     * Retrieve the object's delegates
     * 
     * @param forStateQuery 
     * @param forRemoval 
     * @param asSource 
     * 
     * @return the object's delegate
     * 
     * @throws ServiceException 
     */
    private List getSinkDelegates(
        boolean forStateQuery, 
        boolean forRemoval, 
        boolean asSource
    ) throws ServiceException{
        if(objIsPersistent()) {
            if(isInstanceOfDateState()) {
                DateStateContext context = this.factory.getContext();
                if(forStateQuery){
                    if(this.sinkObject.hasStateCache()) {
                        if(context.isWritable()) {
                            return this.sinkObject.getDelegates(
                                context.getValidFrom(), 
                                context.getValidTo(), 
                                forStateQuery, 
                                forRemoval, 
                                false // forRetrieval
                            );
                        } else {
                            Object_1_0 delegate = getSourceDelegate(
                                false, // assertSingleton
                                forStateQuery
                            );
                            return delegate == null ?
                                Collections.EMPTY_LIST :
                                Collections.singletonList(delegate);
                        }
                    } else {
                        return Collections.EMPTY_LIST;
                    }
                } else if (asSource){
                    if(context.isWritable()) {
                        return this.sinkObject.getDelegates(
                            context.getValidFrom(), 
                            context.getValidTo(), 
                            false, // forQuery
                            forRemoval, 
                            true // forRetrieval
                        );
                    } else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE,
                            null,
                            "getSinkelegates and getSourceDelegate call each other"
                        );
                    }
                } else {
                    if(context.isWritable()) {
                        return this.sinkObject.getDelegates(
                            context.getValidFrom(), 
                            context.getValidTo(),
                            forStateQuery,
                            forRemoval, 
                            false // forRetrieval
                        );
                    } else {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED,
                            new BasicException.Parameter[]{
                                new BasicException.Parameter("context", this.factory.getContext())
                            },
                            "The object is accessed through a read-only view"
                        );
                    }
                }
            } else {
                return Collections.singletonList(this.sinkObject.getDelegate());
            }
        } else {
            return Collections.singletonList(getSourceObject());
        }
    }
    
    
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /**
     * Returns a string representation of the object. In general, the toString
     * method returns a string that "textually represents" this object. The
     * result should be a concise but informative representation that is easy
     * for a person to read. It is recommended that all subclasses override this
     * method. 
     * 
     * @return  a string representation of the object.
     */
    public String toString(
    ) {
        String objectClass;
        try {
            objectClass = this.sinkObject == null ? 
                SinkObject_1.lenientGetObjectClass(this.sourceObject) : 
                this.sinkObject.getObjectClass(); 
            if(objectClass == null) {
                objectClass = "n/a";
            }
        } catch (Exception exception) {
            objectClass = "// " + exception;
        }
        return getClass().getName() + ": " + IndentingFormatter.toString(
            ArraysExtension.asMap(
                TO_STRING_KEYES,
                new Object[]{
                    objGetResourceIdentifier(),  
                    objectClass,
                    this.factory.getContext()
                }
            )
        );
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(
        Object obj
    ) {
        if(obj instanceof ViewObject_1) {
            ViewObject_1 that = (ViewObject_1) obj;
            return this.sinkObject == null ?
                this == that :
                this.sinkObject == that.sinkObject;
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.sinkObject == null ?
            super.hashCode() :
            this.sinkObject.hashCode();
    }

    
    //--------------------------------------------------------------------------
    // Implements Object_1_0
    //--------------------------------------------------------------------------

    /**
     * Set the object's model class.
     *
     * @param objectCLass  the object's model class
     * 
     * @throws ServiceException 
     */
    public void objSetClass(
        String objectClass
    ) throws ServiceException {
        if(this.sinkObject == null) {
            SinkObject_1.lenientSetObjectClass(
                this.sourceObject, 
                objectClass
            );
        } else {
            this.sinkObject.setObjectClass(
                objectClass
            );
        }
    }
    
    /**
     * Returns the object's model class.
     *
     * @return  the object's model class
     * @throws ServiceException 
     *
     * @exception   ServiceException  
     *              if the information is unavailable
     */
    public String objGetClass(
    ) throws ServiceException{
        String objectClass;
        if(this.sinkObject == null) {
            objectClass = this.sourceObject.objGetClass();
        } else {
            objectClass = this.sinkObject.getObjectClass();
            if(objectClass == null) {
                this.sinkObject.setObjectClass(
                    objectClass = getSourceObject().objGetClass()
                );
            }
        }
        return objectClass;
    }
    
    protected boolean isInstanceOfDateState(
    ) throws ServiceException{
        return this.factory.getSink().isInstanceOfDateState(this);
    }
    
    /**
     * Returns the object's identity.
     *
     * @return  the object's identity;
     *          or null for transient objects
     * @throws ServiceException 
     */
    public Path objGetPath(
    ) throws ServiceException{
        return this.sinkObject == null ? 
            null : 
            this.sinkObject.objGetPath();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetResourceIdentifier()
     */
    public Object objGetResourceIdentifier() {
        try {
            return objIsPersistent() && !objIsNew() ? objGetPath().toUri() : null;
        } catch (ServiceException exception) {
            try {
                return objGetPath().toUri();
            } catch (ServiceException exception1) {
                return "// " + exception.getMessage();
            }
        }
    }

    /**
     * Returns a new set containing the names of the features in the default
     * fetch group.
     * <p>
     * The returned set is a copy of the original set, i.e. interceptors are
     * free to modify it before passing it on.
     *
     * @return  the names of the features in the default fetch group
     */
    public Set objDefaultFetchGroup(
    ) throws ServiceException {
        return objIsDeleted() ? Collections.EMPTY_SET : getSourceDelegate().objDefaultFetchGroup();
    }
    
    /**
     * Refresh the state of the instance from its provider.
     *
     * @exception   ServiceException 
     *              if the object can't be synchronized
     */
    public void objRefresh(
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            null,
            "Refresh is not supported for views"
        );
    }

    /**
     * Mark an object as volatile, i.e POST_RELOAD InstanceCallbackEvents
     * may be fired. 
     *
     * @exception   ServiceException 
     *              if the object can't be made volatile.
     */
    public void objMakeVolatile(
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            null,
            "Make volatile is not supported for views"
        );
    }

    /**
     * Flush the state of the instance to its provider.
     * 
     * @return      true if all attributes could be flushed,
     *              false if some attributes contained placeholders
     *
     * @exception   ServiceException NOT_SUPPORTED
     *              if the unit of work is optimistic
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is not persistent
     * @exception   ServiceException 
     *              if the object can't be synchronized
     */
    public boolean objFlush(
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            null,
            "Flush is not supported for views"
        );
    }
    

    //--------------------------------------------------------------------------
    // Unit of work boundaries
    //--------------------------------------------------------------------------

    /**
     * After this call the object observes unit of work boundaries.
     * <p>
     * This method is idempotent.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is locked 
     * @exception   ServiceException 
     *              if the object can't be added to the unit of work for
     *        another reason.
     */
    public void objAddToUnitOfWork(
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_IMPLEMENTED,
            null,
            "Add to unit of work is not yet implemented for views"
        );
    }
     
    /**
     * After this call the object ignores unit of work boundaries.
     * <p>
     * This method is idempotent.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is dirty.
     * @exception   ServiceException 
     *              if the object can't be removed from its unit of work for
     *        another reason 
     */
    public void objRemoveFromUnitOfWork(
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            null,
            "Remove from unit of work is not supported for views"
        );
    }

    //--------------------------------------------------------------------------
    // Life Cycle Operations
    //--------------------------------------------------------------------------

    /**
     * The copy operation makes a copy of the object. The copy is located in the
     * scope of the container passed as the first parameter and includes the
     * object's default fetch set.
     *
     * @return    an object initialized from the existing object.
     * 
     * @param     there
     *            the new object's container or <code>null</code>, in which case
     *            the object will not belong to any container until it is moved
     *            to a container.
     * @param     criteria
     *            The criteria is used to add the object to the container or 
     *            <code>null</null>, in which case it is up to the
     *            implementation to define the criteria.
     *
     * @exception ServiceException
     *            if the copy operation fails.
     */
    public Object_1_0 objCopy(
        FilterableMap there,
        String criteria
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            null,
            "Copy is not supported for views"
        );
    } 

    SinkConnection_1 getSinkConnection(
    ){
        return this.sinkObject.getConnection();
    }
    
    boolean containsInstancesOfDateState(
        String feature
    ) throws ServiceException {
        return this.getSinkConnection().containsInstancesOfDateState(
            this.objGetClass(), 
            feature
        );
    }
    
    FilterableMap getSinkContainer(
        String feature
    ) throws ServiceException{
        return this.sinkObject.objGetContainer(feature);
    }
    
    /**
     * The move operation moves the object to the scope of the container passed
     * as the first parameter. The object remains valid after move has
     * successfully executed.
     *
     * @param     there
     *            the object's new container.
     * @param     criteria
     *            The criteria is used to move the object to the container or 
     *            <code>null</null>, in which case it is up to the
     *            implementation to define the criteria.
     *
     * @exception ServiceException  ILLEGAL_STATE
     *            if the object is persistent.
     * @exception ServiceException BAD_PARAMETER
     *            if <code>there</code> is <code>null</code>.
     * @exception ServiceException  
     *            if the move operation fails.
     */
    public void objMove(
        FilterableMap there,
        String criteria
    ) throws ServiceException {
        if(objIsPersistent()) throw new ServiceException (
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            new BasicException.Parameter[]{
                new BasicException.Parameter("path", objGetPath())
            },
            "Object is already persistent"
        );
        ViewContainer_1 target = (ViewContainer_1) there;
        if(target.isPersistent()) {
            Path identity = target.getPath().getChild(criteria);
            if(isInstanceOfDateState()){
                if(criteria == null) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    null,
                    "Adding date state views with null qualifiers is not supported"
                );
                this.sinkObject = this.factory.getSink().getObject(
                    identity, 
                    objGetClass()
                );
                this.sourceObject.objMove(
                    target.getSinkContainer(),
                    SinkObject_1.toCriteria(
                        null, // criteria
                        criteria, // qualifier
                        (String)this.sourceObject.objGetValue(State_1_Attributes.STATE_VALID_FROM), 
                        (String)this.sourceObject.objGetValue(State_1_Attributes.STATE_VALID_TO)
                    )
                );
                this.sinkObject.addState(this.sourceObject);
            } else {
               this.sourceObject.objMove(
                   target.getSinkContainer(), 
                   criteria
               );
               this.sinkObject = this.factory.getSink().getObject(
                   this.sourceObject
               );
               this.sinkObject.validateDelegate();
            }
            moveChildren(identity);
        } else {
            target.put(criteria, this);
        }
        if(factory != target.getParent().factory) {
            this.factory.evict(this);
            this.factory = target.getParent().factory;
            this.factory.cache(this.sourceObject, this);
        }
    } 
    
    private void moveChildren(
        Path path
    ) throws ServiceException {
        if(this.containers != null) {
            for(
                Iterator i = this.containers.entrySet().iterator();
                i.hasNext();
            ){
                Map.Entry containerEntry = (Map.Entry) i.next();
                ViewContainer_1 container = (ViewContainer_1)containerEntry.getValue(); 
                for(
                    Iterator j = container.fetchTransientObjects().entrySet().iterator();
                    j.hasNext();
                ){
                    Map.Entry objectEntry = (Map.Entry) j.next();
                    ViewObject_1 object = (ViewObject_1)objectEntry.getValue(); 
                    String qualifier = (String)objectEntry.getKey();
                    if(qualifier == null) {
                        System.err.println("Wait a moment");
                    } else {
                        object.objMove(container, qualifier);
                    }
                }
            }
        }
    }
     
    /**
     * Removes an object. 
     * <p>
     * Neither <code>getValue()</code> nor <code>setValue()</code>
     * calls are allowed after an <code>remove()</code> invocation and
     * <code>isDeleted()</code> will return <code>true</code> unless the
     * object has been transient.
     *
     * @exception   ServiceException NOT_SUPPORTED
     *              If the object refuses to be removed.
     * @exception   ServiceException 
     *              if the object can't be removed
     */
    public void objRemove(
    ) throws ServiceException {
        if(isInstanceOfDateState()) {
            if(!this.factory.getContext().isWritable()) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("context", this.factory.getContext()),
                    new BasicException.Parameter("writable", false)
                },
                "A time range view is required to remove states of a DateState instance"
            );
            List delegates = getSinkDelegates(
                false, // forStateQuery
                true, // forRemoval
                false // asSource
            );
            if(!delegates.isEmpty()) {
                //
                // Remove the children (in the given time range)
                //
                boolean removed = !this.sinkObject.isInvolved(
                    (XMLGregorianCalendar)null, 
                    (XMLGregorianCalendar)null, 
                    false // defaultValue
                );
                Model_1_1 model = (Model_1_1) this.factory.getModel();
                Map features = model.getStructuralFeatureDefs(
                    model.getElement(this.objGetClass()), 
                    false, // includeSubtypes, 
                    false, // includeDerived, 
                    false // attributesOnly
                );
                for(
                    Iterator i = features.entrySet().iterator();
                    i.hasNext();
                ){
                    Map.Entry e = (Map.Entry) i.next();
                    ModelElement_1_0 feature = (ModelElement_1_0) e.getValue();
                    if(
                        model.isReferenceType(feature) &&
                        Boolean.TRUE.equals(feature.getValues("isChangeable").get(0))
                    ) {
                        ModelElement_1_0 associationEnd = model.getElement(
                            feature.getValues("referencedEnd").get(0)
                        );
                        if(AggregationKind.COMPOSITE.equals(associationEnd.getValues("aggregation").get(0))) {
                            ViewContainer_1 children = (ViewContainer_1) objGetContainer((String)e.getKey());
                            children.clear(removed);
                        }
                    }
                }
                //
                // Remove the states (in the given time range)
                //
                if(delegates.size() == 1) {
                    Object_1_0 delegate = (Object_1_0) delegates.get(0);
                    this.sinkObject.deleteState(delegate);
                } else {
                    while(!delegates.isEmpty()) {
                        Object_1_0 first = (Object_1_0) delegates.remove(0);
                        Object_1_0 last = first;
                        while(
                            !delegates.isEmpty() &&
                            adjacent(last, (Object_1_0) delegates.get(0))
                        ) {
                            this.sinkObject.invalidateState(last);
                            last = (Object_1_0) delegates.remove(0); 
                        }
                        if(first == last) {
                            this.sinkObject.deleteState(
                                last
                            );
                        } else {
                            this.sinkObject.deleteState(
                                last,
                                (String)first.objGetValue(State_1_Attributes.STATE_VALID_FROM),
                                (String)last.objGetValue(State_1_Attributes.STATE_VALID_TO)
                            );
                        }
                    }
                }
            }
        } else {
            Object_1_0 delegate = (Object_1_0) getSinkDelegates(
                false, // forStateQuery
                true, // forRemoval
                false // asSource
            ).get(
                0
            );
            delegate.objRemove();
        }
    }

    private boolean adjacent(
        Object_1_0 state1,
        Object_1_0 state2
    ) throws ServiceException{  
        return DateStateContexts.adjacent(
            (String)state1.objGetValue(State_1_Attributes.STATE_VALID_TO),
            (String)state2.objGetValue(State_1_Attributes.STATE_VALID_FROM)
        );
    }
    
    //--------------------------------------------------------------------------
    // State Queries    
    //--------------------------------------------------------------------------

    /**
     * Tests whether this object is dirty. Instances that have been modified,
     * deleted, or newly made persistent in the current unit of work return
     * true.
     * <p>
     * Transient instances return false. 
     * 
     * @return true if this instance has been modified in the current unit
     *         of work.
     */ 
    public boolean objIsDirty(
    ) throws ServiceException {
        if(objIsPersistent()) {
            for(
                Iterator i = getSinkDelegates(
                    true, // forStateQuery 
                    false, // forRemoval
                    false // asSource
                ).iterator();
                i.hasNext();
            ){
                Object_1_0 delegate = (Object_1_0) i.next();
                if(delegate.objIsDirty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tests whether this object is persistent. Instances that represent
     * persistent objects in the data store return true. 
     * 
     * @return true if this instance is persistent.
     */
    public boolean objIsPersistent(
    ){
        return this.sinkObject != null;
    }

    /**
     * Tests whether this object has been newly made persistent. Instances
     * that have been made persistent in the current unit of work return true. 
     * <p>
     * Transient instances return false. 
     *
     * @return  true if this instance was made persistent in the current unit
     *          of work. 
     * @throws ServiceException 
     */
    public boolean objIsNew(
    ) throws ServiceException{
        return this.objIsPersistent() && (
            this.sourceObject == null ? this.sinkObject.objIsNew() : this.sourceObject.objIsNew()
        );
    }

    /**
     * Tests whether this object has been deleted. Instances that have been
     * deleted in the current unit of work return true. 
     * Transient instances return false. 
     *
     * @return  true if this instance was deleted in the current unit of work.
     */
    public boolean objIsDeleted(
    ) throws ServiceException {
        Collection delegates = getSinkDelegates(
            true, // forStateQuery
            false, // forRemoval
            false // asSource
        );
        boolean deleted = false;
        for(
            Iterator i = delegates.iterator();
            i.hasNext();
        ){
            Object_1_0 object = (Object_1_0) i.next();
            if(object.objIsDeleted()) {
                deleted = true;
            } else {
                return false;
            }
        }
        return deleted;
    }

    /**
     * Tests whether this object belongs to the current unit of work.
     *
     * @return  true if this instance belongs to the current unit of work.
     */
    public boolean objIsInUnitOfWork(
    ) throws ServiceException {
        for(
            Iterator i = getSinkDelegates(
                true, // forStateQuery
                false, // forRemoval
                false // asSource
            ).iterator();
            i.hasNext();
        ){
            Object_1_0 delegate = (Object_1_0) i.next();
            if(delegate.objIsInUnitOfWork()) {
                return true;
            }
        }
        return false;
    }

    private boolean objIsHollow(
    ) throws ServiceException {
        return SinkObject_1.lenientGetObjectClass(this.sourceObject) == null;
    }
    
    //--------------------------------------------------------------------------
    // Values
    //--------------------------------------------------------------------------

    Object toSinkValue (
        Object source
    ) throws ServiceException {
        if(source instanceof ViewObject_1) {
            ViewObject_1 object = (ViewObject_1) source;
            return object.objIsPersistent() ?
                object.sinkObject.getDelegate() :
                object.sourceObject;
        } else {
            return source;
        }
    }

    Collection toSinkValue (
        Collection source
    ) throws ServiceException {
        Collection value = new ArrayList();
        for(
            Iterator i = source.iterator();
            i.hasNext();
        ){
            value.add(toSinkValue(i.next()));
        }
        return value;
    }
    
    Object toViewValue (
        Object source
    ) throws ServiceException {
        if(source instanceof Object_1_0) {
            Object_1_0 object = (Object_1_0) source;
            return object.objIsPersistent() ?
                this.factory.getObject(object.objGetPath()) :
                object;
        } else {
            return source;
        }
    }

    Object_1_0 toViewValue (
        Path path, 
        boolean dateStateInstance, 
        boolean initializeCacheWithDelegate
    ) throws ServiceException {
        return path == null ? null : this.factory.getObject(
            path,
            Boolean.valueOf(dateStateInstance), 
            initializeCacheWithDelegate || (
                objIsNew() && path.startsWith(objGetPath())
            )
        );
    }
    
    private boolean isStateIncapable(
    ) throws ServiceException {
        Object_1_0 context = this.sinkObject.getQualifiedDelegate(
        ).objGetContainer(
            SystemAttributes.CONTEXT_CAPABLE_CONTEXT
        ).get(
            State_1_Attributes.STATE_CONTEXT
        );
        return "org:openmdx:compatibility:state1:StateIncapable".equals(context.objGetClass());
    }
    
    /**
     * Set an attribute's value.
     * <p>
     * This method returns a <code>BAD_PARAMETER</code> exception unless the 
     * feature is single valued or a stream. 
     *
     * @param       feature
     *              the attribute's name
     * @param       to
     *              the object.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is write protected or the feature is a
     *        stream modified in the current unit of work.
     * @exception   ServiceException BAD_PARAMETER
     *              if the feature is multi-valued
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ServiceException 
     *              if the object is not accessible
     */
    public void objSetValue(
        String feature,
        Object to
    ) throws ServiceException {
        if(
            isInstanceOfDateState() &&
            objIsPersistent() && (
                 State_1_Attributes.STATE_VALID_FROM.equals(feature) ||
                 State_1_Attributes.STATE_VALID_TO.equals(feature)
            ) && (
                 objIsNew() || 
                 !isStateIncapable()
            )
        ) {
           throw new ServiceException(
               BasicException.Code.DEFAULT_DOMAIN,
               BasicException.Code.ILLEGAL_STATE,
               new BasicException.Parameter[]{
                   new BasicException.Parameter("identity", objGetResourceIdentifier()),
                   new BasicException.Parameter("persistent", Boolean.TRUE),
                   new BasicException.Parameter("feature", feature),
                   new BasicException.Parameter("value", to)
               },
               "A persistent states validity is read-only"
           );
        } else {
            Object value = toSinkValue(to);
            for(
                Iterator i = getSinkDelegates().iterator();
                i.hasNext();
            ){
                Object_1_0 delegate = (Object_1_0) i.next();
                delegate.objSetValue(feature, value);
            }
        }
    }

    /**
     * Get a feature.
     *
     * @param       feature
     *              the feature's name
     *
     * @return      the object representing the feature;
     *              or null if the feature's value hasn't been set yet.
     *
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ServiceException 
     *              if the object is not accessible
     */
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        DateStateContext context = this.factory.getContext();
        if(context.isWritable()) {
            if(State_1_Attributes.STATE_VALID_FROM.equals(feature)) {
                return SinkObject_1.toBasicFormat(context.getValidFrom());
            } else if (State_1_Attributes.STATE_VALID_TO.equals(feature)) {
                return SinkObject_1.toBasicFormat(context.getValidTo());
            }
        }
        return toViewValue(
            getSourceDelegate().objGetValue(feature)
        );
    }
    
    /**
     * Get a List attribute.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ClassCastException
     *              if the feature's value is not a list
     */
    public List objGetList(
        String feature
    ) throws ServiceException {
        return new ListView(feature);
    }
        
    /**
     * Get a Set attribute.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ClassCastException
     *              if the feature's value is not a set
     */
    public Set objGetSet(
        String feature
    ) throws ServiceException {
        return new SetView(feature);
    }
    
    /**
     * Get a SparseArray attribute.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ClassCastException
     *              if the feature's value is not a sparse array
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     */
    public SortedMap objGetSparseArray(
        String feature
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_IMPLEMENTED,
            null,
            "Sparse array access is not yet implemented for views"
        );
    }
        
    /**
     * Get a large object feature
     * <p> 
     * This method returns a new LargeObject.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a large object which may be empty but never is null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ClassCastException
     *              if the feature's value is not a large object
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     */
    public LargeObject_1_0 objGetLargeObject(
        String feature
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_IMPLEMENTED,
            null,
            "Large object handling is not yet implemented for views"
        );
    }

    /**
     * Get a reference feature.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ClassCastException
     *              if the feature is not a reference
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     */
    public FilterableMap objGetContainer(
        String feature
    ) throws ServiceException {
        String referenceName = feature.endsWith(SystemAttributes.USE_OBJECT_IDENTITY_HINT) ?
            feature.substring(0, feature.length() - SystemAttributes.USE_OBJECT_IDENTITY_HINT.length()) :
            feature;
        FilterableMap container;    
        if(this.containers == null) {
            this.containers = new HashMap();
            container = null;
        } else {
            container = (FilterableMap) this.containers.get(referenceName);
        }
        if(container == null) {
            this.containers.put(
                referenceName, 
                container = new ViewContainer_1(
                    this.factory, 
                    this,
                    referenceName
                )
            );
        }
        return container;
    }

    
    //--------------------------------------------------------------------------
    // Operations
    //--------------------------------------------------------------------------

    /**
     * Invokes an operation asynchronously.
     *
     * @param       operation
     *              The operation name
     * @param       arguments
     *              The operation's arguments object. 
     *
     * @return      a structure with the result's values if the accessor is
     *        going to populate it after the unit of work has committed
     *        or null if the operation's return value(s) will never be
     *        available to the accessor.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if no unit of work is in progress
     * @exception   ServiceException NOT_SUPPORTED
     *              if either asynchronous calls are not supported by the 
     *        manager or the requested operation is not supportd by the
     *        object.
     * @exception   ServiceException 
     *        if the invocation fails for another reason
     */
    public Structure_1_0 objInvokeOperationInUnitOfWork(
        String operation,
            Structure_1_0 arguments
    ) throws ServiceException {
        Collection delegates = getSinkDelegates();
        if(delegates.size() == 1) {
            Object_1_0 delegate = (Object_1_0) delegates.iterator().next();
            return delegate.objInvokeOperationInUnitOfWork(
                operation,
                arguments
            );
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CARDINALITY,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("involvedStates", delegates.size())
                },
                "Operations must be invoked on a single state"
            );
        }
    }

    /**
     * Invokes an operation synchronously.
     * <p>
     * Only query operations can be invoked synchronously unless the unit of
     * work is non-optimistic or committing.
     *
     * @param       operation
     *              The operation name
     * @param       arguments
     *              The operation's arguments object. 
     *
     * @return      the operation's return object
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if a non-query operation is called in an inappropriate
     *        state of the unit of work.
     * @exception   ServiceException NOT_SUPPORTED
     *              if either synchronous calls are not supported by the 
     *        manager or the requested operation is not supportd by the
     *        object.
     * @exception   ServiceException 
     *        if a checked exception is thrown by the implementation or
     *        the invocation fails for another reason.
     */
    public Structure_1_0 objInvokeOperation(
        String operation,
        Structure_1_0 arguments
    ) throws ServiceException {
        return getSourceDelegate().objInvokeOperation(
            operation,
            arguments
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objAddEventListener(java.lang.String, java.util.EventListener)
     */
    public void objAddEventListener(
        String feature, 
        EventListener listener
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            null,
            "Event listeners are not supported for views"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRemoveEventListener(java.lang.String, java.util.EventListener)
     */
    public void objRemoveEventListener(
        String feature, 
        EventListener listener
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            null,
            "Event listeners are not supported for views"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetEventListeners(java.lang.String, java.lang.Class)
     */
    public EventListener[] objGetEventListeners(
        String feature, 
        Class listenerType
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            null,
            "Event listeners are not supported for views"
        );
    }


    //--------------------------------------------------------------------------
    // Synchronization
    //--------------------------------------------------------------------------

    /**
     * Register a synchronization object for upward delegation.
     *
     * @param   synchronization
     *          The synchronization object to be registered
     *
     * @exception ServiceException TOO_MANY_EVENT_LISTENERS
     *            if an attempt is made to register more than one 
     *            synchronization object.
     * 
     * @deprecated
     */
    public void objRegisterSynchronization(
    org.openmdx.compatibility.base.accessor.object.cci.InstanceCallbacks_1_0 synchronization
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            null,
            "Registration of synchronization objects is not supported for views"
        );
    }

    
    //--------------------------------------------------------------------------
    // Implements Delegating_1_0
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Delegating_1_0#objGetDelegate()
     */
    public Object objGetDelegate() {
        return this.sourceObject;
    }

    
    //--------------------------------------------------------------------------
    // Instance Members
    //--------------------------------------------------------------------------

    private static final String[] TO_STRING_KEYES = new String[]{
        "resourceIdentifier",
        "class",
        "context"
    };

    /**
     * The object's context
     */
    private SinkObject_1 sinkObject;

    /**
     * The object's context
     */
    private Object_1_0 sourceObject;

    /**
     * The object's factory
     */
    private ViewConnection_1 factory;

    private Map containers = null;
    
    
    //--------------------------------------------------------------------------
    // Collection Classes
    //--------------------------------------------------------------------------

    /**
     * 
     */
    class SetView extends AbstractSet {

        /**
         * Constructor 
         *
         * @param feature
         */
        SetView(String feature) {
            this.feature = feature;
        }
        
        private final String feature;
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#clear()
         */
        public void clear() {
            try {
                for(
                     Iterator i = getSinkDelegates().iterator();
                     i.hasNext();
                ){
                    Object_1_0 delegate = (Object_1_0) i.next();
                    delegate.objGetSet(this.feature).clear();
                }
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        public Iterator iterator() {
            try {
                return new MarshallingIterator(
                    getSourceDelegate().objGetSet(this.feature).iterator()
                );
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        public int size() {
            try {
                return getSourceDelegate().objGetSet(this.feature).size();
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#add(java.lang.Object)
         */
        public boolean add(Object o) {
            try {
                boolean reply = false;
                Object value = toSinkValue(o);
                for(
                     Iterator i = getSinkDelegates().iterator();
                     i.hasNext();
                ){
                    Object_1_0 delegate = (Object_1_0) i.next();
                    reply |= delegate.objGetSet(this.feature).add(value);
                }
                return reply;
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#remove(java.lang.Object)
         */
        public boolean remove(Object o) {
            try {
                boolean reply = false;
                Object value = toSinkValue(o);
                for(
                     Iterator i = getSinkDelegates().iterator();
                     i.hasNext();
                ){
                    Object_1_0 delegate = (Object_1_0) i.next();
                    reply |= delegate.objGetSet(this.feature).remove(value);
                }
                return reply;
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        /* (non-Javadoc)
         * @see java.util.AbstractSet#removeAll(java.util.Collection)
         */
        public boolean removeAll(Collection c) {
            try {
                boolean reply = false;
                Collection value = toSinkValue(c);
                for(
                     Iterator i = getSinkDelegates().iterator();
                     i.hasNext();
                ){
                    Object_1_0 delegate = (Object_1_0) i.next();
                    reply |= delegate.objGetSet(this.feature).removeAll(value);
                }
                return reply;
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        
    }

    class MarshallingIterator implements Iterator {
    
        /**
         * Constructor 
         *
         * @param delegate
         */
        MarshallingIterator(
            Iterator delegate
        ) {
            this.delegate = delegate;
        }
                        
        private final Iterator delegate;
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return delegate.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Object next() {
            try {
                return toViewValue(this.delegate.next());
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(
                    exception
                );
            }
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            this.delegate.remove();
        }

    }
    
    /**
     * ListView
     */
    class ListView extends AbstractList {
                
        /**
         * Constructor 
         *
         * @param feature
         */
        ListView(String feature) {
            this.feature = feature;
        }
        
        private final String feature;
        
        /* (non-Javadoc)
         * @see java.util.AbstractList#clear()
         */
        public void clear() {
            try {
                for(
                     Iterator i = getSinkDelegates().iterator();
                     i.hasNext();
                ){
                    Object_1_0 delegate = (Object_1_0) i.next();
                    delegate.objGetList(this.feature).clear();
                }
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        /* (non-Javadoc)
         * @see java.util.AbstractList#get(int)
         */
        public Object get(int index) {
            try {
                return toViewValue(
                    getSourceDelegate().objGetList(this.feature).get(index)
                );
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        public int size() {
            try {
                return getSourceDelegate().objGetList(this.feature).size();
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        /* (non-Javadoc)
         * @see java.util.AbstractList#add(java.lang.Object)
         */
        public boolean add(Object o) {
            try {
                boolean reply = false;
                Object value = toSinkValue(o);
                for(
                     Iterator i = getSinkDelegates().iterator();
                     i.hasNext();
                ){
                    Object_1_0 delegate = (Object_1_0) i.next();
                    reply |= delegate.objGetList(this.feature).add(value);
                }
                return reply;
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        /* (non-Javadoc)
         * @see java.util.AbstractList#add(int, java.lang.Object)
         */
        public void add(int index, Object element) {
            try {
                Object value = toSinkValue(element);
                for(
                     Iterator i = getSinkDelegates().iterator();
                     i.hasNext();
                ){
                    Object_1_0 delegate = (Object_1_0) i.next();
                    delegate.objGetList(this.feature).add(index,value);
                }
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        /* (non-Javadoc)
         * @see java.util.AbstractList#remove(int)
         */
        public Object remove(int index) {
            try {
                boolean first = true;
                Object reply = null;  
                Collection delegates = getSinkDelegates();
                for(
                     Iterator i = delegates.iterator();
                     i.hasNext();
                     first = false
                ){
                    Object_1_0 delegate = (Object_1_0) i.next();
                    Object candidate = delegate.objGetList(this.feature).remove(index);
                    if(first) {
                        reply = candidate;
                    } else if (
                        !(reply instanceof ServiceException) &&
                        reply != candidate && (
                            reply == null || 
                            !reply.equals(candidate)
                        )
                    ) {
                        reply = new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.INVALID_CARDINALITY,
                            new BasicException.Parameter[]{
                                new BasicException.Parameter("involvedStates", delegates.size())
                            },
                            "Removal returned different values for the different states"
                        );
                    }
                }
                return reply;
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        /* (non-Javadoc)
         * @see java.util.AbstractList#set(int, java.lang.Object)
         */
        public Object set(
            int index, 
            Object element
        ) {
            try {
                Object value = toSinkValue(element);
                boolean first = true;
                Object reply = null;  
                Collection delegates = getSinkDelegates();
                for(
                     Iterator i = delegates.iterator();
                     i.hasNext();
                     first = false
                ){
                    Object_1_0 delegate = (Object_1_0) i.next();
                    Object candidate = delegate.objGetList(this.feature).set(index, value);
                    if(first) {
                        reply = candidate;
                    } else if (
                        !(reply instanceof ServiceException) &&
                        reply != candidate && (
                            reply == null || 
                            !reply.equals(candidate)
                        )
                    ) {
                        reply = new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.INVALID_CARDINALITY,
                            new BasicException.Parameter[]{
                                new BasicException.Parameter("involvedStates", delegates.size())
                            },
                            "Set returned different values for the different states"
                        );
                    }
                }
                return reply;
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }        
        
    }
    
    /**
     * Retrieve all states of an object
     * 
     * @param invalidated tells whether one looks for valid or invalid states 
     * @param deleted tells whether one non-deleted or deleted states
     * 
     * @return all states of an object matching the given criteria
     * 
     * @throws ServiceException 
     */
    public Collection allStates(
        Boolean invalidated, 
        Boolean deleted
    ) throws ServiceException{
        return this.sinkObject.allStates(invalidated, deleted);
    }
    
    
    public boolean isReadable(
    ){
        try {
            Object_1_0 delegate = getSourceDelegate(
                false, // assertSingleton 
                false // stateQuery
            );
            return delegate != null && !delegate.objIsDeleted();
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("assertSingleton", false)
                },
                "This excpetion should not occur"
            );
        }        
    }

    public void cloneSourceDelegate(
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ) throws ServiceException {
        this.sinkObject.cloneDelegate(
            getSourceDelegate(),
            validFrom, 
            validTo
        );
    }

    Collection getSinkObjects(
        String feature, 
        boolean fetch
    ) throws ServiceException{
        return this.sinkObject.getSinkObjects(feature, fetch);
    }
    
    /**
     * Tells whether there exist underlying valid states
     * 
     * @return <code>true</code> if there exists at least one underlying valid state
     */
    public boolean exists(
    ) throws ServiceException {
        DateStateContext context = this.factory.getContext();
        if(
            this.sinkObject.hasStateCache() ||
            context.isWritable()
        ) {
            //
            // Object might be dirty
            //
            return !this.sinkObject.getDelegates(
                context.isWritable() ? context.getValidFrom() : context.getValidFor(), 
                context.isWritable() ? context.getValidTo() : context.getValidFor(),
                false, // forQuery
                false, // forRemoval
                true // forRetrieval
            ).isEmpty();
        } else {
            //
            // Object is clean
            //
            this.sourceObject.objGetClass(); // Assert Accessibility 
            return true;
        }
    }

}
