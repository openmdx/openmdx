/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1Connection.java,v 1.7 2008/09/10 08:55:21 hburger Exp $
 * Description: Dataprovider WebService connection
 * Revision:    $Revision: 1.7 $
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

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * Dataprovider WebService connection
 * 
 * @author wfro
 */
public class Dataprovider_1Connection
implements Dataprovider_1_1Connection, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3907215944224094009L;
//  -------------------------------------------------------------------------
    /**
     * Creates connection for specified URL.
     */
    public Dataprovider_1Connection(
        String urlString
    ) throws ServiceException {
        this(
            urlString,
            "guest",
            "guest"
        );
    }

    //-------------------------------------------------------------------------
    /**
     * Creates connection for specified URL.
     */
    public Dataprovider_1Connection(
        String urlString,
        String userId,
        String passwd
    ) throws ServiceException {
        try {
            this.target = new URL(urlString);
            this.credentials = "Basic " + Base64.encode((userId + ":" + passwd).getBytes());
        }
        catch (MalformedURLException exception) {
            new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "URL string is malformed",
                new BasicException.Parameter("urlString", urlString)
            );
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Creates connection for specified URL.
     */
    public Dataprovider_1Connection(
        String urlString,
        String userId,
        String passwd,
        boolean acceptGzipEncoding
    ) throws ServiceException {
        this(
            urlString,
            "guest",
            "guest"
        );
        this.acceptGzipEncoding = acceptGzipEncoding;
    }

    //-------------------------------------------------------------------------
    /**
     * The connection marshals and unmarshals all requests to/from a SOAP request.
     * The delegates the unmarshalled request to the delegation. This mode is
     * useful for testing purposes.  
     */
    public Dataprovider_1Connection(
        Dataprovider_1_1Connection delegation
    ) throws ServiceException {
        this.target = delegation;
    }

    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0
    //------------------------------------------------------------------------
    /**
     * Process units of work
     *
     * @param header the service header
     * @param workingUnits a collection of working units
     *
     * @return  a collection of working unit replies
     */
    public UnitOfWorkReply[] process(
        ServiceHeader header,
        UnitOfWorkRequest[] workingUnits
    ) {
        try {
            StringBuilder request = new StringBuilder(4096);
            if(target instanceof URL) {
                if (req == null) {
                    SysLog.detail("Open Connection for SOAP Transport, " + target);
                    req = (HttpURLConnection)((URL)target).openConnection();
                    req.setRequestProperty(
                        "Content-Type",
                        "text/xml; charset=ISO-8859-1"
                    );
                    req.setRequestProperty(
                        "Authorization",
                        this.credentials
                    );
                    if(acceptGzipEncoding) {
                        req.setRequestProperty(
                            "Accept-Encoding",
                            "gzip"
                        );
                    }
                    req.setRequestMethod("POST");
                    req.setDoOutput(true);
                    req.setDoInput(true);
                    req.connect();
                }
            }

            // envelope header
            request.append(
                "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"
            ).append(
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" "
            ).append(
                "xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" "
            ).append(
                "xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\">"
            ).append(
                "<SOAP-ENV:Body>"
            ).append(
                "<m:processInput xmlns:m=\"" + target.toString() + "\">"
            );

            Writer requestWriter = null;
            if(target instanceof URL) {
                BufferedOutputStream out = new BufferedOutputStream(req.getOutputStream());
                requestWriter = new OutputStreamWriter(out,"ISO-8859-1");
            }
            // Dataprovider_1_0Connection
            else {
                requestWriter = new StringWriter();
            }

            // write the request to the underlying writer
            RequestMapper requestMapper = new RequestMapper(requestWriter);
            requestMapper.mapProlog(request.toString());
            requestMapper.mapServiceHeader(header);
            requestMapper.mapUnitOfWorkRequests(workingUnits);
            requestMapper.mapEpilog("</m:processInput></SOAP-ENV:Body></SOAP-ENV:Envelope>");
            requestWriter.flush();

            // send/receive
            Reader soapReplyReader = null;
            // URL
            if(target instanceof URL) {
                InputStream soapReply = null;
                String reqIsEncoded = req.getContentEncoding();
                if ((reqIsEncoded != null) && "gzip".equals(reqIsEncoded)) {
                    try {
                        soapReply = new GZIPInputStream(req.getInputStream());
                    }
                    catch(Exception e) {
                        SysLog.info("Error reading gzipped request. ", new ServiceException(e));
                        SysLog.info("Continuing...");
                    }
                }
                // request is not gzipped
                else {
                    soapReply = req.getInputStream();
                }
                soapReplyReader = new InputStreamReader(soapReply, "ISO-8859-1");
            }
            // Dataprovider_1_0Connection
            else {
                String theRequest = ((StringWriter)requestWriter).toString();
                RequestParser soapRequest = new RequestParser(
                    new StringReader(theRequest)
                );
                UnitOfWorkReply[] replies = ((Dataprovider_1_1Connection)this.target).process(
                    soapRequest.getHeader(),
                    soapRequest.getUnitOfWorkRequests()
                );
                StringWriter writer = new StringWriter();
                ReplyMapper replyMapper = new ReplyMapper(writer);
                replyMapper.mapUnitOfWorkReplies(replies);
                writer.flush();
                soapReplyReader = new StringReader(writer.toString());
            }
            if(SysLog.isTraceOn()) {
                StringBuilder soapReplyAsString = new StringBuilder(4096);
                int c;
                while((c = soapReplyReader.read()) != -1) {
                    soapReplyAsString.append((char)c);
                }
                SysLog.trace("Reply", soapReplyAsString);
                soapReplyReader = new StringReader(
                    soapReplyAsString.toString()
                );
            }
            ReplyParser reply = new ReplyParser(soapReplyReader);
            soapReplyReader.close();
            return reply.getReplies();
        }
        catch (Exception exception) {
            throw new RuntimeServiceException(
                new ServiceException(exception)
            );
        }
    }

    //------------------------------------------------------------------------
    // Implements LifeCycleObject_1_0
    //------------------------------------------------------------------------
    public void remove(
    ) throws ServiceException {
        close();
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_0Connection#close()
     */
    public void close() {
        this.target = null;
    }

    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------
    // target is URL or Dataprovider_1_0Connection
    private Object target;
    private String credentials;
    private boolean acceptGzipEncoding = false;
    private transient HttpURLConnection req = null;
}

//--- End of File -----------------------------------------------------------
