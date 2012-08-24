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
package org.openmdx.uses.net.sourceforge.jradiusclient.jaas;

import java.security.Principal;

/**
 *
 * @author <a href="mailto:bloihl@users.sourceforge.net">Robert J. Loihl</a>
 * @version $Revision: 1.3 $
 */
public class RadiusPrincipal implements Principal{
    private String principalName;
    /**
     * Constructs RadiusPrincipal objects
     * @param name java.lang.String The name of this principal
     */
    public RadiusPrincipal(String name){
        if (name == null){
            throw new NullPointerException("Illegal name input, name cannot be null.");
        }
        this.principalName = name;
    }
    /**
     * Gets the name of this <code>RadiusPrincipal</code>
     * @return java.lang.String The name of this <code>RadiusPrincipal</code>
     */
    public String getName(){
        return this.principalName;
    }
    /**
     * This method returns a string representation of this
     * <code>RadiusPrincipal</code>.
     *
     * @return a string representation of this <code>RadiusPrincipal</code>.
     */
    @Override
    public String toString(){
        return this.getName();
    }
    /**
     * Compares the specified Object with this <code>RadiusPrincipal</code>
     * for equality.  Returns true if the given object is also a
     * <code>RadiusPrincipal</code> and the two RadiusPrincipal
     * have the same username.
     * @param object Object to be compared for equality with this
     *		<code>RadiusPrincipal</code>.
     *
     * @return true if the specified Object is equal to this
     *		<code>RadiusPrincipal</code>.
     */
    @Override
    public boolean equals(Object object){
        if (object == null){
            return false;
        }
        if (this == object){
            return true;
        }
        if (!(object instanceof RadiusPrincipal)){
            return false;
        }
        RadiusPrincipal that = (RadiusPrincipal)object;
        if (this.getName().equals(that.getName())){
            return true;
        }
        return true;
    }
    /**
     * @return int the hashCode for this <code>RadiusPrincipal</code>
     */
    @Override
    public int hashCode(){
        return this.principalName.hashCode();
    }
}