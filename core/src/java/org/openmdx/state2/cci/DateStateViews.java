/*
 * ====================================================================
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:37 $
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.JDOHelper;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.resource.cci.InteractionSpec;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackageFactory_1_1;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_2;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_5;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.ExtentCapable;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.state2.cci2.BasicState;
import org.openmdx.state2.jmi1.DateState;
import org.openmdx.state2.jmi1.StateCapable;
import org.openmdx.state2.spi.DateStateContexts;
import org.openmdx.state2.spi.DateStateViewContext;
import org.openmdx.state2.spi.ValidTimes;

/**
 * Date State Views
 * <p>
 * Such an API will be provided in state2 as well.
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
     * Tells whether a propagation event may override valid states by default.
     */
    protected static final boolean OVERRIDE_DEFAULT = true;

    /**
     * Typed empty set
     */
    private final static Set<? extends DateState> NO_STATES = Collections.emptySet();

    private static Path getPath(
        RefObject refObject
    ){
        return refObject instanceof RefObject_1_0 ?
            ((RefObject_1_0)refObject).refGetPath() :
            new Path(refObject.refMofId());
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
        RefPackageFactory_1_1 refPackageFactory = (RefPackageFactory_1_1) refObject.refOutermostPackage();
        RefPackage_1_5 refPackage = refPackageFactory.getRefPackage((InteractionSpec) viewContext);
        String resourceIdentifier = refObject instanceof ExtentCapable ? 
            ((ExtentCapable)refObject).getIdentity() :
            refObject.refMofId();  
        return resourceIdentifier == null ? null : (T)refPackage.refObject(resourceIdentifier);
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
     */
    public static <T extends RefObject> T getViewForTimePoint(
        T object,
        XMLGregorianCalendar validFor,
        Date validAt
    ){
        return getView(
            object,
            DateStateViewContext.newTimePointViewContext(
                validFor == null ? today() : validFor,
                validAt // keep null to avoid time synchronization problems
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
    public static <T extends RefObject> T  getViewForTimePoint(
        T object,
        XMLGregorianCalendar validFor
    ){
        return getViewForTimePoint(
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
    public static <T extends RefObject> T getViewForTimePoint(
        T refObject
    ){
        return getViewForTimePoint(
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
     */
    @SuppressWarnings("unchecked")
    public static <T extends DateState> T getViewForLifeTime(
        StateCapable stateCapable
    ){
        List<? extends DateState> states = getStates(
            stateCapable, 
            (XMLGregorianCalendar)null, // validFrom
            (XMLGregorianCalendar)null // validTo
        );
        return states.isEmpty() ? null : (T)getView(
            stateCapable,
            DateStateViewContext.newTimeRangeViewContext(
                states.get(0).getStateValidFrom(),
                states.get(states.size()-1).getStateValidTo()
            ),
            false // clone
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
     */
    public static <T extends DateState> T getViewForContiguousStates(
        T dateStateView
    ){
        return null; // TODO
//        List<? extends DateState> head = getStates(dateStateView, null, dateStateView.getStateValidFrom());
//        List<? extends DateState> tail = getStates(dateStateView, dateStateView.getStateValidTo(), null);
//        if(head.isEmpty()) {
//            //
//            // dateStateView is probably invalid
//            //
//            return null;
//        } else {
//            //
//            // look for begin
//            //
//            int from = head.size() - 1;
//            while(
//                from > 0 &&
//                adjacent(head.get(from - 1), head.get(from))
//            ) from--;
//            //
//            // look for end
//            //
//            int to = 1;
//            while(
//                to < tail.size() &&
//                adjacent(tail.get(to-1), tail.get(to))
//            ) to++;
//            //
//            // Retrieve view
//            //
//            return (DateState)getView(
//                dateStateView,
//                DateStateViewContext.newTimeRangeViewContext(
//                    head.get(from).getStateValidFrom(),
//                    tail.get(to-1).getStateValidTo()
//                ),
//                false // clone
//            );
//        }
    }


    //------------------------------------------------------------------------
    // State Propagation Views (readable and writable)
    //------------------------------------------------------------------------

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
    public static <T extends DateState> T getViewForInitializedState(
        T source,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        boolean override
    ){
        T target = getViewForPropagatedState(
            source,
            validFrom,
            validTo,
            override
        );
        target.refInitialize(
            source.refClass().refCreateInstance(null)
        );
        return target;
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
    public static <T extends DateState> T getViewForPropagatedState(
        T source,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        boolean override
    ){
        //
        // Compare source and view validity
        //
        boolean fromExcluded = ValidTimes.compareValidFrom(
            source.getStateValidFrom(),
            validFrom
        ) > 0;
        boolean toExcluded = ValidTimes.compareValidTo(
            source.getStateValidTo(),
            validTo
        ) < 0;
        //
        // Validate source
        //
        if(
            !isView(source) ||
            !isReadable(source, true)
        ) {
            throw new IllegalArgumentException(
                "The source should be a readable date state view object"
            );
        }
        T target = getView(
            source,
            DateStateViewContext.newTimeRangeViewContext(
                validFrom,
                validTo
            ),
            fromExcluded || toExcluded // clone
        );
        if(
            (fromExcluded || toExcluded) &&
            !override
        ) {
            for(DateState d : getStates((StateCapable)source, validFrom, validTo)){
                if(!d.equals(source)) throw new JmiServiceException(
                    new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.DUPLICATE,
                        new BasicException.Parameter[]{
                            new BasicException.Parameter("path", getPath(source)),
                            new BasicException.Parameter("validFrom", validFrom),
                            new BasicException.Parameter("validTo", validTo),
                            new BasicException.Parameter("override", Boolean.FALSE)
                        },
                        "There is another valid state in the given period"
                    )
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
     * @param source the underlying date state view
     * @param validFrom begin of the overridden period 
     * @param validTo end of the overridden period
     * 
     * @return a view to the given object
     */
    public static <T extends DateState> T getViewForPropagatedState(
        T source,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        return getViewForPropagatedState(
            source,
            validFrom,
            validTo,
            OVERRIDE_DEFAULT
        );
    }


    //------------------------------------------------------------------------
    // State Retrieval 
    //------------------------------------------------------------------------

    /**
     * Filter and order date state instances
     * 
     * @param dateStates a collection of date state instances
     * @param validFrom
     * @param validTo
     * 
     * @return a list of DateState instances
     */
    public static List<? extends DateState> order(
        Collection<? extends DateState> dateStates,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        SortedSet<DateState> set = new TreeSet<DateState>(ValidFromComparator.getInstance());
        for(
            Iterator<? extends DateState> i = dateStates.iterator();
            i.hasNext();
        ){
            DateState state = i.next();
            if(
                (validFrom == null || ValidTimes.compareValidTo(state.getStateValidTo(), validFrom) >= 0) &&
                (validTo == null || ValidTimes.compareValidTo(state.getStateValidFrom(), validTo) <= 0)
            ) {
                set.add(state);
            }
        }
        return new ArrayList<DateState>(set);
    }

    /**
     * Retrieve states 
     * 
     * @param stateCapable either a core or view object
     * @param invalidated tells whether one looks for valid or invalid states 
     * @param deleted tells whether one looks for persistent or persistent-deleted states
     * 
     * @return a collection of DateState instances matching the given criteria
     */
    public static Collection<? extends DateState> getStates(
        StateCapable stateCapable,
        Boolean invalidated,
        Boolean deleted
    ){
        List<DateState> target = new ArrayList<DateState>();
        for(Object o : stateCapable.getState()) {
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
     * @param stateCapable either a core or view object
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
    public static Collection<? extends DateState> getStates(
        StateCapable stateCapable,
        boolean includeValidStates,
        boolean includeInvalidStates,
        boolean includeNonDeletedStates,
        boolean includeDeletedStates
    ){
        return (
            (includeValidStates | includeInvalidStates) &
            (includeNonDeletedStates | includeDeletedStates)
        ) ? getStates(
            stateCapable,
            includeValidStates & includeInvalidStates ? null : Boolean.valueOf(includeInvalidStates),
            includeNonDeletedStates & includeDeletedStates ? null : Boolean.valueOf(includeDeletedStates)
        ) : NO_STATES;
    }

    /**
     * Retrieve valid non-deleted states 
     * 
     * @param stateCapable either a core or view object
     * @param validFrom include all states ending at validFrom or later
     * @param validTo include all states beginning at validTo or earlier
     * 
     * @return a list of DateState instances
     */
    public static List<? extends DateState> getStates(
        StateCapable stateCapable,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        return order(
            getStates(stateCapable, Boolean.FALSE, Boolean.FALSE),
            validFrom,
            validTo
        );
    }

    /**
     * Retrieve valid non-deleted states
     * 
     * @param stateCapable either a core or view object
     * 
     * @return a list of DateState instances
     */
    public static List<? extends DateState> getStates(
        StateCapable stateCapable
    ){
        return getStates(
            stateCapable,
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
        RefPackage refPackage = refObject.refOutermostPackage();
        Object viewContext = refPackage instanceof RefPackage_1_2 ?
            ((RefPackage_1_2)refPackage).refInteractionSpec() :
            null;
        return viewContext instanceof DateStateContext ?
            (DateStateContext)viewContext :
            null;
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
     * @param completely if <ocde>true</core> all underlying states are tested for their attributes equality
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
        RefObject refObject, 
        boolean completely
    ){
        if(refObject instanceof RefObject_1_0) {
            RefObject_1_0 reOobject = (RefObject_1_0) refObject;
//            try {
                return
                    reOobject instanceof DateState &&
                    isView(reOobject) &&
//                    reOobject.refDelegate() instanceof ViewObject_1_0 ?
//                    ((ViewObject_1_0)reOobject.refDelegate()).isReadable() :
                    !reOobject.refIsDeleted();
//            } catch (ServiceException exception) {
//                return false;
//            }
        } else {
            return refObject != null;
        }
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

//    /**
//     * Tests whether two states are adjacent
//     * 
//     * @param earlier
//     * @param later
//     * 
//     * @return <code>true</code> if two states are adjacent
//     */
//    private static boolean adjacent(
//        DateState earlier,
//        DateState later
//    ){
//        XMLGregorianCalendar date = (XMLGregorianCalendar) earlier.getStateValidTo().clone();
//        date.add(DateStateContexts.ONE_DAY);
//        return date.equals(later.getStateValidFrom());
//    }


     //------------------------------------------------------------------------
     // Class ValidFromComparator
     //------------------------------------------------------------------------

     /**
      * Date State Valid From Comparator 
      */
     final static class ValidFromComparator
         implements Comparator<DateState>, Serializable
     {

         /**
          * Constructor 
          */
         private ValidFromComparator(){
             // Avoid external instantiation
         }

         /**
          * A singleton
          */
         private final static Comparator<DateState> instance = new ValidFromComparator();
                  
         /**
          * Implements <code>Serializable</code>
          */
         private static final long serialVersionUID = -6816519954526341332L;

         /**
          * Implements <code>Comparable</code>
          */
         public int compare(DateState o1, DateState o2) {
             return ValidTimes.compareValidFrom(
                 o1.getStateValidFrom(),
                 o2.getStateValidFrom()
             );
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

         static Comparator<DateState> getInstance(){
             return ValidFromComparator.instance;
         }
         
     }

}
