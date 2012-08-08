/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Preferences_1.java,v 1.5 2009/01/06 13:13:37 wfro Exp $
 * Description: Preferences_1 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/06 13:13:37 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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

package org.openmdx.test.weblogic.security.ssl.layer.application;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.application.dataprovider.spi.Layer_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.security.ExecutionContext;
import org.openmdx.kernel.security.resource.ConnectionFactory;

/**
 * Preferences_1
 *
 */
public class Preferences_1
    extends Layer_1
{

    /**
     * Constructor 
     */
    public Preferences_1() {
    }

    private Context pkiContext;
    private Map<String, Object> pkiProviders;
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#activate(short, org.openmdx.compatibility.base.application.configuration.Configuration, org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0)
     */
    public void activate(
        short id, 
        Configuration configuration, 
        Layer_1_0 delegation
    ) throws Exception, ServiceException {
        super.activate(id, configuration, delegation);
        this.pkiContext = (Context) new InitialContext().lookup("java:comp/env/pki");
        this.pkiProviders = new HashMap<String, Object>();
    }
    
    private final ConnectionFactory getConnectionFactory(
        String segment
    ) throws ServiceException {
        Object connectionFactory;
        if(this.pkiProviders.containsKey(segment)) {
            connectionFactory = this.pkiProviders.get(segment);
        } else try {
            this.pkiProviders.put(
                segment, 
                connectionFactory = pkiContext.lookup(segment)
            );
        } catch (NamingException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("jndiName", "java:comp/env/pki/" + segment)                    
                },
                "Key store connection factory acquisition failed"
            ).log();
        }
        return (ConnectionFactory) connectionFactory;
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#deactivate()
     */
    public void deactivate(
    ) throws Exception, ServiceException {
        this.pkiContext = null;
        this.pkiProviders = null;
        super.deactivate();
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#get(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @SuppressWarnings("unchecked")
    public DataproviderReply get(
        ServiceHeader header, 
        DataproviderRequest request
    ) throws ServiceException {
        if(request.path().isLike(PKI_SEGMENT_PATTERN)) {
            try {
                Object connection = getConnectionFactory(request.path().getBase()).getConnection();
                DataproviderObject segment = new DataproviderObject(
                    request.path(),
                    SEGMENT_INSTANCE_OF[0],
                    Arrays.asList(SEGMENT_INSTANCE_OF),
                    null,
                    null
                );
                segment.values("description").add(connection.getClass().getName());
                return new DataproviderReply(segment);
            } catch (GeneralSecurityException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.MEDIA_ACCESS_FAILURE,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("jndiName", "java:comp/env/pki/" + request.path().getBase())                    
                    },
                    "Key store connection factory acquisition failed"
                ).log();
            }
        } else if (request.path().isLike(PKI_PREFERENCES_PATTERN)) {
            String jndiName = "java:comp/env/pki/" + request.path().get(4);
            try {
                Object connection = getConnectionFactory(request.path().get(4)).getConnection();
                if(connection instanceof ExecutionContext) {
                    DataproviderObject preferences = new DataproviderObject(
                        request.path(),
                        PREFERENCES_INSTANCE_OF[0],
                        Arrays.asList(PREFERENCES_INSTANCE_OF),
                        null,
                        null
                    );
                    ExecutionContext context = (ExecutionContext)connection;
                    preferences.values("absolutePath").add("execution/context");
                    preferences.values("description").add(context.toString());
                    return new DataproviderReply(preferences);
                } else {
                    return super.get(header, request);
                }
            } catch (GeneralSecurityException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.MEDIA_ACCESS_FAILURE,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("jndiName", jndiName)                    
                    },
                    "Key store connection factory acquisition failed"
                ).log();
            }
        } else {
            return super.get(header, request);
        }
    }

    private final Path PKI_SEGMENT_PATTERN = new Path(
        "xri:@openmdx*org:openmdx:preferences1/provider/JKS/segment/:*"
    );
    
    private final static String[] SEGMENT_INSTANCE_OF = new String[]{
        "org:openmdx:preferences1:Segment",
        "org:openmdx:base:Segment",
        "org:openmx:base:ContextCapable",
        "org:openmdx:compatbility:view1:ViewCapable"
    };

    private final Path PKI_PREFERENCES_PATTERN = new Path(
        "xri:@openmdx*org:openmdx:preferences1/provider/JKS/segment/:*/preferences/:*"
    );

    private final static String[] PREFERENCES_INSTANCE_OF = new String[]{
        "org:openmdx:preferences1:Preferences",
        "org:openmdx:generic1:PropertySet"
    };

}
