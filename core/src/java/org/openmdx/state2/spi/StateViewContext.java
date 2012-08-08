/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: StateViewContext.java,v 1.4 2009/03/03 15:24:38 hburger Exp $
 * Description: AbstractStateContext 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/03 15:24:38 $
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
package org.openmdx.state2.spi;

import java.util.Date;

import javax.resource.cci.InteractionSpec;

import org.openmdx.base.text.format.DateFormat;
import org.openmdx.state2.cci.StateContext;
import org.openmdx.state2.cci.ViewKind;

/**
 * AbstractStateContext
 */
@SuppressWarnings("serial")
public abstract class StateViewContext<V>
    implements InteractionSpec, StateContext<V>
{

    /**
     * Constructor 
     *
     * @param viewKind
     * @param existsAt
     */
    protected StateViewContext(
        ViewKind viewKind, 
        V validAt,
        Date existsAt,
        V lowerBound,
        V upperBound,
        boolean includeUpperBound
    ) {
        this.viewKind = viewKind;
        this.validAt = validAt;
        this.existsAt = existsAt; 
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        StringBuilder id = new StringBuilder();
        switch (viewKind) {
            case TIME_POINT_VIEW:
                if(validAt == null) {
                    id.append('?');
                } else {
                    id.append(toString(validAt));
                }
                if(existsAt != null){
                    id.append(
                        '@'
                    ).append(
                        DateFormat.getInstance().format(getExistsAt())
                    );
                }
                break;
            case TIME_RANGE_VIEW:
                if(lowerBound == null) {
                    id.append("(-\u221E");
                } else {
                    id.append(
                        '['
                    ).append(
                        toString(lowerBound)
                    );
                }
                id.append(',');
                if(upperBound == null) {
                    id.append("\u221E)");
                } else {
                    id.append(
                        toString(upperBound)
                    ).append(
                        includeUpperBound ? ']' : ')'
                    );
                }
                break;
        }
        this.id = id.toString();
    }
    
    /**
     * 
     */
    private final ViewKind viewKind;

    /**
     * 
     */
    private final V validAt;

    /**
     * 
     */
    private final Date existsAt;

    /**
     * validFrom
     */
    private final V lowerBound;
    
    /**
     * validTo or invalidFrom
     */
    private final V upperBound;
    
    /**
     * The state context's id
     */
    private final String id;
    
    /**
     * Tests the transaction time
     * 
     * @param existsAt <code>null</code> stands for the "head" 
     * @param createdAt 
     * @param removedAt
     * 
     * @return <code>true</code> if the entry exists at the given transaction time
     */
    public static boolean compareTransactionTime(
        Date existsAt,
        Date createdAt,
        Date removedAt
    ){
        return existsAt == null ? removedAt == null : (
             (createdAt != null && !existsAt.before(createdAt)) &&
             (removedAt == null || existsAt.before(removedAt))
        );
            
    }
        
        
    
    //------------------------------------------------------------------------
    // Implements StateContext
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.state2.cci.StateContext#getViewKind()
     */
    public ViewKind getViewKind() {
        return this.viewKind;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.state2.cci.StateContext#getExistsAt()
     */
    public Date getExistsAt() {
        return this.existsAt;
    }

    /**
     * A time point view's <code>validAt</code>
     * 
     * @return <code>validAt</code> in case of a <code>TIME_POINT_VIEW</code>,
     * <code>null</code> otherwise
     */
    public V getValidAt(
    ){
        return this.validAt;
    }

    /**
     * Tells from when on modifications are valid
     * 
     * @return the view's validFrom
     */
    public V getValidFrom(
    ){
        return this.lowerBound;
    }

    /**
     * Tells up to when modifications are valid
     * 
     * @return the view's validFrom
     */
    protected V getUpperBound(
    ){
        return this.upperBound;
    }
    
    /**
     * Convert a valid time point into its XML format
     * 
     * @param timePoint
     * 
     * @return the time points string representation
     */
    protected abstract String toString(
        V timePoint
    );
    
    
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return 
            obj instanceof StateViewContext &&
            this.id.equals(((StateViewContext<?>)obj).id);
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.id;
    }

}
