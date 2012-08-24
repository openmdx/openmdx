/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Legacy URI Marshaller 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
package org.openmdx.base.naming;

import java.util.ArrayList;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.text.conversion.UnicodeTransformation;
import org.openmdx.kernel.exception.BasicException;


/**
 * Handles SPICE URIs
 */
@SuppressWarnings({"rawtypes","unchecked"})
public final class URI_1 implements Marshaller {

    private URI_1(
    ){
        // Avoid external instantiation
    }

    /**
     * Memorize the singleton
     */ 
    final static private Marshaller instance = new URI_1();

    /**
     * Legacy' URI Scheme Prefix
     */
    public final static String OPENMDX_PREFIX = "spice:/";

    /**
     * Return the singleton
     */ 
    static public Marshaller getInstance(
    ){
        return URI_1.instance;
    }


    //------------------------------------------------------------------------
    // Implements Marshaller
    //------------------------------------------------------------------------

    /**
     * Marshal a CharSequence[] into a CharSequence
     * 
     * @param     charSequences
     *            The array of CharSequences to be marshalled.
     * 
     * @return      A CharSequence containing the marshalled objects.
     */
    public Object marshal (
        Object charSequences
    ) throws ServiceException {
        if (charSequences == null) return null;
        Object[]source = (Object[])charSequences;
        StringBuilder target = new StringBuilder(URI_1.OPENMDX_PREFIX);
        for(
                int i=0;
                i < source.length;
        )encode(
            source[i],
            target.append(COMPONENT_DELIMITER),
            ++i == source.length
        );
        return target;
    }


    /**
     * Unmarshal a CharSequence into a CharSequence[].
     * 
     * @param         marshalledObjects
     *            A string containing a marshalled sequence of objects
     * 
     * @return    A String array containing the unmarshaled sequence
     *                  of objects.
     * @exception ServiceException ILLEGAL_ARGUMENT
     */
    public Object unmarshal (
        Object charSequence
    ) throws ServiceException {
        if (charSequence == null) return null;
        ArrayList target = new ArrayList();
        try {
            String source = charSequence.toString();
            if(source.length() > URI_1.OPENMDX_PREFIX.length()){
                source = source.substring(URI_1.OPENMDX_PREFIX.length());
                if (
                        source.charAt(0) != '/'
                ) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "Relative URIs are not supported",
                    new BasicException.Parameter("uri",source)
                );
                int fromIndex = 0;
                int toIndex = -1;
                do {
                    toIndex = source.indexOf(
                        COMPONENT_DELIMITER,
                        fromIndex + 1
                    );
                    target.add(
                        decode(
                            source.substring(
                                fromIndex + 1, 
                                toIndex >= 0 ? toIndex : source.length()
                            )
                        )
                    );
                    fromIndex = toIndex;
                } while (toIndex > 0);
            }
            return target.toArray(new String[target.size()]);
        } catch (RuntimeException exception){
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "CharSequence can't be transformed into an URI",
                new BasicException.Parameter("charSequence", charSequence)
            );
        }
    }

    /**
     * 
     */
    final static private void encode(
        Object charSequence,
        StringBuilder target,
        boolean terminal
    ) throws ServiceException {
        String string = charSequence.toString();
        if(terminal && string.endsWith("%")) {
            encode(
                string.substring(0, string.length() - 1),
                target,
                false
            );
            target.append("%");
        } else try {
            byte[]source=UnicodeTransformation.toByteArray(string);
            for (
                    int index = 0;
                    index < source.length;
                    index++
            ){
                byte octet = source[index];
                if ( 
                        (octet >= '0' && octet <= '9') || // Numeric character
                        (octet >= '@' && octet <= 'Z') || // pchar and Alphanumeric character
                        (octet >= 'a' && octet <= 'z') || // Alphanumeric characer
                        (octet >= '&' && octet <= '.') || // Mark character and pchar
                        octet == '_' || // Mark character
                        octet == '~' || // Mark character
                        octet == '!' || // Mark character
                        octet == '=' || // Mark character
                        octet == '$' || // Mark character
                        octet == ';' || // Parameter delimiter
                        ( // recognize lonley field delimiters
                                octet == FIELD_DELIMITER && (
                                        index + 1 == source.length ||
                                        source[index+1] != FIELD_DELIMITER 
                                ) && (
                                        index == 0 ||
                                        source[index-1] != FIELD_DELIMITER
                                )
                        )
                ){ 
                    target.append((char) octet);
                } else { // Characters to be escaped
                    target.append(ESCAPE);
                    if (octet < 0) {
                        target.append(
                            Integer.toHexString(octet).substring(6)
                        );
                    } else {
                        target.append(
                            Character.forDigit(octet / RADIX, RADIX)
                        ).append(
                            Character.forDigit(octet % RADIX, RADIX)
                        );
                    }
                }
            }
        } catch (RuntimeException exception) {
            throw new ServiceException(exception);
        }
    }


    /**
     *
     */ 
    final static private String decode(
        String source
    ) throws ServiceException {
        byte[] target = new byte[source.length()];
        int targetIndex = 0;
        for (
                int sourceIndex = 0;
                sourceIndex < target.length;
        ) target[targetIndex++] = source.charAt(sourceIndex) == ESCAPE && sourceIndex + 2 < target.length ?
            (byte)Integer.parseInt( // Parse as int to cope with f0 to ff
                source.substring(++sourceIndex, sourceIndex+=2),
                RADIX
            ) :
                (byte)source.charAt(sourceIndex++);
                try {
                    return UnicodeTransformation.toString(target,0, targetIndex);
                } catch (RuntimeException exception) {
                    throw new ServiceException(exception);
                }
    }


    //------------------------------------------------------------------------
    // Constants
    //------------------------------------------------------------------------

    final static public String ROOT = "/";

    final static public char COMPONENT_DELIMITER = '/';

    final static public char FIELD_DELIMITER = ':';

    final static public char ESCAPE = '%';

    final static int RADIX = 16;

}
