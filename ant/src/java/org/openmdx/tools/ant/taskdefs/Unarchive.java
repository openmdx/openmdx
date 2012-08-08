/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Unarchive.java,v 1.5 2005/12/19 12:53:10 hburger Exp $
 * Description: Ant Unarchive Task
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/12/19 12:53:10 $
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.apache.tools.ant.util.FileUtils;
import org.openmdx.tools.ant.types.UnarchivePatternSet;
import org.openmdx.tools.ant.util.Encodings;

/**
 * Ant Unarchive Task
 */
public class Unarchive extends Task {

	/**
	 * 
	 */
	public Unarchive() {
	}

    private Format format = new Format();
    private CompressionMethod compression = null;
    private Delegate delegate = null;

    private File dest; //req
    private File source; // req
	private String nameEncoding = null;
    private final PatternSetCollection patternsets = new PatternSetCollection();
    private Vector filesets = new Vector();

	/**
     * Sets the character encoding for the archive entry names.
     * 
     * @param encoding the character encoding
     */
    public void setEncoding(String encoding) {
		this.nameEncoding = encoding;
    }

    /**
     * Set archive format.
     * Allowable values are<ul>
     * <li>  zip - ZIP format
     * <li>  tar - TAR format
     * </ul>
     * @param format the archive format
     */
    public void setFormat(Format format) {
        this.format = format;
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
     * Set the destination directory. File will be unzipped into the
     * destination directory.
     *
     * @param d Path to the directory.
     */
    public void setDest(File d) {
        this.dest = d;
    }
    
    /**
     * Should we overwrite files in dest, even if they are newer than
     * the corresponding entries in the archive?
     */
    public void setOverwrite(boolean b) {
        this.patternsets.setOverwrite(b);
    }

    /**
     * Set the path to zip-file.
     *
     * @param s Path to zip-file.
     */
    public void setSrc(File s) {
        this.source = s;
    }

    /**
     * Add a patternset
     */
    public void addPatternset(PatternSet set) {
        this.patternsets.addPatternSet(set);
    }

    /**
     * Add a patternset
     */
    public void addUnarchivepatternset(UnarchivePatternSet set) {
        this.patternsets.addPatternSet(set);
    }

    /**
     * Add a fileset
     */
    public void addFileset(FileSet set) {
        this.filesets.addElement(set);
    }

    /**
     * Retrieve this <code>Task</code>'s delegate
     * 
     * @return this <code>Task</code>'s delegate
     */
    protected Delegate getDelegate(){
    	if(this.delegate == null) {
    		Format format = this.format;
    		this.delegate = format.newDelegate();
    		this.delegate.setPatterns(this.patternsets);
    		this.delegate.setProject(getProject());
     		this.delegate.setTaskName(getTaskName());     		
    		this.delegate.setEncoding(this.nameEncoding);
    		this.delegate.setCompression(this.compression);
    		this.delegate.setDest(this.dest);
    		this.delegate.setSrc(this.source);
    		this.delegate.setOverwrite(this.patternsets.isOverwrite());
    		for(
    			Enumeration e = this.filesets.elements();
    			e.hasMoreElements();
    		) this.delegate.addFileset((FileSet) e.nextElement());
    	}
    	return this.delegate;
    }
    	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Task#execute()
	 */
	public void execute() throws BuildException {
		getDelegate().execute();
	}

	
	//------------------------------------------------------------------------
	// Interface Delegate
	//------------------------------------------------------------------------

    /**
     * 
     */
    protected static interface Delegate {
    	
    	/**
         * Sets the character encoding for the archive entry names.
         * 
         * @param encoding the character encoding
         */
        public void setEncoding(
        	String encoding
        );

        /**
    	 * Set the patterns to be applied
    	 * 
    	 * @param patterns
    	 */
    	void setPatterns(
    		PatternSetCollection patterns
        );
    	
        /**
         * Sets the project object of this component. This method is used by
         * Project when a component is added to it so that the component has
         * access to the functions of the project. It should not be used
         * for any other purpose.
         *
         * @param project Project in whose scope this component belongs.
         *                Must not be <code>null</code>.
         */
        void setProject(
        	Project project
        );

        /**
         * Sets the name to use in logging messages.
         *
         * @param name The name to use in logging messages.
         *             Should not be <code>null</code>.
         */
        public void setTaskName(
        	String name
        );

        /**
         * Set the destination directory. File will be unzipped into the
         * destination directory.
         *
         * @param d Path to the directory.
         */
        public void setDest(File d);

        /**
         * Set the path to zip-file.
         *
         * @param s Path to zip-file.
         */
        public void setSrc(File s);

        /**
         * Should we overwrite files in dest, even if they are newer than
         * the corresponding entries in the archive?
         */
        public void setOverwrite(boolean b);

        /**
         * Whether we want to compress the files or only store them;
         * optional
         */
        void setCompression(
        	CompressionMethod compression
        );
                	
        /**
         * Adds a set of files.
         */
        void addFileset(FileSet set);

        /* (non-Javadoc)
    	 * @see org.apache.tools.ant.Task#execute()
    	 */
    	public void execute() throws BuildException;

    }
    
    
	//------------------------------------------------------------------------
	// Class PatternSetCollection
	//------------------------------------------------------------------------

	/**
	 * A set of filter chains
	 */
	protected static final class PatternSetCollection {

	    private boolean overwrite = true;
		private final Vector patternsets = new Vector();
		
	    /**
	     * Should we overwrite files in dest, even if they are newer than
	     * the corresponding entries in the archive?
	     */
	    public void setOverwrite(boolean b) {
	        this.overwrite = b;
	    }

	    /**
		 * @return Returns the overwrite.
		 */
		public boolean isOverwrite() {
			return overwrite;
		}

		/**
		 * 
		 * @param patternSet
		 */
		public void addPatternSet(
			PatternSet patternSet
		){
			this.patternsets.addElement(patternSet);
		}

		/**
		 * 
		 * 
		 * @param project 
		 * @param entryName
		 * @param entryDate TODO
		 * @param dir 
		 * @param fileUtils
		 * @return
		 * @throws IOException  
		 */
		public void extractFile(
			Project project, 
        	FileUtils fileUtils, 
        	File srcF, 
        	File dir,
            InputStream compressedInputStream,
            String entryName,
            Date entryDate, 
            boolean isDirectory
		) throws IOException {
			File file = null; 
			if (patternsets.isEmpty()) {
				file = fileUtils.resolveFile(
					dir, 
					entryName
				);
			} else {
				String name = canonicalize(entryName, false);
				for (
					Enumeration v = patternsets.elements();
					v.hasMoreElements();
				) {
					PatternSet patternset = (PatternSet) v.nextElement();
					if(matches(patternset, name, project)) {
						if(patternset instanceof UnarchivePatternSet) {
							UnarchivePatternSet modifier = (UnarchivePatternSet) patternset;
							String fullpath = modifier.getFullpath(project);
							String prefix = modifier.getPrefix(project);
							String path = fullpath != null ? 
								fullpath :
							  prefix != null ?
								entryName.substring(prefix.length()) :
								entryName;
							file = fileUtils.resolveFile(dir, path);
						} else {
							file = fileUtils.resolveFile(dir, entryName);
						}
						break;
					}
				}
			}
    		if (file == null) return;
    		if (
				!isOverwrite() && 
				file.exists() && 
				file.lastModified() >= entryDate.getTime()
			) {
			    project.log(
			    	"Skipping " + file + " as it is up-to-date",
			    	Project.MSG_DEBUG
			    );				  
				return;
			}
			project.log(
				"expanding " + entryName + " to " + file,
				Project.MSG_VERBOSE
			);
			//
			// create intermediary directories - sometimes zip don't add them
			//
			File dirF = fileUtils.getParentFile(file);
			if (dirF != null) dirF.mkdirs();
			if (isDirectory) {
				file.mkdirs();
			} else try {
				byte[] buffer = new byte[1024];
				int length = 0;
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(file);
					while (
						(length = compressedInputStream.read(buffer)) >= 0
					) fos.write(buffer, 0, length);
					fos.close();
					fos = null;
				} finally {
					if (fos != null) try {
		                fos.close();
					} catch (IOException e) {
						// ignore
					}
				}
				fileUtils.setFileLastModified(file, entryDate.getTime());
			} catch (FileNotFoundException ex) {
				project.log(
					"Unable to expand to file " + file.getPath(), 
					Project.MSG_WARN
				);
			}
		}

		/**
		 * Test whether a path matches a given pattern set
		 * 
		 * @param patternset
		 * @param entryName
		 * @param project
		 * @param inclusion <code>true</code> to test include patterns,
		 * <code>false</code> to test exclude patterns
		 * 
		 * @return <code>true</code> if the value matches any of the patterns;
		 * or onNoPattern if patterns is <code>null</code> or empty.
		 */
		static private final boolean matches (
			PatternSet patternset,
			String entryName,
			Project project
		){
			String name;
			if(patternset instanceof UnarchivePatternSet) {
				String prefix = ((UnarchivePatternSet)patternset).getPrefix(project);
				if(prefix == null) {
					name = entryName;
				} else if(SelectorUtils.matchPath(canonicalize(prefix, true), entryName)) {
					name = entryName.substring(prefix.length());
				} else {
					return false;
				}
			} else {
				name = entryName;
			}
			return matches(
				patternset.getIncludePatterns(project),
				name,
				true
			) && !matches (
				patternset.getExcludePatterns(project),
				name,
				false
			);
		}
		
		/**
		 * Test whether a path matches a given pattern array
		 * 
		 * @param patterns
		 * @param path
		 * @param onNoPattern
		 * 
		 * @return <code>true</code> if the value matches any of the patterns;
		 * or onNoPattern if patterns is <code>null</code> or empty.
		 */
		static private final boolean matches (
			String[] patterns,
			String name,
			boolean onNoPattern
		){
			if(
				patterns == null || 
				patterns.length == 0
			) return onNoPattern;
			for (
				int i = 0; 
				i < patterns.length; 
				i++
			) if (
				SelectorUtils.matchPath(
					canonicalize(patterns[i], true), 
					name
				)
			) return true;
			return false;
		}
		
		/**
		 * Replace '/' and'\' by the actual file separator
		 * 
		 * @param source
		 * @param pattern 
		 * 
		 * @return canonicalized path
		 */
		static private final String canonicalize(
			String source, 
			boolean pattern
		){
			String target = source.replace(
				'/', File.separatorChar
			).replace(
				'\\', File.separatorChar
			);
			return pattern && target.endsWith(File.separator) ?
				target + "**" :
				target;
		}
		
	}

	
	//------------------------------------------------------------------------
	// Class Untar
	//------------------------------------------------------------------------
    
