/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DatabaseCommand.java,v 1.1 2007/04/24 16:41:50 hburger Exp $
 * Description: Database Command
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/04/24 16:41:50 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package org.openmdx.tools.ant.taskdefs;

import org.apache.tools.ant.types.EnumeratedAttribute;

/**
 * Database Task Commands<ul>
 * <li><code>create-schema</code>
 * <li><code>delete-schema</code>
 * <li><code>validate-schema</code>
 * <li><code>database-info</code>
 * <li><code>schema-info</code>
 * </ul>
 */
public final class DatabaseCommand extends EnumeratedAttribute {

    /**
     * Constructor
     */
    public DatabaseCommand() {
        setValue(CREATE_SCHEMA);
    }
    
    /**
     * Retrieve the corresponding vendor specific option
     * 
     * @return the corresponding vendor specific option
     */
    String getValue(
    	JDOVendor vendor
    ){
    	int index = getIndex();
    	return 
    		index < 0 ? null : 
    		JDOVendor.JPOX.equals(vendor.getValue()) ? JPOX_VALUES[index] :
    		null;
    }
   
    /**
     *  Get valid enumeration values.
     *  @return valid enumeration values
     */
    public String[] getValues() {
        return new String[] {
        	CREATE_SCHEMA, 
        	DELETE_SCHEMA, 
        	VALIDATE_SCHEMA, 
        	DATABASE_INFO, 
        	SCHEMA_INFO
        };
    }

    
	//------------------------------------------------------------------------
    // permissible values for compression attribute
	//------------------------------------------------------------------------
	
    /**
     * create mode 
     */
    public static final String CREATE_SCHEMA = "create-schema";
    /** 
     * delete mode
     */
    public static final String DELETE_SCHEMA = "delete-sechema";
    /** 
     * validate mode
     */
    public static final String VALIDATE_SCHEMA = "validate-schema";
    /** 
     * database info mode 
     */
    public static final String DATABASE_INFO = "database-info";
    /** 
     * schema info mode 
     */
    public static final String SCHEMA_INFO = "schema-info";

    /**
     * The JPOX specific Schema Tool commands
     */
    private static final String[] JPOX_VALUES = new String[]{
    	"-create", // CREATE_SCHEMA
    	"-delete", // DELETE_SCHEMA
    	"-validate", // VALIDATE_SCHEMA
    	"-dbinfo", // DATABASE_INFO
    	"-schemainfo" // SCHEMA_INFO
    };
    
}