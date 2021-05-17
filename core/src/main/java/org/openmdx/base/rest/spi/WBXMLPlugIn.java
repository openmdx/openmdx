/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: REST Plug-In
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
 * All rights reserved.
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
package org.openmdx.base.rest.spi;

import org.openmdx.base.wbxml.CodeToken;
import org.openmdx.base.wbxml.DynamicPlugIn;

/**
 * REST Plug-In
 */
public class WBXMLPlugIn extends DynamicPlugIn {
    
    /**
     * Constructor 
     */
    public WBXMLPlugIn() {
        super(page0);
    }

    /**
     * Page 0
     */
    private static final Page page0 = preparePage0(true);
    
	/**
	 * Prepare page 0
	 * 
	 * @param lenient if <code>true</code> then collisions are logged as warnings,
	 * otherwise they lead to exceptions.
	 */
	static Page preparePage0(
		boolean lenient
	) {
		final Page page0 = newPage();
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "_item", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "description", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "features", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "groups", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "org.openmdx.base.Void", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "org.openmdx.kernel.Condition", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "org.openmdx.kernel.FeatureOrder", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "org.openmdx.kernel.Message", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "org.openmdx.kernel.Object", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "org.openmdx.kernel.Query", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "org.openmdx.kernel.QueryExtension", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "org.openmdx.kernel.QueryFilter", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "org.openmdx.kernel.ResultSet", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "org.openmdx.kernel.UnitOfWork", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "parameter", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "parameters", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "position", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "queryFilter", lenient);		
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "queryType", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "query", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "refresh", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "size", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "stackTraceElements", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME, "declaringClass", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME, "exceptionClass", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME, "exceptionCode", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME, "exceptionDomain", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME, "exceptionTime", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME, "fileName", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME, "hasMore", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME, "href", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME, "id", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME, "index", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME, "lineNumber", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME, "methodName", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME, "total", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME, "more", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME, "type", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME, "version", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME_WITH_VALUE_PREFIX, "type=org:w3c:string", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME_WITH_VALUE_PREFIX, "type=org:w3c:integer", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME_WITH_VALUE_PREFIX, "type=org:w3c:dateTime", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME_WITH_VALUE_PREFIX, "type=org:w3c:date", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME_WITH_VALUE_PREFIX, "type=org:w3c:duration", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME_WITH_VALUE_PREFIX, "type=org:w3c:decimal", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.ATTRIBUTE_NAME_WITH_VALUE_PREFIX, "type=org:w3c:boolean", lenient);
		
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "body", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "booleanParam", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "dateTimeParam", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "decimalParam", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "feature", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "featureName", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "orderSpecifier", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "quantifier", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "resourceIdentifier", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "sortOrder", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "stringParam", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "transientObjectId", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "value", lenient);
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "version", lenient);
		
		/**
		 * Collisions exclude some entries:
		 * 
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "clause", lenient); // is excluded by queryFilter
	    DynamicPlugIn.addTo(page0, CodeSpace.TAG, "condition", lenient); // is excluded by queryFilter
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "dateParam", lenient); // is excluded by stackTraceElements
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "integerParam", lenient); // is excluded by transient object id
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "extension", lenient); // is excluded by body
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "lock", lenient); // is excluded by position
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "org.openmdx.kernel.Exception", lenient); // is excluded by orderSpecifier
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "fetchGroupName", lenient); // is excluded by quantifier
		DynamicPlugIn.addTo(page0, CodeSpace.TAG, "type", lenient); // is excluded by _item
		 */
        return page0;
	}

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.base.xml.wbxml.spi.AbstractPlugIn#getAttributeStartToken(
     * boolean, java.lang.String, java.lang.String)
     */
    @Override
    public CodeToken findAttributeStartToken(
        boolean force,
        String namespaceURI,
        String elementName, String attributeName, String value
    ) {
        // TODO enable value prefix encoding
        return super.getAttributeNameToken(namespaceURI, elementName, attributeName); 
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.AbstractPlugIn#getDefaultDocumentPublicIdentifier()
     */
    @Override
    public String getDefaultDocumentPublicIdentifier(
        String defautNamespace
    ) {
        return "-//openMDX//REST 2.0//EN";
    }

}
