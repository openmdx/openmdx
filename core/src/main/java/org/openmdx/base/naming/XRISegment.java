/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: XRI Segment 
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

import java.io.Serializable;
import java.util.List;

/**
 * This class represents an XRI segment
 */
public abstract class XRISegment implements Comparable<XRISegment>, Serializable {
	
	protected XRISegment(){
		super();
	}

	/**
	 * Implements {@code Serializable}
	 */
    private static final long serialVersionUID = -5111725167053688670L;

	public abstract String toClassicRepresentation();

	public abstract String toXRIRepresentation();

	public abstract boolean isPattern();

	protected abstract Object discriminant();

	/**
	 * The pattern {@code ($..)} matches any child
	 *
	 * @return the {@code ($..)} pattern
	 */
	public static XRISegment anyChildPattern() {
		return ClassicWildcardSegment.ANY_CHILD;
	}

	/**
	 * The pattern {@code ($...)} matches the object itself and any descendant
	 *
	 * @return the {@code ($...)} pattern
	 */
	public static XRISegment itselfAndAnyDescendantPattern() {
		return ClassicWildcardMultiSegment.ITSELF_AND_ANY_DESCENDENT;
	}

	public static List<XRISegment> parseDescendant(String descedants){
		final List<XRISegment> segments = new Path("@openmdx!-/" + descedants).getSegments();
		return segments.subList(1, segments.size());
	}

	static XRISegment valueOf(
		int index,	
		String classicRepresentation
	){
    	return
			parseAsClassicSegmentWildcard(classicRepresentation) ? new ClassicWildcardSegment(classicRepresentation) :
			parseAsTransactional(index, classicRepresentation) ? new TransactionalSegment(index, classicRepresentation) :
			parseAsClassicDescendantWildcard(classicRepresentation) ? new ClassicWildcardMultiSegment(classicRepresentation) :
			parseAsClassicCrossReference(classicRepresentation) ? new ClassicCrossReferenceSegment(classicRepresentation) :	
			parseAsSimpleSegment(classicRepresentation)	? (
				index == 0 ? new AuthoritySegment(classicRepresentation) : new PlainVanillaSegment(index, classicRepresentation)
			) : new GeneralSegment(index, classicRepresentation);
				
    }
    
    static XRISegment[] valueOf(String[] classicRepresentations) {
    	XRISegment[] pathComponent = new XRISegment[classicRepresentations.length];
    	for(int i = 0; i < classicRepresentations.length; i++){
    		pathComponent[i] = valueOf(i, classicRepresentations[i]);
    	}
    	return pathComponent;
    }
    
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return discriminant().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		return 
			that != null &&
			this.getClass() == that.getClass() &&
			this.discriminant().equals(((XRISegment)that).discriminant());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toClassicRepresentation();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(XRISegment that) {
		return this.toXRIRepresentation().compareTo(that.toXRIRepresentation());
	}

	public abstract boolean matches(XRISegment candidate);

	private static boolean parseAsClassicSegmentWildcard(String classicRepresentation) {
		return 
			classicRepresentation.startsWith(":") &&
			classicRepresentation.endsWith("*");
	}

	private static boolean parseAsClassicDescendantWildcard(String classicRepresentation) {
		return classicRepresentation.endsWith("%");
	}

	private static boolean parseAsTransactional(int index, String classicRepresentation) {
		return index == 0 ? (
			classicRepresentation.length() == 47 && 
			classicRepresentation.startsWith("!($t*uuid*") && 
			classicRepresentation.endsWith(")")
		) : (
			classicRepresentation.length() == 37 && 
			classicRepresentation.startsWith(":")
		);
	}
	
	protected static boolean parseAsClassicCrossReference(String classicRepresentation) {
		return classicRepresentation.indexOf('/') > 0 && classicRepresentation.indexOf('(') < 0;
	}

	private static boolean parseAsSimpleSegment(String classicRepresentation){
		for(int i = 0, l = classicRepresentation.length(); i < l; i++){
			if(!isPChar(classicRepresentation.charAt(i))){
				return false;
			}
		}
		return true;
	}
	
	static boolean isPChar(char c) {
        return 
        	Character.isLetterOrDigit(c) ||
        	// classic component separator
        	c == ':' ||
        	// xri-sub.delims
        	c == '&' || c == ';' || c == ',' || c == '\'' ||
        	// other iunreserved characters
            c == '-' || c == '.' || c == '_' || c == '~' || 
	        (0x000A0 <= c && c <= 0x0D7FF) || (0x0F900 <= c && c <= 0x0FDCF) || (0x0FDF0 <= c && c <= 0x0FFEF) ||
	        (0x10000 <= c && c <= 0x1FFFD) || (0x20000 <= c && c <= 0x2FFFD) || (0x30000 <= c && c <= 0x3FFFD) ||
	        (0x40000 <= c && c <= 0x4FFFD) || (0x50000 <= c && c <= 0x5FFFD) || (0x60000 <= c && c <= 0x6FFFD) ||
	        (0x70000 <= c && c <= 0x7FFFD) || (0x80000 <= c && c <= 0x8FFFD) || (0x90000 <= c && c <= 0x9FFFD) ||
	        (0xA0000 <= c && c <= 0xAFFFD) || (0xB0000 <= c && c <= 0xBFFFD) || (0xC0000 <= c && c <= 0xCFFFD) ||
	        (0xD0000 <= c && c <= 0xDFFFD) || (0xE1000 <= c && c <= 0xEFFFD);
		
	}
	
}
