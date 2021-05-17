/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Involved Members
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2012, OMEX AG, Switzerland
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
package org.openmdx.state2.aop1;

import java.util.Iterator;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException.Parameter;

/**
 * To access the delegates
 */
abstract class InvolvedMembers<O,M> implements Involved<M> {

    /**
     * Constructor 
     *
     * @param involvedStates
     * @param feature
     */
    protected InvolvedMembers(
        Involved<O> involvedStates,
        String feature
    ){
        this.involvedStates = involvedStates;
        this.feature = feature;
        this.accessorForQueries = new Accessor(involvedStates.getQueryAccessMode());
        this.accessorForUpdates = new Accessor(AccessMode.FOR_UPDATE);
    }
    
    /**
     * The involved states
     */
    final Involved<O> involvedStates;
    
    /**
     * The feature name
     */
    final String feature;

    /**InvolvedMembers
     * A readable state iterator
     */
    private final Iterable<M> accessorForQueries;
        
    /**
     * A modifiable state iterator
     */
    private final Iterable<M> accessorForUpdates;
    
    /* (non-Javadoc)
     * @see org.openmdx.state2.aop1.Involved#getIdParameter()
     */
    @Override
    public Parameter getIdParameter() {
        return this.involvedStates.getIdParameter();
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop1.Involved#getQueryAccess()
     */
    @Override
    public AccessMode getQueryAccessMode() {
        return this.involvedStates.getQueryAccessMode();
    }

    /**
     * Retrieve a state's member
     * 
     * @param state
     * 
     * @return the appropriate member
     * 
     * @exception ServiceException
     */
    protected abstract M getMember(
        O state
    ) throws ServiceException;    
    
    /* (non-Javadoc)
     * @see org.openmdx.state2.plugin.Involved#getInvolved(Access)
     */
    public Iterable<M> getInvolved(AccessMode access) {
        return access == AccessMode.FOR_UPDATE ? this.accessorForUpdates : this.accessorForQueries;
    }

    
    //------------------------------------------------------------------------
    // Class Accessor
    //------------------------------------------------------------------------
    
    /**
     * Accessor
     */
    private class Accessor implements Iterable<M> {
        
        /**
         * Constructor 
         *
         * @param access the access mode
         */
        Accessor(
            AccessMode access 
        ){
            this.access = access;
        }
        
        /**
         * Tells whether the accessor is read-only or not
         */
        final AccessMode access;
        
        
        /* (non-Javadoc)
         * @see java.lang.Iterable#iterator()
         */
        public Iterator<M> iterator(
        ) {
            return new Iterator<M>(){

                /**
                 * Involved States Iterator
                 */
                private final Iterator<O> states = involvedStates.getInvolved(
                    Accessor.this.access
                ).iterator(
                );
                
                public boolean hasNext() {
                    return states.hasNext();
                }

                public M next() {
                    try {
                        return getMember(this.states.next());
                    } catch (ServiceException exception) {
                        throw new RuntimeServiceException(exception);
                    }
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
                
            };
        }
        
    }

}