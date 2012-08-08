/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: EmailAddressImpl.java,v 1.1 2009/02/04 11:06:37 hburger Exp $
 * Description: E-Mail Address  
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/04 11:06:37 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.test.app1.aop2;

import org.openmdx.base.jmi1.Void;
import org.openmdx.test.app1.jmi1.AddressFormatAsParams;
import org.openmdx.test.app1.jmi1.AddressFormatAsResult;
import org.openmdx.test.app1.jmi1.App1Package;
import org.openmdx.test.app1.jmi1.EmailAddress;
import org.openmdx.test.app1.jmi1.EmailAddressSendMessageParams;
import org.openmdx.test.app1.jmi1.EmailAddressSendMessageTemplateParams;
import org.openmdx.test.app1.jmi1.EmailAddressSendMessageTemplateResult;
import org.openmdx.test.app1.jmi1.MessageTemplate;

/**
 * E-Mail Address
 */
public class EmailAddressImpl extends AddressImpl<org.openmdx.test.app1.jmi1.EmailAddress,org.openmdx.test.app1.cci2.EmailAddress> {

    /**
     * Constructor 
     *
     * @param same
     * @param next
     */
    public EmailAddressImpl(
        org.openmdx.test.app1.jmi1.EmailAddress same,
        org.openmdx.test.app1.cci2.EmailAddress next
    ) {
        super(same, next);
    }

    /**
     * Format address
     * 
     * @param in the method's input structure
     * 
     * @return the method's result structure
     * 
     * @see org.openmdx.test.app1.cci2.Address#formatAs(org.openmdx.test.app1.cci2.AddressFormatAsParams)
     */
    public AddressFormatAsResult formatAs(AddressFormatAsParams in) {
        EmailAddress same = sameObject();
        App1Package app1Package = (App1Package) same.refImmediatePackage();
        if(STANDARD.equals(in.getType())) {
            return app1Package.createAddressFormatAsResult(
                same.getAddress()
            );
        } else throw new IllegalArgumentException(
            "name format not supported. Supported are [" + STANDARD + "]"
        );
    }

    /**
     * Send message
     * 
     * @param in the method's input structure
     * 
     * @return the method's result structure
     * 
     * @see org.openmdx.test.app1.cci2.EmailAddress#sendMessage(org.openmdx.test.app1.cci2.EmailAddressSendMessageParams)
     */
    public Void sendMessage(
        EmailAddressSendMessageParams in
    ) {
        EmailAddress same = sameObject();
        System.out.println("sending message " + in.getText() + " to " + same.refMofId());
        return null;
    }

    /**
     * Send message with template
     * 
     * @param in the method's input structure
     * 
     * @return the method's result structure
     * 
     * @see org.openmdx.test.app1.cci2.EmailAddress#sendMessageTemplate(org.openmdx.test.app1.cci2.EmailAddressSendMessageTemplateParams)
     */
    public EmailAddressSendMessageTemplateResult sendMessageTemplate(
        EmailAddressSendMessageTemplateParams in
     ) {
        EmailAddress same = sameObject();
        System.out.println("sending message " + in.getBody().getText() + " with template to " + same.refMofId());
        App1Package app1Package = samePackage();
        return app1Package.createEmailAddressSendMessageTemplateResult(
            (MessageTemplate) in.getBody()
        );
    }

}
