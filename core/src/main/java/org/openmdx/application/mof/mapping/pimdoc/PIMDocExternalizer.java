/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: PIMDoc Externalizer
 * Owner:       the original authors.
 * ====================================================================
 * 
 * This software is published under the BSD license as listed below.
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.application.mof.mapping.pimdoc;

import org.openmdx.application.mof.mapping.pimdoc.image.ClusterImageMapper;
import org.openmdx.application.mof.mapping.pimdoc.spi.Archiving;
import org.openmdx.application.mof.mapping.pimdoc.spi.NamespaceFilter;
import org.openmdx.application.mof.mapping.pimdoc.spi.PackagePatternComparator;
import org.openmdx.application.mof.mapping.pimdoc.text.ClassMapper;
import org.openmdx.application.mof.mapping.pimdoc.text.IndexMapper;
import org.openmdx.application.mof.mapping.pimdoc.text.ClusterTextMapper;
import org.openmdx.application.mof.mapping.pimdoc.text.PackageMapper;
import org.openmdx.application.mof.mapping.pimdoc.text.StructureMapper;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.io.Sink;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.exception.BasicException;

/**
 * This class externalizes the PIM documentation 
 */
class PIMDocExternalizer {

    PIMDocExternalizer(
    	Model_1_0 model,
		Sink sink,
		boolean markdown, 
		PIMDocConfiguration configuration
    ){
    	this.model = model;
    	this.sink = sink;
    	this.markdown = markdown;
        this.configuration = configuration;
    }

    private final Model_1_0 model;
    
    /**
     * Tells whether annotations use markdown
     */
    private final boolean markdown;
    
    /**
     * The configuration providing default values where necessary
     */
    private final PIMDocConfiguration configuration;

	/**
     * The archive for the produced entries
     */
	private final Sink sink;

	public void externalize() {
        try {
	        exportResources();
        	exportIndexFile();
        	exportNamespaces();
        	exportPackageGroups();
        } catch(Exception exception) {
			throw new RuntimeServiceException(
				exception,
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.PROCESSING_FAILURE,
				"Unable to externalize package(s)"
			);
        }    
	}

	private void exportPackageGroups() {
		configuration
			.getTableOfContentEntries()
			.stream()
			.map(PackagePatternComparator::getAncestor)
			.map(model::findElement)
			.forEach(this::exportPackageCluster);
	}

	private void exportNamespaces() {
		this.model
			.getContent()
			.stream()
			.filter(ModelElement_1_0::isPackageType)
			.forEach(this::exportNamespace);
	}
	
	private void exportNamespace(
		final ModelElement_1_0 namespace
	){
		exportPackageFile(namespace);
		exportClassFiles(namespace);
		exportStructureFiles(namespace);
	}

	private void exportStructureFiles(final ModelElement_1_0 namespace) {
		namespace
			.getModel()
			.getContent()
			.stream()
			.filter(new NamespaceFilter(namespace))
			.filter(ModelElement_1_0::isStructureType)
			.forEach(this::exportStructureFile);
	}

	private void exportClassFiles(final ModelElement_1_0 namespace) {
		namespace
			.getModel()
			.getContent()
			.stream()
			.filter(new NamespaceFilter(namespace))
			.filter(ModelElement_1_0::isClassType)
			.forEach(this::exportClassFile);
	}		
	
	/**
	 * Export the (configurable) magic files.
	 * 
	 * @see MagicFile#CUSTOM
	 * @see MagicFile#STYLE
	 */
	private void exportResources(){
		addResources(MagicFile.STYLE);
		addResources(MagicFile.CUSTOM);
	}

	/**
	 * Export the (configurable) magic files.
	 * @param buffer TODO
	 * 
	 * @see MagicFile.Type#TEXT
	 * @see MagicFile.Type#IMAGE
	 */
	private void addResources(MagicFile magicFile){
		addResource(magicFile, MagicFile.Type.TEXT);
		addResource(magicFile, MagicFile.Type.IMAGE);
	}
	
	/**
	 * Export the (configurable) magic file.
	 * @param buffer TODO
	 */
	private void addResource(MagicFile magicFile, MagicFile.Type type) {
		final MagicResource resource = newResource(magicFile, type);
		resource.copyTo(sink);
	}

	private void exportIndexFile() {
		try (Archiving indexMapper = new IndexMapper(sink, model, markdown, configuration)){
			indexMapper.createArchiveEntry();
		}
	}

	private void exportPackageFile(ModelElement_1_0 packageToBeExported){
		try (Archiving mapper = new PackageMapper(sink, packageToBeExported, markdown, configuration)){
			mapper.createArchiveEntry();
		}
	}

	private void exportClassFile(ModelElement_1_0 classToBeExported){
		try (Archiving mapper = new ClassMapper(sink, classToBeExported, markdown, configuration)){
			mapper.createArchiveEntry();
		}
	}

	private void exportStructureFile(ModelElement_1_0 structureToBeExported){
		try (Archiving mapper = new StructureMapper(sink, structureToBeExported, markdown, configuration)){
			mapper.createArchiveEntry();
		}
	}

	private void exportPackageCluster(ModelElement_1_0 ancestor){
		try (Archiving mapper = new ClusterTextMapper(sink, ancestor, markdown, configuration)){
			mapper.createArchiveEntry();
		}
		try (Archiving mapper = new ClusterImageMapper(sink, ancestor, markdown, configuration)){
			mapper.createArchiveEntry();
		}
	}


	MagicResource newResource(MagicFile magicFile, MagicFile.Type type) {
		return new MagicResource(configuration.getActualFile(magicFile, type), magicFile.getFileName(type));
	}

}
