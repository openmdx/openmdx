/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Jmi1PredicateInvocationHandler.java,v 1.6 2008/02/22 18:14:33 hburger Exp $
 * Description: Jmi1PackageInvocationHandler 
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/22 18:14:33 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

import org.openmdx.base.accessor.jmi.cci.RefFilter_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.model1.mapping.java.Identifier;

/**
 * Jmi1ObjectInvocationHandler
 *
 */
public class Jmi1PredicateInvocationHandler implements InvocationHandler {
    
    //-----------------------------------------------------------------------
    public static class RefPredicate_1Proxy extends RefPredicate_1 {
        
        public RefPredicate_1Proxy(
            RefPackage_1_0 refPackage,
            String filterType,
            FilterProperty[] filterProperties,
            AttributeSpecifier[] attributeSpecifiers,
            RefFilter_1_0 delegateFilter,
            Short delegateQuantor,
            String delegateName
        ) {
            super(
                refPackage,
                filterType,
                filterProperties,
                attributeSpecifiers,
                delegateFilter,
                delegateQuantor,
                delegateName
            );
        }
        
        private static final long serialVersionUID = -6399595381625181656L;
    }
    
    //-----------------------------------------------------------------------
    public Jmi1PredicateInvocationHandler(
        RefPackage_1_0 refPackage,
        String filterType,
        FilterProperty[] filterProperties,
        AttributeSpecifier[] attributeSpecifiers,
        RefFilter_1_0 delegateFilter,
        Short delegateQuantor,
        String delegateName
    ) {
        this.delegation = new RefPredicate_1Proxy(
            refPackage,
            filterType,
            filterProperties,
            attributeSpecifiers,
            delegateFilter,
            delegateQuantor,
            delegateName
        );
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(
        Object proxy, 
        Method method, 
        Object[] args
    ) throws Throwable {
        // RefObject
        if(
            method.getName().startsWith("ref") && 
            (method.getName().length() > 3) &&
            Character.isUpperCase(method.getName().charAt(3))
        ) {
            try {
                return method.invoke(
                    this.delegation, 
                    args
                );
            }
            catch(InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
        // orderBy
        else if(method.getName().startsWith("orderBy")) {
            String featureName = method.getName().substring(7);
            featureName = Identifier.ATTRIBUTE_NAME.toIdentifier(featureName);
            return this.delegation.refGetOrder(
                featureName
            );
        }
        // thereExists
        else if(method.getName().startsWith("thereExists")) {
            String featureName = method.getName().substring(11);
            featureName = Identifier.ATTRIBUTE_NAME.toIdentifier(featureName);
            return "thereExistsContext".equals(method.getName())
                ? this.delegation.refGetPredicate(
                    Quantors.THERE_EXISTS,
                    "org:openmdx:base:ContextCapable:context"
                  )
                : this.delegation.refGetPredicate(
                    Quantors.THERE_EXISTS,
                    featureName
                  );
        }
        // forAll
        else if(method.getName().startsWith("forAll")) {
            String featureName = method.getName().substring(6);
            featureName = Identifier.ATTRIBUTE_NAME.toIdentifier(featureName);
            return "thereExistsContext".equals(method.getName())
                ? this.delegation.refGetPredicate(
                    Quantors.FOR_ALL,
                    "org:openmdx:base:ContextCapable:context"
                  )
                : this.delegation.refGetPredicate(
                    Quantors.FOR_ALL,
                    featureName
                  );
        }
        // equalTo
        else if("equalTo".equals(method.getName())) {
            this.delegation.equalTo(
                args[0]
            );
            return null;
        }        
        // notEqualTo
        else if("notEqualTo".equals(method.getName())) {
            this.delegation.notEqualTo(
                args[0]
            );
            return null;
        }        
        // elementOf
        else if("elementOf".equals(method.getName())) {
            if(args[0] == null) {
                this.delegation.elementOf(
                    Collections.EMPTY_SET
                );
                return null;
            } else if (args[0] instanceof Collection){
                this.delegation.elementOf(
                    (Collection<?>)args[0]
                );
                return null;
            } else if (args[0].getClass().isArray()) {
                this.delegation.elementOf(
                    (Object[])args[0]
                );
                return null;
            } else throw new IllegalArgumentException(
                "Invalid argument for 'elementOf': " + args[0].getClass().getName()
            );
        }        
        // notAnElementOf
        else if("notAnElementOf".equals(method.getName())) {
            if(args[0] == null) {
                this.delegation.elementOf(
                    Collections.EMPTY_SET
                );
                return null;
            } else if (args[0] instanceof Collection){
                this.delegation.notAnElementOf(
                    (Collection<?>)args[0]
                );
                return null;
            } else if (args[0].getClass().isArray()) {
                this.delegation.notAnElementOf(
                    (Object[])args[0]
                );
                return null;
            } else throw new IllegalArgumentException(
                "Invalid argument for 'notAnElementOf': " + args[0].getClass().getName()
            );
        }        
        // Object
        else if("toString".equals(method.getName())) {
            return this.delegation.toString();
        }
        // Predicate
        else if((args == null) || (args.length == 0)) {
            return this.delegation.refGetPredicate(
                method.getName()
            );
        }
        throw new UnsupportedOperationException(method.getName());
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    protected final RefPredicate_1 delegation;
    
}
