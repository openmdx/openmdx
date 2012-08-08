/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: DelegatingStructure_1.java,v 1.6 2008/09/22 23:38:19 hburger Exp $
 * Description: Delegating Structure_1_0 Implementation
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/22 23:38:19 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.generic.spi;

import java.util.List;

import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.exception.ServiceException;


/**
 * Abstract implementation of Structure_1_0 which delegates to a delegate.
 */
public abstract class DelegatingStructure_1
    implements Structure_1_0, Delegating_1_0 
{

    /**
     * Constructor
     */
    protected DelegatingStructure_1(
        Structure_1_0 delegate
    ){
        this.delegate = delegate;
    }

    /**
     * Retrieve structure.
     *
     * @return Returns the structure.
     */
    protected Structure_1_0 getDelegate() {
        return this.delegate;
    }

    /**
     * Set structure.
     * 
     * @param delegate The structure to set.
     */
    protected void setDelegate(
        Structure_1_0 delegate
    ) {
        this.delegate = delegate;
    }


    //------------------------------------------------------------------------
    // Implements Delegating_1_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Delegating_1_0#objGetDelegate()
     */
    public final Object objGetDelegate() {
        return getDelegate();
    }


    //--------------------------------------------------------------------------
    // Extends Object
    //--------------------------------------------------------------------------

    /**
     * Retrieve the object's string representation
     * 
     * @return a string representation of its delegate amended by the
     * object's class name.
     */
    public String toString(
    ) {
        return Delegating_1.toString(this);
    }

    /**
     * Indicates whether some other object is "equal to" this one. 
     * <p>
     * The equals method implements an equivalence relation:
     * <ul>
     * <li> It is reflexive: for any reference value x, x.equals(x) should
     *      return true.</li>
     * <li> It is symmetric: for any reference values x and y, x.equals(y)
     *      should return true if and only if y.equals(x) returns true.<li>
     * <li> It is transitive: for any reference values x, y, and z, if
     *      x.equals(y) returns true and y.equals(z) returns true, then
     *      x.equals(z) should return true.</li>
     * <li> It is consistent: for any reference values x and y, multiple
     *      invocations of x.equals(y) consistently return true or consistently
     *      return false, provided no information used in equals comparisons on
     *      the object is modified.</li>
     * <li> For any non-null reference value x, x.equals(null) should return
     *      false.<li>
     * </ul>
     * <p>
     * The equals method for class Object implements the most discriminating
     * possible equivalence relation on objects; that is, for any reference
     * values x and y, this method returns true if and only if x and y refer to
     * the same object (x==y has the value true).
     *
     * @para    object
     *          the reference object with which to compare.
     *
     * @return  true if this object is the same as the obj argument;
     *          false otherwise.
     */
    public boolean equals(
        Object that
    ){
        return Delegating_1.equal(this, that);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return Delegating_1.hashCode(this);
    }


    //--------------------------------------------------------------------------
    // Implements Structure_1_0
    //--------------------------------------------------------------------------

    /**
     * Returns the object's model class.
     *
     * @return  the object's model class
     */
    public String objGetType(
    ) {
        return getDelegate().objGetType();
    }

    /**
     * Returns structure.objFieldNames()
     */
    public List<String> objFieldNames(
    ) {
        return getDelegate().objFieldNames();
    }

    /**
     * Return structure.getValue()
     */
    public Object objGetValue(
        String field
    ) throws ServiceException {
        return getDelegate().objGetValue(field);
    }

    //--------------------------------------------------------------------------
    // Instance Members
    //--------------------------------------------------------------------------

    /**
     * The delegate
     */
    private Structure_1_0 delegate;

}
