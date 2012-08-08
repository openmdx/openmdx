/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ArchiveFileSet.java,v 1.9 2006/02/19 15:28:08 hburger Exp $
 * Description: Archive File Set
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/02/19 15:28:08 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2006, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.tools.ant.types;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.util.FileUtils;
import org.openmdx.tools.ant.taskdefs.CompressionMethod;
import org.openmdx.tools.ant.taskdefs.Unarchive;
import org.openmdx.tools.ant.util.ArchiveTask;


/**
 * Archive File Set
 * 
 * Workaround for ASF Bugzilla Entry 25782
 */
public class ArchiveFileSet 
	extends ZipFileSet
	implements Condition
{

    /**
     * Constructor
     */
    public ArchiveFileSet() {
        super();
    }

    /**
     * Constructor
     * 
     * @param fileset
     */
    public ArchiveFileSet(FileSet fileset) {
        super(fileset);
    }

    /**
     * Constructor
     * 
     * @param fileset
     */
    public ArchiveFileSet(
    	ZipFileSet fileset
    ) {
        super(fileset);
    }

    /**
     * Tells whether the task fails or not if the fileset's src or basedir is missing. 
     */
    private boolean skipWhenMissing = false;

    /**
     * 
     */
    private Unarchive.Format format = new Unarchive.Format();
    
    /**
     * 
     */
    private CompressionMethod compression = null;
    
    /** 
     * The "if" condition to test on execution. 
     */
    private String ifCondition = "";
    
    /** 
     * The "unless" condition to test on execution. 
     */
    private String unlessCondition = "";

    /**
     * Sets the "if" condition to test on execution. This is the
     * name of a property to test for existence - if the property
     * is not set, the archive file set will be processed. 
     * The property goes through property substitution once before 
     * testing, so if property <code>foo</code> has value 
     * <code>bar</code>, setting the "if" condition to 
     * <code>${foo}_x</code> will mean that the task will only execute if 
     * property <code>bar_x</code> is set.
     *
     * @param property The property condition to test on execution.
     *                 May be <code>null</code>, in which case
     *                 no "if" test is performed.
     */
    public void setIf(String property) {
        this.ifCondition = (property == null) ? "" : property;
    }

    /**
     * Returns the "if" property condition of this archive file set
     *
     * @return the "if" property condition or <code>null</code> if no
     *         "if" condition had been defined.
     */
    public String getIf() {
        return ("".equals(ifCondition) ? null : ifCondition);
    }

    /**
     * Sets the "unless" condition to test on execution. This is the
     * name of a property to test for existence - if the property
     * is set, the archive file set will not be processed. 
     * The property goes through property substitution once before 
     * testing, so if property <code>foo</code> has value 
     * <code>bar</code>, setting the "if" condition to 
     * <code>${foo}_x</code> will mean that the task will only execute if 
     * property <code>bar_x</code> is set.
     *
     * @param property The property condition to test on execution.
     *                 May be <code>null</code>, in which case
     *                 no "unless" test is performed.
     */
    public void setUnless(String property) {
        this.unlessCondition = (property == null) ? "" : property;
    }

    /**
     * Returns the "unless" property condition of this target.
     *
     * @return the "unless" property condition or <code>null</code>
     *         if no "unless" condition had been defined.
     * @since 1.6.2
     */
    public String getUnless() {
        return ("".equals(unlessCondition) ? null : unlessCondition);
    }

    private String getSource(){
    	Project project = getProject();
    	File source = getDir(project);
    	if(source == null) source = getSrc(project);
    	return source == null ? " " : " '" + source + "' ";
    }
    
    /**
     * Tests whether or not the "if" condition is satisfied.
     *
     * @return whether or not the "if" condition is satisfied. If no
     *         condition (or an empty condition) has been set,
     *         <code>true</code> is returned.
     *
     * @see #setIf(String)
     */
    private boolean testIfCondition() {
        if ("".equals(ifCondition)) return true;
        Project project = getProject();
        String test = project.replaceProperties(ifCondition);
        boolean reply = project.getProperty(test) != null; 
        if(!reply) project.log(
        	"Skipped archive file set" + getSource() + "because property '" + test + "' is not set.", 
        	Project.MSG_VERBOSE
        );
        return reply;
    }

    /**
     * Tests whether or not the "unless" condition is satisfied.
     *
     * @return whether or not the "unless" condition is satisfied. If no
     *         condition (or an empty condition) has been set,
     *         <code>true</code> is returned.
     *
     * @see #setUnless(String)
     */
    private boolean testUnlessCondition() {
        if ("".equals(unlessCondition)) return true;
        Project project = getProject();
        String test = project.replaceProperties(unlessCondition);
        boolean reply = project.getProperty(test) == null; 
        if(!reply) project.log(
        	"Skipped archive file set" + getSource() + "because property '" + test + "' is set.", 
        	Project.MSG_VERBOSE
        );
        return reply;
    }

    /**
     * Set compression method.
     * Allowable values are
     * <ul>
     * <li>  none - no compression
     * <li>  zip - ZIP compression
     * <li>  gzip - Gzip compression
     * <li>  bzip2 - Bzip2 compression
     * </ul>
     * @param mode the compression method.
     */
    public void setCompression(CompressionMethod mode) {
        this.compression = mode;
    }

    /**
	 * @return Returns the compression.
	 */
	protected final CompressionMethod getCompression() {
		return this.compression;
	}

    /**
     * Set unarchive format.
     * @param format the archive format
     */
    public void setFormat(Unarchive.Format format) {
        this.format = format;
    }

    /**
     * Retrieve the archive format
     * 
     * @return the archive format
     */
    public Unarchive.Format getFormat(
    ){
    	return this.format;    	
    }
    
    /* (non-Javadoc)
     * @see org.apache.tools.ant.types.AbstractFileSet#getDirectoryScanner(org.apache.tools.ant.Project)
     */
    public DirectoryScanner getDirectoryScanner(Project project) {
        if (isReference()) {
        	return getRef(project).getDirectoryScanner(project);
        } else {
            File srcFile = super.getSrc(project);
            if (srcFile == null) return super.getDirectoryScanner(project);
            ArchiveScanner scanner = new ArchiveScanner();
            scanner.setSrc(srcFile);
            setupDirectoryScanner(scanner, project);
            scanner.init();
            return scanner;
        }
    }
    
    /**
     * Sets whether byte order marks should be handled by this archive filter 
     * chain.
     *
     * @param byteOrderMarkAware <code>boolean</code>.
     */
    public void setWhenmissing(WhenMissing whenMissing) {
   	 	checkAttributesAllowed();
        this.skipWhenMissing = "skip".equals(whenMissing.getValue());
    }

	/* (non-Javadoc)
	 * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
	 */
	public boolean eval(
	) throws BuildException {
		if(testIfCondition() && testUnlessCondition()) {
	    	if(this.skipWhenMissing) {
	        	Project project = this.getProject();
		    	File source = super.getSrc(project);
		    	if(source == null) source = super.getDir(project);
		    	if(source == null) {
	                log(
	                	"ArchiveFileSet has neither 'src' nor 'dir' specification",
	                    Project.MSG_WARN
	                );
		    		return true; // Let the delegate handle the missing source condition
		    	} else if (source.exists()) {
		    		return true; // Include the file set
		    	} else {
	                log(
	                	"Skipping ArchiveFileSet due to lack of its source " + source, 
	                    Project.MSG_VERBOSE
	                );
		    		return false; // Do not pass the file set to the delegate
		    	}
	    	} else {
	    		return true; // Let the delegate handle the condition
	    	}
		} else {
			return false; // if or unless condition not met
		}
    }


    //------------------------------------------------------------------------
    // Class WhenMissing
    //------------------------------------------------------------------------
    
    /**
     * Enumerated attribute with the values for indicating whether the
     * basdir's or src's presence is required.
     */
    public static class WhenMissing extends EnumeratedAttribute {
        /**
         * @return the values as an array of strings
         */
        public String[] getValues() {
            return new String[]{"fail", "skip"};
        }
    }


    //------------------------------------------------------------------------
    // Class TarFileSet
    //------------------------------------------------------------------------

    public ZipFileSet toTarFileSet(
    	ArchiveTask task
    ){
    	return new TarFileSet(this, task);
    }
    
    /**
     * A tar file set extracts the tar files content to a temporary directory
     * before processing ist content.
     */
    private static class TarFileSet 
    	extends ArchiveFileSet
	{

		TarFileSet(
			ArchiveFileSet archiveFileSet,
			ArchiveTask archiveTask
		) {
			super(archiveFileSet);
			Project project = archiveFileSet.getProject();
			String taskName = archiveTask.getTaskName();
			File src = archiveFileSet.getSrc(project);
	        File dir = FileUtils.newFileUtils().createTempFile(
				"ant.unarchive$", 
				'.' + archiveFileSet.getFormat().getValue(),
				archiveTask.getTempDir()
			);
			setProject(project);
			setSrc(null);
			setDir(dir);
	        if(src.isFile()) {
		        Mkdir mkdir = new Mkdir();
		        mkdir.setProject(project);
		        mkdir.setTaskName(taskName);
		        mkdir.setDir(dir);
		        mkdir.execute();
				Unarchive unarchive = new Unarchive();
				unarchive.setProject(project);
				unarchive.setTaskName(taskName);
				unarchive.setFormat(archiveFileSet.getFormat());
				unarchive.setCompression(archiveFileSet.getCompression());
				unarchive.setSrc(src);
				unarchive.setDest(dir);
				unarchive.execute();
				Delete cleanUp = new Delete();
				cleanUp.setProject(project);
				cleanUp.setTaskName(taskName);
				cleanUp.setDir(dir);
				cleanUp.setDeleteOnExit(true);
				archiveTask.addCleanUpTask(cleanUp);
	        } else log(
            	"Directory " + dir.getAbsolutePath() + 
            	" not created because archive " + src.getAbsolutePath() + 
            	" is missing",
                Project.MSG_WARN
            );
		}    	

	}

}