    /**
     * 
     */
    protected static class Untar 
    	extends org.apache.tools.ant.taskdefs.Untar
    	implements Delegate
    {

    	/**
    	 * Constructor
    	 */
    	protected Untar(
    	){
    	}
    	
    	PatternSetCollection patternsets;
    	final static UntarCompressionMethod GZIP_COMPRESSION = new UntarCompressionMethod();

        /**
    	 * Set the patterns to be applied
    	 * 
    	 * @param patterns
    	 */
    	public void setPatterns(
    		PatternSetCollection patterns
        ){
    		this.patternsets = patterns;
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
            super.setCompression(
            	getCompressionMethod(mode)
            );
        }

        /**
         * Sets the character encoding for the archive entry names.
         * 
         * @param encoding the character encoding
         */
        public void setEncoding(
        	String encoding
        ){
        	if(
        		encoding != null &
        		!Encodings.US_ASCII.equals(encoding)
        	) throw new BuildException(
        		"tar archive file names are restricted to US_ASCII or the platform's default encoding"
        	);
        }
		
		/* (non-Javadoc)
		 * @see org.apache.tools.ant.taskdefs.Expand#extractFile(org.apache.tools.ant.util.FileUtils, java.io.File, java.io.File, java.io.InputStream, java.lang.String, java.util.Date, boolean)
		 */
    	protected void extractFile(
        	FileUtils fileUtils, 
        	File srcF, 
        	File dir,
            InputStream compressedInputStream,
            String entryName,
            Date entryDate, 
            boolean isDirectory
        ) throws IOException {
    		this.patternsets.extractFile(
    			getProject(),
            	fileUtils, 
            	srcF, 
            	dir,
                compressedInputStream,
                entryName,
                entryDate, 
                isDirectory
    		);
    	}

        static UntarCompressionMethod getCompressionMethod(
        	CompressionMethod compressionMethod
        ){
        	if(compressionMethod == null) return GZIP_COMPRESSION;
        	UntarCompressionMethod reply = new UntarCompressionMethod();
        	reply.setValue(compressionMethod.getValue());
        	return reply;
        }

    	static {
        	GZIP_COMPRESSION.setValue("gzip");
        }

    }

    
	//------------------------------------------------------------------------
	// Class Unzip
	//------------------------------------------------------------------------
    
