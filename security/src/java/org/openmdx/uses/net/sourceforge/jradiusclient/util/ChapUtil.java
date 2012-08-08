/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ChapUtil.java,v 1.2 2004/10/14 19:28:24 hburger Exp $
 * Description: Java Radius Client Derivate
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/10/14 19:28:24 $
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
package org.openmdx.uses.net.sourceforge.jradiusclient.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Released under the LGPL<BR>
 * @author <a href="mailto:bloihl@users.sourceforge.net">Robert J. Loihl</a>
 * @version $Revision: 1.2 $
 */
public class ChapUtil {
    public static final int DEFAULT_CHALLENGE_SIZE = 16;
    private SecureRandom srand = null;
    /**
     *
     *
     */
    public ChapUtil(){
        this.srand = new SecureRandom();
    }
    /**
     *
     * @return  a random byte to use as a chap Identifier
     */
    public byte getNextChapIdentifier(){
        synchronized (this.srand){
            return (byte)this.srand.nextInt(255);
        }
    }
    /**
     * Get a fixed number of bytes to use as a chap challenge, generally you don't want this to
     * be less than the number of bytes outbut by the hash algoritm to be used to encrypt the
     * password, in this case 16 since Radius rfc 2865 section 2.2 specifies MD5 if size is <1
     * we will use the default of 16
     * @param size number of bytes the challenge will be
     * @return suze bytes of random data to use as a chapchallenge
     */
    public byte[] getNextChapChallenge(final int size){
        byte[] challenge = new byte[size];
        synchronized (this.srand){
            this.srand.nextBytes(challenge);
        }
        return challenge;
    }
    /**
     * This method performs the CHAP encryption according to RFC 2865 section 2.2 for use with Radius Servers using
     * MD5 as the one way hashing algorithm
     * @param chapIdentifier a byte to help correlate unique challenges/responses
     * @param plaintextPassword exactly what it says.
     * @param chapChallenge the bytes to encode the plaintext password with
     * @return the encrypted password as a byte array (16 bytes to be exact as a result of the MD5 process)
     */
    public static final byte[] chapEncrypt(final byte chapIdentifier, final byte[] plaintextPassword, byte[] chapChallenge){
        //pretend we are a client who is encrypting his password with a random
        //challenge from the NAS, see RFC 2865 section 2.2
        //generate next chapIdentifier
        byte[] chapPassword = plaintextPassword;// if we get an error we will send back plaintext
        try{
            MessageDigest md5MessageDigest = MessageDigest.getInstance("MD5");
            md5MessageDigest.reset();
            md5MessageDigest.update(chapIdentifier);
            md5MessageDigest.update(plaintextPassword);
            chapPassword = md5MessageDigest.digest(chapChallenge);
        }catch(NoSuchAlgorithmException nsaex) {
            throw new RuntimeException("Could not access MD5 algorithm, fatal error");
        }
        return chapPassword;
    }

}
