/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: XRI_2Marshaller.java,v 1.14 2008/03/21 18:48:02 hburger Exp $
 * Description: Path/ObjectId Marshaller 
 * Revision:    $Revision: 1.14 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:48:02 $
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
package org.openmdx.compatibility.base.naming;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.UnicodeTransformation;
import org.openmdx.compatibility.base.marshalling.Marshaller;
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
        StringBuilder oid = new StringBuilder("xri://@openmdx");
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
                        encode(
                            segment.substring(0, segment.length() - 1),
                            oid, // target
                            i == 0, // authority
                            "($./", // prefix
                            ")/($...)" // suffix
                        );
                    }
                } else if (segment.startsWith(":") && segment.endsWith("*")) {
                    if (segment.length() == 2) {
                        oid.append(
                            "($.)"
                        );
                    } else {
                        encode(
                            segment.substring(1, segment.length() - 1),
                            oid, // target
                            i == 0, // authority
                            "($./", // prefix
                            ")" // suffix
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
                        xRef
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
        String subSegment = authority ? 
            source.replace(':', '.') : 
            source.replace('=', ':');
        if(isCaseExactString(source)) {
            appendCaseExactString(oid, subSegment);
        } else try {
            new XRISubSegment('*' + subSegment, true);
            oid.append(subSegment);
        } catch (XRIParseException exception) {
            appendCaseExactString(oid, subSegment);
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
        if("@openmdx".equals(authority)) return new String[]{};
        XRIPath path = xri.getXRIPath();        
        String[] reply = new String[
            path == null ? 1 : path.getNumSegments() + (treeWildcard ? 0 : 1)
        ];
        reply[0] = authority.substring(9).replaceAll(
            "\\(\\$\\.\\.\\.\\)", "%"
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
            StringBuilder target = new StringBuilder();
            boolean parameter = false;
            boolean wildcard = false;
            SubSegments: for(
                int j = 0, jLimit = segment.getNumSubSegments(); 
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
                    j == 0 ? "" :
                    ":";
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
                                } else if(!wasWildcard) {
                                    if(j > 0) target.insert(0, ':');
                                    target.append(":*");
                                }
                            } else if ("$.".equals(xrefAuthority)) {
                                XRIPath xriPath = xriReference.getXRIPath();
                                if(i + 1 == reply.length && treeWildcard) {
                                    if(xriPath != null) target.append(
                                        xriPath.toString().substring(1)
                                    );
                                    target.append('%');
                                    break SubSegments;
                                } else {
                                    target.append(":");
                                    if(xriPath != null) target.append(
                                        xriPath.toString().substring(1)
                                    );
                                    target.append('*');
                                }
                                wildcard = true;
                            } else if (xrefAuthority.startsWith("$.*")) {
                                if(i + 1 == reply.length && treeWildcard) {
                                    target.append(
                                        xrefAuthority.substring(3)
                                    ).append(
                                        '%'
                                    );
                                    break SubSegments;
                                } else {
                                    target.append(
                                        ':'
                                    ).append(
                                        xrefAuthority.substring(3)
                                    ).append(
                                        '*'
                                    );
                                }
                                wildcard = true;
                            } else if (xrefAuthority.startsWith("$.!")) {
                                if(i + 1 == reply.length && treeWildcard) {
                                    target.append(
                                        xrefAuthority.substring(2)
                                    ).append(
                                        '%'
                                    );
                                    break SubSegments;
                                } else {
                                    target.append(
                                        ':'
                                    ).append(
                                        xrefAuthority.substring(2)
                                    ).append(
                                        '*'
                                    );
                                }                                   
                                wildcard = true;
                            } else if ("$...".equals(xrefAuthority)) {
                                target.append('%');
                            } else if (xrefAuthority.startsWith("@openmdx")) {
                                target.append(
                                    delimiter
                                ).append(
                                    new Path(
                                        source.substring(1, source.length() - 1)
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
