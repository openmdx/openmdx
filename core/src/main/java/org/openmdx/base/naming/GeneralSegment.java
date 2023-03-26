/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: General XRI Segment 
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
package org.openmdx.base.naming;

/**
 * General XRI Segment
 */
public class GeneralSegment extends XRISegment {

	GeneralSegment(
	    int index,
		String classicRepresentation
	) {
		this.classicRepresentation = classicRepresentation;
		this.containsWildcard = classicRepresentation.contains("($.");
		this.authority = index == 0;
	}

	private final String classicRepresentation;
	private transient String xriRepresentation;
	private final boolean containsWildcard;
	private final boolean authority;

	/**
	 * Implements {@code Serializable}
	 */
	private static final long serialVersionUID = -5611747218232362282L;
	
	/* (non-Javadoc)
	 * @see org.openmdx.base.naming.PathComponent#discriminant()
	 */
	@Override
	protected String discriminant() {
		return this.classicRepresentation;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.naming.PathComponent#toClassicRepresentation()
	 */
	@Override
	public String toClassicRepresentation() {
		return this.classicRepresentation;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.naming.PathComponent#isWildcard()
	 */
	@Override
	public boolean isPattern() {
		return this.containsWildcard;
	}

	@Override
	public boolean matches(XRISegment candidate) {
		return 
			containsWildcard ? XRI_2Marshaller.segmentMatchesPattern(candidate.toClassicRepresentation(), this.toClassicRepresentation()) :
			this.equals(candidate);
	}

	@Override
	public String toXRIRepresentation() {
		if(this.xriRepresentation == null) {
			this.xriRepresentation = XRI_2Marshaller.xriRepresentationOfGeneralSegment(authority, classicRepresentation);
		}
		return this.xriRepresentation;
	}

}
