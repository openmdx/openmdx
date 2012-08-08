/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ExceptionDef.java,v 1.4 2009/06/09 12:45:18 hburger Exp $
 * Description: VelocityExceptionDef class
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 12:45:18 $
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
package org.openmdx.application.mof.mapping.cci;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.exception.BasicException;

@SuppressWarnings("unchecked")
public class ExceptionDef 
extends FeatureDef {

    //-------------------------------------------------------------------------
    public ExceptionDef(
        ModelElement_1_0 exceptionDef,
        Model_1_0 model 
    ) throws ServiceException {
        this(
            mapName((String)exceptionDef.objGetValue("name")),
            (String)exceptionDef.objGetValue("qualifiedName"),
            (String)exceptionDef.objGetValue("annotation"),
            new HashSet(exceptionDef.objGetList("stereotype")),
            (String)exceptionDef.objGetValue("visibility"),
            getParameters(exceptionDef, model)
        );
    }

    //-------------------------------------------------------------------------
    private static String mapName(
        String modelName
    ){
        return modelName.endsWith("Exception") ? modelName :  modelName + "Exception";  
    }

    //-------------------------------------------------------------------------
    private static List getParameters(
        ModelElement_1_0 exceptionDef,
        Model_1_0 model 
    ) throws ServiceException {  

        HashMap params = new HashMap();
        for(
            Iterator i = exceptionDef.objGetList("content").iterator();
            i.hasNext();
        ) {
            ModelElement_1_0 param = model.getElement(i.next());
            params.put(
                param.objGetValue("name"),
                param
            );
        }
        if(params.get("in") == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "no parameter with name \"in\" defined for exception",
                new BasicException.Parameter("exception", exceptionDef.jdoGetObjectId()),
                new BasicException.Parameter("params", params)
            );
        }
        // set exeption parameters (as attributes)
        ModelElement_1_0 inParamType = model.getElementType(
            ((ModelElement_1_0)params.get("in"))
        );
        List parameters = new ArrayList();
        for(
            Iterator i = inParamType.objGetList("content").iterator();
            i.hasNext();
        ) {
            ModelElement_1_0 field = model.getElement(i.next());
            if(model.isStructureFieldType(field)) {
                parameters.add(
                    new AttributeDef(
                        field,
                        model 
                    )
                );
            }
        }
        return parameters;
    }

    //-------------------------------------------------------------------------
    public ExceptionDef(
        String name,
        String qualifiedName,
        String annotation,
        Set stereotype,
        String visibility,
        List parameters
    ) {
        super(
            name, 
            qualifiedName, 
            annotation,
            stereotype,
            visibility
        );
        this.parameters = parameters;
    }

    //-------------------------------------------------------------------------
    public List getParameters(
    ) {
        return this.parameters;
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private final List parameters;

}
