/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: VelocityClassDef class
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.application.mof.mapping.cci;

import java.util.HashSet;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;

public class AssociationDef extends ElementDef {

    /**
     * Constructor 
     *
     * @param associationDef
     * @param model
     * @throws ServiceException
     */
    public AssociationDef(
        ModelElement_1_0 associationDef,
        Model_1_0 model
    ) throws ServiceException {
        super( 
            (String)associationDef.objGetValue("name"),
            (String)associationDef.objGetValue("qualifiedName"),
            (String)associationDef.objGetValue("annotation"),
            new HashSet<Object>(associationDef.objGetList("stereotype"))
        );      
        List<Object> content = associationDef.objGetList("content");
        this.ends = new AssociationEndDef[]{
            new AssociationEndDef(
                model.getElement(content.get(0)),
                model
            ),
            new AssociationEndDef(
                model.getElement(content.get(1)),
                model
            )
        };
        this.associationDef = associationDef;
        this.model = model;
    }

    private final AssociationEndDef[] ends;
    
    /**
     * 
     */
    private final ModelElement_1_0 associationDef;
    
    /**
     * 
     */
    @SuppressWarnings("unused")
    private final Model_1_0 model;

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
        return 
        that instanceof AssociationDef && 
        this.associationDef.equals(((AssociationDef)that).associationDef);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.associationDef.hashCode();
    }

    /**
     * Retrieve ends.
     *
     * @return Returns the ends.
     */
    public final AssociationEndDef[] getEnds() {
        return this.ends;
    }
    
}
