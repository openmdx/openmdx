/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractIterator.java,v 1.1 2009/05/26 14:31:22 wfro Exp $
 * Description: JDBC Iterator for find requests
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/26 14:31:22 $
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
package org.openmdx.application.dataprovider.layer.persistence.common;

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
 * AbstractIterator
 *
 * Supports serialization and deserialization
 */
public class AbstractIterator
implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3834309523048248884L;

    public static byte[] serialize(
        Serializable object
    ) throws ServiceException {
        try {
            CRC32 crc = new CRC32();
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            CheckedOutputStream checkStream = new CheckedOutputStream(
                byteStream, 
                crc
            );
            ObjectOutputStream objectStream = new ObjectOutputStream(
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

    @SuppressWarnings("unchecked")
    public static Serializable deserialize(
        byte[] object
    ) throws ServiceException {
        if(object.length == 0) return new HashMap();
        CRC32  crc = new CRC32();
        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(
                object
            );
            CheckedInputStream checkStream = new CheckedInputStream(
                byteStream,
                crc
            );
            ObjectInputStream objectStream = new ObjectInputStream(
                checkStream
            );
            // read the objects
            Object target = objectStream.readObject();
            // remember the checksum
            long readChecksum = crc.getValue();
            long writeChecksum = objectStream.readLong();
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

}
