/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: MarshallingContainer_1.java,v 1.10 2008/02/08 16:50:58 hburger Exp $
 * Description: Marshalling Container
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/08 16:50:58 $
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
package org.openmdx.base.accessor.generic.spi;

import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.collection.MarshallingFilterableMap;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.marshalling.CollectionMarshallerAdapter;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;


/**
 * Marshalling Container
 */
public class MarshallingContainer_1<K,V,M extends FilterableMap<K,?>> 
    extends MarshallingFilterableMap<K,V,M> 
{

    /**
     * Constructor
     * 
     * @param marshaller
     * @param container
     */
    public MarshallingContainer_1(
        Marshaller marshaller,
        M container
    ) throws ServiceException {
        super(marshaller, container);
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3257009873437996080L;

    /**
     * Get the delegate and verifies the marshaller
     * 
     * @param marshaller
     * 
     * @return the delegate map
     * 
     * @exception ServiceException BAD_PARAMETER
     *            If the request specifies a different marshaller
     *            
     */
    M getDelegate(
        Marshaller marshaller
    ) throws ServiceException{
        if(!(super.marshaller instanceof CollectionMarshallerAdapter)) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ASSERTION_FAILURE,
            new BasicException.Parameter[]{
                new BasicException.Parameter("expected",CollectionMarshallerAdapter.class.getName()),
                new BasicException.Parameter("actual",super.marshaller.getClass().getName())
            },
            "Unexpected marshaller"
        );
		Marshaller delegate = ((CollectionMarshallerAdapter)super.marshaller).getDelegate();
        if(delegate != marshaller) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            new BasicException.Parameter[]{
                new BasicException.Parameter("expected",marshaller.getClass().getName()),
                new BasicException.Parameter("actual",delegate.getClass().getName())
            },
            "Delegate marshaller mismatch"
        );
        return super.getDelegate();
    }    
}
