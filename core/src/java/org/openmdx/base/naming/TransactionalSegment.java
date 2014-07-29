/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Transactional Segment 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2014, OMEX AG, Switzerland
 * All rights reserved.
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
package org.openmdx.base.naming;

import java.util.UUID;

/**
 * Transactional Segment
 */
public class TransactionalSegment extends XRISegment {

	TransactionalSegment(
		int index, 
		UUID transactionalObjectId
	) {
		this.transactionalObjectId = transactionalObjectId;
		this.contained = index > 0;
	}

	TransactionalSegment(
		int index, 
		String classicRepresentation
	) {
		this(index, index == 0 ? UUID.fromString(classicRepresentation.substring(10, 46)) : UUID.fromString(classicRepresentation.substring(1)));
		this.unifiedRepresentation = classicRepresentation;
	}
	
	private final UUID transactionalObjectId;
	private final boolean contained;
	private transient String unifiedRepresentation;
	
	/**
	 * Implements <code>Serializable</code>
	 */
	private static final long serialVersionUID = 9186604789391301906L;
	
	/* (non-Javadoc)
	 * @see org.openmdx.base.naming.PathComponent#discriminant()
	 */
	@Override
	protected UUID discriminant() {
		return this.transactionalObjectId;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.naming.PathComponent#toClassicRepresentation()
	 */
	@Override
	public String toClassicRepresentation() {
		return toXRIRepresentation();
	}
	
	/* (non-Javadoc)
	 * @see org.openmdx.base.naming.XRISegment#toXRIRepresentation()
	 */
	@Override
	public String toXRIRepresentation() {
		if(this.unifiedRepresentation == null) {
			this.unifiedRepresentation = contained ? (":" + transactionalObjectId) : ("!($t*uuid*" + transactionalObjectId + ")");
		}
		return this.unifiedRepresentation;
	}

	@Override
	public boolean isPattern() {
		return false;
	}

	@Override
	public boolean matches(XRISegment pattern){
		return false;
	}

	public UUID getTransactionalObjectId(){
		return this.transactionalObjectId;
	}

}
