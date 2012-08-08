/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefPackage_1_3.java,v 1.4 2008/02/08 16:51:25 hburger Exp $
 * Description: RefPackage interface 1.3
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/08 16:51:25 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2008, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.jmi.cci;

import javax.jdo.PersistenceManagerFactory;

/**
 * RefPackage interface 1.3
 */
public interface RefPackage_1_3
    extends RefPackage_1_2
{
    /**
     * Empty the cache
     */
    void clear();

    /**
     * Lookup the implementation specified by <code>qualifiedClassName</code> and create
     * an instance with refDelegate as delegate object. The Java package name is derived 
     * from the package name of the specified class and this RefPackage packageImpl 
     * mapping, i.e. refCreateImpl() returns an instance of org.openmdx.base.BasicObjectImpl 
     * if <code>qualifiedClassName</code> is <code>org:openmdx:base:BasicObject</code> 
     * the packageImpl mapping is set to [org:openmdx:base->org.openmdx.base]. 
     */
    Object refCreateImpl(
        String qualifiedClassName,
        RefObject_1_0 refDelegate
    );

    /**
     * @return config option useOpenMdx1ImplLookup
     */
    boolean refUseOpenMdx1ImplLookup();

    /**
     * @return config option bindingPackageSuffix
     */
    String refBindingPackageSuffix();

    /**
     * @return config option userContext
     */
    Object refUserContext();
    
    /**
     * Retrieves the JDO Persistence Manager Factory.
     * 
     * @return the JDO Persistence Manager Factory configured according to this package
     */
    PersistenceManagerFactory refPersistenceManagerFactory();
    
}
