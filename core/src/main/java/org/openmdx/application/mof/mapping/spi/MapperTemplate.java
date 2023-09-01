/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Mapper Template 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.application.mof.mapping.spi;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.function.Function;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;

/**
 * Mapper Template
 */
public abstract class MapperTemplate {
    
    protected MapperTemplate(
        Writer writer,
        Model_1_0 model, 
        Function<String, String> annotationRenderer
    ){
        this.pw = new PrintWriter(writer);
        this.model = model;
        this.annotationRenderer = annotationRenderer;
    }
        
    protected final PrintWriter pw;
    protected final Model_1_0 model;
    protected final Function<String, String> annotationRenderer;

    /**
     * Renders the annotation 
     * 
     * @param annotation the provided annotation
     * 
     * @return the rendered annotation
     */
	protected String renderAnnotation(final String annotation) {
		return annotationRenderer.apply(annotation);
	}

	protected void print(CharSequence text) {
		this.pw.print(text);
	}
	
	protected void printLine(CharSequence... text) {
		for(CharSequence segment : text) {
			this.pw.print(segment);
		}
		this.pw.println();
	}
	
	protected void printLine(CharSequence text) {
		this.pw.println(text);
	}

	protected void newLine() {
		this.pw.println();
	}

	protected void flush() {
		this.pw.flush();
	}

	protected ModelElement_1_0 getElement(String qualifiedName) {
		try {
			return this.model.getElement(qualifiedName);
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}

	protected ModelElement_1_0 getContainer(ModelElement_1_0 element) {
		try {
			return this.model.getElement(element.getContainer());
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}
	
}
