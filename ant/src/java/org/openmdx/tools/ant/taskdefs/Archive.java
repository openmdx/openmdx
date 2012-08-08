/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Archive.java,v 1.25 2010/06/04 22:22:49 hburger Exp $
 * Description: Ant Archive Task
 * Revision:    $Revision: 1.25 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/04 22:22:49 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2010, OMEX AG, Switzerland
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.filters.util.ChainReaderHelper;
import org.apache.tools.ant.taskdefs.Checksum;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.UpToDate;
import org.apache.tools.ant.taskdefs.Tar.TarCompressionMethod;
import org.apache.tools.ant.taskdefs.Zip.Duplicate;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.types.selectors.SelectorContainer;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;
import org.apache.tools.zip.ZipOutputStream;
import org.openmdx.tools.ant.types.ArchiveFileSet;
import org.openmdx.tools.ant.types.ArchiveFilterChain;
import org.openmdx.tools.ant.types.ManifestType;
import org.openmdx.tools.ant.types.UnarchivePatternSet;
import org.openmdx.tools.ant.util.AdaptiveInputStreamReader;
import org.openmdx.tools.ant.util.ArchiveTask;
import org.openmdx.tools.ant.util.Encodings;
import org.openmdx.tools.ant.util.ReaderInputStream;
import org.openmdx.tools.ant.util.References;
import org.openmdx.tools.ant.util.XMLDeclaration;

/**
 * Ant Archive Task
 * 
 * @ant.task category="packaging"
 */
