/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Path 
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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.URI;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.xri.XRI_1Protocols;
import org.openmdx.kernel.xri.XRI_2Protocols;
import org.openmdx.kernel.xri.XRIAuthorities;

/**
 * The Path class represents an object's XRI.
 * <p>
 * The components of a name are numbered. The indices of a
 * name with N components range from 0 up to, but not
 * including, N. This range may be written as [0,N). The
 * most significant component is at index 0. An empty name
 * has no components. 
 * </p>
 * <p>
 * None of the methods in this interface accept null as a
 * valid value for a parameter that is a name or a name
 * component. Likewise, methods that return a name or name
 * component never return null. 
 * </p>
 * A path is immutable.
 */
public final class Path implements Comparable<Path>, Cloneable, Serializable {

	/**
	 * Empty path constructor
	 * 
	 * @deprecated use Path("") instead
	 */
	@Deprecated
	public Path(){
		this(0);
	}
	
    /**
     * Creates a new path object given by multiple path components.
     * 
     * @param components    path components
     *
     * @exception   RuntimeServiceException 
     *              in case of invalid components
     * @exception   NullPointerException 
     *              if components is null
     */
    public Path(
        String[] components
    ){
    	this(components.length, XRISegment.valueOf(components));
    }

	/**
     * Creates a new path object given by multiple path components.
     * 
     * @param components    path components
     *
     * @exception   RuntimeServiceException 
     *              in case of invalid components
     * @exception   NullPointerException 
     *              if components is null
     */
    public Path(
        XRISegment... components
    ){
    	this(components.length, components);
    }
    
    /**
     * Construct a path when the components are already parsed
     * 
     * @param components the path components, all components beyond size are ignored
     * @param size the size of the path
     */
    private Path(
		int size,
		XRISegment... components
    ) {
    	this(
    		size == 0 ? null : size == 1 ? ROOT : new Path(size - 1, components),
    		size == 0 ? null : components[size - 1]
    	);
    }
    
    /**
     * Creates a {@code Path} object.
     *
     * @param  charSequence  The non-null string to parse.
     * 
     * @exception   RuntimeServiceException
     *              in case of marshalling error
     */ 
    public Path (
        String charSequence
    ){
        this (
            charSequence, 
            charSequence.startsWith(XRI_2Protocols.OPENMDX_PREFIX) ? XRI_2Marshaller.getInstance() :
            charSequence.startsWith(XRIAuthorities.OPENMDX_AUTHORITY) ? IRI_2Marshaller.getInstance() :     
            charSequence.startsWith(XRI_1Protocols.OPENMDX_PREFIX) ? XRI_1Marshaller.getInstance() :
            charSequence.startsWith(URI_1Marshaller.OPENMDX_PREFIX) ? URI_1Marshaller.getInstance() : 
            LegacyMarshaller.getInstance()
        );
    }

    /**
     * Creates a {@code Path} object.
     *
     * @param  iri  The non-null IRI.
     * 
     * @exception   RuntimeServiceException
     *              in case of marshalling error
     */ 
    public Path (
        URI iri
    ){
        this (
            iri.toString(), 
            IRI_2Marshaller.getInstance()
        );
    }

    /**
     * Constructor 
     */
    public Path(
        UUID transactionalObjectId
    ){
    	this(ROOT, new TransactionalSegment(0, transactionalObjectId));
    }

    /**
     * Creates a {@code Path} object.
     *
     * @param   charSequence
     *          The non-null string to parse.
     * @param   marshaller
     *          the char sequence parser
     * 
     * @exception   RuntimeServiceException
     *              in case of marshalling error
     */ 
    private Path (
        String charSequence,
        Marshaller marshaller
    ){
    	this(getComponents(charSequence, marshaller));
    	if(marshaller == XRI_2Marshaller.getInstance()) {
    		this.xri = charSequence;
    	}
    }

    /**
     * Clones the given {@code Path}
     *
     * @param  that     The new path will consist of this name's components
     * 
     * @deprecated usually there is no need to clone a {@code Path}
     */ 
    @Deprecated
    public Path (
        Path that
    ){
    	this(that.parent, that.base);
    	this.xri = that.xri;
    }

    /**
     * Creates a child
     * 
     * @param parent the parent path
     * @param base the last component
     */
    private Path(
    	Path parent,
    	Path base
    ){
    	this.parent = parent;
    	this.base = new ClassicCrossReferenceSegment(base);
    	this.size = parent.size + 1;
    }
    
