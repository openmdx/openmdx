/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: XRI Protocol Handler
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 * ___________________________________________________________________________ 
 *
 * This class should log as it has to be loaded by the system class loader. 
 */
package org.openmdx.kernel.url.protocol.xri;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.openmdx.compatibility.kernel.url.protocol.xri.Handler_1;
import org.openmdx.kernel.url.protocol.XriAuthorities;

/**
 * A protocol handler for the 'xri' protocol. 
 */
public class Handler
    extends Handler_1 // URLStreamHandler
{

    /**
     *
     */
    private final static String ZIP_PREFIX = XriAuthorities.ZIP_AUTHORITY + "*(";

    /* (non-Javadoc)
     * @see java.net.URLStreamHandler#openConnection(java.net.URL)
     */
    @Override
    public URLConnection openConnection(
        final URL url
    ) throws IOException {
        String authority = url.getAuthority();
        if(authority == null) {
            return super.openConnection(url);
        } else if(XriAuthorities.RESOURCE_AUTHORITY.equals(authority)) {
            return new ResourceURLConnection(url);
        } else if(authority.startsWith(ZIP_PREFIX)){
            return new ZipURLConnection(url);
        } else {
            throw newMalformedURLException(url);
        }
    }

    /* (non-Javadoc)
     * @see java.net.URLStreamHandler#parseURL(java.net.URL, java.lang.String, int, int)
     */
    @Override
    protected void parseURL(
        URL u, 
        String spec, 
        int start, 
        int limit
    ) {
        //
        // These fields may receive context content if this was relative URL
        //
        String protocol = u.getProtocol();
        String authority = u.getAuthority(); 
        String userInfo = u.getUserInfo();
        String host = u.getHost();
        int port = u.getPort();
        String path = u.getPath();
        String query = u.getQuery();
        //
        // Parse
        //
        int pathStart;
        int authorityStart = start + 2;
        if(
            start + 1 < limit &&
            spec.charAt(start) == '/' &&
            spec.charAt(start+1) == '/'
        ){
            int openParenthesis = 0;
            pathStart = -1;
            ParseAuthority: for(
                int cursor = authorityStart;
                cursor < limit;
                cursor++
            ) {
                switch (spec.charAt(cursor)) {
                    case '(':
                        openParenthesis++;
                        break;
                    case ')':
                        openParenthesis--;
                        break;
                    case '/':
                        if(openParenthesis == 0) {
                            authority = spec.substring(authorityStart, cursor);
                            pathStart = cursor;
                            break ParseAuthority;
                        } else {
                            break;
                        }
                }
            }
            if(openParenthesis != 0) {
                throw new IllegalArgumentException(
                    "More " + 
                    (openParenthesis > 0 ? "opening than closing" : "closing than opening") +
                    " parenthesis in authority: " + spec
                );
            }
        } else {
            pathStart = start;
        }
        if(pathStart < 0) {
            authority = spec.substring(authorityStart, limit);
        } else {
            int pathEnd = limit;
            int openParenthesis = 0;
            ParsePath: for(
                int cursor = pathStart;
                cursor < limit;
                cursor++
            ){
                switch (spec.charAt(cursor)) {
                    case '(':
                        openParenthesis++;
                        break;
                    case ')':
                        openParenthesis--;
                        break;
                    case '?': 
                        if(openParenthesis == 0) {
                            pathEnd = cursor;
                            query = spec.substring(cursor + 1, limit);
                            break ParsePath;
                        } else {
                            break;
                        }
                }
            }
            if(openParenthesis != 0) {
                throw new IllegalArgumentException(
                    "More " + 
                    (openParenthesis > 0 ? "opening than closing" : "closing than opening") +
                    " parenthesis in path: " + spec
                );
            }
            String pathSpec = spec.substring(pathStart, pathEnd);
            int contextEnd = -1;
            if(pathSpec.length() > 0) {
                if(path != null && pathSpec.charAt(0) != '/') {
                    ParsePath: for(
                        int cursor = path.length() - 1;
                        cursor >= 0;
                        cursor--
                    ){
                        switch (path.charAt(cursor)) {
                            case '(':
                                openParenthesis++;
                                break;
                            case ')':
                                openParenthesis--;
                                break;
                            case '/': 
                                if(openParenthesis == 0) {
                                    contextEnd = cursor + 1;
                                    break ParsePath;
                                } else {
                                    break;
                                }
                        }
                    }
                }
                path = normalizePath(
                    contextEnd < 0 ? pathSpec : path.substring(0, contextEnd) + pathSpec
                );
            }
        }        
        //
        // Propagate fields to oURL
        //
        setURL(
            u, 
            protocol, 
            host, 
            port, 
            authority, 
            userInfo, 
            path, 
            query, 
            u.getRef() // This field has already been parsed
        );
    }

    /**
     * Normalize the path according to the chapter
     * <em>Relative XRI References</em> in
     * <pre>
     * OASIS
     * Extensible Resource Identifier (XRI) Syntax V2.0
     * Committee Specification, 14 November 2005
     * (http://docs.oasis-open.org/xri/V2.0)
     * </pre>
     * 
     * @param source the unnormalized path
     * 
     * @return the normalized path
     */
    private String normalizePath(
        String source
    ){
        List<String> segments = new ArrayList<String>();
        int openParenthesis = 0;
        int start = 0;
        for(
            int cursor = 0, limit = source.length();
            cursor < limit;
            cursor++
        ){
            switch (source.charAt(cursor)) {
                case '(':
                    openParenthesis++;
                    break;
                case ')':
                    openParenthesis--;
                    break;
                case '/': 
                    if(openParenthesis == 0) {
                        segments.add(
                            source.substring(start, cursor)
                        );
                        start = cursor + 1;
                    }
                    break;
            }
        }
        segments.add(source.substring(start));
        boolean end = true;
        int pendingRemoval = 0;
        for(
            ListIterator<String> i = segments.listIterator(segments.size());
            i.hasPrevious();
        ){
            String segment = i.previous();
            if(".".equals(segment)){
                if(end) {
                    i.set("");
                    end = false;
                } else {
                    i.remove();
                }
            } else if ("..".equals(segment)) {
                if(end) {
                    i.set("");
                    end = false;
                } else {
                    i.remove();
                }
                pendingRemoval++;
            } else if (pendingRemoval > 0){
                pendingRemoval--;
                if(i.hasPrevious()) {
                    i.remove();
                }
            } else {
                end = false;
            }
        }
        StringBuilder target = new StringBuilder();
        String separator = "";
        for(String segment : segments) {
            target.append(separator).append(segment);
            separator = "/";
        }
        return target.toString();
    }
    
    /* (non-Javadoc)
     * @see java.net.URLStreamHandler#hashCode(java.net.URL)
     */
    @Override
    protected int hashCode(URL u) {
        String a = u.getAuthority();
        int h = a == null ? 0 : a.hashCode();
        String f = u.getFile();
        return f == null ? h : h + f.hashCode();
    }

    /* (non-Javadoc)
     * @see java.net.URLStreamHandler#hostsEqual(java.net.URL, java.net.URL)
     */
    @Override
    protected boolean hostsEqual(URL u1, URL u2) {
        String authority1 = u1.getAuthority();
        String authority2 = u2.getAuthority();
        return authority1 == null ? authority2 == null : authority1.equals(authority2); 
    }

    /**
     * Unsupported authority exception
     * 
     * @param url an <code>URL</code> which can't be handled by this handler
     * 
     * @return a <code>MalformedURLException</code> for the given <code>URL</code>.
     */
    @Override
    protected MalformedURLException newMalformedURLException(
        URL url
    ){
        return new MalformedURLException(
            getClass().getName() + 
            " supports only XRI authorities starting with " + 
            XriAuthorities.RESOURCE_AUTHORITY + " or " + 
            XriAuthorities.ZIP_AUTHORITY + ": " + url
        );
    }

}
