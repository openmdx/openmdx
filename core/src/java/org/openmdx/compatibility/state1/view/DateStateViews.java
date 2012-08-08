/*
 * ====================================================================
 * Description: Date State Views 
 * Revision:    $Revision: 1.57 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/29 17:04:09 $
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


import java.io.Serializable;
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

import javax.jdo.JDOHelper;
import javax.jmi.reflect.RefBaseObject;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.resource.cci.InteractionSpec;
import javax.xml.datatype.XMLGregorianCalendar;

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefFilter_1_0;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.cci2.ExtentCapable;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.PathComponent;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.query.AttributeSpecifier;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.state1.aop1.StateContainer_1;
import org.openmdx.compatibility.state1.jmi1.DateState;
import org.openmdx.compatibility.state1.spi.StateCapables;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.state2.cci.DateStateContext;
import org.openmdx.state2.cci.ViewKind;
import org.openmdx.state2.spi.DateStateContexts;
import org.openmdx.state2.spi.DateStateViewContext;
import org.openmdx.state2.spi.Order;
import org.w3c.cci2.AnyTypePredicate;

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
     * Retrieve a <code>DateState</code> comparator
     * 
     * @returna <code>DateState</code> comparator instance
     */
    public static Comparator<DateState> getComparator(
    ){
        return StateComparator.instance;
    }
    
    private static Path getResourceIdentifier(
        Object object
    ){
        Path resourceIdentifier = null;
        Object objectId = PersistenceHelper.getCurrentObjectId(object);
        if(objectId != null) {
            resourceIdentifier = objectId instanceof Path ? (Path)objectId : new Path(objectId.toString());
        }
        if(object instanceof RefObject_1_0) {
            resourceIdentifier = ((RefObject_1_0)object).refGetPath();
        }
        if(resourceIdentifier == null && object instanceof ExtentCapable){
            resourceIdentifier = new Path(((ExtentCapable)object).getIdentity());
        }
        if(resourceIdentifier == null && object instanceof RefObject) {
            resourceIdentifier = new Path(((RefObject)object).refMofId());
        }
        if(resourceIdentifier != null) { 
            resourceIdentifier = StateCapables.getResourceIdentifier(resourceIdentifier);
            PathComponent base = resourceIdentifier.getLastComponent();
            if(base.isPrivate()) {
                resourceIdentifier = resourceIdentifier.getParent().add(
                    base.getPrefix(base.size() - 2)
                );
            }
        }
        return resourceIdentifier; 
    }

    private static Path getResourceIdentifier(
        RefBaseObject refContainer,
        String qualifier
    ){
        return new Path(refContainer.refMofId()).add(qualifier);
    }
    
    private static RefPackage_1_0 getPackageForContext(
        RefBaseObject refContext,
        DateStateViewContext context
    ){
        RefPackage_1_0 refPackageFactory = (RefPackage_1_0)refContext.refOutermostPackage(); 
        return refPackageFactory.refPackage(context);
    }
    
    /**
     * 
     * @param container
     * @param predicate
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <E extends DateState,C extends org.w3c.cci2.Container<E>> List<E> getStates(
        C container,
        AnyTypePredicate predicate
    ){
        RefBaseObject collection = (RefBaseObject) container;
        Path collectionId = new Path(collection.refMofId());
        C stateContainer = (C)getPackageForContext(
            collection,
            null
        ).refContainer(
            collectionId,
            container.getClass().getInterfaces()[0]
        );
        AnyTypePredicate statePredicate = predicate;
        if(predicate != null) {
            Collection<FilterProperty> filterPropertyCollection = ((RefFilter_1_0)predicate).refGetFilterProperties();
            FilterProperty[] filterPropertyArray = filterPropertyCollection == null ? new FilterProperty[]{} : filterPropertyCollection.toArray(
                new FilterProperty[filterPropertyCollection.size()]
            );
            if(StateContainer_1.amend(filterPropertyArray)) {
                FilterProperty[] amendment = getAmendment(getContext(collection));
                if(amendment != null) {
                    FilterProperty[] amended = new FilterProperty[filterPropertyArray.length + amendment.length];
                    System.arraycopy(filterPropertyArray, 0, amended, 0, filterPropertyArray.length);
                    System.arraycopy(amendment, 0, amended, filterPropertyArray.length, amendment.length);
                    Collection<AttributeSpecifier> attributeSpecifierCollection = ((RefFilter_1_0)predicate).refGetAttributeSpecifiers();
                    AttributeSpecifier[] attributeSpecifierArray = attributeSpecifierCollection == null ? new AttributeSpecifier[]{} : attributeSpecifierCollection.toArray(
                        new AttributeSpecifier[attributeSpecifierCollection.size()]
                    );
                    statePredicate = new Filter(
                        amended,
                        attributeSpecifierArray
                    );
                }
            }
        }
        return new StateList<E>(
            stateContainer.getAll(statePredicate)
        );
    }
            
    private static Object[] toSelector(
        String xriSegment
    ){
        boolean persistent = xriSegment.startsWith("!");
        return new Object []{
            QualifierType.valueOf(persistent),
            persistent ? xriSegment.substring(1) : xriSegment
        };
    }
   
    private static RefObject getView(
        RefPackage_1_0 refPackage,
        Path resourceIdentifier
    ){
        try {
            if(resourceIdentifier.startsWith(StateCapables.TRANSIENT_CONTAINER)) {
                int cursor = StateCapables.TRANSIENT_OBJECT.size();
                int limit = resourceIdentifier.size();
                if(limit > cursor) {
                    if(limit % 2 == 0) {
                        throw new RuntimeServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            "Even resource identifier size",
                            new BasicException.Parameter("resourceIdentifier", resourceIdentifier.toXRI()),
                            new BasicException.Parameter("size", limit)
                        );
                    }
                    RefObject refObject = refPackage.refObject(
                        resourceIdentifier.getPrefix(cursor)
                    );
                    while(cursor < limit) {
                        RefContainer refContainer = (RefContainer) refObject.refGetValue(
                            resourceIdentifier.get(cursor++)
                        );
                        refObject = (RefObject) refContainer.refGet(
                            toSelector(
                                resourceIdentifier.get(cursor++)
                            )
                        );
                    }
                    return refObject;
                }
            }
            return refPackage.refObject(resourceIdentifier);
        } catch (JmiServiceException exception) {
            if(exception.getExceptionCode() == BasicException.Code.NOT_FOUND) {
                return null;
            }
            throw exception;
        }
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
                    String dateTime = DateFormat.getInstance().format(createdAt);
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
    // Time Point Views (read-only for DateTime instances)
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
        return object == null ? null : (T) getView( 
            getPackageForTimePoint(
                object,
                validFor,
                validAt
            ),
            getResourceIdentifier(object)
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
        return object == null ? null : (T) getView(
            getPackageForTimePoint(
                object,
                validFor,
                null
            ),
            getResourceIdentifier(object)
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
        return object == null ? null : (T) getView(
            getPackageForTimePoint(
                object,
                null, // validFor
                null // validAt
            ),
            getResourceIdentifier(object)
        );
    }
    
    
    //------------------------------------------------------------------------
    // Time Range Views (write-only for DateTime instances)
    //------------------------------------------------------------------------

    private static RefPackage_1_0 getPackageForTimeRange(
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
        Class<T> refClass,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo        
    ){
        return refContext == null ? null : (T) getPackageForTimeRange(
            refContext,
            validFrom,
            validTo
        ).refPackage(
             refClass.getName()
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
        RefBaseObject refBaseObject = (RefBaseObject) referenceCollection; 
        return (T) getView(
            getPackageForTimeRange(
                refBaseObject,
                validFrom,
                validTo
            ),
            getResourceIdentifier(
                refBaseObject,
                qualifier
            )
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
           return DateStateViews.getPackageForTimeRange(
               dateState, 
               packageClass, 
               dateState.getStateValidFrom(),
               dateState.getStateValidTo()
           );
       }
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
        return object == null ? null : (T) getView(
            getPackageForTimeRange(
                object,
                validFrom,
                validTo
            ),
            getResourceIdentifier(object)
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
        return (T) getView(
            getPackageForTimeRange(
                dateState,
                states.get(0).getStateValidFrom(),
                states.get(states.size()-1).getStateValidTo()
            ),
            getResourceIdentifier(dateState)
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
        Path resourceIdentifier = getResourceIdentifier(source);
        String classId = source.refClass().refMofId();
        RefPackage_1_0 refPackage = getPackageForTimeRange(
            source,
            validFrom,
            validTo
        );
        T range = (T) refPackage.refObject(
            resourceIdentifier
        );
        if(range != null && !JDOHelper.isDeleted(range)) {
            if(override) {
                range.refDelete();
            } else {
                throw new RuntimeServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.DUPLICATE,
                    "The given range is not empty"
                );
            }
        }
        RefContainer container = refPackage.refContainer(
            resourceIdentifier.getParent(),
            null
        );
        range = (T) refPackage.refClass(
            classId
        ).refCreateInstance(
            null
        ); 
        container.refAdd(
            RefContainer.REASSIGNABLE, 
            resourceIdentifier.getBase(),
            range
        );
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
        Path resourceIdentifier = getResourceIdentifier(source);
        DateStateViewContext viewContext =  DateStateViewContext.newTimeRangeViewContext(
            validFrom,
            validTo
        );
        RefPackage_1_0 refPackage = getPackageForContext(
            source,
            viewContext
        ); 
        T target = (T) getView(
            refPackage,
            resourceIdentifier
        );
        if(fromExcluded || toExcluded) {
            if(override) {
                RefContainer container = getContainerForContext(
                    source,
                    resourceIdentifier.getParent(), 
                    null
                );
                RefObject state = getView(
                    refPackage,
                    getResourceIdentifierOfClone(source)
                );
                target.refDelete();
                container.refAdd(
                    RefContainer.REASSIGNABLE,
                    resourceIdentifier.getBase(),
                    state
                );
            } else {
                for(org.openmdx.state2.cci2.DateState d : getStates(source, validFrom, validTo)){
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
    
    @SuppressWarnings("unchecked")
    private static <T extends DateState> List<T> getStates(
        RefContainer container,
        Path resourceIdentifier
    ){
        return (List<T>) container.refGetAll(
            new FilterProperty[]{
                new FilterProperty(
                    Quantors.THERE_EXISTS,
                    SystemAttributes.OBJECT_INSTANCE_OF,
                    FilterOperators.IS_IN,
                    "org:openmdx:compatibility:state1:DateState"
                ),
                new FilterProperty(
                    Quantors.THERE_EXISTS,
                    "core",
                    FilterOperators.IS_IN,
                    StateCapables.getResourceIdentifier(resourceIdentifier)
                )
            }
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
    private static <T extends DateState> List<T> getStates(
        RefBaseObject context,
        Path containerIdentifier,
        Path resourceIdentifier
    ){
        return getStates(
            getContainerForContext(
                context,
                containerIdentifier, null
            ),
            resourceIdentifier
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
     * @param deleted tells whether persistent or persistent-deleted states are excluded<ul>
     * <li><code>null</code>: Neither persistent nor persistent-deleted states are excluded
     * <li><code>TRUE</code>: Persistent states are excluded
     * <li><code>FALSE</code>: Persistent-deleted states are excluded
     * </ul>
     * @param existsAt TODO
     * @param asView 
     * @return a collection of DateState instances
     */
    @SuppressWarnings("unchecked")
    private static <T extends DateState> List<T> getStates(
        Collection<T> referenceCollection,
        String qualifier,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        Boolean invalidated, 
        Boolean deleted, 
        Date existsAt, boolean asView
    ){
        RefBaseObject refBaseObject = (RefBaseObject) referenceCollection;
        Path containerIdentifier = new Path(((RefBaseObject)referenceCollection).refMofId());
        return (List<T>) find(
            getStates(
                refBaseObject,
                containerIdentifier, 
                getResourceIdentifier((RefContainer) referenceCollection, qualifier)
            ),
            validFrom,
            validTo,
            invalidated,
            deleted, 
            existsAt, asView
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
    public static <T extends DateState> List<T> getStates(
        Collection<T> referenceCollection,
        String qualifier,
        Boolean invalidated, 
        Boolean deleted
    ){
        return getStates(
            referenceCollection,
            qualifier,
            null, // validFrom
            null, // validTo
            invalidated,
            deleted, null, true
        );
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
    public static <T extends DateState> List<T> getStates(
        Collection<T> referenceCollection,
        String qualifier,
        boolean includeValidStates,
        boolean includeInvalidStates,
        boolean includeNonDeletedStates,
        boolean includeDeletedStates
    ){
        if(
            (!includeValidStates && !includeInvalidStates) ||
            (!includeNonDeletedStates && !includeDeletedStates)
        ) {
            return Collections.emptyList();
        }
        return getStates(
            referenceCollection,
            qualifier,
            includeInvalidStates && includeValidStates ? null : Boolean.valueOf(includeInvalidStates),
            includeDeletedStates && includeNonDeletedStates ? null : Boolean.valueOf(includeDeletedStates)
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
            Boolean.FALSE, // invalidated
            Boolean.FALSE, // deleted
            null, // existsAt
            true // asView
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
            Boolean.FALSE, // invalidated
            Boolean.FALSE, // deleted
            null, // existsAt
            true // asView
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
     * @param deleted tells whether persistent or persistent-deleted states are excluded<ul>
     * <li><code>null</code>: Neither persistent nor persistent-deleted states are excluded
     * <li><code>TRUE</code>: Persistent states are excluded
     * <li><code>FALSE</code>: Persistent-deleted states are excluded
     * </ul>
     * @param existsAt TODO
     * @param asView 
     * @return a collection of DateState instances
     */
    private static <T extends DateState> List<T> getStates(
        T dateState,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        Boolean invalidated, 
        Boolean deleted, 
        Date existsAt, 
        boolean asView
    ){
        Path resourceIdentifier = getResourceIdentifier(dateState);
        List<T> states = getStates(
            dateState,
            resourceIdentifier.getParent(),
            resourceIdentifier
        ); 
        return find(
            states,
            validFrom,
            validTo,
            invalidated,
            deleted, 
            existsAt, asView
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
    public static <T extends DateState> List<T>getStates(
        T dateState,
        Boolean invalidated, 
        Boolean deleted
    ){
        return dateState == null ? null : getStates(
            dateState,
            null, // validFrom
            null, // validTo
            invalidated,
            deleted, 
            null, // existsAt
            true // asView
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
     * @param includeNonDeletedStates tells whether the <code>deleted</code> 
     * predicate may be <code>false</code>
     * @param includeDeletedStates tells whether the <code>deleted</code> 
     * predicate may be <code>true</code>
     * 
     * @return a collection of DateState instances
     */
    public static Collection<? extends DateState> getStates(
        DateState dateState,
        boolean includeValidStates,
        boolean includeInvalidStates,
        boolean includeNonDeletedStates, 
        boolean includeDeletedStates
    ){
        if(dateState == null) {
            return null;
        }
        if(
            (!includeValidStates && !includeInvalidStates) ||
            (!includeNonDeletedStates && !includeDeletedStates)
        ) {
            return Collections.emptyList();
        }
        return getStates(
            dateState,
            includeInvalidStates && includeValidStates ? null : Boolean.valueOf(includeInvalidStates),
            includeDeletedStates && includeNonDeletedStates ? null : Boolean.valueOf(includeDeletedStates)
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
    public static <T extends DateState> List<T> getStates(
        T dateState,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo
    ){
        return dateState == null ? null : getStates(
            dateState,
            validFrom,
            validTo,
            Boolean.FALSE, // invalidated
            Boolean.FALSE, // deleted
            null, // existsAt
            true // asView
        );
    }    
    
    /**
     * Retrieve valid non-deleted states
     * 
     * @param dateState a plain JMI object or a date state view
     * 
     * @return a list of DateState instances
     */
    public static <T extends DateState> List<T> getStates(
        T dateState
    ){
        return dateState == null ? null : getStates(
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
     * @return <code>true</code> unless one of the following conditionsis met<ul>
     * <li><code>refObject</code> is <code>null</code> 
     * <li><code>refObject is deleted</code> 
     * is <code>true</code> 
     * <li><code>refObject instanceof DateState && isView(refObject)</code> is <code>true</code> 
     * and the view does not refer to exactly one valid state 
     * </ul>
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
        List<?> involved = getStates(
            (DateState)refObject, 
            context.getValidFrom(), 
            context.getValidTo()
        );
        switch(involved.size()) {
            case 0: return false;
            case 1: return true;
            default : return false;
        }
    }
    
    /**
     * Clone an object 
     * 
     * @param refObject view to the object to be cloned
     *
     * @return the clone's transient resource identifier
     */
    private static Path getResourceIdentifierOfClone(
        DateState refObject
    ){
        if(refObject != null && !JDOHelper.isDeleted(refObject)){
            DateStateContext context = getContext(refObject);
            if(context != null) {
                List<DateState> involved = context.getViewKind() == ViewKind.TIME_POINT_VIEW ? getStates(
                    refObject, 
                    context.getValidAt(), 
                    context.getValidAt(),
                    null, // invalidated
                    Boolean.FALSE, // deleted 
                    context.getExistsAt(), 
                    false // asView
                ) : getStates(
                    refObject, 
                    context.getValidFrom(), 
                    context.getValidTo(),
                    Boolean.FALSE, // invalidated
                    Boolean.FALSE, // deleted 
                    null, // existstAt
                    false // asView
                );
                if(involved.size() == 1) {
                    return getResourceIdentifier(
                        PersistenceHelper.clone(
                            involved.get(0)
                        )
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
     * Filter and order date state instances
     * 
     * @param dateStates a collection of date state instances
     * @param validFrom
     * @param validTo
     * @param existsAt TODO
     * @param asView TODO
     * @return a list of DateState instances
     */
    private static <T extends DateState> List<T> find(
        Collection<T> dateStates,
        XMLGregorianCalendar validFrom,
        XMLGregorianCalendar validTo,
        Boolean invalidated,
        Boolean deleted, 
        Date existsAt, 
        boolean asView
    ){
        SortedSet<T> set = new TreeSet<T>(getComparator());
        for(T state : dateStates) {
            if(deleted == null || (deleted.booleanValue() == JDOHelper.isDeleted(state))) {
                if(
                    JDOHelper.isDeleted(state) || (    
                        (invalidated == null || invalidated.booleanValue() == (state.getRemovedAt() != null)) &&
                        Order.compareValidFromToValidTo(validFrom, state.getStateValidTo()) <= 0 &&
                        Order.compareValidFromToValidTo(state.getStateValidFrom(), validTo) <= 0 &&
                        (asView || (existsAt == null ? state.getRemovedAt() == null : (
                              existsAt.compareTo(state.getCreatedAt()) >= 0 && 
                              Order.compareRemovedAt(existsAt, state.getRemovedAt()) < 0
                         )))
                    )
                ) {
                    set.add(state);
                }
            } 
        }
        List<T> list = new ArrayList<T>();
        for(T t : set) {
            list.add(
                asView ? getViewForState(t) : t
            );
        }
        return list;
    }
    
    /**
     * Derive the filter from the state context
     * 
     * @param interactionSpec
     * @param instanceOf 
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
    // Class ValidFromComparator
    //------------------------------------------------------------------------
    
    /**
     * Date State Valid From Comparator 
     */
    final static class StateComparator
        implements Comparator<DateState>, Serializable
    {

        /**
         * Constructor 
         */
        private StateComparator(){
            // Avoid external instantiation
        }

        /**
         * A singleton
         */
        final static Comparator<DateState> instance = new StateComparator();
                 
        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = 8801286952504763272L;

        /**
         * Implements <code>Comparable</code>
         */
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

}
