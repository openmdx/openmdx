/*
 * ====================================================================
 * Project:     opencrx, http://www.opencrx.org/
 * Description: HttpEncoder class
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, CRIXP Corp., Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of CRIXP Corp. nor the names of the contributors
 *   to openCRX may be used to endorse or promote products derived
 *   from this software without specific prior written permission
 * 
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
 * 
 * This product includes software developed by contributors to
 * openMDX (http://www.openmdx.org/)
 */
package org.openmdx.base.text.conversion;

public class HtmlEncoder {

    //-----------------------------------------------------------------------
    private HtmlEncoder(
    ) {
        super();
    }

    //-----------------------------------------------------------------------
    /**
     * Html encodes the given string. xssChars and unknown html tags are escaped.
     * 
     */
    public static String encode(
        String s,
        boolean forEditing
    ) {
        if(s == null) return null;
        int len = s.length();
        if(len < 1) {
            return s;
        }
        StringBuilder target = new StringBuilder();
        int i = 0;
        while(i < len) {
            char c = s.charAt(i);
            // Test for known tag
            boolean isKnownTag = false;
            if(c == '<' || c == '&') {
                for(int j = 0; j < KNOWN_TAGS.length; j++) {
                    if(s.toLowerCase().startsWith(KNOWN_TAGS[j], i)) {
                        int pos = 0;
                        if(KNOWN_TAGS[j].startsWith("&")) {
                            pos = s.indexOf(';', i);
                        }
                        else {
                            pos = s.indexOf('>', i);
                        }
                        if(pos > i) {
                            target.append(s.substring(i, pos+1));
                            i = pos + 1;
                            isKnownTag = true;
                            break;
                        }
                    }
                }
            }
            // Test for xssChars
            if(!isKnownTag) {
                int k = c - '"';
                if(
                    (k >= 0) && 
                    (k < XSS_CHARS.length) && 
                    (XSS_CHARS[k] != null) && 
                    !s.startsWith("&#", i)
                ) {
                    target.append(XSS_CHARS[k]);
                } 
                else if(c >= 128) {            
                    target
                    .append("&#")
                    .append(Integer.toString(c))
                    .append(";");                                
                }
                else {
                    target.append(c);
                }
                i++;
            }
        }
        String t = target.toString();
        return t;
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final String XSS_CHARS[] = {
        "&quot;", null, null, null, "&amp;", "&#39;", null, null, null, null,
        null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, "&#59;", "&lt;", null, "&gt;", null, "&#64;"
    };
    private static final String KNOWN_TAGS[] = {
        "&nbsp;", "&quot;", "&amp;", "&lt;", "&gt;",
        "<b>", "</b>", "<i>", "</i>", "<u>", "</u>", "<big>", "</big>",
        "<em>", "</em>", "<small>", "</small>", "<strong>", "</strong>",
        "<sub>", "</sub>", "<sup>", "</sup>", "<u>", "</u>", "<h1>",
        "<pre>", "</pre>", "</h1>", "<h2>", "</h2>", "<h3>", "</h3>", "<h4>",
        "</h4>", "<h5>", "</h5>", "<h6>", "</h6>", "<center>", "<br>", "<br />",
        "<ul>", "</ul>", "<ol>", "</ol>", "<li>", "</li>",
        "<font", "</font>",
        "<del>", "</del>",
        "<tbody>", "</tbody>",
        "<caption>", "</caption>",
        "<table", "</table>",
        "<a ", "<a>", "</a>",
        "<th ", "<th>", "</th>",
        "<td ", "<td>", "</td>",
        "<tr ", "<tr>", "</tr>",
        "<p ", "<p>", "</p>",
        "<hr />"
    };

}

//--- End of File -----------------------------------------------------------
