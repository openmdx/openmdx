/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractParser.java,v 1.16 2008/09/10 08:55:21 hburger Exp $
 * Description: AbstractParser.java
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:21 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.transport.webservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * @author wfro
 *
 * Abstract class for parsing SOAP request and replies.
 * NOTE: this class does not use xerces to ensure .NET portability
 */
@SuppressWarnings("unchecked")
public abstract class AbstractParser {

    /**
     * @param reader    the Reader to be parsed as Object to be compatible
     *                  with .NET implementation
     * @throws ServiceException
     */
    void parse(
        Object reader
    ) throws ServiceException {
        try {
            if (! (reader instanceof Reader)) {
                ServiceException se = new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.PARSE_FAILURE,
                    "Could not read the Request"
                );
                SysLog.info("Error reading request. ", se);
                SysLog.info("Continuing...");
                throw se;
            }
            BufferedReader bufferedReader =
                new BufferedReader((Reader)reader);
            int c = bufferedReader.read();
            StringBuilder chars = new StringBuilder(4096);
            StringBuilder rawname = new StringBuilder(512);
            while(c != -1) {
                if(c == '<') {
                    c = bufferedReader.read();
                    // XML Header
                    if(c == '?') {
                        while((c != -1) && (c != '>')) {
                            c = bufferedReader.read();
                        }
                    }
                    // begin tag
                    else if(c != '/') {
                        rawname.setLength(0);
                        boolean append = true;
                        while((c != -1) && (c != '>')) {
                            append &= c != ' ';
                            if(append) {
                                rawname.append((char)c);
                            }
                            c = bufferedReader.read();
                        }
                        this.startElement(
                            rawname.toString()
                        );
                    }
                    // end tag
                    else {
                        this.characters(
                            chars.toString().toCharArray(),
                            0,
                            chars.length()
                        );
                        chars.setLength(0);
                        c = bufferedReader.read();
                        rawname.setLength(0);
                        while((c != -1) && (c != '>')) {
                            rawname.append((char)c);
                            c = bufferedReader.read();
                        }
                        this.endElement(
                            rawname.toString()
                        );
                    }
                }
                else {
                    chars.append((char)c);
                }
                c = bufferedReader.read();
            }
        }
        catch(IOException e) {
            throw new ServiceException(e);
        }
    }

    //-------------------------------------------------------------------------   
    abstract void characters(
        char[] ch,
        int offset,
        int length
    ) throws ServiceException;

    //-------------------------------------------------------------------------   
    abstract void startElement(
        String rawname
    ) throws ServiceException;

    //-------------------------------------------------------------------------   
    abstract void endElement(
        String rawname
    ) throws ServiceException;

}

//--- End of File -----------------------------------------------------------