    /**
     * 
     */
    protected static class Unzip 
    	extends Expand
    	implements Delegate
    {

    	/**
    	 * Constructor
    	 */
    	protected Unzip(
    	){
    		super.setEncoding(Encodings.UTF_8);
    	}
    	
    	PatternSetCollection patternsets;

        /* (non-Javadoc)
		 * @see org.openmdx.tools.ant.taskdefs.Unarchive.Delegate#setCompression(org.openmdx.tools.ant.taskdefs.CompressionMethod)
		 */
		public void setCompression(CompressionMethod compression) {
			// ignore			
		}

		/**
    	 * Set the patterns to be applied
    	 * 
    	 * @param patterns
    	 */
    	public void setPatterns(
    		PatternSetCollection patterns
        ){
    		this.patternsets = patterns;
    	}

		/* (non-Javadoc)
		 * @see org.apache.tools.ant.taskdefs.Expand#extractFile(org.apache.tools.ant.util.FileUtils, java.io.File, java.io.File, java.io.InputStream, java.lang.String, java.util.Date, boolean)
		 */
    	protected void extractFile(
        	FileUtils fileUtils, 
        	File srcF, 
        	File dir,
            InputStream compressedInputStream,
            String entryName,
            Date entryDate, 
            boolean isDirectory
        ) throws IOException {
    		this.patternsets.extractFile(
    			getProject(),
            	fileUtils, 
            	srcF, 
            	dir,
                compressedInputStream,
                entryName,
                entryDate, 
                isDirectory
    		);
    	}
    
    }
    
    
    //------------------------------------------------------------------------
	// Class Format
	//------------------------------------------------------------------------
	
	/**
     * Valid <code>Format</code> values for <code>Archive</code> tasks.
     */
	public static final class Format extends ArchiveFormat {

        /**
         * Default constructor
         */
        public Format(
        	String value
        ) {
            super(value);
        }

        /**
         * Default constructor
         */
        public Format() {
        	super(ZIP);
        }

        /**
         * Create a delegate instance apprioporaite for the given format.
         * 
         * @return the delegate apprioporaite for the given format
         */
		Delegate newDelegate(){
        	return TAR.equals(this.value) ? (Delegate) new Untar() : new Unzip();
        }
                
    }

}
