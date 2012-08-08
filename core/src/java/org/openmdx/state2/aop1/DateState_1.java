/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: DateState_1.java,v 1.14 2009/11/04 16:00:14 hburger Exp $
 * Description: Date State
 * Revision:    $Revision: 1.14 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/11/04 16:00:14 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.state2.aop1;

import static org.openmdx.base.accessor.cci.SystemAttributes.CREATED_AT;
import static org.openmdx.base.accessor.cci.SystemAttributes.CREATED_BY;
import static org.openmdx.base.accessor.cci.SystemAttributes.REMOVED_AT;
import static org.openmdx.base.accessor.cci.SystemAttributes.REMOVED_BY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.JDOHelper;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.mof.spi.ModelUtils;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.state2.cci.DateStateContext;
import org.openmdx.state2.cci.ViewKind;
import org.openmdx.state2.spi.Order;
import org.w3c.spi.DatatypeFactories;

/**
 * Registers the the delegates with their manager
 */
public class DateState_1
    extends BasicState_1<DateStateContext> 
{

    /**
     * Constructor 
     * 
     * @param self
     * @throws ServiceException
     */
    public DateState_1(
        ObjectView_1_0 self, 
        Interceptor_1 next
    ) throws ServiceException{
        super(self, next);
    }

    /**
     * 
     */
    private static final XMLGregorianCalendar NULL = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar();

    private static final List<String> IGNORABLE_ATTRIBUTES = Arrays.asList(
        "stateValidFrom", "stateValidTo",
        CREATED_AT, CREATED_BY,
        REMOVED_AT, REMOVED_BY
    );
        
    
    //------------------------------------------------------------------------
    // Extends AbstractState_1
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop2.core.AbstractState_1#propagateValidTime()
     */
    @Override
    protected void initialize(
        DataObject_1_0 dataObject
    ) throws ServiceException {
        DateStateContext context = (DateStateContext) self.getInteractionSpec();
        if(dataObject.objGetValue("stateValidFrom") == null) {
            dataObject.objSetValue("stateValidFrom", context.getValidFrom());
        }
        if(dataObject.objGetValue("stateValidTo") == null) {
            dataObject.objSetValue("stateValidTo", context.getValidTo());
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop2.core.AbstractState_1#getStateClass()
     */
    @Override
    protected String getStateClass() {
        return "org:openmdx:state2:DateState";
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop2.core.BasicState_1#isValidTimeFeature(java.lang.String)
     */
    @Override
    protected boolean isValidTimeFeature(String featureName) {
        return "stateValidFrom".equals(featureName) || "stateValidTo".equals(featureName);
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop2.core.BasicState_1#objGetValue(java.lang.String)
     */
    @Override
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        if(isValidTimeFeature(feature)){
            DateStateContext context = (DateStateContext) self.getInteractionSpec();
            if(context.getViewKind() == ViewKind.TIME_RANGE_VIEW) {
                if("stateValidFrom".equals(feature)) return  context.getValidFrom();
                if("stateValidTo".equals(feature)) return  context.getValidTo();
            }
        }
        return super.objGetValue(feature);
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.plugin.AbstractState_1#enableUpdate(java.util.Collection, int)
     */
    @Override
    protected void enableUpdate(
        Map<DataObject_1_0,BoundaryCrossing> pending
    ) throws ServiceException {
        Collection<DataObject_1_0> states = getStates();
        DateStateContext context = getContext();
        XMLGregorianCalendar validFrom;
        XMLGregorianCalendar validTo;
        for(Map.Entry<DataObject_1_0,BoundaryCrossing> e : pending.entrySet()) {
            DataObject_1_0 source = e.getKey();
            BoundaryCrossing boundaryCrossing = e.getValue();
            //
            // Handle the period which is not yet involved
            //
            if(boundaryCrossing.startsEarlier) {
                DataObject_1_0 predecessor = PersistenceHelper.clone(source);
                predecessor.objSetValue(
                    "stateValidTo", 
                    Order.predecessor(
                        validFrom = context.getValidFrom()
                    )
                );
                if(!predecessor.jdoIsNew()) {
                    states.add(predecessor);
                }
            } else {
                validFrom = NULL;
            }
            //
            // Handle the period which is not longer involved
            //
            if(boundaryCrossing.endsLater) {
                DataObject_1_0 successor = PersistenceHelper.clone(source);
                successor.objSetValue(
                    "stateValidFrom", 
                    Order.successor(
                        validTo = context.getValidTo()
                    )
                );
                if(!successor.jdoIsNew()) {
                    states.add(successor);
                }
            } else {
                validTo = NULL;
            }
            //
            // Handle the period which is involved
            //
            DataObject_1_0 target = PersistenceHelper.clone(source);
            if(validFrom != NULL) {
                target.objSetValue("stateValidFrom", validFrom);
            }
            if(validTo != NULL) {
                target.objSetValue("stateValidTo", validTo);
            }
            if(!target.jdoIsNew()) {
                states.add(target);
            }
            //
            // Replace states
            //
            if(source.jdoIsPersistent()) {
                invalidate(source);
            } else {
                states.remove(source);
            }
        }
    }

    private void invalidate(
        DataObject_1_0 state
    ) throws ServiceException{
        if(state.jdoIsNew()) {
            JDOHelper.getPersistenceManager(state).deletePersistent(state);
        } else {
            state.objSetValue(REMOVED_AT, IN_THE_FUTURE);
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.state2.plugin.AbstractState_1#exceedsTimeRangeLimits(org.openmdx.base.accessor.generic.cci.Object_1_0)
     */
    @Override
    protected BoundaryCrossing getBoundaryCrossing(
        DataObject_1_0 candidate
    ) throws ServiceException {
        DateStateContext context = getContext();
        return BoundaryCrossing.valueOf(
            Order.compareValidFrom(
                (XMLGregorianCalendar) candidate.objGetValue("stateValidFrom"),
                context.getValidFrom() 
            ) < 0,
            Order.compareValidTo(
                (XMLGregorianCalendar) candidate.objGetValue("stateValidTo"),
                context.getValidTo() 
            ) > 0
        );
    }


    /* (non-Javadoc)
     * @see org.openmdx.state2.aop1.AbstractState_1#isInvolved()
     */
    @Override
    protected boolean isInvolved(
        DataObject_1_0 candidate, 
        DateStateContext context, 
        boolean removed
    ) throws ServiceException {
        if(super.isInvolved(candidate, context, removed)) {
            //
            // Valid Time Test
            // 
            switch(context.getViewKind()) {
                case TIME_POINT_VIEW:
                    XMLGregorianCalendar validAt = context.getValidAt();
                    return Order.compareValidFrom(
                        (XMLGregorianCalendar) candidate.objGetValue("stateValidFrom"),
                        validAt
                    ) <= 0 && Order.compareValidTo(
                        validAt,
                        (XMLGregorianCalendar) candidate.objGetValue("stateValidTo")
                    ) <= 0;
                case TIME_RANGE_VIEW:
                    return Order.compareValidFromToValidTo(
                        context.getValidFrom(), 
                        (XMLGregorianCalendar) candidate.objGetValue("stateValidTo")
                    ) <= 0 && Order.compareValidFromToValidTo(
                        (XMLGregorianCalendar) candidate.objGetValue("stateValidFrom"),
                        context.getValidTo()
                    ) <= 0;
            }
        }
        return false;
    }

    /**
     * Merge similar adjacent states
     */
    @Override
    protected void reduceStates(
    ) throws ServiceException {
        Collection<DataObject_1_0> states = getStates();
        SortedSet<DataObject_1_0> active = new TreeSet<DataObject_1_0>(StateComparator.getInstance());
        for(DataObject_1_0 state : states){
            if(!state.jdoIsDeleted() && state.objGetValue(SystemAttributes.REMOVED_AT) == null) {
                active.add(state);
            }
        }
        if(active.size() > 1) {
            Iterator<DataObject_1_0> i = active.iterator();
            List<DataObject_1_0> merged = new ArrayList<DataObject_1_0>();
            for(
                DataObject_1_0 predecessor = i.next(), successor = null;
                predecessor != null;
                predecessor = successor
            ){
                boolean merge;
                if(i.hasNext()) {
                    successor = i.next();
                    merge = adjacent(predecessor, successor) && similar(predecessor, successor);
                    if(merge) {
                        if(merged.isEmpty()) {
                            merged.add(predecessor);
                        }
                        merged.add(successor);
                    }
                } else {
                    merge = false;
                    successor = null;
                }
                if(!merge && !merged.isEmpty()) {
                    DataObject_1_0 extendable = null;
                    Extendable: for(DataObject_1_0 state : merged) {
                        if(state.jdoIsNew()) {
                            extendable = state;
                            break Extendable;
                        }
                    }
                    //
                    // Avoid increasing the total number of states by merging
                    //
                    if(extendable != null) {
                        //
                        // Merging reduces the number of states
                        //
                        extendable.objSetValue(
                            "stateValidFrom",
                            merged.get(0).objGetValue("stateValidFrom")
                        );
                        extendable.objSetValue(
                            "stateValidTo",
                            merged.get(merged.size()-1).objGetValue("stateValidTo")
                        );
                        for(DataObject_1_0 state : merged) {
                            if(state != extendable) {
                                invalidate(state);
                            }
                        }
                    }
                    merged.clear();
                }
            }
        }
    }

    /**
     * Tells which attributes shall be ignored when comparing two states for similarity
     * 
     * @return the set of attributes to  be ignored when comparing two states for similarity
     */
    protected Collection<String> ignorableAttributes(
    ){
        return IGNORABLE_ATTRIBUTES;
    }

    /**
     * Tells whether tow states follow each other immediately
     * 
     * @param left
     * @param right
     * 
     * @return <code>true</code> if the two states are dajacent
     * 
     * @throws ServiceException
     */
    protected boolean adjacent(
        DataObject_1_0 left,
        DataObject_1_0 right
    ) throws ServiceException{
        XMLGregorianCalendar leftEnd = (XMLGregorianCalendar) left.objGetValue("stateValidTo");
        XMLGregorianCalendar rightStart = (XMLGregorianCalendar) right.objGetValue("stateValidFrom");
        return rightStart.equals(Order.successor(leftEnd));
    }
    
    /**
     * Tests whether the two objects are similar
     * 
     * @param left
     * @param right
     * 
     * @return <code>true</code> if all attributes apart from the ones to be ignored are equal
     */
    protected boolean similar(
        DataObject_1_0 left,
        DataObject_1_0 right
    ) throws ServiceException{
        String type = left.objGetClass();
        if(!type.equals(right.objGetClass())) {
            return false;
        }
        Set<String> attributes = left.objDefaultFetchGroup();        
        attributes.addAll(right.objDefaultFetchGroup());
        attributes.removeAll(ignorableAttributes());
        Model_1_0 model = getModel();
        ModelElement_1_0 classifier = model.getElement(type);
        for(String attribute : attributes) {
            ModelElement_1_0 attributeDef = 
                attribute.indexOf(':') < 0 ? model.getFeatureDef(classifier, attribute, false) : 
                null;
            if(attributeDef != null && !Boolean.TRUE.equals(attributeDef.objGetValue("isDerived"))) {            
                String multiplicity = ModelUtils.getMultiplicity(attributeDef);
                boolean matching =  
                    Multiplicities.LIST.equals(multiplicity) ? left.objGetList(attribute).equals(right.objGetList(attribute)) :
                    Multiplicities.SET.equals(multiplicity) ? left.objGetSet(attribute).equals(right.objGetSet(attribute)) :
                    Multiplicities.SPARSEARRAY.equals(multiplicity) ? left.objGetSparseArray(attribute).equals(right.objGetSparseArray(attribute)) :
                    equal(left.objGetValue(attribute),right.objGetValue(attribute));
                if(!matching){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Tests whether the two objects are either equal or both <code>null</code>
     * 
     * @param left
     * @param right
     * 
     * @return <code>true</code> if the two objects are either equal or both <code>null</code>
     */
    static protected boolean equal(
        Object left,
        Object right
    ){
        return left == null ? right == null : left.equals(right);
    }

}