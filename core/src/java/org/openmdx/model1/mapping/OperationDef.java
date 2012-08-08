/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: OperationDef.java,v 1.9 2008/11/11 15:40:46 wfro Exp $
 * Description: VelocityOperationDef class
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/11 15:40:46 $
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
package org.openmdx.model1.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;

@SuppressWarnings("unchecked")
public class OperationDef 
extends FeatureDef {

    //-------------------------------------------------------------------------
    public OperationDef(
        ModelElement_1_0 operationDef,
        Model_1_0 model 
    ) throws ServiceException {
        this(
            (String)operationDef.values("name").get(0),
            (String)operationDef.values("qualifiedName").get(0),
            (String)operationDef.values("annotation").get(0),
            new HashSet(operationDef.values("stereotype")),
            (String)operationDef.values("visibility").get(0),
            getResultParamTypeName(operationDef, (Model_1_3)model),
            ((Boolean)operationDef.values("isQuery").get(0)).booleanValue(),
            getInParameters(operationDef, (Model_1_3)model),
            getExceptions(operationDef, model)
        );
    }

    //-------------------------------------------------------------------------
    private static List getExceptions(
        ModelElement_1_0 operationDef,
        Model_1_0 model 
    ) throws ServiceException {  
        List exceptions = new ArrayList();
        for(
            Iterator i = operationDef.values("exception").iterator();
            i.hasNext();
        ) {
            exceptions.add(
                new ExceptionDef(
                    model.getElement(i.next()),
                    model 
                )
            );
        }
        return exceptions;    
    }

    //-------------------------------------------------------------------------
    private static String getResultParamTypeName(
        ModelElement_1_0 operationDef,
        Model_1_3 model 
    )  throws ServiceException {
        for(
                Iterator i = operationDef.values("content").iterator();
                i.hasNext();
        ) {
            ModelElement_1_0 paramDef = model.getElement(i.next());
            if("result".equals(paramDef.values("name").get(0))) {
                return (String)model.getElementType(
                    paramDef
                ).values("qualifiedName").get(0);
            }
        }
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ASSERTION_FAILURE,
            "no parameter with name \"result\" defined for operation",
            new BasicException.Parameter("operation", operationDef.path())
        );
    }

    //-------------------------------------------------------------------------
    private static List<AttributeDef> getInParameters(
        ModelElement_1_0 operationDef,
        Model_1_3 model
    ) throws ServiceException {
        List<AttributeDef> inParameters = new ArrayList<AttributeDef>();
        for(
            Iterator i = operationDef.values("content").iterator();
            i.hasNext();
        ) {
            ModelElement_1_0 param = model.getElement(i.next());
            String direction = (String)param.values("direction").get(0);
            if(org.omg.model1.code.DirectionKind.IN_DIR.equals(direction)) {
                inParameters.add(
                    new AttributeDef(
                        param, 
                        model 
                    )              
                );
            }
        }
        return inParameters;
    }

    //-------------------------------------------------------------------------
    public OperationDef(
        String name,
        String qualifiedName,
        String annotation,
        Set stereotype,
        String visibility,
        String qualifiedReturnTypeName,
        boolean isQuery,
        List parameters,
        List exceptions
    ) {
        super(
            name, 
            qualifiedName, 
            annotation,
            stereotype,
            visibility
        );
        this.parameters = parameters;
        this.exceptions = exceptions;
        this.qualifiedReturnTypeName = qualifiedReturnTypeName;
        this.isQuery = isQuery;
    }

    //-------------------------------------------------------------------------
    public String getQualifiedReturnTypeName(
    ) {
        return this.qualifiedReturnTypeName;  
    }

    //-------------------------------------------------------------------------
    public boolean isQuery(
    ) {
        return this.isQuery;
    }

    //-------------------------------------------------------------------------
    public List<StructuralFeatureDef> getParameters(
    ) {
        return this.parameters;
    }

    //-------------------------------------------------------------------------
    public List getExceptions(
    ) {
        return this.exceptions;
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private final List<StructuralFeatureDef> parameters;
    private final List exceptions;
    private final String qualifiedReturnTypeName;
    private final boolean isQuery;

}
