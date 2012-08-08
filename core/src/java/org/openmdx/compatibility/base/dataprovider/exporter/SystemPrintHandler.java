/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: SystemPrintHandler.java,v 1.8 2009/01/06 13:14:45 wfro Exp $
 * Description: SystemPrintHandler class
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/06 13:14:45 $
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
package org.openmdx.compatibility.base.dataprovider.exporter;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;

@SuppressWarnings("unchecked")
public class SystemPrintHandler extends DelegatingHandler {

    public SystemPrintHandler(TraversalHandler delegation) {
        super(delegation);
    }

    public boolean startReference(
        String reference
    ) throws ServiceException {

        if (reference != null) {
            System.out.println("startReference");

            System.out.println("\t reference:");
            System.out.println("\t\t" + reference);
        }
        return super.startReference(reference);
    }

    public void endReference(
        String reference
    ) throws ServiceException {

        if (reference != null) {
            System.out.println("endReference");

            System.out.println("\t reference:");
            System.out.println("\t\t" + reference);
        }
        super.endReference(reference);
    }

    public boolean startObject(
        Path parentPath,
        String qualifiedName, 
        String qualifierName, 
        String id,
        short properties
    ) throws ServiceException {
        System.out.println("startObject");
        System.out.println("\t qualifiedName:");
        System.out.println("\t\t" + qualifiedName);
        System.out.println("\t qualifierName:");
        System.out.println("\t\t" + qualifierName);
        System.out.println("\t id:");
        System.out.println("\t\t" + id);
        return super.startObject(
            parentPath,
            qualifiedName, 
            qualifierName, 
            id, 
            properties
        );
    }

    public void endObject(
        String qualifiedName
    ) throws ServiceException {

        System.out.println("endObject");

        System.out.println("\t qualifiedName:");
        System.out.println("\t\t" + qualifiedName);

        super.endObject(qualifiedName);
    }

    public boolean featureComplete(
        Path parentPath,
        DataproviderObject_1_0 object
    ) throws ServiceException {

        System.out.println("featureComplete");

        System.out.println("\t object:");
        System.out.println(
            "\t\t"
            + "path: " + object.path()
            + " identity: " + object.getValues(SystemAttributes.OBJECT_IDENTITY)
            + " class: " + object.getValues(SystemAttributes.OBJECT_CLASS)
            + " validFrom: " + object.getValues("object_validFrom")
            + " validTo: " + object.getValues("object_validTo")
        );

        return super.featureComplete(
            parentPath,
            object
        );
    }

    public void contentComplete(
        Path objectPath,
        String objectClassName,
        List containedReferences
    ) throws ServiceException {

        System.out.println("contentComplete");

        System.out.println("\t objectPath:");
        System.out.println("\t\t" + objectPath);
        System.out.println("\t objectClassName:");
        System.out.println("\t\t" + objectClassName);
        System.out.println("\t containedReferences:");
        Iterator references = containedReferences.iterator();
        while (references.hasNext()) {
            System.out.println("\t\t" + references.next().toString());
        }

        super.contentComplete(objectPath, objectClassName, containedReferences);
    }


    public void referenceComplete(
        Path reference, 
        Collection objectIds
    ) throws ServiceException {

        System.out.println("referenceComplete");

        System.out.println("\t reference:");
        System.out.println("\t\t" + reference);
        System.out.println("\t objectIds:");
        Iterator ids = objectIds.iterator();
        while (ids.hasNext()) {
            System.out.println("\t\t" + ids.next().toString());
        }

        super.referenceComplete(reference, objectIds);
    }

    public void startTraversal(
        List startPaths
    ) throws ServiceException {

        System.out.println("startTraversal");

        System.out.println("\t startPaths:");
        Iterator paths = startPaths.iterator();
        while (paths.hasNext()) {
            System.out.println("\t\t" + paths.next().toString());
        }

        super.startTraversal(startPaths);
    }

    public void endTraversal() throws ServiceException {

        System.out.println("endTraversal");

        super.endTraversal();
    }

    public void setTransactionBehavior(
        short transactionBehavior
    ) throws ServiceException {

        super.setTransactionBehavior(transactionBehavior);
    }

    public short getTransactionBehavior() {

        return super.getTransactionBehavior();
    }

}
