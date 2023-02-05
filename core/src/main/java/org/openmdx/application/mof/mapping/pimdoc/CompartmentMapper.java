/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Compartment Mapper
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.application.mof.mapping.pimdoc;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Stream;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;

/**
 * Compartment Mapper
 */
abstract class CompartmentMapper {

	protected CompartmentMapper(
		PrintWriter pw, 
		ModelElement_1_0 element, 
		Function<ModelElement_1_0, String> hrefMapper
	) {
		this.pw = pw;
		this.element = element;
		this.hrefMapper = hrefMapper;
	}

	protected final ModelElement_1_0 element;
	protected static final Comparator<ModelElement_1_0> ELEMENT_NAME_COMPARATOR = new ElementNameComparator();
	private final PrintWriter pw;
	private final Function<ModelElement_1_0, String> hrefMapper;

	protected String getHref(ModelElement_1_0 element) {
		return hrefMapper.apply(element);
	}

	protected String getDisplayName(ModelElement_1_0 element) {
		return HTMLMapper.getDisplayName(element);
	}
	
	protected Model_1_0 getModel() {
		return this.element.getModel();
	}
	
	protected ModelElement_1_0 getElement(Object objectId) {
		try {
			return getModel().getElement(objectId);
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}

	protected Stream<ModelElement_1_0> containedElements(){
		return getModel().getContent().stream().filter(new NamespaceFilter(element));
	}
	
	protected ModelElement_1_0 getType(ModelElement_1_0 element) {
		try {
			return element.getModel().getElement(element.getType());
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}

	protected void print(CharSequence text) {
		this.pw.print(text);
	}
	
	protected void printLine(CharSequence text) {
		this.pw.println(text);
	}

	protected void printLine(CharSequence... text) {
		for(CharSequence segment : text) {
			this.pw.print(segment);
		}
		this.pw.println();
	}
	
	protected void newLine() {
		this.pw.println();
	}
		
	protected abstract void compartment();
	
}