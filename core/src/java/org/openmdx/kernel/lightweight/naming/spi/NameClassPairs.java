/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: NameClassPairs.java,v 1.1 2009/09/11 13:16:23 hburger Exp $
 * Description: NameClassPairs 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/11 13:16:23 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2009, OMEX AG, Switzerland
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
package org.openmdx.kernel.lightweight.naming.spi;

import javax.naming.Binding;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * NameClassPairs
 */
public class NameClassPairs
    implements NamingEnumeration<NameClassPair>
{

    /**
     * Constructor 
     *
     * @param bindings the delegate
     */
    public NameClassPairs(
        NamingEnumeration<Binding> bindings
    ) {
        this.bindings = bindings;
    }

    /**
     * The delegate
     */
    private final NamingEnumeration<Binding> bindings;
    
    /* (non-Javadoc)
     * @see javax.naming.NamingEnumeration#close()
     */
    public void close(
    ) throws NamingException {
        this.bindings.close();
    }

    /* (non-Javadoc)
     * @see javax.naming.NamingEnumeration#hasMore()
     */
    public boolean hasMore(
    ) throws NamingException {
        return this.bindings.hasMore();
    }

    /* (non-Javadoc)
     * @see javax.naming.NamingEnumeration#next()
     */
    public NameClassPair next(
    ) throws NamingException {
        return marshal(this.bindings.next());
    }

    /* (non-Javadoc)
     * @see java.util.Enumeration#hasMoreElements()
     */
    public boolean hasMoreElements() {
        return this.bindings.hasMoreElements();
    }

    /* (non-Javadoc)
     * @see java.util.Enumeration#nextElement()
     */
    public NameClassPair nextElement() {
        return marshal(this.bindings.nextElement());
    }

    /**
     * Convert the binding to a name/class pair
     * 
     * @param binding
     * 
     * @return the corresponding name/class pair
     */
    private NameClassPair marshal(
        Binding binding
    ){
        return new NameClassPair(
            binding.getName(),
            binding.getClassName(),
            binding.isRelative()
        );
    }

}
