/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: DateStateViews.java,v 1.53 2010/08/30 15:02:24 hburger Exp $
 * Description: Date State Views
 * Revision:    $Revision: 1.53 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/08/30 15:02:24 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2010, OMEX AG, Switzerland
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

import static org.oasisopen.cci2.QualifierType.REASSIGNABLE;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.accessor.jmi.spi.DelegatingRefObject_1_0;
import org.openmdx.base.accessor.spi.ExceptionHelper;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.AspectCapable;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.IsGreaterCondition;
import org.openmdx.base.query.IsGreaterOrEqualCondition;
import org.openmdx.base.query.IsInCondition;
import org.openmdx.base.query.IsInstanceOfCondition;
import org.openmdx.base.query.Quantifier;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.state2.jmi1.BasicState;
import org.openmdx.state2.jmi1.DateState;
import org.openmdx.state2.jmi1.StateCapable;
import org.openmdx.state2.spi.DateStateContexts;
import org.openmdx.state2.spi.DateStateViewContext;
import org.openmdx.state2.spi.Order;
import org.openmdx.state2.spi.Parameters;
import org.openmdx.state2.spi.StateViewContext;
import org.w3c.cci2.AnyTypePredicate;
import org.w3c.cci2.Container;
import org.w3c.cci2.ImmutableDatatype;
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

    /**
     * Used in views for invalidated states open at both sides
     */
    private static XMLGregorianCalendar DEFAULT_VALID_FOR = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(
        2000, 
        1, // January
        1, // 1st
        DatatypeConstants.FIELD_UNDEFINED
    );
    
    /**
     * Retrieve the PersistenceManager for a given state view context
     * 
     * @param persistenceManager the JDO context
     * @param viewContext, may be <code>null</code>
     * 
     * @return the <code>PersistenceManager</code> for the requested state view context
     */
    public static PersistenceManager getPersistenceManager(
        PersistenceManager persistenceManager,
        StateViewContext<XMLGregorianCalendar> viewContext
    ){
        RefPackage_1_0 refPackageFactory = (RefPackage_1_0) persistenceManager.getUserObject(RefPackage.class);
        return refPackageFactory.refPackage(viewContext).refPersistenceManager();
    }
    
    /**
     * Retrieve the PersistenceManager for a given time range view
     * 
     * @param persistenceManager the JDO context
     * @param validFrom the begin of the time range, or <code>null</code> for an unconstrained lower bound
     * @param validTo the end of the time range, or <code>null</code> for an unconstrained upper bound
     * 
     * @return the <code>PersistenceManager</code> for the given state view context
     */
    public static PersistenceManager getPersistenceManager(
        PersistenceManager persistenceManager,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        return DateStateViews.getPersistenceManager(
            persistenceManager,
            DateStateViewContext.newTimeRangeViewContext(validFrom, validTo)
        );
    }

    /**
     * Retrieve the PersistenceManager for a given time point view
     * 
     * @param persistenceManager the JDO context
     * @param validFor the view's valid time point, or <code>null</code> for today
     * @param validAt the view's transaction time point, or <code>null</code> for an up-to-date view
     * 
     * @return the <code>PersistenceManager</code> for the given state view context
     */
    public static PersistenceManager getPersistenceManager(
        PersistenceManager persistenceManager,
        XMLGregorianCalendar validFor,
        Date validAt
    ){
        return DateStateViews.getPersistenceManager(
            persistenceManager,
            DateStateViewContext.newTimePointViewContext(
                validFor == null ? DateStateViews.today() : validFor, 
                validAt
            )
        );
    }
    
    /**
     * Retrieve the outermost package for given contexts
     * 
     * @param refContext
     * @param viewContext
     * 
     * @return the outermost package for the given contexts
     */
    private static RefPackage_1_0 getPackageForContext(
        RefBaseObject refContext,
        StateViewContext<XMLGregorianCalendar> viewContext
    ){
        RefPackage_1_0 refPackageFactory = (RefPackage_1_0)refContext.refOutermostPackage(); 
        return refPackageFactory.refPackage(viewContext);
    }

    /**
     * Retrieve the PersistenceManager for a given state view context
     * 
     * @param refContext
     * @param viewContext, may be <code>null</code>
     * 
     * @return the <code>PersistenceManager</code> for the given state view context
     */
    public static PersistenceManager getPersistenceManager(
        RefBaseObject refContext,
        StateViewContext<XMLGregorianCalendar> viewContext
    ){
        return DateStateViews.getPackageForContext(refContext, viewContext).refPersistenceManager();
    }

    /**
     * Retrieve the PersistenceManager for a given time range view
     * 
     * @param refContext the JMI context
     * @param validFrom the begin of the time range, or <code>null</code> for an unconstrained lower bound
     * @param validTo the end of the time range, or <code>null</code> for an unconstrained upper bound
     * 
     * @return the <code>PersistenceManager</code> for the given state view context
     */
    public static PersistenceManager getPersistenceManager(
        RefBaseObject refContext,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        return DateStateViews.getPersistenceManager(
            refContext,
            DateStateViewContext.newTimeRangeViewContext(validFrom, validTo)
        );
    }

    /**
     * Retrieve the PersistenceManager for a given time point view
     * 
     * @param refContext the JMI context
     * @param validFor the view's valid time point, or <code>null</code> for today
     * @param validAt the view's transaction time point, or <code>null</code> for an up-to-date view
     * 
     * @return the <code>PersistenceManager</code> for the given state view context
     */
    public static PersistenceManager getPersistenceManager(
        RefBaseObject refContext,
        XMLGregorianCalendar validFor,
        Date validAt
    ){
        return DateStateViews.getPersistenceManager(
            refContext,
            DateStateViewContext.newTimePointViewContext(
                validFor == null ? DateStateViews.today() : validFor, 
                validAt
            )
        );
    }
    
    /**
     * Create a core object in the given context
     * 
     * @param refContext
     * @param coreClass
     * 
     * @return a new core object
     */
    public static <T extends StateCapable> T createCore(
        RefBaseObject refContext,
        Class<T> coreClass
    ){
        return DateStateViews.getPackageForContext(
            refContext, 
            null
        ).refPersistenceManager(
        ).newInstance(
            coreClass
        );
    }
    
    /**
     * Add a core object to the date state context free  view of the given container
     * 
     * @param container
     * @param qualifier
     * @param core
     */
    @SuppressWarnings("unchecked")
    public static <T extends org.openmdx.state2.jmi1.StateCapable>  void addCoreToContainer(
        Container<? super T> container,
        String qualifier,
        T core
    ){
        DateStateContext context = DateStateViews.getContext(core); 
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
        RefContainer<? super T> refContainer = (RefContainer<? super T>) container;
        if(DateStateViews.getContext(refContainer) != null) {
            refContainer = (RefContainer<? super T>) DateStateViews.getPersistenceManager(refContainer, null).getObjectById(
                PersistenceHelper.getTransientContainerId(refContainer)
            );
        }
        refContainer.refAdd(
            REASSIGNABLE,
            qualifier,
            core
        );
    }

    /**
     * Create an additional state
     * 
     * @param core
     * @param stateValidFrom
     * @param stateValidTo
     * @param stateClass
     * 
     * @return a new state
     * 
     * @throws InvalidArgumentException unless coreClass.getClass().isAssignableFrom(stateClass)
     */
    public static <T extends DateState> T createState(
        StateCapable core,
        XMLGregorianCalendar stateValidFrom,
        XMLGregorianCalendar stateValidTo,
        Class<T> stateClass
    ){
        T state = DateStateViews.getPersistenceManager(
            core, 
            stateValidFrom, 
            stateValidTo
        ).newInstance(
            stateClass
        );
        state.setCore(core);
        return state;
    }

    /**
     * Create a stated object
     * 
     * @param container
     * @param qualifier
     * @param coreClass
     * @param stateValidFrom
     * @param stateValidTo
     * @param stateClass
     * 
     * @return a stated object with a single state
     * 
     * @throws InvalidArgumentException unless coreClass.isAssignableFrom(stateClass)
     */
    public static <C extends StateCapable, S extends DateState> S createStatedObject(
        Container<? super C> container,
        String qualifier,
        Class<C> coreClass,
        XMLGregorianCalendar stateValidFrom,
        XMLGregorianCalendar stateValidTo,
        Class<S> stateClass
     ){
        C core = DateStateViews.createCore((RefContainer<?>)container, coreClass);
        DateStateViews.addCoreToContainer(container, qualifier, core);
        return DateStateViews.createState(core, stateValidFrom, stateValidTo, stateClass);
    }
    
    /**
     * Retrieve the state context aware predicate
     * 
     * @param refContainer
     * @param predicate
     * 
     * @return the state context aware predicate
     */
    private static AnyTypePredicate getStatePredicate(
        RefContainer<?> refContainer,
        AnyTypePredicate predicate
    ){
        if(predicate == null) return null;
        Filter original = ((RefQuery_1_0)predicate).refGetFilter();
        for(Condition condition : original.getCondition()) {
            String feature = condition.getFeature();
            if (
                SystemAttributes.CREATED_AT.equals(feature) ||
                SystemAttributes.REMOVED_AT.equals(feature) ||
                "stateValidFrom".equals(feature) ||
                "stateValidTo".equals(feature) 
            ){
                return predicate;
            }
        }
        List<Condition> amendment = DateStateViews.getAmendment(
            getContext(refContainer)
        );
        if(amendment == null) return predicate;
        Filter newFilter = new Filter(
            original.getCondition(),
            original.getOrderSpecifier(),
            null // extension
        );
        newFilter.getCondition().addAll(amendment);
        return newFilter;
    }
    
    /**
     * 
     * @param container
     * @param predicate
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <E extends DateState> List<E> getStates(
        Container<E> container,
        AnyTypePredicate predicate
    ){
        RefContainer<E> refContainer = (RefContainer<E>)container; 
        Container<E> stateContainer = (Container<E>)DateStateViews.getPersistenceManager(
            refContainer,
            null
        ).getObjectById(
            PersistenceHelper.getTransientContainerId(container)
        );
        return new StateList<E>(
            stateContainer.getAll(
                getStatePredicate(refContainer,predicate)
            )
        );
    }
            
    /**
     * Retrieve a view appropriate to represent the given state, i.e.<ul>
     * <li>a <em>time-range</em> view for a <em>valid</em> state
     * <li>a <em>time-point</em> view for an <em>invalidated</em> state
     * </ul>
     *
     * @param state
     * 
     * @return a view representing the given state
     */
    static <T extends DateState> T getViewForState(
        T state
    ){
        if(state == null) {
            return null;
        } else if(JDOHelper.isDeleted(state)) {
            return state;
        } else if(state.getRemovedAt() == null) {
            return DateStateViews.<T,T>getViewForTimeRange(
                state, 
                state.getStateValidFrom(), 
                state.getStateValidTo()
            );
        } else {
            XMLGregorianCalendar validFor = state.getStateValidFrom();
            if(validFor == null) validFor = state.getStateValidTo();         
            if(validFor == null) validFor = DateStateViews.DEFAULT_VALID_FOR;
            return DateStateViews.<T,T>getViewForTimePoint(
                state,
                validFor,
                state.getCreatedAt()
            );
        }
    }

    
    //------------------------------------------------------------------------
    // Core View 
    //------------------------------------------------------------------------
    
    /**
     * Retrieve a context-free view onto the given object
     * 
     * @param refObject the object for which a context free view is requested
     * 
     * @return a context-free view onto the given object
     */
    @SuppressWarnings("unchecked")
    public static <C extends RefObject, S extends C> C getViewForCore(
        S refObject
    ){
        return (C) DateStateViews.getPersistenceManager(
            refObject,
            null
        ).getObjectById(
            JDOHelper.getTransactionalObjectId(refObject)
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
            return left instanceof XMLGregorianCalendar || right instanceof XMLGregorianCalendar ? (
                datatypeFactory.toDate((XMLGregorianCalendar) left).equals(datatypeFactory.toDate((XMLGregorianCalendar)right))
            ) : left instanceof Date && right instanceof Date ? (
                datatypeFactory.toDateTime((Date) left).equals(datatypeFactory.toDateTime((Date) right))
            ) : false;
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
    public static <T extends RefObject, V extends T> V getViewForTimePoint(
        T object,
        XMLGregorianCalendar validFor,
        Date validAt
    ){
        if(object == null) return null;
        DateStateContext context = DateStateViews.getContext(object);
        RefObject refObject = object;
        if(context == null) {
            if(object instanceof BasicState) {
                refObject = ((BasicState)object).getCore();
            }
        } else if(
            context.getViewKind() == ViewKind.TIME_POINT_VIEW &&
            DateStateViews.equals(validFor, context.getExistsAt()) &&
            DateStateViews.equals(validAt, context.getValidAt())
        ){
            return (V)object;
        }
        return (V) DateStateViews.getPersistenceManager( 
            object,
            validFor,
            validAt
        ).getObjectById(
            JDOHelper.getTransactionalObjectId(refObject)
        );
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
     * @deprecated Use {@link #getViewForTimePoint(T,XMLGregorianCalendar,Date)} instead
     */
    @Deprecated
    public static <T extends RefObject, V extends T> V getView(
        T object,
        XMLGregorianCalendar validFor,
        Date validAt
    ){
        return DateStateViews.<T,V>getViewForTimePoint(object, validFor, validAt);
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
     * @deprecated Use {@link #getViewForTimePoint(T,XMLGregorianCalendar,Date)} instead
     */
    @Deprecated
    public static <T extends RefObject> T getView(
        T object,
        XMLGregorianCalendar validFor
    ){
        return DateStateViews.<T,T>getViewForTimePoint(
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
     * 
     * @deprecated {@link #getView(RefObject, XMLGregorianCalendar)} use getView(object,null)
     */
    @Deprecated
    public static <T extends RefObject> T getView(
        RefObject object
    ){
        return DateStateViews.<RefObject,T>getViewForTimePoint(
            object,
            null, // validFor,
            null // validAt
        );
    }
    
    
    //------------------------------------------------------------------------
    // Time Range Views 
    //------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public static <T extends RefPackage> T getPackageForTimeRange(
        RefBaseObject refContext,
        Class<T> refPackageClass,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo        
    ){
        return refContext == null ? null : (T) DateStateViews.getPackageForContext(
            refContext,
            DateStateViewContext.newTimeRangeViewContext(validFrom, validTo)
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
        RefContainer<T> refContainer = (RefContainer<T>) DateStateViews.getPersistenceManager(
            (RefBaseObject)referenceCollection, 
            validFrom,
            validTo
        ).getObjectById(
            PersistenceHelper.getTransientContainerId(referenceCollection)
        );
        return refContainer.refGet(
            REASSIGNABLE, 
            qualifier
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
       return DateStateViews.getViewKind(dateState) == ViewKind.TIME_RANGE_VIEW ?  (T)dateState.refImmediatePackage(
       ) : DateStateViews.getPackageForTimeRange(
           dateState, 
           packageClass, 
           dateState.getStateValidFrom(),
           dateState.getStateValidTo()
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
    public static <T extends RefObject, V extends T> V getViewForTimeRange(
        T object,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        DateStateContext context = DateStateViews.getContext(object);
        Object objectId;
        if(context == null) {
            objectId = JDOHelper.getTransactionalObjectId(
               object instanceof BasicState ? ((BasicState)object).getCore() : object
            );
        } else if(
            context.getViewKind() == ViewKind.TIME_RANGE_VIEW &&
            DateStateViews.equals(validFrom, context.getValidFrom()) &&
            DateStateViews.equals(validTo, context.getValidTo())
        ){
            return (V) object;
        } else {
            objectId = JDOHelper.getTransactionalObjectId(object);
        }
        DelegatingRefObject_1_0 refView = (DelegatingRefObject_1_0) DateStateViews.getPersistenceManager(
            object,
            validFrom,
            validTo
        ).getObjectById(
            objectId
        );
        return (V) refView;
    }
    
    /**
     * Retrieve time-range views for the states in a given range
     * <p><em>
     * Note:<br>
     * The border states are represented by views for propagated states if necessary.
     * </em>
     * @param stateCapable a plain JMI object or a date state view
     * @param validFrom exclude all states not valid at or after the given 
     * date unless validFrom is <code>null</code>
     * @param validTo exclude all states not valid at or before the given 
     * date unless validTo is <code>null</code>
     * 
     * @return a view to the given object
     * 
     * @exception IllegalArgumentException if the <code>object</code> does not support views
     */
    public static <T extends DateState> List<T> getCroppedStates(
        StateCapable stateCapable,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        return stateCapable == null ? null : DateStateViews.<T>getStates(
            stateCapable,
            validFrom,
            validTo,
            Boolean.FALSE, // invalidated
            null, // existsAt
            AccessMode.FOR_UPDATE
        );
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
        if(dateState == null) return null;
        List<? extends DateState> states = DateStateViews.getValidStates(dateState);
        return states.isEmpty() ? null : (T)DateStateViews.getViewForTimeRange(
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
     * @param stateClass the class of the new state
     * @param core the underlying date state view
     * @param validFrom begin of the overridden period 
     * @param validTo end of the overridden period
     * @param override tells whether it is allowed to override valid states 
     * 
     * @return a view to the given object
     */
    public static <T extends DateState> T getViewForInitializedState(
        Class<T> stateClass,
        StateCapable core,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        boolean override
    ){
        if(core == null) return null;
        PersistenceManager targetManager = DateStateViews.getPersistenceManager(
            core, 
            validFrom,
            validTo
        );
        Object object = targetManager.getObjectById(
            JDOHelper.getTransactionalObjectId(core)
        );
        if(object != null && !JDOHelper.isDeleted(object)) {
            if(override) {
                targetManager.deletePersistent(object);
            } else {
                throw new RuntimeServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.DUPLICATE,
                    "The given range is not empty",
                    ExceptionHelper.newObjectIdParameter("id", core),
                    new BasicException.Parameter("validFrom", validFrom),
                    new BasicException.Parameter("validTo", validTo)
                );
            }
        }
        T range = targetManager.newInstance(stateClass);
        range.setCore(core);
        return range;
    }
        
    /**
     * Retrieve a view for a given period.
     * <p>
     * The attributes are readable and writable.
     * 
     * @param source the underlying date state view
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
        if(source == null) return null;
        PersistenceManager persistenceManager = DateStateViews.getPersistenceManager(
            source,
            validFrom,
            validTo
        );
        T range = (T) persistenceManager.getObjectById(
            JDOHelper.getTransactionalObjectId(source)
        );
        if(range != null && !JDOHelper.isDeleted(range)) {
            if(override) {
                range.refDelete();
            } else {
                throw new RuntimeServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.DUPLICATE,
                    "The given range is not empty",
                    ExceptionHelper.newObjectIdParameter("xri", source),
                    new BasicException.Parameter("validFrom", validFrom),
                    new BasicException.Parameter("validTo", validTo),
                    new BasicException.Parameter("override", override)
                );
            }
        }
        range = (T) persistenceManager.newInstance(
            source.getClass().getInterfaces()[0]
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
        if(source == null) return null;
        if(JDOHelper.isDeleted(source)) throw new IllegalArgumentException(
            "The source must not be deleted"
        );
        //
        // Compare source and view validity
        //
        PersistenceManager persistenceManager = DateStateViews.getPersistenceManager(
            source,
            validFrom,
            validTo
        ); 
        T target = (T) persistenceManager.getObjectById(
            JDOHelper.getTransactionalObjectId(source)
        );
        final int lowerFlag = Order.compareValidFrom(
            source.getStateValidFrom(),
            validFrom
        ); 
        final int upperFlag = Order.compareValidTo(
            source.getStateValidTo(),
            validTo
        );
        if(
            (lowerFlag != 0 || upperFlag != 0) && 
            (lowerFlag > 0 || upperFlag < 0 || (Parameters.STRICT_QUERY && !JDOHelper.isNew(source)))
        ){
            if(override) {
                DateState state = (DateState) persistenceManager.getObjectById(
                    DateStateViews.getResourceIdentifierOfClone(source)
                );
                state.setStateValidFrom(validFrom);
                state.setStateValidTo(validTo);
                target.refDelete();
                state.setCore((AspectCapable) source);
            } else {
                for(org.openmdx.state2.jmi1.DateState state : DateStateViews.getValidStates((StateCapable)source, validFrom, validTo)){
                    if(source != state) throw new JmiServiceException(
                        new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.DUPLICATE,
                            "There is another valid state in the given period",
                            ExceptionHelper.newObjectIdParameter("xri", source),
                            new BasicException.Parameter("override", Boolean.FALSE),
                            new BasicException.Parameter("viewContext", DateStateViews.getContext(target)),
                            new BasicException.Parameter("stateContext", DateStateViews.getContext(state))
                        )
                    );
                }
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
        return DateStateViews.getViewForPropagatedState(
            dateStateView,
            validFrom,
            validTo,
            true
        );
    }

    /**
     * Retrieve states by their core reference 
     * 
     * @param refContainer
     * @param resourceIdentifier
     * 
     * @return the states belonging to a core object
     */
    @SuppressWarnings("unchecked")
    private static <T extends DateState> List<T> getRawStates(
        RefContainer refContainer,
        Object resourceIdentifier
    ){
        return refContainer.refGetAll(
            new Filter(
                new IsInstanceOfCondition(
                    "org:openmdx:state2:DateState"
                ),
                new IsInCondition(
                    Quantifier.THERE_EXISTS,
                    "core",
                    true,
                    resourceIdentifier
                )
            )
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
        Container<? super T> referenceCollection,
        String qualifier,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        Boolean invalidated
    ){
        Path containerId = PersistenceHelper.getContainerId(referenceCollection);
        RefContainer contextFreeContainer = (RefContainer<?>) DateStateViews.getPersistenceManager(
            ((RefContainer)referenceCollection),
            null
        ).getObjectById(
            PersistenceHelper.getTransientContainerId(referenceCollection)
        );
        return new FilteredStates<T>(
            DateStateViews.<T>getRawStates(
                contextFreeContainer,
                containerId == null ? JDOHelper.getTransactionalObjectId(
                    qualifier.startsWith("!") ? contextFreeContainer.refGet(
                        QualifierType.PERSISTENT,
                        qualifier.substring(1)
                    ) : contextFreeContainer.refGet(
                        QualifierType.REASSIGNABLE,
                        qualifier
                    ) 
                ) : containerId.getChild(
                    qualifier
                )
            ),
            validFrom,
            validTo,
            invalidated,
            null, 
            AccessMode.FOR_QUERY
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
        Container<? super T> referenceCollection,
        String qualifier,
        Boolean invalidated
    ){
        return DateStateViews.getStates(
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
        Container<? super T> referenceCollection,
        String qualifier,
        boolean includeValidStates,
        boolean includeInvalidStates
    ){
        return includeValidStates || includeInvalidStates ? DateStateViews.<T>getStates(
            referenceCollection,
            qualifier,
            includeInvalidStates && includeValidStates ? null : Boolean.valueOf(includeInvalidStates)
        ): Collections.<T>emptyList(
        );
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
    public static <T extends DateState> List<T> getValidStates(
        Container<? super T> referenceCollection,
        String qualifier,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        return DateStateViews.getStates(
            referenceCollection,
            qualifier,
            validFrom,
            validTo,
            Boolean.FALSE
        );
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
     * @deprecated Use {@link #getValidStates(Container<? super T>,String,XMLGregorianCalendar,XMLGregorianCalendar)} instead
     */
    @Deprecated
    public static <T extends DateState> List<T> getStates(
        Container<? super T> referenceCollection,
        String qualifier,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        return getValidStates(
            referenceCollection,
            qualifier,
            validFrom,
            validTo);
    }

    /**
     * Retrieve all valid states
     * 
     * @param referenceCollection the result of the parents getXXX() method
     * @param qualifier the object's qualifier
     * 
     * @return a list of DateState instances
     */
    public static <T extends DateState> List<T> getValidStates(
        Container<? super T> referenceCollection,
        String qualifier
    ){
        return DateStateViews.getStates(
            referenceCollection,
            qualifier,
            null, // validFrom
            null, // validTo
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
     * @deprecated Use {@link #getValidStates(Container<? super T>,String)} instead
     */
    @Deprecated
    public static <T extends DateState> List<T> getStates(
        Container<? super T> referenceCollection,
        String qualifier
    ){
        return getValidStates(referenceCollection, qualifier);
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
     * @param mode 
     * @return a collection of DateState instances
     */
    private static <T extends DateState> List<T> getStates(
        StateCapable dateState,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        Boolean invalidated, 
        Date existsAt, 
        AccessMode mode
    ){
        Path resourceIdentifier = (Path) JDOHelper.getObjectId(dateState);
        return new FilteredStates<T>(
            DateStateViews.<T>getRawStates(
                (RefContainer<?>) DateStateViews.getPackageForContext(
                    dateState,
                    null
                ).refContainer(
                    resourceIdentifier.getParent(),
                    null // containerClass
                ),
                resourceIdentifier
            ),
            validFrom,
            validTo,
            invalidated,
            existsAt, 
            mode
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
    public static <T extends DateState> List<T> getStates(
        StateCapable dateState,
        Boolean invalidated
    ){
        return dateState == null ? null : DateStateViews.<T>getStates(
            dateState,
            null, // validFrom
            null, // validTo
            invalidated,
            null, 
            AccessMode.FOR_QUERY
        );
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
    public static <T extends DateState> Collection<T> getStates(
        StateCapable dateState,
        boolean includeValidStates,
        boolean includeInvalidStates
    ){
        return includeValidStates || includeInvalidStates ? Collections.<T>emptySet(
        ) : DateStateViews.<T>getStates(
            dateState,
            includeInvalidStates && includeValidStates ? null : Boolean.valueOf(includeInvalidStates)
        );
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
    public static <T extends DateState> List<T> getValidStates(
        StateCapable dateState,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        return dateState == null ? null : DateStateViews.<T>getStates(
            dateState,
            validFrom,
            validTo,
            Boolean.FALSE, // invalidated
            null, // existsAt
            AccessMode.FOR_QUERY
        );
    }    
    
    /**
     * Retrieve valid states 
     * 
     * @param dateState a plain JMI object or a date state view
     * @param validFrom include all states ending at validFrom or later
     * @param validTo include all states beginning at validTo or earlier
     * 
     * @return a list of DateState instances
     * @deprecated Use {@link #getValidStates(StateCapable,XMLGregorianCalendar,XMLGregorianCalendar)} instead
     */
    @Deprecated
    public static <T extends DateState> List<T> getStates(
        StateCapable dateState,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        return getValidStates(dateState, validFrom, validTo);
    }

    /**
     * Retrieve valid states
     * 
     * @param dateState a plain JMI object or a date state view
     * 
     * @return a list of DateState instances
     */
    public static <T extends DateState> List<T> getValidStates(
        StateCapable dateState
    ){
        return dateState == null ? null : DateStateViews.<T>getValidStates(
            dateState,
            (XMLGregorianCalendar)null,
            (XMLGregorianCalendar)null
        );
    }

    /**
     * Retrieve valid states
     * 
     * @param dateState a plain JMI object or a date state view
     * 
     * @return a list of DateState instances
     * 
     * @deprecated Use {@link #getValidStates(StateCapable)} instead
     */
    @Deprecated
    public static <T extends DateState> List<T> getStates(
        StateCapable dateState
    ){
        
        return DateStateViews.getValidStates(dateState);
    }
    
    /**
     * Retrieve states involved in the current view
     * 
     * @param dateState a plain JMI object or a date state view
     * 
     * @return a list of DateState instances
     */
    @SuppressWarnings("unchecked")
    public static <T extends DateState> List<T> getStatesInvolvedInView(
        StateCapable dateState
    ){
        DateStateContext context = DateStateViews.getContext(dateState);
        return 
            context == null ? DateStateViews.<T>getStates(dateState) :
            context.getViewKind() == ViewKind.TIME_POINT_VIEW ? Collections.<T>singletonList((T)dateState) :
            DateStateViews.<T>getValidStates(dateState, context.getValidFrom(), context.getValidTo()); 
    }
    
    
    //------------------------------------------------------------------------
    // Date State Context
    //------------------------------------------------------------------------
    
    /**
     * Retrieve the context for date state views.
     * 
     * @param jmiContext a plain JMI object or a date state view
     * 
     * @return the <code>DateStateContext</code> in case of a 
     * Date State View, <code>null</code> otherwise.
     */
    public static DateStateContext getContext(
        RefBaseObject jmiContext
    ){
        if(jmiContext != null) {
            RefPackage refPackage = jmiContext.refOutermostPackage();
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
     * Retrieve a JMI context's view kind
     * 
     * @param jmiContext a JMI context
     * 
     * @return the view kind, or <code>null</code> if the view context is <code>null</code> 
     */
    public static ViewKind getViewKind(
        RefBaseObject jmiContext
    ){
        DateStateContext viewContext = DateStateViews.getContext(jmiContext);
        return viewContext == null ? null : viewContext.getViewKind();
    }
    
    /**
     * Tells whether the object is a validity aware view
     * 
     * @param refObject a plain JMI object or a date state view
     * 
     * @deprecated use {@link DateStateViews#getViewKind(RefBaseObject)} != null instead
     */
    @Deprecated
    public static boolean isView(
        RefObject refObject
    ){
        return DateStateViews.getContext(refObject) != null;
    }

    /**
     * Tests whether a given object is readable
     * 
     * @param refObject a plain JMI object or a date state view
     * 
     * @return <code>true</code> unless one of the following conditions is met<ul>
     * <li><code>refObject</code> is <code>null</code> 
     * <li><code>refObject</code> is deleted 
     * <li>all if the following conditions are <code>true</code><ol>
     * <li><code>refObject instanceof DateState</code>
     * <li>getViewKind(refObject) == ViewKind.TIME_RANGE_VIEW</code>
     * <li>the view has either been disturbed through write operations to other 
     * views or it has not been acquired through one of the following methods<ul> 
     * <li><code>newInstance()</code> or <code>create&hellip;()</code>
     * <li><code>getStates()</code>
     * <li><code>getValidStates()</code>
     * <li><code>getViewForInitializedState()</code>
     * <li><code>getViewForPropagatedState()</code>
     * </ul>
     * </ol>
     * </ul>
     */
    public static boolean isReadable(
        RefObject refObject
    ){
        return
            refObject != null &&
            !JDOHelper.isDeleted(refObject) && (
                !(refObject instanceof DateState) ||
                DateStateViews.getViewKind(refObject) != ViewKind.TIME_RANGE_VIEW || 
                DateStateViews.getStatesInvolvedInView((StateCapable)refObject).size() == 1 
            );
    }
    
    /**
     * Clone an object 
     * 
     * @param refObject view to the object to be cloned
     *
     * @return the clone's transient resource identifier
     */
    static UUID getResourceIdentifierOfClone(
        DateState refObject
    ){
        if(refObject == null || JDOHelper.isDeleted(refObject)) throw BasicException.initHolder(
            new IllegalArgumentException(
                "The object to be cloned must be neither null nor deleted",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    ExceptionHelper.newObjectIdParameter("xri", refObject)
                )
            )
        );
        DateState state;
        DateStateContext context = DateStateViews.getContext(refObject);
        if(context == null) {
            state = refObject;
        } else {
            List<DateState> involved = null;
            switch(context.getViewKind()) {
                case TIME_POINT_VIEW:
                    involved = DateStateViews.getStates(
                        (StateCapable) refObject, 
                        context.getValidAt(), 
                        context.getValidAt(),
                        null, // invalidated
                        context.getExistsAt(), 
                        AccessMode.RAW
                    );
                    break;
                case TIME_RANGE_VIEW: 
                    involved = DateStateViews.getStates(
                        (StateCapable) refObject, 
                        context.getValidFrom(), 
                        context.getValidTo(),
                        Boolean.FALSE, // invalidated
                        null, // existsAt 
                        AccessMode.RAW
                    );
                    break;
            }
            int cardinality = involved.size(); 
            if(cardinality != 1) throw BasicException.initHolder(
                new IllegalArgumentException(
                    "A view can be cloned only if there is exactly one underlying state",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CARDINALITY,
                        ExceptionHelper.newObjectIdParameter("xri", refObject),
                        new BasicException.Parameter("states", cardinality)
                    )
                )
            );
            state = involved.get(0);
        }
        DateState clone = PersistenceHelper.clone(state);
        clone.setCore(null);
        return (UUID) JDOHelper.getTransactionalObjectId(clone);
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
    private static List<Condition> getAmendment(
        DateStateContext stateContext
    ){
        if(stateContext == null) {
            return null;
        }
        switch(stateContext.getViewKind()) {
            case TIME_RANGE_VIEW:
                return Arrays.asList(
                    new IsGreaterCondition(
                        Quantifier.FOR_ALL,
                        "stateValidFrom",
                        false, // IS_LESS_OR_EQUAL,
                        stateContext.getValidTo()
                    ),
                    new IsGreaterOrEqualCondition(
                        Quantifier.FOR_ALL,
                        "stateValidTo",
                        true, // IS_GREATER_OR_EQUAL,
                        stateContext.getValidFrom()
                    ),
                    new IsInCondition(
                        Quantifier.FOR_ALL,
                        SystemAttributes.REMOVED_AT,
                        true // ConditionType
                    )
                );
            case TIME_POINT_VIEW:
                return stateContext.getExistsAt() == null ? Arrays.asList(
                    new IsGreaterCondition(
                        Quantifier.FOR_ALL,
                        "stateValidFrom",
                        false, // IS_LESS_OR_EQUAL
                        stateContext.getValidAt()
                    ),
                    new IsGreaterOrEqualCondition(
                        Quantifier.FOR_ALL,
                        "stateValidTo",
                        true, // IS_GREATER_OR_EQUAL,
                        stateContext.getValidAt()
                    ),
                    new IsInCondition(
                        Quantifier.FOR_ALL,
                        SystemAttributes.REMOVED_AT,
                        true // IS_IN
                    )
                ) : Arrays.asList(
                    new IsGreaterCondition(
                        Quantifier.FOR_ALL,
                        "stateValidFrom",
                        false, // IS_LESS_OR_EQUAL
                        stateContext.getValidAt()
                    ),
                    new IsGreaterOrEqualCondition(
                        Quantifier.FOR_ALL,
                        "stateValidTo",
                        true, // IS_GREATER_OR_EQUAL
                        stateContext.getValidAt()
                    ),
                    new IsGreaterCondition(
                        Quantifier.FOR_ALL,
                        SystemAttributes.CREATED_AT,
                        false, // IS_LESS_OR_EQUAL
                        stateContext.getExistsAt()
                    ),
                    new IsGreaterOrEqualCondition(
                        Quantifier.FOR_ALL,
                        SystemAttributes.REMOVED_AT,
                        true, // IS_GREATER_OR_EQUAL
                        stateContext.getExistsAt()
                    )
                );
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
            return DateStateViews.getViewForState(
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
    // Class AccessMode
    //------------------------------------------------------------------------

    /**
     * Access Mode
     */
    private static enum AccessMode {
        
        /**
         * Used internally only the raw access mode provides the states data
         * object  representation
         */
        RAW,
        
        /**
         * The validity of the boundary states may exceed the time range
         */
        FOR_QUERY,
        
        /**
         * The validity of the boundary states does not exceed the time range
         */
        FOR_UPDATE
        
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
         * @param mode
         */
        FilteredStates(
            Collection<E> dateStates,
            XMLGregorianCalendar validFrom,
            XMLGregorianCalendar validTo,
            Boolean invalidated,
            Date existsAt,
            AccessMode mode
        ) {
            this.dateStates = dateStates;
            this.validFrom = validFrom;
            this.validTo = validTo;
            this.invalidated = invalidated;
            this.existsAt = existsAt;
            this.mode = mode;
            if(Parameters.STRICT_QUERY && mode == AccessMode.FOR_UPDATE) {
                for(E raw : getDelegate()) {
                    if(!JDOHelper.isNew(raw)) {
                        boolean propagateState = false;
                        XMLGregorianCalendar stateValidFrom = raw.getStateValidFrom();
                        XMLGregorianCalendar stateValidTo = raw.getStateValidTo();
                        if(Order.compareValidFrom(this.validFrom, stateValidFrom) > 0) {
                            stateValidFrom = this.validFrom;
                            propagateState = true;
                        }
                        if(Order.compareValidTo(this.validTo, stateValidTo) < 0) {
                            stateValidTo = this.validTo;
                            propagateState = true;
                        }
                        if(propagateState) {
                            AspectCapable core = raw.getCore();
                            PersistenceManager viewManager = DateStateViews.getPersistenceManager(
                                raw,
                                stateValidFrom,
                                stateValidTo
                            );
                            RefObject target = (RefObject) viewManager.getObjectById(
                                JDOHelper.getTransactionalObjectId(core)
                            );
                            DateState state = (DateState) viewManager.getObjectById(
                                DateStateViews.getResourceIdentifierOfClone(raw)
                            );
                            state.setStateValidFrom(stateValidFrom);
                            state.setStateValidTo(stateValidTo);
                            target.refDelete();
                            state.setCore(core);
                        }
                    }
                }
            }
        }


        private final Collection<E> dateStates;
        private final XMLGregorianCalendar validFrom;
        private final XMLGregorianCalendar validTo;
        private final Boolean invalidated;
        private final Date existsAt;
        private final AccessMode mode;
        
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
         * Retrieve the delegate collection
         * 
         * @return the delegate collection
         */
        private Collection<E> getDelegate(){
            SortedSet<E> set = new TreeSet<E>(FilteredStates.stateComparator);
            for(E state : this.dateStates) {
                if(
                    (this.invalidated == null || this.invalidated.booleanValue() == (state.getRemovedAt() != null)) &&
                    Order.compareValidFromToValidTo(this.validFrom, state.getStateValidTo()) <= 0 &&                     
                    Order.compareValidFromToValidTo(state.getStateValidFrom(), this.validTo) <= 0 && (
                        this.mode != AccessMode.RAW || (
                            this.existsAt == null ? (
                                state.getRemovedAt() == null
                            ) : (
                                this.existsAt.compareTo(state.getCreatedAt()) >= 0 &&
                                Order.compareRemovedAt(this.existsAt, state.getRemovedAt()) < 0
                            )
                        )
                    )
                ) {
                    set.add(state);
                }
            }
            return set;
        }
        
        
        /* (non-Javadoc)
         * @see java.util.AbstractSequentialList#listIterator(int)
         */
        @Override
        public ListIterator<E> listIterator(int index) {
            List<E> list = new ArrayList<E>();
            for(E raw : this.getDelegate()) {
                switch(this.mode){
                    case RAW:
                        list.add(raw);
                        break;
                    case FOR_QUERY:
                        list.add(DateStateViews.getViewForState(raw));
                        break;
                    case FOR_UPDATE:
                        XMLGregorianCalendar validFrom = raw.getStateValidFrom();
                        XMLGregorianCalendar validTo = raw.getStateValidTo();
                        list.add(
                            DateStateViews.<E,E>getViewForTimeRange(
                                raw,
                                Order.compareValidFrom(this.validFrom, validFrom) > 0 ? this.validFrom : validFrom,
                                Order.compareValidTo(this.validTo, validTo) < 0 ? this.validTo : validTo
                            )
                        );
                        break;
                }
            }
            return list.listIterator(index);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return this.getDelegate().size();
        }


        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return this.getDelegate().isEmpty();
        }
        
    }    
    
}
