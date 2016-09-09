/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Data Object interface
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.cci;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.UUID;

import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;

/**
 * The openMDX Service Data Object interface.
 * <p>
 * The core of the openMDX Data Accessor Framework is the Service Data 
 * Object (<code>SDO</code>), which is a generic object and is not tied to any 
 * specific persistent storage or service mechanism. An <code>SDO</code> is used 
 * in conjunction with data accessors which populate <code>SDO</code>s from back-end 
 * data sources. For more information on <code>SDO</code>s see the
 * <a href="http://www.ibm.com/developerworks/java/library/j-sdo/">SDO specification</a>. 
 * <p> 
 * An openMDX <code>SDO</code> implements the <code>DataObject_1_0</code> 
 * interface. The API gives access to typed and untyped <code>SDO</code>s. This 
 * has the following advantages:
 * <p>
 * <ul><li>Data Objects often have typed interfaces. However, sometimes it is
 *   either impossible or undesirable to create interfaces to represent the 
 *   Data Objects. One common reason for this is when the data being transferred 
 *   is defined by the output of a query. Examples would be:
 *   <ul>
 *     <li>A relational query against a relational persistence store.
 *     <li>An EJBQL queries against an EJB entity bean domain model.
 *     <li>Web services.
 *     <li>XML queries against an XML source.
 *     <li>When deployment of generated code is not practical.
 *   </ul>
 *   In these situations, it is necessary to use a dynamic store and associated 
 *   API. <code>DataObject_1_0</code> has the ability to represent Data Objects 
 *   through a standard generic data API.
 * <li>In cases where metadata is known at development time (for example, the 
 *   UML model or the SQL relational schema is known), <code>DataObject_1_0</code> 
 *   allows a generic interface to access typed objects such as
 *   <ul><li>Popular XML schema languages.
 *     <li>Relational database schemas with queries known at the time of code generation.
 *     <li>Web services, when the message is specified by an XML schema.
 *     <li>JCA connectors.
 *     <li>JMS message formats.
 *     <li>UML models
 *   </ul>
 * <li>Many applications are coded with built-in knowledge of the shape of the data
 *   being returned. These applications know which methods to call or fields to 
 *   access on the Data Objects they use. However, in order to enable development 
 *   of generic or framework code that works with Data Objects, it is important to 
 *   be able to introspect on Data Object metadata, which exposes the data model for 
 *   the Data Objects. The object class of a Data Object allows to retrieve the
 *   object's metadata from a <code>MOF</code> repository.
 * </ul>N
 * <p>
 * A <code>DataObject_1_0</code> extends from the <code>javax.jdo.spi.PersistenceCapable</code>.
 * As a consequence, a <code>DataObject_1_0</code> offers the semantics as a <code>JDO 
 * PersistenceCapable</code>. A <code>DataObject_1_0</code> is managed by
 * a Data Access Service (which is in turn a <code>javax.jdo.PersistenceManager</code>) 
 * and maintains the object states and lifecycle defined by the 
 * <a href="http://java.sun.com/jdo/index.jsp">JDO specification</a>.    
 * <p>
 * Summarized a <code>DataObject_1_0</code> 
 * <ul>
 *   <li>is conceptually an <code>SDO</code> specified by the <a href="http://www.ibm.com/developerworks/java/library/j-sdo/">SDO specification</a>. 
 *     However, it does not implement the <code>commonj.sdo.DataObject</code> interface 
 *     mainly for the following reasons: 1) The object states and lifecycle are not as complete 
 *     and precisely specified as the <code>javax.jdo.spi.PersistenceCapable</code> does.
 *     2) The many typed getters and setters are replaced by untyped getters and setters
 *     which make the interface easier to use and to implement. Attribute types can
 *     be derived from a <code>MOF</code> repository.
 *   <li>implements the the interface <code>javax.jdo.spi.PersistenceCapable</code> and adds
 *     generic attribute value setters and getters. It also adds methods which allow to
 *     invoke operations on <code>SDO</code>s.
 * </ul>
 */
