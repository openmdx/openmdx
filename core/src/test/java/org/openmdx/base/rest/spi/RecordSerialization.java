/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Object Record Test
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

#if JAVA_8 
	import javax.resource.ResourceException;
	import javax.resource.spi.EISSystemException;
	import javax.resource.cci.Record;
#else 
	import jakarta.resource.ResourceException; 
	import jakarta.resource.spi.EISSystemException;
	import jakarta.resource.cci.Record;
#endif;

/**
 * Manual serialization/deserialization of JCA {@code Record}s in order to
 * combine serialization and EJB local interfaces.
 */
class RecordSerialization {

	/**
	 * JCA record serialization
	 * 
	 * @param record a JCA record
	 * @return the serialized JCA {@code Record}
	 * @throws {@code EISSystemException} in case of failure
	 */
	public byte[] serialize(Record record) throws ResourceException {
		try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
			try (ObjectOutput converter = new ObjectOutputStream(buffer)) {
				converter.writeObject(record);
			}
			return buffer.toByteArray();
		} catch (IOException exception) {
			throw new EISSystemException("Record serialization failure", exception);
		}
	}

	/**
	 * JCA record deserialization
	 * 
	 * @param serializedRecord
	 * @return the deserialized {@code Record}
	 * @throws {@code EISSystemException} in case of failure
	 */
	public Record deserialize(byte[] serializedRecord) throws ResourceException {
		try (ByteArrayInputStream buffer = new ByteArrayInputStream(serializedRecord);
				ObjectInput converter = new ObjectInputStream(buffer);) {
			return (Record) converter.readObject();
		} catch (IOException | ClassCastException | ClassNotFoundException exception) {
			throw new EISSystemException("Record deserialization failure", exception);
		}
	}

}
