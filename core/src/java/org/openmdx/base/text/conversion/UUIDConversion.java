/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UUIDConversion.java,v 1.4 2008/01/08 16:16:31 hburger Exp $
 * Description: UUID conversion
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/08 16:16:31 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.base.text.conversion;

import org.openxri.XRI;


import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URI;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;






import java.util.UUID;




/**
 * UUID Conversion
 * <p>
 * This class supports the following <code>UUID</code> String representations<ul>
 * <li><b>UID</b>, e.g. <i>XubSsL0VEdufdQFDCgEBxg</i>
 * <li><b>UUID</b>, e.g. <i>5ee6d2b0-bd15-11db-9f75-01430a0101c6</i>
 * <li><b>URN</b>, e.g. <i>urn:uuid:5ee6d2b0-bd15-11db-9f75-01430a0101c6</i>
 * </ul>
 */
public class UUIDConversion {

    protected UUIDConversion() {
        // Avoid instantiation
    }

    /**
     * Parse a string to a UUID<ul>
     * <li><code>null</code> is returned if <code>uuid</code> is <code>null</code>
     * <li>if the length of <code>uuid</code> is <code>22</code> it is considered to be in <b>UID</b> format
     * <li>if the length of <code>uuid</code> is <code>36</code> it is considered to be in <b>UUID</b> format
     * <li>if the length of <code>uuid</code> is <code>45</code> it is considered to be in <b>URN</b> format
     * </ul>
     *
     * @param uuid the String representatation of a UUID
     *
     * @return the UUID corresponding to the given String representation
     */
    public static UUID fromString(
        String uuid
    ){
        if(uuid == null) {
                return null;
        } else switch(uuid.length()){
            case UUID_FORMAT: return UUID.fromString(uuid);
            case URN_FORMAT: return fromString(uuid.substring(9));
            case UID_FORMAT: return fromBinary(
                Base64.decode(
                        uuid.replace(
                                SOLIDUS_REPLACEMENT,
                                '/'
                        ).replace(
                                PLUS_REPLACEMENT,
                                '+'
                        ) + "=="
                )
            );
            default: throw new IllegalArgumentException(
                "Invalid UUID string: " + uuid
            );
        }
    }

    /**
     * Returns an UUID URN according to
     * http://www.ietf.org/rfc/rfc4122.txt
     * <p> 
     * Example: urn:uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6
     * 
     * @param uuid
     * 
     * @return the corresponding UUID URN
     */
    public static String toURN (
        UUID uuid
    ){
        return uuid == null ?
            null :
            "urn:uuid:" + uuid;
    }

    /**
     * Returns a UUID URI according to
     * http://www.ietf.org/rfc/rfc4122.txt
     * <p> 
     * Example: <i>urn:uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6</i>
     * 
     * @param uuid
     * 
     * @return the corresponding UUID URN
     */
    public static URI toURI (
        UUID uuid
    ){
        try {
            return uuid == null ? null : new URI(toURN(uuid));
        } catch (URISyntaxException exception) {
            throw new RuntimeException(
                "UUID to URI conversion failed",
                exception
            );
        }
    }

    /**
     * Returns a UUID XRI according to OASIS' specification:<ul>
     * <li>Extensible Resource Identifier (XRI)<br>
     * Type Metadata V2.0<br>
     * Universal Unique IDentifier (uuid) Sub-Tag
     * </ul>
     * <p> 
     * Example:<ul>
     * <li><i>xri://$t*uuid*f81d4fae-7dec-11d0-a765-00a0c91e6bf6</i>
     * </ul>
     * 
     * @param uuid the uuid to be converted
     * 
     * @return the corresponding UUID XRI
     */
    public static XRI toXRI (
        UUID uuid
    ){
        return new XRI("xri://$t*uuid*" + uuid);
    }

    /**
     * Returns a base 64 based UID, where<ul>
     * <li>all '/' have been replaced by '-'
     * <li>all '+' have been repalced by '.'
     * <li>the two trailing '=' have been removed
     * </ul>
     * @param uuid the UUID to be converted
     * 
     * @return the corresponding UID
     */
    public static String toUID (
       UUID uuid
    ){
        return uuid == null?
            null :
            Base64.encode(
                toBinary(uuid)
            ).substring(
                0,
                22
            ).replace(
                '/',
                SOLIDUS_REPLACEMENT
            ).replace(
                '+',
                PLUS_REPLACEMENT
            );
    }


    /**
     * Returns a UUID Oid according to ISO/IEC 9834-8 | ITU-T Rec. X.667
     * http://www.itu.int/ITU-T/studygroups/com17/oid/X.667-E.pdf
     * <p> 
     * Example: <i>{joint-iso-itu-t(2) uuid(25) 
     * technical-university-of-crete(3325839809379844461264382260940242222)}</i>,
     * an Oid based on the UUID 02808890-0ad8-1085-9bdf-0002a5d5fd2e. 
     * 
     * @param uuid
     * 
     * @return the corresponding UUID Oid
     */
    public static Oid toOid (
        UUID uuid
    ){
        try {
            return uuid == null ? null : new Oid(
                "2.25." + new BigInteger(1, toBinary(uuid))
            );
        } catch (GSSException exception) {
            throw new RuntimeException(
                "UUID to oid conversion failed",
                exception
            );
        }
    }


    /**
     * Returns the binary representation of a UUID
     * 
     * @param uuid
     * 
     * @return the UUID's binary representation
     */
    public static byte[] toBinary (
       UUID uuid
    ){
        if(uuid == null) {
                return null;
        } else {
                byte[] binary = new byte[16];
                long mostSignificantBits = uuid.getMostSignificantBits();
                long leastSignificantBits = uuid.getLeastSignificantBits();
                for(
                     int i = 15;
                     i >= 8;
                     i--
                ) {
                        binary[i] = (byte) (leastSignificantBits & 0xff);
                        leastSignificantBits >>= 8;
                }
                for(
                    int i = 7;
                    i >= 0;
                    i--
                ) {
                    binary[i] = (byte) (mostSignificantBits & 0xff);
                    mostSignificantBits >>= 8;
                }
                return binary;
            }
    }

    /**
     * Re-create a UUID from its binary representation
     *
     * @param uuid a UUID in its binary representation
     *
     * @return a UUID in its native representation
     */
    public static UUID fromBinary(
        byte[] uuid
    ){
        if(uuid == null) {
                return null;
        } else {
                long mostSignificantBits = 0L;
                long leastSignificantBits = 0L;
                for(
                    int i = 0;
                    i < 8;
                    i++
                ) {
                    mostSignificantBits <<= 8;
                    mostSignificantBits |= (uuid[i] & 0xff);
                }
                for(
                    int i = 8;
                    i < 16;
                    i++
                ) {
                    leastSignificantBits <<= 8;
                    leastSignificantBits |= (uuid[i] & 0xff);
                }
                return new UUID(mostSignificantBits, leastSignificantBits);
            }
    }

    /**
     * Treat the given <code>uuid</code> object as <code>UUID</code>
     */
    public static UUID asUUID(
        Object uuid
    ){
        return (UUID)uuid;
    }

    /**
     * The UUID format's length
     */
    private static final int UUID_FORMAT = 36;

    /**
     * The UID format's length
     */
    private static final int UID_FORMAT = 22;

    /**
     * The URN format's length
     */
    private static final int URN_FORMAT = 45;

    /**
     * To replace '/' in UIDs
     */
    private static final char SOLIDUS_REPLACEMENT = '-';

    /**
     * To replace '+' in UIDs
     */
    private static final char PLUS_REPLACEMENT = '.';

}
