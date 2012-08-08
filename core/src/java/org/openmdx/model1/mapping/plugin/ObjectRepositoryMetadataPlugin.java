/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ObjectRepositoryMetadataPlugin.java,v 1.2 2007/06/22 15:29:54 hburger Exp $
 * Description: Object Repository Metadata Plug-In
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/06/22 15:29:54 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

package org.openmdx.model1.mapping.plugin;

import java.util.List;

/**
 * Object Repository Metadata Plug-In
 */
public interface ObjectRepositoryMetadataPlugin {

    /**
     * The mapping name is referred to by the option
     * javax.jdo.option.Mapping.
     * 
     * @return the mapping name
     */
    public String getMappingName();
    
    /**
     * Retrieve the table name for a given class
     * 
     * @param packageDigest the package prefix specified in the the openmdxjdo file; 
     * or <code>null</code>
     * @param qualifiedClassName the components of the fully qualified class name
     * 
     * the components of the fully qualified class name
     * 
     * @return the table name for a given class
     */
    public String getTableName(
        String packageDigest,
        List<String> qualifiedClassName
    );

    /**
     * Retrieve the slice table name for a given table name
     * 
     * @param qualifiedClassName the components of the fully qualified class name
     * 
     * the tableName the table name
     * 
     * @return the slice table name
     */
    public String getSliceTableName(
        String tableName
    );

    /**
     * Retrieve the slice table name for a table name
     * 
     * @param packageDigest the package prefix specified in the the openmdxjdo file; 
     * or <code>null</code>
     * @param qualifiedClassName the components of the fully qualified class name
     * 
     * the components of the fully qualified class name
     * 
     * @return the table name for a given class
     */
    /**
     * Retrieve the discriminator column name for a given class
     * 
     * @param qualifiedClassName the components of the fully qualified class name
     * 
     * @return the discriminator column name for a given class
     */
    public String getDiscriminatorColumnName(
        List<String> qualifiedClassName
    );
    
    /**
     * Retrieve the discriminator value for a given class
     * 
     * @param packageDigest the package prefix specified in the the openmdxjdo file; 
     * or <code>null</code>
     * @param qualifiedClassName the components of the fully qualified class name
     * 
     * @return the discriminator value for a given class
     */
    public String getDiscriminatorValue(
        String packageDigest,
        List<String> qualifiedClassName
    );

    /**
     * Retrieve the index column name for a given class
     * 
     * @param qualifiedClassName the components of the fully qualified class name
     * 
     * @return the index column name for a given class
     */
    public String getIndexColumnName(
        List<String> qualifiedClassName
    );
    
    /**
     * Retrieve the identity column name for a given class
     * 
     * @param qualifiedClassName the components of the fully qualified class name
     * 
     * @return the identity column name for a given class
     */
    public String getIdentityColumnName(
        List<String> qualifiedClassName
    );
    
    /**
     * Retrieve the field column name for a given class and field
     * 
     * @param qualifiedClassName the components of the fully qualified class name
     * @param fieldName the field name
     * 
     * @return the field column name
     */
    public String getFieldColumnName(
        List<String> qualifiedClassName,
        String fieldName
    );

    /**
     * Retrieve the size column name for a given field column name
     * 
     * @param fieldColumnName the field column name
     * 
     * @return the size column name
     */
    public String getSizeColumnName(
        String fieldColumnName
    );

    /**
     * Retrieve the size column name for a given field column name
     * 
     * @param fieldColumnName the field column name
     * @param index the field column index
     * 
     * @return the size column name
     */
    public String getEmbeddedColumnName(
        String fieldColumnName,
        int index
    );

    /**
     * Retrieve the default decimal scale.
     * 
     * return the default decimal scale
     */
    public String getDecimalScale(
        List<String> qualifiedClassName,
        String fieldName
    );
    
}
