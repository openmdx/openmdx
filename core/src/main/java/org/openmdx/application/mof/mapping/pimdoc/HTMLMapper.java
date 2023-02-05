/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: HTML Mapper 
 * Owner: the original authors. 
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

import java.time.Instant;
import java.util.function.Function;
import java.util.stream.Stream;

import org.openmdx.application.mof.mapping.spi.MapperTemplate;
import org.openmdx.base.Version;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;

/**
 * HTML Mapper
 */
abstract class HTMLMapper extends MapperTemplate {

	private HTMLMapper(Sink sink, Model_1_0 model, ModelElement_1_0 element, boolean markdown, PIMDocConfiguration configuration) {
		super(
			sink.createWriter(getEntryName(element)), 
			model, 
			markdown ? configuration.getMarkdownRendererFactory().instantiate() : Function.identity()
		);
		this.configuration = configuration;
		this.element = element;
	}

	protected HTMLMapper(Sink sink, Model_1_0 model, boolean markdown, PIMDocConfiguration configuration) {
		this(sink, model, null, markdown, configuration);
	}

	protected HTMLMapper(Sink sink, ModelElement_1_0 element, boolean markdown, PIMDocConfiguration configuration) {
		this(sink, element.getModel(), element, markdown, configuration);
	}

	protected final ModelElement_1_0 element;
	
    protected final PIMDocConfiguration configuration;
    
    static String FRAME_NAME = "uml-element";
    
    protected String getMapperId() {
        return getClass().getSimpleName() + " " + Version.getImplementationVersion();
    }
	
    void createArchiveEntry() {
    	fileHeader();
        htmlPage();
    }

	protected void htmlPage() {
		printLine("<html>");
        htmlHead();
        htmlBody();
        printLine("</html>");
	}
    
    protected void fileHeader(
    ) {
        printLine("<!DOCTYPE html>");
        fileGenerated();
    }

	protected void fileGenerated() {
		printLine("<!--");
        printLine(" !");
        printLine(" ! Generated by ", getMapperId());
        printLine(" ! Generated at ", Instant.now().toString());
        printLine(" !");
        printLine(" ! GENERATED - DO NOT CHANGE MANUALLY");
        printLine(" !");
        printLine(" !-->");
	}

    protected void fileFooter() {
        printLine("</html>");
    }

    protected void htmlHead() {
        printLine("<head>");
        printLine("\t<meta charset=\"utf-8\">");
        printLine("\t<link rel=\"stylesheet\" href=\"", getFileURL(MagicFile.STYLE_SHEET), "\" />");
        htmlTitle(getTitle());
        printLine("</head>");
    }

	protected void htmlTitle(final String title) {
		if(title != null) {
	        printLine("\t<title>", title, "</title>");
        }
	}
    	
	/**
	 * Provide the entry name (using HTML entries)
	 * 
	 * @param element the model element used to derive the entry name
	 * 
	 * @return the entry name
	 */
    private static String getEntryName(ModelElement_1_0 element){
    	try {
	    	if(element == null) {
	    		return MagicFile.INDEX.getFileName();
	    	}
	    	final StringBuilder entryName = new StringBuilder(
	    		element.getModel().toJavaPackageName(element, null).replace('.', '/')
	    	);
			entryName.append('/').append(element.getName()).append(".html");
	    	return entryName.toString();
		} catch (ServiceException exception) {
			throw new RuntimeServiceException(exception);
    	}
    }
	
    private String getBaseURL() {
    	if(this.element == null) {
    		return "";
    	}
    	StringBuilder baseDir = new StringBuilder();
    	for(long i = element.getQualifiedName().chars().filter(HTMLMapper::isColon).count(); i > 0L; i--) {
    		baseDir.append("../");
    	}
    	return baseDir.toString();
    }

    protected String getFileURL(
    	MagicFile magicFile
    ) {
    	return getBaseURL() + magicFile.getFileName();
    }

    protected String getHref(
    	ModelElement_1_0 element
    ){
    	if(element.isPackageType() || element.isClassType() || element.isStructureType()){
        	return getBaseURL() + getEntryName(element);
    	} else try {
    		final ModelElement_1_0 container = this.model.getElement(element.getContainer());
        	return getBaseURL() + getEntryName(container) + "#" + element.getName();
    	} catch (ServiceException e) {
    		throw new RuntimeServiceException(e);
    	}
    }

    protected static String getDisplayName(
    	ModelElement_1_0 element
    ) {
    	String qualifiedName = element.getQualifiedName();
    	return (element.isPackageType() ? qualifiedName.substring(0, qualifiedName.lastIndexOf(':')) : qualifiedName).replace(":", "::");
    }
    
    protected void annotation(
    	ModelElement_1_0 element
    ) {
    	final String annotation  = (String)element.objGetValue("annotation");
		if(annotation != null && !annotation.isEmpty()) {
			printLine("<div class=\"uml-comment\">");
			print(renderAnnotation(annotation));		
			printLine("</div>");
		}
    }

    private static boolean isColon(int c) {
    	return c == ':';
    }
    
	protected Stream<ModelElement_1_0> streamElements(){
		return this.model.getContent().stream();
	}
	
    protected abstract String getTitle();
    
    protected abstract void htmlBody();
    
}