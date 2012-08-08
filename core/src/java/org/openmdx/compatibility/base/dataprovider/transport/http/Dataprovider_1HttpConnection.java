/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1HttpConnection.java,v 1.7 2008/11/27 16:46:56 hburger Exp $
 * Description: Lightweight Container's Dataprovider Connection  
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/27 16:46:56 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.transport.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.kernel.exception.BasicException;

/**
 * The Lightweight Container's Dataprovider Connection
 */
@SuppressWarnings("unchecked")
public class Dataprovider_1HttpConnection 
implements Dataprovider_1_1Connection
{

    /**
     * Constructor
     * 
     * @param url
     * @param requestProperties additional request properties
     */
    Dataprovider_1HttpConnection(
        URL url,
        Map requestProperties
    ){
        this.url = url;
        this.requestProperties = new HashMap(
            requestProperties
        );
        this.requestProperties.put(
            "Content-Type", 
            "application/octet-stream"
        );
    }

    //-----------------------------------------------------------------------
    /**
     * @deprecated
     */
    public void remove(
    ) throws ServiceException {
        close();		
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection#close()
     */
    public void close(
    ) {
        this.url = null;
        this.requestProperties = null;
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0#process(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest[])
     */
    public UnitOfWorkReply[] process(
        ServiceHeader header, 
        UnitOfWorkRequest... workingUnits
    ) {
        try {
            HttpURLConnection connection = getConnection(
                this.url,
                this.requestProperties
            );
            ObjectInputStream replyStream = null;
            try {
                ObjectOutputStream requestStream = new ObjectOutputStream(connection.getOutputStream());
                requestStream.writeObject(header);
                requestStream.writeObject(workingUnits);
                int responseCode = connection.getResponseCode(); 
                if(
                        responseCode != HttpURLConnection.HTTP_OK &&
                        StatusCodes.isRetriable(responseCode)
                ) {
                    close(connection.getInputStream());
                    close(connection.getErrorStream());
                    connection = getConnection(
                        this.url,
                        this.requestProperties
                    );
                    requestStream = new ObjectOutputStream(connection.getOutputStream());
                    requestStream.writeObject(header);
                    requestStream.writeObject(workingUnits);
                    responseCode = connection.getResponseCode(); 
                }
                if(responseCode != HttpURLConnection.HTTP_OK) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.COMMUNICATION_FAILURE,
                    "Unexpected response",
                    new BasicException.Parameter("response.code", responseCode),
                    new BasicException.Parameter("response.message", connection.getResponseMessage())
                );              
                replyStream = new ObjectInputStream(connection.getInputStream());	
                return  (UnitOfWorkReply[]) replyStream.readObject();
            } catch (ClassCastException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "Probably unexpected reply class",
                    new BasicException.Parameter("url", url)
                );
            } catch (IOException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.COMMUNICATION_FAILURE,
                    "Communication failure",
                    new BasicException.Parameter("url", url)
                );
            } catch (ClassNotFoundException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "Unmarshalling failure",
                    new BasicException.Parameter("url", url)
                );
            } finally {
                close(connection.getErrorStream());
                close(replyStream);
            }
        } catch (ServiceException exception) {
            UnitOfWorkReply[] reply = new UnitOfWorkReply[workingUnits.length];
            Arrays.fill(
                reply,
                new UnitOfWorkReply(exception)
            );
            return reply;
        }
    }

    /**
     * Get a connection
     * 
     * @param url
     * @param thereuqest properties
     * 
     * @return the connection set up for a conversational request.
     * 
     * @throws ServiceException
     */
    protected static HttpURLConnection getConnection(
        URL url,
        Map requestProperties
    ) throws ServiceException{
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            for(
                    Iterator i = requestProperties.entrySet().iterator();
                    i.hasNext();
            ){
                Map.Entry e = (Entry) i.next();
                connection.setRequestProperty(
                    (String)e.getKey(), 
                    (String)e.getValue()
                );
            }
            connection.connect();
            return connection;
        } catch (IOException exception) {				
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.COMMUNICATION_FAILURE,
                "URL connection could not be established",
                new BasicException.Parameter("url", url),
                new BasicException.Parameter("requestProperties", requestProperties.keySet())
            );
        }
    }

    /**
     * Close a stream unless its reference is null.
     * 
     * @param stream
     */
    protected static void close(
        InputStream stream
    ){
        if (stream != null) try {
            stream.close();
        } catch (IOException exception) {
            // Ignore I/O Exceptions
        }
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------

    // the connection's URL
    protected URL url;

    // the request properties
    private Map requestProperties;

}
