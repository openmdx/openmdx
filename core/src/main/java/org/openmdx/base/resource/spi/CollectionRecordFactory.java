/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Collection Record Factory
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.resource.spi;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.resource.NotSupportedException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;

/**
 * Collection Record Factory
 */
class CollectionRecordFactory {

    private CollectionRecordFactory(){
        // Avoid instantiation
    }
    
    /**
     * Create a record with the given name
     * 
     *  @param recordName the name of the record to be created, where one of
     *  the following values will lead to an {@code Optional.isPresent()} result:<ul>
     *  <li>sparsearray
     *  </ul>
     * 
     * @return a record with the given name; or {@code Optional.empty()} if no specific 
     *         implementation is provided by this factory
     */
    static Optional<MappedRecord> createMappedRecord(String recordName) {
        return Optional.ofNullable(
            org.openmdx.base.resource.cci.SparseArrayRecord.NAME.equals(recordName) ? new SparseArrayRecord() :
            null
        );
    }

    /** 
     * Creates an <code>MappedRecord</code> of the given type.
     *  
     *  @param typedInterface the interface the record shall implement, where one of
     *  the following values will lead to an {@code Optional.isPresent()} result:<ul>
     *  <li>{@link org.openmdx.base.resource.cci.SparseArrayRecord}
     *  </ul>
     *  
     *  @return a new record of the given type; or {@code Optional.empty()} if no specific 
     *         implementation is provided by this factory
     */
    static <T extends MappedRecord> Optional<T> createMappedRecord(
        Class<T> typedInterface
    ){
        return Optional.ofNullable(
            typedInterface.cast(
                org.openmdx.base.resource.cci.SparseArrayRecord.class == typedInterface ? new SparseArrayRecord() : 
                null
            )
        );
    }
    
    /**
     * Create a record with the given name
     * 
     *  @param recordName the name of the record to be created, where one of
     *  the following values will lead to an {@code Optional.isPresent()} result:<ul>
     *  <li>list
     *  <li>set
     *  </ul>
     * 
     * @return a record with the given name; or {@code Optional.empty()} if no specific 
     *         implementation is provided by this factory
     */
    static Optional<IndexedRecord> createIndexedRecord(String recordName) {
        return Optional.ofNullable(
            org.openmdx.base.resource.cci.ListRecord.NAME.equals(recordName) ? new ListRecord() :
            org.openmdx.base.resource.cci.SetRecord.NAME.equals(recordName) ? new SetRecord() :
            null
        );
    }

    /** 
     * Creates an <code>IndexdRecord</code> of the given type.
     *  
     *  @param typedInterface the interface the record shall implement, where one of
     *  the following values will lead to an {@code Optional.isPresent()} result:<ul>
     *  <li>{@link org.openmdx.base.resource.cci.ListRecord
     *  <li>{@link org.openmdx.base.resource.cci.SetRecord}
     *  </ul>
     *  
     *  @return a new record of the given type; or {@code Optional.empty()} if no specific 
     *         implementation is provided by this factory
     */
    static <T extends IndexedRecord> Optional<T> createIndexedRecord(
        Class<T> typedInterface
    ){
        return Optional.ofNullable(
            typedInterface.cast(
                org.openmdx.base.resource.cci.ListRecord.class == typedInterface ? new ListRecord() : 
                org.openmdx.base.resource.cci.SetRecord.class == typedInterface ? new SetRecord() : 
                null
            )
        );
    }

    /** 
     * Creates an <code>IndexdRecord</code> facade of the given type.
     *  
     *  @param typedInterface the interface the record shall implement, where one of
     *  the following values will lead to an {@code Optional.isPresent()} result:<ul>
     *  <li>{@link org.openmdx.base.resource.cci.ListRecord
     *  <li>{@link org.openmdx.base.resource.cci.SetRecord}
     *  </ul>
     *  
     *  @return a new record of the given type; or {@code Optional.empty()} if no specific 
     *         implementation is provided by this factory
     */
    static <T extends IndexedRecord> Optional<T> createIndexedRecordFacade(
        Class<T> typedInterface,
        Supplier<Object> getter,
        Consumer<Object> setter
    ){
        return Optional.ofNullable(
            typedInterface.cast(
                org.openmdx.base.resource.cci.SetRecord.class == typedInterface  ? new SetRecordFacade(getter, setter) :
                org.openmdx.base.resource.cci.ListRecord.class == typedInterface  ? new ListRecordFacade(getter, setter) :
                null
            )
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
     *            if <code>values</code> is null
     * @exception IllegalArgumentException
     *            if <code>values</code> is neither an array not a list.
     * 
     */
    static Optional<IndexedRecord> asIndexedRecord(
        String recordName,
        String recordShortDescription,
        Object values
    ) {
        return Optional.ofNullable(
            values instanceof List<?> ? new DelegatingIndexedRecord(
                recordName,
                recordShortDescription,
                (List<?>) values
            ) : values instanceof Object[] ? new FixedSizeIndexedRecord(
                recordName,
                recordShortDescription,
                (Object[])values
            ) : values.getClass().isArray() ? new PrimitiveArrayRecord(
                recordName,
                recordShortDescription,
                values
            ) : null
        );
    }

    @Deprecated
    static IndexedRecord singletonIndexedRecord(
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
