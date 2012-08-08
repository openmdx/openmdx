/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: SinkObject_1.java,v 1.32 2008/02/05 18:42:57 hburger Exp $
 * Description: Date State View Object
 * Revision:    $Revision: 1.32 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/05 18:42:57 $
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
 * listed in the NOTICE file.
 */
package org.openmdx.base.accessor.generic.spi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.resource.ResourceException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_2;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.resource.spi.OrderedRecordFactory;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.layer.model.State_1_Attributes;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.StringBuilders;
import org.slf4j.LoggerFactory;

/**
 * Date State View Object
 */
class SinkObject_1 {

    private SinkObject_1(
        SinkConnection_1 connection,
        Path path,
        Object_1_0 delegate, 
        int hashCode
    ){
        this.connection = connection;
        this.path = path;
        this.delegate = delegate;
        this.hashCode = hashCode;
        //
        // Validate the path
        //
//      for(
//          int i = 0, iLimit = path.size();
//          i < iLimit;
//          i++
//      ){
//          if(path.get(i).indexOf(';') >= 0) {
//              System.err.println("Invalid path for SinkObject_1: " + path);
//          }
//      }
    }
    
    /**
     * Constructor
     *
     * @param connection
     * @param path
     * @param objectClass
     *
     * @throws ServiceException
     */
    SinkObject_1(
        SinkConnection_1 connection,
        Path path,
        String objectClass
    ) throws ServiceException {
        this(
            connection,
            objectClass,
            path,
            null, // delegate
            path.hashCode()
        );
    }

    /**
     * Constructor
     *
     * @param connection
     * @param path
     * @param initializeCacheWithDelegate 
     * @param objectClass
     * 
     * @throws ServiceException
     */
    SinkObject_1(
        SinkConnection_1 connection,
        Path path,
        boolean dateStateInstance, 
        boolean initializeCacheWithDelegate
    ) throws ServiceException {
        this(
            connection,
            path,
            null, // delegate
            path.hashCode()
        );
        this.instanceOfDateState = Boolean.valueOf(dateStateInstance);
        if(initializeCacheWithDelegate) {
            try {
                Object_1_0 delegate = connection.getDelegate().getObject(path);
                this.objectClass = delegate.objGetClass();
                this.delegate = delegate;
                if(dateStateInstance) {
                    this.states = new ArrayList();
                    this.states.add(this.delegate);
                }
            } catch (Exception ignore) {
                // Do not cache inaccessible object
            }
        }
    }
    
    /**
     * Constructor
     *
     * @param connection
     * @param delegate
     * @param hashCode 
     * 
     * @throws ServiceException
     */
    SinkObject_1(
        SinkConnection_1 connection,
        Object_1_0 delegate, 
        int hashCode
    ) throws ServiceException {
        this(
            connection,
            lenientGetObjectClass(delegate),
            delegate.objGetPath(),
            delegate, 
            hashCode
        );
    }

    private SinkObject_1(
        SinkConnection_1 connection,
        String objectClass,
        Path path,
        Object_1_0 delegate, 
        int hashCode
    ) throws ServiceException {
        this(
            connection,
            path,
            delegate, 
            hashCode
        );
        setObjectClass(objectClass);
        this.states = null;
    }

    private final int hashCode;
    
    private Boolean instanceOfDateState;

    private final SinkConnection_1 connection;

    private Object_1_0 delegate;

    private Path path;

    private String objectClass;

    private List states;

    private Map containers = null;

    String getObjectClass(
    ){
        return this.objectClass;
    }

    void setObjectClass(
        String objectClass
    ) throws ServiceException {
        this.objectClass = objectClass;
        this.instanceOfDateState = objectClass == null ? null : Boolean.valueOf(
            this.connection.isAssigneableToDateState(objectClass)
        );
    }

    boolean isHollow(
    ){
        return this.instanceOfDateState == null;
    }

