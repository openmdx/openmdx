/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TraversalHandler.java,v 1.3 2009/06/09 15:39:58 hburger Exp $
 * Description: A traversal handler receives and treats callbacks from a Traverser. 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 15:39:58 $
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
 * The TraversalHandler receives and treats callbacks from a Traverser.
 * 
 * 
 *  @author  anyff
 *
 */
@SuppressWarnings("unchecked")
public interface TraversalHandler {

    /** no special treatment of transactions */
    static public short NO_TRANSACTION = 0;

    /** a new transaction for each top level object */
    static public short TOP_LEVEL_TRANSACTION = 1;

    /** a new transaction per object (including roles and states) */
    static public short OBJECT_TRANSACTION = 2;

    /** a new transaction for the entire synchronization */
    static public short ONE_TRANSACTION = 3;
    
    /** Treatment of object */
    /** ignore the object */
    static public short NULL_OP = 0;
    /** synchronize the object */
    static public short SET_OP = 1;
    
    /** for future use */
    static public short CREATE_OP = 2;
    static public short UPDATE_OP = 3;
    static public short OPERATION_OP = 4; 

    /**
     * start of a reference. 
     * <p>
     * The parameter name can be the full qualifiedName of the reference or just
     * the name.
     * <p> 
     * The return value indicates if this reference should be followed 
     * (true) or if it should be omitted (false). Even if false is returned
     * (omit the reference), the corresponding endReference gets called.
     * <p>
     * The Traverser may use this return value to omit further processing.
     * 
     * @param name     name or qualifiedName of the reference
     * @return boolean continue processing this reference
     */
    public boolean startReference(String name) throws ServiceException;

    /**
     * End of a reference. 
     * <p>
     * All objects at this reference have been treated. The parameter name must
     * be the same as in startReference().
     * 
     * @param reference  name or qualifiedName of the reference
     */
    public void endReference(String reference) throws ServiceException;

    /**
     * Start of a new object.
     * <p>
     * The corresponding endObject() gets called, even if false was returned.
     * <p>
     * The operation parameter decides if the object should be synchronized
     * (SET_OP) or ignored (NULL_OP). Objects to be ignored are delivered 
     * to ensure that the object tree in the handler is complete.
     * 
     * @param qualifiedName qualifiedName of the class 
     * @param qualifierName name of the qualifier for this class
     * @param id            id of current object
     * @param operation     if the object should be synchronized or not
     * @return boolean      continue processing this object
     */
    public boolean startObject(
        Path reference,
        String qualifiedName, 
        String qualifierName, 
        String id, 
        short operation
    ) throws ServiceException;

    /**
     * End of the object. 
     * <p>
     * All the data of this object and all the references have been treated. The
     * qualifiedName must be the same as in startObject.
     * 
     * @param qualifiedName  qualifiedName of the object
     */
    public void endObject(String qualifiedName) throws ServiceException;

    /**
     * Return attribute-specific tags. Tags are user-defined string values and are
     * added to the output stream as comment. 
     */
    public Map getAttributeTags(MappedRecord object) throws ServiceException;
    
    /**
     * Provide the object with all its features. 
     * <p>
     * The format of the object is as a valid openMDX object. This provides the
     * possibility to have several TraversalHandler in a row.
     * 
     * @param object   object containing all features 
     * @param tags     attribute tags added as XML comment <!-- tag value -->
     * @return boolean continue processing of this object
     */
    public boolean featureComplete(
        Path reference,
        MappedRecord object
    ) throws ServiceException;

    /**
     * Provide all the contained objects of the object.
     * <p>
     * The map contains the references as key and Collections of the referenced
     * objects ids.
     * 
     * @param path          path of the container object
     * @param objectClassName  name of the object class
     * @param referenceMap  map references to objects at the reference
     */
    public void contentComplete(Path objectPath, String objectClassName, List containedReferences) throws ServiceException;
    
    /**
     * Provide all the contained objects at the reference.
     * <p>
     * The list contains all the object ids of the objects at this path.
     * @param startPaths
     * @throws ServiceException
     */
    public void referenceComplete(Path reference, Collection objectIds) throws ServiceException;

    /**
     * Start of the traversal process. 
     * <p> 
     * May be used for initialisations. E.g. start a transaction.
     * 
     * @param  startPaths  list of paths at which the traversal started
     * @throws ServiceException
     */
    public void startTraversal(List startPaths) throws ServiceException;

    /**
     * End of the traversal process. 
     * <p> 
     * May be used to free resources. Is guaranteed to be called, even if
     * there were processing errors.
     * 
     * @throws ServiceException
     */
    public void endTraversal() throws ServiceException;

    /**
     * Set the transaction behavior.
     * <p>
     * If a certain behavior can't be supported by an implementation of this
     * interface, it should throw a not supported exception. 
     */
    public void setTransactionBehavior(short transactionBehavior) throws ServiceException;

    /**
     * Get the currently active transaction behavior.
     */
    public short getTransactionBehavior();

}
