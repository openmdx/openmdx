/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Java Connector Architecture: Extended Record Factory Implementation
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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
package org.openmdx.base.resource.spi;

import java.util.List;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.base.resource.cci.ExtendedRecordFactory;
import org.openmdx.kernel.exception.BasicException;

/**
 * This <code>RecordFactory</code> is able to create variable-size and fixed-size
 * records.
 */
public class StandardRecordFactory implements ExtendedRecordFactory { 

    /**
     * No external instantiation allowed
     */
    private StandardRecordFactory(
    ){
        super();
    }
    
    /**
     * The record factory is not yet configurable.
     */
    private static final ExtendedRecordFactory instance = new StandardRecordFactory();

    /**
     * Retrieve the singleton
     * 
     * @return an <code>ExtendedRecordFactory</code> instance
     */
    public static ExtendedRecordFactory getInstance(
    ){
        return StandardRecordFactory.instance;
    }


    //--------------------------------------------------------------------------
    // Implements RecordFactory  
    //--------------------------------------------------------------------------

    /**
     * Creates a MappedRecord with the specified name and the given content.
     * <p>
     * The elements in this map are ordered according to their insertion.
     *
     * @param     recordName
     *            The name of the record acts as a pointer to the meta
     *            information (stored in the metadata repository) for a specific
     *            record type. 
     *
     * @exception ResourceException
     *            Failed to create an initialized MappedRecord.
     *            Example error cases are:<ul>
     *            <li>Invalid specification of record name</li>
     *            <li>Resource adapter internal error</li>
     *            <li>Failed to access metadata repository</li>
     *            </ul>
     * @exception NotSupportedException
     *            Operation not supported
     */
    @Override
    public MappedRecord createMappedRecord(
        String recordName
    ) throws ResourceException {
        return 
    		org.openmdx.base.rest.cci.ConditionRecord.NAME.equals(recordName) ? new org.openmdx.base.rest.spi.ConditionRecord() : 
    		org.openmdx.base.rest.cci.MessageRecord.NAME.equals(recordName) ? new org.openmdx.base.rest.spi.MessageRecord() : 
			org.openmdx.base.rest.cci.FeatureOrderRecord.NAME.equals(recordName) ? new org.openmdx.base.rest.spi.FeatureOrderRecord() : 
            org.openmdx.base.rest.cci.ObjectRecord.NAME.equals(recordName) ? new org.openmdx.base.rest.spi.ObjectRecord() : 
        	org.openmdx.base.rest.cci.QueryFilterRecord.NAME.equals(recordName) ? new org.openmdx.base.rest.spi.QueryFilterRecord() : 
    		org.openmdx.base.rest.cci.QueryExtensionRecord.NAME.equals(recordName) ? new org.openmdx.base.rest.spi.QueryExtensionRecord() : 
            org.openmdx.base.rest.cci.QueryRecord.NAME.equals(recordName) ? new org.openmdx.base.rest.spi.QueryRecord() : 
            org.openmdx.base.rest.cci.VoidRecord.NAME.equals(recordName) ? org.openmdx.base.rest.spi.VoidRecord.getInstance() : 
            new VariableSizeMappedRecord(recordName);
    }

