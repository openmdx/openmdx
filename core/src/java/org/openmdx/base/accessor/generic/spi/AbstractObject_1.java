/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractObject_1.java,v 1.17 2008/06/27 16:59:27 hburger Exp $
 * Description: SPICE Object Layer: Abstract Object_1_0 Implementation
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/27 16:59:27 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.generic.spi;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.WeakHashMap;

import org.openmdx.base.accessor.generic.cci.JdoStates_1_0;
import org.openmdx.base.accessor.generic.cci.LargeObject_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.event.InstanceCallbackEvent;
import org.openmdx.compatibility.base.event.InstanceCallbackListener;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.format.IndentingFormatter;

/**
 * Abstract Object_1_0 implementation
 */
@SuppressWarnings("unchecked")
public class AbstractObject_1 implements 
    Object_1_0 
{

    protected AbstractObject_1(
        Path identity,
        String objectClass
    ){
        this.state = identity == null ? JdoStates_1_0.TRANSIENT : JdoStates_1_0.PERSISTENT;
        this.identity = identity;
        this.objectClass = objectClass;
    }
    
    protected short state;        
    protected Path identity;
    protected String objectClass;
    protected final Set dirty = new HashSet();
    
    private Map listeners = new WeakHashMap();
    
    //------------------------------------------------------------------------
    // Implements Object_1_0
    //------------------------------------------------------------------------
    
    /** 
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetPath()
     * <p>
     * While in general an Object implementing Object_1_0 is allowed to throw 
     * a ServiceException for status requests, the AbstractObject_1 does
     * not include them in its status method declarations.
     */
    public Path objGetPath(
    ){
        return this.identity; 
    }

    /**
     * Returns the object's access path.
     *
     * @return  the object's access path;
     *          or null for transient or new objects
     */
    public Object objGetResourceIdentifier(
    ){
        return objIsPersistent() && !objIsNew() ? this.identity.toUri() : null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objDefaultFetchGroup()
     */
    public Set objDefaultFetchGroup() throws ServiceException {
        return new HashSet();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetClass()
     * <p>
     * While in general an Object implementing Object_1_0 is allowed to throw 
     * a ServiceException for status requests, the AbstractObject_1 does
     * not include them in its statsu method declarations.
     */
    public String objGetClass(
    ){
        return this.objectClass; 
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objAddToUnitOfWork()
     */
    public void objAddToUnitOfWork() throws ServiceException {
        if(objIsInUnitOfWork()) return;
        this.state |= JdoStates_1_0.TRANSACTIONAL;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRemoveFromUnitOfWork()
     */
    public void objRemoveFromUnitOfWork() throws ServiceException {
        if(objIsDirty()) throw new ServiceException (
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            getExceptionParameters(),
            "Attempt to remove a dirty object from the unit of work"
        );
        this.state &= ~JdoStates_1_0.TRANSACTIONAL;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objIsDirty()
     * <p>
     * While in general an Object implementing Object_1_0 is allowed to throw 
     * a ServiceException for status requests, the AbstractObject_1 does
     * not include them in its statsu method declarations.
     */
    public boolean objIsDirty(){
        return (this.state & JdoStates_1_0.DIRTY) != 0;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objIsPersistent()
     * <p>
     * While in general an Object implementing Object_1_0 is allowed to throw 
     * a ServiceException for status requests, the AbstractObject_1 does
     * not include them in its statsu method declarations.
     */
    public boolean objIsPersistent(){
        return (this.state & JdoStates_1_0.PERSISTENT) != 0;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objIsNew()
     * <p>
     * While in general an Object implementing Object_1_0 is allowed to throw 
     * a ServiceException for status requests, the AbstractObject_1 does
     * not include them in its statsu method declarations.
     */
    public boolean objIsNew(){
        return (this.state & JdoStates_1_0.NEW) != 0;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objIsDeleted()
     * <p>
     * While in general an Object implementing Object_1_0 is allowed to throw 
     * a ServiceException for status requests, the AbstractObject_1 does
     * not include them in its statsu method declarations.
     */
    public boolean objIsDeleted(){
        return (this.state & JdoStates_1_0.DELETED) != 0;
    }

    /**
     * Tests whether this object belongs to the current unit of work.
     *
     * @return  true if this instance belongs to the current unit of work.
     * <p>
     * While in general an Object implementing Object_1_0 is allowed to throw 
     * a ServiceException for status requests, the AbstractObject_1 does
     * not include them in its statsu method declarations.
     */
    public boolean objIsInUnitOfWork(
    ){
        return (this.state & JdoStates_1_0.TRANSACTIONAL) != 0;
    }

    protected BasicException.Parameter[] getExceptionParameters(
    ){
        return new BasicException.Parameter[]{
            new BasicException.Parameter("path",objGetPath()), //...              
            new BasicException.Parameter("class",new String[]{objGetClass(), getClass().getName()}),
            new BasicException.Parameter("state",stateToString(this)),
        };
    }

    protected BasicException.Parameter[] getExceptionParameters(
        BasicException.Parameter[] extension
    ){
        return BasicException.Parameter.add(
            getExceptionParameters(),
            extension
        );  
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRefresh()
     */
    public void objRefresh() throws ServiceException {
        //
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objFlush()
     * 
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
    public boolean objFlush() throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            getExceptionParameters(),
            "This object is unmodifiable"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objMakeVolatile()
     * 
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
            getExceptionParameters(),
            "This object can't be made volatile"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objCopy(org.openmdx.base.collection.FilterableMap, java.lang.String)
     */
    public Object_1_0 objCopy(
        FilterableMap there, 
        String criteria
    ) throws ServiceException {
        throw new ServiceException (
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_IMPLEMENTED,
            getExceptionParameters(),
            "Copy not implemented yet"
        ); //... Copy not implemented yet
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objMove(org.openmdx.base.collection.FilterableMap, java.lang.String)
     */
    public void objMove(
        FilterableMap there, 
        String criteria
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            getExceptionParameters(),
            "This object is unmodifiable"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRemove()
     */
    public void objRemove() throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            getExceptionParameters(),
            "This object is unmodifiable"
        );
    }

    protected void setValue(
        String feature, 
        Object to
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            getExceptionParameters(
                new BasicException.Parameter[]{
                    new BasicException.Parameter("feature",feature)
                }
            ),
            "This object has no such feature or it is unmodifiable"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objSetValue(java.lang.String, java.lang.Object)
     */
    public void objSetValue(
        String feature, 
        Object to
    ) throws ServiceException {
        PropertyChangeEvent event = new PropertyChangeEvent(
            this,
            feature,
            objGetValue(feature),
            to
        );
        try {
            vetoableChange(event);
        } catch (PropertyVetoException exception) {
            throw new ServiceException(exception);
        }
        setValue(feature, to);
        this.propertyChange(event);
    }


    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetValue(java.lang.String)
     */
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            getExceptionParameters(
                new BasicException.Parameter[]{
                    new BasicException.Parameter("feature",feature)
                }
            ),
            "This object has no such feature"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetList(java.lang.String)
     */
    public List objGetList(
        String feature
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            getExceptionParameters(
                new BasicException.Parameter[]{
                    new BasicException.Parameter("feature",feature)
                }
            ),
            "This object has no such feature"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetSet(java.lang.String)
     */
    public Set objGetSet(
        String feature
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            getExceptionParameters(
                new BasicException.Parameter[]{
                    new BasicException.Parameter("feature",feature)
                }
            ),
            "This object has no such feature"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetSparseArray(java.lang.String)
     */
    public SortedMap objGetSparseArray(
        String feature
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            getExceptionParameters(
                new BasicException.Parameter[]{
                    new BasicException.Parameter("feature",feature)
                }
            ),
            "This object has no such feature"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetLargeObject(java.lang.String)
     */
    public LargeObject_1_0 objGetLargeObject(
        String feature
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            getExceptionParameters(
                new BasicException.Parameter[]{
                    new BasicException.Parameter("feature",feature)
                }
            ),
            "This object has no such feature"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetContainer(java.lang.String)
     */
    public FilterableMap objGetContainer(
        String feature
    ) throws ServiceException {
        if(SystemAttributes.CONTEXT_CAPABLE_CONTEXT.equals(feature)) return CONTEXT_FREE;
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            getExceptionParameters(
                new BasicException.Parameter[]{
                    new BasicException.Parameter("feature",feature)
                }
            ),
            "This object has no such feature"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objInvokeOperation(java.lang.String, org.openmdx.base.accessor.generic.cci.Structure_1_0)
     */
    public Structure_1_0 objInvokeOperation(
        String operation,
        Structure_1_0 arguments
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            getExceptionParameters(
                new BasicException.Parameter[]{
                    new BasicException.Parameter("operation",operation)
                }
            ),
            "Operation not supported for this object/in this context"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objInvokeOperationInUnitOfWork(java.lang.String, org.openmdx.base.accessor.generic.cci.Structure_1_0)
     */
    public Structure_1_0 objInvokeOperationInUnitOfWork(
        String operation,
        Structure_1_0 arguments
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            getExceptionParameters(
                new BasicException.Parameter[]{
                    new BasicException.Parameter("operation",operation)
                }
            ),
            "Operation not supported for this object/in this context"
        );
    }

    private void verifyListenerArguments(
        String feature,
        EventListener listener
    ) throws ServiceException {
        verifyListenerArguments(
            feature,
            listener == null ? null : listener.getClass()
        );
    }

    private void verifyListenerArguments(
        String feature,
        Class listenerType
    ) throws ServiceException {
        if(listenerType == null) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            null,
            "The listener argument must not be null"
        );
        if(InstanceCallbackListener.class.isAssignableFrom(listenerType)){
            if(feature != null) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                getExceptionParameters(
                    new BasicException.Parameter[]{
                        new BasicException.Parameter(
                            "feature", 
                            feature
                        ), 
                        new BasicException.Parameter(
                            "listenerType", 
                            listenerType.getName()
                        )
                    }
                ),
                "Instance level events must not be associated with a feature"
            );
        } else if (
            PropertyChangeListener.class.isAssignableFrom(listenerType) ||
            VetoableChangeListener.class.isAssignableFrom(listenerType)
        ){
            // Feature scope o.k.
        } else throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            getExceptionParameters(
                new BasicException.Parameter[]{
                    new BasicException.Parameter(
                        "listenerType", 
                        listenerType.getName()
                    ), 
                    new BasicException.Parameter(
                        "supported", 
                        new String[]{
                            InstanceCallbackListener.class.getName(),
                            PropertyChangeListener.class.getName(),
                            VetoableChangeListener.class.getName()
                        }
                    ), 
                }
            ),
            "Unsupported listener class"
        );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objAddEventListener(java.lang.String, java.util.EventListener)
     * 
     * Add an event listener.
     * 
     * @param feature
     *        restrict the listener to this feature;
     *        or null if the listener is interested in all features
     * @param listener
     *        the event listener to be added
     * <p>
     * It is implementation dependent whether the feature name is verified or 
     * not.
     * 
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature or if a non-null
     *              feature name is specified for an instance level event
     * @exception   ServiceException NOT_SUPPORTED
     *              if the listener's class is not supported
     * @exception   ServiceException TOO_MANY_EVENT_LISTENERS
     *              if an attempt is made to register more than one 
     *              listener for a unicast event.
     * @exception   ServiceException BAD_PARAMETER
     *              If the listener is null 
     */
    public void objAddEventListener(
        String feature,
        EventListener listener
    ) throws ServiceException {
        verifyListenerArguments(feature,listener);
        if(feature == null){
            this.listeners.put(listener, null);
        } else {
            Set features = (Set)this.listeners.get(listener);
            if(features == null){
                if (this.listeners.containsKey(listener)) return;
                this.listeners.put(
                    listener, 
                    features = new HashSet()
                );
            }
            features.add(feature);
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRemoveEventListener(java.lang.String, java.util.EventListener)
     * 
     * Remove an event listener.
     * <p>
     * It is implementation dependent whether feature name and listener
     * class are verified. 
     * 
     * @param feature
     *        the name of the feature that was listened on,
     *        or null if the listener is interested in all features
     * @param listener
     *        the event listener to be removed
     * 
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature or if a non-null
     *              feature name is specified for an instance level event
     * @exception   ServiceException NOT_SUPPORTED
     *              if the listener's class is not supported
     * @exception   ServiceException BAD_PARAMETER
     *              If the listener is null 
     */
    public void objRemoveEventListener(
        String feature,
        EventListener listener
    ) throws ServiceException {
        verifyListenerArguments(feature,listener);
        if(feature == null){
            this.listeners.remove(listener);
        } else {
            Set features = (Set)this.listeners.get(listener);
            if(features != null)features.remove(feature);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetEventListeners(java.lang.String, java.lang.Class)
     * 
     * Get event listeners.
     * <p>
     * The <code>feature</code> argument is ignored for listeners registered 
     * with a <code>null</code> feature argument.
     * <p>
     * It is implementation dependent whether feature name and listener
     * type are verified. 
     * 
     * @param feature
     *        the name of the feature that was listened on,
     *        or null for listeners interested in all features
     * @param listenerType
     *        the type of the event listeners to be returned
     * 
     * @return an array of listenerType containing the matching event
     *         listeners
     * 
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature or if a non-null
     *              feature name is specified for an instance level event
     * @exception   ServiceException BAD_PARAMETER
     *              If the listener's type is not a subtype of EventListener 
     * @exception   ServiceException NOT_SUPPORTED
     *              if the listener type is not supported
     */
    public EventListener[] objGetEventListeners(
        String feature,
        Class listenerType
    ) throws ServiceException {
        verifyListenerArguments(feature,listenerType);
        List matchingListeners = new ArrayList();
        for(
            Iterator i = this.listeners.entrySet().iterator();
            i.hasNext();
        ){
            Map.Entry e = (Map.Entry) i.next();
            EventListener l = (EventListener) e.getKey();
            Set f = (Set) e.getValue();
            if(
                listenerType.isInstance(l) && (
                    f == null || f.contains(feature)
                )
            ) matchingListeners.add(l);
        }
        return (EventListener[]) matchingListeners.toArray(
            (EventListener[])Array.newInstance(
                listenerType, 
                matchingListeners.size()
            )
        );
    }

    /**
     * Fire an instance callback
     *  
     * @param type
     * @throws ServiceException
     */
    protected void objFireInstanceCallbackEvent (
        short type
    ) throws ServiceException {
        InstanceCallbackListener[] listeners = (InstanceCallbackListener[]) objGetEventListeners(
            null,
            InstanceCallbackListener.class
        );
        if(listeners.length != 0) {
            InstanceCallbackEvent event = new InstanceCallbackEvent(
                type,
                this,
                null
            );
            for(InstanceCallbackListener listener : listeners) {
                switch (type) {
                    case InstanceCallbackEvent.POST_LOAD :
                    case InstanceCallbackEvent.POST_RELOAD :
                        listener.postLoad(event);
                        break;
                    case InstanceCallbackEvent.PRE_CLEAR :
                        listener.preClear(event);
                        break;
                    case InstanceCallbackEvent.PRE_DELETE :
                        listener.preDelete(event);
                        break;
                    case InstanceCallbackEvent.PRE_STORE :
                        listener.preStore(event);
                        break;
                    case InstanceCallbackEvent.POST_CREATE :
                        listener.postCreate(event);
                        break;
                }
            }
        }
    }
    

    //------------------------------------------------------------------------
    // Implements java.beans.PropertyChangeListener
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        try {
            this.objAddToUnitOfWork();
            if(event.getPropertyName() != null) this.dirty.add(event.getPropertyName());
            PropertyChangeListener[] listeners = (PropertyChangeListener[]) objGetEventListeners(
                event.getPropertyName(),
                PropertyChangeListener.class
            );
            if(listeners.length == 0) return;
            for(
                int i = 0;
                i < listeners.length;
                i++
            ) listeners[i].propertyChange(event);
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }


    //------------------------------------------------------------------------
    // Implements java.beans.VetoableChangeListener
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.beans.VetoableChangeListener#vetoableChange(java.beans.PropertyChangeEvent)
     */
    public void vetoableChange(PropertyChangeEvent event) throws PropertyVetoException {
        try {
            VetoableChangeListener[] listeners = (VetoableChangeListener[]) objGetEventListeners(
                event.getPropertyName(),
                VetoableChangeListener.class
            );
            if(listeners.length == 0) return;
            for(
                int i = 0;
                i < listeners.length;
                i++
            ) listeners[i].vetoableChange(event);
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------
    protected static String stateToString(
    	Object_1_0 source
    ){
        try {
            Set state = new HashSet();
			if(source.objIsDeleted()) state.add("deleted");
	        if(source.objIsDirty()) state.add("dirty");
	        if(source.objIsInUnitOfWork()) state.add("inUnitOfWork");
	        if(source.objIsNew()) state.add("new");
	        if(source.objIsPersistent()) state.add("persistent");
	        return state.toString();
		} catch (ServiceException e) {
			return '(' + e.getMessage() + ')';
		}
    }

    protected static Object defaultFetchGroupToString(
    	Object_1_0 source
    ){
    	try {
			return source.objDefaultFetchGroup().toString();
		} catch (ServiceException e) {
			return '(' + e.getMessage() + ')';
		}
    }
    
    /**
     * Create a String representation of an Object_1_0 instance
     * 
     * @param source
     * @param objectClass
     * @param description
     * 
     * @return
     */
    public static String toString(
    	Object_1_0 source,
		String objectClass, 
		String description
    ) {
    	return source.getClass().getName() + ": " + (
    		description == null ? "" : '(' + description + ')'
    	) +	IndentingFormatter.toString(
    		ArraysExtension.asMap(
				TO_STRING_KEYES,
				new Object[]{
					source.objGetResourceIdentifier(),	
					objectClass,
					stateToString(source),
					defaultFetchGroupToString(source)
				}
    		)
		);
    }

    private static final String[] TO_STRING_KEYES = new String[]{
    	"resourceIdentifier",
		"class",
		"state",
		"defaultFetchGroup"
    };
								
    public String toString(
    ){
        return toString(this,this.objGetClass(), null);
    }

    
    //------------------------------------------------------------------------
    // Class ContextFree
    //------------------------------------------------------------------------

    final static FilterableMap CONTEXT_FREE = new ContextFree();
    
    /* (non-Javadoc)
     */
    static class ContextFree 
        extends AbstractMap 
        implements FilterableMap, Serializable 
    {

        /**
         * 
         */
        private static final long serialVersionUID = 3258129141898753592L;

        /* (non-Javadoc)
         * @see java.util.AbstractMap#entrySet()
         */
        public Set entrySet() {
            return Collections.EMPTY_SET;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.collection.FilterableMap#subMap(java.lang.Object)
         */
        public FilterableMap subMap(Object filter) {
            return this;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.collection.FilterableMap#values(java.lang.Object)
         */
        public List values(Object criteria) {
            return Collections.EMPTY_LIST;
        }
    } 
    
}
