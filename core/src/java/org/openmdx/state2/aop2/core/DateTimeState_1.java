/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: DateTimeState_1.java,v 1.2 2008/12/15 03:15:36 hburger Exp $
 * Description: Date State
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:36 $
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
package org.openmdx.state2.aop2.core;

import java.util.Date;
import java.util.Map;

import javax.resource.cci.InteractionSpec;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.spi.Object_1_5;
import org.openmdx.base.accessor.generic.spi.Object_1_6;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.state2.cci.DateTimeStateContext;
import org.openmdx.state2.cci.StateContext;
import org.openmdx.state2.spi.DateTimeStateViewContext;
import org.openmdx.state2.spi.StateViewContext;
import org.openmdx.state2.spi.ValidTimes;

/**
 * Registers the the delegates with their manager
 */
public class DateTimeState_1 extends AbstractState_1<DateTimeStateContext> {

    /**
     * Constructor 
     * 
     * @param viewObject
     * 
     * @throws ServiceException
     */
    public DateTimeState_1(
        Object_1_6 viewObject
    ) throws ServiceException{
        super(viewObject);
        Object_1_5 dataObject = self.objGetDelegate();
        if(!dataObject.objIsPersistent()) {
            InteractionSpec interactionSpec = self.getInteractionSpec();
            if(interactionSpec instanceof DateTimeStateContext) {
                DateTimeStateContext context = (DateTimeStateContext) interactionSpec;
                dataObject.objSetValue(STATE_VALID_FROM, context.getValidFrom());
                dataObject.objSetValue(STATE_INVALID_FROM, context.getInvalidFrom());
            }
        }
    }

    /**
     * org::openmdx::state2::DateTimeState's MOF id
     */
    public final static String CLASS = "org:openmdx:state2:DateTimeState";

    /**
     * The begin of the valid time range.
     */
    public static final String STATE_VALID_FROM = "stateValidFrom";

    /**
     * The end of the valid time range.
     */
    public static final String STATE_INVALID_FROM = "stateInvalidFrom";
    

    //------------------------------------------------------------------------
    // Extends BasicState_1View
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop2.core.BasicState_1#isValidTimeFeature(java.lang.String)
     */
    @Override
    protected boolean isValidTimeFeature(String featureName) {
        return STATE_VALID_FROM.equals(featureName) || STATE_INVALID_FROM.equals(featureName);
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop2.core.BasicState_1#objGetValue(java.lang.String)
     */
    @Override
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        return 
            STATE_VALID_FROM.equals(feature) ? ((DateTimeStateContext)self.getInteractionSpec()).getValidFrom() :
            STATE_INVALID_FROM.equals(feature) ? ((DateTimeStateContext)self.getInteractionSpec()).getInvalidFrom() :
            super.objGetValue(feature);
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.plugin.generic.BasicState_1View#enableUpdate(java.util.Map, int)
     */
    @Override
    protected void enableUpdate(
        Map<Object_1_0, BoundaryCrossing> pending
    ) throws ServiceException {
        Map<String,Object_1_0> states = getStates();
        DateTimeStateContext context = getContext();
        for(Map.Entry<Object_1_0,BoundaryCrossing> e : pending.entrySet()) {
            Object_1_0 source = e.getKey();
            BoundaryCrossing boundaryCrossing = e.getValue();
            //
            // Handle the period which is not yet involved
            //
            Object_1_0 predecessor;
            if(boundaryCrossing.startsEarlier) {
                predecessor = self.cloneDelegate(source, null);
                predecessor.objSetValue(STATE_INVALID_FROM, context.getValidFrom());
                states.put(newPlaceHolder(), predecessor);
            } else {
                predecessor = null;
            }
            //
            // Handle the period which is not longer involved
            //
            Object_1_0 successor;
            if(boundaryCrossing.endsLater) {
                successor = self.cloneDelegate(source, null);
                successor.objSetValue(STATE_VALID_FROM, context.getInvalidFrom());
                states.put(newPlaceHolder(), successor);
            } else {
                successor = null;
            }
            //
            // Handle the period which is involved
            //
            Object_1_0 target = self.cloneDelegate(source, null);
            target.objSetValue(STATE_VALID_FROM, context.getValidFrom());
            target.objSetValue(STATE_INVALID_FROM, context.getInvalidFrom());
            states.put(newPlaceHolder(), target);
            //
            // Replace states
            //
            source.objRemove();
        }
    }



    /* (non-Javadoc)
     * @see org.openmdx.state2.plugin.generic.BasicState_1View#exceedsTimeRangeLimits(org.openmdx.base.accessor.generic.cci.Object_1_0)
     */
    @Override
    protected BoundaryCrossing getBoundaryCrossing(
        Object_1_0 candidate
    ) throws ServiceException {
        DateTimeStateContext context = getContext();
        return BoundaryCrossing.valueOf(
            ValidTimes.compareValidFrom(
                (Date) candidate.objGetValue(STATE_VALID_FROM),
                context.getValidFrom() 
            ) < 0,
            ValidTimes.compareInvalidFrom(
                (Date) candidate.objGetValue(STATE_INVALID_FROM),
                context.getInvalidFrom() 
            ) > 0
        );
    }


    /* (non-Javadoc)
     * @see org.openmdx.state2.plugin.generic.BasicState_1View#isInvolved(org.openmdx.base.accessor.generic.cci.Object_1_0)
     */
    @Override
    protected boolean isInvolved(
        Object_1_0 candidate, 
        DateTimeStateContext context
     ) throws ServiceException {
        switch(context.getViewKind()) {
            case TIME_POINT_VIEW:
                return StateViewContext.compareTransactionTime(
                    context.getExistsAt(),
                    (Date)candidate.objGetValue(SystemAttributes.CREATED_AT),
                    (Date)candidate.objGetValue(SystemAttributes.REMOVED_AT)
                ) && ValidTimes.compareValidFrom(
                    (Date) candidate.objGetValue(STATE_VALID_FROM),
                    context.getValidAt()
                ) <= 0 && ValidTimes.compareInvalidFrom(
                    context.getValidAt(),
                    (Date) candidate.objGetValue(STATE_INVALID_FROM)
                ) <= 0;
            case TIME_RANGE_VIEW:
                return (
                    !candidate.objIsDeleted() && 
                    candidate.objGetValue(SystemAttributes.REMOVED_AT) == null
                ) && ValidTimes.compareValidFrom(
                    context.getValidFrom(), 
                    (Date) candidate.objGetValue(STATE_INVALID_FROM)
                ) <= 0 && ValidTimes.compareInvalidFrom(
                    (Date) candidate.objGetValue(STATE_VALID_FROM), 
                    context.getInvalidFrom()
                ) <= 0;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop2.core.BasicState_1#getInteractionSpec(org.openmdx.base.accessor.generic.cci.Object_1_0)
     */
    @Override
    protected StateContext<?> getInteractionSpec(
        Object_1_0 dataObject
    ) throws ServiceException {
        DateTimeStateContext stateContext;
        if(dataObject.objGetValue(SystemAttributes.REMOVED_AT) == null) {
            stateContext = DateTimeStateViewContext.newTimeRangeViewContext(
                (Date)dataObject.objGetValue(STATE_VALID_FROM), 
                (Date)dataObject.objGetValue(STATE_INVALID_FROM)
            );
        } else {
            stateContext = DateTimeStateViewContext.newTimePointViewContext(
                (Date)dataObject.objGetValue(STATE_VALID_FROM), 
                (Date)dataObject.objGetValue(SystemAttributes.CREATED_AT)
            );
        }
        return stateContext;
    }

}