/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: NonManagedInitialContext.java,v 1.3 2009/11/11 18:38:26 hburger Exp $
 * Description: NonManagedInitialContext 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/11/11 18:38:26 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.kernel.lightweight.naming;

import java.util.Map;
import java.util.NoSuchElementException;

import javax.naming.Binding;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.openmdx.kernel.lightweight.naming.spi.NameBasedContext;

/**
 * NonManagedInitialContext
 */
class NonManagedInitialContext extends NameBasedContext {

    /**
     * Constructor 
     *
     * @param environment
     */
    NonManagedInitialContext(
        Map<?, ?> environment
    ) {
        super(environment);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.lightweight.naming.NameBasedContext#listBindings()
     */
    @Override
    protected NamingEnumeration<Binding> listBindings(
    ) throws NamingException {
        return new NamingEnumeration<Binding>(){

            public void close(
            ) throws NamingException {
            }

            public boolean hasMore(
            ) throws NamingException {
                return false;
            }

            public Binding next(
            ) throws NamingException {
                throw new NoSuchElementException();
            }

            public boolean hasMoreElements(
            ) {
                return false;
            }

            public Binding nextElement() {
                throw new NoSuchElementException();
            }
            
        };
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.lightweight.naming.NameBasedContext#resolveLink(java.lang.String)
     */
    @Override
    protected Object resolveLink(
        String nameComponent
    ) throws NamingException {
        int colon = nameComponent.indexOf(':');
        if(colon > 0) {
            String scheme = nameComponent.substring(0, colon);
            return NamingManager.getURLContext(scheme, environment).lookup(nameComponent);
        } else {
            throw new NameNotFoundException(
                "There are only URL contexts in a non-managed environment"
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.naming.Context#getNameInNamespace()
     */
    public String getNameInNamespace(
    ) throws NamingException {
        return "";
    }


}
