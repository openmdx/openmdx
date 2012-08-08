/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: PathMarshaller.java,v 1.1 2009/01/06 13:14:44 wfro Exp $
 * Description: Property 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/06 13:14:44 $
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.naming;

import java.util.ArrayList;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;


/**
 * Converts Path into a human readable form.
 */
@SuppressWarnings("unchecked")
public final class PathMarshaller
implements Marshaller
{

    private PathMarshaller (
    ){
        // Avoid external instantiation
    }

    final public static char COMPONENT_SEPARATOR = '/';
    final public static char FIELD_SEPARATOR = ':';

    /**
     * Memorize the singleton
     */ 
    final private static Marshaller instance = new PathMarshaller();

    /**
     * Return the singleton
     */ 
    static public Marshaller getInstance(
    ){
        return PathMarshaller.instance;
    }


    //------------------------------------------------------------------------
    // Implements Marshaller
    //------------------------------------------------------------------------

    /**
     * Marshal a CharSequence[] into a CharSequence.
     * 
     * @param     charSequences
     *            The array of CharSequences to be marshalled.
     * 
     * @return      A CharSequence containing the marshalled objects.
     */
    public Object marshal (
        Object charSequences
    ) throws ServiceException {
        Object[] source = (Object[])charSequences;
        if (source == null) return null;
        if (source.length==0) return "";
        StringBuilder target = new StringBuilder();
        appendMarshalled(target,source[0]);
        for(
                int i=1;
                i<source.length;
                i++
        )appendMarshalled(
            target.append(COMPONENT_SEPARATOR),
            source[i]
        );
        return target;
    }

    /**
     * Append a marshalled component to a <code>StringBuilder</code>.
     * 
     * @param     buffer
     *            The StringBuffer to be modified.
     * @param     charSequence
     *            The component to be appended.
     */
    private void appendMarshalled (
        StringBuilder buffer,
        Object charSequence
    ) throws ServiceException {
        int cursor = buffer.length();
        buffer.append(charSequence);
        if(cursor == buffer.length())throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "One of the paths' components is empty"
        );
        for (
                int index = buffer.length ();
                index > cursor;
        ) {
            final char character = buffer.charAt (--index);
            if (
                    character == COMPONENT_SEPARATOR || 
                    character == FIELD_SEPARATOR
            ) buffer.insert(
                index,
                character
            );
        }           
    }


    /**
     * Unmarshal a CharSequence into a CharSequence[].
     * 
     * @param         marshalledObjects
     *            A string containing a marshalled sequence of objects
     * 
     * @return    A String array containing the unmarshaled sequence
     *                  of objects.
     */
    public Object unmarshal (
        Object charSequence
    ) throws ServiceException {
        final ArrayList target = new ArrayList();
        final String source = charSequence.toString();
        if (source.length() > 0) { 
            int begin = 0;
            int end = source.indexOf (COMPONENT_SEPARATOR);
            if(end==0) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Path starts with '" + COMPONENT_SEPARATOR + "'"
            );  
            while(end != -1) {
                if(end+1==source.length()) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "Path ends with '" + COMPONENT_SEPARATOR + "'"
                );
                if(source.charAt(end+1) == COMPONENT_SEPARATOR) {
                    end = source.indexOf (COMPONENT_SEPARATOR, end+2);
                } else {
                    appendUnmarshalled (
                        target, 
                        source.substring (begin, end)
                    );
                    begin = end + 1;
                    end = source.indexOf (COMPONENT_SEPARATOR, begin);
                }
            }
            appendUnmarshalled (target, source.substring (begin));
        }
        return target.toArray(new String[target.size()]);
    }

    /**
     * Append unmarshalled
     * 
     * Append an unmarshalled component to a ArrayList.
     * 
     * @param  object   The component to be appended.
     */
    private void appendUnmarshalled (
        ArrayList target,
        String source
    ){
        StringBuilder buffer = new StringBuilder();
        int end;
        for (
                int start = 0;
                start < source.length();
                start = end + 1
        ){
            end = start;
            while(
                    end+1 < source.length() &&
                    ! (source.charAt(end) == COMPONENT_SEPARATOR) &&
                    ! (source.charAt(end) == FIELD_SEPARATOR)
            ) end++;
            buffer.append(source.substring(start,++end));
        }
        target.add(buffer.toString());
    }

}
