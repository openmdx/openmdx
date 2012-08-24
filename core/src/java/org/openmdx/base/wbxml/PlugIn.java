/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: WAP XML Plug-In
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD licensev as listed below.
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
package org.openmdx.base.wbxml;

import java.nio.ByteBuffer;

import javax.xml.namespace.NamespaceContext;

import org.openmdx.base.exception.ServiceException;


/**
 * A WAP XML plug-in implements this interface
 */
public interface PlugIn {

    /**
     * Call this method in order to re-use the plug-in.
     */
    void reset();

    /**
     * Retrieves the default public identifier
     * 
     * @param defaultNamespaceURI 
     * 
     * @return the default public identifier, which may be <code>null</code> 
     */
    String getDefaultDocumentPublicIdentifier(
        String defaultNamespaceURI
    );
    
    /**
     * Tells whether the (initial or final) WBXL string table is ready during start of document.
     * 
     * @return <code>false</code> when the string table is not ready until the end of document.
     */
    boolean isStringTableReadyAtStartOfDocument(
    );
    
    /**
     * Sets the string table
     * 
     * @param stringTable
     * 
     * @throws ServiceException if the plug-in is inapt to handle the string table
     */
    void setStringTable(
        ByteBuffer stringTable
    ) throws ServiceException;

    /**
     * Retrieve the string table
     * 
     * @return the string table; or <code>null</code> if the string table will 
     * not be ready until later
     */
    ByteBuffer getStringTable(
    ) throws ServiceException;

    /**
     * Retrieve the token for the given value
     * 
     * @param value the value to be tokenized
     * 
     * @return the requested token, or <code>null</code> if no such token is available 
     */
    StringToken getStringToken(
        String value
    );
    
    /**
     * Retrieve the token for the given value
     * 
     * @param namespaceURI 
     * @param value the value to be tokenized
     * 
     * @return the requested token, or <code>null</code> if no such token is available 
     */
    CodeToken getTagToken(
        String namespaceURI, 
        String value
    );

    /**
     * Retrieve the token for the given value
     * 
     * @param namespaceURI 
     * @param elementName, may be <code>null</code> in case of namespace global scope 
     * @param attributeName the attribute name
     * 
     * @return the requested token, or <code>null</code> if no such token is available 
     */
    CodeToken getAttributeNameToken(
        String namespaceURI, 
        String elementName, 
        String attributeName
    );

    /**
     * Retrieve the token for the given value
     * 
     * @param namespaceURI 
     * @param value the attribute value
     * 
     * @return the requested token, or <code>null</code> if no such token is available 
     */
    CodeToken getAttributeValueToken(
        String namespaceURI, 
        String value
    );

    /**
     * Retrieve the token for the given value
     * 
     * @param set if <code>true</code> the complete name/value pair is set, 
     * otherwise the best match is returned, which rarely is <code>null</code> 
     * @param namespaceURI 
     * @param elementName, may be <code>null</code> in case of namespace global scope 
     * @param attributeName the attribute name
     * @param value the attribute value
     * 
     * @return the requested token, or <code>null</code> if no such token is available 
     */
    CodeToken findAttributeStartToken(
        boolean set,
        String namespaceURI,
        String elementName, 
        String attributeName, 
        String value
    );

     /**
     * Retrieve the token for the given value
     * 
     * @param set if <code>true</code> the complete value is set, 
     * otherwise the best match is returned, which may be <code>null</code>
     * @param namespaceURI 
     * @param value the attribute value
     * @return the requested token, or <code>null</code> if no such token is available 
     */
    CodeToken findAttributeValueToken(
        boolean set,
        String namespaceURI, 
        String value
    );

    /**
     * String table entry lookup
     * 
     * @param index the index into the string table
     * 
     * @return the corresponding value
     * 
     * @throws ServiceException in case of resolution failure
     */
    CharSequence resolveString(
        int index
    ) throws ServiceException;
    
    /**
     * Tag Lookup
     * 
     * @param page
     * @param code
     * 
     * @return the appropriate tag value
     * 
     * @throws ServiceException in case of resolution failure
     */
    Object resolveTag(
        int page,
        int code
    ) throws ServiceException;
    
    /**
     * Attribute Lookup
     * 
     * @param page
     * @param code
     * 
     * @return the appropriate attribute start value
     * 
     * @throws ServiceException in case of resolution failure
     */
    CodeResolution resolveAttributeStart(
        int page,
        int code
    ) throws ServiceException;

    /**
     * Attribute Lookup
     * 
     * @param page
     * @param code
     * 
     * @return the appropriate attribute start value
     * 
     * @throws ServiceException in case of resolution failure
     */
    String resolveAttributeValue(
        int page,
        int code
    ) throws ServiceException;

    /**
     * Called when {@link GlobalTokens#EXT_I_0 EXT_I_0} is detected in the document
     * 
     * @param argument the argument
     * 
     * @throws ServiceException in case of failure
     */
    void ext0 (
        String argument
    ) throws ServiceException;

    /**
     * Called when {@link GlobalTokens#EXT_I_1 EXT_I_1} is detected in the document
     * 
     * @param argument the argument
     * 
     * @throws ServiceException in case of failure
     */
    void ext1 (
        String argument
    ) throws ServiceException;

    /**
     * Called when {@link GlobalTokens#EXT_I_2 EXT_I_02 is detected in the document
     * 
     * @param argument the argument
     * 
     * @throws ServiceException in case of failure
     */
    void ext2 (
        String argument
    ) throws ServiceException;

    /**
     * Called when {@link GlobalTokens#EXT_T_0 EXT_T_0} is detected in the document
     * 
     * @param argument the argument
     * 
     * @throws ServiceException in case of failure
     */
    void ext0 (
        int argument
    ) throws ServiceException;

    /**
     * Called when {@link GlobalTokens#EXT_T_1 EXT_T_1} is detected in the document
     * 
     * @param argument the argument
     * 
     * @throws ServiceException in case of failure
     */
    void ext1 (
        int argument
    ) throws ServiceException;
    
    /**
     * Called when {@link GlobalTokens#EXT_T_2 EXT_T_2} is detected in the document
     * 
     * @param argument the argument
     * 
     * @throws ServiceException in case of failure
     */
    void ext2(
        int argument
    ) throws ServiceException;

    /**
     * Called when {@link GlobalTokens#EXT_0 EXT_0} is detected in the document
     * 
     * @throws ServiceException in case of failure
     */
    void ext0 (
    ) throws ServiceException;

    /**
     * Called when {@link GlobalTokens#EXT_1 EXT_1} is detected in the document
     * 
     * @throws ServiceException in case of failure
     */
    void ext1 (
    ) throws ServiceException;
    
    /**
     * Called when {@link GlobalTokens#EXT_2 EXT_2} is detected in the document
     * 
     * @throws ServiceException in case of failure
     */
    void ext2 (
    ) throws ServiceException;
    
    /**
     * Called when the {@link GlobalTokens#OPAQUE OPAQUE} token is detected in the document
     * 
     * @param data
     * 
     * @throws SAXException
     */
    void opaque (
        byte[] data
    ) throws ServiceException;

    /**
     * Retrieve the namespace context
     * 
     * @return the namespace context; or <code>null</code> if the standard one shall be used
     */
    NamespaceContext getNamespaceContext();
    
}    
