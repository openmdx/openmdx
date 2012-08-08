/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ModelUtils.java,v 1.3 2009/05/16 22:17:54 wfro Exp $
 * Description: ModelUtils
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/16 22:17:54 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2008, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.mof.spi;


import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicities;

public class ModelUtils {

    /**
     * Determine multiplicity of feature. In case of an attribute it is
     * the modeled multiplicity. In case of a reference with a qualifier
     * the multiplicity is <<list>> else the modeled multiplicity.
     * 
     * @param featureDef
     * @return the featur's multiplicity
     * 
     * @throws ServiceException
     */
    public static String getMultiplicity(
        ModelElement_1_0 featureDef
    ) throws ServiceException{
        Model_1_0 model = featureDef.getModel();
        String multiplicity = (String) featureDef.objGetValue("multiplicity");
        if(model.isReferenceType(featureDef)) {
            ModelElement_1_0 referencedEnd = model.getElement(
                featureDef.objGetValue("referencedEnd")
            );
            if(!referencedEnd.objGetList("qualifierType").isEmpty()) {
                ModelElement_1_0 qualifierType = model.getDereferencedType(
                    referencedEnd.objGetValue("qualifierType")
                );
                multiplicity = model.isNumericType(qualifierType) ? 
                    Multiplicities.LIST : 
                        Multiplicities.MAP;
            } else if (Multiplicities.MULTI_VALUE.equals(multiplicity)) {
                // map aggregation none, multiplicity 0..n, no qualifier to
                // <<set>>
                // in case <<list>> semantic is required it must be modeled as
                // aggregation none, multiplicity 0..1, numeric qualifier
                multiplicity = Multiplicities.SET;
            }
        }
        return multiplicity;
    }

}

//--- End of File -----------------------------------------------------------
