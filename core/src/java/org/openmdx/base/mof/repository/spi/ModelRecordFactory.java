/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: ModelRecordFactory 
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

package org.openmdx.base.mof.repository.spi;

import javax.resource.cci.MappedRecord;

/**
 * ModelRecordFactory
 */
public class ModelRecordFactory {

    private ModelRecordFactory(){
        // Avoid instantiation
    }
    
    private static String PREFIX = "org:omg:model1:";

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
            org.openmdx.base.mof.repository.cci.AliasTypeRecord.NAME.equals(recordName) ? new AliasTypeRecord() :
            org.openmdx.base.mof.repository.cci.AssociationEndRecord.NAME.equals(recordName) ? new AssociationEndRecord() :
            org.openmdx.base.mof.repository.cci.AssociationRecord.NAME.equals(recordName) ? new AssociationRecord() :
            org.openmdx.base.mof.repository.cci.AttributeRecord.NAME.equals(recordName) ? new AttributeRecord() :
            org.openmdx.base.mof.repository.cci.ClassRecord.NAME.equals(recordName) ? new ClassRecord() :
            org.openmdx.base.mof.repository.cci.ExceptionRecord.NAME.equals(recordName) ? new ExceptionRecord() :
            org.openmdx.base.mof.repository.cci.OperationRecord.NAME.equals(recordName) ? new OperationRecord() :
            org.openmdx.base.mof.repository.cci.PackageRecord.NAME.equals(recordName) ? new PackageRecord() :
            org.openmdx.base.mof.repository.cci.ParameterRecord.NAME.equals(recordName) ? new ParameterRecord() :
            org.openmdx.base.mof.repository.cci.PrimitiveTypeRecord.NAME.equals(recordName) ? new PrimitiveTypeRecord() :
            org.openmdx.base.mof.repository.cci.ReferenceRecord.NAME.equals(recordName) ? new ReferenceRecord() :
            org.openmdx.base.mof.repository.cci.StructureFieldRecord.NAME.equals(recordName) ? new StructureFieldRecord() :
            org.openmdx.base.mof.repository.cci.StructureTypeRecord.NAME.equals(recordName) ? new StructureTypeRecord() :
            null;
    }
    
}
