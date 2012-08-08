/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: XRI_2Marshaller.java,v 1.1 2009/01/06 13:14:43 wfro Exp $
 * Description: Path/ObjectId Marshaller 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/06 13:14:43 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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

import java.util.Iterator;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.text.conversion.UnicodeTransformation;
import org.openmdx.kernel.url.protocol.XRI_2Protocols;
import org.openmdx.kernel.url.protocol.XriAuthorities;
import org.openxri.AuthorityPath;
import org.openxri.XRI;
import org.openxri.XRIParseException;
import org.openxri.XRIPath;
import org.openxri.XRIReference;
import org.openxri.XRISegment;
import org.openxri.XRISubSegment;
import org.openxri.XRef;


/**
 * Path/ObjectId Marshaller
 */
@SuppressWarnings("unchecked")
public final class XRI_2Marshaller
    implements Marshaller
{

    private XRI_2Marshaller(
    ){
        // Avoid external instantiation
    }
    
    /**
     * Memorize the singleton
     */ 
    final static private Marshaller instance = new XRI_2Marshaller();
    
    /**
     * xri://@openmdx
     */
    final static String[] ROOT = {};
    
    /**
     * Return the singleton
     */ 
    static public Marshaller getInstance(
    ){
        return XRI_2Marshaller.instance;
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
        Object[]segments = (Object[])charSequences;        
        StringBuilder oid = new StringBuilder(XRI_2Protocols.OPENMDX_PREFIX);
        char separator = '*';
        for(
            int i = 0;
            i < segments.length;
            i++
        ){
            oid.append(
                separator
            );
            String segment = segments[i].toString();
            if(isCrossReference(segment)) {
                encode(
                    segment, // source
                    oid, // target
                    i == 0, // authority
                    "", // prefix
                    "" // suffix
                );
            } else if (segment.indexOf('/') < 0) {
                if(segment.endsWith("%")) {
                    if(segment.length() == 1) {
                        oid.append(
                            "($...)"
                        );
                    } else {
                        XRISegment xriSegment = new XRISegment(segment);
                        StringBuilder target = new StringBuilder();
                        boolean first = true;
                        for(
                             Iterator<XRISubSegment> j = xriSegment.getSubSegmentIterator();
                             j.hasNext();
                             first = false
                        ){
                             XRISubSegment subSegment = j.next();
                             String value = subSegment.toString(!first);
                             if(j.hasNext()) {
                                 target.append(value);
                             } else {
                                 if(!first) {
                                     target.append('*');
                                 }
                                 if (value.length() > 1) {
                                     target.append(
                                         "($."
                                     ).append(
                                         subSegment.isPersistant() ? '!' : '*'
                                     ).append(
                                         value.substring(0, value.length() - 1)
                                     ).append(
                                         ")*"
                                     );
                                 }
                                 encode(
                                     target.toString(), // source
                                     oid, // target
                                     i == 0, // authority
                                     "", // prefix
                                     "($..)/($...)" // suffix
                                 );
                             }
                        }
                    }
                } else if (segment.startsWith(":") && segment.endsWith("*")) {
                    if (segment.length() == 2) {
                        oid.append(
                            "($..)"
                        );
                    } else {
                        encode(
                            segment.substring(1, segment.length() - 1),
                            oid, // target
                            i == 0, // authority
                            "($.*", // prefix
                            ")*($..)" // suffix
                        );
                    }
                } else {
                    encode(
                        segment, // source
                        oid, // target
                        i == 0, // authority
                        "", // prefix
                        "" // suffix
                    );
                }
            } else {
                try {
                    Object xRef = marshal(new Path(segment).getSuffix(0));
                    oid.append(
                        '('
                    ).append(
                        xRef.toString().substring(XRI_2Protocols.SCHEME_PREFIX.length())
                    ).append(
                        ')'
                    );
                } catch (Exception exeption) {
                    appendCaseExactString(
                        oid,
                        segment
                    );
                }
            }
            separator = '/';
        }
        return oid.toString();
    }

    /**
     * The following, then, are the XRI-specific steps required to convert an XRI into a URI.
     * <ol>
     * <li>Escape all percent '%' characters within a segment as "%25".
     * <li>Escape unbalanced paranthesis within a segment as "%28" and "%29".
     * <li>Escape all number sign '#' characters that appear within a segment as "%23".
     * <li>Escape all question mark '?' characters that appear within a segment as "%3F".
     * <li>Escape all slash '/' characters that appear within a segment as "%2F".
     * </ol>
     * @param oid
     * @param authority 
     * @param prefix 
     * @param suffix 
     * @param charSequence
     */
    private static void encode (
        String source,
        StringBuilder oid, 
        boolean authority, 
        String prefix, 
        String suffix
    ){
        oid.append(prefix);
        String segment = authority ? 
            source.replace(':', '.') : 
            source.replace('=', ':');
        if(isCaseExactString(source)) {
            appendCaseExactString(oid, segment);
        } else try {
            new XRISegment(segment);
            oid.append(segment);
        } catch (XRIParseException exception) {
            appendCaseExactString(oid, segment);
        }
        oid.append(suffix);
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
        String xriString = charSequence.toString();
        boolean treeWildcard = xriString.endsWith(")/($...)");
        XRI xri = new XRI(xriString);
        String authority = xri.getAuthorityPath().toString();
        if(XriAuthorities.OPENMDX_AUTHORITY.equals(authority)) return ROOT;
        XRIPath path = xri.getXRIPath();        
        String[] reply = new String[
            path == null ? 1 : path.getNumSegments() + (treeWildcard ? 0 : 1)
        ];
        reply[0] = authority.substring(9).replaceAll(
            "\\(\\$\\.\\.\\.\\)", "%"
        ).replaceAll(
            "\\(\\$\\.\\.\\)", ":*"
        ).replaceAll(
            "\\(\\$\\./([^\\)]+)\\)",
            "\1:*"
        ).replaceAll(
            "\\(\\$\\.\\)",
            ":*"
        ).replaceAll(
            "\\.",
            ":"
        );
        for(
            int i = 1;
            i < reply.length;
            i++
        ) {
            XRISegment segment = path.getSegmentAt(i - 1);
            int jLimit = segment.getNumSubSegments();
            boolean subSegmentsWildcard = segment.getSubSegmentAt(jLimit - 1).toString().equals("*($..)");
            StringBuilder target = new StringBuilder();
            boolean parameter = false;
            boolean wildcard = false;
            SubSegments: for(
                int j = 0; 
                j < jLimit;
                j++
            ) {                                
                boolean wasParameter = parameter;
                boolean wasWildcard = wildcard;
                parameter = false;
                wildcard = false;
                XRISubSegment subSegment = segment.getSubSegmentAt(j);
                String source = subSegment.toString().substring(1);
                int s = source.indexOf(';');
                if(s > 0) {
                    source = source.substring(0, s) + source.substring(s).replace(':', '=');
                }
                String delimiter = 
                    wasParameter ? "=" :
                    subSegment.isPersistant() ? "!" :
                    j == 0 ? "" : 
                    "*";
                parameter = false;
                XRef xRef = subSegment.getXRef();
                if(xRef == null) {
                    target.append(delimiter).append(source);
                } else {
                    XRIReference xriReference = xRef.getXRIReference();
                    if (xriReference != null) {
                        AuthorityPath authorityPath = xriReference.getAuthorityPath();
                        if(authorityPath == null) {
                            target.append(delimiter).append(source);
                        } else {
                            String xrefAuthority = authorityPath.toString();
                            if("$..".equals(xrefAuthority)) {
                                if(i + 1 == reply.length && treeWildcard) {
                                    target.append(":%");
                                } else if(j == 0) {
                                    target.append(":*");
                                } else if(!wasWildcard) {
                                    target.append(subSegment);
                                }
                            } else if ("$.".equals(xrefAuthority)) {
                                target.append(subSegment);
                            } else if (xrefAuthority.startsWith("$.*")) {
                                if(i + 1 == reply.length && treeWildcard) {
                                    target.append(
                                        xrefAuthority.substring(3)
                                    ).append(
                                        '%'
                                    );
                                    break SubSegments;
                                } else if (j + 2 == jLimit && subSegmentsWildcard) {
                                    target.append(
                                        ':'
                                    ).append(
                                        xrefAuthority.substring(3)
                                    ).append(
                                        '*'
                                    );
                                    wildcard = true;
                                } else {
                                    target.append(subSegment);
                                }
                            } else if (xrefAuthority.startsWith("$.!")) {
                                if(i + 1 == reply.length && treeWildcard) {
                                    target.append(
                                        xrefAuthority.substring(2)
                                    ).append(
                                        '%'
                                    );
                                    break SubSegments;
                                } else if (j + 1 == jLimit && subSegmentsWildcard) {
                                    target.append(
                                        ':'
                                    ).append(
                                        xrefAuthority.substring(2)
                                    ).append(
                                        '*'
                                    );
                                    wildcard = true;
                                } else {
                                    target.append(subSegment);
                                }
                            } else if ("$...".equals(xrefAuthority)) {
                                target.append('%');
                            } else if (xrefAuthority.startsWith(XriAuthorities.OPENMDX_AUTHORITY)) {
                                String embeddedXri = source.substring(1, source.length() - 1);
                                target.append(
                                    delimiter
                                ).append(
                                    new Path(
                                        embeddedXri.startsWith(XRI_2Protocols.SCHEME_PREFIX) ? embeddedXri : XRI_2Protocols.SCHEME_PREFIX + embeddedXri
                                    )
                                );
                            } else {
                                target.append(
                                    delimiter
                                ).append(
                                    source
                                );
                            }
                        }
                    } else {
                        target.append(delimiter).append(source);
                    }
                }
            }
            reply[i] = unescape(target.toString());
        }
        return reply;
    }
    
    private String unescape(
        String source
    ){
        if(isCaseExactString(source)) {
            byte[] utf8 = new byte[source.length()];
            int j = 0;
            for(
                int i = 8, iLimit = source.length() - 1;
                i < iLimit;
                i++
            ){
                char c = source.charAt(i);
                if(c == '%') {
                    utf8[j++] = (byte) (
                        0x10 * digitForCharacter(source.charAt(++i)) +
                        digitForCharacter(source.charAt(++i))
                    );
                } else {
                    utf8[j++] = (byte) c;
                }
            }
            return UnicodeTransformation.toString(utf8, 0, j);   
        } else {
            return source;
        }
    }

    private static boolean isCaseExactString(
        String subSegment
    ){
        return 
            subSegment.startsWith("($t*ces*") && 
            subSegment.endsWith(")");
    }
    
    /**
     * Test whether the parenthesis are balanced
     * 
     * @param segment
     * 
     * @return true if the segmnet could be an XRef
     */
    private static boolean isCrossReference(
        String segment
    ){
        if(
            segment.startsWith("(") &&
            segment.endsWith(")")
        ){
            int open = 0;
            for(
               int i = 0, iLimit = segment.length();
               i < iLimit;
               i++
            ) {
                char c = segment.charAt(i);
                if(c == '(') {
                    ++open;
                } else if (c == ')') {
                    if(--open < 0) return false;
                }                
            }
            return open == 0;
        } else {
            return false;
        }
    }

    private static void appendCaseExactString(
        StringBuilder target,
        String source
    ){
        target.append(
            "($t*ces*"
        );
        byte[] utf8 = UnicodeTransformation.toByteArray(source);
        for(
            int i = 0;
            i < utf8.length;
            i++
        ){
            byte octet = utf8[i];
            if(
                (octet >= 'A' && octet <= 'Z') ||
                (octet >= 'a' && octet <= 'z') ||
                (octet >= '0' && octet <= '9') ||
                (octet == '-' || octet == '.' || octet == '_' || octet == '~')
            ) {
                target.append((char)octet);
            } else {
                int digits = 0xFF & octet;
                target.append(
                    '%'
                ).append(
                    characterForDigit(digits / 0x10)
                ).append(
                    characterForDigit(digits % 0x10)
                );
            }
        }
        target.append(
            ')'
        );
    }
    
    private final static char characterForDigit(
        int digit
    ){
        return (char) (
            digit < 10 ? '0' + digit : 'A' - 10 + digit
        );
    }
    
    private final static int digitForCharacter(
        char character
    ){
        return 
            character >= '0' && character <= '9' ? character - '0' :
            character >= 'A' && character <= 'Z' ? character - 'A' + 10 :
            character >= 'a' && character <= 'z' ? character - 'a' + 10 :
            -1; // invalid hexadecimal digit
    }

}
