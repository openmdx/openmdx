/*
 * ====================================================================
 * Project:     openMEX, http://www.openmdx.org/
 * Name:        $Id: IndentingFormatter.java,v 1.7 2009/04/28 13:58:52 hburger Exp $
 * Description: Intending Formatter
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/04/28 13:58:52 $
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
 *    the documentation and/or other materials provided with the
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
package org.openmdx.kernel.text.format;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.resource.cci.Record;

import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.text.MultiLineStringRepresentation;

/**
 * Intending Formatter
 */
public class IndentingFormatter
    implements MultiLineStringRepresentation
{

    /**
     * Constructor
     */
    public IndentingFormatter(
        Object source
    ){
        this.source = source;
    }

    /**
     * 
     */
    private final Object source;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return toString(this.source);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public static String toString(
        Object source
    ) {
        return appendObject(
            new StringBuilder(), 
            source
        ).toString();
    }

    /**
     * Appends an object to the given StringBuilder
     * 
     * @param target
     * @param source
     * 
     * @return the target
     */
    protected static CharSequence appendObject(
        StringBuilder target,
        Object source
    ){
        if(source == null){
            target.append("null");
        } else if(source instanceof Record){
            appendHeader(
                target,
                (Record)source
            );
            if(source instanceof List<?>){
                appendList(target, (List<?>)source);
            } else if (source instanceof Map<?,?>){
                appendMap(target, (Map<?,?>)source);
            } else {
                target.append(
                    source.getClass().getName()
                ).append(
                    '@'
                ).append(
                    System.identityHashCode(source)
                );
            }
        } else if(source instanceof List<?>){
            appendList(target, (List<?>)source);
        } else if (source instanceof Map<?,?>){
            appendMap(target, (Map<?,?>)source);
        } else if (source instanceof byte[]){
            target.append(new HexadecimalFormatter((byte[])source));
        } else if (source.getClass().isArray()){
            appendList(target, ArraysExtension.asList(source));
        } else {
            target.append(source);
        }
        return target;
    }

    /**
     * Create the reacord header
     * 
     * @param   source
     *          the record to be formatted
     *
     * @return  a StringBuffer filled with the record header
     */
    protected static void appendHeader(
        StringBuilder target,
        Record source
    ){
        String recordName = source.getRecordName();
        String recordShortDescription = source.getRecordShortDescription();
        if(recordName!=null){
            target.append(recordName);
            if(recordShortDescription!=null) target.append(
                " ("
            ).append(
                recordShortDescription
            ).append(
                ')'
            );
            target.append(": ");
        } else if (recordShortDescription!=null) {
            target.append(
                '('
            ).append(
                recordShortDescription
            ).append(
                "): "
            );
        }
    }

    /**
     * Create a value entry
     * 
     * @param   target
     *          the target string buffer
     * @param	key
     *			the entry's key
     * @param	separator
     *			the separator between key and value
     * @param	value
     *			the entry's value
     */
    protected static void appendEntry(
        StringBuilder target,
        Object key,
        String separator,
        Object value
    ){
        int j=target.append("\n\t").length();
        target.append(key).append(separator);
        if(value instanceof String){ // CharSequence
            target.append('"').append(value).append('"');
        }else if(value instanceof Character){
            target.append('\'').append(value).append('\'');
        }else{
            appendObject(target,value);
        }
        while(
                j<target.length()
        )if(
                target.charAt(j++)=='\n'
        )target.insert(j++,'\t');
    }

    /**
     * Append a list
     * 
     * @param target
     * @param source
     */
    protected static void appendList(
        StringBuilder target,
        List<?> source
    ){
        target.append('[');
        String terminator="]";
        for(
            ListIterator<?> i=source.listIterator();
            i.hasNext();
            terminator="\n]"
        ) appendEntry(
            target,
            String.valueOf(i.nextIndex()),
            ": ",
            i.next()
        );
        target.append(terminator);
    }

    /**
     * Append a list
     * 
     * @param target
     * @param source
     */
    protected static void appendMap(
        StringBuilder target,
        Map<?,?> source
    ){
        target.append('{');
        String terminator="}";
        for(Map.Entry<?,?> entry : source.entrySet()){
            appendEntry(
                target,
                entry.getKey(),
                " = ",
                entry.getValue()
            );
            terminator="\n}";
        }
        target.append(terminator);
    }

}
