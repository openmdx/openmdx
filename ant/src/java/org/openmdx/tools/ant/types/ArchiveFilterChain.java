/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ArchiveFilterChain.java,v 1.8 2005/12/09 17:19:40 hburger Exp $
 * Description: Archive Filter Chain
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/12/09 17:19:40 $
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

import java.io.File;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.filters.ChainableReader;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.apache.tools.ant.util.FileNameMapper;
import org.openmdx.tools.ant.filters.ElementFilter;
import org.openmdx.tools.ant.util.Encodings;

/**
 * Archive Filter Chain
 */
public class ArchiveFilterChain extends FilterChain {

    /**
     * Analyze the input stream in oder to determine the encoding.
     */
    private static final String DEFAULT_INPUT_ENCODING = null;
    
    /**
     * Create UTF-8 files by default
     */
    private static final String DEFAULT_OUTPUT_ENCODING = Encodings.UTF_8;

    private PatternSet defaultPatterns = new PatternSet();
    private boolean hasDefaultPatterns = false;
    private Vector additionalPatterns = new Vector();

    private boolean initialized = false;
    private String[] includePatterns = null;
    private String[] excludePatterns = null;
    
    private String inputEncoding = DEFAULT_INPUT_ENCODING;
    private String outputEncoding = DEFAULT_OUTPUT_ENCODING;

    private boolean byteOrderMarkAware = true;
    private boolean xmlDeclarationAware = true;
    
    protected Mapper mapperElement = null;

    /**
     * Creates a nested patternset.
     * @return <code>PatternSet</code>.
     */
    public PatternSet createPatternSet() {
    	checkChildrenAllowed();
        PatternSet patterns = new PatternSet();
        this.additionalPatterns.addElement(patterns);
        return patterns;
    }

    /**
     * Add a name entry to the include list.
     * @return <code>PatternSet.NameEntry</code>.
     */
    public PatternSet.NameEntry createInclude() {
    	checkChildrenAllowed();
    	this.hasDefaultPatterns = true;
        return defaultPatterns.createInclude();
    }

    /**
     * Add a name entry to the include files list.
     * @return <code>PatternSet.NameEntry</code>.
     */
    public PatternSet.NameEntry createIncludesFile() {
    	checkChildrenAllowed();
    	this.hasDefaultPatterns = true;
        return defaultPatterns.createIncludesFile();
    }

    /**
     * Add a name entry to the exclude list.
     * @return <code>PatternSet.NameEntry</code>.
     */
    public PatternSet.NameEntry createExclude() {
    	checkChildrenAllowed();
    	this.hasDefaultPatterns = true;
        return defaultPatterns.createExclude();
    }

    /**
     * Add a name entry to the excludes files list.
     * @return <code>PatternSet.NameEntry</code>.
     */
    public PatternSet.NameEntry createExcludesFile() {
    	checkChildrenAllowed();
    	this.hasDefaultPatterns = true;
        return defaultPatterns.createExcludesFile();
    }
    
    /**
	 * @return Returns the defaultPatterns.
	 */
	protected final PatternSet getDefaultPatterns() {
		return this.defaultPatterns;
	}

    /**
	 * @return Returns the additionalPatterns.
	 */
	protected final Vector getAdditionalPatterns() {
		return this.additionalPatterns;
	}

	/**
     * Appends <code>includes</code> to the current list of include
     * patterns.
     *
     * <p>Patterns may be separated by a comma or a space.</p>
     *
     * @param includes the <code>String</code> containing the include patterns.
     */
    public void setIncludes(String includes) {
    	checkAttributesAllowed();
    	defaultPatterns.setIncludes(includes);
    }

    /**
     * Appends <code>excludes</code> to the current list of exclude
     * patterns.
     *
     * <p>Patterns may be separated by a comma or a space.</p>
     *
     * @param excludes the <code>String</code> containing the exclude patterns.
     */
    public void setExcludes(String excludes) {
    	checkAttributesAllowed();
        defaultPatterns.setExcludes(excludes);
    }

    /**
     * Sets the <code>File</code> containing the includes patterns.
     *
     * @param incl <code>File</code> instance.
     */
     public void setIncludesfile(File incl) throws BuildException {
       	 checkAttributesAllowed();
         defaultPatterns.setIncludesfile(incl);
     }

    /**
     * Sets the <code>File</code> containing the excludes patterns.
     *
     * @param excl <code>File</code> instance.
     */
     public void setExcludesfile(File excl) throws BuildException {
    	 checkAttributesAllowed();
         defaultPatterns.setExcludesfile(excl);
     }

    /**
     * @see org.apache.tools.ant.ProjectComponent#setProject
     */
    public void setProject(Project project) {
    	this.defaultPatterns.setProject(project);
        super.setProject(project);
    }

    /**
	 * @return Returns the inputEncoding.
	 */
	public String getInputEncoding() {
		return inputEncoding;
	}

	/**
     * Sets the character encoding for input files.
     * @param encoding the character encoding
     */
    public void setInputEncoding(String encoding) {
        this.inputEncoding = encoding;
    }

	/**
	 * @return Returns the outputEncoding.
	 */
	public String getOutputEncoding() {
		return outputEncoding;
	}

