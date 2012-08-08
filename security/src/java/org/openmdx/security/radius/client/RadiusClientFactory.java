/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RadiusClientFactory.java,v 1.6 2005/06/14 21:18:17 hburger Exp $
 * Description: Java Radius Client Derivate
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/06/14 21:18:17 $
 * ====================================================================
 *
 * Copyright (C) 2004  OMEX AG
 *
 * * This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 * * This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 * * You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * 
 * This library BASED on Java Radius Client 2.0.0
 * (http://http://jradius-client.sourceforge.net/),
 * but it's namespace and content has been MODIFIED by OMEX AG
 * in order to integrate it into the openMDX framework.
 */
package org.openmdx.security.radius.client;

import java.net.InetAddress;

import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusClient;

import org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory;


/**
 * Radius Client Factory
 */
public class RadiusClientFactory implements PoolableObjectFactory {

    /**
     * Constructor
     * @param hostNames The Radius Server's Host Name
     * @param authenticationPorts The Radius Server's Authentication Port
     * @param accountingPorts The Radius Server's Accounting Port
     * @param sharedSecret The Radius Protocol's Shared Secret 
     * @param socketTimeout The Radius Client's Socket Timeout
     * @param trace defines whether the radius requests should be traced
     * @param nasAddress the NAS IP Address; the NAS Identifier is used in case of null
     */
    public RadiusClientFactory(
        String[] hostNames,
        int[] authenticationPorts,
        int[] accountingPorts,
        String sharedSecret, 
        long socketTimeout,
        boolean trace, 
        InetAddress nasAddress
    ) {
        this.hostNames = hostNames;
        this.authenticationPorts = authenticationPorts;
        this.accountingPorts = accountingPorts;
        this.sharedSecret = sharedSecret;
        this.socketTimeout = socketTimeout;
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
    private final long socketTimeout;
    
    /**
     * Defines whether the radius requests should be traced.
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
            (int)this.socketTimeout, 
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
        return obj instanceof RadiusClient;
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
