/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: XRI 2 Marshaller 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.text.conversion.URITransformation;
import org.openmdx.base.text.conversion.UnicodeTransformation;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.xri.XRI_2Protocols;
import org.openmdx.kernel.xri.XRIAuthorities;

/**
 * XRI 2 Marshaller
 */
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
     * 
     */
    final Pattern OPENMDX_OBJECT_AUTHORITY = Pattern.compile(
        XRIAuthorities.OPENMDX_AUTHORITY + "[*][A-Za-z0-9\\-]+([.][A-Za-z0-9\\-]+)*"
    );
    
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
            String segment = segments[i].toString();
            if(i > 0 || !segment.startsWith("!")) {
                oid.append(
                    separator
                );
            }
            if(isForeignCrossReference(segment)) {
                //
                // Foreign Cross Reference
                //
                encode(
                    segment, // source
                    oid, // target
                    i == 0, // authority
                    "", // prefix
                    "" // suffix
                );
            } else if (segment.indexOf('/') < 0) {
                //
                // No Cross Reference
                //
                int percent = segment.indexOf('%');
                if(percent >= 0) {
                    //
                    // Percent Sign
                    //
                    if(percent+1 == segment.length() && i+1 == segments.length) {
                        //
                        // Trailing Percent Sign (leads to "$..." wildcard)
                        //
                        if(segment.length() == 1) {
                            oid.append(
                                "($...)"
                            );
                        } else {
                            List<String> subSegments = parseSubSegments(true, segment.substring(0, segment.length() - 1));
                            if(subSegments == null) {
                                appendCaseExactString(
                                    oid,
                                    segment
                                );
                            } else {
                                StringBuilder target = new StringBuilder();
                                boolean first = true;
                                for(
                                     Iterator<String> j = subSegments.iterator();
                                     j.hasNext();
                                     first = false
                                ){
                                     String subSegment = j.next();
                                     String value = first && subSegment.startsWith("*") ? subSegment.substring(1) : subSegment;
                                     if(j.hasNext()) {
                                         target.append(value);
                                     } else {
                                         if(!first) {
                                             target.append('*');
                                         }
                                         if (value.length() > 0) {
                                             target.append(
                                                 "($.*"
                                             ).append(
                                                 value
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
                        }
                    } else {
                        //
                        // Embedded Percent Sign (requires escaping)
                        //
                        appendCaseExactString(
                            oid,
                            segment
                        );
                    }
                } else if (segment.startsWith(":") && segment.endsWith("*")) {
                    //
                    // "$.." wildcard
                    //
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
                //
                // openMDX cross reference
                //
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

	static String xriRepresentationOfGeneralSegment(
		boolean authority,	
		String classicRepresentation
	) {
		StringBuilder xriRepresentation = new StringBuilder();
		if(isForeignCrossReference(classicRepresentation)) {
		    //
		    // Foreign Cross Reference
		    //
		    encode(
		        classicRepresentation, // source
		        xriRepresentation, // target
		        authority, // authority
		        "", // prefix
		        "" // suffix
		    );
		} else {
		    //
		    // No Cross Reference
		    //
		    int percent = classicRepresentation.indexOf('%');
		    if(percent >= 0) {
	            //
	            // Embedded Percent Sign (requires escaping)
	            //
	            appendCaseExactString(
	                xriRepresentation,
	                classicRepresentation
	            );
		    } else {
		        encode(
		            classicRepresentation, // source
		            xriRepresentation, // target
			        authority, // authority
		            "", // prefix
		            "" // suffix
		        );
		    }
		}
		return xriRepresentation.toString();
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
     */
    private static void encode (
        String source,
        StringBuilder oid, 
        boolean authority, 
        String prefix, 
        String suffix
    ){
        oid.append(prefix);
        String xriSegment = authority ? source.replace(':', '.') : source;
        if(isCaseExactString(source)) {
            appendCaseExactString(oid, xriSegment);
        } else {
            Segment segment = new Segment(
                true, 
                authority, 
                source
            );
            if(segment.isValid()) {
                oid.append(xriSegment);
            } else {
                appendCaseExactString(oid, xriSegment);
            }
        }
        oid.append(suffix);
    }
    
    /**
     * Unmarshal a CharSequence into a CharSequence[].
     * 
     * @param         charSequence
     *            A string containing a marshalled sequence of objects
     * 
     * @return    A String array containing the unmarshaled sequence
     *                  of objects.
     * @exception ServiceException ILLEGAL_ARGUMENT
    */
    public Object unmarshal (
        Object charSequence
    ) throws ServiceException {
        final String xriString = SpecialResourceIdentifiers.unescapeResourceIdentifier(charSequence.toString());
        try {
            final boolean treeWildcard = xriString.endsWith(")/($...)");
            final List<Segment> segments = getObjectIdentifier(false, xriString);
            final String authority = segments.get(0).toString();
            final String[] reply;
            if(segments.size() == 1 && XRIAuthorities.OPENMDX_AUTHORITY.equals(authority)) {
                reply = ROOT;
            } else {
                reply = new String[
                        segments.size() - (treeWildcard ? 1 : 0)
                        ];
                reply[0] = authority.substring(
                        authority.charAt(8) == '*' ? 9 : 8
                ).replaceAll(
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
                for (
                        int i = 1;
                        i < reply.length;
                        i++
                ) {
                    Segment segment = segments.get(i);
                    List<String> subSegments = segment.getSubSegments();
                    int jLimit = subSegments.size();
                    boolean subSegmentsWildcard = segment.getSubSegmentAt(jLimit - 1).equals("*($..)");
                    StringBuilder target = new StringBuilder();
                    boolean parameter = false;
                    boolean wildcard = false;
                    SubSegments:
                    for (
                            int j = 0;
                            j < jLimit;
                            j++
                    ) {
                        boolean wasParameter = parameter;
                        boolean wasWildcard = wildcard;
                        parameter = false;
                        wildcard = false;
                        String subSegment = segment.getSubSegmentAt(j);
                        String source = subSegment.toString().substring(1);
                        String delimiter =
                                wasParameter ? "=" :
                                        subSegment.startsWith("!") ? "!" :
                                                j == 0 ? "" :
                                                        "*";
                        parameter = false;
                        if (subSegment.startsWith("(", 1)) {
                            List<Segment> xriReference = parseCrossReference(false, subSegment);
                            if (xriReference != null) {
                                Segment authorityPath = xriReference.get(0);
                                if (authorityPath == null) {
                                    target.append(delimiter).append(source);
                                } else {
                                    String xrefAuthority = authorityPath.toString();
                                    if ("$..".equals(xrefAuthority)) {
                                        if (i + 1 == reply.length && treeWildcard) {
                                            target.append(":%");
                                        } else if (j == 0) {
                                            target.append(":*");
                                        } else if (!wasWildcard) {
                                            target.append(subSegment);
                                        }
                                    } else if ("$.".equals(xrefAuthority)) {
                                        target.append(subSegment);
                                    } else if (xrefAuthority.startsWith("$.*")) {
                                        if (i + 1 == reply.length && treeWildcard) {
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
                                        if (i + 1 == reply.length && treeWildcard) {
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
                                    } else if (OPENMDX_OBJECT_AUTHORITY.matcher(xrefAuthority).matches()) {
                                        String embeddedXri = source.substring(1, source.length() - 1);
                                        target.append(
                                                delimiter
                                        ).append(
                                                new Path(
                                                        embeddedXri.startsWith(XRI_2Protocols.SCHEME_PREFIX) ? embeddedXri : XRI_2Protocols.SCHEME_PREFIX + embeddedXri
                                                ).toClassicRepresentation()
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
                        } else {
                            target.append(delimiter).append(source);
                        }
                    }
                    reply[i] = unescape(target.toString());
                }
            }
            return reply;
        } catch (RuntimeServiceException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.TRANSFORMATION_FAILURE,
                "Input is not a valid resource identifier",
                new BasicException.Parameter(BasicException.Parameter.XRI, xriString)
            );
        }
    }
    
    private String unescape(
        String source
    ){
        return isCaseExactString(source) ?
            URITransformation.decode(source.substring(8, source.length() - 1)) :
            source;
    }

    private static boolean isCaseExactString(
        String subSegment
    ){
        return 
            subSegment.startsWith("($t*ces*") && 
            subSegment.indexOf(')') == subSegment.length() - 1;
    }

    /**
     * Test whether the parenthesis are balanced
     * 
     * @param segment the segment to be analyzed
     * 
     * @return {@code true} if the segment could be an XRef
     */
    private static boolean isForeignCrossReference(
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
    
    /**
     * Tests whether the String represents an openMDX object identifier
     * 
     * @param lenient tells whether exceptions are thrown or mapped to {@code null} return values
     * @param xri the XRI 2 candidate
     *
     * @return the Segment List if the String represents an absolute extensible resource or {@code null} in case of lenient mismatch
     * @throws RuntimeServiceException in case of non-lenient mismatch
     */
    private static List<Segment> getObjectIdentifier(
        boolean lenient, 
        String xri
    ){
        return xri.startsWith(XRI_2Protocols.OPENMDX_PREFIX) ? getExtensibleResourceIdentifier(lenient, xri) : null;
    }

    /**
     * Tests whether the String represents an absolute extensible resource identifier without scheme
     * 
     * @param lenient tells whether exceptions are thrown or mapped to {@code null} return values
     * @param resourceIdentifier the XRI 2 candidate with an optional scheme
     * 
     * @return the Segment List if the String represents an absolute extensible resource or {@code null} in case of lenient mismatch
     * @throws RuntimeServiceException in case of non-lenient mismatch
     */
    private static List<Segment> getExtensibleResourceIdentifier(
        boolean lenient, 
        String resourceIdentifier
    ){
        final String xri;
        if(resourceIdentifier.startsWith(XRI_2Protocols.SCHEME_PREFIX)) {
            xri = resourceIdentifier.substring(XRI_2Protocols.SCHEME_PREFIX.length());
        } else {
            xri = resourceIdentifier;
        }
        if(xri.startsWith("!")) { 
            //
            // pgcs-authority
            //
            List<Segment> segments = parseSegments(lenient, xri);
            if(segments == null || !segments.isEmpty()) {
                if(lenient) {
                    return null;
                } else {
                    throw new RuntimeServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.TRANSFORMATION_FAILURE,
                        "The global context symbol '!' must be followed by a non-empty authority segment",
                        new BasicException.Parameter(BasicException.Parameter.XRI, xri),
                        new BasicException.Parameter("position", 0)
                    );
                }
            } else {
                List<String> authority = segments.get(0).xriSubSegments;
                return authority.isEmpty() || authority.get(0).length() <= 1 ? null : segments;
            }
        } else if(xri.startsWith("=") || xri.startsWith("@") || xri.startsWith("+") || xri.startsWith("$")) {
            //
            // rgcs-authority
            //
            return parseSegments(lenient, xri);
        } else {
            //
            // NO gcs-authority
            //
            if(lenient) {
                return null;
            } else {
                throw new RuntimeServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.TRANSFORMATION_FAILURE,
                    "The XRI should start with a valid context symbol",
                    new BasicException.Parameter(BasicException.Parameter.XRI, xri),
                    new BasicException.Parameter("position", 0),
                    new BasicException.Parameter("expected", "!", "=", "@", "+", "$")
                );
            }
        }
    }

    /**
     * Retrieve the segments of an absolute extensible resource identifier without scheme
     * 
     * @param lenient tells whether exceptions are thrown or mapped to {@code null} return values
     * @param path the path tobe parsed
     * 
     * @return the segments of an absolute extensible resource identifier without scheme
     */
    private static List<Segment> parseSegments(
        boolean lenient, 
        String path
    ){
        List<Segment> segments = new ArrayList<Segment>();
        int xRef = 0;
        int anchor = 0;
        for(
            int  i = 1, limit = path.length();
            i < limit;
            i++
        ){
            char c = path.charAt(i);
            if(c == '(') {
                xRef++;
            } else if(xRef == 0) {
                if(c == '/') {
                    Segment segment = new Segment(
                        lenient,
                        anchor == 0, path.substring(anchor, i)
                    );
                    if(segment.isValid()) {
                        segments.add(segment);
                        anchor = i + 1;
                    } else {
                        return null;
                    }
                } else if (c == ')') {
                    if(lenient) {
                        return null;
                    } else {
                        throw new RuntimeServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.TRANSFORMATION_FAILURE,
                            "The XRI contains a closing parenthesis without matching opening parenthesis",
                            new BasicException.Parameter(BasicException.Parameter.XRI, path),
                            new BasicException.Parameter("position", i)
                        );
                    }
                }
            } else if(c == ')') {
                xRef--;
            }
        }
        Segment segment = new Segment(
            lenient,
            anchor == 0, path.substring(anchor)
        );
        if(xRef == 0 && segment.isValid()) {
            segments.add(segment);
            return segments;
        } else {
            return null;
        }
    }

    /**
     * Retrieve the sub-segments
     * 
     * @param lenient tells whether exceptions are thrown or mapped to {@code null} return values
     * @param segment the segment to be parsed
     * 
     * @return the sub-segments, or {@code null} in case of parse failure
     */
    static List<String> parseSubSegments(
        boolean lenient, 
        String segment
    ){
        List<String> subSegments = new ArrayList<>();
        int xRef = 0;
        int anchor;
        char type;
        if(segment.startsWith("*") || segment.startsWith("!")) {
            anchor = 1;
            type = segment.charAt(0);
        } else {
            anchor = 0;
            type = '*';
        }
        for(
            int i = anchor, limit = segment.length();
            i < limit;
            i++
        ){
            char c = segment.charAt(i);
            if(xRef == 0) {
                if(c == ')') {
                    return null;
                } else if (c == '(') {
                    if(i == anchor) {
                        xRef++;
                    } else {
                        return null;
                    }
                } else if (c == '*' || c == '!') {
                    String subSegment = parseSubSegment(
                        lenient,
                        type + segment.substring(anchor, i)
                    );
                    if(subSegment == null) {
                        return null;
                    } else {
                        subSegments.add(subSegment);
                        type = c;
                        anchor = i + 1;
                    }
                }
            } else {
                if(c == ')') {
                    xRef--;
                } else if (c == '(') {
                    xRef++;
                }
            }
        }
        if(xRef == 0) {
            String subSegment = parseSubSegment(
                lenient,
                type + segment.substring(anchor)
            );
            if(subSegment == null) {
                return null;
            } else {
                subSegments.add(subSegment);
                return subSegments;
            }
        } else {
            if(lenient) {
                return null;
            } else {
                throw new RuntimeServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.TRANSFORMATION_FAILURE,
                    "Incomplete cross reference: Bot all parenthesis are closed",
                    new BasicException.Parameter("segment",segment)
                );
            }
        }
    }

    /**
     * Parse the cross reference's segments
     * 
     * @param lenient tells whether exceptions are thrown or mapped to {@code null} return values
     * @param subSegment the sub-segment to be parsed
     * 
     * @return the cross reference's content
     */
    List<Segment> parseCrossReference(
        boolean lenient, 
        String subSegment
    ){
        return (subSegment.startsWith("*(") || subSegment.startsWith("!(")) && subSegment.endsWith(")")  ? 
              parseSegments(lenient, subSegment.substring(2, subSegment.length() - 1)) : 
              null;
    }
    
    /**
     * Validate a single sub-segment with delimiter
     * 
     * @param lenient tells whether exceptions are thrown or mapped to {@code null} return values
     * @param subSegment the sub-segment to be parsed
     * 
     * @return the subSegment, or {@code null} if it is not valid
     */
    private static String parseSubSegment(
        boolean lenient,
        String subSegment
    ){
        int limit = subSegment.length();
        if(subSegment.startsWith("(", 1)) {
            if(subSegment.endsWith(")")) {
                String xri = subSegment.substring(2, limit -1 );
                try {
                    if (getExtensibleResourceIdentifier(true, xri) != null) {
                        return subSegment;
                    } else {
                        new URI(xri);
                        return subSegment;
                    }
                } catch (URISyntaxException exception) {
                    if(lenient) {
                        return null;
                    } else {
                        throw new RuntimeServiceException(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.TRANSFORMATION_FAILURE,
                            "Bad cross reference; It cntains neither an XRI nor an URI",
                            new BasicException.Parameter("subSegment", subSegment),
                            new BasicException.Parameter("xir", xri)
                        );
                    }
                }
            } else {
                if(lenient) {
                    return null;
                } else {
                    throw new RuntimeServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.TRANSFORMATION_FAILURE,
                        "Bad cross reference; The sub-segment starts with a parenthesis but does not end with one",
                        new BasicException.Parameter("subSegment", subSegment)
                    );
                }
            }
        } else {
            int expectedHexadecimalDigits = 0;
            for(
                int pos = 1;
                pos < limit;
                pos++
            ){
                char c = subSegment.charAt(pos);
                if(expectedHexadecimalDigits > 0) {
                    if(Character.digit(c, 16) < 0) {
                        //
                        // HEXDIG
                        //
                        if(lenient) {
                            return null;
                        } else {
                            throw new RuntimeServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.TRANSFORMATION_FAILURE,
                                "Bad escape sequence; The percent sign must be followed by two hexadecimal digits",
                                new BasicException.Parameter("subSegment", subSegment),
                                new BasicException.Parameter("position", pos),
                                new BasicException.Parameter("digit", expectedHexadecimalDigits == 2 ? "first" : "second")
                            );
                        }
                    } else {
                        expectedHexadecimalDigits--;
                    }
                } else if (c == '%') {
                    //
                    // pct-encoded
                    //
                    expectedHexadecimalDigits = 2; 
                } else if(!XRISegment.isPChar(c)) { 
                    // 
                    // NOT xri-pchar
                    // 
                    if(lenient) {
                        return null;
                    } else {
                        throw new RuntimeServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.TRANSFORMATION_FAILURE,
                            "The sub-segment contains a character which is not a valid xri-pchar",
                            new BasicException.Parameter("subSegment", subSegment),
                            new BasicException.Parameter("position", pos),
                            new BasicException.Parameter("character", c)
                        );
                    }
                }
            }
            if(expectedHexadecimalDigits == 0) {
                return subSegment;
            } else {
                if(lenient) {
                    return null;
                } else {
                    throw new RuntimeServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.TRANSFORMATION_FAILURE,
                        "Incomplete escape sequence; The percent sign must be followed by two hexadecimal digits",
                        new BasicException.Parameter("subSegment", subSegment),
                        new BasicException.Parameter("position", limit),
                        new BasicException.Parameter("digit", expectedHexadecimalDigits == 2 ? "first" : "second")
                    );
                }
            }
        }
    }
    
    /**
     * Determines whether to segments match
     * 
     * @param pathSegment the segment to be copared with the pattern
     * @param patternSegment the pattern against which the segment is to be compared
     * 
     * @return {@code true} if the segments match
     */
    private static boolean segmentMatchesPattern(
        Segment pathSegment,
        Segment patternSegment
    ){
        List<String> pathSubSegments = pathSegment.getSubSegments();
        List<String> patternSubSegments = patternSegment.getSubSegments();
        int i = 0, limit = pathSubSegments.size();
        for(String patternSubSegment : patternSubSegments) {
            if("*($..)".equals(patternSubSegment)) {
                return true;
            }
            if(i == limit) {
                return false;
            }
            String pathSubSegment = pathSubSegments.get(i++);
            if(
                !pathSubSegment.equals(patternSubSegment) &&
                !"*($.)".equals(patternSubSegment) &&
                !(patternSubSegment.startsWith("*($.") && pathSubSegment.startsWith(patternSubSegment.substring(4, patternSubSegment.length() - 1))) 
            ) {
                return false;
            }
        }
        return i == limit;
    }
    
    /**
     * Determines whether to segments match
     *
     * @param candidate the segment to be copared with the pattern
     * @param pattern the pattern against which the candidate is to be compared
     * 
     * @return {@code true} if the segments match
     */
    static boolean segmentMatchesPattern(
        String candidate,
        String pattern
    ){
    	Segment candidateSegment = new Segment(false, true, candidate);
    	Segment patternSegment = new Segment(false, true, pattern);
    	return 
    		candidateSegment.isValid() &&
    		patternSegment.isValid() &&
    		segmentMatchesPattern(candidateSegment, patternSegment);
    }
    
    /**
     * Determines whether the path corresponds to the pattern.<ul>
     * <li>{@code ($.)} matches any sub-segment
     * <li>{@code ($.*&lsaquo;prefix&rsaquo;)} matches a re-assignable sub-segment with the given prefix
     * <li>{@code ($.!&lsaquo;prefix&rsaquo;)} matches a persistent sub-segment with the given prefix
     * <li>{@code ($..)} matches any number of sub-segments, including 0<br>
     * <em>Note: This wildcard only allowed as last sub-segment in a segment.</em>
     * <li>{@code ($...)} matches any number of segments, including 0<br>
     * <em>Note: This wildcard only allowed as last segment in a path.</em>
     * </ul>
     *
     * @return {@code true} if this path matches the pattern
     */
    static boolean pathMatchesPattern(
        String path,
        String pattern
    ){
        List<Segment> pathSegments = getExtensibleResourceIdentifier(true, path);
        List<Segment> patternSegments = getExtensibleResourceIdentifier(true, pattern); 
        if(pathSegments == null || patternSegments == null) {
            return false;
        }
        int i = 0, limit = pathSegments.size();
        for(Segment patternSegment : patternSegments) {
            if("($...)".equals(patternSegment.toString())) {
                return true;
            }
            if(i == limit || !segmentMatchesPattern(pathSegments.get(i++), patternSegment)) {
                return false;
            }
        }
        return i == limit;
    }

    
    //------------------------------------------------------------------------
    // Class Segment
    //------------------------------------------------------------------------
    
    /**
     * XRI Segment
     */
    private static class Segment {
        
        /**
         * Constructor 
         *
         * @param lenient tells whether the status is retrieved via isValid() or exceptions are thrown
         */
        Segment(
            boolean lenient,
            boolean authority, 
            String xriSegment
        ){
            this.xriSegment = xriSegment;
            this.xriSubSegments = XRI_2Marshaller.parseSubSegments(
                lenient, authority ? xriSegment.substring(1) : xriSegment
            );
        }

        final String xriSegment;
        final List<String> xriSubSegments;

        @Override
        public String toString(){
            return this.xriSegment;
        }
        
        List<String> getSubSegments(
        ){
            return this.xriSubSegments;
        }

        String getSubSegmentAt(
            int index
        ){
            return this.xriSubSegments.get(index);
        }
                
        boolean isValid(){
            return this.xriSubSegments != null;
        }
        
    }

}
