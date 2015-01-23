/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Fetch Size 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013, OMEX AG, Switzerland
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
package org.openmdx.base.dataprovider.layer.persistence.jdbc.spi;

import javax.jdo.FetchPlan;

/**
 * Fetch Size
 */
public class FetchSize {
    
    
    /**
     * Constructor 
     *
     * @param fetchSizeFixed disabled with FetchPlan.FETCH_SIZE_OPTIMAL
     * @param fetchSizeGreedy
     * @param fetchSizeOptimal
     */
    public FetchSize(
        int fetchSizeFixed,
        int fetchSizeOptimal,
        int fetchSizeGreedy
    ){
        this.fetchSizeFixed = fetchSizeFixed;
        this.fetchSizeOptimal = fetchSizeOptimal; 
        this.fetchSizeGreedy = fetchSizeGreedy;
    }

    private final int fetchSizeFixed;
    private final int fetchSizeGreedy;
    private final int fetchSizeOptimal;
    
    /**
     * Determines whether the fetch size is fixed or variable
     *
     * @return <code>true</ccode> if the fetch size is fixed
     */
    public boolean isFixed(){
        return this.fetchSizeFixed != FetchPlan.FETCH_SIZE_OPTIMAL;
    }
    
    /**
     * Adjust the requested fetch size to the configuration
     * 
     * @param requested
     * 
     * @return the adjusted fetch size
     */
    public int adjust(
        int requested
    ){
        if(this.isFixed()) {
            return this.fetchSizeFixed;
        } else if(FetchPlan.FETCH_SIZE_GREEDY == requested || requested > this.fetchSizeGreedy) {
            return this.fetchSizeGreedy;
        } else if (FetchPlan.FETCH_SIZE_OPTIMAL == requested || requested < 1) {
            return this.fetchSizeOptimal;
        } else {
            return requested;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString(
    ) {
        return isFixed()  ?
            "The fetch size is fixed to " + this.fetchSizeFixed :
            "The fetch size is varies in the range 1 to " + this.fetchSizeGreedy + " with an optimal value of " + this.fetchSizeOptimal ;
    }
    
    
    
}
