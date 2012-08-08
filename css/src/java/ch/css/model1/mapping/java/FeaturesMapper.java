/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: FeaturesMapper.java,v 1.1 2007/01/06 17:25:27 hburger Exp $
 * Description: Features Mapper
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/01/06 17:25:27 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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
 */
package ch.css.model1.mapping.java;

import java.io.Writer;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.model1.mapping.java.JMIAbstractMapper;
import org.openmdx.kernel.text.StringBuilders;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;

/**
 * Class Features Mapper
 */
public abstract class FeaturesMapper
    extends JMIAbstractMapper 
{

    /**
     * Constructor 
     *
     * @param classDef
     * @param writer
     * @param model
     * @param format
     * @param packageSuffix
     * 
     * @throws ServiceException
     */
    public FeaturesMapper(
        Writer writer,
        Model_1_0 model,
        String format, 
        String packageSuffix
    ) throws ServiceException {
        super(
            writer,
            model,
            format, 
            packageSuffix
        );
    }

    /**
     * Convert feature to constant names, e.g. "A_FEATURE"
     * for "aFeature".
     * 
     * @param feature
     * 
     * @return the constant name for the given feature
     */
    protected String getConstantName(
        String feature
    ){
        int iLimit = feature.length();
        boolean lower = false;
        CharSequence buffer = StringBuilders.newStringBuilder(
            8 + iLimit // some room for underscores
        );
        for(
            int i = 0;
            i < iLimit;
            i++
        ){
            char c = feature.charAt(i);
            if(lower && Character.isUpperCase(c)) {
                StringBuilders.asStringBuilder(buffer).append('_');
            }
            lower = Character.isLowerCase(c);
            StringBuilders.asStringBuilder(buffer).append(
                lower ? Character.toUpperCase(c) : c
            );
        }
        return buffer.toString();
    }
 
    /**
     * 
     */
    protected static final String FEATURES_INTERFACCE_SUFFIX = "Features";
    
}
