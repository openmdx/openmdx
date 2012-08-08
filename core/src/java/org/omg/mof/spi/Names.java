/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Names.java,v 1.5 2010/09/13 16:46:29 hburger Exp $
 * Description: Names 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/09/13 16:46:29 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2009, OMEX AG, Switzerland
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
package org.omg.mof.spi;

/**
 * Names
 */
public class Names extends AbstractNames {

    protected Names(
    ){
        // Avoid instantiation 
    }

    /**
     * Retrieve the Java class name for a given model class name 
     * <p>
     * This method uses toIdentifier() for all components
     * 
     * @param qualifiedName
     * @param bindingPackageSuffix
     * 
     * @return the requested class
     * @throws ServiceException 
     * 
     * @see Names.getClass(String,String)
     * 
     * @exception ServiceException if the class can not be found on on the class path
     */
    public static String toClassName(
        String qualifiedName,
        String bindigPackageSuffix
    ){
        String[] modelClass = qualifiedName.split(":");
        StringBuilder nameBuilder = new StringBuilder();
        int iLimit = modelClass.length - 1;
        for(
            int i = 0;
            i < iLimit;
            i++
        ) {
            nameBuilder.append(
                Identifier.PACKAGE_NAME.toIdentifier(modelClass[i])
            ).append(
                '.'
            );
        }
        return nameBuilder.append(
            bindigPackageSuffix
        ).append(
            '.'
        ).append(
            Identifier.CLASS_PROXY_NAME.toIdentifier(modelClass[iLimit])
        ).toString();
    }
        
    /**
     * Retrieve the Java package name for a given refMofId
     * <p>
     * This method uses toIdentifier() for all components and leaves out the last component
     * 
     * @param refMofId
     * @param bindingPackageSuffix
     * 
     * @return the requested class
     * @throws ServiceException 
     * 
     * @see Names.getClass(String,String)
     * 
     * @exception ServiceException if the class can not be found on on the class path
     */
    public static String toPackageName(
        String refMofId,
        String bindingPackageSuffix
    ){
        String[] modelPackage = refMofId.split(":");
        StringBuilder nameBuilder = new StringBuilder();
        int iLimit = modelPackage.length - 1;
        for(
            int i = 0;
            i < iLimit;
            i++
        ) {
            nameBuilder.append(
                Identifier.PACKAGE_NAME.toIdentifier(modelPackage[i])
            ).append(
                '.'
            );
        }
        return nameBuilder.append(bindingPackageSuffix).toString();
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
     * openmdx 2 JPA package suffix
     */ 
    public static final String JPA3_PACKAGE_SUFFIX = "jpa3";

}
