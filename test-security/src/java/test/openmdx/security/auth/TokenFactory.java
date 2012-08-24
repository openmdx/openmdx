/*
 * ====================================================================
 * Project:     OMEX/Security, http://www.omex.ch/
 * Description: Token Factory
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package test.openmdx.security.auth;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignedObject;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.openmdx.kernel.exception.BasicException;

/**
 * Token Factory
 */
public class TokenFactory {

    /**
     * Constructor
     * 
     * @param privateKey
     * @param algorithm
     */
    public TokenFactory(
        String algorithm,
        PrivateKey privateKey
    ){
        this.privateKey = privateKey;
        this.algorithm = algorithm;
    }

    /**
     * The tokens are signed with this private key
     */
    private final PrivateKey privateKey;

    /**
     * The algoritme to be used to sign the token
     */
    private final String algorithm;

    /**
     * Create a token for 
     * 
     * @param timeout the timeout in milliseconds; or -1 if the token should 
     * never expire
     * @param value the token's value
     * @return the signed token
     * @throws TokenException 
     */
    public byte[] create (
        long timeout,
        Serializable value
    ) throws TokenException {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ZipOutputStream compressedStream = new ZipOutputStream(byteStream);
            compressedStream.putNextEntry(new ZipEntry(Token.VERSION_TAG));
            ObjectOutputStream objectStream = new ObjectOutputStream(compressedStream);		
            objectStream.writeObject(
                new SignedObject(
                    new Token(timeout, value), 
                    this.privateKey, 
                    Signature.getInstance(this.algorithm)
                )
            );
            objectStream.close();
            return byteStream.toByteArray();
        } 
        catch (Exception exception) {
            throw new TokenException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.GENERIC,
                null, // description
                new BasicException.Parameter(
                    "class", 
                    this.privateKey == null ? null : this.privateKey.getClass().getName()
                ),
                new BasicException.Parameter(
                    "algorithm", 
                    this.privateKey == null ? null : this.privateKey.getAlgorithm()
                ),
                new BasicException.Parameter(
                    "format", 
                    this.privateKey == null ? null : this.privateKey.getFormat()
                )
            );
        }
    }

}
