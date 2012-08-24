/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: E-Mail Address  
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2010, OMEX AG, Switzerland
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
package test.openmdx.app1.aop2;

import org.openmdx.base.jmi1.Void;
import org.openmdx.kernel.log.SysLog;

import test.openmdx.app1.jmi1.AddressFormatAsParams;
import test.openmdx.app1.jmi1.AddressFormatAsResult;
import test.openmdx.app1.jmi1.App1Package;
import test.openmdx.app1.jmi1.EmailAddress;
import test.openmdx.app1.jmi1.EmailAddressSendMessageParams;
import test.openmdx.app1.jmi1.EmailAddressSendMessageTemplateParams;
import test.openmdx.app1.jmi1.EmailAddressSendMessageTemplateResult;

/**
 * E-Mail Address
 */
public class EmailAddressImpl extends AddressImpl<test.openmdx.app1.jmi1.EmailAddress,test.openmdx.app1.cci2.EmailAddress> {

    /**
     * Constructor 
     *
     * @param same
     * @param next
     */
    public EmailAddressImpl(
        test.openmdx.app1.jmi1.EmailAddress same,
        test.openmdx.app1.cci2.EmailAddress next
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
     * @see test.openmdx.app1.cci2.Address#formatAs(test.openmdx.app1.cci2.AddressFormatAsParams)
     */
    @Override
    public AddressFormatAsResult formatAs(
        AddressFormatAsParams in
    ) {
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
     * @see test.openmdx.app1.cci2.EmailAddress#sendMessage(test.openmdx.app1.cci2.EmailAddressSendMessageParams)
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
     * @see test.openmdx.app1.cci2.EmailAddress#sendMessageTemplate(test.openmdx.app1.cci2.EmailAddressSendMessageTemplateParams)
     */
    public EmailAddressSendMessageTemplateResult sendMessageTemplate(
        EmailAddressSendMessageTemplateParams in
     ) {
        SysLog.detail("sending message " + in.getBody().getText() + " with template to " + sameObject().refMofId());
        return this.<App1Package>samePackage().createEmailAddressSendMessageTemplateResult(
            in.getBody()
        );
    }

}
