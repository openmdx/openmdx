/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ExtendedRecordFactory.java,v 1.7 2009/02/19 16:30:58 hburger Exp $
 * Description: Java Connector Architecture: Initialized Record Factory
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/19 16:30:58 $
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
 * This product includes software developed by other organizations 
 * as listed in the NOTICE file.
 */
package org.openmdx.base.resource.cci;

import java.util.List;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.RecordFactory;

/**
 * The ExtendedRecordFactory interface is used for creating initialized
 * MappedRecord and IndexedRecord instances.
 */
public interface ExtendedRecordFactory 
	extends RecordFactory
{

  //--------------------------------------------------------------------------
  // MappedRecord Factory
  //--------------------------------------------------------------------------

  /**
   * Creates a MappedRecord with the given name and short description.  
   *
   * @param     recordName
   *            The name of the record acts as a pointer to the meta 
   *            information (stored in the metadata repository) for a specific
   *            record type. 
   * @param     recordShortDescription
   *            The short description of the Record; or null.
   *
   * @exception ResourceException
   *            Failed to create an initialized MappedRecord.
   *            Example error cases are:<ul>
   *            <li>Invalid specification of record name</li>
   *            <li>Resource adapter internal error</li>
   *            <li>Failed to access metadata repository</li>
   *            </ul>
   */
  MappedRecord createMappedRecord(
    String recordName,
    String recordShortDescription
  ) throws ResourceException;
  	
  /**
   * Creates a MappedRecord with the given name, short description and
   * content.  
   *
   * @param     recordName
   *            The name of the record acts as a pointer to the meta 
   *            information (stored in the metadata repository) for a specific
   *            record type. 
   * @param     recordShortDescription
   *            The short description of the Record; or null.
   * @param     content
   *            The content to be copied into the record instance.
   *
   * @exception ResourceException
   *            Failed to create an initialized MappedRecord.
   *            Example error cases are:<ul>
   *            <li>Invalid specification of record name</li>
   *            <li>Resource adapter internal error</li>
   *            <li>Failed to access metadata repository</li>
   *            </ul>
   */
  MappedRecord createMappedRecord(
    String recordName,
    String recordShortDescription,
    Map<?,?> content
  ) throws ResourceException;
  	
  /**
   * Creates a MappedRecord with the given name, short description and
   * content.  
   *
   * @param     recordName
   *            The name of the record acts as a pointer to the meta
   *            information (stored in the metadata repository) for a specific 
   *            record type. 
   * @param     recordShortDescription
   *            The short description of the Record; or null.
   * @param     keys
   *            The keys of the mapped record
   * @param     values
   *            The values of the mapped record sorted according to the keys
   *
   * @exception ResourceException
   *            Failed to create an initialized MappedRecord.
   *            Example error cases are:<ul>
   *            <li>Invalid specification of record name</li>
   *            <li>Resource adapter internal error</li>
   *            <li>Failed to access metadata repository</li>
   *            </ul>
   */
  MappedRecord createMappedRecord(
    String recordName,
    String recordShortDescription,
    List<?> keys,
    List<?> values
  ) throws ResourceException;
  	
  /**
   * Creates a MappedRecord with the given name, short description and
   * content.  
   * <p>
   * The MappedRecord is backed up by the given arrays.
   *
   * @param     recordName
   *            The name of the record acts as a pointer to the meta
   *            information (stored in the metadata repository) for a specific 
   *            record type. 
   * @param     recordShortDescription
   *            The short description of the Record; or null.
   * @param     keys
   *            The keys of the mapped record
   * @param     values
   *            The values of the mapped record sorted according to the keys
   *
   * @exception ResourceException
   *            Failed to create an initialized MappedRecord.
   *            Example error cases are:<ul>
   *            <li>Invalid specification of record name</li>
   *            <li>Resource adapter internal error</li>
   *            <li>Failed to access metadata repository</li>
   *            </ul>
   */
  MappedRecord asMappedRecord(
    String recordName,
    String recordShortDescription,
    Object keys,
    Object values
  ) throws ResourceException;
  	

  //--------------------------------------------------------------------------
  // IndexedRecord Factory
  //--------------------------------------------------------------------------

  /**
   * Creates an IndexedRecord with the given name and short description.  
   *
   * @param     recordName
   *            The name of the record acts as a pointer to the meta 
   *            information (stored in the metadata repository) for a specific
   *            record type. 
   * @param     recordShortDescription
   *            The short description of the Record; or null.
   * @param     values
   *            The values of the indexed record.
   *
   * @exception ResourceException
   *            Failed to create an initialized IndexedRecord.
   *            Example error cases are:<ul>
   *            <li>Invalid specification of record name</li>
   *            <li>Resource adapter internal error</li>
   *            <li>Failed to access metadata repository</li>
   *            </ul>
   */
  IndexedRecord createIndexedRecord(
    String recordName,
    String recordShortDescription
  ) throws ResourceException;
    
  /**
   * Creates an IndexedRecord with the given name, short description and
   * content.  
   *
   * @param     recordName
   *            The name of the record acts as a pointer to the meta 
   *            information (stored in the metadata repository) for a specific
   *            record type. 
   * @param     recordShortDescription
   *            The short description of the Record; or null.
   * @param     values
   *            The values of the indexed record.
   *
   * @exception ResourceException
   *            Failed to create an initialized IndexedRecord.
   *            Example error cases are:<ul>
   *            <li>Invalid specification of record name</li>
   *            <li>Resource adapter internal error</li>
   *            <li>Failed to access metadata repository</li>
   *            </ul>
   */
  IndexedRecord createIndexedRecord(
    String recordName,
    String recordShortDescription,
    List<?> values
  ) throws ResourceException;
    
  /**
   * Creates an IndexedRecord with the given name, description and content.  
   * <p>
   * The Record is backed up by the given array.
   *
   * @param     recordName
   *            The name of the record acts as a pointer to the meta 
   *            information (stored in the metadata repository) for a specific
   *            record type. 
   * @param     recordShortDescription
   *            The short description of the Record; or null.
   * @param     values
   *            The values of the indexed record represented by a List or an 
   *            array of objects or primitive types.
   *
   * @exception ResourceException
   *            Failed to create an initialized IndexedRecord.
   *            Example error cases are:<ul>
   *            <li>Invalid specification of record name</li>
   *            <li>Resource adapter internal error</li>
   *            <li>Failed to access metadata repository</li>
   *            </ul>
   */
  IndexedRecord asIndexedRecord(
    String recordName,
    String recordShortDescription,
    Object values
  ) throws ResourceException;

}
