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
package org.openmdx.uses.org.apache.servicemix.soap;

import java.net.URI;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.openmdx.uses.org.apache.servicemix.soap.marshalers.SoapMarshaler;

/**
 * Represents a SOAP fault which occurred while processing the
 * message.
 *
 * @author Guillaume Nodet
 * @version $Revision: 1.1 $
 * @since 3.0
 */
public class SoapFault extends Exception {
	
    private static final long serialVersionUID = 984561453557136677L;
    
    public static final QName SENDER = SoapMarshaler.SOAP_12_CODE_SENDER;
	public static final QName RECEIVER = SoapMarshaler.SOAP_12_CODE_RECEIVER;
	
    private QName code;
    private QName subcode;
    private String reason;
    private URI node;
    private URI role;
    private Source details;

    public SoapFault(Exception cause) {
        super(cause);
    }

    public SoapFault(QName code, String reason) {
        super(reason);
        this.code = code;
        this.reason = reason;
    }

    public SoapFault(QName code, QName subcode, String reason) {
        super(reason);
        this.code = code;
        this.subcode = subcode;
        this.reason = reason;
    }

    public SoapFault(QName code, String reason, URI node, URI role) {
        super(reason);
        this.code = code;
        this.reason = reason;
        this.node = node;
        this.role = role;
    }

    public SoapFault(QName code, String reason, URI node, URI role, Source details) {
        super(reason);
        this.code = code;
        this.reason = reason;
        this.node = node;
        this.role = role;
        this.details = details;
    }

    public SoapFault(QName code, QName subcode, String reason, URI node, URI role, Source details) {
        super(reason);
        this.code = code;
        this.subcode = subcode;
        this.reason = reason;
        this.node = node;
        this.role = role;
        this.details = details;
    }

    public QName getCode() {
        return code;
    }

    public QName getSubcode() {
        return subcode;
    }

    public String getReason() {
        return reason;
    }

    public URI getNode() {
        return node;
    }

    public URI getRole() {
        return role;
    }

	public Source getDetails() {
		return details;
	}

    public void translateCodeTo11() {
        if (code != null) {
            if (subcode != null) {
                code = subcode;
                subcode = null;
            } else if (SoapMarshaler.SOAP_12_CODE_DATAENCODINGUNKNOWN.equals(code)) {
                code = SoapMarshaler.SOAP_11_CODE_CLIENT;
            } else if (SoapMarshaler.SOAP_12_CODE_MUSTUNDERSTAND.equals(code)) {
                code = SoapMarshaler.SOAP_11_CODE_MUSTUNDERSTAND;
            } else if (SoapMarshaler.SOAP_12_CODE_RECEIVER.equals(code)) {
                code = SoapMarshaler.SOAP_11_CODE_SERVER;
            } else if (SoapMarshaler.SOAP_12_CODE_SENDER.equals(code)) {
                code = SoapMarshaler.SOAP_11_CODE_CLIENT;
            }
        } else {
            code = SoapMarshaler.SOAP_11_CODE_SERVER;
        }
    }

    public void translateCodeTo12() {
        if (code != null && subcode == null) {
            if (SoapMarshaler.SOAP_11_CODE_CLIENT.equals(code)) {
                code = SoapMarshaler.SOAP_12_CODE_SENDER;
            } else if (SoapMarshaler.SOAP_11_CODE_MUSTUNDERSTAND.equals(code)) {
                code = SoapMarshaler.SOAP_12_CODE_MUSTUNDERSTAND;
            } else if (SoapMarshaler.SOAP_11_CODE_SERVER.equals(code)) {
                code = SoapMarshaler.SOAP_12_CODE_RECEIVER;
            } else if (SoapMarshaler.SOAP_11_CODE_VERSIONMISMATCH.equals(code)) {
                code = SoapMarshaler.SOAP_12_CODE_VERSIONMISMATCH;
            } else {
                subcode = code;
                code = SoapMarshaler.SOAP_12_CODE_SENDER;
            }
        } else if (code == null) {
            code = SoapMarshaler.SOAP_12_CODE_RECEIVER;
        }
    }
}
