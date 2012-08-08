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
package org.openmdx.uses.org.apache.servicemix.jbi.util;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.security.auth.Subject;
import javax.xml.transform.Source;

import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.StringSource;

/**
 * @author gnodet
 * @version $Revision: 1.1 $
 */
public class MessageUtil {

    public static void transfer(NormalizedMessage source, NormalizedMessage dest) throws MessagingException {
        dest.setContent(source.getContent());
        for (Iterator it = source.getPropertyNames().iterator(); it.hasNext();) {
            String name = (String) it.next();
            dest.setProperty(name, source.getProperty(name));
        }
        for (Iterator it = source.getAttachmentNames().iterator(); it.hasNext();) {
            String name = (String) it.next();
            dest.addAttachment(name, source.getAttachment(name));
        }
        dest.setSecuritySubject(source.getSecuritySubject());
    }
    
    public static NormalizedMessage copy(NormalizedMessage source) throws MessagingException {
        if (source instanceof Fault) {
            return new FaultImpl((Fault) source);
        } else {
            return new NormalizedMessageImpl(source);
        }
    }
    
    public static NormalizedMessage copyIn(MessageExchange exchange) throws MessagingException {
        return copy(exchange.getMessage("in"));
    }
    
    public static NormalizedMessage copyOut(MessageExchange exchange) throws MessagingException {
        return copy(exchange.getMessage("out"));
    }
    
    public static Fault copyFault(MessageExchange exchange) throws MessagingException {
        return (Fault) copy(exchange.getMessage("fault"));
    }
    
    public static void transferInToIn(MessageExchange source, MessageExchange dest) throws MessagingException {
        transferToIn(source.getMessage("in"), dest);
    }
    
    public static void transferOutToIn(MessageExchange source, MessageExchange dest) throws MessagingException {
        transferToIn(source.getMessage("out"), dest);
    }
    
    public static void transferToIn(NormalizedMessage sourceMsg, MessageExchange dest) throws MessagingException {
        transferTo(sourceMsg, dest, "in");
    }
    
    public static void transferOutToOut(MessageExchange source, MessageExchange dest) throws MessagingException {
        transferToOut(source.getMessage("out"), dest);
    }
    
    public static void transferInToOut(MessageExchange source, MessageExchange dest) throws MessagingException {
        transferToOut(source.getMessage("in"), dest);
    }
    
    public static void transferToOut(NormalizedMessage sourceMsg, MessageExchange dest) throws MessagingException {
        transferTo(sourceMsg, dest, "out");
    }
    
    public static void transferFaultToFault(MessageExchange source, MessageExchange dest) throws MessagingException {
        transferToFault(source.getFault(), dest);
    }
    
    public static void transferToFault(Fault fault, MessageExchange dest) throws MessagingException {
        transferTo(fault, dest, "fault");
    }
    
    public static void transferTo(NormalizedMessage sourceMsg, MessageExchange dest, String name) throws MessagingException {
        NormalizedMessage destMsg = (sourceMsg instanceof Fault) ? dest.createFault() : dest.createMessage();
        transfer(sourceMsg, destMsg);
        dest.setMessage(destMsg, name);
    }
    
    public static void transferTo(MessageExchange source, MessageExchange dest, String name) throws MessagingException {
        NormalizedMessage sourceMsg = source.getMessage(name);
        NormalizedMessage destMsg = (sourceMsg instanceof Fault) ? dest.createFault() : dest.createMessage();
        transfer(sourceMsg, destMsg);
        dest.setMessage(destMsg, name);
    }
    
    public static class NormalizedMessageImpl implements NormalizedMessage, Serializable {

        private static final long serialVersionUID = -5813947566001096708L;
        
        private Subject subject;
        private Source content;
        private Map properties = new HashMap();
        private Map attachments = new HashMap();
        
        public NormalizedMessageImpl() {
        }
        
        public NormalizedMessageImpl(NormalizedMessage message) throws MessagingException {
            try {
                String str = new SourceTransformer().contentToString(message);
                if (str != null) {
                    this.content = new StringSource(str);
                }
                for (Iterator it = message.getPropertyNames().iterator(); it.hasNext();) {
                    String name = (String) it.next();
                    this.properties.put(name, message.getProperty(name));
                }
                for (Iterator it = message.getAttachmentNames().iterator(); it.hasNext();) {
                    String name = (String) it.next();
                    DataHandler dh = message.getAttachment(name);
                    DataSource ds = dh.getDataSource();
                    if (ds instanceof ByteArrayDataSource == false) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        FileUtil.copyInputStream(ds.getInputStream(), baos);
                        ByteArrayDataSource bads = new ByteArrayDataSource(baos.toByteArray(), ds.getContentType());
                        bads.setName(ds.getName());
                        dh = new DataHandler(bads);
                    }
                    this.attachments.put(name, dh);
                }
                this.subject = message.getSecuritySubject();
            } catch (MessagingException e) {
                throw e;
            } catch (Exception e) {
                throw new MessagingException(e);
            }
        }
        
        public void addAttachment(String id, DataHandler content) throws MessagingException {
            this.attachments.put(id, content);
        }

        public Source getContent() {
            return content;
        }

        public DataHandler getAttachment(String id) {
            return (DataHandler) this.attachments.get(id);
        }

        public Set getAttachmentNames() {
            return this.attachments.keySet();
        }

        public void removeAttachment(String id) throws MessagingException {
            this.attachments.remove(id);
        }

        public void setContent(Source content) throws MessagingException {
            this.content = content;
        }

        public void setProperty(String name, Object value) {
            this.properties.put(name, value);
        }

        public void setSecuritySubject(Subject subject) {
            this.subject = subject;
        }

        public Set getPropertyNames() {
            return this.properties.keySet();
        }

        public Object getProperty(String name) {
            return this.properties.get(name);
        }

        public Subject getSecuritySubject() {
            return this.subject;
        }
        
    }
    
    public static class FaultImpl extends NormalizedMessageImpl implements Fault {
        private static final long serialVersionUID = -6076815664102825860L;

        public FaultImpl() {
        }
        
        public FaultImpl(Fault fault) throws MessagingException {
            super(fault);
        }
    }
    
}
