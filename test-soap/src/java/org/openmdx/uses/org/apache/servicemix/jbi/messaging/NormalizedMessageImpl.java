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
package org.openmdx.uses.org.apache.servicemix.jbi.messaging;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
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
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.openmdx.uses.org.apache.servicemix.client.Message;
import org.openmdx.uses.org.apache.servicemix.jbi.RuntimeJBIException;
import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.BytesSource;
import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.StringSource;
import org.openmdx.uses.org.apache.servicemix.jbi.util.ByteArrayDataSource;
import org.openmdx.uses.org.apache.servicemix.jbi.util.FileUtil;

/**
 * Represents a JBI NormalizedMessage.
 *
 * @version $Revision: 1.1 $
 */
public class NormalizedMessageImpl implements NormalizedMessage, Externalizable, Message {
    
    private static final long serialVersionUID = 9179194301410526549L;
    
    protected transient MessageExchangeImpl exchange;
    private transient Source content;
    private transient Object body;
    private Subject securitySubject;
    private Map properties;
    private Map attachments;

    private static SourceTransformer transformer = new SourceTransformer();

    /**
     * Constructor
     *
     */
    public NormalizedMessageImpl() {
    }


    /**
     * Constructor
     * @param exchange
     */
    public NormalizedMessageImpl(MessageExchangeImpl exchange) {
        this.exchange = exchange;
    }

    


    /**
     * @return the content of the message
     */
    public Source getContent() {
        if (content == null && body != null) {
            try {
                getMarshaler().marshal(exchange, this, body);
            }
            catch (MessagingException e) {
                throw new RuntimeJBIException(e);
            }
        }
        return content;
    }

    /**
     * set the content fo the message
     *
     * @param source
     */
    public void setContent(Source source) {
        this.content = source;
    }

    /**
     * @return the security subject from the message
     */
    public Subject getSecuritySubject() {
        return securitySubject;
    }

    /**
     * set the security subject
     *
     * @param securitySubject
     */
    public void setSecuritySubject(Subject securitySubject) {
        this.securitySubject = securitySubject;
    }

    /**
     * get a named property
     *
     * @param name
     * @return a property from the message
     */
    public Object getProperty(String name) {
        if (properties != null) {
            return properties.get(name);
        }
        return null;
    }

    /**
     * @return an iterator of property names
     */
    public Set getPropertyNames() {
        if (properties != null) {
            return Collections.unmodifiableSet(properties.keySet());
        }
        return Collections.EMPTY_SET;
    }

    /**
     * set a property
     *
     * @param name
     * @param value
     */
    public void setProperty(String name, Object value) {
        if (value == null) {
            if (properties != null) {
                properties.remove(name);
            }
        } else {
            getProperties().put(name, value);
        }
    }

    /**
     * Add an attachment
     *
     * @param id
     * @param content
     */
    public void addAttachment(String id, DataHandler content) {
        getAttachments().put(id, content.getDataSource());
    }

    /**
     * Get a named attachement
     *
     * @param id
     * @return the specified attachment
     */
    public DataHandler getAttachment(String id) {
        if (attachments != null) {
            return new DataHandler((DataSource) attachments.get(id));
        }
        return null;
    }

    /**
     * @return a list of identifiers for atachments
     */
    public Iterator listAttachments() {
        if (attachments != null) {
            return attachments.keySet().iterator();
        }
        return Collections.EMPTY_LIST.iterator();
    }

    /**
     * remove an identified attachment
     *
     * @param id
     */
    public void removeAttachment(String id) {
        if (attachments != null) {
            attachments.remove(id);
        }
    }
    
    /** Returns a list of identifiers for each attachment to the message.
     *  @return iterator over String attachment identifiers
     */
    public Set getAttachmentNames(){
        if (attachments != null){
            return Collections.unmodifiableSet(attachments.keySet());
        }
        return Collections.EMPTY_SET;
    }


    public String toString() {
        return super.toString() + "{properties: " + getProperties() + "}";
    }
    
    // Scripting helper methods to add expressive power
    // when using languages like Groovy, Velocity etc
    //-------------------------------------------------------------------------

    public Object getBody() throws MessagingException {
        if (body == null) {
            body = getMarshaler().unmarshal(exchange, this);
        }
        return body;
    }

    public Object getBody(PojoMarshaler marshaler) throws MessagingException {
        return marshaler.unmarshal(exchange, this);
    }

    public void setBody(Object body) throws MessagingException {
        this.body = body;
    }

    public String getBodyText() throws TransformerException {
        return transformer.toString(getContent());
    }

    public void setBodyText(String xml) {
        setContent(new StringSource(xml));
    }

    public PojoMarshaler getMarshaler() {
        return exchange.getMarshaler();
    }

    public MessageExchange getExchange() {
        return exchange;
    }

    public Fault createFault() throws MessagingException {
        return getExchange().createFault();
    }


    // Implementation methods
    //-------------------------------------------------------------------------
    protected Map getProperties() {
        if (properties == null) {
            properties = createPropertiesMap();
        }
        return properties;
    }

    protected Map getAttachments() {
        if (attachments == null) {
            attachments = createAttachmentsMap();
        }
        return attachments;
    }

    protected void setAttachments(Map attachments) {
        this.attachments = attachments;
    }

    protected void setProperties(Map properties) {
        this.properties = properties;
    }

    protected Map createPropertiesMap() {
        // Normalized exchanges do not need to be thread-safe
        return new HashMap();
    }

    protected Map createAttachmentsMap() {
        // Normalized exchanges do not need to be thread-safe
        return new HashMap();
    }

    /**
     * Write to a Stream
     * @param out
     * @throws IOException
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        try {
            convertAttachments();
            out.writeObject(attachments);
            out.writeObject(properties);
            String src = transformer.toString(content);
            out.writeObject(src);
            // We have read the source
            // so now, ensure that it can be re-read
            if ((content instanceof StreamSource ||
                    content instanceof SAXSource) &&
                    !(content instanceof StringSource) &&
                    !(content instanceof BytesSource) /*&&
                    !(content instanceof ResourceSource)*/) {
                content = new StringSource(src); 
            }
        } catch (TransformerException e) {
            throw (IOException) new IOException("Could not transform content to string").initCause(e);
        }
    }

    private void convertAttachments() throws IOException {
        if (attachments != null) {
            Map newAttachments = createAttachmentsMap();
            for (Iterator it = attachments.keySet().iterator(); it.hasNext();) {
                String name = (String) it.next();
                DataSource ds = (DataSource) attachments.get(name);
                if (ds instanceof ByteArrayDataSource) {
                    newAttachments.put(name, ds);
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    FileUtil.copyInputStream(ds.getInputStream(), baos);
                    ByteArrayDataSource bads = new ByteArrayDataSource(baos.toByteArray(), ds.getContentType());
                    bads.setName(ds.getName());
                    newAttachments.put(name, bads);
                }
            }
            attachments = newAttachments;
        }
    }

    /**
     * Read from a stream
     * 
     * @param in
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        attachments = (Map) in.readObject();
        properties = (Map) in.readObject();
        String src = (String) in.readObject();
        if (src != null) {
            content = new StringSource(src);
        }
    }

}

