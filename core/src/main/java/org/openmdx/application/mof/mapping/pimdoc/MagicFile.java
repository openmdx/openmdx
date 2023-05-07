/*
 * ==================================================================== 
 * Project: openMDX, http://www.openmdx.org
 * Description: Magic Files
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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public enum MagicFile {

	/**
	 * Logo & Welcome Page
	 */
	WELCOME_PAGE("welcome-page", "html", "png", null),

	/**
	 * PIMDoc style sheets
	 */
	STYLE_SHEET("style-sheet", "css", "dots", null),

	/**
	 * Package Group Files
	 */
	PACKAGE_CLUSTER("+package-cluster", "html", "svg", "dot"),
	
	/**
	 * The table of content
	 */
	TABLE_OF_CONTENT("index", "html", null, null),
	
	/**
	 * The UML symbol is used for the diagram link in the table of content
	 */	
	UML_SYMBOL("uml-symbol", null, "svg", null);
		
	private MagicFile(
		String baseName,
		String textExtension,
		String imageExtension,
		String sourceExtension
	) {
		setFileName(baseName, Type.TEXT, textExtension);
		setFileName(baseName, Type.IMAGE, imageExtension);
		setFileName(baseName, Type.SOURCE, sourceExtension);
	}
	
	private void setFileName(String baseName, Type type, String extension) {
		if(extension != null) {
			this.fileName.put(type, baseName + '.' + extension);
		}
	}
	
	private final Map<Type,String> fileName = new HashMap<>();
	
	public String getFileName(Type type) {
		return this.fileName.get(type);
	}
	
	public URL getDefault(Type type) {
		return getClass().getResource("default-" + getFileName(type));
	}

	public enum Type {
		TEXT, IMAGE, SOURCE
	}
	
}
