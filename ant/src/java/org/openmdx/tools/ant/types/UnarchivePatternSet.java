/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UnarchivePatternSet.java,v 1.1 2005/12/05 13:55:11 hburger Exp $
 * Description: Archive Pattern Set
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/12/05 13:55:11 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
package org.openmdx.tools.ant.types;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.PatternSet;

/**
 * Archive Pattern Set
 */
public class UnarchivePatternSet extends PatternSet {

	/**
	 * Constructor
	 */
	public UnarchivePatternSet() {
	}

	/**
	 * The prefix to be removed from the entry names.
	 */
    private String prefix         = null;
    
    /**
     * The full pathname for the single entry in this patternset.
     */
    private String fullpath       = null;

    /**
     * Prepend this prefix to the path for each zip entry.
     * Prevents both prefix and fullpath from being specified
     *
     * @param prefix The prefix to prepend to entries in the zip file.
     */
    public void setPrefix(String prefix) {
        if (prefix != null && this.fullpath != null) {
            throw new BuildException("Cannot set both fullpath and prefix attributes");
        }
        this.prefix = prefix;
    }

    /**
     * Return the prefix prepended to entries in the zip file.
     */
    public String getPrefix(
    	Project project
    ) {
    	return isReference() ? 
    		getRef().getPrefix(project) : 
    		this.prefix;
    }

    /**
     * Set the full pathname of the single entry in this patternset.
     * Prevents both prefix and fullpath from being specified
     *
     * @param fullpath the full pathname of the single entry in this patternset.
     */
    public void setFullpath(
    	String fullpath
    ) {
        if (this.prefix != null && fullpath != null) throw new BuildException(
        	"Cannot set both fullpath and prefix attributes"
        );
        this.fullpath = fullpath;
    }

    /**
     * Return the full pathname of the single entry in this patternset.
     */
    public String getFullpath(
    	Project project
    ) {
    	return isReference() ? 
    		getRef().getFullpath(project) : 
    		this.fullpath;
    }

    /**
     * Performs the check for circular references and returns the
     * referenced PatternSet.
     */
    private UnarchivePatternSet getRef(
    ) {
    	return (UnarchivePatternSet) getCheckedRef(
    		UnarchivePatternSet.class,
    		"archivepatternset"
    	);
    }

}
