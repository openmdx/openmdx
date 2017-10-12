/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: KernelRecordFactory 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2016, OMEX AG, Switzerland
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
package org.openmdx.base.rest.spi;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;

/**
 * KernelRecordFactory
 */
public class KernelRecordFactory {

    private KernelRecordFactory(){
        // Avoid instantiation
    }
    
    private static String PREFIX = "org:openmdx:kernel:";

    public static boolean supports(String recordName) {
        return recordName.startsWith(PREFIX);
    }
    
    /**
     * Create a record with the given name
     * 
     * @param recordName
     * 
     * @return a record with the given name; or <code>null</code> if no specific 
     *         implementation is provided by this factory;
     */
    public static MappedRecord createMappedRecord(String recordName) {
        return 
            org.openmdx.base.rest.cci.ConditionRecord.NAME.equals(recordName) ? new ConditionRecord() : 
            org.openmdx.base.rest.cci.MessageRecord.NAME.equals(recordName) ? new MessageRecord() : 
            org.openmdx.base.rest.cci.FeatureOrderRecord.NAME.equals(recordName) ? new FeatureOrderRecord() : 
            org.openmdx.base.rest.cci.ObjectRecord.NAME.equals(recordName) ? new ObjectRecord() : 
            org.openmdx.base.rest.cci.QueryFilterRecord.NAME.equals(recordName) ? new QueryFilterRecord() : 
            org.openmdx.base.rest.cci.QueryExtensionRecord.NAME.equals(recordName) ? new QueryExtensionRecord() : 
            org.openmdx.base.rest.cci.QueryRecord.NAME.equals(recordName) ? new QueryRecord() : 
            null;
    }

    /** 
     * Creates an <code>MappedRecord</code> of the given type.
     *  
     *  @param typedInterface the interface the record shall implement, one of<ul>
     *  <li>{@link org.openmdx.base.rest.cci.ConditionRecord}
     *  <li>{@link org.openmdx.base.rest.cci.MessageRecord}
     *  <li>{@link org.openmdx.base.rest.cci.FeatureOrderRecord}
     *  <li>{@link org.openmdx.base.rest.cci.ObjectRecord}
     *  <li>{@link org.openmdx.base.rest.cci.QueryFilterRecord}
     *  <li>{@link org.openmdx.base.rest.spi.QueryFilterRecord}
     *  <li>{@link org.openmdx.base.rest.cci.QueryExtensionRecord}
     *  <li>{@link org.openmdx.base.rest.cci.QueryRecord}
     *  </ul>
     *  
     *  @return a new record of the given type
     */
    public static <T extends MappedRecord> MappedRecord createMappedRecord(
        Class<T> typedInterface
    ){
        return 
            org.openmdx.base.rest.cci.ConditionRecord.class == typedInterface ? new ConditionRecord() : 
            org.openmdx.base.rest.cci.MessageRecord.class == typedInterface ? new MessageRecord() : 
            org.openmdx.base.rest.cci.FeatureOrderRecord.class == typedInterface ? new FeatureOrderRecord() : 
            org.openmdx.base.rest.cci.ObjectRecord.class == typedInterface ? new ObjectRecord() : 
            org.openmdx.base.rest.cci.QueryFilterRecord.class == typedInterface ? new QueryFilterRecord() : 
            org.openmdx.base.rest.cci.QueryExtensionRecord.class == typedInterface ? new QueryExtensionRecord() : 
            org.openmdx.base.rest.cci.QueryRecord.class == typedInterface ? new QueryRecord() : 
            null;
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
    public static <T extends IndexedRecord> IndexedRecord createIndexedRecord(
        Class<T> typedInterface
    ) throws ResourceException {
        return
            org.openmdx.base.rest.cci.ResultRecord.class == typedInterface ? new ResultRecord() : 
            null;
    }

}