    /**
     * Creates a child
     * 
     * @param parent the parent path
     * @param base the last component
     */
    private Path(
    	Path parent,
    	XRISegment base
    ){
    	this.parent = parent;
    	this.base = base;
    	if(parent == null) {
    		this.xri = "xri://@openmdx";
    		this.size = 0;
    	} else {
    		this.size = parent.size + 1;
    	}
    }
    
    private final int size;
    private final Path parent;
    private final XRISegment base;
    
    private transient String xri;
    private transient int hash;

    private static final Path ROOT = new Path(new String[0]); // not a singleton in the current implementation!

	/**
	 * Implements {@code Serializable}
	 */
	private static final long serialVersionUID = -6970183208008259633L;

    /**
     * Parse the components with the given marshaller
     *
     * @return the components
     */
    private static String[] getComponents(
		String charSequence,
		Marshaller marshaller
	) {
    	try {
    		return (String[])marshaller.unmarshal(charSequence);
    	} catch (ServiceException exception) {
    		throw new RuntimeServiceException(exception);
    	}
    }

    /**
     * Retrieves the components in their classic representation
     * 
     * @return the components in their classic representation
     * 
     * @deprecated use {@link #getSegments()}
     */
    @Deprecated
    public String[] getComponents(
    ) {
    	String[] result = new String[this.size];
    	Path current = this;
    	for(int i = this.size; i > 0; current = current.parent) {
    		result[--i] = current.getLastSegment().toClassicRepresentation(); 
    	}
    	return result;
    }


    //--------------------------------------------------------------------------
    // Operations returning a new Path
    //--------------------------------------------------------------------------

    /**
     * Returns the parent path.
     *
     * @return the parent path.
     *
     * @exception   ArrayIndexOutOfBoundsException
     *              if the path is empty
     */
    public Path getParent(
    ) {
        return this.parent;
    }

    /**
     * Returns a child path
     *
     * @param       component 
     *              the component to be added
     *
     * @return      a new and longer path
     *
     * @exception   RuntimeServiceException
     *              if the component is null or empty
     */
    public Path getChild(
        String component
    ) {
        return getChild(XRISegment.valueOf(this.size, component));
    }

