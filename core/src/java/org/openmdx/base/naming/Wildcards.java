/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Wildcards.java,v 1.1 2009/01/06 13:14:44 wfro Exp $
 * Description: Wildcards
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/06 13:14:44 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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

import java.util.Iterator;

import org.openxri.AuthorityPath;
import org.openxri.XRIPath;
import org.openxri.XRIReference;
import org.openxri.XRISegment;
import org.openxri.XRISubSegment;
import org.openxri.XRef;

/**
 * Wildcards
 */
@SuppressWarnings("unchecked")
class Wildcards {

    /**
     * Constructor 
     */
    protected Wildcards(
    ) {        
        // Avoid instantiation
    }
    
    //--------------------------------------------------------------------------
    // Verification
    //--------------------------------------------------------------------------
    
    /**
     * Test whether a given path segment matches a given pattern
     * @param path
     * @param pattern
     * 
     * @return <code>true</code> if the path matches th epattern
     */
    private static boolean isLike(
        XRISegment path,
        boolean skipPathStart,
        XRISegment pattern,
        boolean skipPatternStart
    ){
        Iterator<XRISubSegment> patternIterator = pattern.getSubSegmentIterator();
        if(skipPatternStart) {
            if(patternIterator.hasNext()) {
                patternIterator.next();
            } else {
                return false;
            }
        }
        Iterator<XRISubSegment> pathIterator = path.getSubSegmentIterator();
        if(skipPathStart) {
            if(pathIterator.hasNext()) {
                pathIterator.next();
            } else {
                return false;
            }
        }
        SubSegments: while(patternIterator.hasNext()) {
            XRISubSegment patternSubSegment = patternIterator.next();
            String patternString = patternSubSegment.toString(); 
            if("*($..)".equals(patternString)){
                return true;
            }
            if(!pathIterator.hasNext()) {
                return false;
            }
            XRISubSegment pathSubSegment = pathIterator.next();
            if("*($.)".equals(patternString) || patternSubSegment.equals(pathSubSegment)) {
                continue SubSegments;
            }
            XRef patternXRef = patternSubSegment.getXRef();
            if(patternXRef == null) {
                return false;
            }
            XRIReference patternXRIReference = patternXRef.getXRIReference();
            if(patternXRIReference != null) {
                AuthorityPath patternAuthorityPath = patternXRIReference.getAuthorityPath();
                if(patternAuthorityPath != null) {
                    String patternAuthorityString = patternAuthorityPath.toString();
                    if(patternAuthorityString.startsWith("$.")) {
                        if(pathSubSegment.toString().startsWith(patternAuthorityString.substring(2))) {
                            continue SubSegments;
                        } else {
                            return false;
                        }
                    }
                }
            }
            XRef pathXRef = pathSubSegment.getXRef();
            if(
                pathXRef == null ||
                !isLike(pathXRef.getXRIReference(), patternXRIReference)
            ){    
                return false;
            }
        }
        return !pathIterator.hasNext();
    }
    
    /**
     * Test whether a given authority path matches a given pattern
     * @param path
     * @param pattern
     * 
     * @return <code>true</code> if the path matches the pattern
     */
    protected static boolean isLike(
        AuthorityPath path,
        AuthorityPath pattern
    ){
        if(pattern == null || path == null) {
            return pattern == path;
        } else if (pattern.equals(path)) {
            return true;
        }
        String patternString = pattern.toString();
        String pathString = path.toString();
        if(patternString.length() == 0 || pathString.length() == 0) {
            return patternString.length() == pathString.length();
        }
        char pathStart = pathString.charAt(0);
        boolean crossReferenceAuthority = pathStart == '(';
        if(patternString.startsWith("($.)")) {
            return crossReferenceAuthority ? isLike(
                new XRISegment(pathString), true,
                new XRISegment(patternString), true
            ) : isLike(
                new XRISegment(pathString.substring(1)), false,
                new XRISegment(patternString), true
            );
        }
        if(patternString.charAt(0) == pathStart) {
            return crossReferenceAuthority ? isLike(
                new XRISegment(pathString), false,
                new XRISegment(patternString), false
            ) : isLike(
                new XRISegment(pathString.substring(1)), false,
                new XRISegment(patternString.substring(1)), false
            );
        }
        return false;
    }

    /**
     * Test whether a given path matches a given pattern
     * @param path
     * @param pattern
     * 
     * @return <code>true</code> if the path matches the pattern
     */
    protected static boolean isLike(
        XRIPath path,
        XRIPath pattern
    ){
        Iterator<XRISegment> patternIterator = pattern.getSegmentIterator();
        Iterator<XRISegment> pathIterator = path.getSegmentIterator();
        while(patternIterator.hasNext()) {
            XRISegment patternSegment = patternIterator.next();
            String patternString = patternSegment.toString();
            if("($...)".equals(patternString)) {
                return true;
            }
            if( 
                !pathIterator.hasNext() ||
                !isLike(pathIterator.next(), false, patternSegment, false)
            ) {
                return false;
            }            
        }
        return !pathIterator.hasNext();
    }
    
    /**
     * Determines whether the path corresponds to the pattern.<ul>
     * <li><code>($.)</code> matches any sub-segment
     * <li><code>($.*&lsaquo;prefix&rsaquo;)</code> matches a re-assignable sub-segment with the given prefix
     * <li><code>($.!&lsaquo;prefix&rsaquo;)</code> matches a persistent sub-segment with the given prefix
     * <li><code>($..)</code> matches any number of sub-segments, including 0<br>
     * <em>Note: This wildcard only allowed as last sub-segment in a segment.</em>
     * <li><code>($...)</code> matches any number of segments, including 0<br>
     * <em>Note: This wildcard only allowed as last segment in a path.</em>
     * </ul>
     * @param pattern
     * 
     * @return <code>true</code> if this path matches the pattern
     */
    public static boolean isLike(
        XRIReference path,
        XRIReference pattern
    ){
        return 
            isLike(path.getAuthorityPath(), pattern.getAuthorityPath()) &&
            isLike(path.getXRIPath(), pattern.getXRIPath());
    }
        
}
