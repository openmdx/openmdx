/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractPlatform_1.java,v 1.8 2008/03/21 18:36:29 hburger Exp $
 * Description: openMDX Platform Description Interface 
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:36:29 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.kernel.environment.spi;

import java.util.HashMap;
import java.util.Map;

import org.openmdx.kernel.environment.cci.Platform_1_0;
import org.openmdx.kernel.environment.cci.VersionNumber;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;

/**
 *
 */
public abstract class AbstractPlatform_1 
    implements MultiLineStringRepresentation, Platform_1_0
{
    
    /**
     * 
     */
    private final String name;

    /**
     * 
     */
    private final String shortDescription;

    /**
     * 
     */
    protected AbstractPlatform_1(
        String name,
        String shortDescription
    ) {
        this.name = name;
        this.shortDescription = shortDescription;
    }


    //------------------------------------------------------------------------
    // Implements Platform_1_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.environment.Platform_1_0#getName()
     */
    public String getName(
    ){
        return this.name;    
    }
    
    /**
     * Java Runtime Environment specification name
     */
    protected String specificationTitle;

    /* (non-Javadoc)
     * @see org.openmdx.environment.Platform_1_0#getSpecificationTitle()
     */
    public String getSpecificationTitle() {
        return this.specificationTitle;
    }

    /**
     * 
     */
    protected String specificationVersion;

    /* (non-Javadoc)
     * @see org.openmdx.environment.Platform_1_0#getSpecificationVersion()
     */
    public String getSpecificationVersion() {
        return this.specificationVersion;
    }

    /**
     * 
     */
    protected String specificationVendor;

    /* (non-Javadoc)
     * @see org.openmdx.environment.Platform_1_0#getSpecificationVendor()
     */
    public String getSpecificationVendor(
    ){
        return this.specificationVendor;
    }

    /**
     * 
     */
    protected String implementationTitle;

    /* (non-Javadoc)
     * @see org.openmdx.environment.Platform_1_0#getImplementationTitle()
     */
    public String getImplementationTitle() {
        return this.implementationTitle;
    }

    /**
     * 
     */
    protected String implementationVersion;

    /* (non-Javadoc)
     * @see org.openmdx.environment.Platform_1_0#getImplementationVersion()
     */
    public String getImplementationVersion() {
        return this.implementationVersion;
    }

    /**
     * 
     */
    protected String implementationVendor;

    /* (non-Javadoc)
     * @see org.openmdx.environment.Platform_1_0#getImplementationVendor()
     */
    public String getImplementationVendor() {
        return this.implementationVendor;
    }

    /**
     * 
     */
    private VersionNumber mappingVersion;

    /**
     * 
     * @param size
     */
    private VersionNumber getMappingVersion(
        int size
    ){
        if(this.mappingVersion == null) this.mappingVersion = new VersionNumber(
            getSpecificationVersion()
        );
        return this.mappingVersion.getPrefix(size);
    }

    /* (non-Javadoc)
     * @see org.openmdx.environment.Platform_1_0#getImplementationMapping(java.lang.String, int, java.lang.String)
     */
    public String getMapping(
        VersionNumber before,
        int exactSize,
        VersionNumber since
    ) {
        return 
            before != null && getMappingVersion(before.size()).compareTo(before) < 0 ?
                getName() + ".before" + before :
            since != null && getMappingVersion(since.size()).compareTo(since) >= 0 ?
                getName() + ".since" + since :
                getName() + ".only" + getMappingVersion(exactSize);
    }


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------
    
    /**
     * 
     */
    private String description = null;
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString(
    ) {
        if(description == null){
        	Map<String,String> properties = new HashMap<String,String>();
        	properties.put("name",getName());
        	properties.put("specificationTitle",getSpecificationTitle());
        	properties.put("specificationVersion",getSpecificationVersion());
        	properties.put("specificationVendor",getSpecificationVendor());
        	properties.put("implementationTitle",getImplementationTitle());
        	properties.put("implementationVersion",getImplementationVersion());
        	properties.put("implementationVendor",getImplementationVendor());
            description = getClass().getName() + " (" + this.shortDescription + "): \n" + new IndentingFormatter(properties);
        }
        return this.description;
    }    

}
