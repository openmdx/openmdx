/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Lists.java,v 1.3 2004/04/02 16:59:01 wfro Exp $
 * Description: Property Set
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/04/02 16:59:01 $
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
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.compatibility.base.collection;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * List utility classes
 *
 * @author   H. Burger
 * 
 * @deprecated
 */
public class Lists 
{ 

    //------------------------------------------------------------------------
    // List to array conversions
    //------------------------------------------------------------------------
    
    /**
     * Store a list's values in a String array. 
     *
     * @return a String array with the list's values
     *   
     * @exception   ClassCastException
     *              If any of the values is not an instance of String
     */
    public static String[] toStringArray(
        List source
    ){
        return source == null ?
            null:
            (String[])source.toArray(new String[source.size()]);
    }

    /**
     * Store a list's values in a Number array. 
     *
     * @return a Number array with the list's values
     *   
     * @exception   ClassCastException
     *              If any of the values is not an instance of Number
     */
    public static Number[] toNumberArray(
        List source
    ){
        return source == null ?
            null:
            (Number[])source.toArray(new Number[source.size()]);
    }

    /**
     * Store a list's values in an array of byte arrays. 
     *
     * @return an array of byte arrays with the list's values
     *   
     * @exception   ClassCastException
     *              If any of the values is not an instance of byte[]
     */
    public static byte[][] toBinaryArray(
        List source
    ){
        return source == null ?
            null:
            (byte[][])source.toArray(new byte[source.size()][]);
    }

    /**
     * Store a list's values in a boolean array. 
     *
     * @return a boolean array with the list's values
     *   
     * @exception   NullPointerException
     *              if any of the values is null
     * @exception   ClassCastException
     *              if any of the values is not an instance of Boolean
     */
    public static boolean[] toBooleanArray(
        List source
    ){
        if(source == null) return null;
        final boolean[] target = new boolean[source.size()];
        for(
            int index = 0;
            index < target.length;
            index++
        ) target[index] = ((Boolean)source.get(index)).booleanValue();
        return target;
    }


    //------------------------------------------------------------------------
    // Array to string conversions
    //------------------------------------------------------------------------
    
    /**
     * Applys the String.valueOf() method to each of the array members. 
     *
     * @return an array with the string representation of the source's values
     *   
     */
    public static String[] stringValues(
        byte[] source
    ){
        if(source == null) return null;
        final String[] target = new String[source.length];
        for(
            int index = 0;
            index < target.length;
            index++
        ) target[index] = String.valueOf(source[index]);
        return target;
    }
    
    /**
     * Applys the String.valueOf() method to each of the array members. 
     *
     * @return an array with the string representation of the source's values
     *   
     */
    public static String[] stringValues(
        short[] source
    ){
        if(source == null) return null;
        final String[] target = new String[source.length];
        for(
            int index = 0;
            index < target.length;
            index++
        ) target[index] = String.valueOf(source[index]);
        return target;
    }
    
    /**
     * Applys the String.valueOf() method to each of the array members. 
     *
     * @return an array with the string representation of the source's values
     *   
     */
    public static String[] stringValues(
        int[] source
    ){
        if(source == null) return null;
        final String[] target = new String[source.length];
        for(
            int index = 0;
            index < target.length;
            index++
        ) target[index] = String.valueOf(source[index]);
        return target;
    }
    
    /**
     * Applys the String.valueOf() method to each of the array members. 
     *
     * @return an array with the string representation of the source's values
     *   
     */
    public static String[] stringValues(
        long[] source
    ){
        if(source == null) return null;
        final String[] target = new String[source.length];
        for(
            int index = 0;
            index < target.length;
            index++
        ) target[index] = String.valueOf(source[index]);
        return target;
    }
    
    /**
     * Applys the String.valueOf() method to each of the array members. 
     *
     * @return an array with the string representation of the source's values
     *   
     */
    public static String[] stringValues(
        float[] source
    ){
        if(source == null) return null;
        final String[] target = new String[source.length];
        for(
            int index = 0;
            index < target.length;
            index++
        ) target[index] = String.valueOf(source[index]);
        return target;
    }
    
    /**
     * Applys the String.valueOf() method to each of the array members. 
     *
     * @return an array with the string representation of the source's values
     *   
     */
    public static String[] stringValues(
        double[] source
    ){
        if(source == null) return null;
        final String[] target = new String[source.length];
        for(
            int index = 0;
            index < target.length;
            index++
        ) target[index] = String.valueOf(source[index]);
        return target;
    }
    
    /**
     * Applys the String.valueOf() method to each of the array members. 
     *
     * @return an array with the string representation of the source's values
     *   
     */
    public static String[] stringValues(
        boolean[] source
    ){
        if(source == null) return null;
        final String[] target = new String[source.length];
        for(
            int index = 0;
            index < target.length;
            index++
        ) target[index] = String.valueOf(source[index]);
        return target;
    }
    
    /**
     * Applyies String.valueOf() to each of the collection's members. 
     *
     * @return      a an array with the string representation of the 
     *              collection's values
     */
    public static String[] stringValues(
        Collection source
    ){
        if(source == null) return null;
        final String[] target = new String[source.size()];  
        final Iterator iterator = source.iterator();
        for(
            int index = 0;
            index < target.length;
            index++
        ) target[index] = String.valueOf(iterator.next());
        return target;
    }

