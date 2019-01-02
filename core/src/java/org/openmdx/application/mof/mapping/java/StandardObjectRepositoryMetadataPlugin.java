/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: StandardObjectRelationalMapping 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2018, OMEX AG, Switzerland
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
import org.openmdx.application.mof.mapping.spi.StandardDigest;


/**
 * Standard Object Relational Mapping
 */
public class StandardObjectRepositoryMetadataPlugin
    implements ObjectRepositoryMetadataPlugin
{

    @Override
    public String getMappingName() {
        return "standard";
    }

    @Override
    public String getDiscriminatorColumnName(List<String> qualifiedClassName) {
        return "DTYPE";
    }

    @Override
    public String getIndexColumnName(List<String> qualifiedClassName) {
        return "IDX";
    }

    @Override
    public String getIdentityColumnName(List<String> qualifiedClassName) {
        return qualifiedClassName.get(qualifiedClassName.size() - 1).toUpperCase(Locale.US) + "_ID";
    }

    @Override
    public String getFieldColumnName(List<String> qualifiedClassName, String fieldName) {
        return Identifier.CONSTANT.toIdentifier(fieldName);
    }

    @Override
    public String getSizeColumnName(String fieldColumnName) {
        return fieldColumnName + "_";
    }

    @Override
    public String getEmbeddedColumnName(
        String fieldColumnName, 
        int index
    ) {
        return fieldColumnName + "_" + index;
    }

    @Override
    public String getDiscriminatorValue(String packageDigest, List<String> qualifiedClassName) {
        StringBuilder className = new StringBuilder();
        for(String element: qualifiedClassName) {
            (className.length() == 0 ? className : className.append(':')).append(element);
        }
        return className.toString();
    }

    @Override
    public String getTableName(
        String packageDigest, 
        List<String> qualifiedClassName
    ) {
        return packageDigest == null ? 
            StandardDigest.toObjectNameWithCalculatedPackageDigest(qualifiedClassName) :
            StandardDigest.toObjectNameWithGivenPackageDigest(packageDigest, qualifiedClassName);
    }

    @Override
    public String getSliceTableName(String tableName) {
        return tableName + "_";
    }

    @Override
    public String getDecimalScale(List<String> qualifiedClassName, String fieldName) {
        return "9";
    }
    
}
