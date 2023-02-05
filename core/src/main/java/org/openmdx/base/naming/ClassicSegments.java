/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Classic Segments 
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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classic Segments
 */
public class ClassicSegments {

    /**
     * Aspect qualifier placeholder pattern with group 1 representing the core component
     */
    private static Pattern ASPECT_QUALIFIER_PLACEHOLDER_PATTERN = Pattern.compile("^:([^:]+):[^:]+$");
    
    /**
     * Aspect qualifier pattern with group 1 representing the core component
     */
    private static Pattern ASPECT_QUALIFIER_PATTERN = Pattern.compile("^([^:]+):[^:]+:$");

    /**
     * Tells whether a given XRI segment is a placeholder
     * 
     * @param xriSegment
     *            an XRI segment
     * 
     * @return {@code true} if the given XRI segment is a placeholder
     */
    public static boolean isPlaceholder(
        String xriSegment
    ) {
        return xriSegment.startsWith(":");
    }

    /**
     * Tells whether a given XRI segment is a placeholder
     * 
     * @param xriSegment
     *            an XRI segment
     * 
     * @return {@code true} if the given XRI segment is a placeholder
     */
    public static boolean isPlaceholder(
        XRISegment xriSegment
    ) {
        return isPlaceholder(xriSegment.toClassicRepresentation());
    }
    
    /**
     * Tests whether the argument matches formally an aspect qualifier 
     * placeholder and extracts its core component if it does
     *  
     * @param qualifier a qualifier
     * 
     * @return the aspect qualifier placeholder's core component
     */
    public static Optional<String> getCoreComponentFromAspectQualifierPlaceholder (
        final String qualifier
    ){
        final Matcher matcher = ASPECT_QUALIFIER_PLACEHOLDER_PATTERN.matcher(qualifier);
        return matcher.matches() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    /**
     * Tells whether a given XRI segment is a placeholder
     * 
     * @param xriSegment
     *            an XRI segment
     * 
     * @return {@code true} if the given XRI segment is a placeholder
     */
    public static boolean isPrivate(
        String xriSegment
    ) {
        return xriSegment.endsWith(":");
    }

    /**
     * Tells whether a given XRI segment is a placeholder
     * 
     * @param xriSegment
     *            an XRI segment
     * 
     * @return {@code true} if the given XRI segment is a placeholder
     */
    public static boolean isPrivate(
        XRISegment xriSegment
    ) {
        return isPrivate(xriSegment.toClassicRepresentation());
    }

    /**
     * Creates a private segment in classic colon format
     * 
     * @param coreComponent the segment's core component
     * @param aspectComponent the segment's aspect component
     * 
     * @return a private segment in classic colon format
     */
    public static String createPrivateSegment(String coreComponent, Object aspectComponent) {
        return coreComponent + ":" + aspectComponent + ":";
    }

    /**
     * Tests whether the argument matches formally an private aspect qualifier 
     * segment extracts its core component if it does
     *  
     * @param qualifier a qualifier
     * 
     * @return the aspect qualifier's core component
     */
    public static Optional<String> getCoreComponentFromAspectQualifier (
        final String qualifier
    ){
        final Matcher matcher = ASPECT_QUALIFIER_PATTERN.matcher(qualifier);
        return matcher.matches() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
}
