/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Names.java,v 1.7 2007/10/10 16:06:11 hburger Exp $
 * Description: Names 
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:11 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */

package org.openmdx.model1.mapping;

/**
 * Names
 */
public class Names extends AbstractNames {

    protected Names(
    ){
        // Avoid instantiation 
    }

    /**
     * Append a namespace element
     * 
     * @param target
     * @param source
     */
    public static StringBuffer openmdx1NamespaceElement(
        StringBuffer target,
        String source
    ){
        for(
            int i = 0, iLimit = source.length();
            i < iLimit;
            i++
         ){
            char c = source.charAt(i);
            if(isNotSignificant(c)) {
                target.append('_');
            } else {
                target.append(
                    i == 0 && Character.isUpperCase(c) ? Character.toLowerCase(c) : c
                );
            }
        }
        return target;
    }

    /**
     * Append a namespace element
     * 
     * @param target
     * @param source
     */
    public static StringBuffer openmdx1PackageName(
        StringBuffer target,
        String source
    ){
        boolean start = true;
        for(
            int i = 0, iLimit = source.length();
            i < iLimit;
            i++
        ){
            char c = source.charAt(i);
            if(isNotSignificant(c)) {
                target.append('_');
            } else {
                target.append(
                    start && Character.isUpperCase(c) ? Character.toLowerCase(c) : c
                );
            }
            start = false;
        }
        return target.append("Package");
    }
    
    /**
     * Evaluate accesor name
     */ 
    public static String openmdx1AccessorName (
        String featureName,
        boolean forQuery,
        boolean forBoolean
    ){        
        if(forQuery) {
            if(
                forBoolean
            ) {
                if(
                    featureName.startsWith("is")
                 ) {
                    return featureName;
                } else {
                    return "is" + capitalize(featureName);
                }
            } else {
                return "get" + capitalize(featureName);
            }  
        } else {
            if(
                forBoolean
            ) {
                if(
                    featureName.startsWith("is")
                 ) {
                    return "set" + featureName.substring(2);
                } else {
                    return "set" + capitalize(featureName);
                }
            } else {
                return "set" + capitalize(featureName);
            }  
        }
    }

    /**
     * 
     * @param openmdx1
     * 
     * @return
     */
    public static String cciPackageSuffix(
        boolean openmdx1
    ){
        return openmdx1 ? null : CCI2_PACKAGE_SUFFIX;
    }
    
    /**
     * 
     * @param openmdx1
     * 
     * @return
     */
    public static String jmiPackageSuffix(
        boolean openmdx1
    ){
        return openmdx1 ? CCI1_PACKAGE_SUFFIX : JMI1_PACKAGE_SUFFIX;
    }
    
    /**
     * openMDX 2 CCI package suffix
     */ 
    public static final String CCI2_PACKAGE_SUFFIX = "cci2";

    /**
     * openMDX 2 SPI package suffix
     */ 
    public static final String SPI2_PACKAGE_SUFFIX = "spi2";

    /**
     * openmdx 2 JMI package suffix
     */ 
    public static final String JMI1_PACKAGE_SUFFIX = "jmi1";

    /**
     * openmdx 1 CCI package suffix
     */ 
    public static final String CCI1_PACKAGE_SUFFIX = "cci";

    /**
     * openmdx 2 JDO package suffix
     */ 
    public static final String JDO2_PACKAGE_SUFFIX = "jdo2";

}