public class Archive 
	extends MatchingTask 
	implements ArchiveTask
{
	
    private FilterChainCollection filterset = new FilterChainCollection();
    private Vector nested = new Vector ();
    private Vector filesets = new Vector ();
    private Format format = new Format();
    private CompressionMethod compression = null;
    private String checksumAlgorithm = null;
    protected Duplicate duplicate = null;
    private File destFile;
    private File baseDir;
    private Delegate delegate = null;
	private String nameEncoding = null;
    private Vector cleanupTasks = new Vector ();

    /**
     * merged manifests added through addConfiguredManifest 
     */
    private Manifest configuredManifest;
    
    /**
     * The encoding to use when reading in a manifest file 
     */
    private String manifestEncoding;

    /**
     * The file found from the 'manifest' attribute.  This can be
     * either the location of a manifest, or the name of a jar added
     * through a fileset.  If its the name of an added jar, the
     * manifest is looked for in META-INF/MANIFEST.MF
     */
    private File manifestFile;
	
	protected static final Collection MODULE_HOLDER = Collections.unmodifiableCollection(
		Arrays.asList(
			new String[]{
				ArchiveFormat.TAR,
				ArchiveFormat.ZIP,
				ArchiveFormat.EAR
			}
		)
	);
	
	protected static final Collection MANIFEST_HOLDER = Collections.unmodifiableCollection(
		Arrays.asList(
			new String[]{
				ArchiveFormat.JAR,
				ArchiveFormat.WAR,
				ArchiveFormat.RAR,
				ArchiveFormat.PAR,
				ArchiveFormat.EAR
			}
		)
	);

	/**
	 * Used in case of tar source
	 */
    private File tempDir = null;

    /* (non-Javadoc)
	 * @see org.openmdx.tools.ant.util.ArchiveTask#isUpToDate()
	 */
	public boolean isUpToDate() {
		for(	
			Enumeration e = this.nested.elements();
			e.hasMoreElements();
		){
			Nested nested = (Nested) e.nextElement();
			if(!nested.isUpToDate()) return false;
		}
		Project project = getProject();
		UpToDate upToDate = new UpToDate();
		upToDate.setProject(project);
		upToDate.setTaskName(getTaskName());
		upToDate.setTargetFile(getDestFile());
		File baseDir = getBaseDir();
		if(baseDir != null) {
			FileSet implicitFileSet = (FileSet) getImplicitFileSet().clone();
			implicitFileSet.setDir(baseDir);
			upToDate.addSrcfiles(implicitFileSet);
		}
		for(
			Enumeration e = this.filesets.elements();
			e.hasMoreElements();
		){
			FileSet fileSet = (FileSet)e.nextElement();
			if(fileSet instanceof ZipFileSet) {
				ZipFileSet zfs = (ZipFileSet) fileSet;
				File src = zfs.getSrc(project);
				if(src != null) {
					fileSet = new FileSet();
					fileSet.setFile(src);
				}
			}
			upToDate.addSrcfiles(fileSet);
		}
		return upToDate.eval();
	}

	/**
     * 
     * @return
     */
	public File getTempDir(
	) {
		return this.tempDir;
	}

	/**
     * Set the temporary archive directory
     *
     * @param tempdir the temporary archive directory
     */
    public void setTempdir(File tempDir) {
        this.tempDir = tempDir;
    }

    /**
     * Retrieve the archive's filter set
     * 
     * @return the archive's filter set
     */
    protected final FilterChainCollection getFilterSet(){
    	return this.filterset;
    }
    
    /**
     * Set archive format.
     * @param format the archive format
     */
    public void setFormat(Format format) {
        this.format = format;
    }

    /**
     * Retrieve the archive format
     * 
     * @return the archive format
     */
    protected Format getFormat(
    ){
    	return this.format;    	
    }
    
    /**
     * Set the checksum algorithm
     * 
     * @param alorithm the checksum algorithm
     */
    public void setChecksum(
    	String alorithm
    ) {
    	this.checksumAlgorithm = alorithm;
    }

	/**
     * Sets the character encoding for the archive entry names.
     * 
     * @param encoding the character encoding
     */
    public void setNameEncoding(String encoding) {
        this.nameEncoding = encoding;
    }

    /**
     * Rettrieve the archive entry name's encoding
     * @returnthe archive entry name's encoding
     */
    protected String getNameEncoding(
    ){
    	return this.nameEncoding;    	
    }
    
    /**
     * The file to create; required.
     * @since Ant 1.5
     * @param destFile The new destination File
     */
    public void setDestFile(File destFile) {
       this.destFile = destFile;
    }

    /**
     * The file to create.
     * @since Ant 1.5.2
     */
    protected File getDestFile() {
        return this.destFile;
    }


    /**
     * Directory from which to archive files; optional.
     */
    public void setBasedir(File baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * 
     * @return
     */
    protected File getBaseDir() {
        return this.baseDir;
    }

    /**
     * Sets behavior for when a duplicate file is about to be added -
     * one of <code>add</code>, <code>preserve</code> or <code>fail</code>:<ul>
     * <li><code>add</code> (keep both of the files)
     * <li><code>preserve</code> (keep the first version of the file found)
     * <li><code>fail</code> (throw a build exception)
     * </ul>
     * Default for archive tasks is <code>add</code>.
     * <p>
     * <code>preserve</code> and <code>fail</code> are not allowed 
     * in case of <code>tar</code> format.
     * 
     * @param duplicate the value to be set
     */
    public void setDuplicate(Duplicate duplicate) {
        this.duplicate = duplicate;
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
     * Adds a set of files that can be
     * read from an archive and be given a prefix/fullpath.
     */
    public void addArchivefileset(ArchiveFileSet set) {
        filesets.addElement(set);
    }

    /**
     * Adds a set of files.
     */
    public void addFileset(FileSet set) {
        filesets.addElement(set);
    }

    /**
     * Adds a set of files that can be
     * read from an archive and be given a prefix/fullpath.
     */
    public void addZipfileset(ZipFileSet set) {
        filesets.addElement(set);
    }

    /**
     * Adds an archive filter chain
     * @return 	an archive filter chain object
     */
    public ArchiveFilterChain createArchiveFilterChain() {
    	ArchiveFilterChain filterChain = new ArchiveFilterChain();
        this.filterset.addFilterChain(filterChain);
        return filterChain;
    }

    /**
     * Adds a ZIP archive
     * 
     * @return a "zip" archive object
     * @throws IOException 
     */
    public Archive createZip() throws IOException {
    	Nested archive = new Nested(this, ArchiveFormat.ZIP);
    	this.nested.addElement(archive);
        return archive;
    }

    /**
     * Adds an EAR
     * 
     * @return an "ear" archive object
     * @throws IOException 
     */
    public Archive createEar() throws IOException {
    	Nested archive = new Nested(this, ArchiveFormat.EAR);
    	this.nested.addElement(archive);
        return archive;
    }
    
    /**
     * Adds a RAR
     * 
     * @return a "rar" archive object
     * @throws IOException 
     */
    public Archive createRar() throws IOException {
    	checkParentFormat(MODULE_HOLDER);
    	Nested archive = new Nested(this, ArchiveFormat.WAR);
    	this.nested.addElement(archive);
        return archive;
    }

    /**
     * Adds a JAR
     * 
     * @return a "jar" archive object
     * @throws IOException 
     */
    public Archive createJar() throws IOException {
    	checkParentFormat(MODULE_HOLDER);
    	Nested archive = new Nested(this, ArchiveFormat.JAR);
    	this.nested.addElement(archive);
        return archive;
    }

    /**
     * Adds a WAR
     * 
     * @return a "war" archive object
     * @throws IOException 
     */
    public Archive createWar() throws IOException {
    	checkParentFormat(MODULE_HOLDER);
    	Nested archive = new Nested(this, ArchiveFormat.WAR);
    	this.nested.addElement(archive);
        return archive;
    }

    /**
     * Adds a PAR
     * 
     * @return a "par" archive object
     * @throws IOException 
     */
    public Archive createPar() throws IOException {
    	checkParentFormat(MODULE_HOLDER);
    	Nested archive = new Nested(this, ArchiveFormat.PAR);
    	this.nested.addElement(archive);
        return archive;
    }

    public void setManifestEncoding(String manifestEncoding) {
        this.manifestEncoding = manifestEncoding;
    }

    /**
     * The manifest file to use. This can be either the location of a manifest,
     * or the name of a jar added through a fileset. If its the name of an added
     * jar, the task expects the manifest to be in the jar at META-INF/MANIFEST.MF.
     *
     * @param manifestFile the manifest file to use.
     */
    public void setManifest(File manifestFile) {
        if (!manifestFile.exists()) throw new BuildException(
        	"Manifest file: " + manifestFile + " does not exist.", getLocation()
        );
        this.manifestFile = manifestFile;
    }

    /**
     * Allows the manifest for the archive file to be provided inline
     * in the build file rather than in an external file.
     *
     * @param newManifest
     * @throws ManifestException
     */
    public void addConfiguredManifest(
    	Manifest newManifest
    ) throws ManifestException {
    	checkParentFormat(MANIFEST_HOLDER);
        if (this.configuredManifest == null) configuredManifest = new Manifest();
        this.configuredManifest.merge(newManifest);
    }

    /**
     * Allows the manifest for the archive file to be provided inline
     * in the build file or by reference rather than in an external file.
     *
     * @param newManifest
     * 
     * @throws ManifestException
     */
    public void addConfiguredArchiveManifest(
    	ManifestType newManifest
    ) throws ManifestException {
    	addConfiguredManifest(newManifest.getManifest());
    }

    /**
	 * @return Returns the configuredManifest.
	 */
	protected Manifest getConfiguredManifest() {
		return configuredManifest;
	}

	/**
	 * @return Returns the manifestEncoding.
	 */
	protected String getManifestEncoding() {
		return manifestEncoding;
	}

	/**
	 * @return Returns the manifestFile.
	 */
	protected File getManifestFile() {
		return manifestFile;
	}

	/**
     * 
     * @param parentFormat
     */
    protected void checkParentFormat(
    	Collection acceptedFormat
    ){
    	if(!acceptedFormat.contains(getFormat().getValue())) throw new BuildException(
    		"The parent's archive format should not be " + getFormat() +
    		" but a member of " + acceptedFormat
    	);
    }

    
    /* (non-Javadoc)
	 * @see org.openmdx.tools.ant.util.CleaningUp#addCleanUpTask(org.apache.tools.ant.Task)
	 */
	public void addCleanUpTask(Task task) {
    	this.cleanupTasks.add(task);
	}

    /**
     * Retrieve this <code>Task</code>'s delegate
     * 
     * @return this <code>Task</code>'s delegate
     */
    protected Delegate getDelegate(){
    	if(this.delegate == null) {
    		Format format = getFormat();
    		CompressionMethod compression = getCompression();
    		Project project = getProject();
    		this.delegate = format.newDelegate(this);
    		this.delegate.setProject(project);
    		this.delegate.setTaskName(getTaskName());
    		this.delegate.setNameEncoding(getNameEncoding());
    		compression = this.delegate.setCompression(compression);
    		if(duplicate != null) this.delegate.setDuplicate(duplicate);
    		this.delegate.setFilters(getFilterSet());
    		this.delegate.setDestFile(
    			format.getDestFile(getDestFile(), compression)
    		);
    		this.delegate.setBasedir(this.getBaseDir());
    		this.delegate.setImplicitFileSet(
    			References.getReference(project, getImplicitFileSet())
    		);
    		for(
    			Enumeration e = this.filesets.elements();
    			e.hasMoreElements();
    		) {
    			FileSet fileSet = (FileSet) e.nextElement();
    			if(
    				!(fileSet instanceof Condition) ||
    				((Condition) fileSet).eval()
    			) { 
    				if(fileSet instanceof ArchiveFileSet) {
    					ArchiveFileSet archiveFileSet = (ArchiveFileSet) fileSet;
    					this.delegate.addFileset(
    						ArchiveFormat.TAR.equals(archiveFileSet.getFormat().getValue()) ?
    							archiveFileSet.toTarFileSet(this) :
    							archiveFileSet
    					);
    				} else {
        				this.delegate.addFileset(fileSet);
    				}
    			}    			
    		}
    		if(this.delegate instanceof Jar) {
    			Jar jar = (Jar) this.delegate;
    			File manifestFile = getManifestFile();
    			if(manifestFile != null) jar.setManifest(manifestFile);
    			String manifestEncoding = getManifestEncoding();
    			if(manifestEncoding != null) jar.setManifestEncoding(manifestEncoding);
    			Manifest configuredManifest = getConfiguredManifest();
    			if(configuredManifest != null) try {
					jar.addConfiguredManifest(configuredManifest);
				} catch (ManifestException exception) {
					// Should not occur as it is the methods first invocation
					// for this instance.
					throw new BuildException(
						"Unable to propagate Manifest", 
						exception
					);
				}
    		}
    	}
    	return this.delegate;
    }
    	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Task#execute()
	 */
	@Override
	public void execute() throws BuildException {
		if(isUpToDate()) {
			this.log(
				"Archive " + getDestFile().getAbsolutePath() + " is up-to-date", 
				Project.MSG_VERBOSE
			);
		} else try {
			for(
				Enumeration e = this.nested.elements();
				e.hasMoreElements();		
			){
				Nested nested = (Nested) e.nextElement();
				nested.execute();
			}
			Delegate delegate = getDelegate();
			delegate.execute();
			if(
				this.checksumAlgorithm != null &&
				delegate.getDestFile() != null
			) {
				Checksum checksum = new Checksum();
				checksum.setProject(getProject());
				checksum.setTaskName(getTaskName());
				checksum.setAlgorithm(this.checksumAlgorithm);
				checksum.setFile(delegate.getDestFile());
				checksum.execute();
			}
		} catch (BuildException exception) {
			throw exception;
		} catch (RuntimeException exception) {
			exception.printStackTrace();
		} finally {
			for(
				Enumeration e = this.nested.elements();
				e.hasMoreElements();		
			){
				Nested nested = (Nested) e.nextElement();
				if(nested.isTempFile()) nested.getDestFile().delete();
			}
			for(
				Enumeration e = this.cleanupTasks.elements();
				e.hasMoreElements();
			) try {
				Task cleanupTask = (Task) e.nextElement();
				cleanupTask.execute();
			} catch (BuildException exception) {
				this.log("Clean-up failure: " + exception.getMessage());
			}
		}
	}

	/**
	 * ZIP Pcompression method defaults to <code>true</code>
	 * @param compression
	 * @return
	 */
    static boolean zipCompressionMethod(
    	CompressionMethod compression
    ){
    	String value = compression.getValue();
    	if (CompressionMethod.ZIP.equals(value)) { 
			return true;
		} else if(CompressionMethod.NONE.equals(value)) {
    		return false;
    	} else throw new BuildException(
	         value + " is not a legal compression method for a ZIP archive"
	    );
    }

    /**
     * TAR compression method defaults to <code>gzip</code>.
     * 
     * @param compression
     * @return
     */
    static TarCompressionMethod tarCompressionMethod(
    	CompressionMethod compression
    ){
    	TarCompressionMethod reply = new TarCompressionMethod();
    	reply.setValue(
    		compression.getValue()
    	);
    	return reply;
    }

		
	//------------------------------------------------------------------------
	// Class FilterChainCollection
	//------------------------------------------------------------------------

    /**
     * Represents an archive entry
     */
    protected static final class Entry {

    	/**
    	 * Constructor
    	 * 
    	 * @param path
    	 * @param stream
    	 */
    	protected Entry(
    		String path,
    		InputStream stream
    	){
    		this.path = path;
    		this.stream = stream;
    	}
    	
    	/**
    	 * The archie entry's name
    	 */
    	private final String path;
    	
    	/**
    	 * The stream to be put into the archive
    	 */
    	private final InputStream stream;

		/**
		 * @return Returns the name.
		 */
		public String getPath() {
			return path;
		}

		/**
		 * @return Returns the stream.
		 */
		public final InputStream getStream() {
			return stream;
		}
     	
    }
    
	/**
	 * A set of filter chains
	 */
	protected static final class FilterChainCollection {

		/**
		 * 
		 */
		private final Vector filterChains = new Vector();
		
		/**
		 * 
		 * @param filterChain
		 */
		public void addFilterChain(
			ArchiveFilterChain filterChain
		){
			this.filterChains.addElement(filterChain);
		}

		/**
		 * Enhance the input stream with the appliable filters
		 * 
		 * @param in
		 * @param path
		 * @param project
		 * 
		 * @return the corresponding filtered input stream
		 * 
		 * @throws IOException
		 */
		protected Entry getEntry(
			InputStream in,
			String path, 
			Project project
		) throws IOException {
	    	String mappedPath = path;
	    	InputStream filteredInputStream = in;
			if(!this.filterChains.isEmpty()) {
		    	Vector applicableFilterChains = new Vector();		    	
		    	FilterChains: for(
		    		Enumeration e = this.filterChains.elements();
		    		e.hasMoreElements();
		    	) {
		    		ArchiveFilterChain candidate = (ArchiveFilterChain) e.nextElement();
		    		if(candidate.appliesTo(mappedPath)) {
		    			FileNameMapper mapper = candidate.getMapper();
		    			if(mapper != null) {
			    			String[] paths = mapper.mapFileName(mappedPath);
			    			if(paths == null || paths.length == 0) continue FilterChains;
		    				mappedPath = paths[0];
		    			}
		    			if(
		    				!candidate.getFilterReaders().isEmpty()
		    			) applicableFilterChains.add(candidate);		    			
		    		}
		    	}
				if(!applicableFilterChains.isEmpty()) {		
				    ChainReaderHelper helper = new ChainReaderHelper();
				    ArchiveFilterChain firstElement = (ArchiveFilterChain) applicableFilterChains.firstElement();
				    String outputEncoding = ((ArchiveFilterChain) applicableFilterChains.lastElement()).getOutputEncoding(); 
				    AdaptiveInputStreamReader primaryReader = new AdaptiveInputStreamReader(
			    		filteredInputStream,
			    		firstElement.getInputEncoding(),
			    		firstElement.getByteordermarkaware(),
			    		firstElement.getXmldeclarationaware(),
			    		project
			    	); 
				    helper.setPrimaryReader(primaryReader);
				    helper.setFilterChains(applicableFilterChains);
				    helper.setProject(project);
				    ReaderInputStream readerInputStream = outputEncoding == null ?
			    		new ReaderInputStream(helper.getAssembledReader()) :
			    		new ReaderInputStream(helper.getAssembledReader(), outputEncoding);			    	
				    XMLDeclaration xmlDeclaration = primaryReader.getXMLDclaration();
				    if(xmlDeclaration != null) readerInputStream.insert(
				    	new XMLDeclaration(
			    			xmlDeclaration.getVersion(),
			    			outputEncoding,
			    			xmlDeclaration.getStandalone()
				    	).toString()
			    	);
				    filteredInputStream = readerInputStream;
				}
			}
	    	return new Entry(
	    		mappedPath,
	    		filteredInputStream
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
         * @param archiveTask TODO
         * 
         * @return the delegate apprioporaite for the given format
         */
		Delegate newDelegate(
			ArchiveTask archiveTask
		){
        	return TAR.equals(this.value) ?
        		(Delegate) new Tar(archiveTask) :
        	  ZIP.equals(this.value) ? 
        		(Delegate) new Zip() :
        		(Delegate) new Jar();
        }
        
        File getDestFile(
        	File specification,
        	CompressionMethod compression
        ){
        	String name = specification.getName();
        	if (!name.endsWith(".")) return specification;
        	name += compression == null ?
        		getValue() :
        	    compression.getSuffix(this);
        	return new File(specification.getParent(), name);
        }
        
    }

    
    //------------------------------------------------------------------------
	// Interface Delegate
	//------------------------------------------------------------------------

    /**
     * 
     */
    protected static interface Delegate extends SelectorContainer {
    	
    	/**
         * Sets the character encoding for the archive entry names.
         * 
         * @param encoding the character encoding
         */
        void setNameEncoding(
        	String encoding
        );

        /**
    	 * Set the filters to be applied
    	 * 
    	 * @param filters
    	 */
    	void setFilters(
    		FilterChainCollection filters
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
        void setTaskName(
        	String name
        );

        /**
         * The file to create; required.
         * @since Ant 1.5
         * @param destFile The new destination File
         */
        void setDestFile(
        	File destFile
        );

        /**
         * The file to create.
         * @since Ant 1.5.2
         */
        File getDestFile();

    	/**
         * Directory from which to archive files; optional.
         */
        void setBasedir(
            File baseDir
        );

        /**
         * Whether we want to compress the files or only store them.
         * 
         * @return the default compression method if the argument was
         * <code>null</code>, the argument istelf otherwise
         */
        CompressionMethod setCompression(
        	CompressionMethod compression
        );
        
        /**
         * Sets behavior for when a duplicate file is about to be added -
         * one of <code>add</code>, <code>preserve</code> or <code>fail</code>:<ul>
         * <li><code>add</code> (keep both of the files)
         * <li><code>preserve</code> (keep the first version of the file found)
         * <li><code>fail</code> (throw a build exception)
         * </ul>
         * Default for archive tasks is <code>add</code>.
         * <p>
         * <code>preserve</code> and <code>fail</code> are not allowed 
         * in case of <code>tar</code> format.
         * 
         * @param duplicate the value to be set
         */
        public void setDuplicate(
            Duplicate duplicate
        );

        /**
         * Link the implicit file sets
         * 
         * @param refrerence a reference to the archive's implicit fileset
         */
        void setImplicitFileSet (
        	Reference reference
        ) throws BuildException;
        	
        /**
         * Adds a set of files.
         */
        void addFileset(
        	FileSet set
        );

        /* (non-Javadoc)
    	 * @see org.apache.tools.ant.Task#execute()
    	 */
    	public void execute(    			
    	) throws BuildException;

    }

    
    //------------------------------------------------------------------------
	// Class Zip
	//------------------------------------------------------------------------

    /**
     * Handles archive requests for the "zip" format.
     */
    protected static class Zip 
    	extends org.apache.tools.ant.taskdefs.Zip
    	implements Delegate
    {
    	
        /**
		 * Constructor
		 */
		protected Zip(
		) {
			setNameEncoding(Encodings.UTF_8);
		}

		/**
		 * 
		 */
		protected FilterChainCollection filterset;
    	
    	/**
         * Sets the character encoding for the archive entry names.
         * 
         * @param encoding the character encoding
         */
        public void setNameEncoding(
        	String encoding
        ){
        	setEncoding(encoding);
        }

        /**
    	 * Set the archive's filter set
    	 * 
    	 * @param filterset
    	 */
    	public void setFilters(
    		FilterChainCollection filterset
        ){
    		this.filterset = filterset;
    	}

    	/**
         * Link the implicit file sets
         * 
         * @param refrerence a reference to the archive's implicit fileset
         */
        public void setImplicitFileSet (
        	Reference reference
        ) throws BuildException {
        	getImplicitFileSet().setRefid(reference);
        }
    	    	
        /* (non-Javadoc)
    	 * @see org.apache.tools.ant.taskdefs.Zip#zipFile(java.io.InputStream, org.apache.tools.zip.ZipOutputStream, java.lang.String, long, java.io.File, int)
    	 */
    	@Override
		protected void zipFile(
    		InputStream in, 
    		ZipOutputStream zOut, 
    		String vPath, 
    		long lastModified, 
    		File fromArchive, 
    		int mode
    	) throws IOException {
    		Entry entry = filterset.getEntry(
				in, 
				vPath, getProject()
			); 
    		super.zipFile(
    			entry.getStream(), 
    			zOut, 
    			entry.getPath(), 
    			lastModified, 
    			fromArchive, 
    			mode
    		);
    	}
    	
		/* (non-Javadoc)
		 * @see org.openmdx.tools.ant.taskdefs.Archive.Delegate#setCompression(org.openmdx.tools.ant.taskdefs.CompressionMethod)
		 */
		public CompressionMethod setCompression(
			CompressionMethod compression
		) {
			if(compression == null) {
				compression = new CompressionMethod();
				compression.setValue(CompressionMethod.ZIP);
			}
			super.setCompress(
				zipCompressionMethod(compression)
			);
			return compression;
		}
    	
		
    }

    
    //------------------------------------------------------------------------
	// Class Jar
	//------------------------------------------------------------------------

    /**
     * Handles archive requests for<ul>
     * <li>jar
     * <li>war
     * <li>rar
     * <li>par
     * <li>ear
     * </ul>
     */
    protected static class Jar 
    	extends org.apache.tools.ant.taskdefs.Jar
    	implements Delegate
    {
    	
        /**
		 * Constructor
		 */
		protected Jar(
		) {
			setNameEncoding(Encodings.UTF_8);
		}

		/**
		 * 
		 */
		protected FilterChainCollection filterset;
    	
    	/**
         * Sets the character encoding for the archive entry names.
         * 
         * @param encoding the character encoding
         */
        public void setNameEncoding(
        	String encoding
        ){
        	setEncoding(encoding);
        }

        /**
    	 * Set the archive's filter set
    	 * 
    	 * @param filterset
    	 */
    	public void setFilters(
    		FilterChainCollection filterset
        ){
    		this.filterset = filterset;
    	}

    	/**
         * Link the implicit file sets
         * 
         * @param refrerence a reference to the archive's implicit fileset
         */
        public void setImplicitFileSet (
        	Reference reference
        ) throws BuildException {
        	getImplicitFileSet().setRefid(reference);
        }
    	    	
        /* (non-Javadoc)
    	 * @see org.apache.tools.ant.taskdefs.Zip#zipFile(java.io.InputStream, org.apache.tools.zip.ZipOutputStream, java.lang.String, long, java.io.File, int)
    	 */
    	@Override
		protected void zipFile(
    		InputStream in, 
    		ZipOutputStream zOut, 
    		String vPath, 
    		long lastModified, 
    		File fromArchive, 
    		int mode
    	) throws IOException {
    		Entry entry = filterset.getEntry(
				in, 
				vPath, getProject()
			); 
    		super.zipFile(
				entry.getStream(), 
    			zOut, 
    			entry.getPath(), 
    			lastModified, 
    			fromArchive, 
    			mode
    		);
    	}

		/* (non-Javadoc)
		 * @see org.openmdx.tools.ant.taskdefs.Archive.Delegate#setCompression(org.openmdx.tools.ant.taskdefs.CompressionMethod)
		 */
		public CompressionMethod setCompression(
			CompressionMethod compression
		) {			
			if(compression == null) {
				compression = new CompressionMethod();
				compression.setValue(CompressionMethod.ZIP);
			}
			super.setCompress(
				zipCompressionMethod(compression)
			);
			return compression;
		}
    	
    }

    
    //------------------------------------------------------------------------
	// Class Tar
	//------------------------------------------------------------------------

    /**
     * Handles archive requests for the "tar" format.
     */
    protected static class Tar 
    	extends org.apache.tools.ant.taskdefs.Tar
    	implements Delegate
    {

		/**
		 * Constructor
		 */
		protected Tar(
			ArchiveTask parent
		) {
			setLongfile(GNU_MODE);
			setNameEncoding(Encodings.US_ASCII);
			this.parent = parent;
		}

		protected FilterChainCollection filterset;
		private File destFile = null;	
		final private ArchiveTask parent;
    	final static TarCompressionMethod NO_COMPRESSION = new TarCompressionMethod();
    	final static TarCompressionMethod GZIP_COMPRESSION = new TarCompressionMethod();
    	final static TarCompressionMethod BZIP2_COMPRESSION = new TarCompressionMethod();
    	final static TarLongFileMode GNU_MODE = new TarLongFileMode();
    	
		/**
         * Sets the character encoding for the archive entry names.
         * 
         * @param encoding the character encoding
         */
        public void setNameEncoding(
        	String encoding
        ){
        	if(
        		encoding != null &
        		!Encodings.US_ASCII.equals(encoding)
        	) throw new BuildException(
        		"tar archive file names are restricted to US_ASCII"
        	);
        }

        /**
         * Sets behavior for when a duplicate file is about to be added -
         * one of <code>add</code>, <code>preserve</code> or <code>fail</code>:<ul>
         * <li><code>add</code> (keep both of the files)
         * <li><code>preserve</code> (keep the first version of the file found)
         * <li><code>fail</code> (throw a build exception)
         * </ul>
         * Default for archive tasks is <code>add</code>.
         * <p>
         * <code>preserve</code> and <code>fail</code> are not allowed 
         * in case of <code>tar</code> format.
         * 
         * @param duplicate the value to be set
         */
        public void setDuplicate(
        	Duplicate duplicate
        ) {
        	if(
        		duplicate != null &&
        		!"add".equals(duplicate.getValue())
        	) throw new BuildException(
        		"tar archive duplicate value is restricted to 'add'"
        	);
        }

        /**
    	 * Set the archive's filter set
    	 * 
    	 * @param filterset
    	 */
    	public void setFilters(
    		FilterChainCollection filterset
        ){
    		this.filterset = filterset;
    	}

    	/**
         * Link the implicit file sets
         * 
         * @param refrerence a reference to the archive's implicit fileset
         */
        public void setImplicitFileSet (
        	Reference reference
        ) throws BuildException {
        	getImplicitFileSet().setRefid(reference);
        }

        /**
         * Whether we want to compress the files or only store them.
         */
        public CompressionMethod setCompression(
        	CompressionMethod compression
        ){
        	if(compression == null) {
        		compression = new CompressionMethod();
        		compression.setValue(CompressionMethod.GZIP);
        	}
        	super.setCompression(
        		tarCompressionMethod(compression)
        	);
        	return compression;
        }

        /**
         * Adds a set of files.
         */
        public void addFileset(FileSet fileSet) {
        	String id = References.newId();
        	Project project = getProject();
        	TarFileSet target = this.createTarFileSet();
        	if(fileSet instanceof ZipFileSet) {
        		ZipFileSet source = (ZipFileSet) fileSet;
        		if(source.getSrc(project) == null) {
                	project.addReference(
                		id,
                    	new TarFileSet(fileSet)
                	);
        		} else if(source instanceof ArchiveFileSet) {
                	project.addReference(
                		id,
                    	new TarFileSet(((ArchiveFileSet)source).toTarFileSet(this.parent))
                	);
    			} else throw new BuildException(
        			"Archives in tar format can be populated from another archive by specifying an ArchiveFileSet: " + 
        			source.getSrc(project)
        		);
        		if(source.hasDirModeBeenSet()) target.setDirMode(
        			Integer.toOctalString(source.getDirMode(project))
        		);
        		if(source.hasFileModeBeenSet()) target.setMode(
	    			Integer.toOctalString(source.getFileMode(project))
        		);
        		target.setPrefix(source.getPrefix(project));
        		target.setFullpath(source.getFullpath(project));
        	} else if (fileSet instanceof TarFileSet) {
        		TarFileSet source = (TarFileSet) fileSet;
        		target.setFollowSymlinks(source.isFollowSymlinks());        		
        		target.setDirMode(
        			Integer.toOctalString(source.getDirMode() % 01000) 
        		);
        		target.setFullpath(source.getFullpath());
        		target.setGid(source.getGid());
        		target.setGroup(source.getGroup());
        		target.setMode(
        			Integer.toOctalString(source.getMode() % 01000)
        		);
        		target.setPrefix(source.getPrefix());
        		target.setPreserveLeadingSlashes(source.getPreserveLeadingSlashes());
        		target.setUid(source.getUid());
        		target.setUserName(source.getUserName());
            	project.addReference(
            		id,
                	fileSet
            	);
        	} else {
            	project.addReference(
            		id,
                	new TarFileSet(fileSet)
            	);
        	}
        	target.setRefid(
        		new Reference(project, id)
        	);
        }


		/**
		 * @return Returns the destFile.
		 */
		public File getDestFile() {
			return destFile;
		}

		/**
		 * @param destFile The destFile to set.
		 */
		@Override
		public void setDestFile(File destFile) {
			super.setDestFile(
				this.destFile = destFile
			);
		}

		/* (non-Javadoc)
		 * @see org.apache.tools.ant.taskdefs.Tar#tarFile(java.io.File, org.apache.tools.tar.TarOutputStream, java.lang.String, org.apache.tools.ant.taskdefs.Tar.TarFileSet)
		 */
		@Override
		protected void tarFile(
			File file, 
			TarOutputStream tOut, 
			String vPath, 
			TarFileSet tarFileSet
		) throws IOException {
	        FileInputStream fIn = null;
	        InputStream in = null;

	        String fullpath = tarFileSet.getFullpath();
	        if (fullpath.length() > 0) {
	            vPath = fullpath;
	        } else {
	            // don't add "" to the archive
	            if (vPath.length() <= 0) {
	                return;
	            }

	            if (file.isDirectory() && !vPath.endsWith("/")) {
	                vPath += "/";
	            }

	            String prefix = tarFileSet.getPrefix();
	            // '/' is appended for compatibility with the zip task.
	            if (prefix.length() > 0 && !prefix.endsWith("/")) {
	                prefix = prefix + "/";
	            }
	            vPath = prefix + vPath;
	        }

	        if (vPath.startsWith("/") && !tarFileSet.getPreserveLeadingSlashes()) {
	            int l = vPath.length();
	            if (l <= 1) {
	                // we would end up adding "" to the archive
	                return;
	            }
	            vPath = vPath.substring(1, l);
	        }

	        try {
	            TarEntry te = new TarEntry(vPath);
	            te.setModTime(file.lastModified());
	            if (file.isDirectory()) {
	                te.setMode(tarFileSet.getDirMode());
	            } else {
	        		Entry entry = filterset.getEntry(
                		fIn = new FileInputStream(file), 
        				vPath, getProject()
        			);
	            	in = entry.getStream();
	                if(in == fIn) {
		                te.setSize(file.length());
	                } else {
	                	in = new BufferedInputStream(in);
	                	in.mark(Integer.MAX_VALUE);
	                	long size = 0L, step = 0L;
	                	while(
                			(step = in.skip(Long.MAX_VALUE)) > 0L
	                	) size += step;
	                	while(
	                		in.read() > 0
	                	) size++;
	                	in.reset();    
		                te.setSize(size);
	                }
	                te.setMode(tarFileSet.getMode());
	            }
	            te.setUserName(tarFileSet.getUserName());
	            te.setGroupName(tarFileSet.getGroup());
	            te.setUserId(tarFileSet.getUid());
	            te.setGroupId(tarFileSet.getGid());

	            tOut.putNextEntry(te);

	            if (!file.isDirectory()) {

	                byte[] buffer = new byte[8 * 1024];
	                int count = 0;
	                do {
	                    tOut.write(buffer, 0, count);
	                    count = in.read(buffer, 0, buffer.length);
	                } while (count != -1);
	            }

	            tOut.closeEntry();
	        } finally {
	            if (in != null) {
	                in.close();
	            } else if (fIn != null) {
	            	fIn.close();
	            }
	        }
		}
		
        static {
        	NO_COMPRESSION.setValue("none");
        	GZIP_COMPRESSION.setValue("gzip");
        	BZIP2_COMPRESSION.setValue("bzip2");
        	GNU_MODE.setValue(TarLongFileMode.GNU);
        }

    }


	//------------------------------------------------------------------------
	// Class Nested
	//------------------------------------------------------------------------

    /**
     * 
     */
    public static class Nested extends Archive {

		/**
		 * @param parent 
		 * @param format
		 * 
		 * @throws IOException 
		 */
		Nested(
			Archive parent, 
			String format
		) throws IOException {
			this.parent = parent;
			super.setFormat(new Format(format));
		}
    
		/**
		 * 
		 */
		private final Archive parent;
		
		/**
		 * The full pathname of the nested archive
		 */
	    private String fullpath = null;

		/**
		 * Tells whether the destfile is a temporary file
		 */
	    private boolean tempFile = true;

		/* (non-Javadoc)
		 * @see org.openmdx.tools.ant.taskdefs.Archive#getDestFile()
		 */
		@Override
		public File getTempDir(
		) {
			return this.parent.getTempDir();
		}

	    /**
	     * Set the full pathname of the nested archive.
	     *
	     * @param fullpath the full pathname of the nested archive
	     */
	    public void setFullpath(String fullpath) {
	        this.fullpath = fullpath;
	    }

	    /**
	     * Retrieve the full pathname of the single entry in this fileset.
	     * 
	     * @return the full pathname of the single entry in this fileset.
	     */
	    protected String getFullPath() {
	        return this.fullpath;
	    }

		/**
		 * Retrieve the parent.
		 * 
		 * @return the parent
		 */
		protected  Archive getParent(
		) {
			return this.parent;
		}

		/* (non-Javadoc)
		 * @see org.openmdx.tools.ant.taskdefs.Archive#getDestFile()
		 */
		@Override
		protected File getDestFile(
		) throws BuildException {
			File destFile = super.getDestFile();
			if(destFile == null) try {
				super.setDestFile(
					destFile = File.createTempFile(
						"ant.archive$", 
						'.' + getFormat().getValue(),
						getTempDir()
					)
				);
			} catch (IOException exception) {
				throw new BuildException(
					"Failed to create temporary archive file", 
					exception
				);
			}
			return destFile;
		}

		/* (non-Javadoc)
		 * @see org.openmdx.tools.ant.taskdefs.Archive#setDestFile(java.io.File)
		 */
		@Override
		public void setDestFile(File destFile) {
			this.tempFile = false;
			super.setDestFile(destFile);
		}

		/**
		 * Tells whether the archive file should be deleted a the end
		 * @return Returns <code>true</code> if the archive file should be 
		 * deleted a the end
		 */
		public boolean isTempFile() {
			return this.tempFile;
		}

		/* (non-Javadoc)
		 * @see org.openmdx.tools.ant.taskdefs.Archive#execute()
		 */
		@Override
		public void execute() throws BuildException {
			Archive parent = getParent();
			File parentFile = parent.getDestFile();
			File destFile = getDestFile();
			File destDir = destFile.getParentFile();
			String destName = destFile.getName();
			if(parentFile.exists()){
				UnarchivePatternSet unarchivePatternSet = new UnarchivePatternSet();
				unarchivePatternSet.setFullpath(destName);
				unarchivePatternSet.createInclude().setName(getFullPath());
				Unarchive unarchive = new Unarchive();
				unarchive.setProject(getProject());
				unarchive.setTaskName(unarchive.getTaskName());
				unarchive.setFormat(
					new Unarchive.Format(getFormat().getValue())
				);
				unarchive.setCompression(getCompression());
				unarchive.setEncoding(getNameEncoding());
				unarchive.setSrc(parentFile);
				unarchive.setDest(destDir);
				unarchive.addUnarchivepatternset(unarchivePatternSet);
				unarchive.execute();
			} else if (destFile.exists()) {
				destFile.delete();
			}
			super.execute();
			ArchiveFileSet archiveFileSet = new ArchiveFileSet();
			archiveFileSet.setFullpath(getFullPath());
			archiveFileSet.setDir(destDir);
			archiveFileSet.createInclude().setName(destName);
			parent.addArchivefileset(archiveFileSet);
		}
		
    }

}
