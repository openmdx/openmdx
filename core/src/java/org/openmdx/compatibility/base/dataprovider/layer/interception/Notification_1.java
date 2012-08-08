/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Notification_1.java,v 1.5 2008/06/28 00:21:45 hburger Exp $
 * Description: Notification_1 class
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/28 00:21:45 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.compatibility.base.dataprovider.layer.interception;

import java.util.Iterator;
import java.util.ListIterator;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.naming.Path;

/**
 * A notifying interception layer plugin
 */
@SuppressWarnings("unchecked")
public class Notification_1
    extends Standard_1
{

    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws Exception, ServiceException {
        super.activate(id, configuration, delegation);
        Context context = new InitialContext();
        TopicConnectionFactory factory = (
            TopicConnectionFactory
        ) context.lookup(
            (String)configuration.values(
                LayerConfigurationEntries.JMS_CONNECTION_FACTORY
            ).get(0)
        );
        this.connection = factory.createTopicConnection();
        this.session = this.connection.createTopicSession(
            false, 
            Session.AUTO_ACKNOWLEDGE // WLTopicSession.NO_ACKNOWLEDGE
        );
        this.topic = (Topic) context.lookup(
            (String)configuration.values(
                LayerConfigurationEntries.JMS_TOPIC
            ).get(0)
        );
        this.publisher = this.session.createPublisher(this.topic);
        this.message = session.createMapMessage(); 
        this.connection.start();
    }

    private static Object marshal(
        Object object
    ){
        if (object instanceof Boolean ||
            object instanceof Byte ||
            object instanceof Short ||
            object instanceof Integer ||
            object instanceof Long ||
            object instanceof Float ||
            object instanceof Double ||
            object instanceof String ||
            object instanceof byte[]
        ) {
            return object;
        } else if (object instanceof Path) {
            return ((Path)object).toUri();
        } else {
            return object.toString();
        }           
    }
    
    /**
     * Notify about a completed request
     */
    private void publish(
        DataproviderRequest request,
        DataproviderReply reply
    ){
        try {
            this.message.clearBody();
            this.message.clearProperties();
            this.message.setStringProperty("path", request.path().toUri());
            this.message.setShortProperty("operation", request.operation());
            for(
                Iterator a = request.object().attributeNames().iterator();
                a.hasNext();
            ){
                String name = (String) a.next();
                SparseList values = request.object().getValues(name);
                for (
                    ListIterator i = values.populationIterator();
                    i.hasNext();
                ){
                    int index = i.nextIndex();
                    this.message.setObject(
                        name + '[' + index + ']',
                        marshal(i.next())
                    );
                }
            } 
            this.publisher.publish(this.message);
        } catch (JMSException exception) {
            new ServiceException(exception).log();
        }
    }
    
    /**
     * This method allows the dataprovider layers postprocessing of a 
     * collection of requests as a whole after the actual processing of the 
     * individual requests has been done.
     *
     * @param       header
     *              the requests' service header
     * @param       requests
     *              the request list
     * @param       replys
     *              the reply list
     *
     * @exception   ServiceException
     *              on failure
     */
    public void epilog(
        ServiceHeader header,
        DataproviderRequest[] requests,
        DataproviderReply[] replies
    ) throws ServiceException {
        for (
            int index = 0;
            index < requests.length;
            index++
        ) switch (requests[index].operation()) {
            case DataproviderOperations.OBJECT_CREATION:
            case DataproviderOperations.OBJECT_SETTING:
            case DataproviderOperations.OBJECT_MODIFICATION:
            case DataproviderOperations.OBJECT_REPLACEMENT:
            case DataproviderOperations.OBJECT_REMOVAL:
             publish (requests[index], replies[index]);
        }
    }


    //------------------------------------------------------------------------
    // Instance members
    //------------------------------------------------------------------------

    private TopicConnection connection;
    private TopicSession session;
    private TopicPublisher publisher;
    private Topic topic;
    private MapMessage message;

}
