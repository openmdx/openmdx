/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: RadiusClientFactory
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.security.radius.client;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.logging.Logger;

import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusClient;
import org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory;


/**
 * Radius Client Factory
 */
public class RadiusClientFactory implements PoolableObjectFactory {

    /**
     * Constructor
     * 
     * @param hostNames The Radius Server's Host Name
     * @param authenticationPorts The Radius Server's Authentication Port
     * @param accountingPorts The Radius Server's Accounting Port
     * @param sharedSecret The Radius Protocol's Shared Secret 
     * @param socketTimeout The Radius Client's Socket Timeout
     * @param logger the logger must not be {@code null}
     * @param trace tells whether the radius requests are traced
     * @param nasAddress the NAS IP Address; the NAS Identifier is used in case of null
     */
    public RadiusClientFactory(
        String[] hostNames,
        int[] authenticationPorts,
        int[] accountingPorts,
        String sharedSecret, 
        long socketTimeout,
        Logger logger,
        boolean trace,
        InetAddress nasAddress
    ) {
        this.hostNames = hostNames;
        this.authenticationPorts = authenticationPorts;
        this.accountingPorts = accountingPorts;
        this.sharedSecret = sharedSecret;
        this.socketTimeout = new BigDecimal(
        	BigInteger.valueOf(socketTimeout), 
        	3
        );
        this.logger = logger;
        this.trace = trace;
        this.nasAddress = nasAddress;
    }

    /**
     * The Radius Server's Host Name
     */
    private final String[] hostNames;

    /**
     * The Radius Server's Authentication Port
     */
    private final int[] authenticationPorts;

    /**
     * The Radius Server's Accounting Port
     */
    private final int[] accountingPorts;

    /**
     * The Radius Protocol's Shared Secret
     */
    private final String sharedSecret;
    
    /**
     * The Radius Client's Socket Timeout
     */
    private final BigDecimal socketTimeout;
    
    /**
     * The logger for trace and failure messages
     */
    private final Logger logger;
    
    /**
     * Tells whether the Radius requests are traced
     */
    private final boolean trace;
    
    /**
     * Defines whether the NAS identifier or address should be used
     */
    private final InetAddress nasAddress;
    
    
    //------------------------------------------------------------------------
    // Implements PoolableObjectFactory
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#makeObject()
     */
    public Object makeObject() throws Exception {
        return new RadiusClient(
            this.hostNames,
            this.authenticationPorts,
            this.accountingPorts,
            this.sharedSecret,
            this.socketTimeout, 
            this.logger, 
            this.trace, 
            this.nasAddress
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#destroyObject(java.lang.Object)
     */
    public void destroyObject(Object obj) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#validateObject(java.lang.Object)
     */
    public boolean validateObject(Object obj) {
        return 
        	obj instanceof RadiusClient &&
        	((RadiusClient)obj).isValid();
    }

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#activateObject(java.lang.Object)
     */
    public void activateObject(Object obj) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#passivateObject(java.lang.Object)
     */
    public void passivateObject(Object obj) throws Exception {
    }

}