    /**
     * The component representation of a cross-reference path
     * 
     * @return the legacy path representation
     */
    public String toClassicRepresentation(){
        try {
            return LegacyMarshaller.getInstance().marshal(getComponents()).toString();
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }
    
    /**
     * Returns a child path
     *
     * @param       crossReference
     *              the component to be added
     *
     * @return      a new and longer path
     *
     * @exception   RuntimeServiceException
     *              if the component is null or empty
     */
    public Path getChild(
        Path crossReference
    ) {
        return new Path(
        	this,
        	crossReference
        );
    }
    
    /**
     * Returns a child path
     *
     * @param       xriSegment 
     *              the last XRI segment of the new Path
     *
     * @return      a new path with a size greater by one  
     *
     * @exception   NullPointerException if the xriSegment is {@code null}
     */
    public Path getChild(
    	XRISegment xriSegment
    ){
    	if(xriSegment == null) {
    		throw new NullPointerException("An XRI segment may not be null");
    	}
        return new Path(this, xriSegment);
    }
    
    /**
     * Returns a descendant path.
     *
     * @param       suffix
     *              the components to be added
     *
     * @return      the descendant path.
     *
     * @exception   RuntimeServiceException
     *              if any of the components is null or empty
     */
    public Path getDescendant(
        List<XRISegment> suffix
    ) {
    	Path result = this;
    	for(XRISegment component : suffix) {
    		result = result.getChild(component);
    	}
        return result;
    }

    /**
     * Returns a descendant path.
     *
     * @param       suffix
     *              the components to be added
     *
     * @return      the descendant path.
     *
     * @exception   RuntimeServiceException
     *              if any of the components is null or empty
     */
    public Path getDescendant(
        String... suffix
    ) {
    	Path result = this;
    	for(String component : suffix) {
    		result = result.getChild(component);
    	}
        return result;
    }

    //--------------------------------------------------------------------------
    // Operations not modifying the path
    //--------------------------------------------------------------------------

    /**
     * Tests, whether the path contains a wildcard
     * 
     * @return {@code true} if the path in XRI format contains any of the following XRI cross-references<ul>
     * <li>($.)
     * <li>($..)
     * <li>($...)
     * </ul>
     */
    public boolean isPattern(){
    	return 
    		this.base != null &&
    		(this.base.isPattern() || this.parent.isPattern());
    }
    
    /**
     * Returns the last segment of a path.
     *
     * @return  the last path segment.
     */
    public XRISegment getLastSegment(
	) {
    	return this.base; 
    }

    /**
     * Provides a specific XRI segment
     * 
     * @param index the index of the requested XRI segment
     *
     * @return the requested XRI segment
     */
    public XRISegment getSegment(
    	int index
	) {
    	validatePosition(index);
    	return getPrefix(index + 1).getLastSegment(); 
    }
    
    /**
     * Generates the URI representation of this path. An empty
     * path is represented by "spice:/". The string representation
     * thus generated can be passed to the Path constructor to create a
     * new equivalent path.
     *
     * @return   A non-null string representation of this path.
     * 
     * @deprecated use toURI()
     * 
     * @see #toURI()
     */
    @Deprecated
    public String toUri()
    {
        try {
            return URI_1Marshaller.getInstance().marshal(this.getComponents()).toString();
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    /**
     * Generates a relative URI representation of this path which may safely be appended to another hierarchical URI.
     * An empty path is represented by {@code "@openmdx"}. 
     * <p> 
     * The string representation thus generated can be passed to the 
     * Path constructor to create a new equivalent path.
     *
     * @return   A non-null string representation of this path.
     */
    public String toURI()
    {
    	URI iri = toIRI();
        String uri = iri.toASCIIString();
        if(uri.startsWith(XRI_2Protocols.SCHEME_PREFIX)) {
            return uri.substring(XRI_2Protocols.SCHEME_PREFIX.length());
        } else throw new RuntimeServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ASSERTION_FAILURE,
            "The IRI should start with the XRI scheme specification",
            new BasicException.Parameter("iri", iri)
        );
    }

    
    /**
     * Generates the XRI 1 representation of this path.
     * <p> 
     * The string representation thus generated can be passed to the 
     * Path constructor to create a new equivalent path.
     *
     * @return   A non-null string representation of this path.
     * 
     * @deprecated use toXRI()
     * @see #toXRI()
     */
    @Deprecated
    public String toXri()
    {
        try {
            return XRI_1Marshaller.getInstance().marshal(this.getComponents()).toString();
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    /**
     * Generates the XRI 2 String representation of this path.
     * <p> 
     * The string can be passed to the Path constructor to create a new equivalent path.
     *
     * @return   An XRI 2 String representation of this path.
     */
    public String toXRI(
    ){
        if(this.xri == null) {
        	final String baseRepresentation = this.base.toXRIRepresentation();
			this.xri = (
        		this.size == 1 ? (
        			baseRepresentation.startsWith("!") ? "xri://@openmdx" : "xri://@openmdx*"
        		) : (
        			this.parent.toXRI() + "/"
        		)
        	) + baseRepresentation;
        }
        return this.xri;
    }

    /**
     * Generates the XRI 2 based IRI representation of this path.
     * <ol>
     * <li>Percent-encode all percent "%" characters as "%25" across the entire XRI reference.
     * <li>Percent-encode all number sign "#" characters that appear within a cross-reference as "%23".
     * <li>Percent-encode all question mark "?" characters that appear within a cross-reference as "%3F".
     * <li>Percent-encode all slash "/" characters that appear within a cross-reference as "%2F".
     * </ol>
     * @return   An XRI 2 based IRI representation of this path.
     * 
     * @see #toAnyURI()
     */
    public URI toIRI(
    ){
        String xri = toXRI();
        StringBuilder iri = new StringBuilder(xri.length());
        int xRef = 0;
        for(
            int i = 0, limit = xri.length();
            i < limit;
            i++
        ){
            char c = xri.charAt(i);
            if(c == '%') {
                iri.append("%25");
            } else if (c == '(') {
                xRef++;
                iri.append(c);
            } else if (c == ')') {
                xRef--;
                iri.append(c);
            } else if (xRef == 0) {
                iri.append(c);
            } else if (c == '#') {
                iri.append("%23");
            } else if (c == '?') {
                iri.append("%3F");
            } else if (c == '/') {
                iri.append("%2F");
            } else {
                iri.append(c);
            }
        }
        return URI.create(iri.toString());
    }

    /**
     * Generates an AnyURI representation of this path.
     * <ol>
     * <li>Percent-encode all percent "%" characters as "%25" across the entire XRI reference.
     * <li>Percent-encode all number sign "#" characters that appear within a cross-reference as "%23".
     * <li>Percent-encode all question mark "?" characters that appear within a cross-reference as "%3F".
     * <li>Percent-encode all slash "/" characters that appear within a cross-reference as "%2F".
     * </ol>
     * @return   An AnyURI representation of this path.
     * 
     * @see #toIRI()
     */
    public URI toAnyURI(
    ){
        return toIRI();
    }
    
    /**
     * Transient object id pattern ({@code xri://@openmdx!($t*uuid*&lt;uuid&gt;)}).
     * 
     * @return {@code true} if this path represents a transient object id
     */
    public boolean isTransactionalObjectId(){
        return this.size == 1 && this.getLastSegment() instanceof TransactionalSegment;
    }
        
    /**
     * Retrieve the transactional object id represented by this path.
     * 
     * @return the transactional object id represented by this path
     */
    public UUID toTransactionalObjectId(
    ){
        if(isTransactionalObjectId()) {
            return ((TransactionalSegment)this.base).getTransactionalObjectId();
        } else {
            throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TRANSFORMATION_FAILURE,
                "This path does not represent a transient object id",
                new BasicException.Parameter(BasicException.Parameter.XRI, this)
            );
        }
    }
    
    
    //--------------------------------------------------------------------------
    // Implements Comparable
    //--------------------------------------------------------------------------

    /**
     * Compares this path with another path for order.
     * Returns a negative integer, zero, or a positive integer as this  
     * path is less than, equal to, or greater than the given path. 
     * 
     * @param       that the non-null object to compare against.
     * 
     * @return      a negative integer, zero, or a positive integer as this 
     *              path is less than, equal to, or greater than the given 
     *              path

     * @exception   ClassCastException
     *              if obj is not an instance of Path
     */
    public int compareTo(
        Path that
    ) {
        final Path left;
        final Path right;
        final int toCompare;
        if(this.size == that.size) {
            toCompare = this.size;
            left = this;
            right = that;
        } else if (this.size > that.size) {
            toCompare = that.size;
            left = this.getPrefix(toCompare);
            right = that;
        } else { // this.size < that.size
            toCompare = this.size;
            left = this;
            right = that.getPrefix(toCompare);
        }
        if(toCompare > 1) {
            int value = left.parent.compareTo(right.parent);
            if(value != 0) return value;
        } 
        if(toCompare > 0) {
            int value = left.base.compareTo(right.base);
            if(value != 0) return value;
        }
        return this.size - that.size;
    }

    //--------------------------------------------------------------------------
    // Similar to Name
    //--------------------------------------------------------------------------

    /**
     * Returns the number of path components for this path.
     *
     * @return the number of path components represented by this object.
     */
    public int size()
    {
        return this.size;
    }

    /**
     * Determines whether this path is empty.
     * An empty path is one with zero components.
     *
     * @ return true if the path is empty
     */
    public boolean isEmpty()
    {
        return this.size == 0;
    }

    /**
     * Retrieves a component of this path.
     * 
     * @param   position    the 0-based index of the component to
     *                  retrieve. Must be in the range [0,size()).
     * 
     * @return  the component at index position
     * 
     * @exception   ArrayIndexOutOfBoundsException
     *              if position is outside the specified range
     * 
     * @deprecated use {@link #getSegment(int)}.toClassicRepresentation()            
     */
    @Deprecated
    public String get(
        int position
    ) {
    	return getSegment(position).toClassicRepresentation();
    }

    /**
     * Creates a path whose components consist of a prefix of
     * the components of this path. Subsequent changes to this
     * path will not affect the path that is returned and vice versa.
     * 
     * @param   position    the 0-based index of the component at which
     *                  to stop. Must be in the range [0,size()].
     * 
     * @return  a path consisting of the components at indexes in the
     *          range [0,position).
     * 
     * @exception   ArrayIndexOutOfBoundsException
     *              if position is outside the specified range
     */
    public Path getPrefix(
        int position
    ){
        validateSize(position);
        Path cursor = this;
        while(position < cursor.size) {
        	cursor = cursor.parent;
        }
        return cursor;
    }

    /**
     * Creates a suffix of the components in this path.
     * Subsequent changes to this path do not affect the string array that is
     * returned and vice versa.
     * 
     * @param   position    the 0-based index of the component at which
     *                  to start. Must be in the range [0,size()].
     * 
     * @return          a string array consisting of the components at indices
     *                  in the range [position,size()). If position is equal to
     *                  size(), an empty array is returned.
     * 
     * @exception       ArrayIndexOutOfBoundsException
     *                  if position is outside the specified range
     *                  
     * @deprecated use {@link #getSegments()}.subList(position,{@link #size()}).toArray(new String[{@link #size()}-position])
     */
    @Deprecated
    public String[] getSuffix(
        int position
    ){
    	validateSize(position);
    	final int length = this.size - position;
		final String[] suffix = new String[length];
    	int i = length;
    	for(Path cursor = this; i > 0; cursor = cursor.parent) {
    		suffix[--i] = cursor.getLastSegment().toClassicRepresentation();
    	}
    	return suffix;
    }
    
    public List<XRISegment> getSegments(){
    	return new SegmentList();
    }

    private void validatePosition(int position) {
    	if (position < 0 || position >= this.size) {
    		throw new ArrayIndexOutOfBoundsException("The component number must be in the range [0,size())");
    	}
    }
    
    private void validateSize(int position) {
    	if (position < 0 || position > this.size) {
    		throw new ArrayIndexOutOfBoundsException("The component number must be in the range [0,size()]");
    	}
    }
    
    
    /**
     * Determines whether this path starts with a specified prefix.
     * A string array is a prefix if it is equal to getPrefix(prefix.length).
     * 
     * @param   prefix  the path to check
     * 
     * @return          true if components is a prefix of this path, false otherwise
     * 
     * @deprecated use {@link #startsWith(Path)}
     */
    @Deprecated
    public boolean startsWith(
        String... prefix
    ) {
    	if(prefix.length > size){
    		return false;
    	}
    	if(prefix.length < size) {
    		return getPrefix(prefix.length).startsWith(prefix);
    	}
    	return Arrays.equals(prefix, getComponents()); // TODO optimize
    }

    /**
     * Determines whether this path starts with a specified prefix.
     * A path is a prefix if it is equal to getPrefix(prefix.size()).
     * 
     * @param   prefix  the path to check
     * 
     * @return  true if components is a prefix of this path, false otherwise
     */
    public boolean startsWith(
        Path prefix
    ) {
        return prefix.size <= this.size && prefix.equals(getPrefix(prefix.size));
    }

    /**
     * Determines whether this path ends with a specified suffix. 
     * A string array is a suffix if it is equal to 
     * getSuffix(size()-suffix.size()).
     * 
     * @param   suffix  the string array to check
     * 
     * @return  true if suffix is a suffix of this path, false otherwise
     */
    public boolean endsWith(
        String... suffix
    ) {
    	// TODO optimize
        int offset = size() - suffix.length;
        if (offset < 0) return false;
        for(
            int index = 0;
            index < suffix.length;
            index++
        ) {
            if(!suffix[index].equals(this.getSegment(offset+index).toClassicRepresentation())) return false;
        }
        return true; 
    }

    
    //--------------------------------------------------------------------------
    // XRI like
    //--------------------------------------------------------------------------

    /**
     * Make this path object unmodifiable
     * 
     * @return the (now) unmodifiable path
     * 
     * @deprecated no need to lock anymore: proxy paths must not be locked and
     * all other paths are read-only
     */
    @Deprecated
    public Path lock(
    ) {
        return this;
    }

    /**
     * Test whether it is a cross-reference or a path component pattern
     * 
     * @return {@code true} if it is a cross-reference pattern
     */
    private boolean isCrossReferencePattern(
    ) {
    	return (
    		this.base instanceof GeneralSegment &&
    		this.base.isPattern()
    	) || (
    		this.parent != null &&
			this.parent.isCrossReferencePattern()
    	);
    }

    /**
     * Determines whether the path corresponds to the pattern.
     * The following patterns are supported:<ul>
     * <li>The pattern component ":&lsaquo;prefix&rsaquo;*" matches the corresponding
     *     path component starting with &lsaquo;prefix&rsaquo;. The pattern component
     *     ":*" matches therefore the corresponding path component regardless
     *     of its content.
     * <li>Field "%" is only allowed as the last field of the pattern's last 
     *     path component and matches any number of fields and path components 
     *     regardless of their content.
     * <li>Cross-reference pattern
     * </ul> 
     */
    public boolean isLike(
        Path pattern
    ){
        if(pattern.isCrossReferencePattern()) {
            return XRI_2Marshaller.pathMatchesPattern(
                this.toXRI(),
                pattern.toXRI()
            );
        } 
        // Match Classic Wildcards
        final XRISegment patternBase;
        if(pattern.size > size) {
        	if(!(
        		pattern.size == size + 1 &&
        		pattern.base instanceof ClassicWildcardMultiSegment &&
        		((ClassicWildcardMultiSegment)pattern.base).discriminant().isEmpty()
        	)) {
        		return false;
        	}
        	patternBase = pattern.parent.base;
        } else if (this.size == 0){
        	return true;
        } else {
    		patternBase = pattern.base;
        }
        final int nextSize;
        if(pattern.size < this.size) {
        	nextSize = pattern.size; 
        	if(
        		!(patternBase instanceof ClassicWildcardMultiSegment) ||
        		!patternBase.toClassicRepresentation().endsWith("%")
        	) {
        		return false;
        	}
        } else {
	        nextSize = this.size - 1; 
	        if(!patternBase.matches(this.base)){
	        	return false;
	        }
        }
        return this.getPrefix(nextSize).isLike(pattern.getPrefix(nextSize));
    }

    public boolean isPlaceHolder(
    ) {        
        return this.parent == null || this.getLastSegment() instanceof TransactionalSegment || this.parent.isPlaceHolder();
    }
    
    /**
     * Tests whether the path refers to an object
     *
     * @return {@code true} if the path refers to an object
     */
    public boolean isObjectPath(){
        return (this.size & 1) == 1;
    }
    
    /**
     * Tests whether the path refers to a container
     *
     * @return {@code true} if the path refers to a container
     */
    public boolean isContainerPath(){
        return this.size % 2 == 0;
    }

    //--------------------------------------------------------------------------
    // Implements Serializable
    //--------------------------------------------------------------------------
    
    private Object readResolve() throws ObjectStreamException {
    	return this.parent == null && this.base == null ? ROOT : this; 
    }
    
    
    //--------------------------------------------------------------------------
    // Implements Cloneable
    //--------------------------------------------------------------------------

    /**
     * Generates a new copy of this path.
     * Subsequent changes to the components of this path will
     * not affect the new copy, and vice versa.
     *
     * @return    a clone of this instance.
     * 
     * @deprecated usually there is no need to clone a path
     */
    @Override
    @Deprecated
    public Object clone(
    ) {   
        return new Path(this);
    }

    //--------------------------------------------------------------------------
    // Extends Object
    //--------------------------------------------------------------------------

    /**
     * Provides legacy representation of a path
     *
     * @return legacy representation of a path
     */
    @Override
    public String toString(
    ){
        return toXRI(); 
    }

    /**
     * Indicates whether some other object is "equal to" this one. 
     *
     * @param   object - the reference object with which to compare.
     *
     * @return  true if this object is the same as the object argument;
     *          false otherwise.
     */
    @Override
    public boolean equals(
        Object object
    ){
    	if(this == object) {
    		return true;
    	}
    	if(object instanceof Path) {
    		Path that = (Path) object;
    		if(this.size == that.size) {
    			if(this.size == 0) {
    				return true;
    			}
    			if(this.base.equals(that.base)) {
    			    return 
    			        this.parent == that.parent ||
    			        this.parent.equals(that.parent);
    			}
    		}
    	}
		return false;
    }

    /**
     * Returns the hash code value for this path. 
     * <p>
     * This ensures that path1.equals(path2) implies that 
     * path1.hashCode()==path2.hashCode() for any two paths, path1 and path2,
     * as required by the general contract of Object.hashCode.
     *
     * @return the path's hash code
     */
    @Override
    public int hashCode(
    ) {
    	int h = hash;
    	if (h == 0 && size > 0) {
    		h = 31 * parent.hashCode() + base.hashCode();
    		if(! isPlaceHolder()) {
    			hash = h;
    		}
    	}	
        return h;
    }

    
    //--------------------------------------------------------------------------
    // Class Segment List
    //--------------------------------------------------------------------------

    class SegmentList extends AbstractList<XRISegment> {
        
		@Override
		public XRISegment get(int index) {
			return getSegment(index);
		}

		@SuppressWarnings("synthetic-access")
        @Override
		public int size() {
			return Path.this.size;
		}
    	
    }
    
}
