/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Path.java,v 1.28 2010/10/20 11:33:48 hburger Exp $
 * Description: Profile Path 
 * Revision:    $Revision: 1.28 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/10/20 11:33:48 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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

import java.io.Serializable;
import java.net.URI;
import java.util.Iterator;
import java.util.UUID;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.url.protocol.XRI_1Protocols;
import org.openmdx.kernel.url.protocol.XRI_2Protocols;
import org.openmdx.kernel.url.protocol.XriAuthorities;

/**
 * The Path class represents a data provider path.
 * 
 * The components of a name are numbered. The indexes of a
 * name with N components range from 0 up to, but not
 * including, N. This range may be written as [0,N). The
 * most significant component is at index 0. An empty name
 * has no components. 
 * 
 * None of the methods in this interface accept null as a
 * valid value for a parameter that is a name or a name
 * component. Likewise, methods that return a name or name
 * component never return null. 
 * 
 * An instance of a Path is not synchronized against
 * concurrent multithreaded access if that access is not
 * read-only.
 */
public final class Path
    implements Comparable<Path>, Cloneable, Serializable, Iterable<String>  
{

    /**
     * Implements <code>Externalizable</code>
     * 
     * @deprecated Do NOT use! 
     */
    @Deprecated
    public Path(
    ){        
        // Required for Externalizable and XMLEncoder
    }

    /**
     * Creates a new path object given by multiple path components.
     * The path is backed by the components list as long as it is
     * not modified by an add, addAll or remove method. 
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
        this.setComponents(components);
        this.xri = null;
    }

    /**
     * Constructor 
     *
     * @param components
     * @param size
     */
    private Path(
        String components,
        int size
    ) {
        this.components = components;
        this.size = size;
        this.xri = null;
    }

    /**
     * Creates a <code>Path</code> object.
     *
     * @param  path  The non-null string to parse.
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
            charSequence.startsWith(XriAuthorities.OPENMDX_AUTHORITY) ? IRI_2Marshaller.getInstance() :     
            charSequence.startsWith(XRI_1Protocols.OPENMDX_PREFIX) ? XRI_1Marshaller.getInstance() :
            charSequence.startsWith(URI_1.OPENMDX_PREFIX) ? URI_1.getInstance() : 
            LegacyMarshaller.getInstance()
        );
    }

    /**
     * Creates a <code>Path</code> object.
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
     *
     * @param transactionalObjectId
     */
    public Path(
        UUID transactionalObjectId
    ){
        this(
            new String[]{
                "!($t*uuid*" + transactionalObjectId + ")"
            }
        );
    }
        
    //-------------------------------------------------------------------------
    private void setComponents(
        String[] components
    ) {
        this.checkComponents(components);
        this.size = components.length;
        StringBuilder tmp = new StringBuilder();
        for(String component : components) {
            tmp.append(component).append(COMPONENT_SEPARATOR);
        }
        this.components = tmp.toString();
    }

    //-------------------------------------------------------------------------
    public String[] getComponents(
    ) {
        return this.components.length() == 0 ? 
            EMPTY_COMPONENTS :
            this.components.substring(0,this.components.length()-1).split(COMPONENT_SEPARATOR_STRING);
    }

    /**
     * Creates a <code>Path</code> object.
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
        try {
            this.setComponents(
                (String[])marshaller.unmarshal(charSequence)
            );
            this.xri = marshaller == XRI_2Marshaller.getInstance() ? charSequence : null;
        }  catch (ServiceException exception){
            throw new RuntimeServiceException(exception);
        }
    }

    /**
     * Creates a <code>Path</code> object.
     *
     * @param  that     The new path will consist of this name's components
     */ 
    public Path (
        Path that
    ){
        this.components = that.components;
        this.size = that.size;
        this.xri = that.xri;
    }

    //--------------------------------------------------------------------------
    // Verification
    //--------------------------------------------------------------------------

    /**
     * Checks the path components
     *
     * @exception   RuntimeServiceException
     *              if any of the components is null or empty
     */
    private void checkComponents(
        String[] components
    ){
        int size = components.length;
        for (
            int index = 0;
            index < size;
            index++
        ) {
            String component = components[index];
            if(
                (component == null) || 
                (component.length() == 0)
            ) {
                throw new RuntimeServiceException(
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.BAD_PARAMETER,
                    "A path component can neither be null nor empty",
                    new BasicException.Parameter("index",index),
                    new BasicException.Parameter("component",component)
                );
            }
        }   
    }

    /**
     * Checks the path's state
     *
     * @exception   RuntimeServiceException
     *              if the path is in read only state
     */
    private void checkState(
    ){
        if(this.readOnly) {
            throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.ILLEGAL_STATE,
                "This path is in read only state"
            );
        } else {
            this.xri = null;
        }
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
        return this.getPrefix(this.size - 1);
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
        return new Path(
            this.components + component + COMPONENT_SEPARATOR,
            this.size + 1
        );
    }

    /**
     * The component representation of a cross-reference path
     * 
     * @return the legacy path representation
     */
    String toComponent(){
        try {
            return LegacyMarshaller.getInstance().marshal(getComponents()).toString();
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
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
        Path crossReference
    ) {
        return new Path(
            this.components + crossReference.toComponent() + COMPONENT_SEPARATOR,
            this.size + 1
        );
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
        PathComponent component
    ){
        return this.getChild(component.toString());
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
        StringBuilder components = new StringBuilder(this.components);
        for(int i = 0; i < suffix.length; i++) {
            components.append(suffix[i]).append(COMPONENT_SEPARATOR);
        }        
        return new Path(
            components.toString(),
            this.size + suffix.length
        );
    }

    //--------------------------------------------------------------------------
    // Operations not modifying the path
    //--------------------------------------------------------------------------

    /**
     * Tests, whether the path contains a wildcard
     * 
     * @return <code>true</code> if the path contains any of the following XRI cross references<ul>
     * <li>$.
     * <li>$..
     * <li>$...
     * </ul>
     */
    public boolean containsWildcard(){
        String xri = toXRI();
        return 
            xri.indexOf("($.") >= 0 || 
            xri.indexOf("($\\.")  >= 0; // TODO to be removed together with the toResourcePattern() method
    }
    
    /**
     * Returns the base of the path. The base is the last component of a path.
     *
     * @return the base path component.
     *
     * @exception   ArrayIndexOutOfBoundsException
     *              if the path is empty
     */
    public String getBase(
    ){
        int pos = this.components.lastIndexOf(
            COMPONENT_SEPARATOR, 
            this.components.length() - 2
        );
        return this.components.substring(
            pos + 1,
            this.components.length() - 1
        );
    }

    /**
     * Returns the last component of a path.
     *
     * @return      the last path component.
     *
     * @exception   ArrayIndexOutOfBoundsException
     *              if the path is empty
     */
    public PathComponent getLastComponent(
    ) {
        return this.getComponent(this.size - 1); 
    }

    /**
     * Return the specified component
     *
     * @param   position    the 0-based index of the component to
     *                  retrieve. Must be in the range [0,size()).
     * 
     * @return  the component at index position
     * 
     * @exception   ArrayIndexOutOfBoundsException
     *              if position is outside the specified range
     */
    public PathComponent getComponent(
        int position
    ) {
        return new PathComponent(this.get(position)); 
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
            return URI_1.getInstance().marshal(this.getComponents()).toString();
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    /**
     * Generates a relative URI representation of this path which may safely be appended to another hierarchical URI.
     * An empty path is represented by <code>"@openmdx"</code>. 
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
     * @see toXRI()
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
        if(this.xri == null) try {
            this.xri = XRI_2Marshaller.getInstance().marshal(this.getComponents()).toString();
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
        return this.xri;
    }

    /**
     * Generates an escaped XRI 2 String representation of this path to be used with
     * <code>like</code> queries.
     * <p> 
     * The string can be passed to the Path constructor to create a new equivalent path
     * after replacing all escaped dots by plain dots.
     *
     * @return an escaped XRI 2 String representation of this path.
     * 
     * @deprecated will be removed as soon as the identity.isLike() pattern is no longer necessary
     */
    @Deprecated
    public String toResourcePattern(
    ){
        return toXri().replace(".", "\\."); 
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
     * Transient object id pattern (<code>xri://@openmdx!($t*uuid*&lt;uuid&gt;)</code>).
     * 
     * @return <code>true</code> if this path represents a transient object id
     */
    public boolean isTransientObjectId(){
        return 
            this.size == 1 && 
            this.components.startsWith("!($t*uuid*") &&
            this.components.length() == 48;
    }
    
    /**
     * Retrieve the transient object id represented by this path
     * 
     * @return the transient object id represented by this path
     */
    public UUID toUUID(
    ){
        if(isTransientObjectId()) {
            return UUIDConversion.fromString(this.components.substring(10, 46));
        } else {
            throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TRANSFORMATION_FAILURE,
                "This path does not represent a transient object id",
                new BasicException.Parameter("path", this)
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
     * @param       the non-null object to compare against.
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
        return this.components.compareTo(that.components);
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
     */
    public String get(
        int position
    ) {
        if((position < 0) || (position >= this.size)) {
            throw new ArrayIndexOutOfBoundsException("position not in 0.." + this.size);
        }
        int n = 0;
        int pos = 0;
        while(n < position) {
            pos = this.components.indexOf(COMPONENT_SEPARATOR, pos) + 1;
            n++;
        }
        return this.components.substring(
            pos,
            this.components.indexOf(COMPONENT_SEPARATOR, pos)
        );
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
        if (
            (position < 0) || 
            (position > this.size)
        ) {
            throw new ArrayIndexOutOfBoundsException(BAD_COMPONENT_NUMBER);
        }
        int n = 0;
        int pos = 0;
        while(n < position) {
            pos = this.components.indexOf(COMPONENT_SEPARATOR, pos) + 1;
            n++;
        }
        return new Path(
            this.components.substring(0, pos),
            position
        );
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
     */
    public String[] getSuffix(
        int position
    ){
        if (
            (position < 0) || 
            (position > this.size)
        ) {
            throw new ArrayIndexOutOfBoundsException(BAD_COMPONENT_NUMBER);
        }
        int n = 0;
        int len = this.components.length();
        int start = 0;
        if(position > 0) {
            for(int i = 0; i < len; i++) {
                if(this.components.charAt(i) == COMPONENT_SEPARATOR) {
                    n++;
                    if(n == position) {
                        start = i + 1;
                        break;
                    }
                }            
            }
        }
        if(position == n) {
            String suffix = this.components.substring(start);
            return suffix.length() == 0 ?
                EMPTY_COMPONENTS :
                    suffix.split(COMPONENT_SEPARATOR_STRING);            
        }
        throw new ArrayIndexOutOfBoundsException(BAD_COMPONENT_NUMBER);        
    }

    /**
     * Determines whether this path starts with a specified prefix.
     * A string array is a prefix if it is equal to getPrefix(prefix.length).
     * 
     * @param   prefix  the path to check
     * 
     * @return          true if components is a prefix of this path, false otherwise
     */
    public boolean startsWith(
        String... prefix
    ) {
        if(prefix.length > this.size) {
            return false;
        }
        int n = 0;
        int lastPos = 0;
        int len = this.components.length();
        for(int i = 0; i < len; i++) {
            if(this.components.charAt(i) == COMPONENT_SEPARATOR) {
                String p = prefix[n];
                if(!this.components.regionMatches(lastPos, p, 0, p.length())) {
                    return false;
                }
                lastPos = i + 1;
                n++;
            }            
        }
        return true;
    }

    /**
     * Determines whether this path starts with a specified prefix.
     * A path is a prefix if it is equal to getPrefix(prefix.size()).
     * 
     * @param   components  the path to check
     * 
     * @return  true if components is a prefix of this path, false otherwise
     */
    public boolean startsWith(
        Path prefix
    ) {
        return this.components.startsWith(prefix.components);
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
        int offset = size() - suffix.length;
        if (offset < 0) return false;
        for(
            int index = 0;
            index < suffix.length;
            index++
        ) {
            if(!suffix[index].equals(this.get(offset+index))) return false;
        }
        return true; 
    }

    /**
     * Adds the elements of a string array -- in order -- to the end of
     * this path.
     * 
     * @param       suffix
     *              the components to add
     * 
     * @return      the updated path (not a new one)
     * 
     * @exception   RuntimeServiceException
     *              if suffix is not a valid name, or if the
     *              addition of the components would violate the
     *              syntax rules of this path
     */
    public Path addAll(
        String... suffix
    ){
        return this.addAll(this.size, suffix);
    }

    /**
     * Adds the elements of a string array -- in order -- at a specified
     * position within this path. Components of this path at or
     * after the index of the first new component are shifted up
     * (away from 0) to accommodate the new components.
     * 
     * @param   components      the components to add
     * @param   position    the index in this path at which to add the
     *                  new components. Must be in the range
     *                  [0,size()].
     * 
     * @return  the updated path (not a new one)
     * 
     * @exception   ArrayIndexOutOfBoundsException
     *              if position is outside the specified range
     * @exception   RuntimeServiceException
     *              if components contains invalid path components
     */
    public Path addAll (
        int position,
        String... components
    ){
        this.checkState();
        int n = 0;
        int end = 0;
        if(position > 0) {
            int len = this.components.length();
            for(int i = 0; i < len; i++) {
                if(this.components.charAt(i) == COMPONENT_SEPARATOR) {
                    n++;
                }
                if(n == position) {
                    end = i + 1;
                    break;
                }
            }
        }
        if(n == position) {
            StringBuilder tmp = new StringBuilder(
                this.components.substring(0, end)
            );
            for(String c: components) {
                tmp.append(c).append(COMPONENT_SEPARATOR);
            }
            this.components = tmp.toString();
            this.size = position + components.length;
            return this;
        }
        throw new ArrayIndexOutOfBoundsException(BAD_COMPONENT_NUMBER + ": components=[" + this.components + "]; position=" + position);                
    }

    /**
     * Adds a single component to the end of this path.
     * 
     * @param   component   the component to add
     * 
     * @return  the updated path (not a new one)
     * 
     * @exception   RuntimeServiceException
     *              if adding component would violate the syntax
     *              rules of this path
     */
    public Path add(
        String component
    ){
        this.checkState();
        this.components += component + COMPONENT_SEPARATOR;
        this.size++;
        return this;
    }

    /**
     * Adds a single component to the end of this path.
     * 
     * @param       component
     *              the component to add
     * 
     * @return      the updated path (not a new one)
     *
     * @exception   RuntimeServiceException
     *              if the component is null or empty
     */
    public Path add(
        PathComponent component
    ){
        return this.add(component.toString());
    }

    /**
     * Adds a single component to the end of this path.
     * 
     * @param       crossReference
     *              the component to add
     * 
     * @return      the updated path (not a new one)
     *
     * @exception   RuntimeServiceException
     *              if the component is null or empty
     */
    public Path add(
        Path crossReference
    ){
        return this.add(crossReference.toComponent());
    }
    
    /**
     * Adds a single component at a specified position within
     * this path. Components of this path at or after the
     * index of the new component are shifted up by one (away
     * from index 0) to accommodate the new component.
     * 
     * @param   component   the component to add
     * @param   position    the index at which to add the new
     *                  component. Must be in the range
     *                  [0,size()].
     * 
     * @return  the updated path (not a new one)
     * 
     * @exception   ArrayIndexOutOfBoundsException
     *              if position is outside the specified range
     * @exception   RuntimeServiceException
     *              if adding component would violate the syntax
     *              rules of this path
     */
    public Path add(
        int position,
        String component
    ){
        return this.addAll(
            position, 
            component
        );
    }

    /**
     * Adds a single component at a specified position within
     * this path. Components of this path at or after the
     * index of the new component are shifted up by one (away
     * from index 0) to accommodate the new component.
     * 
     * @param       component
     *              the component to add
     * @param       position
     *              the index at which to add the new component.
     *              Must be in the range [0,size()].
     * 
     * @return      the updated path (not a new one)
     * 
     * @exception   ArrayIndexOutOfBoundsException
     *              if position is outside the specified range
     * @exception   RuntimeServiceException
     *              if the component is null or empty
     */
    public Path add(
        int position,
        PathComponent component
    ){
        return this.add(
            position, 
            component.toString()
        );
    }       
    
    /**
     * Adds a single component at a specified position within
     * this path. Components of this path at or after the
     * index of the new component are shifted up by one (away
     * from index 0) to accommodate the new component.
     * 
     * @param       crossReference
     *              the component to add
     * @param       position
     *              the index at which to add the new component.
     *              Must be in the range [0,size()].
     * 
     * @return      the updated path (not a new one)
     * 
     * @exception   ArrayIndexOutOfBoundsException
     *              if position is outside the specified range
     * @exception   RuntimeServiceException
     *              if the component is null or empty
     */
    public Path add(
        int position,
        Path crossReference
    ){
        return this.add(
            position, 
            crossReference.toComponent()
        );
    }       

    

    /**
     * Removes a component from this path. The component of
     * this path at the specified position is removed.
     * Components with indexes greater than this position
     * are shifted down (toward index 0) by one.
     * 
     * @param   position    the index of the component to remove.
     *                  Must be in the range [0,size()).
     * 
     * @return  the component removed (a String)
     * 
     * @exception   ArrayIndexOutOfBoundsException
     *              if position is outside the specified range
     * @exception   RuntimeServiceException
     *              if deleting the component would violate the
     *              syntax rules of the path
     */
    public String remove(
        int position
    ){
        this.checkState();
        int n = 0;
        int lastPos = 0;
        int len = this.components.length();
        for(int i = 0; i < len; i++) {
            if(this.components.charAt(i) == COMPONENT_SEPARATOR) {
                if(n == position) {
                    String component = this.components.substring(
                        lastPos,
                        i
                    );
                    this.components = 
                        this.components.substring(0, lastPos) +
                        this.components.substring(i + 1);
                    this.size--;
                    return component;
                }
                lastPos = i + 1;
                n++;           
            }
        }
        throw new ArrayIndexOutOfBoundsException(BAD_COMPONENT_NUMBER);                        
    }

    /**
     * Set this path to the same value as another one.
     * Subsequent changes to the components of this path will
     * not affect the other one, and vice versa.
     *
     * @param       source
     *              This path will have the same components as source.
     *
     * @exception   ArrayIndexOutOfBoundsException
     *              if position is outside the specified range
     * @exception   RuntimeServiceException
     *              if deleting the component would violate the
     *              syntax rules of the path
     */     
    public void setTo(
        Path source
    ) {
        this.checkState();
        this.components = source.components;
        this.size = source.size;
        this.xri = source.xri;
    }

    /**
     * Make this path object unmodifiable
     * 
     * @return the (now) unmodifiable path
     */
    public Path lock(
    ) {
        this.readOnly = true;
        return this;
    }

    /**
     * Test whether it is a cross reference or a path component pattern
     * 
     * @return <code>true</code> if it is a cross reference pattern
     */
    private boolean isCrossReferencePattern(
    ) {
        return this.components.indexOf("$.") >= 0;
    }

    /**
     * Determines whether the path corresponds to the pattern.
     * The following patterns are supported:<ul>
     * <li>The pattern component ":&lt;prefix&gt;*" matches the corresponding
     *     path component starting with &lt;prefix&gt;. The pattern component
     *     ":*" matches therefore the corresponding path component regardless
     *     of its content.
     * <li>Field "%" is only allowed as the last field of the pattern's last 
     *     path component and matches any number of fields and path components 
     *     regardless of their content.
     * <li>Cross reference pattern {@link Wildcards#isLike(org.openxri.XRIReference, org.openxri.XRIReference)}
     * </ul> 
     */
    public boolean isLike(
        Path pattern
    ){
        if(pattern.isCrossReferencePattern()) {
            return XRI_2Marshaller.isLike(
                this.toXRI(),
                pattern.toXRI()
            );
        } 
        else {
            int pos = 0;
            int posPattern = 0;
            int lenPattern = pattern.components.length();
            while(true)  {
                int index, starIndex;
                if(
                    (starIndex = pattern.components.indexOf(WILDCARD_COMPONENT_TERMINATOR, posPattern)) >= posPattern &&
                    (index = pattern.components.lastIndexOf(':', starIndex)) >= posPattern
                ) {
                    int lenRegion = index - posPattern;
                    if(!this.components.regionMatches(pos, pattern.components, posPattern, lenRegion)) {
                        return false;
                    }
                    pos += lenRegion;
                    lenRegion = starIndex - index - 1;
                    if(!this.components.regionMatches(pos, pattern.components, index + 1, lenRegion)) {
                        return false;
                    }
                    pos += lenRegion;
                    pos = this.components.indexOf(COMPONENT_SEPARATOR, pos) + 1;            
                    posPattern = starIndex + 2;
                }
                else if((index = pattern.components.indexOf(MATCHES_ALL_PATTERN, posPattern)) >= posPattern) {
                    int lenRegion = index - posPattern;
                    return this.components.regionMatches(pos, pattern.components, posPattern, lenRegion);                    
                }
                else {
                    int lenRegion = lenPattern - posPattern;
                    return 
                        (pos + lenRegion == this.components.length()) &&
                        this.components.regionMatches(pos, pattern.components, posPattern, lenRegion);
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    public boolean isPlaceHolder(
    ) {        
        return this.components.indexOf(PLACEHOLDER_COMPONENT) >= 0;
    }
    
    //--------------------------------------------------------------------------
    // Implements Iterable
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<String> iterator() {
        return new SegmentIterator();
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
     */
    @Override
    public Object clone(
    ) {   
        return new Path(this);
    }

    //--------------------------------------------------------------------------
    // Implements Serializable
    //--------------------------------------------------------------------------

    /**
     * Save the components of the <tt>Path</tt> instance to a stream (that
     * is, serialize it).
     *
     * @serialData The array containing the <tt>Path</tt> components is
     *             emitted.
     */
    private void writeObject(
        java.io.ObjectOutputStream stream
    ) throws java.io.IOException {
        stream.writeUTF(this.components);
        stream.writeInt(this.size);
    }

    /**
     * Reconstitute the <tt>Path</tt> instance from a stream (that is,
     * deserialize it).
     */
    private void readObject(
        java.io.ObjectInputStream stream
    ) throws java.io.IOException, ClassNotFoundException {
        this.components = stream.readUTF();
        this.size = stream.readInt();
    }

    //--------------------------------------------------------------------------
    // Extends Object
    //--------------------------------------------------------------------------

    /**
     * Provides the XRI representation of a path
     *
     * @return   the XRI 2 representation of a path
     */
    @Override
    public String toString(
    ){
        return LEGACY_STRING_REPRESENTATION ? toComponent() : toXRI(); 
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
        Object that
    ){
        return this == that || (
            that instanceof Path && this.components.equals(((Path)that).components)
        );
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
        return this.components.hashCode();
    }

    
    //--------------------------------------------------------------------------
    // Variables
    //--------------------------------------------------------------------------

    /**
     * The path's components
     */
    private transient String components;
    private transient int size;
    private transient String xri;
    private static char COMPONENT_SEPARATOR = '\u0009';
    private static String PLACEHOLDER_COMPONENT = COMPONENT_SEPARATOR + ":";
    private static String MATCHES_ALL_PATTERN = "%" + COMPONENT_SEPARATOR;
    private static String WILDCARD_COMPONENT_TERMINATOR = "*" + COMPONENT_SEPARATOR;
    private static final String COMPONENT_SEPARATOR_STRING = Character.toString(COMPONENT_SEPARATOR);
    private static final String[] EMPTY_COMPONENTS = {};
    private static final boolean LEGACY_STRING_REPRESENTATION = Boolean.TRUE; // avoid dead code warning

    /**
     * Defines, whether the path can be modified or not
     */
    private transient boolean readOnly = false;

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    /**
     * Serial Version UID
     */
    static final long serialVersionUID = 8827631310993135122L;

    /**
     * An error message in case the number of a component is outside the
     * allowed range.
     */
    final static private String BAD_COMPONENT_NUMBER =
        "The component number must be in the range [0,size()]";

    
    //--------------------------------------------------------------------------
    // Class SegmentIterator
    //--------------------------------------------------------------------------

    /**
     * Segment Iterator
     */
    class SegmentIterator implements Iterator<String> {

        /**
         * The next element's index
         */
        int nextIndex = 0;

        /**
         * The current element's index
         */
        int currentIndex = -1;
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return this.nextIndex < Path.this.size();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public String next() {
            return Path.this.get(
                this.currentIndex = this.nextIndex++
            );
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            if(this.currentIndex < 0) {
                throw new IllegalStateException("No current component");
            }
            Path.this.remove(this.currentIndex);
            this.currentIndex = -1;
        }        
        
    }

}
