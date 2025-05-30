/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Validating Marshaller
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
package org.openmdx.base.accessor.jmi.spi;

import javax.jmi.reflect.RefBaseObject;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;

/**
 * Validating Marshaller
 */
public class ValidatingMarshaller implements Marshaller {

	/**
	 * Constructor
	 *  
	 * @param outermostPackage
	 */
	ValidatingMarshaller(
		RefRootPackage_1 outermostPackage
    ){
		this.outermostPackage = outermostPackage;
    }

	/**
	 * The outermost package this marshaller belongs to
	 */
	private final RefRootPackage_1 outermostPackage;
	
    /**
     * Retrieve the marshaller's delegate
     * 
     * @return the outermost package
     */
    Jmi1Package_1_0 getOutermostPackage(){
        return this.outermostPackage;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Marshaller#unmarshal(java.lang.Object)
     */
    public Object unmarshal(
        Object source
    ){
    	return source;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Marshaller#marshal(java.lang.Object)
     */
    public Object marshal(
        Object source
    ) throws ServiceException{
    	validate(source);
    	return source;
    }

	/**
	 * Validate a given object
	 * 
	 * @param value
	 * 
	 * @throws ServiceException 
	 */
	protected void validate(
		Object value
	) throws ServiceException{
		if(value instanceof RefBaseObject) {
			RefBaseObject object = (RefBaseObject) value;
			if(this.outermostPackage !=  object.refOutermostPackage()){
				throw new ServiceException(
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.ASSERTION_FAILURE,
					"RefPackage mismatch, the object does not have the expected outermost package",
					new BasicException.Parameter(BasicException.Parameter.XRI, object.refMofId())
				).log();
			}
		}
	}
    
}