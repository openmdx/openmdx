/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: InteractionSpecs.java,v 1.5 2009/05/15 00:26:34 hburger Exp $
 * Description: InteractionSpecs 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/15 00:26:34 $
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
import java.util.List;

import javax.resource.cci.InteractionSpec;

import org.openmdx.base.resource.cci.RestFunction;
import org.openmdx.base.resource.spi.MethodInvocationSpec;
import org.openmdx.base.resource.spi.RestInteractionSpec;

/**
 * InteractionSpecs
 */
public class InteractionSpecs {

    //----------------------------------------------------------------------------------
    // REST Interaction Specifications
    //----------------------------------------------------------------------------------
    
    /**
     * Constructor 
     * 
     * @param principalChainAsList
     * @param retainValues
     */
    private InteractionSpecs(
        List<String> principalChainAsList,
        boolean retainValues
    ){
        String[] principalChain = principalChainAsList == null ? null : principalChainAsList.toArray(
            new String[principalChainAsList.size()]
        );
        this.CREATE = newRestInteractionSpec(
            RestFunction.POST,
            retainValues ? InteractionSpec.SYNC_SEND_RECEIVE : InteractionSpec.SYNC_SEND,
            principalChain
        );
        this.INVOKE = newRestInteractionSpec(
            RestFunction.POST,
            InteractionSpec.SYNC_SEND_RECEIVE,
            principalChain
        );
        this.GET = newRestInteractionSpec(
            RestFunction.GET,
            InteractionSpec.SYNC_SEND_RECEIVE,
            principalChain
        );
        this.VERIFY = newRestInteractionSpec(
            RestFunction.GET,
            InteractionSpec.SYNC_SEND,
            principalChain
        );
        this.PUT = newRestInteractionSpec(
            RestFunction.PUT,
            retainValues ? InteractionSpec.SYNC_SEND_RECEIVE : InteractionSpec.SYNC_SEND,
            principalChain
        );
        this.DELETE = newRestInteractionSpec(
            RestFunction.DELETE,
            InteractionSpec.SYNC_SEND,
            principalChain
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
     * A {@link RestFunction#DELETE} Object Removal Interaction Specification
     */
    public final RestInteractionSpec DELETE;

    /**
     * REST <code>InteractionSpecs</code> factory method.
     * 
     * @param principalChain
     * 
     * @return a new <code>InteractionSpecs</code> instance
     */
    public static InteractionSpecs newRestInteractionSpecs(
        List<String> principalChain,
        boolean retainValues
    ){
        return new InteractionSpecs(
            principalChain,
            retainValues
        );
    }
    
    /**
     * Internal <code>RestInteractionSpec</code> factory method 
     * 
     * @param function
     * @param interactionVerb
     * @param principalChain
     * 
     * @return a new <code>InteractionSpec</code> instance for the given function
     */
    private static RestInteractionSpec newRestInteractionSpec(
        RestFunction function,
        int interactionVerb,
        String[] principalChain
    ){
        RestInteractionSpec interactionSpec = new RestInteractionSpec();
        interactionSpec.setFunction(function);
        interactionSpec.setInteractionVerb(interactionVerb);
        interactionSpec.setPrincipalChain(principalChain);
        interactionSpec.setUnmodifiable();
        return interactionSpec;
    }
    
    
    //----------------------------------------------------------------------------------
    // Method Invocation Specifications
    //----------------------------------------------------------------------------------
    
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
