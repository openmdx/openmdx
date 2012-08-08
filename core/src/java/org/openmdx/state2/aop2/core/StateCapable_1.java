/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: StateCapable_1.java,v 1.2 2008/12/15 03:15:36 hburger Exp $
 * Description: State Capble Plug-In
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:36 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.state2.aop2.core;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.spi.Object_1_6;
import org.openmdx.base.aop2.core.PlugIn_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.state2.cci.StateContext;

/**
 * State Capble Plug-In
 */
public class StateCapable_1 
    extends PlugIn_1 
{

    /**
     * Constructor 
     *
     * @param self the plug-in holder
     * 
     * @throws ServiceException
     */
    public StateCapable_1(
        Object_1_6 self,
        Object_1_0 next
    ) throws ServiceException {
        super(self, next);
    }

    /**
     * org::openmdx::state2::StateCapable's MOF id
     */
    public final static String CLASS = "org:openmdx:state2:StateCapable";

    /**
     * The org::openmdx::state2::Core::state feature
     */
    public final static String STATE = "state";
    
    /**
     * The states
     */
    private transient Set<Object> states = null;
    
    
    //------------------------------------------------------------------------
    // Implements Object_1_0
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objGetSet(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Set<Object> objGetSet(
        String feature
    ) throws ServiceException {
        if(StateCapable_1.STATE.equals(feature)) {
            if(this.states == null) {
                this.states = this.self.getInteractionSpec() instanceof StateContext ? Collections.singleton(
                    getStateDelegate()
                ) : new StateSet(
                    this.self.getAspect(AbstractState_1.CLASS).values()
                );
            }
            return this.states;
        } else {
            return super.objGetSet(feature);
        }
    }

    protected Object getStateDelegate(
    ) throws ServiceException {
        return self.objGetDelegate();
    }
    
    //------------------------------------------------------------------------
    // Class StateSet
    //------------------------------------------------------------------------
    
    /**
     * Set consisting of all states
     */
    final class StateSet extends AbstractSet<Object> {

        /**
         * Constructor 
         * @throws ServiceException 
         */
        StateSet(
            Collection<Object_1_0> delegate
        ){
            this.delegate = delegate;
        }
        
        /**
         * The set of states managed by the connection
         */
        private final Collection<Object_1_0> delegate;
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @SuppressWarnings("unchecked")
        @Override
        public Iterator<Object> iterator() {
            return new StateIterator(
                this.delegate.iterator()
            );
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return this.delegate.size();
        }
        
        
        //--------------------------------------------------------------------
        // Class StateIterator
        //--------------------------------------------------------------------
        
        /**
         * Iterator returning all states
         */
        final class StateIterator implements Iterator<Object> {

            /**
             * Constructor 
             *
             * @param delegate
             */
            StateIterator(Iterator<?> delegate) {
                this.delegate = delegate;
            }
                        
            /**
             * Iterator for the states managed by the connection
             */
            private final Iterator<?> delegate;
            
            /* (non-Javadoc)
             * @see java.util.Iterator#hasNext()
             */
            public boolean hasNext() {
                return this.delegate.hasNext();
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#next()
             */
            public Object next() {
                return this.delegate.next();
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#remove()
             */
            public void remove() {
                throw new UnsupportedOperationException(
                    "Thw state collection must not be modified directly"
                );
                
            }

        }
        
    }

}