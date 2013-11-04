/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Jmi1ObjectPredicateInvocationHandler 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2013, OMEX AG, Switzerland
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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

import org.openmdx.base.query.AnyTypeCondition;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.ConditionType;
import org.w3c.cci2.AnyTypePredicate;

/**
 * Jmi1ObjectPredicateInvocationHandler
 */
public class Jmi1ObjectPredicateInvocationHandler extends Jmi1QueryInvocationHandler {

    /**
     * Constructor 
     *
     * @param predicate
     * @param subQuery
     */
    public Jmi1ObjectPredicateInvocationHandler(
        RefQuery_1.RefPredicate predicate,
        RefQuery_1 subQuery
    ) {
        super(subQuery);
        this.predicate = predicate;        
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 4054872493209535130L;
    
    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(
        Object proxy, 
        Method method, 
        Object[] args
    ) throws Throwable {
        Class<?> declaringClass = method.getDeclaringClass();
        String methodName = method.getName();
        if(declaringClass == Object.class) {
            if("toString".equals(methodName)) {
                return this.predicate.toString();
            } 
            else if("hashCode".equals(methodName)) {
                return Integer.valueOf(this.predicate.hashCode());
            } 
            else if ("equals".equals(methodName)) {
                return Boolean.valueOf(args[0] == proxy); // Identity
            }
        } 
        if (declaringClass == AnyTypePredicate.class) {
            if("equalTo".equals(methodName)) {
                this.predicate.equalTo(
                    args[0]
                );
                return null;
            } 
            else if("notEqualTo".equals(methodName)) {
                this.predicate.notEqualTo(
                    args[0]
                );
                return null;
            } 
            else if("elementOf".equals(methodName)) {
                if(args[0] == null) {
                    this.predicate.elementOf(
                        Collections.EMPTY_SET
                    );
                    return null;
                } 
                else if (args[0] instanceof Collection<?>){
                    this.predicate.elementOf(
                        (Collection<?>)args[0]
                    );
                    return null;
                } 
                else if (args[0].getClass().isArray()) {
                    this.predicate.elementOf(
                        (Object[])args[0]
                    );
                    return null;
                } 
                else throw new IllegalArgumentException(
                    "Invalid argument for 'elementOf': " + args[0].getClass().getName()
                );
            } 
            else if("notAnElementOf".equals(methodName)) {
                if(args[0] == null) {
                    this.predicate.notAnElementOf(
                        Collections.EMPTY_SET
                    );
                    return null;
                } 
                else if (args[0] instanceof Collection<?>){
                    this.predicate.notAnElementOf(
                        (Collection<?>)args[0]
                    );
                    return null;
                } 
                else if (args[0].getClass().isArray()) {
                    this.predicate.notAnElementOf(
                        (Object[])args[0]
                    );
                    return null;
                } 
                else throw new IllegalArgumentException(
                    "Invalid argument for 'notAnElementOf': " + args[0].getClass().getName()
                );
            }        
        }
        else {
            // Add subquery as filter property to parent query
            String featureName = this.predicate.getFeatureName();
            boolean exists = false;
            Conditions: for(Condition condition: this.predicate.getQuery().refGetFilter().getCondition()) {
                if(
                    condition.getFeature().equals(featureName) &&
                    condition.getQuantifier() == this.predicate.quantifier
                ) {
                    exists = true;
                    break Conditions;
                }
            }
            if(!exists) {
                this.predicate.getQuery().refGetFilter().getCondition().add(
                    new AnyTypeCondition(
                        this.predicate.quantifier,
                        featureName,
                        ConditionType.IS_IN,
                        super.getQuery().refGetFilter()
                    )
                );
            }
            return super.invoke(
                proxy, 
                method, 
                args
            );
        } 
        throw new UnsupportedOperationException(methodName);
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    protected final RefQuery_1.RefPredicate predicate;
   
}
