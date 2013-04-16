/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Abstract Plug-In
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
package org.openmdx.base.wbxml;

import java.nio.ByteBuffer;

import javax.xml.namespace.NamespaceContext;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.kernel.exception.BasicException;


/**
 * Abstract Extension Handler
 */
public abstract class AbstractPlugIn implements PlugIn {
    
    /**
     * Constructor 
     */
    protected AbstractPlugIn(
    ){
        super();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.PlugIn#reset()
     */
    @Override
    public void reset() {
        // nothing to do
    }

    /**
     * Retrieves the default public identifier
     * 
     * @param defaultNamespaceURI the default name space URI, which may be <code>null</code> 
     *
     * @return the default public identifier, which may be <code>null</code> 
     */
    @Override
    public String getDefaultDocumentPublicIdentifier(
        String defaultNamespaceURI
    ){
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.PlugIn#isStringTableReadyAtStartOfDocument()
     */
    @Override
    public boolean isStringTableReadyAtStartOfDocument() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.PlugIn#setStringTable(byte[])ic
     */
    @Override
    public void setStringTable(
        ByteBuffer stringTable
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "String tables are not supported by this plug-in"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.PlugIn#getStringTable()
     */
    @Override
    public ByteBuffer getStringTable(
    ) throws ServiceException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.PlugIn#findStringToken(java.lang.String)
     */
    @Override
    public StringToken findStringToken(String value) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.PlugIn#getStringToken(java.lang.String)
     */
    @Override
    public StringToken getStringToken(
        String value
    ) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.PlugIn#getTagToken(java.lang.String)
     */
    @Override
    public CodeToken getTagToken(
        String namespaceURI, String value
    ) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.PlugIn#getAttributeNameToken(java.lang.String)
     */
    @Override
    public CodeToken getAttributeNameToken(
        String namespaceURI, 
        String elementName, 
        String attributeName
    ) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.PlugIn#getAttributeStartToken(boolean, java.lang.String, java.lang.String)
     */
    @Override
    public CodeToken findAttributeStartToken(
        boolean force, 
        String namespaceURI, 
        String elementName, String attributeName, String value
    ) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.PlugIn#getAttributeValueStartToken(boolean, java.lang.String)
     */
    @Override
    public CodeToken findAttributeValueToken(
        boolean force, 
        String namespaceURI, String value
    ) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.PlugIn#getAttributeValueToken(java.lang.String)
     */
    @Override
    public CodeToken getAttributeValueToken(
        String namespaceURI, String value
    ) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.PlugIn#resolveString(int)
     */
    @Override
    public CharSequence resolveString(
        int index
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "String tables are not supported by this plug-in",
            new BasicException.Parameter("index", index)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.PlugIn#resolveAttributeStart(int, int)
     */
    @Override
    public CodeResolution resolveAttributeStart(
        int page, 
        int id
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "No such attribute start entry",
            new BasicException.Parameter("page", page),
            new BasicException.Parameter("id", id)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.PlugIn#resolveAttributeValue(int, int)
     */
    @Override
    public String resolveAttributeValue(
        int page, 
        int id
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "No such attribute value entry",
            new BasicException.Parameter("page", page),
            new BasicException.Parameter("id", id)
        );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.PlugIn#resolveTag(int, int)
     */
    @Override
    public Object resolveTag(
        int page, 
        int id
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "No such tag entry",
            new BasicException.Parameter("page", page),
            new BasicException.Parameter("id", id)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.ExtensionHandler#ext0()
     */
    @Override
    public void ext0(
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "This extension is not supported",
            new BasicException.Parameter("token", GlobalTokens.EXT_0)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.ExtensionHandler#ext1()
     */
    @Override
    public void ext1(
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "This extension is not supported",
            new BasicException.Parameter("token", GlobalTokens.EXT_1)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.ExtensionHandler#ext2()
     */
    @Override
    public void ext2(
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "This extension is not supported",
            new BasicException.Parameter("token", GlobalTokens.EXT_2)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.ExtensionHandler#extI0(java.lang.String)
     */
    @Override
    public void ext0(
        String argument
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "This extension is not supported",
            new BasicException.Parameter("token", GlobalTokens.EXT_I_0),
            new BasicException.Parameter("argument", argument)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.ExtensionHandler#extI1(java.lang.String)
     */
    @Override
    public void ext1(
        String argument
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "This extension is not supported",
            new BasicException.Parameter("token", GlobalTokens.EXT_I_1),
            new BasicException.Parameter("argument", argument)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.ExtensionHandler#extI2(java.lang.String)
     */
    @Override
    public void ext2(
        String argument
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "This extension is not supported",
            new BasicException.Parameter("token", GlobalTokens.EXT_I_2),
            new BasicException.Parameter("argument", argument)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.ExtensionHandler#extT0(int)
     */
    @Override
    public void ext0(
        int argument
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "This extension is not supported",
            new BasicException.Parameter("token", GlobalTokens.EXT_T_0),
            new BasicException.Parameter("argument", argument)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.ExtensionHandler#extT1(int)
     */
    @Override
    public void ext1(
        int argument
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "This extension is not supported",
            new BasicException.Parameter("token", GlobalTokens.EXT_T_1),
            new BasicException.Parameter("argument", argument)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.ExtensionHandler#extT2(int)
     */
    @Override
    public void ext2(
        int argument
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "This extension is not supported",
            new BasicException.Parameter("token", GlobalTokens.EXT_T_2),
            new BasicException.Parameter("argument", argument)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.ExtensionHandler#opaque(byte[])
     */
    @Override
    public void opaque(
        byte[] data
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "This extension is not supported",
            new BasicException.Parameter("token", GlobalTokens.OPAQUE),
            new BasicException.Parameter("argument", data == null ? null : Base64.encode(data))
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.PlugIn#getNamespaceContext()
     */
    @Override
    public NamespaceContext getNamespaceContext() {
        return null;
    }
        
}
