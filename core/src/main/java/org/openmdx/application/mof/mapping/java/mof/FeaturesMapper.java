/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Features Mapper
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.application.mof.mapping.java.mof;

import java.io.Writer;

import org.openmdx.application.mof.externalizer.spi.AnnotationFlavour;
import org.openmdx.application.mof.externalizer.spi.ChronoFlavour;
import org.openmdx.application.mof.externalizer.spi.JakartaFlavour;
import org.openmdx.application.mof.mapping.cci.MetaData_1_0;
import org.openmdx.application.mof.mapping.java.AbstractMapper;
import org.openmdx.application.mof.mapping.java.Format;
import org.openmdx.application.mof.mapping.java.PrimitiveTypeMapper;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;

/**
 * Class Features Mapper
 */
public abstract class FeaturesMapper extends AbstractMapper {

    public FeaturesMapper(
        ModelElement_1_0 featuredDef,        
        Writer writer, 
        Model_1_0 model,
        Format format, 
        String packageSuffix,
        MetaData_1_0 metaData, 
        AnnotationFlavour annotationFlavour, 
        JakartaFlavour jakartaFlavour, 
        ChronoFlavour chronoFlavour,
        PrimitiveTypeMapper primitiveTypeMapper
    ){
        super(
            writer, 
            model,
            format, 
            packageSuffix,
            metaData, 
            annotationFlavour, 
            jakartaFlavour,
            chronoFlavour,
            primitiveTypeMapper
        );
    }

    /**
     * Convert feature to constant names, e.g. "A_FEATURE"
     * for "aFeature".
     * 
     * @param feature the feature's simple name
     * 
     * @return the constant name for the given feature
     */
    protected String getConstantName(
        String feature
    ){
        int iLimit = feature.length();
        boolean lower = false;
        StringBuilder buffer = new StringBuilder(
            8 + iLimit // some room for underscores
        );
        for(
            int i = 0;
            i < iLimit;
            i++
        ){
            char c = feature.charAt(i);
            if(lower && Character.isUpperCase(c)) {
                buffer.append('_');
            }
            lower = Character.isLowerCase(c);
            buffer.append(
                lower ? Character.toUpperCase(c) : c
            );
        }
        return buffer.toString();
    }
 
    /**
     * 
     */
    protected static final String FEATURES_INTERFACE_SUFFIX = "Features";
    
}
