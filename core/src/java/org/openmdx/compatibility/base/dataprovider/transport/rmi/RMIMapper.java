/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RMIMapper.java,v 1.23 2008/12/15 11:35:46 hburger Exp $
 * Description: RMIMapper class
 * Revision:    $Revision: 1.23 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 11:35:46 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.transport.rmi;

import java.io.File;
import java.rmi.Remote;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.stream.rmi.spi.StreamSynchronization_1_1;
import org.openmdx.base.transaction.Synchronization_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.transport.rmi.spi.DataproviderObjectInterceptor;
import org.openmdx.kernel.application.container.lightweight.LightweightContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Marshalls remote streams and service exceptions
 * <p>
 * The protocoll is selected by the presence of system properties:<ul>
 * <li>"weblogic.Name" &rarr; WebLogic RMI
 * <li>"was.install.root" &rarr; IIOP
 * <li>default &rarr; JRMP
 * </ul>
 */
public class RMIMapper {
    
    private RMIMapper(){
        // Avoid instantiation
    }
    
    /**
     * Assume WebLogc platform if this property is set
     */
    protected static final String WEBLOGIC_PORPERTY = "weblogic.Name";

    /**
     * Assume WebSpehre platform if this property is set
     */
    protected static final String WEBSPHERE_PORPERTY = "was.install.root";
    
    /**
     * Assume Sun Java System Application Server platform if this property is set
     */
    protected static final String JSAS_PORPERTY = "com.sun.aas.domainName";

    /**
     * Use a specific RMI interceptor
     */
    private static final DataproviderObjectInterceptor interceptor = System.getProperty(WEBLOGIC_PORPERTY) != null ? 
        (DataproviderObjectInterceptor) new org.openmdx.compatibility.base.dataprovider.transport.rmi.weblogic.StreamMarshaller() :
      System.getProperty(WEBSPHERE_PORPERTY) != null ?
        (DataproviderObjectInterceptor) new org.openmdx.compatibility.base.dataprovider.transport.rmi.websphere.StreamMarshaller() :
      System.getProperty(JSAS_PORPERTY) != null ?
        (DataproviderObjectInterceptor) new org.openmdx.compatibility.base.dataprovider.transport.rmi.jsas.StreamMarshaller() :
      LightweightContainer.getMode() == LightweightContainer.Mode.ENTERPRISE_JAVA_BEAN_CONTAINER ||
      LightweightContainer.getMode() == LightweightContainer.Mode.ENTERPRISE_APPLICATION_CONTAINER ? // TODO replace by local call detection
        (DataproviderObjectInterceptor) new org.openmdx.compatibility.base.dataprovider.transport.rmi.inprocess.StreamMarshaller() :
        (DataproviderObjectInterceptor) new org.openmdx.compatibility.base.dataprovider.transport.rmi.standard.StreamMarshaller();
    
    /**
     * Logger instance
     */
    private static Logger logger = LoggerFactory.getLogger(RMIMapper.class);    
        
    static {
        logger.info(
            "Stream interceptor = {}", 
            interceptor == null ? "null" : interceptor.getClass().getName()
        );
    }
    
    
    //------------------------------------------------------------------------
    // Streaming
    //------------------------------------------------------------------------

    /**
     * Marshal requests
     *
     * @param   unitsOfWork a collection of requests
     *
     * @return  the modifed collection of requests
     *
     * @exception   ServiceException    MEDIA_ACCESS
     *              if RMI transport is unavailable
     */
    public static UnitOfWorkRequest[] marshal(
        UnitOfWorkRequest[] unitsOfWork
    ) throws ServiceException {
        for(
            int i = 0;
            i < unitsOfWork.length;
            i++
        ){
            DataproviderRequest[] requests = unitsOfWork[i].getRequests();
            for(
                int j = 0;
                j < requests.length;
                j++
            ) RMIMapper.interceptor.marshal(requests[j].object());
        }
        return unitsOfWork;
    }

    /**
     * Unmarshal requests
     *
     * @param   unitsOfWork a collection of requests
     *
     * @return  the modifed collection of requests
     */
    public static UnitOfWorkRequest[] unmarshal(
        UnitOfWorkRequest[] unitsOfWork
    ) {
        for(
            int i = 0;
            i < unitsOfWork.length;
            i++
        ){
            DataproviderRequest[] requests = unitsOfWork[i].getRequests();
            for(
                int j = 0;
                j < requests.length;
                j++
            ) RMIMapper.interceptor.unmarshal(requests[j].object());
        }
        return unitsOfWork;
    }

    /**
     * Marshal replies
     *
     * @param   unitsOfWork a collection of replies
     *
     * @return  the modifed collection of replies
     */
    public static UnitOfWorkReply[] marshal(
        UnitOfWorkReply[] unitsOfWork
    ){
        for(
            int i = 0;
            i < unitsOfWork.length;
            i++
        ) try {
            UnitOfWorkReply unitOfWork = unitsOfWork[i];
            if (! unitOfWork.failure()) {
                DataproviderReply[] replies = unitOfWork.getReplies();
                for(
                    int j = 0;
                    j < replies.length;
                    j++
                ){
                    DataproviderObject[] objects = replies[j].getObjects();
                    for(
                        int k = 0;
                        k < objects.length;
                        k++
                    ) RMIMapper.interceptor.marshal(objects[k]);
                }
            }
        } catch (ServiceException exception) {
            unitsOfWork[i] = new UnitOfWorkReply(exception);
        }
        return unitsOfWork;
    }

