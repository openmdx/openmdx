/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DataproviderObject_1_0.java,v 1.3 2004/04/02 16:59:01 wfro Exp $
 * Description: spice: dataprovider object interface
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/04/02 16:59:01 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.compatibility.base.dataprovider.cci;

import java.util.Set;

import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.naming.Path;


/**
 * Dataprovider objects implement this interface
 */
public interface DataproviderObject_1_0 {

    /**
     * Returns the dataprovider object's path object.
     */
    Path path(
    );


    //------------------------------------------------------------------------
    // Value list interface
    //------------------------------------------------------------------------

    /**
     * Returns the attribute value list.
     *
     * @param       attributeName
     *              the attribute's name
     *
     * @return      the attribute value list; or null if no list with this
     *              name exists
     */
    SparseList getValues(
        String attributeName
    );

    /**
     * Returns the existing or newly created attribute value list (optional
     * method).
     *
     * @param       attributeName
     *              the attribute's name
     *
     * @return      the attribute value list (never null)
     */
    SparseList values(
        String attributeName
    );

    /**
     * Returns the existing and cleared or newly created attribute value list
     * (optional method).
     *
     * @param       attributeName
     *              the attribute's name
     *
     * @return      the attribute value list (never null)
     */
    SparseList clearValues(
        String attributeName
    );
    
    
    //------------------------------------------------------------------------
    // Attribute collection interface
    //------------------------------------------------------------------------

    /**
     * Clear the dataprovider object's attributes (optional operation).
     */
    void clear(
    );
    
    /**
     * Get a set view of the dataprovider object's attribute names
     */
    Set attributeNames(
    );
        
    /**
     * Checks whether the dataprovider object contains an attribute with the
     * given name.
     */
    boolean containsAttributeName(
        String attributeName
    );


    //------------------------------------------------------------------------
    // Digest
    //------------------------------------------------------------------------

    /**
     * Get the object's digest (which must not be modified!)
     */
    public byte[] getDigest();

    /** 
     * Set the object's digest (optional operation)
     */
    public void setDigest(
        byte[] digest
    );

}
