/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Void Record 
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
package org.openmdx.base.rest.spi;



/**
 * Void Record
 */
public class VoidRecord 
    extends AbstractMappedRecord<org.openmdx.base.rest.cci.VoidRecord.Member>
    implements org.openmdx.base.rest.cci.VoidRecord 
{
    
	/**
     * Constructor 
     */
    private VoidRecord() {
        makeImmutable();
    }

    /**
     * Allows to share the member information among the instances
     */
    private static final Members<Member> MEMBERS = Members.newInstance(Member.class);
    
    /**
     * Implements <code>Serializable</code>
     */
	private static final long serialVersionUID = 239706723820907197L;

	/**
	 * Void can be implemented as singleton
	 */
	private static final VoidRecord INSTANCE = new VoidRecord();
	
    /**
	 * @return the instance
	 */
	public static org.openmdx.base.rest.cci.VoidRecord getInstance() {
		return INSTANCE;
	}

	/* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.AbstractRecord#clone()
     */
    @Override
    public VoidRecord clone(
    ){
        return INSTANCE; 
    }

	/* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordName()
     */
    @Override
    public String getRecordName() {
        return NAME;
    }

    /**
     * Instead of the object we're on, we
     *  
     * @return the class variable INSTANCE
     */
    private Object readResolve(
    ){
    	return INSTANCE; 
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.rest.spi.AbstractMappedRecord#members()
	 */
	@Override
	protected org.openmdx.base.rest.spi.AbstractMappedRecord.Members<Member> members() {
		return MEMBERS;
	}

	
}
