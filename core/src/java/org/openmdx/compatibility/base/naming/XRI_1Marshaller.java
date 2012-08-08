/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: XRI_1Marshaller.java,v 1.2 2007/10/10 16:06:03 hburger Exp $
 * Description: Path/XRI Marshaller 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:03 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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

import java.util.ArrayList;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.StringBuilders;
import org.openmdx.kernel.url.protocol.XriAuthorities;

/**
 * Path/XRI Marshaller
 */
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
        CharSequence xri = StringBuilders.newStringBuilder(OPENMDX_XRI_PREFIX);
        char delimiter = ':';
        for(
          int i=0;
          i<source.length;
        ){
            encode(
                    source[i],
                    StringBuilders.asStringBuilder(xri).append(delimiter),
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
                new BasicException.Parameter[]{
                    new BasicException.Parameter("path", charSequences)
                },
                "Path marshalling failed"
            );
        }
    }

    static void encode(
        Object charSequence,
        CharSequence xri,
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
                StringBuilders.asStringBuilder(xri).append(XREF_BEGIN),
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
            StringBuilders.asStringBuilder(xri).append(
               '('
            ).append(
               new Path(source).toXri().substring(4)
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
        CharSequence xri,
        boolean authority,
        boolean terminal
    ){
        if(terminal && "%".equals(source)) {
            StringBuilders.asStringBuilder(xri).append("***");
        } else if(terminal && source.endsWith("%")) {
            StringBuilders.asStringBuilder(xri).append(source.substring(0, source.length() - 1)).append("***");
        } else if(authority && ":*".equals(source)){
            StringBuilders.asStringBuilder(xri).append("**");
        } else if(source.startsWith(":") && source.endsWith("*")) {
            encode(
                source.substring(1, source.length() - 1),
                xri,
                false,
                false
            );
            StringBuilders.asStringBuilder(xri).append("**");
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
                            StringBuilders.asStringBuilder(xri).append(authority ? '.' : ':');
                            break;
                        case '%':
                            StringBuilders.asStringBuilder(xri).append("%25");
                                break;
                        case '{': case '}':case '<': case '>': case '[' : case ']':
                        case ' ': case '"': case '\\': case '^': case '`': case '|':
                        appendTo(xri, character, true);
                        break;
                        case '(': case ')':
                        case '#': case '?': case '/':
                        case ';': case '@': case '&': case '=': case '+': case '$': case ',': // remaining reserved
                        case '-': case '_': case '.': case '!': case '~': case '*': case '\'': // remaining mark
                            StringBuilders.asStringBuilder(xri).append(character);
                            break;
                        default:
                            if(Character.isLetterOrDigit(character)){
                                StringBuilders.asStringBuilder(xri).append(character);
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
        if(!source.toLowerCase().startsWith(OPENMDX_XRI_PREFIX)) throw new ServiceException (
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            new BasicException.Parameter[]{
                new BasicException.Parameter("xri",source)
            },
            "'xri' scheme and '" + XriAuthorities.OPENMDX_AUTHORITY + "' authority expected"
        );
        ArrayList target = new ArrayList();
        if(source.length() == OPENMDX_XRI_PREFIX.length()) {
            // Valid Empty Path
        } else if (":**".equals(source.substring(OPENMDX_XRI_PREFIX.length()))) {
            target.add(":*");
        } else try {
            CharSequence segment = StringBuilders.newStringBuilder();
            int crossReference = 0;
            boolean authority = true;
            for(
              int i = OPENMDX_XRI_PREFIX.length() + 1, limit = source.length();
              i < limit;
              i++
            ){
                char character = source.charAt(i);
                switch(character){
                    case '(':
                        ++crossReference;
                        if(
                            i + 1 < limit &&
                            !source.substring(i).startsWith(XREF_BEGIN + XriAuthorities.OPENMDX_AUTHORITY) &&
                            XRI_GCS.indexOf(source.charAt(i + 1)) > 0
                        ) {
                            StringBuilders.asStringBuilder(segment).append(XRI_XREF_PREFIX);
                        } else {
                            StringBuilders.asStringBuilder(segment).append(character);
                        }
                        break;
                    case ')':
                        if(--crossReference < 0) throw new ServiceException (
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            new BasicException.Parameter[]{
                                new BasicException.Parameter("xri",source),
                                new BasicException.Parameter("position",i)
                            },
                            "More closing than opening parenthesis"
                        );
                        StringBuilders.asStringBuilder(segment).append(character);
                        break;
                    case '/':
                        if(crossReference > 0){
                            StringBuilders.asStringBuilder(segment).append(character);
                        } else {
                            target.add(
                                decode(
                                    segment.toString(),
                                    authority,
                                    false // terminal
                                )
                            );
                            authority = false;
                            StringBuilders.asStringBuilder(segment).setLength(0);
                        }
                        break;
                    case '.':
                        StringBuilders.asStringBuilder(segment).append(
                            target.isEmpty() ? ':' : '.'
                        );
                        break;
                    default:
                        StringBuilders.asStringBuilder(segment).append(character);
                }
            }
            if(crossReference > 0) throw new ServiceException (
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("xri",source),
                    new BasicException.Parameter("position", source.length())
                },
                "More opening than closing parenthesis"
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
                new BasicException.Parameter[]{
                    new BasicException.Parameter("xri", charSequence)
                },
                "XRI to Path conversion failed"
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
            CharSequence target = StringBuilders.newStringBuilder();
            boolean xrefAuthority = true;
            for(
                int i = OPENMDX_XREF_PREFIX.length(), limit = source.length() - XREF_END.length();
                i < limit;
                i++
            ){
                char character = source.charAt(i);
                switch (character) {
                    case '%' :
                        StringBuilders.asStringBuilder(target).append(
                            i + 2 < limit ? (char) Integer.parseInt(source.substring(++i, ++i+1), 0x10) : character
                        );
                        break;
                    case '.':
                        StringBuilders.asStringBuilder(target).append(
                            xrefAuthority ? "::" : "."
                        );
                        break;
                    case '/':
                        xrefAuthority = false;
                    default:
                        StringBuilders.asStringBuilder(target).append(character);
                }
            }
            return new Path(target.toString()).toString();
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
                CharSequence target = StringBuilders.newStringBuilder(source.length());
                for(
                    int i = 0, limit = source.length();
                    i < limit;
                    i++
                ){
                    char character = source.charAt(i);
                    StringBuilders.asStringBuilder(target).append(
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
        CharSequence target,
        char character,
        boolean escaped
    ){
        if(escaped){
            appendTo(target, character, "%");
        } else {
            StringBuilders.asStringBuilder(target).append(character);
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
        CharSequence target,
        char character,
        String escape
    ){
        StringBuilders.asStringBuilder(
            target
        ).append(
            escape
        ).append(
            Character.toUpperCase(Character.forDigit(character / 0x10, 0x10))
        ).append(
            Character.toUpperCase(Character.forDigit(character % 0x10, 0x10))
        );
    }

    private final static String XREF_BEGIN = "(";

    private final static String XREF_END = ")";

    private final static String XRI_XREF_PREFIX = XREF_BEGIN + "xri://";

    private final static String OPENMDX_XRI_PREFIX = "xri:" + XriAuthorities.OPENMDX_AUTHORITY;

    private final static String OPENMDX_XREF_PREFIX = XREF_BEGIN + XriAuthorities.OPENMDX_AUTHORITY + ":";

    private final static String XRI_GCS = "!=@+$";

}
