/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Jmi1PackageInvocationHandler 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.base.accessor.jmi.spi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.omg.mof.spi.Identifier;
import org.openmdx.base.accessor.jmi.cci.RefStruct_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.AnyTypePredicate;
import org.w3c.cci2.ComparableTypePredicate;
import org.w3c.cci2.MatchableTypePredicate;
import org.w3c.cci2.MultivaluedFeaturePredicate;
import org.w3c.cci2.PartiallyOrderedTypePredicate;
import org.w3c.cci2.ResourceIdentifierTypePredicate;
import org.w3c.cci2.StringTypePredicate;

/**
 * Jmi1PackageInvocationHandler
 *
 */
public class Jmi1PackageInvocationHandler implements InvocationHandler {
    
    //-----------------------------------------------------------------------
    public Jmi1PackageInvocationHandler(
        String qualifiedPackageName,
        javax.jmi.reflect.RefPackage outermostPackage,
        javax.jmi.reflect.RefPackage immediatePackage
    ) {
        this.delegation = new RefPackage_1(
            qualifiedPackageName,
            outermostPackage, 
            immediatePackage
        );
    }

    //-----------------------------------------------------------------------
    public String getQualifiedMofName(
        String qualifiedPackageName,
        String cciName
    ) throws ServiceException {
        String qualifiedCciName = qualifiedPackageName + ":" + cciName;
        String qualifiedMofName = qualifiedMofNames.get(qualifiedCciName);
        if(qualifiedMofName == null) {
            Model_1_0 model = Model_1Factory.getModel();
            for(
                Iterator<ModelElement_1_0> i = model.getContent().iterator(); 
                i.hasNext(); 
            ) {
                ModelElement_1_0 e = i.next();
                String elementName = Identifier.CLASS_PROXY_NAME.toIdentifier((String)e.objGetValue("name"));
                String qualifiedElementName = (String)e.objGetValue("qualifiedName");
                if(
                    (model.isClassType(e) || model.isStructureType(e)) &&
                    elementName.equals(cciName) &&
                    qualifiedElementName.substring(0, qualifiedElementName.lastIndexOf(":")).equals(qualifiedPackageName)
                ) {
                    qualifiedMofNames.putIfAbsent(
                        qualifiedCciName, 
                        qualifiedMofName = qualifiedElementName
                    );
                    break;
                }
            }
        }
        if(qualifiedMofName == null) {
            throw new ServiceException (
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.NOT_FOUND, 
                "model element not found with qualified cci name.",
                new BasicException.Parameter("package.name", qualifiedPackageName),
                new BasicException.Parameter("cci.name", cciName)
            );
        }
        return qualifiedMofName;
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
        Class<?> declaringClass = method.getDeclaringClass();
        String methodName = method.getName();
        if(Object.class == declaringClass) {
            //
            // Object methods
            //
            if("toString".equals(methodName)) {
                return proxy.getClass().getName() + " delegating to " + this.delegation;
            } 
            else if("hashCode".equals(methodName)) {
                return this.delegation.hashCode();
            } 
            else if("equals".equals(methodName)) {
                if(Proxy.isProxyClass(args[0].getClass())) {
                    InvocationHandler invocationHandler = Proxy.getInvocationHandler(args[0]);
                    if(invocationHandler instanceof Jmi1PackageInvocationHandler) {
                        return this.delegation.equals(
                            ((Jmi1PackageInvocationHandler)invocationHandler).delegation
                        );
                    }
                }
                return false;
            }
        } 
        else {
            this.delegation.assertOpen();
            if("refClass".equals(methodName) && args.length == 1) {
                //
                // RefClass
                //
                return this.delegation.refClass(
                    (String)args[0], // qualifiedClassName
                    (Jmi1Package_1_0)proxy
                );
            } 
            else if(
                declaringClass == Jmi1Package_1_0.class ||    
                methodName.startsWith("ref") && 
                (method.getName().length() > 3) &&
                Character.isUpperCase(methodName.charAt(3))
            ) {
                return "refOutermostPackage".equals(methodName) ? 
                    this.delegation.refOutermostPackage() : 
                    method.invoke(this.delegation, args);
            } 
            else if(methodName.startsWith("get")) {
                //
                // Getters
                //
                String cciName = method.getName().substring(3);
                String qualifiedPackageName = this.delegation.refMofId();
                return this.delegation.refClass(
                    this.getQualifiedMofName(
                        qualifiedPackageName.substring(0, qualifiedPackageName.lastIndexOf(":")), 
                        cciName
                    ),
                    (Jmi1Package_1_0)proxy
                );            
            } 
            else if(methodName.startsWith("create")) {
                // 
                // Creators
                //
                Class<?> returnType = method.getReturnType();
                String cciName = method.getName().substring(6);
                String qualifiedPackageName = this.delegation.refMofId();   
                if(RefStruct_1_0.class.isAssignableFrom(returnType)) {
                    //
                    // Structs
                    //
                    List<Object> iargs = new ArrayList<Object>();
                    if(args != null) {
                        for(int i = 0; i < args.length; i++) {
                            iargs.add(args[i]);
                        }
                    }
                    return this.delegation.refCreateStruct(
                        this.getQualifiedMofName(
                            qualifiedPackageName.substring(0, qualifiedPackageName.lastIndexOf(":")),
                            cciName
                        ), 
                        iargs
                    );
                } 
                else if(
                    //
                    // Queries
                    //
                    AnyTypePredicate.class.isAssignableFrom(returnType) ||
                    ComparableTypePredicate.class.isAssignableFrom(returnType) ||
                    MatchableTypePredicate.class.isAssignableFrom(returnType) ||
                    MultivaluedFeaturePredicate.class.isAssignableFrom(returnType) ||
                    PartiallyOrderedTypePredicate.class.isAssignableFrom(returnType) ||
                    ResourceIdentifierTypePredicate.class.isAssignableFrom(returnType) ||
                    StringTypePredicate.class.isAssignableFrom(returnType)
                ) {
                    return this.delegation.refCreateQuery(
                        this.getQualifiedMofName(
                            qualifiedPackageName.substring(0, qualifiedPackageName.lastIndexOf(":")),
                            cciName.endsWith("Query") 
                                ? cciName.substring(0, cciName.length() - "Query".length()) 
                                : cciName
                        ),
                        true, // subclasses 
                        null
                    );
                }
            }
        }        
        throw new UnsupportedOperationException(methodName);
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    protected final RefPackage_1 delegation;
    protected final static ConcurrentMap<String,String> qualifiedMofNames = new ConcurrentHashMap<String,String>();
    
}
