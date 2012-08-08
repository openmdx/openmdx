/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Path.java,v 1.29 2008/04/25 00:51:25 hburger Exp $
 * Description: Profile Path 
 * Revision:    $Revision: 1.29 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/25 00:51:25 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.compatibility.base.naming;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.DataInput;
import org.openmdx.base.io.DataOutput;
import org.openmdx.base.io.Externalizable;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.uri.scheme.OpenMDXSchemes;
import org.openxri.XRI;


/**
 * The Path class reperesents a data provider path.
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
@SuppressWarnings("unchecked")
public final class Path
    implements Comparable, Cloneable, Serializable, Externalizable  
{
    
    /**
     * Do NOT use! Required for Externalizable.
     * 
s    */
    public Path(
    ) {        
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
        this.components = components;
        checkComponents();
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
        String      charSequence
    ){
        this (
            charSequence, 
            charSequence.startsWith(OpenMDXSchemes.URI_PREFIX) ? 
                UriMarshaller.getInstance() : 
            charSequence.startsWith("xri:@openmdx") ?
                XRI_1Marshaller.getInstance() :
            charSequence.startsWith("xri://@openmdx") ?
                XRI_2Marshaller.getInstance() :
                PathMarshaller.getInstance()
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
            IriMarshaller.getInstance()
        );
    }
    
    /**
     * Creates a <code>Path</code> object.
     *
     * @param   charSequence
     *          The non-null string to parse.
     * @param   uri
     *          tells whether the charSequence is to be interpreted as URI or
     *          not.
     * 
     * @exception   RuntimeServiceException
     *              in case of marshalling error
     */ 
    private Path (
        String      charSequence,
        Marshaller  marshaller
    ){
        try {
            this.components = (String[])marshaller.unmarshal(charSequence); 
        } catch (ServiceException exception){
            throw new RuntimeServiceException(exception);
        }
        checkComponents();
    }

    /**
     * Creates a <code>Path</code> object.
     *
     * @param  path     The new path will consist of this name's components
     */ 
    public Path (
        Path path
    ){
        this.components = path.components;
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
    private Path checkComponents(
    ){
        int size = this.size();
        for (
            int index = 0;
            index < size;
            index++
        ) {
            String component = this.components[index];
            if(
                component == null || 
                component.length() == 0
            ) throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.BAD_PARAMETER,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("index",index),
                    new BasicException.Parameter("component",component)
                },
                "A path component can neither be null nor empty"
            );
        }   
        return this;
    }

    /**
     * Checks the path's state
     *
     * @exception   RuntimeServiceException
     *              if the path is in read only state
     */
    private void checkState(
    ){
        if (this.readOnly) throw new RuntimeServiceException(
            BasicException.Code.DEFAULT_DOMAIN, 
            BasicException.Code.ILLEGAL_STATE,
            null,
            "This path is in read only state"
        );
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
    ){
        return getPrefix(size()-1);
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
    ){
    int size = this.size();
        String[] components = new String[size+1];
        System.arraycopy(
            this.components, 0, 
            components, 0, 
            size
        );
        components[size] = component;
        return new Path(components);
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
        return getChild(component.toString());
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
    ){
    int size = this.size();
        String[] components = new String[size+suffix.length];
        System.arraycopy(
            this.components, 0, 
            components, 0, 
            size
        );
        System.arraycopy(
            suffix, 0, 
            components, size, 
            suffix.length
        );
        return new Path(components);
    }


    //--------------------------------------------------------------------------
    // Operations not modifying the path
    //--------------------------------------------------------------------------
    
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
        return this.components[this.components.length-1]; 
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
    ){
        return getComponent(size()-1); 
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
    ){
        return new PathComponent(get(position)); 
    }

    /**
     * Return list of path components.
     */
    public String[] getComponents(
    ) {
        return this.components;
    }
    
    /**
     * Generates the URI representation of this path. An empty
     * path is represented by "spice:/". The string representation
     * thus generated can be passed to the Path constructor to create a
     * new equivalent path.
     *
     * @return   A non-null string representation of this path.
     */
    public String toUri()
    {
        try {
            return UriMarshaller.getInstance().marshal(this.components).toString();
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    /**
     * Generates the XRI 1 representation of this path.
     * <p> 
     * The string representation thus generated can be passed to the 
     * Path constructor to create a new equivalent path.
     *
     * @return   A non-null string representation of this path.
     */
    public String toXri()
    {
        try {
            return XRI_1Marshaller.getInstance().marshal(this.components).toString();
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    /**
     * Generates the XRI 2 representation of this path.
     * <p> 
     * The string representation of the generated XRI can be passed to the 
     * Path constructor to create a new equivalent path.
     *
     * @return   An XRI 2 representation of this path.
     */
    public XRI toXRI(
    ){
        try {
            return new XRI(XRI_2Marshaller.getInstance().marshal(this.components).toString());
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }
    
    /**
     * Generates the XRI 2 based IRI representation of this path.
     *
     * @return   An XRI 2 based IRI representation of this path.
     */
    public URI toIRI(
    ){
        try {
            return new URI(toXRI().toIRINormalForm());
        } catch (URISyntaxException exception) {
            throw new RuntimeServiceException(exception);
        }
    }
    
    /**
     * Generates an URI refercence for this Path and the given fragment
     * identifier
     *
     * @param   fragmentIdentifier
     *          The fragment identifier
     *
     * @return  A non-null URI reference with the given fragment identifier
     *          or the the paths' URI if the fragemtn identifier is null.
     *          
     */
    public String getUriReference(
        String fragmentIdentifier
    ){
        return fragmentIdentifier == null ?
            toUri() :
            toUri() + '#' + fragmentIdentifier;
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
    public int compareTo(Object obj) {
        Path that = (Path) obj;
        int limit = this.size() < that.size() ? this.size() : that.size();
        for (
            int cursor = 0;
            cursor < limit;
            cursor++
        ) {
            int result = this.get(cursor).compareTo(that.get(cursor));
            if (result != 0) return result;
        }
        return this.size() - that.size();
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
        return this.components.length;
    }
    
    /**
     * Determines whether this path is empty.
     * An empty path is one with zero components.
     *
     * @ return true if the path is empty
     */
    public boolean isEmpty()
    {
        return size() == 0;
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
        return this.components[position];
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
            position < 0 || 
            position > size()
        ) throw new ArrayIndexOutOfBoundsException(BAD_COMPONENT_NUMBER);
        String[] components = new String[position];
        System.arraycopy(
            this.components, 0,
            components, 0,
            position
        );
        return new Path(components);
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
    int size = this.size();
        if (
            position < 0 || 
            position > size
        ) throw new ArrayIndexOutOfBoundsException(BAD_COMPONENT_NUMBER);
        String[] components = new String[size-position];
        System.arraycopy(
            this.components, position,
            components, 0,
            size-position
        );
        return components;
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
        if(prefix.length > size()) return false;
        for(
            int index = 0;
            index < prefix.length;
            index++
        ) if(! prefix[index].equals(this.get(index))) return false;
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
        return startsWith(prefix.components);
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
        ) if(! suffix[index].equals(this.get(offset+index))) return false;
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
        return addAll(size(), suffix);
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
        checkState();
    int size = this.size();
        String[] buffer = new String[size+components.length];
        System.arraycopy(
            this.components, 0,
            buffer, 0,
            position
        );
        System.arraycopy(
            components, 0,
            buffer, position,
            components.length
        );
        System.arraycopy(
            this.components, position,
            buffer, position+components.length,
            size-position
        );
        this.components = buffer;       
        return checkComponents();
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
        return add (size(), component);
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
        return add (size(), component);
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
        checkState();
        int size = this.size();
        String[] components = new String[size+1];
        System.arraycopy(
            this.components, 0,
            components, 0,
            position
        );
        components[position] = component;
        System.arraycopy(
            this.components, position,
            components, position+1,
            size-position
        );
        this.components = components;
        return checkComponents();
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
        return add (position, component.toString());
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
        checkState();
    int size = this.size();
        String  component = this.components[position];
        String[]  components = new String [size-1];
        System.arraycopy(
            this.components, 0,
            components, 0,
            position
        );
        System.arraycopy(
            this.components, position+1,
            components, position,
            size-position-1
        );
        this.components = components;
        return component;
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
    ){
          checkState();
      this.components = source.components;
    }

    /**
     * Make this path object unmodifiable
     */
    public void lock(
    ){
        this.readOnly = true;
    }
    
    /**
     * Determines whether the path corresponds to the pattern.
     * The following patterns are supported:
     * - The pattern component ":<prefix>*" matches the corresponding
     *   path component starting with <prefix>. The pattern component
     *   ":*" matches therefore the corresponding path component regardless
     *   of its content.
     * - Field "%" is only allowed as the last field of the pattern's last 
     *   path component and matches any number of fields and path components 
     *   regardless of their content. 
     */
    public boolean isLike(
        Path pattern
    ){
        // Determine the components amount to be checked.
        int checkIndex;
        if(pattern.components[pattern.components.length - 1].equals("%")) {
            checkIndex = pattern.components.length - 1;
            if(checkIndex > this.components.length) return false;
        } else if(pattern.components[pattern.components.length - 1].endsWith("%")) {
            checkIndex = pattern.components.length;
            if(checkIndex > this.components.length) return false;
        } else {
            checkIndex = pattern.components.length;
            if(pattern.components.length != this.components.length) return false;
        }        
        for(
            int index = 0;
            index < checkIndex;
            index++
        ) {
            String patternComponent = pattern.components[index];
            if(patternComponent.endsWith("%")) {
                String prefix = patternComponent.substring(0, patternComponent.length() - 1);
                if(!this.components[index].startsWith(prefix)) return false;
            } else if(patternComponent.startsWith(":") && patternComponent.endsWith("*")) {
                if(patternComponent.length() == 2) {                
	                // The pattern ":*" matches everything
                } else {
                    // Check for ":<prefix>*".
                    String prefix = patternComponent.substring(1, patternComponent.length() - 1);
                    if(!this.components[index].startsWith(prefix)) return false;
                }
            } else {
                // In this case the components should fully match.
                if(!patternComponent.equals(this.components[index])) return false;
            }
        }
        return true;
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
    public Object clone()  
    {   
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
        stream.writeObject(this.components);
    }

    /**
     * Reconstitute the <tt>Path</tt> instance from a stream (that is,
     * deserialize it).
     */
    private void readObject(
        java.io.ObjectInputStream stream
    ) throws java.io.IOException, ClassNotFoundException {
        this.components = (String[])stream.readObject();
    }

    //--------------------------------------------------------------------------
    // Externalizable
    //--------------------------------------------------------------------------
    
    //--------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.io.Externalizable#readExternal(org.openmdx.base.io.DataInput)
     */
    public void readExternal(
        DataInput in
    ) throws IOException {
        this.components = in.readStrings();
    }

    //--------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.io.Externalizable#writeExternal(org.openmdx.base.io.DataOutput)
     */
    public void writeExternal(
        DataOutput out
    ) throws IOException {
        out.writeStrings(this.components);
    }

    //--------------------------------------------------------------------------
    // Extends Object
    //--------------------------------------------------------------------------

    /**
     * Generates the string representation of this path. An empty
     * path is represented by an empty string. The string representation
     * thus generated can be passed to the Path constructor to create a
     * new equivalent path.
     *
     * @return   A non-null string representation of this path.
     */
    public String toString()
    {
        try {
            return String.valueOf(
              PathMarshaller.getInstance().marshal(this.components)
            );
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    /**
     * Indicates whether some other object is "equal to" this one. 
     *
     * @param   object - the reference object with which to compare.
     *
     * @return  true if this object is the same as the object argument;
     *          false otherwise.
     */
    public boolean equals(
        Object that
    ){
        return this == that || (
          that != null && that instanceof Path && Arrays.equals(
            this.components,
            ((Path)that).components
          )
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
    public int hashCode(
    ){
        int hashCode = 1;
    int size = this.size();
        for(
            int index = 0;
            index < size; 
            index++
        ) hashCode = 31 * hashCode + this.components[index].hashCode();
        return hashCode;
    }

    //--------------------------------------------------------------------------
    // Static methods
    //--------------------------------------------------------------------------

    /**
     * Store a list's values in a Path array. 
     *
     * @return a Path array with the list's values
     *   
     * @exception   ClassCastException
     *              If any of the values is not an instance of Path
     */
    public static Path[] toPathArray(
        List source
    ){
        return source == null ?
            null:
            (Path[])source.toArray(new Path[source.size()]);
    }


    //--------------------------------------------------------------------------
    // Variables
    //--------------------------------------------------------------------------

    /**
     * The path's components
     */
    private transient String[] components;

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
     * A path's URI scheme
     * 
     * @deprecated use org.openmdx.kernel.uri.scheme.OpenMDXSchemes.URI_SCHEME
     */
    public final static String URI_SCHEME = OpenMDXSchemes.URI_SCHEME;
    
    /**
     * An error message in case the number of a component is outside the
     * allowed range.
     */
    final static private String BAD_COMPONENT_NUMBER =
        "The component number must be in the range [0,size()]";

    
}
