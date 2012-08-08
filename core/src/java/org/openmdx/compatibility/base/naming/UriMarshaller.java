/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UriMarshaller.java,v 1.16 2007/10/10 16:06:03 hburger Exp $
 * Description: Marshaller 
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:03 $
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
package org.openmdx.compatibility.base.naming;

import java.util.ArrayList;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.UnicodeTransformation;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.StringBuilders;
import org.openmdx.kernel.uri.scheme.OpenMDXSchemes;


/**
 * Converts Path to an international resource identifier
 */
public final class UriMarshaller
    implements Marshaller
{

    private UriMarshaller(
    ){
        // Avoid external instantiation
    }
    
    /**
     * Memorize the singleton
     */ 
    final static private Marshaller instance = new UriMarshaller();
    
    /**
     * Return the singleton
     */ 
    static public Marshaller getInstance(
    ){
        return UriMarshaller.instance;
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
        CharSequence target = StringBuilders.newStringBuilder(URI_PREFIX);
        for(
          int i=0;
          i < source.length;
        )encode(
            source[i],
            StringBuilders.asStringBuilder(target).append(COMPONENT_DELIMITER),
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
            if(source.length() > URI_PREFIX.length()){
                source = source.substring(URI_PREFIX.length());
                if (
                    source.charAt(0) != '/'
                ) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("uri",source)
                    },
                    "Relative URIs are not supported"
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
                new BasicException.Parameter[] {
                    new BasicException.Parameter("charSequence", charSequence)
                },
                "CharSequence can't be transformed into an URI"
            );
        }
    }

    /**
     * 
     */
    final static private void encode(
        Object charSequence,
        CharSequence target,
        boolean terminal
    ) throws ServiceException {
        String string = charSequence.toString();
        if(terminal && string.endsWith("%")) {
            encode(
                string.substring(0, string.length() - 1),
                target,
                false
            );
            StringBuilders.asStringBuilder(target).append("%");
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
                    StringBuilders.asStringBuilder(target).append((char) octet);
                } else { // Characters to be escaped
                    StringBuilders.asStringBuilder(target).append(ESCAPE);
                    if (octet < 0) {
                        StringBuilders.asStringBuilder(target).append(
                            Integer.toHexString(octet).substring(6)
                        );
                    } else {
                        StringBuilders.asStringBuilder(target).append(
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

    final static String URI_PREFIX = OpenMDXSchemes.URI_SCHEME + ":/";
    
}