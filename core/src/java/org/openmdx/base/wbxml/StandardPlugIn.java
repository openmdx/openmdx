/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Standard Plug-In
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010-2013, OMEX AG, Switzerland
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
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * The standard plug-in supports <em>normal</em> WBXML string tables
 */
public class StandardPlugIn extends AbstractPlugIn {
    
    /**
     * Constructor 
     * 
     * @param stringEncoding
     */
    protected StandardPlugIn(
        String stringEncoding
    ){
        Charset charset = Charset.forName(stringEncoding);
        this.stringSink = new StringSink(charset.newEncoder());
        this.decoder =  charset.newDecoder();
    }

    /**
     * The re-usable decoder
     */
    private final CharsetDecoder decoder;
    
    /**
     * The string source
     */
    private BasicStringSource stringSource;

    /**
     * The string sink
     */
    private final StringSink stringSink;

    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.AbstractPlugIn#reset()
     */
    @Override
    public void reset() {
        super.reset();
        this.stringSource = null;
        this.stringSink.reset();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.PlugIn#setStringTable(byte[])ic
     */
    @Override
    public void setStringTable(
        ByteBuffer stringTable
    ) throws ServiceException {
        setStringSource(
            new StringSource(
                this.decoder, 
                stringTable
            )
        );
    }

    /**
     * Set the string source
     * 
     * @param stringSource
     */
    protected void setStringSource(
        BasicStringSource stringSource
    ){
        this.stringSource = stringSource;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.PlugIn#getStringTable()
     */
    @Override
    public ByteBuffer getStringTable() {
        return this.stringSink.getStringTable();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.PlugIn#findStringToken(java.lang.String)
     */
    @Override
    public StringToken findStringToken(String value) {
        return this.stringSink == null ? null : this.stringSink.findStringToken(value);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.PlugIn#getStringIndex(java.lang.String)
     */
    @Override
    public StringToken getStringToken(
        String value
    ) {
        return this.stringSink == null ? null : this.stringSink.getStringToken(value);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.xml.wbxml.spi.PlugIn#resolveString(int)
     */
    @Override
    public CharSequence resolveString(
        int index
    ) throws ServiceException {
        CharSequence literal = this.stringSource == null ? null : this.stringSource.resolveString(index);
        if(literal != null) {
            return literal;
        } else throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "No such string table entry",
            new BasicException.Parameter("index", index)
        );
    }
    
}
