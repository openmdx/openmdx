/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: AssociationDef 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013-2014, OMEX AG, Switzerland
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
package org.openmdx.application.mof.repository.accessor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.naming.Path;

/**
 * Helper class holding the types of the exposed and referenced ends.
 * In addition provides a method which returns the type of the referenced
 * end plus all its subtypes and their supertypes. An AssociationDef allows
 * efficient validation of object paths.
 */
class AssociationDef {

    //-----------------------------------------------------------------------
    /**
     * Creates and AssociationDef. exposedType and referencedType are the
     * types of the exposedEnd and referenceEnd of the supplied reference.
     */
    public AssociationDef(
        ModelElement_1_0 exposedType, 
        ModelElement_1_0 referencedType,
        ModelElement_1_0 reference,
        Map<String,ModelElement_1_0> elements
    ) throws ServiceException {
        this.exposedType = exposedType;
        this.referencedType = referencedType;
        /**
         * Calculate the set of types referenced by referencedType
         * This is all subtypes of the referenced type and their supertypes
         */
        this.allReferencedTypes = new HashSet<String>();      
        for(
            Iterator<?> i = this.referencedType.objGetList("allSubtype").iterator(); 
            i.hasNext(); 
        ) {
            this.allReferencedTypes.add(
                ((Path)i.next()).getLastSegment().toClassicRepresentation()
            );
        }
        // add all supertypes of referenced types
        Set<String> allSupertypes = new HashSet<String>();
        for(
            Iterator<?> i = this.allReferencedTypes.iterator();
            i.hasNext();
        ) {
            try {
                ModelElement_1_0 type = ModelHelper.getElement(i.next(), elements);
                for(
                    Iterator<?> j = type.objGetList("allSupertype").iterator(); 
                    j.hasNext(); 
                ) {
                    allSupertypes.add(
                        ((Path)j.next()).getLastSegment().toClassicRepresentation()
                    );
                }
            }
            catch(Exception e) {
                System.out.println("element not found");
            }
        }
        this.allReferencedTypes.addAll(
            allSupertypes
        );
        this.reference = reference;
    }

    //-----------------------------------------------------------------------
    public Set<String> getAllReferencedTypes(
    ) {
        return this.allReferencedTypes;
    }

    //-----------------------------------------------------------------------
    public ModelElement_1_0 getReferencedType(
    ) {
        return this.referencedType;
    }

    //-----------------------------------------------------------------------
    public ModelElement_1_0 getExposedType(
    ) {
        return this.exposedType;
    }

    //-----------------------------------------------------------------------
    public ModelElement_1_0 getReference(
    ) {
        return this.reference;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString(
    ) {
        return
        "  reference=" + reference.jdoGetObjectId() + "\n" +
        "  referencedType=" + referencedType.jdoGetObjectId() + "\n" +
        "  exposedType=" + exposedType.jdoGetObjectId() + "\n" + 
        "  allReferencedTypes=" + allReferencedTypes + "\n";
    }

    //-----------------------------------------------------------------------
    // Variables
    //-----------------------------------------------------------------------
    private ModelElement_1_0 exposedType;
    private ModelElement_1_0 referencedType;
    private ModelElement_1_0 reference;
    private Set<String> allReferencedTypes;

}