    /* (non-Javadoc)
	 * @see org.openmdx.base.resource.cci.ExtendedRecordFactory#createMappedRecord(java.lang.Class)
	 */
	@Override
	public <T extends MappedRecord> T createMappedRecord(
		Class<T> typedInterface
	) throws ResourceException {
		return cast(
			typedInterface, 
    		org.openmdx.base.rest.cci.ConditionRecord.class == typedInterface ? new org.openmdx.base.rest.spi.ConditionRecord() : 
	        org.openmdx.base.rest.cci.MessageRecord.class == typedInterface ? new org.openmdx.base.rest.spi.MessageRecord() : 
	    	org.openmdx.base.rest.cci.FeatureOrderRecord.class == typedInterface ? new org.openmdx.base.rest.spi.FeatureOrderRecord() : 
	        org.openmdx.base.rest.cci.ObjectRecord.class == typedInterface ? new org.openmdx.base.rest.spi.ObjectRecord() : 
	        org.openmdx.base.rest.cci.QueryFilterRecord.class == typedInterface ? new org.openmdx.base.rest.spi.QueryFilterRecord() : 
	        org.openmdx.base.rest.cci.QueryExtensionRecord.class == typedInterface ? new org.openmdx.base.rest.spi.QueryExtensionRecord() : 
	        org.openmdx.base.rest.cci.QueryRecord.class == typedInterface ? new org.openmdx.base.rest.spi.QueryRecord() : 
	        org.openmdx.base.rest.cci.VoidRecord.class == typedInterface ? org.openmdx.base.rest.spi.VoidRecord.getInstance() :
			null
		);
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.resource.cci.ExtendedRecordFactory#createIndexedRecord(java.lang.Class)
	 */
	@Override
	public <T extends IndexedRecord> T createIndexedRecord(
		Class<T> typedInterface
	) throws ResourceException {
		return cast(
			typedInterface,
    		org.openmdx.base.rest.cci.ResultRecord.class == typedInterface ? new org.openmdx.base.rest.spi.ResultRecord() : 
			null
		);
	}

	private static <T extends Record> T cast(
		Class<T> typedInterface, 
		final Record record
	) throws NotSupportedException {
		if(record == null) {
			throw ResourceExceptions.initHolder(
				new NotSupportedException(
					"Unsupported record interface",
					BasicException.newEmbeddedExceptionStack(
						BasicException.Code.DEFAULT_DOMAIN,
						BasicException.Code.BAD_PARAMETER,
						new BasicException.Parameter("interfaceName", typedInterface.getName()),
						new BasicException.Parameter("isInterface", typedInterface.isInterface())
					)
				)
			);
		}
		return typedInterface.cast(record);
	}

	/**
     * Creates an IndexedRecord with the specified name and the given content.  
     *
     * @param     recordName
     *            The name of the record acts as a pointer to the meta 
     *            information (stored in the metadata repository) for a specific
     *            record type. 
     *
     * @exception ResourceException
     *            Failed to create an initialized IndexedRecord.
     *            Example error cases are:<ul>
     *            <li>Invalid specification of record name</li>
     *            <li>Resource adapter internal error</li>
     *            <li>Failed to access metadata repository</li>
     *            </ul>
     * @exception NotSupportedException
     *            Operation not supported
     */
    @Override
    public IndexedRecord createIndexedRecord(
        String recordName
    ) throws ResourceException {
        return 
            org.openmdx.base.rest.cci.ResultRecord.NAME.equals(recordName) ? new org.openmdx.base.rest.spi.ResultRecord() : 
            new VariableSizeIndexedRecord(recordName);
    }


    //--------------------------------------------------------------------------
    // Implements ExtendedRecordFactory  
    //--------------------------------------------------------------------------

    /**
     * Creates a initialized MappedRecord with the specified name, short description
     * and content.  
     * <p>
     * The MappedRecord is backed up by the given arrays.
     * <p>
     * The elements in this map are ordered according to their insertion 
     * retaining the order of the <code>keys</code> and  <code>values</code>
     * arguments.
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
     * @exception NotSupportedException
     *            Operation not supported
     * @exception	NullPointerException
     *						if <code>values</code> is null.
     */
    @Override
    public MappedRecord asMappedRecord(
        String recordName,
        String recordShortDescription,
        Object[] keys,
        Object[] values
    ) {
        return new FixedSizeMappedRecord(
            recordName,
            recordShortDescription,
            keys,
            values
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ExtendedRecordFactory#singletonMappedRecord(java.lang.String, java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public MappedRecord singletonMappedRecord(
        String recordName,
        String recordShortDescription,
        Object key,
        Object value
    ) {
        return new SingletonMappedRecord(
            recordName,
            recordShortDescription,
            key,
            value
        );   
    }

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
     * @exception NotSupportedException
     *            Operation not supported
     * @exception NullPointerException
     *			  if <code>values</code> is null
     * @exception IllegalArgumentException
     *			  if <code>values</code> is neither an array not a list.
     * 
     */
    @Override
    public IndexedRecord asIndexedRecord(
        String recordName,
        String recordShortDescription,
        Object values
    ) {
        if(values instanceof List<?>) {
            return new DelegatingIndexedRecord(
                recordName,
                recordShortDescription,
                (List<?>) values
            );
        } else if(values instanceof Object[]){
            return new FixedSizeIndexedRecord(
                recordName,
                recordShortDescription,
                (Object[])values
            );
        } else if(values.getClass().isArray()) { 
            return new PrimitiveArrayRecord(
                recordName,
                recordShortDescription,
                values
            );
        } else {
            throw new IllegalArgumentException(
                "The argument 'values' is neither an array nor a List: " + values.getClass().getName() 
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ExtendedRecordFactory#singletonIndexedRecord(java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public IndexedRecord singletonIndexedRecord(
        String recordName,
        String recordShortDescription,
        Object value
    ) {
        return new SingletonIndexedRecord(
            recordName,
            recordShortDescription,
            value
        );
    }

} 