    /**
     * Applys the String.valueOf() method to each of the array members. 
     *
     * @return an array with the string representation of the source's values
     *   
     */
    public static String[] stringValues(
        Object[] source
    ){
        if(source == null) return null;
        String[] target = new String[source.length];
        for(
            int index = 0;
            index < target.length;
            index++
        ) target[index] = String.valueOf(source[index]);
        return target;
    }
    
    /**
     * Applys the String.valueOf() method to each of the array or collections
     * members. 
     *
     * @return an array with the string representation of the source's values
     *   
     */
    public static String[] stringValues(
        Object source
    ){
        return 
            source == null ? NO_STRING_VALUES :
            source instanceof Object[] ? stringValues((Object[])source) :
            source instanceof Collection ? stringValues((Collection)source) :
            source instanceof boolean[] ? stringValues((boolean[])source) :
            source instanceof byte[] ? stringValues((byte[])source) :
            source instanceof short[] ? stringValues((short[])source) :
            source instanceof int[] ? stringValues((int[])source) :
            source instanceof long[] ? stringValues((long[])source) :
            source instanceof float[] ? stringValues((float[])source) :
            source instanceof double[] ? stringValues((double[])source) :
            new String[]{source.toString()};
    }

    /**
     * The method String values returns an empty list if source is null.
     */
    public static String[] NO_STRING_VALUES = {};
    

    //------------------------------------------------------------------------
    // Array to list conversions
    //------------------------------------------------------------------------
    
    /**
     * Returns a fixed-size list backed by the specified array. (Changes to
     * the returned list "write through" to the array.) This method acts as
     * bridge between array-based and collection-based APIs. The returned list
     * is serializable.
     *
     * @param   source
     *          the array by which the list will be backed.
     *
     * @return  a list view of the specified array.
     *
     * @exception   ClassCastException 
     *              if array is not an instance of an array
     */
    public static List fromArray(
        Object source
    ){
        return 
            source == null ? null :
            source instanceof boolean[] ? new BooleanList((boolean[])source) :
            source instanceof byte[] ? new ByteList((byte[])source) :
            source instanceof short[] ? new ShortList((short[])source) :
            source instanceof int[] ? new IntegerList((int[])source) :
            source instanceof long[] ? new LongList((long[])source) :
            source instanceof float[] ? new FloatList((float[])source) :
            source instanceof double[] ? new DoubleList((double[])source) :
            Arrays.asList((Object[])source);
    }

    private static class BooleanList extends AbstractList {
        
        final boolean[] array;

        BooleanList(
            boolean[] array
        ){
            this.array = array;
        }
        
        public int size(
        ){
            return array.length;
        }

        public Object get(
            int index
        ){
            return new Boolean(array[index]);
        }
                
        public Object set(
            int index,
            Object element
        ){
            Object result = get(index);
            array[index]=((Boolean)element).booleanValue();
            return result;
        }

    }

    private static class ByteList extends AbstractList {
        
        final byte[] array;

        ByteList(
            byte[] array
        ){
            this.array = array;
        }
        
        public int size(
        ){
            return array.length;
        }

        public Object get(
            int index
        ){
            return new Byte(array[index]);
        }
                
        public Object set(
            int index,
            Object element
        ){
            Object result = get(index);
            array[index]=((Number)element).byteValue();
            return result;
        }

    }

    private static class ShortList extends AbstractList {
        
        final short[] array;

        ShortList(
            short[] array
        ){
            this.array = array;
        }
        
        public int size(
        ){
            return array.length;
        }

        public Object get(
            int index
        ){
            return new Short(array[index]);
        }
                
        public Object set(
            int index,
            Object element
        ){
            Object result = get(index);
            array[index]=((Number)element).shortValue();
            return result;
        }

    }

    private static class IntegerList extends AbstractList {
        
        final int[] array;

        IntegerList(
            int[] array
        ){
            this.array = array;
        }
        
        public int size(
        ){
            return array.length;
        }

        public Object get(
            int index
        ){
            return new Integer(array[index]);
        }
                
        public Object set(
            int index,
            Object element
        ){
            Object result = get(index);
            array[index]=((Number)element).intValue();
            return result;
        }

    }

    private static class LongList extends AbstractList {
        
        final long[] array;

        LongList(
            long[] array
        ){
            this.array = array;
        }
        
        public int size(
        ){
            return array.length;
        }

        public Object get(
            int index
        ){
            return new Long(array[index]);
        }
                
        public Object set(
            int index,
            Object element
        ){
            Object result = get(index);
            array[index]=((Number)element).longValue();
            return result;
        }

    }

    private static class FloatList extends AbstractList {
        
        final float[] array;

        FloatList(
            float[] array
        ){
            this.array = array;
        }
        
        public int size(
        ){
            return array.length;
        }

        public Object get(
            int index
        ){
            return new Float(array[index]);
        }
                
        public Object set(
            int index,
            Object element
        ){
            Object result = get(index);
            array[index]=((Number)element).floatValue();
            return result;
        }

    }

    private static class DoubleList extends AbstractList {
        
        final double[] array;

        DoubleList(
            double[] array
        ){
            this.array = array;
        }
        
        public int size(
        ){
            return array.length;
        }

        public Object get(
            int index
        ){
            return new Double(array[index]);
        }

        public Object set(
            int index,
            Object element
        ){
            Object result = get(index);
            array[index]=((Number)element).doubleValue();
            return result;
        }
                
    }

}   