    boolean isInstanceOfDateState(
    ){
        return this.instanceOfDateState.booleanValue();
    }

    boolean isDirty(
    ) throws ServiceException{
        if(Boolean.FALSE.equals(this.instanceOfDateState)) {
            return getDelegate().objIsDirty();
       } else if(
            Boolean.TRUE.equals(this.instanceOfDateState) &&
            hasStateCache()
       ){
            for(
                Iterator i = allStates(Boolean.FALSE, null).iterator();
                i.hasNext();
            ){
                Object_1_0 state = (Object_1_0) i.next();
                if(state.objIsDirty()) {
                    return true;
                }
            }
        }
        return false;
    }

    void validateDelegate(
    ) throws ServiceException{
        if(
            this.path != null &&
            isInaccessable(this.delegate)
        ) {
            this.delegate = null;
        }
    }

    void setDelegate(
        Object_1_0 delegate
    ){
        this.delegate = delegate;
    }

    boolean hasStateCache(
    ){
        return this.states != null;
    }

    public String toString(
    ){
        try {
            return StringBuilders.newStringBuilder(
                getClass().getName()
            ).append(
                ": "
            ).append(
                OrderedRecordFactory.getInstance().asMappedRecord(
                    getObjectClass(),
                    isHollow() ?
                        "hollow" :
                        isInstanceOfDateState() ?
                            (hasStateCache() ? Integer.toString(this.states.size()) : "no") + " states in cache" :
                            "non-stated",
                    new String[]{"refMofId"},
                    new String[]{this.path == null ? "n/a" : this.path.toXri()}
                )
            ).toString();
        } catch (ResourceException exception) {
            LoggerFactory.getLogger(
                SinkObject_1.class
            ).error(
                "Farmatting failed",
                exception
            );
            return super.toString();
        }
    }

    void afterCompletion(
        boolean committed
    ){
        this.delegate = null;
        this.states = null;
        this.containers = null;
    }

    Path objGetPath() throws ServiceException{
        return this.path;
    }

    private Object_1_0 getParentDelegate(
    ) throws ServiceException {
        Path path = this.path.getPrefix(this.path.size() - 2);
        SinkObject_1 parent = this.connection.getObject(path);
        if(parent == null) {
            parent = this.connection.getObject(
                this.connection.getDelegate().getObject(path)
            );
        }
        return parent.objIsNew() ?
            null :
            parent.getDelegate();
    }

