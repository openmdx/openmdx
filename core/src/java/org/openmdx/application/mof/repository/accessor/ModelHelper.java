/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Model Helper 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013, OMEX AG, Switzerland
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.omg.mof.spi.AbstractNames;
import org.openmdx.application.mof.cci.ModelConstraints;
import org.openmdx.application.mof.cci.ModelExceptions;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;


/**
 * Model Helper
 */
class ModelHelper extends org.openmdx.base.mof.cci.ModelHelper {

    /**
     * Constructor 
     */
    private ModelHelper() {
        // Avoid instantiation
    }

    static ModelElement_1_0 getDereferencedType(
        java.lang.Object element,
        Map<String,ModelElement_1_0> elements
    ) throws ServiceException {
        java.lang.Object current = element;
        Set<ModelElement_1_0> visitedElements = null;
        while(true) {
            ModelElement_1_0 modelElement = findElement(current, elements);
            if(modelElement == null) {
                throw new ServiceException (
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.NOT_FOUND, 
                    "element not found in repository. Can not dereference type",
                    new BasicException.Parameter("element", current)
                );
            }
            if(modelElement.isAliasType()) {
                if(visitedElements == null) {
                    visitedElements = new HashSet<ModelElement_1_0>();
                }
                if(visitedElements.contains(modelElement)) {
                    throw new ServiceException (
                        ModelExceptions.MODEL_DOMAIN,
                        ModelExceptions.CIRCULAR_ALIAS_TYPE_DEFINITION, 
                        ModelConstraints.CIRCULAR_TYPE_DEPENCENCY_NOT_ALLOWED,
                        new BasicException.Parameter("element", current)
                    );
                }
                visitedElements.add(modelElement);
                current = modelElement.objGetValue("type");
            } else {
                return modelElement;
            }
        }
    }
    
    static ModelElement_1_0 findElement(
        java.lang.Object element,
        Map<String,ModelElement_1_0> elements
    ) {
        if(element instanceof ModelElement_1_0) {
            return (ModelElement_1_0)element;
        } else if(element instanceof Path) {
            return elements.get(((Path)element).getBase());
        } else if(element instanceof List<?>) {
            String qualifiedElementName = "";
            int ii = 0;
            for(Iterator<?> i = ((List<?>)element).iterator(); i.hasNext(); ii++) {
                qualifiedElementName += (ii == 0 ? "" : ":") + i.next();
            }
            return elements.get(qualifiedElementName);
        } else if(element instanceof String) {
            return elements.get(element);            
        } else {
            throw new UnsupportedOperationException("Unsupported element type. Element is " + element);
        }
    }

    static ModelElement_1_0 getElement(
        java.lang.Object element,
        Map<String,ModelElement_1_0> elements
    ) throws ServiceException {
        ModelElement_1_0 e = findElement(
            element,
            elements
        ); 
        if(e == null) {
            throw new ServiceException (
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.NOT_FOUND, 
                "element not found in model package",
                new BasicException.Parameter("element", element)
            );
        } else {
            return e;
        }
    }

    static boolean referenceIsStoredAsAttribute(
        Object referenceType,
        Map<String,ModelElement_1_0> elements
    ) throws ServiceException {
        return getElement(
            referenceType, 
            elements
        ).isReferenceStoredAsAttribute();
    }

    //-------------------------------------------------------------------------
    static boolean referenceIsDerived(
        java.lang.Object referenceType,
        Map<String,ModelElement_1_0> elements
    ) throws ServiceException {
        ModelElement_1_0 reference = findElement(
            referenceType,
            elements
        );
        ModelElement_1_0 referencedEnd = findElement(
            reference.objGetValue("referencedEnd"),
            elements
        );
        ModelElement_1_0 association = findElement(
            referencedEnd.objGetValue("container"),
            elements
        );
        if(association.objGetList("isDerived").size() < 1) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "missing feature isDerived",
                new BasicException.Parameter("association", association)
            );
        }
        return ((Boolean)association.objGetValue("isDerived")).booleanValue();
    }

    //---------------------------------------------------------------------------  
    static String toJavaPackageName(
        String qualifiedPackageName,
        String packageSuffix
    ) throws ServiceException {
        List<String> packageNameComponents = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(qualifiedPackageName, ":");
        while(tokenizer.hasMoreTokens()) {
            packageNameComponents.add(tokenizer.nextToken());
        }
        //javaPackageName
        StringBuffer javaPackageName = new StringBuffer();
        for(
            int i = 0, iLimit = packageNameComponents.size(); 
            i < iLimit; 
            i++
        )  {
            StringBuffer target = i == 0 ? javaPackageName : javaPackageName.append('.');
            String source = packageNameComponents.get(i);
            AbstractNames.openmdx2NamespaceElement(target, source);
        }
        return javaPackageName.append(
            '.'
        ).append(
            packageSuffix
        ).toString();
    }

}
