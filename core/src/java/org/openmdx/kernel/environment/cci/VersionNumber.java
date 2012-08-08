/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: VersionNumber.java,v 1.12 2008/03/21 18:36:29 hburger Exp $
 * Description: openMDX: Version Number Class
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:36:29 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.kernel.environment.cci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * openMDX
 * Version Number Class
 */
@SuppressWarnings("unchecked")
public final class VersionNumber
    implements Cloneable, Serializable, Comparable
{

    /**
     * 
     */
    private static final long serialVersionUID = 3258128059516401713L;

    /**
     * 
     */
    protected final int[] internal;

    /**
     * 
     */
    protected final String external;

    /**
     * Internal constructor
     * 
     * @param internal
     * @param external
     */
    private VersionNumber(
        String external,
        int[] internal
    ){
        this.external = external;
        this.internal = internal;
    }

    /**
     * Creates a version based on its string representation.
     * 
     * @param version
     * 
     * @exception   IllegalArgumentException
     *              if the version string can't be parsed.
     */
    public VersionNumber(
        String version
    ){
        this(version, toInternal(version, true));
    }

    /**
     * Create the version number represented by the components
     * 
     * @param components
     *        an array of version number components, starting with the most
     *        significant one
     * 
     * @return the version number represented by the components
     * 
     * @exception NullPointerException 
     * 			  if components is <code>null</code>
     */
    public VersionNumber(
        int[] components
    ){
        this(
            toExternal(components),
                        components.clone()
                );
    }

        /**
     * Compliance test: The first components must be equal and
     * the second component of this version number must be
     * greater or equal than the second component of the other
     * object.
     * <p>
     * Positive examples:<ul>
     * <li>3.1 is compliant with 3
     * <li>3.3 is compliant with 3.1
     * <li>3.3.1 is compliant with 3.3.3
     * <li>3.3.3 is compliant with 3.3.1
     * <li>3.3.3.1245 is compliant with 3.3.1.1728
     * </ul>
     * Negative examples:<ul>
     * <li>3 is not compliant with 3.1
     * <li>3.1 is not compliant with 3.3
     * <li>3.1 is not compliant with 2.1
     * </ul>
     * 
     * @param that
     */
    public boolean isCompliantWith(
        VersionNumber that
    ){
        int thisMajor = this.internal.length > 0 ? this.internal[0] : 0;
        int thatMajor = that.internal.length > 0 ? that.internal[0] : 0;
        int thisMinor = this.internal.length > 1 ? this.internal[1] : 0;
        int thatMinor = that.internal.length > 1 ? that.internal[1] : 0;
        return
            thisMajor == thatMajor &&
            thisMinor >= thatMinor;
    }

    /**
     * Get the number of components
     * 
     * @return the number of components
     */
    public int size(
    ){
        return this.internal.length;
    }

    /**
     * Retrieves a component of this verison number.
     * 
     * @param index the 0-based index of the component to retrieve. 
     *              Must be in the range [0,size()).
     * 
     * @return the component at the given position 
     * 
     * @exception ArrayIndexOutOfBoundsException
     *            if index is outside the specified range
     */
    public int get(
        int index
    ){
        return this.internal[index];
    }

    /**
     * Get a version number's prefix
     * 
     * @parameter   position
     *              which should not be included
     * 
     * @return a version number's prefix
     * 
     * @exception   IndexOutOfBoundsException
     *              if position is greater than size
     */
    public VersionNumber getPrefix(
        int position
    ) {
        // Get internal
        int[] internal = new int[position];
        System.arraycopy(this.internal, 0, internal, 0, position);
        int cursor = -1;
        for(
            int i = 0;
            i < position;
        ) if(this.external.charAt(++cursor) == '.') i++;
        return new VersionNumber(this.external.substring(0, cursor < 0 ? 0 : cursor), internal);
    }

    /**
     * Get the external representation of the internal format.
     * 
     * @param   internal
     * 			the version number's internal representatiom
     *          
     * @return  the version number's external representation
     * 
     * @exception NullPointerException if internal is 
     *            <code>null</code>.
     */
    protected static String toExternal(
        int[] internal
    ){
        if(internal.length == 0) return "";
        StringBuilder external = new StringBuilder().append(internal[0]);
        for(
            int i=1;
            i< internal.length;
            i++
        ) external.append(
            '.'
        ).append(
            internal[i]
        );
        return external.toString();
    }

    /**
     * Parses the external format
     * 
     * @param   external
     *          the version number's external representation
     * @param   strict
     *          tells whether a parsing failure is signalled by
     *          a BadParameterException or a <code>null</code> return value
     * 
     * @return  the version number's internal representatiom
     * 
     * @exception IllegalArgumentException
     *            in case of parsing failure if strict is true
     */
    protected static int[] toInternal(
        String external,
        boolean strict
    ){
        List elements = new ArrayList();
        int element = 0;
        for(
            int i = 0;
            i < external.length();
            i++
        ){
            char c = external.charAt(i);
            if(c >= '0' && c <= '9') {
                element = 10 * element + Character.digit(c, 10);
            } else if (c == '.'){
                if(i == 0) {
                    if(strict) {
                        throw new IllegalArgumentException(
                                "A version string must not start with a dot: " + external
                        );
                    } else {
                        return null;
                    }
                } else if (i == external.length() - 1){
                    if(strict) {
                        throw new IllegalArgumentException(
                                "A version string must not end with a dot: " + external
                        );
                    } else {
                        return null;
                    }
                } else {
                        elements.add(new Integer(element));
                        element = 0;
                }
            } else {
                if(strict) {
                        throw new IllegalArgumentException(
                                "Illegal character '" + c + "' at position " + i + ": " + external
                                        );
                } else {
                    return null;
                }
            }
        }
        elements.add(new Integer(element));
        int[] internal = new int[elements.size()];
        for(
            int i = 0;
            i < elements.size();
            i++
        ) internal[i] = ((Integer)elements.get(i)).intValue();
        return internal;
    }

    /**
     * Create a version number if the string represents one.
     * <p>
     * As opposed to VersionNumber(String) this method does not throw an
     * exception in case of parse failure but returns null.
     * 
     * @param source
     * 
     * @return  Return the corresponding version number or null if if the 
     *          string does not represent a version number
     * 
     */
    public static VersionNumber toVersionNumber(
        String source
    ){
        int[] internal = toInternal(source, false);
        return internal == null ? null : new VersionNumber(source, internal);
    }

    /**
     * Create the version number represented by the components
     * 
     * @param components
     *        an array of version number components, starting with the most
     *        significant one
     * 
     * @return the version number represented by components;
     * 		   or <code>null</code> if components is <code>null</code>
     */
    public static VersionNumber toVersionNumber(
        int[] components
    ){
        return components == null ?
                null :
                new VersionNumber(components);
    }


    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException {
        return new VersionNumber(this.external, this.internal);
    }


    //------------------------------------------------------------------------
    // Implements Comparable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object object) {
        VersionNumber that = (VersionNumber)object;
        for(
            int i = 0, c;
            i < this.internal.length && i < that.internal.length;
            i++
        ) if(
            (c = this.internal[i] - that.internal[i]) != 0
        ) return c;
        return this.internal.length - that.internal.length;
    }


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object that) {
        return
            that instanceof VersionNumber &&
            this.external.equals(((VersionNumber)that).external);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.external.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.external;
    }

}
