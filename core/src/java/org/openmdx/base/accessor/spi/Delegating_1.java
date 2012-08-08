/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Delegating_1.java,v 1.4 2009/04/28 13:58:51 hburger Exp $
 * Description: Delegating_1 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/04/28 13:58:51 $
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
package org.openmdx.base.accessor.spi;

import org.openmdx.base.exception.ServiceException;

/**
 * Delegating_1
 */
public class Delegating_1 {

    /**
     * Avoid instantiation 
     */
    protected Delegating_1() {
        super();
    }

    /**
     * Compare two delegating objects
     * 
     * @return <code>true</code> if both have the same class and
     * their delegates are equal.
     * 
     * @throws NullPointerException if <code>left</code> is <code>null</code>.
     */
    public static boolean equal(
        Delegating_1_0<?> left,
        Object right
    ) throws ServiceException {
        if(right instanceof Delegating_1_0<?>) {
            Delegating_1_0<?> that = (Delegating_1_0<?>) right;
            return left == that || ( // both same OR
                left.getClass() == that.getClass() && // same class AND
                equal (left.objGetDelegate(), that.objGetDelegate()) // equal delegates
            );
        } else {
            return false;
        }
    }

    /**
     * Compare the delegates
     * 
     * @return <code>true</code> of the delegates are either both
     * <code>null</code> or equal.
     */
    private static boolean equal(
        Object left,
        Object right
    ){
        return left == right || (
            left != null && left.equals(right)
        );
    }

    /**
     * Calculate a delegating object's hash code
     * 
     * @param delegating the delegating object
     * 
     * @return its hash code
     */
    public static int hashCode(
        Delegating_1_0<?> delegating
    ) throws ServiceException {
        Object delegate = delegating.objGetDelegate();
        return delegate == null ? 0 : delegate.hashCode();
    }

    /**
     * Retrieve a delegating object's string representation
     * 
     * @param delegating the delegating object
     * 
     * @return a string representation of its delegate amended by the
     * delegating object's class name.
     */
    public static String toString(
        Delegating_1_0<?> delegating
    ){
        try {
            return delegating.getClass().getName() + ": " + delegating.objGetDelegate();
        }
        catch(Exception e) {
            return delegating.getClass().getName() + ": " + e.getMessage();            
        }
    }

}
