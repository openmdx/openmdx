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
package org.openmdx.uses.org.apache.servicemix.soap.marshalers;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.StAXSourceTransformer;

/**
 * 
 * @author Guillaume Nodet
 * @version $Revision: 1.1 $
 * @since 3.0 
 */
public class SoapMarshaler {

    public static final String MIME_CONTENT_TYPE = "Content-Type";
	public static final String MULTIPART_CONTENT = "multipart/";
	public static final String SOAP_PART_ID = "soap-request";
	public static final String SOAP_11_URI = "http://schemas.xmlsoap.org/soap/envelope/";
	public static final String SOAP_12_URI = "http://www.w3.org/2003/05/soap-envelope";
	public static final String SOAP_PREFIX = "env";
	public static final String ENVELOPE = "Envelope";
	public static final String HEADER = "Header";
	public static final String BODY = "Body";
	public static final String FAULT = "Fault";
    
    public static final QName SOAP_11_FAULTCODE = new QName("faultcode");
    public static final QName SOAP_11_FAULTSTRING = new QName("faultstring");
    public static final QName SOAP_11_FAULTACTOR = new QName("faultactor");
    public static final QName SOAP_11_FAULTDETAIL = new QName("detail");
    public static final QName SOAP_11_CODE_VERSIONMISMATCH = new QName(SOAP_11_URI, "VersionMismatch");
    public static final QName SOAP_11_CODE_MUSTUNDERSTAND = new QName(SOAP_11_URI, "MustUnderstand");
    public static final QName SOAP_11_CODE_CLIENT = new QName(SOAP_11_URI, "Client");
    public static final QName SOAP_11_CODE_SERVER = new QName(SOAP_11_URI, "Server");
    
    public static final QName SOAP_12_FAULTCODE = new QName(SOAP_12_URI, "Code");
    public static final QName SOAP_12_FAULTSUBCODE = new QName(SOAP_12_URI, "Subcode");
    public static final QName SOAP_12_FAULTVALUE = new QName(SOAP_12_URI, "Value");
    public static final QName SOAP_12_FAULTREASON = new QName(SOAP_12_URI, "Reason");
    public static final QName SOAP_12_FAULTTEXT = new QName(SOAP_12_URI, "Text");
    public static final QName SOAP_12_FAULTNODE = new QName(SOAP_12_URI, "Node");
    public static final QName SOAP_12_FAULTROLE = new QName(SOAP_12_URI, "Role");
    public static final QName SOAP_12_FAULTDETAIL = new QName(SOAP_12_URI, "Detail");
    public static final QName SOAP_12_CODE_DATAENCODINGUNKNOWN = new QName(SOAP_12_URI, "DataEncodingUnknown");
    public static final QName SOAP_12_CODE_VERSIONMISMATCH = new QName(SOAP_12_URI, "VersionMismatch");
    public static final QName SOAP_12_CODE_MUSTUNDERSTAND = new QName(SOAP_12_URI, "MustUnderstand");
    public static final QName SOAP_12_CODE_RECEIVER = new QName(SOAP_12_URI, "Receiver");
    public static final QName SOAP_12_CODE_SENDER = new QName(SOAP_12_URI, "Sender");
    
	protected XMLInputFactory inputFactory;
	protected XMLOutputFactory outputFactory;
	protected StAXSourceTransformer  sourceTransformer;
	protected boolean repairingNamespace;
	protected String prefix = SOAP_PREFIX;
	protected boolean soap = true;
    protected boolean useDom = false;
	protected String soapUri = SOAP_12_URI;

	public SoapMarshaler() {
	}

    public SoapMarshaler(boolean soap) {
        this.soap = soap;
    }

	public SoapMarshaler(boolean soap, boolean useDom) {
		this.soap = soap;
        this.useDom = useDom;
	}

    public XMLInputFactory getInputFactory() {
        if (inputFactory == null) {
            inputFactory = XMLInputFactory.newInstance();
            inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        }
        return inputFactory;
    }

    public XMLOutputFactory getOutputFactory() {
        if (outputFactory == null) {
            outputFactory = XMLOutputFactory.newInstance();
            if (isRepairingNamespace()) {
                outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, 
                						  Boolean.valueOf(isRepairingNamespace()));
            }
        }
        return outputFactory;
    }
    
    public StAXSourceTransformer getSourceTransformer() {
    	if (sourceTransformer == null) {
    		sourceTransformer = new StAXSourceTransformer();
    	}
    	return sourceTransformer;
    }

    public boolean isRepairingNamespace() {
        return repairingNamespace;
    }

    public void setRepairingNamespace(boolean repairingNamespace) {
        this.repairingNamespace = repairingNamespace;
    }
    
    public boolean isSoap() {
    	return soap;
    }
    
    public void setSoap(boolean soap) {
    	this.soap = soap;
    }
    
    /**
     * @return the useDom
     */
    public boolean isUseDom() {
        return useDom;
    }

    /**
     * @param useDom the useDom to set
     */
    public void setUseDom(boolean useDom) {
        this.useDom = useDom;
    }

    public String getPrefix() {
    	return prefix;
    }
    
    public void setPrefix(String prefix) {
    	this.prefix = prefix;
    }
    
    public String getSoapUri() {
    	return soapUri;
    }
    
    public void setSoapUri(String soapUri) {
    	this.soapUri = soapUri;
    }
    
    public SoapReader createReader() {
    	return new SoapReader(this);
    }

	public SoapWriter createWriter(SoapMessage message) {
		return new SoapWriter(this, message);
	}
}
