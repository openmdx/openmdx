/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: DateState_1.java,v 1.2 2008/12/15 03:15:36 hburger Exp $
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
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.spi.Object_1_5;
import org.openmdx.base.accessor.generic.spi.Object_1_6;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.state2.cci.DateStateContext;
import org.openmdx.state2.cci.StateContext;
import org.openmdx.state2.spi.DateStateContexts;
import org.openmdx.state2.spi.DateStateViewContext;
import org.openmdx.state2.spi.StateViewContext;
import org.openmdx.state2.spi.ValidTimes;
import org.w3c.spi.DatatypeFactories;

/**
 * Registers the the delegates with their manager
 */
public class DateState_1
    extends AbstractState_1<DateStateContext> 
{

    /**
     * Constructor 
     * 
     * @param self
     * @throws ServiceException
     */
    public DateState_1(
        Object_1_6 self
    ) throws ServiceException{
        super(self);
        Object_1_5 dataObject = self.objGetDelegate();
        if(!dataObject.objIsPersistent()) {
            InteractionSpec interactionSpec = self.getInteractionSpec();
            if(interactionSpec instanceof DateStateContext) {
                DateStateContext context = (DateStateContext) interactionSpec;
                dataObject.objSetValue(STATE_VALID_FROM, context.getValidFrom());
                dataObject.objSetValue(STATE_VALID_TO, context.getValidTo());
            }
        }
    }

    /**
     * org::openmdx::state2::DateState's MOF id
     */
    public final static String CLASS = "org:openmdx:state2:DateState";

    /**
     * The begin of the valid time range.
     */
    public static final String STATE_VALID_FROM = "stateValidFrom";

    /**
     * The end of the valid time range.
     */
    public static final String STATE_VALID_TO = "stateValidTo";
    
    /**
     * 
     */
    private static final XMLGregorianCalendar NULL = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar();

    
    //------------------------------------------------------------------------
    // Extends BasicState_1View
    //------------------------------------------------------------------------

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
        return 
            STATE_VALID_FROM.equals(feature) ? ((DateStateContext)self.getInteractionSpec()).getValidFrom() :
            STATE_VALID_TO.equals(feature) ? ((DateStateContext)self.getInteractionSpec()).getValidTo() :
            super.objGetValue(feature);
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop2.core.BasicState_1#getInteractionSpec(org.openmdx.base.accessor.generic.cci.Object_1_0)
     */
    @Override
    protected StateContext<?> getInteractionSpec(
        Object_1_0 dataObject
    ) throws ServiceException {
        DateStateContext stateContext;
         if(dataObject.objGetValue(SystemAttributes.REMOVED_AT) == null) {
             stateContext = DateStateViewContext.newTimeRangeViewContext(
                 (XMLGregorianCalendar)dataObject.objGetValue(STATE_VALID_FROM), 
                 (XMLGregorianCalendar)dataObject.objGetValue(STATE_VALID_TO)
             );
         } else {
             stateContext = DateStateViewContext.newTimePointViewContext(
                 (XMLGregorianCalendar)dataObject.objGetValue(STATE_VALID_FROM), 
                 (Date)dataObject.objGetValue(SystemAttributes.CREATED_AT)
             );
         }
         return stateContext;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.state2.plugin.AbstractState_1#enableUpdate(java.util.Collection, int)
     */
    @Override
    protected void enableUpdate(
        Map<Object_1_0,BoundaryCrossing> pending
    ) throws ServiceException {
        Map<String,Object_1_0> states = getStates();
        DateStateContext context = getContext();
        XMLGregorianCalendar validFrom;
        XMLGregorianCalendar validTo;
        for(Map.Entry<Object_1_0,BoundaryCrossing> e : pending.entrySet()) {
            Object_1_0 source = e.getKey();
            BoundaryCrossing boundaryCrossing = e.getValue();
            //
            // Handle the period which is not yet involved
            //
            if(boundaryCrossing.startsEarlier) {
                Object_1_0 predecessor = self.cloneDelegate(source, null);
                predecessor.objSetValue(
                    STATE_VALID_TO, 
                    DateStateContexts.predecessor(
                        validFrom = context.getValidFrom()
                    )
                );
                states.put(newPlaceHolder(), predecessor);
            } else {
                validFrom = NULL;
            }
            //
            // Handle the period which is not longer involved
            //
            if(boundaryCrossing.endsLater) {
                Object_1_0 successor = self.cloneDelegate(source, null);
                successor.objSetValue(
                    STATE_VALID_FROM, 
                    DateStateContexts.successor(
                        validTo = context.getValidTo()
                    )
                );
                states.put(newPlaceHolder(), successor);
            } else {
                validTo = NULL;
            }
            //
            // Handle the period which is involved
            //
            Object_1_0 target = self.cloneDelegate(source, null);
            if(validFrom != NULL) {
                target.objSetValue(STATE_VALID_FROM, validFrom);
            }
            if(validTo != NULL) {
                target.objSetValue(STATE_VALID_TO, validTo);
            }
            states.put(newPlaceHolder(), target);
            //
            // Replace states
            //
            if(source.objIsPersistent()) {
                source.objRemove();
            } else {
                states.values().remove(source);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.plugin.AbstractState_1#exceedsTimeRangeLimits(org.openmdx.base.accessor.generic.cci.Object_1_0)
     */
    @Override
    protected BoundaryCrossing getBoundaryCrossing(
        Object_1_0 candidate
    ) throws ServiceException {
        DateStateContext context = getContext();
        return BoundaryCrossing.valueOf(
            ValidTimes.compareValidFrom(
                (XMLGregorianCalendar) candidate.objGetValue(STATE_VALID_FROM),
                context.getValidFrom() 
            ) < 0,
            ValidTimes.compareValidTo(
                (XMLGregorianCalendar) candidate.objGetValue(STATE_VALID_TO),
                context.getValidTo() 
            ) > 0
        );
    }


    /* (non-Javadoc)
     * @see org.openmdx.state2.plugin.BasicState_1#isInvolved(org.openmdx.base.accessor.generic.cci.Object_1_0)
     */
    @Override
    protected boolean isInvolved(
        Object_1_0 candidate, 
        DateStateContext context
     ) throws ServiceException {
        switch(context.getViewKind()) {
            case TIME_POINT_VIEW:
                return (
                    context.getExistsAt() == null || (candidate.objIsPersistent() && !candidate.objIsNew())
                ) && StateViewContext.compareTransactionTime(
                    context.getExistsAt(),
                    (Date)candidate.objGetValue(SystemAttributes.CREATED_AT),
                    (Date)candidate.objGetValue(SystemAttributes.REMOVED_AT)
                ) && ValidTimes.compareValidFrom(
                    (XMLGregorianCalendar) candidate.objGetValue(STATE_VALID_FROM),
                    context.getValidAt()
                ) <= 0 && ValidTimes.compareValidTo(
                    context.getValidAt(),
                    (XMLGregorianCalendar) candidate.objGetValue(STATE_VALID_TO)
                ) <= 0;
            case TIME_RANGE_VIEW:
                return (
                    !candidate.objIsDeleted() &&
                    candidate.objGetValue(SystemAttributes.REMOVED_AT) == null
                ) && ValidTimes.compareValidFrom(
                    context.getValidFrom(), 
                    (XMLGregorianCalendar) candidate.objGetValue(STATE_VALID_TO)
                ) <= 0 && ValidTimes.compareValidTo(
                    context.getValidTo(),
                    (XMLGregorianCalendar) candidate.objGetValue(STATE_VALID_FROM) 
                ) >= 0;
        }
        return false;
    }
    
}