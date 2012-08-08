/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1Bean.java,v 1.20 2008/02/19 13:47:40 hburger Exp $
 * Description: Dataprovider_1Bean class
 * Revision:    $Revision: 1.20 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/19 13:47:40 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.application.dataprovider.transport.ejb.to.ejb;

import java.security.Principal;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1ConnectionFactoryImpl;
import org.openmdx.compatibility.base.application.j2ee.SessionBean_1;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.compatibility.base.dataprovider.transport.rmi.RMIMapper;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.text.format.IndentingFormatter;

public class Dataprovider_1Bean 
    extends SessionBean_1 
    implements Dataprovider_1_0
{
    /**
     * 
     */
    private static final long serialVersionUID = 3690760583393851185L;

    /**
     * The delegation object.
     */
    private Dataprovider_1_1Connection connection;

    /**
     * Clear the principal chain before processing the units of work
     */
    private boolean clearPrincipalChain;

    /**
     * Append the principal's id
     */
    private boolean appendPrincipal;
    
    /**
     * Use the principal's name as opposed to the principal's String 
     * representation.
     */
    private boolean usePrincipalName;

    
    //------------------------------------------------------------------------
    // Implements Manageable_1_0
    //------------------------------------------------------------------------

    /**
     * The activate() method is used to initialize the client.
     * <p>
     * An activate() implementation of a subclass must be of the form:
     * <pre>
     *   {
     *     super.activate();
     *     local activation code...
     *   }
     * </pre>
     */
    public void activate(
    ) throws Exception {
        super.activate();
        
        String registrationId = getOptions().getFirstValue("registrationId");

        if (registrationId == null) {
            Hashtable<String,Object> properties = new Hashtable<String,Object>();
            setProperty(properties,Context.PROVIDER_URL,"server-url");
            setProperty(properties,Context.SECURITY_PRINCIPAL,"principal");
            setProperty(properties,Context.SECURITY_CREDENTIALS,"password");
            Context initialContext = new InitialContext(properties);
            try {
                this.connection = Dataprovider_1ConnectionFactoryImpl.createRemoteConnection(
                    initialContext.lookup(
                        getOptions().getFirstValue("jndi-name")
                    )
                );                
            } finally {
                initialContext.close();
            }
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("option", "registrationId"),
                    new BasicException.Parameter("class", "org.openmdx.compatibility.base.application.j2ee.StandardServiceLocator")
                },
                "The formerly deprecated StandardServiceLocator class has been removed"
            );
        }

        this.clearPrincipalChain = !"false".equalsIgnoreCase(getOptions().getFirstValue("clear-principal"));
        this.appendPrincipal = !"false".equalsIgnoreCase(getOptions().getFirstValue("append-principal"));
        this.usePrincipalName = "Name".equalsIgnoreCase(getOptions().getFirstValue("append-principal"));

    }

    private void setProperty(
        Hashtable<String,Object> properties,
        String target,
        String source
    ){
        String value = getOptions().getFirstValue(source);
        if(value != null) properties.put(target, value);
    }
    
    /**
     * Deactivates the dataprovider.
     */
    public void deactivate(
    ) throws Exception {
        this.connection.close();
        super.deactivate();
    }


    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0
    //------------------------------------------------------------------------

    /**
     * Process a set of working units
     *
     * @param   header          the service header
     * @param   workingUnits    a collection of working units
     *
     * @return  a collection of working unit replies
     */
    public UnitOfWorkReply[] process(
        ServiceHeader header,
        UnitOfWorkRequest[] workingUnits
    ){
        try {
            Principal principal = getSessionContext().getCallerPrincipal(); 
            String principalId = this.usePrincipalName ? principal.getName() : principal.toString();
            ServiceHeader serviceHeader = this.clearPrincipalChain ? (
                this.appendPrincipal ? new ServiceHeader(
                    principalId,
                    header.getCorrelationId(),
                    header.traceRequest(),
                    header.getQualityOfService(),
                    header.getRequestedAt(),
                    header.getRequestedFor()
                ) : new ServiceHeader(
                    new String[]{},
                    header.getCorrelationId(),
                    header.traceRequest(),
                    header.getQualityOfService(),
                    header.getRequestedAt(),
                    header.getRequestedFor()
                )                    
             ) : (
                this.appendPrincipal ? new ServiceHeader(
                    concatenate(header.getPrincipalChain(), principalId),
                    header.getCorrelationId(),
                    header.traceRequest(),
                    header.getQualityOfService(),
                    header.getRequestedAt(),
                    header.getRequestedFor()
                ) : header
             );
             SysLog.detail(
                principalId, 
            	new IndentingFormatter(
            		ArraysExtension.asMap(
            			new String[]{
            			    "correlationId",
            			    "principalChain",
            			    "requestedAt",
            			    "requestedFor",
            			    "unitsOfWork"
            			},
            			new Object[]{
            			    header.getCorrelationId(),
            			    header.getPrincipalChain(),
            			    header.getRequestedAt(),
            			    header.getRequestedFor(),
            			    workingUnits
            			}
            		)
            	)
            );
            UnitOfWorkReply[] replies = RMIMapper.intercept(
                this.connection.process(
                    serviceHeader,
                    RMIMapper.intercept(workingUnits)
                )
            );
            SysLog.detail(
                principalId, 
            	new IndentingFormatter(
            		ArraysExtension.asMap(
            			new String[]{
            			    "correlationId",
            			    "principalChain",
            			    "replies"
            			},
            			new Object[]{
            			    serviceHeader.getCorrelationId(),
            			    serviceHeader.getPrincipalChain(),
            			    replies
            			}
            		)
            	)
            );
            return replies;
        } catch (RuntimeException exception) {
            new ServiceException(exception).log();
            throw exception;    
        } catch (ServiceException exception) {
            UnitOfWorkReply[] reply = new UnitOfWorkReply[workingUnits.length];
            Arrays.fill(reply, new UnitOfWorkReply(exception));
            return reply;
        }
    }

    /**
     * Add current principal to principal chain
     * 
     * @param list
     * @param element
     * @return
     */
    private static String[] concatenate(
        List<String> list,
        String element
    ){
        String[] result = new String[list.size() + 1];
        int i = 0;
        for(
            int s=list.size(); 
            i < s; 
            i++
        ){
            result[i] = list.get(i);
        }
        result[i] = element;
        return result;
    }

}