    private List getStates(
    ) throws ServiceException{
        if(Boolean.FALSE.equals(this.instanceOfDateState)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("class", this.objectClass)
                },
                "The class is not an instance of org::openmdx::compatibility::state1::DateState"
            );
        } else if(this.states == null) {
            List states = new ArrayList();
            this.instanceOfDateState = Boolean.TRUE;
            Object_1_0 parent = getParentDelegate();
            if(parent != null && parent.objIsPersistent()) {
                FilterableMap container = parent.objGetContainer(
                    this.path.get(this.path.size() - 2) // reference name
                ).subMap(
                    new FilterProperty[]{
                        new FilterProperty(
                            Quantors.THERE_EXISTS,
                            State_1_Attributes.STATED_OBJECT,
                            FilterOperators.IS_IN,
                            new Object[]{this.path}
                        )
                    }
                );
                for(
                    Iterator i = container.values().iterator();
                    i.hasNext();
                ){
                    states.add(i.next());
                }
            }
            this.states = states;
        }
        return this.states;
    }

    Object_1_0 getDelegate(
        XMLGregorianCalendar validFor,
        Date validAt,
        boolean forStateQuery
    ) throws ServiceException {
        String viewValidFor = toBasicFormat(validFor);
        List states = getStates();
        String viewValidAt = validAt == null ?
            null :
            DateFormat.getInstance().format(validAt);
        for(
            Iterator i = states.iterator();
            i.hasNext();
        ){
            Object_1_0 state = (Object_1_0) i.next();
            if(
                (forStateQuery || !state.objIsDeleted()) &&
                isValid(state, viewValidAt) &&
                isInRange(state, viewValidFor)
            ) {
               return state;
            }
        }
        return null;
    }

    static String toCriteria (
        String criteria,
        String qualifier,
        String validFrom,
        String validTo
    ){
        boolean basedOnPersistentState =
            STATES_COMPLETED_BY_SERVICE &&
            criteria != null &&
            criteria.substring(qualifier.length()).startsWith(';' + State_1_Attributes.OP_STATE);
        return StringBuilders.newStringBuilder(
            basedOnPersistentState ? criteria : qualifier
        ).append(
            ';'
        ).append(
            State_1_Attributes.OP_VALID_FROM
        ).append(
            '='
        ).append(
            validFrom == null ? State_1_Attributes.OP_VAL_EVER : validFrom
        ).append(
            ';'
        ).append(
            State_1_Attributes.OP_VALID_TO
        ).append(
            '='
        ).append(
            validTo == null ? State_1_Attributes.OP_VAL_EVER : validTo
        ).toString(
        );
    }


    void cloneDelegate(
        Object_1_0 source,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ) throws ServiceException {
        List states = getStates();
        String viewValidFrom = toBasicFormat(validFrom);
        String viewValidTo = toBasicFormat(validTo);
        for(
            int i = 0, iLimit = states.size();
            i < iLimit;
            i++
        ){
            Object_1_0 state = (Object_1_0) states.get(i);
            if(
                isValid(state, null) &&
                isInvolved(state, viewValidFrom, viewValidTo)
            ) {
                splitState(state, validFrom, viewValidFrom, validTo, viewValidTo, null);
                states.set(i, new InactiveObject_1(state, false));
            }
        }
        clone(
            source,
            viewValidFrom,
            viewValidTo, 
            true // newObject
        );
    }

    List getDelegates(
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        boolean forQuery, 
        boolean forRemoval, 
        boolean forRetrieval
    ) throws ServiceException{
        List states = getStates();
        List delegates = new ArrayList();
        String viewValidFrom = toBasicFormat(validFrom);
        String viewValidTo = toBasicFormat(validTo);
        //
        // 'states' is modified, do not use an iterator!
        //
        for(
            int i = 0, iLimit = states.size();
            i < iLimit;
            i++
        ){
            Object_1_0 state = (Object_1_0) states.get(i);
            if(
                (forQuery || !state.objIsDeleted()) &&
                isValid(state, null) &&
                isInvolved(state, viewValidFrom, viewValidTo)
            ) {
                if (forQuery || forRetrieval) {
                    delegates.add(state);
                } else if(splitState(state, validFrom, viewValidFrom, validTo, viewValidTo, delegates)){
                    states.set(i, new InactiveObject_1(state, false));
                } else if (state.objIsDirty()) {
                    delegates.add(state);
                } else {
                    delegates.add(
                        clone(
                            state,
                            (String)state.objGetValue(State_1_Attributes.STATE_VALID_FROM),
                            (String)state.objGetValue(State_1_Attributes.STATE_VALID_TO), 
                            false // newObject
                        )
                    );
                    states.set(i, new InactiveObject_1(state, false));
                }
            }
        }
        Collections.sort(delegates, VALID_FROM_COMPARATOR);
        return delegates;
    }
    
    /**
     * Test whether an object is involved in a given context
     * 
     * @param validFrom
     * @param validTo
     * @param defaultValue the default value in case the object is not cached
     * 
     * @return <code>true</code> if the object is involved in a given context
     * 
     * @throws ServiceException
     */
    boolean isInvolved(
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo, 
        boolean defaultValue
    ) throws ServiceException {
        if(hasStateCache()) {
            String viewValidFrom = toBasicFormat(validFrom);
            String viewValidTo = toBasicFormat(validTo);
            for(
                int i = 0, iLimit = this.states.size();
                i < iLimit;
                i++
            ){
                Object_1_0 state = (Object_1_0) this.states.get(i);
                if(
                    isValid(state, null) &&
                    isInvolved(state, viewValidFrom, viewValidTo)
                ) {
                    return true;
                }
            }
            return false;
        } else {
            return defaultValue;
        }
    }

    /**
     * Test whether an object is involved in a given context
     * 
     * @param validFor
     * @param validAt
     * @param defaultValue the default value in case the object is not cached
     * 
     * @return <code>true</code> if the object is involved in a given context
     * 
     * @throws ServiceException
     */
    boolean isInvolved(
        XMLGregorianCalendar validFor,
        Date validAt, 
        boolean defaultValue
    ) throws ServiceException {
        if(hasStateCache()) {
            String viewValidFor = toBasicFormat(validFor);
            String viewValidAt = validAt == null ? null : DateFormat.getInstance().format(validAt);
            for(
                int i = 0, iLimit = this.states.size();
                i < iLimit;
                i++
            ){
                Object_1_0 state = (Object_1_0) this.states.get(i);
                if(
                    isValid(state, viewValidAt) &&
                    isInvolved(state, viewValidFor, viewValidFor)
                ) {
                    return true;
                }
            }
            return false;
        } else {
            return defaultValue;
        }
    }
    
    Object_1_0 getDelegate() throws ServiceException{
        if(this.path != null && this.delegate == null) {
            this.delegate = connection.getDelegate().getObject(this.path);
        }
        return this.delegate;
    }

    Object_1_0 getQualifiedDelegate(
    ) throws ServiceException {
        if(Boolean.TRUE.equals(this.instanceOfDateState)) {
            List states = getStates();
            return states.isEmpty() ? getDelegate() : (Object_1_0)states.get(0);
        } else {
            return getDelegate();
        }
    }

    FilterableMap objGetContainer(
        String feature
    ) throws ServiceException{
        Object_1_0 qualifiedDelegate = getQualifiedDelegate();
        return (
             qualifiedDelegate.objIsNew() ? qualifiedDelegate : getDelegate()
        ).objGetContainer(feature);
    }

    boolean objIsNew() throws ServiceException{
        return getQualifiedDelegate().objIsNew();
    }
    
    void addState(
        Object_1_0 state
    ) throws ServiceException{
        String stateValidFrom = (String) state.objGetValue(
            State_1_Attributes.STATE_VALID_FROM
        );
        String stateValidTo = (String) state.objGetValue(
            State_1_Attributes.STATE_VALID_TO
        );
        state.objSetValue(
            SystemAttributes.CREATED_AT,
            DateFormat.getInstance().format(
                this.getConnection().getUnitOfWork().getDateTime()
            )
        );
        for(
            Iterator i = allStates(Boolean.FALSE, Boolean.FALSE).iterator();
            i.hasNext();
        ){
            Object_1_0 candidate = (Object_1_0) i.next();
            if(
               isInvolved(
                   candidate,
                   stateValidFrom,
                   stateValidTo
               )
            ) {
               throw new ServiceException(
                   BasicException.Code.DEFAULT_DOMAIN,
                   BasicException.Code.DUPLICATE,
                   new BasicException.Parameter[]{
                       new BasicException.Parameter(
                           "path", this.objGetPath()
                       ),
                       new BasicException.Parameter(
                           "new state's validity",
                           new Object[]{
                               stateValidFrom,
                               stateValidTo
                           }
                       ),
                       new BasicException.Parameter(
                           "conflicting state's validity",
                           new Object[]{
                               candidate.objGetValue(State_1_Attributes.STATE_VALID_FROM),
                               candidate.objGetValue(State_1_Attributes.STATE_VALID_TO)
                           }
                       )
                   },
                   "Overlapping validity"
               );
            }
        }
        if(this.states.isEmpty()) {
            state.objGetPath().setTo(this.path);
        }
        this.states.add(state);
    }

    private void cacheStates(
        List states
    ) throws ServiceException {
        if(
            !hasStateCache() &&
            !states.isEmpty()
        ){
            Object_1_0 object = (Object_1_0) states.get(0);
            if(isHollow()) {
                setObjectClass(
                    object.objGetClass()
                );
            }
            this.states = new ArrayList(states);
        }
    }

    static boolean isInaccessable(
        Object_1_0 object
    ){
        return
            object instanceof Object_1_2 &&
            ((Object_1_2)object).objIsInaccessable();
    }

    void deleteState(
        Object_1_0 state
    ) throws ServiceException{
        int i = this.states == null ? -1 : this.states.indexOf(state);
        if(i < 0) {
            throw new IllegalArgumentException("Foreign object");
        } else {
            states.set(
                i,
                new InactiveObject_1(
                    state,
                    !state.objIsNew()
                )
            );
        }
    }
    
    void deleteState(
        Object_1_0 state,
        String validFrom,
        String validTo
    ) throws ServiceException{
        deleteState(
            clone(state, validFrom, validTo, false)
        );
        invalidateState(state);
    }
    
    void invalidateState(
        Object_1_0 state
    ) throws ServiceException{
        int i = this.states == null ? -1 : this.states.indexOf(state);
        if(i < 0) {
            throw new IllegalArgumentException("Foreign object");
        } else {
            states.set(
                i,
                new InactiveObject_1(
                    state,
                    false
                )
            );
        }
    }
    
    private Object_1_0 clone(
        Object_1_0 source,
        String validFrom,
        String validTo, 
        boolean newObject
    ) throws ServiceException {
        Path path = objGetPath().getParent().add(
            toCriteria(
                source.objGetPath().getBase(),
                objGetPath().getBase(),
                validFrom,
                validTo
             )
        );
        boolean dirty = newObject || (
            STATES_COMPLETED_BY_SERVICE ? source.objIsNew() : source.objIsDirty()           
        );
        Object_1_0 object = this.connection.getDelegate().cloneObject(
            path,
            source,
            dirty // completelyDirty
        );
        object.objSetValue(
            dirty ? SystemAttributes.CREATED_AT : '$' + SystemAttributes.CREATED_AT, 
            DateFormat.getInstance().format(this.getConnection().getUnitOfWork().getDateTime())
        );
        object.objSetValue(
            dirty ? State_1_Attributes.STATE_VALID_FROM : '$' + State_1_Attributes.STATE_VALID_FROM, 
            validFrom
        );
        object.objSetValue(
            dirty ? State_1_Attributes.STATE_VALID_TO : '$' + State_1_Attributes.STATE_VALID_TO, 
            validTo
        );
        if(object.objIsDirty()) {
            object.objAddToUnitOfWork();
        }
        getStates().add(object);
        return object;
    }

    /**
     * Retrieve the sink objects for a given reference
     * 
     * @param feature the reference name
     * @param fetch tells, whether the collection has to be complete
     * 
     * @return the sink objects for the given reference
     * 
     * @throws ServiceException
     */
    Collection getSinkObjects(
        String feature, 
        boolean fetch
    ) throws ServiceException {
        Collection objects = this.containers == null ? 
            null : 
            (Collection) this.containers.get(feature);
        if(objects == null) {
            Path referenceFilter = this.path.getChild(feature);
            if(fetch) {
                if(objIsNew()) {
                    for(
                        Iterator i = objGetContainer(feature).entrySet().iterator();
                        i.hasNext();
                    ){
                        Map.Entry e = (Entry) i.next();
                        Path p = referenceFilter.getChild((String)e.getKey());
                        Object_1_0 s = (Object_1_0) e.getValue();
                        String c = SinkObject_1.lenientGetObjectClass(s);
                        SinkObject_1 object = this.connection.getObject(p);
                        if(object == null) {
                            object = this.connection.getObject(p, c);
                        } else if (c != null && object.isHollow()) {
                            object.setObjectClass(c);
                        }
                        object.cacheStates(
                            Collections.singletonList(e.getValue())
                        );
                    }
                } else {
                    Map stateDelegates = new HashMap();
                    for(
                        Iterator i = objGetContainer(feature).subMap(DATE_STATE_FILTER).entrySet().iterator();
                        i.hasNext();
                    ){
                        Map.Entry e = (Entry) i.next();
                        String q = (String) e.getKey();
                        int s = q.indexOf(";"+ State_1_Attributes.OP_STATE + "=");
                        if(s > 0) {
                            q = q.substring(0, s);
                        }
                        List target = (List) stateDelegates.get(q);
                        if(target == null) {
                            stateDelegates.put(
                                q,
                                target = new ArrayList()
                            );
                        }
                        target.add(e.getValue());
                    }
                    for(
                        Iterator i = stateDelegates.entrySet().iterator();
                        i.hasNext();
                    ){
                        Map.Entry e = (Entry) i.next();
                        Path p = referenceFilter.getChild((String)e.getKey());
                        Object_1_0 s = (Object_1_0) ((List)e.getValue()).get(0);
                        String c = SinkObject_1.lenientGetObjectClass(s);
                        SinkObject_1 object = this.connection.getObject(p);
                        if(object == null) {
                            object = this.connection.getObject(p, c);
                        } else if (c != null && object.isHollow()) {
                            object.setObjectClass(c);
                        }
                        object.cacheStates((List)e.getValue());
                    }
                }
            }
            objects = this.connection.getSinkObjects(referenceFilter);
            if(fetch) {
                if(this.containers == null) {
                    this.containers = new HashMap();
                }
                this.containers.put(
                    feature,
                    objects
                );
            }
        }
        return objects;
    }

    private static boolean isInRange(
        Object_1_0 state,
        String viewValidFor
    ) throws ServiceException {
        String stateValidFrom = (String) state.objGetValue(State_1_Attributes.STATE_VALID_FROM);
        String stateValidTo = (String) state.objGetValue(State_1_Attributes.STATE_VALID_TO);
        return
            compareValidFrom(stateValidFrom, viewValidFor) <= 0 &&
            compareValidTo(stateValidTo, viewValidFor) >= 0;
    }

    private static boolean isInvolved(
        Object_1_0 state,
        String viewValidFrom,
        String viewValidTo
    ) throws ServiceException {
        String stateValidFrom = (String) state.objGetValue(State_1_Attributes.STATE_VALID_FROM);
        String stateValidTo = (String) state.objGetValue(State_1_Attributes.STATE_VALID_TO);
        return (
            stateValidTo == null || viewValidFrom == null || compareValidFrom(stateValidTo, viewValidFrom) >= 0
        ) && (
            stateValidFrom == null || viewValidTo == null || compareValidFrom(stateValidFrom, viewValidTo) <= 0
        );
    }

    private boolean splitState(
        Object_1_0 state,
        XMLGregorianCalendar validFrom,
        String viewValidFrom,
        XMLGregorianCalendar validTo,
        String viewValidTo,
        Collection innerStates
    ) throws ServiceException {
        String stateValidFrom = (String) state.objGetValue(State_1_Attributes.STATE_VALID_FROM);
        String stateValidTo = (String) state.objGetValue(State_1_Attributes.STATE_VALID_TO);
        boolean leftSplit = compareValidFrom(stateValidFrom, viewValidFrom) < 0;
        boolean rightSplit = compareValidTo(stateValidTo, viewValidTo) > 0;
        if(leftSplit && rightSplit) {
            clone(
                state,
                stateValidFrom,
                toBasicFormat(add(validFrom, PREVIOUS_DAY)), 
                false // newObject
            );
            clone(
                state,
                toBasicFormat(add(validTo, NEXT_DAY)),
                stateValidTo, 
                false // newObject
            );
            if(innerStates != null) {
                innerStates.add(
                    clone(
                        state, 
                        viewValidFrom, 
                        viewValidTo, 
                        true // newObject
                    )
                );
            }
        } else {
            if(leftSplit) {
                clone(
                    state,
                    stateValidFrom,
                    toBasicFormat(add(validFrom, PREVIOUS_DAY)), 
                    false // newObject
                );
                if(innerStates != null) {
                    innerStates.add(
                        clone(state, viewValidFrom, stateValidTo, 
                            true // newObject
                        )
                    );
                }
            }
            if (rightSplit) {
                clone(
                    state,
                    toBasicFormat(add(validTo, NEXT_DAY)),
                    stateValidTo, false
                );
                if(innerStates != null) {
                    innerStates.add(
                        clone(
                            state,
                            stateValidFrom,
                            viewValidTo, 
                            true // newObject
                        )
                    );
                }
            }
        }
        return leftSplit || rightSplit;
    }

    static boolean isValid(
        Object_1_0 state,
        String viewValidAt
    ) throws ServiceException{
        String stateInvalidatedAt = (String) state.objGetValue(State_1_Attributes.INVALIDATED_AT);
        if(viewValidAt == null) {
            return stateInvalidatedAt == null;
        } else {
            String stateCreatedAt = (String) state.objGetValue(SystemAttributes.CREATED_AT);
            return stateCreatedAt.compareTo(viewValidAt) <= 0 && (
                stateInvalidatedAt == null || stateInvalidatedAt.compareTo(viewValidAt) > 0
            );
        }
    }

    /**
     * Retrieve an object's states
     * 
     * @param invalidated tells whether one looks for valid or invalid states 
     * @param deleted tells whether one non-deleted or deleted states
     * 
     * @return all states of an object matching the given criteria
     * 
     * @throws ServiceException
     */
    Collection allStates(
        Boolean invalidated, 
        Boolean deleted
    ) throws ServiceException{
        if(invalidated == null & deleted == null) {
            return getStates();
        } else {
            Collection target = new ArrayList();
            for(
                Iterator i = getStates().iterator();
                i.hasNext();
            ){
                Object_1_0 state = (Object_1_0) i.next();
                boolean invalidatedMatches = invalidated == null || (
                    state.objGetValue(State_1_Attributes.INVALIDATED_AT) != null
                ) == invalidated.booleanValue();
                boolean deletedMatches = deleted == null || 
                    deleted.booleanValue() == state.objIsDeleted();
                if(invalidatedMatches & deletedMatches) {
                  target.add(state);
               }
            }
            return target;
        }
    }

    void invalidate() throws ServiceException{
        this.connection.invalidate(this);
    }

    SinkConnection_1 getConnection(
    ){
        return this.connection;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode(
    ){
        return this.hashCode;
    }

    /**
     * Compare two XMLGregorianCalendar values where <code>null</code> is
     * considered to be smaller than every other value.
     *
     * @param d1
     * @param d2
     *
     * @return a negative integer, zero, or a positive integer as d1 is less
     * than, equal to, or greater than d2.
     */
    static int compareValidFrom(
        String d1,
        String d2
    ){
        return d1 == null ? (
            d2 == null ? 0 : -1
        ) : (
            d2 == null ? 1 : d1.compareTo(d2)
        );
    }

    /**
     * Compare two XMLGregorianCalendar values where <code>null</code> is
     * considered to be greater than every other value.
     *
     * @param d1
     * @param d2
     *
     * @return a negative integer, zero, or a positive integer as d1 is less
     * than, equal to, or greater than d2.
     */
    static int compareValidTo(
        String d1,
        String d2
    ){
        return d1 == null ? (
            d2 == null ? 0 : 1
        ) : (
            d2 == null ? -1 : d1.compareTo(d2)
        );
    }

    static String lenientGetObjectClass(
        Object_1_0 object
    ) throws ServiceException{
        return object == null ? 
            null : 
            (String) object.objGetValue('$' + SystemAttributes.OBJECT_CLASS);
    }

    void lenientSetObjectClass(
        Object_1_0 object
    ) throws ServiceException{
        if(this.objectClass == null) {
            this.objectClass = lenientGetObjectClass(object);
        }
    }

    static void lenientSetObjectClass(
        Object_1_0 object,
        String objectClass
    ) throws ServiceException{
        object.objSetValue('$' + SystemAttributes.OBJECT_CLASS, objectClass);
    }

    static String toBasicFormat(
        XMLGregorianCalendar value
    ){
        if(value == null) {
            return null;
        } else {
            int year = value.getYear();
            int month = value.getMonth();
            int day = value.getDay();
            return StringBuilders.newStringBuilder(
            ).append(
                year
            ).append(
                month < 10 ? "0" : ""
            ).append(
                month
            ).append(
                day < 10 ? "0" : ""
            ).append(
                day
            ).toString();
        }
    }

    /**
     * Tests whether two states are adjacent
     *
     * @param earlier
     * @param later
     *
     * @return <code>true</code> if two states are adjacent
     */
    private static XMLGregorianCalendar add(
        XMLGregorianCalendar date,
        Duration delta
    ){
        XMLGregorianCalendar sum = (XMLGregorianCalendar) date.clone();
        sum.add(delta);
        return sum;
    }

    /**
     * Retrieve the Datatype Factory
     *
     * @return the datatype factory to be used
     */
    private static DatatypeFactory newDatatypeFactory(
    ){
        try {
            return DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException exception) {
            LoggerFactory.getLogger(
                SinkObject_1.class
            ).error(
                "DatatypeFactory acquisition failed, " +
                "DateStateViews will not be able to provide current date",
                exception
            );
            return null;
        }
    }

    /**
     * The DatatypeFactory instance
     */
    static private final DatatypeFactory datatypeFactory = newDatatypeFactory();

    private static final Duration NEXT_DAY = datatypeFactory == null ? null :
        datatypeFactory.newDurationDayTime(true, 1, 0, 0, 0);

    private static final Duration PREVIOUS_DAY = NEXT_DAY.negate();

    /**
     * Defines whether the unmodified attributes are added by the client or by the service.
     */
    private static final boolean STATES_COMPLETED_BY_SERVICE = false;

    /**
     * Tells the service to return states instead of objects
     */
    private static final FilterProperty[] DATE_STATE_FILTER = new FilterProperty[] {
        new FilterProperty(
            Quantors.THERE_EXISTS,
            State_1_Attributes.STATED_OBJECT,
            FilterOperators.IS_NOT_IN,
            new Object[]{}
        )
    };

    /**
     * To sort the states
     */
    private static final Comparator VALID_FROM_COMPARATOR = new ValidFromComparator();

    
    //------------------------------------------------------------------------
    // Class ValidFromComparator
    //------------------------------------------------------------------------
    
    /**
     * Date State Valid From Comparator 
     */
    private final static class ValidFromComparator
        implements Comparator, Serializable
    {

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = -6816519954526341332L;
                 
        /**
         * Implements <code>Comparable</code>
         */
        public int compare(Object o1, Object o2) {
            try {
                return compareValidFrom(
                    (String)((Object_1_0) o1).objGetValue(State_1_Attributes.STATE_VALID_FROM),
                    (String)((Object_1_0) o2).objGetValue(State_1_Attributes.STATE_VALID_FROM)
                );
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj) {
            return obj instanceof ValidFromComparator;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return ValidFromComparator.class.hashCode();
        }
        
    }

}
