/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: CloneableObject_1.java,v 1.2 2007/10/23 10:27:20 hburger Exp $
 * Description: Cloneable Object 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/23 10:27:20 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.jmi.spi;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.spi.DelegatingObject_1;
import org.openmdx.compatibility.base.naming.Path;

/**
 * Cloneable Object
 * <p>
 * Attribute holder for another object's  initial values
 */
class CloneableObject_1
    extends DelegatingObject_1
{

    /**
     * Constructor 
     * @param identity 
     * @param object
     * @param completelyDirty
     */
    CloneableObject_1(
        Path identity, 
        Object_1_0 object, 
        boolean completelyDirty
    ) {
        super(object);
        this.identity = identity;
        this.completeyDirty = completelyDirty;
    }

    private final Path identity;
    private final boolean completeyDirty;
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.DelegatingObject_1#objGetDelegate()
     */
    public Object objGetDelegate() {
        return super.getDelegate();
    }

    /**
     * Retrieve identity.
     *
     * @return Returns the identity.
     */
    final Path getIdentity() {
        return this.identity;
    }
    

    /**
     * Retrieve completeyDirty.
     *
     * @return Returns the completeyDirty.
     */
    final boolean isCompleteyDirty() {
        return this.completeyDirty;
    }
    
}
