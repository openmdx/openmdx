/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Preferences Root plug-in
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2012, OMEX AG, Switzerland
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

package org.openmdx.preferences2.aop2;

import java.util.Arrays;
import java.util.List;

import org.openmdx.base.aop2.AbstractObject;

/**
 * Root plug-in
 */
public class RootImpl<
    S extends org.openmdx.preferences2.jmi1.Root,
    N extends org.openmdx.preferences2.cci2.Root
> extends AbstractObject<S, N, Void> {

    /**
     * Constructor 
     *
     * @param same
     * @param next
     */
    public RootImpl(S same, N next) {
        super(same, next);
    }

    private static final List<String> TYPES = Arrays.asList(
        "user", "system"
    );
    
    /**
     * Sets a new value for the attribute <code>type</code>.
     * <p>
     * This attribute is not changeable, i.e. its value can be set as long as the object is <em>TRANSIENT</em> or <em>NEW</em>
     * @param type The non-null new value for attribute <code>type</code>.
     * 
     * @exception IllegalArgumentException if type is neither 'system' nor 'user'
     */
    public void setType(
      java.lang.String type
    ){
        if(TYPES.contains(type)) {
            nextObject().setType(type);
        } else {
            throw new IllegalArgumentException(
                "The type must be one of " + TYPES + ": " + type
            );
        }
    }
    
}
