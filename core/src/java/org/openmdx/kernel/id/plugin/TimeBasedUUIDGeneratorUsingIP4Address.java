/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Random Based UUID Provider
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
package org.openmdx.kernel.id.plugin;

import java.net.InetAddress;

import org.openmdx.kernel.id.cci.UUIDGenerator;
import org.openmdx.kernel.id.spi.TimeBasedIdGenerator;


import java.util.UUID;




/**
 * Time Based UUID Provider using IP 4 Address Based Nodes
 * <p>
 * This implementation follow the guidelines of
 * http://www.ietf.org/internet-drafts/draft-mealling-uuid-urn-03.txt and avoids
 * conflicts with TimeBasedUUIDGeneratorUsingRandomBasedNodes by setting the
 * bit next to the MAC address multicast bit.
 */
public class TimeBasedUUIDGeneratorUsingIP4Address
    extends TimeBasedIdGenerator
    implements UUIDGenerator
{

    /**
     * Create an IP 4 based based node id
     * 
     * @return an IP 4 based based node id
     */
    private static long createIP4AddressBasedNodeId(
    ){
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            byte[] ipAddress = inetAddress.getAddress();
            if(ipAddress.length != 4) throw new Exception("Invalid IP Address: " + inetAddress);
            return
                (getRandom().nextLong() & 0x0000FEFF00000000L) | // 15 bit random part in case of duplicate IP address
                0x0000010000000000L | // with the MAC multicast bit set
                ((ipAddress[0] & 0xFFL) << 24) | ((ipAddress[1] & 0xFFL) << 16) |
                ((ipAddress[2] & 0xFFL) << 8) | ((ipAddress[3] & 0xFFL)); // IP v4 Address
        } catch(Exception exception){
            exception.printStackTrace();
            System.err.println("Falling back to random based node id");
            return getRandomBasedNode();
        }
    }

    //------------------------------------------------------------------------
    // Extends TimeBasedIdGenerator
    //------------------------------------------------------------------------

    /**
     * The IP Address based node value
     */
    final static private long node = createIP4AddressBasedNodeId();

    /* (non-Javadoc)
     * @see org.openmdx.kernel.id.spi.TimeBasedUUIDBuilder#getNode()
     */
    @Override
    protected long getNode() {
        return TimeBasedUUIDGeneratorUsingIP4Address.node;
    }


    //------------------------------------------------------------------------
    // Implements UUIDGenerator
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.kernel.id.cci.UUIDGenerator#next()
     */
    public UUID next(
    ){
        return new UUID(
            nextMostSignificantBits(),
            nextLeastSignificantBits()
         );
    }

}
