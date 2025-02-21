/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Address  
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package test.openmdx.app1.aop2;

import javax.jdo.JDOHelper;

import org.openmdx.base.aop2.AbstractObject;
import org.openmdx.base.naming.Path;
import test.openmdx.app1.jmi1.AddressFormatAsParams;
import test.openmdx.app1.jmi1.AddressFormatAsResult;
import test.openmdx.app1.jmi1.EmailAddress;
import test.openmdx.app1.jmi1.PostalAddress;

/**
 * Address 
 */
public class AddressImpl<S extends test.openmdx.app1.jmi1.Address,N extends test.openmdx.app1.cci2.Address> 
    extends AbstractObject<S,N,Void> 
{

    /**
     * Constructor 
     *
     * @param same the same layer JMI API
     * @param next the next layer CCI API
     */
    public AddressImpl(
        S same,
        N next
    ) {
        super(same, next);
    }

    /**
     * Standard format type
     */
    protected static final String STANDARD = "Standard";
  
    /**
     * Format address
     * 
     * @param in the method's input structure
     * 
     * @return the method's result structure
     * 
     * @see test.openmdx.app1.jmi1.Address#formatAs(test.openmdx.app1.jmi1.AddressFormatAsParams)
     */
    public AddressFormatAsResult formatAs(AddressFormatAsParams in) {
       throw new UnsupportedOperationException(
            "Address type not supported. Supported are [" +
            EmailAddress.class.getSimpleName() + ", " +
            PostalAddress.class.getSimpleName() + "]"
        );
    }

    /**
     * Retrieve the address id
     * 
     * @return the address id
     * 
     * @see test.openmdx.app1.cci2.Address#getId()
     */
    public String getId() {
        Path objectId = (Path) JDOHelper.getObjectId(sameObject());
        return objectId == null ? null : objectId.getLastSegment().toClassicRepresentation();
    }
    
}
