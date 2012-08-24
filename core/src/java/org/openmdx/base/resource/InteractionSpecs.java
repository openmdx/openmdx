/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: InteractionSpecs 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2009, OMEX AG, Switzerland
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

package org.openmdx.base.resource;

import java.io.ObjectStreamException;

import javax.resource.cci.InteractionSpec;

import org.openmdx.base.resource.cci.RestFunction;
import org.openmdx.base.resource.spi.MethodInvocationSpec;
import org.openmdx.base.resource.spi.RestInteractionSpec;

/**
 * InteractionSpecs
 */
public class InteractionSpecs {

    /**
     * Constructor 
     * @param retainValues
     */
    private InteractionSpecs(
        boolean retainValues
    ){
        this.CREATE = new RestInteractionSpec(
            RestFunction.POST,
            retainValues ? InteractionSpec.SYNC_SEND_RECEIVE : InteractionSpec.SYNC_SEND
        );
        this.INVOKE = new RestInteractionSpec(
            RestFunction.POST,
            InteractionSpec.SYNC_SEND_RECEIVE
        );
        this.GET = new RestInteractionSpec(
            RestFunction.GET,
            InteractionSpec.SYNC_SEND_RECEIVE
        );
        this.VERIFY = new RestInteractionSpec(
            RestFunction.GET,
            InteractionSpec.SYNC_SEND
        );
        this.PUT = new RestInteractionSpec(
            RestFunction.PUT,
            retainValues ? InteractionSpec.SYNC_SEND_RECEIVE : InteractionSpec.SYNC_SEND
        );
        this.MOVE = new RestInteractionSpec(
            RestFunction.PUT,
            InteractionSpec.SYNC_SEND_RECEIVE
        );
        this.DELETE = new RestInteractionSpec(
            RestFunction.DELETE,
            InteractionSpec.SYNC_SEND
        );
    }

    /**
     * A {@link RestFunction#POST} Object Creation Interaction Specification
     */
    public final RestInteractionSpec CREATE;

    /**
     * A {@link RestFunction#POST} Method Invocation Interaction Specification
     */
    public final RestInteractionSpec INVOKE;

    /**
     * A {@link RestFunction#GET} Object Retrieval Interaction Specification
     */
    public final RestInteractionSpec GET;

    /**
     * A {@link RestFunction#GET} Object Verification Interaction Specification
     */
    public final RestInteractionSpec VERIFY;

    /**
     * A {@link RestFunction#PUT} Object Update Interaction Specification
     */
    public final RestInteractionSpec PUT;

    /**
     * A {@link RestFunction#PUT} Object Update Interaction Specification
     */
    public final RestInteractionSpec MOVE;    
    
    /**
     * A {@link RestFunction#DELETE} Object Removal Interaction Specification
     */
    public final RestInteractionSpec DELETE;

    /**
     * CREATE and PUT return updates
     */
    private static InteractionSpecs retainingValues = new InteractionSpecs(true);
    
    /**
     * CREATE and PUT return nothing
     */
    private static InteractionSpecs notRetainingValues = new InteractionSpecs(false);
    
    /**
     * REST <code>InteractionSpecs</code> factory method.
     * 
     * @return a new <code>InteractionSpecs</code> instance
     */
    public static InteractionSpecs getRestInteractionSpecs(
        boolean retainValues
    ){
        return retainValues ? retainingValues : notRetainingValues;
    }
    
    /**
     * Method invocation specification factory method.
     * 
     * @param methodName the name of the method to be invoked
     * @param query the execute method waits for the reply if query is <code>true</code>
     * 
     * @return a new <code>InteractionSpec</code> instance
     */
    public static InteractionSpec newMethodInvocationSpec(
        String methodName,
        int interactionVerb
    ){
        return new MethodInvocationSpec(
            methodName,
            interactionVerb
        );
    }
    
    /**
     * Test whether the <code>InteractionSpec</code> is logically <code>null</code>
     * 
     * @param interactionSpec the  <code>InteractionSpec</code>  to be tested
     * 
     * @return <code>true</code> if the <code>InteractionSpec</code> is logically <code>null</code>
     */
    public static boolean isNull(
        InteractionSpec interactionSpec
    ){
        return interactionSpec == null || interactionSpec == NULL;
    }
    
    //----------------------------------------------------------------------------------
    // Class NULL-InteractionSpec
    //----------------------------------------------------------------------------------
    
    /**
     * The <code>NULL</code> interaction spec
     */
    public static final InteractionSpec NULL = new InteractionSpec(){

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = -6765662708933344857L;

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "NULL";
        }
       
        private Object readResolve(
        ) throws ObjectStreamException {
            return NULL;
        }
                
    };

}
