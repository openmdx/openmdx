/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: PapAccessRequest.java,v 1.4 2010/06/04 22:41:44 hburger Exp $
 * Description: Java Radius Client Derivate
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/04 22:41:44 $
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
import org.openmdx.uses.net.sourceforge.jradiusclient.attributes.UserNameAttribute;
import org.openmdx.uses.net.sourceforge.jradiusclient.attributes.UserPasswordAttribute;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException;


/**
 * Released under the LGPL<BR>
 * @author <a href="mailto:bloihl@users.sourceforge.net">Robert J. Loihl</a>
 * @version $Revision: 1.4 $
 */
public class PapAccessRequest extends RadiusPacket {
    private boolean initialized = false;
    /**
     * 
     * @param userName
     * @param plaintextPassword
     * @throws InvalidParameterException
     */
    public PapAccessRequest(final String userName, final String plaintextPassword )
            throws InvalidParameterException{
        this(userName,plaintextPassword.getBytes());
    }
    /**
     * 
     * @param userName
     * @param plaintextPassword
     * @throws InvalidParameterException
     */
    public PapAccessRequest(final String userName, final byte[] plaintextPassword )
            throws InvalidParameterException{
        super (ACCESS_REQUEST);
        setAttribute(new UserNameAttribute(userName));
        setAttribute(new UserPasswordAttribute(plaintextPassword));
        this.initialized = true;
    }
    /**
     * 
     * @param radiusAttribute
     */
    @Override
    public void validateAttribute(RadiusAttribute radiusAttribute) throws InvalidParameterException{
        if ((this.initialized) && (radiusAttribute.getType() == RadiusAttributeValues.USER_NAME )){
            throw new InvalidParameterException ("Already initialized, cannot reset username.");
        }
    }
}