    /**
     * Unmarshal replies
     *
     * @param   unitsOfWork a collection of replies
     *
     * @return  the modifed collection of replies
     */
    public static UnitOfWorkReply[] unmarshal(
        UnitOfWorkReply[] unitsOfWork
    ) {
        for(
            int i = 0;
            i < unitsOfWork.length;
            i++
        ){
            UnitOfWorkReply unitOfWork = unitsOfWork[i];
            if (! unitOfWork.failure()) {
                DataproviderReply[] replies = unitOfWork.getReplies();
                for(
                    int j = 0;
                    j < replies.length;
                    j++
                ){
                    DataproviderObject[] objects = replies[j].getObjects();
                    for(
                        int k = 0;
                        k < objects.length;
                        k++
                    ) RMIMapper.interceptor.unmarshal(objects[k]);
                }
            }
        }
        return unitsOfWork;
    }       

    /**
     * Intercept requests
     * <p>
     * Buffers streams in a temporary file if transactionBoundary is 
     * <code>true</code>. If the steamBufferDirectory argument is 
     * <code>null</code> then the system-dependent default temporary-file 
     * directory will be used. The default temporary-file directory is 
     * specified by the system property <code>java.io.tmpdir</code>. On UNIX 
     * systems the default value of this property is typically 
     * <code>"/tmp"</code> or <code>"/var/tmp"</code>; on Microsoft Windows 
     * systems it is typically <code>"c:\\temp"</code>. A different value may 
     * be given to this system property when the Java virtual machine is 
     * invoked, but programmatic changes to this property are not guaranteed 
     * to have any effect upon the the temporary directory used by this method. 
     *
     * @param unitsOfWork a collection of requests
     * @param steamBufferDirectory to buffer the stream in case of a transaction
     * boundary, defaults to the 
     * @param chunkSize 
     * @param transactionBoundary 
     * @return  the modifed collection of requests
     * @throws ServiceException in case of failure
     */
    public static Synchronization_1_0 intercept(
        UnitOfWorkRequest[] unitsOfWork, 
        String unitOfWorkId, 
        File steamBufferDirectory, 
        int chunkSize
    ) throws ServiceException {
        StreamSynchronization_1_1 synchronization = unitOfWorkId == null ? null : new StreamSynchronization_1(
            unitOfWorkId,
            steamBufferDirectory,
            chunkSize
        );
        for(
            int i = 0;
            i < unitsOfWork.length;
            i++
        ){
            DataproviderRequest[] requests = unitsOfWork[i].getRequests();
            for(
                int j = 0;
                j < requests.length;
                j++
            ) RMIMapper.interceptor.intercept(
                requests[j].object(),
                synchronization
            );
        }
        return synchronization;
    }

    /**
     * Intercept requests
     *
     * @param   unitsOfWork a collection of requests
     *
     * @return  the modifed collection of requests
     * @throws ServiceException 
     */
    public static UnitOfWorkRequest[] intercept(
        UnitOfWorkRequest[] unitsOfWork
    ) throws ServiceException {
        for(
            int i = 0;
            i < unitsOfWork.length;
            i++
        ){
            DataproviderRequest[] requests = unitsOfWork[i].getRequests();
            for(
                int j = 0;
                j < requests.length;
                j++
            ) RMIMapper.interceptor.intercept(requests[j].object());
        }
        return unitsOfWork;
    }

    /**
     * Intercept replies
     *
     * @param   unitsOfWork a collection of replies
     *
     * @return  the modifed collection of replies
     */
    public static UnitOfWorkReply[] intercept(
        UnitOfWorkReply[] unitsOfWork
    ) {
        for(
            int i = 0;
            i < unitsOfWork.length;
            i++
        ) try {
            UnitOfWorkReply unitOfWork = unitsOfWork[i];
            if (! unitOfWork.failure()) {
                DataproviderReply[] replies = unitOfWork.getReplies();
                for(
                    int j = 0;
                    j < replies.length;
                    j++
                ){
                    DataproviderObject[] objects = replies[j].getObjects();
                    for(
                        int k = 0;
                        k < objects.length;
                        k++
                    ) RMIMapper.interceptor.intercept(objects[k]);
                }
            }
        } catch (ServiceException exception) {
            unitsOfWork[i] = new UnitOfWorkReply(exception);
        }
        return unitsOfWork;
    }       

    /**
     * Unmarshals remote values
     * 
     * @param value
     * 
     * @return an appropriate proxy object for the given value
     */
    public static Object unmarshal(
        Remote value
    ){
        return RMIMapper.interceptor.unmarshal(value);
    }

}
