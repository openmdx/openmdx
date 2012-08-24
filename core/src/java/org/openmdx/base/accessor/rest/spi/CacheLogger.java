/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: CacheLogger 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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

package org.openmdx.base.accessor.rest.spi;

import java.util.logging.Level;

import javax.cache.CacheListener;

import org.openmdx.kernel.log.SysLog;


/**
 * Cache Logger
 */
public class CacheLogger implements CacheListener {

    /**
     * Constructor 
     *
     * @param mode
     * @param level
     */
    CacheLogger(
        String mode,
        Level level
    ){
        this.mode = mode;
        this.level = level;
    }

    /**
     * Constructor 
     */
    CacheLogger(
    ){
        this("ANY", Level.FINE);
    }
    
    /**
     * 
     */
    private String mode;
    
    /**
     * 
     */
    private Level level;
    
    /* (non-Javadoc)
     * @see javax.cache.CacheListener#onClear()
     */
//  @Override
    public void onClear() {
        SysLog.log(this.level, "Clear {0} cache", this.mode);
    }

    /* (non-Javadoc)
     * @see javax.cache.CacheListener#onEvict(java.lang.Object)
     */
//  @Override
    public void onEvict(Object key) {
        SysLog.log(this.level, "Evict {1} from {0} cache", this.mode, key);
    }

    /* (non-Javadoc)
     * @see javax.cache.CacheListener#onLoad(java.lang.Object)
     */
//  @Override
    public void onLoad(Object key) {
        SysLog.log(this.level, "Load {1} into {0} cache", this.mode, key);
    }

    /* (non-Javadoc)
     * @see javax.cache.CacheListener#onPut(java.lang.Object)
     */
//  @Override
    public void onPut(Object key) {
        SysLog.log(this.level, "Put {1} to {0} cache", this.mode, key);
    }

    /* (non-Javadoc)
     * @see javax.cache.CacheListener#onRemove(java.lang.Object)
     */
//  @Override
    public void onRemove(Object key) {
        SysLog.log(this.level, "Remove {1} from {0} cache", this.mode, key);
    }

}
