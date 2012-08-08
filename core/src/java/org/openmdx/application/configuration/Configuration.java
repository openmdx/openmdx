/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Configuration.java,v 1.8 2010/08/06 12:26:25 hburger Exp $
 * Description: Configuration
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/08/06 12:26:25 $
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
package org.openmdx.application.configuration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openmdx.base.collection.TreeSparseArray;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.w3c.cci2.SparseArray;


/**
 * Note that this implementation is not synchronized. If multiple threads
 * access this dataprovider object concurrently, and at least one of the
 * threads modifies it structurally, it must be synchronized externally. 
 * (A structural modification is any operation that adds or deletes one or
 * more attributes; merely modifying the values associated with an attribute
 * that an instance already contains is not a structural modification.) This
 * is typically accomplished by synchronizing on the dataprovider object
 * itself. 
 */
public class Configuration
    implements Cloneable, MultiLineStringRepresentation
{

    /**
     *
     */
    public Configuration(
    ){
        super();
    }

    /**
     *
     */
    public Configuration(
        Configuration that
    ){
        this(that.entries());
    }

    /**
     *
     */
    public Configuration(
        Map<String, ?> source
    ){
        for(Map.Entry<String, ?> entry : source.entrySet()) {
            final String name = entry.getKey();
            SparseArray<Object> values;
            if (entry.getValue() instanceof Collection<?>) {
                values = new TreeSparseArray<Object>(
                    (Collection<?>)entry.getValue()
                );
            } else if (entry.getValue() instanceof TreeSparseArray<?>) {
                values = new TreeSparseArray<Object>(
                    (TreeSparseArray<?>)entry.getValue()
                );
            } else if (entry.getValue() instanceof Object[]) {
                values = new TreeSparseArray<Object>(
                    Arrays.asList((Object[])entry.getValue())
                );
            } else {
                values = new TreeSparseArray<Object>(
                    Collections.singletonList(entry.getValue())
                );
            }
            this.entries.put(name,values);
        }
    }

    //------------------------------------------------------------------------
    // Value list interface
    //------------------------------------------------------------------------

    /**
     * Returns true if this configuration contains an entry for the specified 
     * name.
     *
     * @return      true if this configuration contains an entry for the
     *              specified name
     */
    public boolean containsEntry(
        String entryName
    ){
        return this.entries.containsKey(entryName);
    } 

    /**
     * Returns true if the named flag is set.
     *
     * @return      true if this configuration contains an entry for the
     *              specified name and its value is true
     */
    public boolean isOn(
        String entryName
    ){
        if (! containsEntry(entryName)) return false;
        final Object value = values(entryName).get(0);
        return (
            value instanceof Boolean && ((Boolean)value).booleanValue() 
        ) || "true".equalsIgnoreCase(value.toString());
    } 

    /**
     * Returns the modifiable attribute value list.
     * This method never returns null.
     */
    @SuppressWarnings("unchecked")
    public final <T> SparseArray<T> values(
        String entryName
    ){
        SparseArray<T> values = (SparseArray<T>)this.entries.get(entryName);
        if (values == null) {
            values = new TreeSparseArray<T>();
            this.entries.put(entryName, values);
        }
        return values;
    }


    //------------------------------------------------------------------------
    // Flat list 
    //------------------------------------------------------------------------

    public void setValue(
        String key,
        Object value,
        boolean override
    ) throws ServiceException {
        // Set the default values for name and index
        String name = key;
        int index = 0;

        // If the name has [] brackets, then correct its value and index
        if(key.endsWith("]")) try {
            name = key.substring(
                0,
                key.indexOf('[')
            );
            index = Integer.parseInt(
                key.substring(
                    key.indexOf('[') + 1,
                    key.indexOf(']') 
                )
            );
        } catch (NumberFormatException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.TRANSFORMATION_FAILURE,
                "Could not evaluate the key's index",
                new BasicException.Parameter("key",key),
                new BasicException.Parameter("value",value)
            ); 
        }

        // Set the value at its correct position. 
        // Do not allow overwriting 
        final SparseArray<?> list = values(name);
        if (list.get(index) != null) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN, 
            BasicException.Code.DUPLICATE,
            "Attempt to overwrite " + key,
            new BasicException.Parameter("name",name),
            new BasicException.Parameter("index",index),
            new BasicException.Parameter(
                "values",
                list.get(index),
                value
            )
        );
        values(name).put(
            index,
            value
        );
    }

    //------------------------------------------------------------------------
    // Entry collection interface
    //------------------------------------------------------------------------

    public final Map<String, SparseArray<?>> entries(
    ){
        return this.entries;
    }

    //------------------------------------------------------------------------
    // String interface
    //------------------------------------------------------------------------

    /**
     *
     */
    final private static String[] NO_VALUES = {};

    /**
     * Returns the property values. 
     * This method never returns null.
     *
     * @param   name    the property's name
     *
     * @return  The values as string array; 
     *          it is empty if the property is empty or missing
     */
    public final String[] getValues(
        String name
    ) {
        final SparseArray<?> values = this.entries.get(name);
        if(values == null) return NO_VALUES;
        final String[] target = new String[values.size()];  
        final Iterator<?> iterator = values.iterator();
        for(
            int index = 0;
            index < target.length;
            index++
        ) target[index] = String.valueOf(iterator.next());
        return target;  
    }           

    /**
     * Returns the property's first value. 
     *
     * @param   name    the property's name
     *
     * @return  The property's first value as a string; 
     *          or null if it is empty or missing
     */
    public final String getFirstValue(
        String name
    ) {
        final SparseArray<?> values = this.entries.get(name);
        return values == null || values.isEmpty() ? 
            null : 
                values.get(values.firstKey()).toString();
    }

    /**
     * Returns an array containing all of the elements in this collection. 
     * The returned array will be "safe" in that no references to it are
     * maintained by this set. (In other words, this method must allocate a
     * new array even if this set is backed by an array). The caller is thus 
     * free to modify the returned array.
     * <p>
     * This method acts as bridge between array-based and collection-based
     * APIs.
     * 
     * @return  an array containing a Property for each of this set's
     *          entries.
     */
    public BasicException.Parameter[] toExceptionParameters (
    ){
        final BasicException.Parameter[] target = new BasicException.Parameter[this.entries.size()];
        final Iterator<String> iterator = this.entries.keySet().iterator();
        for(
            int index = 0;
            index < target.length;
            index++
        ){
            final String name = iterator.next();
            target[index] = new BasicException.Parameter(
                name,
                (Object[])getValues(name)
            );
        }
        return target;
    }


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /**
     * Returns a string representation of the object. In general, the toString
     * method returns a string that "textually represents" this object. The
     * result should be a concise but informative representation that is easy
     * for a person to read. It is recommended that all subclasses override
     * this method. 
     *
     * @return the dataprovider object's string representation
     */
    @Override
    public String toString (
    ){
        return IndentingFormatter.toString(this.entries);
    }


    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------

    /**
     * Generates a new copy of this dataprovider object.
     * Subsequent changes to the path or the attributes of this
     * object will not affect the new copy, and vice versa.
     *
     * @return    a clone of this instance.
     */
    @Override
    public Object clone(  
    ){
        return new Configuration(this);
    }


    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------

    /**
     *
     */
    private Map<String, SparseArray<?>> entries = new HashMap<String, SparseArray<?>>();

}
