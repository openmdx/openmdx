/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Public Document IDs 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010-2013, OMEX AG, Switzerland
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
package org.openmdx.base.wbxml;

import java.util.HashMap;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * Public Document IDs<ul>
 * <li><code>0x00</code> to 0x7F</code>: <em>Well-Known values</em> are set as specified by the OMA MAG/MAE group processes.
 * <li><code>0x80</code> to 0x3FFF</code>: <em>Reserved values</em>, registered and managed by OMNA.
 * <li><code>0x4000</code> to <code>0x1FFFFF</code>: <em>Experimental values</em>, registered by OMNA only for advisory purposes
 * </ul>
 */
public class ExternalIdentifiers {

    /**
     * Public identifiers may also be encoded as strings, in the situation 
     * where a pre-defined numeric identifier is not available.
     */
    public static final int LITERAL = 0x00;
    
    /**
     * <code>NULL</code> represents a missing or unknown public document id.
     */
    public static final int NULL = 0x01;
    
    /**
     * The well known values are in the range code>0x00</code> to 0x7F</code>
     */
    private static String[] WELL_KNOWN = {
        "-//WAPFORUM//DTD WML 1.0//EN", // 0x02 WML 1.0
        "-//WAPFORUM//DTD WTA 1.0//EN", // 0x03 DEPRECATED - WTA Event 1.0
        "-//WAPFORUM//DTD WML 1.1//EN", // 0x04 WML 1.1
        "-//WAPFORUM//DTD SI 1.0//EN", // 0x05 Service Indication 1.0
        "-//WAPFORUM//DTD SL 1.0//EN", // 0x06 Service Loading 1.0
        "-//WAPFORUM//DTD CO 1.0//EN", // 0x07 Cache Operation 1.0
        "-//WAPFORUM//DTD CHANNEL 1.1//EN", // 0x08 Channel 1.1
        "-//WAPFORUM//DTD WML 1.2//EN", // 0x09 WML 1.2
        "-//WAPFORUM//DTD WML 1.3//EN", // 0x0A WML 1.3
        "-//WAPFORUM//DTD PROV 1.0//EN", // 0x0B Provisioning 1.0
        "-//WAPFORUM//DTD WTA-WML 1.2//EN", // 0x0C WTA-WML 1.2
        "-//WAPFORUM//DTD EMN 1.0//EN", // 0x0D Email Notification 1.0 WAP-297
        "-//OMA//DTD DRMREL 1.0//EN", // 0x0E DRM REL 1.0
        "-//WIRELESSVILLAGE//DTD CSP 1.0//EN", // 0x0F Wireless Village CSP DTD v1.0
        "-//WIRELESSVILLAGE//DTD CSP 1.1//EN", // 0x10 Wireless Village CSP DTD v1.1
        "-//OMA//DTD WV-CSP 1.2//EN", // 0x11 OMA IMPS - CSP protocol DTD v1.2
        "-//OMA//DTD IMPS-CSP 1.3//EN", // 0x12 This document type is used to carry OMA IMPS 1.3 primitives and the information elements within.
        "-//OMA//DRM 2.1//EN", // 0x13 OMA DRM 2.1
        "-//OMA//SRM 1.0//EN", // 0x14 OMA SRM 1.0
        "-//OMA//DCD 1.0//EN", // 0x15 OMA DCD 1.0
        "-//OMA//DTD DS-DataObjectEmail 1.2//EN", // 0x16 In order to use a WBXML representation of the 'email data object' defined by OMA-DS
        "-//OMA//DTD DS-DataObjectFolder 1.2//EN", // 0x17 In order to use a WBXML representation of the 'folder data object' defined by OMA-DS
        "-//OMA//DTD DS-DataObjectFile 1.2//EN" // 0x18 In order to use a WBXML representation of the 'file data object' defined by OMA-DS
    };

    /**
     * The registered values are in the range <code>0x80</code> to 0x3FFF</code>
     */
    private static final Map<Integer,String> REGISTERED = new HashMap<Integer, String>();
    
