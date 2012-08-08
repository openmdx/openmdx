/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DelegatingHandler.java,v 1.2 2009/06/01 15:37:12 wfro Exp $
 * Description: 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/01 15:37:12 $
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
package org.openmdx.application.dataprovider.exporter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;

/**
 * Default implementation of a intermediate handler, which delegates all 
 * callbacks. To build an intermediate handler with some logic, just derive
 * from this Class.
 * 
 * @author anyff
 */
@SuppressWarnings("unchecked")
public class DelegatingHandler implements TraversalHandler {

    /**
     * Initialize a DelegatingHandler with the handler it delegates to.
     * 
     * @param delegation   TraversalHandler to delegate to
     */
    public DelegatingHandler(TraversalHandler delegation) {
        this.delegation = delegation;
        this.transactionBehavior = TraversalHandler.NO_TRANSACTION;
    }

    /**
     * Delegate call.
     * 
     * @see org.openmdx.application.dataprovider.exporter.TraversalHandler#startReference(java.lang.String)
     */
    public boolean startReference(
        String name
    ) throws ServiceException {
        return this.delegation.startReference(name);
    }

    /**
     * Delegate call.
     * 
     * @see org.openmdx.application.dataprovider.exporter.TraversalHandler#endReference(java.lang.String)
     */
    public void endReference(
        String reference
    ) throws ServiceException {
        this.delegation.endReference(reference);
    }

    /** 
     * Delegate call.
     * 
     * @see org.openmdx.application.dataprovider.exporter.TraversalHandler#startObject(java.lang.String, java.lang.String, java.lang.String, short)
     */
    public boolean startObject(
        Path reference,
        String qualifiedName,
        String qualifierName,
        String id,
        short operation
    ) throws ServiceException {
        return this.delegation.startObject(
            reference,
            qualifiedName, 
            qualifierName, 
            id, 
            operation
        );
    }

    /**
     * Delegate call.
     * 
     * @see org.openmdx.application.dataprovider.exporter.TraversalHandler#endObject(java.lang.String)
     */
    public void endObject(
        String qualifiedName
    ) throws ServiceException {
        this.delegation.endObject(qualifiedName);
    }

    public Map getAttributeTags(
        MappedRecord object
    ) throws ServiceException {
        return this.delegation.getAttributeTags(object);
    }
    
    /**
     * Delegate call.
     * 
     * @see org.openmdx.application.dataprovider.exporter.TraversalHandler#featureComplete(org.openmdx.application.dataprovider.cci.DataproviderObject_1_0)
     */
    public boolean featureComplete(
        Path reference,
        MappedRecord object
    ) throws ServiceException {
        return this.delegation.featureComplete(
            reference,
            object
        );
    }

    /**
     * Delegate call.
     * 
     * @see org.openmdx.application.dataprovider.exporter.TraversalHandler#contentComplete(org.openmdx.base.naming.Path, java.lang.String, java.util.List)
     */
    public void contentComplete(
        Path objectPath, 
        String objectClassName, 
        List containedReferences
    ) throws ServiceException {
        this.delegation.contentComplete(objectPath, objectClassName, containedReferences);
    }

    /**
     * Delegate call.
     * 
     * @see org.openmdx.application.dataprovider.exporter.TraversalHandler#referenceComplete(org.openmdx.base.naming.Path, java.util.Collection)
     */
    public void referenceComplete(
        Path reference, 
        Collection objectIds
    ) throws ServiceException {
        this.delegation.referenceComplete(reference, objectIds);

    }

    /** 
     * Delegate call.
     * 
     * @see org.openmdx.application.dataprovider.exporter.TraversalHandler#startTraversal(java.util.List)
     */
    public void startTraversal(
        List startPaths
    ) throws ServiceException {
        this.delegation.startTraversal(startPaths);
    }

    /** 
     * Delegate call.
     * 
     * @see org.openmdx.application.dataprovider.exporter.TraversalHandler#endTraversal()
     */
    public void endTraversal() throws ServiceException {
        this.delegation.endTraversal();

    }

    /**
     * Intermediate handlers probably are not aware of any transactions.
     * 
     * @see org.openmdx.application.dataprovider.exporter.TraversalHandler#setTransactionBehavior(short)
     */
    public void setTransactionBehavior(
        short transactionBehavior
    ) throws ServiceException {
        // just accept all
        this.transactionBehavior = transactionBehavior;
    }

    /**
     * Return the transaction behavior.
     * 
     * @see org.openmdx.application.dataprovider.exporter.TraversalHandler#getTransactionBehavior()
     */
    public short getTransactionBehavior() {
        return this.transactionBehavior;
    }

    /** TraversalHandler to delegate to */
    private TraversalHandler delegation;

    /** must keep it to reply on get request */
    private short transactionBehavior;


}
