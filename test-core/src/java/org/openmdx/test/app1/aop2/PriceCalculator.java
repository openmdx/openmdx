/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PriceCalculator.java,v 1.1 2009/02/04 11:06:37 hburger Exp $
 * Description: TariffCalculator 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/04 11:06:37 $
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
package org.openmdx.test.app1.aop2;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TariffCalculator
 */
public class PriceCalculator {
 
    /**
     * Constructor 
     */
    public PriceCalculator() {
        super();
    }

    /**
     * The number of milliseconds it takes to calculate the value
     */
    private long duration = 1000l;

    private final Logger logger = LoggerFactory.getLogger(PriceCalculator.class);
    
    /**
     * Retrieve duration.
     *
     * @return Returns the duration.
     */
    public final long getDuration() {
        return this.duration;
    }

    /**
     * Set duration.
     * 
     * @param duration The duration to set.
     */
    public final void setDuration(long duration) {
        this.duration = duration;
    }

    public BigDecimal getPrice(
        String mofId
    ){
        if(mofId == null || mofId.length() == 0) {
            return null;
        } else {
            try {
                long duration = getDuration();
                //
                // Simulate an expensive calculation
                //
                Thread.sleep(getDuration());
                logger.debug("The price has been calculated in {} ms", duration);
            } catch (InterruptedException exception) {
                logger.info("The price calculator has been iterrupted", exception);
            }
            char last = mofId.charAt(mofId.length() - 1);
            if(last >= '0' && last <= '9') {
                return BigDecimal.valueOf(1, '0' - last);
            } else {
                return PI;
            }
        }
            
    }
    
    private static final BigDecimal PI = new BigDecimal(Math.PI);
    
}
