/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: A Path COmponent 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;

/**
 * The PathComponent class reperesents a path component.
 * 
 * The field of a path component are numbered. The indexes of a
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
 * An instance of a PathComponent is not synchronized against
 * concurrent multithreaded access if that access is not
 * read-only.
 */
public final class PathComponent
	implements Comparable<PathComponent>, Cloneable, Serializable
{

    /**
     * Creates a new path component given by multiple fields.
     * The path component is backed by the field list as long as it is
     * not modified by an add, addAll or remove method. 
     * 
     * @param fields    path component fields
     *
     * @exception   RuntimeServiceException 
     *              in case of invalid components
     * @exception   NullPointerException 
     *              if components is null
     */
    public PathComponent(
        String[] fields
    ){
        this.fields = fields;
        checkFields();
    }

    /**
     * Creates a <code>Path</code> object.
     *
     * @param  path  The non-null string to parse.
     * 
     * @exception   RuntimeServiceException
     *              in case of marshalling error
     */
    public PathComponent (
        String fields
    ){
        this(parse (fields));
    }

    /**
     * Creates a <code>PathComponent</code> object.
     *
     * @param  name     The path will consist of this components's fields
     */
    public PathComponent (
        PathComponent component
    ){
        this.fields = component.fields;
    }

    
    //------------------------------------------------------------------------
    // Verification
    //------------------------------------------------------------------------

    /**
     * Checks the component's fields
     *
     * @exception   RuntimeServiceException
     *              if any of the fields is null or empty
     */
    private PathComponent checkFields(
    ){
        try {
            for (
                    int index = 0;
                    index < this.fields.length;
                    index++
            ) {
                String field = this.fields[index];
                if(
                        field == null ||
                        (field.length() == 0 && index != 0 && index != this.fields.length-1)
                ) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.BAD_PARAMETER,
                    "A path component's field can neither be null nor empty",
                    new BasicException.Parameter("index",index),
                    new BasicException.Parameter("field",field)
                );
                if (parse(field).length > 1) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.BAD_PARAMETER,
                    "A path component's field can't contain un-escaped field delimiters",
                    new BasicException.Parameter("index",index),
                    new BasicException.Parameter("field",field)
                );
            }
            return this;
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    //------------------------------------------------------------------------
    // Operations returning a new path component
    //------------------------------------------------------------------------

    /**
     * Returns the parent path component.
     *
     * @return the parent path component.
     *
     * @exception   ArrayIndexOutOfBoundsException
     *              if the path is empty
     */
    public PathComponent getParent(
    ){
        return getPrefix(size()-1);
    }

    /**
     * Returns a child path
     *
     * @return a new and longer path
     *
     * @exception   RuntimeServiceException
     *              if the component is null or empty
     */
    public PathComponent getChild(
        String field
    ){
        String[] fields = new String[this.fields.length+1];
        System.arraycopy(
            this.fields, 0,
            fields, 0,
            this.fields.length
        );
        fields[this.fields.length] = field;
        return new PathComponent(fields);
    }

    /**
     * Returns a descendant path component.
     *
     * @param       suffix
     *              the fields to be added
     *
     * @return      the descendant field.
     *
     * @exception   RuntimeServiceException
     *              if any of the components is null or empty
     */
    public PathComponent getDescendant(
        String... suffix
    ){
        String[] fields = new String[this.fields.length+suffix.length];
        System.arraycopy(
            this.fields, 0,
            fields, 0,
            this.fields.length
        );
        System.arraycopy(
            suffix, 0,
            fields, this.fields.length,
            suffix.length
        );
        return new PathComponent(fields);
    }


    //------------------------------------------------------------------------
    // Operations not modifying the path
    //------------------------------------------------------------------------

    /**
     *
     */
    public boolean isPlaceHolder(
    ){
        return tagField(0);
    }

    /**
     * Tells whether a given XRI segment is a placeholder
     * 
     * @param segment an XRI segment
     * 
     * @return <code>true</code> if the given XRI segment is a placeholder
     */
    public static boolean isPlaceHolder(
        String segment
    ){
        return new PathComponent(segment).isPlaceHolder();
    }
    
    /**
     *
     */
    public boolean isPrivate(
    ){
        return tagField(this.fields.length-1);
    }

    private boolean tagField(
        int position
    ){
        return this.fields.length > 0 && this.fields[position].length() == 0;
    }

    /**
     * Returns      the last field of a path component.
     *
     * @return      the last component field.
     *
     * @exception   ArrayIndexOutOfBoundsException
     *              if the path is empty
     */
    public String getLastField(
    ){
        return get(size() - 1);
    }


    //------------------------------------------------------------------------
    // Similar to Name
    //------------------------------------------------------------------------

    /**
     * Compares this path component with another path componentn for order.
     * Returns a negative integer, zero, or a positive integer as this  
     * path component is less than, equal to, or greater than the given one. 
     * 
     * @param       the non-null object to compare against.
     * 
     * @return      a negative integer, zero, or a positive integer as this 
     *              path component is less than, equal to, or greater than the
     *              given one
     *
     * @exception   ClassCastException
     *              if obj is not an instance of PathComponent
     */
    public int compareTo(PathComponent that) {
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

    /**
     * Returns the number of path components for this path.
     *
     * @return the number of path components represented by this object.
     */
    public int size()
    {
        return this.fields.length;
    }

    /**
     * Determines whether this path component is empty.
     * An empty path component is one with zero components.
     *
     * @ return true if the path is empty
     */
    public boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * Retrieves a field of this path component.
     * 
     * @param       position    
     *              the 0-based index of the field to retrieve.
     *              Must be in the range [0,size()).
     * 
     * @return      the field at index position
     * 
     * @exception   ArrayIndexOutOfBoundsException
     *              if position is outside the specified range
     */
    public String get(
        int position
    ) {
        return this.fields[position];
    }

    /**
     * Creates a path whose components consist of a prefix of
     * the components of this path. Subsequent changes to this
     * path will not affect the path that is returned and vice versa.
     * 
     * @param       position
     *              the 0-based index of the component at which to stop.
     *              Must be in the range [0,size()].
     * 
     * @return      a path consisting of the components at indices in the
     *              range [0,position).
     * 
     * @exception   ArrayIndexOutOfBoundsException
     *              if position is outside the specified range
     */
    public PathComponent getPrefix(
        int position
    ){
        if (
                position < 0 ||
                position > size()
        ) throw new ArrayIndexOutOfBoundsException("The field number must be in the range [0,size()]");
        String[] fields = new String[position];
        System.arraycopy(
            this.fields, 0,
            fields, 0,
            position
        );
        return new PathComponent(fields);
    }

    /**
     * Creates a path component consisting of a suffix of the
     * fields in this path component. Subsequent changes to this path
     * do not affect the path that is returned and vice versa.
     * 
     * @param       position
     *              the 0-based index of the component at which to start.
     *              Must be in the range [0,size()].
     * 
     * @return      a path component consisting of the fields at indexes in
     *              the range [position,size()). If position is equal to
     *              size(), an empty path component is returned.
     * 
     * @exception   ArrayIndexOutOfBoundsException
     *              if position is outside the specified range
     */
    public String[] getSuffix(
        int position
    ){
        if (
                position < 0 ||
                position > size()
        ) throw new ArrayIndexOutOfBoundsException("The field number must be in the range [0,size()]");
        String[] fields = new String[size()-position];
        System.arraycopy(
            this.fields, position,
            fields, 0,
            size()-position
        );
        return fields;
    }

    /**
     * Determines whether this path component starts with a specified prefix.
     * A string array is a prefix if it is equal to getPrefix(prefix.length).
     * 
     * @param       prefix
     *              the path component to check
     * 
     * @return      true if the path starts with the specified prefix,
     *              false otherwise
     */
    public boolean startsWith(
        String[] prefix
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
     * @param       prefix
     *              the string arry to check
     * 
     * @return      true if prefix is a prefix of this path, false otherwise
     */
    public boolean startsWith(
        PathComponent prefix
    ) {
        return startsWith(prefix.fields);
    }


    /**
     * Determines whether this path component ends with a specified suffix. 
     * A string array is a suffix if it is equal to 
     * getSuffix(size()-suffix.size()).
     * 
     * @param       suffix
     *              the string array to check
     * 
     * @return      true if suffix is a suffix of this path component,
     *              false otherwise
     */
    public boolean endsWith(
        String[] suffix
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
     * Adds a string array -- in order -- to the end of this path component.
     * 
     * @param   suffix  the components to add
     * 
     * @return  the updated path (not a new one)
     * 
     * @exception   RuntimeServiceException
     *              if suffix is not a valid name, or if the
     *              addition of the components would violate the
     *              syntax rules of this path
     */
    public PathComponent addAll(
        String[] suffix
    ){
        return addAll(size(), suffix);
    }

    /**
     * Adds the elements of a string array -- in order -- at a specified
     * position within this path component. Fields of this path component at
     * or after the index of the first new field are shifted up
     * (away from 0) to accommodate the new fields.
     * 
     * @param       fields          
     *              the fields to add
     * @param       position
     *              the index in this path component at which to add the new
     *              fields.
     *              Must be in the range [0,size()].
     * 
     * @return      the updated path component (not a new one)
     * 
     * @exception   ArrayIndexOutOfBoundsException
     *              if position is outside the specified range
     * @exception   RuntimeServiceException
     *              if n contains invalid path component fields
     */
    public PathComponent addAll (
        int position,
        String[] fields
    ){
        String[] components = new String[size()+fields.length];
        System.arraycopy(
            this.fields, 0,
            fields, 0,
            position
        );
        System.arraycopy(
            fields, 0,
            components, position,
            fields.length
        );
        System.arraycopy(
            this.fields, position,
            fields, position+fields.length,
            size()-position
        );
        this.fields = fields;
        return checkFields();
    }

    /**
     * Adds a single field to the end of this path component.
     * 
     * @param       field
     *              the field to add
     * 
     * @return      the updated path component (not a new one)
     * 
     * @exception   RuntimeServiceException
     *              if adding field would violate the syntax
     *              rules of this path component
     */
    public PathComponent add(
        String field
    ){
        return add (size(), field);
    }

    /**
     * Adds a single field at a specified position within
     * this path component. Fields of this path component at or after the
     * index of the new field are shifted up by one (away from index 0) to
     * accommodate the new field.
     * 
     * @param       field
     *              the field to add
     * @param       position
     *              the index at which to add the new field.
     *              Must be in the range [0,size()].
     * 
     * @return  the updated path (not a new one)
     * 
     * @exception   ArrayIndexOutOfBoundsException
     *              if position is outside the specified range
     * @exception   RuntimeServiceException
     *              if adding field would violate the syntax
     *              rules of this path
     */
    public PathComponent add(
        int position,
        String field
    ){
        String[] fields = new String[size()+1];
        System.arraycopy(
            this.fields, 0,
            fields, 0,
            position
        );
        fields[position] = field;
        System.arraycopy(
            this.fields, position,
            fields, position+1,
            size()-position
        );
        this.fields = fields;
        return checkFields();
    }

    /**
     * Removes a field from this path component.
     * <p>
     * The field of this path component at the specified position is removed.
     * Fields with indices greater than this position are shifted down (toward
     * index 0) by one.
     * 
     * @param       position
     *              the index of the component to remove.
     *              Must be in the range [0,size()).
     * 
     * @return      the component removed (a String)
     * 
     * @exception   ArrayIndexOutOfBoundsException
     *              if position is outside the specified range
     * @exception   RuntimeServiceException
     *              if deleting the component would violate the
     *              syntax rules of the path
     */
    public Object remove(
        int position
    ){
        String field = this.fields[position];
        String[] fields = new String [size()-1];
        System.arraycopy(
            this.fields, 0,
            fields, 0,
            position
        );
        System.arraycopy(
            this.fields, position+1,
            fields, position,
            size()-position-1
        );
        this.fields = fields;
        return field;
    }


    /**
     * Set this path component to the same value as another one.
     * Subsequent changes to the fields of this path component will
     * not affect the other one, and vice versa.
     *
     * @param       source
     *              This path component will have the same fields as source.
     *
     * @exception   ArrayIndexOutOfBoundsException
     *              if position is outside the specified range
     * @exception   RuntimeServiceException
     *              if deleting the component would violate the
     *              syntax rules of the path
     */
    public void setTo(
        PathComponent source
    ){
        this.fields = source.fields;
    }

    /**
     * Parse an XRI segment
     *  
     * @param source
     * 
     * @return the XRI segment's sub-segments
     */
    private static String[] parse(
        String source
    ){
        List<String> target = new ArrayList<String>();
        for (
                int begin = 0, end = -1;
                begin <= source.length();
                begin = end + 1
        ){
            end = subSegmentEnd(source, begin);
            target.add(source.substring(begin, end));

        }
        return target.toArray(new String[target.size()]);
    }

    /**
     * Find the XRI sub-segment's end
     * 
     * @param segment
     * @param position
     * 
     * @return the XRI sub-segment's end position
     */
    private static int subSegmentEnd(
        String segment,
        int position
    ){
        if(position < segment.length()) {
            int cursor = position;
            if(segment.charAt(cursor) == CROSS_REFERENCE_START) {
                cursor = segment.indexOf(CROSS_REFERENCE_END, cursor + 1);
                if(cursor < 0) cursor = position;
            }
            for(
                    int end = segment.indexOf(FIELD_DELIMITER, cursor);
                    end >= 0;
                    end = segment.indexOf(FIELD_DELIMITER, end + 1)
            ) if(
                    ++end >= segment.length() ||
                    segment.charAt(end) != FIELD_DELIMITER
            ) {
                return end - 1;
            }
        }
        return segment.length();
    }

    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------

    /**
     * Generates a new copy of this path component.
     * Subsequent changes to the fields of this path component will
     * not affect the new copy, and vice versa.
     *
     * @return    a clone of this instance.
     */
    @Override
    public Object clone()
    {
        return new PathComponent(this);
    }


    //------------------------------------------------------------------------
    // Extends object
    //------------------------------------------------------------------------

    /**
     * Generates the string representation of this path component. An empty
     * path component is represented by an empty string. The string 
     * representation thus generated can be passed to the PathComponent
     * constructor to create a new equivalent path component.
     *
     * @return   A non-null string representation of this path component.
     */
    @Override
    public String toString(
    ){
        StringBuilder target = new StringBuilder();
        for (
                int index = 0;
                index < this.fields.length;
                index++
        ){
            (index > 0 ?
                target.append(FIELD_DELIMITER) :
                    target
            ).append(
                this.fields[index]
            );
        }
        return target.toString();
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
        return
        this == that ||
        that != null &&
        Arrays.equals(this.fields,((PathComponent)that).fields);
    }

    /**
     * Returns the hash code value for this path component. 
     * <p>
     * This ensures that pathComponent1.equals(pathComponent2) implies that 
     * pathComponent1.hashCode()==pathComponent2.hashCode() for any two paths,
     * pathComponent1 and pathComponent2, as required by the general contract
     * of Object.hashCode.
     *
     * @return the path component's hash code
     */
    @Override
    public int hashCode(
    ){
        int hashCode = 1;
        for(
                int index = 0;
                index < size();
                index++
        ) hashCode = 31 * hashCode + this.fields[index].hashCode();
        return hashCode;
    }


    //------------------------------------------------------------------------
    // Class features
    //------------------------------------------------------------------------

    public static PathComponent createPlaceHolder(
    ){
        return new PathComponent(
            new String[]{"",UUIDs.newUUID().toString()}
        );
    }

    public static PathComponent createPrivate(
        String fields
    ){
        return new PathComponent(fields).add("");
    }


    //------------------------------------------------------------------------
    // Instance Members
    //------------------------------------------------------------------------

    /**
     * The path's components
     */
    private String[] fields;


    //------------------------------------------------------------------------
    // Constants
    //------------------------------------------------------------------------

    /**
     * The fields are delimited by a single colon in a path component's String
     * representation.
     */
    final static public char FIELD_DELIMITER = ':';

    /**
     * XRI cross reference start.
     */
    final static public char CROSS_REFERENCE_START = '(';

    /**
     * XRI cross reference start.
     */
    final static public char CROSS_REFERENCE_END = ')';

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3257008748089325624L;

}
