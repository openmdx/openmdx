/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: ObjectRecords 
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

package org.openmdx.base.accessor.rest.spi;

import java.util.Arrays;

import #if JAVA_8 javax.resource.ResourceException #else jakarta.resource.ResourceException #endif;

import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.ExtendedRecordFactory;
import org.openmdx.base.rest.cci.ObjectRecord;

/**
 * ObjectRecords
 *
 */
public class ObjectRecords {

    /**
     * Constructor 
     */
    private ObjectRecords() {
        // Avoid instantiation
    }
    
    /**
     * Virtual objects may be identified by their version
     */
    private final static byte[] VIRTUAL_OBJECT_VERSION = {
        'v', 'i', 'r', 't', 'u', 'a', 'l'
    };
    
    public static ObjectRecord createObjectRecord(
        Path xri,
        String objectClass
    ) throws ResourceException {
        final ExtendedRecordFactory recordFactory = Records.getRecordFactory();
        final ObjectRecord objectRecord = recordFactory.createMappedRecord(ObjectRecord.class);
        objectRecord.setResourceIdentifier(xri);
        objectRecord.setValue(recordFactory.createMappedRecord(objectClass));
        return objectRecord;
    }

    public static ObjectRecord createVirtualObjectRecord(
        Path xri,
        String objectClass
    ) throws ResourceException {
        final ObjectRecord objectRecord = createObjectRecord(xri, objectClass);
        objectRecord.setVersion(VIRTUAL_OBJECT_VERSION);
        return objectRecord;
    }

    /**
     * Tells whether the candidate corresponds to the virtual objects' magic version
     * 
     * @param version the version to be tested
     * 
     * @return {@code true} if the version corresponds to the virtual objects' magic version
     */
    public static boolean isVirtualObjectVersion(byte[] version) {
        return Arrays.equals(VIRTUAL_OBJECT_VERSION, version);
    }
    
}
