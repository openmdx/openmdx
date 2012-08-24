/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Date State Views
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2011, OMEX AG, Switzerland
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

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
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.accessor.jmi.spi.DelegatingRefObject_1_0;
import org.openmdx.base.accessor.spi.ExceptionHelper;
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
import org.openmdx.kernel.log.SysLog;
import org.openmdx.state2.jmi1.BasicState;
import org.openmdx.state2.jmi1.DateState;
import org.openmdx.state2.jmi1.Legacy;
import org.openmdx.state2.jmi1.StateCapable;
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
    protected DateStateViews(
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
    
    private static final String[] EXCLUDE_FROM_STATE_CLONING = {
    	"identity", "core", "stateValidFrom", "stateValidTo", "removedAt", "removedBy", "createdAt", "createdBy"
    };
    private static final String[] EXCLUDE_FROM_CORE_CLONING = {
    	"identity", "stateVersion", "modifiedAt", "modifiedBy"
    };
    
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
     * 
     * @throws IllegalArgumentException if validTo is less than validFrom 
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
     * Validates a container
     * 
     * @param container the container to be validated
     * 
     * @return the RefContainer
     * 
     * @throws IllegalArgumentException if the container is not an instance of RefContainer<?>
     */
    @SuppressWarnings("unchecked")
	private static <T extends RefObject> RefContainer<T> asRefContainer(
    	Container<?> container
    ){
    	if(container instanceof RefContainer<?>) {
    		return (RefContainer<T>) container;
    	} else if(container == null) {
	        throw new IllegalArgumentException("The container must not be null");
    	} else {
	        throw BasicException.initHolder(
                new IllegalArgumentException(
                    "The container should be an instance of " + RefBaseObject.class.getName(),
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("class", container.getClass().getName())
                    )
                )
            );
    	}    	
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
     * 
     * @throws IllegalArgumentException if validTo is less than validFrom 
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
     * 
     * @throws IllegalArgumentException if one of the arguments is null or if the
     * core object is not context free
     */
    @SuppressWarnings("unchecked")
    public static <T extends org.openmdx.state2.jmi1.StateCapable>  void addCoreToContainer(
        Container<? super T> container,
        String qualifier,
        T core
    ){
    	if(core == null) {
            new IllegalArgumentException("The core object must not be null");
    	}
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
        RefContainer<? super T> refContainer = asRefContainer(container);
        if(DateStateViews.getContext(refContainer) != null) {
            refContainer = (RefContainer<? super T>) DateStateViews.getPersistenceManager(refContainer, null).getObjectById(
                JDOHelper.getTransactionalObjectId(refContainer)
            );
        }
        refContainer.refAdd(
            REASSIGNABLE,
            qualifier,
            core
        );
    }

    /**
     * Link core and state
     * 
     * @param state
     * @param core
     */
    public static void linkStateAndCore(
    	DateState state,
    	StateCapable core
    ){
    	state.setCore(core);
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
     * @throws InvalidArgumentException 
     *      if validTo is less than validFrom or
     *      coreClass.getClass().isAssignableFrom(stateClass) is false
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
        linkStateAndCore(state, core);
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
     * @throws InvalidArgumentException 
     *      if validTo is less than validFrom or
     *      coreClass.getClass().isAssignableFrom(stateClass) is false
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
     * Retrieve the valid state predicate
     * 
     * @param refContainer
     * @param predicate
     * 
     * @return the valid state predicate
     */
    private static AnyTypePredicate getValidStatePredicate(
        AnyTypePredicate predicate
    ){
    	Filter newFilter;
        if(predicate == null) {
        	newFilter = new Filter(
        		new IsInstanceOfCondition("org:openmdx:state2:DateState")
        	);
        } else {
            Filter original = ((RefQuery_1_0)predicate).refGetFilter();
            newFilter = new Filter(
                original.getCondition(),
                original.getOrderSpecifier(),
                null // extension
            );
        }
        newFilter.getCondition().add(
        	new IsInCondition(
        		Quantifier.FOR_ALL,
        		SystemAttributes.REMOVED_AT,
        		true
        	)
        );
    	return newFilter;
    }
    
    @SuppressWarnings("unchecked")
    private static <T extends DateState> Container<T> getTimeIndependentContainer(
		RefContainer<? super T> refContainer
    ){
        return (Container<T>)DateStateViews.getPersistenceManager(
            refContainer,
            null
        ).getObjectById(
            JDOHelper.getTransactionalObjectId(refContainer)
        );
    }
   
    /**
     * Retrieve the states selected by the given predicate
     * 
     * @param container
     * @param predicate
     * 
     * @return the states selected by the given predicate
     */
    public static <T extends DateState> List<T> getStates(
        Container<? super T> container,
        AnyTypePredicate predicate
    ){
        RefContainer<? super T> refContainer = (RefContainer<? super T>)container; 
        Container<T> coreContainer = getTimeIndependentContainer(refContainer); 
        return new StateList<T>(
    		coreContainer.getAll(
                getStatePredicate(refContainer,predicate)
            )
        );
    }

    /**
     * Retrieve the valid states selected by the given predicate
     * 
     * @param container
     * @param predicate
     * 
     * @return the states selected by the given predicate
     */
    public static <T extends DateState> List<T> getValidStates(
        Container<? super T> container,
        AnyTypePredicate predicate
    ){
        RefContainer<? super T> refContainer = (RefContainer<? super T>)container; 
        Container<T> coreContainer = getTimeIndependentContainer(refContainer); 
        return new StateList<T>(
        		coreContainer.getAll(
                getValidStatePredicate(predicate)
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
        	getCore(state); // heal if necessary
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
     * 
     * @exception NullPointerException if refObject is <code>null</code>
     */
    @SuppressWarnings("unchecked")
    public static <C extends StateCapable, S extends C> C getViewForCore(
        S refObject
    ){
    	if(getContext(refObject) == null){
    		if(refObject instanceof BasicState) {
    			return (C) ((BasicState)refObject).getCore();
    		} else {
    			return refObject;
    		}
    	} else {
            return (C) DateStateViews.getPersistenceManager(
                refObject,
                null
            ).getObjectById(
                JDOHelper.getTransactionalObjectId(refObject)
            );
    	}
    }

    /**
     * Retrieve a context-free view onto the given object or container
     * 
     * @param refBaseObject the object or container for which a context free view is requested
     * 
     * @return a context-free view onto the given object or container
     */
    @SuppressWarnings("unchecked")
    public static <C extends RefBaseObject> C getTimeIndependentView(
        RefBaseObject refBaseObject
    ){
        return (C) (
            getContext(refBaseObject) == null ? refBaseObject : DateStateViews.getPersistenceManager(
                refBaseObject,
                null
            ).getObjectById(
                JDOHelper.getTransactionalObjectId(refBaseObject)
            )
        );
    }
    
    private static boolean equals(
        XMLGregorianCalendar left,
        XMLGregorianCalendar right
    ){
        if(left == right) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        if (left instanceof ImmutableDatatype<?> != right instanceof ImmutableDatatype<?>) {
            ImmutableDatatypeFactory datatypeFactory = DatatypeFactories.immutableDatatypeFactory();
            return datatypeFactory.toDate(left).equals(datatypeFactory.toDate(right));
        }
        return left.equals(right);
    }

    private static boolean equals(
        Date left,
        Date right
    ){
        if(left == right) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        if (left instanceof ImmutableDatatype<?> != right instanceof ImmutableDatatype<?>) {
            ImmutableDatatypeFactory datatypeFactory = DatatypeFactories.immutableDatatypeFactory();
            return datatypeFactory.toDateTime(left).equals(datatypeFactory.toDateTime(right));
        }
        return left.equals(right);
    }

    /**
     * Retrieve a state's core
     * 
     * @param state
     * 
     * @return a state's core
     */
    private static RefObject getCore(
    	BasicState state
    ){
    	RefObject core = state.getCore();
        if(core == null) {
            if(state instanceof Legacy && ((Legacy)state).isValidTimeUnique()) {
                return state;
            }
        	if(JDOHelper.isDirty(state) && !JDOHelper.isTransactional(state)) {
        		SysLog.log(
        			Level.FINE, 
        			"The object {0} with XRI {1} is in state {2}. " +
        			"Going to make it transactional and to retry", 
        			new Object[]{
        				JDOHelper.getTransactionalObjectId(state), 
        				state.refMofId(), 
        				JDOHelper.getObjectState(state)
        			}
        		);
        		JDOHelper.getPersistenceManager(state).makeTransactional(state);
                core = state.getCore();
                if(core == null) {
            		SysLog.log(
            			Level.FINE, 
            			"The object {0} with XRI {1} is now in state {2}. " +
            			"Its core is still unavailable. " +
            			"Last resort is going to return null.", 
            			new Object[]{
            				JDOHelper.getTransactionalObjectId(state), 
            				state.refMofId(), 
            				JDOHelper.getObjectState(state)
            			}
            		);
                } else {
            		SysLog.log(
            			Level.FINE, 
            			"The object {0} with XRI {1} is now in state {2}. Its core is {3} with XRI {4}.", 
            			new Object[]{
            				JDOHelper.getTransactionalObjectId(state), 
            				state.refMofId(), 
            				JDOHelper.getObjectState(state),
            				JDOHelper.getTransactionalObjectId(core), 
            				core.refMofId()
            			}
            		);
                }
        	} else {
        		SysLog.log(
        			Level.FINE, 
        			"The object {0} with XRI {1} is in state {2}. " +
        			"Its core is unavailable. " +
        			"Last resort is going to return null.", 
        			new Object[]{
        				JDOHelper.getTransactionalObjectId(state), 
        				state.refMofId(), 
        				JDOHelper.getObjectState(state)
        			}
        		);
        	}
        }
    	return core;
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
                refObject = getCore((BasicState)object);
            }
        } else if(
            context.getViewKind() == ViewKind.TIME_POINT_VIEW &&
            DateStateViews.equals(validAt, context.getExistsAt()) &&
            DateStateViews.equals(validFor, context.getValidAt())
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
     * @param container a plain JMI container or a date view of a container
     * @param validFor exclude all states not valid at the given date,
     * use <code>today()</code> in case of <code>null</code>
     * @param validAt exclude all states created after the given time point
     * 
     * @return a view to the given container
     * 
     * @exception IllegalArgumentException if the <code>container</code> is neither <code>null</code>
     * nor an instance of <code>RefBaseObject</code>
     */
    @SuppressWarnings("unchecked")
	public static <T extends Container<?>> T getViewForTimePoint(
    	T container,
        XMLGregorianCalendar validFor,
        Date validAt
    ){
        RefContainer<?> source = asRefContainer(container);
        DateStateContext context = DateStateViews.getContext(source);
        if(
            context != null &&
            context.getViewKind() == ViewKind.TIME_POINT_VIEW &&
            DateStateViews.equals(validAt, context.getExistsAt()) &&
            DateStateViews.equals(validFor, context.getValidAt())
        ){
        	return (T) source;
        } else {
        	return (T) DateStateViews.getPersistenceManager( 
                source,
                validFor,
                validAt
            ).getObjectById(
                JDOHelper.getTransactionalObjectId(source)
            );
        }
    }
    

    /**
     * 
     * @param <T>
     * @param refContext
     * @param refPackageClass
     * @param validFrom
     * @param validTo
     * @return
     * 
     * @throws IllegalArgumentException if validTo is less than validFrom 
     */
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
     * getViewForTimeRange
     * @throws IllegalArgumentException if validTo is less than validFrom 
     * 
     * @deprecated use {@link #getViewForTimeRange(Container, 
     * XMLGregorianCalendar, XMLGregorianCalendar)}.get(QualifierType.REASSIGNABLE, String)
     */
    @SuppressWarnings("unchecked")
    @Deprecated
	public static <T extends RefObject> T getViewForTimeRange(
        Collection<? extends DateState> referenceCollection,
        String qualifier,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
    	Container<? super T> container = (Container<? super T>) referenceCollection;
		RefContainer<T> refContainer = (RefContainer<T>) DateStateViews.getPersistenceManager(
			asRefContainer(container), 
		    validFrom,
		    validTo
		).getObjectById(
		    JDOHelper.getTransactionalObjectId(container)
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
     * or validTo is less than validFrom 
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
               object instanceof BasicState ? getCore((BasicState)object) : object
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
     * @exception IllegalArgumentException if the <code>container</code> is neither <code>null</code>
     * nor an instance of <code>RefBaseObject</code> or if validTo is less than validFrom. 
     */
    @SuppressWarnings("unchecked")
    public static <T extends Container<?>> T getViewForTimeRange(
        T container,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        RefBaseObject refContainer = (RefBaseObject) container;
        DateStateContext context = DateStateViews.getContext(refContainer);
        if(
            context != null &&
            context.getViewKind() == ViewKind.TIME_RANGE_VIEW &&
            DateStateViews.equals(validFrom, context.getValidFrom()) &&
            DateStateViews.equals(validTo, context.getValidTo())
        ){
            return container;
        } else {
            return (T) DateStateViews.getPersistenceManager( 
                refContainer,
                validFrom,
                validTo
            ).getObjectById(
                JDOHelper.getTransactionalObjectId(refContainer)
            );
        }        
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
     * @param stateCapable a plain JMI object or a date state view
     * 
     * @return a view covering the whole period
     * 
     * @exception IllegalArgumentException if the <code>dateState</code> does not support views
     */
    @SuppressWarnings("unchecked")
    public static <T extends DateState> T getViewForLifeTime(
        StateCapable stateCapable
    ){
        if(stateCapable == null) return null;
        List<? extends DateState> states = DateStateViews.getValidStates(stateCapable);
        if(states.isEmpty()) return null;
        return (T) DateStateViews.<StateCapable,StateCapable>getViewForTimeRange(
            stateCapable, 
            states.get(0).getStateValidFrom(), 
            states.get(states.size()-1).getStateValidTo()
        );
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
     * 
     * @throws IllegalArgumentException if override is false and the given range is not empty
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
                throw BasicException.initHolder(
                    new IllegalArgumentException(
                		"The given range is not empty",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.DUPLICATE,
                            ExceptionHelper.newObjectIdParameter("xri", core),
                            new BasicException.Parameter("validFrom", validFrom),
                            new BasicException.Parameter("validTo", validTo),
                            new BasicException.Parameter("override", override)
                        )
                    )
                );
            }
        }
        T range = targetManager.newInstance(stateClass);
        linkStateAndCore(range, core);
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
     * 
     * @throws IllegalArgumentException if override is false and the given range is not empty
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
                throw BasicException.initHolder(
                    new IllegalArgumentException(
                		"The given range is not empty",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.DUPLICATE,
                            ExceptionHelper.newObjectIdParameter("xri", source),
                            new BasicException.Parameter("validFrom", validFrom),
                            new BasicException.Parameter("validTo", validTo),
                            new BasicException.Parameter("override", override)
                        )
                    )
                );
            }
        }
        range = (T) persistenceManager.newInstance(
            source.getClass().getInterfaces()[0]
        ); 
        linkStateAndCore(range, (StateCapable) source);
        return range;
    }
    
    /**
     * This method fills the holes in the given time-range but leaves existing 
     * states before returning a view for the given time-range.
     * 
     * @param stateClass
     * @param core
     * @param validFrom
     * @param validTo
     * 
     * @return a view for the requested time range.
     */
    public static <T extends DateState> T getViewForTimeRangeWithoutHoles(
        Class<T> stateClass,
        StateCapable core,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        XMLGregorianCalendar nextFrom = validFrom;
        States: for(DateState state : DateStateViews.getValidStates(core, validFrom, validTo)) {
            XMLGregorianCalendar nextTo = state.getStateValidFrom();
            if(Order.compareValidFrom(nextFrom, nextTo) < 0) {
                DateStateViews.createState(core, nextFrom, Order.predecessor(nextTo), stateClass);
            }
            nextFrom = state.getStateValidTo();
            if(nextFrom == null) break States;
            nextFrom = Order.successor(nextFrom);
        }
        if(nextFrom != null && Order.compareValidTo(nextFrom, validTo) <= 0) {
            DateStateViews.createState(core, nextFrom, validTo, stateClass);
        }
        return stateClass.cast(
        	DateStateViews.<StateCapable,StateCapable>getViewForTimeRange(core, validFrom, validTo)
        );
    }
    

    /**
     * Determine whether the propagation is idempotent or not
     * 
     * @param source
     * @param validFrom
     * @param validTo
     * 
     * @return <code>true</code> if the propagation is idempotent
     */
    private static boolean isPropagationIdempotent(
        DateState source,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ) {
        DateStateContext viewContext = DateStateViews.getContext(source);
        if(viewContext.getViewKind() == ViewKind.TIME_POINT_VIEW && viewContext.getExistsAt() != null) {
            return false;
        } else {
            int lowerFlag = Order.compareValidFrom(
                source.getStateValidFrom(),
                validFrom
            ); 
            int upperFlag = Order.compareValidTo(
                source.getStateValidTo(),
                validTo
            );
            return lowerFlag <= 0 && upperFlag >= 0;
        }
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
     * 
     * @throws IllegalArgumentException if source is deleted or if override is 
     * false and the given range is occupied by another state
     */
    @SuppressWarnings("unchecked")
    public static <T extends DateState> T getViewForPropagatedState(
        T source,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        boolean override
    ){
        if(source == null) return null;
        if(JDOHelper.isDeleted(source)) {
            throw new IllegalArgumentException(
                "The source must not be deleted"
            );
        }
        //
        // Compare source and view validity
        //
        PersistenceManager targetManager = getPersistenceManager(
            source,
            validFrom,
            validTo
        ); 
        T target = (T) targetManager.getObjectById(
            JDOHelper.getTransactionalObjectId(source)
        );
        if(!isPropagationIdempotent(source, validFrom, validTo)){
            if(override || target == null) {
                T state = (T) targetManager.getObjectById(
                    DateStateViews.getResourceIdentifierOfClone(source, DateStateViews.EXCLUDE_FROM_STATE_CLONING)
                );
                state.setStateValidFrom(validFrom);
                state.setStateValidTo(validTo);
                if(target == null) {
                    target = state;
                } else {
                    target.refDelete();
                }
                linkStateAndCore(state, (StateCapable) source);
            } else {
                signalInterference(source, target);
            }
        }
        return target;            
    }

    /**
     * Validate that the requested space is not blocked by another state
     * 
     * @param source
     * @param target
     * 
     * @throws IllegalArgumentException if the given range is occupied by another state
     */
    private static void signalInterference(
        DateState source,
        DateState target
    ) {
        DateStateContext sourceContext = DateStateViews.getContext(source);
        DateState compatibleState = null;
        switch(sourceContext.getViewKind()) {
            case TIME_RANGE_VIEW:
                compatibleState = source;
                break;
            case TIME_POINT_VIEW:
                if(sourceContext.getExistsAt() == null) {
                    compatibleState = DateStateViews.<DateState,DateState>getViewForTimeRange(
                        source, 
                        source.getStateValidFrom(), 
                        source.getStateValidTo()
                    );
                }
                break;
        }
        DateStateContext targetContext = DateStateViews.getContext(target);
        for(DateState state : DateStateViews.<DateState>getValidStates((StateCapable)source, targetContext.getValidFrom(), targetContext.getValidTo())){
            if(state != compatibleState) {
                throw BasicException.initHolder(
                    new IllegalArgumentException(
                        "There is another valid state in the given period",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.DUPLICATE,
                            ExceptionHelper.newObjectIdParameter("xri", source),
                            new BasicException.Parameter("override", Boolean.FALSE),
                            new BasicException.Parameter("viewContext", targetContext),
                            new BasicException.Parameter("stateContext", DateStateViews.getContext(state))
                        )
                    )
                );                      
            }
        }
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
     * Clones a state
     * <p>
     * The attributes are readable and writable.
     * 
     * @param source the underlying date state view
     * @param validFrom begin of the overridden period 
     * @param validTo end of the overridden period
     * 
     * @return a view for the clone
     */
    @SuppressWarnings("unchecked")
    public static <T extends DateState> T getViewForClonedState(
        T source,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        T state = (T) DateStateViews.getPersistenceManager(
            source,
            validFrom,
            validTo
        ).getObjectById(
            DateStateViews.getResourceIdentifierOfClone(source, DateStateViews.EXCLUDE_FROM_STATE_CLONING)
        );
        state.setStateValidFrom(validFrom);
        state.setStateValidTo(validTo);
        return state;
    }

    /**
     * Clones a state
     * <p>
     * The attributes are readable and writable.
     * 
     * @param source the underlying date state view
     * 
     * @return a view for the clone
     */
    public static <T extends DateState> T getViewForClonedState(
        T source
    ){
    	return getViewForClonedState(
    		source,
    		source.getStateValidFrom(),
    		source.getStateValidTo()
    	);
    }

    /**
     * Clones a core object
     * <p>
     * The attributes are readable and writable.
     * 
     * @param source the underlying 
     * 
     * @return a view for the clone
     */
    public static <T extends StateCapable> T getViewForClonedCore(
        T source
    ){
    	return PersistenceHelper.clone(
        	DateStateViews.<T, T>getViewForCore(source),
        	DateStateViews.EXCLUDE_FROM_CORE_CLONING
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
    private static <T extends DateState> List<T> getRawStates(
        RefContainer<T> refContainer,
        Object... resourceIdentifier
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
     * @param validFrom include all states ending at validFrom or later
     * @param validTo include all states beginning at validTo or earlier
     * @param invalidated tells whether valid or invalid states excluded<ul>
     * <li><code>null</code>: Neither valid nor invalid states are excluded
     * <li><code>TRUE</code>: Valid states are excluded
     * <li><code>FALSE</code>: Invalid states are excluded
     * </ul>
     * @param qualifiers the objects' qualifiers
     * @return a collection of DateState instances
     * 
     * @throws IllegalArgumentException if validTo is less than validFrom 
     */
    @SuppressWarnings("unchecked")
	private static <T extends DateState> List<T> getStates(
        Container<? super T> referenceCollection,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        Boolean invalidated,
        String... qualifiers
    ){
        Order.assertTimeRange(validFrom, validTo);
        Path containerId = (Path) JDOHelper.getObjectId(referenceCollection);
        RefContainer<T> contextFreeContainer = (RefContainer<T>) DateStateViews.getPersistenceManager(
            ((RefContainer<T>)referenceCollection),
            null
        ).getObjectById(
            JDOHelper.getTransactionalObjectId(referenceCollection)
        );
        Object[] resourceIdentifiers = new Object[qualifiers.length];
        int i = 0;
        for(String qualifier : qualifiers) {
            resourceIdentifiers[i++] = containerId == null ? JDOHelper.getTransactionalObjectId(
                qualifier.startsWith("!") ? contextFreeContainer.refGet(
                    QualifierType.PERSISTENT,
                    qualifier.substring(1)
                ) : contextFreeContainer.refGet(
                    QualifierType.REASSIGNABLE,
                    qualifier
                ) 
            ) : containerId.getChild(
                qualifier
            );
        }
        return new FilteredStates<T>(
            DateStateViews.<T>getRawStates(
                contextFreeContainer,
                resourceIdentifiers
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
     * @param invalidated tells whether valid or invalid states excluded<ul>
     * <li><code>null</code>: Neither valid nor invalid states are excluded
     * <li><code>TRUE</code>: Valid states are excluded
     * <li><code>FALSE</code>: Invalid states are excluded
     * </ul>
     * @param qualifiers the objects' qualifiers
     * @return a collection of DateState instances
     */
    public static <T extends DateState> List<T> getStates(
        Container<? super T> referenceCollection,
        Boolean invalidated,
        String... qualifiers
    ){
        return DateStateViews.getStates(
            referenceCollection,
            null, // validFrom
            null, // validTo
            invalidated, 
            qualifiers
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
            includeInvalidStates && includeValidStates ? null : Boolean.valueOf(includeInvalidStates),
            qualifier
        ): Collections.<T>emptyList(
        );
    }    
    
    /**
     * Retrieve valid states
     * 
     * @param referenceCollection the result of the parents getXXX() method
     * @param validFrom include all states ending at validFrom or later
     * @param validTo include all states beginning at validTo or earlier
     * @param qualifiers the objects' qualifiers
     * 
     * @return a list of DateState instances
     */
    public static <T extends DateState> List<T> getValidStates(
        Container<? super T> referenceCollection,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        String... qualifiers
    ){
        return DateStateViews.getStates(
            referenceCollection,
            validFrom,
            validTo,
            Boolean.FALSE,
            qualifiers
        );
    }    
    
    /**
     * Retrieve all valid states
     * 
     * @param referenceCollection the result of the parents getXXX() method
     * @param qualifiers the objects' qualifiers
     * 
     * @return a list of DateState instances
     */
    public static <T extends DateState> List<T> getValidStates(
        Container<? super T> referenceCollection,
        String... qualifiers
    ){
        return DateStateViews.getStates(
            referenceCollection,
            null, // validFrom
            null, // validTo
            Boolean.FALSE, // invalidated
            qualifiers
        );
    }    
    
    /**
     * Retrieve states 
     * 
     * @param stateCapable a plain JMI object or a date state view
     * @param validFrom include all states ending at validFrom or later
     * @param validTo include all states beginning at validTo or earlier
     * @param invalidated tells whether valid or invalid states excluded<ul>
     * <li><code>null</code>: Neither valid nor invalid states are excluded
     * <li><code>TRUE</code>: Valid states are excluded
     * <li><code>FALSE</code>: Invalid states are excluded
     * </ul>
     * @param existsAt 
     * @param mode 
     * @return a collection of DateState instance
     * 
     * @throws IllegalArgumentException if validTo is less than validFrom 
     */
    @SuppressWarnings("unchecked")
	private static <T extends DateState> List<T> getStates(
        StateCapable stateCapable,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        Boolean invalidated, 
        Date existsAt, 
        AccessMode mode
    ){
        Order.assertTimeRange(validFrom, validTo);
        Path resourceIdentifier = (Path) JDOHelper.getObjectId(stateCapable);
        return new FilteredStates<T>(
            DateStateViews.<T>getRawStates(
                (RefContainer<T>) DateStateViews.getPackageForContext(
                    stateCapable,
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
     * @param stateCapable a plain JMI object or a date state view
     * @param invalidated tells whether valid or invalid states excluded<ul>
     * <li><code>null</code>: Neither valid nor invalid states are excluded
     * <li><code>TRUE</code>: Valid states are excluded
     * <li><code>FALSE</code>: Invalid states are excluded
     * </ul>
     * @return a collection of DateState instances matching the given criteria
     */
    public static <T extends DateState> List<T> getStates(
        StateCapable stateCapable,
        Boolean invalidated
    ){
        return stateCapable == null ? null : DateStateViews.<T>getStates(
            stateCapable,
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
     * @param stateCapable a plain JMI object or a date state view
     * @param includeValidStates tells whether the invalidatedAt attribute may 
     * be non-<code>null</code>
     * @param includeInvalidStates tells whether the invalidatedAt attribute 
     * may be <code>null</code>
     * 
     * @return a collection of DateState instances
     */
    public static <T extends DateState> Collection<T> getStates(
        StateCapable stateCapable,
        boolean includeValidStates,
        boolean includeInvalidStates
    ){
        return includeValidStates || includeInvalidStates ? DateStateViews.<T>getStates(
            stateCapable,
            includeInvalidStates && includeValidStates ? null : Boolean.valueOf(includeInvalidStates)
        ) : Collections.<T>emptySet(
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
     * 
     * @throws IllegalArgumentException if validTo is less than validFrom 
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
            context == null ? DateStateViews.<T>getValidStates(dateState) :
            context.getViewKind() == ViewKind.TIME_POINT_VIEW ? Collections.<T>singletonList((T)dateState) :
            DateStateViews.<T>getValidStates(dateState, context.getValidFrom(), context.getValidTo()); 
    }
    
	@SuppressWarnings("unchecked")
	public static <T extends RefBaseObject> T getViewForContext(
		RefBaseObject context,
		T value
	){
		if(context == null || value == null) {
			return value;
		} else {
			 PersistenceManager actual = JDOHelper.getPersistenceManager(value);
			 PersistenceManager expected = JDOHelper.getPersistenceManager(context);
			 return actual == expected ? value : (T) expected.getObjectById(JDOHelper.getTransactionalObjectId(value));
		}
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
     * Tests whether the view represents a single state
     * 
     * @param refObject
     * 
     * @return <code>true</code> if the view refers to a single state
     * 
     * @throws IllegalArgumentException if there is no date-state context 
     */
    public static boolean representsSingleState(
        DateState state
    ){
        ViewKind viewKind = DateStateViews.getViewKind(state);
        if(viewKind == null) {
            throw new IllegalArgumentException("DateState context lacking");
        }
        switch(viewKind) {
            case TIME_POINT_VIEW:  
                return true;
            case TIME_RANGE_VIEW: 
                List<DateState> involvedStates = DateStateViews.getStatesInvolvedInView((StateCapable)state);
                return involvedStates.size() == 1 && state == involvedStates.get(0);
            default:
                throw new RuntimeException("Assertion failure");
        }
    }
    
    /**
     * Clone an object 
     * 
     * @param refObject view to the object to be cloned
     * @param exclude features not to be cloned
     *
     * @return the clone's transient resource identifier
     * 
     * @throws IllegalArgumentException if the refObject is null, deleted or 
     * not represented by one state exactly
     */
    static UUID getResourceIdentifierOfClone(
        DateState refObject, String... exclude
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
                        Boolean.valueOf(context.getExistsAt() != null), // invalidated
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
        return (UUID) JDOHelper.getTransactionalObjectId(
        	((org.openmdx.base.persistence.spi.Cloneable<?>)state).openmdxjdoClone(
        		exclude
        	)
        );
    }
    
    /**
     * Retrieve the current date
     * 
     * @return the current date
     */
    public static XMLGregorianCalendar today(
    ){
        return DateStateViewContext.today();
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
        } else {
            List<Condition> amendement = new ArrayList<Condition>();
            switch(stateContext.getViewKind()) {
                case TIME_RANGE_VIEW:
                    XMLGregorianCalendar validTo = stateContext.getValidTo();
                    if(validTo != null) {
                        amendement.add(
                            new IsGreaterCondition(
                                Quantifier.FOR_ALL,
                                "stateValidFrom",
                                false, // IS_LESS_OR_EQUAL,
                                validTo
                            )
                        );
                    }
                    XMLGregorianCalendar validFrom = stateContext.getValidFrom();
                    if(validFrom != null) {
                        amendement.add(
                            new IsGreaterOrEqualCondition(
                                Quantifier.FOR_ALL,
                                "stateValidTo",
                                true, // IS_GREATER_OR_EQUAL,
                                validFrom
                            )
                        );
                    }
                    amendement.add(
                        new IsInCondition(
                            Quantifier.FOR_ALL,
                            SystemAttributes.REMOVED_AT,
                            true // ConditionType
                        )
                    );
                    break;
                case TIME_POINT_VIEW:
                    Date existsAt = stateContext.getExistsAt();
                    XMLGregorianCalendar validAt = stateContext.getValidAt();
                    if(validAt == null) {
                        validAt = DateStateViewContext.today();
                    }
                    amendement.add(
                        new IsGreaterCondition(
                            Quantifier.FOR_ALL,
                            "stateValidFrom",
                            false, // IS_LESS_OR_EQUAL
                            validAt
                        )
                    );
                    amendement.add(
                        new IsGreaterOrEqualCondition(
                            Quantifier.FOR_ALL,
                            "stateValidTo",
                            true, // IS_GREATER_OR_EQUAL,
                            validAt
                        )
                    );
                    if(existsAt == null) {
                        amendement.add(
                            new IsInCondition(
                                Quantifier.FOR_ALL,
                                SystemAttributes.REMOVED_AT,
                                true // IS_IN
                            )
                        );
                    }  else {
                        amendement.add(
                            new IsGreaterCondition(
                                Quantifier.FOR_ALL,
                                SystemAttributes.CREATED_AT,
                                false, // IS_LESS_OR_EQUAL
                                existsAt
                            )
                        );
                        amendement.add(
                            new IsGreaterOrEqualCondition(
                                Quantifier.FOR_ALL,
                                SystemAttributes.REMOVED_AT,
                                true, // IS_GREATER_OR_EQUAL
                                existsAt
                            )
                        );
                    }
                    break;
            }
            return amendement;
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

        private static final String[] EXCLUDE_FROM_STATE_SPLITTING = {
            "identity", "core", "stateValidFrom", "stateValidTo"
        };

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
                                DateStateViews.getResourceIdentifierOfClone(raw, EXCLUDE_FROM_STATE_SPLITTING)
                            );
                            state.setStateValidFrom(stateValidFrom);
                            state.setStateValidTo(stateValidTo);
                            target.refDelete();
                            linkStateAndCore(state, (StateCapable) core);
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
            	int id;
            	try {
            		AspectCapable c1 = o1.getCore();
            		AspectCapable c2 = o2.getCore();
            		if(c1 == null) {
            			if(c2 == null) {
                    		return System.identityHashCode(o1) - System.identityHashCode(o2);
            			} else {
            				id = +1;
            			}
            		} else {
            			if (c2 == null){
	            			id = -1;
	            		} else {
	                		UUID u1 = (UUID) JDOHelper.getTransactionalObjectId(c1);
	                		UUID u2 = (UUID) JDOHelper.getTransactionalObjectId(c2);
	                		id = u1.compareTo(u2);
	            		}
            		}
            	} catch (Exception exception) {
            		return System.identityHashCode(o1) - System.identityHashCode(o2);
            	}
            	if(id != 0) {
            		return id;
            	}
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
        Collection<E> getDelegate(){
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
        
        E marshal(E raw){
           switch(this.mode){
	          case RAW:
	              return raw;
	          case FOR_QUERY:
	        	  return DateStateViews.getViewForState(raw);
	          case FOR_UPDATE:
	              XMLGregorianCalendar validFrom = raw.getStateValidFrom();
	              XMLGregorianCalendar validTo = raw.getStateValidTo();
	              return DateStateViews.<E,E>getViewForTimeRange(
                      raw,
                      Order.compareValidFrom(this.validFrom, validFrom) > 0 ? this.validFrom : validFrom,
                      Order.compareValidTo(this.validTo, validTo) < 0 ? this.validTo : validTo
                  );
	           default:
	        	   return null;
	      	}       	
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractSequentialList#listIterator(int)
         */
        @Override
        public ListIterator<E> listIterator(final int index) {
        	return new ListIterator<E>() {
        		
        		private final ListIterator<E> delegate = new ArrayList<E>(getDelegate()).listIterator(index);
        		private E current;
        		
				public boolean hasNext() {
					return delegate.hasNext();
				}

				public E next() {
					return this.current = marshal(delegate.next());
				}

				public boolean hasPrevious() {
					return delegate.hasPrevious();
				}

				public E previous() {
					return this.current = marshal(delegate.previous());
				}

				public int nextIndex() {
					return delegate.nextIndex();
				}

				public int previousIndex() {
					return delegate.previousIndex();
				}

				public void remove() {
					if(this.current == null) {
						throw new IllegalStateException("No current element");
					} else {
						this.current.refDelete();
						this.current = null;
					}
				}

				public void set(E o) {
					throw new UnsupportedOperationException();
				}

				public void add(E o) {
					throw new UnsupportedOperationException();
				}
        		
        	};
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
