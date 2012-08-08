/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RadiusCallback.java,v 1.3 2005/06/07 20:07:00 hburger Exp $
 * Description: Java Radius Client Derivate
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/06/07 20:07:00 $
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
package org.openmdx.uses.net.sourceforge.jradiusclient.jaas;

import javax.security.auth.callback.Callback;

public class RadiusCallback implements Callback {

    private String hostName;
    private String sharedSecret;
    private int authPort;
    private int acctPort;
    private String callingStationID;
    private int numRetries;
    private int reqTimeout;

    public String getHostName() {
        return this.hostName;
    }

    public String getSharedSecret() {
        return this.sharedSecret;
    }

    public int getAuthPort() {
        return this.authPort;
    }

    public int getAcctPort() {
        return this.acctPort;
    }

    public String getCallingStationID() {
        return this.callingStationID;
    }

    public int getNumRetries() {
        return this.numRetries;
    }
    public int getTimeout(){
        return this.reqTimeout;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public void setAuthPort(int authPort) {
        this.authPort = authPort;
    }

    public void setAcctPort(int acctPort) {
        this.acctPort = acctPort;
    }

    public void setCallingStationID(String callingStationId){
        this.callingStationID = callingStationId;
    }

    public void setNumRetries(int numRetries) {
        if (numRetries <=0){
            numRetries = 1;
        }
        this.numRetries = numRetries;
    }
    public void setTimeout(int seconds){
        if (seconds < 0) {
            seconds = 0;
        }
        this.reqTimeout = seconds * 1000;
    }
}
