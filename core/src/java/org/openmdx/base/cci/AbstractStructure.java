/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractStructure.java,v 1.1 2006/03/29 22:23:23 hburger Exp $
 * Description: Structure 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/03/29 22:23:23 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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

package org.openmdx.base.cci;

import java.io.IOException;
import java.io.Serializable;

/**
 * Structure
 */
public abstract class AbstractStructure implements Serializable {

    /**
     * Constructor 
     */
    protected AbstractStructure() {
        this.initialized = false;
    }

    /**
     * Tells whether the structure is already initialized
     */
    private boolean initialized;

    /**
     * Switches the structure's state to <em>initialized</em>.
     * 
     * @exception IllegalStateException
     * if the structure has already been initialized
     */
    protected void initialize(
    ){
        if(this.initialized) throw new IllegalStateException(
            "The structure is already initialized"
        );
        this.initialized = true;
    }
    
    /**
     * Checks whether the structure is already <em>initialized</em>.
     * 
     * @exception IllegalStateException
     * if the structure has not yet been initialized
     */
    protected void validate(
    ){
        if(!this.initialized) throw new IllegalStateException(
            "The structure is not yet initialized"
        );
    }

    /**
     * The writeObject method is responsible for writing the state of the 
     * object for its particular class so that the corresponding 
     * readObject method can restore it. 
     * <p>
     * The method does not need to concern itself with the state belonging 
     * to its superclasses or subclasses. 
     * <p> 
     * @param out
     * 
     * @throws IOException if validation fails.
     */
    private void writeObject(
        java.io.ObjectOutputStream out
    ) throws IOException {
        try {
            validate();
        } catch (IllegalStateException exception) {
            throw (IOException) new IOException(
                getClass().getName() + " can't be serialized"
            ).initCause(
                exception
            );
        }
    }

    /**
     * The readObject method is responsible for reading from the stream and 
     * restoring the classes fields. 
     * <p>
     * The method does not need to concern itself with the state belonging to
     * its superclasses or subclasses.
     * <p>
     * @param in
     * 
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(
        java.io.ObjectInputStream in
    ) throws IOException, ClassNotFoundException {
        this.initialized = true;
    }
    
}
