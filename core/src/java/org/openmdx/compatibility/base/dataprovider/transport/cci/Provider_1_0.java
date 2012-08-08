/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Provider_1_0.java,v 1.7 2008/10/14 16:05:33 hburger Exp $
 * Description: SPICE Provider Layer: Provider interface
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/10/14 16:05:33 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.transport.cci;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.transaction.Synchronization_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Manager_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterProperty;


/**
 * Provider interface.
 */
public interface Provider_1_0 
    extends Synchronization_1_0
{

    /**
     * A provider's exposed paths define which objects are provided by it.
     *
     * @return  the provider's exposed paths
     */
    Path[] getExposedPaths(
    ) throws ServiceException;
  
    /**
     * //... Must be replaced by getTransactionPolicy()
     */
    boolean isTransactionPolicyIsNew(
    ) throws ServiceException;

    //------------------------------------------------------------------------
    // Life cycle management
    //------------------------------------------------------------------------
    
    /**
     * Close the connection.
     * <p>
     * After the close method completes, all methods on the Provider_1_0
     * instance except isClosed throw a ILLEGAL_STATE RuntimeServiceException.
     */
    void close(
  ) throws ServiceException;

    /**
     * This method tells whether the manager is closed.
     * <p> 
     * The isClosed method returns false upon construction of the Provider_1_0
     * instance. It returns true only after the close method completes 
     * successfully. 
     */
    boolean isClosed(
  ) throws ServiceException;
    
    //------------------------------------------------------------------------
    // Query
    //------------------------------------------------------------------------

    /**
     * Retrieve a list containing the identities of all objects for which the
     * <code>referenceFilter</code> as well as the
     * <code>attributeFilter</code> evaluate to true.
     *
     * @param   referenceFilter
     *          an object may be included into the result sets only if it
     *          is accessable through the path passed as
     *          <code>referenceFilter</code>
     * @param   attributeFilter
     *          an object may be included into the result sets only if all
     *          the filter properties evaluate to true if applied to it; 
     *          this argument may be <code>null</code>.
     * @param   attributeSpecifier
     *          An array of attribute specifiers
     * @param   manager
     *          Manager to deliver pre-fetched objects
     * 
     * @return  a list of paths
     *
     * @exception   ServiceException  NOT_SUPPORTED
     *              if no provider for the given reference filter is reachable.
     */
    List<Object> find(
        Path referenceFilter,
        FilterProperty[] attributeFilter,
        AttributeSpecifier[] attributeSpecifier,
        Manager_1_0 manager
     ) throws ServiceException;

    /**
     * Reconstruct the list.
     *
     * @param   referenceFilter
     *          an object may be included into the result sets only if it
     *          is accessable through the path passed as
     *          <code>referenceFilter</code>
     * @param   criteria
     *          Criteria to be used to  reconstruct the list
     * @param   attributeSpecifier
     *          An array of attribute specifiers
     * 
     * @return  a list of paths
     *
     * @exception   ServiceException  NOT_SUPPORTED
     *              if no provider for the given reference filter is reachable.
     */
     List<Object> reconstruct(
        Path referenceFilter,
        Manager_1_0 manager,
        InputStream criteria
     ) throws ServiceException;

    /**
     * Retrieve an object.
     * 
     * @param identity
     *         the object's identity
     *
   * @param requestedSet set of features which are requested to be in
   *         the fetch group. 
   * 
     * @return a map containing the object's default fetch group;
     *          or null if the object doesn't exist. If null, set is defined
   *          by provider.
     * @param   manager
     *          Manager to deliver pre-fetched objects
     *
     * @exception ServiceException  NOT_SUPPORTED
     *            if no provider for the given path is reachable.
     */
    MappedRecord getDefaultFetchGroup(
      Path identity,
      Set<String> requestedSet,
      Manager_1_0 manager
    ) throws ServiceException;

    /**
     * Retrieve an attribute.
     *
     * @param   accessPath
     *          the object's identity
     * @param   name
     *          the attribute name
     * @param   target
     *          where the atttibute should be stored
     * @param   manager
     *          Manager to deliver pre-fetched objects
     * 
     * @return  the specified feature
     *
     * @exception ServiceException  NOT_SUPPORTED
     *            if no provider for the given path is reachable.
     */
    @SuppressWarnings("unchecked")
    void getAttribute(
        Path accessPath,
        String name,
        Map target,
        Manager_1_0 manager
    ) throws ServiceException;

    /**
     * Retrieve part of an attribute
     *
     * @param   accessPath
     *          the object's identity
     * @param   name
     *          the attribute name
     * @param   position
     *          start position of the part to be retrieved
     * @param   capacity
     *          maximal number of elements to be returned
     * 
     * @return  a partof the attribute
     *
     * @exception ServiceException  NOT_SUPPORTED
     *            if no provider for the given path is reachable.
     *            
     * @deprecated as optional objects can't be accepted
     */
    Object getAttributePart(
        Path accessPath,
        String name,
        int position,
        int capacity
    ) throws ServiceException;

    /**
     * Invokes an operation on the specified object.
     *
     * @param   identity
     *          the object's identity
     * @param   name
     *          the operation name
     * @param   arguments
     *          the operation arguments
     *
     * @return  the operation's return values
     *
     * @exception ServiceException  NOT_SUPPORTED
     *            if no provider for the given path is reachable.
     */
    MappedRecord invokeOperation(
        Path identity,
        String name,
        MappedRecord arguments
    ) throws ServiceException;

      
    //------------------------------------------------------------------------
    // Unit of work
    //------------------------------------------------------------------------
  
    /**
     * Create an object.
     *
     * @param identity
     *        the identity of the object
     * @param attributes
     *        the object's attributes
     * @param   manager
     *          Manager to deliver pre-fetched objects
     * 
     */
    void createObject(
        Path identity,
        MappedRecord attributes,
        Manager_1_0 manager
    ) throws ServiceException;  

    /**
     * Modify an object.
     *
     * @param identity
     *        the identity of the object
     * @param attributes
     *        the attributes to be replaced
     * @param   manager
     *          Manager to deliver pre-fetched objects
     */
    void editObject(
        Path identity,
        MappedRecord attributes,
        Manager_1_0 manager
    ) throws ServiceException;  
    
    /**
     * Remove an object.
     *
     * @param identity
     *        the identity of the object
     * @param   manager
     *          Manager to invalidate not found objects
     */
    void removeObject(
        Path identity,
        Manager_1_0 manager
    ) throws ServiceException;  
                
}
