/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openmdx.uses.org.apache.servicemix.components.http;

import java.io.IOException;

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.NormalizedMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

/**
 * A HTTP Binding Component which performs an {@link InOut} exchange with JBI and returns the response
 * by default but is configurable to be an {@link InOnly} exchange.
 *
 * @version $Revision: 1.1 $
 */
public class HttpInOutBinding extends HttpBindingSupport {

    private boolean defaultInOut = true;

    public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException,
            JBIException {
        //response.setContentType("application/soap+xml");
        if (isInOutRequest(request, response)) {
            processInOut(request, response);
        }
        else {
            processInOnly(request, response);
        }
    }

    public void processInOut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException,
            JBIException {
        MessageExchangeFactory factory = getExchangeFactory();
        InOut exchange = factory.createInOutExchange();
        NormalizedMessage inMessage = exchange.createMessage();
        try {
            getMarshaler().toNMS(exchange, inMessage, request);
            exchange.setInMessage(inMessage);
            boolean result = getDeliveryChannel().sendSync(exchange);
            if (result) {
            	if (exchange.getStatus() == ExchangeStatus.ERROR) {
            		if (exchange.getError() != null) {
            			throw new ServletException(exchange.getError());
            		} else {
            			throw new ServletException("Exchange status is ERROR");
            		}
            	}
                getMarshaler().toResponse(exchange, exchange.getOutMessage(), response);
            }
            done(exchange);
            response.setStatus(HttpServletResponse.SC_OK);
        }
        catch (IOException e) {
            fail(exchange, e);
            outputException(response, e);
        }
        catch (TransformerException e) {
            fail(exchange, e);
            outputException(response, e);
        }
    }

    public void processInOnly(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException,
            JBIException {
        MessageExchangeFactory factory = getExchangeFactory();
        InOnly exchange = factory.createInOnlyExchange();
        NormalizedMessage inMessage = exchange.createMessage();
        try {
            HttpMarshaler marshaler = getMarshaler();
            marshaler.toNMS(exchange, inMessage, request);
            exchange.setInMessage(inMessage);
            // Do a sendSync so that the stream is not closed
            getDeliveryChannel().sendSync(exchange);
            response.setStatus(HttpServletResponse.SC_OK);
        }
        catch (IOException e) {
            fail(exchange, e);
            outputException(response, e);
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    public boolean isDefaultInOut() {
        return defaultInOut;
    }

    /**
     * Sets whether an InOut (the default) or an InOnly message exchange will be used by default.
     */
    public void setDefaultInOut(boolean defaultInOut) {
        this.defaultInOut = defaultInOut;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Return true if this request is an {@link InOut} request otherwise it will be assumed to be an {@link InOnly}
     */
    protected boolean isInOutRequest(HttpServletRequest request, HttpServletResponse response) {
        return isDefaultInOut();
    }


}
