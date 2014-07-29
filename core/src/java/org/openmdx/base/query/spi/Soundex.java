/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Soundex
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013, OMEX AG, Switzerland
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
package org.openmdx.base.query.spi;

/**
 * Encodes words using the soundex phonetic algorithm.
 * 
 * @author Aaron Hansen
 */
public final class Soundex {

    private Soundex() {
		super();
	}

	/**
     * 
     */
    public static Soundex getInstance(
    ) {
        return instance;
    }

    /**
     * @param _word
     */    
    public String encode(
        String _word
    ) {
        String word = _word.trim();
        if (dropFinalSBoolean) {
            //we're not dropping double s as in guess
            if ( (word.length() > 1) 
                    && (word.endsWith("S") || word.endsWith("s")))
                word = word.substring(0, (word.length() - 1));
        }
        if(word.length() == 0) return "";
        word = reduce(word);
        int wordLength = word.length(); //original word size
        int sofar = 0; //how many codes have been created
        int max = lengthInt - 1; //max codes to create (less the first char)
        if (lengthInt < 0) //if NO_MAX
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
        if (padBoolean && (lengthInt > 0)) {
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
        if ((arrayidx >= 0) && (arrayidx < soundexInts.length))
            return soundexInts[arrayidx];
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
        return dropFinalSBoolean;
    }

    /**
     * The length of code strings to build, 4 by default.
     * If negative, length is unlimited.
     * @see #NO_MAX
     */
    public int getLength(
    ) {
        return lengthInt;
    }

    /**
     * If true, appends zeros to a soundex code if the code is less than
     * Soundex.getLength().  True by default.
     */
    public boolean getPad(
    ) {
        return padBoolean;
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
        if ((0 <= arrayidx) && (arrayidx < soundexInts.length))
            soundexInts[arrayidx] = code;
    }

    /**
     * If true, a final char of 's' or 'S' of the word being encoded will be 
     * dropped.
     */
    public void setDropFinalS(
        boolean bool
    ) {
        dropFinalSBoolean = bool;
    }

    /**
     * Sets the length of code strings to build. 4 by default.
     * @param Length of code to produce, must be &gt;= 1
     */
    public void setLength(
        int length
    ) {
        lengthInt = length;
    }

    /**
     * If true, appends zeros to a soundex code if the code is less than
     * Soundex.getLength().  True by default.
     */
    public void setPad(
        boolean bool
    ) {
        padBoolean = bool;
    }

    /**
     * Creates the Soundex code table.
     */
    private static int[] createArray(
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
    private String reduce(
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
     * The singleton
     */
    private static final Soundex instance = new Soundex();  

    /**
     * 
     */
    public static final int NO_MAX = -1;

    /**
     * If true, the final 's' of the word being encoded is dropped.
     */
    private boolean dropFinalSBoolean = false;

    /**
     * Length of code to build.
     */
    private int lengthInt = 4;

    /**
     * If true, codes are padded to the LengthInt with zeros.
     */
    private boolean padBoolean = true;

    /**
     * Soundex code table.
     */
    private final int[] soundexInts = createArray();

}