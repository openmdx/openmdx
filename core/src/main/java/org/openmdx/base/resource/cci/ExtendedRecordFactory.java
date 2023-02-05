/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Java Connector Architecture: Initialized Record Factory
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.RecordFactory;

/**
 * The ExtendedRecordFactory interface is used for creating
 * {@code MappedRecord} and {@code IndexedRecord} instances wrapping
 * the given data.
 */
public interface ExtendedRecordFactory extends RecordFactory {

    //--------------------------------------------------------------------------
    // MappedRecord Factory
    //--------------------------------------------------------------------------

    /**
     * Creates an {@code MappedRecord} of the given type.
     * 
     * @param typedInterface
     *            the interface the record shall implement, one of
     *            <ul>
     *            <li>{@link org.openmdx.base.resource.cci.SparseArrayRecord}
     *            <li>{@link org.openmdx.base.rest.cci.ConditionRecord}
     *            <li>{@link org.openmdx.base.rest.cci.MessageRecord}
     *            <li>{@link org.openmdx.base.rest.cci.FeatureOrderRecord}
     *            <li>{@link org.openmdx.base.rest.cci.ObjectRecord}
     *            <li>{@link org.openmdx.base.rest.cci.QueryFilterRecord}
     *            <li>{@link org.openmdx.base.rest.spi.QueryFilterRecord}
     *            <li>{@link org.openmdx.base.rest.cci.QueryExtensionRecord}
     *            <li>{@link org.openmdx.base.rest.cci.QueryRecord}
     *            <li>{@link org.openmdx.base.rest.cci.VoidRecord}
     *            </ul>
     * 
     * @return a new record of the given type
     * @throws ResourceException
     *             Failed to create a MappedRecord.
     */
    <T extends MappedRecord> T createMappedRecord(
        Class<T> typedInterface
    )
        throws ResourceException;

    /**
     * Creates a MappedRecord with the given name, short description and
     * content.
     * <p>
     * The MappedRecord is backed up by the given arrays.
     *
     * @param recordName
     *            The name of the record acts as a pointer to the meta
     *            information (stored in the metadata repository) for a specific
     *            record type.
     * @param recordShortDescription
     *            The short description of the Record; or null.
     * @param keys
     *            The keys of the mapped record
     * @param values
     *            The values of the mapped record sorted according to the keys
     *
     * @exception ResourceException
     *                Failed to create an initialized MappedRecord.
     *                Example error cases are:
     *                <ul>
     *                <li>Invalid specification of record name</li>
     *                <li>Resource adapter internal error</li>
     *                <li>Failed to access metadata repository</li>
     *                </ul>
     */
    MappedRecord asMappedRecord(
        String recordName,
        String recordShortDescription,
        Object[] keys,
        Object[] values
    );

    /**
     * Creates a MappedRecord with the given name, short description and
     * content.
     * 
     * @param recordName
     *            The name of the record acts as a pointer to the meta
     *            information (stored in the metadata repository) for a specific
     *            record type.
     * @param recordShortDescription
     *            The short description of the Record; or {@code null</null>.
     * @param key
     *            The key of the single mapped record entry
     * @param value
     *            The value of the single mapped record entry
     */
    MappedRecord singletonMappedRecord(
        String recordName,
        String recordShortDescription,
        Object key,
        Object value
    );

    //--------------------------------------------------------------------------
    // IndexedRecord Factory
    //--------------------------------------------------------------------------

    /**
     * Creates an {@code IndexdRecord} of the given type.
     * 
     * @param typedInterface
     *            the interface the record shall implement, one of
     *            <ul>
     *            <li>{@link org.openmdx.base.resource.cci.ListRecord}
     *            <li>{@link org.openmdx.base.resource.cci.SetRecord}
     *            <li>{@link org.openmdx.base.rest.cci.ResultRecord}
     *            </ul>
     * 
     * @return a new record of the given type
     * 
     * @throws ResourceException
     *             Failed to create a MappedRecord.
     */
    <T extends IndexedRecord> T createIndexedRecord(
        Class<T> typedInterface
    )
        throws ResourceException;

    /**
     * Creates an IndexedRecord with the given name, description and content.
     * <p>
     * The Record is backed up by the given array.
     *
     * @param recordName
     *            The name of the record acts as a pointer to the meta
     *            information (stored in the metadata repository) for a specific
     *            record type.
     * @param recordShortDescription
     *            The short description of the Record; or {@code null</null>.
     * @param values
     *            The values of the indexed record represented by a List or an
     *            array of objects or primitive types.
     */
    IndexedRecord asIndexedRecord(
        String recordName,
        String recordShortDescription,
        Object values
    );

    /**
     * Creates an IndexedRecord with the given name, description and content.
     *
     * @param recordName
     *            The name of the record acts as a pointer to the meta
     *            information (stored in the metadata repository) for a specific
     *            record type.
     * @param recordShortDescription
     *            The short description of the Record; or {@code null</null>.
     * @param value
     *            The single value of the indexed record.
     *            
     * @deprecated as it's semantic (set or list) is unknown           
     */
    @Deprecated
    IndexedRecord singletonIndexedRecord(
        String recordName,
        String recordShortDescription,
        Object value
    );

    /**
     * Creates an {@code MappedRecord} of the given type.
     * 
     * @param typedInterface
     *            the interface the record shall implement, one of
     *            <ul>
     *            <li>{@link org.openmdx.base.resource.cci.ListRecord}
     *            <li>{@link org.openmdx.base.resource.cci.SetRecord}
     *            </ul>
     * @param getter
     *            The getter for the tiny indexed record's optional value.
     * @param setter
     *            The setter for the tiny indexed record's optional value.
     * 
     * @return a new record of the given type
     * 
     * @throws ResourceException
     *             Failed to create a MappedRecord.
     */
    <T extends IndexedRecord> T indexedRecordFacade(
        Class<T> typedInterface,
        Supplier<Object> getter,
        Consumer<Object> setter
    ) throws ResourceException;

}
