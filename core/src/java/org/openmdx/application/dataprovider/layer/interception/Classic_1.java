/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Standard Interception Plug-In
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2012, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.application.dataprovider.layer.interception;

import javax.resource.ResourceException;
import javax.resource.cci.Interaction;

import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.base.rest.cci.RestConnection;

/**
 * @deprecated will not be supported by the dataprovider 2 stack 
 */
public class Classic_1 extends Layer_1 {

    /**
     * Constructor 
     */
    public Classic_1(
    ) {
        super();
    }
    
    //--------------------------------------------------------------------------
    @Override
    public Interaction getInteraction(
        RestConnection connection
    ) throws ResourceException {
        return new ClassicLayerInteraction(connection);
    }
 
    //--------------------------------------------------------------------------
    public class ClassicLayerInteraction extends Layer_1.LayerInteraction {
      
        /**
         * Tells whether an object retrieval request shall throw a NOT_FOUND 
         * exception rather than returning and empty collection when a requested
         * object does not exist
         * 
         * @return <code>true</code> if an object retrieval request shall throw a NOT_FOUND 
         * exception rather than returning and empty collection when a requested
         * object does not exist
         */
        @Override
        protected boolean isPreferringNotFoundException(){
            return true;
        }
        
        public ClassicLayerInteraction(
            RestConnection connection
        ) throws ResourceException {
            super(connection);
        }
                
    }
    
}
