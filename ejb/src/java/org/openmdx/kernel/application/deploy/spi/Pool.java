/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Pool.java,v 1.1 2009/09/10 14:45:07 hburger Exp $
 * Description: Pool 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/10 14:45:07 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package org.openmdx.kernel.application.deploy.spi;

/**
 * An openMDX Pool 
 */
public interface Pool {
    
    /**
     * The initial-capacity element identifies the initial number 
     * of instance which the openMDX Container will 
     * attempt to obtain during deployment.
     * <p>
     * The default initial capacity is 1.
     * 
     * @return the initial number of pool instances
     */
    Integer getInitialCapacity();
    
    /**
     * The maximum-capacity element identifies the maximum number of 
     * managed connections which the openMDX Container will allow. 
     * Requests beyond this limit will result in an Exception being 
     * returned to the caller.
     * <p>
     * The default maximum capacity is java.lang.Integer.MAX_VALUE, 
     * i.e.2<sup>31</sup>-1.
     * 
     * @return the maximum number of pool instances that are allowed.
     */
    Integer getMaximumCapacity(
    );
    
    /**
     * The maximum-wait element defines the time in milliseconds to wait 
     * for an instance to be returned to the pool when there are 
     * maximum-capacity active instances.
     * <p>
     * A value of 0 will mean not to wait at all. When a request times out 
     * waiting for an instance an Exception is generated and the call aborted.
     * <p>
     * The default timeout value is java.lang.Long.MAX_VALUE, 
     * i.e. 2<sup>63</sup>-1.
     * 
     * @return time in milliseconds to wait for an instance to be returned 
     * to the pool when there are maximum-capacity active instances
     * 
     * @see #getMaximumCapacity()
     */
    Long getMaximumWait(
    ); 

    /**
     * Returns the cap on the number of "idle" instances in the pool.
     * @return the cap on the number of "idle" instances in the pool.
     */
    Integer getMaximumIdle();
    
    /**
     * Returns the minimum number of objects allowed in the pool
     * before the evictor thread (if active) spawns new objects.
     * (Note no objects are created when: numActive + numIdle >= maxActive)
     *
     * @return The minimum number of objects.
     */
    Integer getMinimumIdle();
    
    /**
     * When <tt>true</tt>, objects will be validated
     * before being returned by the borrowObject()
     * method.  If the object fails to validate,
     * it will be dropped from the pool, and we will attempt
     * to borrow another.
     */
    Boolean getTestOnBorrow();
    
    /**
     * When <tt>true</tt>, objects will be validated
     * before being returned to the pool within the
     * returnObject().
     */
    Boolean getTestOnReturn();
    
    /**
     * Returns the number of milliseconds to sleep between runs of the
     * idle object evictor thread.
     * When non-positive, no idle object evictor thread will be
     * run.
     */
    Long getTimeBetweenEvictionRuns();

    /**
     * Returns the number of objects to examine during each run of the
     * idle object evictor thread (if any).
     */
    Integer getNumberOfTestsPerEvictionRun();
    
    /**
     * Returns the minimum amount of time an object may sit idle in the pool
     * before it is eligable for eviction by the idle object evictor
     * (if any).
     */
    Long getMinimumEvictableIdleTime();
    
    /**
     * When <tt>true</tt>, objects will be validated
     * by the idle object evictor (if any).  If an object
     * fails to validate, it will be dropped from the pool.
     */
    Boolean getTestWhileIdle();
    
}