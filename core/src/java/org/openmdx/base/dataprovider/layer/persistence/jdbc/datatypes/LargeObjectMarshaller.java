/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Large Object Marshaller 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2012, OMEX AG, Switzerland
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
package org.openmdx.base.dataprovider.layer.persistence.jdbc.datatypes;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObject;
import org.w3c.cci2.CharacterLargeObjects;

/**
 * Large Object Marshaller
 */
public class LargeObjectMarshaller {

    /**
     * Constructor 
     *
     * @param getLargeObjectByValue Tells whether streams have to be copied into memory
     */
    public LargeObjectMarshaller(
        boolean getLargeObjectByValue
    ) {
        this.getLargeObjectByValue = getLargeObjectByValue;
    }

    /**
     * Tells whether streams have to be copied into memory
     */
    private final boolean getLargeObjectByValue;

    /**
     * Remebers if the feature is not supported
     */
    private boolean supportsLargeObjectLength = true;

    /**
     * Tells whether large objects must be tallied
     * 
     * @param setLargeObjectMethod
     * 
     * @return <code>true</code> if large objects must be tallied
     */
    public boolean isTallyingRequired(
        SetLargeObjectMethod setLargeObjectMethod
    ){
        return SetLargeObjectMethod.TALLYING == setLargeObjectMethod;
    }

