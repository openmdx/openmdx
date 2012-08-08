/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: SystemProperties.java,v 1.1 2009/09/07 13:03:04 hburger Exp $
 * Description: System Properties 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/07 13:03:04 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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

package org.openmdx.kernel.application.deploy.spi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * System Properties
 */
public class SystemProperties {

    private SystemProperties() {
        // Avoid instantiation 
    }

    /**
     * The system property expansion pattern.
     */
    private static final String PATTERN = "\\$\\{([^}]+)\\}";


    /**
     * The property expansion <code>Pattern</code>. 
     */
    private static final Pattern pattern = Pattern.compile(PATTERN);







    /**
     * Expand system properties of the form <code>${system.property}</code>.
     * 
     * @param source the source
     * 
     * @return the corresponding expaned value if expansion is enabled
     */
    public static String expand(
        boolean enabled,
        String source
    ){
        return enabled ? expand(source) : source;
    }

    /**
     * Expand system properties of the form <code>${system.property}</code>.
     * 
     * @param source the source
     * 
     * @return the corresponding expanded value if expansion is enabled
     */
    public static String expand(
        String source
    ){
        if(source == null) {
            return source;
        } else {
            StringBuilder target = new StringBuilder();

            int tail = 0;
            for(
                Matcher matcher = SystemProperties.pattern.matcher(source);
                matcher.find();
                tail = matcher.end()
            ){
                String propertyValue = getProperty(matcher.group(1));
                if(propertyValue == null) {
                    target.append(
                        source.substring(tail, matcher.end())
                    );
                } else {
                    target.append(
                        source.substring(tail, matcher.start())
                    ).append(
                        propertyValue
                    );
                }
            }
            return target.append(
                source.substring(tail)
            ).toString();
        }
    }

    /**
     * Retrieve a system property
     * 
     * @param propertyName
     *
     * @return the property's value
     */
    private static String getProperty(
        String propertyName
    ){
        return System.getProperty(
          "/".equals(propertyName) ? "file.separator" : propertyName
        );
    }

}
