/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractFilter.java,v 1.21 2008/09/10 08:55:20 hburger Exp $
 * Description: Abstract Filter Class
 * Revision:    $Revision: 1.21 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:20 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.compatibility.base.query;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;

import javax.resource.ResourceException;

import org.openmdx.base.exception.BadParameterException;
import org.openmdx.base.resource.Records;
import org.openmdx.base.text.pattern.SQLExpression;
import org.openmdx.base.text.pattern.cci.Pattern_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;


/**
 * FilterProperty based Filter
 */
@SuppressWarnings("unchecked")
public abstract class AbstractFilter implements Selector, Serializable {

    protected AbstractFilter(
    ){
        // For Deserialization
    }

    /**
     * Constructor
     * 
     * @param filter
     * 
     * @exception   BadParameterException
     *              in case of an invalid filter property set
     * @exception   NullPointerException
     *              if the filter is <code>null</code>
     */
    protected AbstractFilter(
        FilterProperty[] filter
    ){
        this.filter = filter;
        for (
                int i = 0;
                i < this.filter.length;
                i++
        ) try {
            short operator = filter[i].operator();
            if(
                    operator == FilterOperators.IS_LIKE ||
                    operator == FilterOperators.IS_UNLIKE
            ){
                if(this.pattern == null) {
                    this.pattern = new Pattern_1_0[filter.length];
                }
                Object value = filter[i].getValue(0);
                this.pattern[i] = value instanceof Path ?
                    new PathPattern((Path)value) :
                        SQLExpression.compile((String)value);                
            }
        } catch (BadParameterException exception) {
            throw new BadParameterException(
                exception,
                "Invalid filter property",
                new BasicException.Parameter("filterProperty",filter[i])
            );
        }
    }

    /**
     * 
     * @param candidate
     * @param attribute
     * @return an iterator for the values
     *         or null in case of failure
     * 
     * @exception   ClassCastException
     *              If the filter is not applicable to the candidate
     */
    protected abstract Iterator getValues(
        Object candidate,
        String attribute
    );

    /**
     * 
     */
    protected FilterProperty[] filter;

    /**
     * 
     */
    private Pattern_1_0[] pattern;

    /**
     * 
     */
    private static Comparator comparator = LenientPathComparator.getInstance();    

    /**
     * 
     */
    public FilterProperty[] getDelegate(
    ){
        return this.filter;
    }


