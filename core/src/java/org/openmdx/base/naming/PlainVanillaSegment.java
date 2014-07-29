/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Plain Vanilla Segment 
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

/**
 * Plain Vanilla Path Component
 */
public class PlainVanillaSegment extends XRISegment {

	PlainVanillaSegment(
		int index, 
		String xriSegment
	) {
		this.unifiedRepresentation = stringShouldBeInternalized(index) ? xriSegment : xriSegment.intern();
	}

	private final String unifiedRepresentation;

	/**
	 * Implements <code>Serializable</code>
	 */
	private static final long serialVersionUID = -1614499628778417508L;
	
	/**
	 * Let's try to optimize String usage
	 */
	private static boolean stringShouldBeInternalized(int index) {
		return index < 5 || index % 2 == 0;
	}
	
	/* (non-Javadoc)
	 * @see org.openmdx.base.naming.PathComponent#discriminant()
	 */
	@Override
	protected String discriminant() {
		return this.unifiedRepresentation;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.naming.PathComponent#toClassicRepresentation()
	 */
	@Override
	public String toClassicRepresentation() {
		return this.unifiedRepresentation;
	}

	@Override
	public String toXRIRepresentation() {
		return this.unifiedRepresentation;
	}
	
	/* (non-Javadoc)
	 * @see org.openmdx.base.naming.PathComponent#isWildcard()
	 */
	@Override
	public boolean isPattern() {
		return false;
	}

	@Override
	public boolean matches(XRISegment candidate) {
		return this.equals(candidate);
	}

}
