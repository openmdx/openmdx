/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: StandardObjectRepositoryMetadataPlugin.java,v 1.1 2009/01/13 02:10:37 wfro Exp $
 * Description: StandardObjectRelationalMapping 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/13 02:10:37 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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

package org.openmdx.application.mof.mapping.java;

import java.util.List;
import java.util.Locale;

import org.omg.mof.spi.Identifier;


/**
 * StandardObjectRelationalMapping
 *
 */
public class StandardObjectRepositoryMetadataPlugin
    implements ObjectRepositoryMetadataPlugin
{

    /* (non-Javadoc)
     * @see org.openmdx.model1.mapping.spi.ObjectRepositoryMetadataPlugin#getMappingName()
     */
    public String getMappingName() {
        return "standard";
    }

    /* (non-Javadoc)
     * @see org.openmdx.model1.mapping.spi.ObjectRepositoryMetadataPlugin#getDesicriminatorColumn(java.util.List)
     */
    public String getDiscriminatorColumnName(List<String> qualifiedClassName) {
        return "DTYPE";
    }

    /* (non-Javadoc)
     * @see org.openmdx.model1.mapping.spi.ObjectRepositoryMetadataPlugin#getIndexColumnName(java.util.List)
     */
    public String getIndexColumnName(List<String> qualifiedClassName) {
        return "IDX";
    }

    /* (non-Javadoc)
     * @see org.openmdx.model1.mapping.spi.ObjectRepositoryMetadataPlugin#getIdentityColumnName(java.util.List)
     */
    public String getIdentityColumnName(List<String> qualifiedClassName) {
        return qualifiedClassName.get(qualifiedClassName.size() - 1).toUpperCase(Locale.US) + "_ID";
    }

    /* (non-Javadoc)
     * @see org.openmdx.model1.mapping.spi.ObjectRepositoryMetadataPlugin#getIdentityColumnName(java.util.List, java.lang.String)
     */
    public String getFieldColumnName(List<String> qualifiedClassName, String fieldName) {
        return Identifier.CONSTANT.toIdentifier(fieldName);
    }

    /* (non-Javadoc)
     * @see org.openmdx.model1.mapping.spi.ObjectRepositoryMetadataPlugin#getSizeColumnName(java.lang.String)
     */
    public String getSizeColumnName(String fieldColumnName) {
        return fieldColumnName + "_";
    }

    /* (non-Javadoc)
     * @see org.openmdx.model1.mapping.plugin.ObjectRepositoryMetadataPlugin#getEmbeddedColumnName(java.lang.String, int)
     */
    public String getEmbeddedColumnName(
        String fieldColumnName, 
        int index
    ) {
        return fieldColumnName + "_" + index;
    }

    /* (non-Javadoc)
     * @see org.openmdx.model1.mapping.spi.ObjectRepositoryMetadataPlugin#getDesicriminatorValue(java.lang.String, java.util.List)
     */
    public String getDiscriminatorValue(String packageDigest, List<String> qualifiedClassName) {
        StringBuilder className = new StringBuilder();
        for(String element: qualifiedClassName) {
            (className.length() == 0 ? className : className.append(':')).append(element);
        }
        return className.toString();
    }

    /* (non-Javadoc)
     * @see org.openmdx.model1.mapping.spi.ObjectRepositoryMetadataPlugin#getTableName(java.lang.String, java.util.List)
     */
    public String getTableName(
        String packageDigest, 
        List<String> qualifiedClassName
    ) {
        return (
            packageDigest == null ? getTablePrefix(
                qualifiedClassName, 
                DIGEST_PATTERN[
                    qualifiedClassName.size() - 1 > DIGEST_PATTERN.length ? 
                        DIGEST_PATTERN.length - 1 : 
                        qualifiedClassName.size() - 2
                ]
            ) : new StringBuilder(
                packageDigest
            )
        ).append(
            '_'
        ).append(
            qualifiedClassName.get(qualifiedClassName.size() - 1)
        ).toString(
        ).toUpperCase(
            Locale.US
        );
    }

    private StringBuilder getTablePrefix(
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

    private char getVersion(
        String component
    ){
        char c = component.charAt(component.length() - 1);
        return c >= '0' && c <= '9' ? c : 0; 
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.model1.mapping.spi.ObjectRepositoryMetadataPlugin#getSliceTableName(java.lang.String)
     */
    public String getSliceTableName(String tableName) {
        return tableName + "_";
    }

    /* (non-Javadoc)
     * @see org.openmdx.model1.mapping.spi.ObjectRepositoryMetadataPlugin#getDefaultDecimalScale(java.util.List, java.lang.String)
     */
    public String getDecimalScale(List<String> qualifiedClassName, String fieldName) {
        return "9";
    }

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
    
}
