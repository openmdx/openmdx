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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.transaction.Transaction;
import javax.xml.namespace.QName;

import org.openmdx.uses.org.apache.servicemix.components.util.CopyTransformer;
import org.openmdx.uses.org.apache.servicemix.jbi.framework.ComponentNameSpace;

/**
 * ExchangePacket is responsible for carrying MessageExchange payloads
 * 
 * @version $Revision: 1.1 $
 */
public class ExchangePacket implements Externalizable {
    
    private static final long serialVersionUID = -9110837382914609624L;
    
    protected URI pattern;
    protected String exchangeId;
    protected ComponentNameSpace destinationId;
    protected ComponentNameSpace sourceId;
    protected ExchangeStatus status = ExchangeStatus.ACTIVE;
    protected QName serviceName;
    protected QName interfaceName;
    protected QName operationName;
    protected Exception error;
    protected Map properties;
    protected NormalizedMessageImpl in;
    protected NormalizedMessageImpl out;
    protected FaultImpl fault;
    protected ServiceEndpoint endpoint;
    protected transient Transaction transactionContext;
    protected Boolean persistent;
    protected boolean aborted;

    
    public ExchangePacket() {
    }

    public ExchangePacket(ExchangePacket packet) throws MessagingException {
        this.destinationId = packet.destinationId;
        this.endpoint = null; // packet.endpoint;
        this.error = null;
        this.exchangeId = null; //???;
        this.interfaceName = packet.interfaceName;
        CopyTransformer ct = new CopyTransformer();
        if (packet.in != null) {
            in = new NormalizedMessageImpl();
            ct.transform(null, packet.in, in);
        }
        if (packet.out != null) {
            out = new NormalizedMessageImpl();
            ct.transform(null, packet.out, out);
        }
        if (packet.fault != null) {
            fault = new FaultImpl();
            ct.transform(null, packet.fault, fault);
        }
        this.operationName = packet.operationName;
        this.pattern = packet.pattern;
        if (packet.properties != null && packet.properties.size() > 0) {
            getProperties().putAll(packet.properties);
        }
        this.serviceName = packet.serviceName;
        this.sourceId = packet.sourceId;
        this.status = packet.status;
        this.transactionContext = packet.transactionContext;
        this.persistent = packet.persistent;
    }

