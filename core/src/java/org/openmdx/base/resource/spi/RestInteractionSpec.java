/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: RestInteractionSpec.java,v 1.4 2009/05/22 00:51:22 wfro Exp $
 * Description: REST Interaction Specification
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/22 00:51:22 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.base.resource.spi;

import javax.resource.ResourceException;
import javax.resource.cci.InteractionSpec;

import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.RestFunction;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.format.IndentingFormatter;


/**
 * REST Interaction Specification
 */
public class RestInteractionSpec implements InteractionSpec {

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -7618869395528893650L;

    /**
     * The REST function
     */
    private RestFunction function = null;
    
    /**
     * The interaction verb
     */
    private int interactionVerb = InteractionSpec.SYNC_SEND_RECEIVE;

    /**
     * The principal chain
     */
    private String[] principalChain;

    /**
     * Tells, whether this <code>InteractionSpec</code> may be modified or not.
     */
    boolean unmodifiable = false;
    
    /**
     * Test whether this <code>InteractionSpec</code> is modifiable.
     */
    private void assertModifiability(
    ){
        if(this.unmodifiable) throw BasicException.initHolder(
            new IllegalStateException(
                "This REST InteractionSpec is unmodifiable",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE
                )
            )
        );
    }
    
    /**
     * Retrieve function.
     *
     * @return Returns the function.
     */
    public RestFunction getFunction() {
        return this.function;
    }

    /**
     * Set the function
     * 
     * @param function The function to set.
     */
    public void setFunction(
        RestFunction function
    ) {
        assertModifiability();
        this.function = function;
    }
    
    /**
     * Retrieve the function by name 
     * 
     * @return the function by name 
     */
    public String getFunctionName(){
        return this.function.name();
    }

    /**
     * Set the function by name
     * 
     * @param functionName the function name
     */
    public void setFunctionName(
        String functionName
    ){
        setFunction(
            RestFunction.valueOf(functionName)
        );
    }
    
    /**
     * Retrieve the interaction verb.
     *
     * @return Returns the interaction verb
     */
    public int getInteractionVerb() {
        return this.interactionVerb;
    }
    
    /**
     * Set the interaction verb.
     * 
     * @param interactionVerb the interaction verb to set
     */
    public void setInteractionVerb(int interactionVerb) {
        assertModifiability();
        this.interactionVerb = interactionVerb;
    }

    
    /**
     * Retrieve the principal chain
     *
     * @return Returns the principal chain
     */
    public String[] getPrincipalChain() {
        return this.principalChain;
    }
    
    /**
     * Retrieve the principal chain
     *
     * @return Returns the principal chain
     */
    public String getPrincipalChain(
        int index
    ) {
        return this.principalChain[index];
    }
    
    /**
     * Set the principal chain
     *    
     * @param principalChain the principal chain to be set
     * 
     * @throws ResourceException 
     */
    public void setPrincipalChain(
        String[] principalChain
    ){
        assertModifiability();
        this.principalChain = principalChain;
    }    

    /**
     * Set the principal chain
     *    
     * @param principal the principal to be set
     * 
     * @throws ResourceException 
     */
    public void setPrincipalChain(
        int index,
        String principal
    ) throws ResourceException {
        assertModifiability();
        this.principalChain[index] = principal;
    }    

    /**
     * Make this <code>InteractionSpec</code> unmodifiable. 
     * If already unmodifiable, this method has no effect.
     */
    public void setUnmodifiable(){
        this.unmodifiable = true;
    }

    @Override
    public String toString(
    ) {
        try {
            return IndentingFormatter.toString(
                Records.getRecordFactory().asMappedRecord(
                    this.getClass().getName(), 
                    null, 
                    new String[]{
                        "function",
                        "interactionVerb",
                        "principalChain",
                        "unmodifiable"
                    },
                    new Object[]{
                        this.function,
                        this.interactionVerb,
                        this.principalChain,
                        this.unmodifiable
                    }
                )
            );
        }
        catch(ResourceException e) {
            return null;
        }
    }
}
