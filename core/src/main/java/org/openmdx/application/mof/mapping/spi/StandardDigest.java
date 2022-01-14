/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Standard Digest 
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
package org.openmdx.application.mof.mapping.spi;

import java.util.List;
import java.util.Locale;

/**
 * Standard Digest
 */
public class StandardDigest {

    /**
     * Digest creation rules
     */
    private static final int[][] DIGEST_PATTERN = new int[][] {
        {7},
        {2, 5},
        {1, 2, 4},
        {1, 2, 2, 2},
        {1, 1, 1, 2, 2},
        {1, 1, 1, 1, 1, 2},
        {1, 1, 1, 1, 1, 1, 1},
    };

    /**
     * Convert a qualified model name to an object name
     * 
     * @param qualifiedModelName the fully qualified model name
     * 
     * @return an object name with the qualified package name replaced by its 
     * calculated digest
     */
    public static String toObjectNameWithCalculatedPackageDigest(
        List<String> qualifiedModelName
    ) {
        return createObjectName(toPrefix(qualifiedModelName), qualifiedModelName);
    }

    /**
     * Convert a qualified model name to an object name
     * 
     * @param configuredDigest the given digest
     * @param qualifiedModelName the fully qualified model name
     * 
     * @return an object name with the qualified package name replaced by the 
     * given digest
     */
    public static String toObjectNameWithGivenPackageDigest(
        String configuredDigest, 
        List<String> qualifiedModelName
    ) {
        return createObjectName(new StringBuilder(configuredDigest), qualifiedModelName);
    }
    
    private static String createObjectName(
        final StringBuilder packageDigest,
        List<String> qualifiedClassName
    ) {
        return packageDigest.append(
            '_'
        ).append(
            qualifiedClassName.get(qualifiedClassName.size() - 1)
        ).toString(
        ).toUpperCase(
            Locale.US
        );
    }
    
    static StringBuilder toPrefix(List<String> qualifiedClassName) {
        return getTablePrefix(
            qualifiedClassName, 
            DIGEST_PATTERN[
               qualifiedClassName.size() - 1 > DIGEST_PATTERN.length ? 
                   DIGEST_PATTERN.length - 1 : 
                   qualifiedClassName.size() - 2
            ]
        );
    }

    private static StringBuilder getTablePrefix(
        List<String> components,
        int[] size
    ){
        StringBuilder name = new StringBuilder();
        char version = '0';
        for(
            int i = 0;
            i < size.length;
            i++
        ) {
            int l = components.get(i).length();
            char v = getVersion(components.get(i));
            if(v > 0) {
                version = v;
                l--;
            }
            if (i + 1 == size.length) {
                name.append(version);
            }
            switch(size[i]) {
                case 1:  
                    name.append(
                        components.get(i).charAt(0)
                    );
                    break;
                case 2: 
                    name.append(
                        components.get(i).charAt(0)
                    ).append(
                        components.get(i).charAt(l > 4 ? 4 : l - 1)
                    );
                    break;
                case 4:
                    if(l >= 4) {
                        name.append(
                            components.get(i).substring(0, 2)
                        ).append(
                            components.get(i).substring(l - 2, l)
                        );
                    } else {
                        name.append(components.get(i).substring(0, l));
                    }
                    break;
                default: {
                    name.append(
                        components.get(i).substring(0, l > size[i] ? size[i] : l)
                    );
                }
            }
        }
        return name;   
    }

    private static char getVersion(
        String component
    ){
        char c = component.charAt(component.length() - 1);
        return c >= '0' && c <= '9' ? c : 0; 
    }

}
