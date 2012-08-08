/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Jmi1Class_1_0.java,v 1.3 2009/06/09 12:45:17 hburger Exp $
 * Description: Jmi1Class 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 12:45:17 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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

package org.openmdx.base.accessor.jmi.spi;

import java.util.Set;

import javax.jdo.JDOFatalUserException;
import javax.jmi.reflect.RefClass;

import org.openmdx.base.accessor.jmi.spi.Jmi1ObjectInvocationHandler.StandardMarshaller;
import org.openmdx.base.mof.cci.ModelElement_1_0;

/**
 * Jmi1Class
 *
 */
public interface Jmi1Class_1_0 extends RefClass {
        
    /**
     * Returns the set of features which are recognized as having no implementations. 
     * The set is not managed by users of a RefClass_1_1 implementation and not by
     * the implementation itself.
     */
    Set<ModelElement_1_0> refFeaturesHavingNoImpl();
    
    /**
     * Convert a model class name to the corresponding delegate class
     * 
     * @return the corresponding delegate class
     * 
     * @throws JDOFatalUserException 
     */
    Class<?> getDelegateClass ();    

    /**
     * Tells whether its RefPackage delegates to an ObjectFactory_1_0 or to
     * a PersistenceManager.
     * 
     * @return <code>true</code> if its RefPackage delegates to an ObjectFactory_1_0
     */
    boolean isTerminal();

    /**
     * Retrieve the feature mapper which maps the class features 
     * to the corresponding instance level interface methods.
     * 
     * @return this Jmi1Class' instance feature mapper
     */
    FeatureMapper getFeatureMapper();

    /**
     * Retrievve the corresponding marshaller.
     * 
     * @return this Jmi1Class' marshaller
     */
    StandardMarshaller getMarshaller();
    
}
