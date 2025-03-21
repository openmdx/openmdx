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

import java.util.Collection;
import java.util.Map;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.state2.cci.DateTimeStateContext;
import org.openmdx.state2.spi.Order;
import org.openmdx.state2.spi.TechnicalAttributes;
import org.w3c.spi2.Datatypes;

/**
 * Registers the the delegates with their manager
 */
public class DateTimeState_1 extends BasicState_1<DateTimeStateContext> {

    /**
     * Constructor 
     */
    public DateTimeState_1(
        ObjectView_1_0 self, 
        Interceptor_1 next
    ) throws ServiceException{
        super(self, next);
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop2.core.AbstractState_1#propagateValidTime()
     */
    @Override
    protected void initialize(
        DataObject_1_0 dataObject
    ) throws ServiceException {
        DateTimeStateContext context = (DateTimeStateContext) self.getInteractionSpec();
        if(dataObject.objGetValue(TechnicalAttributes.STATE_VALID_FROM) == null) {
            dataObject.objSetValue(TechnicalAttributes.STATE_VALID_FROM, context.getValidFrom());
        }
        if(dataObject.objGetValue(TechnicalAttributes.STATE_INVALID_FROM) == null) {
            dataObject.objSetValue(TechnicalAttributes.STATE_INVALID_FROM, context.getInvalidFrom());
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop2.core.BasicState_1#isValidTimeFeature(java.lang.String)
     */
    @Override
    protected boolean isValidTimeFeature(String featureName) {
        return TechnicalAttributes.STATE_VALID_FROM.equals(featureName) || TechnicalAttributes.STATE_INVALID_FROM.equals(featureName);
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop2.core.BasicState_1#objGetValue(java.lang.String)
     */
    @Override
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        return 
            TechnicalAttributes.STATE_VALID_FROM.equals(feature) ? ((DateTimeStateContext)self.getInteractionSpec()).getValidFrom() :
                TechnicalAttributes.STATE_INVALID_FROM.equals(feature) ? ((DateTimeStateContext)self.getInteractionSpec()).getInvalidFrom() :
            super.objGetValue(feature);
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.plugin.generic.BasicState_1View#enableUpdate(java.util.Map, int)
     */
    @Override
    protected void enableUpdate(
        Map<DataObject_1_0, BoundaryCrossing> pending
    ) throws ServiceException {
        Collection<DataObject_1_0> states = getStates();
        DateTimeStateContext context = getContext();
        for(Map.Entry<DataObject_1_0,BoundaryCrossing> e : pending.entrySet()) {
            DataObject_1_0 source = e.getKey();
            BoundaryCrossing boundaryCrossing = e.getValue();
            //
            // Handle the period which is not yet involved
            //
            DataObject_1_0 predecessor;
            if(boundaryCrossing.startsEarlier) {
                predecessor = PersistenceHelper.clone(source);
                predecessor.objSetValue(TechnicalAttributes.STATE_INVALID_FROM, context.getValidFrom());
                if(!predecessor.jdoIsNew()) {
                    states.add(predecessor);
                }
            } else {
                predecessor = null;
            }
            //
            // Handle the period which is not longer involved
            //
            DataObject_1_0 successor;
            if(boundaryCrossing.endsLater) {
                successor = PersistenceHelper.clone(source);
                successor.objSetValue(TechnicalAttributes.STATE_VALID_FROM, context.getInvalidFrom());
                if(!successor.jdoIsNew()) {
                    states.add(successor);
                }
            } else {
                successor = null;
            }
            //
            // Handle the period which is involved
            //
            DataObject_1_0 target = PersistenceHelper.clone(source);
            target.objSetValue(TechnicalAttributes.STATE_VALID_FROM, context.getValidFrom());
            target.objSetValue(TechnicalAttributes.STATE_INVALID_FROM, context.getInvalidFrom());
            if(!target.jdoIsNew()) {
                states.add(target);
            }
            //
            // Replace states
            //
            ReducedJDOHelper.getPersistenceManager(source).deletePersistent(source);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.plugin.generic.BasicState_1View#exceedsTimeRangeLimits(org.openmdx.base.accessor.generic.cci.Object_1_0)
     */
    @Override
    protected BoundaryCrossing getBoundaryCrossing(
        DataObject_1_0 candidate
    ) throws ServiceException {
        DateTimeStateContext context = getContext();
        return BoundaryCrossing.valueOf(
            Order.compareValidFrom(
                Datatypes.DATE_TIME_CLASS.cast(candidate).objGetValue(TechnicalAttributes.STATE_VALID_FROM),
                context.getValidFrom() 
            ) < 0,
            Order.compareInvalidFrom(
                Datatypes.DATE_TIME_CLASS.cast(candidate).objGetValue(TechnicalAttributes.STATE_INVALID_FROM),
                context.getInvalidFrom() 
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
        DateTimeStateContext context = getContext();
        return 
            Order.compareValidFromToValidTo(context.getValidFrom(), Datatypes.DATE_TIME_CLASS.cast(candidate).objGetValue(TechnicalAttributes.STATE_VALID_TO)) < 0 &&
            Order.compareValidFromToValidTo(Datatypes.DATE_TIME_CLASS.cast(candidate).objGetValue(TechnicalAttributes.STATE_VALID_FROM), context.getInvalidFrom()) < 0;
    }

    
    /* (non-Javadoc)
     * @see org.openmdx.state2.aop1.AbstractState_1#isInvolved()
     */
    @Override
    protected boolean isInvolved(
        DataObject_1_0 candidate, 
        DateTimeStateContext context, 
        AccessMode accessMode
     ) throws ServiceException {
        if(super.isInvolved(candidate, context, accessMode)) {
            //
            // Valid Time Test
            // 
            switch(context.getViewKind()) {
                case TIME_POINT_VIEW:
                    return Order.compareValidFrom(
                        Datatypes.DATE_TIME_CLASS.cast(candidate).objGetValue(TechnicalAttributes.STATE_VALID_FROM),
                        context.getValidAt()
                    ) <= 0 && Order.compareInvalidFrom(
                        context.getValidAt(),
                        Datatypes.DATE_TIME_CLASS.cast(candidate).objGetValue(TechnicalAttributes.STATE_INVALID_FROM)
                    ) <= 0;
                case TIME_RANGE_VIEW:
                	return accessMode == AccessMode.UNDERLYING_STATE ? (
                        Order.compareValidFrom(
                            context.getValidFrom(), 
                            Datatypes.DATE_TIME_CLASS.cast(candidate).objGetValue(TechnicalAttributes.STATE_VALID_FROM)
                        ) >= 0 && Order.compareInvalidFrom(
                            Datatypes.DATE_TIME_CLASS.cast(candidate).objGetValue(TechnicalAttributes.STATE_INVALID_FROM),
                            context.getInvalidFrom()
                        ) >= 0 
                    ) : (
                		Order.compareValidFromToValidTo(
	                        context.getValidFrom(), 
	                        Datatypes.DATE_TIME_CLASS.cast(candidate).objGetValue(TechnicalAttributes.STATE_VALID_TO)
	                    ) <= 0 && Order.compareValidFromToValidTo(
	                        Datatypes.DATE_TIME_CLASS.cast(candidate).objGetValue(TechnicalAttributes.STATE_VALID_FROM),
	                        context.getInvalidFrom()
	                    ) <= 0 
                    );
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop1.AbstractState_1#reduceStates()
     */
    @Override
    protected void reduceStates(
    ){

    }

}