    /**
     * The private and experimental values are in the range <code>0x4000</code> to <code>0x1FFFFF</code>
     */
    private static final Map<Integer,String> PRIVATE = new HashMap<Integer, String>();
    
    /**
     * Retrieve the WBXML specific public document identifier
     * 
     * @param xmlDocumentId
     * 
     * @return the WBXML specific public document identifier, or <code>LITERAL</code> if it has to be transferred literally
     * 
     * @throws ServiceException
     */
    public static int toPublicDocumentId(
        String xmlDocumentId
    ) throws ServiceException{
        if(xmlDocumentId == null) {
            return NULL;
        }
        for(
            int i = 0;
            i < WELL_KNOWN.length;
            i++
        ){
            if(xmlDocumentId.equals(WELL_KNOWN[i])) {
                return i + 0x02;
            }
        }
        for(Map.Entry<Integer,String> entry : REGISTERED.entrySet()) {
            if(xmlDocumentId.equals(entry.getValue())) {
                return entry.getKey().intValue();
            }
        }
        for(Map.Entry<Integer,String> entry : PRIVATE.entrySet()) {
            if(xmlDocumentId.equals(entry.getValue())) {
                return entry.getKey().intValue();
            }
        }
        return LITERAL;
    }

    /**
     * Retrieve the WBXML specific public document identifier
     * 
     * @param wbxmlDocumentId
     * @return the WBXML specific public document identifier
     * 
     * @throws ServiceException
     */
    public static String toPublicDocumentId(
        int wbxmlDocumentId
    ) throws ServiceException{
        if(wbxmlDocumentId == NULL) {
            return null;
        } else if(wbxmlDocumentId > 0x02 && wbxmlDocumentId <= 0x7F) {
            int i = wbxmlDocumentId - 0x02;
            if(i < WELL_KNOWN.length) return WELL_KNOWN[i];
        } else if (wbxmlDocumentId >= 0x80 && wbxmlDocumentId <= 0x3FFF) {
            String xmlDocumentId = REGISTERED.get(Integer.valueOf(wbxmlDocumentId));
            if(xmlDocumentId != null) return xmlDocumentId;
        } else if (wbxmlDocumentId >= 0x4000 && wbxmlDocumentId <= 0x1FFFFF) {
            String xmlDocumentId = PRIVATE.get(Integer.valueOf(wbxmlDocumentId));
            if(xmlDocumentId != null) return xmlDocumentId;
        }
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "No matching document id found",
            new BasicException.Parameter("wbxmlDocumentId", wbxmlDocumentId)
        );
    }
    
    static {
        REGISTERED.put(Integer.valueOf(0x0FD1), "-//SYNCML//DTD SyncML 1.0//EN");
        REGISTERED.put(Integer.valueOf(0x0FD2), "-//SYNCML//DTD DevInf 1.0//EN");
        REGISTERED.put(Integer.valueOf(0x0FD3), "-//SYNCML//DTD SyncML 1.1//EN");
        REGISTERED.put(Integer.valueOf(0x0FD4), "-//SYNCML//DTD DevInf 1.1//EN");
        REGISTERED.put(Integer.valueOf(0x1201), "-//SYNCML//DTD SyncML 1.2//EN"); // OMA Data Synchronization (SyncML) Representation Protocol DTD v1.2
        REGISTERED.put(Integer.valueOf(0x1202), "-//SYNCML//DTD MetaInf 1.2//EN"); // OMA Data Synchronization (SyncML) Meta Information DTD v1.2
        REGISTERED.put(Integer.valueOf(0x1203), "-//SYNCML//DTD DevInf 1.2//EN"); // OMA Data Synchronization (SyncML) Device Information DTD v1.2
        REGISTERED.put(Integer.valueOf(0x1205), "-//SyncML//Schema SyncML 2.0//EN"); // OMA Data Synchronization (SyncML) Representation Protocol Schema v2.0
        REGISTERED.put(Integer.valueOf(0x1206), "-//SyncML//Schema DevInf 2.0//EN"); // OMA Data Synchronization (SyncML) Device Information Schema v2.0
        REGISTERED.put(Integer.valueOf(0x1100), "-//PHONE.COM//DTD ALERT 1.0//EN"); // intended to inform users of events that may be of relevance to them.
        REGISTERED.put(Integer.valueOf(0x1101), "-//PHONE.COM//DTD CACHE-OPERATION 1.0//EN"); // to perform user-agent cache invalidation operation.
        REGISTERED.put(Integer.valueOf(0x1102), "-//PHONE.COM//DTD SIGNAL 1.0//EN"); // to indicate the presence of a pending notifications to be retrieved.
        REGISTERED.put(Integer.valueOf(0x1103), "-//PHONE.COM//DTD LIST 1.0//EN"); // specifies a List Resource.
        REGISTERED.put(Integer.valueOf(0x1104), "-//PHONE.COM//DTD LISTCMD 1.0//EN"); // command to manipulate the content of a list.
        REGISTERED.put(Integer.valueOf(0x1105), "-//PHONE.COM//DTD CHANNEL 1.0//EN"); // specifies a collection of resources that are to be pre-loaded into and made persistent in the user agent cache.
        REGISTERED.put(Integer.valueOf(0x1106), "-//PHONE.COM//DTD MMC 1.0//EN"); // to read/write arbitrary parameter from/to a device.
        REGISTERED.put(Integer.valueOf(0x1107), "-//PHONE.COM//DTD BEARER-CHOICE 1.0//EN"); // specifies the bearer selection preference.
        REGISTERED.put(Integer.valueOf(0x1108), "-//PHONE.COM//DTD WML 1.1//EN"); // defines the Phone.com WML extensions.
        REGISTERED.put(Integer.valueOf(0x1109), "-//PHONE.COM//DTD CHANNEL 1.1//EN"); // collection of resources that are to be pre-loaded into and made persistent in the user agent cache.
        REGISTERED.put(Integer.valueOf(0x110A), "-//PHONE.COM//DTD LIST 1.1//EN"); // specifies a List Resource.
        REGISTERED.put(Integer.valueOf(0x110B), "-//PHONE.COM//DTD LISTCMD 1.1//EN"); // specifies a command to manipulate the content of a list.
        REGISTERED.put(Integer.valueOf(0x110C), "-//PHONE.COM//DTD MMC 1.1//EN");
        REGISTERED.put(Integer.valueOf(0x110D), "-//PHONE.COM//DTD WML 1.3//EN");
        REGISTERED.put(Integer.valueOf(0x110E), "-//PHONE.COM//DTD MMC 2.0//EN"); // Openwave IP-based Over-the-Air-Activation (IOTA) v2.0 protocol document that is exchanged with the mobile device to read/write arbitrary parameters.
        REGISTERED.put(Integer.valueOf(0x1200), "-//3GPP2.COM//DTD IOTA 1.0//EN"); // 3GPP2 IP-based Over-the-Air Activation (IOTA) v1.0 protocol document that is exchanged with the mobile device to read/write arbitrary parameters.
        REGISTERED.put(Integer.valueOf(0x1204), "-//NOKIA//DTD LANDMARKS 1.0//EN"); // The document is used for delivering location information between mobile devices or network servers. A landmark object is a waypoint or a point-of-interest. The use of binary format allows for compact transmission through narrow-band bearers (e.g. GSM SMS).", // 0x0 A landmark object may contain the following information: the name of landmark, textual description of the landmark, the geographical coordinates of the location, like latitude and longitude.
        REGISTERED.put(Integer.valueOf(0x1207), "-//OMA//DTD DRMREL 1.0//EN"); //  We intend to create wbxml to send drm message
        PRIVATE.put(Integer.valueOf(0x104F52), "-//openMDX//REST 2.0//EN"); // openMDX REST 2.0 data with dynamic code pages
        PRIVATE.put(Integer.valueOf(0x104F58), "-//openMDX//XMI 1.0//EN"); // openMDX XMI 1.0 data 
    }

}
