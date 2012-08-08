/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UUIDConversion.java,v 1.7 2009/05/27 22:48:21 hburger Exp $
 * Description: UUID conversion
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/27 22:48:21 $
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

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;




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
            case LEGACY_FORMAT: return fromBinary(
                Base64.decode(
                    uuid.replace(
                        '-',
                        '/'
                    ).replace(
                        '.',
                        '+'
                    ) + "=="
                )
            );
            case UID_FORMAT: return UUIDConversion.fromUID(uuid);
            case UUID_FORMAT: return UUID.fromString(uuid);
            case URN_FORMAT: return UUID.fromString(uuid.substring(9));
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
    public static String toXRI (
        UUID uuid
    ){
        return "xri://$t*uuid*" + uuid;
    }

    /**
     * Returns a base 36 encoded URI.
     * <p>
     * Note:<br>
     * The encoding is as following:<ul>
     * <li>Digit 0 contains sum of<ul>
     * <li>6 times (bits 1 to 63 divided by 36^12 plus 3 if bit 0 is set) 
     * <li>bits 65 to 127 divided by 36^12 plus 3 if bit 64 is set
     * </ul>
     * <li>Digits 1 to 12 contain bits 1 to 63 modulo 36^12
     * <li>Digits 13 to 24 contain bits 65 to 127 modulo 36^12
     * </ul>
     * 
     * @param uuid the UUID to be converted
     * 
     * @return the corresponding UID
     */
    public static String toUID (
       UUID uuid
    ){
        return uuid == null ? null : toUID(
            uuid.getMostSignificantBits(),
            uuid.getLeastSignificantBits()
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
     * Decodes a base 36 encoded UID
     * <p>
     * Note:<br>
     * The encoding is as following:<ul>
     * <li>Digit 0 contains sum of<ul>
     * <li>6 times (bits 1 to 63 divided by 36^12 plus 3 if bit 0 is set) 
     * <li>bits 65 to 127 divided by 36^12 plus 3 if bit 64 is set
     * </ul>
     * <li>Digits 1 to 12 contain bits 1 to 63 modulo 36^12
     * <li>Digits 13 to 24 contain bits 65 to 127 modulo 36^12
     * </ul>
     * 
     * @param uid the base 36 encoded UID
     * 
     * @return the corresponding UUID
     */
    private static UUID fromUID(
        String uid
    ){
        long split = digit(uid.charAt(0));
        long mostSignificantBits = split / 6;
        boolean mostSignificantIsNegative = mostSignificantBits >= 3;
        if(mostSignificantIsNegative) mostSignificantBits -= 3;
        long leastSignificantBits = split % 6;
        boolean leastSignificantIsNegative = leastSignificantBits >= 3;
        if(leastSignificantIsNegative)leastSignificantBits -= 3;
        for(int i = 1; i < 13; i++){
            mostSignificantBits *= 36;
            mostSignificantBits += digit(uid.charAt(i));
        }
        if(mostSignificantIsNegative) mostSignificantBits |= 0x8000000000000000l;
        for(int i = 13; i < 25; i++){
            leastSignificantBits *= 36;
            leastSignificantBits += digit(uid.charAt(i));
        }
        if(leastSignificantIsNegative) leastSignificantBits |= 0x8000000000000000l;
        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    /**
     * Uppercase radix 36 character for digit
     * 
     * @param digit the digit to be converted
     * 
     * @return its character representation
     */
    private static char forDigit(
        long digit
    ){
        return (char) (digit + (digit < 10 ? '0' : 'A' - 10)); 
    }
    
    /**
     * Uppercase radix 16 digit for character 
     * 
     * @param character the digits character representation
     * 
     * @return the digit 
     */
    private static long digit (
        char character
    ){
        return character - (character < 'A' ? '0' : 'A' - 10);
    }
    
    /**
     * 
     * @param mostSignificantBits
     * @param leastSignificantBits
     * @return
     */
    private static String toUID(
        long mostSignificantBits,
        long leastSignificantBits
    ){
        StringBuilder uid = new StringBuilder(UID_FORMAT);
        long leastSignificantSignum = leastSignificantBits < 0 ? 3 : 0;
        leastSignificantBits &= 0x7FFFFFFFFFFFFFFFl;
        long mostSignificantSignum = mostSignificantBits < 0 ? 3 : 0;
        mostSignificantBits &= 0x7FFFFFFFFFFFFFFFl;
        for(int i = 0; i < 12; i++) {
            uid.insert(0, forDigit(leastSignificantBits % 36));
            leastSignificantBits /= 36;
        }
        for(int i = 0; i < 12; i++) {
            uid.insert(0, forDigit(mostSignificantBits % 36));
            mostSignificantBits /= 36;
        }
        leastSignificantBits += leastSignificantSignum;
        mostSignificantBits += mostSignificantSignum;
        uid.insert(0, forDigit(6 * mostSignificantBits + leastSignificantBits));
        return uid.toString();
    }
    
    /**
     * The legacy UID format's length
     */
    private static final int LEGACY_FORMAT = 22;

    /**
     * The UID format's length
     */
    private static final int UID_FORMAT = 25;

    /**
     * The UUID format's length
     */
    private static final int UUID_FORMAT = 36;
    
    /**
     * The URN format's length
     */
    private static final int URN_FORMAT = 45;

}
