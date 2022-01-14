/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Classic Cross Reference Segment 
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
 * Classic Cross Reference Segment
 */
public class ClassicCrossReferenceSegment extends XRISegment {

	ClassicCrossReferenceSegment(
		Path crossReference
	) {
		this.crossReference = crossReference;
	}

	ClassicCrossReferenceSegment(
		String classicRepresentation
	) {
		this(new Path(classicRepresentation));
		this.classicRepresentation = classicRepresentation;
	}
	
	private final Path crossReference;
	private transient String classicRepresentation;
	private transient String xriRepresentation;

	/**
	 * Implements <code>Serializable</code>
	 */
	private static final long serialVersionUID = -1225320959690566317L;
	
	/* (non-Javadoc)
	 * @see org.openmdx.base.naming.PathComponent#discriminant()
	 */
	@Override
	protected Path discriminant() {
		return this.crossReference;
	}
	
	/* (non-Javadoc)
	 * @see org.openmdx.base.naming.PathComponent#toClassicRepresentation()
	 */
	@Override
	public String toClassicRepresentation() {
		if(this.classicRepresentation == null) {
			this.classicRepresentation = crossReference.toClassicRepresentation();
		}
		return this.classicRepresentation;
	}

	@Override
	public String toXRIRepresentation() {
		if(this.xriRepresentation == null) {
			final String crossReference = this.crossReference.toXRI();
			this.xriRepresentation = "(" + crossReference.subSequence(6, crossReference.length()) + ")";
		}
		return this.xriRepresentation;
	}
	
	/* (non-Javadoc)
	 * @see org.openmdx.base.naming.PathComponent#isWildcard()
	 */
	@Override
	public boolean isPattern() {
		return crossReference.isPattern();
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.naming.PathComponent#matches(org.openmdx.base.naming.PathComponent)
	 */
	@Override
	public boolean matches(XRISegment candidate) {
		return 
			candidate instanceof ClassicCrossReferenceSegment &&
			((ClassicCrossReferenceSegment)candidate).discriminant().isLike(discriminant());
	}

}
