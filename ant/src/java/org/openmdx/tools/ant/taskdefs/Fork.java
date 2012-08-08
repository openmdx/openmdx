/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Fork.java,v 1.1 2007/04/24 16:41:50 hburger Exp $
 * Description: Spawn Task
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.JavaEnvUtils;
import org.openmdx.tools.ant.Launch;

/**
 * Spawn Task
 */
public class Fork extends Java {

	/**
	 * Constructor
	 */
	public Fork(
	){
		super.setFork(true);
		super.setClassname(Launch.class.getName());
	}

    /* (non-Javadoc)
	 * @see org.apache.tools.ant.taskdefs.Java#executeJava()
	 */
	public int executeJava() throws BuildException {
		if(this.className == null) {
			throw new BuildException(
				Fork.class.getName() + " requires className option",
				getLocation()
			);
		} else {
	        createArg().setValue(Launch.CLASSNAME_OPTION);
	        createArg().setValue(this.className);
		}
        List paths = getPaths();
        if(paths != null) {
	        createArg().setValue(
	    		createArgumentFile((String[]) paths.toArray(new String[paths.size()]))
	        );
        }
		return super.executeJava();
	}

	
    /* (non-Javadoc)
	 * @see org.apache.tools.ant.taskdefs.Java#setClassname(java.lang.String)
	 */
	public void setClassname(
		String className
	) throws BuildException {
		this.className = className;
	}

	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.taskdefs.Java#setJar(java.io.File)
	 */
	public void setJar(File jarfile) throws BuildException {
		throw new BuildException(
			Fork.class.getName() + " does not support 'jar' attributes",
			getLocation()
		);
	}

	/**
     * Add a fileset. @see ant manual
     * @param fs the FileSet
     */
    public void addFileSet(FileSet fs)
    {
        filesets.addElement(fs);
    }
    
    /**
     * Evaluate the file sets
     * 
     * @return the paths specified, or <code>null</code> if none are specified
     */
    private List getPaths(
    ){
        final Project project = getProject();
        final List paths = new ArrayList();
        for (
        	int i = 0, size = filesets.size(); 
        	i < size; 
        	i++
        ){
            FileSet fs = (FileSet) filesets.elementAt(i);
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            ds.scan();
            String[] f = ds.getIncludedFiles();
            for (
            	int j = 0; 
            	j < f.length; 
            	j++
            ){
                paths.add(project.resolveFile(new File(ds.getBasedir(), f[j]).getPath()).getAbsolutePath());
            }
        }
        return paths.isEmpty() ? null : paths;
    }

	/**
     * There might be a fork problem if the number of arguments is large.
     * 
     * @param arguments the free command-line arguments arguments
     */
    protected String createArgumentFile(
    	String[] arguments
    ) {
        try {
            File argumentFile = JavaEnvUtils.createVmsJavaOptionFile(arguments);
            //we mark the file to be deleted on exit.
            //the alternative would be to cache the filename and delete
            //after execution finished, which is much better for long-lived runtimes
            //though spawning complicates things...
            argumentFile.deleteOnExit();
            return '@' + argumentFile.getPath();
        } catch (IOException exception) {
            throw new BuildException(
            	"Failed to create an argument file", 
            	exception,
            	getLocation()
             );
        }
    }
	
    /**
     * Filesets being the source for the argument file
     */
    private final Vector filesets = new Vector();    
    
    /**
     * The target class name
     */
    private String className = null;
    
}
