/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CompressionMethod.java,v 1.1 2005/12/03 21:48:00 hburger Exp $
 * Description: Ant Compression Method
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/12/03 21:48:00 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
 * Valid Modes for Compression attribute to Archive Task
 */
public final class CompressionMethod extends EnumeratedAttribute {

    // permissible values for compression attribute
    /**
     *    No compression
     */
    public static final String NONE = "none";
    /**
     *    ZIP compression
     */
    public static final String ZIP = "zip";
    /**
     *    GZIP compression
     */
    public static final String GZIP = "gzip";
    /**
     *    BZIP2 compression
     */
    public static final String BZIP2 = "bzip2";


    /**
     * Default constructor
     */
    public CompressionMethod() {
        super();
        setValue(NONE);
    }
    
    public String getSuffix(
    	ArchiveFormat archiveFormat
    ){
    	switch (getIndex()) {        	
        	case 2: return "tar.gz"; // GZIP
        	case 3: return "tar.bz2"; // BZIP2
        	default: return archiveFormat.getValue();
    	}
    }
   
    /**
     *  Get valid enumeration values.
     *  @return valid enumeration values
     */
    public String[] getValues() {
        return new String[] {NONE, ZIP, GZIP, BZIP2 };
    }

}