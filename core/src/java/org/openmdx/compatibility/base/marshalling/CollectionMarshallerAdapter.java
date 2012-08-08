/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: CollectionMarshallerAdapter.java,v 1.12 2008/03/06 19:03:23 hburger Exp $
 * Description: SPICE Collections: Merging List
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/06 19:03:23 $
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
package org.openmdx.compatibility.base.marshalling;

import java.beans.ExceptionListener;
import java.io.Serializable;

import org.openmdx.base.exception.BadParameterException;
import org.openmdx.base.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
* This class signals marshal exceptions.
*/
public class CollectionMarshallerAdapter
    implements org.openmdx.base.persistence.spi.Marshaller, Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 3977302131193624624L;

    /**
     * @serial
     */
    protected final Marshaller marshaller;

    /**
     * @serial
     */
    protected final ExceptionListener exceptionListener;

    /**
     * 
     */
    private transient Logger logger;
    
    /**
     * Constructor
     * 
     * @param marshaller
     * @param exceptionListener
     */
    public CollectionMarshallerAdapter(
        Marshaller marshaller,
        ExceptionListener exceptionListener
    ) {
        this.marshaller = marshaller;
        this.exceptionListener = exceptionListener;
    }

    /**
     * Constructor
     * 
     * @param marshaller
     */
    public CollectionMarshallerAdapter(
        Marshaller marshaller
    ) {
        this(marshaller, null);
    }

    /**
     * Marshals an object
     *
     * @param  source    The object to be marshalled
     * 
     * @return           The marshalled object, 
     *                   or null if a ServiceException is caught
     */
    public Object marshal (
        Object source
    ){
        try {
            return this.marshaller.marshal(source);
        } catch (ServiceException exception) {
            if(this.exceptionListener == null){
                if (this.logger == null) this.logger = LoggerFactory.getLogger(
                    CollectionMarshallerAdapter.class
                );
                this.logger.warn(
                    this.marshaller.getClass().getName(),
                    exception
                );
            } else {
                this.exceptionListener.exceptionThrown(exception);
            }
            return null;
        }
    }

    /**
     * Unmarshals an object
     *
     * @param  source   The marshalled object
     * 
     * @return          The unmarshalled object
     * 
     * @exception       ServiceException
     *                  MARSHAL_FAILURE: Object can't be unmarshalled
     */
    public Object unmarshal (
        Object source
    ){
        try {
            return this.marshaller.unmarshal(source);
        } catch (ServiceException exception) {
            throw new BadParameterException(exception);
        }
    }

    /**
     * Accessor for the wrapped marshaller
     * 
     * @return  the wrapped marshaller
     */
    public Marshaller getDelegate(
    ){
        return this.marshaller;
    }

}
