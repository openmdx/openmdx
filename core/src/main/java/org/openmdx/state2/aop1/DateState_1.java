/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Date State
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
import static org.openmdx.state2.spi.TechnicalAttributes.STATE_VALID_FROM;
import static org.openmdx.state2.spi.TechnicalAttributes.STATE_VALID_TO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jdo.JDOUserException;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.state2.cci.DateStateContext;
import org.openmdx.state2.cci.ViewKind;
import org.openmdx.state2.spi.Order;

#if CLASSIC_CHRONO_TYPES
import org.w3c.spi.DatatypeFactories;
#endif

/**
 * Date State Plug-In
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
    private static final #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif NULL = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar();

    private static final List<String> IGNORABLE_ATTRIBUTES = Arrays.asList(
        STATE_VALID_FROM, STATE_VALID_TO,
        CREATED_AT, CREATED_BY,
        REMOVED_AT, REMOVED_BY
    );
        
    /**
     * Compare two XMLGregorianCalendar values where {@code null} is
     * considered to be smaller than every other value.
     */
    private static final Comparator<#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif> VALID_FROM_COMPARATOR = new Comparator<#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif> () {

        @Override
        public int compare(
            #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif o1,
            #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif o2
        ) {
            return Order.compareValidFrom(o1, o2);
        }
    };


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
        if(dataObject.objGetValue(STATE_VALID_FROM) == null) {
            dataObject.objSetValue(STATE_VALID_FROM, context.getValidFrom());
        }
        if(dataObject.objGetValue(STATE_VALID_TO) == null) {
            dataObject.objSetValue(STATE_VALID_TO, context.getValidTo());
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop2.core.BasicState_1#isValidTimeFeature(java.lang.String)
     */
    @Override
    protected boolean isValidTimeFeature(String featureName) {
        return STATE_VALID_FROM.equals(featureName) || STATE_VALID_TO.equals(featureName);
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
                if(STATE_VALID_FROM.equals(feature)) return  context.getValidFrom();
                if(STATE_VALID_TO.equals(feature)) return  context.getValidTo();
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
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif validFrom;
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif validTo;
        for(Map.Entry<DataObject_1_0,BoundaryCrossing> e : pending.entrySet()) {
            DataObject_1_0 source = e.getKey();
            BoundaryCrossing boundaryCrossing = e.getValue();
            //
            // Handle the period which is not yet involved
            //
            if(boundaryCrossing.startsEarlier) {
                DataObject_1_0 predecessor = PersistenceHelper.clone(source);
                predecessor.objSetValue(
                    STATE_VALID_TO, 
                    Order.predecessor(
                        validFrom = context.getValidFrom()
                    )
                );
                if(!predecessor.jdoIsNew()) {
                    addState(states, predecessor);
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
                    STATE_VALID_FROM, 
                    Order.successor(
                        validTo = context.getValidTo()
                    )
                );
                if(!successor.jdoIsNew()) {
                    addState(states, successor);
                }
            } else {
                validTo = NULL;
                
            }
            //
            // Handle the period which is involved
            //
            DataObject_1_0 target = PersistenceHelper.clone(source);
            if(validFrom != NULL) {
                target.objSetValue(STATE_VALID_FROM, validFrom);
            }
            if(validTo != NULL) {
                target.objSetValue(STATE_VALID_TO, validTo);
            }
            if(!target.jdoIsNew()) {
                addState(states, target);
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

	protected boolean addState(
		Collection<DataObject_1_0> states,
		DataObject_1_0 state
	) {
		try {
			return states.add(state);
		} catch (RuntimeException exception) {
			final BasicException stack = BasicException.toExceptionStack(exception);
			if(stack.getCause(null).getExceptionCode() == BasicException.Code.DUPLICATE) {
				final String message = "The technical property stateVersion has probably an incosistent value: It must not be smaller than the largest state number";
				throw new JDOUserException(
					message,	
					BasicException.newStandAloneExceptionStack(
						stack, 
						BasicException.Code.DEFAULT_DOMAIN, 
						BasicException.Code.ASSERTION_FAILURE, 
						message
					)
				);
			} else {
				throw exception;
			}
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
                (#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif) candidate.objGetValue(STATE_VALID_FROM),
                context.getValidFrom() 
            ) < 0,
            Order.compareValidTo(
                (#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif) candidate.objGetValue(STATE_VALID_TO),
                context.getValidTo() 
            ) > 0
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop1.BasicState_1#interfers(org.openmdx.base.accessor.cci.DataObject_1_0)
     */
    @Override
    protected boolean interfersWith(
        DataObject_1_0 candidate
    ) throws ServiceException {
        DateStateContext context = getContext();
        return 
            Order.compareValidFromToValidTo(context.getValidFrom(), (#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif) candidate.objGetValue(STATE_VALID_TO)) <= 0 &&
            Order.compareValidFromToValidTo((#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif) candidate.objGetValue(STATE_VALID_FROM), context.getValidTo()) <= 0;
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop1.AbstractState_1#isInvolved()
     */
    @Override
    protected boolean isInvolved(
        DataObject_1_0 candidate, 
        DateStateContext context, 
        AccessMode accessMode
    ) throws ServiceException {
        if(super.isInvolved(candidate, context, accessMode)) {
            //
            // Valid Time Test
            // 
            switch(context.getViewKind()) {
                case TIME_POINT_VIEW:
                    #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif validAt = context.getValidAt();
                    return Order.compareValidFrom(
                        (#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif) candidate.objGetValue(STATE_VALID_FROM),
                        validAt
                    ) <= 0 && Order.compareValidTo(
                        validAt,
                        (#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif) candidate.objGetValue(STATE_VALID_TO)
                    ) <= 0;
                case TIME_RANGE_VIEW:
                	return accessMode == AccessMode.UNDERLYING_STATE ? (
                        Order.compareValidFrom(
                            context.getValidFrom(), 
                            (#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif) candidate.objGetValue(STATE_VALID_FROM)
                        ) >= 0 && Order.compareValidTo(
                            (#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif) candidate.objGetValue(STATE_VALID_TO),
                            context.getValidTo()
                        ) >= 0 
                    ) : (
                		Order.compareValidFromToValidTo(
	                        context.getValidFrom(), 
	                        (#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif) candidate.objGetValue(STATE_VALID_TO)
	                    ) <= 0 && Order.compareValidFromToValidTo(
	                        (#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif) candidate.objGetValue(STATE_VALID_FROM),
	                        context.getValidTo()
	                    ) <= 0 
                    );
        		default:
        			throw new RuntimeServiceException(
        				BasicException.Code.DEFAULT_DOMAIN,
        				BasicException.Code.ASSERTION_FAILURE,
        				"Unexpected view kind",
        				new BasicException.Parameter("context", context)
        			);
            }
        } else {
	        return false;
        }
    }

    /**
     * Reduce number of states
     */
    @Override
    protected void reduceStates(
    ) throws ServiceException {
        final Collection<DataObject_1_0> states = getStates();
        reduceStatesToBeRemoved(states); // before merging
        if(reduceActiveStates(states)) { // merge
            reduceStatesToBeRemoved(states); // after merging
        }
    }

    /**
     * Merge similar adjacent states
     */
    private boolean reduceActiveStates(
        final Collection<DataObject_1_0> states
    ) throws ServiceException {
        boolean reduced = false;
        final SortedMap<#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif, DataObject_1_0> active = getActiveStates(states);
        if(active.size() > 1) {
            final Iterator<DataObject_1_0> i = active.values().iterator();
            final List<DataObject_1_0> merged = new ArrayList<DataObject_1_0>();
            for(
                DataObject_1_0 predecessor = i.next(), successor = null;
                predecessor != null;
                predecessor = successor
            ){
                final boolean merge;
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
                    final DataObject_1_0 extendable = getExtendableState(merged);
                    //
                    // Avoid increasing the total number of states by merging
                    //
                    if(extendable != null) {
                        reduced = true;
                        //
                        // Merging reduces the number of states
                        //
                        extendable.objSetValue(
                            STATE_VALID_FROM,
                            merged.get(0).objGetValue(STATE_VALID_FROM)
                        );
                        extendable.objSetValue(
                            STATE_VALID_TO,
                            merged.get(merged.size()-1).objGetValue(STATE_VALID_TO)
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
        return reduced;
    }

    /**
     * Re-activate states similar to active states
     */
    private void reduceStatesToBeRemoved(
        final Collection<DataObject_1_0> states
    ) throws ServiceException {
        final SortedMap<#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif, DataObject_1_0> toBeRemoved = getStatesToBeRemoved(states);
        if(!toBeRemoved.isEmpty()) {
            final SortedMap<#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif, DataObject_1_0> newStates = getNewStates(states);
            if(!newStates.isEmpty()) { // Accessing the unmodifiable empty map would result in a ClassCastExcept
                for(Map.Entry<#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif, DataObject_1_0> e : toBeRemoved.entrySet()) {
                    final DataObject_1_0 newState = newStates.get(e.getKey());
                    if(newState != null) {
                        final DataObject_1_0 oldState = e.getValue();
                        final #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif newStateValidTo = (#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif)newState.objGetValue(STATE_VALID_TO);
                        final #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif oldStateValidTo = (#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif)oldState.objGetValue(STATE_VALID_TO);
                        if(
                            Order.compareValidTo(newStateValidTo,oldStateValidTo) == 0 &&
                            similar(newState, oldState)
                        ){
                            invalidate(newState);
                            reactivate(oldState);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Retrieve the active states
     * 
     * @param states the object's states
     * 
     * @return the active states ordered by {@code STATE_VALID_FROM}
     * 
     * @throws ServiceException if {@code STATE_VALID_FROM} can't be retrieved
     */
    private SortedMap<#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif,DataObject_1_0> getActiveStates(
        final Collection<DataObject_1_0> states
    ) throws ServiceException {
        final SortedMap<#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif,DataObject_1_0> activeStates = new TreeMap<#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif,DataObject_1_0>(VALID_FROM_COMPARATOR);
        for(DataObject_1_0 state : states){
            if(isActive(state)) {
                activeStates.put((#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif)state.objGetValue(STATE_VALID_FROM), state);
            }
        }
        return activeStates;
    }

    /**
     * Retrieve new states
     * 
     * @param states the object's states
     * 
     * @return new states ordered by {@code STATE_VALID_FROM}
     * 
     * @throws ServiceException if {@code STATE_VALID_FROM} can't be retrieved
     */
    private SortedMap<#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif,DataObject_1_0> getNewStates(
        final Collection<DataObject_1_0> states
    ) throws ServiceException {
        SortedMap<#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif,DataObject_1_0> newStates = null;
        for(DataObject_1_0 state : states){
            if(isNew(state)) {
                if(newStates == null) {
                    newStates = new TreeMap<#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif,DataObject_1_0>(VALID_FROM_COMPARATOR);
                }
                newStates.put((#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif)state.objGetValue(STATE_VALID_FROM), state);
            }
        }
        return newStates == null ? Collections.emptySortedMap() : newStates;
    }
    
    /**
     * Retrieve the states to be removed at the end of this unit of work 
     * 
     * @param states the object's states
     * 
     * @return the states to be removed ordered by {@code STATE_VALID_FROM}
     * 
     * @throws ServiceException if {@code STATE_VALID_FROM} can't be retrieved
     */
    private SortedMap<#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif,DataObject_1_0> getStatesToBeRemoved(
        final Collection<DataObject_1_0> states
    ) throws ServiceException {
        SortedMap<#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif,DataObject_1_0> toBeRemoved = null;
        for(DataObject_1_0 state : states){
            if(isToBeRemoved(state)){
                if(toBeRemoved == null) {
                    toBeRemoved = new TreeMap<#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif,DataObject_1_0>(VALID_FROM_COMPARATOR);
                }
                toBeRemoved.put((#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif)state.objGetValue(STATE_VALID_FROM), state);
            }
        }
        return toBeRemoved == null ? Collections.emptySortedMap() : toBeRemoved;
    }
    
    private DataObject_1_0 getExtendableState(
        final List<DataObject_1_0> merged
    ) {
        for(DataObject_1_0 state : merged) {
            if(state.jdoIsNew()) {
                return state;
            }
        }
        return null;
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
     * @return {@code true} if the two states are dajacent
     * 
     * @throws ServiceException
     */
    protected boolean adjacent(
        DataObject_1_0 left,
        DataObject_1_0 right
    ) throws ServiceException{
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif leftEnd = (#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif) left.objGetValue(STATE_VALID_TO);
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif rightStart = (#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif) right.objGetValue(STATE_VALID_FROM);
        return rightStart.equals(Order.successor(leftEnd));
    }
    
    /**
     * Tests whether the two objects are similar
     * 
     * @param left
     * @param right
     * 
     * @return {@code true} if all attributes apart from the ones to be ignored are equal
     */
    protected boolean similar(
        DataObject_1_0 left,
        DataObject_1_0 right
    ) throws ServiceException{
        String type = left.objGetClass();
        if(!type.equals(right.objGetClass())) {
            return false;
        }
        Model_1_0 model = getModel();
        ModelElement_1_0 classifier = model.getElement(type);
        Map<String,Multiplicity> postponed = null;
        Set<String> leftFetched = left.objDefaultFetchGroup();
        Set<String> rightFetched = right.objDefaultFetchGroup();
        Collection<String> ignorable = ignorableAttributes();
        for(Map.Entry<String,ModelElement_1_0> feature : classifier.objGetMap("allFeature").entrySet()){
            ModelElement_1_0 featureDef = feature.getValue();
            String featureName = feature.getKey();
            if(!ignorable.contains(featureName) && this.isAttribute(featureDef) && !ModelHelper.isDerived(featureDef)) {
            	Multiplicity multiplicity = ModelHelper.getMultiplicity(featureDef);
                if(leftFetched.contains(featureName) && rightFetched.contains(featureName)) {
                	//
                	// Compare cached values first
                	//
                    if(!equal(featureName,multiplicity,left,right)) {
                        return false;
                    }
                } else {
                	//
                	// Compare values to be lazily fetched later on
                	//
                    if(postponed == null) {
                        postponed = new HashMap<String, Multiplicity>();
                    }
                    postponed.put(featureName, multiplicity);
                }
            }
        }
        if(postponed != null) {
            for(Map.Entry<String, Multiplicity> entry : postponed.entrySet()) {
                if(!equal(entry.getKey(),entry.getValue(),left,right)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Tells whether a feature is either an attribute or a reference stored as attribute.
     * 
     * @param featureDef
     * @return {@code true} if the feature is either an attribute or a reference stored as attribute
     * 
     * @throws ServiceException
     */
    private boolean isAttribute(
    	ModelElement_1_0 featureDef
    ) throws ServiceException {
    	Model_1_0 model = featureDef.getModel();
    	return model.isAttributeType(featureDef) || (
    		model.isReferenceType(featureDef) && model.referenceIsStoredAsAttribute(featureDef)
    	);
    }
    /**
     * 
     * @param attribute
     * @param multiplicity
     * @param left
     * @param right
     * 
     * @return {@code >true{@code  if the values are equal
     * @throws ServiceException
     */
    private boolean equal(
        String attribute, 
        Multiplicity multiplicity, 
        DataObject_1_0 left,
        DataObject_1_0 right
    ) throws ServiceException{
    	switch(multiplicity) {
	    	case LIST:
	    		return left.objGetList(attribute).equals(right.objGetList(attribute));
	    	case SET:
	    		return left.objGetSet(attribute).equals(right.objGetSet(attribute));
	    	case SPARSEARRAY:
	    		return left.objGetSparseArray(attribute).equals(right.objGetSparseArray(attribute));
	    	case MAP:
	    	    return left.objGetMap(attribute).equals(right.objGetMap(attribute));
	    	case STREAM:
	    	    return false; // we should not read streams in this context
	    	default:
	    		return equal(left.objGetValue(attribute),right.objGetValue(attribute));
    	}
    }
    
    /**
     * Tests whether the two objects are either equal or both {@code null}
     * 
     * @param left
     * @param right
     * 
     * @return {@code true} if the two objects are either equal or both {@code null}
     */
    static protected boolean equal(
        Object left,
        Object right
    ){
        if(left == null) {
            return right == null;
        } else {
            // Some datatype implementations don't accept null as equals argument!
            return right != null && left.equals(right);
        }
    }

}