public interface DataObject_1_0 
    extends PersistenceCapable, org.openmdx.base.persistence.spi.Cloneable<DataObject_1_0>
{

    /** 
     * Return the openMDX identity associated with this instance <em>(i.e. not a copy 
     * as prescribed by JDO)</em>.
     * <P>
     * Transient instances return null.
     * 
     * @return the object id
     */
	Path jdoGetObjectId();
	
    /** 
     * Return a transient object id in case of transient object instances,
     * and <code>null</code> for persistent instances <em>(i.e. not the transactional 
     * object id as prescribed by JDO)</em>.
     *
     * @see #jdoGetObjectId()
     * @see PersistenceManager#getObjectId(Object pc)
     * @see PersistenceManager#getObjectById(Object oid, boolean validate)
     * @return a transient object id
     */
    UUID jdoGetTransactionalObjectId();
    
    /**
     * Returns the object's model class.
     *
     * @return  the object's model class
     *
     * @exception   ServiceException  
     *              if the information is unavailable
     */
    String objGetClass(
    ) throws ServiceException;

    /**
     * Returns a new set containing the names of the features in the default
     * fetch group.
     * <p>
     * The returned set is a copy of the original set, i.e. interceptors are
     * free to modify it before passing it on.
     *
     * @return  the names of the features in the default fetch group
     *
     * @exception   ServiceException  
     *              if the information is unavailable
     */
    Set<String> objDefaultFetchGroup(
    ) throws ServiceException;

    /**
     * The move operation moves the object to the scope of the container passed
     * as the first parameter. The object remains valid after move has
     * successfully executed.
     *
     * @param     there
     *            the object's new container.
     * @param     criteria
     *            The criteria is used to move the object to the container or 
     *            <code>null</null>, in which case it is up to the
     *            implementation to define the criteria.
     *
     * @exception ServiceException  ILLEGAL_STATE
     *            if the object is persistent.
     * @exception ServiceException BAD_PARAMETER
     *            if <code>there</code> is <code>null</code>.
     * @exception ServiceException  
     *            if the move operation fails.
     */
    void objMove(
        Container_1_0 there,
        String criteria
    ) throws ServiceException;

    //------------------------------------------------------------------------
    // Values
    //------------------------------------------------------------------------

    /**
     * Set an attribute's value.
     * <p>
     * This method returns a <code>BAD_PARAMETER</code> exception unless the 
     * feature is single valued or a stream. 
     *
     * @param       feature
     *              the attribute's name
     * @param       to
     *              the object.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is write protected 
     * @exception   ServiceException BAD_PARAMETER
     *              if the feature is multi-valued
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     * @exception   ServiceException 
     *              if the object is not accessible
     */
    void objSetValue(
        String feature,
        Object to
    ) throws ServiceException;

    /**
     * Get a single-valued attribute.
     * <p>
     * This method returns a <code>BAD_PARAMETER</code> exception unless the 
     * feature is single valued or a stream. 
     *
     * @param       feature
     *              the feature's name
     *
     * @return      the object representing the feature;
     *              or null if the feature's value hasn't been set yet.
     *
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     * @exception   ServiceException BAD_PARAMETER
     *              if the feature is multi-valued
     * @exception   ServiceException 
     *              if the object is not accessible
     */
    Object objGetValue(
        String feature
    ) throws ServiceException;

    /**
     * Get a List attribute.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     * @exception   ClassCastException
     *              if the feature's value is not a list
     */
    List<Object> objGetList(
        String feature
    ) throws ServiceException;
    
    /**
     * Get a Set attribute.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     * @exception   ClassCastException
     *              if the feature's value is not a set
     */
    Set<Object> objGetSet(
        String feature
    ) throws ServiceException;

    /**
     * Get a SparseArray attribute.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ClassCastException
     *              if the feature's value is not a sparse array
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     */
    SortedMap<Integer,Object> objGetSparseArray(
        String feature
    ) throws ServiceException;

    /**
     * Get a Map attribute.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a map which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     * @exception   ClassCastException
     *              if the feature's value is not a set
     */
    @SuppressWarnings("rawtypes")
    Map objGetMap(
        String feature
    ) throws ServiceException;
    
    /**
     * Get a reference feature.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ClassCastException
     *              if the feature is not a reference
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     */
    Container_1_0 objGetContainer(
        String feature
    ) throws ServiceException;

    //------------------------------------------------------------------------
    // Operations
    //------------------------------------------------------------------------

    /** 
     * Executes an interaction represented by the InteractionSpec.
     * This form of invocation takes an input Record and updates
     * the output Record. 
     *  
     *  @param   ispec   InteractionSpec representing a target EIS 
     *                   data/function module   
     *  @param   input   Input Record
     *  @param   output  Output Record
     * 
     *  @return  true if execution of the EIS function has been 
     *           successful and output Record has been updated; false
     *           otherwise
     *
     *  @throws  ResourceException   Exception if execute operation
     *                               fails. Examples of error cases
     *                               are:
     *         <UL>
     *           <LI> Resource adapter internal, EIS-specific or 
     *                communication error 
     *           <LI> Invalid specification of an InteractionSpec, 
     *                input or output record structure
     *           <LI> Errors in use of input or output Record
     *           <LI> Invalid connection associated with this 
     *                Interaction
     *         </UL>
     *  @throws NotSupportedException Operation not supported 
     *                             
     */
    boolean execute(
        InteractionSpec ispec, 
        Record input, 
        Record output
    ) throws ResourceException;
    
    /**
     * Return the object's container
     * 
     * @param lazily if <code>true</code> the method returns <code>null</code> for persistent 
     * objects with not yet initialized container reference 
     * 
     * @return the object's container if <code>objIsContained()</code>, <code>null</code> otherwise 
     */
    Container_1_0 getContainer(
        boolean lazily
    );

    /**
     * Retrieve this object's persistent aspects
     * 
     * @return the persistent aspects
     * 
     * @throws ServiceException 
     */
    Container_1_0 getAspects(
    ) throws ServiceException;
    
    /**
     * Retrieve a specific aspect
     * 
     * @param aspectType the aspect type such as 
     * 
     * @return the specific aspect
     * 
     * @throws ServiceException 
     */
    Map<String, DataObject_1_0> getAspect(
        String aspectType
    );
    
    /**
     * Tests whether object is member of a container.
     * 
     * @return <code>true</code> if object was moved into a container.
     */
    boolean objIsContained(
    );

    /**
     * Tests whether this object can't leave its hollow state
     *
     * @return  true if this instance is inaccessible
     */
    boolean objIsInaccessible(
    );

    /**
     * Tells whether retrieving the object did fail with a not found exception
     *
     * @return  true if the object does not exists
     */
    boolean objDoesNotExist(
    );
    
    /**
     * Retrieve the inaccessibility reason.
     *
     * @return Returns the inaccessibilityReason.
     */
    ServiceException getInaccessibilityReason(
    ) throws ServiceException;
    
}
