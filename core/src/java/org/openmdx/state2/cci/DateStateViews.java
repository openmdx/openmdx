/*
 * ====================================================================
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/16 09:57:22 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2008, OMEX AG, Switzerland
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
package org.openmdx.state2.cci;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefBaseObject;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.resource.cci.InteractionSpec;
import javax.xml.datatype.XMLGregorianCalendar;

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.accessor.spi.ExceptionHelper;
import org.openmdx.base.accessor.spi.PersistenceManager_1_0;
import org.openmdx.base.cci2.AspectCapable;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.base.query.AnyTypeCondition;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.state2.jmi1.BasicState;
import org.openmdx.state2.jmi1.DateState;
import org.openmdx.state2.jmi1.StateCapable;
import org.openmdx.state2.spi.DateStateContexts;
import org.openmdx.state2.spi.DateStateViewContext;
import org.openmdx.state2.spi.Order;
import org.w3c.cci2.AnyTypePredicate;
import org.w3c.cci2.Container;
import org.w3c.cci2.ImmutableDatatype;
import org.w3c.format.DateTimeFormat;
import org.w3c.spi.DatatypeFactories;
import org.w3c.spi.ImmutableDatatypeFactory;

/**
 * Date State Views
 */
public class DateStateViews {

    /**
     * Constructor 
     */
    private DateStateViews(
    ) {
        // Avoid instantiation 
    }

    private static RefPackage_1_0 getPackageForContext(
        RefBaseObject refContext,
        DateStateViewContext context
    ){
        RefPackage_1_0 refPackageFactory = (RefPackage_1_0)refContext.refOutermostPackage(); 
        return refPackageFactory.refPackage(context);
    }

    /**
     * Create a core object in the given context
     * 
     * @param context
     * @param coreClass
     * 
     * @return a new core object
     */
    public static <T extends StateCapable> T createCore(
        RefBaseObject context,
        Class<T> coreClass
    ){
        return getPackageForContext(context, null).refPersistenceManager().newInstance(coreClass);
    }
    
    /**
     * TODO make state2 compliant
     */
    private static boolean amend(
        Filter original
    ){
        for(Condition condition : original.getCondition()) {
            String feature = condition.getFeature();
            if (
                SystemAttributes.CREATED_AT.equals(feature) ||
                SystemAttributes.REMOVED_AT.equals(feature) ||
                "stateValidFrom".equals(feature) ||
                "stateValidTo".equals(feature) 
            ){
                return false;
            }
        }
        return true;
    }

