/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UIDFactory.java,v 1.7 2008/09/10 08:55:31 hburger Exp $
 * Description: Profile Context
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:31 $
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
package org.openmdx.compatibility.base.net;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.rmi.server.UID;
import java.util.WeakHashMap;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.text.StringBuilders;


/**
 * The UIDfactory generates globally unique IDs
 *
 * @deprecated use org.openmdx.kernel.id.UUIDs#getGenerator() 
 */
@SuppressWarnings("unchecked")
public class UIDFactory
{

    /**
     * Returns a new globally unique ID
     *
     * @return a new UID
     * 
     * @deprecated use org.openmdx.kerne.id.UUIDs.getProvider().next().toString() instead
     */
    public static String create()
    {
        UIDFactory factory;
        synchronized(factories){
            final Thread thread = Thread.currentThread();
            factory = (UIDFactory)factories.get(thread);
            if(factory == null) factories.put(
                thread,
                factory = new UIDFactory()
            );
        }
        return factory.internalCreate();
    }

    /**
     * Returns a new globally unique ID
     *
     * @return a new UID
     */
    private String internalCreate()
    {
        try {
            // Flush and reset the streams for reuse
            outStream.flush();
            byteOutStream.reset();

            UID id = new UID();                                 // create a UID

            id.write(outStream);                                // stream the UID
            byteOutStream.write(hostAddr);                      // add the host address
            byte[] uidOpaque = byteOutStream.toByteArray();     // get the byte array
            BigInteger bigInt = new BigInteger(1, uidOpaque);   // convert it to an integer

            CharSequence   renderedUID = StringBuilders.newStringBuilder();
            String         javaUID     = bigInt.toString(10);   // render the UID

            // Prefix with UUID_PREFIX_JAVA_RMI from common.idl
            StringBuilders.asStringBuilder(renderedUID).append(UIDPrefixes.JAVA_RMI);

            // Pad with leading 0s, decremented by 1 because UUID_PREFIX_JAVA_RMI
            // is a single character.
            for(int ii=javaUID.length(); ii<(UID_RENDER_LEN-1); ii++) {
                StringBuilders.asStringBuilder(renderedUID).append(PADDING_CHAR);
            }

            // Append the rendered UID 
            StringBuilders.asStringBuilder(renderedUID).append(javaUID);

            return renderedUID.toString(); 

        }catch(java.io.IOException ex) {
            // This is a critical error condition.
            // Don't throw a ServiceException that can be caught by application code
            BasicException assertionFailure = new BasicException(
                ex,
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.ASSERTION_FAILURE,
                "UIDFactory: Failure during UID creation"            );
            SysLog.error(
                assertionFailure.getMessage(), 
                assertionFailure.getCause()
            );
            throw new Error(assertionFailure.getMessage());
        }
    }


    /**
     * Initialize the UID Generator
     */
    protected UIDFactory()
    {
        try {
            byteOutStream = new ByteArrayOutputStream();
            outStream     = new DataOutputStream(byteOutStream);

            if(hostAddr == null) synchronized(UIDFactory.class) {
                hostAddr = InetAddress.getByName("localhost").getAddress();
            }
        }catch(Exception ex) {
            // This is a critical error condition.
            // Don't throw a ServiceException that can be caught by application code
            BasicException assertionFailure = new BasicException(
                ex,
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.ASSERTION_FAILURE,
                "UIDFactory: Failure during initialization"
            );
            SysLog.error(
                assertionFailure.getMessage(), 
                assertionFailure.getCause()
            );
            throw new Error(assertionFailure.getMessage());
        }
    }

    /**
     * The stream objects that convert UUIDs
     */
    private final ByteArrayOutputStream    byteOutStream;
    private final DataOutputStream         outStream;

    /**
     * The host address that makes the UIDs global
     */
    private static byte                     hostAddr[]    = null;

    /**
     * UID_RENDER_LEN defines the length of UID. MUST BE an even number
     * to support the barcode rendering on the barcode scan frontpages.
     */
    private final static int                UID_RENDER_LEN= 46;
    private final static char               PADDING_CHAR  = '0';

    /**
     * Keep track of a single UIDFactory per thread.
     */
    private final static WeakHashMap        factories     = new WeakHashMap();

}