    //------------------------------------------------------------------------
    // Implements Selector 
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.query.Selector#accept(java.lang.Object)
     */
    public boolean accept(
        Object candidate
    ){
        for (
                int propertyIndex = 0;
                propertyIndex < this.filter.length;
                propertyIndex++
        ){
            boolean forAll = true;
            boolean thereExists = false;
            FilterProperty property = this.filter[propertyIndex];

            Iterator iterator = getValues(candidate, property.name());
            if(iterator == null) {
                return false;
            }
            while (iterator.hasNext()){
                Object raw = iterator.next();
                switch(property.operator()){

                    case FilterOperators.IS_UNLIKE: {
                        if (!matches(propertyIndex, raw)){
                            thereExists = true;
                        } else {
                            forAll = false;
                        }
                        break;
                    }

                    case FilterOperators.IS_LIKE: {
                        if (matches(propertyIndex, raw)){
                            thereExists = true;
                        } else {
                            forAll = false;
                        }
                        break;
                    }

                    case FilterOperators.IS_OUTSIDE: {
                        if(
                                comparator.compare(raw,property.getValue(0)) < 0 ||
                                comparator.compare(raw,property.getValue(1)) > 0
                        ){
                            thereExists = true;
                        } else {
                            forAll = false;
                        }
                        break;
                    }

                    case FilterOperators.IS_BETWEEN: {
                        if(
                                comparator.compare(raw,property.getValue(0)) >= 0 &&
                                comparator.compare(raw,property.getValue(1)) <= 0
                        ){
                            thereExists = true;
                        } else {
                            forAll = false;
                        }
                        break;
                    }

                    case FilterOperators.IS_LESS_OR_EQUAL: {                
                        if(
                                comparator.compare(raw,property.getValue(0)) <= 0
                        ){
                            thereExists = true;
                        } else {
                            forAll = false;
                        }
                        break;
                    }

                    case FilterOperators.IS_GREATER: {
                        if(
                                comparator.compare(raw,property.getValue(0)) > 0
                        ){
                            thereExists = true;
                        } else {
                            forAll = false;
                        }
                        break;
                    }

                    case FilterOperators.IS_LESS: {
                        if(
                                comparator.compare(raw,property.getValue(0)) < 0
                        ){
                            thereExists = true;
                        } else {
                            forAll = false;
                        }
                        break;
                    }

                    case FilterOperators.IS_GREATER_OR_EQUAL: {
                        if(
                                comparator.compare(raw,property.getValue(0)) >= 0
                        ){
                            thereExists = true;
                        } else {
                            forAll = false;
                        }
                        break;
                    }

                    case FilterOperators.IS_NOT_IN: {
                        boolean test = true;
                        IsNotIn: for(
                                int setIndex = 0, setSize = property.getValues().length;
                                setIndex < setSize;
                                setIndex++
                        ) {
                            boolean equal =  raw instanceof Comparable || raw instanceof Number ? 
                                comparator.compare(raw,property.getValue(setIndex)) == 0 :
                                    raw.equals(property.getValue(setIndex)); 
                            if(equal) {
                                test = false;
                                break IsNotIn;
                            }
                        }
                        if(test){
                            thereExists = true;
                        } else {
                            forAll = false;
                        }
                        break;
                    }

                    case FilterOperators.IS_IN: {
                        boolean test = false;
                        IsIn: for(
                                int setIndex = 0, setSize = property.getValues().length;
                                setIndex < setSize;
                                setIndex++
                        ) {
                            boolean equal = raw instanceof Comparable || raw instanceof Number ? 
                                comparator.compare(raw,property.getValue(setIndex)) == 0 :
                                    raw.equals(property.getValue(setIndex));
                            if(equal) {
                                test = true;
                                break IsIn;
                            }
                        }
                        if(test){
                            thereExists = true;
                        } else {
                            forAll = false;
                        }
                        break;
                    }

                    case FilterOperators.SOUNDS_LIKE: {
                        boolean test = false;
                        String encoded = Soundex.getInstance().encode((String)raw);
                        for(
                                int setIndex = 0, setSize = property.getValues().length;
                                setIndex < setSize;
                                setIndex++
                        ){
                            if(encoded.equals(Soundex.getInstance().encode((String)property.getValue(setIndex)))) {
                                test = true;
                            }
                        } 
                        if(test){
                            thereExists = true;
                        } else {
                            forAll = false;
                        }
                        break;
                    }

                    case FilterOperators.SOUNDS_UNLIKE: {
                        boolean test = true;
                        String encoded = Soundex.getInstance().encode((String)raw);
                        SoundsUnlike: for(
                                int setIndex = 0, setSize = property.getValues().length;
                                setIndex < setSize;
                                setIndex++
                        ) {
                            if(encoded.equals(Soundex.getInstance().encode((String)property.getValue(setIndex)))) {
                                test = false;
                                break SoundsUnlike;
                            }
                        }
                        if(test){
                            thereExists = true;
                        } else {
                            forAll = false;
                        }
                        break;
                    }

                    default: throw new BadParameterException(
                        new BasicException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            "Unsupported operator", 
                            new BasicException.Parameter(
                                "operator",
                                FilterOperators.toString(
                                    property.operator()
                                )
                            )
                        )
                    );                
                }
            }
            switch (property.quantor()) {
                case Quantors.FOR_ALL: 
                    if(!forAll) {
                        return false;
                    }
                    break;
                case Quantors.THERE_EXISTS:
                    if(!thereExists) {
                        return false;
                    }
                    break;
                default: 
                    throw new BadParameterException(
                        new BasicException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            "Unsupported quantor", 
                            new BasicException.Parameter(
                                "quantor",
                                Quantors.toString(
                                    property.quantor()
                                )
                            )
                        )
                    );
            }

        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        try {
            return Records.getRecordFactory().asIndexedRecord(
                getClass().getName(), 
                null, 
                this.filter
            ).toString();
        } catch (ResourceException e) {
            return getClass().getName() + "//" + e.getMessage();
        }
    }

    public int size(){
        return this.filter.length;
    }

    /**
     * PathPattern aware match method
     * 
     * @param pattern
     * @param value
     * @return
     */
    private boolean matches (
        int index,
        Object value
    ){        
        if(value instanceof Path){
            if(!(this.pattern[index] instanceof PathPattern)) try {
                this.pattern[index] = new PathPattern(new Path(this.pattern[index].pattern()));
            } catch (Exception exception) {
                return false;
            }
            return ((PathPattern)this.pattern[index]).matches((Path)value);
        } else {
            return this.pattern[index].matches(value.toString());
        }
    }


    //------------------------------------------------------------------------
    // Class Soundex 
    //------------------------------------------------------------------------

    /**
     * Encodes words using the soundex phonetic algorithm.
     * The primary method to call is Soundex.encode(String).<p>
     * The main method encodes arguments to System.out.
     * @author Aaron Hansen
     */
    final static class Soundex {

        /**
         * 
         */
        public static Soundex getInstance(
        ) {
            if(Soundex.instance == null) {
                Soundex.instance = new Soundex();
            }
            return instance;
        }

        /**
         * @param _word
         */    
        public String encode(
            String _word
        ) {
            String word = _word.trim();
            if (DropFinalSBoolean) {
                //we're not dropping double s as in guess
                if ( (word.length() > 1) 
                        && (word.endsWith("S") || word.endsWith("s")))
                    word = word.substring(0, (word.length() - 1));
            }
            if(word.length() == 0) return "";
            word = reduce(word);
            int wordLength = word.length(); //original word size
            int sofar = 0; //how many codes have been created
            int max = LengthInt - 1; //max codes to create (less the first char)
            if (LengthInt < 0) //if NO_MAX
                max = wordLength; //wordLength was the max possible size.
            int code = 0; 
            StringBuilder buf = new StringBuilder(
                max
            ).append(
                Character.toLowerCase(word.charAt(0))
            );
            for (int i = 1;(i < wordLength) && (sofar < max); i++) {
                code = getCode(word.charAt(i));
                if (code > 0) {
                    buf.append(code);
                    sofar++;
                }
            }
            if (PadBoolean && (LengthInt > 0)) {
                for (;sofar < max; sofar++)
                    buf.append('0');
            }
            return buf.toString();
        }

        /**
         * Returns the Soundex code for the specified character.
         * @param ch Should be between A-Z or a-z
         * @return -1 if the character has no phonetic code.
         */
        public int getCode(
            char ch
        ) {
            int arrayidx = -1;
            if (('a' <= ch) && (ch <= 'z'))
                arrayidx = ch - 'a';
            else if (('A' <= ch) && (ch <= 'Z'))
                arrayidx = ch - 'A';
            if ((arrayidx >= 0) && (arrayidx < SoundexInts.length))
                return SoundexInts[arrayidx];
            else
                return -1;
        }

        /**
         * If true, a final char of 's' or 'S' of the word being encoded will be 
         * dropped. By dropping the last s, lady and ladies for example,
         * will encode the same. False by default.
         */
        public boolean getDropFinalS(
        ) {
            return DropFinalSBoolean;
        }

        /**
         * The length of code strings to build, 4 by default.
         * If negative, length is unlimited.
         * @see #NO_MAX
         */
        public int getLength(
        ) {
            return LengthInt;
        }

        /**
         * If true, appends zeros to a soundex code if the code is less than
         * Soundex.getLength().  True by default.
         */
        public boolean getPad(
        ) {
            return PadBoolean;
        }

        /**
         * Allows you to modify the default code table
         * @param ch The character to specify the code for.
         * @param code The code to represent ch with, must be -1, or 1 thru 9
         */
        public void setCode(
            char ch, 
            int code
        ) {
            int arrayidx = -1;
            if (('a' <= ch) || (ch <= 'z'))
                arrayidx = ch - 'a';
            else if (('A' <= ch) || (ch <= 'Z'))
                arrayidx = ch - 'A';
            if ((0 <= arrayidx) && (arrayidx < SoundexInts.length))
                SoundexInts[arrayidx] = code;
        }

        /**
         * If true, a final char of 's' or 'S' of the word being encoded will be 
         * dropped.
         */
        public void setDropFinalS(
            boolean bool
        ) {
            DropFinalSBoolean = bool;
        }

        /**
         * Sets the length of code strings to build. 4 by default.
         * @param Length of code to produce, must be &gt;= 1
         */
        public void setLength(
            int length
        ) {
            LengthInt = length;
        }

        /**
         * If true, appends zeros to a soundex code if the code is less than
         * Soundex.getLength().  True by default.
         */
        public void setPad(
            boolean bool
        ) {
            PadBoolean = bool;
        }

        /**
         * Creates the Soundex code table.
         */
        protected static int[] createArray(
        ) {
            return new int[] {
                -1, //a 
                1, //b
                2, //c 
                3, //d
                -1, //e 
                1, //f
                2, //g 
                -1, //h
                -1, //i 
                2, //j
                2, //k
                4, //l
                5, //m
                5, //n
                -1, //o
                1, //p
                2, //q
                6, //r
                2, //s
                3, //t
                -1, //u
                1, //v
                -1, //w
                2, //x
                -1, //y
                2  //z
            };
        }

        /**
         * Removes adjacent sounds.
         */
        protected String reduce(
            String word
        ) {
            int len = word.length();
            StringBuilder buf = new StringBuilder(len);
            char ch = word.charAt(0);
            int currentCode = getCode(ch);
            buf.append(ch);
            int lastCode = currentCode;
            for (int i = 1; i < len; i++) {
                ch = word.charAt(i);
                currentCode = getCode(ch);
                if ((currentCode != lastCode) && (currentCode >= 0)) {
                    buf.append(ch);
                    lastCode = currentCode;
                }
            }
            return buf.toString();
        }

        /**
         * 
         */
        private static transient Soundex instance = null;

        /**
         * 
         */
        public static final int NO_MAX = -1;

        /**
         * If true, the final 's' of the word being encoded is dropped.
         */
        protected boolean DropFinalSBoolean = false;

        /**
         * Length of code to build.
         */
        protected int LengthInt = 4;

        /**
         * If true, codes are padded to the LengthInt with zeros.
         */
        protected boolean PadBoolean = true;

        /**
         * Soundex code table.
         */
        protected int[] SoundexInts = createArray();

    }

}
