/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Java Radius Client Derivate
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.uses.net.sourceforge.jradiusclient.packets;

import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusAttribute;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusAttributeValues;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusPacket;
import org.openmdx.uses.net.sourceforge.jradiusclient.attributes.ChapChallengeAttribute;
import org.openmdx.uses.net.sourceforge.jradiusclient.attributes.ChapPasswordAttribute;
import org.openmdx.uses.net.sourceforge.jradiusclient.attributes.UserNameAttribute;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException;
import org.openmdx.uses.net.sourceforge.jradiusclient.util.ChapUtil;


/**
 * Released under the LGPL<BR>
 * @author <a href="mailto:bloihl@users.sourceforge.net">Robert J. Loihl</a>
 */
public class ChapAccessRequest extends RadiusPacket {
    private static final ChapUtil chapUtil = new ChapUtil();
    private boolean initialized = false;
    /**
     * 
     * @param userName
     * @param chapEncryptedPassword
     * @param chapIndentifier
     * @param chapChallenge
     * @throws InvalidParameterException
     */
    public ChapAccessRequest(final String userName, final byte[] chapEncryptedPassword, final byte chapIndentifier, final byte[] chapChallenge)
            throws InvalidParameterException{
        super (ACCESS_REQUEST);
        initialize(userName, chapEncryptedPassword, chapIndentifier, chapChallenge);
    }
    /**
     * 
     * @param userName
     * @param plaintextPassword
     * @throws InvalidParameterException
     */
    public ChapAccessRequest(final String userName, final String plaintextPassword )
            throws InvalidParameterException{
        this(userName, plaintextPassword.getBytes(), ChapUtil.DEFAULT_CHALLENGE_SIZE);
    }
    /**
     * 
     * @param userName
     * @param plaintextPassword
     * @throws InvalidParameterException
     */
    public ChapAccessRequest(final String userName, final byte[] plaintextPassword)
            throws InvalidParameterException{
        this(userName, plaintextPassword, ChapUtil.DEFAULT_CHALLENGE_SIZE);
    }
    /**
     * 
     * @param userName
     * @param plaintextPassword
     * @throws InvalidParameterException
     */
    public ChapAccessRequest(final String userName, final byte[] plaintextPassword, final int challengeSize)
            throws InvalidParameterException{
        super (ACCESS_REQUEST);
        byte chapIndentifier = chapUtil.getNextChapIdentifier();
        byte[] chapChallenge = chapUtil.getNextChapChallenge(challengeSize);
        byte[] chapEncryptedPassword = ChapUtil.chapEncrypt(chapIndentifier, plaintextPassword, chapChallenge);
        initialize(userName, chapEncryptedPassword, chapIndentifier, chapChallenge);
    }
    /**
     * 
     * @param userName
     * @param chapEncryptedPassword
     * @param chapIndentifier
     * @param chapChallenge
     * @throws InvalidParameterException
     */
    private void initialize(final String userName, final byte[] chapEncryptedPassword, final byte chapIndentifier, final byte[] chapChallenge)
    throws InvalidParameterException{
        setAttribute(new UserNameAttribute(userName));
        setAttribute(new ChapPasswordAttribute(chapIndentifier,chapEncryptedPassword));
        setAttribute(new ChapChallengeAttribute(chapChallenge));
        this.initialized = true;
    }
    /**
     * This method implements a callback from the super class RadiusPacket to validate input
     * @param radiusAttribute the attribute to validate
     * @throws InvalidParameterException if the RadiusAttribute does not pass validation
     */
    @Override
    public void validateAttribute(final RadiusAttribute radiusAttribute) throws InvalidParameterException{
        if ((this.initialized) && (radiusAttribute.getType() == RadiusAttributeValues.USER_NAME ||
                    radiusAttribute.getType() == RadiusAttributeValues.CHAP_PASSWORD ||
                    radiusAttribute.getType() == RadiusAttributeValues.CHAP_CHALLENGE)){
            throw new InvalidParameterException ("Already initialized, cannot reset username, chap password or chap challenge.");
        }else if (radiusAttribute.getType() == RadiusAttributeValues.USER_PASSWORD){
            throw new InvalidParameterException ("Already initialized, cannot set USER_PASSWORD in a CHAP Access Request.");
        }
    }
}