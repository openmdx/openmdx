/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: UniqueValue.java,v 1.2 2008/12/15 03:15:36 hburger Exp $
 * Description: Unique
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

/**
 * Unique
 */
final class UniqueValue<T> {

    /**
     * Constructor 
     */
    UniqueValue(){
        super();
    }
    
    /**
     * 
     */
    private T value = null;

    /**
     * 
     */
    private boolean empty = true;
    
    /**
     * Process a single state's reply
     * 
     * @param value
     */
    void set(
        T value
    ){
        if(empty) {
            this.value = value;
            this.empty = false;
        } else if (this.value == null ? value != null : !this.value.equals(value)) {
            throw new IllegalStateException(
                "The underlying states have inconsistent values for the given request" 
            );
        }
    }
    
    /**
     * Retrieve the consolidated reply
     * 
     * @return the value returned by all underlying states
     * 
     * @exception IllegalStateException if there is no underlying state
     */
    T get(
    ){
        if(this.empty) {
            throw new IllegalStateException(
                "There is no underlying state for the given request" 
            );
        } else {
            return this.value;
        }
    }

    /**
     * Tells whether no value has been set
     * 
     * @return <code>true</code> if no value has been set
     */
    boolean isEmpty(
    ){
        return this.empty;
    }

}