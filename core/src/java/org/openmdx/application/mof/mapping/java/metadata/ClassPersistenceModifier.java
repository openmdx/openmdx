/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ClassPersistenceModifier.java,v 1.1 2009/01/13 02:10:28 wfro Exp $
 * Description: Class Persistence Modifier 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/13 02:10:28 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
package org.openmdx.application.mof.mapping.java.metadata;

/**
 * Class Persistence Modifier
 */
public enum ClassPersistenceModifier {

    PERSISTENCE_CAPABLE("persistence-capable"),
    PERSISTENCE_AWARE("persistence-aware"),
    NON_PERSISTENT("non-persistent");
    
    /**
     * Constructor 
     *
     * @param xmlFormat
     */
    private ClassPersistenceModifier(final String xmlFormat) {
        this.xmlFormat = xmlFormat;
    }

    /**
     * The XML value
     * 
     * @return the XML value for literal
     */
    public final String toXMLFormat(){
        return this.xmlFormat;
    }
    
    /**
     * 
     */
    private final String xmlFormat;

    /**
     * Retrieve a <code>ClassPersistenceModifier</code> from its XML representation-
     * 
     * @param xmlFormat
     * 
     * @return the corresponding <code>ClassPersistenceModifier</code> value
     * 
     * @throws IllegalArgumentException if <code>xmlFormat</code> is
     * neither <code>null</code> nor representing any of the 
     * <code>ClassPersistenceModifier</code> values. 
     */
    public static ClassPersistenceModifier fromXMLFormat(
        String xmlFormat
    ){
        if(xmlFormat == null) {
            return null;
        };
        for(ClassPersistenceModifier value : values()) {
            if(value.xmlFormat.equals(xmlFormat)) {
                return value;
            }
        }
        throw new IllegalArgumentException(
            "There is no " + ClassPersistenceModifier.class.getName() + 
            " enum represented by " + xmlFormat
        );
    }
        
}
