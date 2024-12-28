/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Method Invocation Interaction Specification
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import #if JAVA_8 javax.resource.cci.InteractionSpec #else jakarta.resource.cci.InteractionSpec #endif;


/**
 * Method Invocation Interaction Specification
 */
public class MethodInvocationSpec implements InteractionSpec {

    /**
     * Constructor 
     *
     * @param functionName
     * @param interactionVerb
     */
    public MethodInvocationSpec(
        String functionName, 
        int interactionVerb
    ) {
        this.functionName = functionName;
        this.interactionVerb = interactionVerb;
    }

    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = 2223358654830587379L;

    /**
     * The REST function
     */
    private final String functionName;
    
    /**
     * The interaction verb
     */
    private final int interactionVerb;

    /**
     * Retrieve the function by name 
     * 
     * @return the function by name 
     */
    public String getFunctionName(){
        return this.functionName;
    }

    /**
     * Retrieve the interaction verb.
     *
     * @return Returns the interaction verb
     */
    public int getInteractionVerb() {
        return this.interactionVerb;
    }
    
}