    /**
     * Sets the character encoding for output files.
     * @param encoding the character encoding
     */
    public void setOutputEncoding(String encoding) {
        this.outputEncoding = encoding;
    }

    /**
     * Sets whether byte order marks should be handled by this archive filter 
     * chain.
     *
     * @param byteOrderMarkAware <code>boolean</code>.
     */
    public void setByteordermarkaware(boolean byteOrderMarkAware) {
   	 	checkAttributesAllowed();
        this.byteOrderMarkAware = byteOrderMarkAware;
    }

    /**
     * Tells whether byte order marks should be handled by this archive filter 
     * chain.
     */
    public boolean getByteordermarkaware() {
        return isReference() ? 
        	getRef().getByteordermarkaware() : 
        	this.byteOrderMarkAware;
    }

    /**
     * Sets whether byte order marks should be handled by this archive filter 
     * chain.
     *
     * @param byteOrderMarkAware <code>boolean</code>.
     */
    public void setXmldeclarationaware(boolean xmlDeclarationAware) {
   	 	checkAttributesAllowed();
        this.xmlDeclarationAware = xmlDeclarationAware;
    }

    /**
     * Tells whether byte order marks should be handled by this archive filter 
     * chain.
     */
    public boolean getXmldeclarationaware() {
        return isReference() ?
            getRef().getXmldeclarationaware() : 
            this.xmlDeclarationAware;
    }

    /**
     * Adds an ElementFilter
     * 
     * @param elementFilter
     */
    public final void addElementFilter(final ElementFilter elementFilter) {
        getFilterReaders().addElement(elementFilter);
    }

    /**
     * Adds a filter reference
     * 
     * @param filter
     */
    public final void addFilter(
    	final Filter filter
    ){
        getFilterReaders().addElement(filter);
    }
    
    /**
     * Performs the check for circular references and returns the
     * referenced Selector.
     */
    private ArchiveFilterChain getRef(){
        return (ArchiveFilterChain) getCheckedRef(
        	ArchiveFilterChain.class, 
        	"archivefilterchain"
        );
    }
    
    /**
     * Defines the mapper to map source to destination files.
     * @return a mapper to be configured
     * @exception BuildException if more than one mapper is defined
     */
    public Mapper createMapper() throws BuildException {
        if (mapperElement != null) throw new BuildException(
        	"Cannot define more than one mapper"
        );
        return mapperElement = new Mapper(getProject());
    }

    /**
     * A nested filenamemapper
     * @param fileNameMapper the mapper to add
     * @since Ant 1.6.3
     */
    public void add(FileNameMapper fileNameMapper) {
        createMapper().add(fileNameMapper);
    }

    /**
     * Retrieve this  archive filter chain's mapper
     * 
     * @return
     */
    public FileNameMapper getMapper(){
    	return this.mapperElement == null ? 
    		null : 
    		this.mapperElement.getImplementation();
    }
    
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.types.FilterChain#setRefid(org.apache.tools.ant.types.Reference)
	 */
	public void setRefid(Reference reference) throws BuildException {
        if (
        	this.hasDefaultPatterns ||
        	!this.additionalPatterns.isEmpty() 
        ) throw noChildrenAllowed();
        super.setRefid(reference);
    	ArchiveFilterChain delegate = getRef();
        this.defaultPatterns = delegate.getDefaultPatterns();
        this.additionalPatterns = delegate.getAdditionalPatterns();
	}

    /**
     * 
     * @param path
     * @return
     */
    public boolean appliesTo(
    	String path
    ){
    	if(!this.initialized) {
    		Project p = getProject();
            for(
            	Enumeration e = additionalPatterns.elements();
            	e.hasMoreElements();
            ) defaultPatterns.append((PatternSet) e.nextElement(), p);
            this.includePatterns = defaultPatterns.getIncludePatterns(p);
            this.excludePatterns = defaultPatterns.getExcludePatterns(p);
            this.initialized = true;
    	}
    	boolean included = this.includePatterns == null;
    	for(
    		int i = 0;
    		!included && i < this.includePatterns.length;
    		i++
    	) included = SelectorUtils.matchPath(this.includePatterns[i], path);
    	if(this.excludePatterns != null) for(
    		int i = 0;
    		included && i < this.excludePatterns.length;
    		i++
    	) included = !SelectorUtils.matchPath(this.excludePatterns[i], path);
    	return included;
    }


    //------------------------------------------------------------------------
    // Class Filter
    //------------------------------------------------------------------------

    /**
     * A delegating filter
     */
    public static class Filter 
		extends DataType
    	implements ChainableReader
    {

		/* (non-Javadoc)
		 * @see org.apache.tools.ant.filters.ChainableReader#chain(java.io.Reader)
		 */
		public Reader chain(Reader rdr) {
			return isReference() ? getRef().chain(rdr) : rdr;
		}

		/* (non-Javadoc)
		 * @see org.apache.tools.ant.types.DataType#setRefid(org.apache.tools.ant.types.Reference)
		 */
		public void setRefid(Reference ref) {
			super.setRefid(ref);
			getRef(); // verify referenced class
		}
    	
	    /**
	     * Performs the check for circular references and returns the
	     * referenced filter.
	     */
	    private ChainableReader getRef(){
	        return (ChainableReader) getCheckedRef(
        		ChainableReader.class, 
	        	"filter"
	        );
	    }
		
    }

}
