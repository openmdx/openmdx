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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.openmdx.application.mof.mapping.pimdoc.image.ClusterDiagramMapper;
import org.openmdx.application.mof.mapping.pimdoc.spi.Archiving;
import org.openmdx.application.mof.mapping.pimdoc.spi.NamespaceFilter;
import org.openmdx.application.mof.mapping.pimdoc.text.ClassMapper;
import org.openmdx.application.mof.mapping.pimdoc.text.ImageFrameMapper;
import org.openmdx.application.mof.mapping.pimdoc.text.IndexMapper;
import org.openmdx.application.mof.mapping.pimdoc.text.PackageMapper;
import org.openmdx.application.mof.mapping.pimdoc.text.StructureMapper;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.Sink;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.image.GraphvizTemplates;
import org.openmdx.base.mof.spi.PIMDocFileType;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.BinaryLargeObjects;

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
        	exportDiagrams();
	        exportResources();
        	exportIndexFile();
        	exportNamespaces();
        	exportAllPackageCluster();
        	exportImageFrames();
        } catch(Exception exception) {
			throw new RuntimeServiceException(
				exception,
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.PROCESSING_FAILURE,
				"Unable to externalize package(s)"
			);
        }    
	}

	private void exportDiagrams() {
		configuration.getGraphvizTemplateDirectory().ifPresent(this::drawDiagrams);
	}
	
	private void drawDiagrams(File sourceDir) {
		try {
			final GraphvizTemplates graphvizDiagrams = new GraphvizTemplates(model, configuration.getGraphvizStyleSheet(), sink);
			graphvizDiagrams.drawDiagrams(sourceDir);
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
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
		exportPackageCluster(namespace);
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
	 * This must be called after the package cluster export
	 * 
	 * @see #exportNamespaces()
	 * @see #exportPackageCluster(ModelElement_1_0)
	 * @see #exportAllPackageCluster()
	 */
	private void exportImageFrames() {
		getGraphvizSourceEntries().forEach(this::exportImageFrame);
	}

	/**
	 * The entries must be copied into a new Map as adding image 
	 * frames will modify the table of content.
	 * 
	 * @return a Map containing the Graphviz source entries
	 */
	private Map<URI, String> getGraphvizSourceEntries() {
		final Map<URI, String> dotFiles = new HashMap<>();
		for(Map.Entry<URI, String> e : sink.getTableOfContent().entrySet()) {
			if(PIMDocFileType.GRAPHVIZ_SOURCE.test(e.getKey().getPath())) {
				dotFiles.put(e.getKey(), e.getValue());
			}
		}
		return dotFiles;
	}
	
	/**
	 * Export the (configurable) magic files.
	 * 
	 * @see MagicFile#WELCOME_PAGE
	 * @see MagicFile#STYLE_SHEET
	 * @see MagicFile#UML_SYMBOL
	 */
	private void exportResources(){
		addResource(MagicFile.STYLE_SHEET, MagicFile.Type.TEXT);
		addResource(MagicFile.STYLE_SHEET, MagicFile.Type.IMAGE);
		addResource(MagicFile.WELCOME_PAGE, MagicFile.Type.TEXT);
		addResource(MagicFile.WELCOME_PAGE, MagicFile.Type.IMAGE);
		addResource(MagicFile.UML_SYMBOL, MagicFile.Type.IMAGE);
	}

	/**
	 * Export the (configurable) magic file.
	 */
	private void addResource(MagicFile magicFile, MagicFile.Type type) {
		try {
			sink.accept(
				configuration.getTargetName(magicFile, type), 
				magicFile.name() + " (" + type.name() + ")",
				BinaryLargeObjects.createByteArrayOutputStream(configuration.getSource(magicFile, type))
			);
		} catch (IOException exception) {
			throw new RuntimeServiceException(
				exception,
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.PROCESSING_FAILURE,
				"Unable to add resource",
				new BasicException.Parameter("magic-file", magicFile),
				new BasicException.Parameter("magic-file-type", type)
			);
		}
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

	private void exportAllPackageCluster(){
		try (Archiving mapper = new ClusterDiagramMapper(sink, model, markdown, configuration)){
			mapper.createArchiveEntry();
		}
	}
	
	private void exportPackageCluster(ModelElement_1_0 ancestor){
		try (Archiving mapper = new ClusterDiagramMapper(sink, ancestor, markdown, configuration)){
			mapper.createArchiveEntry();
		}
	}

	private void exportImageFrame(URI name, String title){
		if(PIMDocFileType.GRAPHVIZ_SOURCE.test(name.getPath())) {
			try (Archiving mapper = new ImageFrameMapper(sink, model, name, title, markdown, configuration)){
				mapper.createArchiveEntry();
			}
		}
	}

}