    /**
     * Add a core object to the date state context free  view of the given container
     * 
     * @param container
     * @param qualifier
     * @param core
     */
    public static <T extends org.openmdx.state2.cci2.StateCapable>  void addCoreToContainer(
        Container<T> container,
        String qualifier,
        T core
    ){
        RefContainer to = (RefContainer) container;
        DateStateContext context = getContext((org.openmdx.state2.jmi1.StateCapable)core); 
        if(context != null) {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "The core object must be context free",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("context", context)
                    )
                )
            );
        }
        if(getContext(to) != null) {
            TransientContainerId toId = PersistenceHelper.getTransientContainerId(to);
            to = (RefContainer) getPackageForContext(
                to,
                null
            ).refObject(
                toId.getParent()
             ).refGetValue(
                 toId.getFeature()
             );
        }
        to.refAdd(
            QualifierType.REASSIGNABLE,
            qualifier,
            core
        );
    }

    
    /**
     * 
     * @param container
     * @param predicate
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <E extends DateState,C extends Container<E>> List<E> getStates(
        C container,
        AnyTypePredicate predicate
    ){
        RefBaseObject collection = (RefBaseObject) container;
        TransientContainerId collectionId = PersistenceHelper.getTransientContainerId(collection);
        C stateContainer = (C)getPackageForContext(
            collection,
            null
        ).refObject(
            collectionId.getParent()
        ).refGetValue(
            collectionId.getFeature()
        );
        AnyTypePredicate statePredicate = predicate;
        if(predicate != null) {
            Filter filter = ((RefQuery_1_0)predicate).refGetFilter();
            if(amend(filter)) {
                FilterProperty[] amendment = getAmendment(getContext(collection));
                if(amendment != null) {
                    Filter newFilter = new Filter(
                        filter.getCondition(),
                        filter.getOrderSpecifier()
                    );
                    for(FilterProperty p: amendment) {
                        newFilter.addCondition(
                            new AnyTypeCondition(p)
                        );
                    }
                    statePredicate = newFilter;
                }
            }
        }
        return new StateList<E>(
            stateContainer.getAll(statePredicate)
        );
    }
            
    private static RefObject refObjectById(
        RefPackage_1_0 refPackage,
        UUID transientObjectId
    ){
        return transientObjectId == null ? null : refPackage.refObject(transientObjectId);
    }

    public static <T extends DateState> T getViewForState(
        T state
    ){
        if(state == null) {
            return null;
        } else if(JDOHelper.isDeleted(state)) {
            return state;
        } else if(state.getRemovedAt() == null) {
            return getViewForTimeRange(
                state,
                state.getStateValidFrom(),
                state.getStateValidTo()
            );
        } else {
            Date createdAt = state.getCreatedAt(); 
            XMLGregorianCalendar validFor = state.getStateValidFrom();
            if(validFor == null) {
                validFor = state.getStateValidTo();                
            }
            if(validFor == null) {
                try {
                    String dateTime = DateTimeFormat.BASIC_UTC_FORMAT.format(createdAt);
                    validFor = DateStateContexts.fromBasicFormat(
                        dateTime.substring(0, dateTime.indexOf('T'))
                    );
                } catch (Exception exception) {
                    validFor = today();
                }                
            }
            return getView(
                state,
                validFor,
                createdAt
            );
        }
    }
    
    //------------------------------------------------------------------------
    // Time Point Views 
    //------------------------------------------------------------------------
    
    private static RefPackage_1_0 getPackageForTimePoint(
        RefBaseObject refContext,
        XMLGregorianCalendar validFor,
        Date validAt
    ){
        return getPackageForContext(
            refContext,
            DateStateViewContext.newTimePointViewContext(
                validFor == null ? today() : validFor,
                validAt
            )
        );
    }
    
    private static RefObject refViewForTimePoint(
        RefObject object,
        XMLGregorianCalendar validFor,
        Date validAt
    ){
        if(object == null) {
            return null;
        }
        DateStateContext context = getContext(object);
        RefObject refObject = object;
        if(context == null) {
            if(object instanceof BasicState) {
                refObject = ((BasicState)object).getCore();
            }
        } else if(
            context.getViewKind() == ViewKind.TIME_POINT_VIEW &&
            equals(validFor, context.getExistsAt()) &&
            equals(validAt, context.getValidAt())
        ){
            return object;
        }
        return object == null ? null : refObjectById( 
            getPackageForTimePoint(
                object,
                validFor,
                validAt
            ),
            (UUID)JDOHelper.getTransactionalObjectId(refObject)
        );
    }

    private static boolean equals(
        Object left,
        Object right
    ){
        if(left == right) {
            return true;
        } else if (left == null || right == null) {
            return false;
        } else if (left.getClass() == right.getClass()) {
            return left.equals(right);
        } else if (left instanceof ImmutableDatatype<?> != right instanceof ImmutableDatatype<?>) {
            ImmutableDatatypeFactory datatypeFactory = DatatypeFactories.immutableDatatypeFactory();
            if(left instanceof XMLGregorianCalendar || right instanceof XMLGregorianCalendar) {
                return datatypeFactory.toDate((XMLGregorianCalendar) left).equals(datatypeFactory.toDate((XMLGregorianCalendar)right));
            } else if (left instanceof Date && right instanceof Date) {
                return datatypeFactory.toDateTime((Date) left).equals(datatypeFactory.toDateTime((Date) right));
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    /**
     * Retrieve a view for a given validity, which is propagated
     * through navigation and applied to <code>DateState</code> instances.
     * <p>
     * Views to DateState instances are read-only, views to other objects
     * are readable and writable.
     * 
     * @param object a plain JMI object or a date view
     * @param validFor exclude all states not valid at the given date,
     * use <code>today()</code> in case of <code>null</code>
     * @param validAt exclude all states created after the given time point
     * 
     * @return a view to the given object
     * 
     * @exception IllegalArgumentException if the <code>object</code> does not support views
     */
    @SuppressWarnings("unchecked")
    public static <T extends RefObject> T getView(
        T object,
        XMLGregorianCalendar validFor,
        Date validAt
    ){
        return (T) refViewForTimePoint(
            object,
            validFor,
            validAt
        );
    }

    /**
     * Retrieve a view for a given validity, which is propagated
     * through navigation and applied to <code>DateState</code> instances.
     * <p>
     * Views to DateState instances are read-only.
     * 
     * @param object a plain JMI object or a date state view
     * @param validFor exclude all states not valid at the given date
     * 
     * @return a view to the given object
     */
    @SuppressWarnings("unchecked")
    public static <T extends RefObject> T getView(
        T object,
        XMLGregorianCalendar validFor
    ){
        return (T) refViewForTimePoint(
            object,
            validFor,
            null // validAt
        );
    }

    /**
     * Retrieve a view valid today, which is propagated
     * through navigation and applied to <code>DateState</code> instances.
     * <p>
     * Views to DateState instances are read-only.
     * 
     * @param refObject a plain JMI object or a date state view
     * 
     * @return a view to the given object
     */
    @SuppressWarnings("unchecked")
    public static <T extends RefObject> T getView(
        T object
    ){
        return (T) refViewForTimePoint(
            object,
            null, // validFor,
            null // validAt
        );
    }
    
    
    //------------------------------------------------------------------------
    // Time Range Views 
    //------------------------------------------------------------------------

    private static RefPackage_1_0 refPackageForTimeRange(
        RefBaseObject refContext,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo        
    ){
        return getPackageForContext(
            refContext,
            DateStateViewContext.newTimeRangeViewContext(
                validFrom,
                validTo
            )
        );
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends RefPackage> T getPackageForTimeRange(
        RefBaseObject refContext,
        Class<T> refPackageClass,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo        
    ){
        return refContext == null ? null : (T) refPackageForTimeRange(
            refContext,
            validFrom,
            validTo
        ).refPackage(
             refPackageClass.getName()
        );
    }
    
    /**
     * Retrieve a view for a given period.
     * 
     * @param referenceCollection the result of the parents getXXX() method
     * @param qualifier the object's qualifier
     * @param validFrom exclude all states not valid at or after the given 
     * date unless validFrom is <code>null</code>
     * @param validTo exclude all states not valid at or before the given 
     * date unless validTo is <code>null</code>
     * 
     * @return a list of DateState instances
     */
    @SuppressWarnings("unchecked")
    public static <T extends RefObject> T getViewForTimeRange(
        Collection<? extends DateState> referenceCollection,
        String qualifier,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        TransientContainerId transientContainerId = PersistenceHelper.getTransientContainerId(
            referenceCollection
        );
        RefContainer refContainer = (RefContainer) getPackageForContext(
            (RefBaseObject)referenceCollection, 
            null
        ).refObject(
            transientContainerId.getParent()
        ).refGetValue(
            transientContainerId.getFeature()
        );
        return (T) refViewForTimeRange(
            (RefObject) refContainer.refGet(
                QualifierType.REASSIGNABLE, 
                qualifier
            ),
            validFrom,
            validTo
        );
    }    
    
    /**
     * Retrieve a view for a given period.
     * 
     * @param dateState
     * @param packageClass
     * 
     * @return a package for the time range given by dateState
     */
    @SuppressWarnings("unchecked")
    public static <T extends RefPackage> T getPackageForTimeRange(
        DateState dateState,
        Class<T> packageClass
   ){
       DateStateContext dateStateContext = DateStateViews.getContext(dateState);
       if(
           dateStateContext != null && 
           dateStateContext.getViewKind() == ViewKind.TIME_RANGE_VIEW
       ) {
           return (T) dateState.refImmediatePackage();
       } else {
           return getPackageForTimeRange(
               dateState, 
               packageClass, 
               dateState.getStateValidFrom(),
               dateState.getStateValidTo()
           );
       }
    }
       
    private static RefObject refViewForTimeRange(
        RefObject object,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        DateStateContext context = getContext(object);
        RefObject refObject = object;
        if(context == null) {
            if(object instanceof BasicState) {
                refObject = ((BasicState)object).getCore();
            }
        } else if(
            context.getViewKind() == ViewKind.TIME_RANGE_VIEW &&
            equals(validFrom, context.getValidFrom()) &&
            equals(validTo, context.getValidTo())
        ){
            return object;
        }
        return object == null ? null : refObjectById( 
            refPackageForTimeRange(
                object,
                validFrom,
                validTo
            ),
            (UUID) JDOHelper.getTransactionalObjectId(refObject)
        );
    }

    /**
     * Retrieve a view for a given period.
     * <p>
     * Views to DateState instances are write-only except for stateValidFrom 
     * and stateValidTo. Attribute modification operations are propagated to 
     * all included states. Holes remain untouched.
     * 
     * @param refObject a plain JMI object or a date state view
     * @param validFrom exclude all states not valid at or after the given 
     * date unless validFrom is <code>null</code>
     * @param validTo exclude all states not valid at or before the given 
     * date unless validTo is <code>null</code>
     * 
     * @return a view to the given object
     * 
     * @exception IllegalArgumentException if the <code>object</code> does not support views
     */
    @SuppressWarnings("unchecked")
    public static <T extends RefObject> T getViewForTimeRange(
        T object,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        return (T) refViewForTimeRange(object, validFrom, validTo);
    }

    /**
     * Retrieve a view for the period beginning with the first valid state
     * and ending with the last valid state.
     * <p>
     * Views to DateState instances are write-only except for stateValidFrom 
     * and stateValidTo. Attribute modification operations are propagated to 
     * all included states. Holes inside the given range remain untouched.
     * 
     * @param dateState a plain JMI object or a date state view
     * 
     * @return a view covering the whole period
     * 
     * @exception IllegalArgumentException if the <code>dateState</code> does not support views
     */
    @SuppressWarnings("unchecked")
    public static <T extends DateState> T getViewForLifeTime(
        StateCapable dateState
    ){
        if(dateState == null) {
            return null;
        }
        List<? extends DateState> states = getStates(
            dateState,
            (XMLGregorianCalendar)null, // validFrom
            (XMLGregorianCalendar)null // validTo
        );
        if(states.isEmpty()) {
            return null;
        }
        return (T) refViewForTimeRange(
            dateState,
            states.get(0).getStateValidFrom(),
            states.get(states.size()-1).getStateValidTo()
        );
    }

    /**
     * Retrieve a view for adjacent states.
     * <p>
     * Views to DateState instances are write-only except for stateValidFrom 
     * and stateValidTo. Attribute modification operations are propagated to 
     * all included states.
     * 
     * @param dateStateView the underlying date state view
     * 
     * @return a view covering the whole period
     * 
     * @exception IllegalArgumentException if the <code>dateStateView</code> does not support views
     */
    public static <T extends DateState> T getViewForContiguousStates(
        T dateStateView
    ){
        throw new UnsupportedOperationException();
    }
    
        
    //------------------------------------------------------------------------
    // State Propagation Views (readable and writable)
    //------------------------------------------------------------------------

    /**
     * Retrieve a view for a given period.
     * <p>
     * The attributes are readable and writable.
     * 
     * @param dateStateView the underlying date state view
     * @param validFrom begin of the overridden period 
     * @param validTo end of the overridden period
     * @param override tells whether it is allowed to override valid states 
     * 
     * @return a view to the given object
     */
    public static <T extends DateState> T getViewForInitializedState(
        Class<T> targetClass,
        StateCapable source,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        boolean override
    ){
        if(source == null) {
            return null;
        }
        PersistenceManager_1_0 sourceManager = (PersistenceManager_1_0) JDOHelper.getPersistenceManager(source);
        InteractionSpec stateContext = DateStateViewContext.newTimeRangeViewContext(
            validFrom,
            validTo
        );
        PersistenceManager targetManager = sourceManager.getPersistenceManager(stateContext);
        Object object = targetManager.getObjectById(JDOHelper.getTransactionalObjectId(source));
        T target = targetClass.cast(object);
        if(target != null && !JDOHelper.isDeleted(target)) {
            if(override) {
                target.refDelete();
            } else {
                throw new RuntimeServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.DUPLICATE,
                    "The given range is not empty",
                    ExceptionHelper.newObjectIdParameter("id", source),
                    new BasicException.Parameter("context", stateContext)
                );
            }
        }
        target = targetManager.newInstance(targetClass);
        target.setCore(source);
        return target;
    }
    
    /**
     * Retrieve a view for a given period.
     * <p>
     * The attributes are readable and writable.
     * 
     * @param dateStateView the underlying date state view
     * @param validFrom begin of the overridden period 
     * @param validTo end of the overridden period
     * @param override tells whether it is allowed to override valid states 
     * 
     * @return a view to the given object
     */
    @SuppressWarnings("unchecked")
    public static <T extends DateState> T getViewForInitializedState(
        T source,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        boolean override
    ){
        if(source == null) {
            return null;
        }
        UUID transientObjectId = (UUID) JDOHelper.getTransactionalObjectId(source);
        String classId = source.refClass().refMofId();
        RefPackage_1_0 refPackage = refPackageForTimeRange(
            source,
            validFrom,
            validTo
        );
        T range = (T) refObjectById(
            refPackage,
            transientObjectId
        );
        if(range != null && !JDOHelper.isDeleted(range)) {
            if(override) {
                range.refDelete();
            } else {
                throw new RuntimeServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.DUPLICATE,
                    "The given range is not empty",
                    ExceptionHelper.newObjectIdParameter("id", source)
                );
            }
        }
        range = (T) refPackage.refClass(
            classId
        ).refCreateInstance(
            null
        ); 
        range.setCore((AspectCapable) source);
        return range;
    }
    
    /**
     * Retrieve a view for a given period.
     * <p>
     * The attributes are readable and writable.
     * 
     * @param dateStateView the underlying date state view
     * @param validFrom begin of the overridden period 
     * @param validTo end of the overridden period
     * @param override tells whether it is allowed to override valid states 
     * 
     * @return a view to the given object
     * 
     * @exception IllegalArgumentException if the <code>source</code> does not support views
     */
    @SuppressWarnings("unchecked")
    public static <T extends DateState> T getViewForPropagatedState(
        T source,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        boolean override
    ){
        if(source == null) {
            return null;
        }
        //
        // Compare source and view validity
        //
        boolean fromExcluded = Order.compareValidFrom(
            source.getStateValidFrom(),
            validFrom
        ) > 0;
        boolean toExcluded = Order.compareValidTo(
            source.getStateValidTo(),
            validTo
        ) < 0;
        //
        // Validate source
        //
        UUID resourceIdentifier = (UUID) JDOHelper.getTransactionalObjectId(source);
        DateStateViewContext viewContext =  DateStateViewContext.newTimeRangeViewContext(
            validFrom,
            validTo
        );
        RefPackage_1_0 refPackage = getPackageForContext(
            source,
            viewContext
        ); 
        T target = (T) refObjectById(
            refPackage,
            resourceIdentifier
        );
        if(fromExcluded || toExcluded) {
            if(override) {
                DateState state = (DateState) refObjectById(
                    refPackage,
                    getResourceIdentifierOfClone(source)
                );
                state.setStateValidFrom(validFrom);
                state.setStateValidTo(validTo);
                target.refDelete();
                state.setCore((AspectCapable) source);
            } else {
                for(org.openmdx.state2.cci2.DateState d : getStates((StateCapable)source, validFrom, validTo)){
                    if(!d.equals(source)) throw new JmiServiceException(
                        new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.DUPLICATE,
                            "There is another valid state in the given period",
                            new BasicException.Parameter("path", JDOHelper.getObjectId(source)),
                            new BasicException.Parameter("validFrom", validFrom),
                            new BasicException.Parameter("validTo", validTo),
                            new BasicException.Parameter("override", Boolean.FALSE)
                        )
                    );
                }
            }
        } else {
            if(!isReadable(source)) {
                throw new IllegalArgumentException(
                    "The source must be completely readable"
                );
            }
        }
        return target;            
    }

    /**
     * Retrieve a view for a given period, which is propagated
     * through navigation and applied to <code>DateState</code> instances.
     * <p>
     * The attributes are readable and writable.
     * 
     * @param dateStateView the underlying date state view
     * @param validFrom begin of the overridden period 
     * @param validTo end of the overridden period
     * 
     * @return a view to the given object
     */
    public static <T extends DateState> T getViewForPropagatedState(
        T dateStateView,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        return getViewForPropagatedState(
            dateStateView,
            validFrom,
            validTo,
            true
        );
    }
    
    /**
     * 
     * @param refContext
     * @param containerIdentifier
     * @param viewContext 
     * 
     * @return
     */
    private static RefContainer getContainerForContext(
        RefBaseObject refContext,
        Path containerIdentifier, 
        DateStateViewContext viewContext
    ){
        return getPackageForContext(
            refContext,
            viewContext
        ).refContainer(
            containerIdentifier,
            null // containerClass
        );
    }
    
    /**
     * 
     * @param refContext
     * @param transientContainerId
     * @param viewContext 
     * 
     * @return
     */
    private static RefContainer getContainerForContext(
        RefBaseObject refContext,
        TransientContainerId transientContainerId, 
        DateStateViewContext viewContext
    ){
        return (RefContainer) getPackageForContext(
            refContext,
            viewContext
        ).refObject(
            transientContainerId.getParent()
        ).refGetValue(
            transientContainerId.getFeature()
        );
    }
    
    /**
     * 
     * @param <T>
     * @param context
     * @param containerIdentifier
     * @param resourceIdentifier
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T extends DateState> List<T> getStates(
        RefContainer refContainer,
        Object resourceIdentifier
    ){
        return (List<T>) refContainer.refGetAll(
            new FilterProperty[]{
                new FilterProperty(
                    Quantors.THERE_EXISTS,
                    SystemAttributes.OBJECT_INSTANCE_OF,
                    FilterOperators.IS_IN,
                    "org:openmdx:state2:DateState"
                ),
                new FilterProperty(
                    Quantors.THERE_EXISTS,
                    "core",
                    FilterOperators.IS_IN,
                    resourceIdentifier
                )
            }
        );        
    }
    
    /**
     * Retrieve states 
     * 
     * @param referenceCollection the result of the parent view's getXXX() method
     * @param qualifier the object's qualifier
     * @param validFrom include all states ending at validFrom or later
     * @param validTo include all states beginning at validTo or earlier
     * @param invalidated tells whether valid or invalid states excluded<ul>
     * <li><code>null</code>: Neither valid nor invalid states are excluded
     * <li><code>TRUE</code>: Valid states are excluded
     * <li><code>FALSE</code>: Invalid states are excluded
     * </ul>
     * @return a collection of DateState instances
     */
    @SuppressWarnings("unchecked")
    private static <T extends DateState> List<T> getStates(
        Collection<T> referenceCollection,
        String qualifier,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        Boolean invalidated
    ){
        RefContainer contextFreeContainer = getContainerForContext(
            (RefBaseObject) referenceCollection,
            PersistenceHelper.getTransientContainerId(referenceCollection),
            null
        );
        Object coreIdentifier;
        Path containerId = PersistenceHelper.getContainerId(referenceCollection);
        if(containerId == null)  {
            Object core = qualifier.startsWith("!") ? contextFreeContainer.refGet(
                 RefContainer.PERSISTENT, qualifier.substring(1)
            ) : contextFreeContainer.refGet(
                 RefContainer.REASSIGNABLE, qualifier
            );
            coreIdentifier = JDOHelper.getTransactionalObjectId(core);
        } else {
            coreIdentifier = containerId.getChild(qualifier);
        }
        return (List<T>) new FilteredStates<DateState>(
            getStates(
                contextFreeContainer,
                coreIdentifier
            ),
            validFrom,
            validTo,
            invalidated,
            null, 
            true
        );
    }
    
    /**
     * Retrieve states 
     * 
     * @param referenceCollection the result of the parent view's getXXX() method
     * @param qualifier the object's qualifier
     * @param invalidated tells whether valid or invalid states excluded<ul>
     * <li><code>null</code>: Neither valid nor invalid states are excluded
     * <li><code>TRUE</code>: Valid states are excluded
     * <li><code>FALSE</code>: Invalid states are excluded
     * </ul>
     * @return a collection of DateState instances
     */
    public static <T extends DateState> List<T> getStates(
        Collection<T> referenceCollection,
        String qualifier,
        Boolean invalidated
    ){
        return getStates(
            referenceCollection,
            qualifier,
            null, // validFrom
            null, // validTo
            invalidated
        );
    }

    /**
     * Retrieve states 
     * 
     * @param referenceCollection the result of the parents getXXX() method
     * @param qualifier the object's qualifier
     * @param includeValidStates tells whether the invalidatedAt attribute may be non-null
     * @param includeInvalidStates tells whether the invalidatedAt attribute may be null
     * @return a collection of DateState instances
     */
    public static <T extends DateState> List<T> getStates(
        Collection<T> referenceCollection,
        String qualifier,
        boolean includeValidStates,
        boolean includeInvalidStates
    ){
        if(includeValidStates || includeInvalidStates){
            return getStates(
                referenceCollection,
                qualifier,
                includeInvalidStates && includeValidStates ? null : Boolean.valueOf(includeInvalidStates)
            );
        } else {
            return Collections.emptyList();
        }
    }    
    
    /**
     * Retrieve valid states
     * 
     * @param referenceCollection the result of the parents getXXX() method
     * @param qualifier the object's qualifier
     * @param validFrom include all states ending at validFrom or later
     * @param validTo include all states beginning at validTo or earlier
     * 
     * @return a list of DateState instances
     */
    public static <T extends DateState> List<T> getStates(
        Collection<T> referenceCollection,
        String qualifier,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        return getStates(
            referenceCollection,
            qualifier,
            validFrom,
            validTo,
            Boolean.FALSE
        );
    }    
    
    /**
     * Retrieve all valid states
     * 
     * @param referenceCollection the result of the parents getXXX() method
     * @param qualifier the object's qualifier
     * 
     * @return a list of DateState instances
     */
    public static <T extends DateState> List<T> getStates(
        Collection<T> referenceCollection,
        String qualifier
    ){
        return getStates(
            referenceCollection,
            qualifier,
            null, // validFrom
            null, // validTo
            Boolean.FALSE
        );
    }    
    
    /**
     * Retrieve states 
     * 
     * @param dateState a plain JMI object or a date state view
     * @param validFrom include all states ending at validFrom or later
     * @param validTo include all states beginning at validTo or earlier
     * @param invalidated tells whether valid or invalid states excluded<ul>
     * <li><code>null</code>: Neither valid nor invalid states are excluded
     * <li><code>TRUE</code>: Valid states are excluded
     * <li><code>FALSE</code>: Invalid states are excluded
     * </ul>
     * @param existsAt 
     * @param asView 
     * @return a collection of DateState instances
     */
    private static <T extends DateState> List<T> getStates(
        StateCapable dateState,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        Boolean invalidated, 
        Date existsAt, 
        boolean asView
    ){
        Path resourceIdentifier = (Path) JDOHelper.getObjectId(dateState);
        List<T> states = getStates(
            getContainerForContext(
                dateState,
                resourceIdentifier.getParent(),
                null
            ),
            resourceIdentifier
        ); 
        return new FilteredStates<T>(
            states,
            validFrom,
             validTo,
             invalidated,
             null, 
             asView
        );
    }

    
    /**
     * Retrieve states 
     * 
     * @param dateState a plain JMI object or a date state view
     * @param invalidated tells whether valid or invalid states excluded<ul>
     * <li><code>null</code>: Neither valid nor invalid states are excluded
     * <li><code>TRUE</code>: Valid states are excluded
     * <li><code>FALSE</code>: Invalid states are excluded
     * </ul>
     * @return a collection of DateState instances matching the given criteria
     */
    @SuppressWarnings("unchecked")
    public static <T extends DateState> List<T>getStates(
        StateCapable dateState,
        Boolean invalidated
    ){
        return (List<T>) (dateState == null ? null : getStates(
            dateState,
            null, // validFrom
            null, // validTo
            invalidated,
            null, 
            true // asView
        ));
    }

    /**
     * Retrieve states 
     * 
     * @param dateState a plain JMI object or a date state view
     * @param includeValidStates tells whether the invalidatedAt attribute may 
     * be non-<code>null</code>
     * @param includeInvalidStates tells whether the invalidatedAt attribute 
     * may be <code>null</code>
     * @return a collection of DateState instances
     */
    public static Collection<? extends DateState> getStates(
        StateCapable dateState,
        boolean includeValidStates,
        boolean includeInvalidStates
    ){
        if(includeValidStates || includeInvalidStates) {
            return Collections.emptyList();
        } else {
            return getStates(
                dateState,
                includeInvalidStates && includeValidStates ? null : Boolean.valueOf(includeInvalidStates)
            );
        }
    }
    
    /**
     * Retrieve valid states 
     * 
     * @param dateState a plain JMI object or a date state view
     * @param validFrom include all states ending at validFrom or later
     * @param validTo include all states beginning at validTo or earlier
     * 
     * @return a list of DateState instances
     */
    @SuppressWarnings("unchecked")
    public static <T extends DateState> List<T> getStates(
        StateCapable dateState,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        return (List<T>) (dateState == null ? null : getStates(
            dateState,
            validFrom,
            validTo,
            Boolean.FALSE, // invalidated
            null, // existsAt
            true // asView
        ));
    }    
    
    /**
     * Retrieve valid states
     * 
     * @param dateState a plain JMI object or a date state view
     * 
     * @return a list of DateState instances
     */
    @SuppressWarnings("unchecked")
    public static <T extends DateState> List<T> getStates(
        StateCapable dateState
    ){
        return (List<T>) (dateState == null ? null : getStates(
            dateState,
            (XMLGregorianCalendar)null,
            (XMLGregorianCalendar)null
        ));
    }

    
    //------------------------------------------------------------------------
    // Date State Context
    //------------------------------------------------------------------------
    
    /**
     * Retrieve the context for date state views.
     * 
     * @param refBaseObject a plain JMI object or a date state view
     * 
     * @return the <code>DateStateContext</code> in case of a 
     * Date State View, <code>null</code> otherwise.
     */
    public static DateStateContext getContext(
        RefBaseObject refBaseObject
    ){
        if(refBaseObject != null) {
            RefPackage refPackage = refBaseObject.refOutermostPackage();
            if(refPackage instanceof RefPackage_1_0) {
                InteractionSpec interactionSpec = ((RefPackage_1_0)refPackage).refInteractionSpec();
                if(interactionSpec instanceof DateStateContext) {
                    return (DateStateContext)interactionSpec;
                }
            }
        }
        return null;
    }

    /**
     * Tells whether the object is a validity aware view
     * 
     * @param refObject a plain JMI object or a date state view
     */
    public static boolean isView(
        RefObject refObject
    ){
        return getContext(refObject) != null;
    }

    /**
     * Tests whether a given object is readable
     * 
     * @param refObject a plain JMI object or a date state view
     * 
     * @return <code>true</code> unless one of the following conditions is met<ul>
     * <li><code>refObject</code> is <code>null</code> 
     * <li><code>refObject</code> is deleted 
     * <li><code>refObject instanceof DateState && isView(refObject)</code> is <code>true</code> 
     * and the view does not refer to exactly one valid state 
     * </ul>
     * 
     * @deprecated readability is now determined on a <u>by attribute</u> basis
     */
    public static boolean isReadable(
        RefObject refObject
    ){
        if(
            refObject == null ||
            JDOHelper.isDeleted(refObject)
        ){
            return false;
        }
        DateStateContext context = getContext(refObject);
        if(
            !(refObject instanceof DateState) ||    
            context == null ||
            context.getViewKind() == ViewKind.TIME_POINT_VIEW
        ){
            return true;
        }
        return getStates(
            (StateCapable)refObject, 
            context.getValidFrom(), 
            context.getValidTo()
        ).size() == 1;
    }
    
    /**
     * Clone an object 
     * 
     * @param refObject view to the object to be cloned
     *
     * @return the clone's transient resource identifier
     */
    private static UUID getResourceIdentifierOfClone(
        DateState refObject
    ){
        if(refObject != null && !JDOHelper.isDeleted(refObject)){
            DateStateContext context = getContext(refObject);
            if(context != null) {
                List<DateState> involved = context.getViewKind() == ViewKind.TIME_POINT_VIEW ? getStates(
                    (StateCapable) refObject, 
                    context.getValidAt(), 
                    context.getValidAt(),
                    null, // invalidated
                    context.getExistsAt(), 
                    false // asView
                ) : getStates(
                    (StateCapable) refObject, 
                    context.getValidFrom(), 
                    context.getValidTo(),
                    Boolean.FALSE, // invalidated
                    null, // existsAt 
                    false // asView
                );
                if(involved.size() == 1) {
                    return (UUID) JDOHelper.getTransactionalObjectId(
                        PersistenceHelper.clone(involved.get(0))
                    );
                }
            }
        }
        throw new IllegalArgumentException(
            "The source must be completely readable"
        );
    }
    
    /**
     * Retrieve the current date
     * 
     * @return the current date
     */
    public static XMLGregorianCalendar today(
    ){
        return DateStateContexts.today();
    }

    /**
     * Derive the filter from the state context
     * 
     * @param stateContext
     * 
     * @return the corresponding filter
     */
    protected static FilterProperty[] getAmendment(
        DateStateContext stateContext
    ){
        if(stateContext == null) {
            return null;
        }
        switch(stateContext.getViewKind()) {
            case TIME_RANGE_VIEW:
                return new FilterProperty[]{
                    new FilterProperty(
                        Quantors.FOR_ALL,
                        "stateValidFrom",
                        FilterOperators.IS_LESS_OR_EQUAL,
                        stateContext.getValidTo()
                    ),
                    new FilterProperty(
                        Quantors.FOR_ALL,
                        "stateValidTo",
                        FilterOperators.IS_GREATER_OR_EQUAL,
                        stateContext.getValidFrom()
                    ),
                    new FilterProperty(
                        Quantors.FOR_ALL,
                        SystemAttributes.REMOVED_AT,
                        FilterOperators.IS_IN
                    )
                };
            case TIME_POINT_VIEW:
                return stateContext.getExistsAt() == null ? new FilterProperty[]{
                    new FilterProperty(
                        Quantors.FOR_ALL,
                        "stateValidFrom",
                        FilterOperators.IS_LESS_OR_EQUAL,
                        stateContext.getValidAt()
                    ),
                    new FilterProperty(
                        Quantors.FOR_ALL,
                        "stateValidTo",
                        FilterOperators.IS_GREATER_OR_EQUAL,
                        stateContext.getValidAt()
                    ),
                    new FilterProperty(
                        Quantors.FOR_ALL,
                        SystemAttributes.REMOVED_AT,
                        FilterOperators.IS_IN
                    )
                } : new FilterProperty[]{
                    new FilterProperty(
                        Quantors.FOR_ALL,
                        "stateValidFrom",
                        FilterOperators.IS_LESS_OR_EQUAL,
                        stateContext.getValidAt()
                    ),
                    new FilterProperty(
                        Quantors.FOR_ALL,
                        "stateValidTo",
                        FilterOperators.IS_GREATER_OR_EQUAL,
                        stateContext.getValidAt()
                    ),
                    new FilterProperty(
                        Quantors.FOR_ALL,
                        SystemAttributes.CREATED_AT,
                        FilterOperators.IS_LESS_OR_EQUAL,
                        stateContext.getExistsAt()
                    ),
                    new FilterProperty(
                        Quantors.FOR_ALL,
                        SystemAttributes.REMOVED_AT,
                        FilterOperators.IS_GREATER_OR_EQUAL,
                        stateContext.getExistsAt()
                    )
                };
            default:
                return null;
        }
    }
        
    
    //------------------------------------------------------------------------
    // Class StateList
    //------------------------------------------------------------------------
    
    /**
     * State List
     */
    static class StateList<E extends DateState> extends AbstractSequentialList<E> {

        /**
         * Constructor 
         *
         * @param delegate
         */
        StateList(
            List<E> delegate
        ){
            this.delegate = delegate;
        }

        /**
         * 
         */
        final List<E> delegate;
        
        /* (non-Javadoc)
         * @see java.util.AbstractSequentialList#listIterator(int)
         */
        @Override
        public ListIterator<E> listIterator(int index) {
            return new StateIterator<E>(
                this.delegate.listIterator(index)
            );
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return this.delegate.size();
        }
    
        
    }
    
    
    //------------------------------------------------------------------------
    // Class StateIterator
    //------------------------------------------------------------------------
    
    /**
     * State Iterator
     */
    static class StateIterator<E extends DateState> implements ListIterator<E> {

        StateIterator(
            ListIterator<E> delegate
        ){
            this.delegate = delegate;
        }
        
        private final ListIterator<E> delegate;

        /**
         * @param o
         * @see java.util.ListIterator#add(java.lang.Object)
         */
        public void add(E o) {
            throw new UnsupportedOperationException();
        }

        /**
         * @return
         * @see java.util.ListIterator#hasNext()
         */
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        /**
         * @return
         * @see java.util.ListIterator#hasPrevious()
         */
        public boolean hasPrevious() {
            return this.delegate.hasPrevious();
        }

        /**
         * @return
         * @see java.util.ListIterator#next()
         */
        public E next() {
            return getViewForState(
                this.delegate.next()
            );
        }

        /**
         * @return
         * @see java.util.ListIterator#nextIndex()
         */
        public int nextIndex() {
            return this.delegate.nextIndex();
        }

        /**
         * @return
         * @see java.util.ListIterator#previous()
         */
        public E previous() {
            return getViewForState(
                this.delegate.previous()
            );
        }

        /**
         * @return
         * @see java.util.ListIterator#previousIndex()
         */
        public int previousIndex() {
            return this.delegate.previousIndex();
        }

        /**
         * 
         * @see java.util.ListIterator#remove()
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * @param o
         * @see java.util.ListIterator#set(java.lang.Object)
         */
        public void set(E o) {
            throw new UnsupportedOperationException();
        }

        
    }
    

    //------------------------------------------------------------------------
    // Class FilteredStates
    //------------------------------------------------------------------------

    /**
     * Filtered and ordered states
     */
    private static class FilteredStates<E extends DateState> extends AbstractSequentialList<E> {

        /**
         * Constructor 
         *
         * @param dateStates
         * @param validFrom
         * @param validTo
         * @param invalidated
         * @param existsAt
         * @param asView
         */
        FilteredStates(
            Collection<E> dateStates,
            XMLGregorianCalendar validFrom,
            XMLGregorianCalendar validTo,
            Boolean invalidated,
            Date existsAt,
            boolean asView
        ) {
            this.dateStates = dateStates;
            this.validFrom = validFrom;
            this.validTo = validTo;
            this.invalidated = invalidated;
            this.existsAt = existsAt;
            this.asView = asView;
        }


        private final Collection<E> dateStates;
        private final XMLGregorianCalendar validFrom;
        private final XMLGregorianCalendar validTo;
        private final Boolean invalidated;
        private final Date existsAt;
        private final boolean asView;
        
        /**
         * The <code>DateState</code> comparator
         */
        private final static Comparator<DateState> stateComparator = new Comparator<DateState>(){

            public int compare(DateState o1, DateState o2) {
                if(JDOHelper.isDeleted(o1)) {
                    return JDOHelper.isDeleted(o2) ? 0 : 1;
                }
                if(JDOHelper.isDeleted(o2)) {
                    return -1;
                }
                int validFrom = Order.compareValidFrom(
                    o1.getStateValidFrom(),
                    o2.getStateValidFrom()
                ); 
                if(validFrom != 0) {
                    return validFrom;
                }
                int removedAt = Order.compareRemovedAt(
                    o1.getRemovedAt(),
                    o2.getRemovedAt()
                );  
                return removedAt;
           }
            
        };
        
        /**
         * Return the
         * @return
         */
        private Collection<E> getDelegate(){
            SortedSet<E> set = new TreeSet<E>(stateComparator);
            Next: for(E state : dateStates) {
                if(this.invalidated != null) {
                    boolean invalidated = state.getRemovedAt() != null; 
                    if(invalidated != this.invalidated) continue Next;
                }
                if(Order.compareValidFromToValidTo(this.validFrom, state.getStateValidTo()) > 0) continue Next;                     
                if(Order.compareValidFromToValidTo(state.getStateValidFrom(), this.validTo) > 0) continue Next;
                if(!this.asView) {
                    if(this.existsAt == null) {
                        if(state.getRemovedAt() != null) continue Next;
                    } else {
                        if(this.existsAt.compareTo(state.getCreatedAt()) < 0) continue Next;
                        if(Order.compareRemovedAt(this.existsAt, state.getRemovedAt()) >= 0) continue Next;
                    }       
                }
                set.add(state);
            }
            return set;
        }
        
        
        /* (non-Javadoc)
         * @see java.util.AbstractSequentialList#listIterator(int)
         */
        @Override
        public ListIterator<E> listIterator(int index) {
            List<E> list = new ArrayList<E>();
            for(E t : getDelegate()) {
                list.add(
                    asView ? getViewForState(t) : t
                );
            }
            return list.listIterator(index);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return getDelegate().size();
        }


        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return getDelegate().isEmpty();
        }
        
    }
    
}
