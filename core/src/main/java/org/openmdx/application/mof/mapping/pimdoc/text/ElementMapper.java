/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Element Mapper 
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
package org.openmdx.application.mof.mapping.pimdoc.text;

import org.openmdx.application.mof.mapping.pimdoc.PIMDocConfiguration;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.Sink;
import org.openmdx.base.mof.cci.ModelElement_1_0;

/**
 * Element Mapper 
 */
abstract class ElementMapper extends HTMLMapper {

	/**
     * Constructor 
     */
    protected ElementMapper(
    	String elementId,
        Sink sink, 
        ModelElement_1_0 element,
        boolean markdown, 
        PIMDocConfiguration configuration
    ){
		super(sink, element, markdown, configuration);
		this.elementKind = elementId;		
		this.element = element;
    }    
    
    private final String elementKind;
	protected final ModelElement_1_0 element;
	
	@Override
	protected void htmlBody() {
		printLine("<body class=\"uml-element ", getImageClass(), "\">");
		columnHead();
		columnBody();
		printLine("</body>");
   }

	protected String getImageClass() {
		return "uml-" + elementKind.toLowerCase().replace(' ', '-');
	}
	
	protected void columnHead() {
		printLine("\t<div class=\"column-head\">");
		if(isAbstract()) {
			printLine("\t\t<h2 class=\"uml-abstract\" >", getTitle(), "</h2>");
		} else {
			printLine("\t\t<h2>", getTitle(), "</h2>");
		}
		printLine("\t</div>");
	}
	
	@Override
    protected String getBaseURL() {
    	StringBuilder baseDir = new StringBuilder();
    	for(long i = element.getQualifiedName().chars().filter(ElementMapper::isColon).count(); i > 0L; i--) {
    		baseDir.append("../");
    	}
    	return baseDir.toString();
    }
	
    static boolean isColon(int c) {
    	return c == ':';
    }
    
	@Override
	protected String getTitle() {
		return this.elementKind + " " + getDisplayName();
	}
	
	protected String getDisplayName() {
		return getDisplayName(element); 
	}
	
	protected boolean isAbstract() {
		try {
			return Boolean.TRUE.equals(element.isAbstract());
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
	}

	protected abstract void columnBody();
	
}
