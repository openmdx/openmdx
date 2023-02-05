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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * PIM documentation in HTML format
 */
class PIMDocExternalizer implements Consumer<List<ModelElement_1_0>>, AutoCloseable {

    PIMDocExternalizer(
		BiConsumer<String, ByteArrayOutputStream> archive,
		boolean markdown, 
		PIMDocConfiguration configuration
    ){
    	this.archive = archive;
    	this.markdown = markdown;
        this.configuration = configuration;
        this.buffer = new ByteArrayOutputStream();
    }

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
	private final BiConsumer<String, ByteArrayOutputStream> archive;

	/**
	 * The re-usable {@code Stream}
	 */
    private final ByteArrayOutputStream buffer;
    
    /**
     * The (configurable) resources to be added to the archive
     */
    private final static Set<MagicFile> RESOURCES = EnumSet.of(MagicFile.LOGO, MagicFile.STYLE_SHEET, MagicFile.WELCOME);
    
	@Override
	public void accept(List<ModelElement_1_0> packagesToExport) {
        try {
	        SysLog.detail("copying magic files");
	        addResources();
	        SysLog.detail("exporting index file", packagesToExport);
        	exportIndexFile(packagesToExport);
	        SysLog.detail("exporting packages", packagesToExport);
        	packagesToExport.forEach(this::exportNamespace);
        } catch(Exception exception) {
			throw new RuntimeServiceException(
				exception,
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.PROCESSING_FAILURE,
				"Unable to externalize package(s)"
			);
        }    
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
	 * @throws IOException 
	 * 
	 * @see MagicFile#LOGO
	 * @see MagicFile#WELCOME
	 * @see MagicFile#STYLE_SHEET
	 */
	private void addResources(){
		RESOURCES.forEach(this::addResource);
	}
	
	private void addResource(MagicFile magicFile) {
		this.buffer.reset();
		copyBinary(configuration.getActualFile(magicFile));
		this.archive.accept(magicFile.getFileName(), this.buffer);
	}

	/**
	 * Copies a resource to the buffer
	 *
	 * @param url the resource's URL
	 * 
	 * @throws RuntimeServiceException in case of failure
	 */
	protected void copyBinary(URL url) {
		final byte[] bytes = new byte[1024];
		try(InputStream s = url.openStream()){
			for(int i = s.read(bytes); i > 0; i = s.read(bytes)){
				this.buffer.write(bytes, 0, i);
			}
		} catch (IOException exception) {
			throw new RuntimeServiceException(
				exception,
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.PROCESSING_FAILURE,
				"Unable to retrieve resource",
				new BasicException.Parameter("url", url)
			);
		}
	}
	
	private void exportIndexFile(List<ModelElement_1_0> packagesToBeExported) {
		try (Sink sink = newSink()){
			new IndexMapper(sink, packagesToBeExported, markdown, configuration).createArchiveEntry();
		} catch (IOException e) {
			throw new RuntimeServiceException(e);
		}
	}

	private void exportPackageFile(ModelElement_1_0 packageToBeExported){
		try (Sink sink = newSink()){
			new PackageMapper(sink, packageToBeExported, markdown, configuration).createArchiveEntry();
		} catch (IOException e) {
			throw new RuntimeServiceException(e);
		}
	}

	private void exportClassFile(ModelElement_1_0 classToBeExported){
		try (Sink sink = newSink()){
			new ClassMapper(sink, classToBeExported, markdown, configuration).createArchiveEntry();
		} catch (IOException e) {
			throw new RuntimeServiceException(e);
		}
	}

	private void exportStructureFile(ModelElement_1_0 structureToBeExported){
		try (Sink sink = newSink()){
			new StructureMapper(sink, structureToBeExported, markdown, configuration).createArchiveEntry();
		} catch (IOException e) {
			throw new RuntimeServiceException(e);
		}
	}
	
	private Sink newSink() {
		return new Sink() {

			private String entryName;
  			private Writer writer;
			
			@Override
			public Writer createWriter(String entryName) {
		    	this.entryName = entryName;
		    	buffer.reset();
				return this.writer = new OutputStreamWriter(buffer);
			}
			
			@Override
			public void close() throws IOException {
				this.writer.flush();
				archive.accept(this.entryName, buffer);
				this.writer = null;
				this.entryName = null;
			}

		};
	}
	
	@Override
	public void close() throws Exception {
		buffer.close();
	}
	
}