    /**
     * Get CLOB column value
     * 
     * @param val
     * @param attributeName
     * @param attributeDef
     * 
     * @return the CLOB column value
     * 
     * @throws ServiceException
     * @throws SQLException
     */
    public Object getCharacterColumnValue(
        Object val,
        String attributeName,
        ModelElement_1_0 attributeDef
    ) throws ServiceException, SQLException {
        boolean isStream = attributeDef != null && ModelHelper.getMultiplicity(attributeDef).isStreamValued();
        if(val == null) {
            return null;
        } else if(val instanceof Clob) {
            Clob clob = (Clob)val;
            if(isStream){
                Long length = getLength(clob);
                try {
                    return getLargeObjectByValue ? CharacterLargeObjects.copyOf(
                        clob.getCharacterStream(),
                        length
                    ) : CharacterLargeObjects.valueOf(
                        clob.getCharacterStream(),
                        length
                    );
                } catch (IOException exception) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.TRANSFORMATION_FAILURE,
                        "Unable to copy CLOB value",
                        new BasicException.Parameter("length", length)
                    );
                }
            } else {
                return clob.getSubString(1L, (int)clob.length());
            }
        } else if(val instanceof String) {
            return isStream ? CharacterLargeObjects.valueOf((String)val) : val;
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Unsupported CLOB representation received from database",
                new BasicException.Parameter("supported", Clob.class.getName(), String.class.getName()),
                new BasicException.Parameter("actual", val.getClass().getName())
            );
        }
    }

    /**
     * Determine a CLOB's length
     * 
     * @param clob
     * 
     * @return the CLOB's length, or <code>null</code> if it is undeterminable
     */
    private Long getLength(
        Clob clob
    ) {
        if(this.supportsLargeObjectLength) {
            try {
                return Long.valueOf(clob.length());

            } catch (SQLFeatureNotSupportedException exception) {
                this.supportsLargeObjectLength = false;
                return null;

            } catch (SQLException exception) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Get BLOB column value
     * 
     * @param val
     * @param attributeName
     * @param attributeDef
     * 
     * @return a BLOB column value
     * 
     * @throws ServiceException
     * @throws SQLException
     */
    public Object getBinaryColumnValue(
        Object val,
        String attributeName,
        ModelElement_1_0 attributeDef
    ) throws ServiceException, SQLException {
        if(val == null) {
            return null;
        } else {
            boolean isStream = attributeDef != null && ModelHelper.getMultiplicity(attributeDef).isStreamValued();
            if(val instanceof Blob ) {
                Blob value = (Blob)val;
                if(isStream){
                    Long length = getLength(value);
                    try {
                        return getLargeObjectByValue ? BinaryLargeObjects.copyOf(
                            value.getBinaryStream(),
                            length
                        ) : BinaryLargeObjects.valueOf(
                            value.getBinaryStream(),
                            length
                        );
                    } catch (IOException exception) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.TRANSFORMATION_FAILURE,
                            "Unable to copy BLOB value",
                            new BasicException.Parameter("length", length)
                        );
                    }
                } else {
                    return value.getBytes(1L, (int)value.length());
                }
            } else if(val instanceof byte[]) {
                byte[] value = (byte[])val;
                return isStream ? BinaryLargeObjects.valueOf(value) : value;
            } else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "Unsupported BLOB representation received from database",
                    new BasicException.Parameter("supported", Blob.class.getName(), "byte[]"),
                    new BasicException.Parameter("actual", val.getClass().getName())
                );
            }
        }
    }

    /**
     * Determine a BLOB's length
     * 
     * @param blob
     * 
     * @return the BLOB's length, or <code>null</code> if it is undeterminable
     */
    private Long getLength(Blob blob) {
        if(this.supportsLargeObjectLength) {
            try {
                return Long.valueOf(blob.length());

            } catch (SQLFeatureNotSupportedException exception) {
                this.supportsLargeObjectLength = false;
                return null;

            } catch (SQLException exception) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Set CLOB column value
     * 
     * @param preparedStatement
     * @param parameterIndex
     * @param largeObject
     * @param setLargeObjectMethod
     * 
     * @throws IOException
     * @throws SQLException
     */
    public void setCharacterColumnValue(
        PreparedStatement preparedStatement,
        int parameterIndex,
        CharacterLargeObject largeObject,
        SetLargeObjectMethod setLargeObjectMethod
    ) throws IOException, SQLException {
        Long length = largeObject.getLength();
        if(setLargeObjectMethod == SetLargeObjectMethod.BY_VALUE){
            String value;
            if(length == null) {
                CharArrayWriter buffer = new CharArrayWriter();
                CharacterLargeObjects.streamCopy(largeObject.getContent(), 0, buffer);
                value = buffer.toString();
            } else {
                char[] buffer = new char[length.intValue()];
                largeObject.getContent().read(buffer);
                value = new String(buffer);
            }
            preparedStatement.setString(
             parameterIndex,
             value
            );
        } else {
         if(length == null) {

             preparedStatement.setCharacterStream(
              parameterIndex,
              largeObject.getContent()
             );
         } else if (length.longValue() > Integer.MAX_VALUE) {
              preparedStatement.setCharacterStream(
               parameterIndex,
               largeObject.getContent(),
               length.longValue()
              );
         } else {
              preparedStatement.setCharacterStream(
               parameterIndex,
               largeObject.getContent(),
               length.intValue()
              );
            }
        }
    }
    
    /**
     * Set a BLOB column value
     * 
     * @param preparedStatement
     * @param parameterIndex
     * @param largeObject
     * @param setLargeObjectMethod
     * 
     * @throws IOException
     * @throws SQLException
     */
    public void setBinaryColumnValue(
        PreparedStatement preparedStatement,
        int parameterIndex,
        BinaryLargeObject largeObject,
        SetLargeObjectMethod setLargeObjectMethod
    ) throws IOException, SQLException {
        Long length = largeObject.getLength();
        if(setLargeObjectMethod == SetLargeObjectMethod.BY_VALUE){
            byte[] value;
            if(length == null) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                BinaryLargeObjects.streamCopy(largeObject.getContent(), 0, buffer);
                value = buffer.toByteArray();
            } else {
                value = new byte[length.intValue()];
                largeObject.getContent().read(value);
            }
            preparedStatement.setBytes(
             parameterIndex,
             value
            );
        } else {
         if(length == null) {
             preparedStatement.setBinaryStream(
              parameterIndex,
              largeObject.getContent()
             );
         } else if (length.longValue() > Integer.MAX_VALUE) {
             preparedStatement.setBinaryStream(
              parameterIndex,
              largeObject.getContent(),
              length.longValue()
             );
         } else {
             preparedStatement.setBinaryStream(
              parameterIndex,
              largeObject.getContent(),
              length.intValue()
             );
            }
        }
    }

}
