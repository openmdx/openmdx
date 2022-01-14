/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: REST Record Factory
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
package org.openmdx.base.rest.spi;

import java.util.Optional;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;

/**
 * REST Record Factory
 */
public class RestRecordFactory {

    private RestRecordFactory(){
        // Avoid instantiation
    }
    
    /**
     * Create a record with the given name
     * 
     *  @param recordName the name of the record to be created, where one of
     *  the following values will lead to an {@code Optional.isPresent()} result:<ul>
     *  <li>org:openmdx:kernel:Condition
     *  <li>org:openmdx:kernel:Message
     *  <li>org:openmdx:kernel:FeatureOrder
     *  <li>org:openmdx:kernel:Object
     *  <li>org:openmdx:kernel:QueryFilter
     *  <li>org:openmdx:kernel:QueryExtension
     *  <li>org:openmdx:kernel:Query
     *  <li>org:openmdx:base:Void
     *  <ul>
     * 
     * @return a record with the given name; or {@code Optional.empty()} if no specific 
     *         implementation is provided by this factory
     */
    public static Optional<MappedRecord> createMappedRecord(String recordName) {
        return Optional.ofNullable(
            org.openmdx.base.rest.cci.ConditionRecord.NAME.equals(recordName) ? new ConditionRecord() : 
            org.openmdx.base.rest.cci.MessageRecord.NAME.equals(recordName) ? new MessageRecord() : 
            org.openmdx.base.rest.cci.FeatureOrderRecord.NAME.equals(recordName) ? new FeatureOrderRecord() : 
            org.openmdx.base.rest.cci.ObjectRecord.NAME.equals(recordName) ? new ObjectRecord() : 
            org.openmdx.base.rest.cci.QueryFilterRecord.NAME.equals(recordName) ? new QueryFilterRecord() : 
            org.openmdx.base.rest.cci.QueryExtensionRecord.NAME.equals(recordName) ? new QueryExtensionRecord() : 
            org.openmdx.base.rest.cci.QueryRecord.NAME.equals(recordName) ? new QueryRecord() : 
            org.openmdx.base.rest.cci.VoidRecord.NAME.equals(recordName) ? org.openmdx.base.rest.spi.VoidRecord.getInstance() : 
            null
        );
    }

    /** 
     * Creates an <code>MappedRecord</code> of the given type.
     *  
     *  @param typedInterface the interface the record shall implement, where one of
     *  the following values will lead to an {@code Optional.isPresent()} result:<ul>
     *  <li>{@link org.openmdx.base.rest.cci.ConditionRecord}
     *  <li>{@link org.openmdx.base.rest.cci.MessageRecord}
     *  <li>{@link org.openmdx.base.rest.cci.FeatureOrderRecord}
     *  <li>{@link org.openmdx.base.rest.cci.ObjectRecord}
     *  <li>{@link org.openmdx.base.rest.cci.QueryFilterRecord}
     *  <li>{@link org.openmdx.base.rest.spi.QueryFilterRecord}
     *  <li>{@link org.openmdx.base.rest.cci.QueryExtensionRecord}
     *  <li>{@link org.openmdx.base.rest.cci.QueryRecord}
     *  <li>{@link org.openmdx.base.rest.cci.QueryRecord}
     *  </ul>
     *  
     *  @return a new record of the given type
     */
    public static <T extends MappedRecord> Optional<T> createMappedRecord(
        Class<T> typedInterface
    ){
        return Optional.ofNullable(
            typedInterface.cast(
                org.openmdx.base.rest.cci.ConditionRecord.class == typedInterface ? new ConditionRecord() : 
                org.openmdx.base.rest.cci.MessageRecord.class == typedInterface ? new MessageRecord() : 
                org.openmdx.base.rest.cci.FeatureOrderRecord.class == typedInterface ? new FeatureOrderRecord() : 
                org.openmdx.base.rest.cci.ObjectRecord.class == typedInterface ? new ObjectRecord() : 
                org.openmdx.base.rest.cci.QueryFilterRecord.class == typedInterface ? new QueryFilterRecord() : 
                org.openmdx.base.rest.cci.QueryExtensionRecord.class == typedInterface ? new QueryExtensionRecord() : 
                org.openmdx.base.rest.cci.QueryRecord.class == typedInterface ? new QueryRecord() : 
                org.openmdx.base.rest.cci.VoidRecord.class == typedInterface ? org.openmdx.base.rest.spi.VoidRecord.getInstance() : 
                null
            )
        );
    }

    /**
     * Create a record with the given name
     * 
     *  @param recordName the name of the record to be created, where one of
     *  the following values will lead to an {@code Optional.isPresent()} result:<ul>
     *  <li>org:openmdx:kernel:Result
     *  <ul>
     * 
     * @return a record with the given name; or {@code Optional.empty()} if no specific 
     *         implementation is provided by this factory
     */
    public static Optional<IndexedRecord> createIndexedRecord(String recordName) {
        return Optional.ofNullable(
            org.openmdx.base.rest.cci.ResultRecord.NAME.equals(recordName) ? new ResultRecord() : 
            null
        );
    }
    
    /** 
     * Creates an <code>IndexdRecord</code> of the given type.
     *  
     *  @param typedInterface the interface the record shall implement, one of<ul>
     *  <li>{@link org.openmdx.base.rest.cci.ResultRecord}
     *  </ul>
     *  
     *  @return a new record of the given type
     */
    public static <T extends IndexedRecord> Optional<T> createIndexedRecord(
        Class<T> typedInterface
    ) throws ResourceException {
        return Optional.ofNullable(
            typedInterface.cast(
                org.openmdx.base.rest.cci.ResultRecord.class == typedInterface ? new ResultRecord() : 
                null
            )
        );
    }

}
