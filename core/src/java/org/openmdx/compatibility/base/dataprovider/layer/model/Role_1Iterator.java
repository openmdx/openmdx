/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Role_1Iterator.java,v 1.10 2008/09/10 08:55:22 hburger Exp $
 * Description: JDBC Iterator for find requests
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:22 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.layer.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;



/**
 * RoleIterator
 *
 * Stores the status for a role iteration continuation. Stored is just the name
 * of the role requested. Keeps existing iterators (eg. from persistence layer)
 */
@SuppressWarnings("unchecked")
class Role_1Iterator
implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3977575913783310390L;
//  ---------------------------------------------------------------------------
    static byte[] serialize(
        Serializable object
    ) throws ServiceException {
        try {
            final CRC32 crc = new CRC32();
            final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            final CheckedOutputStream checkStream = new CheckedOutputStream(
                byteStream, 
                crc
            );
            final ObjectOutputStream objectStream = new ObjectOutputStream(
                checkStream
            );
            // write the objects
            objectStream.writeObject(object);
            // write the checksum
            objectStream.writeLong(crc.getValue());
            // release ressources
            objectStream.close();
            // save the extende session key
            return byteStream.toByteArray();
        } catch (Exception exception) {
            throw new ServiceException(exception);
        }
    }

    //---------------------------------------------------------------------------
    static Serializable deserialize(
        byte[] object
    ) throws ServiceException {
        if(object.length == 0) return new HashMap();
        final CRC32  crc = new CRC32();
        try {
            final ByteArrayInputStream byteStream = new ByteArrayInputStream(
                object
            );
            final CheckedInputStream checkStream = new CheckedInputStream(
                byteStream,
                crc
            );
            final ObjectInputStream objectStream = new ObjectInputStream(
                checkStream
            );
            // read the objects
            final Object target = objectStream.readObject();
            // remember the checksum
            final long readChecksum = crc.getValue();
            final long writeChecksum = objectStream.readLong();
            // release ressources
            objectStream.close();
            // compare the two checksums
            if(readChecksum != writeChecksum) {
                throw new ServiceException( 
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.TRANSFORMATION_FAILURE,
                    "Checksum test failed"
                );
            }    
            // return the extracted value
            return (Serializable)target;
        } catch (Exception exception) {
            throw new ServiceException(exception);
        }
    }

    //---------------------------------------------------------------------------
    public Role_1Iterator(
        String role,
        byte[] iterator
    ) { 
        this.iterator = iterator;
        this.role = role;
    }

    //---------------------------------------------------------------------------
    public byte[] getIterator() {
        return this.iterator;
    }

    //---------------------------------------------------------------------------
    public String getRole() {
        return this.role;
    }

    //---------------------------------------------------------------------------

    private byte[] iterator = null;
    private String role = null;

}

//--- End of File -----------------------------------------------------------
