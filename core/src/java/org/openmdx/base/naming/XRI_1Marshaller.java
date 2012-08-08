/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: XRI_1Marshaller.java,v 1.3 2009/05/29 17:04:11 hburger Exp $
 * Description: Path/XRI Marshaller 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/29 17:04:11 $
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

import java.util.ArrayList;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.url.protocol.XRI_1Protocols;
import org.openmdx.kernel.url.protocol.XRI_2Protocols;
import org.openmdx.kernel.url.protocol.XriAuthorities;

/**
 * Path/XRI Marshaller
 */
@SuppressWarnings("unchecked")
public final class XRI_1Marshaller
implements Marshaller
{

    private XRI_1Marshaller(
    ){
        // Avoid external instantiation
    }

    /**
     * Memorize the singleton
     */
    final static private Marshaller instance = new XRI_1Marshaller();

    /**
     * Return the singleton
     */
    static public Marshaller getInstance(
    ){
        return XRI_1Marshaller.instance;
    }


    //------------------------------------------------------------------------
    // Implements Marshaller
    //------------------------------------------------------------------------

    /**
     * Marshal a CharSequence[] into a CharSequence
     * 
     * @param     charSequences
     *            The array of CharSequences to be marshalled.
     * 
     * @return      A CharSequence containing the marshalled objects.
     */
    public Object marshal (
        Object charSequences
    ) throws ServiceException {
        if (charSequences == null) return null;
        Object[]source = (Object[])charSequences;
        StringBuilder xri = new StringBuilder(XRI_1Protocols.OPENMDX_PREFIX);
        char delimiter = ':';
        for(
                int i=0;
                i<source.length;
        ){
            encode(
                source[i],
                xri.append(delimiter),
                i == 0,
                ++i == source.length
            );
            delimiter = '/';
        }
        try {
            return xri.toString();
        } catch (IllegalArgumentException e) {
            throw new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Path marshalling failed",
                new BasicException.Parameter("path", charSequences)
            );
        }
    }

    @SuppressWarnings("deprecation")
    static void encode(
        Object charSequence,
        StringBuilder xri,
        boolean authority,
        boolean terminal
    ){
        String source = charSequence.toString();
        if (
                source.startsWith(XRI_XREF_PREFIX) &&
                source.endsWith(XREF_END)
        ) {
            encode(
                source.substring(XRI_XREF_PREFIX.length()),
                xri.append(XREF_BEGIN),
                authority,
                terminal
            );
        } else if(
                source.indexOf('/') < 0 ||
                (source.startsWith(XREF_BEGIN) && source.endsWith(XREF_END))
        ) {
            encode(
                source,
                xri,
                authority,
                terminal
            );
        } else {
            xri.append(
                '('
            ).append(
                new Path(source).toXri().substring(XRI_1Protocols.SCHEME_PREFIX.length())
            ).append(
                ')'
            );
        }
    }

    /**
     * The following, then, are the XRI-specific steps required to convert an XRI into a URI.
     * <ol>
     * <li>Escape all percent "%" characters within a segment as "%25".
     * <lo>Escape unbalanced paranthesis within a segment as "%28" and "%29".
     * <li>Escape all number sign "#" characters that appear within a segment as "%23".
     * <li>Escape all question mark "?" characters that appear within a segment as "%3F".
     * <li>Escape all slash "/" characters that appear within a segment as "%2F".
     * </ol>
     * 
     * @param charSequence
     * @param xri
     */
    static void encode(
        String source,
        StringBuilder xri,
        boolean authority,
        boolean terminal
    ){
        if(terminal && "%".equals(source)) {
            xri.append("***");
        } else if(terminal && source.endsWith("%")) {
            xri.append(source.substring(0, source.length() - 1)).append("***");
        } else if(authority && ":*".equals(source)){
            xri.append("**");
        } else if(source.startsWith(":") && source.endsWith("*")) {
            encode(
                source.substring(1, source.length() - 1),
                xri,
                false,
                false
            );
            xri.append("**");
        } else {
            int crossReference = 0;
            boolean unbalanced = false;
            for(
                    int i = 0, l = source.length();
                    i < l;
                    i++
            ){
                char character = source.charAt(i);
                switch (character) {
                    case '(':
                        ++crossReference;
                        break;
                    case ')':
                        unbalanced |= --crossReference < 0;
                        break;
                }
            }
            unbalanced |= crossReference > 0;
            for(
                    int i = 0, l = source.length();
                    i < l;
                    i++
            ){
                char character = source.charAt(i);
                switch (character) {
                    case ':':
                        xri.append(authority ? '.' : ':');
                        break;
                    case '%':
                        xri.append("%25");
                        break;
                    case '{': case '}':case '<': case '>': case '[' : case ']':
                    case ' ': case '"': case '\\': case '^': case '`': case '|':
                        appendTo(xri, character, true);
                        break;
                    case '(': case ')':
                    case '#': case '?': case '/':
                    case ';': case '@': case '&': case '=': case '+': case '$': case ',': // remaining reserved
                    case '-': case '_': case '.': case '!': case '~': case '*': case '\'': // remaining mark
                        xri.append(character);
                        break;
                    default:
                        if(Character.isLetterOrDigit(character)){
                            xri.append(character);
                        } else {
                            appendTo(xri, character, "%25");
                        }
                }
            }
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
     * @exception ServiceException ILLEGAL_ARGUMENT
     */
    public Object unmarshal (
        Object charSequence
    ) throws ServiceException {
        if (charSequence == null) return null;
        String source = charSequence.toString();
        if(!source.toLowerCase().startsWith(XRI_1Protocols.OPENMDX_PREFIX)) throw new ServiceException (
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "'xri' scheme and '" + XriAuthorities.OPENMDX_AUTHORITY + "' authority expected",
            new BasicException.Parameter("xri",source)
        );
        ArrayList target = new ArrayList();
        if(source.length() == XRI_1Protocols.OPENMDX_PREFIX.length()) {
            // Valid Empty Path
        } else if (":**".equals(source.substring(XRI_1Protocols.OPENMDX_PREFIX.length()))) {
            target.add(":*");
        } else try {
            StringBuilder segment = new StringBuilder();
            int crossReference = 0;
            boolean authority = true;
            for(
                    int i = XRI_1Protocols.OPENMDX_PREFIX.length() + 1, limit = source.length();
                    i < limit;
                    i++
            ){
                char character = source.charAt(i);
                switch(character){
                    case '(':
                        ++crossReference;
                        segment.append(character);
                        break;
                    case ')':
                        if(--crossReference < 0) throw new ServiceException (
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            "More closing than opening parenthesis",
                            new BasicException.Parameter("xri",source),
                            new BasicException.Parameter("position",i)
                        );
                        segment.append(character);
                        break;
                    case '/':
                        if(crossReference > 0){
                            segment.append(character);
                        } else {
                            target.add(
                                decode(
                                    segment.toString(),
                                    authority,
                                    false // terminal
                                )
                            );
                            authority = false;
                            segment.setLength(0);
                        }
                        break;
                    case '.':
                        segment.append(
                            target.isEmpty() ? ':' : '.'
                        );
                        break;
                    default:
                        segment.append(character);
                }
            }
            if(crossReference > 0) throw new ServiceException (
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "More opening than closing parenthesis",
                new BasicException.Parameter("xri",source),
                new BasicException.Parameter("position", source.length())
            );
            target.add(
                decode(
                    segment.toString(),
                    authority,
                    true // terminal
                )
            );
        } catch (Exception exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TRANSFORMATION_FAILURE,
                "XRI to Path conversion failed",
                new BasicException.Parameter("xri", charSequence)
            );
        }
        return target.toArray(new String[target.size()]);
    }

    /**
     * Decodes encoded ASCII characters only!
     * 
     * @param source
     * @param authority 
     * @param terminal
     *  
     * @throws ServiceException
     */
    private static String decode(
        String source,
        boolean authority,
        boolean terminal
    ) throws ServiceException{
        if(source.startsWith(OPENMDX_XREF_PREFIX) && source.endsWith(XREF_END)) {
            StringBuilder target = new StringBuilder();
            boolean xrefAuthority = true;
            for(
                    int i = OPENMDX_XREF_PREFIX.length(), limit = source.length() - XREF_END.length();
                    i < limit;
                    i++
            ){
                char character = source.charAt(i);
                switch (character) {
                    case '%' :
                        target.append(
                            i + 2 < limit ? (char) Integer.parseInt(source.substring(++i, ++i+1), 0x10) : character
                        );
                        break;
                    case '.':
                        target.append(
                            xrefAuthority ? "::" : "."
                        );
                        break;
                    case '/':
                        xrefAuthority = false;
                    default:
                        target.append(character);
                }
            }
            return new Path(target.toString()).toComponent();
        } else if (authority && "*".equals(source)) {
            return ":*";
        } else if (terminal && ":***".equals(source)) {
            return "%";
        } else if (terminal && source.endsWith("***")) {
            return decode(source.substring(0, source.length() - 3), authority, false) + "%";
        } else if (source.endsWith("**")) {
            return ':' + decode(
                source.substring(0, source.length() - 2),
                authority, false
            ) + '*';
        } else if (source.indexOf('%') < 0){
            return source;
        } else {
            StringBuilder target = new StringBuilder(source.length());
            for(
                    int i = 0, limit = source.length();
                    i < limit;
                    i++
            ){
                char character = source.charAt(i);
                target.append(
                    character == '%' && i + 2 < limit ?
                        (char) Integer.parseInt(source.substring(++i, ++i+1), 0x10) :
                            character
                );
            }
            return target.toString();
        }
    }

    /**
     * Append a character or its escape sequence
     * 
     * @param target
     * @param character
     * @param escaped
     */
    private static void appendTo(
        StringBuilder target,
        char character,
        boolean escaped
    ){
        if(escaped){
            appendTo(target, character, "%");
        } else {
            target.append(character);
        }
    }

    /**
     * Append an escape sequence
     * 
     * @param target
     * @param character
     * @param escape
     */
    private static void appendTo(
        StringBuilder target,
        char character,
        String escape
    ){
        target.append(
            escape
        ).append(
            Character.toUpperCase(Character.forDigit(character / 0x10, 0x10))
        ).append(
            Character.toUpperCase(Character.forDigit(character % 0x10, 0x10))
        );
    }

    private final static String XREF_BEGIN = "(";

    private final static String XREF_END = ")";

    private final static String XRI_XREF_PREFIX = XREF_BEGIN + XRI_2Protocols.SCHEME_PREFIX;

    private final static String OPENMDX_XREF_PREFIX = XREF_BEGIN + XriAuthorities.OPENMDX_AUTHORITY + ":";

}
