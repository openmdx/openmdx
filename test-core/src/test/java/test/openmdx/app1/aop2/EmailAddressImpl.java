/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: E-Mail Address  
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import test.openmdx.app1.jmi1.AddressFormatAsResult;
import test.openmdx.app1.jmi1.App1Package;
import test.openmdx.app1.jmi1.EmailAddress;
import test.openmdx.app1.jmi1.EmailAddressSendMessageTemplateResult;
import test.openmdx.app1.jmi1.MessageTemplate;

/**
 * E-Mail Address
 */
public class EmailAddressImpl extends AddressImpl<test.openmdx.app1.jmi1.EmailAddress,test.openmdx.app1.cci2.EmailAddress> {

    /**
     * Constructor 
     *
     * @param same the same layer JMI API
     * @param next the next layer CCI API
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
     */
    @Override
    public AddressFormatAsResult formatAs(
            #if CLASSIC_CHRONO_TYPES
            test.openmdx.app1.jmi1.AddressFormatAsParams in
            #else
            String type
            #endif
    ) {
            #if CLASSIC_CHRONO_TYPES
            String type = in.getType();
            #endif
        EmailAddress same = sameObject();
        App1Package app1Package = (App1Package) same.refImmediatePackage();
        if(STANDARD.equals(type)) {
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
     * @return the method's result structure
     */
    public Void sendMessage(
            #if CLASSIC_CHRONO_TYPES
            test.openmdx.app1.jmi1.EmailAddressSendMessageParams in
            #else
            String text
            #endif
    ) {
        #if CLASSIC_CHRONO_TYPES
        String text = in.getText();
        #endif
        EmailAddress same = sameObject();
        System.out.println("sending message " + text + " to " + same.refMofId());
        return null;
    }

    /**
     * Send message with template
     *
     * @return the method's result structure
     */
    public EmailAddressSendMessageTemplateResult sendMessageTemplate(
            #if CLASSIC_CHRONO_TYPES
            test.openmdx.app1.jmi1.EmailAddressSendMessageTemplateParams in
            #else
            MessageTemplate body
            #endif
     ) {
        #if CLASSIC_CHRONO_TYPES
        MessageTemplate body = in.getBody();
        #endif
        SysLog.detail("sending message " + body.getText() + " with template to " + sameObject().refMofId());
        return this.<App1Package>samePackage().createEmailAddressSendMessageTemplateResult(body);
    }

}
