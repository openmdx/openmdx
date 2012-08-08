/*
 * ====================================================================
 * Description: Date State Views 
 * Revision:    $Revision: 1.26 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:28 $
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
package org.openmdx.compatibility.state1.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jmi.reflect.RefBaseObject;
import javax.jmi.reflect.RefObject;
import javax.resource.cci.InteractionSpec;
import javax.xml.datatype.XMLGregorianCalendar;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.jmi.cci.RefPackageFactory_1_1;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_5;
import org.openmdx.base.accessor.jmi.spi.RefPackage_1_6;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.state1.jmi1.DateState;
import org.openmdx.compatibility.state1.jmi1.StateCapable;
import org.openmdx.compatibility.state1.spi.StateCapables;
import org.openmdx.state2.cci.DateStateContext;
import org.openmdx.state2.cci2.BasicState;
import org.openmdx.state2.spi.DateStateViewContext;

import static org.openmdx.state2.cci.DateStateViews.order;

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
     * Create a view for a given object
     * 
     * @param refObject
     * @param viewContext
     * @param clone 
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private static final <T extends RefObject> T getView(
        T refObject,
        DateStateContext viewContext,
        boolean clone
    ){
        if(clone) {
            throw new UnsupportedOperationException("Not yet implememted"); // TODO
        }
        if(refObject == null) {
            return null;
        }
        RefPackageFactory_1_1 refPackageFactory = (RefPackageFactory_1_1) refObject.refOutermostPackage();
        RefPackage_1_5 refPackage = refPackageFactory.getRefPackage((InteractionSpec) viewContext);
        if(refPackage == refObject.refOutermostPackage()) {
            return refObject;
        }
        if(refObject instanceof DateState) {
            DateState dateState = (DateState) refObject;
            StateCapable stateCapable = (StateCapable)refPackage.refObject(
               StateCapables.getStateCapable(dateState.getIdentity())
            );
            Set<DateState> states = stateCapable.getState();
            return states.isEmpty() ? null : (T) states.iterator().next();
        } else {
            return (T) refPackage.refObject(refObject.refMofId());
        }
    }


    //------------------------------------------------------------------------
    // Time Point Views (read-only for DateTime instances)
    //------------------------------------------------------------------------
    
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
    public static <T extends RefObject> T getView(
        T object,
        XMLGregorianCalendar validFor,
        Date validAt
    ){
        return getView(
            object,
            DateStateViewContext.newTimePointViewContext(
                validFor == null ? today() : validFor,
                validAt
            ),
            false // clone
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
    public static <T extends RefObject> T getView(
        T object,
        XMLGregorianCalendar validFor
    ){
        return getView(
            object,
            validFor,
            null // include all changes
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
    public static <T extends RefObject> T getView(
        T refObject
    ){
        return getView(
            refObject,
            today()
        );
    }
    
    
    //------------------------------------------------------------------------
    // Time Range Views (write-only for DateTime instances)
    //------------------------------------------------------------------------
    
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
    public static <T extends RefObject> T getViewForTimeRange(
        T object,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        return getView(
            object,
            DateStateViewContext.newTimeRangeViewContext(
                validFrom,
                validTo
            ),
            false // clone
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
        T dateState
    ){
        return (T) org.openmdx.state2.cci.DateStateViews.getViewForLifeTime(
            dateState
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
        return org.openmdx.state2.cci.DateStateViews.getViewForContiguousStates(
            dateStateView
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
     * @param dateStateView the underlying date state view
     * @param validFrom begin of the overridden period 
     * @param validTo end of the overridden period
     * @param override tells whether it is allowed to override valid states 
     * 
     * @return a view to the given object
     */
    public static <T extends DateState> T getViewForInitializedState(
        T source,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        boolean override
    ){
        return org.openmdx.state2.cci.DateStateViews.getViewForInitializedState(
            source,
            validFrom,
            validTo,
            override
        );
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
    public static <T extends DateState> T getViewForPropagatedState(
        T source,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        boolean override
    ){
        return org.openmdx.state2.cci.DateStateViews.getViewForPropagatedState(
            source,
            validFrom,
            validTo,
            override
        );            
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
        return org.openmdx.state2.cci.DateStateViews.getViewForPropagatedState(
            dateStateView,
            validFrom,
            validTo
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
     * @param deleted tells whether persistent or persistent-deleted states are excluded<ul>
     * <li><code>null</code>: Neither persistent nor persistent-deleted states are excluded
     * <li><code>TRUE</code>: Persistent states are excluded
     * <li><code>FALSE</code>: Persistent-deleted states are excluded
     * </ul>
     * 
     * @return a collection of DateState instances
     */
    @SuppressWarnings("unchecked")
    public static Collection<? extends DateState> getStates(
        Collection<? extends DateState> referenceCollection,
        String qualifier,
        Boolean invalidated, 
        Boolean deleted
    ){
        return (Collection<? extends DateState>) org.openmdx.state2.cci.DateStateViews.getStates(
            getStateCapable(referenceCollection, qualifier),
            invalidated,
            deleted
        );
    }

    /**
     * Retrieve the virtual core object
     * 
     * @param identity the stated object's identity
     * 
     * @return the the virtual core object
     */
    static StateCapable getStateCapable(
        Collection<? extends DateState> referenceCollection,
        String qualifier
    ){
        Path objectId = StateCapables.getStateCapable(
            (RefBaseObject)referenceCollection,
            qualifier
        );
        RefContainer refContainer = (RefContainer) referenceCollection;
        RefPackageFactory_1_1 refPackageFactory = (RefPackageFactory_1_1) refContainer.refOutermostPackage();
        RefPackage_1_6 refPackage = refPackageFactory.getRefPackage(null);
        return (StateCapable) refPackage.refObject(objectId);
    }
    
    /**
     * Retrieve states 
     * 
     * @param referenceCollection the result of the parents getXXX() method
     * @param qualifier the object's qualifier
     * @param includeValidStates tells whether the invalidatedAt attribute may be non-null
     * @param includeInvalidStates tells whether the invalidatedAt attribute may be null
     * @param includeNonDeletedStates tells whether the <code>deleted</code> 
     * predicate may be <code>false</code>
     * @param includeDeletedStates tells whether the <code>deleted</code> 
     * predicate may be <code>true</code>
     * 
     * @return a collection of DateState instances
     */
    @SuppressWarnings("unchecked")
    public static Collection<? extends DateState> getStates(
        Collection<? extends DateState> referenceCollection,
        String qualifier,
        boolean includeValidStates,
        boolean includeInvalidStates,
        boolean includeNonDeletedStates,
        boolean includeDeletedStates
    ){
        return (Collection<? extends DateState>) org.openmdx.state2.cci.DateStateViews.getStates(
            getStateCapable(referenceCollection, qualifier),
            includeValidStates, 
            includeInvalidStates, 
            includeNonDeletedStates,
            includeDeletedStates
        );
    }    
    
    /**
     * Retrieve non-deleted valid states
     * 
     * @param referenceCollection the result of the parents getXXX() method
     * @param qualifier the object's qualifier
     * @param validFrom include all states ending at validFrom or later
     * @param validTo include all states beginning at validTo or earlier
     * 
     * @return a list of DateState instances
     */
    @SuppressWarnings("unchecked")
    public static List<? extends DateState> getStates(
        Collection<? extends DateState> referenceCollection,
        String qualifier,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        return (List<? extends DateState>) org.openmdx.state2.cci.DateStateViews.getStates(
            getStateCapable(referenceCollection, qualifier),
            validFrom,
            validTo
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
    @SuppressWarnings("unchecked")
    public static List<? extends DateState> getStates(
        Collection<? extends DateState> referenceCollection,
        String qualifier
    ){
        return (List<? extends DateState>) org.openmdx.state2.cci.DateStateViews.getStates(
            getStateCapable(referenceCollection, qualifier)
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
     * @param deleted tells whether persistent or persistent-deleted states are excluded<ul>
     * <li><code>null</code>: Neither persistent nor persistent-deleted states are excluded
     * <li><code>TRUE</code>: Persistent states are excluded
     * <li><code>FALSE</code>: Persistent-deleted states are excluded
     * </ul>
     * @return a collection of DateState instances matching the given criteria
     */
    @SuppressWarnings("unchecked")
    public static Collection<? extends DateState> getStates(
        DateState dateState,
        Boolean invalidated, 
        Boolean deleted
    ){
        List<DateState> target = new ArrayList<DateState>();
        RefPackageFactory_1_1 refPackageFactory = (RefPackageFactory_1_1) dateState.refOutermostPackage();
        RefPackage_1_5 refPackage = refPackageFactory.getRefPackage((InteractionSpec) null);
        StateCapable stateCapable =  (StateCapable) refPackage.refObject(
           StateCapables.getStateCapable(dateState.getIdentity())
        );
        Set<DateState> source = stateCapable.getState();
        for(Object o : source) {
            BasicState s = (BasicState) o;
            if(
                (
                    invalidated != null || 
                    invalidated.booleanValue() == (s.getRemovedAt() != null)
                ) &&
                (
                    deleted != null || 
                    deleted.booleanValue() == JDOHelper.isDeleted(s)
                )
            ) {
                target.add((DateState) s);
            }
        }
        return target;
    }

    
    /**
     * Retrieve states 
     * 
     * @param dateState a plain JMI object or a date state view
     * @param includeValidStates tells whether the invalidatedAt attribute may 
     * be non-<code>null</code>
     * @param includeInvalidStates tells whether the invalidatedAt attribute 
     * may be <code>null</code>
     * @param includeNonDeletedStates tells whether the <code>deleted</code> 
     * predicate may be <code>false</code>
     * @param includeDeletedStates tells whether the <code>deleted</code> 
     * predicate may be <code>true</code>
     * 
     * @return a collection of DateState instances
     */
    @SuppressWarnings("unchecked")
    public static Collection<? extends DateState> getStates(
        DateState dateState,
        boolean includeValidStates,
        boolean includeInvalidStates,
        boolean includeNonDeletedStates, 
        boolean includeDeletedStates
    ){
        return (Collection<? extends DateState>) org.openmdx.state2.cci.DateStateViews.getStates(
            dateState,
            includeValidStates,
            includeInvalidStates,
            includeNonDeletedStates,
            includeDeletedStates
        );
    }
    
    /**
     * Retrieve valid non-deleted states 
     * 
     * @param dateState a plain JMI object or a date state view
     * @param validFrom include all states ending at validFrom or later
     * @param validTo include all states beginning at validTo or earlier
     * 
     * @return a list of DateState instances
     */
    @SuppressWarnings("unchecked")
    public static List<? extends DateState> getStates(
        DateState dateState,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        return (List<? extends DateState>) order(
            getStates(dateState, Boolean.FALSE, Boolean.FALSE),
            validFrom,
            validTo
        );
    }    
    
    /**
     * Retrieve valid non-deleted states
     * 
     * @param dateState a plain JMI object or a date state view
     * 
     * @return a list of DateState instances
     */
    @SuppressWarnings("unchecked")
    public static List<? extends DateState> getStates(
        DateState dateState
    ){
        return getStates(
            dateState,
            (XMLGregorianCalendar)null,
            (XMLGregorianCalendar)null
        );
    }

    
    //------------------------------------------------------------------------
    // Date State Context
    //------------------------------------------------------------------------
    
    /**
     * Retrieve the context for date state views.
     * 
     * @param refObject a plain JMI object or a date state view
     * 
     * @return the <code>DateStateContext</code> in case of a 
     * Date State View, <code>null</code> otherwise.
     */
    public static DateStateContext getContext(
        RefObject refObject
    ){
        return org.openmdx.state2.cci.DateStateViews.getContext(refObject);
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
     * @return <code>true</code> unless<ul>
     * <li><code>refObject</code> is <code>null</code> 
     * <li><code>refObject instanceof RefObject_1_0 && ((RefObject_1_0)refObject).refIsDeleted()</code> 
     * is <code>true</code> 
     * <li><code>refObject instanceof DateState && isView(refObject)</code> is <code>true</code> 
     * and the view does not refer to exactly one valid state 
     * </ul>
     */
    public static boolean isReadable(
        RefObject refObject
    ){
        return org.openmdx.state2.cci.DateStateViews.isReadable(
            refObject, 
            true // completely
        );
    }
    
    /**
     * Retrieve the current date
     * 
     * @return the current date
     */
    public static XMLGregorianCalendar today(
    ){
        return org.openmdx.state2.cci.DateStateViews.today();
    }

}