    /**
     * @return Returns the endpoint.
     */
    public ServiceEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * @param endpoint The endpoint to set.
     */
    public void setEndpoint(ServiceEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @return Returns the transactionContext.
     */
    public Transaction getTransactionContext() {
        return transactionContext;
    }

    /**
     * @param transactionContext The transactionContext to set.
     */
    public void setTransactionContext(Transaction transactionContext) {
        this.transactionContext = transactionContext;
    }

    /**
     * @return Returns the interfaceName.
     */
    public QName getInterfaceName() {
        return interfaceName;
    }

    /**
     * @param interfaceName The interfaceName to set.
     */
    public void setInterfaceName(QName interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * @return Returns the operationName.
     */
    public QName getOperationName() {
        return operationName;
    }

    /**
     * @param operationName The operationName to set.
     */
    public void setOperationName(QName operationName) {
        this.operationName = operationName;
    }

    /**
     * @return Returns the serviceName.
     */
    public QName getServiceName() {
        return serviceName;
    }

    /**
     * @param serviceName The serviceName to set.
     */
    public void setServiceName(QName serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * @param status The status to set.
     */
    public void setStatus(ExchangeStatus status) {
        this.status = status;
    }

    /**
     * @return the status
     */
    public ExchangeStatus getStatus() {
        return status;
    }

    /**
     * @return Returns the pattern.
     */
    public URI getPattern() {
        return pattern;
    }

    /**
     * @param pattern The pattern to set.
     */
    public void setPattern(URI pattern) {
        this.pattern = pattern;
    }

    /**
     * @return Returns the error.
     */
    public Exception getError() {
        return error;
    }

    /**
     * @param error The error to set.
     */
    public void setError(Exception error) {
        this.error = error;
        this.status = ExchangeStatus.ERROR;
    }

    /**
     * @return Returns the exchangeId.
     */
    public String getExchangeId() {
        return exchangeId;
    }

    /**
     * @param exchangeId The exchangeId to set.
     */
    public void setExchangeId(String exchangeId) {
        this.exchangeId = exchangeId;
    }

    /**
     * @return Returns the properties.
     */
    public Map getProperties() {
        if (properties == null) {
            // No need to have concurrent access, as the
            // message exchange can only be used from a single thread at a time
            properties = new HashMap();
        }
        return properties;
    }

    /**
     * @param name
     * @return the proerty from the exchange
     */
    public Object getProperty(String name) {
        if (properties != null) {
            return properties.get(name);
        }
        return null;
    }

    /**
     * set a named property on the exchange
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
     * @return property names
     */
    public Set getPropertyNames() {
        if (properties != null) {
            return Collections.unmodifiableSet(properties.keySet());
        }
        return Collections.EMPTY_SET;
    }

    /**
     * @return Returns the sourceId.
     */
    public ComponentNameSpace getSourceId() {
        return sourceId;
    }

    /**
     * @param sourceId The sourceId to set.
     */
    public void setSourceId(ComponentNameSpace sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * @return Returns the destinationId.
     */
    public ComponentNameSpace getDestinationId() {
        return destinationId;
    }

    /**
     * @param destinationId The destinationId to set.
     */
    public void setDestinationId(ComponentNameSpace destinationId) {
        this.destinationId = destinationId;
    }

    /**
     * @return Returns the fault.
     */
    public Fault getFault() {
        return fault;
    }

    /**
     * @param fault The fault to set.
     */
    public void setFault(FaultImpl fault) {
        this.fault = fault;
    }

    /**
     * @return Returns the in.
     */
    public NormalizedMessage getIn() {
        return in;
    }

    /**
     * @param in The in to set.
     */
    public void setIn(NormalizedMessageImpl in) {
        this.in = in;
    }

    /**
     * @return Returns the out.
     */
    public NormalizedMessage getOut() {
        return out;
    }

    /**
     * @param out The out to set.
     */
    public void setOut(NormalizedMessageImpl out) {
        this.out = out;
    }

    /**
     * @return pretty print
     */
    public String toString() {
        return "ExchangePacket[: id=" + exchangeId + ", serviceDest=" + serviceName + ",endpoint=" + endpoint + "]";
    }

    /**
     * Write to a Stream
     * @param output
     * @throws IOException
     */
    public void writeExternal(ObjectOutput output) throws IOException {
        output.writeUTF(pattern.toString());
        output.writeUTF(exchangeId != null ? exchangeId : "");
        output.writeUTF(status.toString());
        output.writeObject(destinationId);
        output.writeObject(sourceId);
        output.writeObject(serviceName);
        output.writeObject(interfaceName);
        output.writeObject(operationName);
        output.writeObject(error);
        output.writeObject(properties);
        output.writeObject(in);
        output.writeObject(out);
        output.writeObject(fault);
        output.writeObject(endpoint);
        output.writeByte((persistent == null) ? 0 : persistent.booleanValue() ? 1 : 2);
    }

    /**
     * Read from a stream
     * 
     * @param input
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
        pattern = URI.create(input.readUTF());
        exchangeId = input.readUTF();
        status = ExchangeStatus.valueOf(input.readUTF());
        destinationId = (ComponentNameSpace) input.readObject();
        sourceId = (ComponentNameSpace) input.readObject();
        serviceName = (QName) input.readObject();
        interfaceName = (QName) input.readObject();
        operationName = (QName) input.readObject();
        error = (Exception) input.readObject();
        properties = (Map) input.readObject();
        in = (NormalizedMessageImpl) input.readObject();
        out = (NormalizedMessageImpl) input.readObject();
        fault = (FaultImpl) input.readObject();
        endpoint = (ServiceEndpoint) input.readObject();
        byte p = input.readByte();
        persistent = (p == 0) ? null : p == 1 ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Creates a copy of the packet so it can be sent to another destination
     * @throws MessagingException 
     */
    public ExchangePacket copy() throws MessagingException {
        return new ExchangePacket(this);
    }

	public Boolean getPersistent() {
		return persistent;
	}

	public void setPersistent(Boolean persistent) {
		this.persistent = persistent;
	}

    public boolean isAborted() {
        return aborted;
    }

    public void setAborted(boolean timedOut) {
        this.aborted = timedOut;
    }
    
    /**
     * Retrieve the serialized from of this packet
     * @return the serialized packet
     * @throws IOException 
     */
    public byte[] getData() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(this);
        out.close();
        return buffer.toByteArray();
    }
    
    /**
     * Deserialize an ExchangePacket.
     * @param data the serialized packet
     * @return the deserialized packet
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static ExchangePacket readPacket(byte[] data) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        return (ExchangePacket) ois.readObject();
    }

}