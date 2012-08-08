/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: WBXMLPlugIn.java,v 1.2 2010/04/08 11:35:33 hburger Exp $
 * Description: REST Plug-In
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/08 11:35:33 $
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
    private static final Page page0 = newPage();
    
    static {
        addTo(page0, CodeSpace.TAG, "org.openmdx.kernel.UnitOfWork");
        addTo(page0, CodeSpace.TAG, "org.openmdx.kernel.Object");
        addTo(page0, CodeSpace.TAG, "org.openmdx.kernel.Query");
        addTo(page0, CodeSpace.TAG, "org.openmdx.kernel.ResultSet");
        addTo(page0, CodeSpace.TAG, "org.openmdx.kernel.Exception");
        addTo(page0, CodeSpace.TAG, "_item");
        addTo(page0, CodeSpace.TAG, "path");
        addTo(page0, CodeSpace.TAG, "queryType");
        addTo(page0, CodeSpace.TAG, "query");
        addTo(page0, CodeSpace.TAG, "position");
        addTo(page0, CodeSpace.TAG, "size");
        addTo(page0, CodeSpace.TAG, "groups");
        addTo(page0, CodeSpace.TAG, "group");
        addTo(page0, CodeSpace.TAG, "parameters");
        addTo(page0, CodeSpace.TAG, "element");
        addTo(page0, CodeSpace.TAG, "description");
        addTo(page0, CodeSpace.TAG, "parameter");
        addTo(page0, CodeSpace.TAG, "stackTraceElements");
        addTo(page0, CodeSpace.ATTRIBUTE_NAME, "id");
        addTo(page0, CodeSpace.ATTRIBUTE_NAME, "href");
        addTo(page0, CodeSpace.ATTRIBUTE_NAME, "version");
        addTo(page0, CodeSpace.ATTRIBUTE_NAME, "index");
        addTo(page0, CodeSpace.ATTRIBUTE_NAME, "hasMore");
        addTo(page0, CodeSpace.ATTRIBUTE_NAME, "total");
        addTo(page0, CodeSpace.ATTRIBUTE_NAME, "exceptionDomain");
        addTo(page0, CodeSpace.ATTRIBUTE_NAME, "exceptionCode");
        addTo(page0, CodeSpace.ATTRIBUTE_NAME, "exceptionTime");
        addTo(page0, CodeSpace.ATTRIBUTE_NAME, "exceptionClass");
        addTo(page0, CodeSpace.ATTRIBUTE_NAME, "declaringClass");
        addTo(page0, CodeSpace.ATTRIBUTE_NAME, "methodName");
        addTo(page0, CodeSpace.ATTRIBUTE_NAME, "lineNumber");
        addTo(page0, CodeSpace.ATTRIBUTE_NAME, "fileName");
        addTo(page0, CodeSpace.ATTRIBUTE_NAME, "more");
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
    public String getDefaultDocumentPublicIdentifier(String defautNamespace
    ) {
        return "-//openMDX//REST 2.0//EN";
    }

}
